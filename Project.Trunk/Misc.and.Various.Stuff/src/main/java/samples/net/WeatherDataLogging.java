package samples.net;

import i2c.sensor.BMP180;

import i2c.sensor.HTU21DF;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import java.text.SimpleDateFormat;

import java.util.Date;

import org.json.JSONObject;

import http.client.HTTPClient;

/**
 * Log weather data with php/MySQL over the net
 */
public class WeatherDataLogging
{
  private final static String LOGGER_URL = "http://donpedro.lediouris.net/php/raspi/insert.php"; // ?board=OlivRPi1&sensor=BMP180&type=TEMPERATURE&data=24
  private final static String BMP_SENSOR_ID = "BMP180";
  private final static String HUM_SENSOR_ID = "HTU21D-F";
  private final static String TEMPERATURE = "TEMPERATURE";
  private final static String PRESSURE    = "PRESSURE";
  private final static String HUMIDITY    = "HUMIDITY";

  private static String boardID = "OlivRPi1";
  private static long waitTime  = 10_000L;
  private static String sessionID = "XX";
  static
  {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    sessionID = sdf.format(new Date());
  }

  private final static String BOARD_PRM = "-board";
  private final static String WAIT_PRM  = "-wait";
  private final static String SESS_PRM  = "-sess";
  private final static String HELP_PRM  = "-help";

  private final static String NO_BMP180  = "-nobmp180";
  private final static String NO_HTU21DF = "-nohtu21df";

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
      if (BOARD_PRM.equals(args[i]))
        boardID = args[i + 1];
      else if (NO_BMP180.equals(args[i]))
        withBMP180 = false;
      else if (NO_HTU21DF.equals(args[i]))
        withHTU21DF = false;
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
      else if (SESS_PRM.equals(args[i]))
        sessionID = args[i + 1];
      else if (HELP_PRM.equals(args[i]))
      {
        System.out.println("Usage is:");
        System.out.println("  java raspisamples.log.net.WeatherDataLogging -board <BoardID> -sess <Session ID> -wait <time-in-sec> -help ");
        System.out.println("  <BoardID> is your board ID (default is OlivRPi1)");
        System.out.println("  <Session ID> identifies your logging session (default current date YYYY-MM-DD)");
        System.out.println("  <time-in-sec> is the amount of seconds between logs (default is 10)");
        System.out.println();
        System.out.println("Logging data for board [" + boardID + "]");
        System.out.println("Logging data every " + Long.toString(waitTime / 1_000) + " s. Session ID:" + sessionID);
        System.exit(0);
      }
    }
  }

  public static void main(String... args)
  {
    processPrm(args);

    System.out.println("Logging data for [" + boardID + "], every " + Long.toString(waitTime / 1_000) + " s.");

    final NumberFormat NF = new DecimalFormat("##00.00");

    float press = 0;
    float temp  = 0;
    double alt  = 0;
    float hum  = 0;
    HTU21DF humSensor = null;
    BMP180 bmpSensor = null;

    if (withBMP180)
    {
      try
      {
        bmpSensor = new BMP180();
      }
      catch (Exception ex)
      {
        ex.printStackTrace();
      }
    }
    if (withHTU21DF)
    {
      try
      {
        humSensor = new HTU21DF();
      }
      catch (Exception ex)
      {
        ex.printStackTrace();
      }
    }
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
                                           }
                                         });

    while (true) // forever loop
    {
      // Read/Measure here
      if (bmpSensor != null)
      {
        try { press = bmpSensor.readPressure(); }
        catch (Exception ex)
        {
          System.err.println(ex.getMessage());
          ex.printStackTrace();
        }
        bmpSensor.setStandardSeaLevelPressure((int)press); // As we ARE at the sea level (in San Francisco).
        try { alt = bmpSensor.readAltitude(); }
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
      System.out.println("At " + new Date().toString());
      System.out.println("Temperature: " + NF.format(temp) + " C");
      System.out.println("Pressure   : " + NF.format(press / 100) + " hPa");
      System.out.println("Altitude   : " + NF.format(alt) + " m");
      System.out.println("Humidity   : " + NF.format(hum) + " %");

      // Log here
      try
      {
        if (bmpSensor != null)
        {
          String url = LOGGER_URL + "?board=" + boardID + "&session=" + sessionID + "&sensor=" + BMP_SENSOR_ID + "&type=" + TEMPERATURE + "&data=" + NF.format(temp);
          String response = HTTPClient.getContent(url);
          JSONObject json = new JSONObject(response);
          System.out.println("Returned\n" + json.toString(2));
          try { Thread.sleep(1_000); } catch (Exception ex) { ex.printStackTrace(); } // To avoid duplicate PK
          url = LOGGER_URL + "?board=" + boardID + "&session=" + sessionID + "&sensor=" + BMP_SENSOR_ID + "&type=" + PRESSURE + "&data=" + NF.format(press / 100);
          response = HTTPClient.getContent(url);
          json = new JSONObject(response);
          System.out.println("Returned\n" + json.toString(2));
          try { Thread.sleep(1_000); } catch (Exception ex) { ex.printStackTrace(); } // To avoid duplicate PK
        }
        if (humSensor != null)
        {
          String url = LOGGER_URL + "?board=" + boardID + "&session=" + sessionID + "&sensor=" + HUM_SENSOR_ID + "&type=" + HUMIDITY + "&data=" + NF.format(hum);
          String response = HTTPClient.getContent(url);
          JSONObject json = new JSONObject(response);
          System.out.println("Returned\n" + json.toString(2));
        }
      }
      catch (Exception ex)
      {
        ex.printStackTrace();
      }
      waitfor(waitTime);
    }
  }
}
