package SheriffsssPackage.render;

import SheriffsssPackage.context.DebugOptions;
import SheriffsssPackage.context.GameConfig;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

public class DebugMenuRenderer
{
  private static final Color DEBUG_PANEL_BG = new Color(0, 0, 0, 175);
  private static final Color DEBUG_PANEL_BORDER = new Color(255, 230, 120, 220);
  private static final Color DEBUG_SWITCH_ON_COLOR = new Color(70, 210, 110, 230);
  private static final Color DEBUG_SWITCH_OFF_COLOR = new Color(80, 80, 80, 210);
  private static final int CHECKMARK_OFFSET_1 = 4;
  private static final int CHECKMARK_BASE_Y = 9;
  private static final int CHECKMARK_MID_OFFSET = 8;
  private static final int CHECKMARK_MID_Y = 13;
  private static final int CHECKMARK_TIP_OFFSET = 15;
  private static final int CHECKMARK_TIP_Y = 4;
  private static final int SWITCH_TEXT_Y_OFFSET = 14;
  private static final int TRAJECTORY_LABEL_Y_OFFSET = 5;
  private static final int TRAJECTORY_SLIDER_BG_Y_OFFSET = 20;
  private static final int TRAJECTORY_SLIDER_BG_PADDING = 4;
  private static final int TRAJECTORY_SLIDER_BG_X_OFFSET = 2;
  private static final int TRAJECTORY_SLIDER_Y_OFFSET = 22;
  private static final int SLIDER_KNOB_HALF_WIDTH = 5;
  private static final int SLIDER_KNOB_Y_OFFSET = 14;
  private static final int SLIDER_KNOB_WIDTH = 10;
  private static final int SLIDER_KNOB_HEIGHT = 22;

  private final Font debugTitleFont = new Font("Arial", Font.BOLD, 16);
  private final Font debugFont = new Font("Arial", Font.BOLD, 13);

  public void drawDebugMenu(Graphics2D g2, GameView game)
  {
    DebugOptions debug = game.getDebugOptions();
    if (!game.getTrainingHudView().active() || debug == null || !debug.isMenuOpen())
    {
      return;
    }
    java.awt.Stroke previousStroke = g2.getStroke();
    g2.setColor(DEBUG_PANEL_BG);
    g2.fillRect(DebugOptions.PANEL_X, DebugOptions.PANEL_Y, DebugOptions.PANEL_WIDTH, DebugOptions.PANEL_HEIGHT);
    g2.setColor(DEBUG_PANEL_BORDER);
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
    g2.setColor(enabled ? DEBUG_SWITCH_ON_COLOR : DEBUG_SWITCH_OFF_COLOR);
    g2.fillRect(DebugOptions.SWITCH_X, y, DebugOptions.SWITCH_SIZE, DebugOptions.SWITCH_SIZE);
    g2.setColor(enabled ? Color.WHITE : Color.GRAY);
    g2.drawRect(DebugOptions.SWITCH_X, y, DebugOptions.SWITCH_SIZE, DebugOptions.SWITCH_SIZE);
    if (enabled)
    {
      g2.drawLine(DebugOptions.SWITCH_X + CHECKMARK_OFFSET_1, y + CHECKMARK_BASE_Y, DebugOptions.SWITCH_X + CHECKMARK_MID_OFFSET, y + CHECKMARK_MID_Y);
      g2.drawLine(DebugOptions.SWITCH_X + CHECKMARK_MID_OFFSET, y + CHECKMARK_MID_Y, DebugOptions.SWITCH_X + CHECKMARK_TIP_OFFSET, y + CHECKMARK_TIP_Y);
    }
    TextRenderer.draw(g2, this.debugFont, label + (enabled ? " ON" : " OFF"), enabled ? Color.WHITE : Color.LIGHT_GRAY,
      DebugOptions.TEXT_X, y + SWITCH_TEXT_Y_OFFSET, false);
  }

  private void drawDebugTrajectorySlider(Graphics2D g2, DebugOptions debug)
  {
    int y = DebugOptions.TRAJECTORY_SLIDER_Y;
    TextRenderer.draw(g2, this.debugFont, "Trayectorias: " + debug.getBulletTrajectoryLimit(), Color.WHITE,
      DebugOptions.SWITCH_X, y + TRAJECTORY_LABEL_Y_OFFSET, false);
    g2.setColor(Color.BLACK);
    g2.fillRect(DebugOptions.TRAJECTORY_SLIDER_X - TRAJECTORY_SLIDER_BG_X_OFFSET, y + TRAJECTORY_SLIDER_BG_Y_OFFSET, DebugOptions.TRAJECTORY_SLIDER_WIDTH + TRAJECTORY_SLIDER_BG_PADDING,
      DebugOptions.TRAJECTORY_SLIDER_HEIGHT + TRAJECTORY_SLIDER_BG_PADDING);
    g2.setColor(Color.GRAY);
    g2.fillRect(DebugOptions.TRAJECTORY_SLIDER_X, y + TRAJECTORY_SLIDER_Y_OFFSET, DebugOptions.TRAJECTORY_SLIDER_WIDTH,
      DebugOptions.TRAJECTORY_SLIDER_HEIGHT);
    g2.setColor(Color.GREEN);
    g2.fillRect(DebugOptions.TRAJECTORY_SLIDER_X, y + TRAJECTORY_SLIDER_Y_OFFSET,
      (int) Math.round(DebugOptions.TRAJECTORY_SLIDER_WIDTH * debug.getBulletTrajectorySliderValue()),
      DebugOptions.TRAJECTORY_SLIDER_HEIGHT);
    int knobX = DebugOptions.TRAJECTORY_SLIDER_X
      + (int) Math.round(DebugOptions.TRAJECTORY_SLIDER_WIDTH * debug.getBulletTrajectorySliderValue());
    g2.setColor(Color.WHITE);
    g2.fillRect(knobX - SLIDER_KNOB_HALF_WIDTH, y + SLIDER_KNOB_Y_OFFSET, SLIDER_KNOB_WIDTH, SLIDER_KNOB_HEIGHT);
  }
}
