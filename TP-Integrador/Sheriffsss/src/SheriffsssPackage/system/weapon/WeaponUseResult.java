package SheriffsssPackage.system.weapon;


public final class WeaponUseResult
{
  private final boolean fired;
  private final ItemDefinition weapon;
  private final ProjectileType projectileType;
  private final int startWorldX;
  private final int startWorldY;
  private final int aimWorldX;
  private final int aimWorldY;

  public WeaponUseResult(
      boolean fired,
      ItemDefinition weapon,
      ProjectileType projectileType,
      int startWorldX,
      int startWorldY,
      int aimWorldX,
      int aimWorldY)
      {
    this.fired = fired;
    this.weapon = weapon;
    this.projectileType = projectileType;
    this.startWorldX = startWorldX;
    this.startWorldY = startWorldY;
    this.aimWorldX = aimWorldX;
    this.aimWorldY = aimWorldY;
  }

  public static WeaponUseResult missed()
  {
    return new WeaponUseResult(false, null, null, 0, 0, 0, 0);
  }

  public boolean fired()
  {
    return this.fired;
  }

  public ItemDefinition weapon()
  {
    return this.weapon;
  }

  public ProjectileType projectileType()
  {
    return this.projectileType;
  }

  public int startWorldX()
  {
    return this.startWorldX;
  }

  public int startWorldY()
  {
    return this.startWorldY;
  }

  public int aimWorldX()
  {
    return this.aimWorldX;
  }

  public int aimWorldY()
  {
    return this.aimWorldY;
  }
}
