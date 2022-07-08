### A Java limit? the nanosecond?
This is an interesting challenge.

This module provides
- A pure Java implementation (`HC_SR04.java`)
- A JNI (Java Native Interface) implementation, invoking ther WiringPI/C implementation (`JNI_HC_SR04.java`)

**_The JNI implementation requires [wiringPi](http://wiringpi.com/download-and-install/)_**.

This device needs the nano-second precision, which is provided - but not garanteed - by Java.

#### Wiring

![Wiring](./HC-SR04_bb.png)

Make sure the pins you use match the code.

You can run the pure C version, see in the `C` directory, the script `c.compile` will produce the executable.

### JNI quick guide
Look into [This project](../JNISample/README.md).

---
