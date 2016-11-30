# NMEA Multiplexer
Any input (File, Serial, TCP, UDP, WebSocket...), any output (File, Serial, TCP, UDP, WebSockets...), and a REST API on top of that.

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
- Parameter for the baud rate (Serial port).

## TODO
- verbose options
- distinctions  between 
  - reader and client
  - producer (ie from sensors and transducers), computer (calculate data from other ones: current, true wind)
  - broadcaster?

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
