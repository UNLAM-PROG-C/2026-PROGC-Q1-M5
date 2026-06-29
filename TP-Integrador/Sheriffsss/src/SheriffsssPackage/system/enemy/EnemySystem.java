package SheriffsssPackage.system.enemy;

import SheriffsssPackage.render.CombatFloatingText;
import SheriffsssPackage.render.FlameBurstEffect;
import SheriffsssPackage.session.GameMap;
import SheriffsssPackage.session.Player;
import SheriffsssPackage.system.weapon.ItemDefinition;


import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class EnemySystem {
  private static final int MAX_ENEMIES = 2000;
  private static final double ENEMY_COLLISION_EPSILON = 0.0001;
  private static final double ENEMY_COLLISION_MAX_PUSH_PIXELS = 4.0;
  private static final Color CRIT_TEXT_COLOR = new Color(255, 45, 45);

  private final ArrayList<Enemy> enemies = new ArrayList<Enemy>(MAX_ENEMIES);
  private final ArrayList<FlameBurstEffect> flameBurstEffects = new ArrayList<FlameBurstEffect>();
  private final ArrayList<CombatFloatingText> combatFloatingTexts = new ArrayList<CombatFloatingText>();
  private final ArrayList<EnemyHitSound> hitSounds = new ArrayList<EnemyHitSound>();
  private final ArrayList<Enemy> collectedDeadEnemies = new ArrayList<Enemy>();
  private Random combatRandom = new Random(0L);

  public void reset(int seedHash) {
    this.enemies.clear();
    this.flameBurstEffects.clear();
    this.combatFloatingTexts.clear();
    this.hitSounds.clear();
    this.collectedDeadEnemies.clear();
    this.combatRandom = new Random(seedHash ^ 0xC2B2AE35);
  }

  public void clear()
    {
    this.enemies.clear();
    this.flameBurstEffects.clear();
    this.combatFloatingTexts.clear();
    this.hitSounds.clear();
    this.collectedDeadEnemies.clear();
  }

  public void addEnemy(Enemy enemy)
    {
    if (enemy != null && this.enemies.size() < MAX_ENEMIES) {
      this.enemies.add(enemy);
    }
  }

  public boolean removeEnemy(Enemy enemy)
      {
    return this.enemies.remove(enemy);
  }

  public int enemyCount()
    {
    return this.enemies.size();
  }

  public void trimEnemiesToCount(int desiredCount)
    {
    int safeCount = Math.max(0, desiredCount);
    while (this.enemies.size() > safeCount)
    {
      this.enemies.remove(this.enemies.size() - 1);
    }
  }

  public int removeByTypeAndMinimumAnimationTicks(EnemyType type, int animationTicks)
      {
    int removedCount = 0;
    for (int i = this.enemies.size() - 1; i >= 0; i--) {
      Enemy enemy = this.enemies.get(i);
      if (enemy.getType() == type && enemy.getAnimationTicks() >= animationTicks)
      {
        this.enemies.remove(i);
        removedCount++;
      }
    }
    return removedCount;
  }

  public void update(GameMap map, Player player)
        {
    if (map == null || player == null) {
      updateFlameBurstEffects();
      updateCombatFloatingTexts();
      return;
    }
    for (int i = this.enemies.size() - 1; i >= 0; i--)
      {
      Enemy enemy = this.enemies.get(i);
      if (enemy.isDead())
      {
        this.enemies.remove(i);
        continue;
      }
      enemy.update(map, player);
      if (enemy.isDead())
        {
        this.enemies.remove(i);
      }
    }
    resolveEnemyCollisions(map);
    updateFlameBurstEffects();
    updateCombatFloatingTexts();
  }

  private void resolveEnemyCollisions(GameMap map)
        {
    int count = this.enemies.size();
    for (int i = 0; i < count - 1; i++)
    {
      Enemy first = this.enemies.get(i);
      for (int j = i + 1; j < count; j++)
      {
        resolveEnemyCollision(map, first, this.enemies.get(j), i, j);
      }
    }
  }

  private void resolveEnemyCollision(GameMap map, Enemy first, Enemy second, int firstIndex, int secondIndex)
        {
    int minimumDistance = first.getType().getCollisionRadius() + second.getType().getCollisionRadius();
    double deltaX = second.getCollisionX() - first.getCollisionX();
    double deltaY = second.getCollisionY() - first.getCollisionY();
    double distanceSquared = deltaX * deltaX + deltaY * deltaY;
    double minimumDistanceSquared = minimumDistance * minimumDistance;
    if (distanceSquared >= minimumDistanceSquared)
    {
      return;
    }
    double distance;
    if (distanceSquared <= ENEMY_COLLISION_EPSILON) {
      deltaX = ((firstIndex + secondIndex) & 1) == 0 ? 1.0 : -1.0;
      deltaY = (firstIndex & 1) == 0 ? 0.35 : -0.35;
      distance = Math.sqrt(deltaX * deltaX + deltaY * deltaY);
    } else {
      distance = Math.sqrt(distanceSquared);
    }
    double pushDistance = Math.min((minimumDistance - distance) * 0.5, ENEMY_COLLISION_MAX_PUSH_PIXELS);
    if (pushDistance <= 0.0)
      {
      return;
    }
    double normalX = deltaX / distance;
    double normalY = deltaY / distance;
    first.pushBy(map, -normalX * pushDistance, -normalY * pushDistance);
    second.pushBy(map, normalX * pushDistance, normalY * pushDistance);
  }

  public void damageEnemy(Enemy enemy, double amount, Player sourcePlayer, ItemDefinition weapon)
    {
    if (enemy == null || amount <= 0.0) {
      return;
    }
    double finalDamage = amount;
    if (isCriticalHit(sourcePlayer, weapon)) {
      finalDamage *= 2.0;
      this.combatFloatingTexts.add(new CombatFloatingText(
        enemy.getWorldX(),
        enemy.getWorldY() - enemy.getType().getDrawHeight() / 2,
        "CRIT",
        CRIT_TEXT_COLOR));
    }
    recordHitSound(enemy);
    enemy.damage(finalDamage, sourcePlayer);
  }

  private void recordHitSound(Enemy enemy)
      {
    String hitSoundPath = enemy.getType().getHitSoundPath();
    if (hitSoundPath == null || hitSoundPath.isEmpty())
    {
      return;
    }
    this.hitSounds.add(new EnemyHitSound(hitSoundPath, enemy.getWorldX(), enemy.getWorldY()));
  }

  private boolean isCriticalHit(Player sourcePlayer, ItemDefinition weapon)
    {
    if (sourcePlayer == null || weapon == null || !weapon.canCrit()) {
      return false;
    }
    int critChancePercent = Math.max(0, Math.min(100, weapon.getBaseCritChancePercent()));
    return critChancePercent > 0 && this.combatRandom.nextInt(100) < critChancePercent;
  }

  public void collectDeadEnemies()
    {
    this.collectedDeadEnemies.clear();
    for (int i = this.enemies.size() - 1; i >= 0; i--)
    {
      Enemy enemy = this.enemies.get(i);
      if (!enemy.isDead())
      {
        continue;
      }
      this.collectedDeadEnemies.add(enemy);
      this.enemies.remove(i);
    }
  }

  public List<Enemy> getCollectedDeadEnemies()
      {
    return this.collectedDeadEnemies;
  }

  public void clearCollectedDeadEnemies()
  {
    this.collectedDeadEnemies.clear();
  }

  private void updateFlameBurstEffects()
    {
    for (int i = this.flameBurstEffects.size() - 1; i >= 0; i--) {
      FlameBurstEffect effect = this.flameBurstEffects.get(i);
      effect.update();
      if (effect.isExpired())
      {
        this.flameBurstEffects.remove(i);
      }
    }
  }

  private void updateCombatFloatingTexts()
        {
    for (int i = this.combatFloatingTexts.size() - 1; i >= 0; i--) {
      CombatFloatingText text = this.combatFloatingTexts.get(i);
      text.update();
      if (text.isExpired())
      {
        this.combatFloatingTexts.remove(i);
      }
    }
  }

  public List<Enemy> getEnemies()
        {
    return Collections.unmodifiableList(this.enemies);
  }

  public List<FlameBurstEffect> getFlameBurstEffects()
    {
    return this.flameBurstEffects;
  }

  public List<CombatFloatingText> getCombatFloatingTexts()
  {
    return this.combatFloatingTexts;
  }

  public List<EnemyHitSound> getHitSounds()
  {
    return this.hitSounds;
  }

  public void clearHitSounds()
  {
    this.hitSounds.clear();
  }
}
