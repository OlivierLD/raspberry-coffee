# NMEA-multiplexer, Web Implementation
This is module is an illustration of what can be done on top of the `NMEA-multiplexer`.  
This includes the way to start the Multiplexer (through its configuration file), and the way to reach the data to 
display on a web page.


## Basics
It obviously requires the NMEA-multiplexer to be running.  
Data are fetched from the `NMEA Data Cache` using REST Requests.
> For the Data Cache to be available/reachable, you need to have the property `with.http.server` set to `true`,
> and the property `init.cache` set to `true`

### Get Started now:
Build, run, see:
- `../gradlew shadowJar`
- `./mux.sh nmea.mux.replay.big.log.yaml`
- then reach <http://localhost:8080/web/basic.html> in your browser.
- and also <http://localhost:8080/web/admin.html>.

> Notice the config file `nmea.mux.replay.big.log.yaml`, look into it.

Also try (requires some polishing, several values are hard-coded...)
```
$ ./mux.sh --interactive-config
```

## Other samples
Many other examples are available.

---
