# WIP: Plant Watering System
We want to interface a Moisture/Humidity/Temperature sensor (https://www.adafruit.com/product/1298) with
a solenoid valve (https://www.adafruit.com/product/997), to irrigate the plants in need.

### Reading the STH10 Sensor
We start from an Arduino sketch, that can read the STH10. The 10KOhms resistor is not to be forgotten.

![Arduino wiring](./Arduino.STH10_bb.png)

This produces an output like that:

![Serial console](./serial.console.png)

The idea here is to trigger the valve when the humidity goes below a given threshold.
The valve opens a pipe connected to a tank of water.
As the valve requires a 12 Volt power supply, it with be driven by relay.

On the Raspberry PI, the code at https://github.com/drohm/pi-sht1x works fine with the following wiring:

![Raspberry Wiring](./RaspberryPI.STH10_bb.png)

---

Inspired from https://github.com/drohm/pi-sht1x, the Java code is now also available.

---

### Triggering the valve
https://www.adafruit.com/product/997

With a Relay

##### To determine
- The humidity threshold beyond which to start watering
- How long should the watering last
- How long to wait after watering before re-start measuring

### Extras
- IoT and stuff...
- Reading the sensor as an I<small><sup>2</sup></small>C device?

### Power supply
12 & 5 Volts.
- 5 for the Raspberry
- 12 for the valve

### The Hardware
The tank, the hoses

