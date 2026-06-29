package SheriffsssPackage.system.enemy;

import SheriffsssPackage.level.TrainingModeConstants;



public final class EnemyFactory {
  public Enemy createTrainingTarget(int worldX, int worldY, int dayCount)
  {
    Enemy enemy = new Enemy(
      EnemyType.DIANA,
      worldX,
      worldY,
      dayCount,
      TrainingModeConstants.TARGET_LIFETIME_SECONDS,
      TrainingModeConstants.TARGET_LIFETIME_SECONDS);
    enemy.setBehaviorOverride(EnemyBehavior.STATIC);
    return enemy;
  }
}
