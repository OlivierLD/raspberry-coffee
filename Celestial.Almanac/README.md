## Celestial Almanac(s)
This is not strictly related to java nor Raspberry Pi.

This is about reproducing the celestial almanac features (available in `common-utils/src/main/java/calc/calculation/nauticalalmanac`)
is several other languages than Java.

> _Note_: This is actually an interesting exercise. Those celestial calculations involve **_a lot_** of code (more than **20K** lines of code, for each language presented here).
> It is always interesting to consider the following aspects:
> - Productivity (how long it takes to write the _right_ code), with and without an IDE - when it exists
> - Readability (do you still understand what you wrote two weeks later 🤓)
> - Speed of execution
> - Debugging (local, remote, ...)  
> - Maintainability (can someone who did not write it add feature(s) or maintain the code) 

Beside Java, we (will) have
- [ES6](./ES6/README.md)
- [C & C++, (Arduino?)](./Arduino.Cpp/README.md)
    > Note: This is **_way too demanding_** for an Arduino (mostly because of then _volume_ of the code, mentioned above), but it works OK for C & C++. 
- [Python](./Python/README.md)
- and more when possible (Go, Kotlin,...)

### Run it
#### From Java 
From this modules root
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

#### From ES6 (JavaScript, using NodeJS)
```
$ cd src/main/ES6
$ node server.js
```
Then from a browser, load `http://localhost:8080/index.html` and follow the instructions on the page.

You should get a JSON object like this:
```json   
{
  "sun": {
    "GHA": {
      "raw": 104.34312607934919,
      "fmt": "104° 20' 35\""
    },
    "RA": {
      "raw": 324.8703016388234,
      "fmt": "21h 39m 28.9s"
    },
    "DEC": {
      "raw": -14.006110810890373,
      "fmt": "S   14° 00' 22\""
    },
    "SD": {
      "raw": 972.3712371377966,
      "fmt": "972.4\""
    },
    "HP": {
      "raw": 8.910760042297326,
      "fmt": "8.9\""
    }
  },
  "EOT": {
    "raw": -14.210829015936724,
    "fmt": " - 14m 12.6s"
  },
  "moon": {
    "GHA": {
      "raw": 249.73013980252705,
      "fmt": "249° 43' 49\""
    },
    "RA": {
      "raw": 179.48328791564552,
      "fmt": "11h 57m 56s"
    },
    "DEC": {
      "raw": 5.73293116162958,
      "fmt": "N   05° 43' 59\""
    },
    "SD": {
      "raw": 992.4948640943279,
      "fmt": "992.5\""
    },
    "HP": {
      "raw": 3642.4486678501053,
      "fmt": "3642.4\""
    },
    "illum": 91,
    "phase": {
      "phase": " -gib",
      "phaseAngle": 214.7
    }
  },
  "venus": {
    "GHA": {
      "raw": 64.85952940578073,
      "fmt": "064° 51' 34\""
    },
    "RA": {
      "raw": 4.353898312391831,
      "fmt": "00h 17m 24.9s"
    },
    "DEC": {
      "raw": 1.6319770044428068,
      "fmt": "N   01° 37' 55\""
    },
    "SD": {
      "raw": 8.266740814849628,
      "fmt": "8.3\""
    },
    "HP": {
      "raw": 8.644199610676294,
      "fmt": "8.6\""
    },
    "illum": 69.8
  },
  "mars": {
    "GHA": {
      "raw": 162.72083658880223,
      "fmt": "162° 43' 15\""
    },
    "RA": {
      "raw": 266.49259112937034,
      "fmt": "17h 45m 58.2s"
    },
    "DEC": {
      "raw": -23.532797272560273,
      "fmt": "S   23° 31' 58\""
    },
    "SD": {
      "raw": 2.5144066939815386,
      "fmt": "2.5\""
    },
    "HP": {
      "raw": 4.724720612579841,
      "fmt": "4.7\""
    },
    "illum": 92.4
  },
  "jupiter": {
    "GHA": {
      "raw": 141.89748479874265,
      "fmt": "141° 53' 51\""
    },
    "RA": {
      "raw": 287.3159429194299,
      "fmt": "19h 09m 15.8s"
    },
    "DEC": {
      "raw": -22.458393242885755,
      "fmt": "S   22° 27' 30\""
    },
    "SD": {
      "raw": 16.483945823855873,
      "fmt": "16.5\""
    },
    "HP": 1.4725702923099202,
    "illum": 99.7
  },
  "saturn": {
    "GHA": {
      "raw": 130.94093818782687,
      "fmt": "130° 56' 27\""
    },
    "RA": {
      "raw": 298.2724895303457,
      "fmt": "19h 53m 05.4s"
    },
    "DEC": {
      "raw": -20.894138935246513,
      "fmt": "S   20° 53' 39\""
    },
    "SD": {
      "raw": 7.586006087138657,
      "fmt": "7.6\""
    },
    "HP": {
      "raw": 0.8063741995684438,
      "fmt": "0.8\""
    },
    "illum": 100
  },
  "polaris": {
    "GHA": {
      "raw": 25.043779995214884,
      "fmt": "025° 02' 38\""
    },
    "RA": {
      "raw": 44.169647722957684,
      "fmt": "02h 56m 40.7s"
    },
    "DEC": {
      "raw": 89.35252081672385,
      "fmt": "N   89° 21' 09\""
    }
  },
  "sidTmean": {
    "raw": 69.21742916898802,
    "fmt": "4h 36m 52.183s"
  },
  "sidTapp": {
    "raw": 69.21342771817257,
    "fmt": "4h 36m 51.223s"
  },
  "EoEquin": -0.96,
  "dPsi": -15.7,
  "dEps": -0.614,
  "obliq": {
    "raw": 23.43667557083188,
    "fmt": "23° 26' 12.032\""
  },
  "trueObliq": {
    "raw": 23.43650488421495,
    "fmt": "23° 26' 11.418\""
  },
  "julianDay": 2458891.299711,
  "julianEphemDay": 2458891.300509,
  "lunarDist": {
    "raw": 144.95704848913826,
    "fmt": "144° 57' 25\""
  },
  "dayOfWeek": "TUE"
}
```

