# No Code at all!
See in the `build.gradle`, this project only involves the `NMEA-multiplexer`.

It's all driven by `nmea.mux.gps.log.properties` or `nmea.mux.gps.log.yaml` (they are equivalent).

With _optional_ Web user interface (look into `small-server-extended` for details)

![At work](./logger.two.jpeg)

> _**Note**_: 
> As you would see, there is _**NO**_ `src` directory in this project.  
> The `build.gradle` pulls everything it needs from the `:NMEA-multiplexer` module.

---
