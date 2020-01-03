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

### Several tentatives to open serial ports
```
$ ./mux.sh nmea.mux.2.serial.yaml 
Using properties file nmea.mux.2.serial.yaml
Running java  -Djava.library.path=/usr/lib/jni -Dscreen.verbose=true -Dprocess.on.start=true -Dmux.properties=nmea.mux.2.serial.yaml -Dno.ais=false -Dcalculate.solar.with.eot=true -Djava.util.logging.config.file=./logging.properties   -cp ./build/libs/NMEA.multiplexer-1.0-all.jar:/usr/share/java/RXTXcomm.jar nmea.mux.GenericNMEAMultiplexer
Definition Name: Read 2 serial ports, generate one log file, for Windows.
{with.http.server=false, init.cache=false}
nmea.consumers.reader.SerialReader: There are 3 listener(s)
nmea.consumers.reader.SerialReader: There are 3 listener(s)
- Start writing to nmea.forwarders.DataFileWriter, data.nmea 
>> nmea.consumers.reader.SerialReader: Reader Running
>> nmea.consumers.reader.SerialReader: Reader Running
Stable Library
=========================================
Native lib Version = RXTX-2.2pre2
Java lib Version   = RXTX-2.1-7
WARNING:  RXTX Version mismatch
	Jar version = RXTX-2.1-7
	native lib Version = RXTX-2.2pre2
RXTX fhs_lock() Error: creating lock file: /var/lock/LCK..ttyUSB0: File exists----- Serial Port List -----
Port: /dev/ttyS80, SERIAL, free.
Found 1 port(s)
----------------------------
/dev/ttyUSB0: No Such Port      <<< Wrong, but not found anyway!..
gnu.io.NoSuchPortException
	at gnu.io.CommPortIdentifier.getPortIdentifier(CommPortIdentifier.java:218)
	at nmea.consumers.reader.SerialReader.startReader(SerialReader.java:105)
	at nmea.api.NMEAReader.run(NMEAReader.java:123)
	... Retrying to open /dev/ttyUSB0 in 1000 ms   <<< Will try again in a bit
----- Serial Port List -----
Port: /dev/ttyS80, SERIAL, free.
Port: /dev/ttyUSB0, SERIAL, free.
Port: /dev/ttyS80, SERIAL, free.
Found 3 port(s)
----------------------------
	>> Serial port /dev/ttyS80 opened after 1 try(ies)  << That one opened successfully
Port Ownership changed: type=1, Owned
This is a serial port
Reading serial port...
/dev/ttyS80:Port is open...
>> nmea.consumers.reader.SerialReader: Reader Setting Completed
Received from Serial (/dev/ttyS80): $GNRMC,101753.00,A,3744.93525,N,12230.41991,W,0.560,,030120,,,A*74
Received from Serial (/dev/ttyS80): $GNVTG,,T,,M,0.560,N,1.038,K,A*34
Received from Serial (/dev/ttyS80): $GNGGA,101753.00,3744.93525,N,12230.41991,W,1,12,0.75,-5.3,M,-29.9,M,,*5E
Received from Serial (/dev/ttyS80): $GNGSA,A,3,25,21,26,15,20,13,12,05,29,,,,1.62,0.75,1.44*13
Received from Serial (/dev/ttyS80): $GNGSA,A,3,68,73,82,67,,,,,,,,,1.62,0.75,1.44*1B
Received from Serial (/dev/ttyS80): $GPGSV,4,1,14,02,06,071,04,05,35,049,21,12,07,166,18,13,14,100,29*73
Received from Serial (/dev/ttyS80): $GPGSV,4,2,14,15,20,134,28,16,07,325,,20,24,212,20,21,46,280,13*7A
Received from Serial (/dev/ttyS80): $GPGSV,4,3,14,25,37,192,22,26,26,309,10,29,76,040,21,31,03,264,*75
Received from Serial (/dev/ttyS80): $GPGSV,4,4,14,46,46,191,27,51,43,156,*71
Received from Serial (/dev/ttyS80): $GLGSV,3,1,09,67,37,050,21,68,49,126,19,69,14,173,,73,29,262,10*64
Received from Serial (/dev/ttyS80): $GLGSV,3,2,09,74,12,330,,80,05,206,08,82,29,074,27,83,54,012,*62
Received from Serial (/dev/ttyS80): $GLGSV,3,3,09,84,23,302,*50
Received from Serial (/dev/ttyS80): $GNGLL,3744.93525,N,12230.41991,W,101753.00,A,A*6E
Received from Serial (/dev/ttyS80): $GNTXT,01,01,02,u-blox AG - www.u-blox.com*4E
Received from Serial (/dev/ttyS80): $GNTXT,01,01,02,HW UBX-M8030 00080000*60
Received from Serial (/dev/ttyS80): $GNTXT,01,01,02,ROM CORE 3.01 (107888)*2B
Received from Serial (/dev/ttyS80): $GNTXT,01,01,02,FWVER=SPG 3.01*46
Received from Serial (/dev/ttyS80): $GNTXT,01,01,02,PROTVER=18.00*11
Received from Serial (/dev/ttyS80): $GNTXT,01,01,02,GPS;GLO;GAL;BDS*77
Received from Serial (/dev/ttyS80): $GNTXT,01,01,02,SBAS;IMES;QZSS*49
Received from Serial (/dev/ttyS80): $GNTXT,01,01,02,GNSS OTP=GPS;GLO*37
Received from Serial (/dev/ttyS80): $GNTXT,01,01,02,LLC=FFFFFFFF-FFFFFFFF-FFFFFFFF-FFFFFFFF-FFFFFFFD*2F
Received from Serial (/dev/ttyS80): $GNTXT,01,01,02,ANTSUPERV=AC SD PDoS SR*3E
Received from Serial (/dev/ttyS80): $GNTXT,01,01,02,ANTSTATUS=OK*25
Received from Serial (/dev/ttyS80): $GNTXT,01,01,02,PF=3FF*4B
	>> Serial port /dev/ttyUSB0 opened after 2 try(ies)    <<< All good now!
Port Ownership changed: type=1, Owned
This is a serial port
Reading serial port...
/dev/ttyUSB0:Port is open...
>> nmea.consumers.reader.SerialReader: Reader Setting Completed
Received from Serial (/dev/ttyS80): $GNRMC,101754.00,A,3744.93536,N,12230.42034,W,0.664,,030120,,,A*73
Received from Serial (/dev/ttyS80): $GNVTG,,T,,M,0.664,N,1.229,K,A*31
Received from Serial (/dev/ttyS80): $GNGGA,101754.00,3744.93536,N,12230.42034,W,1,12,0.75,-6.3,M,-29.9,M,,*5D
Received from Serial (/dev/ttyS80): $GNGSA,A,3,25,21,26,15,20,13,12,05,29,,,,1.62,0.75,1.44*13
Received from Serial (/dev/ttyS80): $GNGSA,A,3,68,73,82,67,,,,,,,,,1.62,0.75,1.44*1B
Received from Serial (/dev/ttyS80): $GPGSV,4,1,14,02,06,071,06,05,35,049,22,12,07,166,19,13,14,100,29*73
Received from Serial (/dev/ttyS80): $GPGSV,4,2,14,15,20,134,28,16,07,325,,20,24,212,21,21,46,280,12*7A
Received from Serial (/dev/ttyS80): $GPGSV,4,3,14,25,37,192,21,26,26,309,12,29,76,040,21,31,03,264,*74
Received from Serial (/dev/ttyS80): $GPGSV,4,4,14,46,46,191,27,51,43,156,*71
Received from Serial (/dev/ttyS80): $GLGSV,3,1,09,67,37,050,20,68,49,126,20,69,14,173,,73,29,262,16*69
Received from Serial (/dev/ttyS80): $GLGSV,3,2,09,74,12,330,,80,05,206,09,82,29,074,27,83,54,012,*63
Received from Serial (/dev/ttyS80): $GLGSV,3,3,09,84,23,302,*50
Received from Serial (/dev/ttyS80): $GNGLL,3744.93536,N,12230.42034,W,101754.00,A,A*6E
```
