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

Example (early preview):
```
$ curl http://localhost:8989/sf/status
{
  "CELESTIAL_DATA":{
    "date":"Feb 6, 2020, 8:52:59 AM",
    "azimuth":126.61237681273008,
    "elevation":17.201838275625548 },
  "MOVING_AZIMUTH_START":{
    "date":"Feb 6, 2020, 8:52:25 AM",
    "deviceAzimuth":180.0,
    "sunAzimuth":126.50198638792624 },
  "DEVICE_DATA":{
    "date":"Feb 6, 2020, 8:52:59 AM",
    "azimuth":126.50198638792624,
    "elevation":17.109124088002982 },
  "DEVICE_INFO":{
    "date":"Feb 6, 2020, 8:52:24 AM",
    "message":"Device was parked" },
  "MOVING_ELEVATION_START":{
    "date":"Feb 6, 2020, 8:52:25 AM",
    "deviceElevation":90.0,
    "sunElevation":17.109124088002982 }
}
```

### The Device
The soft of this project is designed to drive [this device](https://github.com/OlivierLD/3DPrinting/blob/master/OpenSCAD/SolarPanelStand/stl/the.full.stand.stuck.stl).
Its construction is detailed in [its repo](https://github.com/OlivierLD/3DPrinting/tree/master/OpenSCAD/SolarPanelStand).

## TODO
- An ANSI Console. &#9989; Done (...ish).
- A Web Console. WiP.
    - REST and/or WebSockets?
    

---
