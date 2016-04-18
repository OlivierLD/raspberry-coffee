### Raspberry Coffee ###
#### Java code and wiring for the Raspberry PI, featuring reusable libraries and snippets ####
It uses the [PI4J library](http://pi4j.com).

---
This project contains Java code, mostly translated from Python, dedicated to usually *one* board (like BMP180, LSM303, etc).
More consistant samples can be found in the RasPISamples project, where several components have been assembled together. 
Do take a look, it also comes with a readme file.

---
_Note:_
This project uses `gradle` and `git`. `Gradle` will be installed automatically if it is not present on your system,
it uses the gradle wrapper (`gradlew`).

`Git` is usually installed on Linux and Mac, but not on Windows. On Windows, you need to install the [`git bash shell`](http://lmgtfy.com/?q=install+git+bash+shell+on+windows), and run in it
the commands mentioned in this document.
---
To build it, clone this project, make sure the scripts named <code>makeall</code> and <code>gradlew</code> are executable, and execute <code>makeall</code>.
<pre>
 Prompt> chmod +x makeall gradlew
 Prompt> ./makeall
</pre>
<i>Note:</i> If you are behind a firewall, you need a proxy. Mention it in all the files named <code>gradle.propetries</code>, and in <b>all</b> the <code>build.gradle</code> scripts, uncomment the following two lines:
<pre>
// ant.setproxy(proxyhost: "$proxyHost", proxyport: "$proxyPort") //, proxyuser="user", proxypassword="password") 
// compileJava.dependsOn(tellMeProxy)
</pre>

---

### Raspberry PI, a possible thing of the Internet of things... ###
  * The Raspberry PI is a fully featured Linux computer, which can - as such - connect to the Internet.
  * The Raspberry PI has a General Purpose Input Output (GPIO) interface that allows it to drive all kind of electronic components, from a simple LED to a complex robot, and including all kind of sensors (GPS, light resistors, pressure sensors, temperature sensors, all kinds!).
None of the above is new. Connecting to the Internet does not impress anyone anymore. Driving a robot, modern kitchens are full of robots, cars are loaded with electronic components...
**But** what if we put those two together, with the Raspberry PI sitting in between.
**Then**, we can drive a robot over the Internet. And **this** is not that usual (yet).

---

The snippets provided in this project are here to help in this kind of context. Some will use the network aspect of the story, some others will interact with electronic components. The two aspects should be easy to bridge, that is the goal. If that was not the case, please let me know (email address of the left side).

---

Several projects are featured here:
  * Basic GPIO interaction
  * Two Leds
  * Use the Raspberry PI to turn LEDs on and off, **through email** ([with doc](http://www.lediouris.net/RaspberryPI/email/readme.html))
  * Read Serial Port ([with doc](http://www.lediouris.net/RaspberryPI/serial/readme.html))
  * Read _and parse_ NMEA Data from a GPS ([with doc](http://www.lediouris.net/RaspberryPI/GPS/readme.html))
  * Read analog data with an Analog Digital Converter (ADC). ([with doc](http://www.lediouris.net/RaspberryPI/ADC/readme.html), with [node.js and WebSocket](http://www.lediouris.net/RaspberryPI/ADC/adc-websocket.html))
  * Drive servos using the  PCA9685. ([with doc](http://www.lediouris.net/RaspberryPI/servo/readme.html)).
  * Drive servos using the  PCA9685, **over the Internet**, with an Android client option. ([with doc](http://www.lediouris.net/RaspberryPI/servo/node.servo.html)).
  * Use the  LSM303. (I<sup>2</sup>C compass & accelerometer,  [with doc](http://www.lediouris.net/RaspberryPI/LSM303/readme.html)).
  * Use the  BMP180. (I<sup>2</sup>C temperature and pressure sensor,  [with doc](http://www.lediouris.net/RaspberryPI/BMP180/readme.html)).
  * Use the  BMP183. (SPI temperature and pressure sensor,  [with doc](http://www.lediouris.net/RaspberryPI/BMP183/readme.html)).
  * Use a relay, through email. ([with doc](http://www.lediouris.net/RaspberryPI/Relay.by.email/readme.html)).
  * Use a relay, through HTTP. ([with doc](http://www.lediouris.net/RaspberryPI/Relay.by.http/readme.html)).
  * Use a seven-segment display. ([with doc](http://www.lediouris.net/RaspberryPI/SevenSegment/readme.html)).
  * Use the  VCNL4000 (I<sup>2</sup>C proximity sensor).
  * Use the  TCS34725 (I<sup>2</sup>C color sensor, [demo](http://www.lediouris.net/RaspberryPI/TCS34725/readme.html)).
  * Use the  TSL2561 (I<sup>2</sup>C light sensor).
  * Use the  L3GD20 (I<sup>2</sup>C gyroscope, [demo](http://www.lediouris.net/RaspberryPI/L3GD20/readme.html)).
  * Use the  MCP4725 (I<sup>2</sup>C Digital to Analog Converter, [demo](http://www.lediouris.net/RaspberryPI/DAC/readme.html)).
  * ... and more.

---

All the doc - with more details than here - can be reached from [this page](http://www.lediouris.net/RaspberryPI/index.html).
