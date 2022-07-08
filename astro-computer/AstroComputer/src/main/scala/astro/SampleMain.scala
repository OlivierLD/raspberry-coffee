package astro

import java.text.SimpleDateFormat
import java.util.{Calendar, TimeZone}
import astro.celestial.LongTermAlmanac
import astro.utils.MiscUtils.{decToSex, lpad, renderEoT, renderRA, renderSdHp}
import astro.utils.{MiscUtils, TimeUtils}
import astro.celestial.Core.{moonPhase, weekDay}

object SampleMain {

  private val SDF_UTC = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss 'UTC'")
  SDF_UTC.setTimeZone(TimeZone.getTimeZone("Etc/UTC"))

  def main(args: Array[String]): Unit = {
    val now = args.exists(arg => arg.equals("--now"))
    val date = Calendar.getInstance(TimeZone.getTimeZone("Etc/UTC")) // Now
    if (!now) { // Hard coded date
      date.set(Calendar.YEAR, 2020)
      date.set(Calendar.MONTH, Calendar.MARCH)
      date.set(Calendar.DAY_OF_MONTH, 28)
      date.set(Calendar.HOUR_OF_DAY, 16) // and not just Calendar.HOUR !!!!
      date.set(Calendar.MINUTE, 50)
      date.set(Calendar.SECOND, 20)
    }
    System.out.println(String.format("Calculations for %s (%s)", SDF_UTC.format(date.getTime), if (now) "now" else "not now"))
    // Take time here, JVM is loaded, etc...
    val before = System.currentTimeMillis
    // Recalculate
    val deltaT = TimeUtils.getDeltaT(date.get(Calendar.YEAR), date.get(Calendar.MONTH) + 1)
    println(s"New deltaT: $deltaT")
    // All calculations here
    val result = LongTermAlmanac.calculate(
      date.get(Calendar.YEAR),
      date.get(Calendar.MONTH) + 1, // Jan: 1, Dec: 12.
      date.get(Calendar.DAY_OF_MONTH),
      date.get(Calendar.HOUR_OF_DAY),
      date.get(Calendar.MINUTE),
      date.get(Calendar.SECOND),
      deltaT)
    // Done with calculations, now display
    val after = System.currentTimeMillis
    println(s"Calculations done for ${SDF_UTC.format(date.getTime)}, in ${after - before} ms <<<")
    println(s"Sun:\t Decl: ${lpad(decToSex(result.DECsun, MiscUtils.SWING, MiscUtils.NS), 10, " ")}, " +
      s"GHA: ${lpad(decToSex(result.GHAsun, MiscUtils.SWING, MiscUtils.NONE), 11, " ")}, " +
      s"RA: ${renderRA(result.RAsun)}, " +
      s"SD: ${lpad(renderSdHp(result.SDsun), 9, " ")}, " +
      s"HP: ${lpad(renderSdHp(result.HPsun), 9, " ")}")
    println(s"Moon:\t Decl: ${lpad(decToSex(result.DECmoon, MiscUtils.SWING, MiscUtils.NS), 10, " ")}, " +
      s"GHA: ${lpad(decToSex(result.GHAmoon, MiscUtils.SWING, MiscUtils.NONE), 11, " ")}, " +
      s"RA: ${renderRA(result.RAmoon)}, " +
      s"SD: ${lpad(renderSdHp(result.SDmoon), 9, " ")}, " +
      s"HP: ${lpad(renderSdHp(result.HPmoon), 9, " ")}")
    println(s"Venus:\t Decl: ${lpad(decToSex(result.DECvenus, MiscUtils.SWING, MiscUtils.NS), 10, " ")}, " +
      s"GHA: ${lpad(decToSex(result.GHAvenus, MiscUtils.SWING, MiscUtils.NONE), 11, " ")}, " +
      s"RA: ${renderRA(result.RAvenus)}, " +
      s"SD: ${lpad(renderSdHp(result.SDvenus), 9, " ")}, " +
      s"HP: ${lpad(renderSdHp(result.HPvenus), 9, " ")}")
    println(s"Mars:\t Decl: ${lpad(decToSex(result.DECmars, MiscUtils.SWING, MiscUtils.NS), 10, " ")}, " +
      s"GHA: ${lpad(decToSex(result.GHAvenus, MiscUtils.SWING, MiscUtils.NONE), 11, " ")}, " +
      s"RA: ${renderRA(result.RAmars)}, " +
      s"SD: ${lpad(renderSdHp(result.SDmars), 9, " ")}, " +
      s"HP: ${lpad(renderSdHp(result.HPmars), 9, " ")}")
    println(s"Jupiter:\t Decl: ${lpad(decToSex(result.DECjupiter, MiscUtils.SWING, MiscUtils.NS), 10, " ")}, " +
      s"GHA: ${lpad(decToSex(result.GHAjupiter, MiscUtils.SWING, MiscUtils.NONE), 11, " ")}, " +
      s"RA: ${renderRA(result.RAjupiter)}, " +
      s"SD: ${lpad(renderSdHp(result.SDjupiter), 9, " ")}, " +
      s"HP: ${lpad(renderSdHp(result.HPjupiter), 9, " ")}")
    println(s"Saturn:\t Decl: ${lpad(decToSex(result.DECsaturn, MiscUtils.SWING, MiscUtils.NS), 10, " ")}, " +
      s"GHA: ${lpad(decToSex(result.GHAsaturn, MiscUtils.SWING, MiscUtils.NONE), 11, " ")}, " +
      s"RA: ${renderRA(result.RAsaturn)}, " +
      s"SD: ${lpad(renderSdHp(result.SDsaturn), 9, " ")}, " +
      s"HP: ${lpad(renderSdHp(result.HPsaturn), 9, " ")}")
    println("")
    println(s"Polaris:\t Decl: ${lpad(decToSex(result.DECpol, MiscUtils.SWING, MiscUtils.NS), 10, " ")}, " +
      s"GHA: ${lpad(decToSex(result.GHApol, MiscUtils.SWING, MiscUtils.NONE), 11, " ")}, " +
      s"RA: ${renderRA(result.RApol)} ")
    println(s"Equation of Time: ${renderEoT(result.EoT)}")
    println(s"Lunar Distance: ${lpad(decToSex(result.LDist, MiscUtils.SWING, MiscUtils.NONE), 10, " ")}")
    println(s"Moon Phase: ${moonPhase(result)}")
    println(s"Day of Week: ${LongTermAlmanac.WEEK_DAYS(weekDay(result))}")
    System.out.println("Done with Scala!")
  }
}
