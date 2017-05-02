package i2c.sensor.listener;

import java.util.EventListener;

public abstract class LSM303Listener implements EventListener
{
  public void dataDetected(float accX, float accY, float accZ, float magX, float magY, float magZ, float heading, float pitch, float roll) {}
  public void close() {}
}
