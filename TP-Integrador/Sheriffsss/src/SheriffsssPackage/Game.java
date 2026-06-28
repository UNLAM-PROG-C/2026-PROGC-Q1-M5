package SheriffsssPackage;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.HashSet;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import SheriffsssPackage.context.GameContext;
import SheriffsssPackage.level.LevelType;
import SheriffsssPackage.render.EquipmentHudView;
import SheriffsssPackage.render.TrainingHudView;
import SheriffsssPackage.session.GameSession;
import SheriffsssPackage.system.MusicController;
import SheriffsssPackage.system.PlayerMovementSystem;
import SheriffsssPackage.system.ShotFeedback;
import SheriffsssPackage.system.WeaponUseResult;
import SheriffsssPackage.system.WeaponUseSystem;
import SheriffsssPackage.ui.CursorController;
import SheriffsssPackage.ui.DisplaySettingsChange;
import SheriffsssPackage.ui.DisplaySettingsController;
import SheriffsssPackage.ui.EquipmentMenuController;
import SheriffsssPackage.ui.EquipmentMenuLayout;

public class Game extends JPanel implements Runnable, GameView {
  private static final long serialVersionUID = 1L;
  private static final String DEFAULT_WEAPON_ATTACK_SOUND = "sounds/Shot.wav";
  private static final float WEAPON_GAIN_DB = 0f;
  private static final float ENEMY_HIT_GAIN_DB = 0f;
  private static final int INFO_MESSAGE_SLOTS = 4;
  private final AssetManager assets;
  private final AudioManager audio;
  private final GameInput input;
  private final MenuRenderer menuRenderer;
  private final GameRenderer renderer;
  private final DayNightCycle dayNightCycle = new DayNightCycle();
  private final EnemySystem enemySystem = new EnemySystem();
  private final ProjectileSystem projectileSystem = new ProjectileSystem();
  private final DebugOptions debugOptions = new DebugOptions();
  private final GameSession session = new GameSession();
  private final GameContext context;
  private final TrainingSessionBuilder trainingSessionBuilder = new TrainingSessionBuilder();
  private final PlayerMovementSystem playerMovementSystem = new PlayerMovementSystem();
  private final String[] infoMessages = new String[INFO_MESSAGE_SLOTS];
  private final int[] infoMessageTicks = new int[INFO_MESSAGE_SLOTS];
  private final HashSet<String> unavailableSfxPaths = new HashSet<String>();

  private volatile Thread gameThread;
  private State state = State.MENU;
  private boolean usingTool;
  private boolean blockPrimaryGameplayUntilRelease;
  private boolean primaryGameplayPressedThisFrame;
  private MapObject toolTargetObject;
  private int toolUseTicks;
  private int toolUseDurationTicks = 1;
  private ItemDefinition toolAnimationDefinition;
  private int toolAnimationTicksRemaining;
  private boolean activeDebugTrajectorySlider;
  private boolean debugPanelPrimaryHeld;
  private long frameCount;
  private JFrame window;
  private int lastViewportWidth = GameConfig.BASE_SCREEN_WIDTH;
  private int lastViewportHeight = GameConfig.BASE_SCREEN_HEIGHT;
  private volatile boolean shuttingDown;
  private final ShotFeedback shotFeedback = new ShotFeedback();
  private final WeaponUseSystem weaponUseSystem = new WeaponUseSystem();
  private final MusicController musicController = new MusicController();
  private final CursorController cursorController = new CursorController();
  private final DisplaySettingsController displaySettingsController = new DisplaySettingsController();
  private final EquipmentMenuLayout equipmentMenuLayout = new EquipmentMenuLayout();
  private final EquipmentMenuController equipmentMenuController =
    new EquipmentMenuController(this.equipmentMenuLayout);
  private final EquipmentHudView equipmentHudView = new EquipmentHudView();
  private final TrainingHudView trainingHudView = new TrainingHudView();

  public Game() {
    GameConfig.loadDisplayPreferences();
    this.assets = new AssetManager();
    this.audio = new AudioManager();
    this.displaySettingsController.loadPreferences(this.audio);
    this.input = new GameInput();
    this.menuRenderer = new MenuRenderer(this.assets);
    this.renderer = new GameRenderer(this.assets, this.menuRenderer);
    this.context = new GameContext(this.assets, this.audio, this.input,
      this.dayNightCycle, this.enemySystem, this.projectileSystem,
      this.debugOptions);

    setPreferredSize(this.displaySettingsController.windowedSize());
    setDoubleBuffered(true);
    setFocusable(true);
    setFocusTraversalKeysEnabled(false);
    setLayout(null);

    addKeyListener(this.input);
    addMouseListener(this.input);
    addMouseWheelListener(this.input);
    addMouseMotionListener(this.input);

    setCursor(this.assets.getCursor(CursorType.IDLE));
  }

  public void startGame() {
    if (this.shuttingDown || this.gameThread != null) {
      return;
    }
    this.gameThread = new Thread(this, "SheriffsssGameLoop");
    this.gameThread.start();
    requestFocusInWindow();
  }

  public void setWindow(JFrame window) {
    this.window = window;
  }

  public void applyInitialDisplaySettings() {
    applyFullscreen(this.displaySettingsController.fullscreen());
  }

  @Override
  protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    syncViewportSize();
    this.renderer.render((Graphics2D) g, this);
  }

  private void syncViewportSize() {
    int viewportWidth = getWidth() > 0 ? getWidth() : GameConfig.BASE_SCREEN_WIDTH;
    int viewportHeight = getHeight() > 0 ? getHeight() : GameConfig.BASE_SCREEN_HEIGHT;
    if (viewportWidth == this.lastViewportWidth && viewportHeight == this.lastViewportHeight) {
      return;
    }
    GameConfig.setViewportSize(viewportWidth, viewportHeight);
    this.lastViewportWidth = viewportWidth;
    this.lastViewportHeight = viewportHeight;
  }

  private void updateFullscreenToggle() {
    if (!this.input.consumeFullscreenToggle()) {
      return;
    }
    setFullscreen(!this.displaySettingsController.fullscreen());
  }

  private void setFullscreen(boolean fullscreen) {
    this.displaySettingsController.applyImmediateFullscreen(fullscreen);
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        applyFullscreen(fullscreen);
      }
    });
  }

  private void applyFullscreen(boolean fullscreen) {
    JFrame targetWindow = this.window;
    if (targetWindow == null) {
      targetWindow = (JFrame) SwingUtilities.getWindowAncestor(this);
      this.window = targetWindow;
    }
    if (targetWindow == null) {
      return;
    }
    boolean wasVisible = targetWindow.isVisible();
    if (targetWindow.isDisplayable()) {
      targetWindow.dispose();
    }
    targetWindow.setUndecorated(fullscreen);
    targetWindow.setResizable(false);
    if (fullscreen) {
      targetWindow.pack();
      targetWindow.setExtendedState(JFrame.MAXIMIZED_BOTH);
    } else {
      targetWindow.setExtendedState(JFrame.NORMAL);
      setPreferredSize(this.displaySettingsController.windowedSize());
      targetWindow.pack();
      targetWindow.setLocationRelativeTo(null);
    }
    if (wasVisible) {
      targetWindow.setVisible(true);
    }
    targetWindow.revalidate();
    targetWindow.repaint();
    refreshViewportAfterWindowChange();
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        refreshViewportAfterWindowChange();
      }
    });
  }

  private void applyWindowedResolution() {
    JFrame targetWindow = this.window;
    if (targetWindow == null) {
      targetWindow = (JFrame) SwingUtilities.getWindowAncestor(this);
      this.window = targetWindow;
    }
    if (targetWindow == null) {
      return;
    }
    setPreferredSize(this.displaySettingsController.windowedSize());
    targetWindow.pack();
    targetWindow.setLocationRelativeTo(null);
    targetWindow.revalidate();
    targetWindow.repaint();
    refreshViewportAfterWindowChange();
  }

  private void refreshViewportAfterWindowChange() {
    syncViewportSize();
    revalidate();
    repaint();
    requestFocusInWindow();
  }

  @Override
  public void run() {
    double drawInterval = 1000000000.0 / GameConfig.TARGET_FPS;
    double delta = 0.0;
    long lastTime = System.nanoTime();

    while (!this.shuttingDown && Thread.currentThread() == this.gameThread) {
      long currentTime = System.nanoTime();
      delta += (currentTime - lastTime) / drawInterval;
      lastTime = currentTime;

      while (delta >= 1.0) {
        updateGame();
        repaint();
        this.frameCount++;
        delta--;
      }
    }
  }

  private void updateGame() {
    if (this.shuttingDown) {
      return;
    }
    syncViewportSize();
    updateFullscreenToggle();
    updateDebugMenuInput();
    if (this.state != State.PLAYING
        || this.session.player() == null
        || this.session.player().isDead()) {
      this.input.consumeZoomWheelSteps();
      this.input.consumeZoomKeySteps();
    }
    this.shotFeedback.update();
    if (this.state == State.MENU) {
      updateMenu();
    } else if (this.state == State.MENU_SETTINGS) {
      updateMenuSettings();
    } else if (this.state == State.PLAYING) {
      updatePlaying();
    } else if (this.state == State.SETTINGS) {
      updateSettings();
    }
    if (isTrainingLevelActive() && this.session.trainingMode() != null
      && this.state == State.PLAYING
      && this.session.player() != null) {
      this.session.trainingMode().update(this.session.player(), this.input, this.projectileSystem);
    }
    updateInfoMessages();
    updateMusic();
    updateCursor();
  }

  private void updateDebugMenuInput() {
    if (!isTrainingLevelActive()) {
      this.debugOptions.setMenuOpen(false);
      this.activeDebugTrajectorySlider = false;
      this.debugPanelPrimaryHeld = this.input.isPrimaryHeld();
      return;
    }
    if (!this.debugOptions.isMenuOpen()) {
      this.activeDebugTrajectorySlider = false;
      this.debugPanelPrimaryHeld = this.input.isPrimaryHeld();
      return;
    }
    if (!this.input.isPrimaryHeld()) {
      this.activeDebugTrajectorySlider = false;
      this.debugPanelPrimaryHeld = false;
      return;
    }
    if (this.activeDebugTrajectorySlider) {
      this.debugOptions.setBulletTrajectoryLimitFromMouse(this.input.getMouseX());
      this.input.consumePrimaryClick();
      return;
    }
    if (!this.debugPanelPrimaryHeld) {
      boolean handled = this.debugOptions.handleClick(this.input.getMouseX(), this.input.getMouseY());
      this.debugPanelPrimaryHeld = true;
      if (handled) {
        equipRequestedTrainingDebugWeapon();
        this.input.consumePrimaryClick();
        this.activeDebugTrajectorySlider = this.debugOptions.isTrajectorySliderHovered(
          this.input.getMouseX(),
          this.input.getMouseY());
      }
    }
  }

  private void equipRequestedTrainingDebugWeapon() {
    if (!this.debugOptions.consumeUnlockAllWeaponsRequest()
        || this.session.player() == null
        || !isTrainingLevelActive()) {
      return;
    }
    this.trainingSessionBuilder.applyTrainingDebugLoadout(this.context, this.session.player());
    if (this.debugOptions.shouldUnlockAllWeapons()) {
      this.session.player().getEquipment().openMenu();
    }
  }

  private void updateMenu() {
    if (!this.input.consumePrimaryClick()) {
      return;
    }
    if (this.menuRenderer.isExitButtonHovered(this.input.getMouseX(), this.input.getMouseY())) {
      shutdownApplication();
      return;
    }
    if (this.menuRenderer.isTrainingButtonHovered(this.input.getMouseX(), this.input.getMouseY())) {
      startTraining(true);
      return;
    }
    if (this.menuRenderer.isTrainingSettingsButtonHovered(this.input.getMouseX(), this.input.getMouseY())) {
      openMenuSettings();
      return;
    }
  }


  private void startTraining() {
    startTraining(false);
  }

  private void startTraining(boolean resetDebugOptions) {
    stopTrainingIfActive();
    this.trainingSessionBuilder.build(this, this.context, this.session, resetDebugOptions);
    resetTrainingTransientState();
    this.state = State.PLAYING;
    this.input.consumeEscapePressed();
    this.input.clearPrimaryAction();
    this.session.trainingMode().start();
  }

  private void resetTrainingTransientState() {
    this.projectileSystem.clear();
    resetLocalToolAnimation();
    this.toolTargetObject = null;
  }

  private void stopTrainingIfActive() {
    if (this.session.trainingMode() != null) {
      this.session.trainingMode().shutdown();
      this.session.setTrainingMode(null);
    }
    this.session.setActiveLevel(null);
  }

  private boolean isTrainingSessionFinished() {
    return isTrainingLevelActive() && this.session.trainingMode() != null
      && this.session.trainingMode().isSessionFinished();
  }

  private boolean isTrainingWaitingForFirstShot() {
    return isTrainingLevelActive() && this.session.trainingMode() != null
      && this.session.trainingMode().isWaitingForFirstShot();
  }

  private boolean isTrainingLevelActive() {
    return this.session.activeLevel().type() == LevelType.TRAINING;
  }

  public void restartTraining() {
    startTraining();
  }

  public void exitTrainingToMenu() {
    stopTrainingIfActive();
    this.session.setPlayer(null);
    this.session.setMap(null);
    this.projectileSystem.clear();
    this.state = State.MENU;
    this.input.clearMovement();
  }

  private void updatePlaying() {
    if (isTrainingSessionFinished()) {
      clearToolTarget();
      resetLocalToolAnimation();
      return;
    }
    if (this.input.consumeEscapePressed()) {
      beginDisplaySettingsEdit();
      this.state = State.SETTINGS;
      this.displaySettingsController.clearMessage();
      clearToolTarget();
      return;
    }
    if (this.input.consumeEquipmentToggle()) {
      if (this.session.player() != null) {
        this.session.player().getEquipment().toggleMenu();
      }
      clearToolTarget();
      resetLocalToolAnimation();
      return;
    }
    this.input.consumeMapToggle();
    updateCameraZoomInput();

    this.primaryGameplayPressedThisFrame = false;
    boolean primaryPressed = consumePrimaryGameplayClick();
    if (primaryPressed && handleEquipmentClick(this.input.getMouseX(), this.input.getMouseY())) {
      primaryPressed = false;
      this.blockPrimaryGameplayUntilRelease = this.input.isPrimaryHeld();
    }
    this.primaryGameplayPressedThisFrame = primaryPressed;
    this.input.consumeSecondaryClick();
    this.input.consumeToolbarSelection();
    this.input.consumeWheelSteps();

    this.input.consumeInteractPressed();

    int moveX = this.input.getMoveX();
    int moveY = this.input.getMoveY();
    if (isTrainingWaitingForFirstShot()) {
      moveX = 0;
      moveY = 0;
    }
    this.session.player().setTakingDamage(false);
    this.playerMovementSystem.update(
      this.session.map(),
      this.session.player(),
      moveX,
      moveY,
      this.shotFeedback.canUpdateFacing());
    this.dayNightCycle.tick();
    this.enemySystem.update(this.session.map(), this.session.player());

    if (this.session.player().getCurrentHP() <= 0.0) {
      this.session.player().die();
      returnToMenu();
      return;
    }
    updateToolUse();
    updateProjectiles();
    this.primaryGameplayPressedThisFrame = false;
  }

  private void updateToolUse() {
    this.weaponUseSystem.updateProjectileWeaponCooldown(this.session.playerRuntime());
    this.usingTool = false;
    ItemDefinition selectedDefinition = this.session.player().getEquipment().getEquippedWeapon();
    if (this.toolAnimationDefinition != null) {
      if (selectedDefinition == this.toolAnimationDefinition) {
        tickLocalToolAnimation(this.toolAnimationDefinition);
      } else {
        resetLocalToolAnimation();
      }
    }
    boolean primaryActive = isPrimaryGameplayHeld() || this.primaryGameplayPressedThisFrame;
    if (!primaryActive) {
      clearToolTarget();
      return;
    }
    if (selectedDefinition == null || !selectedDefinition.isProjectileWeapon()) {
      clearToolTarget();
      return;
    }
    WeaponUseResult weaponUseResult = this.weaponUseSystem.fireProjectileWeapon(
      this.projectileSystem,
      this.session.player(),
      this.session.playerRuntime(),
      selectedDefinition,
      screenToWorldX(this.input.getMouseX()),
      screenToWorldY(this.input.getMouseY()));
    if (weaponUseResult.fired()) {
      handleWeaponUseResult(weaponUseResult);
      startLocalToolAnimation(selectedDefinition);
    }
    clearToolTarget();
  }

  private boolean handleEquipmentClick(int mouseX, int mouseY) {
    if (this.session.player() == null) {
      return false;
    }
    return this.equipmentMenuController.handleClick(this.session.player().getEquipment(), mouseX, mouseY);
  }

  private void startLocalToolAnimation(ItemDefinition definition) {
    int baseDurationTicks = definition.getUseAnimationTicks();
    int durationTicks = this.session.player() == null
      ? baseDurationTicks
      : this.session.player().applyAttackSpeedToCooldown(baseDurationTicks);
    durationTicks = Math.max(1, durationTicks);
    if (this.toolAnimationTicksRemaining <= 0 || this.toolAnimationDefinition != definition) {
      this.toolUseTicks = 0;
      this.toolAnimationTicksRemaining = durationTicks;
      this.toolAnimationDefinition = definition;
    }
    this.usingTool = true;
    this.toolUseDurationTicks = durationTicks;
  }

  private void tickLocalToolAnimation(ItemDefinition definition) {
    if (this.toolAnimationTicksRemaining <= 0 || this.toolAnimationDefinition != definition) {
      resetLocalToolAnimation();
      return;
    }
    this.usingTool = true;
    this.toolUseDurationTicks = Math.max(1, this.toolUseDurationTicks);
    this.toolUseTicks++;
    this.toolAnimationTicksRemaining--;
    if (this.toolAnimationTicksRemaining <= 0) {
      this.toolAnimationDefinition = null;
    }
  }

  private void resetLocalToolAnimation() {
    this.usingTool = false;
    this.toolUseTicks = 0;
    this.toolUseDurationTicks = 1;
    this.toolAnimationTicksRemaining = 0;
    this.toolAnimationDefinition = null;
  }

  private void handleWeaponUseResult(WeaponUseResult result) {
    this.shotFeedback.lockFacing();
    if (isTrainingLevelActive()) {
      this.debugOptions.recordBulletTrajectory(
        result.startWorldX(),
        result.startWorldY(),
        result.aimWorldX(),
        result.aimWorldY());
    }
    playProjectileWeaponEffects(result);
    if (isTrainingLevelActive() && this.session.trainingMode() != null) {
      this.session.trainingMode().notifyShotFired();
    }
  }

  public int heldItemOriginWorldX(Player sourcePlayer, ItemDefinition definition, Facing facing) {
    return this.weaponUseSystem.heldItemOriginWorldX(sourcePlayer, definition, facing);
  }

  public int heldItemOriginWorldY(Player sourcePlayer, ItemDefinition definition, Facing facing) {
    return this.weaponUseSystem.heldItemOriginWorldY(sourcePlayer, definition, facing);
  }

  private void playProjectileWeaponEffects(WeaponUseResult result) {
    ItemDefinition weapon = result.weapon();
    ProjectileType type = result.projectileType();
    String fireSoundPath = weapon == null ? "" : weapon.getAttackSoundPath();
    if (fireSoundPath != null && !fireSoundPath.isEmpty()) {
      String fallbackSoundPath = weapon != null && weapon.getWeaponType() == WeaponType.ARMA_DE_FUEGO
        ? DEFAULT_WEAPON_ATTACK_SOUND
        : "";
      playPersistentSpatialSfxOrFallback(
        fireSoundPath,
        fallbackSoundPath,
        WEAPON_GAIN_DB,
        result.startWorldX(),
        result.startWorldY());
    }
    if (type != null && type.triggersMuzzleFlash()) {
      this.shotFeedback.triggerMuzzleFlash(result.startWorldX(), result.startWorldY());
    }
  }

  private void playPersistentSpatialSfxOrFallback(
      String resourcePath,
      String fallbackResourcePath,
      float gainDb,
      int sourceWorldX,
      int sourceWorldY) {
    if (resourcePath == null || resourcePath.isEmpty()) {
      return;
    }
    if (this.unavailableSfxPaths.contains(resourcePath)) {
      playFallbackPersistentSpatialSfx(resourcePath, fallbackResourcePath, gainDb, sourceWorldX, sourceWorldY);
      return;
    }
    try {
      playPersistentSpatialSfx(resourcePath, gainDb, sourceWorldX, sourceWorldY);
    } catch (IllegalStateException e) {
      this.unavailableSfxPaths.add(resourcePath);
      System.err.println("Unable to play audio resource " + resourcePath + ": " + e.getMessage());
      playFallbackPersistentSpatialSfx(resourcePath, fallbackResourcePath, gainDb, sourceWorldX, sourceWorldY);
    }
  }

  private void playFallbackPersistentSpatialSfx(
      String failedResourcePath,
      String fallbackResourcePath,
      float gainDb,
      int sourceWorldX,
      int sourceWorldY) {
    if (fallbackResourcePath == null
        || fallbackResourcePath.isEmpty()
        || fallbackResourcePath.equals(failedResourcePath)) {
      return;
    }
    if (this.unavailableSfxPaths.contains(fallbackResourcePath)) {
      return;
    }
    try {
      playPersistentSpatialSfx(fallbackResourcePath, gainDb, sourceWorldX, sourceWorldY);
    } catch (IllegalStateException e) {
      this.unavailableSfxPaths.add(fallbackResourcePath);
      System.err.println("Unable to play fallback audio resource " + fallbackResourcePath + ": " + e.getMessage());
    }
  }

  private void playPersistentSpatialSfx(String resourcePath, float gainDb, int sourceWorldX, int sourceWorldY) {
    double volumeScale = spatialSfxVolumeScale(sourceWorldX, sourceWorldY);
    if (volumeScale > 0.0) {
      this.audio.playOnceUntilFinished(resourcePath, gainDb, volumeScale);
    }
  }

  private double spatialSfxVolumeScale(int sourceWorldX, int sourceWorldY) {
    if (this.session.player() == null || this.session.player().isDead()) {
      return 1.0;
    }
    int listenerX = this.session.player().getX();
    int listenerY = this.session.player().getY();
    int deltaX = sourceWorldX - listenerX;
    int deltaY = sourceWorldY - listenerY;
    int fullRadius = GameConfig.SPATIAL_SFX_FULL_VOLUME_RADIUS_PIXELS;
    int audibleRadius = GameConfig.SPATIAL_SFX_AUDIBLE_RADIUS_PIXELS;
    int distanceSquared = deltaX * deltaX + deltaY * deltaY;
    if (distanceSquared <= fullRadius * fullRadius) {
      return 1.0;
    }
    if (distanceSquared >= audibleRadius * audibleRadius || audibleRadius <= fullRadius) {
      return 0.0;
    }
    double distance = Math.sqrt(distanceSquared);
    return Math.max(0.0, Math.min(1.0, 1.0 - (distance - fullRadius) / (audibleRadius - fullRadius)));
  }

  private void updateProjectiles() {
    this.projectileSystem.update(this.session.map(), this.enemySystem);
    if (this.projectileSystem.didHitTargetThisUpdate()) {
      this.shotFeedback.triggerHitMarker();
    }
    playEnemyHitSounds();
    this.enemySystem.collectDeadEnemies();
  }

  private void playEnemyHitSounds() {
    List<EnemyHitSound> hitSounds = this.enemySystem.getHitSounds();
    for (int i = 0; i < hitSounds.size(); i++) {
      EnemyHitSound hitSound = hitSounds.get(i);
      playPersistentSpatialSfxOrFallback(
        hitSound.getResourcePath(),
        "",
        ENEMY_HIT_GAIN_DB,
        hitSound.getWorldX(),
        hitSound.getWorldY());
    }
    this.enemySystem.clearHitSounds();
  }

  private void clearToolTarget() {
    if (this.toolTargetObject != null) {
      this.toolTargetObject.resetDurabilityDamage();
      this.toolTargetObject = null;
    }
  }

  private void updateSettings() {
    if (this.input.consumeEscapePressed()) {
      resumeFromSettings(false);
      return;
    }
    boolean primaryPressed = this.input.consumePrimaryClick();
    if (primaryPressed) {
      this.displaySettingsController.selectHoveredSlider(
        isMusicSliderHovered(),
        isSfxSliderHovered(),
        isResolutionSliderHovered());
    }
    if (!this.input.isPrimaryHeld()) {
      this.displaySettingsController.clearActiveSlider();
    }
    if (this.displaySettingsController.updateActiveSlider(this.audio, sliderValueFromMouse())) {
      return;
    }
    if (!primaryPressed) {
      return;
    }
    if (isTrainingLevelActive() && isDebugSettingsButtonHovered()) {
      this.debugOptions.toggleMenu();
      return;
    }
    if (isFullscreenButtonHovered()) {
      this.displaySettingsController.togglePendingFullscreen();
      return;
    }
    if (isResumeButtonHovered()) {
      resumeFromSettings(true);
      return;
    }
    if (isExitToMenuButtonHovered()) {
      applyPendingDisplaySettings();
      returnToMenu();
      return;
    }
    if (isQuitButtonHovered()) {
      shutdownApplication();
    }
  }

  private void updateMenuSettings() {
    if (this.input.consumeEscapePressed()) {
      closeMenuSettings();
      return;
    }
    boolean primaryPressed = this.input.consumePrimaryClick();
    if (primaryPressed) {
      this.displaySettingsController.selectHoveredSlider(
        isMusicSliderHovered(),
        isSfxSliderHovered(),
        isResolutionSliderHovered());
    }
    if (!this.input.isPrimaryHeld()) {
      this.displaySettingsController.clearActiveSlider();
    }
    if (this.displaySettingsController.updateActiveSlider(this.audio, sliderValueFromMouse())) {
      return;
    }
    if (primaryPressed
        && this.menuRenderer.isMenuFullscreenButtonHovered(this.input.getMouseX(), this.input.getMouseY())) {
      this.displaySettingsController.togglePendingFullscreen();
      return;
    }
    if (primaryPressed && this.menuRenderer.isMenuBackButtonHovered(this.input.getMouseX(), this.input.getMouseY())) {
      closeMenuSettings();
    }
  }

  private boolean isMusicSliderHovered() {
    return isPointInside(
      settingsSliderInteractionX(),
      settingsSliderInteractionY(GameConfig.SETTINGS_MUSIC_SLIDER_Y) - 10,
      GameConfig.SETTINGS_SLIDER_WIDTH,
      24);
  }

  private boolean isSfxSliderHovered() {
    return isPointInside(
      settingsSliderInteractionX(),
      settingsSliderInteractionY(GameConfig.SETTINGS_SFX_SLIDER_Y) - 10,
      GameConfig.SETTINGS_SLIDER_WIDTH,
      24);
  }

  private boolean isResolutionSliderHovered() {
    return isPointInside(
      settingsSliderInteractionX(),
      settingsSliderInteractionY(GameConfig.SETTINGS_RESOLUTION_SLIDER_Y) - 10,
      GameConfig.SETTINGS_SLIDER_WIDTH,
      24);
  }

  private boolean isFullscreenButtonHovered() {
    return isSettingsButtonHovered(GameConfig.SETTINGS_FULLSCREEN_BUTTON_Y);
  }

  private boolean isDebugSettingsButtonHovered() {
    return isTrainingLevelActive()
      && isSettingsButtonHovered(GameConfig.SETTINGS_DEBUG_BUTTON_Y);
  }

  private int settingsDebugButtonX() {
    return GameConfig.SETTINGS_BUTTON_X;
  }

  private boolean isResumeButtonHovered() {
    return isSettingsButtonHovered(GameConfig.SETTINGS_RESUME_BUTTON_Y);
  }

  private boolean isExitToMenuButtonHovered() {
    return isSettingsButtonHovered(GameConfig.SETTINGS_MENU_BUTTON_Y);
  }

  private boolean isQuitButtonHovered() {
    return isSettingsButtonHovered(GameConfig.SETTINGS_QUIT_BUTTON_Y);
  }

  private boolean isSettingsButtonHovered(int baseY) {
    return isPointInside(
      GameConfig.SETTINGS_BUTTON_X,
      settingsOverlayY(baseY),
      GameConfig.SETTINGS_BUTTON_WIDTH,
      GameConfig.SETTINGS_BUTTON_HEIGHT);
  }

  private boolean isPointInside(int x, int y, int width, int height) {
    return this.input.getMouseX() >= x && this.input.getMouseX() <= x + width
      && this.input.getMouseY() >= y && this.input.getMouseY() <= y + height;
  }

  private double sliderValueFromMouse() {
    double value = (this.input.getMouseX() - settingsSliderInteractionX())
      / (double) GameConfig.SETTINGS_SLIDER_WIDTH;
    return Math.max(0.0, Math.min(1.0, value));
  }

  private int settingsSliderInteractionX() {
    if (this.state == State.MENU_SETTINGS) {
      return this.menuRenderer.getMenuOffsetX() + GameConfig.BASE_SCREEN_WIDTH / 2 - 90;
    }
    return GameConfig.SETTINGS_SLIDER_X;
  }

  private int settingsSliderInteractionY(int baseY) {
    if (this.state == State.MENU_SETTINGS) {
      return this.menuRenderer.getMenuOffsetY() + baseY;
    }
    return settingsOverlayY(baseY);
  }

  private int settingsOverlayY(int baseY) {
    return baseY + settingsOverlayOffsetY();
  }

  private int settingsOverlayOffsetY() {
    return isTrainingLevelActive() && this.state == State.SETTINGS
      ? GameConfig.TRAINING_SETTINGS_OVERLAY_Y_OFFSET
      : 0;
  }

  private int screenToWorldX(int screenX) {
    return getCameraCenterWorldX()
      + (int) Math.round((screenX - GameConfig.SCREEN_CENTER_X) / this.session.cameraZoom());
  }

  private int screenToWorldY(int screenY) {
    return getCameraCenterWorldY()
      + (int) Math.round((screenY - GameConfig.SCREEN_CENTER_Y) / this.session.cameraZoom());
  }

  private void updateCameraZoomInput() {
    int wheelSteps = this.input.consumeZoomWheelSteps();
    int keySteps = this.input.consumeZoomKeySteps();
    int heldDirection = this.input.getZoomKeyDirection();
    if ((wheelSteps == 0 && keySteps == 0 && heldDirection == 0)
        || this.session.player() == null
        || this.session.player().isDead()) {
      return;
    }
    setCameraZoom(this.session.cameraZoom()
      + keySteps * GameConfig.CAMERA_ZOOM_HELD_STEP
      + heldDirection * GameConfig.CAMERA_ZOOM_HELD_STEP
      - wheelSteps * GameConfig.CAMERA_ZOOM_STEP);
  }

  private void setCameraZoom(double cameraZoom) {
    this.session.setCameraZoom(Math.max(GameConfig.CAMERA_MIN_ZOOM, Math.min(GameConfig.CAMERA_MAX_ZOOM, cameraZoom)));
  }

  private void returnToMenu() {
    this.musicController.stop(this.audio);
    this.audio.stopSfxLoop();
    stopTrainingIfActive();
    this.enemySystem.clear();
    this.projectileSystem.clear();
    this.session.setMap(null);
    this.session.setPlayer(null);
    resetLocalToolAnimation();
    this.toolTargetObject = null;
    this.displaySettingsController.clearActiveSlider();
    this.dayNightCycle.reset();
    this.state = State.MENU;
  }

  private void openMenuSettings() {
    this.displaySettingsController.clearMessage();
    this.displaySettingsController.clearActiveSlider();
    beginDisplaySettingsEdit();
    this.state = State.MENU_SETTINGS;
  }

  private void closeMenuSettings() {
    applyPendingDisplaySettings();
    this.displaySettingsController.clearMessage();
    this.displaySettingsController.clearActiveSlider();
    this.state = State.MENU;
  }

  public synchronized void shutdown() {
    if (this.shuttingDown) {
      return;
    }
    this.shuttingDown = true;
    this.gameThread = null;
    stopTrainingIfActive();
    this.audio.stopSfxLoop();
    this.audio.shutdown();
    this.musicController.clear();
    GameConfig.saveDisplayPreferences();
  }

  private void shutdownApplication() {
    shutdown();
    System.exit(0);
  }

  private void updateMusic() {
    if (this.shuttingDown) {
      return;
    }
    this.musicController.update(
      this.audio,
      this.state,
      this.session.player());
  }

  private void updateCursor() {
    CursorType cursorType = this.cursorController.resolve(
      this.state,
      this.shotFeedback.hasHitMarker(),
      isRootMenuButtonHovered(),
      isMenuSettingsControlHovered(),
      isGameSettingsControlHovered());
    setCursor(this.assets.getCursor(cursorType));
  }

  private boolean isMenuSettingsControlHovered() {
    return this.menuRenderer.isMenuBackButtonHovered(this.input.getMouseX(), this.input.getMouseY())
      || isMusicSliderHovered()
      || isSfxSliderHovered()
      || isResolutionSliderHovered()
      || this.menuRenderer.isMenuFullscreenButtonHovered(this.input.getMouseX(), this.input.getMouseY());
  }

  private boolean isGameSettingsControlHovered() {
    return isMusicSliderHovered()
      || isSfxSliderHovered()
      || isResolutionSliderHovered()
      || (isTrainingLevelActive() && isDebugSettingsButtonHovered())
      || isFullscreenButtonHovered()
      || isResumeButtonHovered()
      || isExitToMenuButtonHovered()
      || isQuitButtonHovered();
  }

  public long getFrameCount() {
    return this.frameCount;
  }

  public DayNightCycle getDayNightCycle() {
    return this.dayNightCycle;
  }

  private void updateInfoMessages() {
    for (int i = 0; i < INFO_MESSAGE_SLOTS; i++) {
      if (this.infoMessageTicks[i] > 0) {
        this.infoMessageTicks[i]--;
      }
    }
  }

  private boolean consumePrimaryGameplayClick() {
    boolean clicked = this.input.consumePrimaryClick();
    if (!this.blockPrimaryGameplayUntilRelease) {
      return clicked;
    }
    if (!this.input.isPrimaryHeld()) {
      this.blockPrimaryGameplayUntilRelease = false;
    }
    return false;
  }

  private boolean isPrimaryGameplayHeld() {
    if (!this.blockPrimaryGameplayUntilRelease) {
      return this.input.isPrimaryHeld();
    }
    if (!this.input.isPrimaryHeld()) {
      this.blockPrimaryGameplayUntilRelease = false;
    }
    return false;
  }

  private void resumeFromSettings(boolean blockPrimaryGameplay) {
    applyPendingDisplaySettings();
    this.state = State.PLAYING;
    this.displaySettingsController.clearMessage();
    this.displaySettingsController.clearActiveSlider();
    if (blockPrimaryGameplay && this.input.isPrimaryHeld()) {
      this.blockPrimaryGameplayUntilRelease = true;
    }
  }

  private void beginDisplaySettingsEdit() {
    this.displaySettingsController.beginEdit();
  }

  private void applyPendingDisplaySettings() {
    DisplaySettingsChange change = this.displaySettingsController.applyPending();
    if (change.fullscreenChanged()) {
      final boolean targetFullscreen = change.targetFullscreen();
      SwingUtilities.invokeLater(new Runnable() { @Override public void run() { applyFullscreen(targetFullscreen); } });
    } else if (!change.targetFullscreen() && change.resolutionChanged()) {
      SwingUtilities.invokeLater(new Runnable() { @Override public void run() { applyWindowedResolution(); } });
    }
  }

  public AssetManager getAssets() { return this.assets; }
  public AudioManager getAudio() { return this.audio; }
  public GameInput getInput() { return this.input; }
  public DebugOptions getDebugOptions() { return this.debugOptions; }
  public State getState() { return this.state; }
  public GameMap getMap() { return this.session.map(); }
  public Player getPlayer() { return this.session.player(); }
  public int getCameraCenterWorldX() {
    return this.session.player() == null
      ? GameConfig.SCREEN_CENTER_X
      : this.session.player().getX();
  }

  public int getCameraCenterWorldY() {
    return this.session.player() == null
      ? GameConfig.SCREEN_CENTER_Y
      : this.session.player().getY();
  }
  public double getCameraZoom() { return this.session.cameraZoom(); }
  public boolean isSpectating() { return false; }
  public List<Enemy> getEnemies() { return this.enemySystem.getEnemies(); }
  public List<Projectile> getProjectiles() { return this.projectileSystem.getProjectiles(); }
  public List<FlameBurstEffect> getFlameBurstEffects() { return this.enemySystem.getFlameBurstEffects(); }
  public List<CombatFloatingText> getCombatFloatingTexts() { return this.enemySystem.getCombatFloatingTexts(); }
  public boolean isUsingTool() { return this.usingTool; }
  public int getToolUseTicks() { return this.toolUseTicks; }
  public int getToolUseDurationTicks() { return this.toolUseDurationTicks; }
  public MapObject getToolTargetObject() { return this.toolTargetObject; }
  public int getRevolverFlashTicks() { return this.shotFeedback.muzzleFlashTicks(); }
  public int getRevolverFlashWorldX() { return this.shotFeedback.muzzleFlashWorldX(); }
  public int getRevolverFlashWorldY() { return this.shotFeedback.muzzleFlashWorldY(); }
  public int getRevolverFlashRadiusTiles() { return this.shotFeedback.muzzleFlashRadiusTiles(); }
  public double getRevolverFlashIntensity() { return this.shotFeedback.muzzleFlashIntensity(); }
  public double getEquippedPlayerAccuracy() {
    if (this.session.player() == null) {
      return 1.0;
    }
    ItemDefinition weapon = this.session.player().getEquipment().getEquippedWeapon();
    return this.weaponUseSystem.resolveEffectiveAccuracy(
      this.session.player(),
      this.session.player(),
      weapon);
  }

  public EquipmentHudView getEquipmentHudView() {
    boolean visible = this.session.player() != null
      && !this.session.player().isDead()
      && this.state != State.SETTINGS;
    Equipment equipment = this.session.player() == null ? null : this.session.player().getEquipment();
    this.equipmentHudView.update(equipment, this.equipmentMenuLayout, visible);
    return this.equipmentHudView;
  }
  public int getInfoMessageSlotCount() { return INFO_MESSAGE_SLOTS; }
  public String getInfoMessage(int index) { return this.infoMessages[index]; }
  public int getInfoMessageTicks(int index) { return this.infoMessageTicks[index]; }
  public String getSettingsMessage() { return this.displaySettingsController.message(); }
  public boolean isPendingFullscreen() { return this.displaySettingsController.pendingFullscreen(); }
  public boolean isDebugMenuOpen() { return this.debugOptions.isMenuOpen(); }
  public boolean isSettingsDebugButtonHovered() { return isDebugSettingsButtonHovered(); }
  public int getSettingsDebugButtonX() { return settingsDebugButtonX(); }
  public int getSettingsDebugButtonY() { return settingsOverlayY(GameConfig.SETTINGS_DEBUG_BUTTON_Y); }
  public int getSettingsDebugButtonWidth() { return GameConfig.SETTINGS_BUTTON_WIDTH; }
  public int getSettingsDebugButtonHeight() { return GameConfig.SETTINGS_BUTTON_HEIGHT; }
  public int getSettingsOverlayOffsetY() { return settingsOverlayOffsetY(); }
  public double getWindowResolutionSliderValue() {
    return this.displaySettingsController.windowResolutionSliderValue();
  }
  public String getWindowResolutionLabel() { return this.displaySettingsController.windowResolutionLabel(); }
  public TrainingHudView getTrainingHudView() {
    TrainingMode trainingMode = this.session.trainingMode();
    this.trainingHudView.update(
      isTrainingLevelActive(),
      trainingMode == null ? null : trainingMode.hudSnapshot(this.session.player()));
    return this.trainingHudView;
  }

  private boolean isRootMenuButtonHovered() {
    int mouseX = this.input.getMouseX();
    int mouseY = this.input.getMouseY();
    if (this.menuRenderer.isExitButtonHovered(mouseX, mouseY)) {
      return true;
    }
    return this.menuRenderer.isTrainingButtonHovered(mouseX, mouseY)
      || this.menuRenderer.isTrainingSettingsButtonHovered(mouseX, mouseY);
  }

}
