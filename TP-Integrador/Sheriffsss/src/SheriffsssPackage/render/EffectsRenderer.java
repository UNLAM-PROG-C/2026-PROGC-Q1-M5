package SheriffsssPackage.render;

import SheriffsssPackage.context.GameConfig;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.Graphics2D;
import java.util.List;

public class EffectsRenderer
{
  private final Camera camera;
  private final Color[] flameBurstOuterColors = new Color[256];
  private final Color[] flameBurstCoreColors = new Color[256];
  private final Composite[] pickupTextComposites = new Composite[256];
  private final Color fireYellow = new Color(255, 218, 72, 210);
  private final Color fireOrange = new Color(255, 96, 18, 210);
  private final Font pickupTextFont = new Font("Arial", Font.BOLD, 16);

  public EffectsRenderer(Camera camera)
  {
    this.camera = camera;
  }

  public void drawFlameBurstEffects(Graphics2D g2, GameView game)
  {
    Composite previousComposite = g2.getComposite();
    for (int i = 0; i < game.getFlameBurstEffects().size(); i++)
    {
      FlameBurstEffect effect = game.getFlameBurstEffects().get(i);
      double progress = effect.getAgeTicks() / (double) effect.getLifeTicks();
      double easeOut = 1.0 - (1.0 - progress) * (1.0 - progress);
      int centerX = this.camera.worldToScreenX(effect.getOriginWorldX());
      int centerY = this.camera.worldToScreenY(effect.getOriginWorldY());
      int radius = (int) Math.max(8.0, effect.getRadiusPixels() * easeOut);
      int alpha = Math.max(0, Math.min(220, (int) (220.0 * (1.0 - progress))));

      g2.setComposite(getPickupTextComposite(Math.max(0, Math.min(255, alpha))));
      g2.setColor(getFlameBurstOuterColor(alpha));
      g2.fillOval(centerX - radius, centerY - radius, radius * 2, radius * 2);
      g2.setColor(getFlameBurstCoreColor(alpha));
      int coreRadius = Math.max(8, radius / 4);
      g2.fillOval(centerX - coreRadius, centerY - coreRadius, coreRadius * 2, coreRadius * 2);

      for (int particle = 0; particle < effect.getParticleCount(); particle++)
      {
        double angle = effect.getParticleAngleRadians(particle) + progress * 0.9;
        int distance = (int) (effect.getRadiusPixels() * easeOut * effect.getParticleDistanceScale(particle));
        int particleX = centerX + (int) Math.round(Math.cos(angle) * distance);
        int particleY = centerY + (int) Math.round(Math.sin(angle) * distance);
        int emberSize = 3 + (particle % 5);
        g2.setColor((particle & 1) == 0 ? this.fireYellow : this.fireOrange);
        g2.drawLine(centerX, centerY, particleX, particleY);
        g2.fillOval(particleX - emberSize / 2, particleY - emberSize / 2, emberSize, emberSize + 2);
      }
    }
    g2.setComposite(previousComposite);
  }

  public void drawCombatFloatingTexts(Graphics2D g2, GameView game)
  {
    List<CombatFloatingText> combatFloatingTexts = game.getCombatFloatingTexts();
    Composite previousComposite = g2.getComposite();
    for (int i = 0; i < combatFloatingTexts.size(); i++)
    {
      CombatFloatingText text = combatFloatingTexts.get(i);
      int alpha = text.getAlpha();
      if (alpha <= 0)
      {
        continue;
      }
      int centerX = this.camera.worldToScreenX(text.getWorldX());
      int centerY = this.camera.worldToScreenY(text.getWorldY());
      if (centerX < -GameConfig.TILE_SIZE || centerX > GameConfig.SCREEN_WIDTH + GameConfig.TILE_SIZE
        || centerY < -GameConfig.TILE_SIZE || centerY > GameConfig.SCREEN_HEIGHT + GameConfig.TILE_SIZE)
      {
        continue;
      }
      String message = text.getMessage();
      int x = TextRenderer.centeredX(g2, this.pickupTextFont, message, centerX);
      int y = centerY - text.getOffsetPixels();
      g2.setComposite(getPickupTextComposite(alpha));
      TextRenderer.draw(g2, this.pickupTextFont, message, text.getColor(), x, y, true);
    }
    g2.setComposite(previousComposite);
  }

  private Color getFlameBurstOuterColor(int alpha)
  {
    int colorAlpha = Math.max(35, Math.max(0, Math.min(255, alpha)) / 3);
    Color color = this.flameBurstOuterColors[colorAlpha];
    if (color == null)
    {
      color = new Color(255, 68, 12, colorAlpha);
      this.flameBurstOuterColors[colorAlpha] = color;
    }
    return color;
  }

  private Color getFlameBurstCoreColor(int alpha) {
    int colorAlpha = Math.max(70, Math.max(0, Math.min(255, alpha)) / 2);
    Color color = this.flameBurstCoreColors[colorAlpha];
    if (color == null)
    {
      color = new Color(255, 214, 72, colorAlpha);
      this.flameBurstCoreColors[colorAlpha] = color;
    }
    return color;
  }

  private Composite getPickupTextComposite(int alpha)
  {
    int clampedAlpha = Math.max(0, Math.min(255, alpha));
    Composite composite = this.pickupTextComposites[clampedAlpha];
    if (composite == null)
    {
      composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, clampedAlpha / 255f);
      this.pickupTextComposites[clampedAlpha] = composite;
    }
    return composite;
  }
}
