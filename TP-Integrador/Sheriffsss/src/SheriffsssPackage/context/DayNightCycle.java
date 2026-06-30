package SheriffsssPackage.context;

public class DayNightCycle
{
  private static final int TICKS_PER_DAY = GameConfig.TARGET_FPS * 300;
  private static final double MORNING_END = 0.20;
  private static final double AFTERNOON_END = 0.52;
  private static final double SUNSET_END = 0.68;
  private static final double LATE_NIGHT_START = 0.88;
  private static final double AMBIENT_LIGHT_MORNING_START = 0.82;
  private static final double AMBIENT_LIGHT_SUNSET_START = 0.38;
  private static final double AMBIENT_LIGHT_NIGHT = 0.12;
  private static final int SUNSET_TINT_MAX_ALPHA = 68;
  private static final double NATURALLY_BRIGHT_THRESHOLD = 0.995;
  private static final double DAY_PROGRESS_CLAMP_MAX = 0.999;

  private int dayCount = 1;
  private int tickOfDay;

  public void tick()
 {
    this.tickOfDay++;
    if (this.tickOfDay >= TICKS_PER_DAY)
    {
      this.tickOfDay = 0;
      this.dayCount++;
    }
  }

  public void reset()
 {
    this.dayCount = 1;
    this.tickOfDay = 0;
  }

  public void setDayProgress(double progress)
 {
    double clampedProgress = Math.max(0.0, Math.min(DAY_PROGRESS_CLAMP_MAX, progress));
    this.tickOfDay = Math.max(0, Math.min(TICKS_PER_DAY - 1, (int) Math.round(TICKS_PER_DAY * clampedProgress)));
  }

  public int getDayCount()
 {
    return this.dayCount;
  }

  public DayPhase getPhase()
 {
    double progress = getDayProgress();
    if (progress < MORNING_END)
  {
      return DayPhase.MORNING;
    }
    if (progress < AFTERNOON_END)
    {
      return DayPhase.AFTERNOON;
    }
    if (progress < SUNSET_END)
    {
      return DayPhase.SUNSET;
    }
    return DayPhase.NIGHT;
  }

  public double getAmbientLight()
 {
    double progress = getDayProgress();
    if (progress < MORNING_END)
  {
      return lerp(AMBIENT_LIGHT_MORNING_START, 1.0, progress / MORNING_END);
    }
    if (progress < AFTERNOON_END)
  {
      return 1.0;
    }
    if (progress < SUNSET_END)
    {
      return lerp(1.0, AMBIENT_LIGHT_SUNSET_START, (progress - AFTERNOON_END) / (SUNSET_END - AFTERNOON_END));
    }
    if (progress < LATE_NIGHT_START)
  {
      return lerp(AMBIENT_LIGHT_SUNSET_START, AMBIENT_LIGHT_NIGHT, (progress - SUNSET_END) / (LATE_NIGHT_START - SUNSET_END));
    }
    return lerp(AMBIENT_LIGHT_NIGHT, AMBIENT_LIGHT_MORNING_START, (progress - LATE_NIGHT_START) / (1.0 - LATE_NIGHT_START));
  }

  public int getSunsetTintAlpha()
 {
    double progress = getDayProgress();
    if (progress < AFTERNOON_END || progress >= SUNSET_END)
  {
      return 0;
    }
    double sunsetProgress = (progress - AFTERNOON_END) / (SUNSET_END - AFTERNOON_END);
    double peak = 1.0 - Math.abs(sunsetProgress * 2.0 - 1.0);
    return (int) (SUNSET_TINT_MAX_ALPHA * peak);
  }

  public boolean isNaturallyBright()
 {
    return getAmbientLight() >= NATURALLY_BRIGHT_THRESHOLD;
  }

  private double getDayProgress()
 {
    return this.tickOfDay / (double) TICKS_PER_DAY;
  }

  private double lerp(double start, double end, double amount)
 {
    return start + (end - start) * amount;
  }
}
