package rest

import http.client.HTTPClient
import org.json.JSONObject

/**
  * Read raw mag data from an HTTP Server
  * That one happens to be in Python, polling an LIS3MDL breakout board
  *
  * TODO Introduce calibration
  */
object LIS3MDLReader {

  def calculate(magX: Double, magY: Double, magZ: Double): (Double, Double, Double) = {
    var heading = Math.toDegrees(Math.atan2(magY, magX))
    while (heading < 0)
      heading += 360f
    val pitch = Math.toDegrees(Math.atan2(magY, magZ))
    val roll = Math.toDegrees(Math.atan2(magX, magZ))
    (heading, pitch, roll)
  }

  /**
    * System variables:
    * -Drest.url defaulted to "http://192.168.42.9:8080/lis3mdl/cache"
    * -Dverbose default false
    *
    * @param args Unused
    */
  def main(args: Array[String]): Unit = {
    var keepLooping = true
    println("Ctrl+C to stop")
    sys addShutdownHook {
      println("Stopping")
      keepLooping = false
      println("Bye.")
    }

    val restUrl = System.getProperty("rest.url", "http://192.168.42.9:8080/lis3mdl/cache")
    while (keepLooping) {
      try {
        val str = HTTPClient.doGet(restUrl, null)
        if ("true" == System.getProperty("verbose"))
          println(str)
        val magData = new JSONObject(str)
        val magX = magData.getDouble("x")
        val magY = magData.getDouble("y")
        val magZ = magData.getDouble("z")
        val (heading, pitch, roll) = LIS3MDLReader.calculate(magX, magY, magZ)
        println(s"Heading: ${heading} Pitch: ${pitch}, Roll: ${roll}")
      } catch {
        case ex: Exception =>
          ex.printStackTrace()
      }
    }
  }
}
