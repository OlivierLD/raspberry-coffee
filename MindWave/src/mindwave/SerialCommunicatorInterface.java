package mindwave;

public interface SerialCommunicatorInterface
{
  public boolean isSerialOpen();
  public void writeSerial(byte b);
  public void flushSerial();
}
