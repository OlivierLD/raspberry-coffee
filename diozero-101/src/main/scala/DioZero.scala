import com.diozero.devices.Button
import com.diozero.devices.LED
import com.diozero.util.SleepUtil

import scala.util.Using

object DioZero {

  val ledPin = 24 // 18 does not work (it's an IN pin...), 24 does.
  val buttonPin = 12 // Seems OK

  def main(args: Array[String]): Unit = {
    println("Starting diozero test.")

    Using (new LED(ledPin)) { led =>
      printf("Turning led %d ON\n", ledPin)
      led.on()
      SleepUtil.sleepSeconds(1)
      printf("Turning led %d OFF\n", ledPin)
      led.off()
      SleepUtil.sleepSeconds(1)
      printf("Toggling Led %d\n", ledPin)
      led.toggle()
    }

    println("Button test... (20s)")

    try {
      val button = new Button(buttonPin)
      val led = new LED(ledPin)
      try { // TODO See if we can use the syntax above (https://www.baeldung.com/scala/try-with-resources)
        button.whenPressed((nanoTime: Long) => led.on())
        button.whenReleased((nanoTime: Long) => led.off())
        SleepUtil.sleepSeconds(20)
      } finally {
        if (button != null) button.close()
        if (led != null) led.close()
      }
    }

    println("Bye!")
  }
}