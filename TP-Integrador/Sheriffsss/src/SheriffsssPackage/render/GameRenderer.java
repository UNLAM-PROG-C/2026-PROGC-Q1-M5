package SheriffsssPackage.render;

import SheriffsssPackage.context.AssetManager;
import SheriffsssPackage.context.GameConfig;
import SheriffsssPackage.context.State;
import SheriffsssPackage.session.GameMap;
import SheriffsssPackage.session.MapObject;
import SheriffsssPackage.session.Player;
import SheriffsssPackage.session.WorldLighting;

import java.awt.geom.AffineTransform;
import java.awt.Graphics2D;

public class GameRenderer
{
  private final AssetManager assets;
  private final MenuRenderer menuRenderer;
  private final TrainingHudRenderer trainingHudRenderer;
  private final Camera camera;
  private final WorldLighting lighting;

  // 11 specialized renderers
  private final WorldRenderer worldRenderer;
  private final LightingRenderer lightingRenderer;
  private final EnemyRenderer enemyRenderer;
  private final EffectsRenderer effectsRenderer;
  private final PlayerRenderer playerRenderer;
  private final HeldItemRenderer heldItemRenderer;
  private final DebugWorldRenderer debugWorldRenderer;
  private final DebugMenuRenderer debugMenuRenderer;
  private final SettingsOverlayRenderer settingsOverlayRenderer;
  private final GameHudRenderer gameHudRenderer;
  private final EquipmentRenderer equipmentRenderer;

  public GameRenderer(AssetManager assets, MenuRenderer menuRenderer)
  {
    this.assets = assets;
    this.menuRenderer = menuRenderer;
    this.trainingHudRenderer = new TrainingHudRenderer();
    this.camera = new Camera();
    this.lighting = new WorldLighting();

    // Initialize all 11 specialized renderers
    this.worldRenderer = new WorldRenderer(assets, camera);
    this.lightingRenderer = new LightingRenderer(lighting, camera);
    this.enemyRenderer = new EnemyRenderer(camera, assets);
    this.effectsRenderer = new EffectsRenderer(camera);
    this.playerRenderer = new PlayerRenderer(assets, camera);
    this.heldItemRenderer = new HeldItemRenderer(assets, camera);
    this.debugWorldRenderer = new DebugWorldRenderer(camera, playerRenderer, heldItemRenderer);
    this.debugMenuRenderer = new DebugMenuRenderer();
    this.settingsOverlayRenderer = new SettingsOverlayRenderer();
    this.gameHudRenderer = new GameHudRenderer();
    this.equipmentRenderer = new EquipmentRenderer(assets);
  }

  public void render(Graphics2D g2, GameView game)
  {
    if (game.getState() == State.MENU || game.getState() == State.MENU_SETTINGS)
    {
      this.menuRenderer.draw(g2, game);
      return;
    }
    renderWorld(g2, game);
    gameHudRenderer.drawInfoMessages(g2, game);
    TrainingHudView trainingHud = game.getTrainingHudView();
    if (trainingHud.active())
    {
      trainingHudRenderer.render(g2, trainingHud.snapshot());
    }
    equipmentRenderer.drawEquipment(g2, game);
    if (game.getState() == State.SETTINGS)
    {
      settingsOverlayRenderer.drawSettingsOverlay(g2, game);
    }
    debugMenuRenderer.drawDebugMenu(g2, game);
  }

  private void renderWorld(Graphics2D g2, GameView game)
  {
    GameMap map = game.getMap();
    Player player = game.getPlayer();
    camera.update(game.getCameraCenterWorldX(), game.getCameraCenterWorldY());
    AffineTransform previousTransform = g2.getTransform();
    double cameraZoom = game.getCameraZoom();
    if (cameraZoom != GameConfig.CAMERA_MIN_ZOOM)
    {
      g2.translate(GameConfig.SCREEN_CENTER_X, GameConfig.SCREEN_CENTER_Y);
      g2.scale(cameraZoom, cameraZoom);
      g2.translate(-GameConfig.SCREEN_CENTER_X, -GameConfig.SCREEN_CENTER_Y);
    }

    // Delegate to specialized renderers
    worldRenderer.renderTiles(g2, map, game);
    worldRenderer.drawGroundObjects(g2, map);
    enemyRenderer.drawEnemies(g2, game);
    worldRenderer.drawProjectiles(g2, game);
    effectsRenderer.drawFlameBurstEffects(g2, game);

    // Handle held item layering
    boolean heldBehindPlayer = !player.isDead() && heldItemRenderer.isHeldItemRendered(game, player)
      && heldItemRenderer.heldItemDrawConfig(player).isDrawnBehind(player.getFacing());
    if (heldBehindPlayer)
    {
      heldItemRenderer.drawHeldItems(g2, game, player);
      playerRenderer.drawPlayers(g2, game, player);
    }
    else
    {
      playerRenderer.drawPlayers(g2, game, player);
      heldItemRenderer.drawHeldItems(g2, game, player);
    }

    if (!game.isSpectating() && !player.isDead())
    {
      worldRenderer.drawMapObjectOverlay(g2, map, player);
    }

    worldRenderer.drawSunsetTint(g2, game);
    lightingRenderer.drawLightingOverlay(g2, game);
    effectsRenderer.drawCombatFloatingTexts(g2, game);
    drawToolTargetBar(g2, game);
    debugWorldRenderer.drawDebugWorld(g2, game);
    g2.setTransform(previousTransform);
    gameHudRenderer.drawDebugAndUi(g2, game);
  }


  private void drawToolTargetBar(Graphics2D g2, GameView game)
  {
    MapObject targetObject = game.getToolTargetObject();
    if (targetObject == null || !targetObject.getType().isBreakable())
    {
      return;
    }
    double durability = targetObject.getType().getDurability();
    if (durability <= 0.0)
    {
      return;
    }
    double remainingRatio = 1.0 - Math.min(1.0, targetObject.getDurabilityDamage() / durability);
    int barWidth = 52;
    int barHeight = 8;
    int objectWidth = targetObject.getType().getFootprintWidth() * GameConfig.TILE_SIZE;
    int screenX = camera.tileToScreenX(targetObject.getRootTileX()) + objectWidth / 2 - barWidth / 2;
    int screenY = camera.tileToScreenY(targetObject.getRootTileY()) - 12;
    g2.setColor(java.awt.Color.BLACK);
    g2.fillRect(screenX - 2, screenY - 2, barWidth + 4, barHeight + 4);
    g2.setColor(java.awt.Color.RED);
    g2.fillRect(screenX, screenY, barWidth, barHeight);
    g2.setColor(java.awt.Color.GREEN);
    g2.fillRect(screenX, screenY, (int) (barWidth * remainingRatio), barHeight);
    g2.setColor(java.awt.Color.BLACK);
    g2.setStroke(GameTheme.LIGHT_STROKE);
    g2.drawRect(screenX, screenY, barWidth, barHeight);
  }
}
