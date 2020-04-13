# Weather Station implementation

Requires `websocket` Node module to be installed:
```bash
 $ cd node
 $ npm install websocket
```

Then run the menu:
```bash
 $ ./weather.menu
 N: Start Node server
 W: Start Weather Station reader
 K: Kill them all
 Q: Quit
 You Choose >
```

- First, start the node server [`N`]
- Then the Wether Station reader [`W`]

Use the [`K`] option to stop everything.

---

The class `weatherstation.ws.HomeWeatherStation` can take an array (comma-separated) of
`LoggerInterface` classes as System variable.

See among others `MySQLLoggerImpl` and `NMEAOverTCPLogger`, and the script `weather.station.reader.sh`.

`NMEAOverTCPLogger` is compatible with the NMEA Multiplexer project.

Same with [`NodeRED`](https://nodered.org/).
