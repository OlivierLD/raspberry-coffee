# TCP Watch
No need for a cell phone, no BlueTooth required.

Ping to the multiplexer returns something like this:
```json
{
  "NMEA_AS_IS": {
    "GGA": "$GPGGA,192308.929,3803.8233,N,12256.5032,W,1,09,1.0,-36.6,M,,,,0000*0C",
    "GSA": "$GPGSA,A,3,10,32,14,20,21,27,08,24,31,,,,1.6,1.0,1.2*3A",
    "RMC": "$GPRMC,192307.929,A,3803.8240,N,12256.5028,W,001.9,212.7,241118,,,A*7A",
    "GSV": [
      "$GPGSV,3,1,12,10,50,061,35,32,84,054,39,04,17,207,32,14,73,210,38*74",
      "$GPGSV,3,2,12,20,27,085,28,21,08,140,26,27,28,221,40,08,34,258,34*7C"
    ]
  },
  "Damping": 1,
  "Current calculated with damping": {},
  "HDG Offset": 0.0,
  "D": {
    "angle": -1.7976931348623157E308
  },
  "Position": {
    "lat": 38.063721666666666,
    "lng": -122.94171999999998
  },
  "Solar Time": {
    "date": "Nov 24, 2018 3:11:21 AM",
    "fmtDate": {
      "epoch": 0,
      "year": 0,
      "month": 0,
      "day": 0,
      "hour": 11,
      "min": 11,
      "sec": 21
    }
  },
  "Delta Altitude": 40.800000000000004,
  "Default Declination": {
    "angle": 0.0
  },
  "Deviation file name": "zero-deviation.csv",
  "SOG": {
    "speed": 1.9
  },
  "GPS Date \u0026 Time": {
    "date": "Nov 24, 2018 11:23:08 AM",
    "epoch": 1543087388000,
    "fmtDate": {
      "epoch": 1543087388000,
      "year": 2018,
      "month": 11,
      "day": 24,
      "hour": 19,
      "min": 23,
      "sec": 8
    }
  },
  "BSP Factor": 1.0,
  "GPS Time": {
    "date": "Nov 24, 2018 11:23:08 AM",
    "fmtDate": {
      "epoch": 0,
      "year": 0,
      "month": 0,
      "day": 0,
      "hour": 19,
      "min": 23,
      "sec": 8
    }
  },
  "Max Leeway": 0.0,
  "COG": {
    "angle": 212.7
  },
  "AWS Factor": 1.0,
  "Satellites in view": {
    "32": {
      "svID": 32,
      "elevation": 84,
      "azimuth": 54,
      "snr": 39
    },
    "18": {
      "svID": 18,
      "elevation": 43,
      "azimuth": 311,
      "snr": 0
    },
    "4": {
      "svID": 4,
      "elevation": 17,
      "azimuth": 207,
      "snr": 27
    },
    "20": {
      "svID": 20,
      "elevation": 27,
      "azimuth": 85,
      "snr": 29
    },
    "21": {
      "svID": 21,
      "elevation": 9,
      "azimuth": 140,
      "snr": 26
    },
    "8": {
      "svID": 8,
      "elevation": 34,
      "azimuth": 258,
      "snr": 39
    },
    "24": {
      "svID": 24,
      "elevation": 6,
      "azimuth": 36,
      "snr": 21
    },
    "10": {
      "svID": 10,
      "elevation": 50,
      "azimuth": 61,
      "snr": 34
    },
    "27": {
      "svID": 27,
      "elevation": 28,
      "azimuth": 221,
      "snr": 0
    },
    "11": {
      "svID": 11,
      "elevation": 27,
      "azimuth": 309,
      "snr": 0
    },
    "14": {
      "svID": 14,
      "elevation": 73,
      "azimuth": 210,
      "snr": 38
    },
    "31": {
      "svID": 31,
      "elevation": 16,
      "azimuth": 165,
      "snr": 38
    }
  },
  "Small Distance": 6934.030890023569,
  "AWA Offset": 0.0,
  "NMEA": "$GPGSV,3,2,12,20,27,085,28,21,08,140,26,27,28,221,40,08,34,258,34*7C",
  "Altitude": -36.6
}
```

---
