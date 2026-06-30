package SheriffsssPackage.ui;

import SheriffsssPackage.session.Equipment;



public final class EquipmentMenuController {
  private final EquipmentMenuLayout layout;

  public EquipmentMenuController(EquipmentMenuLayout layout)
  {
    this.layout = layout;
  }

  public boolean handleClick(Equipment equipment, int mouseX, int mouseY)
  {
    if (equipment == null || !equipment.isMenuOpen()) {
      return false;
    }
    if (this.layout.isInsideSelector(mouseX, mouseY)) {
      equipment.toggleWeaponSelector();
      return true;
    }
    if (!equipment.isWeaponSelectorOpen())
      {
      return this.layout.isInsidePanel(mouseX, mouseY);
    }
    handleWeaponSelectorClick(equipment, mouseX, mouseY);
    return true;
  }

  private void handleWeaponSelectorClick(Equipment equipment, int mouseX, int mouseY)
      {
    int index = this.layout.weaponIndexAt(mouseX, mouseY, equipment.getWeaponSelectionOrder().size());
    if (index >= 0)
    {
      equipment.equipWeapon(equipment.getWeaponSelectionOrder().get(index));
      return;
    }
    equipment.closeWeaponSelector();
  }
}
