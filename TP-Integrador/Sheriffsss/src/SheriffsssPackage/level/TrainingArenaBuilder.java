package SheriffsssPackage.level;

import SheriffsssPackage.context.GameConfig;
import SheriffsssPackage.session.GameMap;
import SheriffsssPackage.session.MapObjectType;
import SheriffsssPackage.session.TileType;

import java.util.Arrays;
import java.util.Random;

/**
 * Builder for the training arena. Handles construction of the training mode arena
 * with wilderness vegetation, perimeter fence, center fence, and decorations.
 */
public class TrainingArenaBuilder {
  // --- Arena ---
  private static final int INTERIOR_TILES = GameConfig.TRAINING_INTERIOR_FENCE_TILES;
  private static final int CENTER_FENCE_HALF_SIZE_TILES = 2;
  private static final int BORDER_TREE_WIDTH_TILES = 2;
  private static final int BORDER_TREE_HEIGHT_TILES = 3;
  private static final int DECORATION_COUNT = 16;
  private static final int RANDOM_SPAWN_ATTEMPTS = 80;
  private static final MapObjectType[] BORDER_TREE_TYPES = {
    MapObjectType.TRAINING_BORDER_TREE_1,
    MapObjectType.TRAINING_BORDER_TREE_2,
    MapObjectType.TRAINING_BORDER_TREE_3,
    MapObjectType.TRAINING_BORDER_TREE_4
  };

  private int arenaTilesWide;
  private int arenaTilesHigh;
  private int interiorOriginX;
  private int interiorOriginY;
  private int interiorCenterTileX;
  private int interiorCenterTileY;
  private Random terrainRandom;
  private int playerSpawnWorldX;
  private int playerSpawnWorldY;

  public TrainingArenaBuilder() {
  }

  public void buildArena(GameMap map, int arenaSizeInTiles, Random random) {
    this.arenaTilesWide = arenaSizeInTiles;
    this.arenaTilesHigh = arenaSizeInTiles;
    this.terrainRandom = random;

    // Calculate interior positions
    this.interiorOriginX = (arenaTilesWide - INTERIOR_TILES) / 2;
    this.interiorOriginY = (arenaTilesHigh - INTERIOR_TILES) / 2;
    this.interiorCenterTileX = interiorOriginX + INTERIOR_TILES / 2;
    this.interiorCenterTileY = interiorOriginY + INTERIOR_TILES / 2;

    // Build the arena
    map.clear(TileType.SAND);
    scatterWildernessVegetation(map);
    placePerimeterFence(map);
    setPlayerSpawnFromTile(interiorCenterTileX, interiorCenterTileY);
    placeCenterFence(map, interiorCenterTileX, interiorCenterTileY);
    placeDecorations(map);
    map.rebuildMinimap();
  }

  private void scatterWildernessVegetation(GameMap arena) {
    double p = GameConfig.TRAINING_WILDERNESS_PROP_PER_TILE;
    for (int tileY = 0; tileY < arenaTilesHigh; tileY++) {
      for (int tileX = 0; tileX < arenaTilesWide; tileX++) {
        if (isInsideInnerArena(tileX, tileY) || isPerimeterFenceTile(tileX, tileY)) {
          continue;
        }
        if (this.terrainRandom.nextDouble() >= p) {
          continue;
        }
        if (this.terrainRandom.nextBoolean()) {
          if (arena.canPlaceObject(tileX, tileY)) {
            arena.placeSingleObject(MapObjectType.DRY_BUSH, tileX, tileY, false, false);
          }
        } else {
          placeWildernessTree(arena, this.terrainRandom, tileX, tileY);
        }
      }
    }
  }

  private void placePerimeterFence(GameMap arena) {
    for (int tileY = 0; tileY < arenaTilesHigh; tileY++) {
      for (int tileX = 0; tileX < arenaTilesWide; tileX++) {
        if (!isPerimeterFenceTile(tileX, tileY)) {
          continue;
        }
        arena.placeSingleObject(MapObjectType.WOODEN_FENCE, tileX, tileY, true, false);
      }
    }
  }

  private void placeWildernessTree(GameMap arena, Random r, int rootTileX, int rootTileY) {
    if (!canPlaceWildernessTree(arena, rootTileX, rootTileY)) {
      return;
    }
    MapObjectType type = BORDER_TREE_TYPES[r.nextInt(BORDER_TREE_TYPES.length)];
    arena.placeObjectRect(type, rootTileX, rootTileY, borderTreeSolidMask(), borderTreeAboveMask());
  }

  private boolean canPlaceWildernessTree(GameMap arena, int rootTileX, int rootTileY) {
    if (rootTileX < 0 || rootTileY < 0 || rootTileX + BORDER_TREE_WIDTH_TILES > arenaTilesWide
      || rootTileY + BORDER_TREE_HEIGHT_TILES > arenaTilesHigh) {
      return false;
    }
    for (int tileX = rootTileX; tileX < rootTileX + BORDER_TREE_WIDTH_TILES; tileX++) {
      for (int tileY = rootTileY; tileY < rootTileY + BORDER_TREE_HEIGHT_TILES; tileY++) {
        if (isInsideInnerArena(tileX, tileY) || isPerimeterFenceTile(tileX, tileY)
          || arena.getObject(tileX, tileY) != null) {
          return false;
        }
      }
    }
    return true;
  }

  private boolean[] borderTreeSolidMask() {
    boolean[] solid = new boolean[BORDER_TREE_WIDTH_TILES * BORDER_TREE_HEIGHT_TILES];
    Arrays.fill(solid, true);
    return solid;
  }

  private boolean[] borderTreeAboveMask() {
    boolean[] above = new boolean[BORDER_TREE_WIDTH_TILES * BORDER_TREE_HEIGHT_TILES];
    Arrays.fill(above, true);
    return above;
  }

  private void placeCenterFence(GameMap arena, int centerTileX, int centerTileY) {
    for (int tileX = centerTileX - CENTER_FENCE_HALF_SIZE_TILES;
        tileX <= centerTileX + CENTER_FENCE_HALF_SIZE_TILES;
        tileX++) {
      placeCenterFenceTile(arena, tileX, centerTileY - CENTER_FENCE_HALF_SIZE_TILES);
      placeCenterFenceTile(arena, tileX, centerTileY + CENTER_FENCE_HALF_SIZE_TILES);
    }
    for (int tileY = centerTileY - CENTER_FENCE_HALF_SIZE_TILES + 1;
        tileY < centerTileY + CENTER_FENCE_HALF_SIZE_TILES;
        tileY++) {
      placeCenterFenceTile(arena, centerTileX - CENTER_FENCE_HALF_SIZE_TILES, tileY);
      placeCenterFenceTile(arena, centerTileX + CENTER_FENCE_HALF_SIZE_TILES, tileY);
    }
  }

  private void placeCenterFenceTile(GameMap arena, int tileX, int tileY) {
    if (!isInsideArenaBounds(tileX, tileY)) {
      return;
    }
    arena.placeSingleObject(MapObjectType.WOODEN_FENCE, tileX, tileY, true, false);
  }

  private void placeDecorations(GameMap arena) {
    int loX = interiorOriginX;
    int hiX = interiorOriginX + INTERIOR_TILES;
    int loY = interiorOriginY;
    int hiY = interiorOriginY + INTERIOR_TILES;
    int placed = 0;
    for (int attempt = 0; attempt < RANDOM_SPAWN_ATTEMPTS && placed < DECORATION_COUNT; attempt++) {
      int tileX = this.terrainRandom.nextInt(loX, hiX);
      int tileY = this.terrainRandom.nextInt(loY, hiY);
      if (isInsideCenterFence(tileX, tileY) || !arena.canPlaceObject(tileX, tileY)) {
        continue;
      }
      arena.placeSingleObject(MapObjectType.DRY_BUSH, tileX, tileY, false, false);
      placed++;
    }
  }

  private void setPlayerSpawnFromTile(int centerTileX, int centerTileY) {
    this.playerSpawnWorldX = tileToWorldCenter(centerTileX);
    this.playerSpawnWorldY = tileToWorldCenter(centerTileY);
  }

  private boolean isInsideInnerArena(int tileX, int tileY) {
    return tileX >= interiorOriginX && tileX < interiorOriginX + INTERIOR_TILES
      && tileY >= interiorOriginY && tileY < interiorOriginY + INTERIOR_TILES;
  }

  private boolean isPerimeterFenceTile(int tileX, int tileY) {
    if (isInsideInnerArena(tileX, tileY)) {
      return false;
    }
    return tileX >= interiorOriginX - 1 && tileX <= interiorOriginX + INTERIOR_TILES
      && tileY >= interiorOriginY - 1 && tileY <= interiorOriginY + INTERIOR_TILES;
  }

  private boolean isInsideArenaBounds(int tileX, int tileY) {
    return isInsideInnerArena(tileX, tileY);
  }

  private int tileToWorldCenter(int tile) {
    return tile * GameConfig.TILE_SIZE + GameConfig.TILE_SIZE / 2;
  }

  private boolean isInsideCenterFence(int tileX, int tileY) {
    int centerTileX = interiorCenterTileX;
    int centerTileY = interiorCenterTileY;
    return Math.abs(tileX - centerTileX) <= CENTER_FENCE_HALF_SIZE_TILES
      && Math.abs(tileY - centerTileY) <= CENTER_FENCE_HALF_SIZE_TILES;
  }

  public int getPlayerSpawnWorldX() {
    return playerSpawnWorldX;
  }

  public int getPlayerSpawnWorldY() {
    return playerSpawnWorldY;
  }
}
