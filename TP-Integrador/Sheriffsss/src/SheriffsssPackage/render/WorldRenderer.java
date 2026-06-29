package SheriffsssPackage.render;

import SheriffsssPackage.context.AssetManager;
import SheriffsssPackage.context.GameConfig;
import SheriffsssPackage.session.GameMap;
import SheriffsssPackage.session.MapObject;
import SheriffsssPackage.session.MapObjectType;
import SheriffsssPackage.session.Player;
import SheriffsssPackage.session.TileType;
import SheriffsssPackage.system.weapon.Projectile;
import SheriffsssPackage.system.weapon.ProjectileType;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.List;

public class WorldRenderer
{
  private final AssetManager assets;
  private final Camera camera;
  private final Color[] sunsetTintColors = new Color[69];

  public WorldRenderer(AssetManager assets, Camera camera)
  {
    this.assets = assets;
    this.camera = camera;
  }

  public void renderTiles(Graphics2D g2, GameMap map, GameView game)
  {
    for (int tileX = this.camera.getStartTileX(); tileX <= this.camera.getEndTileX(); tileX++)
    {
      for (int tileY = this.camera.getStartTileY(); tileY <= this.camera.getEndTileY(); tileY++) {
        int screenX = this.camera.tileToScreenX(tileX);
        int screenY = this.camera.tileToScreenY(tileY);
        TileType tileType = map.getTile(tileX, tileY);
        BufferedImage tileSprite;
        if (tileType == null)
        {
          tileSprite = game.getTrainingHudView().active()
            ? TileType.SAND.getSprite(this.assets, game.getFrameCount())
            : this.assets.getImage("sprites/Pasto.png");
        } else {
          tileSprite = tileType.getSprite(this.assets, game.getFrameCount());
        }
        g2.drawImage(tileSprite, screenX, screenY, GameConfig.TILE_SIZE, GameConfig.TILE_SIZE, null);
      }
    }
  }

  public void drawGroundObjects(Graphics2D g2, GameMap map) {
    int scanStartX = this.camera.getStartTileX() - MapObjectType.getMaxFootprintWidth() + 1;
    int scanEndX = this.camera.getEndTileX();
    int scanStartY = this.camera.getStartTileY() - MapObjectType.getMaxFootprintHeight() + 1;
    int scanEndY = this.camera.getEndTileY();
    for (int tileX = scanStartX; tileX <= scanEndX; tileX++)
    {
      for (int tileY = scanStartY; tileY <= scanEndY; tileY++) {
        MapObject mapObject = map.getObject(tileX, tileY);
        if (mapObject != null && mapObject.isRootCell(tileX, tileY) && intersectsVisibleArea(mapObject))
        {
          drawMapObject(g2, mapObject);
        }
      }
    }
  }

  public void drawSunsetTint(Graphics2D g2, GameView game)
  {
    int alpha = game.getDayNightCycle().getSunsetTintAlpha();
    if (alpha <= 0)
    {
      return;
    }
    g2.setColor(getSunsetTintColor(alpha));
    g2.fillRect(0, 0, GameConfig.SCREEN_WIDTH, GameConfig.SCREEN_HEIGHT);
  }

  public void drawMapObjectOverlay(Graphics2D g2, GameMap map, Player player)
  {
    MapObject objectAtFeet = map.getObjectAtWorld(player.getX(), player.getFeetWorldY());
    if (objectAtFeet == null || !objectAtFeet.isAbovePlayer())
    {
      return;
    }
    MapObject rootObject = map.getObject(objectAtFeet.getRootTileX(), objectAtFeet.getRootTileY());
    if (rootObject != null && intersectsVisibleArea(rootObject))
    {
      drawMapObject(g2, rootObject);
    }
  }

  private void drawMapObject(Graphics2D g2, MapObject mapObject)
  {
    BufferedImage sprite = this.assets.getImage(mapObject.getType().getSpritePath());
    int screenX = this.camera.tileToScreenX(mapObject.getRootTileX());
    int screenY = this.camera.tileToScreenY(mapObject.getRootTileY());
    int drawWidth = mapObject.getType().getFootprintWidth() * GameConfig.TILE_SIZE;
    int drawHeight = mapObject.getType().getFootprintHeight() * GameConfig.TILE_SIZE;
    g2.drawImage(sprite, screenX, screenY, drawWidth, drawHeight, null);
  }

  private boolean intersectsVisibleArea(MapObject mapObject)
  {
    int objectStartX = mapObject.getRootTileX();
    int objectStartY = mapObject.getRootTileY();
    int objectEndX = objectStartX + mapObject.getType().getFootprintWidth() - 1;
    int objectEndY = objectStartY + mapObject.getType().getFootprintHeight() - 1;
    return objectEndX >= this.camera.getStartTileX()
      && objectStartX <= this.camera.getEndTileX()
      && objectEndY >= this.camera.getStartTileY()
      && objectStartY <= this.camera.getEndTileY();
  }

  public void drawProjectiles(Graphics2D g2, GameView game)
  {
    List<Projectile> projectiles = game.getProjectiles();
    for (int i = 0; i < projectiles.size(); i++)
    {
      Projectile projectile = projectiles.get(i);
      ProjectileType type = projectile.getType();
      int screenX = this.camera.worldToScreenX(projectile.getWorldX());
      int screenY = this.camera.worldToScreenY(projectile.getWorldY());
      int drawWidth = type.getDrawWidth();
      int drawHeight = type.getDrawHeight();
      if (screenX < -drawWidth || screenX > GameConfig.SCREEN_WIDTH + drawWidth
        || screenY < -drawHeight || screenY > GameConfig.SCREEN_HEIGHT + drawHeight)
      {
        continue;
      }
      BufferedImage sprite = this.assets.getImage(type.getSpritePath());
      AffineTransform previousTransform = g2.getTransform();
      g2.rotate(projectile.getAngleRadians() + type.getDrawAngleOffsetRadians(), screenX, screenY);
      g2.drawImage(sprite, screenX - drawWidth / 2, screenY - drawHeight / 2, drawWidth, drawHeight, null);
      g2.setTransform(previousTransform);
    }
  }

  private Color getSunsetTintColor(int alpha) {
    Color color = this.sunsetTintColors[alpha];
    if (color == null) {
      color = new Color(255, 122, 28, alpha);
      this.sunsetTintColors[alpha] = color;
    }
    return color;
  }
}
