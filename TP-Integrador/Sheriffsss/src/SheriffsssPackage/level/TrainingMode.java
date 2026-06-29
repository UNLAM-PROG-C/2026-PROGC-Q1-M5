package SheriffsssPackage.level;

import SheriffsssPackage.Game;
import SheriffsssPackage.context.GameConfig;
import SheriffsssPackage.context.GameInput;
import SheriffsssPackage.render.TrainingHudSnapshot;
import SheriffsssPackage.render.TrainingHudSnapshotConfig;
import SheriffsssPackage.session.GameMap;
import SheriffsssPackage.session.GameSession;
import SheriffsssPackage.session.Player;
import SheriffsssPackage.system.enemy.EnemyFactory;
import SheriffsssPackage.system.enemy.EnemySystem;
import SheriffsssPackage.system.weapon.ProjectileSystem;

import java.util.Arrays;
import java.util.List;
import java.util.Random;


/**
 * Orquestador del modo entrenamiento. Coordina 7 sistemas:
 *   1. TrainingArenaBuilder - construcción de mapa
 *   2. TargetSpawnController - spawn y control de enemigos
 *   3. TrainingScoreTracker - scoring, precisión, proyectiles
 *   4. TrainingSessionTimer - temporizador de sesión
 *   5. TrainingTutorialController - flujo de tutorial y hints
 *   6. TrainingEndScreenHandler - manejo de pantallas finales
 *   7. TrainingConfigStore - persistencia de controles
 *
 * TrainingMode mantiene ~300-400 líneas y delega responsabilidades específicas
 * a cada subsistema. El update() sincroniza el flujo general del juego.
 */
public final class TrainingMode {
  // --- Arena constants ---
  private static final int ARENA_TILES_WIDE = GameConfig.TRAINING_ARENA_TILES_WIDE;
  private static final int ARENA_TILES_HIGH = GameConfig.TRAINING_ARENA_TILES_HIGH;
  private static final int INTERIOR_TILES = GameConfig.TRAINING_INTERIOR_FENCE_TILES;
  private static final int INTERIOR_ORIGIN_X = (ARENA_TILES_WIDE - INTERIOR_TILES) / 2;
  private static final int INTERIOR_ORIGIN_Y = (ARENA_TILES_HIGH - INTERIOR_TILES) / 2;

  // --- HUD data constants ---
  private static final String AIM_HINT_TEXT = "Apunta con el mouse y dispara con click";
  private static final String TARGET_HINT_TEXT = "Disparale a las dianas para acumular puntos";
  private static final String TIMER_NOTICE_TEXT = "Tenes 60 segundos antes de que termine tu entrenamiento";
  private static final float HINT_START_ALPHA = 1.0f;
  private static final float HINT_ALPHA_FADE_STEP = 0.035f;
  private static final int HINT_BASELINE_BELOW_OUTER_FENCE_BOTTOM = GameConfig.TILE_SIZE + 28;
  private static final int HINT_BOTTOM_SCREEN_MARGIN = 18;

  // --- Tutorial join timeout ---
  private static final long TUTORIAL_JOIN_TIMEOUT_MS = 200L;

  // === Core subsystems ===
  private final Game game;
  private GameSession session;
  private final EnemySystem enemySystem;
  private final EnemyFactory enemyFactory = new EnemyFactory();
  private final TrainingControls controls = new TrainingControls();

  // === Delegated systems ===
  private final TrainingArenaBuilder arenaBuilder;
  private TargetSpawnController spawnController;
  private TrainingScoreTracker scoreTracker;
  private TrainingSessionTimer sessionTimer;
  private TrainingTutorialController tutorialController;
  private TrainingEndScreenHandler endScreenHandler;
  private final TrainingConfigStore configStore;
  private final TutorialThread tutorialThread;

  // === State ===
  private GameMap arena;
  private int lastProjectileCount;
  private boolean shotFired;
  private boolean debugUsed;

  public TrainingMode(Game game, EnemySystem enemySystem, int sessionSeedHash) {
    this.game = game;
    this.session = null; // Will be set later via initializeSystems()
    this.enemySystem = enemySystem;
    this.tutorialThread = new TutorialThread(buildSteps());

    // Initialize config store first to load saved controls
    this.configStore = new TrainingConfigStore(this.controls);
    this.configStore.loadControls();

    // Initialize core subsystems (can be done immediately)
    this.arenaBuilder = new TrainingArenaBuilder();

    // Placeholder initializations for systems that need GameSession
    this.spawnController = null;
    this.scoreTracker = null;
    this.sessionTimer = null;
    this.tutorialController = null;
    this.endScreenHandler = null;

    // State initialization
    this.lastProjectileCount = 0;
    this.shotFired = false;
    this.debugUsed = false;
  }

  /**
   * Initialize remaining systems after GameSession is available.
   * Call this after buildArena() and before start().
   */
  public void initializeSystems(GameSession session, ProjectileSystem projectileSystem,
      GameInput input) {
    if (this.spawnController != null) {
      return; // Already initialized
    }

    this.session = session;

    // Initialize remaining subsystems
    this.spawnController = new TargetSpawnController(session, this.enemySystem, this.enemyFactory,
        this.arena, this.controls);
    this.scoreTracker = new TrainingScoreTracker(
        this.game, session, this.enemySystem, projectileSystem, this.arena, null);
    this.sessionTimer = new TrainingSessionTimer(GameConfig.TARGET_FPS * 60,
        this.game != null ? this.game.getAudio() : null);
    this.tutorialController = new TrainingTutorialController(null);
    this.endScreenHandler = new TrainingEndScreenHandler(input, this.controls, this.game,
        this.tutorialThread, this::resetArena);
  }

  // === Lifecycle ===

  public GameMap buildArena() {
    GameMap map = new GameMap(ARENA_TILES_WIDE, ARENA_TILES_HIGH);
    this.arenaBuilder.buildArena(map, ARENA_TILES_WIDE, new Random(this.session.hashCode()));
    this.arena = map;
    return map;
  }

  public void start() {
    this.spawnController.spawnInitialEnemies();
  }

  public void shutdown() {
    this.tutorialThread.skip();
    try {
      this.tutorialThread.join(TUTORIAL_JOIN_TIMEOUT_MS);
    } catch (InterruptedException ignored) {
      Thread.currentThread().interrupt();
    }
    this.configStore.saveControls();
  }

  public void resetArena() {
    this.spawnController.resetArena();
  }

  // === Update tick ===

  public void update(Player player, GameInput input, ProjectileSystem projectileSystem) {
    markDebugUsedIfNeeded();

    if (this.sessionTimer.isSessionFinished()) {
      this.endScreenHandler.handleFinalInput();
      return;
    }

    if (player != null && player.isDead()) {
      this.endScreenHandler.handleDeadInput();
      return;
    }

    // Delegate to subsystems
    this.endScreenHandler.readPanelInput();
    this.tutorialController.update();

    if (isWaitingForFirstShot()) {
      return;
    }

    // Score and projectile tracking
    this.scoreTracker.updateProjectileFailures(projectileSystem);
    this.sessionTimer.update(this.session);

    // Spawn and target lifecycle
    int replacementCount = this.scoreTracker.awardScoreForDestroyedTargets();
    replacementCount += this.scoreTracker.damageTargetsOverTime();
    replacementCount += this.scoreTracker.removeDeadTargets();
    replacementCount += this.scoreTracker.removeExpiredTargets();
    this.spawnController.syncEnemyCountToControls(replacementCount);
  }

  public void notifyShotFired() {
    this.shotFired = true;
  }

  public boolean isSessionFinished() {
    return this.sessionTimer.isSessionFinished();
  }

  public boolean isWaitingForFirstShot() {
    return this.tutorialController.getPhase() == TutorialPhase.AIM;
  }

  public TrainingHudSnapshot hudSnapshot(Player player) {
    TrainingHudSnapshotConfig config = new TrainingHudSnapshotConfig()
      .withStats(
        this.scoreTracker.getFallos(),
        this.scoreTracker.getAciertos(),
        this.scoreTracker.precisionText())
      .withTimer(timerText(), this.tutorialController.getPhase() == TutorialPhase.NORMAL)
      .withHint(
        this.tutorialController.hintAlpha(),
        currentHintText(),
        outerFenceScreenCenterX(),
        trainingPromptBaselineY())
      .withEndState(
        player != null && player.isDead(),
        this.sessionTimer.isSessionFinished(),
        this.scoreTracker.finalScore(),
        this.debugUsed)
      .withFailurePerimeter(
        getFailurePerimeterLeftWorldX(),
        getFailurePerimeterTopWorldY(),
        getFailurePerimeterWidthWorld(),
        getFailurePerimeterHeightWorld());
    return new TrainingHudSnapshot(config);
  }

  private void markDebugUsedIfNeeded() {
    if (this.game != null && this.game.getDebugOptions() != null
        && this.game.getDebugOptions().hasAnyModeEnabled()) {
      this.debugUsed = true;
    }
  }

  private String timerText() {
    int seconds = Math.max(0,
      (this.sessionTimer.getSessionTimer() + GameConfig.TARGET_FPS - 1) / GameConfig.TARGET_FPS);
    return (seconds / 60) + ":" + (seconds % 60 < 10 ? "0" : "") + (seconds % 60);
  }

  private int outerFenceScreenCenterX() {
    int interiorCenterTileX = INTERIOR_ORIGIN_X + INTERIOR_TILES / 2;
    return tileToWorldCenter(interiorCenterTileX) - this.game.getCameraCenterWorldX()
      + GameConfig.SCREEN_CENTER_X;
  }

  private int outerFenceBottomScreenY() {
    int bottomFenceWorldY = (INTERIOR_ORIGIN_Y + INTERIOR_TILES + 1) * GameConfig.TILE_SIZE;
    return bottomFenceWorldY - this.game.getCameraCenterWorldY() + GameConfig.SCREEN_CENTER_Y;
  }

  private int trainingPromptBaselineY() {
    return Math.min(
      outerFenceBottomScreenY() + HINT_BASELINE_BELOW_OUTER_FENCE_BOTTOM,
      GameConfig.SCREEN_HEIGHT - HINT_BOTTOM_SCREEN_MARGIN);
  }

  private static int tileToWorldCenter(int tile) {
    return tile * GameConfig.TILE_SIZE + GameConfig.TILE_SIZE / 2;
  }

  private String currentHintText() {
    TutorialPhase phase = this.tutorialController.getPhase();
    if (phase == TutorialPhase.TIMER_NOTICE) {
      return TIMER_NOTICE_TEXT;
    }
    if (phase == TutorialPhase.AIM) {
      return AIM_HINT_TEXT;
    }
    if (phase == TutorialPhase.TARGETS) {
      return TARGET_HINT_TEXT;
    }
    return "";
  }

  // === Public state accessors ===

  public int getPlayerSpawnWorldX() {
    return this.arenaBuilder.getPlayerSpawnWorldX();
  }

  public int getPlayerSpawnWorldY() {
    return this.arenaBuilder.getPlayerSpawnWorldY();
  }

  public int getFailurePerimeterLeftWorldX() {
    return INTERIOR_ORIGIN_X * GameConfig.TILE_SIZE;
  }

  public int getFailurePerimeterTopWorldY() {
    return INTERIOR_ORIGIN_Y * GameConfig.TILE_SIZE;
  }

  public int getFailurePerimeterWidthWorld() {
    return INTERIOR_TILES * GameConfig.TILE_SIZE;
  }

  public int getFailurePerimeterHeightWorld() {
    return INTERIOR_TILES * GameConfig.TILE_SIZE;
  }

  // === Tutorial steps ===

  private static List<TutorialStep> buildSteps() {
    return Arrays.asList(
      new TutorialStep(
        TutorialEventType.FIRST_MOVEMENT,
        600L,
        15000L),
      new TutorialStep(
        TutorialEventType.FIRST_SHOT,
        600L,
        20000L),
      new TutorialStep(
        TutorialEventType.FIRST_KILL,
        600L,
        30000L)
    );
  }
}
