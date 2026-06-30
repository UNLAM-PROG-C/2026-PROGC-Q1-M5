package SheriffsssPackage.render;

public final class TrainingHudSnapshot
{
  private final int failures;
  private final int hits;
  private final String precisionText;
  private final String timerText;
  private final boolean timerVisible;
  private final float hintAlpha;
  private final String hintText;
  private final int hintCenterX;
  private final int hintBaselineY;
  private final boolean deathPromptVisible;
  private final boolean finalScreenVisible;
  private final int finalScore;
  private final boolean debugUsed;
  private final int failurePerimeterLeftWorldX;
  private final int failurePerimeterTopWorldY;
  private final int failurePerimeterWidthWorld;
  private final int failurePerimeterHeightWorld;

  public TrainingHudSnapshot(TrainingHudSnapshotConfig config)
  {
    this.failures = config.failures();
    this.hits = config.hits();
    this.precisionText = config.precisionText();
    this.timerText = config.timerText();
    this.timerVisible = config.timerVisible();
    this.hintAlpha = config.hintAlpha();
    this.hintText = config.hintText();
    this.hintCenterX = config.hintCenterX();
    this.hintBaselineY = config.hintBaselineY();
    this.deathPromptVisible = config.deathPromptVisible();
    this.finalScreenVisible = config.finalScreenVisible();
    this.finalScore = config.finalScore();
    this.debugUsed = config.debugUsed();
    this.failurePerimeterLeftWorldX = config.failurePerimeterLeftWorldX();
    this.failurePerimeterTopWorldY = config.failurePerimeterTopWorldY();
    this.failurePerimeterWidthWorld = config.failurePerimeterWidthWorld();
    this.failurePerimeterHeightWorld = config.failurePerimeterHeightWorld();
  }

  public int failures()
  {
    return this.failures;
  }

  public int hits()
  {
    return this.hits;
  }

  public String precisionText()
  {
    return this.precisionText;
  }

  public String timerText()
  {
    return this.timerText;
  }

  public boolean timerVisible()
  {
    return this.timerVisible;
  }

  public float hintAlpha()
  {
    return this.hintAlpha;
  }

  public String hintText()
  {
    return this.hintText;
  }

  public int hintCenterX()
  {
    return this.hintCenterX;
  }

  public int hintBaselineY()
  {
    return this.hintBaselineY;
  }

  public boolean deathPromptVisible()
  {
    return this.deathPromptVisible;
  }

  public boolean finalScreenVisible()
  {
    return this.finalScreenVisible;
  }

  public int finalScore()
  {
    return this.finalScore;
  }

  public boolean debugUsed()
  {
    return this.debugUsed;
  }

  public int failurePerimeterLeftWorldX()
  {
    return this.failurePerimeterLeftWorldX;
  }

  public int failurePerimeterTopWorldY()
  {
    return this.failurePerimeterTopWorldY;
  }

  public int failurePerimeterWidthWorld()
  {
    return this.failurePerimeterWidthWorld;
  }

  public int failurePerimeterHeightWorld()
  {
    return this.failurePerimeterHeightWorld;
  }
}
