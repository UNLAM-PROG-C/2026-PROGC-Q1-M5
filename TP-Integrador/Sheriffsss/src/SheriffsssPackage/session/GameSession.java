package SheriffsssPackage.session;

import SheriffsssPackage.GameConfig;
import SheriffsssPackage.GameMap;
import SheriffsssPackage.Player;
import SheriffsssPackage.PlayerRuntimeState;
import SheriffsssPackage.TrainingMode;
import SheriffsssPackage.level.GameLevel;
import SheriffsssPackage.level.NoLevel;

/** Holds mutable state for the current playable session. */
public final class GameSession {
  private GameMap map;
  private Player player;
  private PlayerRuntimeState playerRuntime = new PlayerRuntimeState();
  private TrainingMode trainingMode;
  private GameLevel activeLevel = new NoLevel();
  private boolean deathOverlayActive;
  private double cameraZoom = GameConfig.CAMERA_MIN_ZOOM;

  public GameMap map() {
    return this.map;
  }

  public void setMap(GameMap map) {
    this.map = map;
  }

  public Player player() {
    return this.player;
  }

  public void setPlayer(Player player) {
    this.player = player;
  }

  public PlayerRuntimeState playerRuntime() {
    return this.playerRuntime;
  }

  public void resetPlayerRuntime() {
    this.playerRuntime = new PlayerRuntimeState();
  }

  public TrainingMode trainingMode() {
    return this.trainingMode;
  }

  public void setTrainingMode(TrainingMode trainingMode) {
    this.trainingMode = trainingMode;
  }

  public GameLevel activeLevel() {
    return this.activeLevel;
  }

  public void setActiveLevel(GameLevel activeLevel) {
    this.activeLevel = activeLevel == null ? new NoLevel() : activeLevel;
  }

  public boolean isDeathOverlayActive() {
    return this.deathOverlayActive;
  }

  public void setDeathOverlayActive(boolean deathOverlayActive) {
    this.deathOverlayActive = deathOverlayActive;
  }

  public double cameraZoom() {
    return this.cameraZoom;
  }

  public void setCameraZoom(double cameraZoom) {
    this.cameraZoom = cameraZoom;
  }
}
