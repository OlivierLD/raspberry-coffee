Big upgrade to ES6 and its Promises (like in RESTNavServer)

### Reading 2 Serial ports
No problem, with a `yaml` like this:
```yaml
#
# MUX definition.
#
name: "Read 2 serial ports, generate one log file"
context:
  with.http.server: false
  http.port: 9999
  init.cache: false
channels:
  - type: serial
    port: /dev/ttyUSB0 
    baudrate: 4800
    # sentence.filters: ~RMC
    verbose: true
  - type: serial
    port: /dev/ttyS80
    baudrate: 4800
    # sentence.filters: RMC
    verbose: true
forwarders:
  - type: file
    filename: ./data.nmea
    flush: true
    verbose: false
    # forward.01.timebase.filename=true
    # forward.01.filename.suffix=_LOG
    # forward.01.log.dir=logged
    # forward.01.split=hour
```

```
Received from Serial (/dev/ttyS80): $GPVTG,,,,,,,,,N*30
Received from Serial (/dev/ttyS80): $GPGGA,175918.00,,,,,0,00,99.99,,,,,,*65
Received from Serial (/dev/ttyS80): $GPGSA,A,1,,,,,,,,,,,,,99.99,99.99,99.99*30
Received from Serial (/dev/ttyS80): $GPGSV,1,1,03,09,,,21,14,,,21,32,,,28*7D
Received from Serial (/dev/ttyS80): $GPGLL,,,,,175918.00,V,N*49
Received from Serial (/dev/ttyUSB0): $GPGGA,175916.571,3744.9386,N,12230.4305,W,1,04,11.4,-1.2,M,,,,0000*0D
Received from Serial (/dev/ttyUSB0): $GPGSA,A,3,10,32,31,14,,,,,,,,,14.2,11.4,8.3*3D
Received from Serial (/dev/ttyUSB0): $GPGSV,3,1,12,10,10,113,27,32,28,057,26,31,64,102,29,14,54,041,25*7D
Received from Serial (/dev/ttyUSB0): $GPGSV,3,2,12,22,56,315,24,03,33,309,15,26,07,142,24,01,59,268,25*70
Received from Serial (/dev/ttyUSB0): $GPGSV,3,3,12,11,35,245,,04,21,255,,23,16,257,,25,09,058,*7F
Received from Serial (/dev/ttyUSB0): $GPRMC,175916.571,A,3744.9386,N,12230.4305,W,000.0,250.7,160500,,,A*70
Received from Serial (/dev/ttyUSB0): $GPGGA,175917.571,3744.9386,N,12230.4305,W,1,04,11.4,-1.3,M,,,,0000*0D
Received from Serial (/dev/ttyUSB0): $GPGSA,A,3,10,32,31,14,,,,,,,,,14.2,11.4,8.3*3D
Received from Serial (/dev/ttyUSB0): $GPRMC,175917.571,A,3744.9386,N,12230.4305,W,000.0,250.7,160500,,,A*71
Received from Serial (/dev/ttyS80): $GPRMC,175919.00,V,,,,,,,311219,,,N*76
Received from Serial (/dev/ttyS80): $GPVTG,,,,,,,,,N*30
Received from Serial (/dev/ttyS80): $GPGGA,175919.00,,,,,0,00,99.99,,,,,,*64
Received from Serial (/dev/ttyS80): $GPGSA,A,1,,,,,,,,,,,,,99.99,99.99,99.99*30
Received from Serial (/dev/ttyS80): $GPGSV,1,1,03,09,,,22,14,,,19,32,,,28*75
Received from Serial (/dev/ttyS80): $GPGLL,,,,,175919.00,V,N*48
Received from Serial (/dev/ttyUSB0): $GPGGA,175918.571,3744.9386,N,12230.4304,W,1,04,11.4,-1.3,M,,,,0000*03
Received from Serial (/dev/ttyUSB0): $GPGSA,A,3,10,32,31,14,,,,,,,,,14.2,11.4,8.3*3D
Received from Serial (/dev/ttyUSB0): $GPRMC,175918.571,A,3744.9386,N,12230.4304,W,000.0,250.7,160500,,,A*7F
Received from Serial (/dev/ttyS80): $GPRMC,175920.00,V,,,,,,,311219,,,N*7C
Received from Serial (/dev/ttyS80): $GPVTG,,,,,,,,,N*30
Received from Serial (/dev/ttyS80): $GPGGA,175920.00,,,,,0,00,99.99,,,,,,*6E
Received from Serial (/dev/ttyS80): $GPGSA,A,1,,,,,,,,,,,,,99.99,99.99,99.99*30
Received from Serial (/dev/ttyS80): $GPGSV,1,1,03,09,,,22,14,,,16,32,,,29*7B
Received from Serial (/dev/ttyS80): $GPGLL,,,,,175920.00,V,N*42
Received from Serial (/dev/ttyS80): $GPRMC,175921.00,V,,,,,,,311219,,,N*7D
Received from Serial (/dev/ttyS80): $GPVTG,,,,,,,,,N*30
Received from Serial (/dev/ttyS80): $GPGGA,175921.00,,,,,0,00,99.99,,,,,,*6F
Received from Serial (/dev/ttyS80): $GPGSA,A,1,,,,,,,,,,,,,99.99,99.99,99.99*30
```
