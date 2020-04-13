//
// Manual interaction with GPIO pins.
// To be started in the Scala REPL.
//

// Load the PI4J resources
:require /opt/pi4j/lib/pi4j-core.jar

import com.pi4j.io.gpio._

val gpio = GpioFactory getInstance

// provision gpio pin #00 & #02 as an output pin and turn on
val pin00 = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_00, "RedLed", PinState.HIGH)
val pin02 = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_02, "GreenLed", PinState.HIGH)

Thread sleep 1000
println("Blinking red fast...")

(0 to 100).foreach(x => {
  pin00 toggle()
  Thread sleep 50 
} )

println("Blinking green fast...")
(0 to 100).foreach(x => {
  pin02 toggle()
  Thread sleep 50
})

pin00 low()
pin02 low()
Thread sleep 1000
pin00 high()
System.out.println("Blinking red & green fast...")
(0 to 100).foreach(x => {
  pin00 toggle()
  pin02 toggle()
  Thread sleep 50
})

pin00 high()
pin02 low()
Thread sleep 100
pin02 high()
Thread sleep 1000

pin00 low()
pin02 low()

Thread sleep 100

pin00 pulse(500, true) // set second argument to 'true' use a blocking call

pin02 pulse(500, true)

Thread sleep 100

pin00 pulse(500, false)
Thread sleep 100
pin02 pulse(500, false)
Thread sleep 1000

// All on
pin00 high()
pin02 high()
Thread sleep 1000

pin00 low()
pin02 low()

// stop all GPIO activity/threads by shutting down the GPIO controller
// (this method will forcefully shutdown all GPIO monitoring threads and scheduled tasks)
gpio shutdown()
