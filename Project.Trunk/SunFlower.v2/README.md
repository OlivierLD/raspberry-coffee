# SunFlower Version 2.0
![Work In Progress](./wip.jpg) 

The goal of this project is to _**automatically**_ orient a solar panel so it faces the sun as long as it is in the sky.

- The [first version ](../SunFlower) used micro-servos to orient a small solar panel.
- Here we want to deal with bigger panels, this will require the usage of bigger motors, like stepper-motors.
- The required hardware is described in a [separate project](https://github.com/OlivierLD/3DPrinting/tree/master/OpenSCAD/SolarPanelStand).
- Stepper motors will be driven by an [Adafruit Motor Hat](https://www.adafruit.com/product/2348).
- Some code to look at is in [this folder](../SteppersPlayground), to play with the different options
of the stepper motors...

### Wiring
![Wiring](./MotorHatWiring.png)

### Standard output
Simple standard output, just run `run.sh`.

#### With the `calibration` option
Takes its input from the CLI, not from the Astro Thread. Use it to calibrate the device,
to make sure it is working as expected.
```
$ ./run.sh 
Starting SunFlowerDriver
------------------------------ C A L I B R A T I O N ---------------------------
To change the Azimuth (Z) value, enter 'Z=12.34', the value goes from 0 to 360.
To change the Elevation (E) value, enter 'E=23.45', the values goes from 0 to 90.
Enter PARK to park the device.
Enter 'Q' to quit.
---------------------------------------------------------------------------------
Current status: Z=180.00, Elev.=90.00
> z=213
Current status: Z=213.00, Elev.=90.00
> 
. . .
``` 
 
### ANSI Console output
Run `sunflower.main.ConsoleMain`, from the script `console.sh`.
```
- Positions -
+------+----------------------------+------+-----+
|      | Date                       | Z    |Elev |
+------+----------------------------+------+-----+
|Sun   |Tue Feb 04 14:37:42 PST 2020|216.67|27.60|
+------+----------------------------+------+-----+
|Device|Tue Feb 04 14:37:42 PST 2020|216.65|27.60|
+------+----------------------------+------+-----+
- Movements -
+-----+----------------------------+------+------+------+
|     | Date                       |from  |to    |diff  |
+-----+----------------------------+------+------+------+
|Elev.|Tue Feb 04 14:37:41 PST 2020| 28.10| 27.60|  0.50|
+-----+----------------------------+------+------+------+
|Z    |Tue Feb 04 14:37:35 PST 2020|216.15|216.65|  0.50|
+-----+----------------------------+------+------+------+
- Status -
+----------------------------+----------------------------------------------------------------+
| Date                       | Info                                                           |
+----------------------------+----------------------------------------------------------------+
|Tue Feb 04 14:33:22 PST 2020|Device Parked                                                   |
+----------------------------+----------------------------------------------------------------+
|Tue Feb 04 14:37:41 PST 2020|Move (2 steps) completed in 0.020 sec                           |
+----------------------------+----------------------------------------------------------------+
|Tue Feb 04 14:37:35 PST 2020|Move (11 steps) completed in 0.111 sec                          |
+----------------------------+----------------------------------------------------------------+
Hit Ctrl-C to stop the program

```
### HTTP Server (WiP) <img src="./cone.png" alt="WIP" width="48" height="48" align="middle">
Same as above, but no UI in the console. Data are accessible through REST requests.
See `server.sh` for details.
```
$ ./server.sh 
  >>> Running on port 8989
  Starting Program
  Hit Ctrl-C to stop the SunFlowerDriver program
  1,581,012,167,407 - Port open: 8989
  1,581,012,167,409 - http.HTTPServer now accepting requests

. . .
```

Example (early preview):
```
$ curl -X GET http://localhost:8989/sf/status
```
would produce
```json
{
    "CELESTIAL_DATA": {
        "date": "Feb 6, 2020, 10:26:30 AM",
        "epoch": 1581013590245,
        "azimuth": 146.94544209045705,
        "elevation": 29.916649314162267
    },
    "MOVING_AZIMUTH_START": {
        "date": "Feb 6, 2020, 10:26:15 AM",
        "epoch": 1581013575179,
        "deviceAzimuth": 180.0,
        "sunAzimuth": 146.88339752645817
    },
    "DEVICE_DATA": {
        "date": "Feb 6, 2020, 10:26:30 AM",
        "epoch": 1581013590245,
        "azimuth": 146.88339752645817,
        "elevation": 29.889625427506264
    },
    "DEVICE_INFO": {
        "date": "Feb 6, 2020, 10:26:14 AM",
        "epoch": 1581013574113,
        "message": "Device was parked"
    },
    "MOVING_ELEVATION_START": {
        "date": "Feb 6, 2020, 10:26:15 AM",
        "epoch": 1581013575183,
        "deviceElevation": 90.0,
        "sunElevation": 29.889625427506264
    }
}
```

### The Device
The soft of this project is designed to drive [this device](https://github.com/OlivierLD/3DPrinting/blob/master/OpenSCAD/SolarPanelStand/stl/the.full.stand.stuck.stl).
Its construction is detailed in [its repo](https://github.com/OlivierLD/3DPrinting/tree/master/OpenSCAD/SolarPanelStand).

## TODO
- An ANSI Console. &#9989; Done (...ish).
- A Web Console. WiP.
    - REST and/or WebSockets?
- A utility, to manually/interactively orient the panel from user's inputs. WiP.
    

---
