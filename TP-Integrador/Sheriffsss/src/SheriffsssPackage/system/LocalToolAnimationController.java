package SheriffsssPackage.system;

import SheriffsssPackage.session.MapObject;
import SheriffsssPackage.system.weapon.ItemDefinition;
import SheriffsssPackage.system.weapon.WeaponUseResult;

public class LocalToolAnimationController
{
  private boolean usingTool;
  private int toolUseTicks;
  private int toolUseDurationTicks = 1;
  private ItemDefinition toolAnimationDefinition;
  private int toolAnimationTicksRemaining;
  private MapObject toolTargetObject;

  public void update()
  {
    if (this.toolAnimationTicksRemaining > 0)
    {
      this.toolAnimationTicksRemaining--;
    }
  }

  public boolean isUsingTool()
  {
    return this.usingTool;
  }

  public int getToolUseTicks()
  {
    return this.toolUseTicks;
  }

  public int getToolUseDurationTicks()
  {
    return this.toolUseDurationTicks;
  }

  public ItemDefinition getToolAnimationDefinition()
  {
    return this.toolAnimationDefinition;
  }

  public int getToolAnimationTicksRemaining()
  {
    return this.toolAnimationTicksRemaining;
  }

  public MapObject getToolTargetObject()
  {
    return this.toolTargetObject;
  }

  public void startToolAnimation(ItemDefinition definition)
  {
    this.toolAnimationDefinition = definition;
    this.toolAnimationTicksRemaining = definition.getAnimationDurationTicks();
  }

  public void tickToolAnimation()
  {
    if (this.toolAnimationTicksRemaining > 0)
    {
      this.toolUseTicks++;
    }
  }

  public void resetToolAnimation()
  {
    this.usingTool = false;
    this.toolUseTicks = 0;
    this.toolUseDurationTicks = 1;
    this.toolAnimationDefinition = null;
    this.toolAnimationTicksRemaining = 0;
  }

  public void setUsingTool(boolean using)
  {
    this.usingTool = using;
  }

  public void setToolUseTicks(int ticks)
  {
    this.toolUseTicks = ticks;
  }

  public void setToolUseDurationTicks(int duration)
  {
    this.toolUseDurationTicks = duration;
  }

  public void setToolTargetObject(MapObject target)
  {
    this.toolTargetObject = target;
  }

  public void clearToolTarget()
  {
    this.toolTargetObject = null;
  }

  public boolean isToolAnimationActive()
  {
    return this.toolAnimationTicksRemaining > 0;
  }
}
