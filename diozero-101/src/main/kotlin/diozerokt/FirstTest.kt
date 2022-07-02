package diozerokt

import com.diozero.devices.Button
import com.diozero.devices.LED
import com.diozero.util.SleepUtil

/*
 * GPIO Pin numbers, see https://www.diozero.com/api/gpio.html
 * PINs are BCM numbers.
 * java -cp ./build/libs/common-utils-1.0-all.jar utils.PinUtil
 */

const val ledPin = 24 // 18 does not work (it's an IN pin...), 24 does.
const val buttonPin = 12 // Seems OK

fun main(args: Array<String>) {
    println("Starting diozero test.")
    LED(ledPin).use { led ->  // PINs are BCM numbers.
        println("Turning led ${ledPin} ON")
        led.on()
        SleepUtil.sleepSeconds(1)
        println("Turning led ${ledPin} OFF")
        led.off()
        SleepUtil.sleepSeconds(1)
        println("Toggling Led ${ledPin}")
        led.toggle()
    }
    println("Button test... (20s)")
    Button(buttonPin).use { button ->
        LED(ledPin).use { led ->  // With resources, nice !
            button.whenPressed { nanoTime -> led.on() }
            button.whenReleased { nanoTime -> led.off() }
            SleepUtil.sleepSeconds(20)
        }
    }
    println("Bye!")
}
