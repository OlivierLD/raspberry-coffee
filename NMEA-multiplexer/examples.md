# Implementation Examples

[<< Back](./README.md)

----

- [Small external OLED display](#small-external-display)
- [Small DOT - Display On Top](#small-display-on-top)
- [Web Console](#web-console)
- [Distinction on the device ID](#inside-and-outside)
- [Driving and Logging](#driving-and-logging)
- [Weather Station](#weather-station)
- [SunFlower](#solar-data)

#### Small external display
There is a forwarder sample `SSD1306Processor` that uses an oled display to show the True Wind Direction read from the cache:
![oled](./docimages/oled.jpg "OLED Display")

![oled](./docimages/oled.2.jpg "OLED Display")

The OLED display in connected to the Raspberry Pi using an SPI interface.

The SSD1306 display is quite small. In the pictures above, there is a Fresnel lens in front of it.

![Wiring](./docimages/ssd1306_bb.png)

The push button is used to scroll through the data available to display (True Wind Direction, Boat Speed, etc).

![Boat Speed](./docimages/opt.bsp.jpg)
![Current](./docimages/opt.current.jpg)
![Position](./docimages/opt.position.jpg)

...etc. There are like 14 data options (for now).
**Warning**: Do look in the code, to make sure the pin used by the push button is the right one...

As it is an example, it is a dynamically loaded `forwarder`.

#### Small display "On Top"
Same as above (SSD1306), but attached on top of the Raspberry Pi. Using here the I<small><sup>2</sup></small>C interface.

  ![Small Screen](./docimages/small.screen.jpg)

 _Another interesting feature_: In case you want to run - for tests for example - somewhere else than on the Raspberry Pi, then the OLED Display would not work.
  In this case, it is automatically substituted to a Swing panel:

  ![Substituted Swing Led Panel](./docimages/substitute.png)

#### Web Console
There is a Web Console, accessible to any device able to run a Web browser (smart phone, tablet, laptop, etc).
It can be accessed from `http://[machine:port]/web/console.html`.
Again, this is just an example, feel free to tweak it your way.

![page 1](./docimages/full.console.01.png "First page")
In the picture above, all the data come from the NMEA station, except the air temperature, pressure and humidity, that come from sensors.
![page 2](./docimages/full.console.02.png "Second page")
![page 3](./docimages/full.console.03.png "Third page")
![page 4](./docimages/full.console.04.png "Fourth page")

#### Inside and Outside
This example involves 2 (or 3) machines.

- A Forwarder with a distinction on the **origin** of the data, based on the device ID.
   - A thermometer inside (BMP180 or BME280), forwarding on TCP, device ID set to `01`
   - A thermometer outside (BMP180 or BME280), forwarding on TCP, device ID set to `02`
   - A MUX reading the 2 TCP streams, and displaying (like with an HTML Interface) the right data in the right place, even if the Sentence IDs of the two streams are identical.
![2 Raspberry Pi Zero](./docimages/Two.RPi.Zero_bb.png "Two thermometers")

##### On the first Raspberry Pi
Let's say its IP address is `192.168.1.166`.
We have a BMP180, and an HTU21D-F.

Its properties file looks like this:
```properties
#
# MUX definition.
#
# HTTP server, and just BMP180 & HTU21D-F
#
with.http.server=yes
http.port=9999
#
mux.01.type=bmp180
mux.01.device.prefix=01
#
mux.02.type=htu21df
mux.02.device.prefix=01
#
# Forwarders
#
forward.01.type=tcp
forward.01.port=7001
#
```

##### On the second Raspberry Pi
Let's say its IP address is `192.168.1.136`.
We have a BME280.

Its properties file looks like this:
```properties
#
#  MUX definition.
#
with.http.server=yes
http.port=9999
#
# Channels (input)
#
mux.01.type=bme280
mux.01.device.prefix=02
#
# Forwarders
#
forward.01.type=tcp
forward.01.port=7001
#
```

##### On a third machine, Raspberry Pi, or not.
We read the two TCP streams from the machines above.
Its properties file looks like
```properties
#
#  MUX definition.
#
with.http.server=yes
http.port=9999
#
# Reads 2 Raspberry Pis:
# First one  reads a BMP180 and an HTU21DF, forwarded on TCP port 7001.
# Second one reads a BME280, forwarded on its port 7001 too.
#
mux.01.type=tcp
mux.01.port=7001
mux.01.server=192.168.1.166
#
mux.02.type=tcp
mux.02.port=7001
mux.02.server=192.168.1.136
#
# Forwarders
#
forward.01.type=tcp
forward.01.port=7001
#
forward.02.type=console
#
# Dynamic
#
forward.03.cls=nmea.forwarders.InOutDataWriter
#
#
##############################################################
#
# Cache parameters. Taken in account only if init.cache=true
#
init.cache=true
#
##############################################################
#
# No computers
#
```

For details on the way it works, look at the code in `nmea.forwarders.InOutDataWriter.java`.
This one in adding custom entries in the cache...

Then you can reach the page at [http://localhost:9999/web/examples/in.out.html]().
![In and Out](./docimages/in.out.png "In and Out")

This page in pinging the cache every second, and extracts the custom entries from it to display the
inside and outside conditions, even if they are represented by the same NMEA sentence ID; the distinction is made on the device ID.

![Diagram](./docimages/3-mux.png)

_Note_: The Web-UI is actually just pinging (using Ajax) the Cache. The `MUX-3` is the only one that requires `init.cache=true`.
In other words, the `MUX-3` just feeds the cache, and the `Web-UI` reads it. Those two can technically ignore each other.

#### Driving and Logging
This is a small exercise, logging GPS Data when driving from San Francisco to Palo-Alto.

The Multiplexer runs on a Raspberry Pi Zero W, with a cheap GPS (USB GPS, less than $20 - June 2017) connected on it. It logs data in a file.
Many options are available to power the Raspberry Pi on the go (Lipo batteries, battery bank, USB cable on a cigarette lighter adapter, etc). I've used a battery bank here.

![Connections](./docimages/gps.battery.bank.jpg "Connections")

The Raspberry Pi, its battery bank and the GPS were sitting in a plastic box, next to the driver's seat.

![In a box](./docimages/in.a.box.jpg "In a box")

The Raspberry Pi generates its own WiFi hotspot (with `hostapd`, see [here](./README.md#note-access-point-and-internet-access)), and it can be driven remotely through `ssh`, I used an Android phone for that, to start and stop
the Multiplexer, also to shutdown the Raspberry Pi (see [here](./README.md#remote-access) for details). Even the admin web console works fine in this configuration.

The properties files used with `mux.sh` just looks like this:
```properties
#
# MUX definition.
#
# All indexes must begin with 01, and be consecutive.
#
# GPS Logging
#
with.http.server=yes
http.port=9999
#
# Channels
#
mux.01.type=serial
mux.01.port=/dev/ttyUSB0
mux.01.baudrate=4800
#
# Forwarders
#
forward.01.type=file
forward.01.filename=./data.nmea
forward.01.append=true
#
```
It reads the GPS on serial port `/dev/ttyUSB0` and logs the data in `./data.nmea`.

The data file can then be replayed. The Web UI has been reworked a bit, the speed scale is not the same as on boat (and it is mph, not knots).

![Driving](./docimages/driving.png "Driving")

You can use a query parameter named `'type'` to set the maximum speed in the display. If the `'type'` is `walk`, then the maximum is `15`. Use it as in
```
 http://localhost:9999/web/googlemaps.driving.html?type=walk
```

With a bonus: a led that blinks every time a _valid_ NMEA sentence is received.
![Blink](./docimages/blink.jpg "Blink")

See how `LedBlinker` is referred to in `nmea.mux.gps.log.properties`, it is a custom forwarder.

```properties
forward.02.cls=nmea.forwarders.LedBlinker
```

Another one:
A small [I2C OLED display](https://www.adafruit.com/product/3527) attached to the Zero, fed from a forwarder (`nmea.forwarders.SSD1306ProcessorI2C`).
![](./docimages/oled.01.jpg)
![](./docimages/oled.02.jpg)

Yet another one, with a Nokia-5110 small screen, fed from another forwarder.

![](./docimages/Nokia5110.jpg)

Notice the small potentiometer to dim the display's light.

#### Weather Station
Feed the NMEA Multiplexer with the Weather Station from SwitchDoc labs.
The code of the project is [here](https://github.com/OlivierLD/raspberry-pi4j-samples/tree/master/WeatherStation),
and an implementation is available [here](https://github.com/OlivierLD/raspberry-pi4j-samples/tree/master/RasPISamples#weatherstation).

There is a  `custom channel` (aka `consumers`) made out of
- `WeatherStationWSClient`
- `WeatherStationWSReader`

The `WeatherStation` feeds a WebSocket server with a `json` object like this:
```json
{ "dir": 350.0,
  "avgdir": 345.67,
  "volts": 3.4567,
  "speed": 12.345,
  "gust": 13.456,
  "rain": 0.1,
  "press": 101300.00,
  "temp": 18.34,
  "hum": 58.5,
  "cputemp": 34.56 }
```
The `consumer` turns this object into several `NMEA` sentences.
It also listens to the WebSocket topic fed by the `WeatherStation`. Its URL is to be provided in
the properties file mentioned in the `channel` definition:
```properties
mux.01.cls=nmea.consumers.client.WeatherStationWSClient
mux.01.reader=nmea.consumers.reader.WeatherStationWSReader
mux.01.properties=weather.station.properties
```

```properties
ws.uri=ws://192.168.127.127:9876/
```

##### Further: IoT

Now the Weather Station is being read, we could forward its data somewhere else, like on an IoT server.

###### Summary
From the `RasPISamples` directory, on its own machine (or not...), use `weather.menu`:
```javascript
 $ ./weather.menu
 N: Start Node server
 W: Start Weather Station reader
 D: Start Weather Station dump
 S: Show processes
 K: Kill them all
 Q: Quit
 You Choose >
```
- Start the node server (including the `WebSocket` server)
- Then start the Weather Station Reader

Then the NMEA Multiplexer will be able to listen to the WebSocket topic.

![Weather Station](./docimages/weather.station.nmea.png)

The `Raspberry Pi` and the `Other machine` are on the same Local Area Network (LAN).

The `IoT server` and the `php/MySQL` are hosted on the Internet.

See it live [here](http://donpedro.lediouris.net/php/weather/reports.v2/weather.report.html).
This renders the data inserted in MySQL.

See those documents for more details:
- [http://raspberrypi.lediouris.net/WeatherStation/readme.html](http://raspberrypi.lediouris.net/WeatherStation/readme.html)
- [https://github.com/OlivierLD/raspberry-pi4j-samples/tree/master/RasPISamples#home-weather-station](https://github.com/OlivierLD/raspberry-pi4j-samples/tree/master/RasPISamples#home-weather-station)

###### More on IoT
I've tried two options:
- Particle
- Adafruit-IO

Both can provide some free access to an IoT server.

Adafruit-IO supports `MQTT` and `REST` protocols.

#### Solar Data
That one is a variation of the [`SunFlower`](https://github.com/OlivierLD/raspberry-pi4j-samples/tree/master/Project.Trunk/SunFlower/README.md) project.

The `SunFlower` project is originally designed to orient a solar panel toward the Sun, to maximize its efficiency.
The solar panel is driven by two servos or motors, one for the heading (vertical axis), and one for the tilt (horizontal axis).

Based on the device geographical position and the system date, the soft calculates the `elevation` and `azimuth` of the sun,
and drives the servos (or motors) accordingly.

The idea here is to forget about the solar panel orientation, get the geographical position from a GPS connected on a serial port, do the astronomical calculations,
and output several data, available through a REST and HTML interface:
- Position
- System and Solar Dates
- Elevation and Azimuth of the Sun

GPS is read from a `channel`, output produced as a `forwarder`:
```properties
#
#  MUX definition.
#
with.http.server=yes
http.port=9999
#
# ...
#
# Channels (input)
#
# GPS, on a Mac
mux.01.type=serial
# GPS, on a Mac
# mux.01.port=/dev/tty.usbserial
# GPS, on a Raspberry Pi
mux.01.port=/dev/ttyUSB0
mux.01.baudrate=4800
mux.01.verbose=false
mux.01.sentence.filters=RMC,~GSV,~GGA,~GSA
#
# Forwarders
#
forward.01.cls=nmea.forwarders.SolarPanelOrienter
forward.01.properties=sunflower.rest.properties
#
```
> Note: this runs on any machine that knows about a serial port, not only a Raspberry Pi.

As seen above, the `forwarder` refers to its own properties file:
```properties
#
# For the SunFlower REST Server (Dates & Co)
#
http.port=9090
#
...
```
The port is the port of a particular REST server used for the Sun data.

The `nmea.forwarders.SolarPanelOrienter` class is part of the `GPS.sun.servo` project. Build all you need this way:

```bash
 $ cd ../GPS.sun.servo
 $ ../gradlew shadowJar
 $ .......
 $ cd ../NMEA-multiplexer
 $ ../gradlew shadowJar
 $ .......
```
And then just run it (with the GPS on the right serial port):
```bash
 $ ./mux.sunflower.sh [Mac]
```
Then the web interface is available on the `http.port` mentioned above, at `sun.data.html`.

![Web Interface](./docimages/solar.data.png)

Do the test: the Sun crosses the meridian at _exactly_ 12:00:00 noon Solar (SLR).

#### And more to come...
##### Ideas
- Another logger, suitable for kayaking:
  - transparent box
  - bigger display (like the [Nokia](https://www.adafruit.com/product/338) one)
  - with external GPS (outside the box)
  - User Interface:
    - a [touchpad](https://www.adafruit.com/product/1332)?
    - A Smart Watch App?

See more implementation examples [here](https://github.com/OlivierLD/raspberry-coffee/tree/master/NMEA-mux-WebUI/README.md).

<!--
### TODO...
[Todo next](./web/TODO.md "What's next")

- 3D compass (LSM303) interface, see http://opencpn.org/ocpn/Basic_data-connections_nmea-sentences (XDR), and http://forum.arduino.cc/index.php?topic=91268.0
```
Once you get the X, Y and Z accelerations into floats you just need some trig to calculate Pitch and Roll (in radians):

pitch = atan (x / sqrt(y^2 + z^2));
roll = atan (y / sqrt(z^2 + z^2));
```

```
Currently, OpenCPN recognizes the following transducers:

------------------------------------------------------------------------------------------------------
Measured Value | Transducer Type | Measured Data                   | Unit of measure | Transducer Name
------------------------------------------------------------------------------------------------------
barometric     | "P" pressure    | 0.8..1.1 or 800..1100           | "B" bar         | "Barometer"
air temperature| "C" temperature |   2 decimals                    | "C" celsius     | "TempAir" or "ENV_OUTAIR_T"
pitch          | "A" angle       |-180..0 nose down 0..180 nose up | "D" degrees     | "PTCH"
rolling        | "A" angle       |-180..0 L         0..180 R       | "D" degrees     | "ROLL"
water temp     | "C" temperature |   2 decimals                    | "C" celsius     | "ENV_WATER_T"
-----------------------------------------------------------------------------------------------------
```
-->
