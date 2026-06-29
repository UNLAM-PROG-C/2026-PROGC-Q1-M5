package SheriffsssPackage.ui;

import SheriffsssPackage.context.AudioManager;
import SheriffsssPackage.context.DebugOptions;
import SheriffsssPackage.context.GameConfig;
import SheriffsssPackage.context.GameInput;
import SheriffsssPackage.context.State;
import SheriffsssPackage.level.LevelType;
import SheriffsssPackage.render.GameView;
import SheriffsssPackage.session.GameSession;

import javax.swing.SwingUtilities;

public class SettingsScreenController
{
  private final DisplaySettingsController displaySettingsController;
  private final GameInput input;
  private final AudioManager audio;
  private final GameSession session;
  private final DebugOptions debugOptions;
  private State state;

  public SettingsScreenController(
      DisplaySettingsController displaySettingsController,
      GameInput input,
      AudioManager audio,
      GameSession session,
      DebugOptions debugOptions)
  {
    this.displaySettingsController = displaySettingsController;
    this.input = input;
    this.audio = audio;
    this.session = session;
    this.debugOptions = debugOptions;
    this.state = State.MENU;
  }

  public void update(GameView game)
  {
    updateSettings();
  }

  public void resume()
  {
    resumeFromSettings(false);
  }

  public State getState()
  {
    return this.state;
  }

  public void setState(State state)
  {
    this.state = state;
  }

  private void updateSettings()
  {
    if (this.input.consumeEscapePressed()) {
      resumeFromSettings(false);
      return;
    }
    boolean primaryPressed = this.input.consumePrimaryClick();
    if (primaryPressed)
    {
      this.displaySettingsController.selectHoveredSlider(
        isMusicSliderHovered(),
        isSfxSliderHovered(),
        isResolutionSliderHovered());
    }
    if (!this.input.isPrimaryHeld())
    {
      this.displaySettingsController.clearActiveSlider();
    }
    if (this.displaySettingsController.updateActiveSlider(this.audio, sliderValueFromMouse()))
    {
      return;
    }
    if (!primaryPressed) {
      return;
    }
    if (isTrainingLevelActive() && isDebugSettingsButtonHovered()) {
      this.debugOptions.toggleMenu();
      return;
    }
    if (isFullscreenButtonHovered())
    {
      this.displaySettingsController.togglePendingFullscreen();
      return;
    }
    if (isResumeButtonHovered())
    {
      resumeFromSettings(true);
      return;
    }
    if (isExitToMenuButtonHovered())
    {
      applyPendingDisplaySettings();
      return;
    }
    if (isQuitButtonHovered())
    {
      // Shutdown will be handled by Game
      return;
    }
  }

  private void resumeFromSettings(boolean blockPrimaryGameplay)
  {
    applyPendingDisplaySettings();
    this.state = State.PLAYING;
    this.displaySettingsController.clearMessage();
    this.displaySettingsController.clearActiveSlider();
  }

  private void beginDisplaySettingsEdit()
  {
    this.displaySettingsController.beginEdit();
  }

  private void applyPendingDisplaySettings()
  {
    DisplaySettingsChange change = this.displaySettingsController.applyPending();
    if (change.fullscreenChanged())
    {
      final boolean targetFullscreen = change.targetFullscreen();
      // Note: Fullscreen application will be handled by Game
    } else if (!change.targetFullscreen() && change.resolutionChanged())
    {
      // Note: Window resolution will be handled by Game
    }
  }

  private boolean isMusicSliderHovered()
  {
    return isPointInside(
      settingsSliderInteractionX(),
      settingsSliderInteractionY(GameConfig.SETTINGS_MUSIC_SLIDER_Y) - 10,
      GameConfig.SETTINGS_SLIDER_WIDTH,
      24);
  }

  private boolean isSfxSliderHovered()
  {
    return isPointInside(
      settingsSliderInteractionX(),
      settingsSliderInteractionY(GameConfig.SETTINGS_SFX_SLIDER_Y) - 10,
      GameConfig.SETTINGS_SLIDER_WIDTH,
      24);
  }

  private boolean isResolutionSliderHovered()
  {
    return isPointInside(
      settingsSliderInteractionX(),
      settingsSliderInteractionY(GameConfig.SETTINGS_RESOLUTION_SLIDER_Y) - 10,
      GameConfig.SETTINGS_SLIDER_WIDTH,
      24);
  }

  private boolean isFullscreenButtonHovered()
  {
    return isSettingsButtonHovered(GameConfig.SETTINGS_FULLSCREEN_BUTTON_Y);
  }

  private boolean isDebugSettingsButtonHovered()
  {
    return isTrainingLevelActive()
      && isSettingsButtonHovered(GameConfig.SETTINGS_DEBUG_BUTTON_Y);
  }

  private int settingsDebugButtonX()
  {
    return GameConfig.SETTINGS_BUTTON_X;
  }

  private boolean isResumeButtonHovered()
  {
    return isSettingsButtonHovered(GameConfig.SETTINGS_RESUME_BUTTON_Y);
  }

  private boolean isExitToMenuButtonHovered()
  {
    return isSettingsButtonHovered(GameConfig.SETTINGS_MENU_BUTTON_Y);
  }

  private boolean isQuitButtonHovered()
  {
    return isSettingsButtonHovered(GameConfig.SETTINGS_QUIT_BUTTON_Y);
  }

  private boolean isSettingsButtonHovered(int baseY)
  {
    return isPointInside(
      GameConfig.SETTINGS_BUTTON_X,
      settingsOverlayY(baseY),
      GameConfig.SETTINGS_BUTTON_WIDTH,
      GameConfig.SETTINGS_BUTTON_HEIGHT);
  }

  private boolean isPointInside(int x, int y, int width, int height)
  {
    return this.input.getMouseX() >= x && this.input.getMouseX() <= x + width
      && this.input.getMouseY() >= y && this.input.getMouseY() <= y + height;
  }

  private double sliderValueFromMouse()
  {
    double value = (this.input.getMouseX() - settingsSliderInteractionX())
      / (double) GameConfig.SETTINGS_SLIDER_WIDTH;
    return Math.max(0.0, Math.min(1.0, value));
  }

  private int settingsSliderInteractionX()
  {
    if (this.state == State.MENU_SETTINGS) {
      // Menu renderer offset would need to be passed in
      return GameConfig.BASE_SCREEN_WIDTH / 2 - 90;
    }
    return GameConfig.SETTINGS_SLIDER_X;
  }

  private int settingsSliderInteractionY(int baseY)
  {
    if (this.state == State.MENU_SETTINGS) {
      // Menu renderer offset would need to be passed in
      return baseY;
    }
    return settingsOverlayY(baseY);
  }

  private int settingsOverlayY(int baseY)
  {
    return baseY + settingsOverlayOffsetY();
  }

  private int settingsOverlayOffsetY()
  {
    return isTrainingLevelActive() && this.state == State.SETTINGS
      ? GameConfig.TRAINING_SETTINGS_OVERLAY_Y_OFFSET
      : 0;
  }

  private boolean isTrainingLevelActive()
  {
    return this.session.activeLevel() != null
      && this.session.activeLevel().type() == LevelType.TRAINING;
  }

  // Public getters for hover states
  public boolean isMusicSliderHoveredPublic()
  {
    return isMusicSliderHovered();
  }

  public boolean isSfxSliderHoveredPublic()
  {
    return isSfxSliderHovered();
  }

  public boolean isResolutionSliderHoveredPublic()
  {
    return isResolutionSliderHovered();
  }

  public boolean isFullscreenButtonHoveredPublic()
  {
    return isFullscreenButtonHovered();
  }

  public boolean isDebugSettingsButtonHoveredPublic()
  {
    return isDebugSettingsButtonHovered();
  }

  public int getSettingsDebugButtonX()
  {
    return settingsDebugButtonX();
  }

  public boolean isResumeButtonHoveredPublic()
  {
    return isResumeButtonHovered();
  }

  public boolean isExitToMenuButtonHoveredPublic()
  {
    return isExitToMenuButtonHovered();
  }

  public boolean isQuitButtonHoveredPublic()
  {
    return isQuitButtonHovered();
  }

  public int getSettingsOverlayOffsetY()
  {
    return settingsOverlayOffsetY();
  }
}
