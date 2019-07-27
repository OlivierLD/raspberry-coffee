## Lib RxTx, Serial communication.
This involves the classes located in the package `gnu.io`.
To install this package (on Raspberry Pi, or more generally on Ubuntu), type
```
$> sudo apt-get install librxtx-java
```
This is an possible alternative to the `com.pi4j.io.serial` package (that comes with PI4J).
Gives a bit more flexibility, specially on the callback side.

It requires:
* on the runtime command line `-Djava.library.path=/usr/lib/jni`
* in the classpath `/usr/share/java/RXTXcomm.jar`, to compile or run.

> If you keep having `UnsatisfiedLinkError` when running the samples, check [this post](http://lukealderton.com/blog/posts/2016/javalangunsatisfiedlinkerror-no-rxtxserial-in-javalibrarypath/)...
> It worked for me. I ran the following command to fix it:
```bash
 $ sudo cp /usr/lib/jni/* /usr/lib
```

### To run the example
The example illustrates a Serial communication between the Raspberry Pi (or any other machine) and an Arduino Uno.

To proceed:
- Upload the sketch `ArduinoSerialEvent.ino` on the Arduino Uno
- Connect the USB cable, between the Arduino and the Raspberry Pi
- Make sure the Serial port names match in the `ArduinoEchoClient.java` (or use the System variable named `serial.port`)

The example send several sentences to the Arduino, the Arduino sends the sentences back to the
Raspberry Pi, in reverse order. A string like 'arduino' will be sent back as 'oniudra'.
The example is reversing the sentences of a `Lorem ipsum` paragraph (look in the code for details).

```
$ ../gradlew runArduinoSample
-> /dev/ttyUSB0
-> /dev/ttyAMA0
-> /dev/ttyACM0

Arduino connected: true
IO Streams initialized
Writing to the serial port.
	>>> [From Arduino] Received:
		0A                                                  .
	>>> [From Arduino] Received:
		20 2C 74 69 6C 65 20 67 6E 69 63 73 69 70 69 64      ,tile gnicsipid
		61 20 72 65 75 74 65 74 63 65 73 6E 6F 63 20 2C     a reutetcesnoc ,
		74 65 6D 61 20 74 69 73 20 72 6F 6C 6F 64 20 6D     tema tis rolod m
		75 73 70 69 20 6D 65 72 6F 4C 0D 0A                 uspi meroL..
	>>> [From Arduino] Received:
		0A                                                  .
	>>> [From Arduino] Received:
		20 2E 74 61 70 74 75 6C 6F 76 20 74 61 72 65 20      .taptulov tare
		6D 61 75 71 69 6C 61 20 61 6E 67 61 6D 20 65 72     mauqila angam er
		6F 6C 6F 64 20 74 65 65 72 6F 61 6C 20 74 75 20     olod teeroal tu
		74 6E 75 64 69 63 6E 69 74 20 64 6F 6D 73 69 75     tnudicnit domsiu
		65 20 68 62 69 6E 20 79 6D 6D 75 6E 6F 6E 20 6D     e hbin ymmunon m
		61 69 64 20 64 65 73 0D 0A                          aid des..
	>>> [From Arduino] Received:
		0A                                                  .
	>>> [From Arduino] Received:
		20 2E 74 61 75 71 65 73 6E 6F 63 20 6F 64 6F 6D      .tauqesnoc odom
		6D 6F 63 20 61 65 20 78 65 20 70 69 75 71 69 6C     moc ae xe piuqil
		61 20 74 75 20 6C 73 69 6E 20 73 69 74 72 6F 62     a tu lsin sitrob
		6F 6C 20 74 69 70 69 63 73 75 73 20 72 65 70 72     ol tipicsus repr
		6F 63 6D 61 6C 6C 75 20 6E 6F 69 74 61 74 20 69     ocmallu noitat i
		63 72 65 78 65 20 64 75 72 74 73 6F 6E 20 73 69     crexe durtson si
		75 71 20 2C 6D 61 69 6E 65 76 20 6D 69 6E 69 6D     uq ,mainev minim
		20 64 61 20 6D 69 6E 65 20 69 73 69 77 20 74 55      da mine isiw tU
		0D 0A                                               ..
	>>> [From Arduino] Received:
		0A                                                  .
	>>> [From Arduino] Received:
		20 2C 74 61 75 71 65 73 6E 6F 63 20 65 69 74 73      ,tauqesnoc eits
		65 6C 6F 6D 20 65 73 73 65 20 74 69 6C 65 76 20     elom esse tilev
		65 74 61 74 75 70 6C 75 76 20 6E 69 20 74 69 72     etatupluv ni tir
		65 72 64 6E 65 68 20 6E 69 20 72 6F 6C 6F 64 20     erdneh ni rolod
		65 72 75 69 72 69 20 6D 75 65 20 6C 65 76 20 6D     eruiri mue lev m
		65 74 75 61 20 73 69 75 44 0D 0A                    etua siuD..
	>>> [From Arduino] Received:
		0A                                                  .
	>>> [From Arduino] Received:
		20 6D 69 73 73 69 6E 67 69 64 20 6F 69 64 6F 20      missingid oido
		6F 74 73 75 69 20 74 65 20 6E 61 73 6D 75 63 63     otsui te nasmucc
		61 20 74 65 20 73 6F 72 65 20 6F 72 65 76 20 74     a te sore orev t
		61 20 73 69 73 69 6C 69 63 61 66 20 61 6C 6C 75     a sisilicaf allu
		6E 20 74 61 69 67 75 65 66 20 75 65 20 65 72 6F     n taiguef ue ero
		6C 6F 64 20 6D 75 6C 6C 69 20 6C 65 76 0D 0A        lod mulli lev..
	>>> [From Arduino] Received:
		0A                                                  .
	>>> [From Arduino] Received:
		2E 69 73 69 6C 69 63 61 66 20 61 6C 6C 75 6E 20     .isilicaf allun
		74 69 61 67 75 65 66 20 65 74 20 65 72 6F 6C 6F     tiaguef et erolo
		64 20 73 69 75 64 20 65 75 67 75 61 20 74 69 6E     d siud eugua tin
		65 6C 65 64 20 6C 69 72 7A 7A 20 6D 75 74 61 74     eled lirzz mutat
		70 75 6C 20 74 6E 65 73 65 61 72 70 20 74 69 64     pul tnesearp tid
		6E 61 6C 62 20 69 75 71 0D 0A                       nalb iuq..
	>>> [From Arduino] Received:
		0A                                                  .
	>>> [From Arduino] Received:
		20 74 61 72 65 63 61 6C 70 20 6D 69 7A 61 6D 20      tarecalp mizam
		64 6F 75 71 20 64 69 20 67 6E 69 6D 6F 64 20 74     douq di gnimod t
		65 69 64 72 65 70 6D 69 20 6C 69 68 69 6E 20 65     eidrepmi lihin e
		75 67 6E 6F 63 20 6E 6F 69 74 70 6F 20 64 6E 65     ugnoc noitpo dne
		66 69 65 6C 65 20 73 69 62 6F 6E 20 61 74 75 6C     fiele sibon atul
		6F 73 20 6D 75 63 20 72 6F 70 6D 65 74 20 72 65     os muc ropmet re
		62 69 6C 20 6D 61 4E 0D 0A                          bil maN..
	>>> [From Arduino] Received:
		0A                                                  .
	>>> [From Arduino] Received:
		20 3B 6D 61 74 69 73 6E 69 20 6D 65 74 61 74 69      ;matisni metati
		72 61 6C 63 20 74 6E 65 62 61 68 20 6E 6F 6E 20     ralc tnebah non
		69 70 79 54 20 2E 6D 75 73 73 61 20 6D 69 73 73     ipyT .mussa miss
		6F 70 20 72 65 63 61 66 0D 0A                       op recaf..
	>>> [From Arduino] Received:
		0A                                                  .
	>>> [From Arduino] Received:
		20 2E 6D 65 74 61 74 69 72 61 6C 63 20 6D 75 72      .metatiralc mur
		6F 65 20 74 69 63 61 66 20 69 75 71 20 73 69 69     oe ticaf iuq sii
		20 6E 69 20 73 69 74 6E 65 67 65 6C 20 73 75 73      ni sitnegel sus
		75 20 74 73 65 0D 0A                                u tse..
	>>> [From Arduino] Received:
		0A                                                  .
	>>> [From Arduino] Received:
		20 2E 73 75 69 70 65 61 73 20 74 6E 75 67 65 6C      .suipeas tnugel
		20 69 69 20 64 6F 75 71 20 73 75 69 6C 20 65 6D      ii douq suil em
		20 65 72 65 67 65 6C 20 73 65 72 6F 74 63 65 6C      eregel serotcel
		20 74 6E 75 72 65 76 61 72 74 73 6E 6F 6D 65 64      tnurevartsnomed
		20 73 65 6E 6F 69 74 61 67 69 74 73 65 76 6E 49      senoitagitsevnI
		0D 0A                                               ..
	>>> [From Arduino] Received:
		0A                                                  .
	>>> [From Arduino] Received:
		20 2E 6D 75 72 6F 74 63 65 6C 20 6D 75 69 64 75      .murotcel muidu
		74 65 75 73 6E 6F 63 20 6D 65 6E 6F 69 74 61 74     teusnoc menoitat
		75 6D 20 72 75 74 69 75 71 65 73 20 69 75 71 20     um rutiuqes iuq
		2C 73 75 63 69 6D 61 6E 79 64 20 73 75 73 73 65     ,sucimanyd susse
		63 6F 72 70 20 6D 61 69 74 65 20 74 73 65 20 73     corp maite tse s
		61 74 69 72 61 6C 43 0D 0A                          atiralC..
	>>> [From Arduino] Received:
		0A                                                  .
	>>> [From Arduino] Received:
		20 2C 6D 61 72 61 6C 63 20 6D 75 72 61 70 20 73      ,maralc murap s
		75 6D 61 74 75 70 20 63 6E 75 6E 20 6D 61 75 71     umatup cnun mauq
		20 2C 61 63 69 68 74 6F 67 20 61 72 65 74 74 69      ,acihtog aretti
		6C 20 6D 61 75 71 20 65 72 61 74 6F 6E 20 74 73     l mauq eraton ts
		65 20 6D 75 72 69 4D 0D 0A                          e muriM..
	>>> [From Arduino] Received:
		0A                                                  .
	>>> [From Arduino] Received:
		20 2E 61 6D 69 63 65 64 20 61 74 6E 69 75 71 20      .amiced atniuq
		74 65 20 61 6D 69 63 65 64 20 61 74 72 61 75 71     te amiced atrauq
		20 61 6C 75 63 61 65 73 20 72 65 70 20 73 69 74      alucaes rep sit
		61 74 69 6E 61 6D 75 68 20 73 61 6D 72 6F 66 20     atinamuh samrof
		6D 75 72 61 72 65 74 74 69 6C 20 74 69 72 65 75     murarettil tireu
		73 6F 70 65 74 6E 61 0D 0A                          sopetna..
	>>> [From Arduino] Received:
		0A                                                  .
	>>> [From Arduino] Received:
		2E 6D 75 72 75 74 75 66 20 6E 69 20 73 65 6E 6D     .murutuf ni senm
		65 6C 6C 6F 73 20 74 6E 61 69 66 20 2C 69 72 61     ellos tnaif ,ira
		6C 63 20 6D 75 72 61 70 20 72 75 74 6E 65 64 69     lc murap rutnedi
		76 20 73 69 62 6F 6E 20 63 6E 75 6E 20 69 75 71     v sibon cnun iuq
		20 2C 69 70 79 74 20 6F 64 6F 6D 20 6D 65 64 6F      ,ipyt odom medo
		45 0D 0A                                            E..
Data written to the serial port.
Arduino connected: false
Done.

Process finished with exit code 0
```
If you are running from a Raspberry Pi, you need sudo access. Run the script named `runArduinoSample` instead.

On the Raspberry Pi, the Serial port needs to be accessed as `root`. In case Gradle cannot do that, you can use the provided script named `runArduionoSample`.
```
$ ./runArduinoSample
```
The output is the same as above.

--------------------------------------------------------------------------------------------------------------------------------------------------------------

A GPS with a USB cable would also produce interesting output.

#### Wiring for [Adafruit Ultimate GPS](https://www.adafruit.com/product/746)
![Adafruit GPS](./Adafruit.Ultimate.GPS_bb.png)

In the script `runGPSSample.sh`:
```
#!/bin/bash
#
# Read a GPS
#
CP=./build/libs/Serial.IO-1.0.jar
CP=$CP:/usr/share/java/RXTXcomm.jar
#
SERIAL_PORT=/dev/ttyS0 # RPi. This port may vary
BAUD_RATE=9600
#
JAVA_OPTS="-Dserial.port=$SERIAL_PORT -Dbaud.rate=$BAUD_RATE"
. . . 

```

### USB-Key GPS
[This one](https://www.amazon.com/HiLetgo-G-Mouse-GLONASS-Receiver-Windows/dp/B01MTU9KTF/ref=sr_1_9?crid=28K7KG7FGLBO6&keywords=u-blox+usb+gps&qid=1564254911&s=gateway&sprefix=U-blox%2Caps%2C207&sr=8-9) works fine as well.


---
