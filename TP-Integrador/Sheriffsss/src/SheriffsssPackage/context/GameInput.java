package SheriffsssPackage.context;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

public class GameInput implements KeyListener, MouseListener, MouseWheelListener, MouseMotionListener
{
  private boolean leftPressed;
  private boolean rightPressed;
  private boolean upPressed;
  private boolean downPressed;
  private boolean escapeHeld;
  private boolean equipmentHeld;
  private boolean fullscreenHeld;
  private boolean primaryHeld;
  private boolean escapeQueued;
  private boolean equipmentToggleQueued;
  private boolean fullscreenToggleQueued;
  private boolean primaryClickQueued;
  private boolean trainingIncQueued;
  private boolean trainingDecQueued;
  private boolean trainingResetQueued;
  private boolean trainingIncHeld;
  private boolean trainingDecHeld;
  private boolean trainingResetHeld;
  private boolean zoomInHeld;
  private boolean zoomOutHeld;
  private int mouseX;
  private int mouseY;
  private int zoomWheelRotationSteps;
  private int zoomKeySteps;

  public int getMoveX()
 {
    return (this.leftPressed ? -1 : 0) + (this.rightPressed ? 1 : 0);
  }

  public int getMoveY()
  {
    return (this.upPressed ? -1 : 0) + (this.downPressed ? 1 : 0);
  }

  public void clearMovement()
  {
    this.leftPressed = false;
    this.rightPressed = false;
    this.upPressed = false;
    this.downPressed = false;
  }

  public void clearPrimaryAction()
 {
    this.primaryHeld = false;
    this.primaryClickQueued = false;
  }

  public boolean consumeEscapePressed()
 {
    boolean value = this.escapeQueued;
    this.escapeQueued = false;
    return value;
  }

  public boolean consumeEquipmentToggle()
 {
    boolean value = this.equipmentToggleQueued;
    this.equipmentToggleQueued = false;
    return value;
  }

  public boolean consumeFullscreenToggle()
 {
    boolean value = this.fullscreenToggleQueued;
    this.fullscreenToggleQueued = false;
    return value;
  }

  public boolean consumePrimaryClick()
 {
    boolean value = this.primaryClickQueued;
    this.primaryClickQueued = false;
    return value;
  }

  public boolean consumeTrainingIncrement()
 {
    boolean value = this.trainingIncQueued;
    this.trainingIncQueued = false;
    return value;
  }

  public boolean consumeTrainingDecrement()
 {
    boolean value = this.trainingDecQueued;
    this.trainingDecQueued = false;
    return value;
  }

  public boolean consumeTrainingReset()
 {
    boolean value = this.trainingResetQueued;
    this.trainingResetQueued = false;
    return value;
  }

  public boolean isPrimaryHeld()
 {
    return this.primaryHeld;
  }

  public int consumeZoomWheelSteps()
 {
    int steps = this.zoomWheelRotationSteps;
    this.zoomWheelRotationSteps = 0;
    return steps;
  }

  public int consumeZoomKeySteps()
 {
    int steps = this.zoomKeySteps;
    this.zoomKeySteps = 0;
    return steps;
  }

  public int getZoomKeyDirection()
 {
    return (this.zoomInHeld ? 1 : 0) + (this.zoomOutHeld ? -1 : 0);
  }

  public int getMouseX()
  {
    return this.mouseX;
  }

  public int getMouseY()
 {
    return this.mouseY;
  }

  @Override
  public void keyTyped(KeyEvent e)
 {
  }

  @Override
  public void keyPressed(KeyEvent e)
 {
    if (e.getKeyCode() == KeyEvent.VK_F11 || (e.getKeyCode() == KeyEvent.VK_ENTER && e.isAltDown()))
    {
      if (!this.fullscreenHeld)
      {
        this.fullscreenToggleQueued = true;
        this.fullscreenHeld = true;
      }
      return;
    }
    switch (e.getKeyCode())
    {
      case KeyEvent.VK_A:
        this.leftPressed = true;
        break;
      case KeyEvent.VK_D:
        this.rightPressed = true;
        break;
      case KeyEvent.VK_W:
        this.upPressed = true;
        break;
      case KeyEvent.VK_S:
        this.downPressed = true;
        break;
      case KeyEvent.VK_ESCAPE:
        if (!this.escapeHeld)
        {
          this.escapeQueued = true;
          this.escapeHeld = true;
        }
        break;
      case KeyEvent.VK_TAB:
        if (!this.equipmentHeld)
        {
          this.equipmentToggleQueued = true;
          this.equipmentHeld = true;
        }
        break;
      case GameConfig.CAMERA_ZOOM_IN_KEY:
      case GameConfig.CAMERA_ZOOM_IN_KEY_ALT:
      case GameConfig.CAMERA_ZOOM_IN_KEY_NUMPAD:
        if (!this.zoomInHeld)
    {
          this.zoomKeySteps++;
          this.zoomInHeld = true;
        }
        break;
      case GameConfig.CAMERA_ZOOM_OUT_KEY:
      case GameConfig.CAMERA_ZOOM_OUT_KEY_NUMPAD:
        if (!this.zoomOutHeld)
        {
          this.zoomKeySteps--;
          this.zoomOutHeld = true;
        }
        break;
      case GameConfig.TRAINING_PANEL_INC_KEY:
        if (!this.trainingIncHeld)
        {
          this.trainingIncQueued = true;
          this.trainingIncHeld = true;
        }
        break;
      case GameConfig.TRAINING_PANEL_DEC_KEY:
        if (!this.trainingDecHeld)
        {
          this.trainingDecQueued = true;
          this.trainingDecHeld = true;
        }
        break;
      case GameConfig.TRAINING_PANEL_RESET_KEY:
        if (!this.trainingResetHeld)
        {
          this.trainingResetQueued = true;
          this.trainingResetHeld = true;
        }
        break;
      default:
        break;
    }
  }

  @Override
  public void keyReleased(KeyEvent e)
 {
    switch (e.getKeyCode())
    {
      case KeyEvent.VK_A:
        this.leftPressed = false;
        break;
      case KeyEvent.VK_D:
        this.rightPressed = false;
        break;
      case KeyEvent.VK_W:
        this.upPressed = false;
        break;
      case KeyEvent.VK_S:
        this.downPressed = false;
        break;
      case KeyEvent.VK_ESCAPE:
        this.escapeHeld = false;
        break;
      case KeyEvent.VK_TAB:
        this.equipmentHeld = false;
        break;
      case KeyEvent.VK_F11:
      case KeyEvent.VK_ENTER:
        this.fullscreenHeld = false;
        break;
      case GameConfig.CAMERA_ZOOM_IN_KEY:
      case GameConfig.CAMERA_ZOOM_IN_KEY_ALT:
      case GameConfig.CAMERA_ZOOM_IN_KEY_NUMPAD:
        this.zoomInHeld = false;
        break;
      case GameConfig.CAMERA_ZOOM_OUT_KEY:
      case GameConfig.CAMERA_ZOOM_OUT_KEY_NUMPAD:
        this.zoomOutHeld = false;
        break;
      case GameConfig.TRAINING_PANEL_INC_KEY:
        this.trainingIncHeld = false;
        break;
      case GameConfig.TRAINING_PANEL_DEC_KEY:
        this.trainingDecHeld = false;
        break;
      case GameConfig.TRAINING_PANEL_RESET_KEY:
        this.trainingResetHeld = false;
        break;
      default:
        break;
    }
  }

  @Override
  public void mouseClicked(MouseEvent e)
 {
  }

  @Override
  public void mousePressed(MouseEvent e)
 {
    if (e.getButton() == MouseEvent.BUTTON1)
    {
      this.primaryClickQueued = true;
      this.primaryHeld = true;
    }
    updateMousePosition(e);
  }

  @Override
  public void mouseReleased(MouseEvent e)
 {
    if (e.getButton() == MouseEvent.BUTTON1)
    {
      this.primaryHeld = false;
    }
    updateMousePosition(e);
  }

  @Override
  public void mouseEntered(MouseEvent e)
 {
    updateMousePosition(e);
  }

  @Override
  public void mouseExited(MouseEvent e)
 {
    updateMousePosition(e);
  }

  @Override
  public void mouseWheelMoved(MouseWheelEvent e)
 {
    if (e.isControlDown())
    {
      this.zoomWheelRotationSteps += e.getWheelRotation();
    }
  }

  @Override
  public void mouseDragged(MouseEvent e)
 {
    updateMousePosition(e);
  }

  @Override
  public void mouseMoved(MouseEvent e)
 {
    updateMousePosition(e);
  }

  private void updateMousePosition(MouseEvent e)
 {
    this.mouseX = e.getX();
    this.mouseY = e.getY();
  }
}
