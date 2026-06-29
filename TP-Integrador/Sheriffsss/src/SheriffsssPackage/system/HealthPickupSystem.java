package SheriffsssPackage.system;

import SheriffsssPackage.context.GameConfig;
import SheriffsssPackage.session.GameMap;
import SheriffsssPackage.session.Player;


import java.util.ArrayList;
import java.util.List;
import java.util.Random;


/** Handles health pickup spawn and collection rules for the active match. */
public final class HealthPickupSystem {
  private static final int SPAWN_INTERVAL_TICKS = GameConfig.TARGET_FPS * 60;
  private static final int START_COOLDOWN_TICKS = SPAWN_INTERVAL_TICKS / 2;
  private static final int MAX_PICKUPS = 2;
  private static final int HEAL_AMOUNT = 25;
  private static final int COLLECT_RADIUS_PIXELS = 22;
  private static final int MAX_SPAWN_ATTEMPTS = 25;
  private static final int SPAWN_OFFSET_RANGE_TILES = 22;
  private static final int SPAWN_OFFSET_CENTER_TILE = 11;
  private static final int MIN_PLAYER_DISTANCE_TILES = 4;

  private final ArrayList<int[]> pickups = new ArrayList<>();
  private final Random random = new Random();
  private int cooldownTicks;

  public void reset() {
    this.pickups.clear();
    this.cooldownTicks = START_COOLDOWN_TICKS;
  }

  public void clear()
    {
    this.pickups.clear();
    this.cooldownTicks = 0;
  }

  public boolean update(GameMap map, Player player)
    {
    boolean collected = collectPickups(player);
    spawnPickupIfReady(map, player);
    return collected;
  }

  public int healAmount()
    {
    return HEAL_AMOUNT;
  }

  public List<int[]> pickups()
  {
    return this.pickups;
  }

  private boolean collectPickups(Player player)
  {
    boolean collected = false;
    int playerX = player.getX();
    int playerY = player.getFeetWorldY();
    for (int i = this.pickups.size() - 1; i >= 0; i--)
    {
      collected |= collectPickupIfReached(player, playerX, playerY, i);
    }
    return collected;
  }

  private boolean collectPickupIfReached(Player player, int playerX, int playerY, int index)
      {
    int[] pickup = this.pickups.get(index);
    int deltaX = pickup[0] - playerX;
    int deltaY = pickup[1] - playerY;
    if (!isInsideCollectRadius(deltaX, deltaY))
    {
      return false;
    }
    player.heal(HEAL_AMOUNT);
    this.pickups.remove(index);
    return true;
  }

  private boolean isInsideCollectRadius(int deltaX, int deltaY)
    {
    return deltaX * deltaX + deltaY * deltaY <= COLLECT_RADIUS_PIXELS * COLLECT_RADIUS_PIXELS;
  }

  private void spawnPickupIfReady(GameMap map, Player player)
  {
    if (this.pickups.size() >= MAX_PICKUPS || isCoolingDown()) {
      return;
    }
    trySpawnPickup(map, player);
  }

  private boolean isCoolingDown()
    {
    if (this.cooldownTicks <= 0) {
      return false;
    }
    this.cooldownTicks--;
    return true;
  }

  private void trySpawnPickup(GameMap map, Player player)
  {
    int playerTileX = map.worldToTileX(player.getX());
    int playerTileY = map.worldToTileY(player.getFeetWorldY());
    for (int attempt = 0; attempt < MAX_SPAWN_ATTEMPTS; attempt++)
    {
      if (trySpawnAtRandomTile(map, playerTileX, playerTileY)) {
        return;
      }
    }
  }

  private boolean trySpawnAtRandomTile(GameMap map, int playerTileX, int playerTileY)
  {
    int offsetX = randomOffsetTile();
    int offsetY = randomOffsetTile();
    if (isTooCloseToPlayer(offsetX, offsetY))
    {
      return false;
    }
    return addPickupIfWalkable(map, playerTileX + offsetX, playerTileY + offsetY);
  }

  private int randomOffsetTile()
    {
    return this.random.nextInt(SPAWN_OFFSET_RANGE_TILES) - SPAWN_OFFSET_CENTER_TILE;
  }

  private boolean isTooCloseToPlayer(int offsetX, int offsetY)
    {
    return Math.abs(offsetX) < MIN_PLAYER_DISTANCE_TILES
      && Math.abs(offsetY) < MIN_PLAYER_DISTANCE_TILES;
  }

  private boolean addPickupIfWalkable(GameMap map, int tileX, int tileY)
    {
    int worldX = tileCenterWorld(tileX);
    int worldY = tileCenterWorld(tileY);
    if (!map.isWalkableAtWorld(worldX, worldY))
    {
      return false;
    }
    this.pickups.add(new int[] { worldX, worldY });
    this.cooldownTicks = SPAWN_INTERVAL_TICKS;
    return true;
  }

  private int tileCenterWorld(int tile)
    {
    return tile * GameConfig.TILE_SIZE + GameConfig.TILE_SIZE / 2;
  }
}
