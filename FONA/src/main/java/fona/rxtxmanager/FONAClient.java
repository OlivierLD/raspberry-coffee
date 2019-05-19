package fona.rxtxmanager;

import fona.rxtxmanager.FONAManager.ReceivedSMS;
import fona.rxtxmanager.FONAManager.NetworkStatus;

public interface FONAClient {
  void receivedSMS(int sms);
  void fonaConnected();
  void moduleNameAndRevision(String str);
  void debugOn();
  void batteryResponse(String percent, String mv);
  void signalResponse(String s);
  void simCardResponse(String s);
  void networkNameResponse(String s);
  void numberSMSResponse(int n);
  void readSMS(ReceivedSMS sms);
  void someoneCalling();
  void networkStatusResponse(NetworkStatus ns);
  void smsDeletedResponse(int sms, boolean success);
}
