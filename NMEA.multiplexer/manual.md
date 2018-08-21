# Technical Manual (WIP)

The program to start is `nmea.mux.GenericNMEAMultiplexer`, it is driven by a `properties` file,
describing the features of the Multiplexer (channels, forwardsers, computers, http server, etc).

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

For the three categories above, the next element is the index of the element.
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

You can also define you own channels (extending `NMEAClient` and with a `reader` attribute).

Look for `mux.01.cls=nmea.consumers.client.WeatherStationWSClient`.

**Forwarders**

_**ALL**_ forwarders can use 2 _optional_ attributes, `subclass` and `properties`:
```properties
forward.XX.type=file
forward.XX.subclass=nmea.forwarders.ExtendedDataFileWriter
forward.XX.properties=validlogger.properties
```
The lines above means that:
- The `nmea.forwarders.ExtendedDataFileWriter` extends `DataFileWriter`
- Required extra properties are in a file named `validlogger.properties`.

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

You can also implement you own forwarder (implementing the `Forwarder` interface).

Look for `forward.02.cls=nmea.forwarders.RESTPublisher`

**Pre-defined computer type(s)**

- `tw-current`
    - One computer, to calculate both True Wind and Current (GPS Based, with possibly several time buffers).

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

