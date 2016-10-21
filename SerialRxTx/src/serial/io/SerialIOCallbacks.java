package serial.io;

public interface SerialIOCallbacks
{
  public void connected(boolean b);
  public void onSerialData(byte b);
}
