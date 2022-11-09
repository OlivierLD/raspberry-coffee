# Python TCP Server
Can be used from the NMEA-multiplexer, using its `TCP` channel (but not only).

## Several components
- `checksum.py` contains utilities for NMEA CheckSum.
- `NMEABuilder.py` contains NMEA String builder(s).
  - For now, generates `ZDA` sentences, based on current time, or a time provided as a parameter.
  - Uses the `checksum.py` mentioned above.
  - Will contain more string generators.
- `ZDA_generator.py`, test for the `ZDA` generation.
- `TCP_ZDA_server.py` is a TCP server.
  - The server generates a `ZDA` sentence every second (current time) and pushes it to all the connected clients.
  - This can be used from the NMEA-multiplexer, using its `TCP` channel.

---

## At work
### A first example: ZDA
We start with a TCP Server producing a ZDA Sentence, because this is simple.  
This sentence represents the current UTC Time and Date, it does not require any sensor, the current date is read from the
Operating System, a ZDA string is generated, and pushed to the connected client(s).

The method producing and sending the ZDA chains is `produce_zda`, in the script `TCP_ZDA_server.py`.
This is the method to modify and adapt when the data you want to produce come - for example - from a sensor.

### ZDA Server
To start the server (port - and other parameters - can be overridden):
```
$ python src/main/python/nmea/TCP_ZDA_server.py --port:7002 --verbose:true
```

To start a client (just an example):
```
$ python src/main/python/simple_tcp_client.py --machine-name:localhost --port:7002
```
The client should display the `ZDA` sentences produced by the server.

It works from the NMEA-multiplexer, with a `yaml` file like that one (see the `channels` section):
```yaml
name: "No GPS, using ZDA produced by a TCP server"
context:
  with.http.server: true
  http.port: 9999
  init.cache: true
channels:
  - type: tcp
    server: localhost
    port: 7002
    verbose: true
forwarders:
  - type: "tcp"
    port: 7001
```
Notice that this TCP server can run anywhere (see the `server` property above), as long as it is on a network accessible from the NMEA-multiplexer. Which brings us back to the concept of "flake computing".

## And then
The structure of `TCP_ZDA_server.py` can be used as the scaffolding for 
other TCP servers, using sensors to get the data to forward to the TCP channels.  
Many - if not all - the drivers coming with the breakout board are written in Python.
Wrapping this Python code into a similar TCP structure should be a no-brainer.

More about that below.

### Producing `MTA` and `MMB` from a `BMP180`
Look into `src/main/python/nmea/TCP_BMP180_server.py`.  
The code showing how to read a `BMP180` is in `src/main/python/sensors/bmp180/basic_101.py`.

```
$ python src/main/python/nmea/TCP_BMP180_server.py --port:7002 --verbose:true
```



--- 
