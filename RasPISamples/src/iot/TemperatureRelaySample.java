package iot;

import adafruit.io.rest.HttpClient;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;

import i2c.sensor.BME280;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

/**
 * Publishes the data from a BME280 on an IoT server (Adafruit-IO)
 * Data from the same server can be read to turn on a heater (with a relay), remotely
 *
 * One thread reads the BME280 and feeds the IoT server
 * Another thread polls a switch, and see what to do with the relay.
 */
public class TemperatureRelaySample
{
  private final static String HEATER_NAME = "onoff";
  private final static String THERMOMETER_NAME = "air-temperature";
  
  private final static String key = System.getProperty("key"); 
  
  final GpioController gpio = GpioFactory.getInstance();
  private GpioPinDigitalOutput relayPin;
  
  private final static int ON  = 1;
  private final static int OFF = 2;

  private void initRelay()
  {
    // For a relay it seems that HIGH means NC (Normally Closed)...
    // pin 00/#17 
    relayPin = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_00, "Relay", PinState.HIGH);
  }
  
  private void setRelay(int pos)
  {
    if (pos == OFF)
      relayPin.high();
    else
      relayPin.low();
  }
  
  private void shutdownRelay()
  {
    gpio.shutdown();
  }
  
  private static String readHeaterSwitch(String key) throws Exception
  {
    String url = "https://io.adafruit.com/api/feeds/" + HEATER_NAME;
    Map<String, String> headers = new HashMap<String, String>(1);
    headers.put("X-AIO-Key", key);
    String content = HttpClient.doGet(url, headers);

 // System.out.println("GET\n" + content);

    JSONObject json = new JSONObject(content);
    String lastValue = json.getString("last_value");
 // System.out.println("Feed value:" + lastValue);
    return lastValue;
  }
  
  private static String readTemperature(String key) throws Exception
  {
    String url = "https://io.adafruit.com/api/feeds/" + THERMOMETER_NAME;
    Map<String, String> headers = new HashMap<String, String>(1);
    headers.put("X-AIO-Key", key);
    String content = HttpClient.doGet(url, headers);

 // System.out.println("GET\n" + content);

    JSONObject json = new JSONObject(content);
    String tempValue = json.getString("value");
 // System.out.println("Feed value:" + tempValue);
    return tempValue;
  }
  
  private static void setTemperature(String key, float temperature) throws Exception
  {
    String url = "https://io.adafruit.com/api/feeds/" + THERMOMETER_NAME + "/data";
    Map<String, String> headers = new HashMap<String, String>(1);
    headers.put("X-AIO-Key", key);
    JSONObject value = new JSONObject();
    value.put("value", Math.round(temperature * 100d) / 100d);
//  System.out.println("Sending " + value.toString(2));
    int httpCode = HttpClient.doPost(url, headers, value.toString());
//  System.out.println("POST Ret:" + httpCode);
  }
  
  private boolean working = false;
  
  private boolean keepWorking()
  {
    return working;
  }
  
  private void setWorking(boolean b)
  {
    working = b;
  }
  
  public static void main(String[] args) throws Exception
  {
    final TemperatureRelaySample trs = new TemperatureRelaySample();
    trs.initRelay();
    
    final BME280 sensor = new BME280();
    
    Thread thermometerThread = new Thread()
    {
      float press = 0;
      float temp  = 0;
      float hum   = 0;
      double alt  = 0;

      public void run()
      {
        System.out.println("Starting temperature thread");
        while (trs.keepWorking())
        {
          try 
          { 
            temp = sensor.readTemperature(); 
            trs.setTemperature(key, temp);
          } 
          catch (Exception ex) 
          { 
            System.err.println(ex.getMessage()); 
            ex.printStackTrace();
          }
          delay(1000L);
        }
        System.out.println("\nTemperature thread completed");
      }
    };
    
    Thread switchReader = new Thread()
    {
      public void run()
      {
        System.out.println("Starting switch thread");
        while (trs.keepWorking())
        {
          try 
          { 
            String switchValue = trs.readHeaterSwitch(key);
            trs.setRelay(switchValue.equals("ON") ? ON : OFF);
          } 
          catch (Exception ex) 
          { 
            System.err.println(ex.getMessage()); 
            ex.printStackTrace();
          }
          delay(1000L);
        }
        System.out.println("\nSwitch thread completed");
      }
    };
    
    final Thread me = Thread.currentThread();
    
    Runtime.getRuntime().addShutdownHook(new Thread()
    {
      public void run()
      {
        trs.setWorking(false);
        delay(3000L);
        trs.shutdownRelay(); 
        synchronized (me)
        {
          me.notify();
        }
      }
    });
    
    trs.setWorking(true);
    thermometerThread.start();
    switchReader.start();
    
    synchronized (me)
    {
      me.wait();
      System.out.println("\nWait is over");
    }
    
    System.out.println("Done!");
  }
  
  private final static void delay(long ms)
  {
    try { Thread.sleep(ms); } catch (Exception ex) {}
  }
}
