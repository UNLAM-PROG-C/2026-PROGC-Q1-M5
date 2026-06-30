package SheriffsssPackage.render;

import SheriffsssPackage.context.GameConfig;
import SheriffsssPackage.level.TrainingModeConstants;
import SheriffsssPackage.system.enemy.Debuff;
import SheriffsssPackage.system.enemy.Enemy;
import SheriffsssPackage.system.enemy.EnemyType;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class EnemyRenderer
{
  private static final Composite TRAINING_DIANA_BLINK_COMPOSITE =
    AlphaComposite.getInstance(AlphaComposite.SRC_OVER, TrainingModeConstants.TARGET_BLINK_ALPHA);
  private static final Color FIRE_YELLOW = new Color(255, 218, 72, 210);
  private static final Color FIRE_ORANGE = new Color(255, 96, 18, 210);
  private static final int ANIMATION_TICK_DIVISOR = 8;
  private static final int HEALTH_BAR_Y_ABOVE_ENEMY = 8;
  private static final int HEALTH_BAR_WIDTH = 34;
  private static final int HEALTH_BAR_HEIGHT = 5;
  private static final int HEALTH_TEXT_Y_OFFSET = 4;
  private static final int BURN_PARTICLE_COUNT = 5;
  private static final int BURN_SEED_OFFSET = 3;
  private static final int BURN_SEED_MULTIPLIER = 17;
  private static final int BURN_X_SPREAD_DIVISOR = 3;
  private static final int BURN_X_STEP_SEED = 7;
  private static final int BURN_X_SPREAD_FACTOR = 2;
  private static final int BURN_Y_BOTTOM_OFFSET = 10;
  private static final int BURN_Y_STEP_SEED = 5;
  private static final int BURN_HEIGHT_MARGIN = 8;
  private static final int BURN_SIZE_BASE = 3;
  private static final int BURN_SIZE_MODULO = 4;
  private static final int BURN_OVAL_HEIGHT_EXTRA = 2;
  private static final double HEALTH_FORMAT_EPSILON = 0.001;

  private final Camera camera;
  private final SheriffsssPackage.context.AssetManager assets;
  private final Font debugFont = new Font("Arial", Font.BOLD, 13);

  public EnemyRenderer(Camera camera, SheriffsssPackage.context.AssetManager assets)
  {
    this.camera = camera;
    this.assets = assets;
  }

  public void drawEnemies(Graphics2D g2, GameView game)
  {
    for (int i = 0; i < game.getEnemies().size(); i++)
    {
      Enemy enemy = game.getEnemies().get(i);
      EnemyType type = enemy.getType();
      int screenX = this.camera.worldToScreenX(enemy.getWorldX());
      int screenY = this.camera.worldToScreenY(enemy.getWorldY());
      int drawWidth = type.getDrawWidth();
      int drawHeight = type.getDrawHeight();
      if (screenX < -drawWidth || screenX > GameConfig.SCREEN_WIDTH + drawWidth
        || screenY < -drawHeight || screenY > GameConfig.SCREEN_HEIGHT + drawHeight)
        {
        continue;
      }
      BufferedImage sheet = this.assets.getImage(type.getSpritePath());
      int frame = (enemy.getAnimationTicks() / ANIMATION_TICK_DIVISOR) % type.getFramesPerRow();
      int row = type.getAnimationRow(enemy.getFacing());
      int sourceX = frame * type.getFrameWidth();
      int sourceY = row * type.getFrameHeight();
      int drawX = screenX - drawWidth / 2;
      int drawY = screenY - drawHeight / 2;
      if (shouldDimTrainingDianaBlink(enemy))
      {
        Composite previousComposite = g2.getComposite();
        g2.setComposite(TRAINING_DIANA_BLINK_COMPOSITE);
        drawEnemySprite(g2, sheet, enemy, drawX, drawY, drawWidth, drawHeight, sourceX, sourceY);
        g2.setComposite(previousComposite);
      }
      else
      {
        drawEnemySprite(g2, sheet, enemy, drawX, drawY, drawWidth, drawHeight, sourceX, sourceY);
      }
      drawEnemyHealthBar(g2, game, enemy, screenX, drawY - HEALTH_BAR_Y_ABOVE_ENEMY);
      if (enemy.hasDebuff(Debuff.BURN))
      {
        drawBurningParticles(g2, enemy, screenX, drawY, drawWidth, drawHeight);
      }
    }
  }

  private void drawEnemySprite(Graphics2D g2, BufferedImage sheet, Enemy enemy, int drawX, int drawY, int drawWidth, int drawHeight, int sourceX, int sourceY)
  {
    EnemyType type = enemy.getType();
    g2.drawImage(sheet, drawX, drawY, drawX + drawWidth, drawY + drawHeight,
      sourceX, sourceY, sourceX + type.getFrameWidth(), sourceY + type.getFrameHeight(), null);
  }

  private void drawEnemyHealthBar(Graphics2D g2, GameView game, Enemy enemy, int centerX, int y)
  {
    if (enemy.getType() == EnemyType.DIANA)
    {
      if (game.getDebugOptions() == null || !game.getDebugOptions().shouldShowTargetHealthBars())
      {
        return;
      }
    }
    else if (enemy.getCurrentHP() >= enemy.getMaxHP())
    {
      return;
    }
    int barWidth = HEALTH_BAR_WIDTH;
    int barHeight = HEALTH_BAR_HEIGHT;
    int x = centerX - barWidth / 2;
    double hpRatio = Math.max(0.0, Math.min(1.0, enemy.getCurrentHP() / enemy.getMaxHP()));
    g2.setColor(Color.BLACK);
    g2.fillRect(x - 1, y - 1, barWidth + 2, barHeight + 2);
    g2.setColor(Color.RED);
    g2.fillRect(x, y, barWidth, barHeight);
    g2.setColor(Color.GREEN);
    g2.fillRect(x, y, (int) (barWidth * hpRatio), barHeight);
    String healthText = formatHealthValue(enemy.getCurrentHP()) + "/" + formatHealthValue(enemy.getMaxHP());
    TextRenderer.draw(g2, this.debugFont, healthText, Color.WHITE,
      TextRenderer.centeredX(g2, this.debugFont, healthText, centerX), y - HEALTH_TEXT_Y_OFFSET, true);
  }

  private void drawBurningParticles(Graphics2D g2, Enemy enemy, int screenX, int drawY, int drawWidth, int drawHeight)
  {
    int ticks = enemy.getAnimationTicks();
    for (int i = 0; i < BURN_PARTICLE_COUNT; i++)
    {
      int offsetSeed = ticks * (i + BURN_SEED_OFFSET) + i * BURN_SEED_MULTIPLIER;
      int particleX = screenX - drawWidth / BURN_X_SPREAD_DIVISOR + Math.floorMod(offsetSeed * BURN_X_STEP_SEED, Math.max(1, drawWidth * BURN_X_SPREAD_FACTOR / BURN_X_SPREAD_DIVISOR));
      int particleY = drawY + drawHeight - BURN_Y_BOTTOM_OFFSET - Math.floorMod(offsetSeed * BURN_Y_STEP_SEED, Math.max(1, drawHeight - BURN_HEIGHT_MARGIN));
      int size = BURN_SIZE_BASE + Math.floorMod(offsetSeed, BURN_SIZE_MODULO);
      g2.setColor((i & 1) == 0 ? FIRE_ORANGE : FIRE_YELLOW);
      g2.fillOval(particleX, particleY, size, size + BURN_OVAL_HEIGHT_EXTRA);
    }
  }

  private boolean shouldDimTrainingDianaBlink(Enemy enemy)
  {
    if (enemy.getType() != EnemyType.DIANA || enemy.getMaxHP() <= 0.0)
    {
      return false;
    }
    double hpRatio = enemy.getCurrentHP() / enemy.getMaxHP();
    int phaseTicks;
    if (hpRatio <= TrainingModeConstants.TARGET_BLINK_FAST_HP_RATIO)
    {
      phaseTicks = TrainingModeConstants.TARGET_BLINK_FAST_PHASE_TICKS;
    }
    else if (hpRatio <= TrainingModeConstants.TARGET_BLINK_SLOW_HP_RATIO)
    {
      phaseTicks = TrainingModeConstants.TARGET_BLINK_SLOW_PHASE_TICKS;
    }
    else
    {
      return false;
    }
    return (enemy.getAnimationTicks() / phaseTicks) % 2 == 0;
  }

  private String formatHealthValue(double health)
  {
    return Math.abs(health - Math.round(health)) < HEALTH_FORMAT_EPSILON
      ? Integer.toString((int) Math.round(health))
      : String.format(java.util.Locale.US, "%.1f", health);
  }
}
