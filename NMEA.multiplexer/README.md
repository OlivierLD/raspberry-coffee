# NMEA Multiplexer
Any input (File, Serial, TCP, UDP, WebSocket, Sensors, Computations, ...), any output (File, Serial, TCP, UDP, WebSockets...), and a REST API on top of that.

### Includes
- NMEA Parser
- NMEA Strings generator
- Serial port Reader / Writer
- TCP Reader / Writer
- UDP Reader / Writer
- WebSocket client (read/write)

## Open questions
- Do we need a parser here? If it is just about multiplexing, then probably not.
- RMI protocol?
- A Cache?

## Open Issues
- UDP client

## TODO
- verbose options
- computer (calculate data from other ones: current, true wind)
- 3D compas (LSM303) interface, see http://opencpn.org/ocpn/Basic_data-connections_nmea-sentences (XDR), and http://forum.arduino.cc/index.php?topic=91268.0

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

### To see it at work
See the class `nmeaproviders.client.mux.GenericNMEAMultiplexer`, it uses the file `nmea.mux.properties` to define what to read, and what to re-broacdast it to. 
See it to understand its content (should be clear enough).

To compile and build:
```
 $> ../gradlew shadowJar
```
To run it, modify `mux.sh` to fit your environment, and run
```
 $> ./mux.sh
```

#### WebSockets
WebSocket protocol is supported, in input, and in output.
If needed, you can start your own local WebSocket server, running on `nodejs`.
To install it (once):
```
 $> npm install
```
Then, to run it,
```
 $> node wsnmea.js
```
or
```
 $> npm start
```

## REST Admin Interface
The properties files like `nmea.mux.proeprties` defines the configuration at startup.

You can remotely manage the input channels and the re-broadcasting ones through a REST interface.
The soft includes a dedicated HTTP Server. The http port is driven by a propety (in `nmea.mux.properties`).
Same if you want the HTTP server to be started or not.
```
with.http.server=yes
http.port=9999

```

### Supported end-points (for now)

```
 GET /serial-ports
```
returns a payload as:
```
[
  "/dev/tty.Bluetooth-Incoming-Port",
  "/dev/cu.Bluetooth-Incoming-Port"
]
```
The list of _available_ serial ports.

``` 
 GET /channels
```
returns a payload like
```
[
  {
    "cls": "nmeaproviders.client.SerialClient",
    "type": "serial",
    "port": "/dev/ttyUSB0",
    "br": 4800
  },
  {
    "cls": "nmeaproviders.client.BME280Client",
    "type": "bme280"
  }
]
```

``` 
 GET /forwarders
```
returns a payload like
```
[
  {
    "cls": "servers.TCPWriter",
    "port": 7001,
    "type": "tcp"
  },
  {
    "cls": "servers.ConsoleWriter",
    "type": "console"
  }
]
```

```text 
 DELETE /forwarders/:type
```
`type` is one of
- `file`. requires a body like 
 ```text
{ 
    "cls": "servers.DataFileWriter",
    "log": "./data.nmea",
    "type": "file"
}
```
identical to the elements returned by `GET /forwarders`.
- `console`. requires no body.
- `tcp`. requires a body like 
```text
{
     "cls": "servers.TCPWriter",
     "port": 7002,
     "type": "tcp"
}
```
identical to the elements returned by `GET /forwarders`.
- `ws`. requires a body like 
```text
{
   "cls": "servers.WebSocketWriter",
   "wsUri": "ws://localhost:9876/",
   "type": "ws"
}
```
identical to the elements returned by `GET /forwarders`.

``` 
 DELETE /channels/:type
```
``` 
 POST /forwarders
```
with payloads like:
- `file`. requires a body like 
 ```text
{ 
    "cls": "servers.DataFileWriter",
    "log": "./data.nmea",
    "type": "file"
}
```
identical to the elements returned by `GET /forwarders`.
- `console`. requires a body like 
```text
{ 
     "cls": "servers.ConsoleWriter",
     "type": "console"
}
```
- `tcp`. requires a body like 
```text
{
     "cls": "servers.TCPWriter",
     "port": 7002,
     "type": "tcp"
}
```
identical to the elements returned by `GET /forwarders`.
- `ws`. requires a body like 
```text
{
   "cls": "servers.WebSocketWriter",
   "wsUri": "ws://localhost:9876/",
   "type": "ws"
}
```
identical to the elements returned by `GET /forwarders`.

``` 
 POST /channels
```

There is a Web UI to deal with this REST Api.
On the HTTP POrt, use a url like `http://machine-name:9999/web/admin.html`,
where `machine-name` is the name of the machine where the multiplexer is running, and `9999` is the port defined in the properties.

And any REST client (NodeJS, Postman, your own code, ...) does the job.

---
