package SheriffsssPackage;

public class MagicConstants
{
  // Valores normalizados [0.0 - 1.0]
  public static final double MIN_VALUE = 0.0;
  public static final double MAX_VALUE = 1.0;
  public static final double HALF_VALUE = 0.5;

  // Epsilon para comparaciones flotantes
  public static final double EPSILON = 0.001;

  // División para centering (dividir por 2)
  public static final int DIVISOR_HALF = 2;

  // Colores - Alpha
  public static final int MAX_ALPHA = 255;
  public static final float ALPHA_NORMALIZATION = 255f;

  // UI - Offsets y padding
  public static final int TEXT_SHADOW_OFFSET_MAJOR = 2;
  public static final int TEXT_SHADOW_OFFSET_MINOR = 1;
  public static final int UI_PADDING = 10;
  public static final int SETTINGS_X_OFFSET = 90;

  // Porcentajes
  public static final int MAX_PERCENTAGE = 100;
  public static final double PERCENT_SCALE = 100.0;

  // Matemáticas
  public static final double FULL_CIRCLE_RADIANS = Math.PI * 2.0;

  // Weapon offsets
  public static final int WEAPON_RENDER_X_OFFSET = 10;
  public static final int WEAPON_RENDER_Y_OFFSET_UP = -1;
  public static final int WEAPON_RENDER_Y_OFFSET_DOWN = 9;

  // Game loop - Delta time
  public static final double DELTA_FRAME_THRESHOLD = 1.0;
  public static final double DELTA_FRAME_DECREMENT = 1.0;

  // Gameplay - Default values
  public static final int MOVEMENT_RESET = 0;
  public static final double ACCURACY_MAX_DEFAULT = 1.0;

  private MagicConstants()
  {
  }
}
