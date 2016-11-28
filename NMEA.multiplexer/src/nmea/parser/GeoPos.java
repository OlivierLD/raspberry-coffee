package nmea.parser;

import java.io.Serializable;

import java.text.DecimalFormat;

public class GeoPos implements Serializable
{
  public double lat = 0.0;
  public double lng = 0.0;

  public GeoPos(double l,
                double g)
  {
    this.lat = l;
    this.lng = g;
  }
   
  public boolean equals(GeoPos compareto)
  {
    return (this.lat == compareto.lat && this.lng == compareto.lng);
  }
  
  public String toString()
  {
    return getLatInDegMinDec() + " / " + getLngInDegMinDec();
  }
  
  public String getLatInDegMinDec()
  {
    int degree = (int)lat;
    String sgn = (degree>=0)?"N":"S";
    double minutes = Math.abs(lat - degree);
    double hexMin = 100.0 * minutes * (6.0/10.0);
    DecimalFormat df = new DecimalFormat("00.00");
    String strMinutes = df.format(hexMin);
    df = new DecimalFormat("00");
    return sgn + "  " + df.format((long)Math.abs(degree)) + "�" + strMinutes + "'";
  }

  public String getLngInDegMinDec()
  {
    int degree = (int)lng;
    String sgn = (degree>=0)?"E":"W";
    double minutes = Math.abs(lng - degree);
    double hexMin = 100.0 * minutes * (6.0/10.0);
    DecimalFormat df = new DecimalFormat("00.00");
    String strMinutes = df.format(hexMin);
    df = new DecimalFormat("000");
    return sgn + " " + df.format((long)Math.abs(degree)) + "�" + strMinutes + "'";
  }
  
  public static GeoPos init()
  {
    return new GeoPos(0d, 0d);
  }
}
