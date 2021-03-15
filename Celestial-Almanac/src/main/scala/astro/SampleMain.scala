package astro

import java.text.SimpleDateFormat
import java.util
import java.util.{Calendar, TimeZone}
import astro.celestial.LongTermAlmanac
import astro.utils.MiscUtils.{decToSex, lpad, renderRA, renderSdHp}
import astro.utils.{MiscUtils, TimeUtils}

object SampleMain {

  private val SDF_UTC = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss 'UTC'")
  SDF_UTC.setTimeZone(TimeZone.getTimeZone("Etc/UTC"))

  def main(args: Array[String]): Unit = {
    val now = util.Arrays.stream(args).filter((arg: String) => "--now" == arg).findFirst.isPresent
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
    // Recalculate
    val deltaT = TimeUtils.getDeltaT(date.get(Calendar.YEAR), date.get(Calendar.MONTH) + 1)
    println(s"New deltaT: ${deltaT}")
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
    println(s"Calculations done for ${SDF_UTC.format(date.getTime)}")
    println(s"Sun:\t Decl: ${lpad(decToSex(result.DECsun, MiscUtils.SWING, MiscUtils.NS), 10, " ")}, GHA: ${lpad(decToSex(result.GHAsun, MiscUtils.SWING, MiscUtils.NONE), 11, " ")}, RA: ${renderRA(result.RAsun)}, SD: ${lpad(renderSdHp(result.SDsun), 9, " ")}, HP: ${lpad(renderSdHp(result.HPsun), 9, " ")}")
    println(s"Moon:\t Decl: ${lpad(decToSex(result.DECmoon, MiscUtils.SWING, MiscUtils.NS), 10, " ")}, GHA: ${lpad(decToSex(result.GHAmoon, MiscUtils.SWING, MiscUtils.NONE), 11, " ")}, RA: ${renderRA(result.RAmoon)}, SD: ${lpad(renderSdHp(result.SDmoon), 9, " ")}, HP: ${lpad(renderSdHp(result.HPmoon), 9, " ")}")
//    System.out.println(String.format("\tMoon phase: %s, %s", decToSex(AstroComputer.getMoonPhase, SWING, NONE), AstroComputer.getMoonPhaseStr))
//    System.out.println(String.format("Venus data:\tDecl.: %s, GHA: %s, RA: %s, sd: %s, hp: %s", lpad(decToSex(AstroComputer.getVenusDecl, SWING, NS), 10, " "), lpad(decToSex(AstroComputer.getVenusGHA, SWING, NONE), 11, " "), renderRA(AstroComputer.getVenusRA), lpad(renderSdHp(AstroComputer.getVenusSd), 9, " "), lpad(renderSdHp(AstroComputer.getVenusHp), 9, " ")))
//    System.out.println(String.format("Mars data:\tDecl.: %s, GHA: %s, RA: %s, sd: %s, hp: %s", lpad(decToSex(AstroComputer.getMarsDecl, SWING, NS), 10, " "), lpad(decToSex(AstroComputer.getMarsGHA, SWING, NONE), 11, " "), renderRA(AstroComputer.getMarsRA), lpad(renderSdHp(AstroComputer.getMarsSd), 9, " "), lpad(renderSdHp(AstroComputer.getMarsHp), 9, " ")))
//    System.out.println(String.format("Jupiter data:\tDecl.: %s, GHA: %s, RA: %s, sd: %s, hp: %s", lpad(decToSex(AstroComputer.getJupiterDecl, SWING, NS), 10, " "), lpad(decToSex(AstroComputer.getJupiterGHA, SWING, NONE), 11, " "), renderRA(AstroComputer.getJupiterRA), lpad(renderSdHp(AstroComputer.getJupiterSd), 9, " "), lpad(renderSdHp(AstroComputer.getJupiterHp), 9, " ")))
//    System.out.println(String.format("Saturn data:\tDecl.: %s, GHA: %s, RA: %s, sd: %s, hp: %s", lpad(decToSex(AstroComputer.getSaturnDecl, SWING, NS), 10, " "), lpad(decToSex(AstroComputer.getSaturnGHA, SWING, NONE), 11, " "), renderRA(AstroComputer.getSaturnRA), lpad(renderSdHp(AstroComputer.getSaturnSd), 9, " "), lpad(renderSdHp(AstroComputer.getSaturnHp), 9, " ")))
//    System.out.println()
//    System.out.println(String.format("Polaris data:\tDecl.: %s, GHA: %s, RA: %s", lpad(decToSex(AstroComputer.getPolarisDecl, SWING, NS), 10, " "), lpad(decToSex(AstroComputer.getPolarisGHA, SWING, NONE), 11, " "), renderRA(AstroComputer.getPolarisRA)))
//    System.out.println(String.format("Equation of time: %s", renderEoT(AstroComputer.getEoT)))
//    System.out.println(String.format("Lunar Distance: %s", lpad(decToSex(AstroComputer.getLDist, SWING, NONE), 10, " ")))
//    System.out.println(String.format("Day of Week: %s", AstroComputer.getWeekDay)) //     dow = WEEK_DAYS(Core.weekDay(context))
    System.out.println("Done with Scala!")
  }

}
