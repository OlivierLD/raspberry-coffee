package weatherstation.logger;

import java.io.BufferedWriter;

import java.io.FileWriter;

import java.io.IOException;

import org.json.JSONObject;

public class FileLogger implements LoggerInterface
{
  private BufferedWriter bw = null;
  public FileLogger()
  {
    try
    {
      bw = new BufferedWriter(new FileWriter("weather.station.log"));
    }
    catch (IOException ioe)
    {
      ioe.printStackTrace();
    }
  }
  
  @Override
  public void pushMessage(JSONObject json)
    throws Exception
  {
    if (bw != null)
    {
      json.put("timestamp", System.currentTimeMillis()); // Add a timestamp in the json obj.
      bw.write(json.toString() + "\n");
      bw.flush();
    }
    else
      System.out.println(">>> Logging:" + json.toString());
  }
}
