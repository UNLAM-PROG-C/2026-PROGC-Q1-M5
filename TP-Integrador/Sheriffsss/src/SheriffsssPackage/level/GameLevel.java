package SheriffsssPackage.level;

import SheriffsssPackage.context.GameContext;
import SheriffsssPackage.session.GameSession;

public interface GameLevel {
  LevelType type();
  void enter(GameContext context, GameSession session, LevelStartOptions options);
  void update(GameContext context, GameSession session);
  void exit(GameContext context, GameSession session);
}
