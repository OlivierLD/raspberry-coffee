See <https://medium.com/@mahesh_joshi/raspberry-pi-3-and-arduino-communication-via-bluetooth-hc-05-5d7b6f162ab3>

Bluetooth is a pier-to-pier communication protocol based on a Serial Communication.

Once two devices are **paired** with Bluetooth, the communication between them is just a regular Serial Communication, a demoed below. 

### To get started
I used an Arduino UNO with a `HC-05` module, to act as a bluetooth device,
and some Python code to run on the Raspberry Pi, acting as a Bluetooth client.

![Wiring](./Arduino.HC-05_bb.png)

Upload the following code on the Arduino (available in `bt.101.py`):
```
/*
 * Use the LED_BUILTIN, 
 * no resistor needed, no extra led.
 */
#define ledPin LED_BUILTIN
int state = 0;

void setup() {
  pinMode(ledPin, OUTPUT);
  digitalWrite(ledPin, LOW);
  Serial.begin(9600); // 38400); // Default communication rate of the Bluetooth module
}

void loop() {
  if (Serial.available() > 0) { // Checks whether data is comming from the serial port
    state = Serial.read(); // Reads the data from the serial port
  }

  if (state == '0') {
    digitalWrite(ledPin, LOW); // Turn LED OFF
    Serial.println("LED: OFF"); // Send back, to the phone, the String "LED: ON"
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

> _Note_: we use the `BUILTIN_LED`, the red one labeled `L` on the left side of then Arduino on the picture above.

From the Raspberry Pi, run once:
```
$ sudo apt-get install pi-bluetooth
$ sudo apt-get install bluetooth bluez blueman
```
and reboot.

With the Arduino with its `HC-05` module up and running, pair your device from the Raspberry Pi desktop (use `1234` for the code) as explained [here](https://medium.com/@mahesh_joshi/raspberry-pi-3-and-arduino-communication-via-bluetooth-hc-05-5d7b6f162ab3).

Then, run this code on the Raspberry Pi
```python
#!/usr/bin/env python3
import serial
import time

port = serial.Serial("/dev/rfcomm0", baudrate=9600)
 
# reading and writing data from and to arduino serially.                                      
# rfcomm0 -> this could be different
data = 0
while True:
  print("DIGITAL LOGIC -- > SENDING...")
  port.write(str.encode(str(data)))
  data = 1 if data == 0 else 0
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
