package SheriffsssPackage.render;

public final class TrainingHudSnapshotConfig {
  private int failures;
  private int hits;
  private String precisionText = "";
  private String timerText = "";
  private boolean timerVisible;
  private float hintAlpha;
  private String hintText = "";
  private int hintCenterX;
  private int hintBaselineY;
  private boolean deathPromptVisible;
  private boolean finalScreenVisible;
  private int finalScore;
  private boolean debugUsed;
  private int failurePerimeterLeftWorldX;
  private int failurePerimeterTopWorldY;
  private int failurePerimeterWidthWorld;
  private int failurePerimeterHeightWorld;

  public TrainingHudSnapshotConfig withStats(int failures, int hits, String precisionText)
  {
    this.failures = failures;
    this.hits = hits;
    this.precisionText = precisionText;
    return this;
  }

  public TrainingHudSnapshotConfig withTimer(String timerText, boolean timerVisible)
  {
    this.timerText = timerText;
    this.timerVisible = timerVisible;
    return this;
  }

  public TrainingHudSnapshotConfig withHint(float hintAlpha, String hintText, int centerX, int baselineY)
  {
    this.hintAlpha = hintAlpha;
    this.hintText = hintText;
    this.hintCenterX = centerX;
    this.hintBaselineY = baselineY;
    return this;
  }

  public TrainingHudSnapshotConfig withEndState(
      boolean deathPromptVisible,
      boolean finalScreenVisible,
      int finalScore,
      boolean debugUsed)
  {
    this.deathPromptVisible = deathPromptVisible;
    this.finalScreenVisible = finalScreenVisible;
    this.finalScore = finalScore;
    this.debugUsed = debugUsed;
    return this;
  }

  public TrainingHudSnapshotConfig withFailurePerimeter(int leftX, int topY, int width, int height)
  {
    this.failurePerimeterLeftWorldX = leftX;
    this.failurePerimeterTopWorldY = topY;
    this.failurePerimeterWidthWorld = width;
    this.failurePerimeterHeightWorld = height;
    return this;
  }

  int failures()
  {
    return this.failures;
  }

  int hits()
  {
    return this.hits;
  }

  String precisionText()
  {
    return this.precisionText;
  }

  String timerText()
  {
    return this.timerText;
  }

  boolean timerVisible()
  {
    return this.timerVisible;
  }

  float hintAlpha()
  {
    return this.hintAlpha;
  }

  String hintText()
  {
    return this.hintText;
  }

  int hintCenterX()
  {
    return this.hintCenterX;
  }

  int hintBaselineY()
  {
    return this.hintBaselineY;
  }

  boolean deathPromptVisible()
  {
    return this.deathPromptVisible;
  }

  boolean finalScreenVisible()
  {
    return this.finalScreenVisible;
  }

  int finalScore()
  {
    return this.finalScore;
  }

  boolean debugUsed()
  {
    return this.debugUsed;
  }

  int failurePerimeterLeftWorldX()
  {
    return this.failurePerimeterLeftWorldX;
  }

  int failurePerimeterTopWorldY()
  {
    return this.failurePerimeterTopWorldY;
  }

  int failurePerimeterWidthWorld()
  {
    return this.failurePerimeterWidthWorld;
  }

  int failurePerimeterHeightWorld()
  {
    return this.failurePerimeterHeightWorld;
  }
}
