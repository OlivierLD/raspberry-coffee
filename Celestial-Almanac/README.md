## Celestial Almanac(s)
This is not strictly related to java nor Raspberry Pi.

This is about reproducing the celestial almanac features (available in `common-utils/src/main/java/calc/calculation/nauticalalmanac`)
in several other languages than Java.

> _Note_: This is actually an interesting exercise. Those celestial calculations involve **_a lot_** of code (more than **20K** lines of code, for each language presented here).
> It is always interesting to consider the following aspects:
> - Productivity (once you know what you want, how long does it takes to write the _right_ code, with and without an IDE - when it exists)
> - Readability (do you still understand what you wrote two weeks later 🤓)
> - Speed of execution (C is probably the fastest, but how far behind are the others?)
> - Debugging (local, remote, ...)  
> - Maintainability (can someone who did not write it add feature(s) or maintain the code)
> 
> _To keep in mind:_ An extra detail, the code presented here can run _**in standalone**_ on a single machine. It does not need to communicate
> with any other resources (on the net, in the cloud, wherever) to reach its goals, to do its job, to complete its work.
> Do keep this important aspect in mind when you setup your mind about the best language ever. 😜   

> _Note-2_: those calculations rely on a parameter called `DeltaT`, that can be obtained
> from the [US Naval Oceanography Portal](https://www.usno.navy.mil/USNO/earth-orientation/eo-products/long-term).
> This (official) web site is undergoing some upgrade..., if it is down, it will
> come back to life soon. Last known value of `DeltaT` is `69.2201` sec.
> > Aug 2020, it's back up! <http://maia.usno.navy.mil/ser7/deltat.preds>

Beside Java, we (will) have
- [ES6](./ES6/README.md)
- [C & C++, (Arduino?)](./Arduino.Cpp/README.md)
    > Note: This is **_way too demanding_** for an Arduino (mostly because of the _volume_ of the code, mentioned above), but it works OK for C & C++. 
- [Python](./Python/README.md)
- and more when possible (Go, Scala, Kotlin,...)

### Integrated Development Environment (IDE)
Here is a quick list of some IDEs I use. They _all_ have a free version.
- For Java and all JVM-aware languages: IntelliJ, period.
- For Python: PyCharm.
- For C & C++: Visual Studio.
- For JavaScript: IntelliJ, WebStorm, Visual Studio.

### Run it
#### From Java 
From this module's root
```
$ ../gradlew clean shadowJar
$ java -cp build/libs/Celestial.Almanac-1.0-all.jar celestial.almanac.JavaSample
Calculations done for 2020-03-28 16:50:20 UTC
Sun data:	Decl.:  3°21.50'N, GHA:   71°21.81', RA: 00h 31m 07.20s, sd: 16'01.22", hp:    08.81"
Moon data:	Decl.: 16°11.39'N, GHA:   25°09.69', RA: 03h 35m 55.70s, sd: 14'54.36", hp: 54'42.28"
	Moon phase:  47°05.52',  +cre
Venus data:	Decl.: 22°06.81'N, GHA:   27°59.37', RA: 03h 24m 37.00s, sd:    12.38", hp:    12.94"
Mars data:	Decl.: 21°20.44'S, GHA:  138°18.16', RA: 20h 03m 21.80s, sd:    03.15", hp:    05.91"
Jupiter data:	Decl.: 21°22.04'S, GHA:  143°16.47', RA: 19h 43m 28.60s, sd:    18.32", hp:    01.64"
Saturn data:	Decl.: 20°06.29'S, GHA:  136°28.12', RA: 20h 10m 42.00s, sd:    07.99", hp:    00.85"

Polaris data:	Decl.: 89°21.04'N, GHA:   35°17.04', RA: 02h 55m 26.30s
Equation of time: - 04m 52.8s
Lunar Distance:  47°10.03'
Day of Week: SAT
$
```

#### From Python
```
$ cd src/main/python
$ ./calculation_sample.py 
  ----------------------------------------------
  Calculations done for 2020-Mar-28 16:50:20 UTC
  ----------------------------------------------
  Sideral Time: 5h 16m 35.526s
  Sun: GHA 071° 21' 49", RA 00h 31m 07.2s, DEC N  03° 21' 30", sd 961.2", hp 8.8"
  Venus: GHA 027° 59' 22", RA 03h 24m 37.0s, DEC N  22° 06' 49", sd 12.4", hp 12.9"
  Mars: GHA 138° 18' 09", RA 20h 03m 21.8s, DEC S  21° 20' 27", sd 3.1", hp 5.9"
  Jupiter: GHA 143° 16' 28", RA 19h 43m 28.6s, DEC S  21° 22' 02", sd 18.3", hp 1.6"
  Saturn: GHA 136° 28' 07", RA 20h 10m 42.0s, DEC S  20° 06' 17", sd 8.0.0", hp 0.8"
  Moon: GHA 025° 09' 40", RA 03h 35m 55.8s, DEC N  16° 11' 24", sd 894.4", hp 3282.3"
  	Moon phase 47.1 ->  +cre
  Polaris: GHA 035° 17' 02", RA 02h 55m 26.3s, DEC N  89° 21' 02"
  Ecliptic: obliquity 23° 26' 11.973", true 23° 26' 12.013"
  Equation of time - 4m 52.8s 
  Lunar Distance: 047° 10' 03"
  Day of Week: SAT
$
```

#### From C++
> Note: The `compile.sh` script used below works for Mac & Linux (including Debian & Raspbian).
> If you need to run this on Windows..., well, Google is your friend!
```
$ cd src/main/cpp
$ ./compile.sh
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
Done!
$
```

#### From ES6 (JavaScript, using NodeJS as a Web server)
```
$ cd src/main/ES6
$ node server.js
```
Then from a browser, load `http://localhost:8080/index.html` and follow the instructions on the page.

You should get a JSON object, and some formatted output, like this:
![WebUI](./ES6WebUI.png)

## Timing
```
$ time src/main/cpp/astro 
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
Done!

real	0m0.019s
user	0m0.016s
sys	0m0.000s

$ time java -cp build/libs/Celestial.Almanac-1.0-all.jar celestial.almanac.JavaSample
Calculations done for 2020-03-28 16:50:20 UTC
Sun data:	Decl.:  3°21.50'N, GHA:   71°21.81', RA: 00h 31m 07.20s, sd: 16'01.22", hp:    08.81"
Moon data:	Decl.: 16°11.39'N, GHA:   25°09.69', RA: 03h 35m 55.70s, sd: 14'54.36", hp: 54'42.28"
	Moon phase:  47°05.52',  +cre
Venus data:	Decl.: 22°06.81'N, GHA:   27°59.37', RA: 03h 24m 37.00s, sd:    12.38", hp:    12.94"
Mars data:	Decl.: 21°20.44'S, GHA:  138°18.16', RA: 20h 03m 21.80s, sd:    03.15", hp:    05.91"
Jupiter data:	Decl.: 21°22.04'S, GHA:  143°16.47', RA: 19h 43m 28.60s, sd:    18.32", hp:    01.64"
Saturn data:	Decl.: 20°06.29'S, GHA:  136°28.12', RA: 20h 10m 42.00s, sd:    07.99", hp:    00.85"

Polaris data:	Decl.: 89°21.04'N, GHA:   35°17.04', RA: 02h 55m 26.30s
Equation of time: - 04m 52.8s
Lunar Distance:  47°10.03'
Day of Week: SAT

real	0m0.375s
user	0m0.368s
sys	0m0.060s

$ time src/main/python/calculation_sample.py 
----------------------------------------------
Calculations done for 2020-Mar-28 16:50:20 UTC
----------------------------------------------
Sideral Time: 5h 16m 35.526s
Sun: GHA 071° 21' 49", RA 00h 31m 07.2s, DEC N  03° 21' 30", sd 961.2", hp 8.8"
Venus: GHA 027° 59' 22", RA 03h 24m 37.0s, DEC N  22° 06' 49", sd 12.4", hp 12.9"
Mars: GHA 138° 18' 09", RA 20h 03m 21.8s, DEC S  21° 20' 27", sd 3.1", hp 5.9"
Jupiter: GHA 143° 16' 28", RA 19h 43m 28.6s, DEC S  21° 22' 02", sd 18.3", hp 1.6"
Saturn: GHA 136° 28' 07", RA 20h 10m 42.0s, DEC S  20° 06' 17", sd 8.0.0", hp 0.8"
Moon: GHA 025° 09' 40", RA 03h 35m 55.8s, DEC N  16° 11' 24", sd 894.4", hp 3282.3"
	Moon phase 47.1 ->  +cre
Polaris: GHA 035° 17' 02", RA 02h 55m 26.3s, DEC N  89° 21' 02"
Ecliptic: obliquity 23° 26' 11.973", true 23° 26' 12.013"
Equation of time - 4m 52.8s 
Lunar Distance: 047° 10' 03"
Day of Week: SAT

real	0m0.678s
user	0m0.660s
sys	0m0.016s
$ 
```

### Micronaut
There is also a Micronaut service.
See in the [`Micronaut`](./Micronaut/README.md) (Serverless MicroService) folder.
Very trending.

It is based on the Java implementation of the Celestial Almanac, and
it exposes all the computed data through HTTP REST requests.

---

