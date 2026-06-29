package SheriffsssPackage.context;

import SheriffsssPackage.session.GameMap;
import SheriffsssPackage.system.weapon.Projectile;

import java.util.HashSet;
import java.util.List;

public class SpatialAudioSystem
{
  private static final float WEAPON_GAIN_DB = 0f;
  private final AudioManager audio;
  private final HashSet<String> unavailableSfxPaths = new HashSet<>();

  public SpatialAudioSystem(AudioManager audio)
  {
    this.audio = audio;
  }

  public void playPersistentSpatialSfxOrFallback(String resourcePath, float gainDb, int sourceWorldX, int sourceWorldY, int playerWorldX, int playerWorldY)
  {
    if (this.unavailableSfxPaths.contains(resourcePath))
    {
      playFallbackPersistentSpatialSfx(gainDb, sourceWorldX, sourceWorldY, playerWorldX, playerWorldY);
    } else {
      playPersistentSpatialSfx(resourcePath, gainDb, sourceWorldX, sourceWorldY, playerWorldX, playerWorldY);
    }
  }

  public void playFallbackPersistentSpatialSfx(float gainDb, int sourceWorldX, int sourceWorldY, int playerWorldX, int playerWorldY)
  {
    playPersistentSpatialSfx("sounds/Shot.wav", gainDb, sourceWorldX, sourceWorldY, playerWorldX, playerWorldY);
  }

  public void playPersistentSpatialSfx(String resourcePath, float gainDb, int sourceWorldX, int sourceWorldY, int playerWorldX, int playerWorldY)
  {
    if (this.audio == null) {
      return;
    }
    try
    {
      double distance = Math.sqrt(Math.pow(sourceWorldX - playerWorldX, 2.0) + Math.pow(sourceWorldY - playerWorldY, 2.0));
      float finalGainDb = (float) (gainDb + spatialSfxVolumeScale(distance, sourceWorldX, sourceWorldY, playerWorldX, playerWorldY));
      this.audio.playPersistentSoundEffect(resourcePath, finalGainDb);
    } catch (Exception ex) {
      this.unavailableSfxPaths.add(resourcePath);
    }
  }

  public double spatialSfxVolumeScale(double distance, int sourceWorldX, int sourceWorldY, int playerWorldX, int playerWorldY)
  {
    return Math.max(-96.0, -Math.log(Math.max(1.0, distance / 256.0)) * 8.685);
  }

  public void markSfxUnavailable(String path)
  {
    this.unavailableSfxPaths.add(path);
  }

  public boolean isSfxAvailable(String path)
  {
    return !this.unavailableSfxPaths.contains(path);
  }
}
