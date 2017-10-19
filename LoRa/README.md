# LoRa (Long Range)
Shows how to use an RFM95W Lora Radio module

This  board has a SPI interface, the code in this directory could belong to the `I2C.SPI` project, but the intention was to also provide the code for the Arduino Uno.

I found the code I started from on the [Adafruit](https://www.adafruit.com/product/3072) web site.

The two sketches require the same wiring:

![RX TX](./RFM95_Arduino_bb.png)

You need two Arduinos: one to transmit, one to receive.

## Next
The same, on a Raspberry PI, in Java. _That_ is a project...


## Another option
Serial communication between the Raspberry PI and the Arduino.

The Raspberry PI can communicate with the Arduino through a Serial Port.
Just use a USB A-B cable. The Serial port would usually be seen as `/dev/ttyACM0`.

> In case you have problem reading `/dev/ttyACM0`, create a symbolic link
> ```bash
> $ sudo ln -s /dev/ttyACM0 /dev/ttyS80
> ```
> Then try reading or writing on /dev/ttyS80
> 
> To unlink:
> ```bash
> $ sudo unlink /dev/ttyS80
> ```

To use this pattern, see the classes in the package `arduino`, and the sketches `ArduinoRF95_RX` and `ArduinoRF95_TX`.
The scripts `runArduinoComm` and `runArduinoServer` go along with those classes.

#### Next
Tests on the range of the LoRa.

### Example
#### Broadcast NMEA Data
Using the NMEA Multiplexer, based on its structure, we've written a `LoRaPublisher`.

On one machine (Raspberry PI or not), connected to an Arduino UNO with the
sketch `ArduinoRF95_RX` running on it, start the Arduino Server:
```
 $ ./runArduinoServer
 Make sure you have uploaded the right sketch on the Arduino, and connected it through its USB cable.
 Stable Library
 =========================================
 Native lib Version = RXTX-2.2pre2
 Java lib Version   = RXTX-2.1-7
 WARNING:  RXTX Version mismatch
 	Jar version = RXTX-2.1-7
 	native lib Version = RXTX-2.2pre2
 == Serial Port List ==
 -> /dev/ttyS80
 -> /dev/ttyS0
 ======================
 Opening port /dev/ttyS80:115200
 Enter 'Q' at the prompt to quit.
 Arduino connected: true
 IO Streams initialized
 ?>
```

Then from the Raspberry PI with the NMEA Multiplexer, connected to an Arduino UNO
running the sketch `ArduinoRF95_TX` running on it:

```
$ ./mux.sh nmea.mux.replay.log.properties
Using properties file nmea.mux.replay.log.properties
.......... serverSocket waiting (TCP:7001).
Stable Library
=========================================
Native lib Version = RXTX-2.2pre2
Java lib Version   = RXTX-2.1-7
WARNING:  RXTX Version mismatch
	Jar version = RXTX-2.1-7
	native lib Version = RXTX-2.2pre2
== Serial Port List ==
-> /dev/tty.usbmodem1411
-> /dev/cu.usbmodem1411
-> /dev/tty.Bluetooth-Incoming-Port
-> /dev/cu.Bluetooth-Incoming-Port
======================
Opening port /dev/cu.usbmodem1411:115200
Arduino connected: true
IO Streams initialized
Received [LORA-0001: Arduino LoRa TX Test.]
Received [LORA-0003: LoRa radio init OK.]
Received [LORA-0005: Set Freq to: 915.00]
Received [LORA-0006: Now ready to send messages]
Received [LORA-0010: Transmitting...]
 (72)}]d [LORA-0011: Sending {$GPRMC,161145.252,A,3720.1538,N,12143.0135,W,000.0,116.3,170617,,,A*77
Received [LORA-0012: Waiting for packet (send) to complete...]
Received [LORA-0013: Waiting for reply...]
Received [LORA-0008]
Dropping GGA
Dropping GSA
Received [LORA-0010: Transmitting...]
 (72)}]d [LORA-0011: Sending {$GPRMC,161304.252,A,3720.1306,N,12142.9689,W,000.0,102.1,170617,,,A*74
Received [LORA-0012: Waiting for packet (send) to complete...]
Received [LORA-0013: Waiting for reply...]
Received [LORA-0008]
Dropping GGA
Dropping GSA
Received [LORA-0010: Transmitting...]
 (72)}]d [LORA-0011: Sending {$GPRMC,161416.252,A,3720.1319,N,12142.9689,W,000.0,004.9,170617,,,A*71
Received [LORA-0012: Waiting for packet (send) to complete...]
Received [LORA-0013: Waiting for reply...]
Received [LORA-0008]
Dropping GSA
Received [LORA-0010: Transmitting...]
```

This `forwarder` filters the `NMEA` sentences, it sends only the `RMC` ones.
As a result, they're received by the RF95 Server:

```
 Received [$GPRMC,161145.252,A,3720.1538,N,12143.0135,W,000.0,116.3,170617,,,A*77]
 Received [$GPRMC,161304.252,A,3720.1306,N,12142.9689,W,000.0,102.1,170617,,,A*74]
 Received [$GPRMC,161416.252,A,3720.1319,N,12142.9689,W,000.0,004.9,170617,,,A*71]
 Received [$GPRMC,161537.253,A,3720.1417,N,12142.9655,W,001.9,334.7,170617,,,A*7C]
```
... this is where you could do something more interesting with them.

This could for example be a way to know where a remote vehicle is, and plot its position on a map.

> And to nail it down one more time, `NMEA` has been designed to convey sensor data.
> I've still not found any reason _not_ to use it. The fact that it's one of the oldest IT standards
> does not make it look like archeology (to me).


---
