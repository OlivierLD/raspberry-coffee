package fromscala

import sensors.sth10._

object ScalaMain {

  def delay(t: Int): Unit = {
    try {
      Thread sleep t * 1000
    } catch {
      case ie: InterruptedException =>
        ie printStackTrace
    }
  }

  // Read the sensor in a loop
  def main(args: Array[String]) {

    val probe = new STH10Driver
    var go = true

    while(go) {
      println (s"Temperature ${ probe readHumidity }\272C, Humidity ${ probe readHumidity }%")
      delay(1)
    }

    sys addShutdownHook {
      println("\nUser interrupted.")
      go = false
      delay(1)
      println("Bye.")
    }
  }
}
