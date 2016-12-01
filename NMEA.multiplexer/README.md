# NMEA Multiplexer
Any input (File, Serial, TCP, UDP, WebSocket, Sensors, Computations, ...), any output (File, Serial, TCP, UDP, WebSockets...), and a REST API on top of that.

### Includes
- NMEA Parser
- NMEA Strings generator
- Serial port Reader / Writer
- TCP Reader / Writer
- UDP Reader / Writer
- WebSocket client (read/write)

## Open questions
- Do we need a parser here? If it is just about multiplexing, then probably not.
- RMI protocol?
- A Cache?

## Open Issues
- UDP client

## TODO
- verbose options
- computer (calculate data from other ones: current, true wind)

### To see it at work
See the class `nmeaproviders.client.mux.GenericNMEAMultiplexer`, it uses the file `nmea.mux.properties` to define what to read, and what to re-broacdast it to. 
See it to understand its content (should be clear enough).

To compile and build:
```
 $> ../gradlew shadowJar
```
To run it, modify `mux.sh` to fit your environment, and run
```
 $> ./mux.sh
```

#### WebSockets
WebSocket protocol is supported, in input, and in output.
If needed, you can start your own local WebSocket server, running on `nodejs`.
To install it (once):
```
 $> npm install
```
Then, to run it,
```
 $> node wsnmea.js
```
or
```
 $> npm start
```
