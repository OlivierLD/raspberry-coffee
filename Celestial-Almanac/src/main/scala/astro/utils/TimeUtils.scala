package astro.utils

object TimeUtils {

  /**
    * Y parameter (not year) for deltaT computing.
    *
    * @param year
    * @param month in [1..12]
    * @return
    */
  private def getY(year: Int, month: Int) = if (year < -1999 || year > 3000) throw new RuntimeException("Year must be in [-1999, 3000]")
  else year + ((month - 0.5) / 12d)


  /**
    * Since the usual (the ones I used to used) on-line resources are not always available, obsolete,
    * or expecting some serious revamping, here is a method to calculate deltaT out of thin air.
    *
    * See https://astronomy.stackexchange.com/questions/19172/obtaining-deltat-for-use-in-software
    * See values at https://eclipse.gsfc.nasa.gov/SEcat5/deltat.html#tab1 and
    * https://eclipse.gsfc.nasa.gov/SEcat5/deltat.html#tab2
    *
    * @param year  from -1999 to +3000
    * @param month in [1..12], NOT in [0..11]
    * @return
    */
  def getDeltaT(year: Int, month: Int): Double = {
    if (year < -1999 || year > 3000) throw new RuntimeException("Year must be in [-1999, 3000]")
    if (month < 1 || month > 12) throw new RuntimeException("Month must be in [1, 12]")
    var deltaT = .0
    val y = getY(year, month)
    if (year < -500) {
      val u = (y - 1820d) / 100d
      deltaT = -20d + (32d * (u * u))
    }
    else if (year < 500) {
      val u = y / 100d
      deltaT = 10583.6 + (-1014.41 * u) + (33.78311 * Math.pow(u, 2)) + (-5.952053 * Math.pow(u, 3)) + (-0.1798452 * Math.pow(u, 4)) + (0.022174192 * Math.pow(u, 5)) + (0.0090316521 * Math.pow(u, 6))
    }
    else if (year < 1600) {
      val u = (y - 1000d) / 100d
      deltaT = 1574.2 + (-556.01 * u) + (71.23472 * Math.pow(u, 2)) + (0.319781 * Math.pow(u, 3)) + (-0.8503463 * Math.pow(u, 4)) + (-0.005050998 * Math.pow(u, 5)) + (0.0083572073 * Math.pow(u, 6))
    }
    else if (year < 1700) {
      val t = y - 1600d
      deltaT = 120 + (-0.9808 * t) + (-0.01532 * Math.pow(t, 2)) + (Math.pow(t, 3) / 7129)
    }
    else if (year < 1800) {
      val t = y - 1700d
      deltaT = 8.83 + 0.1603 * t + (-0.0059285 * Math.pow(t, 2)) + (0.00013336 * Math.pow(t, 3)) + (Math.pow(t, 4) / -1174000)
    }
    else if (year < 1860) {
      val t = y - 1800d
      deltaT = 13.72 + (-0.332447 * t) + (0.0068612 * Math.pow(t, 2)) + (0.0041116 * Math.pow(t, 3)) + (-0.00037436 * Math.pow(t, 4)) + (0.0000121272 * Math.pow(t, 5)) + (-0.0000001699 * Math.pow(t, 6)) + (0.000000000875 * Math.pow(t, 7))
    }
    else if (year < 1900) {
      val t = y - 1860d
      deltaT = 7.62 + (0.5737 * t) + (-0.251754 * Math.pow(t, 2)) + (0.01680668 * Math.pow(t, 3)) + (-0.0004473624 * Math.pow(t, 4)) + (Math.pow(t, 5) / 233174)
    }
    else if (year < 1920) {
      val t = y - 1900
      deltaT = -2.79 + (1.494119 * t) + (-0.0598939 * Math.pow(t, 2)) + (0.0061966 * Math.pow(t, 3)) + (-0.000197 * Math.pow(t, 4))
    }
    else if (year < 1941) {
      val t = y - 1920
      deltaT = 21.20 + (0.84493 * t) + (-0.076100 * Math.pow(t, 2)) + (0.0020936 * Math.pow(t, 3))
    }
    else if (year < 1961) {
      val t = y - 1950
      deltaT = 29.07 + (0.407 * t) + (Math.pow(t, 2) / -233) + (Math.pow(t, 3) / 2547)
    }
    else if (year < 1986) {
      val t = y - 1975
      deltaT = 45.45 + (1.067 * t) + (Math.pow(t, 2) / -260) + (Math.pow(t, 3) / -718)
    }
    else if (year < 2005) {
      val t = y - 2000
      deltaT = 63.86 + (0.3345 * t) + (-0.060374 * Math.pow(t, 2)) + (0.0017275 * Math.pow(t, 3)) + (0.000651814 * Math.pow(t, 4)) + (0.00002373599 * Math.pow(t, 5))
    }
    else if (year < 2050) {
      val t = y - 2000
      deltaT = 62.92 + (0.32217 * t) + (0.005589 * Math.pow(t, 2))
    }
    else if (year < 2150) deltaT = -20 + (32 * Math.pow((y - 1820) / 100, 2)) + (-0.5628 * (2150 - y))
    else {
      val u = (y - 1820) / 100
      deltaT = -20 + (32 * Math.pow(u, 2))
    }
    deltaT
  }
}
