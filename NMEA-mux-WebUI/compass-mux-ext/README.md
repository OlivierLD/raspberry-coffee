## Extending `NMEA.multiplexer`: A Standalone Compass, and a push-button. 
Same as [Compass Mux](../compass-mux), with some small part of code to implement user's interactions with a push button (click, double-click, long-click).

This is an _extension_ of the module `compass-mux`.

### Small Coding involved
The code is all in one file, `mux.MultiplexerWithOneButton.java`, it is extending the `nmea.mux.GenericNMEAMultiplexer`, using the 
`nmea.forwarders.SSD1306_HDMDisplay`, itself extending the `nmea.forwarders.SSD1306Processor`.

There is one push-button, that can be used to shutdown the multiplexer, and the system.
- You double-click to request the shutdown
- A single click for confirmation is expected within 3 seconds
- If provided, the system goes down.

See in the code of `mux.MultiplexerWithOneButton.java` how the button's `Runnable`s are implemented.
> _Note:_ the `nmea.forwarders.SSD1306Processor` provides a`substitue` mechanism for the `oled` screen, in Swing. If the `ssd1306` screen is not found, a Swing UI is displayed instead (you have to be on a graphical desktop to see it), that can also deal with the button clicks.

The rest is all scripted:
- in `gradle` to build it
- in the `yaml` properties file used to start the `mux` at runtime.

See in the `build.gradle`, this project involves others: 
- the `NMEA.multiplexer`
- the `NMEA.mux.extensions`

It's all driven by `nmea.mux.hmc5883l.oled.yaml`.

Use it like 
```
$ ./mux.sh nmea.mux.hmc5883l.oled.yaml
```

It has no Web User Interface for now.

![With a gimbal](./pictures/hmc5883l.gimbal.jpg) <!-- TODO More pictures (more accurate), and Fritzing schemas? -->

The `stl` files to build the gimbal are [here](https://github.com/OlivierLD/3DPrinting/tree/master/OpenSCAD/Gimbal).

## Rationale
See the parent module, [compass-mux](../compass-mux/README.md)

## Sample Use-Case
I have a magnetometer, and I want to test it.
The setup is the one mentioned above, and the Raspberry Pi Zero is powered by a power bank, like [that one](https://github.com/OlivierLD/3DPrinting/tree/master/OpenSCAD/Battery.Cases).

- I start the Raspberry Pi connected to the magnetometer and powered by the battery
- From a laptop, I `ssh` on the Raspberry Pi to start the multiplexer
  - `./mux.sh nmea.mux.hcm5883l.oled.yaml`
- Then I can take the device outside, and see how it behaves in a zone whwre there are less magnetic disturbances than at my desk.
- After a while - when done - I can shut down the system, with a double-click on the button, and take it back home!

---

