# Java to Python, Python to Java, etc

## Background
Here is a thing:  
When you get a new sensor or actuator, it very often comes with the Python code you need
to put it to work. It is sometimes C (for Arduino), but never Java. This code is usually written by the
provider of your breakout board.

This means that if you want to use it from Java - like in this repo - you need to write the driver yourself, using
frameworks like `PI4J`, `diozero`, etc.  
This also means that you _do depend_ on the stability and availability of those frameworks.

Typically, PI4J itself depends on WiringPi, that has itself been recently deprecated... Ooch.    
Now you have to re-write your drivers 😩.

To avoid this mis-fortune, we could try to establish a (two-way) communication
between Python and Java... If it works, it also allows you not to re-write the drivers from Python to Java.

> Java used to implement [JSR-223](https://www.jcp.org/en/jsr/detail?id=223), to natively invoke Python (and other scripting languages),
> but it is now scheduled to be removed.  
> See [this](https://www.baeldung.com/java-working-with-python).

> Good article about the same topics, on Baeldung website, at <https://www.baeldung.com/java-working-with-python>

## Sensors, actuators
Two things to keep in mind - specially when using servers (TCP and REST). Sensors are to be read - you **_get_** data from the sensors,
and actuators are to be written to, you _**push**_ data to the actuator.  
For example:
- You _read_ data from a magnetometer
- You _push_ data to an oled screen, or a servo driver.

Typically, if you build a REST server to read data from a sensor, you'd be using the `GET` verb.
Similarly, if you want to send data to a screen, you'd be using a `POST` verb.

## Options
Several options could be considered...
- GraalVM
- Jython
- TCP and HTTP
  - HTTP can use REST

Those options will be illustrated by the content of the sub-folders, siblings of this document.

## Pros and Cons

### HTTP and REST
#### Pros
TCP and HTTP are language agnostic. Java and Python are well-equipped to write both clients and servers, 
for HTTP as well as for TCP.  
Pretty much any content type can be used, here we use `NMEA` Strings, as well as `JSON` Payloads. Both are defined by strong standards.

#### Cons
Writing the Python wrapper around the code provided with the breakout board is not a trivial job,
but the code provided here could be seen a some scaffolding for it. And if - as we said -
the goal here is _**not**_ to re-write the sensor drivers, we can probably live with that.

### Jython
#### Cons
No support - yet - for Python 3.

### GraalVM
#### Question
Runs on 32-bit architecture ?


. . .

---
