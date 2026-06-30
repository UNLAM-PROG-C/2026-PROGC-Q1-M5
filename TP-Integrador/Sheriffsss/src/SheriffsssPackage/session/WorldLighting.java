package SheriffsssPackage.session;

import SheriffsssPackage.context.DayNightCycle;
import SheriffsssPackage.context.GameConfig;
import SheriffsssPackage.system.enemy.Debuff;
import SheriffsssPackage.system.enemy.Enemy;
import SheriffsssPackage.system.weapon.ItemDefinition;


import java.util.List;

public class WorldLighting
{
	public static final int MAX_DARKNESS_ALPHA = 225;
	private static final double FULL_LIGHT = 1.0;
	private static final Debuff[] DEBUFFS = Debuff.values();

	public int resolveDarknessAlpha(GameMap map, int tileX, int tileY, DayNightCycle cycle)
 {
		double light = Math.max(cycle.getAmbientLight(), resolveLocalLight(map, tileX, tileY));
		return darknessAlphaFromLight(clampLight(light));
	}

	public int resolveDarknessAlpha(GameMap map, Player player, int tileX, int tileY, DayNightCycle cycle)
 {
		double light = Math.max(cycle.getAmbientLight(), resolveLocalLight(map, tileX, tileY));
		light = Math.max(light, resolveHeldLight(map, player, tileX, tileY));
		return darknessAlphaFromLight(clampLight(light));
	}

	private static double clampLight(double light)
 {
		return Math.max(0.0, Math.min(FULL_LIGHT, light));
	}

	private static int darknessAlphaFromLight(double light)
 {
		return (int) ((FULL_LIGHT - light) * MAX_DARKNESS_ALPHA);
	}

	public double resolveDynamicLight(GameMap map, int sourceWorldX, int sourceWorldY, int tileX, int tileY, int radiusTiles, double intensity)
 {
		if (map == null)
		{
			return 0.0;
		}
		int sourceTileX = map.worldToTileX(sourceWorldX);
		int sourceTileY = map.worldToTileY(sourceWorldY);
		return contribution(radiusTiles, intensity, tileX - sourceTileX, tileY - sourceTileY);
	}

	public boolean hasEnemyDebuffLights(List<Enemy> enemies)
 {
		if (enemies == null)
		{
			return false;
		}
		for (int i = 0; i < enemies.size(); i++)
		{
			Enemy enemy = enemies.get(i);
			if (enemy != null && !enemy.isDead() && enemy.hasLightEmittingDebuff())
   {
				return true;
			}
		}
		return false;
	}

	public double resolveEnemyDebuffLight(GameMap map, List<Enemy> enemies, int tileX, int tileY)
 {
		if (map == null || enemies == null)
		{
			return 0.0;
		}
		int tileCenterWorldX = tileX * GameConfig.TILE_SIZE + GameConfig.TILE_SIZE / 2;
		int tileCenterWorldY = tileY * GameConfig.TILE_SIZE + GameConfig.TILE_SIZE / 2;
		double bestLight = 0.0;
		for (int i = 0; i < enemies.size(); i++)
		{
			bestLight = Math.max(bestLight, lightFromEnemy(enemies.get(i), tileCenterWorldX, tileCenterWorldY));
			if (bestLight >= FULL_LIGHT)
			{
				return FULL_LIGHT;
			}
		}
		return bestLight;
	}

	private double lightFromEnemy(Enemy enemy, int tileCenterWorldX, int tileCenterWorldY)
 {
		if (enemy == null || enemy.isDead() || !enemy.hasLightEmittingDebuff())
		{
			return 0.0;
		}
		int deltaX = tileCenterWorldX - enemy.getWorldX();
		int deltaY = tileCenterWorldY - enemy.getWorldY();
		double bestLight = 0.0;
		for (int debuffIndex = 0; debuffIndex < DEBUFFS.length; debuffIndex++)
		{
			Debuff debuff = DEBUFFS[debuffIndex];
			if (!enemy.hasDebuff(debuff))
			{
				continue;
			}
			bestLight = Math.max(bestLight, contributionPixels(
				debuff.getLightRadiusTiles() * GameConfig.TILE_SIZE,
				debuff.getLightIntensity(), deltaX, deltaY));
		}
		return bestLight;
	}

	private double resolveHeldLight(GameMap map, Player player, int tileX, int tileY)
 {
		if (player == null)
		{
			return 0.0;
		}
		ItemDefinition definition = player.getEquipment().getEquippedWeapon();
		if (definition == null)
  {
			return 0.0;
		}
		int radius = definition.getLightRadiusTiles();
		if (radius <= 0)
  {
			return 0.0;
		}
		double intensity = definition.getLightIntensity();
		int sourceX = map.worldToTileX(player.getX());
		int sourceY = map.worldToTileY(player.getFeetWorldY());
		return contribution(radius, intensity, tileX - sourceX, tileY - sourceY);
	}

	private double resolveLocalLight(GameMap map, int tileX, int tileY)
 {
		int radius = GameConfig.MAX_LIGHT_RADIUS_TILES;
		double bestLight = 0.0;
		for (int sourceX = tileX - radius; sourceX <= tileX + radius; sourceX++)
		{
			for (int sourceY = tileY - radius; sourceY <= tileY + radius; sourceY++)
			{
				bestLight = Math.max(bestLight, lightFromTile(map, tileX, tileY, sourceX, sourceY));
				if (bestLight >= FULL_LIGHT)
				{
					return FULL_LIGHT;
				}
			}
		}
		return bestLight;
	}

	private double lightFromTile(GameMap map, int tileX, int tileY, int sourceX, int sourceY)
 {
		if (!map.isInBounds(sourceX, sourceY))
		{
			return 0.0;
		}
		int deltaX = tileX - sourceX;
		int deltaY = tileY - sourceY;
		TileType tileType = map.getTile(sourceX, sourceY);
		double light = contribution(tileType.getLightRadiusTiles(), tileType.getLightIntensity(), deltaX, deltaY);
		MapObject mapObject = map.getObject(sourceX, sourceY);
		if (mapObject != null)
		{
			MapObjectType objectType = mapObject.getType();
			light = Math.max(light, contribution(objectType.getLightRadiusTiles(), objectType.getLightIntensity(), deltaX, deltaY));
		}
		return light;
	}

	private double contribution(int radiusTiles, double intensity, int deltaX, int deltaY)
 {
		if (radiusTiles <= 0 || intensity <= 0.0)
		{
			return 0.0;
		}
		int distanceSquared = deltaX * deltaX + deltaY * deltaY;
		int radiusSquared = radiusTiles * radiusTiles;
		if (distanceSquared > radiusSquared)
		{
			return 0.0;
		}
		return intensity * (1.0 - distanceSquared / (double) radiusSquared);
	}

	private double contributionPixels(int radiusPixels, double intensity, int deltaX, int deltaY)
 {
		if (radiusPixels <= 0 || intensity <= 0.0)
		{
			return 0.0;
		}
		int distanceSquared = deltaX * deltaX + deltaY * deltaY;
		int radiusSquared = radiusPixels * radiusPixels;
		if (distanceSquared > radiusSquared)
		{
			return 0.0;
		}
		return intensity * (1.0 - distanceSquared / (double) radiusSquared);
	}
}
