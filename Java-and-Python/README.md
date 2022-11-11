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

## Options
Several options could be considered...
- GraalVM
- Jython
- TCP and HTTP
  - HTTP can use REST

Those options will be illustrated by the content of the sub-folders, siblings of this document.

## Pros and Cons

. . .

---
