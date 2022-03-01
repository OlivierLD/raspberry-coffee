package restkt

import http.client.HTTPClient
import org.json.JSONObject
import kotlin.math.atan2

/**
 * This is an HTTP Client, using REST request to read data from a magnetometer.
 * The server connected to the magnetometer happens to be written in Python
 */
object KtMagReader {

    data class MagData(val heading: Double, val pitch: Double, val roll: Double)

    private fun calculate(magX: Double, magY: Double, magZ: Double): MagData {
        var heading = Math.toDegrees(atan2(magY, magX))
        while (heading < 0)
            heading += 360f
        val pitch = Math.toDegrees(atan2(magY, magZ))
        val roll = Math.toDegrees(atan2(magX, magZ))
        return MagData(heading, pitch, roll)
    }

    @JvmStatic
    fun main(args: Array<String>) {
        var keepLooping = true
        val restUrl = System.getProperty("rest.url", "http://192.168.42.9:8080/lis3mdl/cache")

        Runtime.getRuntime().addShutdownHook(object : Thread() {
            override fun run() {
                println("Gracefully shutting down...")
                keepLooping = false
                println("Gracefully shut !")
            }
        })

        while (keepLooping) {
            try {
                val str = HTTPClient.doGet(restUrl, null)
                if ("true" == System.getProperty("verbose"))
                    println(str)
                val magData = JSONObject(str)
                val magX = magData.getDouble("x")
                val magY = magData.getDouble("y")
                val magZ = magData.getDouble("z")
                val data = calculate(magX, magY, magZ)
                println("Heading:${data.heading} \u00b0, Pitch:${data.pitch} \u00b0, Roll:${data.roll} \u00b0")
            } catch (ex: Exception) {
                println(ex.toString())
            }
        }
    }
}
