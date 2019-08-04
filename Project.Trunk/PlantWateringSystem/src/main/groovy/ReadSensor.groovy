import sensors.sth10.STH10Driver

import java.text.DecimalFormat
import java.text.NumberFormat

// Just an example

println "==================\nSTH10 from Groovy\n=================="

try {
    sth10 = new STH10Driver()
    NumberFormat NF = new DecimalFormat("##00.00")

    temp = sth10.readTemperature()
    hum = sth10.readHumidity(temp)

    println("Temperature: " + NF.format(temp) + " C")
    println("Humidity   : " + NF.format(hum) + " %")

} catch (all) {
    println ">>> Exception is a " + all.getClass().getName()
    if (all instanceof IOException) {
        if (all.getMessage().startsWith("Cannot run program")) {
            println("You need to run this from a Raspberry Pi...")
        } else {
            all printStackTrace
        }
    } else {
        println "Opps:" + all
//      all printStackTrace
    }
}

