package SheriffsssPackage.session;

import SheriffsssPackage.MagicConstants;
import SheriffsssPackage.context.AssetManager;
import SheriffsssPackage.context.GameConfig;
import SheriffsssPackage.system.weapon.ItemDefinition;


import java.awt.image.BufferedImage;

public class Player
{
  public static final int PLAYER_WIDTH = 64;
  public static final int PLAYER_HEIGHT = 64;
  public static final int HITBOX_WIDTH = PLAYER_WIDTH / MagicConstants.DIVISOR_HALF;
  public static final int HITBOX_HEIGHT = PLAYER_HEIGHT / MagicConstants.DIVISOR_HALF;
  private static final double KNOCKBACK_FRICTION = 0.80;
  private static final double MIN_KNOCKBACK_SPEED = 0.12;
  private static final double DEFAULT_MAX_HP = 100.0;
  private static final double NEAR_ZERO_LENGTH = 0.001;

  private final String name;
  private final Equipment equipment = new Equipment();
  private final BufferedImage[] sprites = new BufferedImage[Facing.values().length];
  private final double speed;

  private double maxHP = DEFAULT_MAX_HP;
  private double currentHP = DEFAULT_MAX_HP;
  private double knockbackX;
  private double knockbackY;
  private double moveCarryX;
  private double moveCarryY;
  private double lastLinearVelocityPixels;
  private int x;
  private int y;
  private Facing facing = Facing.DOWN;
  private boolean takingDamage;
  private boolean dead;

  public Player(String name, int x, int y, AssetManager assets)
  {
    this.name = name;
    this.x = x;
    this.y = y;
    this.speed = GameConfig.PLAYER_SPEED;
    loadDirectionalSprite(assets, Facing.DOWN   , "sprites/sheriffsss-player-sprites/2/sheriff-south.png");
    loadDirectionalSprite(assets, Facing.LEFT   , "sprites/sheriffsss-player-sprites/2/sheriff-west.png");
    loadDirectionalSprite(assets, Facing.UP     , "sprites/sheriffsss-player-sprites/2/sheriff-north.png");
    loadDirectionalSprite(assets, Facing.RIGHT    , "sprites/sheriffsss-player-sprites/2/sheriff-east.png");
    loadDirectionalSprite(assets, Facing.DOWN_LEFT  , "sprites/sheriffsss-player-sprites/2/sheriff-south-west.png");
    loadDirectionalSprite(assets, Facing.UP_RIGHT , "sprites/sheriffsss-player-sprites/2/sheriff-north-east.png");
    loadDirectionalSprite(assets, Facing.UP_LEFT  , "sprites/sheriffsss-player-sprites/2/sheriff-north-west.png");
    loadDirectionalSprite(assets, Facing.DOWN_RIGHT , "sprites/sheriffsss-player-sprites/2/sheriff-south-east.png");
  }

  private void loadDirectionalSprite(AssetManager assets, Facing facing, String spritePath)
 {
    this.sprites[facing.getSpriteIndex()] = assets.getImage(spritePath);
  }

  public int getX()
  { return this.x; }
  public int getY()
 { return this.y; }
  public double getLastLinearVelocityPixels()
 { return this.lastLinearVelocityPixels; }
  public double getMaxHP()
 { return this.maxHP; }
  public double getCurrentHP()
 { return this.currentHP; }
  public int getFeetWorldY()
 { return this.y + PLAYER_HEIGHT / MagicConstants.DIVISOR_HALF; }
  public int getHitboxWidth()
 { return HITBOX_WIDTH; }
  public int getHitboxHeight()
 { return HITBOX_HEIGHT; }
  public int getHitboxLeftWorldX()
 { return this.x - HITBOX_WIDTH / MagicConstants.DIVISOR_HALF; }
  public int getHitboxTopWorldY()
 { return this.y - HITBOX_HEIGHT / MagicConstants.DIVISOR_HALF; }
  public int getHitboxRightWorldX()
 { return getHitboxLeftWorldX() + HITBOX_WIDTH - 1; }
  public int getHitboxBottomWorldY()
 { return getHitboxTopWorldY() + HITBOX_HEIGHT - 1; }
  public Equipment getEquipment()
 { return this.equipment; }
  public Facing getFacing()
 { return this.facing; }
  public BufferedImage getCurrentImage()
 { return this.sprites[this.facing.getSpriteIndex()]; }
  public boolean getTakingDamage()
 { return this.takingDamage; }
  public void setTakingDamage(boolean takingDamage)
 { this.takingDamage = takingDamage; }

  public double getSpeedPixelsPerTick()
 {
    return Math.max(1.0, this.speed);
  }

  public int consumeMoveDeltaX(int direction)
 {
    return consumeMoveDeltaX(direction, 1.0);
  }

  public int consumeMoveDeltaX(int direction, double speedScale)
 {
    if (direction == 0)
    {
      this.moveCarryX = 0.0;
      return 0;
    }
    double desired = direction * getSpeedPixelsPerTick() * speedScale + this.moveCarryX;
    int delta = (int) desired;
    this.moveCarryX = desired - delta;
    return delta;
  }

  public int consumeMoveDeltaY(int direction)
 {
    return consumeMoveDeltaY(direction, 1.0);
  }

  public int consumeMoveDeltaY(int direction, double speedScale)
 {
    if (direction == 0)
    {
      this.moveCarryY = 0.0;
      return 0;
    }
    double desired = direction * getSpeedPixelsPerTick() * speedScale + this.moveCarryY;
    int delta = (int) desired;
    this.moveCarryY = desired - delta;
    return delta;
  }

  public int applyAttackSpeedToCooldown(int cooldownTicks)
 {
    return Math.max(1, cooldownTicks);
  }

  public void moveBy(int deltaX, int deltaY)
 {
    this.x += deltaX;
    this.y += deltaY;
  }

  public void updateKnockback(GameMap map)
 {
    if (Math.abs(this.knockbackX) < MIN_KNOCKBACK_SPEED && Math.abs(this.knockbackY) < MIN_KNOCKBACK_SPEED)
    {
      this.knockbackX = 0.0;
      this.knockbackY = 0.0;
      return;
    }
    moveWithCollision(map, this.knockbackX, this.knockbackY);
    this.knockbackX *= KNOCKBACK_FRICTION;
    this.knockbackY *= KNOCKBACK_FRICTION;
  }

  private void moveWithCollision(GameMap map, double deltaX, double deltaY)
 {
    applyKnockbackAxisX(map, (int) Math.round(deltaX));
    applyKnockbackAxisY(map, (int) Math.round(deltaY));
  }

  private void applyKnockbackAxisX(GameMap map, int stepX)
 {
    if (stepX == 0)
    {
      return;
    }
    this.x += stepX;
    if (isHitboxBlocked(map))
    {
      this.x -= stepX;
      this.knockbackX = 0.0;
    }
  }

  private void applyKnockbackAxisY(GameMap map, int stepY)
 {
    if (stepY == 0)
    {
      return;
    }
    this.y += stepY;
    if (isHitboxBlocked(map))
    {
      this.y -= stepY;
      this.knockbackY = 0.0;
    }
  }

  public boolean isHitboxBlocked(GameMap map)
 {
    return map.isAreaBlockedAtWorld(getHitboxLeftWorldX(), getHitboxTopWorldY(), getHitboxRightWorldX(), getHitboxBottomWorldY());
  }

  public void applyKnockbackFrom(int sourceX, int sourceY, double strength)
 {
    double deltaX = this.x - sourceX;
    double deltaY = getFeetWorldY() - sourceY;
    double length = Math.sqrt(deltaX * deltaX + deltaY * deltaY);
    if (length <= NEAR_ZERO_LENGTH)
    {
      applyKnockbackDown(strength);
      return;
    }
    this.knockbackX = deltaX / length * strength;
    this.knockbackY = deltaY / length * strength;
  }

  private void applyKnockbackDown(double strength)
 {
    this.knockbackX = 0.0;
    this.knockbackY = strength;
  }

  public void updateLinearVelocityFromPosition(int previousX, int previousY)
 {
    int deltaX = this.x - previousX;
    int deltaY = this.y - previousY;
    this.lastLinearVelocityPixels = Math.sqrt(deltaX * deltaX + deltaY * deltaY);
  }

  public void setFacing(Facing facing)
 {
    if (facing != null)
    {
      this.facing = facing;
    }
  }

  public void damage(double amount)
 {
    this.currentHP -= amount;
    if (this.currentHP <= 0.0)
    {
      die();
    }
  }

  public void damageEnemyAttack(double amount)
 {
    if (amount > 0.0)
    {
      damage(amount);
    }
  }

  public void heal(double amount)
 {
    this.currentHP = Math.min(this.maxHP, this.currentHP + amount);
  }

  public void die()
 {
    this.currentHP = 0.0;
    this.dead = true;
    this.takingDamage = false;
  }

  public boolean isDead()
 {
    return this.dead;
  }

  public void updateFacing(int moveX, int moveY)
 {
    if (moveX != 0 && moveY != 0)
    {
      this.facing = facingDiagonal(moveX, moveY);
    }
    else if (moveX != 0)
    {
      this.facing = moveX < 0 ? Facing.LEFT : Facing.RIGHT;
    }
    else if (moveY != 0)
    {
      this.facing = moveY < 0 ? Facing.UP : Facing.DOWN;
    }
  }

  private static Facing facingDiagonal(int moveX, int moveY)
 {
    boolean up = moveY < 0;
    boolean left = moveX < 0;
    if (up)
    {
      return left ? Facing.UP_LEFT : Facing.UP_RIGHT;
    }
    return left ? Facing.DOWN_LEFT : Facing.DOWN_RIGHT;
  }

  public boolean shouldRenderHeldItem()
 {
    ItemDefinition equippedWeapon = this.equipment.getEquippedWeapon();
    return equippedWeapon != null && equippedWeapon.isHandEquipable();
  }
}
