package SheriffsssPackage;

/**
 * State of the training mode control panel.
 * Lives in the game loop thread; not accessed from TutorialThread.
 */
public final class TrainingControls {
  private int enemyCount;

  public TrainingControls() {
    this(GameConfig.TRAINING_DEFAULT_ENEMIES);
  }

  public TrainingControls(int enemyCount) {
    this.enemyCount = clampCount(enemyCount);
  }

  public int getEnemyCount() {
    return this.enemyCount;
  }

  public void incCount() {
    if (this.enemyCount < GameConfig.TRAINING_MAX_ENEMIES) {
      this.enemyCount++;
    }
  }

  public void decCount() {
    if (this.enemyCount > 0) {
      this.enemyCount--;
    }
  }

  public void setEnemyCount(int count) {
    this.enemyCount = clampCount(count);
  }

  public void reset() {
    this.enemyCount = GameConfig.TRAINING_DEFAULT_ENEMIES;
  }

  private static int clampCount(int count) {
    if (count < 0) {
      return 0;
    }
    if (count > GameConfig.TRAINING_MAX_ENEMIES) {
      return GameConfig.TRAINING_MAX_ENEMIES;
    }
    return count;
  }
}
