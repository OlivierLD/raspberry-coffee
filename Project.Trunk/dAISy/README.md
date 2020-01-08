# dAISy, AIS HAT for the Raspberry Pi

##### Links
- [Wegmatt](https://wegmatt.com/).
- [Get started](https://wegmatt.com/files/dAISy%20HAT%20AIS%20Receiver%20Quickstart.pdf)


### Configure
Run that script:
[uart_control](https://github.com/itemir/rpi_boat_utils/blob/master/uart_control/uart_control)

### Run
```
$ ll /dev/ser*
1651 0 lrwxrwxrwx 1 root root 5 Jan  6 19:17 /dev/serial0 -> ttyS0
7149 0 lrwxrwxrwx 1 root root 7 Jan  6 19:17 /dev/serial1 -> ttyAMA0
 $ 
```

#### Reading `/dev/serial0` with PI4J (Module `Serial.PI4J.reader`):
```
pi@rpi-buster:~/raspberry-coffee/Serial.PI4J.reader $ ./run.sh
Read serial port, raw data
Serial Communication.
 ... connect using settings: 38400, N, 8, 1, N.
 ... data received on serial port should be displayed below.
Opening port [/dev/serial0]
Port is opened.



Got Data (44 byte(s))
!AIVDM,1,1,,A,D03Ovk0m9N>4g@ffpfpNfp0,2*38

---+--------------------------------------------------+------------------
   |  0  1  2  3  4  5  6  7  8  9  A  B  C  D  E  F  |
---+--------------------------------------------------+------------------
00 | 21 41 49 56 44 4D 2C 31 2C 31 2C 2C 41 2C 44 30  |  !AIVDM,1,1,,A,D0
01 | 33 4F 76 6B 30 6D 39 4E 3E 34 67 40 66 66 70 66  |  3Ovk0m9N>4g@ffpf
02 | 70 4E 66 70 30 2C 32 2A 33 38 0D 0A              |  pNfp0,2*38..
---+--------------------------------------------------+------------------
Got Data (44 byte(s))
!AIVDM,1,1,,B,D03Ovk0s=N>4g<ffpfpNfp0,2*5D

---+--------------------------------------------------+------------------
   |  0  1  2  3  4  5  6  7  8  9  A  B  C  D  E  F  |
---+--------------------------------------------------+------------------
00 | 21 41 49 56 44 4D 2C 31 2C 31 2C 2C 42 2C 44 30  |  !AIVDM,1,1,,B,D0
01 | 33 4F 76 6B 30 73 3D 4E 3E 34 67 3C 66 66 70 66  |  3Ovk0s=N>4g<ffpf
02 | 70 4E 66 70 30 2C 32 2A 35 44 0D 0A              |  pNfp0,2*5D..
---+--------------------------------------------------+------------------
Got Data (36 byte(s))
!AIVDM,1,1,,A,?03Ovk1Gcv1`D00,2*3C

---+--------------------------------------------------+------------------
   |  0  1  2  3  4  5  6  7  8  9  A  B  C  D  E  F  |
---+--------------------------------------------------+------------------
00 | 21 41 49 56 44 4D 2C 31 2C 31 2C 2C 41 2C 3F 30  |  !AIVDM,1,1,,A,?0
01 | 33 4F 76 6B 31 47 63 76 31 60 44 30 30 2C 32 2A  |  3Ovk1Gcv1`D00,2*
02 | 33 43 0D 0A                                      |  3C..
---+--------------------------------------------------+------------------
Got Data (36 byte(s))
!AIVDM,1,1,,B,?03Ovk1CpiT0D00,2*02

---+--------------------------------------------------+------------------
   |  0  1  2  3  4  5  6  7  8  9  A  B  C  D  E  F  |
---+--------------------------------------------------+------------------
00 | 21 41 49 56 44 4D 2C 31 2C 31 2C 2C 42 2C 3F 30  |  !AIVDM,1,1,,B,?0
01 | 33 4F 76 6B 31 43 70 69 54 30 44 30 30 2C 32 2A  |  3Ovk1CpiT0D00,2*
02 | 30 32 0D 0A                                      |  02..
---+--------------------------------------------------+------------------
Got Data (44 byte(s))
!AIVDM,1,1,,A,D03Ovk06AN>40Hffp00Nfp0,2*52

---+--------------------------------------------------+------------------
   |  0  1  2  3  4  5  6  7  8  9  A  B  C  D  E  F  |
---+--------------------------------------------------+------------------
00 | 21 41 49 56 44 4D 2C 31 2C 31 2C 2C 41 2C 44 30  |  !AIVDM,1,1,,A,D0
01 | 33 4F 76 6B 30 36 41 4E 3E 34 30 48 66 66 70 30  |  3Ovk06AN>40Hffp0
02 | 30 4E 66 70 30 2C 32 2A 35 32 0D 0A              |  0Nfp0,2*52..
---+--------------------------------------------------+------------------
Got Data (44 byte(s))
!AIVDM,1,1,,B,D03Ovk0<EN>40Dffp00Nfp0,2*53

---+--------------------------------------------------+------------------
   |  0  1  2  3  4  5  6  7  8  9  A  B  C  D  E  F  |
---+--------------------------------------------------+------------------
00 | 21 41 49 56 44 4D 2C 31 2C 31 2C 2C 42 2C 44 30  |  !AIVDM,1,1,,B,D0
01 | 33 4F 76 6B 30 3C 45 4E 3E 34 30 44 66 66 70 30  |  3Ovk0<EN>40Dffp0
02 | 30 4E 66 70 30 2C 32 2A 35 33 0D 0A              |  0Nfp0,2*53..
---+--------------------------------------------------+------------------
^C
Shutting down...
java.io.IOException: Failed to close serial file descriptor. (Error #9)
	at com.pi4j.jni.Serial.close(Native Method)
	at com.pi4j.io.serial.impl.SerialImpl.close(SerialImpl.java:359)
	at readserialport.SerialDataReader.lambda$main$1(SerialDataReader.java:81)
	at java.base/java.lang.Thread.run(Thread.java:834)
pi@rpi-buster:~/raspberry-coffee/Serial.PI4J.reader $ 
```

#### Reading `/dev/ttyS0` with Module `Serial.IO`:
```
pi@rpi-buster:~/raspberry-coffee/Serial.IO $ ./serial.dump.sh 
Make sure the device is connected through its USB socket.
Assuming Linux/Raspberry Pi
Executing sudo java -Dserial.port=/dev/ttyS0 -Dbaud.rate=38400 -Dserial.verbose=true -Djava.library.path=/usr/lib/jni -cp ./build/libs/Serial.IO-1.0-all.jar:/usr/share/java/RXTXcomm.jar sample.SerialReaderSample ...
Enter [Return]

Stable Library
=========================================
Native lib Version = RXTX-2.2pre2
Java lib Version   = RXTX-2.1-7
WARNING:  RXTX Version mismatch
	Jar version = RXTX-2.1-7
	native lib Version = RXTX-2.2pre2
Adding /dev/ttyS0 to the Serial list
== Serial Port List ==
-> /dev/ttyS0
======================
Opening port /dev/ttyS0:38400
Serial Port connected: true
IO Streams initialized
	>>> [From Serial Port] Received:
		---+--------------------------------------------------+------------------
		   |  0  1  2  3  4  5  6  7  8  9  A  B  C  D  E  F  |
		---+--------------------------------------------------+------------------
		00 | 21 41 49 56 44 4D 2C 31 2C 31 2C 2C 42 2C 3F 30  |  !AIVDM,1,1,,B,?0
		01 | 33 4F 76 6B 32 30 41 47 35 34 44 30 30 2C 32 2A  |  3Ovk20AG54D00,2*
		02 | 30 38 0D 0A                                      |  08..
		---+--------------------------------------------------+------------------
	>>> [From Serial Port] Received:
		---+--------------------------------------------------+------------------
		   |  0  1  2  3  4  5  6  7  8  9  A  B  C  D  E  F  |
		---+--------------------------------------------------+------------------
		00 | 21 41 49 56 44 4D 2C 31 2C 31 2C 2C 41 2C 3F 30  |  !AIVDM,1,1,,A,?0
		01 | 33 4F 76 6B 32 30 41 47 35 34 44 30 30 2C 32 2A  |  3Ovk20AG54D00,2*
		02 | 30 42 0D 0A                                      |  0B..
		---+--------------------------------------------------+------------------
	>>> [From Serial Port] Received:
		---+--------------------------------------------------+------------------
		   |  0  1  2  3  4  5  6  7  8  9  A  B  C  D  E  F  |
		---+--------------------------------------------------+------------------
		00 | 21 41 49 56 44 4D 2C 31 2C 31 2C 2C 41 2C 44 30  |  !AIVDM,1,1,,A,D0
		01 | 33 4F 76 6B 30 36 41 4E 3E 34 30 48 66 66 70 30  |  3Ovk06AN>40Hffp0
		02 | 30 4E 66 70 30 2C 32 2A 35 32 0D 0A              |  0Nfp0,2*52..
		---+--------------------------------------------------+------------------
	>>> [From Serial Port] Received:
		---+--------------------------------------------------+------------------
		   |  0  1  2  3  4  5  6  7  8  9  A  B  C  D  E  F  |
		---+--------------------------------------------------+------------------
		00 | 21 41 49 56 44 4D 2C 31 2C 31 2C 2C 42 2C 44 30  |  !AIVDM,1,1,,B,D0
		01 | 33 4F 76 6B 30 3C 45 4E 3E 34 30 44 66 66 70 30  |  3Ovk0<EN>40Dffp0
		02 | 30 4E 66 70 30 2C 32 2A 35 33 0D 0A              |  0Nfp0,2*53..
		---+--------------------------------------------------+------------------
^C
Monitor Interrupted
Exiting monitor

Notified (Main).
Disconnecting.
pi@rpi-buster:~/raspberry-coffee/Serial.IO $ 
```

## More dAISy specific

```
$ ./daisy.dump.sh 
Assuming Linux/Raspberry Pi
Executing sudo java -Dserial.port=/dev/ttyS0 -Dbaud.rate=38400 -Dserial.verbose=true -Djava.library.path=/usr/lib/jni -cp ./build/libs/dAISy-1.0-all.jar:/usr/share/java/RXTXcomm.jar ais.sample.AISReaderSample ...
Enter [Return]

Stable Library
=========================================
Native lib Version = RXTX-2.2pre2
Java lib Version   = RXTX-2.1-7
WARNING:  RXTX Version mismatch
	Jar version = RXTX-2.1-7
	native lib Version = RXTX-2.2pre2
RXTX Warning:  Removing stale lock file. /var/lock/LCK..ttyS0
Adding /dev/ttyS0 to the Serial list
== Serial Port List ==
-> /dev/ttyS0
======================
Opening port /dev/ttyS0:38400
Serial Port connected: true
IO Streams initialized
	>>> [From Serial Port] Received:
		---+--------------------------------------------------+------------------
		   |  0  1  2  3  4  5  6  7  8  9  A  B  C  D  E  F  |
		---+--------------------------------------------------+------------------
		00 | 21 41 49 56 44 4D 2C 31 2C 31 2C 2C 41 2C 31 35  |  !AIVDM,1,1,,A,15
		01 | 4D 6C 3D 35 35 33 41 4F 6F 3E 73 77 6C 45 57 49  |  Ml=553AOo>swlEWI
		02 | 76 49 62 47 70 4A 30 3C 31 55 2C 30 2A 32 39 0D  |  vIbGpJ0<1U,0*29.
		03 | 0A                                               |  .
		---+--------------------------------------------------+------------------
Parsed: Type:1, Repeat:0, MMSI:366808340, status:Moored, rot:13, Pos:37.7762/-122.56599 (Acc:1), COG:247.3, SOG:9.5, HDG:252
	>>> [From Serial Port] Received:
		---+--------------------------------------------------+------------------
		   |  0  1  2  3  4  5  6  7  8  9  A  B  C  D  E  F  |
		---+--------------------------------------------------+------------------
		00 | 21 41 49 56 44 4D 2C 31 2C 31 2C 2C 41 2C 44 30  |  !AIVDM,1,1,,A,D0
		01 | 33 4F 76 6B 31 54 31 4E 3E 35 4E 38 66 66 71 4D  |  3Ovk1T1N>5N8ffqM
		02 | 68 4E 66 70 30 2C 32 2A 36 41 0D 0A              |  hNfp0,2*6A..
		---+--------------------------------------------------+------------------
nmea.ais.AISParser$AISException: Message type 20. Not managed yet.
	>>> [From Serial Port] Received:
		---+--------------------------------------------------+------------------
		   |  0  1  2  3  4  5  6  7  8  9  A  B  C  D  E  F  |
		---+--------------------------------------------------+------------------
		00 | 21 41 49 56 44 4D 2C 31 2C 31 2C 2C 42 2C 44 30  |  !AIVDM,1,1,,B,D0
		01 | 33 4F 76 6B 31 62 35 4E 3E 35 4E 34 66 66 71 4D  |  3Ovk1b5N>5N4ffqM
		02 | 68 4E 66 70 30 2C 32 2A 35 37 0D 0A              |  hNfp0,2*57..
		---+--------------------------------------------------+------------------
nmea.ais.AISParser$AISException: Message type 20. Not managed yet.
	>>> [From Serial Port] Received:
		---+--------------------------------------------------+------------------
		   |  0  1  2  3  4  5  6  7  8  9  A  B  C  D  E  F  |
		---+--------------------------------------------------+------------------
		00 | 21 41 49 56 44 4D 2C 31 2C 31 2C 2C 41 2C 34 30  |  !AIVDM,1,1,,A,40
		01 | 33 4F 76 6B 31 76 40 44 44 70 44 6F 3E 6A 4F 48  |  3Ovk1v@DDpDo>jOH
		02 | 45 64 6A 43 4F 30 32 38 3B 6C 2C 30 2A 36 36 0D  |  EdjCO028;l,0*66.
		03 | 0A                                               |  .
		---+--------------------------------------------------+------------------
nmea.ais.AISParser$AISException: Message type 4. Not managed yet.
	>>> [From Serial Port] Received:
		---+--------------------------------------------------+------------------
		   |  0  1  2  3  4  5  6  7  8  9  A  B  C  D  E  F  |
		---+--------------------------------------------------+------------------
		00 | 21 41 49 56 44 4D 2C 31 2C 31 2C 2C 42 2C 34 30  |  !AIVDM,1,1,,B,40
		01 | 33 4F 76 6B 31 76 40 44 44 70 44 6F 3E 6A 4F 48  |  3Ovk1v@DDpDo>jOH
		02 | 45 64 6A 43 4F 30 32 38 3B 6E 2C 30 2A 36 37 0D  |  EdjCO028;n,0*67.
		03 | 0A                                               |  .
		---+--------------------------------------------------+------------------
nmea.ais.AISParser$AISException: Message type 4. Not managed yet.
	>>> [From Serial Port] Received:
		---+--------------------------------------------------+------------------
		   |  0  1  2  3  4  5  6  7  8  9  A  B  C  D  E  F  |
		---+--------------------------------------------------+------------------
		00 | 21 41 49 56 44 4D 2C 31 2C 31 2C 2C 42 2C 31 35  |  !AIVDM,1,1,,B,15
		01 | 4D 6C 3D 35 35 70 51 4D 6F 3E 73 6E 42 45 57 49  |  Ml=55pQMo>snBEWI
		02 | 55 49 61 47 72 66 30 3C 31 55 2C 30 2A 34 32 0D  |  UIaGrf0<1U,0*42.
		03 | 0A                                               |  .
		---+--------------------------------------------------+------------------
Parsed: Type:1, Repeat:0, MMSI:366808340, status:Moored, rot:-30, Pos:37.77603/-122.56649 (Acc:1), COG:246.9, SOG:9.3, HDG:253
	>>> [From Serial Port] Received:
		---+--------------------------------------------------+------------------
		   |  0  1  2  3  4  5  6  7  8  9  A  B  C  D  E  F  |
		---+--------------------------------------------------+------------------
		00 | 21 41 49 56 44 4D 2C 31 2C 31 2C 2C 41 2C 31 35  |  !AIVDM,1,1,,A,15
		01 | 4D 6C 3D 35 35 38 41 4E 6F 3E 73 65 68 45 57 49  |  Ml=558ANo>sehEWI
		02 | 3E 39 60 6F 6D 30 30 40 42 6B 2C 30 2A 37 31 0D  |  >9`om00@Bk,0*71.
		03 | 0A                                               |  .
		---+--------------------------------------------------+------------------
Parsed: Type:1, Repeat:0, MMSI:366808340, status:Moored, rot:33, Pos:37.77588/-122.56695 (Acc:1), COG:246.7, SOG:9.4, HDG:250
	>>> [From Serial Port] Received:
		---+--------------------------------------------------+------------------
		   |  0  1  2  3  4  5  6  7  8  9  A  B  C  D  E  F  |
		---+--------------------------------------------------+------------------
		00 | 21 41 49 56 44 4D 2C 31 2C 31 2C 2C 42 2C 44 30  |  !AIVDM,1,1,,B,D0
		01 | 33 4F 76 6B 30 73 3D 4E 3E 34 67 3C 66 66 70 66  |  3Ovk0s=N>4g<ffpf
		02 | 70 4E 66 70 30 2C 32 2A 35 44 0D 0A              |  pNfp0,2*5D..
		---+--------------------------------------------------+------------------
nmea.ais.AISParser$AISException: Message type 20. Not managed yet.
	>>> [From Serial Port] Received:
		---+--------------------------------------------------+------------------
		   |  0  1  2  3  4  5  6  7  8  9  A  B  C  D  E  F  |
		---+--------------------------------------------------+------------------
		00 | 21 41 49 56 44 4D 2C 31 2C 31 2C 2C 41 2C 34 30  |  !AIVDM,1,1,,A,40
		01 | 33 4F 76 6B 31 76 40 44 44 70 60 6F 3E 6A 4F 46  |  3Ovk1v@DDp`o>jOF
		02 | 45 64 6A 42 77 30 32 38 47 52 2C 30 2A 33 37 0D  |  EdjBw028GR,0*37.
		03 | 0A                                               |  .
		---+--------------------------------------------------+------------------
nmea.ais.AISParser$AISException: Message type 4. Not managed yet.
	>>> [From Serial Port] Received:
		---+--------------------------------------------------+------------------
		   |  0  1  2  3  4  5  6  7  8  9  A  B  C  D  E  F  |
		---+--------------------------------------------------+------------------
		00 | 21 41 49 56 44 4D 2C 31 2C 31 2C 2C 42 2C 34 30  |  !AIVDM,1,1,,B,40
		01 | 33 4F 76 6B 31 76 40 44 44 70 60 6F 3E 6A 4F 46  |  3Ovk1v@DDp`o>jOF
		02 | 45 64 6A 42 77 30 32 38 47 54 2C 30 2A 33 32 0D  |  EdjBw028GT,0*32.
		03 | 0A                                               |  .
		---+--------------------------------------------------+------------------
nmea.ais.AISParser$AISException: Message type 4. Not managed yet.
^C
Monitor Interrupted
Exiting monitor

Notified (Main).
Disconnecting.
$  
```

## NMEA.multiplexer integration
![In OpenCPN](./AIS.png)
