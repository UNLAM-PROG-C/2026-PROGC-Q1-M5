package SheriffsssPackage.level;

import SheriffsssPackage.context.AudioManager;
import SheriffsssPackage.context.GameConfig;
import SheriffsssPackage.session.GameSession;

public class TrainingSessionTimer
{
  private static final int COUNTDOWN_SOUND_SECONDS_REMAINING = 4;
  private static final int COUNTDOWN_SOUND_TICKS_REMAINING = GameConfig.TARGET_FPS * COUNTDOWN_SOUND_SECONDS_REMAINING;
  private static final String COUNTDOWN_SOUND_PATH = "sounds/countdown.wav";
  private static final float COUNTDOWN_SOUND_GAIN_DB = 9.5f;

  private int sessionStartTicks;
  private int sessionDurationTicks;
  private int lastCountdownTicks;
  private boolean sessionFinished;
  private int sessionTicksRemaining;
  private boolean countdownSoundPlayed;
  private final AudioManager audio;

  public TrainingSessionTimer(int durationTicks, AudioManager audio)
  {
    this.sessionDurationTicks = durationTicks;
    this.sessionTicksRemaining = durationTicks;
    this.audio = audio;
    this.sessionStartTicks = 0;
    this.lastCountdownTicks = 0;
    this.sessionFinished = false;
    this.countdownSoundPlayed = false;
  }

  public void update(GameSession session)
  {
    updateSessionTimer();
  }

  public void finish(GameSession session)
  {
    finishSession();
  }

  public boolean isSessionFinished()
  {
    return this.sessionFinished;
  }

  public int getSessionTimer()
  {
    return this.sessionTicksRemaining;
  }

  private void updateSessionTimer()
  {
    if (this.sessionTicksRemaining <= 0)
    {
      finishSession();
      return;
    }
    playCountdownSoundIfNeeded();
    this.sessionTicksRemaining--;
    if (this.sessionTicksRemaining <= 0)
    {
      finishSession();
    }
  }

  private void finishSession()
  {
    this.sessionFinished = true;
  }

  private void playCountdownSoundIfNeeded()
  {
    if (this.countdownSoundPlayed || this.sessionTicksRemaining != COUNTDOWN_SOUND_TICKS_REMAINING)
    {
      return;
    }
    this.countdownSoundPlayed = true;
    this.audio.playOnce(COUNTDOWN_SOUND_PATH, COUNTDOWN_SOUND_GAIN_DB);
  }
}
