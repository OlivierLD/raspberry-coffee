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

- Externalize data (properties file). Ok.
