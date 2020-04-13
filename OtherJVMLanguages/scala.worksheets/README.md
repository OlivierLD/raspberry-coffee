## Interacting manually with the GPIO pins

Scala comes with a REPL (Read Evaluate Print Loop). It can be seen as a Scala console...

It executes the Scala command you enter from the keybord, dynamically, without having to compile or anything.
You type, it runs.

For example, you setup the following components:
<!-- ![Red Green](./red.green_bb.png) -->
<img src="./red.green_bb.png" width="345" height="564">

And from a Terminal running on the Raspberry Pi, you can drive the leds plugged on the breadboard.

```bash
$> sudo scala
Welcome to Scala 2.11.8 (Java HotSpot(TM) 64-Bit Server VM, Java 1.8.0_92).
Type in expressions for evaluation. Or try :help.

scala> :load gpio.sc
Added '/opt/pi4j/lib/pi4j-core.jar' to classpath.
import com.pi4j.io.gpio._
warning: there was one feature warning; re-run with -feature for details
gpio: com.pi4j.io.gpio.GpioController = com.pi4j.io.gpio.impl.GpioControllerImpl@9f73a2
pin00: com.pi4j.io.gpio.GpioPinDigitalOutput = "RedLed" <GPIO 0>
pin02: com.pi4j.io.gpio.GpioPinDigitalOutput = "GreenLed" <GPIO 2>
Blinking red fast...
Blinking green fast...
Blinking red & green fast...
res19: java.util.concurrent.Future[_] = null
res20: java.util.concurrent.Future[_] = null
res22: java.util.concurrent.Future[_] = java.util.concurrent.ScheduledThreadPoolExecutor$ScheduledFutureTask@a70627
res24: java.util.concurrent.Future[_] = java.util.concurrent.ScheduledThreadPoolExecutor$ScheduledFutureTask@1e59f28

scala> :quit
$>
```

The script above loads an `sc` file. Those are called `scala worksheets`, or `scala scripts` (hence the `sc` extension).
You can type the exact same statements they contain directly in the REPL.
The script named `gpio.sc` is part of this repo.

The worksheet is a commodity, it allows you to replay the work you've already done.

As seen above, you exit the REPL by typing `:quit`.

---
