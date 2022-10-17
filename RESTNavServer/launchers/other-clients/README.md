# Why this section ?

## Out-of-the-box
The NMEA-multiplexer is equipped to deal (in input and output) with several channels.  
Several channels are available by default (ie built-in), to get NMEA data _**from**_ channels like
- Serial ports
- TCP ports
- UDP ports (in progress)
- WebSockets
- REST and HTTP ports
- Data files (aka log files)
- Several sensors can also be used (depending on the machine run on, it requires to be connected to the sensor(s)), like
  - BMP180
  - BME180
  - LSM303
  - HTU21DF
  - ... and more
  - Those sensors are producing data read by the Multiplexer, and turned into NMEA to be used like any other NMEA data. This way, those sensors can produce data regarding Air Temperature, Atmospheric Pressure, all thgis kind of things, that would be propably cheaper than a commercial NMEA station.

The Multiplexer can then send those data _**to**_ different channels, like - as above:
- Serial ports
- TCP ports
- UDP ports (in progress)
- WebSockets
- REST Server (to be pinged and pulled by REST/HTTP requests)
- Data files (aka log files)

All those input and output are declaratively driven by `yaml` or `properties` files.  
See the [Multiplexer documentation](../../../NMEA-multiplexer/manual.md) for more details, there is much more to say about all that.

A Serial Port can be continuously read, and the flow it spit out can be turned into NMEA Sentences. Most of the GPSs are providing Serial Ports outputs.
Same for AIS receivers. NMEA Stations also provide their data through serial ports.

TCP and UDP are interesting protocols, in that sense that they are _connected_ protocols.
This means that a TCP server expects TCP Clients connections, and whenever data are available, they are
dispatched to all the connected clients _**without the client having to request them**_.

Web pages do not natively support TCP and UDP, you need to write some code for that (in Java, C, Python, etc).

But there is this WebSocket protocol - that relies on TCP - and that can be used programmatically, as well
as from a Web page.

A Web page can also perform REST requests, and the NMEA-multiplexer can also be access
through a REST interface. REST relies on HTTP. That means that it is a _disconnected_ protocol.
An HTTP (and REST) request goes this way:
- The client creates an HTTP connection to the server.
- It pushes a request to the server (verb, resource, headers, payload, etc).
- Then it receives the response.
- The connection with the server is interrupted.

A subsequent REST request will follow the same steps, the previous connection cannot be re-used.

That is basically what is available out-of-the-box in the NMEA-Multiplexer and its avatars.

Several already existing programs can use the data emitted by the NMEA-Multiplexer, though its different channels.
Thos programs are language-agnostic, they rely on standard protocols (Serial, TCP, etc), and others (GPSD, SignalK, etc).
Programs like OpenCPN, SeaWi, and more, are perfectly happy with this. It is quite easy
to read data from a BME280 (Pressure, Air Temperature, Humidity), and see the Air Temperature in the OpenCPN dashboard.

### TODO
Note about GPSD, SignalK.

## Now what ?
In case you want to read the available data (or push your own to the NMEA-Multiplexer) from your own application, or your own code,
you might be interested in seeing how to do that.  
This folder (`other-clients`) intends to provide some samples and re-usable pieces of code.  
Each sub-folder will contain different language examples, along with `md` files to document them.

---
