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

#### First summary

```
!AIVDM,1,1,,A,D03Ovk0m9N>4g@ffpfpNfp0,2*38
!AIVDM,1,1,,B,D03Ovk0s=N>4g<ffpfpNfp0,2*5D
!AIVDM,1,1,,A,?03Ovk1Gcv1`D00,2*3C
!AIVDM,1,1,,B,?03Ovk1CpiT0D00,2*02
!AIVDM,1,1,,A,D03Ovk06AN>40Hffp00Nfp0,2*52
!AIVDM,1,1,,B,D03Ovk0<EN>40Dffp00Nfp0,2*53

!AIVDM,1,1,,B,?03Ovk20AG54D00,2*08
!AIVDM,1,1,,A,?03Ovk20AG54D00,2*0B
!AIVDM,1,1,,A,D03Ovk06AN>40Hffp00Nfp0,2*52
!AIVDM,1,1,,B,D03Ovk0<EN>40Dffp00Nfp0,2*53
```
is parsed as 
```
Type:20, Repeat:0, MMSI:3669708, status:Under way using engine, rot:-43, Pos:81.94/-250.6231 (Acc:0), COG:377.1, SOG:60.6, HDG:256
Type:20, Repeat:0, MMSI:3669708, status:Under way using engine, rot:-19, Pos:81.94/-250.6233 (Acc:0), COG:377.1, SOG:86.2, HDG:256
Type:15, Repeat:0, MMSI:3669708, status:At anchor, rot:94, Pos:0.0/22.787413 (Acc:0), COG:0.0, SOG:76.6, HDG:0
Type:15, Repeat:0, MMSI:3669708, status:At anchor, rot:79, Pos:0.0/55.99232 (Acc:1), COG:0.0, SOG:56.1, HDG:0
Type:20, Repeat:0, MMSI:3669708, status:Under way using engine, rot:25, Pos:81.92/-250.7831 (Acc:0), COG:377.1, SOG:9.4, HDG:256
Type:20, Repeat:0, MMSI:3669708, status:Under way using engine, rot:49, Pos:81.92/-250.78331 (Acc:0), COG:377.1, SOG:35.0, HDG:256
Type:15, Repeat:0, MMSI:3669708, status:Not under command, rot:1, Pos:0.0/70.847145 (Acc:0), COG:0.0, SOG:8.7, HDG:0
Type:15, Repeat:0, MMSI:3669708, status:Not under command, rot:1, Pos:0.0/70.847145 (Acc:0), COG:0.0, SOG:8.7, HDG:0
Type:20, Repeat:0, MMSI:3669708, status:Under way using engine, rot:25, Pos:81.92/-250.7831 (Acc:0), COG:377.1, SOG:9.4, HDG:256
Type:20, Repeat:0, MMSI:3669708, status:Under way using engine, rot:49, Pos:81.92/-250.78331 (Acc:0), COG:377.1, SOG:35.0, HDG:256
```
