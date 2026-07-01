package SheriffsssPackage.render;

public final class TrainingHudView
{
  private boolean active;
  private TrainingHudSnapshot snapshot;

  public void update(boolean active, TrainingHudSnapshot snapshot)
  {
    this.active = active;
    this.snapshot = snapshot;
  }

  public boolean active()
  {
    return this.active;
  }

  public TrainingHudSnapshot snapshot()
  {
    return this.snapshot;
  }
}
