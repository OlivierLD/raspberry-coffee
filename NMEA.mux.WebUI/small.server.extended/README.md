# WIP, Full NavServer running on a Raspberry Pi Zero W
**Features**:
- 2 [Adafruit Bonnets](https://www.adafruit.com/product/3203), $4.50 each
    - One with a [132x64 oled screen](https://www.adafruit.com/product/938), $19.95, 2 push buttons, 1 switch for the power supply
    - One with the [Adafruit Ultimate GPS](https://www.adafruit.com/product/746), $39.95, and a [BME280](https://www.adafruit.com/product/2652) (Humidity, Temperature, Atmospheric Pressure), $19.95
- LiPo battery
- Buttons can be used to scroll through the different displays
    - also features a "local" menu (to be browsed with the buttons) that can execute different tasks, easy to customize like
        - Pausing the logging
        - Resuming the logging
        - Shutting down the Multiplexer
        - Shutting down the machine (see below)
        - etc...
    - Activate a screen saver mode
- A specific combination on the buttons can shut down the machine.

> Prices, August 2019.

---

This example shows how to _**extend**_  the `NavServer`.

It shows how to add features like **TWO** push-buttons, and take ownership of a screen (Nokia, SSD1306...)

It comes with a class named `navserver.ServerWithKewlButtons`, that extends the `navrest.NavServer`.
As a result, it's driven by the exact same `properties` file.

To see how to interact with the buttons (to start and stop the logging for example, or
to shutdown the whole server), look for the variables named `pbmOne` and `pbmShift`.

Implements `simple-click`, `double-click`, and `long-click`.

```java
final static PushButtonMaster pbmOne = new PushButtonMaster();
final static PushButtonMaster pbmTwo = new PushButtonMaster();
```

> Note: the code to add here is just there for the buttons management. There is _absolutely no code_
> to add for the `BMP280`, the GPS, or the `SSD1306` screen. It is all taken care of by the properties files.

> See `nmea.mux.gps.log.properties` to know how to reference those devices, and 
> `ssd1306.properties` to see how to choose the data to display on the oled screen. 

This is built just like the other examples in this module, just run
```
 $ ./builder.sh
 ```
 and follow the instructions in the console.

---

### A first prototype
| Pressure (BMP280) | Position (GPS) | Side view |
|:--:|:--:|:--:|
| ![PRMSL](./docimg/06.jpg) | ![POS](./docimg/07.jpg) | ![All the layers](./docimg/08.jpg) |
| ![PRMSL](./docimg/09.jpg) | ![POS](./docimg/10.jpg) | On top, it is powered by a USB Cable, it can also be powered by a LiPo battery at the bottom (left, 2200mAH) |

### Screenshots

| NMEA Data | Local Menu | Local Menu, 2 |
|:---------:|:---------:|:---------:|
| ![NMEA](./docimg/01.png) | ![NMEA](./docimg/02.png) | ![NMEA](./docimg/03.png) |
| Screen Saver (one dot blinking) | Complete Shutdown |
| ![NMEA](./docimg/04.png) | ![NMEA](./docimg/05.png) |

### Wiring
![Bare wiring](../Adafruit.Ultimate.GPS.RPiZero_bb.png)

![On Bonnet](../Adafruit.Ultimate.GPS.RPiZero.Bonnet.1_bb.png)

![Bonnet wiring](../Adafruit.Ultimate.GPS.RPiZero.Bonnet.2_bb.png)

### Extras
- Cue-card for the UI (depends on your implementation):
```
Button-2 + LongClick on Button-1: Shutdown (confirm with double-click within 3 seconds) 
DoubleClick on Button-1: Show local menu                                                
DoubleClick on Button-2: Screen Saver mode. Any simple-click to resume.
```

### Configurations
##### Raspberry Pi Zero
- Good for logging
- A bit challenging when acting as a server (Web and REST)

##### Raspberry Pi A+

##### Raspberry Pi B


# TODO
- Describe the way to get to the log files, download, etc.
- Document Web UI: `runner.html`, `logMgmt.html`, etc.
- Simulator for the buttons. &#9989; Done.
- 3D printed enclosure, to contain the Raspberry Pi, the 2 bonnets, and a LiPo battery.
- Go to screen saver mode after a given amount of time of inactivity. &#9989; Done.
- Compare several Raspberry Pi versions (Zero, A, B)
