package SheriffsssPackage.render;

import SheriffsssPackage.context.DebugOptions;
import SheriffsssPackage.context.GameConfig;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

public class DebugMenuRenderer
{
  private final Font debugTitleFont = new Font("Arial", Font.BOLD, 16);
  private final Font debugFont = new Font("Arial", Font.BOLD, 13);
  private final Color debugPanelBg = new Color(0, 0, 0, 175);
  private final Color debugPanelBorder = new Color(255, 230, 120, 220);
  private final Color debugSwitchOnColor = new Color(70, 210, 110, 230);
  private final Color debugSwitchOffColor = new Color(80, 80, 80, 210);

  public void drawDebugMenu(Graphics2D g2, GameView game)
  {
    DebugOptions debug = game.getDebugOptions();
    if (!game.getTrainingHudView().active() || debug == null || !debug.isMenuOpen())
    {
      return;
    }
    java.awt.Stroke previousStroke = g2.getStroke();
    g2.setColor(this.debugPanelBg);
    g2.fillRect(DebugOptions.PANEL_X, DebugOptions.PANEL_Y, DebugOptions.PANEL_WIDTH, DebugOptions.PANEL_HEIGHT);
    g2.setColor(this.debugPanelBorder);
    g2.setStroke(GameTheme.LIGHT_STROKE);
    g2.drawRect(DebugOptions.PANEL_X, DebugOptions.PANEL_Y, DebugOptions.PANEL_WIDTH, DebugOptions.PANEL_HEIGHT);
    TextRenderer.draw(g2, this.debugTitleFont, "DEBUG", Color.WHITE, DebugOptions.PANEL_X + DebugOptions.PANEL_PADDING,
      DebugOptions.TITLE_BASELINE_Y, false);
    drawDebugSwitch(g2, debug, 0, "Hitboxes jugador/objetos/enemigos");
    drawDebugSwitch(g2, debug, 1, "Perimetros sprites");
    drawDebugSwitch(g2, debug, 2, "Linea jugador -> mouse");
    drawDebugSwitch(g2, debug, 3, "Origen jugador");
    drawDebugSwitch(g2, debug, 4, "Origen arma");
    drawDebugSwitch(g2, debug, 5, "Grip anchor");
    drawDebugSwitch(g2, debug, 6, "Barrel anchor");
    drawDebugSwitch(g2, debug, 7, "Cono accuracy completo");
    drawDebugSwitch(g2, debug, 8, "Cono accuracy arma");
    drawDebugSwitch(g2, debug, 9, "Trayectoria de bala");
    drawDebugSwitch(g2, debug, 10, "Forzar precision 100%");
    drawDebugSwitch(g2, debug, 11, "Perimetro fallo training");
    drawDebugSwitch(g2, debug, DebugOptions.UNLOCK_ALL_WEAPONS_ROW, "Desbloquear todas las armas");
    drawDebugSwitch(g2, debug, DebugOptions.SHOW_TARGET_HEALTH_BARS_ROW, "Barras vida dianas");
    drawDebugTrajectorySlider(g2, debug);
    g2.setStroke(previousStroke);
  }

  private void drawDebugSwitch(Graphics2D g2, DebugOptions debug, int row, String label)
  {
    boolean enabled = debug.isRowEnabled(row);
    int y = DebugOptions.FIRST_ROW_Y + row * DebugOptions.ROW_HEIGHT;
    g2.setColor(enabled ? this.debugSwitchOnColor : this.debugSwitchOffColor);
    g2.fillRect(DebugOptions.SWITCH_X, y, DebugOptions.SWITCH_SIZE, DebugOptions.SWITCH_SIZE);
    g2.setColor(enabled ? Color.WHITE : Color.GRAY);
    g2.drawRect(DebugOptions.SWITCH_X, y, DebugOptions.SWITCH_SIZE, DebugOptions.SWITCH_SIZE);
    if (enabled)
    {
      g2.drawLine(DebugOptions.SWITCH_X + 4, y + 9, DebugOptions.SWITCH_X + 8, y + 13);
      g2.drawLine(DebugOptions.SWITCH_X + 8, y + 13, DebugOptions.SWITCH_X + 15, y + 4);
    }
    TextRenderer.draw(g2, this.debugFont, label + (enabled ? " ON" : " OFF"), enabled ? Color.WHITE : Color.LIGHT_GRAY,
      DebugOptions.TEXT_X, y + 14, false);
  }

  private void drawDebugTrajectorySlider(Graphics2D g2, DebugOptions debug)
  {
    int y = DebugOptions.TRAJECTORY_SLIDER_Y;
    TextRenderer.draw(g2, this.debugFont, "Trayectorias: " + debug.getBulletTrajectoryLimit(), Color.WHITE,
      DebugOptions.SWITCH_X, y + 5, false);
    g2.setColor(Color.BLACK);
    g2.fillRect(DebugOptions.TRAJECTORY_SLIDER_X - 2, y + 20, DebugOptions.TRAJECTORY_SLIDER_WIDTH + 4,
      DebugOptions.TRAJECTORY_SLIDER_HEIGHT + 4);
    g2.setColor(Color.GRAY);
    g2.fillRect(DebugOptions.TRAJECTORY_SLIDER_X, y + 22, DebugOptions.TRAJECTORY_SLIDER_WIDTH,
      DebugOptions.TRAJECTORY_SLIDER_HEIGHT);
    g2.setColor(Color.GREEN);
    g2.fillRect(DebugOptions.TRAJECTORY_SLIDER_X, y + 22,
      (int) Math.round(DebugOptions.TRAJECTORY_SLIDER_WIDTH * debug.getBulletTrajectorySliderValue()),
      DebugOptions.TRAJECTORY_SLIDER_HEIGHT);
    int knobX = DebugOptions.TRAJECTORY_SLIDER_X
      + (int) Math.round(DebugOptions.TRAJECTORY_SLIDER_WIDTH * debug.getBulletTrajectorySliderValue());
    g2.setColor(Color.WHITE);
    g2.fillRect(knobX - 5, y + 14, 10, 22);
  }
}
