package SheriffsssPackage.render;

import SheriffsssPackage.context.AssetManager;
import SheriffsssPackage.context.GameConfig;
import SheriffsssPackage.session.Facing;
import SheriffsssPackage.session.MapObject;
import SheriffsssPackage.session.Player;
import SheriffsssPackage.system.weapon.ItemDefinition;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

public class HeldItemRenderer
{
  private final AssetManager assets;
  private final Camera camera;

  public HeldItemRenderer(AssetManager assets, Camera camera)
  {
    this.assets = assets;
    this.camera = camera;
  }

  public boolean isHeldItemRendered(GameView game, Player player)
  {
    ItemDefinition equippedWeapon = player.getEquipment().getEquippedWeapon();
    if (equippedWeapon == null || !equippedWeapon.isHandEquipable())
    {
      return false;
    }
    return game.isUsingTool() || player.shouldRenderHeldItem();
  }

  public void drawHeldItem(Graphics2D g2, GameView game, Player player)
  {
    drawHeldItemAt(g2, player, GameConfig.SCREEN_CENTER_X, GameConfig.SCREEN_CENTER_Y, game.isUsingTool(), game.getToolUseTicks(), game.getToolUseDurationTicks());
  }

  public void drawHeldItems(Graphics2D g2, GameView game, Player localPlayer)
  {
    if (!localPlayer.isDead()) {
      drawHeldItem(g2, game, localPlayer);
    }
  }

  public void drawHeldItemAt(Graphics2D g2, Player player, int centerX, int centerY, boolean usingTool, int toolUseTicks, int toolUseDurationTicks)
  {
    ItemDefinition definition = player.getEquipment().getEquippedWeapon();
    if (definition == null || !definition.isHandEquipable() || (!usingTool && !player.shouldRenderHeldItem()))
    {
      return;
    }
    ItemDefinitionDrawConfig drawConfig = definition.getDrawConfig();
    BufferedImage itemSprite = this.assets.getImage(definition.getSpritePath());
    int itemWidth = definition.getHeldDrawWidth();
    int itemHeight = definition.getHeldDrawHeight();
    Facing facing = player.getFacing();
    int drawX = centerX + drawConfig.getBaseOffsetX(facing);
    int drawY = centerY + drawConfig.getBaseOffsetY(facing);
    double baseAngle = drawConfig.getBaseAngle(facing);
    double recoilAngle = 0.0;
    if (usingTool)
    {
      double swing = toolSwing(toolUseTicks, toolUseDurationTicks);
      drawX += drawConfig.getSwingOffsetX(facing, swing);
      drawY += drawConfig.getSwingOffsetY(facing, swing);
      recoilAngle = drawConfig.getSwingAngle(facing, swing);
    }
    AffineTransform previousTransform = g2.getTransform();
    applyHeldItemRotation(g2, drawConfig, facing, drawX, drawY, itemWidth, itemHeight, baseAngle, recoilAngle);
    if (drawConfig.isMirrored(facing))
    {
      g2.drawImage(itemSprite, drawX + itemWidth, drawY, -itemWidth, itemHeight, null);
    } else {
      g2.drawImage(itemSprite, drawX, drawY, itemWidth, itemHeight, null);
    }
    g2.setTransform(previousTransform);
  }

  public ItemDefinitionDrawConfig heldItemDrawConfig(Player player)
  {
    ItemDefinition equippedWeapon = player.getEquipment().getEquippedWeapon();
    return equippedWeapon == null ? ItemDefinitionDrawConfig.DEFAULT : equippedWeapon.getDrawConfig();
  }

  public void drawToolTargetBar(Graphics2D g2, GameView game)
  {
    MapObject targetObject = game.getToolTargetObject();
    if (targetObject == null || !targetObject.getType().isBreakable())
    {
      return;
    }
    double durability = targetObject.getType().getDurability();
    if (durability <= 0.0)
    {
      return;
    }
    double remainingRatio = 1.0 - Math.min(1.0, targetObject.getDurabilityDamage() / durability);
    int barWidth = 52;
    int barHeight = 8;
    int objectWidth = targetObject.getType().getFootprintWidth() * GameConfig.TILE_SIZE;
    int screenX = this.camera.tileToScreenX(targetObject.getRootTileX()) + objectWidth / 2 - barWidth / 2;
    int screenY = this.camera.tileToScreenY(targetObject.getRootTileY()) - 12;
    g2.setColor(Color.BLACK);
    g2.fillRect(screenX - 2, screenY - 2, barWidth + 4, barHeight + 4);
    g2.setColor(Color.RED);
    g2.fillRect(screenX, screenY, barWidth, barHeight);
    g2.setColor(Color.GREEN);
    g2.fillRect(screenX, screenY, (int) (barWidth * remainingRatio), barHeight);
    g2.setColor(Color.BLACK);
    g2.setStroke(GameTheme.LIGHT_STROKE);
    g2.drawRect(screenX, screenY, barWidth, barHeight);
  }

  public void applyHeldItemRotation(Graphics2D g2, ItemDefinitionDrawConfig drawConfig, Facing facing, int drawX, int drawY,
    int itemWidth, int itemHeight, double baseAngle, double recoilAngle)
  {
    double centerX = drawX + itemWidth / 2.0;
    double centerY = drawY + itemHeight / 2.0;
    if (recoilAngle != 0.0) {
      double anchorX = heldItemGripAnchorX(drawConfig, facing, drawX, itemWidth);
      double anchorY = heldItemGripAnchorY(drawConfig, drawY, itemHeight);
      double rotatedAnchorX = rotateX(anchorX, anchorY, centerX, centerY, baseAngle);
      double rotatedAnchorY = rotateY(anchorX, anchorY, centerX, centerY, baseAngle);
      g2.rotate(recoilAngle, rotatedAnchorX, rotatedAnchorY);
    }
    g2.rotate(baseAngle, centerX, centerY);
  }

  private double heldItemGripAnchorX(ItemDefinitionDrawConfig drawConfig, Facing facing, int drawX, int itemWidth)
  {
    int offsetX = drawConfig.isMirrored(facing) ? -drawConfig.getGripAnchorOffsetX() : drawConfig.getGripAnchorOffsetX();
    return (drawConfig.isMirrored(facing) ? drawX + itemWidth : drawX) + offsetX;
  }

  private double heldItemBarrelAnchorX(ItemDefinitionDrawConfig drawConfig, Facing facing, int drawX, int itemWidth)
  {
    return drawConfig.isMirrored(facing) ? drawX : drawX + itemWidth;
  }

  private double heldItemGripAnchorY(ItemDefinitionDrawConfig drawConfig, int drawY, int itemHeight)
  {
    return drawY + itemHeight + drawConfig.getGripAnchorOffsetY();
  }

  private double heldItemBarrelAnchorY(int drawY)
  {
    return drawY;
  }

  private double rotateX(double x, double y, double centerX, double centerY, double angle)
  {
    double deltaX = x - centerX;
    double deltaY = y - centerY;
    return centerX + deltaX * Math.cos(angle) - deltaY * Math.sin(angle);
  }

  private double rotateY(double x, double y, double centerX, double centerY, double angle)
  {
    double deltaX = x - centerX;
    double deltaY = y - centerY;
    return centerY + deltaX * Math.sin(angle) + deltaY * Math.cos(angle);
  }

  public double heldItemGripAnchorCanvasX(Player player, ItemDefinition definition)
  {
    ItemDefinitionDrawConfig drawConfig = definition.getDrawConfig();
    Facing facing = player.getFacing();
    int itemWidth = definition.getHeldDrawWidth();
    int itemHeight = definition.getHeldDrawHeight();
    int drawX = GameConfig.SCREEN_CENTER_X + drawConfig.getBaseOffsetX(facing);
    int drawY = GameConfig.SCREEN_CENTER_Y + drawConfig.getBaseOffsetY(facing);
    double centerX = drawX + itemWidth / 2.0;
    double centerY = drawY + itemHeight / 2.0;
    double anchorX = heldItemGripAnchorX(drawConfig, facing, drawX, itemWidth);
    double anchorY = heldItemGripAnchorY(drawConfig, drawY, itemHeight);
    return rotateX(anchorX, anchorY, centerX, centerY, drawConfig.getBaseAngle(facing));
  }

  public double heldItemGripAnchorCanvasY(Player player, ItemDefinition definition)
  {
    ItemDefinitionDrawConfig drawConfig = definition.getDrawConfig();
    Facing facing = player.getFacing();
    int itemWidth = definition.getHeldDrawWidth();
    int itemHeight = definition.getHeldDrawHeight();
    int drawX = GameConfig.SCREEN_CENTER_X + drawConfig.getBaseOffsetX(facing);
    int drawY = GameConfig.SCREEN_CENTER_Y + drawConfig.getBaseOffsetY(facing);
    double centerX = drawX + itemWidth / 2.0;
    double centerY = drawY + itemHeight / 2.0;
    double anchorX = heldItemGripAnchorX(drawConfig, facing, drawX, itemWidth);
    double anchorY = heldItemGripAnchorY(drawConfig, drawY, itemHeight);
    return rotateY(anchorX, anchorY, centerX, centerY, drawConfig.getBaseAngle(facing));
  }

  public double heldItemBarrelAnchorCanvasX(Player player, ItemDefinition definition)
  {
    ItemDefinitionDrawConfig drawConfig = definition.getDrawConfig();
    Facing facing = player.getFacing();
    int itemWidth = definition.getHeldDrawWidth();
    int itemHeight = definition.getHeldDrawHeight();
    int drawX = GameConfig.SCREEN_CENTER_X + drawConfig.getBaseOffsetX(facing);
    int drawY = GameConfig.SCREEN_CENTER_Y + drawConfig.getBaseOffsetY(facing);
    double centerX = drawX + itemWidth / 2.0;
    double centerY = drawY + itemHeight / 2.0;
    double anchorX = heldItemBarrelAnchorX(drawConfig, facing, drawX, itemWidth);
    double anchorY = heldItemBarrelAnchorY(drawY);
    return rotateX(anchorX, anchorY, centerX, centerY, drawConfig.getBaseAngle(facing));
  }

  public double heldItemBarrelAnchorCanvasY(Player player, ItemDefinition definition)
  {
    ItemDefinitionDrawConfig drawConfig = definition.getDrawConfig();
    Facing facing = player.getFacing();
    int itemWidth = definition.getHeldDrawWidth();
    int itemHeight = definition.getHeldDrawHeight();
    int drawX = GameConfig.SCREEN_CENTER_X + drawConfig.getBaseOffsetX(facing);
    int drawY = GameConfig.SCREEN_CENTER_Y + drawConfig.getBaseOffsetY(facing);
    double centerX = drawX + itemWidth / 2.0;
    double centerY = drawY + itemHeight / 2.0;
    double anchorX = heldItemBarrelAnchorX(drawConfig, facing, drawX, itemWidth);
    double anchorY = heldItemBarrelAnchorY(drawY);
    return rotateY(anchorX, anchorY, centerX, centerY, drawConfig.getBaseAngle(facing));
  }

  public double toolSwing(int toolUseTicks, int toolUseDurationTicks)
  {
    int duration = Math.max(1, toolUseDurationTicks);
    if (duration <= 1)
    {
      return 0.0;
    }
    double progress = Math.max(0.0, Math.min(1.0, toolUseTicks / (double) (duration - 1)));
    return Math.sin(progress * Math.PI);
  }

  public ItemDefinition equippedHandDefinition(Player player)
  {
    if (player == null)
    {
      return null;
    }
    ItemDefinition equippedWeapon = player.getEquipment().getEquippedWeapon();
    if (equippedWeapon == null || !equippedWeapon.isHandEquipable())
    {
      return null;
    }
    return equippedWeapon;
  }

  public double mouseWorldX(GameView game)
  {
    return game.getCameraCenterWorldX() + (game.getInput().getMouseX() - GameConfig.SCREEN_CENTER_X) / game.getCameraZoom();
  }

  public double mouseWorldY(GameView game)
  {
    return game.getCameraCenterWorldY() + (game.getInput().getMouseY() - GameConfig.SCREEN_CENTER_Y) / game.getCameraZoom();
  }

  public double weaponOriginWorldX(GameView game)
  {
    Player player = game.getPlayer();
    ItemDefinition definition = equippedHandDefinition(player);
    if (player == null || definition == null)
    {
      return game.getCameraCenterWorldX();
    }
    double deltaX = mouseWorldX(game) - player.getX();
    double deltaY = mouseWorldY(game) - player.getY();
    Facing facing = (Math.abs(deltaX) <= 0.001 && Math.abs(deltaY) <= 0.001) ? player.getFacing() : Facing.DOWN;
    return game.heldItemOriginWorldX(player, definition, facing);
  }

  public double weaponOriginWorldY(GameView game)
  {
    Player player = game.getPlayer();
    ItemDefinition definition = equippedHandDefinition(player);
    if (player == null || definition == null)
    {
      return game.getCameraCenterWorldY();
    }
    double deltaX = mouseWorldX(game) - player.getX();
    double deltaY = mouseWorldY(game) - player.getY();
    Facing facing = (Math.abs(deltaX) <= 0.001 && Math.abs(deltaY) <= 0.001) ? player.getFacing() : Facing.DOWN;
    return game.heldItemOriginWorldY(player, definition, facing);
  }

  public double weaponOriginCanvasX(Player player, ItemDefinition definition, GameView game)
  {
    return worldToCanvasX(game, weaponOriginWorldX(game));
  }

  public double weaponOriginCanvasY(Player player, ItemDefinition definition, GameView game)
  {
    return worldToCanvasY(game, weaponOriginWorldY(game));
  }

  public double worldToCanvasX(GameView game, double worldX)
  {
    return worldX - game.getCameraCenterWorldX() + GameConfig.SCREEN_CENTER_X;
  }

  public double worldToCanvasY(GameView game, double worldY)
  {
    return worldY - game.getCameraCenterWorldY() + GameConfig.SCREEN_CENTER_Y;
  }
}
