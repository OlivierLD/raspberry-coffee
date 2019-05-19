import i2c.sensor.BME280
import com.pi4j.system.SystemInfo
import java.text.NumberFormat
import java.text.DecimalFormat

class GroovyApp {
    void sensor () {
        println "==========\nNow running some RPi stuff"
        try {
            BME280 bme280 = new BME280()
            NumberFormat NF = new DecimalFormat("##00.00")
            float press = 0
            float temp  = 0
            float hum   = 0

            try {
                temp = bme280.readTemperature()
            } catch (ex) {
                println(ex.getMessage())
                ex.printStackTrace()
            }
            try {
                press = bme280.readPressure()
            } catch (ex) {
                println(ex.getMessage())
                ex.printStackTrace()
            }
            try {
                hum = bme280.readHumidity()
            } catch (ex) {
                println(ex.getMessage())
                ex.printStackTrace()
            }

            println("Temperature: " + NF.format(temp) + " C")
            println("Pressure   : " + NF.format(press / 100) + " hPa")
            println("Humidity   : " + NF.format(hum) + " %")
            // Bonus : CPU Temperature
            try {
                println("CPU Temperature   :  " + SystemInfo.getCpuTemperature())
                println("CPU Core Voltage  :  " + SystemInfo.getCpuVoltage())
            } catch (all) {
                all.printStackTrace()
            }
        } catch (all) {
            println ">>> Exception is a " + all.getClass().getName()
            if (all instanceof IOException) {
                if (all.getMessage().startsWith("Cannot run program")) {
                    println("You need to run this from a Raspberry Pi...")
                } else {
                    all printStackTrace
                }
            } else {
                all printStackTrace
            }
        }
    }
}
