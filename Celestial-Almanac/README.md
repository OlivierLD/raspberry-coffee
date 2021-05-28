## Celestial Almanac(s)

# Important
This module was moved into its own repo.  
See <https://github.com/OlivierLD/AstroComputer>.

The one you're one should be considered as deprecated, and will eventually be removed. 

---
> _Note_: This is not strictly related to Java nor Raspberry Pi.

This is about reproducing the celestial almanac features (available in `common-utils/src/main/java/calc/calculation/nauticalalmanac`)
in several other languages than Java.

> _Note_:   
> This is actually an interesting exercise. Those celestial calculations involve **_a lot_** of code (more than **20K** lines of code, for each language presented here).
> It is always interesting to consider the following aspects:
> - Productivity (once you know what you want, how long does it takes to write the _right_ code, with and without an IDE - when it exists)
> - Readability (do you still understand what you wrote two weeks before? 🤓 )
> - Speed of execution (C is probably the fastest, but how far behind are the others?)
> - Debugging (local, remote, ...)  
> - Maintainability (can someone who did not write it add feature(s) or maintain the code)
> 
> _To keep in mind:_ An extra detail, the code presented here can run _**in standalone**_ on a single machine. It does not need to communicate
> with any other resources (on the net, in the cloud, wherever) to reach its goals, to do its job, to complete its work.
> Do keep this important aspect in mind when you decide which is the best language ever. 😜   

> _Note-2_:  
> Those calculations rely on a parameter called `DeltaT`, that can (could) be obtained
> from the [US Naval Oceanography Portal](https://www.usno.navy.mil/USNO/earth-orientation/eo-products/long-term).
> This (official) web site is undergoing some upgrade..., if it is down, it will
> come back to life soon. Last known value of `DeltaT` is `69.2201` sec.
> > Aug 2020, it's back up! <http://maia.usno.navy.mil/ser7/deltat.preds>  
> > ...and down.  
> > So we now have a way to _calculate_ the DeltaT value, in all languages (Java, Python, C++, ES6, Go).

**Hopefully, all those implementations will produce the _exact_ same result.**

> The problem presented here is Astronomical Almanac computations.  
> This is mostly arithmetic and algebra, but the code is substantially large (do a `(find . -name '*.java' -print0 | xargs -0 cat) |  wc -l` to know how many lines of Java it takes, same for `*.scala`, `*.py`, `*.go`, `*.cpp`, `*.js`,
> it is more than **_20,000_** lines of code each time). Interestingly, this is the kind of computation the training of an AI model requires...

Beside Java, we (will) have
- [ES6](./ES6/README.md)
- [C & C++, (Arduino?)](./Arduino.Cpp/README.md)
    > Note: This is **_way too demanding_** for an Arduino (mostly because of the _volume_ of the code, mentioned above), but it works OK for C & C++. 
- [Python](./Python/README.md)
- [Golang](./src/main/go/Celest/README.md)
- [Scala](#from-scala)
- and more when possible (Kotlin,...)

### Integrated Development Environment (IDE)
Here is a quick list of some IDEs I use. They _all_ have a free version.
- For Java and all JVM-aware languages (Scala, Kotlin, Groovy, ...): IntelliJ, period❗
- For Python: PyCharm.
- For C & C++: Visual Studio.
- For Golang: Visual Studio.
- For JavaScript: IntelliJ (Pro), WebStorm, Visual Studio.

### Run it
> _Note:_   
> There is something to pay attention to in the different languages,
> this is the way to use `floor`, `ceil`, `round` (or their equivalents) and such methods. This has an
> important impact on the results, and can explain some differences
> in the outputs...

> The `time` command will also take the loading time of the VM (when needed) in account.
> We've put some timestamps in the code, to see the time the actual calculation takes.

#### From Java 
From this module's root
```
$ ../gradlew clean shadowJar
$ java -cp build/libs/Celestial.Almanac-1.0-all.jar celestial.almanac.JavaSample
Calculations for 2020-03-28 16:50:20 UTC (not now)
...DeltaT set to 71.71293632812495
Calculations done for 2020-03-28 16:50:20 UTC, in 55 ms <<<
Sun data:	Decl.:  3°21.51'N, GHA:   71°21.81', RA: 00h 31m 07.20s, sd: 16'01.22", hp:    08.81"
Moon data:	Decl.: 16°11.40'N, GHA:   25°09.65', RA: 03h 35m 55.90s, sd: 14'54.36", hp: 54'42.28"
	Moon phase:  47°05.56',  +cre
Venus data:	Decl.: 22°06.81'N, GHA:   27°59.36', RA: 03h 24m 37.00s, sd:    12.38", hp:    12.94"
Mars data:	Decl.: 21°20.44'S, GHA:  138°18.15', RA: 20h 03m 21.80s, sd:    03.15", hp:    05.91"
Jupiter data:	Decl.: 21°22.04'S, GHA:  143°16.47', RA: 19h 43m 28.60s, sd:    18.32", hp:    01.64"
Saturn data:	Decl.: 20°06.29'S, GHA:  136°28.12', RA: 20h 10m 42.00s, sd:    07.99", hp:    00.85"

Polaris data:	Decl.: 89°21.04'N, GHA:   35°17.04', RA: 02h 55m 26.30s
Equation of time: - 04m 52.8s
Lunar Distance:  47°10.07'
Day of Week: SAT
Done with Java!
$
```
Calculation took 55 ms.
#### From Scala
```
$ java -cp ./build/libs/Celestial-Almanac-1.0-all.jar astro.SampleMain
Calculations for 2020-03-28 16:50:20 UTC (not now)
New deltaT: 71.71293632812495
Calculations done for 2020-03-28 16:50:20 UTC, in 64 ms <<<
Sun:	 Decl:  3°21.51'N, GHA:   71°21.81', RA: 00h 31m 07.00s, SD: 16'01.22", HP:    08.81"
Moon:	 Decl: 16°11.40'N, GHA:   25°09.65', RA: 03h 35m 56.00s, SD: 14'54.36", HP: 54'42.28"
Venus:	 Decl: 22°06.81'N, GHA:   27°59.36', RA: 03h 24m 37.00s, SD:    12.38", HP:    12.94"
Mars:	 Decl: 21°20.44'S, GHA:   27°59.36', RA: 20h 03m 22.00s, SD:    03.15", HP:    05.91"
Jupiter:	 Decl: 21°22.04'S, GHA:  143°16.47', RA: 19h 43m 29.00s, SD:    18.32", HP:    01.64"
Saturn:	 Decl: 20°06.29'S, GHA:  136°28.12', RA: 20h 10m 42.00s, SD:    07.99", HP:    00.85"

Polaris:	 Decl: 89°21.04'N, GHA:   35°17.04', RA: 02h 55m 26.00s 
Equation of Time: - 04m 60.0s
Lunar Distance:  47°10.07'
Moon Phase:  +cre
Day of Week: SAT
Done with Scala!
```
Calculation took 64 ms.

#### From Python
```
$ cd src/main/python
$ ./calculation_sample.py 
----------------------------------------------
Calculations done for 2020-Mar-28 16:50:20 UTC
In 142 ms
----------------------------------------------
Sideral Time: 5h 16m 35.526s
Sun: GHA 071° 21' 49", RA 00h 31m 07.2s, DEC N  03° 21' 30", sd 961.2", hp 8.8"
Venus: GHA 027° 59' 22", RA 03h 24m 37.0s, DEC N  22° 06' 49", sd 12.4", hp 12.9"
Mars: GHA 138° 18' 09", RA 20h 03m 21.8s, DEC S  21° 20' 27", sd 3.1", hp 5.9"
Jupiter: GHA 143° 16' 28", RA 19h 43m 28.6s, DEC S  21° 22' 02", sd 18.3", hp 1.6"
Saturn: GHA 136° 28' 07", RA 20h 10m 42.0s, DEC S  20° 06' 17", sd 8.0.0", hp 0.8"
Moon: GHA 025° 09' 39", RA 03h 35m 55.9s, DEC N  16° 11' 24", sd 894.4", hp 3282.3"
	Moon phase 47.1 ->  +cre
Polaris: GHA 035° 17' 02", RA 02h 55m 26.3s, DEC N  89° 21' 02"
Ecliptic: obliquity 23° 26' 11.973", true 23° 26' 12.013"
Equation of time - 4m 52.8s 
Lunar Distance: 047° 10' 04"
Day of Week: SAT
Done with Python!
$
```
Calculation took 142 ms.

> There is also a Jupyter Notebook, `CalculationSample.ipynb`:
```
$ cd src/main/python
$ jupyter notebook
```

#### From C++
> Note: The `compile.sh` script used below works for Mac & Linux (including Debian & Raspbian).
> If you need to run this on Windows..., well, Google is your friend!
```
$ cd src/main/cpp
$ ./compile.sh
$ ./astro
Sample main, calculating for 2020-Mar-28 16:50:20
Setting DeltaT to 71.712936
--- Calculated 2020-Mar-28 16:50:20 ---
Calculation took 4412 μs
Julian Dates 2458938.000000 2458937.201620 2458937.202450
Sideral Time 5h 16m 35s
EoT: -4.879447 => - 4m 52s
---------------- Bodies ---------------
Sun 	GHA: 71° 21' 48", RA: 0h 31m 7s, Dec: N 3°21'30", sd: 961.200000", hp: 8.800000"
Venus 	GHA: 27° 59' 22", RA: 3h 24m 37s, Dec: N 22°6'49", sd: 12.400000", hp: 12.900000"
Mars 	GHA: 138° 18' 9", RA: 20h 3m 21s, Dec: S 21°20'27", sd: 3.100000", hp: 5.900000"
Jupiter	GHA: 143° 16' 28", RA: 19h 43m 28s, Dec: S 21°22'2", sd: 18.300000", hp: 1.600000"
Saturn 	GHA: 136° 28' 7", RA: 20h 10m 42s, Dec: S 20°6'17", sd: 8.000000", hp: 0.800000"
Moon 	GHA: 25° 9' 39", RA: 3h 35m 55s, Dec: N 16°11'24", sd: 894.400000", hp: 3282.300000"
	Moon phase: 47.100000,  +cre
Polaris	GHA: 35° 17' 2", RA: 2h 55m 26s, Dec: N 89°21'2"
Ecliptic obliquity 23° 26' 11.973000", true 23° 26' 12.013000"
Lunar Distance 47° 10' 4"
Day of Week SAT
---------------------------------------
Done with C!
$
```
Calculation took 4412 μs (4.412 ms)

#### From Golang
```
$ cd src/main/go/Celest
$ cd main
```
you can directly do a 
```
$ go run .
```
This will re-compile the possible modifications.  
Or you can do a 
```
$ go build .
```
Followed by a 
```
./main
```
> `go run .` and `./main` will produce the same output, but `./main` is much faster.

```
Let's get started!
Calculating for 2020-3-28 16:50:20
Calculation took : 3676 μs
Julian Dates 2.458938e+06 2.45893720162037e+06 2.458937202450381e+06
Sideral Time 5h 16m 35.526s
EoT: -4.879415188586677 => - 4m 52.8s
------------- Bodies --------------
Sun 	GHA: 71° 21' 49", RA: 0h 31m 7.2s, Dec: N 3°21'30", sd: 961.200000", hp: 8.800000"
Venus 	GHA: 27° 59' 22", RA: 3h 24m 37s, Dec: N 22°6'49", sd: 12.400000", hp: 12.900000"
Mars 	GHA: 138° 18' 9", RA: -4h 3m 21.8s, Dec: S 21°20'27", sd: 3.100000", hp: 5.900000"
Jupiter 	GHA: 143° 16' 28", RA: -5h 43m 28.6s, Dec: S 21°22'2", sd: 18.300000", hp: 1.600000"
Saturn 	GHA: 136° 28' 7", RA: -4h 10m 42s, Dec: S 20°6'17", sd: 8.000000", hp: 0.800000"
Moon 	GHA: 25° 9' 39", RA: 3h 35m 55.9s, Dec: N 16°11'24", sd: 894.400000", hp: 3282.300000"
	Moon Phase: 47.1,  +cre
Polaris	GHA: 35° 17' 2", RA: 2h 55m 26.3s, Dec: N 89°21'2"
Ecliptic obliquity 23° 26' 11.973000", true 23° 26' 12.013000"
Lunar Distance 47° 10' 4"
Day of Week SAT
---------------------------------------
Done with Golang!
$
```
Calculation took 3676 μs (3.676 ms)

##### Quick execution time comparison
| Language | Time in ms |
|:-----|-----:|
| Go | 3.676 |
| C | 4.412 |
| Java | 55 |
| Scala | 64 |
| Python | 142 |


#### From ES6 (JavaScript, using NodeJS as a Web server)
```
$ cd src/main/ES6
$ node server.js
```
Then from a browser, load `http://localhost:8080/index.html` and follow the instructions on the page.

> Or, from a Mac, just run
```
$ ./start.sh
```

You should get a `JSON` object, and some formatted output, like this:
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
Done with C!

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
Done with Java!

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
Done with Python!

real	0m0.678s
user	0m0.660s
sys	0m0.016s

$ time src/main/go/Celest/main/main 
  Let's get started!
  DeltaT for 2020-3: 71.71293632812495 s
  Calculating for 2020-3-28 16:50:20
  Julian Dates 2.458938e+06 2.45893720162037e+06 2.458937202450381e+06
  Sideral Time 5h 16m 35.526s
  EoT: -4.879415188586677 => - 4m 52.8s
  ------------- Bodies --------------
  Sun 	GHA: 71° 21' 49", RA: 0h 31m 7.2s, Dec: N 3°21'30", sd: 961.200000", hp: 8.800000"
  Venus 	GHA: 27° 59' 22", RA: 3h 24m 37s, Dec: N 22°6'49", sd: 12.400000", hp: 12.900000"
  Mars 	GHA: 138° 18' 9", RA: -4h 3m 21.8s, Dec: S 21°20'27", sd: 3.100000", hp: 5.900000"
  Jupiter 	GHA: 143° 16' 28", RA: -5h 43m 28.6s, Dec: S 21°22'2", sd: 18.300000", hp: 1.600000"
  Saturn 	GHA: 136° 28' 7", RA: -4h 10m 42s, Dec: S 20°6'17", sd: 8.000000", hp: 0.800000"
  Moon 	GHA: 25° 9' 39", RA: 3h 35m 55.9s, Dec: N 16°11'24", sd: 894.400000", hp: 3282.300000"
  	Moon Phase: 47.1,  +cre
  Polaris	GHA: 35° 17' 2", RA: 2h 55m 26.3s, Dec: N 89°21'2"
  Ecliptic obliquity 23° 26' 11.973000", true 23° 26' 12.013000"
  Lunar Distance 47° 10' 4"
  Day of Week SAT
  ---------------------------------------
  Done with Golang!
  
  real	0m0.199s
  user	0m0.005s
  sys	0m0.004s

$
```

### Micronaut
There is also a Micronaut service.
See in the [`Micronaut`](./Micronaut/README.md) (Serverless MicroService) folder.
Very trending.

It is based on the Java implementation of the Celestial Almanac, and
it exposes all the computed data through HTTP REST requests.

---

