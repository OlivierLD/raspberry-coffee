import com.diozero.devices.Button
import com.diozero.devices.LED
import com.diozero.util.SleepUtil

println "diozero groovy test"

// PINs are BCM numbers.
final int ledPin    = 24 // 18 does not work (it's an IN pin...), 24 does.
final int buttonPin = 12 // Seems OK

System.out.println("Starting diozero test.")
LED led = null
try {
    led = new LED(ledPin)
    System.out.printf("Turning led %d ON\n", ledPin)
    led.on()
    SleepUtil.sleepSeconds(1)
    System.out.printf("Turning led %d OFF\n", ledPin)
    led.off()
    SleepUtil.sleepSeconds(1)
    System.out.printf("Toggling Led %d\n", ledPin)
    led.toggle()
} finally {
    if (led != null) {
        led.close()
    }
}

System.out.println("Button test... (20s)")
Button button = new Button(buttonPin)
LED led2 = new LED(ledPin)
def pressedConsumer = { nanoTime -> led2.on() }
def releasedConsumer = { nanoTime -> led2.off() }
try {
    button.whenPressed(pressedConsumer)
    button.whenReleased(releasedConsumer)
    SleepUtil.sleepSeconds(20)
} finally {
    button.close()
    led2.close()
}

System.out.println("Bye!")
