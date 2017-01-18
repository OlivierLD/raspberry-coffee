# How to remotely monitor...

## A Battery
 This is an example intending to demonstrate how to publish information  gathered from the Raspberry PI.
 The information here is the tension (aka voltage) of a battery.
 The battery can be in a remote place, and from wherever you are, you want to make sure its level does not
 drop below a given threshold.

##### Read the tension
 As the Raspberry PI's GPIO pins are all digital ones, to read the battery's tension, you need an Analog to Digital Converter (ADC).
 As you can see in the Fritzing diagram below, we will use here an MCP3008. It is cheap, and good enough for our purpose.

##### Publish it
 This is where we have several options, which we discuss below.

### The battery monitoring trinket
![The hardware](./12-volts.monitor_bb.png "The Hardware")
How to build and connect.

#### NMEA?
NMEA stands for National Marine Electronics Association. It is one of the oldest IT standards.

Battery Voltage can be described by a sentence like
```
 $AAXDR,U,12.34,V,TRINKET*3C
```
Many NMEA parsers are available. NMEA could be an option to consider, in case the
management of the output of the trinket above was to be automated.

### By email
If there is Internet access in the location where the trinket is, and
if you have an email account, then sending the battery voltage by email could be an option, keeping in mind
that it is not a _real time_ communication, there is always a delay between the moment when an email
is sent and the moment when it is received.

It can go both ways though. The Raspberry PI can send emails, and receive some.
As long as the _received_ email complies with a given format, it can be parsed and then managed accordingly.

##### Pros
- Free
- Easy

##### Cons
- Requires Internet Network connection
- Not real-time, delayed.

### By SMS
If you do not have Internet coverage in your location, you could use a device like a `FONA` tio reach out to a cell-phone network

##### Pros
- Can be available where Internet is not

##### Cons
- Not Free
- Not real-time, delayed.

### Outernet ?
[Outernet](https://outernet.is/) might also be something to consider, if you are _really_ out of reach of any kind of network (at sea, far in the desert, etc).

More soon.

##### Pros
- Available everywhere on Earth.

##### Cons
- Requires extra hardware
- Not real-time, delayed.
- Slow

### WebSocket
Very cool technology

##### Pros
- Easy
- Real time
- Supported on clients and servers.

##### Cons
- Requires Internet Network connection
- Requires a WebSocket server

### IoT


##### Pros
- Can be free
- Flexible

##### Cons
- Requires Internet Network connection
