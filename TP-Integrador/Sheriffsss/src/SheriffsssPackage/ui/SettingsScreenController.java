package SheriffsssPackage.ui;

import SheriffsssPackage.context.AudioManager;
import SheriffsssPackage.context.DebugOptions;
import SheriffsssPackage.context.GameConfig;
import SheriffsssPackage.context.GameInput;
import SheriffsssPackage.context.State;
import SheriffsssPackage.level.LevelType;
import SheriffsssPackage.render.GameView;
import SheriffsssPackage.render.MenuRenderer;
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
  private boolean requestExitToMenu;
  private boolean requestShutdown;
  private MenuRenderer menuRenderer;

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
    this.requestExitToMenu = false;
    this.requestShutdown = false;
  }

  public void setMenuRenderer(MenuRenderer renderer)
  {
    this.menuRenderer = renderer;
  }

  public void update(GameView game)
  {
    if (this.state == State.MENU_SETTINGS) {
      updateMenuSettings();
    } else if (this.state == State.SETTINGS) {
      updateSettings();
    }
  }

  public boolean shouldExitToMenu()
  {
    return this.requestExitToMenu;
  }

  public boolean shouldShutdown()
  {
    return this.requestShutdown;
  }

  public void clearRequestFlags()
  {
    this.requestExitToMenu = false;
    this.requestShutdown = false;
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
      this.displaySettingsController.applyPending();
      this.requestExitToMenu = true;
      return;
    }
    if (isQuitButtonHovered())
    {
      this.requestShutdown = true;
      return;
    }
  }

  private void updateMenuSettings()
  {
    if (this.input.consumeEscapePressed()) {
      closeMenuSettings();
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
    if (primaryPressed && this.menuRenderer != null
        && this.menuRenderer.isMenuFullscreenButtonHovered(this.input.getMouseX(), this.input.getMouseY())) {
      this.displaySettingsController.togglePendingFullscreen();
      return;
    }
    if (primaryPressed && this.menuRenderer != null
        && this.menuRenderer.isMenuBackButtonHovered(this.input.getMouseX(), this.input.getMouseY()))
    {
      closeMenuSettings();
    }
  }

  private void closeMenuSettings()
  {
    this.displaySettingsController.applyPending();
    this.displaySettingsController.clearMessage();
    this.displaySettingsController.clearActiveSlider();
    this.state = State.MENU;
  }

  private void resumeFromSettings(boolean blockPrimaryGameplay)
  {
    this.displaySettingsController.applyPending();
    this.state = State.PLAYING;
    this.displaySettingsController.clearMessage();
    this.displaySettingsController.clearActiveSlider();
  }

  public boolean isMusicSliderHovered()
  {
    return isPointInside(
      settingsSliderInteractionX(),
      settingsSliderInteractionY(GameConfig.SETTINGS_MUSIC_SLIDER_Y) - 10,
      GameConfig.SETTINGS_SLIDER_WIDTH,
      24);
  }

  public boolean isSfxSliderHovered()
  {
    return isPointInside(
      settingsSliderInteractionX(),
      settingsSliderInteractionY(GameConfig.SETTINGS_SFX_SLIDER_Y) - 10,
      GameConfig.SETTINGS_SLIDER_WIDTH,
      24);
  }

  public boolean isResolutionSliderHovered()
  {
    return isPointInside(
      settingsSliderInteractionX(),
      settingsSliderInteractionY(GameConfig.SETTINGS_RESOLUTION_SLIDER_Y) - 10,
      GameConfig.SETTINGS_SLIDER_WIDTH,
      24);
  }

  public boolean isFullscreenButtonHovered()
  {
    return isSettingsButtonHovered(GameConfig.SETTINGS_FULLSCREEN_BUTTON_Y);
  }

  public boolean isDebugSettingsButtonHovered()
  {
    return isTrainingLevelActive()
      && isSettingsButtonHovered(GameConfig.SETTINGS_DEBUG_BUTTON_Y);
  }

  public int getSettingsDebugButtonX()
  {
    return GameConfig.SETTINGS_BUTTON_X;
  }

  public int getSettingsDebugButtonY()
  {
    return settingsOverlayY(GameConfig.SETTINGS_DEBUG_BUTTON_Y);
  }

  public boolean isResumeButtonHovered()
  {
    return isSettingsButtonHovered(GameConfig.SETTINGS_RESUME_BUTTON_Y);
  }

  public boolean isExitToMenuButtonHovered()
  {
    return isSettingsButtonHovered(GameConfig.SETTINGS_MENU_BUTTON_Y);
  }

  public boolean isQuitButtonHovered()
  {
    return isSettingsButtonHovered(GameConfig.SETTINGS_QUIT_BUTTON_Y);
  }

  public int getSettingsOverlayOffsetY()
  {
    return settingsOverlayOffsetY();
  }

  private boolean isSettingsButtonHovered(int baseY)
  {
    return isPointInside(
      GameConfig.SETTINGS_BUTTON_X,
      settingsOverlayY(baseY),
      GameConfig.SETTINGS_BUTTON_WIDTH,
      GameConfig.SETTINGS_BUTTON_HEIGHT);
  }

  public boolean isPointInside(int x, int y, int width, int height)
  {
    return this.input.getMouseX() >= x && this.input.getMouseX() <= x + width
      && this.input.getMouseY() >= y && this.input.getMouseY() <= y + height;
  }

  public double sliderValueFromMouse()
  {
    double value = (this.input.getMouseX() - settingsSliderInteractionX())
      / (double) GameConfig.SETTINGS_SLIDER_WIDTH;
    return Math.max(0.0, Math.min(1.0, value));
  }

  public int settingsSliderInteractionX()
  {
    if (this.state == State.MENU_SETTINGS) {
      return GameConfig.BASE_SCREEN_WIDTH / 2 - 90;
    }
    return GameConfig.SETTINGS_SLIDER_X;
  }

  public int settingsSliderInteractionY(int baseY)
  {
    if (this.state == State.MENU_SETTINGS) {
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
}
