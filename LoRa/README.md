# LoRa (Long Range)
Shows how to use an RFM95W Lora Radio module

This  board has a SPI interface, the code in this directory could belong to the `I2C.SPI` project, but the intention was to also provide the code for the Arduino Uno.

I found the code I started from on the [Adafruit](https://www.adafruit.com/product/3072) web site.

The two sketches require the same wiring:

![RX TX](./RFM95_Arduino_bb.png)

You need two Arduinos: one to transmit, one to receive.

## Next
The same, on a Raspberry PI, in Java.


## Another option
Serial communication between the Raspberry PI and the Arduino.

The Raspberry PI can communicate with the Arduino through a Serial Port.
Just use a USB A-B cable. The Serial port would usually be seen as `/dev/ttyACM0`.

> In case you have problem reading `/dev/ttyACM0`, create a symbolic link
> ```
> $ sudo ln -s /dev/ttyACM0 /dev/ttyS80
> ```
> Then try reading or writing on /dev/ttyS80

To use this pattern, see the classes in the package `arduino`, and the sketches `ArduinoRF95_RX` and `ArduinoRF95_TX`.
The scripts `runArduinoComm` and `runArduinoServer` go along with those classes.

----
