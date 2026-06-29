package SheriffsssPackage.level;

import SheriffsssPackage.context.GameConfig;

/**
 * Public constants from TrainingMode that are used by other modules.
 */
public final class TrainingModeConstants {
  public static final int TARGET_LIFETIME_SECONDS = 4;
  public static final int TARGET_LIFETIME_TICKS = GameConfig.TARGET_FPS * TARGET_LIFETIME_SECONDS;
  public static final int TARGET_DECAY_INTERVAL_TICKS = GameConfig.TARGET_FPS;
  public static final double TARGET_DECAY_DAMAGE = 1.0;
  public static final double TARGET_BLINK_SLOW_HP_RATIO = 0.50;
  public static final double TARGET_BLINK_FAST_HP_RATIO = 0.25;
  public static final int TARGET_BLINK_SLOW_PHASE_TICKS = Math.max(1, GameConfig.TARGET_FPS / 4);
  public static final int TARGET_BLINK_FAST_PHASE_TICKS = Math.max(1, GameConfig.TARGET_FPS / 8);
  public static final float TARGET_BLINK_ALPHA = 0.15f;

  private TrainingModeConstants() {
    // Utility class
  }
}
