package fromscala

import sensors.sth10._

/**
  * Just an example...
  */
object ScalaMain {

  def delay(t: Int): Unit = {
    try {
      Thread sleep t * 1000
    } catch {
      case ie: InterruptedException =>
        ie.printStackTrace
    }
  }

  // Read the sensor in a loop
  def main(args: Array[String]) {

    val probe = new STH10Driver
    if (probe.isSimulating) {
      println("Will simulate the probe.")
      probe.setSimulators(() => 20.0, () => 50.0)
    }
    var go = true

    while (go) {
      println (s"Temperature ${ probe.readTemperature }\u00b0C, Humidity ${ probe.readHumidity }%")
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
