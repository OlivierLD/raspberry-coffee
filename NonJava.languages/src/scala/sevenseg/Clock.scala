package sevenseg

import java.io.IOException
import java.util.{Calendar, GregorianCalendar, TimeZone}

import sevensegdisplay.SevenSegment

object Clock {
  @throws[IOException]
  def main(args: Array[String]) {
    val segment: SevenSegment = new SevenSegment(0x70, true)
    println("Press CTRL+C to exit")

    sys addShutdownHook {
      try {
        segment.clear()
        println("\nBye")
      } catch {
        case ioe: IOException => {
          ioe.printStackTrace()
        }
      }
    }

    // Continually update the time on a 4 char, 7-segment display
    while (true) {
      val now: Calendar = new GregorianCalendar(TimeZone.getTimeZone("America/Los_Angeles"))
      val hour: Int = now.get(Calendar.HOUR_OF_DAY)
      val minute: Int = now.get(Calendar.MINUTE)
      val second: Int = now.get(Calendar.SECOND)
      // Set hours
      segment.writeDigit(0, hour / 10) // Tens
      segment.writeDigit(1, hour % 10) // Ones
      // Set minutes
      segment.writeDigit(3, minute / 10)
      segment.writeDigit(4, minute % 10)
      // Toggle colon
      segment.setColon(second % 2 != 0) // Toggle colon at 1Hz
      // Wait one second
      try {
        Thread.sleep(1000L)
      } catch {
        case ie: InterruptedException => {
        }
      }
    }
  }
}
