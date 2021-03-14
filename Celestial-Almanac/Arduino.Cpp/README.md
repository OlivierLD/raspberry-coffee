# C and friends
## Arduino
This was originally an attempt to write the Celestial Computer for the Arduino.

As it is now, it is way too big to fit the memory available on an Arduino.

You can use the script `mklib.sh` to build a library for Arduino, but the files it contains are **way too big** to be used.

It is OK for C++ though. See the `compile.sh` in the `AstroLib` folder (more below).

## C++
This being said, C++ works OK.

Look into the `AstroLib` folder, run the script `compile.sh`, and you should be able to run the `./astro` generated executable.

Do look into `sample_main.cpp` to see how this is done, how to use the `AstroComputer` for yourself.

```
$ ./astro 
  Sample main, calculating for 2020-Mar-28 16:50:20
  --- Calculated 2020-Mar-28 16:50:20 ---
  Julian Dates 2458938.000000 2458937.201620 2458937.202422
  Sideral Time 5h 16m 35s
  EoT: -4.879342 => - 4m 52s
  ---------------- Bodies ---------------
  Sun 	GHA: 71° 21' 49", RA: 0h 31m 7s, Dec: N 3°21'30", sd: 961.200000", hp: 8.800000"
  Venus 	GHA: 27° 59' 22", RA: 3h 24m 37s, Dec: N 22°6'49", sd: 12.400000", hp: 12.900000"
  Mars 	GHA: 138° 18' 9", RA: 20h 3m 21s, Dec: S 21°20'27", sd: 3.100000", hp: 5.900000"
  Jupiter	GHA: 143° 16' 28", RA: 19h 43m 28s, Dec: S 21°22'2", sd: 18.300000", hp: 1.600000"
  Saturn 	GHA: 136° 28' 7", RA: 20h 10m 42s, Dec: S 20°6'17", sd: 8.000000", hp: 0.800000"
  Moon 	GHA: 25° 9' 40", RA: 3h 35m 55s, Dec: N 16°11'24", sd: 894.400000", hp: 3282.300000"
  	Moon phase: 47.100000,  +cre
  Polaris	GHA: 35° 17' 2", RA: 2h 55m 26s, Dec: N 89°21'2"
  Ecliptic obliquity 23° 26' 11.973000", true 23° 26' 12.013000"
  Lunar Distance 47° 10' 3"
  Day of Week SAT
  ---------------------------------------
  Done with C!
$
```

---
