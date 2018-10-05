## Project Trunk
This folder contains projects involving the components available in other modules of this project.
> Note: Will eventually replace the content of `RasPISamples`.

There are several of them - as opposed to just a big one - to minimize the number
of dependencies for each example.

They all come with a `build.gradle`, and the examples are built by a
```
 $ ../../gradlew shadowJar
```
and a script to run the example should be available as well.

- [Buttons, Reflex Game](Button.Reflex)
- [PurePWM](PurePWM)
- [System resolution](System.Languages)
    - [System resolution, in several languages](System.Languages/LanguageComparison.md)
- [Weather Station](Weather.Station)
- [Radar](RasPiRadar)
- [Robot on wheels](Motors)
- [Plant watering system](PlantWateringSystem)
- [Pitch and Roll](LSM303)
- ... and more.

#### TODO
- Stepper Motors with the Motor HAT
- Arduino Communication
- Pure PWM
    - For LEDs
    - For Servos
- FONA
- Document the MeArm interface in `ADCs.Servos.JoySticks` or `Motors`.

### Node JS
Some of the projects in this folder require `NodeJS` (and its `N`ode `P`ackage `M`anager aka `npm`) to be installed, some times with extra modules.

#### To install NodeJS on the Raspberry Pi (Oct 2018)
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
