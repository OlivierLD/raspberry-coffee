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
> _**Note**_: We've chosen here to produce NMEA Sentences, this will be what the client(s) will receive.
> This is done because of the target we have in mind here (NMEA-multiplexer).   
> We could very well use other formats, like JSON. It's all about agreeing on the content type 
> between the client and the server.

### A first example: ZDA
We start with a TCP Server producing a ZDA Sentence, because this is simple.  
This sentence represents the current UTC Time and Date, it does not require any sensor, the current date is read from the
Operating System, a ZDA string is generated, and pushed to the connected client(s).

The method producing and sending the ZDA chains is `produce_zda`, in the script `TCP_ZDA_server.py`.
This is the method to modify and adapt when the data you want to produce come - for example - from a sensor.

### ZDA Server, and client
To start the server (port - and other parameters - can be overridden):
```
$ python src/main/python/nmea/TCP_ZDA_server.py [--port:7002] [--verbose:true]
```

To start a client (just an example):
```
$ python src/main/python/simple_tcp_client.py [--machine-name:localhost] [--port:7002]
Usage is:
python3 /Users/olivierlediouris/repos/raspberry-coffee/java-python/Java-TCP-Python/src/main/python/simple_tcp_client.py [--machine-name:127.0.0.1] [--port:7002] [--verbose:true|false]
	where --machine-name: and --port: must match the server's settings.

connecting to localhost port 7002
...Connected
---------------------- H E L P --------------------------------------
To exit, type Q, QUIT, or EXIT (lower or upper case). Or try '.'
To see this message again, type H (lower or upper case)
To set/unset the output message to JSON, type J (lower or upper case)
To pause the continuous display, type P (lower or upper case)
To resume a paused display, type R (lower or upper case)
---------------------------------------------------------------------
Data from Server: $PYZDA,102849.00,12,11,2022,00,00*7F
Data from Server: $PYZDA,102850.00,12,11,2022,00,00*77
Data from Server: $PYZDA,102851.00,12,11,2022,00,00*76
Data from Server: $PYZDA,102852.00,12,11,2022,00,00*75
Data from Server: $PYZDA,102853.00,12,11,2022,00,00*74
Data from Server: $PYZDA,102854.00,12,11,2022,00,00*73
. . .
^C
Ctrl+C intercepted!
Data from Server: $PYZDA,102855.00,12,11,2022,00,00*72

Empty message. Doing nothing.
closing socket
$
```
The client above displays the `ZDA` sentences produced by the server.

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
  - type: tcp
    port: 7001
```
Notice that this TCP server can run anywhere (see the `server` property above), as long as it is on a network accessible from the NMEA-multiplexer. Which brings us back to the concept of "flake computing".

## And then
The structure of `TCP_ZDA_server.py` can be used as the scaffolding for 
other TCP servers, using sensors to get the data to forward to the TCP channels.  
The method to look at to implement your own code is `produce_zda`.  
Many - if not all - the drivers coming with the breakout board are written in Python.
Wrapping this Python code into a similar TCP structure should be a no-brainer.

More about that below.

### Producing `XDR`, `MTA` and `MMB` from a `BMP180`
Look into `src/main/python/nmea/TCP_BMP180_server.py`.  
The code showing how to read a `BMP180` is in `src/main/python/sensors/bmp180/basic_101.py`.

```
$ python src/main/python/nmea/TCP_BMP180_server.py --port:7002 --verbose:true
```

### Reading a `LIS3MDL`, producing `HDM` and `HDG` strings
Like above,  
Look into `src/main/python/nmea/TCP_LIS3MDL_server.py`.  

#### TODO
Calibration for magnetometers.


### Etc...

--- 
