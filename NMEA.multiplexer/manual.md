# Technical Manual

[<< Back](./README.md)

The following sections will make references to several resources (Java classes, properties files, etc).
They are all part of this project, so you can refer to them if needed for more details.

The program to start is `nmea.mux.GenericNMEAMultiplexer`, it is driven by a `properties` file,
describing the features required by an instance of the Multiplexer (channels, forwarders, computers, http server, etc).

### Properties
Here is a brief description of the properties managed by the `nmea.mux.GenericNMEAMultiplexer`, the ones
present in the file `nmea.mux.properties`, or in the file named as set in the System variable `mux.properties`.

Property names of channels, forwarders and computers follow this pattern:
```
 [element-type].[index].[attribute]
```

Element types can take three values: `mux`, `forward`, or `computer`:

- Whatever begins with `mux.` is a channel
- Whatever begins with `forward.` is a forwarder
- Whatever begins with `computer.` is a computer

For the three categories above, the second item is the index of the element.
Indexes are numbers, mentioned on two digits. Indexes _must_ start at `01` and be
after that incremented by `1`.

For example, `mux.01.xxx`, followed by `mux.02.yyy`.

> _Quick explanation_: To find the first channel, the program looks for a `mux.01.*`.
> If no such entry is found, that would mean for the program that there is no channel to deal with.
> After finding and evaluating `mux.01.xxx`, the program looks for `mux.02.*`. If no
> such channel is found, the program understands that the list of the channels is terminated.
> This is the same for channels, forwarders and computers.

The third part of the property name (the `type` in `mux.0X.type` for example) is the attribute.
_**ALL**_ elements _have_ a mandatory `type` attribute, the other attributes depend on this `type`.

> _**Exception**_: if an element does _not_ have a `type` attribute, then it is a custom element, it _must_ have a _cls_ attribute
> containing the name of the Java `class` to load dynamically, with a `Class.forName`.
> For example, a line like that one
```properties
 forward.02.cls=nmea.forwarders.LedBlinker
```
> would tell the Multiplexer to load a forwarder defined in the class `nmea.forwarders.LedBlinker`.
> If the loaded class does not extend the right `superclass` or implement the right `interface`, an error
> will be raised.

**Pre-defined channel types**

- `serial`
    - Serial port input.
- `tcp`
    - TCP input
- `file`
    - Log file replay
- `ws`
    - WebSocket input
- `htu21df`
    - Temperature, humidity
- `rnd`
    - Random data generator (for debug)
- `zda`
    - ZDA Sentence generator (UTC day, month, and year, and local time zone offset)
- `lsm303`
    - Triple axis accelerometer and magnetometer
- `bme280`
    - Humidity, pressure, temperature
- `bmp180`
    - Temperature, pressure

You can also define your own channels (extending `NMEAClient` and with a `reader` attribute).

Look for `mux.01.cls=nmea.consumers.client.WeatherStationWSClient`.

**Forwarders**

_**ALL**_ forwarders can use 2 _optional_ attributes, `subclass` and `properties`:
```properties
forward.XX.type=file
forward.XX.subclass=nmea.forwarders.ExtendedDataFileWriter
forward.XX.properties=validlogger.properties
```
The lines above means that:
- The `nmea.forwarders.ExtendedDataFileWriter` is a `file` Forwarder (extends `DataFileWriter`)
- Required extra properties are in a file named `validlogger.properties`.

> _**Dynamic loading versus sub-classing**_:
> We've seen before that you have the possibility - using a `cls` attribute - to define your own
> elements (Channel, Forwarder or Computer). Here we see the possibility to `extend` a given element type.
> A dynamically loaded element gives the programmer more flexibility and room for invention, but it _cannot_
> be managed by the `admin` web page. A sub-class of a given type of element can be much lighter to write,
> and _is_ manageable by the `admin` web page.

> See `ExtendedDataFileWriter.java` for details.

**Pre-defined forwarder types**

- `serial`
    - Write to a serial port
- `tcp`
    - TCP Server
- `gpsd`
    - GPSD Server
- `file`
    - Log file output
- `ws`
    - WebSocket server
- `wsp`
    - WebSocket Processor
- `console`
    - Console output
- `rmi`
    - RMI Server

You can also implement your own forwarder (implementing the `Forwarder` interface).

Look for `forward.02.cls=nmea.forwarders.RESTPublisher`

**Pre-defined computer type(s)**

- `tw-current`
    - One computer, to calculate both True Wind and Current (GPS Based, with possibly several time buffers).

> _Important_: Computers may need data coming from the various channels. Those data will
be stored in a cache _if the property `init.cache` is set to `true`_. See below.

You can also define your own computers (extending `Computer`).

Look for `computer.02.cls=nmea.computers.ComputerSkeleton`

**Other properties**

```properties
with.http.server=yes
http.port=9999
#
init.cache=true
deviation.file.name=dp_2011_04_15.csv
# Leeway = max.leeway * cos(awa)
max.leeway=10
#
bsp.factor=1.0
aws.factor=1.0
awa.offset=0
hdg.offset=0
#
default.declination=14
damping=30
```

`with.http.server` is set to `false` by default. `true` means that you will have access to
some `REST` services, for admin and other purposes.

If `with.http.server` is set to `true`, the default http port is `9999`. It can be overridden by `http.port` if needed.

`init.cache` is set to `false` by default. A cache - accessible by `Computers` will be initialized if
`init.cache` is set to `true`.
The cache is a `Map<String, Object>`, see `context.NMEADataCache` for details.

If `init.cache` is set to `true`, the following parameters will be taken in account when inserting data in the cache:
- `bsp.factor` Boat Speed Factor, number, `0` to `n`
- `aws.factor` Apparent Wind Speed Factor, number, `0` to `n`
- `awa.offset` Apparent Wind Angle offset in degrees, from `-180` to `180`
- `hdg.offset` Heading offset in degrees, from `-180` to `180`

`default.declination` will be used if not returned by the GPS (as it could, depends on your GPS). `E` is `+`, `W` is `-`.

`max.leeway` is used to calculate the leeway. The formula used here is:
```
 if awa > 90 and awa < 270 then leeway = 0
 otherwise, leeway = max.leeway * cos(awa)
```

`damping` (default is `1`) unused for now (Aug-2018), but will be.

`deviation.file.name` mentions the name of a CSV file, like - for example - `dp_2011_04_15.csv`. The
default value is `zero-deviation.csv`.

The format of this Comma-Separated-Values (CSV) file is the following one:
```csv
0.0,-0.9830777902388692
5.0,-0.011026572256005562
10.0,0.9376226337606713
15.0,1.8481417760529473
20.0,2.706968419259063
25.0,3.502010498068172
...
```
Each line contains two fields, the first one is the **Compass** Heading, the second one is the corresponding deviation.
Such a file can be rendered like this:

<img src="./docimages/deviation.curve.png" title="deviation curve" width="318" height="440">

### Example
Here is an example of a simple properties file driving the Multiplexer:
```properties
#
#  MUX definition.
#
with.http.server=yes
http.port=9999
#
# Channels (input)
#
mux.01.type=serial
mux.01.port=/dev/ttyUSB0
mux.01.baudrate=4800
mux.01.verbose=false
#
# Forwarders
#
forward.01.type=tcp
forward.01.port=7001
#
forward.02.type=file
forward.02.filename=./data.nmea
#
init.cache=true
```
This file tells the Multiplexer to:
- Read the Serial port `/dev/ttyUSB0` with a baud rate of `4800` bauds
- Forward the NMEA data on `tcp` port `7001`
- Log the data into a file named `data.nmea`.

As `init.cache` is set to `true`, an `admin` web page will be available on port `9999` (at http://localhost:9999/web/admin.html).
