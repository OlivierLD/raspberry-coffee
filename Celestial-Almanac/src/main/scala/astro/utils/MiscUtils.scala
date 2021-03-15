package astro.utils

import java.text.DecimalFormat

object MiscUtils {
  def lpad(s: String, len: Int): String = lpad(s, len, " ")

  def lpad(s: String, len: Int, pad: String): String = {
    var str = s
    while ( {
      str.length < len
    }) str = pad + str
    str
  }

  val HTML = 0
  val SHELL = 1
  val SWING = 2
  val NO_DEG = 3
  val UNICODE = 5
  val DEFAULT_DEG = 4

  val NONE = 0
  val NS = 1
  val EW = 2

  val LEADING_SIGN = 0
  val TRAILING_SIGN = 1

  val DEGREE_SYMBOL = "\u00b0"

  def decToSex(v: Double): String = decToSex(v, SHELL)

  def decToSex(v: Double, display: Int): String = decToSex(v, HTML, display)

  def decToSex(v: Double, output: Int, displayType: Int): String = decToSex(v, output, displayType, TRAILING_SIGN)

  def decToSex(v: Double, output: Int, displayType: Int, truncMinute: Boolean): String = decToSex(v, output, displayType, TRAILING_SIGN, truncMinute)

  def decToSex(v: Double, output: Int, displayType: Int, signPosition: Int): String = decToSex(v, output, displayType, signPosition, false)

  def decToSex(v: Double, output: Int, displayType: Int, signPosition: Int, truncMinute: Boolean): String = {
    var s = ""
    val absVal = Math.abs(v)
    val intValue = Math.floor(absVal)
    var dec = absVal - intValue
    val i = intValue.toInt
    dec *= 60D
    val df = if (truncMinute) new DecimalFormat("00")
    else new DecimalFormat("00.00")
    if (output == HTML) s = Integer.toString(i) + "&deg;" + df.format(dec) + "'"
    else if (output == SWING) s = Integer.toString(i) + '\u00b0' + df.format(dec) + "'"
    else if (output == UNICODE) s = Integer.toString(i) + '\u00b0' + df.format(dec) + "'"
    else if (output == NO_DEG) s = Integer.toString(i) + ' ' + df.format(dec) + "'"
    else s = Integer.toString(i) + '\u00ba' + df.format(dec) + "'"
    if (v < 0.0D) displayType match {
      case NONE =>
        s = "-" + s

      case NS =>
        s = if (signPosition == TRAILING_SIGN) s + "S"
        else "S " + lpad(s, if ((output == HTML)) 13
        else 9)

      case EW =>
        s = if (signPosition == TRAILING_SIGN) s + "W"
        else "W " + lpad(s, if ((output == HTML)) 14
        else 10)

    }
    else displayType match {
      case NONE =>
        s = " " + s

      case NS =>
        s = if (signPosition == TRAILING_SIGN) s + "N"
        else "N " + lpad(s, if ((output == HTML)) 13
        else 9)

      case EW =>
        s = if (signPosition == TRAILING_SIGN) s + "E"
        else "E " + lpad(s, if ((output == HTML)) 14
        else 10)

    }
    s
  }

  /**
    *
    * @param value in seconds of arc
    * @return
    */
  def renderSdHp(value: Double) = {
    var formatted = ""
    val minutes = Math.floor(value / 60d).toInt
    val seconds = value - (minutes * 60)
    if (minutes > 0) formatted = minutes + "'" + seconds.formatted("%05.02f") + "\""
    else formatted = seconds.formatted("%05.02f") + "\""
    formatted
  }

  def renderRA(ra: Double) = {
    var formatted = ""
    val t = ra / 15d
    val raH = Math.floor(t).toInt
    val raMin = Math.floor(60 * (t - raH)).toInt
    val raSec = 10d * (3600d * ((t - raH) - (raMin / 60d))).round.toFloat / 10
    formatted = raH.formatted("%02d") + "h " + raMin.formatted("%02d") + "m " + raSec.formatted("%05.02f") + "s"
    formatted
  }

  /**
    *
    * @param eot in minutes (of time)
    * @return
    */
  def renderEoT(eot: Double) = {
    var formatted = ""
    val dEoT = Math.abs(eot)
    val eotMin = Math.floor(dEoT).toInt
    val eotSec = 600 * (dEoT - eotMin).round / 10d
    if (eotMin == 0) { // Less than 1 minute
      formatted = if (eot > 0) "+"
      else "-" + " " + eotSec.formatted("%04.01f") + "s"
    }
    else formatted = if (eot > 0) "+"
    else "-" + " " + eotMin.formatted("%02d") + "m " + eotSec.formatted("%04.01f") + "s"
    formatted
  }

}
