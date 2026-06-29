package SheriffsssPackage.system.enemy;

import SheriffsssPackage.level.TrainingMode;



public final class EnemyFactory {
  public Enemy createTrainingTarget(int worldX, int worldY, int dayCount)
  {
    Enemy enemy = new Enemy(
      EnemyType.DIANA,
      worldX,
      worldY,
      dayCount,
      TrainingMode.TARGET_LIFETIME_SECONDS,
      TrainingMode.TARGET_LIFETIME_SECONDS);
    enemy.setBehaviorOverride(EnemyBehavior.STATIC);
    return enemy;
  }
}
