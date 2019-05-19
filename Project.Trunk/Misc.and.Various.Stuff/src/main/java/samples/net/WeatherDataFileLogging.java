package samples.net;

import i2c.sensor.BMP180;

import i2c.sensor.HTU21DF;

import java.io.BufferedWriter;

import java.io.FileWriter;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import org.json.JSONObject;

/**
 * Log weather data in a file
 */
public class WeatherDataFileLogging
{
  private static long waitTime  = 10_000L;

  private final static String WAIT_PRM  = "-wait";
  private final static String FILE_PRM  = "-file";
  private final static String HELP_PRM  = "-help";

  private final static String NO_BMP180  = "-nobmp180";
  private final static String NO_HTU21DF = "-nohtu21df";

  private static String logFileName = "weather.data.log";
  private static boolean withBMP180  = true;
  private static boolean withHTU21DF = true;

  protected static void waitfor(long howMuch)
  {
    try { Thread.sleep(howMuch); } catch (InterruptedException ie) { ie.printStackTrace(); }
  }

  private static void processPrm(String... args)
  {
    for (int i=0; i<args.length; i++)
    {
      if (FILE_PRM.equals(args[i]))
        logFileName = args[i + 1];
      else if (WAIT_PRM.equals(args[i]))
      {
        try
        {
          waitTime = 1_000L * Integer.parseInt(args[i + 1]);
        }
        catch (Exception ex)
        {
          ex.printStackTrace();
        }
      }
      else if (NO_BMP180.equals(args[i]))
        withBMP180 = false;
      else if (NO_HTU21DF.equals(args[i]))
        withHTU21DF = false;
      else if (HELP_PRM.equals(args[i]))
      {
        System.out.println("Usage is:");
        System.out.println("  java raspisamples.log.net.WeatherDataFileLogging -file <LogFileName> -wait <time-in-sec> [ -nobmp180 ][ -nohtu21df ] -help ");
        System.out.println("  <LogFileName> is your log file name (default is weather.data.log)");
        System.out.println("  <time-in-sec> is the amount of seconds between logs (default is 10)");
        System.out.println();
        System.out.println("Logging data in [" + logFileName + "]");
        System.out.println("Logging data every " + Long.toString(waitTime / 1_000) + " s");
        System.exit(0);
      }
    }
  }

  public static void main(String... args) throws Exception
  {
    processPrm(args);

    System.out.println("Logging data in [" + logFileName + "], every " + Long.toString(waitTime / 1_000) + " s.");
    final BufferedWriter log = new BufferedWriter(new FileWriter(logFileName));
    final NumberFormat NF = new DecimalFormat("##00.00");
    BMP180 bmpSensor = null;
    if (withBMP180)
    {
      try { bmpSensor = new BMP180(); }
      catch (Exception ex ) { ex.printStackTrace(); }
    }
    float press = 0;
    float temp  = 0;
    HTU21DF humSensor = null;
    if (withHTU21DF)
    {
      try { humSensor = new HTU21DF(); }
      catch (Exception ex ) { ex.printStackTrace(); }
    }
    float hum  = 0;

    if (humSensor != null)
    {
      try
      {
        if (!humSensor.begin())
        {
          System.out.println("Sensor not found!");
          System.exit(1);
        }
      }
      catch (Exception ex)
      {
        ex.printStackTrace();
        System.exit(1);
      }
    }

    Runtime.getRuntime().addShutdownHook(new Thread()
                                         {
                                           public void run()
                                           {
                                             System.out.println("\nBye now.");
                                             // Close log file
                                             if (log != null)
                                             {
                                               try
                                               {
                                                 log.flush();
                                                 log.close();
                                               } catch (Exception ex) { ex.printStackTrace(); }
                                             }
                                           }
                                         });

    while (true)
    {
      if (bmpSensor != null)
      {
        try { press = bmpSensor.readPressure(); }
        catch (Exception ex)
        {
          System.err.println(ex.getMessage());
          ex.printStackTrace();
        }
        try { temp = bmpSensor.readTemperature(); }
        catch (Exception ex)
        {
          System.err.println(ex.getMessage());
          ex.printStackTrace();
        }
      }
      if (humSensor != null)
      {
        try { hum = humSensor.readHumidity(); }
        catch (Exception ex)
        {
          System.err.println(ex.getMessage());
          ex.printStackTrace();
        }
      }
      long now = System.currentTimeMillis();
//    System.out.println("At " + new Date().toString());
      System.out.println("Temperature: " + NF.format(temp) + " C");
      System.out.println("Pressure   : " + NF.format(press / 100) + " hPa");
      System.out.println("Humidity   : " + NF.format(hum) + " %");

      // Log here
      try
      {
        JSONObject dataObject = new JSONObject(); //  + NF.format(temp);
        dataObject.put("epoch", now);
        dataObject.put("pressure", press/100);
        dataObject.put("temperature", temp);
        dataObject.put("humidity", hum);

        String logStr = dataObject.toString();
        log.write(logStr + "\n");
        log.flush();
      }
      catch (Exception ex)
      {
        ex.printStackTrace();
      }
      waitfor(waitTime);
    }
  }
}
