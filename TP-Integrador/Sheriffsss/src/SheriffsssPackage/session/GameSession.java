package SheriffsssPackage.session;

import SheriffsssPackage.context.GameConfig;
import SheriffsssPackage.level.GameLevel;
import SheriffsssPackage.level.NoLevel;
import SheriffsssPackage.level.TrainingMode;



/** Holds mutable state for the current playable session. */
public final class GameSession
{
  private GameMap map;
  private Player player;
  private PlayerRuntimeState playerRuntime = new PlayerRuntimeState();
  private TrainingMode trainingMode;
  private GameLevel activeLevel = new NoLevel();
  private double cameraZoom = GameConfig.CAMERA_MIN_ZOOM;

  public GameMap map()
  {
    return this.map;
  }

  public void setMap(GameMap map)
  {
    this.map = map;
  }

  public Player player()
  {
    return this.player;
  }

  public void setPlayer(Player player)
  {
    this.player = player;
  }

  public PlayerRuntimeState playerRuntime()
  {
    return this.playerRuntime;
  }

  public void resetPlayerRuntime()
  {
    this.playerRuntime = new PlayerRuntimeState();
  }

  public TrainingMode trainingMode()
  {
    return this.trainingMode;
  }

  public void setTrainingMode(TrainingMode trainingMode)
  {
    this.trainingMode = trainingMode;
  }

  public GameLevel activeLevel()
  {
    return this.activeLevel;
  }

  public void setActiveLevel(GameLevel activeLevel)
  {
    this.activeLevel = activeLevel == null ? new NoLevel() : activeLevel;
  }

  public double cameraZoom()
  {
    return this.cameraZoom;
  }

  public void setCameraZoom(double cameraZoom)
  {
    this.cameraZoom = cameraZoom;
  }
}
