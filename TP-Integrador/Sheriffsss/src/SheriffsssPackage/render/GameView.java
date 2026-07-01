package SheriffsssPackage.render;

import SheriffsssPackage.context.AssetManager;
import SheriffsssPackage.context.AudioManager;
import SheriffsssPackage.context.DayNightCycle;
import SheriffsssPackage.context.DebugOptions;
import SheriffsssPackage.context.GameInput;
import SheriffsssPackage.context.State;
import SheriffsssPackage.session.Facing;
import SheriffsssPackage.session.GameMap;
import SheriffsssPackage.session.MapObject;
import SheriffsssPackage.session.Player;
import SheriffsssPackage.system.enemy.Enemy;
import SheriffsssPackage.system.weapon.ItemDefinition;
import SheriffsssPackage.system.weapon.Projectile;


import java.util.List;


public interface GameView
{
  AssetManager getAssets();
  AudioManager getAudio();
  GameInput getInput();
  DebugOptions getDebugOptions();
  State getState();
  GameMap getMap();
  Player getPlayer();
  int getCameraCenterWorldX();
  int getCameraCenterWorldY();
  double getCameraZoom();
  boolean isSpectating();
  List<Enemy> getEnemies();
  List<Projectile> getProjectiles();
  List<FlameBurstEffect> getFlameBurstEffects();
  List<CombatFloatingText> getCombatFloatingTexts();
  boolean isUsingTool();
  int getToolUseTicks();
  int getToolUseDurationTicks();
  MapObject getToolTargetObject();
  int getRevolverFlashTicks();
  int getRevolverFlashWorldX();
  int getRevolverFlashWorldY();
  int getRevolverFlashRadiusTiles();
  double getRevolverFlashIntensity();
  double getEquippedPlayerAccuracy();
  int heldItemOriginWorldX(Player sourcePlayer, ItemDefinition definition, Facing facing);
  int heldItemOriginWorldY(Player sourcePlayer, ItemDefinition definition, Facing facing);
  EquipmentHudView getEquipmentHudView();
  int getInfoMessageSlotCount();
  String getInfoMessage(int index);
  int getInfoMessageTicks(int index);
  String getSettingsMessage();
  boolean isPendingFullscreen();
  boolean isDebugMenuOpen();
  boolean isSettingsDebugButtonHovered();
  int getSettingsDebugButtonX();
  int getSettingsDebugButtonY();
  int getSettingsDebugButtonWidth();
  int getSettingsDebugButtonHeight();
  int getSettingsOverlayOffsetY();
  double getWindowResolutionSliderValue();
  String getWindowResolutionLabel();
  long getFrameCount();
  DayNightCycle getDayNightCycle();
  TrainingHudView getTrainingHudView();
}
