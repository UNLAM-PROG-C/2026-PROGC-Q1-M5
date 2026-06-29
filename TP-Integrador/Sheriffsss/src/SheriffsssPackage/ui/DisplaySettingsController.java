package SheriffsssPackage.ui;

import SheriffsssPackage.context.AudioManager;
import SheriffsssPackage.context.GameConfig;


import java.awt.Dimension;


public final class DisplaySettingsController {
  private enum ActiveSlider {
    NONE,
    MUSIC,
    SFX,
    RESOLUTION
  }

  private Dimension windowedSize = new Dimension(GameConfig.BASE_SCREEN_WIDTH, GameConfig.BASE_SCREEN_HEIGHT);
  private boolean fullscreen;
  private int pendingWindowResolutionIndex;
  private boolean pendingFullscreen;
  private String message = "";
  private ActiveSlider activeSlider = ActiveSlider.NONE;

  public void loadPreferences(AudioManager audio) {
    this.windowedSize = GameConfig.getWindowResolution();
    this.fullscreen = GameConfig.isFullscreenPreferred();
    this.pendingWindowResolutionIndex = GameConfig.getWindowResolutionIndex();
    this.pendingFullscreen = this.fullscreen;
    audio.setMusicVolume(GameConfig.getMusicVolume());
    audio.setSfxVolume(GameConfig.getSfxVolume());
  }

  public void beginEdit() {
    this.pendingFullscreen = this.fullscreen;
    this.pendingWindowResolutionIndex = GameConfig.getWindowResolutionIndex();
  }

  public DisplaySettingsChange applyPending() {
    int currentResolutionIndex = GameConfig.getWindowResolutionIndex();
    boolean resolutionChanged = this.pendingWindowResolutionIndex != currentResolutionIndex;
    boolean fullscreenChanged = this.pendingFullscreen != this.fullscreen;
    GameConfig.setWindowResolutionIndex(this.pendingWindowResolutionIndex);
    GameConfig.setFullscreenPreferred(this.pendingFullscreen);
    GameConfig.saveDisplayPreferences();
    this.windowedSize = GameConfig.getWindowResolution();
    this.fullscreen = this.pendingFullscreen;
    return new DisplaySettingsChange(
      fullscreenChanged,
      resolutionChanged,
      this.fullscreen,
      this.windowedSize);
  }

  public void applyImmediateFullscreen(boolean fullscreen) {
    this.fullscreen = fullscreen;
    this.pendingFullscreen = fullscreen;
    this.pendingWindowResolutionIndex = GameConfig.getWindowResolutionIndex();
    GameConfig.setFullscreenPreferred(fullscreen);
    GameConfig.saveDisplayPreferences();
  }

  public void clearMessage() {
    this.message = "";
  }

  public void clearActiveSlider() {
    this.activeSlider = ActiveSlider.NONE;
  }

  public void togglePendingFullscreen() {
    this.pendingFullscreen = !this.pendingFullscreen;
  }

  public void selectHoveredSlider(
      boolean musicHovered,
      boolean sfxHovered,
      boolean resolutionHovered) {
    if (musicHovered) {
      this.activeSlider = ActiveSlider.MUSIC;
    } else if (sfxHovered) {
      this.activeSlider = ActiveSlider.SFX;
    } else if (resolutionHovered) {
      this.activeSlider = ActiveSlider.RESOLUTION;
    } else {
      this.activeSlider = ActiveSlider.NONE;
    }
  }

  public boolean updateActiveSlider(AudioManager audio, double sliderValue) {
    if (this.activeSlider == ActiveSlider.NONE) {
      return false;
    }
    if (this.activeSlider == ActiveSlider.MUSIC) {
      setMusicVolume(audio, sliderValue);
    } else if (this.activeSlider == ActiveSlider.SFX) {
      setSfxVolume(audio, sliderValue);
    } else if (this.activeSlider == ActiveSlider.RESOLUTION) {
      setWindowResolutionFromSlider(sliderValue);
    }
    return true;
  }

  public Dimension windowedSize() {
    return this.windowedSize;
  }

  public boolean fullscreen() {
    return this.fullscreen;
  }

  public boolean pendingFullscreen() {
    return this.pendingFullscreen;
  }

  public String message() {
    return this.message;
  }

  public double windowResolutionSliderValue() {
    int maxIndex = Math.max(1, GameConfig.getWindowResolutionCount() - 1);
    return this.pendingWindowResolutionIndex / (double) maxIndex;
  }

  public String windowResolutionLabel() {
    return GameConfig.getWindowResolutionLabel(this.pendingWindowResolutionIndex);
  }

  private void setMusicVolume(AudioManager audio, double volume) {
    audio.setMusicVolume(volume);
    GameConfig.setMusicVolume(audio.getMusicVolume());
  }

  private void setSfxVolume(AudioManager audio, double volume) {
    audio.setSfxVolume(volume);
    GameConfig.setSfxVolume(audio.getSfxVolume());
  }

  private void setWindowResolutionFromSlider(double sliderValue) {
    int maxIndex = GameConfig.getWindowResolutionCount() - 1;
    int index = (int) Math.round(sliderValue * maxIndex);
    this.pendingWindowResolutionIndex = Math.max(0, Math.min(maxIndex, index));
  }
}
