# Why this module, what is this section ?
OK. Why do we need this section here?  
Well, in case you want to access the multiplexer's data and features from other languages than Java, or from another software than the ones provided out-of-the-box, you might want to know where to begin...  

So, let's see what you have out-of-the-box, and what to do from there.

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
  - Those sensors are producing data read by the Multiplexer, and turned into NMEA to be used like any other NMEA data. This way, those sensors can produce data regarding Air Temperature, Atmospheric Pressure, all this kind of things, this would be probably cheaper than a commercial NMEA station providing this kind of information.

The Multiplexer can then send those data _**to**_ different channels, like - as above:
- Serial ports
- TCP ports
- UDP ports (in progress)
- WebSockets
- MQTT (in progress)
- REST Server (to be pinged and pulled by REST/HTTP requests)
- Data files (aka log files)

All those inputs and outputs are declaratively driven by `yaml` or `properties` files.  
See the [Multiplexer documentation](../NMEA-multiplexer/manual.md) for more details, there is much more to say about all that.

A Serial Port can be continuously read, and the flow it spit out can be turned into NMEA Sentences. Most of the GPSs are providing Serial Ports outputs.
Same for AIS receivers. NMEA Stations also provide their data through serial ports.

TCP and UDP are interesting protocols, in that sense that they are _connected_ protocols.
This means that a TCP server expects TCP Clients connections, and whenever data are available, they are
dispatched to all the connected clients _**without having the client to request them**_.

Web pages do not natively support TCP and UDP, you need to write some code for that (in Java, C, Python, etc).

But there is this WebSocket protocol - that relies on TCP - and that can be used programmatically, as well
as from a Web page.

A Web page can also perform REST requests, and the NMEA-multiplexer can also be access
through a REST interface. REST relies on HTTP. That means that it is a _disconnected_ protocol.
An HTTP (and REST) request goes this way:
- The client creates an HTTP connection to the server.
- It pushes a request to the server (verb, resource, headers, payload, etc).
- It expects and receives the response.
- Then the connection with the server is interrupted (disconnected).
- Summary: _Connect, Request, Response, Disconnect._

A subsequent REST request will follow the same steps, the previous connection cannot be re-used, as it was closed.

That is basically what is available out-of-the-box in the NMEA-Multiplexer and its avatars.

Several already existing programs can use the data emitted by the NMEA-Multiplexer, though its different channels.
Those programs are language-agnostic, they rely on standard protocols (Serial, TCP, etc), and others (GPSD, SignalK, etc).
Programs like OpenCPN, SeaWi, and more, are perfectly happy with this. For example, it is quite easy
to read data from a BME280 (Pressure, Air Temperature, Humidity), and see the Air Temperature in an OpenCPN dashboard.

### A note (again) about GPSd, SignalK
All the above use NMEA format for the data. NMEA is a well documented standard, that has been around for decades
(it's probably one of the oldest IT standards).

Some other softs are using other formats (data are transformed into this format, from NMEA).

**GPSd**, is a TCP server, that can be ping just like any other, and it returns data in its proprietary format.
GPSd - written in C - comes for free, and is here by default on several Linux releases.  
The NMEA-Multiplexer provides a GPSd forwarder that can be accessed by a GPSd client (like OpenCPN).

**SignalK**, is more like a REST Server, that provides several REST resources to get to the data, returned in JSON format.
JSON is a standard, REST is a standard, but the JSON Schema used to store the data is not. We are facing - in the NMEA-Multiplexer - the
exact same problem. When you do a REST request like `GET /mux/cache HTTP/1.0`, you get a JSON Object that has an arbitrary structure, which you
need to be aware of to get to the data you are interested in.  
SignalK is in the pipe, but not there yet... Working on it.

## Now what ?
In case you want to read the available data (or push your own to the NMEA-Multiplexer) from your own application, or your own code,
you might be interested in seeing how to do that.  
This folder (`NMEA-Mux-Other-Clients`) intends to provide some samples and re-usable pieces of code.  
Each sub-folder will contain different language examples, along with `md` files to document them.

The code presented here should be 100% independent of the NMEA-multiplexer internal plumbing.
The only "exception" could be the NMEA-Parser (which does not belong to the NMEA-multiplexer module, by the way).

<!-- TODO NMEA Parsers in Python (WiP), C -->

The NMEA-Multiplexer - and its extensions - can be easily accessed through HTTP, TCP, and other protocols,
this makes its integration with Web pages smooth and easy, and the Raspberry Pi can emit its own network, so it can be accessed
by any device connected on this very network (that brings us back to this concept of flake computing - as opposed to cloud computing, network without Internet).  
But one may very well want to use another language than ES6 or HTML5 to get to the data...

To give you another point of view, this would tell you how to start writing a client
like OpenCPN or SeaWi...

---
