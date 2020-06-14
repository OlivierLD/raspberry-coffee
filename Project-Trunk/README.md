## Project Trunk
#### a.k.a. "la caisse &agrave; glingues". <!-- 🔧. -->
> _Tags_: Mad Scientist, SteamPunk, James Bond villain, etc...

![ToolBox](./toolbox-icon-30.png)
---
This folder contains projects involving the components available in other modules of this project.
> Note: Replaces the content of what was `RasPISamples`.

There are several of them - as opposed to just a big one - to minimize the number
of dependencies for each example.

They all come with a `build.gradle`, and the examples are built by a
```
 $ ../../gradlew shadowJar
```
and a script to run the example should be available as well.

- [Buttons, Reflex Game](Button-Relex)
- [PurePWM](PurePWM)
- [System resolution](System.Languages)
    - [System resolution, in several languages](System.Languages/LanguageComparison.md)
- [Weather Station](Weather.Station.Implementation)
- [Radar](RasPiRadar)
- [Robots and WebSockets](Motors)
- [Plant watering system](PlantWateringSystem)
- [Pitch and Roll](LSM303)
- [SunFlower](SunFlower)
- Boating and Sailing
  - [Polar Smoother](./PolarSmoother)
  - [Deviation Tool](Deviation.Tool)
- [REST Clients](./REST.clients) featuring 
    - a TCP watch. 👍
    - Micro-services (REST, HTTP, MQTT, the full gang!)
- ... and more.

#### TODO
- Stepper Motors with the Motor HAT
- Pure PWM
    - For LEDs
    - For Servos

### Node JS
Some of the projects in this folder require `Node.js` (and its `N`ode `P`ackage `M`anager aka `npm`) to be installed, some times with extra modules.

#### To install Node.js on the Raspberry Pi (Oct 2018)
```
 $ sudo su
 root# curl -sL https://deb.nodesource.com/setup_9.x | bash -
 root# exit
 $ sudo apt-get install -y nodejs
```

> Note: I've had problems running `node` on the Raspberry Pi Zero (Segmentation Fault)...
> Other models are OK.

When an extra module is required, then there is a `package.json` in the `node ` folder, `cd` to it, and
 just run
```
 $ npm install
```
Explanations about how to run the different node servers are given in the `README.md` inside each project.

---
