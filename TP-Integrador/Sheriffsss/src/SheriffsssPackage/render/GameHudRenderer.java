package SheriffsssPackage.render;

import SheriffsssPackage.context.GameConfig;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

public class GameHudRenderer
{
  private static final int HUD_BOTTOM_MARGIN = 22;
  private static final int INFO_MSG_BG_X_OFFSET = 10;
  private static final int INFO_MSG_BG_Y_ABOVE_TEXT = 17;
  private static final int INFO_MSG_BG_EXTRA_WIDTH = 14;
  private static final int INFO_MSG_BG_HEIGHT = 22;
  private static final int INFO_MSG_TEXT_X = 17;
  private static final int INFO_MSG_LINE_SPACING = 26;

  private final Font infoMessageFont = new Font("Arial", Font.BOLD, 15);

  public void drawInfoMessages(Graphics2D g2, GameView game)
  {
    int drawY = GameConfig.SCREEN_HEIGHT - HUD_BOTTOM_MARGIN;
    for (int i = 0; i < game.getInfoMessageSlotCount(); i++)
    {
      String message = game.getInfoMessage(i);
      if (message == null || game.getInfoMessageTicks(i) <= 0)
      {
        continue;
      }
      int width = g2.getFontMetrics(this.infoMessageFont).stringWidth(message);
      g2.setColor(GameTheme.TRANSPARENT_BLACK);
      g2.fillRect(INFO_MSG_BG_X_OFFSET, drawY - INFO_MSG_BG_Y_ABOVE_TEXT, width + INFO_MSG_BG_EXTRA_WIDTH, INFO_MSG_BG_HEIGHT);
      TextRenderer.draw(g2, this.infoMessageFont, message, Color.WHITE, INFO_MSG_TEXT_X, drawY, false);
      drawY -= INFO_MSG_LINE_SPACING;
    }
  }

  public void drawDebugAndUi(Graphics2D g2, GameView game)
  {
    SheriffsssPackage.session.Player player = game.getPlayer();
    if (player.getTakingDamage())
    {
      g2.setColor(GameTheme.DAMAGE_RED);
      g2.fillRect(0, 0, GameConfig.SCREEN_WIDTH, GameConfig.SCREEN_HEIGHT);
    }
  }
}
