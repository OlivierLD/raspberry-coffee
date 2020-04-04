## Linear Potentiometers as feedback
Linear potentiometer like a [`B10K` or similar](https://www.allelectronics.com/item/nltp-50k/50k-linear-taper-pot-6mm-shaft/1.html) are called this way because their resistance varies linearly
when their knob is turned.

![Chart](./images/chart.png)

Because of this feature, they can also be used as a feedback device.

Let's say you have a motor (servo, Stepper, DC motor...) that rotates some hardware 
(like a door, a solar panel, etc.), and you need to know what the position (angle in this case) of this hardware device.

It the linear potentiometer is conveniently hooked to the hardware, then it can certainly be used
to measure this angle (possibly after some calibration step).

#### Test hardware
For the sake of this demo, we provide some 3D-printed device, `STL` and `scad` files are available
[here](https://github.com/OlivierLD/3DPrinting/tree/master/OpenSCAD/MiscParts).

| ![One](./images/linear.pot.stand.jpg) | ![Two](./images/linear.pot.stand.2.jpg) |
|:-------------------------------------:|:---------------------------------------:|

Wire it like this:
![wiring](https://dwengo.org/wp-content/uploads/2019/07/potentiometer.jpg)

With this setting, you turn the knob by hand (this replaces the motor), and we will see
what the resistance of the potentiometer is.

---
