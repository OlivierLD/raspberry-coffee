# WIP: Plant Watering System
We want to interface a Moisture/Humidity/Temperature sensor (https://www.adafruit.com/product/1298) with
a solenoid valve (https://www.adafruit.com/product/997), to irrigate the plants in need.

We start from an Arduino sketch, that can read the STH10. The 10KOhms resistor is not to be forgotten.

![Arduino wiring](./Arduino.STH10_bb.png)

This produces an out put like that:

![Serial console](./serial.console.png)

The idea here is to trigger the valve when the humidity goes below a given threshold.
The valve opens a pipe connected to a tank of water.
As the valve requires a 12 Volt power supply, it with be driven by relay.

On the Raspberry PI, the code at https://github.com/drohm/pi-sht1x works fine with the following wiring:

![Raspberry Wiring](./RaspberryPI.STH10_bb.png)

---

More soon, Java code.
