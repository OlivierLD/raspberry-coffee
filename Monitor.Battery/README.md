# How to remotely monitor...


## A Battery

##### Read the tension

##### Publish it

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

##### Pros
- Can be available where Internet is not

##### Cons
- Not Free
- Not real-time, delayed.

### WebSocket

### IoT

