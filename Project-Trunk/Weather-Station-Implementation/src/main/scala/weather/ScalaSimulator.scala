package weather

import org.json.JSONObject
import utils.WeatherUtil
import weatherstation.logger.LoggerInterface
import weatherstation.ws.HomeWeatherStation

import scala.language.postfixOps

object ScalaSimulator {

  private var go = true
  private var loggers:List[LoggerInterface] = null

  @throws[Exception]
  def main(args:Array[String]): Unit = {
    val loggerClassNames = System.getProperty("data.logger", null)
    if (loggerClassNames != null) {
      val loggerClasses = loggerClassNames.split(",")
      loggers = List.empty[LoggerInterface]
      for (loggerClassName <- loggerClasses) {
        if (loggerClassName.trim.length > 0) {
          try {
            val logClass = Class.forName(loggerClassName).asSubclass(classOf[LoggerInterface])
            loggers :+= logClass.newInstance
          } catch {
            case ex: Exception =>
              ex printStackTrace
          }
        }
      }
    }

    sys addShutdownHook {
      println("\nUser interrupted.")
      go = false
      if (loggers != null && !loggers.isEmpty)
        loggers.foreach((logger: LoggerInterface) => logger.close())
      println("Bye.")
    }

    var windSpeed = 0d
    var windGust = 0d
    var windDir = 0f
    var voltage = 0d
    var pressure = 101300d
    var humidity = 50d
    var temperature = 15d
    var rainamount = 0d
    var dew = 0d
    while (go) {
      val ws = generateRandomValue(windSpeed, 3, 0, 65)
      val wg = generateRandomValue(windGust, 5, 0, 65)
      val wd = generateRandomValue(windDir, 10, 0, 360).toFloat
      val mwd = HomeWeatherStation.getAverageWD(wd)
      val volts = generateRandomValue(voltage, 3, 0, 65)
      val temp = generateRandomValue(temperature, 2, -10, 50).toFloat
      val press = generateRandomValue(pressure, 100, 98000, 105000).toFloat
      val hum = generateRandomValue(humidity, 5, 0, 100).toFloat
      val rain = generateRandomValue(rainamount, 1, 0, 3).toFloat
      dew = WeatherUtil.dewPointTemperature(hum, temp).toFloat
      val windObj = new JSONObject
      windObj.put("dir", wd)
      windObj.put("avgdir", mwd)
      windObj.put("volts", volts)
      windObj.put("speed", ws)
      windObj.put("gust", wg)
      windObj.put("temp", temp)
      windObj.put("press", press)
      windObj.put("hum", hum)
      windObj.put("rain", rain)
      windObj.put("dew", dew)
      /*
       * Sample message: {
       *   "dir": 350.0,
       *   "avgdir": 345.67,
       *   "volts": 3.4567,
       *   "speed": 12.345,
       *   "gust": 13.456,
       *   "press": 101300.00,
       *   "temp": 18.34,
       *   "rain": 0.1,
       *   "hum": 58.5,
       *   "dew": 9.87 }
       */
      if (loggers != null) {
        loggers.foreach((logger: LoggerInterface) => {
          try {
            if ("true" == System.getProperty("simulator.verbose", "false"))
              println(s">> Pushing ${windObj}")
            logger.pushMessage(windObj)
          } catch {
            case ex: Exception =>
              ex.printStackTrace()
          }
        })
      } else {
        println(s">> Pushing ${windObj}")
      }
      windSpeed = ws
      windGust = wg
      windDir = wd
      voltage = volts
      pressure = press
      temperature = temp
      humidity = hum
      rainamount = rain
      try
        Thread.sleep(1000)
      catch {
        case ex: Exception =>
          ex.printStackTrace()
      }
    }
    println("Done.")
  }

  private def generateRandomValue(
                 from: Double,
                 diffRange: Double,
                 min: Double,
                 max: Double): Double = {
    var value = from
    var go = true
    while (go) {
      var rnd = 0.5 - Math.random
      rnd *= diffRange
      if (value + rnd >= min && value + rnd <= max) {
        value += rnd
        go = false
      }
    }
    value
  }

}
