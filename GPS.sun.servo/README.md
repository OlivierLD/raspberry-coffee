## Servos and Sun

The idea is to build a device with 2 standard servos and an LSM303 to orient a solar panel so it faces the Sun whenever possible (ie during the day).

For a given position (provided, or read from a GPS), the position of the Sun is calculated, and then its altitude and azimuth.
The LSM303 gives the direction (magnetic heading) and inclination (on the Y axis) of the solar panel to which it is attached.
The servos are triggered when necessary, for the data returned by the LSM303 and the calculated azimuth and elevation to match.

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
