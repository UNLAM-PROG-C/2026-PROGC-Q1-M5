package SheriffsssPackage.render;

import SheriffsssPackage.context.GameConfig;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

public class SettingsOverlayRenderer
{
  private static final TextRenderer.Style MENU_TEXT_STYLE = TextRenderer.Style.OUTLINED;

  private final Font settingsTitleFont = new Font("Arial", Font.BOLD, 38);
  private final Font settingsFont = new Font("Arial", Font.BOLD, 18);
  private final Font settingsButtonFont = new Font("Arial", Font.BOLD, 20);

  public void drawSettingsOverlay(Graphics2D g2, GameView game)
  {
    int offsetY = game.getSettingsOverlayOffsetY();
    g2.setColor(GameTheme.TRANSPARENT_BLACK);
    g2.fillRect(0, 0, GameConfig.SCREEN_WIDTH, GameConfig.SCREEN_HEIGHT);
    g2.setColor(GameTheme.DIRT);
    g2.fillRect(GameConfig.SETTINGS_PANEL_X, GameConfig.SETTINGS_PANEL_Y + offsetY, GameConfig.SETTINGS_PANEL_WIDTH, GameConfig.SETTINGS_PANEL_HEIGHT);
    g2.setColor(Color.BLACK);
    g2.setStroke(GameTheme.HEAVY_STROKE);
    g2.drawRect(GameConfig.SETTINGS_PANEL_X, GameConfig.SETTINGS_PANEL_Y + offsetY, GameConfig.SETTINGS_PANEL_WIDTH, GameConfig.SETTINGS_PANEL_HEIGHT);

    int titleX = TextRenderer.centeredX(g2, this.settingsTitleFont, "SETTINGS", GameConfig.SCREEN_CENTER_X);
    TextRenderer.draw(g2, this.settingsTitleFont, "SETTINGS", Color.WHITE, titleX, GameConfig.SETTINGS_PANEL_Y + 58 + offsetY, MENU_TEXT_STYLE);
    drawSlider(g2, "Music", GameConfig.SETTINGS_MUSIC_SLIDER_Y + offsetY, game.getAudio().getMusicVolume());
    drawSlider(g2, "SFX", GameConfig.SETTINGS_SFX_SLIDER_Y + offsetY, game.getAudio().getSfxVolume());
    drawResolutionSlider(g2, game, offsetY);
    drawSettingsButton(g2, game, "FULLSCREEN: " + (game.isPendingFullscreen() ? "ON" : "OFF"), GameConfig.SETTINGS_FULLSCREEN_BUTTON_Y + offsetY);
    drawSettingsButton(g2, game, "RESUME", GameConfig.SETTINGS_RESUME_BUTTON_Y + offsetY);
    if (game.getTrainingHudView().active())
    {
      drawSettingsDebugButton(g2, game);
    }
    drawSettingsButton(g2, game, "MAIN MENU", GameConfig.SETTINGS_MENU_BUTTON_Y + offsetY);
    drawSettingsButton(g2, game, "QUIT", GameConfig.SETTINGS_QUIT_BUTTON_Y + offsetY);
    if (!game.getSettingsMessage().isEmpty())
    {
      int messageX = TextRenderer.centeredX(g2, this.settingsFont, game.getSettingsMessage(), GameConfig.SCREEN_CENTER_X);
      TextRenderer.draw(g2, this.settingsFont, game.getSettingsMessage(), Color.YELLOW, messageX, GameConfig.SETTINGS_PANEL_Y + GameConfig.SETTINGS_PANEL_HEIGHT - 22 + offsetY, MENU_TEXT_STYLE);
    }
  }

  private void drawSlider(Graphics2D g2, String label, int y, double value)
  {
    TextRenderer.draw(g2, this.settingsFont, label, Color.WHITE, GameConfig.SETTINGS_PANEL_X + 55, y + 6, MENU_TEXT_STYLE);
    g2.setColor(Color.BLACK);
    g2.fillRect(GameConfig.SETTINGS_SLIDER_X - 2, y - 2, GameConfig.SETTINGS_SLIDER_WIDTH + 4, 12);
    g2.setColor(Color.GRAY);
    g2.fillRect(GameConfig.SETTINGS_SLIDER_X, y, GameConfig.SETTINGS_SLIDER_WIDTH, 8);
    g2.setColor(Color.GREEN);
    g2.fillRect(GameConfig.SETTINGS_SLIDER_X, y, (int) (GameConfig.SETTINGS_SLIDER_WIDTH * value), 8);
    int knobX = GameConfig.SETTINGS_SLIDER_X + (int) (GameConfig.SETTINGS_SLIDER_WIDTH * value);
    g2.setColor(Color.WHITE);
    g2.fillRect(knobX - 5, y - 6, 10, 20);
  }

  private void drawResolutionSlider(Graphics2D g2, GameView game, int offsetY)
  {
    drawSlider(g2, "Res", GameConfig.SETTINGS_RESOLUTION_SLIDER_Y + offsetY, game.getWindowResolutionSliderValue());
    String label = game.getWindowResolutionLabel();
    int labelX = TextRenderer.centeredX(g2, this.settingsFont, label, GameConfig.SETTINGS_SLIDER_X + GameConfig.SETTINGS_SLIDER_WIDTH / 2);
    TextRenderer.draw(g2, this.settingsFont, label, Color.WHITE, labelX, GameConfig.SETTINGS_RESOLUTION_SLIDER_Y + 36 + offsetY, MENU_TEXT_STYLE);
  }

  private void drawSettingsDebugButton(Graphics2D g2, GameView game)
  {
    boolean hovered = game.isSettingsDebugButtonHovered();
    int x = game.getSettingsDebugButtonX();
    int y = game.getSettingsDebugButtonY();
    int width = game.getSettingsDebugButtonWidth();
    int height = game.getSettingsDebugButtonHeight();
    g2.setColor(game.isDebugMenuOpen() ? GameTheme.TRANSPARENT_BLACK : GameTheme.DARK_DIRT);
    g2.fillRect(x, y, width, height);
    g2.setColor(hovered ? Color.YELLOW : Color.BLACK);
    g2.setStroke(GameTheme.LIGHT_STROKE);
    g2.drawRect(x, y, width, height);
    String text = "DEBUG";
    int textX = TextRenderer.centeredX(g2, this.settingsButtonFont, text, x + width / 2);
    TextRenderer.draw(g2, this.settingsButtonFont, text, hovered ? Color.YELLOW : Color.WHITE, textX, y + 28, MENU_TEXT_STYLE);
  }

  private void drawSettingsButton(Graphics2D g2, GameView game, String text, int y)
  {
    drawSettingsButton(g2, game, text, y, true);
  }

  private void drawSettingsButton(Graphics2D g2, GameView game, String text, int y, boolean enabled)
  {
    boolean hovered = enabled && game.getInput().getMouseX() >= GameConfig.SETTINGS_BUTTON_X
      && game.getInput().getMouseX() <= GameConfig.SETTINGS_BUTTON_X + GameConfig.SETTINGS_BUTTON_WIDTH
      && game.getInput().getMouseY() >= y
      && game.getInput().getMouseY() <= y + GameConfig.SETTINGS_BUTTON_HEIGHT;
    g2.setColor(enabled ? GameTheme.DARK_DIRT : GameTheme.TRANSPARENT_BLACK);
    g2.fillRect(GameConfig.SETTINGS_BUTTON_X, y, GameConfig.SETTINGS_BUTTON_WIDTH, GameConfig.SETTINGS_BUTTON_HEIGHT);
    g2.setColor(hovered ? Color.YELLOW : enabled ? Color.BLACK : Color.GRAY);
    g2.setStroke(GameTheme.LIGHT_STROKE);
    g2.drawRect(GameConfig.SETTINGS_BUTTON_X, y, GameConfig.SETTINGS_BUTTON_WIDTH, GameConfig.SETTINGS_BUTTON_HEIGHT);
    int textX = TextRenderer.centeredX(g2, this.settingsButtonFont, text, GameConfig.SCREEN_CENTER_X);
    TextRenderer.draw(g2, this.settingsButtonFont, text, hovered ? Color.YELLOW : enabled ? Color.WHITE : Color.GRAY, textX, y + 28, MENU_TEXT_STYLE);
  }
}
