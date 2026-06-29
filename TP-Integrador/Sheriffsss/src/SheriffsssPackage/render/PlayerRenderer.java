package SheriffsssPackage.render;

import SheriffsssPackage.context.AssetManager;
import SheriffsssPackage.context.GameConfig;
import SheriffsssPackage.session.Player;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class PlayerRenderer
{
  private final AssetManager assets;
  private final Camera camera;

  public PlayerRenderer(AssetManager assets, Camera camera)
  {
    this.assets = assets;
    this.camera = camera;
  }

  public void drawPlayers(Graphics2D g2, GameView game, Player localPlayer)
  {
    if (!localPlayer.isDead()) {
      drawPlayer(g2, localPlayer);
    }
  }

  private void drawPlayer(Graphics2D g2, Player player)
  {
    drawPlayerAt(g2, player, GameConfig.SCREEN_CENTER_X - Player.PLAYER_WIDTH / 2, GameConfig.SCREEN_CENTER_Y - Player.PLAYER_HEIGHT / 2);
  }

  private void drawPlayerAt(Graphics2D g2, Player player, int drawX, int drawY)
  {
    BufferedImage sprite = player.getCurrentImage();
    int drawWidth = fittedPlayerSpriteWidth(sprite);
    int drawHeight = fittedPlayerSpriteHeight(sprite);
    int spriteX = drawX + (Player.PLAYER_WIDTH - drawWidth) / 2;
    int spriteY = drawY + (Player.PLAYER_HEIGHT - drawHeight) / 2;
    g2.drawImage(sprite, spriteX, spriteY, drawWidth, drawHeight, null);
  }

  public int fittedPlayerSpriteWidth(BufferedImage sprite)
  {
    double scale = Math.min(Player.PLAYER_WIDTH / (double) sprite.getWidth(), Player.PLAYER_HEIGHT / (double) sprite.getHeight());
    return Math.max(1, (int) Math.round(sprite.getWidth() * scale));
  }

  public int fittedPlayerSpriteHeight(BufferedImage sprite)
  {
    double scale = Math.min(Player.PLAYER_WIDTH / (double) sprite.getWidth(), Player.PLAYER_HEIGHT / (double) sprite.getHeight());
    return Math.max(1, (int) Math.round(sprite.getHeight() * scale));
  }
}
