package util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;

public class LogReworker
{
  public static void main(String... args) throws Exception
  {
    String original = "C:\\Users\\olediour.ORADEV\\Desktop\\serial.data.log";
    String reworked = "reworked.serial.log";
    BufferedReader br = new BufferedReader(new FileReader(original));
    BufferedWriter bw = new BufferedWriter(new FileWriter(reworked));
    String line = "";
    while (line != null)
    {
      line = br.readLine();
      if (line != null)
      {
        if (line.startsWith(" "))
        {
          String s = line.substring(0, 50).trim() + " ";
          bw.write(s);
        }
      }
    }
    br.close();
    bw.close();
  }
}
