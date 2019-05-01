# Building and Customizing your own Nav Server
We want to have a Nav Server:
- Able to read from different sources (NMEA Serial port, TCP, whatever)
- Able to compute data
- Able to send or broadcast data on several channels
- Able to run even if no WiFi network is available
- Able to provide a Web Graphical User Interface

The [`NMEA.multiplexer`](../NMEA.multiplexer/README.md) is able to read, compute, and broadcast data.

To work even if no WiFi network is available, the best is probably to have the Raspberry Pi emit its own.
This is totally feasible, follow the instructions provided [here](https://learn.adafruit.com/setting-up-a-raspberry-pi-as-a-wifi-access-point/install-software).

Now, depending on the configuration you want, several different components, from several different modules in this project
will be required or not.

We will show here how to compose a custom server, based on your own needs.

The HTTP/REST server we use here is the one you find in the `common-utils` module.
To minimize the footprint of the final application, all the static pages required by the web interface will
be served from a _**single archive**_, as this feature is available from the HTTP Server we use here.

> Note:
> The operations on a Serial Port would require `libRxTx`, and this dependency cannot be taken care of by Gradle.
> To be able to use it outside gradle, run (on Linux/Debian/Raspberry Pi), you need to install this package:
```bash
 sudo apt-get install librxtx-java
```

## Close to production
It will not require the `git` repository to be cloned on the machine the server runs on.

We will:
- Clone the repository on one machine, used to build the required components
- Archive all the needed artifacts in a single archive (a `tar.gz` here), which can then be distributed
- Transfer it to the final destination machine, where the archive will be expanded (aka un-archived)
- Possibly modify some files
    - the `/etc/rc.local` for the server to start when the machine boots
    - the `properties` file that contains all the server's runtime parameters and definitions

Several of those steps can be scripted or automated, as you would see in the examples provided here.
This basically all you need to get up and running.

We provide here several sub-directories, from which you will be able to run some provided scripts
building several flavors of Nav Servers.

They all come with at least 3 files:
- `builder.sh`, _the one you run_, that will trigger all the required others
- `build.gradle`, the Gradle script used for the build process, probably the most important here
- `to.prod.sh`, eventually triggered by `builder.sh`, which will take care of building your application and archiving the produced artifacts.

Assuming that you've found (or defined for yourself) the configuration of your dreams, **_all_** you will need to do is:
- From the Raspberry Pi used for the build, where you've clone the `git` repository: `cd full.server` (where `full.server` is your dream directory)
- `./builder.sh`
    - During this step, you will have provided - from the command line - the name of the archive to produce, let's call it `NMEADist` as an example.
- Now transfer the generated archive to the destination Raspberry Pi:
    - `scp NMEADist.tar.gz pi@destination.pi:~`
    - `ssh pi@destination.pi`
    - `tar -xzvf NMEADist.tar.gz`
    - `cd NMEADist`
    - `./start-mux.sh`

That's it, your server is up and running! (you might have modified the `properties` file, like `nmea.mux.gps.log.properties`, though. See the [technical manual](../NMEA.multiplexer/manual.md) for details)

Now, from any browser on any machine connected on the server (Raspberry Pi)'s network,
you can reach <http://destination.pi:[port]/zip/index.html>.

In the URL above, `destination.pi` is the name (or IP address) of the machine the server runs on, and `[port]` is the one defined in the mux's properties file,
like in
```properties
with.http.server=yes
http.port=9999
```
This properties file is yours to tweak as needed.

To summarize, you went through the following steps:
- build
- archive
- transfer
- un-archive        
- run


Pick and choose your features in other modules, grab the web ages you need, modify the `index.html`, etc, and
run the script `to.prod.sh` to package everything for distribution.

The "_pick and choose_" part could be scripted as well, as seen in the provided examples.

This project is not supposed to contain any source file except web resources (archived).

It pulls the `NMEA.multiplexer`, `RESTNavServer`, and the `NMEA.mux.extensions` projects (or whatever you want or need).
This is what you would tweak to fit your requirements.

When available, the file `rc.local` is to give you some inspiration, so you can modify the one in `/etc/rc.local`
on the destination machine to start the Multiplexer at boot time.

The script `to.prod.sh` (available in each directory) is not carved in stone. It is also here for inspiration.

> Note: To make sure the runtime components will be 100% compatible with your Raspberry Pi target, I use to run this build _on a Raspberry Pi_ (not on a Windows or Mac laptop, carefull with Linux, probably OK with a Raspberry Pi Desktop).
> The (only) problem that can potentially show up is a Java version mismatch.
> The build process might be a bit too heavy for a Raspberry Pi Zero...
> I usually build on a bigger board (A, or B), and then `scp` the result to a Raspberry Pi Zero if that is the one I want to run my server on,
> as shown above with the `scp` command.

## Warning!
This project directory is a play ground, again, it is here for **you** to _compose_ your own server.

**You**.

Means not **me**. 🤓

## Examples
- Full Nav Server (all features: NMEA multiplexer, Celestial Computer, Tides, Almanacs publication, Weather Wizard, Small screens, ...)
```
 $ cd full.server
 $ ./builder.sh
```

<!-- TODO Screenshots, diagrams, pictures -->

- Minimal Multiplexer
```
 $ cd minimal.mux
 $ ./builder.sh
```

- Full Nav Server, extended (all features: NMEA multiplexer, Celestial Computer, Tides, Almanacs publication, Weather Wizard, Small screens, ...)
```
 $ cd full.server.extended
 $ ./builder.sh
```

- With a 5" or 7" TFT display, setup for Head-Up-Display
    - Use `switch2tftscreen` (at the root of the project)
    - Reboot
    - SSH to the RPi
        - Start the Multiplexer or NavServer of your choice
        - `sudo startx` (should start the Raspian desktop of the TFT)

You can boot directly to the Graphical Desktop (`sudo raspi-config`), and automatically start a browser on a given URL as
soon as the Desktop is up and running.

To do that, you need to start in Graphical mode, have Chromium installed, and boot to the Graphical Desktop.
To start Chromium when the Desktop starts, and load one or several URLs (in different tabs), edit the file named
`~/.config/lxsession/LXDE-pi/autostart`, and add, at the end, the following lines:
```
@chromium-browser --incognito --kiosk http://localhost:9999/web/headup.html \
                                      [url.2] \
                                      [url.3] \
                                      [url.4]
```
It will start Chromium in `kiosk` (full screen) mode, and load the URLs mentioned above.


---

... More to come

<!-- TODO: Docker images ? -->

- With SSD1306 32x128
- With SSD1306 64x128
- With Nokia5110
- With Head-Up-Display (5")
- With USB GPS
- With UART GPS
- Push Buttons and Switches

---
