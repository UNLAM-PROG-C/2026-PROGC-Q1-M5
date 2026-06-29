package SheriffsssPackage.render;

import SheriffsssPackage.context.AssetManager;
import SheriffsssPackage.system.weapon.ItemDefinition;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.List;

public class EquipmentRenderer
{
  private final AssetManager assets;
  private final Font debugFont = new Font("Arial", Font.BOLD, 13);
  private final Color equipmentBorderColor = new Color(255, 230, 120, 220);
  private final Color equipmentPanelColor = new Color(0, 0, 0, 190);
  private final Color equipmentSelectedRowColor = new Color(70, 130, 90, 180);
  private final Color equipmentEvenRowColor = new Color(255, 255, 255, 24);
  private final Color equipmentOddRowColor = new Color(255, 255, 255, 12);

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
    g2.setColor(this.equipmentBorderColor);
    g2.setStroke(GameTheme.LIGHT_STROKE);
    g2.drawRect(x, y, width, height);
    BufferedImage sheriffIcon = this.assets.getImage("sprites/sheriffsss_icono.png");
    int iconSize = 128;
    g2.drawImage(sheriffIcon, x + (width - iconSize) / 2, y + 42, iconSize, iconSize, null);
    drawEquipmentSelector(g2, equipmentHud);
  }

  private void drawEquipmentSelector(Graphics2D g2, EquipmentHudView equipmentHud)
  {
    ItemDefinition weapon = equipmentHud.equippedWeapon();
    int x = equipmentHud.selectorX();
    int y = equipmentHud.selectorY();
    int width = equipmentHud.selectorWidth();
    int height = equipmentHud.selectorHeight();
    g2.setColor(this.equipmentPanelColor);
    g2.fillRect(x, y, width, height);
    g2.setColor(this.equipmentBorderColor);
    g2.drawRect(x, y, width, height);
    if (weapon != null)
    {
      drawEquipmentWeaponSprite(g2, weapon, x + 8, y + 8, width - 16, 38);
      String damageText = "Daño: " + formatDamage(weapon.getProjectileDamage());
      TextRenderer.draw(g2, this.debugFont, damageText, Color.LIGHT_GRAY,
        TextRenderer.centeredX(g2, this.debugFont, damageText, x + width / 2), y + height - 12, false);
    }
  }

  private void drawEquipmentWeaponList(Graphics2D g2, EquipmentHudView equipmentHud)
  {
    List<ItemDefinition> weapons = equipmentHud.weapons();
    int x = equipmentHud.listX();
    int y = equipmentHud.listY();
    int width = equipmentHud.listWidth();
    int rowHeight = equipmentHud.listRowHeight();
    g2.setColor(this.equipmentPanelColor);
    g2.fillRect(x, y, width, rowHeight * weapons.size());
    g2.setColor(this.equipmentBorderColor);
    g2.drawRect(x, y, width, rowHeight * weapons.size());
    for (int i = 0; i < weapons.size(); i++)
    {
      ItemDefinition weapon = weapons.get(i);
      int rowY = y + i * rowHeight;
      boolean selected = weapon == equipmentHud.equippedWeapon();
      g2.setColor(selected ? this.equipmentSelectedRowColor : equipmentRowColor(i));
      g2.fillRect(x + 1, rowY + 1, width - 2, rowHeight - 2);
      drawEquipmentWeaponSprite(g2, weapon, x + 12, rowY + 8, 90, 32);
      TextRenderer.draw(g2, this.debugFont, weapon.getDisplayName(), Color.WHITE, x + 120, rowY + 21, false);
      TextRenderer.draw(g2, this.debugFont, "Daño: " + formatDamage(weapon.getProjectileDamage()), Color.LIGHT_GRAY, x + 120, rowY + 41, false);
    }
  }

  private Color equipmentRowColor(int index)
  {
    return index % 2 == 0 ? this.equipmentEvenRowColor : this.equipmentOddRowColor;
  }

  private void drawEquipmentWeaponSprite(Graphics2D g2, ItemDefinition weapon, int x, int y, int maxWidth, int maxHeight)
  {
    BufferedImage sprite = this.assets.getImage(weapon.getSpritePath());
    int drawWidth = Math.max(1, weapon.getHeldDrawWidth());
    int drawHeight = Math.max(1, weapon.getHeldDrawHeight());
    double scale = Math.min(maxWidth / (double) drawWidth, maxHeight / (double) drawHeight);
    int fittedWidth = Math.max(1, (int) Math.round(drawWidth * scale));
    int fittedHeight = Math.max(1, (int) Math.round(drawHeight * scale));
    int drawX = x + (maxWidth - fittedWidth) / 2;
    int drawY = y + (maxHeight - fittedHeight) / 2;
    g2.drawImage(sprite, drawX, drawY, fittedWidth, fittedHeight, null);
  }

  private String formatDamage(double damage)
  {
    return Math.abs(damage - Math.round(damage)) < 0.001 ? Integer.toString((int) Math.round(damage)) : String.format(java.util.Locale.US, "%.1f", damage);
  }
}
