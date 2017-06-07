package weatherstation.logger;

import java.net.URL;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import org.json.JSONException;
import org.json.JSONObject;

import raspisamples.util.HTTPClient;

/**
 * REST Interface to MySQL
 * JSON payload looks like:
 *
 *  { "dir": 350.0,
 *    "volts": 3.4567,
 *    "speed": 12.345,
 *    "gust": 13.456,
 *    "rain": 0.1,
 *    "press": 101300.00,
 *    "temp": 18.34,
 *    "hum": 58.5,
 *    "cputemp": 34.56 }
 *
 * The DB will take care of a timestamp.
 */
public class MySQLLoggerImpl implements LoggerInterface
{
  private long lastLogged = 0L; // Time of the last logging
  private static final long MINIMUM_BETWEEN_LOGS = 5_000L; // A System variable, in ms.
  private final static NumberFormat DOUBLE_FMT = new DecimalFormat("#0.000");
  private final static String REST_URL = "http://donpedro.lediouris.net/php/raspi/insert.wd.php"; // A System.variable

  private String restURL = "";
  private long betweenLogs = MINIMUM_BETWEEN_LOGS;
  
  public MySQLLoggerImpl()
  {
    restURL = System.getProperty("ws.rest.url", REST_URL);
    betweenLogs = Long.parseLong(System.getProperty("ws.between.logs", Long.toString(MINIMUM_BETWEEN_LOGS)));
  }
  
  private String json2qs(JSONObject json, String jMember, String qsName)
  {
    String ret = null;
    try
    {
      Object o = json.get(jMember);
      if (o != null)
      {
        if (o instanceof Double)
        {
          double d = ((Double)o).doubleValue();
          ret = qsName + "=" + DOUBLE_FMT.format(d);
        }
        else
          System.out.println("Got a " + o.getClass().getName());
      }
      else
        System.out.println("No " + jMember);
    }
    catch (JSONException je) { /* Not there */ }
    return ret;
  }
  
  /**
   * Produces a string like 
   * WDIR=350.0&WSPEED=12.345&WGUST=13.456&RAIN=0.1&PRMSL=101300.00&ATEMP=18.34&HUM=58.5&CPU=34.56
   */
  private String composeQS(JSONObject json)
  {
    String qs = "";
    String s = json2qs(json, "cputemp", "CPU");
    if (s != null)
      qs += ((qs.trim().length() == 0 ? "" : "&") + s);
    s = json2qs(json, "avgdir", "WDIR");
    if (s != null)
      qs += ((qs.trim().length() == 0 ? "" : "&") + s);
    else
    {
      s = json2qs(json, "dir", "WDIR");
      if (s != null)
        qs += ((qs.trim().length() == 0 ? "" : "&") + s);
    }
    s = json2qs(json, "speed", "WSPEED");
    if (s != null)
      qs += ((qs.trim().length() == 0 ? "" : "&") + s);
    s = json2qs(json, "gust", "WGUST");
    if (s != null)
      qs += ((qs.trim().length() == 0 ? "" : "&") + s);
    s = json2qs(json, "rain", "RAIN");
    if (s != null)
      qs += ((qs.trim().length() == 0 ? "" : "&") + s);
    s = json2qs(json, "press", "PRMSL");
    if (s != null)
      qs += ((qs.trim().length() == 0 ? "" : "&") + s);
    s = json2qs(json, "temp", "ATEMP");
    if (s != null)
      qs += ((qs.trim().length() == 0 ? "" : "&") + s);
    s = json2qs(json, "hum", "HUM");
    if (s != null)
      qs += ((qs.trim().length() == 0 ? "" : "&") + s);
    
    return qs;
  }
  
  @Override
  public void pushMessage(JSONObject json)
    throws Exception
  {
    long now = System.currentTimeMillis();
    if (now - this.lastLogged > betweenLogs)
    {
//    System.out.print(" >>> Logging... ");
      String queryString = composeQS(json);        
      this.lastLogged = now;
      /*
       * Actual logging goes here
       * URL would be like
       * http://donpedro.lediouris.net/php/raspi/insert.wd.php?WDIR=350.0&WSPEED=12.345&WGUST=13.456&RAIN=0.1&PRMSL=101300.00&ATEMP=18.34&HUM=58.5&CPU=34.56
       */
      System.out.println("REST Request:" + restURL + "?" + queryString);
      String response = HTTPClient.getContent(restURL + "?" + queryString);
      json = new JSONObject(response);
      System.out.println("Returned\n" + json.toString(2));      
    }
  }
}
