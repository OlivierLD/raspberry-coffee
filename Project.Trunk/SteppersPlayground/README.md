## Driving Stepper Motors with the Adafruit MotorHAT

I'm learning...

### First test ever
With `motorhat.StepperDemo`, launched by the script `one.stepper.sh`.

Using a [Stepper Motor (NEMA-17)](https://www.adafruit.com/product/324), a [Stepper Motor Mount](https://www.adafruit.com/product/1297), and some gears, from a [Gear Set](https://www.allelectronics.com/item/gr-86/4-gear-set/1.html).

![Config 01](./docimg/01.jpg)

The gears:
- 21 teeth on the motor
- 41 teeth on the brass shaft

Ratio is thus `21/41`, which is `0.512:1`.

> Ratio is the key. It applies to all kinds of settings: this one, worm gears, everything. You **MUST** know yours. You would be lost otherwise.
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
--- Current Status ---------------------------------------------------------------------
Motor # 1, RPM set to 30.00, 200 Steps per Rev, 10.000 millisec per step, taking 200 steps.
 Command FORWARD, Style SINGLE 
----------------------------------------------------------------------------------------
Enter your options:
Command:
     - FORWARD
     - BACKWARD
     - BRAKE
     - RELEASE
     - SINGLE
     - DOUBLE
     - INTERLEAVE
     - MICROSTEP
     - RPM XXX
     - STEPS YYYY
     - STEPSPERREV ZZZZ
     - GO
     - OUT
? > 
```
The default config is
- 200 steps per revolution
- take 200 steps
This means it would do 360 degrees to be done by the small wheel.

Enter `GO` at the prompt to see it for yourself.

If you want the _big_ wheel to do a 360, you need the small wheel to do `360 / (21/41)` degrees,
which is `702.8571428571`. 

As `200` steps is equivalent to `360`&deg;, we need to take `200 * (702.8571428571 / 360)` steps,
 which is `390.4761904762`, which we will round to `390` steps.
 
Try this:
```
$ ./interactive.stepper.sh 
Stepper Motor Demo (WIP)
Starting Stepper Demo
RPM set to 30.
Hit Ctrl-C to stop the demo (or OUT at the prompt)
--- Current Status ---------------------------------------------------------------------
Motor # 1, RPM set to 30.00, 200 Steps per Rev, 10.000 millisec per step, taking 200 steps.
 Command FORWARD, Style SINGLE 
----------------------------------------------------------------------------------------
Enter your options:
Command:
     - FORWARD
     - BACKWARD
     - BRAKE
     - RELEASE
     - SINGLE
     - DOUBLE
     - INTERLEAVE
     - MICROSTEP
     - RPM XXX
     - STEPS YYYY
     - STEPSPERREV ZZZZ
     - GO
     - OUT
? > go           <- This will spin the small wheel on 360 degrees
0.01 sec per step
--- Current Status ---------------------------------------------------------------------
Motor # 1, RPM set to 30.00, 200 Steps per Rev, 10.000 millisec per step, taking 200 steps.
 Command FORWARD, Style SINGLE 
----------------------------------------------------------------------------------------
Enter your options:
Command:
     - FORWARD
     - BACKWARD
     - BRAKE
     - RELEASE
     - SINGLE
     - DOUBLE
     - INTERLEAVE
     - MICROSTEP
     - RPM XXX
     - STEPS YYYY
     - STEPSPERREV ZZZZ
     - GO
     - OUT
? > microstep       <- Set to smoother moves
--- Current Status ---------------------------------------------------------------------
Motor # 1, RPM set to 30.00, 200 Steps per Rev, 10.000 millisec per step, taking 200 steps.
 Command FORWARD, Style MICROSTEP 
----------------------------------------------------------------------------------------
Enter your options:
Command:
     - FORWARD
     - BACKWARD
     - BRAKE
     - RELEASE
     - SINGLE
     - DOUBLE
     - INTERLEAVE
     - MICROSTEP
     - RPM XXX
     - STEPS YYYY
     - STEPSPERREV ZZZZ
     - GO
     - OUT
? > steps 100    <- Will make only 180 degrees
--- Current Status ---------------------------------------------------------------------
Motor # 1, RPM set to 30.00, 200 Steps per Rev, 10.000 millisec per step, taking 100 steps.
 Command FORWARD, Style MICROSTEP 
----------------------------------------------------------------------------------------
Enter your options:
Command:
     - FORWARD
     - BACKWARD
     - BRAKE
     - RELEASE
     - SINGLE
     - DOUBLE
     - INTERLEAVE
     - MICROSTEP
     - RPM XXX
     - STEPS YYYY
     - STEPSPERREV ZZZZ
     - GO
     - OUT
? > go          <- Start the motor with oarameters above
0.00125 sec per step
--- Current Status ---------------------------------------------------------------------
Motor # 1, RPM set to 30.00, 200 Steps per Rev, 10.000 millisec per step, taking 100 steps.
 Command FORWARD, Style MICROSTEP 
----------------------------------------------------------------------------------------
Enter your options:
Command:
     - FORWARD
     - BACKWARD
     - BRAKE
     - RELEASE
     - SINGLE
     - DOUBLE
     - INTERLEAVE
     - MICROSTEP
     - RPM XXX
     - STEPS YYYY
     - STEPSPERREV ZZZZ
     - GO
     - OUT
? > steps 390    <- To make the big wheel spin on 360 degrees
--- Current Status ---------------------------------------------------------------------
Motor # 1, RPM set to 30.00, 200 Steps per Rev, 10.000 millisec per step, taking 390 steps.
 Command FORWARD, Style MICROSTEP 
----------------------------------------------------------------------------------------
Enter your options:
Command:
     - FORWARD
     - BACKWARD
     - BRAKE
     - RELEASE
     - SINGLE
     - DOUBLE
     - INTERLEAVE
     - MICROSTEP
     - RPM XXX
     - STEPS YYYY
     - STEPSPERREV ZZZZ
     - GO
     - OUT
? > go           <- Let's watch the big wheel...
0.00125 sec per step
--- Current Status ---------------------------------------------------------------------
Motor # 1, RPM set to 30.00, 200 Steps per Rev, 10.000 millisec per step, taking 390 steps.
 Command FORWARD, Style MICROSTEP 
----------------------------------------------------------------------------------------
Enter your options:
Command:
     - FORWARD
     - BACKWARD
     - BRAKE
     - RELEASE
     - SINGLE
     - DOUBLE
     - INTERLEAVE
     - MICROSTEP
     - RPM XXX
     - STEPS YYYY
     - STEPSPERREV ZZZZ
     - GO
     - OUT
? > out         <- Done, let's get out.
... Done with the demo ...
Bye.
$
``` 
