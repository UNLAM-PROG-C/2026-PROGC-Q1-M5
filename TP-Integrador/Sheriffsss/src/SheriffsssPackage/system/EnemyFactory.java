package SheriffsssPackage.system;

import SheriffsssPackage.Enemy;
import SheriffsssPackage.EnemyBehavior;
import SheriffsssPackage.EnemyType;

public final class EnemyFactory {
  public Enemy createWorldEnemy(EnemyType type, int worldX, int worldY, int playerLevel) {
    return new Enemy(type, worldX, worldY, playerLevel);
  }

  public Enemy createTrainingTarget(int worldX, int worldY, int dayCount) {
    Enemy enemy = new Enemy(EnemyType.DIANA, worldX, worldY, dayCount);
    enemy.setBehaviorOverride(EnemyBehavior.STATIC);
    return enemy;
  }
}
