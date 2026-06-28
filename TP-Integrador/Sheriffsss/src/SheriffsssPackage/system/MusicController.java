package SheriffsssPackage.system;

import SheriffsssPackage.AudioManager;
import SheriffsssPackage.Player;
import SheriffsssPackage.State;

public final class MusicController {
  private static final String MENU_MUSIC = "sounds/Menu.wav";
  private static final String NIGHT_MUSIC = "sounds/Night.wav";
  private static final float MUSIC_GAIN_DB = -5f;

  private String activeMusicPath;

  public void update(AudioManager audio, State state, Player player, boolean deathOverlayActive) {
    String desiredMusicPath = desiredMusicPath(state, player, deathOverlayActive);
    if (desiredMusicPath == null) {
      stop(audio);
      return;
    }
    playIfChanged(audio, desiredMusicPath);
  }

  public void stop(AudioManager audio) {
    if (this.activeMusicPath == null) {
      return;
    }
    audio.stopLoop();
    this.activeMusicPath = null;
  }

  public void clear() {
    this.activeMusicPath = null;
  }

  private void playIfChanged(AudioManager audio, String desiredMusicPath) {
    if (desiredMusicPath.equals(this.activeMusicPath)) {
      return;
    }
    audio.playLoop(desiredMusicPath, MUSIC_GAIN_DB);
    this.activeMusicPath = desiredMusicPath;
  }

  private String desiredMusicPath(State state, Player player, boolean deathOverlayActive) {
    if (deathOverlayActive) {
      return null;
    }
    if (state == State.MENU || state == State.MENU_SETTINGS) {
      return MENU_MUSIC;
    }
    if ((state == State.PLAYING || state == State.SETTINGS) && player != null) {
      return NIGHT_MUSIC;
    }
    return null;
  }
}
