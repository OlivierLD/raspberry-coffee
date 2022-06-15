Bluetooth is a wireless and pier-to-pier (P2P) communication protocol based on a Serial Communication.

In other words, each device (each pier) is listening or writing to a Serial port, and those ports are linked together by a wireless protocol to send and receive data.

**It can be seen it like this**:
```
  Device A > SerialPort ..))))  wifi  ((((.. SerialPort < Device B 
```
That can seem uselessly complicated, but it is not. Think again.
Bluetooth is taking care of the serial-to-wireless (and vice-versa) part (taken care of by the `pi-bluetooth`, `bluetooth`, `bluez` and `blueman` packages mentioned further), 
and as a developer, all you need to care about is the serial communication, on either side.

Once two devices are **paired** with Bluetooth, the communication between them is just a regular Serial Communication, as demoed below.

> **Think of Bluetooth as a serial cable**. It works if both ends are connected.
> You cannot connect if another cable is already there.

> Example: Once your Bluetooth mouse is paired with a computer A, a computer B cannot access it.
> Your BT mouse does not drive more than one computer. This could make some sense 🤓 

I started form this: <https://medium.com/@mahesh_joshi/raspberry-pi-3-and-arduino-communication-via-bluetooth-hc-05-5d7b6f162ab3>

Learn about Bluetooth devices in sight:
```
$ hcitool scan
Scanning ...
	98:D3:61:FD:67:23	HC-05
	18:65:90:CF:BF:80	olivs-mac
```

### To get started
#### Act 1: Arduino as Bluetooth device, Raspberry Pi as Bluetooth client
I used an Arduino UNO with an [`HC-05` module](https://www.allelectronics.com/item/hc-05/hc-05-bluetooth-module/1.html), to act as a bluetooth device,
and some Python (and then Java) code to run on the Raspberry Pi, acting as a Bluetooth client.

> Note: there is a cool and free Arduino on-line emulator at <https://www.tinkercad.com/dashboard?type=circuits>.
> Log in with a Google account, and you're good to go.

The sketch running on the Arduino turns a led on or off, depending on what's read from the Bluetooth device.

The Raspberry Pi will send serial data to the Bluetooth device, and we should then see the Arduino's led go on and off.
In return, the Raspberry receives the status of the led, sent by the Arduino, through the Bluetooth device.

This way, it demonstrates how the Raspberry Pi can _send_ and _receive_ data over Bluetooth.   

![Wiring](./Arduino.HC-05_bb.png)

Upload the following code on the Arduino (available in `src/main/arduino/bluetooth.101`):
```c
/*
 * Use the LED_BUILTIN, 
 * no resistor needed, no extra led.
 */
#define ledPin LED_BUILTIN
int state = 0; // This is the character code.

void setup() {
  pinMode(ledPin, OUTPUT);
  digitalWrite(ledPin, LOW);
  Serial.begin(9600);  // Default communication rate of the Bluetooth module
}

void loop() {
  if (Serial.available() > 0) { // Checks whether data is comming from the serial port
    state = Serial.read(); // Reads the data from the serial port
  }

  if (state == '0') {
    digitalWrite(ledPin, LOW); // Turn LED OFF
    Serial.println("LED: OFF"); // Send back to the client, the String "LED: ON"
    state = 0;
  } else if (state == '1') {
    digitalWrite(ledPin, HIGH);
    Serial.println("LED: ON");;
    state = 0;
  }
}
```
> _Note_: Both `HC-05` and the code uploader for the Arduino are using the serial port. If
> the `HC-05` is active, the code cannot be uploaded through the serial port.
> Just unplug (red wire) the `HC-05` when you want to upload your code, re-plug it after that.

> _Note_: we use the `BUILTIN_LED`, the red one labeled `L` on the left side of the Arduino on the picture above.

From the Raspberry Pi, run once:
```
$ sudo apt-get install pi-bluetooth
$ sudo apt-get install bluetooth bluez blueman
```
and reboot.

With the Arduino with its `HC-05` module up and running, pair your device from the Raspberry Pi desktop (use `1234` for the code) as explained [here](https://medium.com/@mahesh_joshi/raspberry-pi-3-and-arduino-communication-via-bluetooth-hc-05-5d7b6f162ab3).
`hcitool` command mentioned above can help.

Then, run this code on the Raspberry Pi (available in `bt.101.py`)
```python
#!/usr/bin/env python3
import serial
import time

port = serial.Serial("/dev/rfcomm0", baudrate=9600)

# reading and writing data from and to arduino serially.                                      
# rfcomm0 -> this could be different
data = 0
while True:
  print("Digital Logic --> Sending...")
  port.write(str.encode(str(data)))
  data = 1 if data == 0 else 0  # Flip value
  rcv = port.readline()
  if rcv:
    print(rcv)
  time.sleep(3)
```
Notice the port name, and the baud rate.

Run it:
```
$ ./bt.101.py
```
You should see the led blinking every 3 seconds on the Arduino.

### From Java
Compile and archive the code provided here:
```
$ ../gradlew clean shadowJar
```

_Seems there is a problem to fix on the Raspberry Pi, with `librxtx-java`, when trying to read `/dev/rfcomm0`_:
```
$ ./java.101.sh 
Stable Library
=========================================
Native lib Version = RXTX-2.2pre2
Java lib Version   = RXTX-2.1-7
WARNING:  RXTX Version mismatch
	Jar version = RXTX-2.1-7
	native lib Version = RXTX-2.2pre2
== Serial Port List ==
-> /dev/ttyS0
======================
Opening port /dev/rfcomm0:9600
Port /dev/rfcomm0 not found, aborting
```

That seems to work OK on Mac though (the Serial port is - in this case - `/dev/tty.HC-05-DevB`):
```
$ ./java.101.sh 
Stable Library
=========================================
Native lib Version = RXTX-2.2pre2
Java lib Version   = RXTX-2.1-7
WARNING:  RXTX Version mismatch
	Jar version = RXTX-2.1-7
	native lib Version = RXTX-2.2pre2
== Serial Port List ==
-> /dev/tty.HC-05-DevB
-> /dev/tty.Bluetooth-Incoming-Port
-> /dev/tty.usbmodeme2df64a32
-> /dev/cu.Bluetooth-Incoming-Port
-> /dev/cu.HC-05-DevB
-> /dev/cu.usbmodeme2df64a32
======================
Opening port /dev/tty.HC-05-DevB:9600
---------------------
Flipping the switch  
---------------------
Ctrl + C to stop
Bluetooth connected: true
IO Streams initialized
Writing to the serial port.
	>>> [From Bluetooth] Received:
		---+--------------------------------------------------+------------------
		   |  0  1  2  3  4  5  6  7  8  9  A  B  C  D  E  F  |
		---+--------------------------------------------------+------------------
		00 | 4C 45 44 3A 20 4F 4E 0D 0A                       |  LED: ON..
		---+--------------------------------------------------+------------------
Data written to the serial port.
Writing to the serial port.
	>>> [From Bluetooth] Received:
		---+--------------------------------------------------+------------------
		   |  0  1  2  3  4  5  6  7  8  9  A  B  C  D  E  F  |
		---+--------------------------------------------------+------------------
		00 | 4C 45 44 3A 20 4F 46 46 0D 0A                    |  LED: OFF..
		---+--------------------------------------------------+------------------
Data written to the serial port.
Writing to the serial port.
	>>> [From Bluetooth] Received:
		---+--------------------------------------------------+------------------
		   |  0  1  2  3  4  5  6  7  8  9  A  B  C  D  E  F  |
		---+--------------------------------------------------+------------------
		00 | 4C 45 44 3A 20 4F 4E 0D 0A                       |  LED: ON..
		---+--------------------------------------------------+------------------
Data written to the serial port.
Writing to the serial port.
	>>> [From Bluetooth] Received:
		---+--------------------------------------------------+------------------
		   |  0  1  2  3  4  5  6  7  8  9  A  B  C  D  E  F  |
		---+--------------------------------------------------+------------------
		00 | 4C 45 44 3A 20 4F 46 46 0D 0A                    |  LED: OFF..
		---+--------------------------------------------------+------------------

. . .

Data written to the serial port.
Writing to the serial port.
	>>> [From Bluetooth] Received:
		---+--------------------------------------------------+------------------
		   |  0  1  2  3  4  5  6  7  8  9  A  B  C  D  E  F  |
		---+--------------------------------------------------+------------------
		00 | 4C 45 44 3A 20 4F 4E 0D 0A                       |  LED: ON..
		---+--------------------------------------------------+------------------
Data written to the serial port.
Writing to the serial port.
	>>> [From Bluetooth] Received:
		---+--------------------------------------------------+------------------
		   |  0  1  2  3  4  5  6  7  8  9  A  B  C  D  E  F  |
		---+--------------------------------------------------+------------------
		00 | 4C 45 44 3A 20 4F 46 46 0D 0A                    |  LED: OFF..
		---+--------------------------------------------------+------------------
^C
Exiting...
```
The led is indeed blinking on the Arduino.

On the Raspberry Pi though, the `PI4J` approach seems to work. See `bt.pi4j.BtPi4j103.java`.

Run this on the Raspberry Pi:
```
$ sudo java -cp ./build/libs/Bluetooth-1.0-all.jar bt.pi4j.BtPi4j103 --device /dev/rfcomm0
Let's get started
[HEX DATA]   4C,45,44,3A,20,4F,46,46,0D,0A
[ASCII DATA] LED: OFF

[HEX DATA]   4C,45,44,3A,20,4F,4E,0D,0A
[ASCII DATA] LED: ON

[HEX DATA]   4C,45,44,3A,20,4F,46,46,0D,0A
[ASCII DATA] LED: OFF

[HEX DATA]   4C,45,44,3A,20,4F,4E,0D,0A
[ASCII DATA] LED: ON

[HEX DATA]   4C,45,44,3A,20,4F,46,46,0D,0A
[ASCII DATA] LED: OFF
. . .
```
Also try to run the script `java.BT.sh`.

### Full story (on Raspberry Pi)
- Start the Arduino, load and run the `bluetooth.101.ino` sketch, with the `HC-05` device connected to it, its `RXD` on `1`, its `TXD` on `0`.
- From the Raspberry Pi desktop, pair the `HC-05` device:

![Add device](./docimg/BT.01.png)

Choose `Add Device...`

![HC-05](./docimg/BT.02.png)

Choose the `HC-05` device, click `Pair`

![1234](./docimg/BT.03.png)

When prompted, enter the code `1234`

![Done](./docimg/BT.04.png)

You are done! A port `/dev/rfcomm0` should have been created.

Now, still from the Raspberry Pi,  you can run the script `java.basic.pi4j.sh`, to turn the internal red led on or off: 
```
$ ./java.basic.pi4j.sh 
Assuming Linux/Raspberry Pi
Let's get started...
(Q to quit) > 1
	---+--------------------------------------------------+------------------
	   |  0  1  2  3  4  5  6  7  8  9  A  B  C  D  E  F  |
	---+--------------------------------------------------+------------------
	00 | 31 0D 0A                                         |  1..
	---+--------------------------------------------------+------------------
Waiting for reply...
	---+--------------------------------------------------+------------------
	   |  0  1  2  3  4  5  6  7  8  9  A  B  C  D  E  F  |
	---+--------------------------------------------------+------------------
	00 | 4C 45 44 3A 20 4F 4E 0D 0A                       |  LED: ON..
	---+--------------------------------------------------+------------------
	New line detected
	Waiter thread notified
	Waiter thread released
Dispatching response [LED: ON
]
	---+--------------------------------------------------+------------------
	   |  0  1  2  3  4  5  6  7  8  9  A  B  C  D  E  F  |
	---+--------------------------------------------------+------------------
	00 | 4C 45 44 3A 20 4F 4E                             |  LED: ON
	---+--------------------------------------------------+------------------
>> Received [LED: ON]
(Q to quit) > 0
	---+--------------------------------------------------+------------------
	   |  0  1  2  3  4  5  6  7  8  9  A  B  C  D  E  F  |
	---+--------------------------------------------------+------------------
	00 | 30 0D 0A                                         |  0..
	---+--------------------------------------------------+------------------
Waiting for reply...
	---+--------------------------------------------------+------------------
	   |  0  1  2  3  4  5  6  7  8  9  A  B  C  D  E  F  |
	---+--------------------------------------------------+------------------
	00 | 4C 45 44 3A 20 4F 46 46 0D 0A                    |  LED: OFF..
	---+--------------------------------------------------+------------------
	New line detected
	Waiter thread notified
	Waiter thread released
Dispatching response [LED: OFF
]
	---+--------------------------------------------------+------------------
	   |  0  1  2  3  4  5  6  7  8  9  A  B  C  D  E  F  |
	---+--------------------------------------------------+------------------
	00 | 4C 45 44 3A 20 4F 46 46                          |  LED: OFF
	---+--------------------------------------------------+------------------
>> Received [LED: OFF]
(Q to quit) > q
	Exiting...
Out of the loop
Serial is closed, bye!

Exiting the loop
$ 
```
You should have seen the red led turned on when entering `1`, and off when entering `0`.

### Morse Code generator
Here's an eloquent example, illustrating all the above. From a Terminal, you send a string (like `get me a pizza with pickles`) through bluetooth,
and the targeted Arduino is blinking it in morse code.

Wired as above, upload the `morse.generator.ino` to the Arduino. Make sure you've paired the `HC-05` as expected, and on the Raspberry Pi, 
run `java.basic.pi4j.sh`.

#### With 2 Serial ports
In the previous Arduino sketches, we've seen that the Serial Monitor and the `HC-05` are using the same serial port.
This is why you need to unplug the `HC-05` when uploading a sketch.

It is possible though to have a Serial Monitor on its default port, and the `HC-05` on another one.
See for this the sketch `bluetooth.spy.ino`, it replicates the features of the `morse.generator.ino`, but with two serial ports.
It will take a string provided by the user, blink the led in morse accordingly, and eventually return the 
translated morse string to the user.

See in it the usage of 
```C
#define rxPin 2
#define txPin 3
```
and
```C
 SoftwareSerial btSerial = SoftwareSerial(rxPin, txPin); 
```
Default pins (for the Serial Monitor) are `0` and `1`, here we use `2` and `3` for the `HC-05`.
This requires a slight modification in the wiring:
![New wiring](./Arduino.HC-05_bb.v2.png)

This way, you do not need to unplug the `HC-05` to upload a sketch, the program (`basic.SimpleSerialPI4JCommunication`) interacts with the `HC-05` on its own port, 
and you can have the desired output in the Serial Monitor of the Arduino IDE.

You can run the script named `java.basic.pi4j.sh` from the Raspberry Pi.
![2 outputs](./2.serial.ports.png)

##### With a piezo buzzer
Now, not only you can see the light blinking, but with a buzzer like [that](https://www.allelectronics.com/item/sbz-204/3-18-vdc-piezo-buzzer/1.html),
you can also hear it beeping.

In the code of the Arduino sketch (`bluetooth.spy.ino`), comment or uncomment this line:
```C
#define buzzerPin 8
```
If uncommented, then you can wire your circuit like that:
![Buzzer wiring](./Arduino.HC-05.buzzer_bb.png)

The buzzer's hot wire to the pin #8, the other grounded.

### TODO
- Raspberry **as** a Bluetooth device
    - As an OBD (see below) server?
    - See [this](https://ubidots.com/blog/setup-raspberry-wifi-ubidots-send-data/)


## On Board Diagnostic (ODB)
ODB is used in the car industry to convey sensor data. It can run over Bluetooth.
We'll see here if it can be considered as a sibling of NMEA or not.

> Any vehicle manufacture from 1996 or later is required by law to have the OBD-II computer system. 

> Most cars have OBD now, even mine... Mine has a socket in which I could plug a cheap and small device like [that one](https://www.amazon.com/gp/product/B0746H9Y9Z/ref=ppx_yo_dt_b_search_asin_image?ie=UTF8&psc=1),
> and I was able to read the data from some free app running on my phone (like [Torque Lite](https://play.google.com/store/apps/details?id=org.prowl.torquefree&hl=en_US)).

#### To check
- On OBD:
    - [Wikipedia](https://en.wikipedia.org/wiki/OBD-II_PIDs) 
    - <https://pypi.org/project/obd/>
        - Github [repo](https://github.com/brendan-w/python-OBD)
- OBD Dataset <https://www.kaggle.com/vbandaru/data-from-obd-on-board-diagnostics>
- <https://www.obdsol.com/knowledgebase/obd-software-development/reading-real-time-data/>
- OBD Codes: <http://www.fastfieros.com/tech/diagnostic_trouble_codes_for_obdii.htm>
- Sparkfun's [Getting Started with OBD-II](https://learn.sparkfun.com/tutorials/getting-started-with-obd-ii/all).
- Instructables' [Control Bluetooth LE Devices From a Raspberry Pi](https://www.instructables.com/id/Control-Bluetooth-LE-Devices-From-A-Raspberry-Pi/).
- An [Arduino OBD-II Simulator](https://github.com/dplanella/arduino-odb2sim.git), by David Planella.

---
