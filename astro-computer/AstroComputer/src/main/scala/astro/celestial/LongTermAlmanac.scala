package astro.celestial

import java.util.{Calendar, GregorianCalendar, TimeZone}

object LongTermAlmanac {

  private var year = -1
  private var month = -1
  private var day = -1
  private var hour = -1
  private var minute = -1
  private var second = -1
  private var deltaT = 66.4749d // 2011. Overridden by deltaT system variable, or calculated on the fly.


  val WEEK_DAYS: Array[String] = Array("SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT")

  def setDateTime(y: Int, m: Int, d: Int, h: Int, mi: Int, s: Int): Unit = {
    year = y
    month = m
    day = d
    hour = h
    minute = mi
    second = s
  }

  def getCalculationDateTime: Calendar = {
    val calcDate:GregorianCalendar = new GregorianCalendar()   // .getInstance
    calcDate.set(Calendar.YEAR, year)
    calcDate.set(Calendar.MONTH, month - 1)
    calcDate.set(Calendar.DAY_OF_MONTH, day)
    calcDate.set(Calendar.HOUR_OF_DAY, hour)
    calcDate.set(Calendar.MINUTE, minute)
    calcDate.set(Calendar.SECOND, second)
    calcDate
  }

  /**
    * Time are UTC
    *
    * @param y  year
    * @param m  Month. Attention: Jan=1, Dec=12 !!!! Does NOT start with 0.
    * @param d  day
    * @param h  hour
    * @param mi minute
    * @param s  second
    * @return Phase in Degrees
    */
  def getMoonPhase(y: Int, m: Int, d: Int, h: Int, mi: Int, s: Int, context: AstroContext): Double = {
    var phase: Double = 0
    year = y
    month = m
    day = d
    hour = h
    minute = mi
    second = s
    calculate()
    phase = context.lambdaMapp - context.lambda_sun
    while ( {
      phase < 0d
    }) phase += 360d
    phase
  }

  /**
    * Assume that calculate has been invoked already
    *
    * @return
    */
  def getMoonPhase(context: AstroContext): Double = {
    var phase = context.lambdaMapp - context.lambda_sun
    while ( {
      phase < 0d
    }) phase += 360d
    phase
  }

  /**
    * @param y  Year, like 2019
    * @param m  Month, [1..12]                   <- !!! Unlike Java's Calendar, which is zero-based
    * @param d  Day of month [1..28, 29, 30, 31]
    * @param h  Hour of the day [0..23]
    * @param mi Minutes [0..59]
    * @param s  Seconds [0..59], no milli-sec.
    */
  def calculate(y: Int, m: Int, d: Int, h: Int, mi: Int, s: Int, deltaT: Double): AstroContext = {
    this.deltaT = deltaT
    setDateTime(y, m, d, h, mi, s)
    calculate()
  }

  def calculate(): AstroContext = {
    if (year == -1 && month == -1 && day == -1 && hour == -1 && minute == -1 && second == -1) { // Then use current system date
      if ("true" == System.getProperty("astro.verbose"))
        System.out.println("Using System Time")
      val date = Calendar.getInstance(TimeZone.getTimeZone("Etc/UTC")) // Now
      setDateTime(date.get(Calendar.YEAR), date.get(Calendar.MONTH) + 1, date.get(Calendar.DAY_OF_MONTH), date.get(Calendar.HOUR_OF_DAY), // and not just HOUR !!!!
        date.get(Calendar.MINUTE), date.get(Calendar.SECOND))
    }
    val context = new AstroContext
    //		System.out.println(String.format("Using DeltaT: %f", deltaT));
    Core.julianDate(year, month, day, hour, minute, second.toFloat, deltaT, context)
    Anomalies.nutation(context)
    Anomalies.aberration(context)
    Core.aries(context)
    Core.sun(context)
    Moon.compute(context)
    Venus.compute(context)
    Mars.compute(context)
    Jupiter.compute(context)
    Saturn.compute(context)
    Core.polaris(context)
//    moonPhase = Core.moonPhase(context)
    context.moonPhase = getMoonPhase(context)

    context
  }

}