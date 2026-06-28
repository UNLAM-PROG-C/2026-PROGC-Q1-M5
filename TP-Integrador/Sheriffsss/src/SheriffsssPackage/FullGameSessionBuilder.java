package SheriffsssPackage;

import java.util.Random;

import SheriffsssPackage.context.GameContext;
import SheriffsssPackage.level.WorldLevel;
import SheriffsssPackage.session.GameSession;
import SheriffsssPackage.system.HealthPickupSystem;

public final class FullGameSessionBuilder {
  private static final int MAP_TILES = 200;
  private static final String PLAYER_NAME = "Sheriff";
  private static final int TREE_W = 2;
  private static final int TREE_H = 3;
  private static final int SPAWN_CLEAR_RADIUS = 5;
  private static final double VEGETATION_PROP = 0.03;
  private static final int INITIAL_ENEMY_BURST = 8;
  private static final MapObjectType[] TREE_TYPES = {
    MapObjectType.TRAINING_BORDER_TREE_1,
    MapObjectType.TRAINING_BORDER_TREE_2,
    MapObjectType.TRAINING_BORDER_TREE_3,
    MapObjectType.TRAINING_BORDER_TREE_4
  };

  public void build(GameContext context, GameSession session, HealthPickupSystem healthPickupSystem) {
    long seed = System.nanoTime() ^ System.currentTimeMillis();
    int seedHash = Long.hashCode(seed);
    context.debugOptions().resetAll();
    context.enemySystem().clear();
    context.enemySystem().reset(seedHash);
    context.enemySystem().setAutoSpawnEnabled(true);
    session.setActiveLevel(new WorldLevel());
    session.setMap(buildMap(new Random(seed)));
    spawnPlayer(context, session);
    context.projectileSystem().clear();
    context.dayNightCycle().reset();
    session.setDeathOverlayActive(false);
    healthPickupSystem.reset();
    context.enemySystem().spawnBurst(session.map(), session.player(), INITIAL_ENEMY_BURST);
  }

  private void spawnPlayer(GameContext context, GameSession session) {
    int spawnTile = MAP_TILES / 2;
    int spawnX = spawnTile * GameConfig.TILE_SIZE + GameConfig.TILE_SIZE / 2;
    int spawnY = spawnTile * GameConfig.TILE_SIZE + GameConfig.TILE_SIZE / 2;
    session.setPlayer(new Player(PLAYER_NAME, spawnX, spawnY, context.assets()));
    session.resetPlayerRuntime();
    session.player().getEquipment().resetToWeapon(ItemDefinition.BRONZE_REVOLVER);
  }

  private GameMap buildMap(Random random) {
    GameMap map = new GameMap(MAP_TILES, MAP_TILES);
    map.clear(TileType.SAND);
    int centerTile = MAP_TILES / 2;
    for (int tileY = 0; tileY < MAP_TILES; tileY++) {
      for (int tileX = 0; tileX < MAP_TILES; tileX++) {
        placeVegetationIfAllowed(map, random, centerTile, tileX, tileY);
      }
    }
    map.rebuildMinimap();
    return map;
  }

  private void placeVegetationIfAllowed(GameMap map, Random random, int centerTile, int tileX, int tileY) {
    if (Math.abs(tileX - centerTile) <= SPAWN_CLEAR_RADIUS
        && Math.abs(tileY - centerTile) <= SPAWN_CLEAR_RADIUS) {
      return;
    }
    if (random.nextDouble() >= VEGETATION_PROP) {
      return;
    }
    if (random.nextBoolean()) {
      placeDryBush(map, tileX, tileY);
    } else {
      placeTree(map, random, tileX, tileY);
    }
  }

  private void placeDryBush(GameMap map, int tileX, int tileY) {
    if (map.canPlaceObject(tileX, tileY)) {
      map.placeSingleObject(MapObjectType.DRY_BUSH, tileX, tileY, false, false);
    }
  }

  private void placeTree(GameMap map, Random random, int tileX, int tileY) {
    if (!canPlaceTree(map, tileX, tileY)) {
      return;
    }
    MapObjectType type = TREE_TYPES[random.nextInt(TREE_TYPES.length)];
    int cells = TREE_W * TREE_H;
    boolean[] solid = new boolean[cells];
    boolean[] above = new boolean[cells];
    java.util.Arrays.fill(above, true);
    for (int i = 0; i < cells; i++) {
      solid[i] = i >= cells - TREE_W;
    }
    map.placeObjectRect(type, tileX, tileY, solid, above);
  }

  private boolean canPlaceTree(GameMap map, int rootTileX, int rootTileY) {
    for (int tileY = rootTileY; tileY < rootTileY + TREE_H; tileY++) {
      for (int tileX = rootTileX; tileX < rootTileX + TREE_W; tileX++) {
        if (!map.canPlaceObject(tileX, tileY)) {
          return false;
        }
      }
    }
    return true;
  }
}
