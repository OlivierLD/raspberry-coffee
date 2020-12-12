## Some utilities...
#### Not really JVM related (as you can guess by the 'C'...), but usefull

### SatelliteFinder, find your best geo-stationary satellite
For Outernet, helps you to find the satellite to aim for, and where it is in the sky.

The Raspberry Pi and the C.H.I.P. have a similar processors, what is compiled on the Raspberry Pi runs on the C.H.I.P.

##### From the Raspberry Pi
Compile the C code:
```bash
 $> gcc -lm -o SatelliteFinder SatelliteFinder.c
```

##### From your dev machine (your laptop)
Bring the executable on board (`192.168.1.172` is the address of the Raspberry Pi):
```bash
[raspberry-coffee/C]scp pi@192.168.1.172:raspberry-coffee/C/SatelliteFinder .
pi@192.168.1.172's password:
SatelliteFinder                                                                                                 100% 8780     8.6KB/s   00:00
[raspberry-coffee/C]
```
##### Push it to the C.H.I.P.
Connect to the C.H.I.P. network, then:

```bash
[raspberry-coffee/C]scp SatelliteFinder outernet@10.0.0.1:~
outernet@10.0.0.1's password:
SatelliteFinder                                                                                                 100% 8780     8.6KB/s   00:00
```

##### Connect to the C.H.I.P. and run the program
```bash
[raspberry-coffee/C]ssh outernet@10.0.0.1
outernet@10.0.0.1's password:
============================================
Skylark v4.4 / chip (8ed427f)
built at 2017-02-27 21:18:19+00:00

Copyright 2017 Outernet Inc
Some rights reserved.
============================================
[Skylark][outernet@outernet:~]$ ls -ls
total 13
     0 -rw-r--r--    1 outernet outernet        32 Jan  1 00:00 README
    12 -rwx------    1 outernet outernet      8780 Jan  5 06:32 SatelliteFinder
[Skylark][outernet@outernet:~]$ ./SatelliteFinder
Finding the right satellite from 37°44.93 N / 122°30.42 W...

use I-4 F3 Americas: El 39.08°, Z 142.82° (true), Tilt -28.54°
[Skylark][outernet@outernet:~]$
```

That's it!

You could send your earth position as parameter if you wish:
```bash
[Skylark][outernet@outernet:~]$ ./SatelliteFinder -lat:45.10 -lng:3.45
Checking -lat:45.10... Using Lat [45.10]
Checking -lng:3.45... Using Lng [3.45]
Finding the right satellite from 45°6.00 N / 3°27.00 E...

use Alphasat: El 33.85°, Z 150.98° (true), Tilt -20.02°
[Skylark][outernet@outernet:~]$
```

----------
