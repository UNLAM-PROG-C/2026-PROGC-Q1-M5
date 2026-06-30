package SheriffsssPackage.system.weapon;

import SheriffsssPackage.session.GameMap;
import SheriffsssPackage.session.Player;
import SheriffsssPackage.system.enemy.Enemy;
import SheriffsssPackage.system.enemy.EnemyDensity;
import SheriffsssPackage.system.enemy.EnemySystem;


public class Projectile
{
  private static final int MAX_PIERCED_ENEMIES = 24;
  private static final double STEP_SIZE_PIXELS = 8.0;

  private final ProjectileType type;
  private final Player owner;
  private final ItemDefinition weapon;
  private final double damage;
  private final double knockbackStrengthPixels;
  private final double angleRadians;
  private final int[] piercedEnemyKeys = new int[MAX_PIERCED_ENEMIES];
  private double worldX;
  private double worldY;
  private double velocityX;
  private double velocityY;
  private int lifeTicks;
  private int piercedEnemyCount;
  private boolean hitTarget;
  private boolean countedTrainingFailure;
  private boolean active = true;

  public Projectile(ProjectileType type, double worldX, double worldY, double velocityX, double velocityY,
    double damage, double knockbackStrengthPixels, int lifeTicks)
  {
    this(type, null, null, worldX, worldY, velocityX, velocityY, damage, knockbackStrengthPixels, lifeTicks);
  }

  public Projectile(ProjectileType type, Player owner, double worldX, double worldY, double velocityX, double velocityY,
    double damage, double knockbackStrengthPixels, int lifeTicks)
    {
    this(type, owner, null, worldX, worldY, velocityX, velocityY, damage, knockbackStrengthPixels, lifeTicks);
  }

  public Projectile(ProjectileType type, Player owner, ItemDefinition weapon, double worldX, double worldY, double velocityX, double velocityY,
    double damage, double knockbackStrengthPixels, int lifeTicks)
    {
    this.type = type;
    this.owner = owner;
    this.weapon = weapon;
    this.worldX = worldX;
    this.worldY = worldY;
    this.velocityX = velocityX;
    this.velocityY = velocityY;
    this.damage = damage;
    this.knockbackStrengthPixels = knockbackStrengthPixels;
    this.lifeTicks = lifeTicks;
    this.angleRadians = Math.atan2(velocityY, velocityX);
  }

  public void update(GameMap map, EnemySystem enemySystem)
 {
    if (!this.active)
    {
      return;
    }
    this.lifeTicks--;
    if (this.lifeTicks <= 0)
    {
      this.active = false;
      return;
    }
    advanceWithSubsteps(map, enemySystem);
  }

  private void advanceWithSubsteps(GameMap map, EnemySystem enemySystem)
 {
    int steps = computeStepCount();
    double stepX = this.velocityX / steps;
    double stepY = this.velocityY / steps;
    for (int step = 0; step < steps && this.active; step++)
    {
      double previousX = this.worldX;
      double previousY = this.worldY;
      this.worldX += stepX;
      this.worldY += stepY;
      if (map.isProjectileBlockedAtWorld(getWorldX(), getWorldY()))
      {
        this.active = false;
        return;
      }
      hitEnemies(enemySystem, previousX, previousY);
    }
  }

  private int computeStepCount()
 {
    double maxVelocity = Math.max(Math.abs(this.velocityX), Math.abs(this.velocityY));
    return Math.max(1, (int) Math.ceil(maxVelocity / STEP_SIZE_PIXELS));
  }

  private void hitEnemies(EnemySystem enemySystem, double previousX, double previousY)
 {
    java.util.List<Enemy> enemies = enemySystem.getEnemies();
    for (int i = 0; i < enemies.size() && this.active; i++)
    {
      hitEnemyIfColliding(enemies.get(i), enemySystem, previousX, previousY);
    }
  }

  private void hitEnemyIfColliding(Enemy enemy, EnemySystem enemySystem, double previousX, double previousY)
 {
    if (enemy.isDead() || alreadyPierced(enemy))
    {
      return;
    }
    int radius = enemy.getType().getCollisionRadius() + Math.max(this.type.getDrawWidth(), this.type.getDrawHeight()) / 2;
    int deltaX = enemy.getWorldX() - getWorldX();
    int deltaY = enemy.getWorldY() - getWorldY();
    if (deltaX * deltaX + deltaY * deltaY > radius * radius)
    {
      return;
    }
    applyHit(enemy, enemySystem, previousX, previousY);
  }

  private void applyHit(Enemy enemy, EnemySystem enemySystem, double previousX, double previousY)
 {
    this.hitTarget = true;
    enemySystem.damageEnemy(enemy, this.damage, this.owner, this.weapon);
    if (this.knockbackStrengthPixels > 0.0)
    {
      enemy.applyKnockbackFrom((int) Math.round(previousX), (int) Math.round(previousY), this.knockbackStrengthPixels);
    }
    if (this.type.piercesLowDensity() && enemy.getType().getDensity() == EnemyDensity.LOW)
    {
      rememberPierced(enemy);
    }
    else
    {
      this.active = false;
    }
  }

  private boolean alreadyPierced(Enemy enemy)
 {
    int key = System.identityHashCode(enemy);
    for (int i = 0; i < this.piercedEnemyCount; i++)
  {
      if (this.piercedEnemyKeys[i] == key)
      {
        return true;
      }
    }
    return false;
  }

  private void rememberPierced(Enemy enemy)
 {
    if (this.piercedEnemyCount >= this.piercedEnemyKeys.length)
    {
      return;
    }
    this.piercedEnemyKeys[this.piercedEnemyCount] = System.identityHashCode(enemy);
    this.piercedEnemyCount++;
  }

  public ProjectileType getType()
 {
    return this.type;
  }

  public int getWorldX()
 {
    return (int) Math.round(this.worldX);
  }

  public int getWorldY()
  {
    return (int) Math.round(this.worldY);
  }

  public double getPreciseWorldX()
  {
    return this.worldX;
  }

  public double getPreciseWorldY()
 {
    return this.worldY;
  }

  public double getVelocityX()
 {
    return this.velocityX;
  }

  public double getVelocityY()
 {
    return this.velocityY;
  }

  public double getDamage()
 {
    return this.damage;
  }

  public double getKnockbackStrengthPixels()
 {
    return this.knockbackStrengthPixels;
  }

  public int getLifeTicks()
 {
    return this.lifeTicks;
  }

  public double getAngleRadians()
 {
    return this.angleRadians;
  }

  public boolean consumeHitTarget()
 {
    boolean value = this.hitTarget;
    this.hitTarget = false;
    return value;
  }

  public boolean isActive()
 {
    return this.active;
  }

  public boolean isCountedTrainingFailure()
 {
    return this.countedTrainingFailure;
  }

  public void markCountedTrainingFailure()
 {
    this.countedTrainingFailure = true;
  }
}
