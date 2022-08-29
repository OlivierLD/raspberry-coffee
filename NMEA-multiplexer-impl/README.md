# NMEA-multiplexer's Web Implementation (and some more)
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

Also try (requires some polishing, several values are hard-coded...)
```
$ ./mux.sh --interactive-config
```

## How it works (in short)
As it depends on the `http-tiny-server` module, the `NMEA-multiplexer` can also act as an HTTP Server (not only WebServer). When the configuration property `with.http.server` is set to `true`, the multiplexer can also serve:
- static Web pages
    - See the system properties `static.docs` and `static.zip.docs`.
- REST requests
    - Look into the class `nmea.mux.RESTImplementation`

> _**Note**_: _Any_ REST client can reach the features available in the `NMEA-multiplexer` (or the classes extending it).
> Web pages can do it - obviously - but also utilities like `curl`, `wget`, and devices like Arduino, ESP32 & friends, M5 sticks, etc.

# Other samples
Many other examples are available. Just look.

---
