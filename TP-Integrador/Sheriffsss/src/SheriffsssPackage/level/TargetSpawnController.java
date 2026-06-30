package SheriffsssPackage.level;

import SheriffsssPackage.context.GameConfig;
import SheriffsssPackage.session.GameMap;
import SheriffsssPackage.session.GameSession;
import SheriffsssPackage.system.enemy.Enemy;
import SheriffsssPackage.system.enemy.EnemyFactory;
import SheriffsssPackage.system.enemy.EnemySystem;

import java.util.Random;

/**
 * Controlador de spawn de enemigos para el modo entrenamiento.
 * Gestiona la aparición inicial de enemigos, el spawn aleatorio dentro de la arena,
 * y la sincronización de la cantidad de enemigos con los controles de entrenamiento.
 */
public final class TargetSpawnController
{
  // --- Arena constants ---
  private static final int ARENA_TILES_WIDE = GameConfig.TRAINING_ARENA_TILES_WIDE;
  private static final int ARENA_TILES_HIGH = GameConfig.TRAINING_ARENA_TILES_HIGH;
  private static final int INTERIOR_TILES = GameConfig.TRAINING_INTERIOR_FENCE_TILES;
  private static final int INTERIOR_ORIGIN_X = (ARENA_TILES_WIDE - INTERIOR_TILES) / 2;
  private static final int INTERIOR_ORIGIN_Y = (ARENA_TILES_HIGH - INTERIOR_TILES) / 2;
  private static final int INTERIOR_CENTER_TILE_X = INTERIOR_ORIGIN_X + INTERIOR_TILES / 2;
  private static final int INTERIOR_CENTER_TILE_Y = INTERIOR_ORIGIN_Y + INTERIOR_TILES / 2;
  private static final int ENEMY_INITIAL_DAY_COUNT = 1;
  private static final int CENTER_FENCE_HALF_SIZE_TILES = 2;
  private static final int RANDOM_SPAWN_ATTEMPTS = 80;
  private static final int TARGET_SPAWN_INTERVAL_TICKS = GameConfig.TARGET_FPS;

  // --- Instance fields ---
  private final GameSession session;
  private final EnemySystem enemySystem;
  private final EnemyFactory enemyFactory;
  private final GameMap map;
  private final TrainingControls controls;
  private final Random targetSpawnRandom;

  private int lastEnemyCount;
  private int spawnCooldownTicks;

  /**
   * Constructor del controlador de spawn.
   *
   * @param session la sesión de juego actual
   * @param enemySystem el sistema de enemigos
   * @param enemyFactory la fábrica para crear enemigos
   * @param map el mapa de la arena
   * @param controls los controles de entrenamiento
   */
  public TargetSpawnController(GameSession session, EnemySystem enemySystem, EnemyFactory enemyFactory,
                                GameMap map, TrainingControls controls)
                                {
    this.session = session;
    this.enemySystem = enemySystem;
    this.enemyFactory = enemyFactory;
    this.map = map;
    this.controls = controls;
    this.targetSpawnRandom = new Random(~session.hashCode());
    this.lastEnemyCount = 0;
    this.spawnCooldownTicks = 0;
  }

  /**
   * Spawna el enemigo inicial y resetea los contadores de la sesión de entrenamiento.
   */
  public void spawnInitialEnemies()
  {
    this.enemySystem.clear();
    this.enemySystem.addEnemy(createEnemy());
    this.lastEnemyCount = this.enemySystem.enemyCount();
    this.spawnCooldownTicks = TARGET_SPAWN_INTERVAL_TICKS;
  }

  /**
   * Sincroniza la cantidad de enemigos en la arena con la cantidad deseada desde los controles.
   *
   * @param replacementCount cantidad de enemigos que pueden ser reemplazados en este tick
   */
  public void syncEnemyCountToControls(int replacementCount)
  {
    int desired = this.controls.getEnemyCount();
    while (replacementCount > 0 && this.enemySystem.enemyCount() < desired)
    {
      this.enemySystem.addEnemy(createEnemy());
      replacementCount--;
    }
    if (this.enemySystem.enemyCount() < desired)
    {
      if (this.spawnCooldownTicks > 0)
      {
        this.spawnCooldownTicks--;
        return;
      }
      this.enemySystem.addEnemy(createEnemy());
      this.spawnCooldownTicks = TARGET_SPAWN_INTERVAL_TICKS;
    }
    this.enemySystem.trimEnemiesToCount(desired);
    this.lastEnemyCount = this.enemySystem.enemyCount();
  }

  /**
   * Resetea la arena a su estado inicial, reapariendo el enemigo inicial.
   */
  public void resetArena()
  {
    spawnInitialEnemies();
  }

  /**
   * Crea un enemigo de entrenamiento en una posición spawn aleatoria.
   *
   * @return el enemigo creado
   */
  private Enemy createEnemy()
  {
    int[] spawn = randomArenaSpawn();
    return this.enemyFactory.createTrainingTarget(spawn[0], spawn[1], ENEMY_INITIAL_DAY_COUNT);
  }

  /**
   * Genera una posición de spawn aleatoria dentro de la arena, evitando
   * la cerca central y las áreas no transversables.
   *
   * @return un array de dos elementos [worldX, worldY]
   */
  private int[] randomArenaSpawn()
  {
    int loX = INTERIOR_ORIGIN_X;
    int hiX = INTERIOR_ORIGIN_X + INTERIOR_TILES;
    int loY = INTERIOR_ORIGIN_Y;
    int hiY = INTERIOR_ORIGIN_Y + INTERIOR_TILES;
    for (int attempt = 0; attempt < RANDOM_SPAWN_ATTEMPTS; attempt++)
    {
      int tileX = this.targetSpawnRandom.nextInt(loX, hiX);
      int tileY = this.targetSpawnRandom.nextInt(loY, hiY);
      int worldX = tileToWorldCenter(tileX);
      int worldY = tileToWorldCenter(tileY);
      if (!isInsideCenterFence(tileX, tileY) && (this.map == null || this.map.canPlaceObject(tileX, tileY)))
      {
        return new int[] { worldX, worldY };
      }
    }
    return new int[] { tileToWorldCenter(INTERIOR_CENTER_TILE_X), tileToWorldCenter(INTERIOR_CENTER_TILE_Y) };
  }

  /**
   * Verifica si una posición de tile está dentro de la cerca central.
   *
   * @param tileX la coordenada X del tile
   * @param tileY la coordenada Y del tile
   * @return true si la posición está dentro de la cerca central, false en caso contrario
   */
  private boolean isInsideCenterFence(int tileX, int tileY)
  {
    int centerTileX = INTERIOR_CENTER_TILE_X;
    int centerTileY = INTERIOR_CENTER_TILE_Y;
    return Math.abs(tileX - centerTileX) <= CENTER_FENCE_HALF_SIZE_TILES
      && Math.abs(tileY - centerTileY) <= CENTER_FENCE_HALF_SIZE_TILES;
  }

  /**
   * Convierte una coordenada de tile al centro de esa posición en coordenadas del mundo.
   *
   * @param tile la coordenada del tile
   * @return la coordenada en el mundo correspondiente al centro del tile
   */
  private static int tileToWorldCenter(int tile)
  {
    return tile * GameConfig.TILE_SIZE + GameConfig.TILE_SIZE / 2;
  }
}
