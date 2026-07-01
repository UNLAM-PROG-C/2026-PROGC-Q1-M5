package SheriffsssPackage.level;

import SheriffsssPackage.context.GameContext;
import SheriffsssPackage.session.GameSession;



public final class NoLevel implements GameLevel
{
  @Override
  public LevelType type()
  {
    return LevelType.NONE;
  }

  @Override
  public void enter(GameContext context, GameSession session, LevelStartOptions options)
  {
  }

  @Override
  public void update(GameContext context, GameSession session)
  {
  }

  @Override
  public void exit(GameContext context, GameSession session)
  {
  }
}
