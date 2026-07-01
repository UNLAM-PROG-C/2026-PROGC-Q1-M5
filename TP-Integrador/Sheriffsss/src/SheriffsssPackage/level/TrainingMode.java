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

import java.util.Random;


public final class TrainingMode
{
  // --- Arena constants ---
  private static final int ARENA_TILES_WIDE = GameConfig.TRAINING_ARENA_TILES_WIDE;
  private static final int ARENA_TILES_HIGH = GameConfig.TRAINING_ARENA_TILES_HIGH;
  private static final int INTERIOR_TILES = GameConfig.TRAINING_INTERIOR_FENCE_TILES;
  private static final int INTERIOR_ORIGIN_X = (ARENA_TILES_WIDE - INTERIOR_TILES) / 2;
  private static final int INTERIOR_ORIGIN_Y = (ARENA_TILES_HIGH - INTERIOR_TILES) / 2;

  // --- HUD / hint constants ---
  private static final String AIM_HINT_TEXT = "Apunta con el mouse y dispara con click";
  private static final String TARGET_HINT_TEXT = "Disparale a las dianas para acumular puntos";
  private static final String TIMER_NOTICE_TEXT = "Tenes 60 segundos antes de que termine tu entrenamiento";
  private static final float HINT_START_ALPHA = 1.0f;
  private static final float HINT_SHOT_ALPHA_STEP = 0.2f;
  private static final float HINT_ALPHA_FADE_STEP = 0.035f;
  private static final int HINT_BASELINE_BELOW_OUTER_FENCE_BOTTOM = GameConfig.TILE_SIZE + 28;
  private static final int HINT_BOTTOM_SCREEN_MARGIN = 18;
  private static final int TARGET_HINT_MIN_TICKS = GameConfig.TARGET_FPS * 3;
  private static final int TIMER_NOTICE_TICKS = GameConfig.TARGET_FPS * 5;

  private static final int SESSION_DURATION_SECONDS = 60;

  // === Core subsystems ===
  private final Game game;
  private GameSession session;
  private final EnemySystem enemySystem;
  private final EnemyFactory enemyFactory = new EnemyFactory();
  private final TrainingControls controls = new TrainingControls();
  private final int sessionSeedHash;

  // === Delegated systems ===
  private final TrainingArenaBuilder arenaBuilder;
  private TargetSpawnController spawnController;
  private TrainingScoreTracker scoreTracker;
  private TrainingSessionTimer sessionTimer;
  private TrainingTutorialController tutorialController;
  private TrainingEndScreenHandler endScreenHandler;
  private final TrainingConfigStore configStore;

  // === Tutorial / hint state ===
  private int lastProjectileCount;
  private boolean shotFired;
  private int targetHintTicks;
  private int hintFadeShotSteps;
  private float displayedHintAlpha = HINT_START_ALPHA;
  private boolean targetHintHit;

  // === Misc state ===
  private GameMap arena;
  private boolean debugUsed;

  public TrainingMode(Game game, EnemySystem enemySystem, int sessionSeedHash)
  {
    this.game = game;
    this.session = null;
    this.enemySystem = enemySystem;
    this.sessionSeedHash = sessionSeedHash;

    this.configStore = new TrainingConfigStore(this.controls);
    this.configStore.loadControls();

    this.arenaBuilder = new TrainingArenaBuilder();

    this.spawnController = null;
    this.scoreTracker = null;
    this.sessionTimer = null;
    this.tutorialController = null;
    this.endScreenHandler = null;

    this.lastProjectileCount = 0;
    this.shotFired = false;
    this.targetHintTicks = 0;
    this.hintFadeShotSteps = 0;
    this.displayedHintAlpha = HINT_START_ALPHA;
    this.targetHintHit = false;
    this.debugUsed = false;
  }

  public void initializeSystems(GameSession session, ProjectileSystem projectileSystem, GameInput input)
  {
    if (this.spawnController != null)
    {
      return;
    }

    this.session = session;

    this.spawnController = new TargetSpawnController(session, this.enemySystem, this.enemyFactory,
        this.arena, this.controls);
    this.scoreTracker = new TrainingScoreTracker(
        this.game, session, this.enemySystem, projectileSystem, this.arena,
        this.game != null ? this.game.getShotFeedback() : null);
    this.sessionTimer = new TrainingSessionTimer(GameConfig.TARGET_FPS * SESSION_DURATION_SECONDS,
        this.game != null ? this.game.getAudio() : null);
    this.tutorialController = new TrainingTutorialController();
    this.endScreenHandler = new TrainingEndScreenHandler(input, this.controls, this.game,
        this::resetArena);
  }

  // === Lifecycle ===

  public GameMap buildArena()
  {
    GameMap map = new GameMap(ARENA_TILES_WIDE, ARENA_TILES_HIGH);
    this.arenaBuilder.buildArena(map, ARENA_TILES_WIDE, new Random(this.sessionSeedHash));
    this.arena = map;
    return map;
  }

  public void start()
  {
    this.spawnController.spawnInitialEnemies();
  }

  public void shutdown()
  {
    this.configStore.saveControls();
  }

  public void resetArena()
  {
    this.spawnController.resetArena();
  }

  // === Update tick ===

  public void update(Player player, GameInput input, ProjectileSystem projectileSystem)
  {
    markDebugUsedIfNeeded();

    if (this.sessionTimer.isSessionFinished())
    {
      this.endScreenHandler.handleFinalInput();
      return;
    }

    if (player != null && player.isDead())
    {
      this.endScreenHandler.handleDeadInput();
      return;
    }

    this.endScreenHandler.readPanelInput();
    updateTutorialFlow(projectileSystem);
    updateHintAlpha();

    if (isWaitingForFirstShot())
    {
      return;
    }

    this.scoreTracker.updateProjectileFailures(projectileSystem);
    boolean wasFinished = this.sessionTimer.isSessionFinished();
    this.sessionTimer.update(this.session);
    if (!wasFinished && this.sessionTimer.isSessionFinished())
    {
      this.scoreTracker.countPredictedFinalProjectileFailures(projectileSystem);
      this.scoreTracker.updateDisplayedTrainingStats();
    }

    int replacementCount = this.scoreTracker.awardScoreForDestroyedTargets();
    if (this.scoreTracker.isTargetHintHit())
    {
      this.targetHintHit = true;
    }
    replacementCount += this.scoreTracker.damageTargetsOverTime();
    replacementCount += this.scoreTracker.removeDeadTargets();
    replacementCount += this.scoreTracker.removeExpiredTargets();
    this.spawnController.syncEnemyCountToControls(replacementCount);
  }

  public void notifyShotFired()
  {
    this.shotFired = true;
    if (this.scoreTracker != null)
    {
      this.scoreTracker.incrementShotsFired();
    }
    if (this.tutorialController != null && this.tutorialController.getPhase() != TutorialPhase.NORMAL)
    {
      this.hintFadeShotSteps++;
    }
  }

  public boolean isSessionFinished()
  {
    return this.sessionTimer.isSessionFinished();
  }

  public boolean isWaitingForFirstShot()
  {
    return this.tutorialController.getPhase() == TutorialPhase.AIM;
  }

  public TrainingHudSnapshot hudSnapshot(Player player)
  {
    TrainingHudSnapshotConfig config = new TrainingHudSnapshotConfig()
      .withStats(
        this.scoreTracker.getFallos(),
        this.scoreTracker.getAciertos(),
        this.scoreTracker.precisionText())
      .withTimer(timerText(), this.tutorialController.getPhase() == TutorialPhase.NORMAL)
      .withHint(
        this.displayedHintAlpha,
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

  // === Tutorial flow ===

  private void updateTutorialFlow(ProjectileSystem projectileSystem)
  {
    int projectileCount = projectileSystem == null ? 0 : projectileSystem.getProjectiles().size();
    TutorialPhase phase = this.tutorialController.getPhase();

    if (phase == TutorialPhase.AIM && (this.shotFired || projectileCount > this.lastProjectileCount))
    {
      setPhase(TutorialPhase.TARGETS);
      this.targetHintTicks = 0;
      this.hintFadeShotSteps = 0;
      this.displayedHintAlpha = HINT_START_ALPHA;
      this.targetHintHit = false;
      phase = TutorialPhase.TARGETS;
    }
    this.shotFired = false;
    this.lastProjectileCount = projectileCount;

    if (phase == TutorialPhase.TARGETS)
    {
      this.targetHintTicks++;
      if (isDisplayedHintFullyFaded())
      {
        setPhase(TutorialPhase.NORMAL);
        this.scoreTracker.setTargetLifetimeRunning(true);
        return;
      }
      if (this.targetHintHit && this.targetHintTicks >= TARGET_HINT_MIN_TICKS)
      {
        setPhase(TutorialPhase.TIMER_NOTICE);
        this.targetHintTicks = 0;
        this.hintFadeShotSteps = 0;
        this.displayedHintAlpha = HINT_START_ALPHA;
      }
    }
    else if (phase == TutorialPhase.TIMER_NOTICE)
    {
      this.targetHintTicks++;
      if (isDisplayedHintFullyFaded() || this.targetHintTicks >= TIMER_NOTICE_TICKS)
      {
        setPhase(TutorialPhase.NORMAL);
        this.scoreTracker.setTargetLifetimeRunning(true);
      }
    }
  }

  private void setPhase(TutorialPhase phase)
  {
    this.tutorialController.setPhase(phase);
    this.scoreTracker.setTutorialPhase(phase);
  }

  private void updateHintAlpha()
  {
    float targetAlpha = Math.max(0.0f, HINT_START_ALPHA - this.hintFadeShotSteps * HINT_SHOT_ALPHA_STEP);
    if (this.displayedHintAlpha <= targetAlpha)
    {
      this.displayedHintAlpha = targetAlpha;
      return;
    }
    this.displayedHintAlpha = Math.max(targetAlpha, this.displayedHintAlpha - HINT_ALPHA_FADE_STEP);
  }

  private boolean isDisplayedHintFullyFaded()
  {
    float targetAlpha = Math.max(0.0f, HINT_START_ALPHA - this.hintFadeShotSteps * HINT_SHOT_ALPHA_STEP);
    return targetAlpha <= 0.0f && this.displayedHintAlpha <= 0.0f;
  }

  private void markDebugUsedIfNeeded()
  {
    if (this.game != null && this.game.getDebugOptions() != null
        && this.game.getDebugOptions().hasAnyModeEnabled())
        {
      this.debugUsed = true;
    }
  }

  // === HUD helpers ===

  private String timerText()
  {
    int seconds = Math.max(0,
      (this.sessionTimer.getSessionTimer() + GameConfig.TARGET_FPS - 1) / GameConfig.TARGET_FPS);
    return (seconds / 60) + ":" + (seconds % 60 < 10 ? "0" : "") + (seconds % 60);
  }

  private int outerFenceScreenCenterX()
  {
    int interiorCenterTileX = INTERIOR_ORIGIN_X + INTERIOR_TILES / 2;
    return tileToWorldCenter(interiorCenterTileX) - this.game.getCameraCenterWorldX()
      + GameConfig.SCREEN_CENTER_X;
  }

  private int outerFenceBottomScreenY()
  {
    int bottomFenceWorldY = (INTERIOR_ORIGIN_Y + INTERIOR_TILES + 1) * GameConfig.TILE_SIZE;
    return bottomFenceWorldY - this.game.getCameraCenterWorldY() + GameConfig.SCREEN_CENTER_Y;
  }

  private int trainingPromptBaselineY()
  {
    return Math.min(
      outerFenceBottomScreenY() + HINT_BASELINE_BELOW_OUTER_FENCE_BOTTOM,
      GameConfig.SCREEN_HEIGHT - HINT_BOTTOM_SCREEN_MARGIN);
  }

  private static int tileToWorldCenter(int tile)
  {
    return tile * GameConfig.TILE_SIZE + GameConfig.TILE_SIZE / 2;
  }

  private String currentHintText()
  {
    TutorialPhase phase = this.tutorialController.getPhase();
    if (phase == TutorialPhase.TIMER_NOTICE)
    {
      return TIMER_NOTICE_TEXT;
    }
    if (phase == TutorialPhase.AIM)
    {
      return AIM_HINT_TEXT;
    }
    if (phase == TutorialPhase.TARGETS)
    {
      return TARGET_HINT_TEXT;
    }
    return "";
  }

  // === Public state accessors ===

  public int getPlayerSpawnWorldX()
  {
    return this.arenaBuilder.getPlayerSpawnWorldX();
  }

  public int getPlayerSpawnWorldY()
  {
    return this.arenaBuilder.getPlayerSpawnWorldY();
  }

  public int getFailurePerimeterLeftWorldX()
  {
    return INTERIOR_ORIGIN_X * GameConfig.TILE_SIZE;
  }

  public int getFailurePerimeterTopWorldY()
  {
    return INTERIOR_ORIGIN_Y * GameConfig.TILE_SIZE;
  }

  public int getFailurePerimeterWidthWorld()
  {
    return INTERIOR_TILES * GameConfig.TILE_SIZE;
  }

  public int getFailurePerimeterHeightWorld()
  {
    return INTERIOR_TILES * GameConfig.TILE_SIZE;
  }

}
