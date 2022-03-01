import http.client.HTTPClient
import org.json.JSONObject

/**
 * Calculate heading, pitch and roll
 * based on magnetometer data read from a REST Request.
 */
def calculate = { Double x, Double y, Double z ->
    def heading = Math.toDegrees(Math.atan2(y, x))
    while (heading < 0) {
        heading += 360f
    }
    def pitch = Math.toDegrees(Math.atan2(y, z))
    def roll = Math.toDegrees(Math.atan2(x, z))
    return [ heading, pitch, roll ]
}

def restUrl = System.getProperty("rest.url", "http://192.168.42.9:8080/lis3mdl/cache")

def keepGoing = true

addShutdownHook {
    keepGoing = false
    println 'Stopped the script.'
}

while (keepGoing) {
    try {
        def str = HTTPClient.doGet( restUrl, null)
        if ("true" == System.getProperty("verbose")) {
            println str
        }
        JSONObject magData = new JSONObject(str)
        def magX = magData.getDouble("x")
        def magY = magData.getDouble("y")
        def magZ = magData.getDouble("z")
        def data = calculate(magX, magY, magZ)
        println(String.format("Heading: %f Pitch: %f, Roll: %f", data[0], data[1], data[2]))
    } catch (Exception ex) {
        ex.printStackTrace()
    }
}

println "See you!"
