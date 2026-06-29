package SheriffsssPackage.context;

public class InfoMessageSystem
{
  private static final int INFO_MESSAGE_SLOTS = 4;
  private final String[] infoMessages = new String[INFO_MESSAGE_SLOTS];
  private final int[] infoMessageTicks = new int[INFO_MESSAGE_SLOTS];

  public InfoMessageSystem()
  {
  }

  public void update()
  {
    for (int i = 0; i < INFO_MESSAGE_SLOTS; i++) {
      if (this.infoMessageTicks[i] > 0) {
        this.infoMessageTicks[i]--;
      }
    }
  }

  public int getSlotCount()
  {
    return getInfoMessageSlotCount();
  }

  public String getMessage(int slotIndex)
  {
    return getInfoMessage(slotIndex);
  }

  public int getTicks(int slotIndex)
  {
    return getInfoMessageTicks(slotIndex);
  }

  public void setMessage(int slotIndex, String message, int durationTicks)
  {
    if (slotIndex >= 0 && slotIndex < INFO_MESSAGE_SLOTS) {
      this.infoMessages[slotIndex] = message;
      this.infoMessageTicks[slotIndex] = durationTicks;
    }
  }

  public int getInfoMessageSlotCount()
  {
    return INFO_MESSAGE_SLOTS;
  }

  public String getInfoMessage(int index)
  {
    return this.infoMessages[index];
  }

  public int getInfoMessageTicks(int index)
  {
    return this.infoMessageTicks[index];
  }
}
