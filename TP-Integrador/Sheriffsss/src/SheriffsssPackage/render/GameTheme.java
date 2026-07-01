package SheriffsssPackage.render;

import java.awt.BasicStroke;
import java.awt.Color;

public final class GameTheme
{
  public static final BasicStroke LIGHT_STROKE = new BasicStroke(2f);
  public static final BasicStroke HEAVY_STROKE = new BasicStroke(6f);

  public static final Color DARK_DIRT = new Color(64, 41, 5);
  public static final Color DIRT = new Color(118, 85, 43);
  public static final Color DAMAGE_RED = new Color(255, 0, 0, 50);
  public static final Color TRANSPARENT_BLACK = new Color(0, 0, 0, 100);

  private GameTheme()
  {
  }
}
