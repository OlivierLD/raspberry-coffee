package adc.levelreader.manager;

import rangesensor.JNI_HC_SR04;

public class SurfaceDistanceManager
{
  private final static int TRIG_PIN = 3; // GPIO_03, pin #15, BCM 22
  private final static int ECHO_PIN = 2; // GPIO_02, pin #13, BCM 27
  
  private JNI_HC_SR04 rangeSensor = null;
  private AirWaterInterface caller = null;
  
  public SurfaceDistanceManager(final AirWaterInterface client) throws Exception
  {
    this.caller = client;
    rangeSensor = new JNI_HC_SR04();
    rangeSensor.init(TRIG_PIN, ECHO_PIN);
  }
  
  public void startListening()
  {
    Thread distanceThread = new Thread("DistanceThread")
      {
        public void run()
        {
          while (true)
          {
            double dist = rangeSensor.readRange();
            caller.setSurfaceDistance(dist);
            try { Thread.sleep(1_000L); } catch (InterruptedException ie) {}
          }
        }
      };
    distanceThread.start();
  }
}
