package fona.arduino;

public interface FONAClient {
  public void genericSuccess(String mess);
  public void genericFailure(String mess);
  public void adcState(String mess);
  public void batteryState(String mess);
  public void ccidState(String mess);
  public void rssiState(String mess);
  public void networkState(String mess);
  public void numberOfMessages(int nb);
  public void message(ReadWriteFONA.SMS sms);
  public void sendSuccess(String mess);
  public void ready();
}
