"use strict";

// TODO Move to ES6

var checksum = function (str) {
    var cs = 0;
    for (var i = 0; i < str.length; i++) {
        var c = str.charCodeAt(i);
        cs ^= c;
    }
    var ccs = cs.toString(16).toUpperCase();
    while (ccs.length < 2) {
        ccs = '0' + ccs;
    }
    return ccs;
};

var validate = function (str) {
    if (str.charAt(0) !== '$') {
        throw({
            desc: 'Does not start with $',
            data: str
        });
    }
    if (str.charAt(6) !== ',') {
        throw({
            desc: 'Invalid key length',
            data: str
        });
    }
    var starIdx = str.indexOf('*');
    if (starIdx === -1) {
        throw({
            desc: 'Missing checksum',
            data: str
        });
    }
    var checksumStr = str.substring(starIdx + 1).replace(/\n$/, "").replace(/\r$/, "");
    var nmea = str.substring(1, starIdx);
    var cs = checksum(nmea);
    if (checksumStr !== cs) {
        console.log("Expected", cs, "in", str, "[" + checksumStr + "]");
        throw({
            desc: 'Invalid checksum',
            expected: cs,
            found: checksumStr,
            data: str
        });
    }
    var talker = str.substring(1, 3);
    var sentenceId = str.substring(3, 6);
    return {talker: talker, id: sentenceId};
};

var getChunks = function (str) {
    var starIdx = str.indexOf('*');
    try {
        var valid = validate(str);
    } catch (err) {
        throw {
            validating: str,
            error: err
        };
    }
    var nmea = str.substring(1, starIdx);
    var chunks = nmea.split(",");
    return {
        valid: valid,
        data: chunks
    };
};

var parseRMC = function (str) {
    /* Structure is
     *         1      2 3        4 5         6 7     8     9      10    11      <- Indexes in getChunks.
     *  $ddRMC,123519,A,4807.038,N,01131.000,E,022.4,084.4,230394,003.1,W*6A
     *         |      | |        | |         | |     |     |      |     |
     *         |      | |        | |         | |     |     |      |     Variation sign
     *         |      | |        | |         | |     |     |      Variation value
     *         |      | |        | |         | |     |     Date DDMMYY
     *         |      | |        | |         | |     COG
     *         |      | |        | |         | SOG
     *         |      | |        | |         Longitude Sign
     *         |      | |        | Longitude Value
     *         |      | |        Latitude Sign
     *         |      | Latitude value
     *         |      Active or Void
     *         UTC
     */
    var data = getChunks(str).data;

    if (data[2] === 'V') {
        return;
    }
    var latDeg = data[3].substring(0, 2);
    var latMin = data[3].substring(2);
    var lat = sexToDec(parseInt(latDeg), parseFloat(latMin)) * (data[4] === 'S' ? -1 : 1);

    var lonDeg = data[5].substring(0, 3);
    var lonMin = data[5].substring(3);
    var lon = sexToDec(parseInt(lonDeg), parseFloat(lonMin)) * (data[6] === 'W' ? -1 : 1);

    var hours = parseInt(data[1].substring(0, 2));
    var minutes = parseInt(data[1].substring(2, 4));
    var seconds = parseInt(data[1].substring(4, 6));

    var day = parseInt(data[9].substring(0, 2));
    var month = parseInt(data[9].substring(2, 4)) - 1;
    var year = parseInt(data[9].substring(4, 6)) + 2000;
    var d = new Date(Date.UTC(year, month, day, hours, minutes, seconds, 0));

    var sog = parseFloat(data[7]);
    var cog = parseFloat(data[8]);
    var W = parseFloat(data[10]) * (data[11] === 'W' ? -1 : 1);
    return {type: "RMC", epoch: d.getTime(), sog: sog, cog: cog, variation: W, pos: {lat: lat, lon: lon}};
};

var parseDBT = function (str) {
    /* Structure is
     *         1     2 3    4 5    6
     *  $aaDBT,011.0,f,03.3,M,01.8,F*18
     *         |     | |    | |    |
     *         |     | |    | |    F for fathoms
     *         |     | |    | Depth in fathoms
     *         |     | |    M for meters
     *         |     | Depth in meters
     *         |     f for feet
     *         Depth in feet
     */
    var data = getChunks(str).data;
    return {
        type: "DBT",
        feet: parseFloat(data[1]),
        meters: parseFloat(data[3]),
        fathoms: parseFloat(data[5])
    };
};

var parseDPT = function (str) {
    /* Structure is
     *         1     2
     *  $IIDPT,001.7,+0.7,*46
     *         |     |
     *         |     correction
     *         Depth in meters
     */
    var data = getChunks(str).data;
    return {
        type: "DPT",
        depth: parseFloat(data[1]),
        correction: parseFloat(data[2])
    };
};

var parseGLL = function (str) {
    /* Structure is
     *         1       2 3       4 5         6
     *  $aaGLL,llll.ll,a,gggg.gg,a,hhmmss.ss,A*hh
     *         |       | |       | |         |
     *         |       | |       | |         A:data valid
     *         |       | |       | UTC of position
     *         |       | |       Long sign :E/W
     *         |       | Longitude
     *         |       Lat sign :N/S
     *         Latitude
     */
    var data = getChunks(str).data;
    if ("A" !== data[6]) {
        throw {err: "No data available"};
    }
    var latDeg = data[1].substring(0, 2);
    var latMin = data[1].substring(2);
    var lat = sexToDec(parseInt(latDeg), parseFloat(latMin)) * (data[2] === 'S' ? -1 : 1);

    var lonDeg = data[3].substring(0, 3);
    var lonMin = data[3].substring(3);
    var lon = sexToDec(parseInt(lonDeg), parseFloat(lonMin)) * (data[4] === 'W' ? -1 : 1);

    var hours = parseInt(data[5].substring(0, 2));
    var minutes = parseInt(data[5].substring(2, 4));
    var seconds = parseInt(data[5].substring(4, 6));
    var now = new Date();
    var d = new Date(Date.UTC(now.getUTCFullYear(), now.getUTCMonth(), now.getUTCDate(), hours, minutes, seconds, 0));

    return {
        type: "GLL",
        latitude: lat,
        longitude: lon,
        epoch: d.getTime()
    };
};

var parseGGA = function (str) {
    /* Structure is
     *  $GPGGA,014457,3739.853,N,12222.821,W,1,03,5.4,1.1,M,-28.2,M,,*7E
     *
     *         1         2       3 4       5 6 7  8   9   10    12    14
     *                                                      11    13
     *  $aaGGA,hhmmss.ss,llll.ll,a,gggg.gg,a,x,xx,x.x,x.x,M,x.x,M,x.x,xxxx*CS
     *         |         |         |         | |  |   |   | |   | |   |
     *         |         |         |         | |  |   |   | |   | |   Differential reference station ID
     *         |         |         |         | |  |   |   | |   | Age of differential GPS data (seconds)
     *         |         |         |         | |  |   |   | |   Unit of geodial separation, meters
     *         |         |         |         | |  |   |   | Geodial separation
     *         |         |         |         | |  |   |   Unit of antenna altitude, meters
     *         |         |         |         | |  |   Antenna altitude above sea level
     *         |         |         |         | |  Horizontal dilution of precision
     *         |         |         |         | number of satellites in use 00-12 (in use, not in view!)
     *         |         |         |         GPS quality indicator (0:invalid, 1:GPS fix, 2:DGPS fix)
     *         |         |         Longitude
     *         |         Latitude
     *         UTC of position
     */
    var data = getChunks(str).data;

    var hours = parseInt(data[1].substring(0, 2));
    var minutes = parseInt(data[1].substring(2, 4));
    var seconds = parseInt(data[1].substring(4, 6));
    var now = new Date();
    var d = new Date(Date.UTC(now.getUTCFullYear(), now.getUTCMonth(), now.getUTCDate(), hours, minutes, seconds, 0));

    var latDeg = data[2].substring(0, 2);
    var latMin = data[2].substring(2);
    var lat = sexToDec(parseInt(latDeg), parseFloat(latMin)) * (data[3] === 'S' ? -1 : 1);
    var lonDeg = data[4].substring(0, 3);
    var lonMin = data[4].substring(3);
    var lon = sexToDec(parseInt(lonDeg), parseFloat(lonMin)) * (data[5] === 'W' ? -1 : 1);

    return {
        type: "GGA",
        epoch: d.getTime(),
        position: {
            latitude: lat,
            longitude: lon
        },
        quality: data[6],
        nbsat: parseInt(data[7]),
        dilution: parseFloat(data[8]),
        antenna: {
            altitude: parseFloat(data[9]),
            unit: data[10]
        },
        geodial: {
            separation: parseFloat(data[11]),
            unit: data[12]
        },
        age: parseFloat(data[13]),
        refId: data[14]
    };
};

var parseGSA = function (str) {
    /*
     *        1 2 3                           15  16  17
     * $GPGSA,A,3,19,28,14,18,27,22,31,39,,,,,1.7,1.0,1.3*35
     *        | | |                           |   |   |
     *        | | |                           |   |   VDOP
     *        | | |                           |   HDOP
     *        | | |                           PDOP (dilution of precision). No unit; the smaller the better.
     *        | | IDs of the SVs used in fix (up to 12)
     *        | Mode: 1=Fix not available, 2=2D, 3=3D
     *        Mode: M=Manual, forced to operate in 2D or 3D
     *              A=Automatic, 3D/2D
     */
    var data = getChunks(str).data;
    var satId = [];
    for (var i = 3; i <= 15; i++) {
        if (data[i].trim().length > 0) {
            satId.push(parseInt(data[i]));
        } else {
            break;
        }
    }
    return {
        type: "GSA",
        mode: data[1],
        ids: satId,
        pdop: parseFloat(data[15]),
        hdop: parseFloat(data[16]),
        vdop: parseFloat(data[17])
    };
};

var gsvData = {};

var parseGSV = function (str) {
    /* Structure is
     *        1 2 3  4  5  6   7  8  9  10  11 12 13 14  15 16 17 18  19
     * $GPGSV,3,1,11,03,03,111,00,04,15,270,00,06,01,010,00,13,06,292,00*74
     *        | | |  |  |  |   |  |            |            |
     *        | | |  |  |  |   |  |            |            Fourth SV...
     *        | | |  |  |  |   |  |            Third SV...
     *        | | |  |  |  |   |  Second SV...
     *        | | |  |  |  |   SNR (0-99 db)
     *        | | |  |  |  Azimuth in degrees (0-359)
     *        | | |  |  Elevation in degrees (0-90)
     *        | | |  First SV PRN Number
     *        | | Total number of SVs in view
     *        | Message Number
     *        Number of messages in this cycle
     *
     * Sample:
     *  $GPGSV,3,1,12,21,11,137,32,14,64,212,32,10,55,053,35,32,85,063,14*75
     *  $GPGSV,3,1,12,21,11,137,32,14,64,212,32,10,55,053,35,32,85,063,14*75
     *  $GPGSV,3,1,12,21,11,137,32,14,64,212,32,10,55,053,35,32,85,063,14*75
     *  $GPGSV,3,2,12,27,31,223,29,18,28,079,32,31,14,165,31,24,11,041,25*7B
     *  $GPGSV,3,2,12,27,31,223,29,18,28,079,32,31,14,165,31,24,11,041,25*7B
     *  $GPGSV,3,2,12,27,31,223,29,18,28,079,32,31,14,165,31,24,11,041,25*7B
     *  $GPGSV,3,3,12,08,34,273,11,11,27,310,14,01,13,315,,22,08,278,*70
     *  $GPGSV,3,3,12,08,34,273,11,11,27,310,14,01,13,315,,22,08,278,*70
     *  $GPGSV,3,3,12,08,34,273,11,11,27,310,14,01,13,315,,22,08,278,*70
     */
    var data = getChunks(str).data;
    var nbMess = parseInt(data[1]);
    var messNum = parseInt(data[2]);
    var numSat = parseInt(data[3]);
    if (messNum === 1) { // First message of the list
        gsvData = {type: "GSV", satData: []};
        for (var s = 0; s < numSat; s++) {
            gsvData.satData.push({});
        }
    }

    for (var i = 0; i < 4; i++) {
        var sat = {
            prn: parseInt(data[4 + (4 * i)]),
            elevation: parseInt(data[4 + (4 * i) + 1]),
            azimuth: parseInt(data[4 + (4 * i) + 2]),
            snr: parseInt(data[4 + (4 * i) + 3])
        };
        gsvData.satData[((messNum - 1) * 4) + i] = sat;
    }
    if (messNum === nbMess) { // Last message of the list
        return gsvData;
    } else {
        return {type: "GSV"};
    }
};

var parseHDG = function (str) {
    /* Structure is
     *        1   2   3 4   5
     * $xxHDG,x.x,x.x,a,x.x,a*CS
     *        |   |   | |   | |
     *        |   |   | |   | Checksum
     *        |   |   | |   Magnetic Variation direction, E = Easterly, W = Westerly
     *        |   |   | Magnetic Variation degrees
     *        |   |   Magnetic Deviation direction, E = Easterly, W = Westerly
     *        |   Magnetic Deviation, degrees
     *        Magnetic Sensor heading in degrees
     */
    var data = getChunks(str).data;
    var hdg = parseFloat(data[1]);
    var dev = parseFloat(data[2]);
    var dec = parseFloat(data[4]);
    if (isNaN(hdg)) {
      hdg = null;
    }
    if (isNaN(dev)) {
        dev = null;
    } else {
        dev *= (data[3] === 'W' ? -1 : 1);
    }
    if (isNaN(dec)) {
        dec = null;
    } else {
        dec *= (data[5] === 'W' ? -1 : 1);
    }
    return {
        hdg: hdg,
        dev: dev,
        dec: dec
    };
};

var parseHDM = function (str) {
    /*
     * Structure is
     *        1   2
     * $--HDM,x.x,M*hh
     *        |   |
     *        |   magnetic
     *        Heading, magnetic, in degrees
     */
    var data = getChunks(str).data;
    return {
        type: "HDM",
        heading: parseFloat(data[1])
    };
};

var parseMDA = function (str) {
    /*                                             13    15    17    19
     *        1   2 3   4 5   6 7   8 9   10  11  12    14    16    18    20
     * $--MDA,x.x,I,x.x,B,x.x,C,x.x,C,x.x,x.x,x.x,C,x.x,T,x.x,M,x.x,N,x.x,M*hh
     *        |     |     |     |     |   |   |     |     |     |     Wind speed, m/s
     *        |     |     |     |     |   |   |     |     |     Wind speed, knots
     *        |     |     |     |     |   |   |     |     Wind dir Mag
     *        |     |     |     |     |   |   |     Wind dir, True
     *        |     |     |     |     |   |   Dew Point C
     *        |     |     |     |     |   Absolute hum %
     *        |     |     |     |     Relative hum %
     *        |     |     |     Water temp in Celcius
     *        |     |     Air Temp in Celcius  |
     *        |     Pressure in Bars
     *        Pressure in inches
     *
     * Example: $WIMDA,29.4473,I,0.9972,B,17.2,C,,,,,,,,,,,,,,*3E
     */
    var data = getChunks(str).data;
    return {
        type: "MDA",
        pressure: {
            inches: parseFloat(data[1]),
            bars: parseFloat(data[3])
        },
        temperature: {
            air: parseFloat(data[5]),
            water: parseFloat(data[7])
        },
        humidity: {
            relative: parseFloat(data[9]),
            absolute: parseFloat(data[10]),
            dewpoint: parseFloat(data[11])
        },
        wind: {
            dir: {
                true: parseFloat(data[13]),
                magnetic: parseFloat(data[15])
            },
            speed: {
                knots: parseFloat(data[17]),
                ms: parseFloat(data[19])
            }
        }
    };
};

var parseMMB = function (str) {
    /*
     * Structure is
     *        1       2 3      4
     * $IIMMB,29.9350,I,1.0136,B*7A
     *        |       | |      |
     *        |       | |      Bars
     *        |       | Pressure in Bars
     *        |       Inches of Hg
     *        Pressure in inches of Hg
     */
    var data = getChunks(str).data;
    return {
        type: "MMB", pressure: {
            inches: parseFloat(data[1]),
            bars: parseFloat(data[3])
        }
    };
};

var parseMTA = function (str) {
    /*
     * Structure is
     *        1   2
     * $RPMTA,9.9,C*37
     *        |   |
     *        |   Celcius
     *        Value
     */
    var data = getChunks(str).data;
    return {type: "MTA", temp: parseFloat(data[1]), unit: data[2]};
};

var parseMTW = function (str) {
    /*
     * Structure is
     *         1    2
     * $IIMTW,+18.0,C*31
     *         |    |
     *         |    Celcius
     *         Value
     */
    var data = getChunks(str).data;
    return {type: "MTW", temp: parseFloat(data[1]), unit: data[2]};
};

var parseMWV = function (str) {
    /*
     * Structure is:
     *         1    2 3    4 5
     *  $IIMWV,256, R,07.1,N,A*14
     *  $aaMWV,xx.x,a,x.x,a,A*hh
     *         |    | |   | |
     *         |    | |   | status : A=data valid
     *         |    | |   Wind Speed unit (K/M/N)
     *         |    | Wind Speed
     *         |    reference R=relative, T=true
     *         Wind angle 0 to 360 degrees
     */
    var data = getChunks(str).data;
    if (data[5] !== 'A') {
        throw {err: "No data available for MWV"}
    } else {
        return {
            type: "MWV",
            wind: {
                speed: parseFloat(data[3]),
                dir: parseFloat(data[1]),
                unit: data[4],
                reference: (data[2] === 'R' ? 'relative' : 'true')
            }
        };
    }
};

var parseRMB = function (str) {
    /*        1 2   3 4    5    6       7 8        9 10  11  12  13
     * $GPRMB,A,x.x,a,c--c,d--d,llll.ll,e,yyyyy.yy,f,g.g,h.h,i.i,j*kk
     *        | |   | |    |    |       | |        | |   |   |   |
     *        | |   | |    |    |       | |        | |   |   |   A=Entered or perpendicular passed, V:not there yet
     *        | |   | |    |    |       | |        | |   |   Destination closing velocity in knots
     *        | |   | |    |    |       | |        | |   Bearing to destination, degrees, True
     *        | |   | |    |    |       | |        | Range to destination, nm
     *        | |   | |    |    |       | |        E or W
     *        | |   | |    |    |       | Destination Waypoint longitude
     *        | |   | |    |    |       N or S
     *        | |   | |    |    Destination Waypoint latitude
     *        | |   | |    Destination Waypoint ID
     *        | |   | Origin Waypoint ID
     *        | |   Direction to steer (L or R) to correct error
     *        | Crosstrack error in nm
     *        Data Status (Active or Void)
     */
    var data = getChunks(str).data;
    if (data[1] === 'A') {
        var latDeg = data[6].substring(0, 2);
        var latMin = data[6].substring(2);
        var lat = sexToDec(parseInt(latDeg), parseFloat(latMin));
        if (data[7] === 'S') {
            lat = -lat;
        }
        var lonDeg = data[8].substring(0, 3);
        var lonMin = data[8].substring(3);
        var lon = sexToDec(parseInt(lonDeg), parseFloat(lonMin));
        if (data[9] === 'W') {
            lon = -lon;
        }

        return {
            type: "RMB",
            crosstack: {
                error: parseFloat(data[2]),
                steer: data[3]
            },
            waypoints: {
                origin: {
                    id: data[4]
                },
                destination: {
                    id: data[5],
                    latitude: lat,
                    longitude: lon
                }
            },
            range: parseFloat(data[10]),
            bearing: parseFloat(data[11]),
            closingspeed: parseFloat(data[12])
        };
    } else {
        return {
            type: " RMB",
            mess: "No data"
        };
    }
};

var parseVDR = function (str) {
    /*
     * Structure is
     *        1   2 3   4 5   6
     * $--VDR,x.x,T,x.x,M,x.x,N*hh
     *        |   | |   | |   |
     *        |   | |   | |   Knots
     *        |   | |   | Speed of current (in knots)
     *        |   | |   Magnetic
     *        |   | Degrees, magnetic
     *        |   True
     *        Degrees, true
     */
    var data = getChunks(str).data;
    return {
        type: " VDR",
        current: {
            dir: {
                true: parseFloat(data[1]),
                magnetic: parseFloat(data[3])
            },
            speed: {
                knots: parseFloat(data[5])
            }
        }
    };
};

var parseVHW = function (str) {
    /* Structure is
     *         1   2 3   4 5   6 7   8
     *  $aaVHW,x.x,T,x.x,M,x.x,N,x.x,K*hh
     *         |     |     |     |
     *         |     |     |     Speed in km/h
     *         |     |     Speed in knots
     *         |     Heading in degrees, Magnetic
     *         Heading in degrees, True
     */
    var data = getChunks(str).data;
    return {
        type: "VHW",
        heading: {
            true: parseFloat(data[1]),
            magnetic: parseFloat(data[3])
        },
        speed: {
            knots: parseFloat(data[5]),
            kmh: parseFloat(data[7])
        }
    }
};

var parseVLW = function (str) {
    /*
     * Structure is
     *        1     2 3     4
     * $IIVLW,08200,N,000.0,N*59
     *        |     | |     |
     *        |     | |     Nautical miles
     *        |     | Distance since reset
     *        |     Nautical miles
     *        Total cumulative distance
     */
    var data = getChunks(str).data;
    return {
        type: "VLW",
        total: parseFloat(data[1]),
        sincereset: parseFloat(data[3])
    };
};

var parseVTG = function (str) {
    /*
     * Structure is
     *        1   2 3   4 5   6 7   8 9
     * $--VTG,x.x,T,x.x,M,x.x,N,x.x,K,m,*hh
     *        |   | |   | |   | |   | |
     *        |   | |   | |   | |   | FFA mode indicator
     *        |   | |   | |   | |   km/h
     *        |   | |   | |   | Speed in km/h
     *        |   | |   | |   knots
     *        |   | |   | Speed in knots
     *        |   | |   magnetic
     *        |   | Track degrees
     *        |   true
     *        Track, degrees
     */
    var data = getChunks(str).data;
    return {
        type: "VTG",
        cmg: {
            true: parseFloat(data[1]),
            magnetic: parseFloat(data[3])
        },
        speed: {
            knots: parseFloat(data[5]),
            kmh: parseFloat(data[7])
        }
    };
};

var parseVWR = function (str) {
    /*
     * Structure is
     *         1   2 3   4 5   6 7   8
     *  $aaVWR,x.x,a,x.x,N,x.x,M,x.x,K*hh
     *         |   | |     |     |
     *         |   | |     |     Wind Speed, in km/h
     *         |   | |     Wind Speed, in m/s
     *         |   | Wind Speed, in knots
     *         |   L=port, R=starboard
     *         Wind angle 0 to 180 degrees
     */
    var data = getChunks(str).data;
    return {
        type: " VWR",
        wind: {
            dir: parseFloat(data[1]) * (data[2] === 'L' ? -1 : 1),
            speed: {
                knots: parseFloat(data[3]),
                ms: parseFloat(data[5]),
                kmh: parseFloat(data[7])
            }
        }
    };
};

var parseVWT = function (str) {
    /*
     * Structure is
     *        1    2 3   4 5   6 7   8
     * $OSVWT,77.0,L,5.3,N,2.7,M,9.8,K*7F
     *        |    | |   | |   | |   |
     *        |    | |   | |   | |   km/h
     *        |    | |   | |   | In km/h
     *        |    | |   | |   m/s
     *        |    | |   | In m/s
     *        |    | |   knots
     *        |    | In knots
     *        |    Left or Right
     *        Wind angle
     */
    var data = getChunks(str).data;
    return {
        type: " VWT",
        wind: {
            dir: parseFloat(data[1]) * (data[2] === 'L' ? -1 : 1),
            speed: {
                knots: parseFloat(data[3]),
                ms: parseFloat(data[5]),
                kmh: parseFloat(data[7])
            }
        }
    };
};

var parseMWD = function (str) {
    /*
     * Structure is:
     *        1     2 3     4 5   6 7   8
     * $OSMWD,307.0,T,292.0,M,5.0,N,2.6,M*54
     *        |     | |     | |   | |   |
     *        |     | |     | |   | |   m/s
     *        |     | |     | |   | Speed in m/s
     *        |     | |     | |   knots
     *        |     | |     | Speed in knots
     *        |     | |     Magnetic
     *        |     | wind dir
     *        |     True
     *        wind dir
     */
    var data = getChunks(str).data;
    return {
        type: "MWD",
        wind: {
            dir: {
                true: parseFloat(data[1]),
                magnetic: parseFloat(data[3])
            },
            speed: {
                knots: parseFloat(data[5]),
                ms: parseFloat(data[7])
            }
        }
    };
};

var parseXDR = function (str) {
    /*
     *        1 2      3 4
     * $RPXDR,P,1.0280,B,0*7B
     * $IIXDR,P,1.0136,B,BMP180,C,15.5,C,BMP180*58
     *
     *        1 2      3 4 5 6    7 8 9 10   11
     *                                        12
     * $IIXDR,P,1.0136,B,0,C,15.5,C,1,H,65.5,P,2*6B
     *
     * Indexes:
     * 1 Type of data
     * 2 Value
     * 3 Unit
     * 4 Name or index of the transducer
     *
     XDR - Transducer Measurements
     $--XDR,a,x.x,a,c--c,...    ...a,x.x,a,c--c*hh<CR><LF>
     | |   | |    |        ||     |
     | |   | |    |        |+-----+-- Transducer 'n'
     | |   | |    +--------+- Data for variable # of transducers
     | |   | +- Transducer #1 ID
     | |   +- Units of measure, Transducer #1
     | +- Measurement data, Transducer #1
     +- Transducer type, Transducer #1
     Notes:
     1) Sets of the four fields 'Type-Data-Units-ID' are allowed for an undefined number of transducers.
     Up to 'n' transducers may be included within the limits of allowed sentence length, null fields are not
     required except where portions of the 'Type-Data-Units-ID' combination are not available.
     2) Allowed transducer types and their units of measure are:
     Transducer           Type Field  Units Field              Comments
     -------------------------------------------------------------------
     temperature            C           C = degrees Celsius
     angular displacement   A           D = degrees            "-" = anti-clockwise
     linear displacement    D           M = meters             "-" = compression
     frequency              F           H = Hertz
     force                  N           N = Newton             "-" = compression
     pressure               P           B = Bars, P = Pascal   "-" = vacuum
     flow rate              R           l = liters/second
     tachometer             T           R = RPM
     humidity               H           P = Percent
     volume                 V           M = cubic meters
     generic                G           none (null)            x.x = variable data
     current                I           A = Amperes
     voltage                U           V = Volts
     switch or valve        S           none (null)            1 = ON/ CLOSED, 0 = OFF/ OPEN
     salinity               L           S = ppt                ppt = parts per thousand
     */
    var data = getChunks(str).data;
    var txIdx = 0;
    var moreData = true;
    var parsed = [];
    while (moreData) {
        var type = data[(txIdx * 4) + 1];
        if (type !== undefined) {
            var txData = {};
            switch (type) {
                case "C":
                    txData.type = "temperature";
                    break;
                case "A":
                    txData.type = "angular displacement";
                    break;
                case "D":
                    txData.type = "linear displacement";
                    break;
                case "F":
                    txData.type = "frequency";
                    break;
                case "N":
                    txData.type = "force";
                    break;
                case "P":
                    txData.type = "pressure";
                    break;
                case "R":
                    txData.type = "flow rate";
                    break;
                case "T":
                    txData.type = "tachometer";
                    break;
                case "H":
                    txData.type = "humidity";
                    break;
                case "V":
                    txData.type = "volume";
                    break;
                case "G":
                    txData.type = "generic";
                    break;
                case "I":
                    txData.type = "current";
                    break;
                case "U":
                    txData.type = "voltage";
                    break;
                case "S":
                    txData.type = "switch or valve";
                    break;
                case "L":
                    txData.type = "salinity";
                    break;
                default:
                    txData.type = "unknown type [" + type + "]";
                    break;
            }
            txData.value = parseFloat(data[(txIdx * 4) + 2]);
            txData.unit = data[(txIdx * 4) + 3];
            parsed.push(txData);
            txIdx += 1;
        } else {
            moreData = false;
        }
    }
    return {type: "XDR", data: parsed};
};


var sexToDec = function (deg, min) {
    return deg + ((min * 10 / 6) / 100);
};

/**
 * Converts decimal degrees into Deg Min.dd
 * @param val value in decimal degrees
 * @param ns_ew 'NS' or 'EW'
 * @returns {string}
 */
var decToSex = function (val, ns_ew) {
    var absVal = Math.abs(val);
    var intValue = Math.floor(absVal);
    var dec = absVal - intValue;
    var i = intValue;
    dec *= 60;
    var s = i + "°" + dec.toFixed(2) + "'";

    if (val < 0) {
        s += (ns_ew === 'NS' ? 'S' : 'W');
    } else {
        s += (ns_ew === 'NS' ? 'N' : 'E');
    }
    return s;
};

var matcher = {};
matcher["RMC"] = {parser: parseRMC, desc: "Recommended Minimum Navigation Information"};
matcher["DBT"] = {parser: parseDBT, desc: "Depth Below Transducer"};
matcher["DPT"] = {parser: parseDPT, desc: "Depth"};
matcher["GLL"] = {parser: parseGLL, desc: "Geographic Position, Latitude / Longitude"};
matcher["GGA"] = {parser: parseGGA, desc: "Global Positioning System Fix Data"};
matcher["GSA"] = {parser: parseGSA, desc: "GPS DOP and active satellites"};
matcher["GSV"] = {parser: parseGSV, desc: "Satellites in view"};
matcher["HDG"] = {parser: parseHDG, desc: "Magnetic heading, deviation, variation"};
matcher["HDM"] = {parser: parseHDM, desc: "Heading, Magnetic"};
matcher["MDA"] = {parser: parseMDA, desc: "Meteorological Composite"};
matcher["MMB"] = {parser: parseMMB, desc: "Humidity"};
matcher["MTA"] = {parser: parseMTA, desc: "Air Temperature"};
matcher["MTW"] = {parser: parseMTW, desc: "Mean Temperature of Water"};
matcher["MWV"] = {parser: parseMWV, desc: "Wind Speed and Angle"};
matcher["MWD"] = {parser: parseMWD, desc: "Wind Direction & Speed"};
matcher["RMB"] = {parser: parseRMB, desc: "Recommended Minimum Navigation Information"};
matcher["VDR"] = {parser: parseVDR, desc: "Set and Drift"};
matcher["VHW"] = {parser: parseVHW, desc: "Water speed and heading"};
matcher["VTG"] = {parser: parseVTG, desc: "Track Made Good and Ground Speed"};
matcher["VWR"] = {parser: parseVWR, desc: "Relative Wind Speed and Angle"};
matcher["VWT"] = {parser: parseVWT, desc: "True Windspeed and Angle"};
matcher["XDR"] = {parser: parseXDR, desc: "Transducer Values"};
matcher["VLW"] = {parser: parseVLW, desc: "Distance Traveled through Water"};

var autoparse = function (str) {
    var id = getChunks(str).valid.id;
    var parser = matcher[id].parser;
    if (parser !== undefined) {
        var autoParsed = parser(str);
        autoParsed.type = id;
        return autoParsed; // parser(str);
    } else {
        throw {err: "No parser found for sentence [" + str + "]"}
    }
};

// Tests
var tests = function () {
    var val = sexToDec(333, 22.07);
    console.log(val);
    var ret = decToSex(val, 'EW');
    console.log(ret);

    var rmc = "$IIRMC,225158,A,3730.075,N,12228.854,W,,,021014,15,E,A*3C";
    console.log(rmc);
    console.log(validate(rmc));
    var parsed = parseRMC(rmc);
    console.log(parsed);
    console.log("Pos: " + decToSex(parsed.pos.lat, 'NS') + " " + decToSex(parsed.pos.lon, 'NS'));
    var date = new Date(parsed.epoch);
    console.log(date);

    console.log('--- AutoParse ---');
    var auto = autoparse("$IIHDG,217,,,10,E*17");
    console.log(auto);
};


// tests();

// Made public.
exports.validate = validate;
exports.autoparse = autoparse;
exports.toDegMin = decToSex;
exports.parseRMC = parseRMC;
exports.parseDBT = parseDBT;
exports.parseDPT = parseDPT;
exports.parseGLL = parseGLL;
exports.parseGGA = parseGGA;
exports.parseGSA = parseGSA;
exports.parseGSV = parseGSV;
exports.parseHDG = parseHDG;
exports.parseHDM = parseHDM;
exports.parseMDA = parseMDA;
exports.parseMMB = parseMMB;
exports.parseMTA = parseMTA;
exports.parseMTW = parseMTW;
exports.parseMWV = parseMWV;
exports.parseRMB = parseRMB;
exports.parseVDR = parseVDR;
exports.parseVHW = parseVHW;
exports.parseVLW = parseVLW;
exports.parseVTG = parseVTG;
exports.parseVWR = parseVWR;
exports.parseVWT = parseVWT;
exports.parseXDR = parseXDR;
