package SheriffsssPackage.render;

import SheriffsssPackage.context.GameConfig;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

public class GameHudRenderer
{
  private final Font infoMessageFont = new Font("Arial", Font.BOLD, 15);

  public void drawInfoMessages(Graphics2D g2, GameView game)
  {
    int drawY = GameConfig.SCREEN_HEIGHT - 22;
    for (int i = 0; i < game.getInfoMessageSlotCount(); i++)
    {
      String message = game.getInfoMessage(i);
      if (message == null || game.getInfoMessageTicks(i) <= 0)
      {
        continue;
      }
      int width = g2.getFontMetrics(this.infoMessageFont).stringWidth(message);
      g2.setColor(GameTheme.TRANSPARENT_BLACK);
      g2.fillRect(10, drawY - 17, width + 14, 22);
      TextRenderer.draw(g2, this.infoMessageFont, message, Color.WHITE, 17, drawY, false);
      drawY -= 26;
    }
  }

  public void drawDebugAndUi(Graphics2D g2, GameView game)
  {
    SheriffsssPackage.session.Player player = game.getPlayer();
    if (game.isSpectating())
    {
      return;
    }
    if (player.getTakingDamage())
    {
      g2.setColor(GameTheme.DAMAGE_RED);
      g2.fillRect(0, 0, GameConfig.SCREEN_WIDTH, GameConfig.SCREEN_HEIGHT);
    }
  }
}
