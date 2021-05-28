# JS Celestial Almanac
- Ported in ES6 from [Henning Umland](https://www.celnav.de/) 's code.
- See the original web page [here](https://www.celnav.de/longterm.htm), for reference.
- Henning Umland's "[A Short Guide to Celestial Navigation](http://www.titulosnauticos.net/astro/)"

### Test it from NodeJS (server) with a Web UI
Start the provided node server:
```
 $ cd src/main/ES6
 $ node server.js
 ----------------------------------------------------
Usage: node ./server.js --verbose:true|false --port:XXXX --wdir:path/to/working/dir
----------------------------------------------------
arg #0: /usr/local/bin/node
arg #1: ./server.js
----------------------------------------------------
Your working directory: ...
----------------------------------------------------
Wed Feb 12 2020 09:32:54 GMT-0800 (PST): Starting server on port 8080
Wed Feb 12 2020 09:32:54 GMT-0800 (PST): Server is listening on port 8080

```
Then from a browser, load `http://localhost:8080/index.html` and follow the instructions on the page.

> Or on a Mac, just run the script `start.sh`, in the `ES6` folder. 

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

As you would see, it returns the celestial configuration for the current UTC date.

It returns data for the following bodies:
 - `Sun`
 - `Moon`
 - `Venus`
 - `Mars`
 - `Jupiter`
 - `Saturn`
 - `Polaris`
 
For each body, it gives:
- the Greenwich Hour Angle (`GHA`), raw in decimal degrees and formatted in **degrees**, minutes, and seconds.
- the Right Ascension (`RA`), raw in decimal degrees and formatted in **hours**, minutes and second
- the Declination (`DEC`), raw in decimal degrees and formatted in degrees, minutes and seconds (N or S)
- the Semi-Diameter (`SD`), raw in **seconds** of arc and formatted the same way
- the Horizontal Parallax (`HP`), raw in **seconds** of arc and formatted the same way
- the illumination (`illum`) in %, except for the `Sun` 

`Polaris` obviously does not have semi-diameter, horizontal parallax, nor illumination.

In addition for the `Moon`, there is also the phase (raw in degrees, and formatted for display).

Values are in degrees, from `0` to `360`
- `0` and `360`: New Moon
- `90`: First quarter
- `180`: Full Moon
- `270`: Last Quarter

There is also the Equation of Time (`EoT`), raw in signed decimal **minutes**, and formatted in minutes and seconds.

And also some extra data, related to time, equation of equinoxes, and other goodies.

## How to use it
As shown in the _module_ `app.js` (loaded from `index.html`), you need to import `longterm.almanac.js` (from an `import` statement), and then 
invoke the `calculate` function to get the `JSON` object featured above.

> Note: DeltaT is to be provided at runtime. It can be obtained from [here](http://maia.usno.navy.mil/).

## Production
The script `publish.sh` uses `WebPack` to produce the required artifacts in a `lib` sub-folder.
The resource to point to from `app.js` would be `./lib/celestial-computer.min.js`.


---
