![Raspberry Coffee](./raspberryCoffee.png)
### Raspberry Coffee
#### Java code and wiring for the Raspberry PI, featuring reusable libraries and snippets ####
It uses the [PI4J library](http://pi4j.com).

```
$ curl -s get.pi4j.com | sudo bash
```

---
This project contains Java code, mostly translated from Python, dedicated to usually *one* board (like BMP180, LSM303, etc).
More consistent samples can be found in the RasPISamples project, where several components have been assembled together.
Do take a look, it also comes with a readme file.

---
To get started as quickly as possible, and not only for this project, from scratch:

#### Setup a brand new Raspberry Pi
- Install Raspian (not NOOBS) as explained at https://www.raspberrypi.org/learning/software-guide/quickstart/, and burn your SD card
    - Depending on the OS you burn the SD card from, the procedure varies. Well documented in the link above.
- Boot on the Raspberry with the new SD card, USB keyboard and HDMI screen attached to it (if this is an old RPi, use a USB WiFi dongle too)
    - It should boot to the Graphical Desktop.
- Connect to your local network
- Use RPi-Config (from the Desktop GUI, Menu > Preferences > Raspberry Pi Configuration) to:
    - enable needed interfaces (ssh, serial, spi, i2c, VNC, etc)
        - `ssh` and `VNC` will allow remote access to your Raspberry Pi, the others depend on the projects you want to work on.
        - This can be modified or reverted at any time.
    - setup config (keyboard, locale, etc)
    - change pswd, hostname
- Reboot (and now, you can use `ssh` if it has been enabled above) and reconnect

- From a terminal, run the following commands:
```
$ sudo apt-get update
$ sudo apt-get install vim
```
- setup your `.bashrc` as needed, adding for example lines like
```
alias ll="ls -lisah"
```
- recent Raspian's come with a development environment that includes
    - JRE & JDK
    - git
    - python
    - C Compiler (gcc, g++)

```
#  Optional: sudo apt-get install -y curl git build-essential default-jdk
#  Optional too, to install nodejs and npm:
$ sudo su
root# curl -sL https://deb.nodesource.com/setup_9.x | bash -
root# exit
$ sudo apt-get install -y nodejs
```
- make sure what you might need is installed, by typing:
```
$ java -version
$ javac -version
$ git --version
$ python --version
$ python3 --version
$ gcc -v
$ node -v
$ npm -v
```
- some utilities, to make sure they are present, type:
```
$ which scp
$ which curl
$ which wget
```
- You can use VNC (if enabled in the config above)
    - Run `vncserver` from a terminal, and use `VNC Viewer` from another machine to connect.

- If you need AI and Deep Learning (Anaconda, Jupyter notebooks, TensorFlow, Keras), follow [this link](https://medium.com/@margaretmz/anaconda-jupyter-notebook-tensorflow-and-keras-b91f381405f8).
    - or type (not suitable for the Raspberry Pi):
    ```
    $ wget https://repo.anaconda.com/archive/Anaconda3-5.3.0-Linux-x86_64.sh
    ```
    - [Anaconda on Raspberry Pi](https://qiita.com/jpena930/items/eac02cb4e635bfba83d8)
    - [Jupyter Notebooks on Raspberry Pi](https://www.instructables.com/id/Jupyter-Notebook-on-Raspberry-Pi/)

You're ready to rock!

--------------
- [Main highlights](./Papers/README.md)
---
_Note:_
Java code is compiled into `class` files, that run on a Java Virtual Machine (`JVM`). Java is not the only language that runs a `JVM`, this project also contains some small samples of
other JVM-aware languages, invoking and using the features of this project.

Those samples include Scala, Groovy, Kotlin..., and the list is not closed!

See in the [OthetJVM.languages](https://github.com/OlivierLD/raspberry-pi4j-samples/tree/master/OtherJVM.languages) directory.

---
_Note:_
This project uses `gradle` and `git`. `Gradle` will be installed automatically if it is not present on your system,
it uses the gradle wrapper (`gradlew`).

`Git` is usually installed on Linux and Mac, but not on all versions of Windows. On Windows, you need to install the [`git bash shell`](http://lmgtfy.com/?q=install+git+bash+shell+on+windows), and run _in it_ the commands mentioned in this document.
Recent versions of Windows (like Windows 10) seem to come with a git command available in  a Terminal. But this forward-slash/back-slash story
remains in your way, I have not tested it.

---
To build it, clone this project (this repo), make sure the script named `gradlew` is executable, and execute `gradlew`.
```
 Prompt> git clone https://github.com/OlivierLD/raspberry-pi4j-samples.git
 Prompt> cd raspberry-pi4j-samples
 Prompt> chmod +x gradlew
 Prompt> ./gradlew [--daemon] build
```
You are expecting an end like that one:
```


BUILD SUCCESSFUL in 55s
97 actionable tasks: 17 executed, 80 up-to-date
Prompt>
```
See the `gradle` web site for info about Gradle.

We will also be using the `shadowJar` gradle plugin is several projects.
This plugin is aggregating the required classes _and all their dependencies_ into a single archive, called a `fat Jar`. This simplifies the syntax of the `classpath`.

Typically, this operation will be run like this:
```
 Prompt> cd RESTNavServer
 Prompt> ../gradlew shadowJar
```
The expected archive will be produced in the local `build/libs` directory.


> _Important_ : If `JAVA_HOME` is not set at the system level, you can set it in `set.gradle.env` and execute it before running `gradlew`:
```
 Prompt> . ./set.gradle.env
```

<i>Note:</i> If you are behind a firewall, you need a proxy. Mention it in all the files named <code>gradle.propetries</code>, and in <b>all</b> the <code>build.gradle</code> scripts, uncomment the following two lines:
<pre>
// ant.setproxy(proxyhost: "$proxyHost", proxyport: "$proxyPort") //, proxyuser="user", proxypassword="password")
// compileJava.dependsOn(tellMeProxy)
</pre>

---
### Developing **on** the Raspberry PI, or Developing **for** the Raspberry PI ?

To write code, the simplest editor is enough. I have used `vi` for ages, mostly because this was the only one available, but also because it _**is**_ indeed good enough.
`vi` is available on the Raspberry PI, `nano` too, graphical editors like `gedit`, `geany` are even easier to use, on a grahical desktop.

All the code provided here can be built from Gradle (all gradle scripts are provided), _**on the Raspberry PI**_ itself.
The Raspberry PI is self sufficient, if this is all you have, nothing is preventing you from accessing **_all_** the features presented here.

But let us be honest, Integrated Development Environments (IDE) are quite cool.
In my opinion, IntelliJ leads the pack, and Eclipse, JDeveloper, NetBeans follow. Cloud9 provides amazing features, on line.
Smaller ones like GreenFoot, BlueJ are also options to consider.


Those two last ones might be able to run on a Raspberry PI, but forget about the others..., they use way too much RAM.
 The features they provide definitely increase productivity, and when you use them, you learn as you code. Code-insight, auto-completion
 and similar features are here to help. And I'm not even talking about the *remote debugging* features they provide as well.

 So, as the Raspberry PI is not the only machine on my desk, I develop on a laptop using IntelliJ (with several GigaBytes of RAM, like 8, 16, ...), and I use `scp` to transfer the code to (and possibly from) the Raspberry PI.
 Worst case scenario, I do a `git push` from the development machine, and a `git pull` from the Raspberry PI.
 I found it actually faster and more efficient than developing directly on the Raspberry PI.

##### Something to keep in mind

 The Java Virtual Machine (JVM) implements the Java Platform Debugging Architecture (JPDA). This allows **_remote debugging_**.
 In other words, you run the code on the Raspberry PI,
 but you debug it (set breakpoints, introspect variable values, etc) on another machine (the one where the IDE runs).
 This is specially useful when the code interacts with sensors and other devices that are not supported from the laptop.
 This will make your life considerably easier than if you used another language missing it (like Python, C, and many others).
 It uses TCP between the debugger and the debuggee.

---

### Raspberry PI, a possible thing of the Internet of things... ###
  * The Raspberry PI is a fully featured Linux computer, which can - as such - connect to the Internet.
  * The Raspberry PI has a General Purpose Input Output (GPIO) interface that allows it to drive all kind of electronic components, from a simple LED to a complex robot, and including all kind of sensors (GPS, light resistors, pressure sensors, temperature sensors, all kinds!).
None of the above is new. Connecting to the Internet does not impress anyone anymore. Driving a robot, modern kitchens are full of robots, cars are loaded with electronic components...
**But** what if we put those two together, with the Raspberry PI sitting in between.
**Then**, we can drive a robot over the Internet. And **this** is not that usual (yet).

---

The snippets provided in this project are here to help in this kind of context. Some will use the network aspect of the story, some others will interact with electronic components. The two aspects should be easy to bridge, that is the goal. If that was not the case, please let me know (email address of the left side).

---

Several projects are featured here:
  * Basic GPIO interaction
  * Two Leds
  * Use the Raspberry PI to turn LEDs on and off, **through email** ([with doc](http://www.lediouris.net/RaspberryPI/email/readme.html))
  * Read Serial Port ([with doc](http://www.lediouris.net/RaspberryPI/serial/readme.html))
  * Read _and parse_ NMEA Data from a GPS ([with doc](http://www.lediouris.net/RaspberryPI/GPS/readme.html))
  * Read analog data with an Analog Digital Converter (ADC). ([with doc](http://www.lediouris.net/RaspberryPI/ADC/readme.html), with [node.js and WebSocket](http://www.lediouris.net/RaspberryPI/ADC/adc-websocket.html))
  * Drive servos using the  PCA9685. ([with doc](http://www.lediouris.net/RaspberryPI/servo/readme.html)).
  * Drive servos using the  PCA9685, **over the Internet**, with an Android client option. ([with doc](http://www.lediouris.net/RaspberryPI/servo/node.servo.html)).
  * Use the  LSM303. (I<sup>2</sup>C compass & accelerometer,  [with doc](http://www.lediouris.net/RaspberryPI/LSM303/readme.html)).
  * Use the  BMP180. (I<sup>2</sup>C temperature and pressure sensor,  [with doc](http://www.lediouris.net/RaspberryPI/BMP180/readme.html)).
  * Use the  BMP183. (SPI temperature and pressure sensor,  [with doc](http://www.lediouris.net/RaspberryPI/BMP183/readme.html)).
  * Use a relay, through email. ([with doc](http://www.lediouris.net/RaspberryPI/Relay.by.email/readme.html)).
  * Use a relay, through HTTP. ([with doc](http://www.lediouris.net/RaspberryPI/Relay.by.http/readme.html)).
  * Use a seven-segment display. ([with doc](http://www.lediouris.net/RaspberryPI/SevenSegment/readme.html)).
  * Use the  VCNL4000 (I<sup>2</sup>C proximity sensor).
  * Use the  TCS34725 (I<sup>2</sup>C color sensor, [demo](http://www.lediouris.net/RaspberryPI/TCS34725/readme.html)).
  * Use the  TSL2561 (I<sup>2</sup>C light sensor).
  * Use the  L3GD20 (I<sup>2</sup>C gyroscope, [demo](http://www.lediouris.net/RaspberryPI/L3GD20/readme.html)).
  * Use the  MCP4725 (I<sup>2</sup>C Digital to Analog Converter, [demo](http://www.lediouris.net/RaspberryPI/DAC/readme.html)).
  * ... and more.

---

All the doc - with more details than here - can be reached from [this page](http://raspberrypi.lediouris.net/).
