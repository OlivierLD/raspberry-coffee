# Raspberry PI Zero W

The Raspberry PI Zero W is a small single-board computer that
- Runs Linux (Pixel, a clone of Jessie)
- Has 512 Mb of RAM
- Has a 40-pin GPIO Header
- Has a built-in WiFi (hence the **W**) and Bluetooth Low Energy (BLE) support.
- Costs $10.

As a Linux machine, it runs whatever a Linux machine can run (Python, Java and other JVM-based languages,
NodeJS, Node-RED, etc).

We want to compare here the behavior of an NMEA Multiplexer based on Java (this project), and another
one - a prototype - based on Node-RED, specially the memory footprint.

## Multiplexer on Node-RED.

If not already there, Install [Node-RED](http://nodered.org/) on the Raspberry PI.

_March 2017_: Node-RED is already installed on the NOOBS Image. It also had some cool Nodes already available.

Among others:
- Serial
- RPi GPIO
- Sense HAT
- ...

Node-RED is based on NodeJS, itself based on V8, the Google Chrome's OpenSource JavaScript engine.
As such, NodeJS - and thus Node-RED - is programmable in JavaScript.
For those wondering, Java & Javascript are as related to each other than Ham & Hamburger (said _John Resig_).

To enable the replay of NMEA log file, we've created an extra node, available [here](https://github.com/OlivierLD/node.pi/tree/master/node-red).
> Reading a file in a synchronous way from JavaScript is not as trivial as it would sound.

To make sure we have equivalent features in both cases (Java & Node-RED), we also need an implementation in Node-RED of the features of
the BME280. More details about that soon.

_Work in Progress..._

---
_March 2017_

