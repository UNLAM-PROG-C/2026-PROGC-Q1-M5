package SheriffsssPackage.level;

import SheriffsssPackage.Game;
import SheriffsssPackage.context.GameConfig;
import SheriffsssPackage.context.GameInput;

/**
 * Maneja la lógica de entrada y renderizado para las pantallas finales de entrenamiento
 * (cuando el jugador muere o cuando termina la sesión).
 *
 * Responsabilidades:
 *   - Consumir input del jugador mientras está en pantalla de fin
 *   - Detectar clics en botones de reinicio/salida
 *   - Calcular posiciones y tamaños de los elementos de UI
 */
public final class TrainingEndScreenHandler {
  // --- End screen hit testing ---
  private static final int DEATH_BOX_WIDTH = 360;
  private static final int DEATH_BOX_HEIGHT = 170;
  private static final int DEATH_BOX_Y_OFFSET_FROM_CENTER = 60;
  private static final int FINAL_BOX_WIDTH = 460;
  private static final int FINAL_BOX_HEIGHT = 290;
  private static final int END_BUTTON_WIDTH = 220;
  private static final int END_BUTTON_HEIGHT = 38;
  private static final int DEATH_RESTART_BUTTON_Y = 56;
  private static final int DEATH_EXIT_BUTTON_Y = 108;
  private static final int FINAL_RESTART_BUTTON_Y = 188;
  private static final int FINAL_EXIT_BUTTON_Y = 236;

  private final GameInput input;
  private final TrainingControls controls;
  private final Game game;
  private final TutorialThread tutorialThread;
  private final Runnable resetArenaCallback;

  public TrainingEndScreenHandler(
      GameInput input,
      TrainingControls controls,
      Game game,
      TutorialThread tutorialThread,
      Runnable resetArenaCallback)
  {
    this.input = input;
    this.controls = controls;
    this.game = game;
    this.tutorialThread = tutorialThread;
    this.resetArenaCallback = resetArenaCallback;
  }

  public void handleDeadInput()
  {
    drainPanelInputWhileDead();
    drainEndActionKeys();
    handleEndScreenClick(deathBoxX(), deathBoxY(), DEATH_BOX_WIDTH, DEATH_RESTART_BUTTON_Y, DEATH_EXIT_BUTTON_Y);
  }

  public void handleFinalInput()
  {
    drainPanelInputWhileDead();
    drainEndActionKeys();
    handleEndScreenClick(finalBoxX(), finalBoxY(), FINAL_BOX_WIDTH, FINAL_RESTART_BUTTON_Y, FINAL_EXIT_BUTTON_Y);
  }

  public void readPanelInput()
  {
    if (this.input.consumeTrainingIncrement()) {
      this.controls.incCount();
    }
    if (this.input.consumeTrainingDecrement())
    {
      this.controls.decCount();
    }
    if (this.input.consumeTrainingReset())
    {
      this.resetArenaCallback.run();
    }
    if (this.input.consumeTrainingSkipTutorial())
    {
      this.tutorialThread.skip();
    }
  }

  private void drainEndActionKeys()
  {
    this.input.consumeTrainingReset();
    this.input.consumeTrainingBackToMenu();
  }

  private void handleEndScreenClick(
      int boxX,
      int boxY,
      int boxWidth,
      int restartButtonY,
      int exitButtonY)
  {
    if (!this.input.consumePrimaryClick() || this.game == null) {
      return;
    }
    int mouseX = this.input.getMouseX();
    int mouseY = this.input.getMouseY();
    int buttonX = endButtonX(boxX, boxWidth);
    if (isInsideButton(mouseX, mouseY, buttonX, boxY + restartButtonY))
    {
      this.game.restartTraining();
      return;
    }
    if (isInsideButton(mouseX, mouseY, buttonX, boxY + exitButtonY))
    {
      this.game.exitTrainingToMenu();
    }
  }

  private static boolean isInsideButton(int mouseX, int mouseY, int buttonX, int buttonY)
  {
    return mouseX >= buttonX && mouseX <= buttonX + END_BUTTON_WIDTH
      && mouseY >= buttonY && mouseY <= buttonY + END_BUTTON_HEIGHT;
  }

  private void drainPanelInputWhileDead()
  {
    this.input.consumeTrainingIncrement();
    this.input.consumeTrainingDecrement();
    this.input.consumeTrainingSkipTutorial();
  }

  private static int deathBoxX()
  {
    return (GameConfig.SCREEN_WIDTH - DEATH_BOX_WIDTH) / 2;
  }

  private static int deathBoxY()
  {
    return GameConfig.SCREEN_HEIGHT / 2 + DEATH_BOX_Y_OFFSET_FROM_CENTER;
  }

  private static int finalBoxX()
  {
    return (GameConfig.SCREEN_WIDTH - FINAL_BOX_WIDTH) / 2;
  }

  private static int finalBoxY()
  {
    return (GameConfig.SCREEN_HEIGHT - FINAL_BOX_HEIGHT) / 2;
  }

  private static int endButtonX(int boxX, int boxWidth)
  {
    return boxX + (boxWidth - END_BUTTON_WIDTH) / 2;
  }
}
