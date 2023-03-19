# NMEA Multiplexer
## With PI4J Sensors and Actuators

### Work In Progress 🚧 

This project illustrates the way to consume Sensor data, and send data to Actuators, _**in Java**_, as opposed to
what's done in the `NMEA-multiplexer`, now moved [here](https://github.com/OlivierLD/ROB/tree/master/raspberry-sailor/NMEA-multiplexer), where to external access are done using REST or TCP to talk to Python servers.

This one relies on [PI4J](https://pi4j.com/) (V1 for now).

This is the way it was implemented in the first place. That would have been too bad to dump this code...

Those consumers and forwarders are now to be treated as _**custom**_ components.

TODO: Merge with `NMEA-mux-extensions`

---
