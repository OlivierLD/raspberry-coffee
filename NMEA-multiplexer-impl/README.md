# NMEA-multiplexer's Web Implementation Basics (and beyond)
This module is one illustration of what can be done on top of the `NMEA-multiplexer`.  
This includes the way to start the Multiplexer (through its configuration file), and the way to reach the data to 
display on a web page.

## Basics
It obviously requires the NMEA-multiplexer to be running.  
Data are fetched from the `NMEA Data Cache` using REST Requests.
> For the Data Cache to be available/reachable, you need to have the property `with.http.server` set to `true`,
> and the property `init.cache` set to `true`

### Get Started, now!
Build, run, watch:
- `../gradlew shadowJar`
- `./mux.sh nmea.mux.replay.big.log.yaml`
- then reach <http://localhost:8080/web/basic.html> in your browser.
- and also <http://localhost:8080/web/admin.html>.

> Notice the config file `nmea.mux.replay.big.log.yaml`, look into it.

Also try
```
$ ./mux.sh --interactive-config
```
This previous one was developed for tests, it requires some polishing, several values are hard-coded..., 
as you would see in the code (`GenericNMEAMultiplexer.interactiveConfig()`). 

## How it works (in short)
As it depends on the `http-tiny-server` module, the `NMEA-multiplexer` can also act as an HTTP Server (not only WebServer). When the configuration property `with.http.server` is set to `true`, the multiplexer can also serve:
- static Web pages
    - See the system properties `static.docs` and `static.zip.docs`.
- REST requests
    - Look into the class `nmea.mux.RESTImplementation`

> _**Note**_: _Any_ REST client can reach the features available in the `NMEA-multiplexer` (or the classes extending it).
> Web pages can do it - obviously - but also utilities like `curl`, `wget`, and devices like Arduino, ESP32 & friends, M5 sticks, etc.

## About NMEA-multiplexer's REST operations
As you would see in `nmea.mux.RESTImplementation` where all operations are defined, there is a `/mux/oplist` resource, that will list
all its siblings (and child services):
```text
$ curl -X GET http://192.168.1.102:8080/mux/oplist
```
```json
[
  {
    "verb": "GET",
    "path": "/mux/oplist",
    "description": "List of all available operations, on NMEA request manager.",
    "fn": {}
  },
  {
    "verb": "POST",
    "path": "/mux/terminate",
    "description": "Hard stop, shutdown. VERY unusual REST resource...",
    "fn": {}
  },
  {
    "verb": "GET",
    "path": "/mux/serial-ports",
    "description": "Get the list of the available serial ports.",
    "fn": {}
  },
  {
    "verb": "GET",
    "path": "/mux/channels",
    "description": "Get the list of the input channels",
    "fn": {}
  },
  {
    "verb": "GET",
    "path": "/mux/forwarders",
    "description": "Get the list of the output channels",
    "fn": {}
  },
  {
    "verb": "GET",
    "path": "/mux/computers",
    "description": "Get the list of the computers",
    "fn": {}
  },
  {
    "verb": "GET",
    "path": "/mux/mux-config",
    "description": "Get the full mux config, channels, forwarders, and computers",
    "fn": {}
  },
  {
    "verb": "DELETE",
    "path": "/mux/forwarders/{id}",
    "description": "Delete an output channel",
    "fn": {}
  },
  {
    "verb": "DELETE",
    "path": "/mux/channels/{id}",
    "description": "Delete an input channel",
    "fn": {}
  },
  {
    "verb": "DELETE",
    "path": "/mux/computers/{id}",
    "description": "Delete a computer",
    "fn": {}
  },
  {
    "verb": "POST",
    "path": "/mux/forwarders",
    "description": "Creates an output channel",
    "fn": {}
  },
  {
    "verb": "POST",
    "path": "/mux/channels",
    "description": "Creates an input channel",
    "fn": {}
  },
  {
    "verb": "POST",
    "path": "/mux/computers",
    "description": "Creates computer",
    "fn": {}
  },
  {
    "verb": "PUT",
    "path": "/mux/channels/{id}",
    "description": "Update channel",
    "fn": {}
  },
  {
    "verb": "PUT",
    "path": "/mux/forwarders/{id}",
    "description": "Update forwarder",
    "fn": {}
  },
  {
    "verb": "PUT",
    "path": "/mux/computers/{id}",
    "description": "Update computer",
    "fn": {}
  },
  {
    "verb": "PUT",
    "path": "/mux/mux-verbose/{state}",
    "description": "Update Multiplexer verbose",
    "fn": {}
  },
  {
    "verb": "PUT",
    "path": "/mux/mux-process/{state}",
    "description": "Update Multiplexer processing status. Aka enable/disable logging.",
    "fn": {}
  },
  {
    "verb": "GET",
    "path": "/mux/mux-process",
    "description": "Get the mux process status (on/off)",
    "fn": {}
  },
  {
    "verb": "GET",
    "path": "/mux/cache",
    "description": "Get ALL the data in the cache. QS prm: option=tiny|txt",
    "fn": {}
  },
  {
    "verb": "DELETE",
    "path": "/mux/cache",
    "description": "Reset the cache",
    "fn": {}
  },
  {
    "verb": "GET",
    "path": "/mux/dev-curve",
    "description": "Get the deviation curve as a json object",
    "fn": {}
  },
  {
    "verb": "GET",
    "path": "/mux/position",
    "description": "Get position from the cache",
    "fn": {}
  },
  {
    "verb": "POST",
    "path": "/mux/position",
    "description": "Set position in the cache",
    "fn": {}
  },
  {
    "verb": "GET",
    "path": "/mux/distance",
    "description": "Get distance traveled since last reset",
    "fn": {}
  },
  {
    "verb": "GET",
    "path": "/mux/delta-alt",
    "description": "Get delta altitude since last reset",
    "fn": {}
  },
  {
    "verb": "GET",
    "path": "/mux/nmea-volume",
    "description": "Get the time elapsed and the NMEA volume managed so far",
    "fn": {}
  },
  {
    "verb": "GET",
    "path": "/mux/sog-cog",
    "description": "Get Speed and Course Over Ground",
    "fn": {}
  },
  {
    "verb": "POST",
    "path": "/mux/sog-cog",
    "description": "Set Speed and Course Over Ground",
    "fn": {}
  },
  {
    "verb": "GET",
    "path": "/mux/run-data",
    "description": "Get Speed and Course Over Ground, distance, and delta-altitude, in one shot.",
    "fn": {}
  },
  {
    "verb": "GET",
    "path": "/mux/log-files",
    "description": "Download the log files list",
    "fn": {}
  },
  {
    "verb": "GET",
    "path": "/mux/system-time",
    "description": "Get the system time as a long. Optional QS prm 'fmt': date | duration",
    "fn": {}
  },
  {
    "verb": "GET",
    "path": "/mux/log-files/{log-file}",
    "description": "Download the log file",
    "fn": {}
  },
  {
    "verb": "DELETE",
    "path": "/mux/log-files/{log-file}",
    "description": "Delete a given log file",
    "fn": {}
  },
  {
    "verb": "POST",
    "path": "/mux/events/{topic}",
    "description": "Broadcast event (payload in the body) on specific topic. The {topic} can be a regex.",
    "fn": {}
  },
  {
    "verb": "GET",
    "path": "/custom-protocol/{content}",
    "description": "Manage custom protocol",
    "fn": {}
  },
  {
    "verb": "PUT",
    "path": "/mux/utc",
    "description": "Set 'current' UTC Date.",
    "fn": {}
  },
  {
    "verb": "GET",
    "path": "/mux/last-sentence",
    "description": "Get the last available inbound sentence",
    "fn": {}
  },
  {
    "verb": "POST",
    "path": "/mux/nmea-sentence",
    "description": "Push NMEA or AIS Sentence to cache, after parsing it. NMEA Sentence as text/plain in the body.",
    "fn": {}
  }
]
```
See how to add `http.RESTRequestManager`(s) to an `http.HTTPServer`, with the `addRequestManager` method.

# Other samples
Many other examples are available. Just look.

---
