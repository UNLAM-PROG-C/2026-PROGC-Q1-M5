package SheriffsssPackage.context;

import java.util.HashSet;

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
    }
    else
    {
      playPersistentSpatialSfx(resourcePath, gainDb, sourceWorldX, sourceWorldY, playerWorldX, playerWorldY);
    }
  }

  public void playFallbackPersistentSpatialSfx(float gainDb, int sourceWorldX, int sourceWorldY, int playerWorldX, int playerWorldY)
  {
    playPersistentSpatialSfx("sounds/Shot.wav", gainDb, sourceWorldX, sourceWorldY, playerWorldX, playerWorldY);
  }

  public void playPersistentSpatialSfx(String resourcePath, float gainDb, int sourceWorldX, int sourceWorldY, int playerWorldX, int playerWorldY)
  {
    if (this.audio == null)
    {
      return;
    }
    try
    {
      double volumeScale = spatialSfxVolumeScale(sourceWorldX, sourceWorldY, playerWorldX, playerWorldY);
      if (volumeScale > 0.0)
      {
        this.audio.playOnceUntilFinished(resourcePath, gainDb, volumeScale);
      }
    }
    catch (Exception ex)
    {
      this.unavailableSfxPaths.add(resourcePath);
    }
  }

  public double spatialSfxVolumeScale(int sourceWorldX, int sourceWorldY, int playerWorldX, int playerWorldY)
  {
    int deltaX = sourceWorldX - playerWorldX;
    int deltaY = sourceWorldY - playerWorldY;
    int fullRadius = GameConfig.SPATIAL_SFX_FULL_VOLUME_RADIUS_PIXELS;
    int audibleRadius = GameConfig.SPATIAL_SFX_AUDIBLE_RADIUS_PIXELS;
    int distanceSquared = deltaX * deltaX + deltaY * deltaY;
    if (distanceSquared <= fullRadius * fullRadius)
    {
      return 1.0;
    }
    if (distanceSquared >= audibleRadius * audibleRadius || audibleRadius <= fullRadius)
    {
      return 0.0;
    }
    double distance = Math.sqrt(distanceSquared);
    return Math.max(0.0, Math.min(1.0, 1.0 - (distance - fullRadius) / (audibleRadius - fullRadius)));
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
