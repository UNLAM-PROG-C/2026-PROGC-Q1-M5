package SheriffsssPackage.render;

import SheriffsssPackage.context.DayNightCycle;
import SheriffsssPackage.context.GameConfig;
import SheriffsssPackage.session.GameMap;
import SheriffsssPackage.session.WorldLighting;
import SheriffsssPackage.system.enemy.Enemy;
import SheriffsssPackage.system.weapon.Projectile;
import SheriffsssPackage.system.weapon.ProjectileType;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.List;

public class LightingRenderer
{
  private final WorldLighting lighting;
  private final Camera camera;
  private final Color[] darknessColors = new Color[WorldLighting.MAX_DARKNESS_ALPHA + 1];

  public LightingRenderer(WorldLighting lighting, Camera camera)
  {
    this.lighting = lighting;
    this.camera = camera;
  }

  public void drawLightingOverlay(Graphics2D g2, GameView game)
  {
    DayNightCycle cycle = game.getDayNightCycle();
    if (cycle.isNaturallyBright())
    {
      return;
    }
    GameMap map = game.getMap();
    List<Enemy> enemies = game.getEnemies();
    boolean hasEnemyDebuffLights = this.lighting.hasEnemyDebuffLights(enemies);
    List<Projectile> projectiles = game.getProjectiles();
    for (int tileX = this.camera.getStartTileX(); tileX <= this.camera.getEndTileX(); tileX++)
    {
      for (int tileY = this.camera.getStartTileY(); tileY <= this.camera.getEndTileY(); tileY++)
      {
        int alpha = game.isSpectating() || game.getPlayer().isDead()
          ? this.lighting.resolveDarknessAlpha(map, tileX, tileY, cycle)
          : this.lighting.resolveDarknessAlpha(map, game.getPlayer(), tileX, tileY, cycle);
        alpha = applyRevolverFlash(game, map, tileX, tileY, alpha);
        alpha = applyProjectileLights(projectiles, map, tileX, tileY, alpha);
        alpha = applyEnemyDebuffLights(enemies, map, tileX, tileY, alpha, hasEnemyDebuffLights);
        if (alpha <= 0)
        {
          continue;
        }
        g2.setColor(getDarknessColor(alpha));
        g2.fillRect(this.camera.tileToScreenX(tileX), this.camera.tileToScreenY(tileY), GameConfig.TILE_SIZE, GameConfig.TILE_SIZE);
      }
    }
  }

  private int applyProjectileLights(List<Projectile> projectiles, GameMap map, int tileX, int tileY, int alpha)
  {
    if (alpha <= 0 || projectiles == null || projectiles.isEmpty())
    {
      return alpha;
    }
    double bestLight = 0.0;
    for (int i = 0; i < projectiles.size(); i++)
    {
      Projectile projectile = projectiles.get(i);
      ProjectileType type = projectile.getType();
      if (type.getLightRadiusTiles() <= 0 || type.getLightIntensity() <= 0.0)
      {
        continue;
      }
      double light = this.lighting.resolveDynamicLight(map, projectile.getWorldX(), projectile.getWorldY(),
        tileX, tileY, type.getLightRadiusTiles(), type.getLightIntensity());
      if (type.getLightFalloffExponent() > 1.0)
      {
        light = Math.pow(light, type.getLightFalloffExponent());
      }
      bestLight = Math.max(bestLight, light);
      if (bestLight >= 1.0)
      {
        return 0;
      }
    }
    return bestLight <= 0.0 ? alpha : Math.max(0, alpha - (int) (bestLight * WorldLighting.MAX_DARKNESS_ALPHA));
  }

  private int applyEnemyDebuffLights(List<Enemy> enemies, GameMap map, int tileX, int tileY, int alpha, boolean hasEnemyDebuffLights)
  {
    if (!hasEnemyDebuffLights || alpha <= 0)
    {
      return alpha;
    }
    double light = this.lighting.resolveEnemyDebuffLight(map, enemies, tileX, tileY);
    if (light <= 0.0)
    {
      return alpha;
    }
    return Math.max(0, alpha - (int) (light * WorldLighting.MAX_DARKNESS_ALPHA));
  }

  private int applyRevolverFlash(GameView game, GameMap map, int tileX, int tileY, int alpha)
  {
    if (game.getRevolverFlashTicks() <= 0 || alpha <= 0)
    {
      return alpha;
    }
    double light = this.lighting.resolveDynamicLight(map, game.getRevolverFlashWorldX(), game.getRevolverFlashWorldY(),
      tileX, tileY, game.getRevolverFlashRadiusTiles(), game.getRevolverFlashIntensity());
    if (light <= 0.0)
    {
      return alpha;
    }
    return Math.max(0, alpha - (int) (light * WorldLighting.MAX_DARKNESS_ALPHA));
  }

  private Color getDarknessColor(int alpha)
  {
    Color color = this.darknessColors[alpha];
    if (color == null)
    {
      color = new Color(0, 0, 0, alpha);
      this.darknessColors[alpha] = color;
    }
    return color;
  }
}
