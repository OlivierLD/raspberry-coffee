# SunFlower Version 2.0
![Work In Progress](./wip.jpg)

The goal of this project is to orient a solar panel so it faces the sun as long as it is in the sky.

The [first version ](../SunFlower) used micro-servos to orient a small solar panel.

Here we want to deal with bigger panels, this will require the usage of bigger motors, like stepper-motors.

The required hardware is described in another [separate project](https://github.com/OlivierLD/3DPrinting/tree/master/OpenSCAD/SolarPanelStand).

Stepper motors will be driven by an [Adafruit Motor Hat](https://www.adafruit.com/product/2348).

Some code to look at is in [this folder](../SteppersPlayground).

### Wiring
![Wiring](./MotorHatWiring.png)

## TODO
- A REST interface.
