# Java to Python, Python to Java, using TCP

## Background
Here is a thing:  
When you get a new sensor or actuator, it very often comes with the Python code you need
to put it to work. It is sometimes C (for Arduino), but never Java. This code is usually written by the 
provider of your breakout board.

This mens that if you want to use it from Java - like in this repo - you need to write the driver yourself, using
frameworks like PI4J, diozero, etc.  
This also means that you _do depend_ on the stability of those frameworks.  

Typically, PI4J itself depends on WiringPi, that has itself been recently deprecated... Ooch.    
Now you have to re-write your drivers 😩.

To avoid this mis-fortune, we could try to establish a (two-way) communication
between Python and Java...

_**TCP**_ could be an option, it is socket-based, and natively supports read and write.

We would have the Java code acting as a TCP server, and we would wrap the Python code into
a TCP client.

The Java-TCP-server would:
- Start and initiate the TCP server, knowing its own IP address and port
- Start the python code - providing it IP address and port
  - The python code would then log on the Java-TCP server, and use the sensor (or actuator) data to send to the TCP server.

This would require the maintenance of two kinds of code (Java and Python), but this should not be too difficult,
the Python TCP wrapper should not be a big deal. And on top of that, it could be re-used from any language knowing how to deal with TCP.

Let's see if we can come up with some scaffolding for this structure.

