### A Java limit? the nanosecond?
This is an interesting challenge.

I was not able to have the Java code (`HC_SR04.java`) to be as efficient or performant as C, or even Python.

The class `JNI_HC_SR04.java` invokes a C implementation, and is accurate.
**_This requires [wiringPi](http://wiringpi.com/download-and-install/)_**.

This device needs the nano-second precision, which is provided - but not garanteed - by Java.

Interesting.

#### Wiring

![Wiring](./HC-SR04_bb.png)

Make sure the pins you use match the code.

You can run the pure C version, see in the `C` directory, the script `c.compile` will prduce the executable.

### JNI quick guide
Look into [This project](../JNISample/README.md).

---
