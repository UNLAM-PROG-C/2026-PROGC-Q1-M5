package SheriffsssPackage.render;

import SheriffsssPackage.context.DebugBulletTrajectory;
import SheriffsssPackage.context.DebugOptions;
import SheriffsssPackage.context.GameConfig;
import SheriffsssPackage.session.Facing;
import SheriffsssPackage.session.GameMap;
import SheriffsssPackage.session.MapObject;
import SheriffsssPackage.session.Player;
import SheriffsssPackage.system.enemy.Enemy;
import SheriffsssPackage.system.weapon.ItemDefinition;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;

public class DebugWorldRenderer
{
  private final Camera camera;
  private final Color debugHitboxColor = new Color(255, 80, 80, 210);
  private final Color debugSpritePerimeterColor = new Color(190, 120, 255, 230);
  private final Color debugLineColor = new Color(90, 220, 255, 210);
  private final Color debugOriginColor = new Color(255, 230, 80, 230);
  private final Color debugWeaponOriginColor = new Color(120, 255, 190, 235);
  private final Color debugWeaponGripAnchorColor = new Color(255, 140, 230, 235);
  private final Color debugWeaponBarrelAnchorColor = new Color(140, 190, 255, 235);
  private final Color debugBulletTrajectoryColor = new Color(255, 255, 255, 170);
  private final Color debugTrainingFailurePerimeterColor = new Color(255, 70, 70, 220);
  private final Color debugFullConeColor = new Color(255, 140, 60, 115);
  private final Color debugFullConeOutlineColor = new Color(255, 140, 60, 230);
  private final Color debugWeaponConeColor = new Color(80, 255, 140, 140);
  private final Color debugWeaponConeOutlineColor = new Color(80, 255, 140, 230);
  private final java.awt.Stroke debugStroke = new BasicStroke(2f);
  private final Path2D.Double debugConePath = new Path2D.Double();
  private final Line2D.Double debugLine = new Line2D.Double();
  private final PlayerRenderer playerRenderer;
  private final HeldItemRenderer heldItemRenderer;

  public DebugWorldRenderer(Camera camera, PlayerRenderer playerRenderer, HeldItemRenderer heldItemRenderer)
  {
    this.camera = camera;
    this.playerRenderer = playerRenderer;
    this.heldItemRenderer = heldItemRenderer;
  }

  public void drawDebugWorld(Graphics2D g2, GameView game)
  {
    DebugOptions debug = game.getDebugOptions();
    Player player = game.getPlayer();
    if (!game.getTrainingHudView().active() || debug == null || player == null || player.isDead())
    {
      return;
    }
    Composite previousComposite = g2.getComposite();
    java.awt.Stroke previousStroke = g2.getStroke();
    g2.setStroke(this.debugStroke);
    if (debug.shouldDrawFullAccuracyCone())
    {
      drawAccuracyCone(g2, game, 0.0, this.debugFullConeColor, this.debugFullConeOutlineColor);
    }
    if (debug.shouldDrawWeaponAccuracyCone())
    {
      drawAccuracyCone(g2, game, game.getEquippedPlayerAccuracy(), this.debugWeaponConeColor, this.debugWeaponConeOutlineColor);
    }
    if (debug.shouldDrawBulletTrajectories())
    {
      drawDebugBulletTrajectories(g2, debug);
    }
    if (debug.shouldDrawTrainingFailurePerimeter())
    {
      drawDebugTrainingFailurePerimeter(g2, game);
    }
    if (debug.shouldDrawPlayerMouseLine())
    {
      g2.setColor(this.debugLineColor);
      this.debugLine.setLine(GameConfig.SCREEN_CENTER_X, GameConfig.SCREEN_CENTER_Y, mouseCanvasX(game), mouseCanvasY(game));
      g2.draw(this.debugLine);
    }
    if (debug.shouldDrawHitboxes())
    {
      drawDebugHitboxes(g2, game);
    }
    if (debug.shouldDrawSpritePerimeters())
    {
      drawDebugSpritePerimeters(g2, game);
    }
    if (debug.shouldDrawPlayerOrigin())
    {
      drawDebugOrigin(g2);
    }
    if (debug.shouldDrawWeaponOrigin())
    {
      drawDebugWeaponOrigin(g2, game);
    }
    if (debug.shouldDrawWeaponGripAnchor())
    {
      drawDebugWeaponGripAnchor(g2, game);
    }
    if (debug.shouldDrawWeaponBarrelAnchor())
    {
      drawDebugWeaponBarrelAnchor(g2, game);
    }
    g2.setStroke(previousStroke);
    g2.setComposite(previousComposite);
  }

  private void drawDebugHitboxes(Graphics2D g2, GameView game)
  {
    g2.setColor(this.debugHitboxColor);
    Player player = game.getPlayer();
    g2.drawRect(GameConfig.SCREEN_CENTER_X - player.getHitboxWidth() / 2, GameConfig.SCREEN_CENTER_Y - player.getHitboxHeight() / 2,
      player.getHitboxWidth(), player.getHitboxHeight());
    for (int i = 0; i < game.getEnemies().size(); i++)
    {
      Enemy enemy = game.getEnemies().get(i);
      int radius = enemy.getType().getCollisionRadius();
      int screenX = this.camera.worldToScreenX(enemy.getWorldX());
      int screenY = this.camera.worldToScreenY(enemy.getWorldY());
      g2.drawOval(screenX - radius, screenY - radius, radius * 2, radius * 2);
    }
    drawDebugObjectHitboxes(g2, game.getMap());
  }

  private void drawDebugTrainingFailurePerimeter(Graphics2D g2, GameView game)
  {
    TrainingHudSnapshot trainingHud = game.getTrainingHudView().snapshot();
    if (trainingHud == null)
    {
      return;
    }
    g2.setColor(this.debugTrainingFailurePerimeterColor);
    g2.drawRect(
      this.camera.worldToScreenX(trainingHud.failurePerimeterLeftWorldX()),
      this.camera.worldToScreenY(trainingHud.failurePerimeterTopWorldY()),
      trainingHud.failurePerimeterWidthWorld(),
      trainingHud.failurePerimeterHeightWorld()
    );
  }

  private void drawDebugObjectHitboxes(Graphics2D g2, GameMap map)
  {
    if (map == null)
    {
      return;
    }
    for (int tileX = this.camera.getStartTileX(); tileX <= this.camera.getEndTileX(); tileX++)
    {
      for (int tileY = this.camera.getStartTileY(); tileY <= this.camera.getEndTileY(); tileY++)
      {
        MapObject mapObject = map.getObject(tileX, tileY);
        if (mapObject == null || !mapObject.isSolid())
        {
          continue;
        }
        int left = map.objectCollisionLeftWorldX(tileX, mapObject);
        int top = map.objectCollisionTopWorldY(tileY, mapObject);
        g2.drawRect(this.camera.worldToScreenX(left), this.camera.worldToScreenY(top),
          mapObject.getType().getCollisionWidth(), mapObject.getType().getCollisionHeight());
      }
    }
  }

  private void drawDebugSpritePerimeters(Graphics2D g2, GameView game)
  {
    drawDebugPlayerSpritePerimeter(g2, game.getPlayer());
    drawDebugHeldItemSpritePerimeter(g2, game);
  }

  private void drawDebugPlayerSpritePerimeter(Graphics2D g2, Player player)
  {
    java.awt.image.BufferedImage sprite = player.getCurrentImage();
    int drawWidth = this.playerRenderer.fittedPlayerSpriteWidth(sprite);
    int drawHeight = this.playerRenderer.fittedPlayerSpriteHeight(sprite);
    int drawX = GameConfig.SCREEN_CENTER_X - SheriffsssPackage.session.Player.PLAYER_WIDTH / 2;
    int drawY = GameConfig.SCREEN_CENTER_Y - SheriffsssPackage.session.Player.PLAYER_HEIGHT / 2;
    int spriteX = drawX + (SheriffsssPackage.session.Player.PLAYER_WIDTH - drawWidth) / 2;
    int spriteY = drawY + (SheriffsssPackage.session.Player.PLAYER_HEIGHT - drawHeight) / 2;
    g2.setColor(this.debugSpritePerimeterColor);
    g2.drawRect(spriteX, spriteY, drawWidth, drawHeight);
  }

  private void drawDebugHeldItemSpritePerimeter(Graphics2D g2, GameView game)
  {
    Player player = game.getPlayer();
    ItemDefinition definition = this.heldItemRenderer.equippedHandDefinition(player);
    if (definition == null)
    {
      return;
    }
    ItemDefinitionDrawConfig drawConfig = definition.getDrawConfig();
    Facing facing = player.getFacing();
    int itemWidth = definition.getHeldDrawWidth();
    int itemHeight = definition.getHeldDrawHeight();
    int drawX = GameConfig.SCREEN_CENTER_X + drawConfig.getBaseOffsetX(facing);
    int drawY = GameConfig.SCREEN_CENTER_Y + drawConfig.getBaseOffsetY(facing);
    double baseAngle = drawConfig.getBaseAngle(facing);
    double recoilAngle = 0.0;
    if (game.isUsingTool())
    {
      double swing = this.heldItemRenderer.toolSwing(game.getToolUseTicks(), game.getToolUseDurationTicks());
      drawX += drawConfig.getSwingOffsetX(facing, swing);
      drawY += drawConfig.getSwingOffsetY(facing, swing);
      recoilAngle = drawConfig.getSwingAngle(facing, swing);
    }
    java.awt.geom.AffineTransform previousTransform = g2.getTransform();
    this.heldItemRenderer.applyHeldItemRotation(g2, drawConfig, facing, drawX, drawY, itemWidth, itemHeight, baseAngle, recoilAngle);
    g2.setColor(this.debugSpritePerimeterColor);
    g2.drawRect(drawX, drawY, itemWidth, itemHeight);
    g2.setTransform(previousTransform);
  }

  private void drawDebugOrigin(Graphics2D g2)
  {
    int radius = 2;
    g2.setColor(this.debugOriginColor);
    g2.fillOval(GameConfig.SCREEN_CENTER_X - radius, GameConfig.SCREEN_CENTER_Y - radius, radius * 2, radius * 2);
    g2.setColor(Color.BLACK);
    g2.drawOval(GameConfig.SCREEN_CENTER_X - radius, GameConfig.SCREEN_CENTER_Y - radius, radius * 2, radius * 2);
  }

  private void drawDebugWeaponOrigin(Graphics2D g2, GameView game)
  {
    Player player = game.getPlayer();
    ItemDefinition definition = this.heldItemRenderer.equippedHandDefinition(player);
    if (definition == null)
    {
      return;
    }
    int radius = 2;
    int originX = (int) Math.round(this.heldItemRenderer.weaponOriginCanvasX(player, definition, game));
    int originY = (int) Math.round(this.heldItemRenderer.weaponOriginCanvasY(player, definition, game));
    g2.setColor(this.debugWeaponOriginColor);
    g2.fillOval(originX - radius, originY - radius, radius * 2, radius * 2);
    g2.setColor(Color.BLACK);
    g2.drawOval(originX - radius, originY - radius, radius * 2, radius * 2);
  }

  private void drawDebugWeaponGripAnchor(Graphics2D g2, GameView game)
  {
    Player player = game.getPlayer();
    ItemDefinition definition = this.heldItemRenderer.equippedHandDefinition(player);
    if (definition == null)
    {
      return;
    }
    int radius = 3;
    int anchorX = (int) Math.round(this.heldItemRenderer.heldItemGripAnchorCanvasX(player, definition));
    int anchorY = (int) Math.round(this.heldItemRenderer.heldItemGripAnchorCanvasY(player, definition));
    g2.setColor(this.debugWeaponGripAnchorColor);
    g2.fillOval(anchorX - radius, anchorY - radius, radius * 2, radius * 2);
    g2.setColor(Color.BLACK);
    g2.drawOval(anchorX - radius, anchorY - radius, radius * 2, radius * 2);
  }

  private void drawDebugWeaponBarrelAnchor(Graphics2D g2, GameView game)
  {
    Player player = game.getPlayer();
    ItemDefinition definition = this.heldItemRenderer.equippedHandDefinition(player);
    if (definition == null)
    {
      return;
    }
    int radius = 3;
    int anchorX = (int) Math.round(this.heldItemRenderer.heldItemBarrelAnchorCanvasX(player, definition));
    int anchorY = (int) Math.round(this.heldItemRenderer.heldItemBarrelAnchorCanvasY(player, definition));
    g2.setColor(this.debugWeaponBarrelAnchorColor);
    g2.fillOval(anchorX - radius, anchorY - radius, radius * 2, radius * 2);
    g2.setColor(Color.BLACK);
    g2.drawOval(anchorX - radius, anchorY - radius, radius * 2, radius * 2);
  }

  private void drawDebugBulletTrajectories(Graphics2D g2, DebugOptions debug)
  {
    java.util.List<DebugBulletTrajectory> trajectories = debug.getBulletTrajectories();
    g2.setColor(this.debugBulletTrajectoryColor);
    for (int i = 0; i < trajectories.size(); i++)
    {
      DebugBulletTrajectory trajectory = trajectories.get(i);
      g2.drawLine(this.camera.worldToScreenX(trajectory.getStartWorldX()), this.camera.worldToScreenY(trajectory.getStartWorldY()),
        this.camera.worldToScreenX(trajectory.getEndWorldX()), this.camera.worldToScreenY(trajectory.getEndWorldY()));
    }
  }

  private void drawAccuracyCone(Graphics2D g2, GameView game, double accuracy, Color fillColor, Color outlineColor)
  {
    double targetWorldX = this.heldItemRenderer.mouseWorldX(game);
    double targetWorldY = this.heldItemRenderer.mouseWorldY(game);
    double originWorldX = this.heldItemRenderer.weaponOriginWorldX(game);
    double originWorldY = this.heldItemRenderer.weaponOriginWorldY(game);
    double deltaX = targetWorldX - originWorldX;
    double deltaY = targetWorldY - originWorldY;
    double length = Math.sqrt(deltaX * deltaX + deltaY * deltaY);
    if (length <= 0.001)
    {
      return;
    }
    double clampedAccuracy = Math.max(0.0, Math.min(1.0, accuracy));
    double halfBase = length * 0.5 * (1.0 - clampedAccuracy);
    double baseUnitX = -deltaY / length;
    double baseUnitY = deltaX / length;
    double tipX = this.heldItemRenderer.worldToCanvasX(game, originWorldX);
    double tipY = this.heldItemRenderer.worldToCanvasY(game, originWorldY);
    double leftX = this.heldItemRenderer.worldToCanvasX(game, targetWorldX + baseUnitX * halfBase);
    double leftY = this.heldItemRenderer.worldToCanvasY(game, targetWorldY + baseUnitY * halfBase);
    double rightX = this.heldItemRenderer.worldToCanvasX(game, targetWorldX - baseUnitX * halfBase);
    double rightY = this.heldItemRenderer.worldToCanvasY(game, targetWorldY - baseUnitY * halfBase);
    this.debugConePath.reset();
    this.debugConePath.moveTo(tipX, tipY);
    this.debugConePath.lineTo(leftX, leftY);
    this.debugConePath.lineTo(rightX, rightY);
    this.debugConePath.closePath();
    g2.setColor(fillColor);
    g2.fill(this.debugConePath);
    g2.setColor(outlineColor);
    g2.draw(this.debugConePath);
  }

  private double mouseCanvasX(GameView game)
  {
    return GameConfig.SCREEN_CENTER_X + (game.getInput().getMouseX() - GameConfig.SCREEN_CENTER_X) / game.getCameraZoom();
  }

  private double mouseCanvasY(GameView game)
  {
    return GameConfig.SCREEN_CENTER_Y + (game.getInput().getMouseY() - GameConfig.SCREEN_CENTER_Y) / game.getCameraZoom();
  }
}
