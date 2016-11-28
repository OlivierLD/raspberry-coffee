package nmea.parser;

import java.io.Serializable;

import java.text.DecimalFormat;

import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.TimeZone;

public class UTCDate implements Serializable
{
  private Date date = null;
  private static SimpleDateFormat FMT = new SimpleDateFormat("EEE, yyyy MMM dd HH:mm:ss 'UTC'");
  static 
  {
    FMT.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
  }

  public UTCDate()
  {
  }

  public UTCDate(Date date)
  {
    this.date = date;
  }

  public Date getValue()
  {
    return this.date;
  }

  public String toString()
  {
    return (date != null)?FMT.format(this.date):null;
  }
}
