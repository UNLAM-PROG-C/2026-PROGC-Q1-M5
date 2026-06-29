package SheriffsssPackage.system;

import SheriffsssPackage.session.GameMap;
import SheriffsssPackage.session.Player;



public final class PlayerMovementSystem {
  private static final double STRAIGHT_MOVE_SCALE = 1.0;
  private static final double DIAGONAL_AXIS_COUNT = 2.0;
  private static final double DIAGONAL_MOVE_SCALE = 1.0 / Math.sqrt(DIAGONAL_AXIS_COUNT);

  public void update(
      GameMap map,
      Player player,
      int moveX,
      int moveY,
      boolean canUpdateFacing) {
    double moveScale = normalizedMoveScale(moveX, moveY);
    int previousX = player.getX();
    int previousY = player.getY();

    moveHorizontally(map, player, moveX, moveScale);
    moveVertically(map, player, moveY, moveScale);
    player.updateKnockback(map);
    player.updateLinearVelocityFromPosition(previousX, previousY);
    updateFacing(player, moveX, moveY, canUpdateFacing);
  }

  private double normalizedMoveScale(int moveX, int moveY) {
    if (moveX == 0 || moveY == 0) {
      return STRAIGHT_MOVE_SCALE;
    }
    return DIAGONAL_MOVE_SCALE;
  }

  private void moveHorizontally(GameMap map, Player player, int direction, double moveScale) {
    if (direction == 0) {
      player.consumeMoveDeltaX(0);
      return;
    }
    int deltaX = player.consumeMoveDeltaX(direction, moveScale);
    moveWithRollback(map, player, deltaX, 0);
  }

  private void moveVertically(GameMap map, Player player, int direction, double moveScale) {
    if (direction == 0) {
      player.consumeMoveDeltaY(0);
      return;
    }
    int deltaY = player.consumeMoveDeltaY(direction, moveScale);
    moveWithRollback(map, player, 0, deltaY);
  }

  private void moveWithRollback(GameMap map, Player player, int deltaX, int deltaY) {
    player.moveBy(deltaX, deltaY);
    if (player.isHitboxBlocked(map)) {
      player.moveBy(-deltaX, -deltaY);
    }
  }

  private void updateFacing(Player player, int moveX, int moveY, boolean canUpdateFacing) {
    if (canUpdateFacing) {
      player.updateFacing(moveX, moveY);
    }
  }
}
