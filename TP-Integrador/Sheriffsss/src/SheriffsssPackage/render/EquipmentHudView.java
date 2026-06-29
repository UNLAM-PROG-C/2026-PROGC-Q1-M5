package SheriffsssPackage.render;

import SheriffsssPackage.session.Equipment;
import SheriffsssPackage.system.weapon.ItemDefinition;
import SheriffsssPackage.ui.EquipmentMenuLayout;


import java.util.Collections;
import java.util.List;


public final class EquipmentHudView {
  private boolean visible;
  private boolean selectorOpen;
  private ItemDefinition equippedWeapon;
  private List<ItemDefinition> weapons = Collections.emptyList();
  private int panelX;
  private int panelY;
  private int panelWidth;
  private int panelHeight;
  private int selectorX;
  private int selectorY;
  private int selectorWidth;
  private int selectorHeight;
  private int listX;
  private int listY;
  private int listWidth;
  private int listRowHeight;

  public void update(Equipment equipment, EquipmentMenuLayout layout, boolean visible) {
    this.visible = visible && equipment != null && equipment.isMenuOpen();
    this.selectorOpen = this.visible && equipment.isWeaponSelectorOpen();
    this.equippedWeapon = equipment == null ? null : equipment.getEquippedWeapon();
    this.weapons = equipment == null ? Collections.<ItemDefinition>emptyList() : equipment.getWeaponSelectionOrder();
    updateLayout(layout);
  }

  private void updateLayout(EquipmentMenuLayout layout) {
    this.panelX = layout.panelX();
    this.panelY = layout.panelY();
    this.panelWidth = layout.panelWidth();
    this.panelHeight = layout.panelHeight();
    this.selectorX = layout.selectorX();
    this.selectorY = layout.selectorY();
    this.selectorWidth = layout.selectorWidth();
    this.selectorHeight = layout.selectorHeight();
    this.listX = layout.listX();
    this.listY = layout.listY();
    this.listWidth = layout.listWidth();
    this.listRowHeight = layout.listRowHeight();
  }

  public boolean visible() {
    return this.visible;
  }

  public boolean selectorOpen() {
    return this.selectorOpen;
  }

  public ItemDefinition equippedWeapon() {
    return this.equippedWeapon;
  }

  public List<ItemDefinition> weapons() {
    return this.weapons;
  }

  public int panelX() {
    return this.panelX;
  }

  public int panelY() {
    return this.panelY;
  }

  public int panelWidth() {
    return this.panelWidth;
  }

  public int panelHeight() {
    return this.panelHeight;
  }

  public int selectorX() {
    return this.selectorX;
  }

  public int selectorY() {
    return this.selectorY;
  }

  public int selectorWidth() {
    return this.selectorWidth;
  }

  public int selectorHeight() {
    return this.selectorHeight;
  }

  public int listX() {
    return this.listX;
  }

  public int listY() {
    return this.listY;
  }

  public int listWidth() {
    return this.listWidth;
  }

  public int listRowHeight() {
    return this.listRowHeight;
  }
}
