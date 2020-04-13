## Servos and Sun, Sunflower and Multiplexer

This is an illustration of 2 things:
- The [`SunFlower`](../Project.Trunk/SunFlower/README.md) project, that orients a panel (like a Solar Panel) so it faces the Sun
- The [`NMEA.multiplexer`](../NMEA.multiplexer/README.md) project, that can deal with an NMEA feed.

The goal is to have a small device (featured later), carried by some vehicle on the go (a car, RV, boat...), constantly facing the Sun (during the day... of course!)

---

The `Solar Panel forwarder` is in a separate project, named `GSP.sun.servo`.

You can see in the code that it takes the values it require from a `properties` file, named here `sunflower.properties`:
```properties
#
heading.servo.id=14
tilt.servo.id=15
#
declination=14
deltaT=68.8033
#
# If no GPS:
latitude=37.7489
longitude=-122.5070
#
smooth.moves=true
#
ansi.console=false
orient.verbose=true
astro.verbose=true
tilt.verbose=true
servo.super.verbose=false
#
tilt.servo.sign=1
heading.servo.sign=1
tilt.limit=20
tilt.offset=0
#
one.by.one=false
#
```
This `properties` file is referred to from the `properties` file used by the multiplexer:
```properties
#
forward.01.cls=nmea.forwarders.SolarPanelOrienter
forward.01.properties=sunflower.properties
#
```
(a snippet of code part of this project contains the lines above).

### To build and run it
Make sure you have built all the required parts:

```bash
 SunFlower $> cd ../GPS.sun.servo
 GPS.sun.servo $> ../gradlew [--no-daemon] clean shadowJar
 GPS.sun.servo $> cd ../NMEA.multiplexer
 NMEA.multiplexer $> ../gradlew [--no-daemon] clean shadowJar
 NMEA.multiplexer $> cp ../GPS.sun.servo/sunflower.propeties .
 NMEA.multiplexer $> cp mux.sh mux.sunflower.sh
```
The last line creates a duplicate of the script that runs the multiplexer, edit it, and add the required resources in the `classpath`:
```bash
#
CP=./build/libs/NMEA.multiplexer-1.0-all.jar
CP=$CP:../GPS.sun.servo/build/libs/GPS.sun.servo-1.0-all.jar
# CP=$CP:./libs/RXTXcomm.jar          # for Mac
CP=$CP:/usr/share/java/RXTXcomm.jar # For Raspberry Pi
#
```
You can use the file `nmea.mux.sun.flower.properties` to start the multiplexer, is uses a log file as input, and drives the solar panel according to the Position and Heading, provided as NMEA Srings.
```bash
 NMEA.multiplexer $> ./mux.sunflower.sh nmea.mux.sun.flower.properties
```
> If you want to read a GPS from a serial port, use `nmea.mux.sun.flower.properties`.
>
> Use `nmea.mux.sun.flower.02.properties` otherwise.

And that's it! The solar panel keeps facing the Sun, wherever you are, and whatever your heading is.

---
