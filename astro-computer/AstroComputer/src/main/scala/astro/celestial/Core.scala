package astro.celestial

import astro.utils.MathUtils

object Core {
  /**
    * @param year year
    * @param month 1 - Jan, 2 - Feb, etc.
    * @param day day of month
    * @param hour hour of day [0..23]
    * @param minute [0..59]
    * @param second [0..59]
    * @param deltaT As previously calculated
    * @return
    */
  def julianDate(year: Int, month: Int, day: Int, hour: Int, minute: Int, second: Float, deltaT: Double, context: AstroContext): Unit = { //var year, month, day, hour, minute, second, Context.dayfraction, ly=0;
    context.dayfraction = (hour.toDouble + (minute.toDouble / 60D) + (second.toDouble / 3600D)) / 24D
    // Calculating Julian date, century, and millennium
    //Julian daContext.TE (UT1)
    var _month = month
    var _year = year
    if (_month <= 2) {
      _year -= 1
      _month += 12
    }
    val A = Math.floor(_year / 100D)
    val B = 2 - A + Math.floor(A / 4D)
    context.JD0h = Math.floor(365.25 * (_year + 4716D)) + Math.floor(30.6001 * (_month + 1)) + day + B - 1524.5
    context.JD = context.JD0h + context.dayfraction
    //Julian centuries (UT1) from 2000 January 0.5
    context.T = (context.JD - 2451545D) / 36525D
    context.T2 = context.T * context.T
    context.T3 = context.T * context.T2
    context.T4 = context.T * context.T3
    context.T5 = context.T * context.T4
    //Julian ephemeris daContext.TE (TDT)
    context.JDE = context.JD + deltaT / 86400D
    //Julian centuries (TDT) from 2000 January 0.5
    context.TE = (context.JDE - 2451545D) / 36525D
    context.TE2 = context.TE * context.TE
    context.TE3 = context.TE * context.TE2
    context.TE4 = context.TE * context.TE3
    context.TE5 = context.TE * context.TE4
    //Julian millenniums (TDT) from 2000 January 0.5
    context.Tau = 0.1 * context.TE
    context.Tau2 = context.Tau * context.Tau
    context.Tau3 = context.Tau * context.Tau2
    context.Tau4 = context.Tau * context.Tau3
    context.Tau5 = context.Tau * context.Tau4
  }

  //GHA Aries, GAST, GMST, equation of the equinoxes

  def aries(context: AstroContext): Unit = { //Mean GHA Aries
    val GHAAmean = MathUtils.trunc(280.46061837 + 360.98564736629 * (context.JD - 2451545D) + 0.000387933 * context.T2 - context.T3 / 38710000D)
    //GMST
    //  SidTm = OutSidTime(GHAAmean);
    //True GHA Aries
    context.GHAAtrue = MathUtils.trunc(GHAAmean + context.delta_psi * MathUtils.cosD(context.eps))
    //GAST
    //  SidTa = OutSidTime(GHAAtrue);
    //Equation of the equinoxes
    val EoE: Double = 240 * context.delta_psi * MathUtils.cosD(context.eps)
    var EoEout = (Math.round(1000 * EoE) / 1000D).toString
    EoEout = " " + EoEout + "s"
  }


  //Calculations for the Sun
  def sun(context: AstroContext): Unit = { //Mean longitude of the Sun
    context.Lsun_mean = MathUtils.trunc(280.4664567 + 360007.6982779 * context.Tau + 0.03032028 * context.Tau2 + context.Tau3 / 49931D - context.Tau4 / 15299D - context.Tau5 / 1988000D)
    //Heliocentric longitude of the Earth
    context.Le = Earth.lEarth(context.Tau)
    //Geocentric longitude of the Sun
    context.Lsun_true = MathUtils.trunc(context.Le + 180 - 0.000025)
    //Heliocentric latitude of Earth
    context.Be = Earth.bEarth(context.Tau)
    //Geocentric latitude of the Sun
    context.beta = MathUtils.trunc(-context.Be)
    //Corrections
    val Lsun_prime = MathUtils.trunc(context.Le + 180 - 1.397 * context.TE - 0.00031 * context.TE2)
    context.beta = context.beta + 0.000011 * (MathUtils.cosD(Lsun_prime) - MathUtils.sinD(Lsun_prime))
    //Distance Earth-Sun
    context.Re = Earth.rEarth(context.Tau)
    context.dES = 149597870.691 * context.Re
    //Apparent longitude of the Sun
    context.lambda_sun = MathUtils.trunc(context.Lsun_true + context.delta_psi - 0.005691611 / context.Re)
    //Right ascension of the Sun, apparent
    context.RAsun = Math.toDegrees(MathUtils.trunc2(Math.atan2(MathUtils.sinD(context.lambda_sun) * MathUtils.cosD(context.eps) - MathUtils.tanD(context.beta) * MathUtils.sinD(context.eps), MathUtils.cosD(context.lambda_sun))))
    //Declination of the Sun, apparent
    context.DECsun = Math.toDegrees(Math.asin(MathUtils.sinD(context.beta) * MathUtils.cosD(context.eps) + MathUtils.cosD(context.beta) * MathUtils.sinD(context.eps) * MathUtils.sinD(context.lambda_sun)))
    //GHA of the Sun
    context.GHAsun = MathUtils.trunc(context.GHAAtrue - context.RAsun)
    //Semidiameter of the Sun
    context.SDsun = 959.63 / context.Re
    //Horizontal parallax of the Sun
    context.HPsun = 8.794 / context.Re
    //Equation of time
    //EOT = 4*(Lsun_mean-0.0057183-0.0008-RAsun+delta_psi*cosd(eps));
    context.EoT = 4 * context.GHAsun + 720 - 1440 * context.dayfraction
    if (context.EoT > 20) context.EoT -= 1440
    if (context.EoT > 20) context.EoT -= 1440
    if (context.EoT < -20) context.EoT += 1440
  }

  def polaris(context: AstroContext): Unit = { //Equatorial coordinates of Polaris at 2000.0 (mean equinox and equator 2000.0)
    val RApol0 = 37.95293333
    val DECpol0 = 89.26408889
    //Proper motion per year
    val dRApol = 2.98155 / 3600D
    val dDECpol = -0.0152 / 3600D
    //Equatorial coordinates at Julian DaContext.TE T (mean equinox and equator 2000.0)
    val RApol1 = RApol0 + 100 * context.TE * dRApol
    val DECpol1 = DECpol0 + 100 * context.TE * dDECpol
    //Mean obliquity of ecliptic at 2000.0 in degrees
    // double eps0_2000 = 23.439291111;
    //Transformation to ecliptic coordinates in radians (mean equinox and equator 2000.0)
    val lambdapol1 = Math.atan2(MathUtils.sinD(RApol1) * MathUtils.cosD(context.EPS0_2000) + MathUtils.tanD(DECpol1) * MathUtils.sinD(context.EPS0_2000), MathUtils.cosD(RApol1))
    val betapol1 = Math.asin(MathUtils.sinD(DECpol1) * MathUtils.cosD(context.EPS0_2000) - MathUtils.cosD(DECpol1) * MathUtils.sinD(context.EPS0_2000) * MathUtils.sinD(RApol1))
    //Precession
    val eta = Math.toRadians(47.0029 * context.TE - 0.03302 * context.TE2 + 0.00006 * context.TE3) / 3600D
    val PI0 = Math.toRadians(174.876384 - (869.8089 * context.TE + 0.03536 * context.TE2) / 3600D)
    val p0 = Math.toRadians(5029.0966 * context.TE + 1.11113 * context.TE2 - 0.0000006 * context.TE3) / 3600D
    val A1 = Math.cos(eta) * Math.cos(betapol1) * Math.sin(PI0 - lambdapol1) - Math.sin(eta) * Math.sin(betapol1)
    val B1 = Math.cos(betapol1) * Math.cos(PI0 - lambdapol1)
    val C1 = Math.cos(eta) * Math.sin(betapol1) + Math.sin(eta) * Math.cos(betapol1) * Math.sin(PI0 - lambdapol1)
    var lambdapol2 = p0 + PI0 - Math.atan2(A1, B1)
    var betapol2 = Math.asin(C1)
    //Nutation in longitude
    lambdapol2 += Math.toRadians(context.delta_psi)
    //Aberration
    val dlambdapol = (context.e * context.kappa * Math.cos(context.pi0 - lambdapol2) - context.kappa * Math.cos(Math.toRadians(context.Lsun_true) - lambdapol2)) / Math.cos(betapol2)
    val dbetapol = -context.kappa * Math.sin(betapol2) * (Math.sin(Math.toRadians(context.Lsun_true) - lambdapol2) - context.e * Math.sin(context.pi0 - lambdapol2))
    lambdapol2 += dlambdapol
    betapol2 += dbetapol
    //Transformation back to equatorial coordinates in radians
    val RApol2 = Math.atan2(Math.sin(lambdapol2) * MathUtils.cosD(context.eps) - Math.tan(betapol2) * MathUtils.sinD(context.eps), Math.cos(lambdapol2))
    val DECpol2 = Math.asin(Math.sin(betapol2) * MathUtils.cosD(context.eps) + Math.cos(betapol2) * MathUtils.sinD(context.eps) * Math.sin(lambdapol2))
    //Finals
    context.GHApol = context.GHAAtrue - Math.toDegrees(RApol2)
    context.GHApol = MathUtils.trunc(context.GHApol)
    context.RApol = Math.toDegrees(RApol2)
    context.DECpol = Math.toDegrees(DECpol2)
  }

  def moonPhase(context: AstroContext): String = {
    var quarter = ""
    var x = context.lambdaMapp - context.lambda_sun
    x = MathUtils.trunc(x)
    x = Math.round(10 * x).toDouble / 10
    if (x == 0) quarter = " New"
    if (x > 0 && x < 90) quarter = " +cre"
    if (x == 90) quarter = " FQ"
    if (x > 90 && x < 180) quarter = " +gib"
    if (x == 180) quarter = " Full"
    if (x > 180 && x < 270) quarter = " -gib"
    if (x == 270) quarter = " LQ"
    if (x > 270 && x < 360) quarter = " -cre"
    quarter
  }

  def weekDay(context: AstroContext): Int = ((context.JD0h + 1.5) - 7 * Math.floor((context.JD0h + 1.5) / 7)).toInt

  // For tests
  def main(args: Array[String]): Unit = {
    val context = new AstroContext
    julianDate(2009, 4, 20, 0, 0, 0f, 65.5, context)
    System.out.println("DayFraction:" + context.dayfraction)
    julianDate(2009, 4, 20, 0, 10, 0f, 65.5, context)
    System.out.println("DayFraction:" + context.dayfraction)
    julianDate(2009, 4, 20, 0, 20, 0f, 65.5, context)
    System.out.println("DayFraction:" + context.dayfraction)
    julianDate(2009, 4, 20, 0, 30, 0f, 65.5, context)
    System.out.println("DayFraction:" + context.dayfraction)
    julianDate(2009, 4, 20, 0, 40, 0f, 65.5, context)
    System.out.println("DayFraction:" + context.dayfraction)
    julianDate(2009, 4, 20, 0, 50, 0f, 65.5, context)
    System.out.println("DayFraction:" + context.dayfraction)
  }
}