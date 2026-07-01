package SheriffsssPackage.ui;

import java.awt.Dimension;

public final class DisplaySettingsChange
{
  private final boolean fullscreenChanged;
  private final boolean resolutionChanged;
  private final boolean targetFullscreen;
  private final Dimension windowedSize;

  public DisplaySettingsChange(
      boolean fullscreenChanged,
      boolean resolutionChanged,
      boolean targetFullscreen,
      Dimension windowedSize)
      {
    this.fullscreenChanged = fullscreenChanged;
    this.resolutionChanged = resolutionChanged;
    this.targetFullscreen = targetFullscreen;
    this.windowedSize = windowedSize;
  }

  public boolean fullscreenChanged()
  {
    return this.fullscreenChanged;
  }

  public boolean resolutionChanged()
  {
    return this.resolutionChanged;
  }

  public boolean targetFullscreen()
  {
    return this.targetFullscreen;
  }

  public Dimension windowedSize()
  {
    return this.windowedSize;
  }
}
