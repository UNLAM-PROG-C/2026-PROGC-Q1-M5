package SheriffsssPackage.ui;

import SheriffsssPackage.CursorType;
import SheriffsssPackage.State;

public final class CursorController {
  public CursorType resolve(
      State state,
      boolean hitMarkerActive,
      boolean rootMenuHovered,
      boolean menuSettingsHovered,
      boolean gameSettingsHovered) {
    if (hitMarkerActive && state == State.PLAYING) {
      return CursorType.HIT_MARKER;
    }
    if (state == State.MENU && rootMenuHovered) {
      return CursorType.SELECT;
    }
    if (state == State.MENU_SETTINGS && menuSettingsHovered) {
      return CursorType.SELECT;
    }
    if (state == State.SETTINGS && gameSettingsHovered) {
      return CursorType.SELECT;
    }
    return CursorType.IDLE;
  }
}
