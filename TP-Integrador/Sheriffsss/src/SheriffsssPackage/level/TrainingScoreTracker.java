package SheriffsssPackage.level;

import SheriffsssPackage.Game;
import SheriffsssPackage.context.DebugOptions;
import SheriffsssPackage.context.GameConfig;
import SheriffsssPackage.session.GameMap;
import SheriffsssPackage.session.GameSession;
import SheriffsssPackage.session.Player;
import SheriffsssPackage.system.ShotFeedback;
import SheriffsssPackage.system.enemy.Enemy;
import SheriffsssPackage.system.enemy.EnemySystem;
import SheriffsssPackage.system.enemy.EnemyType;
import SheriffsssPackage.system.weapon.Projectile;
import SheriffsssPackage.system.weapon.ProjectileSystem;

import java.util.List;

/**
 * Tracks and manages training mode scoring, statistics, and related gameplay mechanics.
 * Handles target destruction scoring, projectile failure tracking, and precision calculations.
 */
public final class TrainingScoreTracker
{
  // --- Arena ---
  private static final int INTERIOR_TILES = GameConfig.TRAINING_INTERIOR_FENCE_TILES;
  private static final int INTERIOR_ORIGIN_X = (GameConfig.TRAINING_ARENA_TILES_WIDE - INTERIOR_TILES) / 2;
  private static final int INTERIOR_ORIGIN_Y = (GameConfig.TRAINING_ARENA_TILES_HIGH - INTERIOR_TILES) / 2;

  // --- Training constants ---
  public static final int TARGET_LIFETIME_TICKS = GameConfig.TARGET_FPS * 4;
  public static final int TARGET_DECAY_INTERVAL_TICKS = GameConfig.TARGET_FPS;
  public static final double TARGET_DECAY_DAMAGE = 1.0;

  // --- Sound ---
  private static final String HIT_MILESTONE_SOUND_PATH = "sounds/ding_1.wav";
  private static final float COUNTDOWN_SOUND_GAIN_DB = 9.5f;

  // --- Instance variables ---
  private final Game game;
  private final GameSession session;
  private final EnemySystem enemySystem;
  private final ProjectileSystem projectileSystem;
  private final GameMap map;
  private final ShotFeedback shotFeedback;

  private int aciertos;
  private int fallos;
  private int shotsFired;
  private int nextHitSoundMilestone;
  private int displayedFailures;
  private int displayedPrecisionPercent;
  private int lastEnemyCount;
  private boolean targetLifetimeRunning;
  private boolean targetHintHit;
  private TutorialPhase tutorialPhase = TutorialPhase.AIM;

  public TrainingScoreTracker(
      Game game,
      GameSession session,
      EnemySystem enemySystem,
      ProjectileSystem projectileSystem,
      GameMap map,
      ShotFeedback shotFeedback)
  {
    this.game = game;
    this.session = session;
    this.enemySystem = enemySystem;
    this.projectileSystem = projectileSystem;
    this.map = map;
    this.shotFeedback = shotFeedback;
    this.aciertos = 0;
    this.fallos = 0;
    this.shotsFired = 0;
    this.nextHitSoundMilestone = 10;
    this.displayedFailures = 0;
    this.displayedPrecisionPercent = 0;
    this.lastEnemyCount = 0;
    this.targetLifetimeRunning = false;
    this.targetHintHit = false;
  }

  // === Public Score Tracking Methods ===

  public int awardScoreForDestroyedTargets()
  {
    int destroyedCount = playerDestroyedTargetCount();
    if (destroyedCount > 0)
    {
      this.aciertos += destroyedCount;
      playHitMilestoneSounds();
      updateDisplayedTrainingStats();
      if (this.tutorialPhase == TutorialPhase.TARGETS)
      {
        this.targetHintHit = true;
      }
    }
    this.lastEnemyCount = this.enemySystem.enemyCount();
    return destroyedCount;
  }

  public int removeExpiredTargets()
  {
    if (!this.targetLifetimeRunning) {
      return 0;
    }
    int removedCount = this.enemySystem.removeByTypeAndMinimumAnimationTicks(EnemyType.DIANA, TARGET_LIFETIME_TICKS);
    if (removedCount > 0)
    {
      this.lastEnemyCount = Math.max(0, this.lastEnemyCount - removedCount);
    }
    return removedCount;
  }

  public int damageTargetsOverTime()
  {
    if (!this.targetLifetimeRunning) {
      return 0;
    }
    List<Enemy> enemies = this.enemySystem.getEnemies();
    for (int i = 0; i < enemies.size(); i++)
    {
      Enemy enemy = enemies.get(i);
      if (enemy.getType() == EnemyType.DIANA
          && !enemy.isDead()
          && enemy.getAnimationTicks() > 0
          && enemy.getAnimationTicks() % TARGET_DECAY_INTERVAL_TICKS == 0)
      {
        enemy.damage(TARGET_DECAY_DAMAGE);
      }
    }
    return 0;
  }

  public int removeDeadTargets()
  {
    List<Enemy> enemies = this.enemySystem.getEnemies();
    int removedCount = 0;
    for (int i = enemies.size() - 1; i >= 0; i--)
    {
      Enemy enemy = enemies.get(i);
      if (enemy.getType() == EnemyType.DIANA && enemy.isDead())
      {
        this.enemySystem.removeEnemy(enemy);
        removedCount++;
      }
    }
    return removedCount;
  }

  public void updateProjectileFailures(ProjectileSystem projectileSystem)
  {
    if (projectileSystem == null) {
      return;
    }
    List<Projectile> projectiles = projectileSystem.getProjectiles();
    int newFailures = 0;
    for (int i = 0; i < projectiles.size(); i++)
    {
      Projectile projectile = projectiles.get(i);
      if (projectile == null || projectile.isCountedTrainingFailure()
        || isWorldInsideTrainingPerimeter(projectile.getWorldX(), projectile.getWorldY()))
      {
        continue;
      }
      projectile.markCountedTrainingFailure();
      newFailures++;
    }
    if (newFailures > 0)
    {
      this.fallos += newFailures;
      updateDisplayedTrainingStats();
    }
  }

  public void countPredictedFinalProjectileFailures(ProjectileSystem projectileSystem)
  {
    if (projectileSystem == null) {
      return;
    }
    List<Projectile> projectiles = projectileSystem.getProjectiles();
    int predictedFailures = 0;
    for (int i = 0; i < projectiles.size(); i++)
    {
      Projectile projectile = projectiles.get(i);
      if (projectile == null || projectile.isCountedTrainingFailure()
        || !isWorldInsideTrainingPerimeter(projectile.getWorldX(), projectile.getWorldY()))
      {
        continue;
      }
      if (projectileTrajectoryWouldHitTarget(projectile)) {
        continue;
      }
      projectile.markCountedTrainingFailure();
      predictedFailures++;
    }
    if (predictedFailures > 0)
    {
      this.fallos += predictedFailures;
    }
  }

  public void updateDisplayedTrainingStats()
  {
    int effectiveShots = effectiveShotsFired();
    this.displayedFailures = this.fallos;
    if (effectiveShots <= 0)
    {
      this.displayedPrecisionPercent = 0;
      return;
    }
    this.displayedPrecisionPercent = (int) Math.round((this.aciertos * 100.0) / effectiveShots);
  }

  public int finalScore()
  {
    double multiplicador = 1;
    if (this.displayedPrecisionPercent < 100) {
      multiplicador = 1;
    }
    else
    {
      multiplicador = 1.5;
    }
    return (int) Math.round((this.aciertos * (100 + this.displayedPrecisionPercent) / 10.0) * multiplicador);
  }

  public String precisionText()
  {
    return this.displayedPrecisionPercent + "%";
  }

  // === Getter/Setter methods ===

  public int getAciertos()
  {
    return this.aciertos;
  }

  public void setAciertos(int aciertos)
  {
    this.aciertos = aciertos;
  }

  public int getFallos()
  {
    return this.fallos;
  }

  public void setFallos(int fallos)
  {
    this.fallos = fallos;
  }

  public int getShotsFired()
  {
    return this.shotsFired;
  }

  public void setShotsFired(int shotsFired)
  {
    this.shotsFired = shotsFired;
  }

  public int getDisplayedPrecisionPercent()
  {
    return this.displayedPrecisionPercent;
  }

  public void setDisplayedPrecisionPercent(int displayedPrecisionPercent)
  {
    this.displayedPrecisionPercent = displayedPrecisionPercent;
  }

  public int getDisplayedFailures()
  {
    return this.displayedFailures;
  }

  public void setDisplayedFailures(int displayedFailures)
  {
    this.displayedFailures = displayedFailures;
  }

  public int getLastEnemyCount()
  {
    return this.lastEnemyCount;
  }

  public void setLastEnemyCount(int lastEnemyCount)
  {
    this.lastEnemyCount = lastEnemyCount;
  }

  public boolean isTargetLifetimeRunning()
  {
    return this.targetLifetimeRunning;
  }

  public void setTargetLifetimeRunning(boolean targetLifetimeRunning)
  {
    this.targetLifetimeRunning = targetLifetimeRunning;
  }

  public boolean isTargetHintHit()
  {
    return this.targetHintHit;
  }

  public void setTargetHintHit(boolean targetHintHit)
  {
    this.targetHintHit = targetHintHit;
  }

  public TutorialPhase getTutorialPhase()
  {
    return this.tutorialPhase;
  }

  public void setTutorialPhase(TutorialPhase tutorialPhase)
  {
    this.tutorialPhase = tutorialPhase;
  }

  // === Private helper methods ===

  private int playerDestroyedTargetCount()
  {
    int count = 0;
    Player player = this.game == null ? null : this.game.getPlayer();
    List<Enemy> deadEnemies = this.enemySystem.getCollectedDeadEnemies();
    for (int i = 0; i < deadEnemies.size(); i++)
    {
      Enemy enemy = deadEnemies.get(i);
      if (player != null && enemy.getType() == EnemyType.DIANA && enemy.getLastDamageSourcePlayer() == player)
      {
        count++;
      }
    }
    this.enemySystem.clearCollectedDeadEnemies();
    return count;
  }

  private void playHitMilestoneSounds()
  {
    while (this.aciertos >= this.nextHitSoundMilestone) {
      this.game.getAudio().playOnce(HIT_MILESTONE_SOUND_PATH, COUNTDOWN_SOUND_GAIN_DB);
      this.nextHitSoundMilestone += 10;
    }
  }

  private boolean projectileTrajectoryWouldHitTarget(Projectile projectile)
  {
    double startX = projectile.getPreciseWorldX();
    double startY = projectile.getPreciseWorldY();
    double velocityX = projectile.getVelocityX();
    double velocityY = projectile.getVelocityY();
    double maxTicks = Math.min(
      projectile.getLifeTicks(),
      ticksUntilTrajectoryLeavesTrainingPerimeter(startX, startY, velocityX, velocityY));
    if (maxTicks <= 0.0)
    {
      return false;
    }
    double endX = startX + velocityX * maxTicks;
    double endY = startY + velocityY * maxTicks;
    List<Enemy> enemies = this.enemySystem.getEnemies();
    int projectilePadding = Math.max(projectile.getType().getDrawWidth(), projectile.getType().getDrawHeight()) / 2;
    for (int i = 0; i < enemies.size(); i++)
    {
      Enemy enemy = enemies.get(i);
      if (enemy == null || enemy.isDead())
      {
        continue;
      }
      int hitRadius = enemy.getType().getCollisionRadius() + projectilePadding;
      if (segmentIntersectsCircle(startX, startY, endX, endY, enemy.getWorldX(), enemy.getWorldY(), hitRadius))
      {
        return true;
      }
    }
    return false;
  }

  private double ticksUntilTrajectoryLeavesTrainingPerimeter(
      double startX,
      double startY,
      double velocityX,
      double velocityY)
  {
    double left = getFailurePerimeterLeftWorldX();
    double right = left + getFailurePerimeterWidthWorld();
    double top = getFailurePerimeterTopWorldY();
    double bottom = top + getFailurePerimeterHeightWorld();
    double maxTicks = Double.POSITIVE_INFINITY;
    if (velocityX > 0.0)
    {
      maxTicks = Math.min(maxTicks, (right - startX) / velocityX);
    } else if (velocityX < 0.0)
    {
      maxTicks = Math.min(maxTicks, (left - startX) / velocityX);
    }
    if (velocityY > 0.0)
    {
      maxTicks = Math.min(maxTicks, (bottom - startY) / velocityY);
    } else if (velocityY < 0.0)
    {
      maxTicks = Math.min(maxTicks, (top - startY) / velocityY);
    }
    return Double.isFinite(maxTicks) ? Math.max(0.0, maxTicks) : 0.0;
  }

  private static boolean segmentIntersectsCircle(
      double startX,
      double startY,
      double endX,
      double endY,
      double centerX,
      double centerY,
      double radius)
  {
    double segmentX = endX - startX;
    double segmentY = endY - startY;
    double lengthSquared = segmentX * segmentX + segmentY * segmentY;
    if (lengthSquared <= 0.0001) {
      double deltaX = centerX - startX;
      double deltaY = centerY - startY;
      return deltaX * deltaX + deltaY * deltaY <= radius * radius;
    }
    double t = ((centerX - startX) * segmentX + (centerY - startY) * segmentY) / lengthSquared;
    t = Math.max(0.0, Math.min(1.0, t));
    double closestX = startX + segmentX * t;
    double closestY = startY + segmentY * t;
    double deltaX = centerX - closestX;
    double deltaY = centerY - closestY;
    return deltaX * deltaX + deltaY * deltaY <= radius * radius;
  }

  private int effectiveShotsFired()
  {
    DebugOptions debug = this.game == null ? null : this.game.getDebugOptions();
    if (debug != null && debug.shouldForceTrainingPerfectAccuracy())
    {
      return this.aciertos;
    }
    return this.shotsFired;
  }

  private static boolean isWorldInsideTrainingPerimeter(int worldX, int worldY)
  {
    int tileX = Math.floorDiv(worldX, GameConfig.TILE_SIZE);
    int tileY = Math.floorDiv(worldY, GameConfig.TILE_SIZE);
    return tileX >= INTERIOR_ORIGIN_X && tileX < INTERIOR_ORIGIN_X + INTERIOR_TILES
      && tileY >= INTERIOR_ORIGIN_Y && tileY < INTERIOR_ORIGIN_Y + INTERIOR_TILES;
  }

  private int getFailurePerimeterLeftWorldX()
  {
    return INTERIOR_ORIGIN_X * GameConfig.TILE_SIZE;
  }

  private int getFailurePerimeterTopWorldY()
  {
    return INTERIOR_ORIGIN_Y * GameConfig.TILE_SIZE;
  }

  private int getFailurePerimeterWidthWorld()
  {
    return INTERIOR_TILES * GameConfig.TILE_SIZE;
  }

  private int getFailurePerimeterHeightWorld()
  {
    return INTERIOR_TILES * GameConfig.TILE_SIZE;
  }
}
