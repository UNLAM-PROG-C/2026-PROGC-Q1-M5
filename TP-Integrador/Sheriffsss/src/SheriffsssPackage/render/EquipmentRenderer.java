package SheriffsssPackage.render;

import SheriffsssPackage.MagicConstants;
import SheriffsssPackage.context.AssetManager;
import SheriffsssPackage.system.weapon.ItemDefinition;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.List;

public class EquipmentRenderer
{
  private static final Color EQUIPMENT_BORDER_COLOR = new Color(255, 230, 120, 220);
  private static final Color EQUIPMENT_PANEL_COLOR = new Color(0, 0, 0, 190);
  private static final Color EQUIPMENT_SELECTED_ROW_COLOR = new Color(70, 130, 90, 180);
  private static final Color EQUIPMENT_EVEN_ROW_COLOR = new Color(255, 255, 255, 24);
  private static final Color EQUIPMENT_ODD_ROW_COLOR = new Color(255, 255, 255, 12);
  private static final int SHERIFF_ICON_SIZE = 128;
  private static final int ICON_Y_OFFSET = 42;
  private static final int SELECTOR_SPRITE_X_INSET = 8;
  private static final int SELECTOR_SPRITE_Y_INSET = 8;
  private static final int SELECTOR_SPRITE_WIDTH_SHRINK = 16;
  private static final int SELECTOR_SPRITE_HEIGHT = 38;
  private static final int SELECTOR_DAMAGE_Y_FROM_BOTTOM = 12;
  private static final int LIST_SPRITE_X_INSET = 12;
  private static final int LIST_SPRITE_Y_INSET = 8;
  private static final int LIST_SPRITE_WIDTH = 90;
  private static final int LIST_SPRITE_HEIGHT = 32;
  private static final int LIST_TEXT_X_OFFSET = 120;
  private static final int LIST_NAME_Y_OFFSET = 21;
  private static final int LIST_DAMAGE_Y_OFFSET = 41;
  private static final int LIST_ROW_INSET = 1;
  private static final int LIST_ROW_INSET_X2 = 2;
  private static final double DAMAGE_FORMAT_EPSILON = 0.001;

  private final AssetManager assets;
  private final Font debugFont = new Font("Arial", Font.BOLD, 13);

  public EquipmentRenderer(AssetManager assets)
  {
    this.assets = assets;
  }

  public void drawEquipment(Graphics2D g2, GameView game)
  {
    EquipmentHudView equipmentHud = game.getEquipmentHudView();
    if (!equipmentHud.visible())
    {
      return;
    }
    drawEquipmentPanel(g2, equipmentHud);
    if (equipmentHud.selectorOpen())
    {
      drawEquipmentWeaponList(g2, equipmentHud);
    }
  }

  private void drawEquipmentPanel(Graphics2D g2, EquipmentHudView equipmentHud)
  {
    int x = equipmentHud.panelX();
    int y = equipmentHud.panelY();
    int width = equipmentHud.panelWidth();
    int height = equipmentHud.panelHeight();
    g2.setColor(GameTheme.TRANSPARENT_BLACK);
    g2.fillRect(x, y, width, height);
    g2.setColor(EQUIPMENT_BORDER_COLOR);
    g2.setStroke(GameTheme.LIGHT_STROKE);
    g2.drawRect(x, y, width, height);
    BufferedImage sheriffIcon = this.assets.getImage("sprites/sheriffsss_icono.png");
    g2.drawImage(sheriffIcon, x + (width - SHERIFF_ICON_SIZE) / MagicConstants.DIVISOR_HALF, y + ICON_Y_OFFSET, SHERIFF_ICON_SIZE, SHERIFF_ICON_SIZE, null);
    drawEquipmentSelector(g2, equipmentHud);
  }

  private void drawEquipmentSelector(Graphics2D g2, EquipmentHudView equipmentHud)
  {
    ItemDefinition weapon = equipmentHud.equippedWeapon();
    int x = equipmentHud.selectorX();
    int y = equipmentHud.selectorY();
    int width = equipmentHud.selectorWidth();
    int height = equipmentHud.selectorHeight();
    g2.setColor(EQUIPMENT_PANEL_COLOR);
    g2.fillRect(x, y, width, height);
    g2.setColor(EQUIPMENT_BORDER_COLOR);
    g2.drawRect(x, y, width, height);
    if (weapon != null)
    {
      drawEquipmentWeaponSprite(g2, weapon, x + SELECTOR_SPRITE_X_INSET, y + SELECTOR_SPRITE_Y_INSET, width - SELECTOR_SPRITE_WIDTH_SHRINK, SELECTOR_SPRITE_HEIGHT);
      String damageText = "Daño: " + formatDamage(weapon.getProjectileDamage());
      TextRenderer.draw(g2, this.debugFont, damageText, Color.LIGHT_GRAY,
        TextRenderer.centeredX(g2, this.debugFont, damageText, x + width / MagicConstants.DIVISOR_HALF), y + height - SELECTOR_DAMAGE_Y_FROM_BOTTOM, false);
    }
  }

  private void drawEquipmentWeaponList(Graphics2D g2, EquipmentHudView equipmentHud)
  {
    List<ItemDefinition> weapons = equipmentHud.weapons();
    int x = equipmentHud.listX();
    int y = equipmentHud.listY();
    int width = equipmentHud.listWidth();
    int rowHeight = equipmentHud.listRowHeight();
    g2.setColor(EQUIPMENT_PANEL_COLOR);
    g2.fillRect(x, y, width, rowHeight * weapons.size());
    g2.setColor(EQUIPMENT_BORDER_COLOR);
    g2.drawRect(x, y, width, rowHeight * weapons.size());
    for (int i = 0; i < weapons.size(); i++)
    {
      ItemDefinition weapon = weapons.get(i);
      int rowY = y + i * rowHeight;
      boolean selected = weapon == equipmentHud.equippedWeapon();
      g2.setColor(selected ? EQUIPMENT_SELECTED_ROW_COLOR : equipmentRowColor(i));
      g2.fillRect(x + LIST_ROW_INSET, rowY + LIST_ROW_INSET, width - LIST_ROW_INSET_X2, rowHeight - LIST_ROW_INSET_X2);
      drawEquipmentWeaponSprite(g2, weapon, x + LIST_SPRITE_X_INSET, rowY + LIST_SPRITE_Y_INSET, LIST_SPRITE_WIDTH, LIST_SPRITE_HEIGHT);
      TextRenderer.draw(g2, this.debugFont, weapon.getDisplayName(), Color.WHITE, x + LIST_TEXT_X_OFFSET, rowY + LIST_NAME_Y_OFFSET, false);
      TextRenderer.draw(g2, this.debugFont, "Daño: " + formatDamage(weapon.getProjectileDamage()), Color.LIGHT_GRAY, x + LIST_TEXT_X_OFFSET, rowY + LIST_DAMAGE_Y_OFFSET, false);
    }
  }

  private Color equipmentRowColor(int index)
  {
    return index % 2 == 0 ? EQUIPMENT_EVEN_ROW_COLOR : EQUIPMENT_ODD_ROW_COLOR;
  }

  private void drawEquipmentWeaponSprite(Graphics2D g2, ItemDefinition weapon, int x, int y, int maxWidth, int maxHeight)
  {
    BufferedImage sprite = this.assets.getImage(weapon.getSpritePath());
    int drawWidth = Math.max(1, weapon.getHeldDrawWidth());
    int drawHeight = Math.max(1, weapon.getHeldDrawHeight());
    double scale = Math.min(maxWidth / (double) drawWidth, maxHeight / (double) drawHeight);
    int fittedWidth = Math.max(1, (int) Math.round(drawWidth * scale));
    int fittedHeight = Math.max(1, (int) Math.round(drawHeight * scale));
    int drawX = x + (maxWidth - fittedWidth) / MagicConstants.DIVISOR_HALF;
    int drawY = y + (maxHeight - fittedHeight) / MagicConstants.DIVISOR_HALF;
    g2.drawImage(sprite, drawX, drawY, fittedWidth, fittedHeight, null);
  }

  private String formatDamage(double damage)
  {
    return Math.abs(damage - Math.round(damage)) < DAMAGE_FORMAT_EPSILON ? Integer.toString((int) Math.round(damage)) : String.format(java.util.Locale.US, "%.1f", damage);
  }
}
