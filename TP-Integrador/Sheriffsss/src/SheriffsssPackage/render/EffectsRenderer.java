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
  private static final Color FIRE_YELLOW = new Color(255, 218, 72, 210);
  private static final Color FIRE_ORANGE = new Color(255, 96, 18, 210);
  private static final int BURST_FLAME_MAX_ALPHA = 220;
  private static final double BURST_MIN_RADIUS_PIXELS = 8.0;
  private static final int BURST_MIN_CORE_RADIUS_PIXELS = 8;
  private static final int BURST_CORE_RADIUS_DIVISOR = 4;
  private static final double BURST_PARTICLE_ROTATION_SPEED = 0.9;
  private static final int EMBER_SIZE_BASE = 3;
  private static final int EMBER_SIZE_MODULO = 5;
  private static final int EMBER_OVAL_HEIGHT_EXTRA = 2;
  private static final int FLAME_OUTER_MIN_ALPHA = 35;
  private static final int FLAME_OUTER_ALPHA_DIVISOR = 3;
  private static final int FLAME_OUTER_COLOR_R = 255;
  private static final int FLAME_OUTER_COLOR_G = 68;
  private static final int FLAME_OUTER_COLOR_B = 12;
  private static final int FLAME_CORE_MIN_ALPHA = 70;
  private static final int FLAME_CORE_ALPHA_DIVISOR = 2;
  private static final int FLAME_CORE_COLOR_R = 255;
  private static final int FLAME_CORE_COLOR_G = 214;
  private static final int FLAME_CORE_COLOR_B = 72;

  private final Camera camera;
  private final Color[] flameBurstOuterColors = new Color[256];
  private final Color[] flameBurstCoreColors = new Color[256];
  private final Composite[] pickupTextComposites = new Composite[256];
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
      int radius = (int) Math.max(BURST_MIN_RADIUS_PIXELS, effect.getRadiusPixels() * easeOut);
      int alpha = Math.max(0, Math.min(BURST_FLAME_MAX_ALPHA, (int) (BURST_FLAME_MAX_ALPHA * (1.0 - progress))));

      g2.setComposite(getPickupTextComposite(Math.max(0, Math.min(255, alpha))));
      g2.setColor(getFlameBurstOuterColor(alpha));
      g2.fillOval(centerX - radius, centerY - radius, radius * 2, radius * 2);
      g2.setColor(getFlameBurstCoreColor(alpha));
      int coreRadius = Math.max(BURST_MIN_CORE_RADIUS_PIXELS, radius / BURST_CORE_RADIUS_DIVISOR);
      g2.fillOval(centerX - coreRadius, centerY - coreRadius, coreRadius * 2, coreRadius * 2);

      for (int particle = 0; particle < effect.getParticleCount(); particle++)
      {
        double angle = effect.getParticleAngleRadians(particle) + progress * BURST_PARTICLE_ROTATION_SPEED;
        int distance = (int) (effect.getRadiusPixels() * easeOut * effect.getParticleDistanceScale(particle));
        int particleX = centerX + (int) Math.round(Math.cos(angle) * distance);
        int particleY = centerY + (int) Math.round(Math.sin(angle) * distance);
        int emberSize = EMBER_SIZE_BASE + (particle % EMBER_SIZE_MODULO);
        g2.setColor((particle & 1) == 0 ? FIRE_YELLOW : FIRE_ORANGE);
        g2.drawLine(centerX, centerY, particleX, particleY);
        g2.fillOval(particleX - emberSize / 2, particleY - emberSize / 2, emberSize, emberSize + EMBER_OVAL_HEIGHT_EXTRA);
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
    int colorAlpha = Math.max(FLAME_OUTER_MIN_ALPHA, Math.max(0, Math.min(255, alpha)) / FLAME_OUTER_ALPHA_DIVISOR);
    Color color = this.flameBurstOuterColors[colorAlpha];
    if (color == null)
    {
      color = new Color(FLAME_OUTER_COLOR_R, FLAME_OUTER_COLOR_G, FLAME_OUTER_COLOR_B, colorAlpha);
      this.flameBurstOuterColors[colorAlpha] = color;
    }
    return color;
  }

  private Color getFlameBurstCoreColor(int alpha)
  {
    int colorAlpha = Math.max(FLAME_CORE_MIN_ALPHA, Math.max(0, Math.min(255, alpha)) / FLAME_CORE_ALPHA_DIVISOR);
    Color color = this.flameBurstCoreColors[colorAlpha];
    if (color == null)
    {
      color = new Color(FLAME_CORE_COLOR_R, FLAME_CORE_COLOR_G, FLAME_CORE_COLOR_B, colorAlpha);
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
