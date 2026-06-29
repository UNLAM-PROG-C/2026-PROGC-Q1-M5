package SheriffsssPackage.system;

import SheriffsssPackage.context.GameConfig;



/** Tracks short-lived visual feedback created by projectile weapon shots. */
public final class ShotFeedback {
  private static final int MUZZLE_FLASH_TICKS = 7;
  private static final int MUZZLE_FLASH_RADIUS_TILES = 5;
  private static final int HIT_MARKER_TICKS = 8;
  private static final int FACING_LOCK_TICKS = GameConfig.TARGET_FPS;

  private int muzzleFlashWorldX;
  private int muzzleFlashWorldY;
  private int muzzleFlashTicks;
  private int hitMarkerTicks;
  private int facingLockTicks;

  public void update() {
    tickMuzzleFlash();
    tickHitMarker();
    tickFacingLock();
  }

  public void triggerMuzzleFlash(int worldX, int worldY) {
    this.muzzleFlashWorldX = worldX;
    this.muzzleFlashWorldY = worldY;
    this.muzzleFlashTicks = MUZZLE_FLASH_TICKS;
  }

  public void triggerHitMarker() {
    this.hitMarkerTicks = HIT_MARKER_TICKS;
  }

  public void lockFacing() {
    this.facingLockTicks = FACING_LOCK_TICKS;
  }

  public boolean canUpdateFacing() {
    return this.facingLockTicks <= 0;
  }

  public boolean hasHitMarker() {
    return this.hitMarkerTicks > 0;
  }

  public int muzzleFlashTicks() {
    return this.muzzleFlashTicks;
  }

  public int muzzleFlashWorldX() {
    return this.muzzleFlashWorldX;
  }

  public int muzzleFlashWorldY() {
    return this.muzzleFlashWorldY;
  }

  public int muzzleFlashRadiusTiles() {
    return MUZZLE_FLASH_RADIUS_TILES;
  }

  public double muzzleFlashIntensity() {
    return this.muzzleFlashTicks / (double) MUZZLE_FLASH_TICKS;
  }

  private void tickMuzzleFlash() {
    if (this.muzzleFlashTicks > 0) {
      this.muzzleFlashTicks--;
    }
  }

  private void tickHitMarker() {
    if (this.hitMarkerTicks > 0) {
      this.hitMarkerTicks--;
    }
  }

  private void tickFacingLock() {
    if (this.facingLockTicks > 0) {
      this.facingLockTicks--;
    }
  }
}
