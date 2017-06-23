## Servos and Sun

The original idea is to build a device with 2 standard servos to orient a solar panel (like [that one](https://www.adafruit.com/product/200) or similar) so it faces the Sun whenever possible (ie during the day).

For a given position (provided, or read from a GPS), the position of the Sun is calculated, and then its altitude and azimuth.
The servos are triggered when necessary, for the calculated azimuth and elevation to match the orientation of the panel.

By default, in the northern hemisphere, the device would point due South, and the servo angles would be set from there.
Heading and Position can be dynamically provided (in case the device is moving, in a car, a boat, etc) to the class named
`orientation.SunFlower`.

Originally, I had attached an LSM303 (magnetometer and accelerometer), but it was too close to the servos. Those guys are electro-magnets, this was driving the LSM303 crazy...

Requires resources from other projects:

Check out this one: https://github.com/OlivierLD/oliv-soft-project-builder

Build it as instructed.

Then you need to do those things to `install` the nautical almanac components in your local Maven repository:

```bash
 $ cd olivsoft
 $ cd javanauticalalmanac
 $ ../../gradlew clean build install
 $ cd ../geomutils
 $ ../../gradlew clean build install
```

Then the build of this project should work:
```bash
 GPS.sun.servo $ ../gradlew clean shadowJar
```
You can test the Almanac behavior by running
```bash
 $ ./run.test
 -- Sun Jan 01 00:00:00 PST 2017, Mean:23.43708019254589, True:23.434571047530458, Aries GHA:221.1648583732534
    Polaris D:89.33854067501603, Z:177.55048918730466
    Sun Decl:-22.971192378129572
 -- Mon Jan 02 00:00:00 PST 2017, Mean:23.437079836510872, True:23.43458374014393, Aries GHA:222.1505096811722
    Polaris D:89.33860948623546, Z:178.5428345983678
    Sun Decl:-22.882681316884316
 -- Tue Jan 03 00:00:00 PST 2017, Mean:23.43707948047587, True:23.43459523846336, Aries GHA:223.13615195993353
    Polaris D:89.33867311474917, Z:179.53542352222394
    Sun Decl:-22.786581536003055
 -- Wed Jan 04 00:00:00 PST 2017, Mean:23.437079124440857, True:23.434603645974125, Aries GHA:224.12178723691673
...
 From 37°44.93'N / 122°30.42'W, at 19:23:55 UTC, He:72.44º, Z:142.37º (true)
```

### Issues
The LSM303 returns both magnetic and gravity data.
There seems to be a problem with the heading when pitch and/or roll are not equal to zero (ie when the board is not lying flat).

## Tests
To run a first version of the program, start the script named `run`:
```bash
 $ ./run
```
This runs the class named `orientation.SunFlower`.

This class takes the following parameters and system variables:
```
System variables:

 -Dlatitude=37.7489
 -Dlongitude=-122.5070
 -Dtest.servos=true
 -Dorient.verbose=true
 -Dastro.verbose=true
 -Dservo.verbose=true
 -Dansi.console=true
 -Dmanual.entry=true

 -Dheading.servo.sign=-1
 -Dtilt.servo.sign=-1

Program parameters, to change the servo numbers on the PCA9685:
 --heading:14 --tilt:15
```

The variables `heading.servo.sign` and `tilt.servo.sign` can be set to `-1`, to invert the way the servos turn. This depends on the physical construction of your device.

First, the program sets the `heading` servo to zero, and asks you to point it South.

Once this is done, Sun's position is calculate every second, and the 2 servos are driven to point to it, as long as the Sun it up (elevation greater than 0°).

## Inversion
Standard servos like the `SG92R` rotate on 180 degrees, from -90° to 90°.

That could fit if the body to target (ie the Sun) was going for example from East to West, through South. Unfortunately,
this is not the case.

For example, from San Fancisco, on Wed June 21, 2017:
```
At N  37°44.96' / W 122°30.44'
Sun Rise:21 Jun 2017 05:52 (PDT -0700) (Z= 59°)
Sun Set :21 Jun 2017 20:31 (PDT -0700) (Z=301°)
```
The Sun rises in the 59°, and sets in the 301°. 59° translates into -121°, and 301° into 121°, which is far out of `[-90°..90°]`.

<img src="./doc/rise.set.png" width="594" height="523" alt="Sun rise and Set">

This is where we need to invert the angles.

Let's take this example, it is a summer morning in the northern hemisphere, the azimuth of the Sun is smaller than 90.

<img src="./doc/before.invert.png" width="643" height="501" alt="Sun rise and Set">

The angle of the servo goes beyond limit, it cannot be set.
A solution is to point in the opposite direction, and flip the tilt servo:

<img src="./doc/after.invert.png" width="670" height="503" alt="Sun rise and Set">

This is a static situation, from the northern hemisphere.
The device can also be on the go, the code can receive the position and the heading (from a GPS or an NMEA station for example) at any time, it will adjust the servos accordingly.

A snapshot of the (optional) ANSI console:


<img src="./doc/ansi.console.png" alt="ANSI Console" width="561" height="199">


Here is a prototype:
![Proto](./doc/prototype.jpg)
Now I have to build the real stuff!


---
