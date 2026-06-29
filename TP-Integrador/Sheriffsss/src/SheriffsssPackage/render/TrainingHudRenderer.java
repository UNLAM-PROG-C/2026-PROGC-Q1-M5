package SheriffsssPackage.render;

import SheriffsssPackage.context.GameConfig;


import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;


public final class TrainingHudRenderer {
  private static final TextRenderer.Style TEXT_STYLE = TextRenderer.Style.OUTLINED;
  private static final Font TITLE_FONT = new Font("Arial", Font.BOLD, 18);
  private static final Font BODY_FONT = new Font("Arial", Font.BOLD, 28);
  private static final Font HINT_FONT = BODY_FONT.deriveFont(BODY_FONT.getSize2D() * 2.0f);
  private static final Font TIMER_FONT = BODY_FONT.deriveFont(BODY_FONT.getSize2D() * 1.5f);
  private static final Font PRECISION_FONT = BODY_FONT.deriveFont(BODY_FONT.getSize2D() * 1.5f);
  private static final int FAILURE_TEXT_X_OFFSET = 260;
  private static final int HIT_TEXT_X_OFFSET = 280;
  private static final int SIDE_STAT_BASELINE_Y = 40;
  private static final int PRECISION_TITLE_BASELINE_Y = 36;
  private static final int PRECISION_VALUE_LINE_GAP = 44;
  private static final int DEATH_BOX_WIDTH = 360;
  private static final int DEATH_BOX_HEIGHT = 170;
  private static final int DEATH_BOX_Y_OFFSET_FROM_CENTER = 60;
  private static final int DEATH_BOX_TITLE_BASELINE_Y = 32;
  private static final float BOX_BG_ALPHA = 0.88f;
  private static final Color BOX_BG_COLOR = new Color(15, 15, 18);
  private static final Color DEATH_BOX_BORDER_COLOR = new Color(220, 60, 60);
  private static final Color DEATH_TITLE_COLOR = new Color(255, 90, 90);
  private static final Color BODY_COLOR = Color.WHITE;
  private static final int FINAL_BOX_WIDTH = 460;
  private static final int FINAL_BOX_HEIGHT = 290;
  private static final int FINAL_BOX_TITLE_BASELINE_Y = 42;
  private static final int FINAL_BOX_HITS_Y = 92;
  private static final int FINAL_BOX_PRECISION_Y = 126;
  private static final int FINAL_BOX_FINAL_SCORE_Y = 160;
  private static final int END_BUTTON_WIDTH = 220;
  private static final int END_BUTTON_HEIGHT = 38;
  private static final int DEATH_RESTART_BUTTON_Y = 56;
  private static final int DEATH_EXIT_BUTTON_Y = 108;
  private static final int FINAL_RESTART_BUTTON_Y = 188;
  private static final int FINAL_EXIT_BUTTON_Y = 236;
  private static final String DEATH_TITLE_TEXT = "Moriste";
  private static final String RESTART_BUTTON_TEXT = "Reiniciar";
  private static final String EXIT_BUTTON_TEXT = "Salir";
  private static final String FINAL_TITLE_TEXT = "Entrenamiento terminado";
  private static final Color TITLE_COLOR = new Color(240, 220, 120);

  public void render(Graphics2D g2, TrainingHudSnapshot snapshot) {
    if (snapshot == null) {
      return;
    }
    drawStats(g2, snapshot);
    if (snapshot.finalScreenVisible())
    {
      drawFinalScreen(g2, snapshot);
      return;
    }
    drawHint(g2, snapshot);
    if (snapshot.deathPromptVisible())
      {
      drawDeathPrompt(g2);
    }
  }

  private void drawStats(Graphics2D g2, TrainingHudSnapshot snapshot)
      {
    TextRenderer.drawCentered(g2, BODY_FONT, "Fallos: " + snapshot.failures(), Color.WHITE,
      GameConfig.SCREEN_CENTER_X - FAILURE_TEXT_X_OFFSET, SIDE_STAT_BASELINE_Y, TEXT_STYLE);
    TextRenderer.drawCentered(g2, PRECISION_FONT, "Precision", Color.WHITE,
      GameConfig.SCREEN_CENTER_X, PRECISION_TITLE_BASELINE_Y, TEXT_STYLE);
    TextRenderer.drawCentered(g2, PRECISION_FONT, snapshot.precisionText(), Color.WHITE,
      GameConfig.SCREEN_CENTER_X, PRECISION_TITLE_BASELINE_Y + PRECISION_VALUE_LINE_GAP, TEXT_STYLE);
    TextRenderer.drawCentered(g2, BODY_FONT, "Aciertos: " + snapshot.hits(), Color.WHITE,
      GameConfig.SCREEN_CENTER_X + HIT_TEXT_X_OFFSET, SIDE_STAT_BASELINE_Y, TEXT_STYLE);
    if (snapshot.timerVisible())
    {
      TextRenderer.drawCentered(g2, TIMER_FONT, snapshot.timerText(), Color.WHITE,
        snapshot.hintCenterX(), snapshot.hintBaselineY(), TEXT_STYLE);
    }
  }

  private void drawHint(Graphics2D g2, TrainingHudSnapshot snapshot)
      {
    if (snapshot.hintAlpha() <= 0.0f || snapshot.hintText().isEmpty()) {
      return;
    }
    Composite previousComposite = g2.getComposite();
    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, snapshot.hintAlpha()));
    TextRenderer.drawCentered(g2, HINT_FONT, snapshot.hintText(), Color.WHITE,
      snapshot.hintCenterX(), snapshot.hintBaselineY(), TEXT_STYLE);
    g2.setComposite(previousComposite);
  }

  private void drawDeathPrompt(Graphics2D g2)
    {
    int boxX = deathBoxX();
    int boxY = deathBoxY();
    drawTranslucentBox(g2, boxX, boxY, DEATH_BOX_WIDTH, DEATH_BOX_HEIGHT, DEATH_BOX_BORDER_COLOR);
    TextRenderer.drawCentered(g2, TITLE_FONT, DEATH_TITLE_TEXT, DEATH_TITLE_COLOR,
      boxX + DEATH_BOX_WIDTH / 2, boxY + DEATH_BOX_TITLE_BASELINE_Y, TEXT_STYLE);
    drawEndButton(g2, RESTART_BUTTON_TEXT, endButtonX(boxX, DEATH_BOX_WIDTH),
      boxY + DEATH_RESTART_BUTTON_Y);
    drawEndButton(g2, EXIT_BUTTON_TEXT, endButtonX(boxX, DEATH_BOX_WIDTH),
      boxY + DEATH_EXIT_BUTTON_Y);
  }

  private void drawFinalScreen(Graphics2D g2, TrainingHudSnapshot snapshot)
    {
    int boxX = finalBoxX();
    int boxY = finalBoxY();
    drawTranslucentBox(g2, boxX, boxY, FINAL_BOX_WIDTH, FINAL_BOX_HEIGHT, Color.WHITE);
    drawFinalLine(g2, FINAL_TITLE_TEXT, TITLE_FONT, TITLE_COLOR, boxX, boxY + FINAL_BOX_TITLE_BASELINE_Y);
    drawFinalLine(g2, "Aciertos: " + snapshot.hits(), BODY_FONT, BODY_COLOR, boxX, boxY + FINAL_BOX_HITS_Y);
    drawFinalLine(g2, "Precision: " + snapshot.precisionText(), BODY_FONT, BODY_COLOR,
      boxX, boxY + FINAL_BOX_PRECISION_Y);
    Color scoreColor = snapshot.debugUsed() ? Color.RED : BODY_COLOR;
    String scoreText = "Puntaje final: " + snapshot.finalScore() + (snapshot.debugUsed() ? " (debug)" : "");
    drawFinalLine(g2, scoreText, BODY_FONT, scoreColor, boxX, boxY + FINAL_BOX_FINAL_SCORE_Y);
    drawEndButton(g2, RESTART_BUTTON_TEXT, endButtonX(boxX, FINAL_BOX_WIDTH),
      boxY + FINAL_RESTART_BUTTON_Y);
    drawEndButton(g2, EXIT_BUTTON_TEXT, endButtonX(boxX, FINAL_BOX_WIDTH),
      boxY + FINAL_EXIT_BUTTON_Y);
  }

  private static void drawTranslucentBox(
      Graphics2D g2,
      int x,
      int y,
      int width,
      int height,
      Color border)
    {
    Composite previous = g2.getComposite();
    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, BOX_BG_ALPHA));
    g2.setColor(BOX_BG_COLOR);
    g2.fillRect(x, y, width, height);
    g2.setComposite(previous);
    g2.setColor(border);
    g2.drawRect(x, y, width, height);
  }

  private static void drawFinalLine(
      Graphics2D g2,
      String text,
      Font font,
      Color color,
      int boxX,
      int baselineY)
    {
    TextRenderer.drawCentered(g2, font, text, color, boxX + FINAL_BOX_WIDTH / 2, baselineY, TEXT_STYLE);
  }

  private static void drawEndButton(Graphics2D g2, String text, int x, int y)
    {
    g2.setColor(GameTheme.DIRT);
    g2.fillRect(x, y, END_BUTTON_WIDTH, END_BUTTON_HEIGHT);
    g2.setColor(Color.BLACK);
    g2.drawRect(x, y, END_BUTTON_WIDTH, END_BUTTON_HEIGHT);
    int textX = TextRenderer.centeredX(g2, TITLE_FONT, text, x + END_BUTTON_WIDTH / 2);
    FontMetrics metrics = g2.getFontMetrics();
    int textY = y + (END_BUTTON_HEIGHT + metrics.getAscent()) / 2 - 4;
    TextRenderer.draw(g2, TITLE_FONT, text, BODY_COLOR, textX, textY, TEXT_STYLE);
  }

  private static int deathBoxX()
    {
    return (GameConfig.SCREEN_WIDTH - DEATH_BOX_WIDTH) / 2;
  }

  private static int deathBoxY() {
    return GameConfig.SCREEN_HEIGHT / 2 + DEATH_BOX_Y_OFFSET_FROM_CENTER;
  }

  private static int finalBoxX()
  {
    return (GameConfig.SCREEN_WIDTH - FINAL_BOX_WIDTH) / 2;
  }

  private static int finalBoxY() {
    return (GameConfig.SCREEN_HEIGHT - FINAL_BOX_HEIGHT) / 2;
  }

  private static int endButtonX(int boxX, int boxWidth) {
    return boxX + (boxWidth - END_BUTTON_WIDTH) / 2;
  }
}
