package SheriffsssPackage.level;

public final class LevelStartOptions
{
  private final boolean resetDebugOptions;
  private final String seedOverride;

  public LevelStartOptions(boolean resetDebugOptions, String seedOverride)
  {
    this.resetDebugOptions = resetDebugOptions;
    this.seedOverride = seedOverride;
  }

  public static LevelStartOptions defaultOptions()
  {
    return new LevelStartOptions(false, "");
  }

  public static LevelStartOptions resetDebugOptions()
  {
    return new LevelStartOptions(true, "");
  }

  public boolean resetDebugOptionsRequested()
  {
    return this.resetDebugOptions;
  }

  public boolean hasSeedOverride()
  {
    return this.seedOverride != null && !this.seedOverride.isEmpty();
  }

  public String seedOverride()
  {
    return this.seedOverride;
  }
}
