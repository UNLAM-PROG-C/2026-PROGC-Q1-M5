package SheriffsssPackage.level;

public class TrainingTutorialController
{
  private TutorialPhase phase = TutorialPhase.AIM;

  public TutorialPhase getPhase()
  {
    return this.phase;
  }

  public void setPhase(TutorialPhase phase)
  {
    this.phase = phase;
  }
}
