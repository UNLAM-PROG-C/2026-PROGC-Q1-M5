package SheriffsssPackage.level;

import SheriffsssPackage.MagicConstants;
import SheriffsssPackage.system.MusicController;

public class TrainingTutorialController
{
  private static final int TARGET_BLINK_ANIMATION_TICKS = 8;
  private static final int TARGET_ANIMATION_RESET_INTERVAL = 10;
  private int targetBlinkTicks;
  private int targetAnimationResetTicks;
  private int hintAlphaTicks;
  private boolean isHintFullyFaded;
  private TutorialPhase phase = TutorialPhase.AIM;
  private MusicController musicController;

  public TrainingTutorialController(MusicController musicController)
  {
    this.musicController = musicController;
  }

  public void update()
  {
    updateTutorialFlow();
    updateHintAlpha();
  }

  private void updateTutorialFlow()
  {
    switch (this.phase)
    {
      case AIM:
        break;
      case TARGETS:
        updateTargetBlink();
        break;
      case TIMER_NOTICE:
        break;
      case NORMAL:
        break;
    }
  }

  private void updateTargetBlink()
  {
    this.targetBlinkTicks++;
    this.targetAnimationResetTicks++;
    if (this.targetAnimationResetTicks >= TARGET_ANIMATION_RESET_INTERVAL)
    {
      resetTargetAnimationTicks();
    }
  }

  private void updateHintAlpha()
  {
    if (this.hintAlphaTicks > 0)
    {
      this.hintAlphaTicks--;
      if (this.hintAlphaTicks <= 0)
      {
        this.isHintFullyFaded = true;
      }
    }
  }

  public void startTargetLifetime()
  {
    this.targetBlinkTicks = 0;
  }

  public void resetTargetAnimationTicks()
  {
    this.targetAnimationResetTicks = 0;
  }

  public void advancePhase()
  {
    switch (this.phase)
    {
      case AIM:
        this.phase = TutorialPhase.TARGETS;
        break;
      case TARGETS:
        this.phase = TutorialPhase.TIMER_NOTICE;
        break;
      case TIMER_NOTICE:
        this.phase = TutorialPhase.NORMAL;
        break;
      case NORMAL:
        break;
    }
  }

  public void setHintText(String text)
  {
    this.hintAlphaTicks = MagicConstants.MAX_ALPHA;
    this.isHintFullyFaded = false;
  }

  public int hintAlpha()
  {
    return this.hintAlphaTicks;
  }

  public boolean isHintFullyFaded()
  {
    return this.isHintFullyFaded;
  }

  public TutorialPhase getPhase()
  {
    return this.phase;
  }

  public void setPhase(TutorialPhase phase)
  {
    this.phase = phase;
  }
}
