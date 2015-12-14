package fona.manager;

import fona.manager.FONAManager.ReceivedSMS;
import fona.manager.FONAManager.NetworkStatus;

public interface FONAClient
{
  public void recievedSMS(int sms);
  public void fonaConnected();
  public void moduleNameAndRevision(String str);
  public void debugOn();
  public void batteryResponse(String percent, String mv);
  public void signalResponse(String s);
  public void simCardResponse(String s);
  public void networkNameResponse(String s);
  public void numberSMSResponse(int n);
  public void readSMS(ReceivedSMS sms);
  public void someoneCalling();
  public void networkStatusResponse(NetworkStatus ns);
  public void smsDeletedResponse(int sms, boolean success);
}
