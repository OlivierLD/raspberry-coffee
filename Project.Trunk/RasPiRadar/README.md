### Radar

> Radar is an acronym, meaning **RA**dio **D**irection **A**nd **R**ange.

With a `PCA9685` and a `HC-SR04` (ultrasound distance sensor).
A servo connected to the `PCA9685` rotates degree by degree from -90&deg; to +90&deg;. It supports the
`HC-SR04` that measures the distance for each position of the servo.

Wiring:
![Radar](./rpi.radar_bb.png)

Hardware setting:

![Radar](./radar.png)

Servo enclosure [here](https://www.thingiverse.com/thing:1679838),
HC-SR04 enclosure [here](https://www.thingiverse.com/thing:452720).

Graphical User Interface will follow.

### Several Flavors
- Standalone
    Radar reading and User Interface on the Raspberry PI
- Serial interface
- TCP interface. TODO
- REST Interface. TODO

> Note: All the required components are part of this project (Serial Port interface, TCP Reader, REST server).

Serial version requires extra hardware (USB cable), TCP and REST do not.

There is a Console Interface - spitting out the data read by the device, direction and range.

Graphical User Interfaces are done with Processing.

### Comments
It appeared that running the Processing UI on the Raspberry Pi itself was a bit too demanding (see the Processing sketches named `Radar` and `RadarJNI`).
That's actually why we came up with the Serial, TCP and REST versions, that allow
_another machine_ to listen to the data flow emitted by the Raspberry Pi.

![Serial](./rpi.radar.serial_bb.png)

That seems to be the right solution for this problem.

### Processing UI
![Processing](./radar.UI.png)

See in the `Processing` module the sketch named `SerialRadar`.

### TODO
- The same, but with a `VL53L0X`, more accurate.
- Direct Servo, without the `PCA9685`.

---
