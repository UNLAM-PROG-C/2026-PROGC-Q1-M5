package SheriffsssPackage.system.weapon;

import SheriffsssPackage.render.ItemDefinitionDrawConfig;
import SheriffsssPackage.session.Facing;
import SheriffsssPackage.session.Player;
import SheriffsssPackage.session.PlayerRuntimeState;


import java.util.Random;


public final class WeaponUseSystem {
  private static final double MOVING_ACCURACY_PENALTY = 0.2;
  private static final double MOVING_ACCURACY_MIN_LINEAR_SPEED = 0.1;
  private static final double MIN_AIM_LENGTH = 0.001;
  private static final double ACCURACY_SPREAD_HALF_SCALE = 0.5;
  private static final double RANDOM_SIGN_RANGE = 2.0;
  private static final double PROJECTILE_AIM_DISTANCE_PIXELS = 1000.0;
  private static final double EIGHT_WAY_ARC_DEGREES = 45.0;
  private static final double EIGHT_WAY_HALF_ARC_DEGREES = 22.5;
  private static final Facing[] FACING_BY_ANGLE_INDEX = {
    Facing.RIGHT,
    Facing.DOWN_RIGHT,
    Facing.DOWN,
    Facing.DOWN_LEFT,
    Facing.LEFT,
    Facing.UP_LEFT,
    Facing.UP,
    Facing.UP_RIGHT
  };

  private final Random random = new Random(0L);

  public void updateProjectileWeaponCooldown(PlayerRuntimeState runtime) {
    if (runtime != null && runtime.projectileWeaponCooldownTicks > 0) {
      runtime.projectileWeaponCooldownTicks--;
    }
  }

  public WeaponUseResult fireProjectileWeapon(
      ProjectileSystem projectileSystem,
      Player player,
      PlayerRuntimeState runtime,
      ItemDefinition weapon,
      int targetWorldX,
      int targetWorldY) {
    if (!canFire(player, runtime, weapon)) {
      return WeaponUseResult.missed();
    }
    ItemDefinition ammoDefinition = weapon.getProjectileAmmoDefinition();
    if (ammoDefinition == null) {
      return WeaponUseResult.missed();
    }
    return spawnProjectile(projectileSystem, player, runtime, weapon, ammoDefinition,
      targetWorldX, targetWorldY);
  }

  public int heldItemOriginWorldX(Player player, ItemDefinition definition, Facing facing) {
    if (player == null || definition == null || !definition.isHandEquipable()) {
      return player == null ? 0 : player.getX();
    }
    ItemDefinitionDrawConfig drawConfig = definition.getDrawConfig();
    return (int) Math.round(heldItemBarrelAnchorWorldX(player, definition, drawConfig, facing)
      + rotatedBarrelAnchorOffsetX(drawConfig, facing));
  }

  public int heldItemOriginWorldY(Player player, ItemDefinition definition, Facing facing) {
    if (player == null || definition == null || !definition.isHandEquipable()) {
      return player == null ? 0 : player.getY();
    }
    ItemDefinitionDrawConfig drawConfig = definition.getDrawConfig();
    return (int) Math.round(heldItemBarrelAnchorWorldY(player, definition, drawConfig, facing)
      + rotatedBarrelAnchorOffsetY(drawConfig, facing));
  }

  public double resolveEffectiveAccuracy(Player sourcePlayer, Player currentPlayer, ItemDefinition weapon) {
    if (weapon == null) {
      return 1.0;
    }
    double accuracy = weapon.getAccuracy();
    if (sourcePlayer == currentPlayer && isPlayerMovingForAccuracy(currentPlayer)) {
      accuracy -= MOVING_ACCURACY_PENALTY;
    }
    return clampAccuracy(accuracy);
  }

  private boolean canFire(Player player, PlayerRuntimeState runtime, ItemDefinition weapon) {
    return player != null && runtime != null && weapon != null && weapon.isProjectileWeapon()
      && runtime.projectileWeaponCooldownTicks <= 0;
  }

  private WeaponUseResult spawnProjectile(
      ProjectileSystem projectileSystem,
      Player player,
      PlayerRuntimeState runtime,
      ItemDefinition weapon,
      ItemDefinition ammoDefinition,
      int targetWorldX,
      int targetWorldY) {
    ShotVector shotVector = createShotVector(player, weapon, targetWorldX, targetWorldY);
    ProjectileType type = weapon.getProjectileType();
    boolean spawned = projectileSystem.spawn(type, player, weapon, shotVector.startWorldX,
      shotVector.startWorldY, shotVector.aimWorldX, shotVector.aimWorldY,
      weapon.getProjectileSpeedPixels(ammoDefinition), weapon.getProjectileDamage(ammoDefinition),
      weapon.getProjectileKnockbackStrengthPixels(ammoDefinition),
      weapon.getProjectileLifeTicks(ammoDefinition));
    if (!spawned) {
      return WeaponUseResult.missed();
    }
    runtime.projectileWeaponCooldownTicks =
      player.applyAttackSpeedToCooldown(weapon.getProjectileCooldownTicks(ammoDefinition));
    return new WeaponUseResult(true, weapon, type, shotVector.startWorldX, shotVector.startWorldY,
      shotVector.aimWorldX, shotVector.aimWorldY);
  }

  private ShotVector createShotVector(Player player, ItemDefinition weapon, int targetWorldX, int targetWorldY) {
    Facing shotFacing = facingFromTarget(player, targetWorldX, targetWorldY);
    int originX = heldItemOriginWorldX(player, weapon, shotFacing);
    int originY = heldItemOriginWorldY(player, weapon, shotFacing);
    AimVector aimVector = createAimVector(player, weapon, shotFacing, originX, originY,
      targetWorldX, targetWorldY);
    player.setFacing(shotFacing);
    int aimWorldX = originX + (int) Math.round(aimVector.directionX * PROJECTILE_AIM_DISTANCE_PIXELS);
    int aimWorldY = originY + (int) Math.round(aimVector.directionY * PROJECTILE_AIM_DISTANCE_PIXELS);
    return new ShotVector(originX, originY, aimWorldX, aimWorldY);
  }

  private AimVector createAimVector(
      Player player,
      ItemDefinition weapon,
      Facing shotFacing,
      int originX,
      int originY,
      int targetWorldX,
      int targetWorldY) {
    double deltaX = targetWorldX - originX;
    double deltaY = targetWorldY - originY;
    double length = Math.sqrt(deltaX * deltaX + deltaY * deltaY);
    if (length <= MIN_AIM_LENGTH) {
      return new AimVector(shotFacing.unitX(), shotFacing.unitY());
    }
    return applyAccuracy(player, weapon, originX, originY, targetWorldX, targetWorldY, deltaX, deltaY);
  }

  private AimVector applyAccuracy(
      Player player,
      ItemDefinition weapon,
      int originX,
      int originY,
      int targetWorldX,
      int targetWorldY,
      double deltaX,
      double deltaY) {
    double length = Math.sqrt(deltaX * deltaX + deltaY * deltaY);
    double accuracy = resolveEffectiveAccuracy(player, player, weapon);
    if (accuracy >= 1.0) {
      return new AimVector(deltaX / length, deltaY / length);
    }
    return deviatedAim(originX, originY, targetWorldX, targetWorldY, deltaX, deltaY, accuracy);
  }

  private AimVector deviatedAim(
      int originX,
      int originY,
      int targetWorldX,
      int targetWorldY,
      double deltaX,
      double deltaY,
      double accuracy) {
    double length = Math.sqrt(deltaX * deltaX + deltaY * deltaY);
    double halfBasePixels = length * ACCURACY_SPREAD_HALF_SCALE * (1.0 - accuracy);
    double baseOffset = (this.random.nextDouble() * RANDOM_SIGN_RANGE - 1.0) * halfBasePixels;
    double deviatedTargetX = targetWorldX + (-deltaY / length) * baseOffset;
    double deviatedTargetY = targetWorldY + (deltaX / length) * baseOffset;
    double deviatedDeltaX = deviatedTargetX - originX;
    double deviatedDeltaY = deviatedTargetY - originY;
    double deviatedLength = Math.sqrt(deviatedDeltaX * deviatedDeltaX + deviatedDeltaY * deviatedDeltaY);
    return new AimVector(deviatedDeltaX / deviatedLength, deviatedDeltaY / deviatedLength);
  }

  private Facing facingFromTarget(Player player, int targetWorldX, int targetWorldY) {
    double deltaX = targetWorldX - player.getX();
    double deltaY = targetWorldY - player.getY();
    double length = Math.sqrt(deltaX * deltaX + deltaY * deltaY);
    return length <= MIN_AIM_LENGTH ? player.getFacing() : facingFromDelta(deltaX, deltaY);
  }

  private Facing facingFromDelta(double deltaX, double deltaY) {
    double angle = Math.toDegrees(Math.atan2(deltaY, deltaX));
    int index = (int) Math.floor((angle + EIGHT_WAY_HALF_ARC_DEGREES) / EIGHT_WAY_ARC_DEGREES);
    if (index < 0) {
      index += FACING_BY_ANGLE_INDEX.length;
    }
    if (index >= FACING_BY_ANGLE_INDEX.length) {
      index = 0;
    }
    return FACING_BY_ANGLE_INDEX[index];
  }

  private double rotatedBarrelAnchorOffsetX(ItemDefinitionDrawConfig drawConfig, Facing facing) {
    double offsetX = drawConfig.isMirrored(facing) ? -drawConfig.getBarrelAnchorOffsetX() : drawConfig.getBarrelAnchorOffsetX();
    double offsetY = drawConfig.getBarrelAnchorOffsetY();
    double angle = drawConfig.getBaseAngle(facing);
    return offsetX * Math.cos(angle) - offsetY * Math.sin(angle);
  }

  private double rotatedBarrelAnchorOffsetY(ItemDefinitionDrawConfig drawConfig, Facing facing) {
    double offsetX = drawConfig.isMirrored(facing) ? -drawConfig.getBarrelAnchorOffsetX() : drawConfig.getBarrelAnchorOffsetX();
    double offsetY = drawConfig.getBarrelAnchorOffsetY();
    double angle = drawConfig.getBaseAngle(facing);
    return offsetX * Math.sin(angle) + offsetY * Math.cos(angle);
  }

  private double heldItemBarrelAnchorWorldX(Player player, ItemDefinition definition, ItemDefinitionDrawConfig drawConfig, Facing facing) {
    int drawX = player.getX() + drawConfig.getBaseOffsetX(facing);
    int drawY = player.getY() + drawConfig.getBaseOffsetY(facing);
    int itemWidth = definition.getHeldDrawWidth();
    int itemHeight = definition.getHeldDrawHeight();
    double centerX = drawX + itemWidth / 2.0;
    double centerY = drawY + itemHeight / 2.0;
    double anchorX = drawConfig.isMirrored(facing) ? drawX : drawX + itemWidth;
    return rotateX(anchorX, drawY, centerX, centerY, drawConfig.getBaseAngle(facing));
  }

  private double heldItemBarrelAnchorWorldY(Player player, ItemDefinition definition, ItemDefinitionDrawConfig drawConfig, Facing facing) {
    int drawX = player.getX() + drawConfig.getBaseOffsetX(facing);
    int drawY = player.getY() + drawConfig.getBaseOffsetY(facing);
    int itemWidth = definition.getHeldDrawWidth();
    int itemHeight = definition.getHeldDrawHeight();
    double centerX = drawX + itemWidth / 2.0;
    double centerY = drawY + itemHeight / 2.0;
    double anchorX = drawConfig.isMirrored(facing) ? drawX : drawX + itemWidth;
    return rotateY(anchorX, drawY, centerX, centerY, drawConfig.getBaseAngle(facing));
  }

  private double rotateX(double x, double y, double centerX, double centerY, double angle) {
    double deltaX = x - centerX;
    double deltaY = y - centerY;
    return centerX + deltaX * Math.cos(angle) - deltaY * Math.sin(angle);
  }

  private double rotateY(double x, double y, double centerX, double centerY, double angle) {
    double deltaX = x - centerX;
    double deltaY = y - centerY;
    return centerY + deltaX * Math.sin(angle) + deltaY * Math.cos(angle);
  }

  private boolean isPlayerMovingForAccuracy(Player player) {
    return player != null && player.getLastLinearVelocityPixels() > MOVING_ACCURACY_MIN_LINEAR_SPEED;
  }

  private double clampAccuracy(double accuracy) {
    return Math.max(0.0, Math.min(1.0, accuracy));
  }

  private static final class AimVector {
    private final double directionX;
    private final double directionY;

    private AimVector(double directionX, double directionY) {
      this.directionX = directionX;
      this.directionY = directionY;
    }
  }

  private static final class ShotVector {
    private final int startWorldX;
    private final int startWorldY;
    private final int aimWorldX;
    private final int aimWorldY;

    private ShotVector(int startWorldX, int startWorldY, int aimWorldX, int aimWorldY) {
      this.startWorldX = startWorldX;
      this.startWorldY = startWorldY;
      this.aimWorldX = aimWorldX;
      this.aimWorldY = aimWorldY;
    }
  }
}
