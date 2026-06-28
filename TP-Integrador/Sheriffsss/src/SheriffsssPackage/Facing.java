package SheriffsssPackage;

public enum Facing {
  DOWN(0, 0, 1),
  LEFT(1, -1, 0),
  UP(2, 0, -1),
  RIGHT(3, 1, 0),
  DOWN_LEFT(4, -1, 1),
  UP_RIGHT(5, 1, -1),
  UP_LEFT(6, -1, -1),
  DOWN_RIGHT(7, 1, 1);

  private final int spriteIndex;
  private final double unitX;
  private final double unitY;

  Facing(int spriteIndex, int rawX, int rawY) {
    this.spriteIndex = spriteIndex;
    double length = Math.sqrt(rawX * rawX + rawY * rawY);
    this.unitX = rawX / length;
    this.unitY = rawY / length;
  }

  public int getSpriteIndex() {
    return this.spriteIndex;
  }

  public double unitX() {
    return this.unitX;
  }

  public double unitY() {
    return this.unitY;
  }
}
