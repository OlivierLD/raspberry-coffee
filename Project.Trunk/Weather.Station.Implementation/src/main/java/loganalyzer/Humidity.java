package loganalyzer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;

import java.io.FileWriter;

import java.text.SimpleDateFormat;

import java.util.Date;

import org.json.JSONObject;

public class Humidity
{
  public static void main(String... args) throws Exception
  {
    String fName = "weather.data.log";
    SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
    BufferedReader br = new BufferedReader(new FileReader(new File(fName)));
    BufferedWriter bw = new BufferedWriter(new FileWriter(new File("data.csv")));
    int nbl = 0;
    String line = "";
    long before = System.currentTimeMillis();
    bw.write("Date;humidity;temperature;pressure\n");
    while (line != null)
    {
      line = br.readLine();
      if (line != null)
      {
        nbl++;
        JSONObject obj = new JSONObject(line);
        double hum = obj.getDouble("humidity");
        double tmp = obj.getDouble("temperature");
        double prs = obj.getDouble("pressure");
        long date  = obj.getLong("epoch");
        Date timestamp = new Date(date);
        bw.write(sdf.format(timestamp) + ";" + hum + ";" + tmp + ";" + prs +"\n");
      }
    }
    long after = System.currentTimeMillis();
    br.close();
    bw.close();
    System.out.println("Done with " + nbl + " record(s) in " + Long.toString(after - before) + " ms.");
  }
}
