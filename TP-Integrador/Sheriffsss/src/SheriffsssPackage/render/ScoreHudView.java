package SheriffsssPackage.render;

public final class ScoreHudView {
  private int score;
  private int level;
  private int maxLevel;
  private int bossTriggerScore;

  public void update(int score, int level, int maxLevel, int bossTriggerScore) {
    this.score = score;
    this.level = level;
    this.maxLevel = maxLevel;
    this.bossTriggerScore = bossTriggerScore;
  }

  public int score() {
    return this.score;
  }

  public int level() {
    return this.level;
  }

  public int maxLevel() {
    return this.maxLevel;
  }

  public int bossTriggerScore() {
    return this.bossTriggerScore;
  }
}
