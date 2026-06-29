package SheriffsssPackage.ui;

import SheriffsssPackage.context.GameConfig;



public final class EquipmentMenuLayout {
  private static final int PANEL_WIDTH = 250;
  private static final int PANEL_HEIGHT = 310;
  private static final int SELECTOR_X_OFFSET = 28;
  private static final int SELECTOR_Y_OFFSET = 216;
  private static final int SELECTOR_WIDTH = 194;
  private static final int SELECTOR_HEIGHT = 74;
  private static final int LIST_GAP = 8;
  private static final int LIST_WIDTH = 330;
  private static final int LIST_ROW_HEIGHT = 56;

  public int panelX()
  {
    return (GameConfig.SCREEN_WIDTH - PANEL_WIDTH) / 2;
  }

  public int panelY() {
    return (GameConfig.SCREEN_HEIGHT - PANEL_HEIGHT) / 2;
  }

  public int panelWidth() {
    return PANEL_WIDTH;
  }

  public int panelHeight()
  {
    return PANEL_HEIGHT;
  }

  public int selectorX()
  {
    return panelX() + SELECTOR_X_OFFSET;
  }

  public int selectorY()
    {
    return panelY() + SELECTOR_Y_OFFSET;
  }

  public int selectorWidth()
    {
    return SELECTOR_WIDTH;
  }

  public int selectorHeight()
  {
    return SELECTOR_HEIGHT;
  }

  public int listX()
  {
    return panelX() + PANEL_WIDTH + LIST_GAP;
  }

  public int listY()
    {
    return selectorY();
  }

  public int listWidth()
    {
    return LIST_WIDTH;
  }

  public int listRowHeight()
  {
    return LIST_ROW_HEIGHT;
  }

  public boolean isInsidePanel(int mouseX, int mouseY)
  {
    return isInside(mouseX, mouseY, panelX(), panelY(), PANEL_WIDTH, PANEL_HEIGHT);
  }

  public boolean isInsideSelector(int mouseX, int mouseY)
    {
    return isInside(mouseX, mouseY, selectorX(), selectorY(), SELECTOR_WIDTH, SELECTOR_HEIGHT);
  }

  public int weaponIndexAt(int mouseX, int mouseY, int rowCount)
    {
    if (!isInsideWeaponList(mouseX, mouseY, rowCount)) {
      return -1;
    }
    return (mouseY - listY()) / LIST_ROW_HEIGHT;
  }

  private boolean isInsideWeaponList(int mouseX, int mouseY, int rowCount) {
    return isInside(mouseX, mouseY, listX(), listY(), LIST_WIDTH, rowCount * LIST_ROW_HEIGHT);
  }

  private boolean isInside(int mouseX, int mouseY, int x, int y, int width, int height)
    {
    return mouseX >= x && mouseX < x + width
      && mouseY >= y && mouseY < y + height;
  }
}
