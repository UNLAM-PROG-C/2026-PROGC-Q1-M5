package SheriffsssPackage.system;

public final class GameEvents {
  private boolean shotFired;
  private boolean playerDied;

  public void clearFrameEvents() {
    this.shotFired = false;
    this.playerDied = false;
  }

  public void recordShotFired() {
    this.shotFired = true;
  }

  public void recordPlayerDied() {
    this.playerDied = true;
  }

  public boolean consumeShotFired() {
    boolean fired = this.shotFired;
    this.shotFired = false;
    return fired;
  }

  public boolean consumePlayerDied() {
    boolean died = this.playerDied;
    this.playerDied = false;
    return died;
  }
}
