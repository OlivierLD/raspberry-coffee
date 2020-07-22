## Driving Stepper Motors with the [Adafruit MotorHAT](https://www.adafruit.com/product/2348)

I'm learning...

[Here](https://learn.adafruit.com/adafruit-dc-and-stepper-motor-hat-for-raspberry-pi/using-stepper-motors) is a good paper by Adafruit.

### First test ever
With `motorhat.NonLinearStepperDemo`, launched by the script `one.stepper.sh`.

Using a [Stepper Motor (NEMA-17)](https://www.adafruit.com/product/324), a [Stepper Motor Mount](https://www.adafruit.com/product/1297), and some gears, from a [Gear Set](https://www.allelectronics.com/item/gr-86/4-gear-set/1.html).

![Wiring](./docimg/MotorHat_bb.png)

![Config 01](./docimg/01.jpg)

The gears:
- 21 teeth on the motor
- 41 teeth on the brass shaft

Ratio is thus `21/41`, which is `0.512:1`.

> Ratio is the key. It applies to all kinds of settings: this one, worm gears, everything. You **MUST** know yours. You would be lost otherwise. This is the _key_ to your final device's accuracy.
>> When transmitting directly from the motor's shaft, the ratio is `1:1`.
>

### Interactive interface
Steps per Revolution can be set from `35` to `200` (default is `200`).

```
$ ./interactive.stepper.sh 
Stepper Motor Demo (WIP)
Starting Stepper Demo
RPM set to 30.
Hit Ctrl-C to stop the demo (or OUT at the prompt)
Set your options, and enter 'GO' to start the motor.
Enter your options:
     - FORWARD
     - BACKWARD
     - SINGLE
     - DOUBLE
     - INTERLEAVE
     - MICROSTEP
     - RPM XXX
     - STEPS YYYY
     - STEPSPERREV ZZZZ
     - GO
     - OUT
     - QUIT
--- Current Status ---------------------------------------------------------------------
Motor # 1, RPM set to 30.00, 200 Steps per Rev, 10.000 millisec per step, taking 200 steps.
Command FORWARD, Style SINGLE 
----------------------------------------------------------------------------------------
Your option ? > 
```
The default config is
- 200 steps per revolution
- take 200 steps

This means it would do 360 degrees to be done by the small wheel.

Enter `GO` at the prompt to see it for yourself.

If you want the _big_ wheel to do a 360, you need the small wheel to do `360 / (21/41)` degrees,
which is `702.8571428571`. 

As `200` steps is equivalent to `360`&deg;, we need to take `200 * (702.8571428571 / 360)` steps,
 which is `390.4761904762`, rounded to `390` steps.
 
After a build (`../../gradlew clean shadowJar`), try this:
```
$ ./interactive.stepper.sh 
Stepper Motor Demo (WIP)
Starting Stepper Demo
RPM set to 30.
Hit Ctrl-C to stop the demo (or OUT at the prompt)
Set your options, and enter 'GO' to start the motor.
Options are (lowercase supported):
     - FORWARD         	Set the direction to 'FORWARD'
     - BACKWARD        	Set the direction to 'BACKWARD'
     - SINGLE          	Set the style to 'SINGLE'
     - DOUBLE          	Set the style to 'DOUBLE'
     - INTERLEAVE      	Set the style to 'INTERLEAVE'
     - MICROSTEP       	Set the style to 'MICROSTEP'
     - RPM xxx         	Set the Revolution Per Minute to 'xxx', as integer
     - STEPS yyy       	Set the Number of Steps to make to 'yyy', as integer
     - STEPSPERREV zzz 	Set the Steps Per Revolution to 'zzz', as integer
     - GO              	Apply current settings and runs the motor for the required number  of steps
     - OUT             	Release the motor and exit.
     - QUIT            	Same as 'OUT'
     - HELP            	Display command list
--- Current Status ---------------------------------------------------------------------
Motor # 1, RPM set to 30.00, 200 Steps per Rev, 10.000 millisec per step, taking 200 steps.
Command FORWARD, Style SINGLE 
----------------------------------------------------------------------------------------
Your option ? > go   # Turn small wheel on 360 degrees
--- Current Status ---------------------------------------------------------------------
Motor # 1, RPM set to 30.00, 200 Steps per Rev, 10.000 millisec per step, taking 200 steps.
Command FORWARD, Style SINGLE 
----------------------------------------------------------------------------------------
Your option ? > microstep  # smoother moves...
--- Current Status ---------------------------------------------------------------------
Motor # 1, RPM set to 30.00, 200 Steps per Rev, 10.000 millisec per step, taking 200 steps.
Command FORWARD, Style MICROSTEP 
----------------------------------------------------------------------------------------
Your option ? > steps 100  # Only on 180 degrees...
--- Current Status ---------------------------------------------------------------------
Motor # 1, RPM set to 30.00, 200 Steps per Rev, 10.000 millisec per step, taking 100 steps.
Command FORWARD, Style MICROSTEP 
----------------------------------------------------------------------------------------
Your option ? > go  # New start
--- Current Status ---------------------------------------------------------------------
Motor # 1, RPM set to 30.00, 200 Steps per Rev, 10.000 millisec per step, taking 100 steps.
Command FORWARD, Style MICROSTEP 
----------------------------------------------------------------------------------------
Your option ? > steps 390  # This would tell the big wheel to make a 360.
--- Current Status ---------------------------------------------------------------------
Motor # 1, RPM set to 30.00, 200 Steps per Rev, 10.000 millisec per step, taking 390 steps.
Command FORWARD, Style MICROSTEP 
----------------------------------------------------------------------------------------
Your option ? > go   # Let's see...
--- Current Status ---------------------------------------------------------------------
Motor # 1, RPM set to 30.00, 200 Steps per Rev, 10.000 millisec per step, taking 390 steps.
Command FORWARD, Style MICROSTEP 
----------------------------------------------------------------------------------------
Your option ? > quit  # Bye!
... Done with the demo ...
Bye.
$
``` 
> Screenshots above might not be 100% accurate, soft keeps evolving.

### Non-constant speed
Try that:
```
$ ./non-linear.stepper.sh
```

### Some comments
With a `5v` power supply for the motor (the Raspberry Pi does not like to share power),
some required speed/RPM might be too high.

> TODO: Try 12v power supply.

### Goals

#### SunFlower
Following up to the previous micro-servo prototype:

- Using two stepper motors
- Using the `NMEA-multiplexer` to get the GPS Position and the Heading
    - Provide defaults...
- Determine the Sun's location in the sky
- Orient a solar panel accordingly
    - Using one motor for the Azimuth
    - Using the other one for the Elevation (aka Altitude)

---
