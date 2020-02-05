# SunFlower Version 2.0
![Work In Progress](./wip.jpg)

The goal of this project is to orient a solar panel so it faces the sun as long as it is in the sky.

- The [first version ](../SunFlower) used micro-servos to orient a small solar panel.
- Here we want to deal with bigger panels, this will require the usage of bigger motors, like stepper-motors.
- The required hardware is described in a [separate project](https://github.com/OlivierLD/3DPrinting/tree/master/OpenSCAD/SolarPanelStand).
- Stepper motors will be driven by an [Adafruit Motor Hat](https://www.adafruit.com/product/2348).
- Some code to look at is in [this folder](../SteppersPlayground), to play with the different options
of the stepper motors...

### Wiring
![Wiring](./MotorHatWiring.png)

### ANSI Console output (WiP)
```
- Positions -
+------+----------------------------+------+-----+
|      | Date                       | Z    |Elev |
+------+----------------------------+------+-----+
|Sun   |Tue Feb 04 14:37:42 PST 2020|216.67|27.60|
+------+----------------------------+------+-----+
|Device|Tue Feb 04 14:37:42 PST 2020|216.65|27.60|
+------+----------------------------+------+-----+
- Movements -
+-----+----------------------------+------+------+------+
|     | Date                       |from  |to    |diff  |
+-----+----------------------------+------+------+------+
|Elev.|Tue Feb 04 14:37:41 PST 2020| 28.10| 27.60|  0.50|
+-----+----------------------------+------+------+------+
|Z    |Tue Feb 04 14:37:35 PST 2020|216.15|216.65|  0.50|
+-----+----------------------------+------+------+------+
- Status -
+----------------------------+----------------------------------------------------------------+
| Date                       | Info                                                           |
+----------------------------+----------------------------------------------------------------+
|Tue Feb 04 14:33:22 PST 2020|Device Parked                                                   |
+----------------------------+----------------------------------------------------------------+
|Tue Feb 04 14:37:41 PST 2020|Move (2 steps) completed in 0.020 sec                           |
+----------------------------+----------------------------------------------------------------+
|Tue Feb 04 14:37:35 PST 2020|Move (11 steps) completed in 0.111 sec                          |
+----------------------------+----------------------------------------------------------------+
Hit Ctrl-C to stop the program

```
### The Device
The soft of this project is designed to drive [this device](https://github.com/OlivierLD/3DPrinting/blob/master/OpenSCAD/SolarPanelStand/stl/the.full.stand.stuck.stl).
Its construction is detailed in [its repo](https://github.com/OlivierLD/3DPrinting/tree/master/OpenSCAD/SolarPanelStand).

## TODO
- An ANSI Console. &#9989; Done (...ish).
- A Web Console.
    - REST or WebSockets?
    

---
