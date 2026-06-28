package SheriffsssPackage.combat;
import SheriffsssPackage.core.Facing;
import SheriffsssPackage.player.Player;
import SheriffsssPackage.player.ItemDefinition;
import SheriffsssPackage.player.ItemDefinitionDrawConfig;

import java.util.Random;

/**
 * Stateless math for player/enemy shooting: resolves the world-space muzzle
 * (barrel) origin of a held weapon for a given facing, maps an aim delta to a
 * Facing, and applies an accuracy-based horizontal spread to the aim point.
 * Keeps the heavy trigonometry out of {@link Game}, which owns the firing state.
 */
final class ShootingSystem {

  private static final double DIAGONAL_UNIT = 0.70710678118;
  private static final double MIN_AIM_LENGTH = 0.001;
  private static final double SPREAD_HALF_FACTOR = 0.5;

  private ShootingSystem() {
  }

  /** World X of the weapon barrel tip for the given holder and facing. */
  static int heldItemOriginWorldX(Player sourcePlayer, ItemDefinition definition, Facing facing) {
    if (sourcePlayer == null || definition == null || !definition.isHandEquipable()) {
      return sourcePlayer == null ? 0 : sourcePlayer.getX();
    }
    ItemDefinitionDrawConfig drawConfig = definition.getDrawConfig();
    return (int) Math.round(heldItemBarrelAnchorWorldX(sourcePlayer, definition, drawConfig, facing)
      + rotatedBarrelAnchorOffsetX(drawConfig, facing));
  }

  /** World Y of the weapon barrel tip for the given holder and facing. */
  static int heldItemOriginWorldY(Player sourcePlayer, ItemDefinition definition, Facing facing) {
    if (sourcePlayer == null || definition == null || !definition.isHandEquipable()) {
      return sourcePlayer == null ? 0 : sourcePlayer.getY();
    }
    ItemDefinitionDrawConfig drawConfig = definition.getDrawConfig();
    return (int) Math.round(heldItemBarrelAnchorWorldY(sourcePlayer, definition, drawConfig, facing)
      + rotatedBarrelAnchorOffsetY(drawConfig, facing));
  }

  private static double rotatedBarrelAnchorOffsetX(ItemDefinitionDrawConfig drawConfig, Facing facing) {
    double offsetX = drawConfig.isMirrored(facing) ? -drawConfig.getBarrelAnchorOffsetX() : drawConfig.getBarrelAnchorOffsetX();
    double offsetY = drawConfig.getBarrelAnchorOffsetY();
    double angle = drawConfig.getBaseAngle(facing);
    return offsetX * Math.cos(angle) - offsetY * Math.sin(angle);
  }

  private static double rotatedBarrelAnchorOffsetY(ItemDefinitionDrawConfig drawConfig, Facing facing) {
    double offsetX = drawConfig.isMirrored(facing) ? -drawConfig.getBarrelAnchorOffsetX() : drawConfig.getBarrelAnchorOffsetX();
    double offsetY = drawConfig.getBarrelAnchorOffsetY();
    double angle = drawConfig.getBaseAngle(facing);
    return offsetX * Math.sin(angle) + offsetY * Math.cos(angle);
  }

  private static double heldItemBarrelAnchorWorldX(Player sourcePlayer, ItemDefinition definition, ItemDefinitionDrawConfig drawConfig, Facing facing) {
    int drawX = sourcePlayer.getX() + drawConfig.getBaseOffsetX(facing);
    int drawY = sourcePlayer.getY() + drawConfig.getBaseOffsetY(facing);
    int itemWidth = definition.getHeldDrawWidth();
    int itemHeight = definition.getHeldDrawHeight();
    double centerX = drawX + itemWidth / 2.0;
    double centerY = drawY + itemHeight / 2.0;
    double anchorX = drawConfig.isMirrored(facing) ? drawX : drawX + itemWidth;
    double anchorY = drawY;
    return rotateX(anchorX, anchorY, centerX, centerY, drawConfig.getBaseAngle(facing));
  }

  private static double heldItemBarrelAnchorWorldY(Player sourcePlayer, ItemDefinition definition, ItemDefinitionDrawConfig drawConfig, Facing facing) {
    int drawX = sourcePlayer.getX() + drawConfig.getBaseOffsetX(facing);
    int drawY = sourcePlayer.getY() + drawConfig.getBaseOffsetY(facing);
    int itemWidth = definition.getHeldDrawWidth();
    int itemHeight = definition.getHeldDrawHeight();
    double centerX = drawX + itemWidth / 2.0;
    double centerY = drawY + itemHeight / 2.0;
    double anchorX = drawConfig.isMirrored(facing) ? drawX : drawX + itemWidth;
    double anchorY = drawY;
    return rotateY(anchorX, anchorY, centerX, centerY, drawConfig.getBaseAngle(facing));
  }

  private static double rotateX(double x, double y, double centerX, double centerY, double angle) {
    double deltaX = x - centerX;
    double deltaY = y - centerY;
    return centerX + deltaX * Math.cos(angle) - deltaY * Math.sin(angle);
  }

  private static double rotateY(double x, double y, double centerX, double centerY, double angle) {
    double deltaX = x - centerX;
    double deltaY = y - centerY;
    return centerY + deltaX * Math.sin(angle) + deltaY * Math.cos(angle);
  }

  /** Unit direction vector for a Facing (8-way). */
  static double[] facingVector(Facing facing) {
    if (facing == Facing.LEFT) {
      return new double[] {-1.0, 0.0};
    }
    if (facing == Facing.RIGHT) {
      return new double[] {1.0, 0.0};
    }
    if (facing == Facing.UP) {
      return new double[] {0.0, -1.0};
    }
    if (facing == Facing.UP_LEFT) {
      return new double[] {-DIAGONAL_UNIT, -DIAGONAL_UNIT};
    }
    if (facing == Facing.UP_RIGHT) {
      return new double[] {DIAGONAL_UNIT, -DIAGONAL_UNIT};
    }
    if (facing == Facing.DOWN_LEFT) {
      return new double[] {-DIAGONAL_UNIT, DIAGONAL_UNIT};
    }
    if (facing == Facing.DOWN_RIGHT) {
      return new double[] {DIAGONAL_UNIT, DIAGONAL_UNIT};
    }
    return new double[] {0.0, 1.0};
  }

  /** Maps an aim delta to the nearest 8-way Facing. */
  static Facing facingFromDelta(double deltaX, double deltaY) {
    double angle = Math.toDegrees(Math.atan2(deltaY, deltaX));
    if (angle >= -22.5 && angle < 22.5) {
      return Facing.RIGHT;
    }
    if (angle >= 22.5 && angle < 67.5) {
      return Facing.DOWN_RIGHT;
    }
    if (angle >= 67.5 && angle < 112.5) {
      return Facing.DOWN;
    }
    if (angle >= 112.5 && angle < 157.5) {
      return Facing.DOWN_LEFT;
    }
    if (angle >= -67.5 && angle < -22.5) {
      return Facing.UP_RIGHT;
    }
    if (angle >= -112.5 && angle < -67.5) {
      return Facing.UP;
    }
    if (angle >= -157.5 && angle < -112.5) {
      return Facing.UP_LEFT;
    }
    return Facing.LEFT;
  }

  /**
   * Applies accuracy-based spread to the aim point and returns the resulting
   * {deltaX, deltaY, length} from the muzzle origin. Lower accuracy widens the
   * random perpendicular offset; accuracy &gt;= 1.0 returns the input unchanged.
   */
  static double[] deviateTargetForSpread(int targetWorldX, int targetWorldY, int originX, int originY,
      double deltaX, double deltaY, double length, double accuracy, Random random) {
    if (accuracy >= 1.0 || length <= MIN_AIM_LENGTH) {
      return new double[] {deltaX, deltaY, length};
    }
    double halfBasePixels = length * SPREAD_HALF_FACTOR * (1.0 - accuracy);
    double baseOffset = (random.nextDouble() * 2.0 - 1.0) * halfBasePixels;
    double baseUnitX = -deltaY / length;
    double baseUnitY = deltaX / length;
    double deviatedTargetX = targetWorldX + baseUnitX * baseOffset;
    double deviatedTargetY = targetWorldY + baseUnitY * baseOffset;
    double newDeltaX = deviatedTargetX - originX;
    double newDeltaY = deviatedTargetY - originY;
    double newLength = Math.sqrt(newDeltaX * newDeltaX + newDeltaY * newDeltaY);
    return new double[] {newDeltaX, newDeltaY, newLength};
  }
}
