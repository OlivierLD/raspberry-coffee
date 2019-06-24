# Plant Watering System
## A real world project, hardware, software, 3D Printing

We want to interface a Moisture/Humidity sensor with
some kind or pump or valve to irrigate the plants in need.

#### BOM
- [Raspberry Pi Zero](https://www.adafruit.com/product/3400) with a [header](https://www.adafruit.com/product/2822)
- [Adafruit Perma Proto Bonnet](https://www.adafruit.com/product/3203)
- [Sparkfun Soil Moisture Sensor](https://www.sparkfun.com/products/13322)
- [MCP3008](https://www.adafruit.com/product/856) and its [socket](https://www.adafruit.com/product/2203)
- [Relay](https://www.amazon.com/WINGONEER-KY-019-Channel-Module-arduino/dp/B06XHJ2PBJ/ref=sr_1_8?keywords=relay+board&qid=1561387608&s=gateway&sr=8-8)
- If you take the full 5V approach
    - [Power Booster 1000c](https://www.adafruit.com/product/2465), [LiPo battery](https://www.adafruit.com/product/1781)
- Water Pump (2 versions, [12v](https://www.adafruit.com/product/1150), or [5v](https://www.allelectronics.com/item/pmp-20/mini-water-pump/1.html))
- [5V DC Adapter](https://www.adafruit.com/product/276)
    - If you take the 12V pump, a [12V DC Adapter](https://www.adafruit.com/product/798)
- Water hoses, clamps, wires, cable glands, etc.

Early `stl` files for the 3D-printed enclosure are in the [`3D`](./3D) directory.

<!--TODO Add a switch on the LiPo battery?-->

For the elaboration of the project, see [HISTORY.md](./HISTORY.md).

#### March 24, 2019
With the MCP3008 and the _SparkFun Soil Moisture Sensor_ and a peristaltic pump (12v), it works!

| Assembling | In the Box |
|:----------:|:----------:|
| ![Assembling](./docimg/mcp3008.version.jpg) | ![In the Box](./docimg/mcp3008.in.the.box.jpg) |
 
| Pump & Tank | Soil Sensor |
|:-----------:|:-----------:|
| ![Pump & Tank](./docimg/pump.and.tank.jpg) | ![Soil Sensor](./docimg/soil.sensor.jpg) | 

##### Working prototype
- Calibration is required for the MCP3008 & Soil Moisture Sensor. The value returned by the MCP3008 is a value between
0 and 1023, not necessarily reflecting the actual humidity.

###### To keep an eye on
- The size of the log file(s). Purge it from time to time if it is on.

#### Wiring
![2 power supplies](./SparkFunSoilMoisture_bb.png)

##### Next
The above works, but it requires 2 power supplies
- 5v for the Raspberry
- 12v for the pump

Let's see if that can be done with only one 5v outlet. The - unexpected - challenge here, is the sharing of power.
If you just plug the Raspberry Pi and the Pump on the same 5v outlet, the Raspberry Pi will
just shutdown when the pump starts. Hence problem.

After several configurations, I finally managed to come up with one that seems to work.
If someone has a better idea, please do speak up! 

With an Adafruit Bonnet, and a 5v pump (same power supply as the RPi).

> 5v pump: <https://www.allelectronics.com/item/pmp-20/mini-water-pump/1.html>

![Wiring](./SparkFun.Bonnet_bb.png)

> _Note_: wires are shown on top of the Bonnet, they are actually _under_ it.

> _Remark_: Notice the PowerBoost (500 or 1000) and the LiPo battery.
> The power supply is powering the Raspberry Pi and the booster, that in turn powers the pump, through its LiPo battery when the relay in on.
> The reason for that is that it is difficult (if even possible) to power the Raspberry Pi and the Pump from the same 5v source without caution. 
> When the pump starts, this generates a voltage drop that makes the Raspberry Pi reboot...
> Along the same lines, when the pump is primed, the power demand on start is higher than when the pump is not primed. In some (previous) configurations,
> the system was working OK when the pump was not primed (like the first tests), but was failing (rebooting) when the pump was already primed, 
> like during a subsequent watering.
> In this configuration, the booster and its LiPo battery are acting as a buffer on the power supply line, and dissociate the pump and the Raspberry Pi.
> I was not able to work around this problem with capacitors (this does not mean it is not possible)...

| Bonnet top | Bonnet bottom |
|:----------:|:-------------:|
| ![top](./docimg/bonnet.top.jpg) | ![bottom](./docimg/bonnet.bottom.jpg) |
| Card box prototype | Assembling |
| ![proto](./docimg/card.box.jpg) | ![assembling](./docimg/assembling.jpg) |
| 3D printed enclosure | Closed | 
| ![one](./docimg/in.the.box.00.jpg) | ![two](./docimg/in.the.box.01.jpg) |
| Good to go | At work | 
| ![three](./docimg/in.the.box.02.jpg) | ![at work](./docimg/at.work.jpg) | 

#### June 2019: bonus
If after watering, the humidity does not go up, that may mean that the tank is empty.
In that case, you have the possibility to send an email to a list of recipients, telling them to do something about it. 

### Flowchart

![Flowchart](./docimg/PWS.flowchart.svg)

<small>For the diagram above, I used [draw.io](https://www.draw.io/), cool tool.</small>

### REST
Most features are available through REST Services.
Any REST client (`PostMan`, `curl`, etc) can reach them.

To get the list of endpoints:
```
 $ curl http://[raspberrypi.address]:8088/pws/oplist
```
Get the program's parameters:
```
 $ curl -X GET http://192.168.42.5:8088/pws/pws-parameters
 {"humidityThreshold":75,"wateringTime":10,"resumeWatchAfter":120}
```

Set the program's parameters:
```
 $ curl -X PUT -H "Content-Type:application/json" -d "{\"humidityThreshold\":75,\"wateringTime\":10,\"resumeWatchAfter\":120}" http://192.168.42.5:8088/pws/pws-parameters 
```
or
```
 $ curl --request PUT --header "Content-Type:application/json" --data "{\"humidityThreshold\":75,\"wateringTime\":10,\"resumeWatchAfter\":120}" http://192.168.42.5:8088/pws/pws-parameters 
```
etc...

#### Remark about the Soil Humidity Sensor
As you would see, the Soil Humidity Sensor is not strictly measuring the soil humidity.
With the way it is hooked on the `MCP3008`, it is actually measuring the soil's conductivity.

You will need to calibrate it - by doing some tests - to establish the required relationship
between the data read by the sensor and the actual humidity of the soil of you plant(s), to come
up with the right threshold for the plant watering.

The position of the watering hose, relative to the humidity sensor will also impact the
value given to the `--resume-after` parameter. 

#### Web UI
![Web UI](./docimg/webindex.png)

#### Ideas to move on...
- A Web UI without external libraries (JQuery is great, but not required with ES6).

---
