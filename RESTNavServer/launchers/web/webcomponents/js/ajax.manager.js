/*
 * @author Olivier Le Diouris
 * Uses ES6 Promises for Ajax.
 */

const DEBUG = false;

function initAjax() {
    let interval = setInterval(() => {
        fetch();
        // console.log(`LoadSunData, gpstime: ${gpsTime}, pos: ${lastKnownPos}`);
        loadSunData({ position: lastKnownPos, utcdate: gpsTime});
    }, 1000);

    // Example:
    // ISS Position http://api.open-notify.org/iss-now.json
    // ISS Passage time http://api.open-notify.org/iss-pass.json?lat=37.7&lon=-122.5 [ &alt=20&n=5 ]
    // ISS Crew members: http://api.open-notify.org/astros.json
    if (false) {
        let issInterval = setInterval(() => {
            let issPromise = getISSData();
            issPromise.then(issData => {
                console.log('ISSData:', issData);
            }, (error, message) => {
                console.debug('ISSData error', error, message);
            });
        }, 5000);
    }
    if (true) {
        let issInterval = setInterval(() => {
            let issCB = document.getElementById('iss-01');
            if (issCB !== undefined && issCB !== null && issCB.checked) {
                let issPromise = getISSDataFromServer();
                issPromise.then(issData => {
                    // console.log('ISSData:', issData);
                    // like {"timestamp": 1598281185, "iss_position": {"longitude": "19.4043", "latitude": "-30.5893"}, "message": "success"}
                    try {
                        setISSData(JSON.parse(issData));
                    } catch (error) {
                        console.log('Error setting ISS data', error);
                    }
                }, (error, message) => {
                    console.debug('ISSData error', error, message);
                });
            }
        }, 5000);
    }
}

const DEFAULT_TIMEOUT = 60000; // 1 minute
/* global events */

/* Uses ES6 Promises */
function getPromise(
    url,                          // full api path
    timeout,                      // After that, fail.
    verb,                         // GET, PUT, DELETE, POST, etc
    happyCode,                    // if met, resolve, otherwise fail.
    data = null,             // payload, when needed (PUT, POST...)
    show = true,          // Show the traffic [true]|false
    headers = null) {        // Array of { name: '', value: '' }

    if (show === true) {
        document.body.style.cursor = 'wait';
    }

    if (DEBUG) {
        console.log(">>> Promise", verb, url);
    }

    let promise = new Promise((resolve, reject) => {
        let xhr = new XMLHttpRequest();
        let TIMEOUT = timeout;

        let req = verb + " " + url;
        if (data !== undefined && data !== null) {
            req += ("\n" + JSON.stringify(data, null, 2));
        }

        xhr.open(verb, url, true);
        if (headers === null) {
            xhr.setRequestHeader("Content-type", "application/json");
        } else {
            headers.forEach(header => xhr.setRequestHeader(header.name, header.value));
        }
        try {
            if (data === undefined || data === null) {
                xhr.send();
            } else {
                xhr.send(JSON.stringify(data));
            }
        } catch (err) {
            console.log("Send Error ", err);
        }

        let requestTimer = setTimeout(() => {
            xhr.abort();
            let mess = {code: 408, message: 'Timeout'};
            reject(mess);
        }, TIMEOUT);

        xhr.onload = () => {
            clearTimeout(requestTimer);
            if (xhr.status === happyCode) {
                resolve(xhr.response);
            } else {
                reject({code: xhr.status, message: xhr.response});
            }
        };
    });
    return promise;
}

function getNMEAData() {
    return getPromise('/mux/cache', DEFAULT_TIMEOUT, 'GET', 200, null, false);
}

/*
 * Doc at http://api.open-notify.org/
 * Also try this: http://api.open-notify.org/astros.json
 */
function getISSDataFromServer() {
    return getPromise('/server/generic-get', DEFAULT_TIMEOUT, 'GET', 200, null, false, [{
        name: 'get-url',
        value: 'http://api.open-notify.org/iss-now.json'
    }]);
}

function getISSData() {
    return getPromise('http://api.open-notify.org/iss-now.json',
        DEFAULT_TIMEOUT,
        'GET',
        200,
        null,
        false,
        [{
            name: 'Access-Control-Allow-Origin',
            value: '*'
        }, {
            name: 'Access-Control-Allow-Methods',
            value: 'GET, POST, PUT, OPTIONS, HEAD'
        }, {
            name: 'Access-Control-Allow-Headers',
            value: 'Content-Type'
        }]);
}

function fetch() {
    let getData = getNMEAData();
    getData.then((value) => { // Resolve
//  console.log("Done:", value);
        try {
            let json = JSON.parse(value);
            onMessage(json);
        } catch (err) {
            console.log("Error:", err, ("\nfor value [" + value + "]"));
        }
    }, (error) => { // Reject
        console.log("Failed to get NMEA data..." + (error !== undefined && error.code !== undefined ? error.code : ' - ') + ', ' + (error !== undefined && error.message !== undefined ? error.message : ' - '));
    });
}

function requestDeclinations(payload) {
    let url = "/astro/declination";
    return getPromise(url, DEFAULT_TIMEOUT, 'POST', 200, payload, false);
}

function requestDevCurve() {
    let url = "/mux/dev-curve";
    return getPromise(url, DEFAULT_TIMEOUT, 'GET', 200, null, false);
}

function requestSunPath(pos) {
    let url = "/astro/sun-path-today";
    return getPromise(url, DEFAULT_TIMEOUT, 'POST', 200, pos, false);
}

function requestSunData(pos) {
    let url = "/astro/sun-now";
    return getPromise(url, DEFAULT_TIMEOUT, 'POST', 200, pos, false);
}

function loadSunData(pos) {
    let getData = requestSunData(pos); // null (for pos) will use the default position. Can also contain the utcdate!!
    getData.then((value) => { // Resolve
//  console.log("Done:", value);
        try {
            let json = JSON.parse(value);

            // console.log('Sun Data for SunPath', json);
            let riseTime = json.riseTime; // epoch
            let riseZ = json.riseZ; // degrees
            let setTime = json.setTime; // epoch
            let setZ = json.setZ; // degrees

            // Specific
            let sunPathElement = document.getElementById('sun-path-01');
            if (sunPathElement !== null && sunPathElement !== undefined) {
                sunPathElement.sunData = json;
                sunPathElement.userPos = {latitude: json.lat, longitude: json.lng};

                sunPathElement.sunRise = {time: riseTime, z: riseZ};
                sunPathElement.sunSet = {time: setTime, z: setZ};

                sunPathElement.repaint();
            }
        } catch (err) {
            console.log("Error:", err, ("\nfor value [" + value + "]"));
        }
    }, (error) => { // Reject
        console.log("Failed to get Sun data..." + (error !== undefined && error.code !== undefined ? error.code : ' - ') + ', ' + (error !== undefined && error.message !== undefined ? error.message : ' - '));
    });
}

/**
 *
 * @param when UTC, Duration format: like "Y-m-dTH:i:s" -> 2018-09-10T10:09:00
 * @param position { lat: 37.7489, lng: -122.507 }
 * @param wandering true|false
 * @param stars true|false
 * @returns {Promise<any>}
 */
function getSkyGP(when, position, wandering, stars) {
    let url = "/astro/positions-in-the-sky";
    // Add date
    url += ("?at=" + when);
    url += ("&fromL=" + position.lat);
    url += ("&fromG=" + position.lng);
    // Wandering bodies
    if (wandering !== undefined && wandering === true) { // to minimize the size of the payload
        url += ("&wandering=true");
    }
    // Stars
    if (stars !== undefined && stars === true) { // to minimize the size of the payload
        url += ("&stars=true");
    }
    return getPromise(url, DEFAULT_TIMEOUT, 'GET', 200, null, false);
}

/**
 *
 * @param when UTC, Duration format: like "Y-m-dTH:i:s" -> 2018-09-10T10:09:00
 * @param position { lat: 37.7489, lng: -122.507 }
 * @param wandering true|false
 * @param stars true|false
 * @returns {Promise<any>}
 */
function getAstroData(when, position, wandering, stars, callback) {
    let getData = getSkyGP(when, position, wandering, stars);
    getData.then((value) => { // resolve
        let json = JSON.parse(value);
        if (callback !== undefined) {
            callback(json);
        } else {
            console.log(JSON.stringify(json, null, 2));
        }
    }, (error) => { // reject
        console.log("Failed to get the Astro Data..." + (error !== undefined && error.code !== undefined ? error.code : ' - ') + ', ' + (error !== undefined && error.message !== undefined ? error.message : ' - '));
    });
}

function setUTC(epoch) {
    let url = "/mux/utc";
    let obj = {epoch: epoch};
    return getPromise(url, DEFAULT_TIMEOUT, 'PUT', 200, obj, false);
}

function setPosition(lat, lng) {
    let url = "/mux/position";
    let obj = {lat: lat, lng: lng};
    return getPromise(url, DEFAULT_TIMEOUT, 'POST', 201, obj, false);
}

function setSOGCOG(sog, cog) {
    let url = "/mux/sog-cog";
    let obj = {sog: {unit: 'kt', sog: sog}, cog: {unit: 'deg', cog: cog}};
    return getPromise(url, DEFAULT_TIMEOUT, 'POST', 201, obj, false);
}

function setUTCTime(epoch, callback) {
    let setData = setUTC(epoch);
    setData.then((value) => { // resolve
        if (value !== undefined && value !== null && value.length > 0) {
            let json = JSON.parse(value);
            if (callback !== undefined) {
                callback(json);
            } else {
                console.log(JSON.stringify(json, null, 2));
            }
        }
    }, (error) => { // reject
        console.log("Failed to set the UTC date and time..." + (error !== undefined && error.code !== undefined ? error.code : ' - ') + ', ' + (error !== undefined && error.message !== undefined ? error.message : ' - '));
    });
}

let lastKnownPos = null;

function setUserPos(lat, lng, callback) {
    lastKnownPos = {
        latitude: lat,
        longitude: lng
    };
    let setData = setPosition(lat, lng);
    setData.then((value) => { // resolve
        if (value !== undefined && value !== null && value.length > 0) {
            let json = JSON.parse(value);
            if (callback !== undefined) {
                callback(json);
            } else {
                console.log(JSON.stringify(json, null, 2));
            }
        }
    }, (error) => { // reject
        console.log("Failed to set the UTC date and time..." + (error !== undefined && error.code !== undefined ? error.code : ' - ') + ', ' + (error !== undefined && error.message !== undefined ? error.message : ' - '));
    });
}

function getQueryParameterByName(name, url) {
    if (!url) url = window.location.href;
    name = name.replace(/[\[\]]/g, "\\$&");
    let regex = new RegExp("[?&]" + name + "(=([^&#]*)|&|#|$)"),
        results = regex.exec(url);
    if (!results) {
        return null;
    }
    if (!results[2]) {
        return '';
    }
    return decodeURIComponent(results[2].replace(/\+/g, " "));
}

/**
 * Takes care of re-broadcasting the data to whoever subscribed to it.
 * Should ideally re-broadcast (aka publish) all the data/event, even if there's no subscriber.
 *
 * See cache.sample.json, for a cache sample.
 *
 * @param json - The cache content.
 * TODO A schema for the json object?
 * 
 * Events/Topics names are defined in pub.sub.js
 */
function onMessage(json) {
    try {
        let errMess = "";

        // Coeffs
        try {
            let bspCoeff = json["BSP Factor"];
            if (bspCoeff !== undefined) {
                events.publish(events.topicNames.BSP_COEFF, bspCoeff);
            }
        } catch (err) {
            // ABSORB
        }
        try {
            let awsCoeff = json["AWS Factor"];
            if (awsCoeff !== undefined) {
                events.publish(events.topicNames.AWS_COEFF, awsCoeff);
            }
        } catch (err) {
            // ABSORB
        }
        try {
            let hdgOffset = json["HDG Offset"];
            if (hdgOffset !== undefined) {
                events.publish(events.topicNames.HDG_OFFSET, hdgOffset);
            }
        } catch (err) {
            // ABSORB
        }
        try {
            let awaOffset = json["AWA Offset"];
            if (awaOffset !== undefined) {
                events.publish(events.topicNames.AWA_OFFSET, awaOffset);
            }
        } catch (err) {
            // ABSORB
        }
        // Position
        try {
            let latitude = json.Position.lat;
            if (DEBUG) {
                console.log("latitude:" + latitude)
            }
            let longitude = json.Position.lng;
            if (DEBUG) {
                console.log("Pt:" + latitude + ", " + longitude);
            }
            let gridSquare = json.Position.gridSquare;
            events.publish(events.topicNames.POS, {
                'lat': latitude,
                'lng': longitude,
                'gridSquare': gridSquare
            });
        } catch (err) {
            errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "position");
        }
        // Displays
        try {
            let bsp = json.BSP.speed;
            events.publish(events.topicNames.BSP, bsp);
        } catch (err) {
            errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "boat speed");
        }
        try {
            let log = json.Log.distance;
            if (DEBUG) {
                console.log(`Publishing LOG ${log}`);
            }
            events.publish(events.topicNames.LOG, log);
        } catch (err) {
            errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "log (" + err + ")");
        }
        try {
            let log = json.Daily.distance;
            if (DEBUG) {
                console.log(`Publishing DAILY-LOG ${log}`);
            }
            events.publish(events.topicNames.DAILY_LOG, log);
        } catch (err) {
            errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "log (" + err + ")");
        }
        try {
            let gdt = json["GPS Date & Time"];
			// let date = new Date(Date.UTC(gdt.fmtDate.year, gdt.fmtDate.month - 1, gdt.fmtDate.day, gdt.fmtDate.hour, gdt.fmtDate.min, gdt.fmtDate.sec, 0));
			// let date = new Date(gdt.epoch); 
            let gpsDate = new Date(gdt.epoch); // new Date(date.getTime() + date.getTimezoneOffset() * 60000);
            // let gpsDate =  new Date(Date.UTC(date.getUTCFullYear(), date.getUTCMonth(), date.getUTCDate(), date.getUTCHours(), date.getUTCMinutes(), date.getUTCSeconds()));
            // UTC dates
            events.publish(events.topicNames.GPS_DATE_TIME, gpsDate);
        } catch (err) {
            errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "GPS Date (" + err + ")");
        }
        try {
            let gpsSat = json["Satellites in view"];
            if (gpsSat !== undefined) {
                events.publish(events.topicNames.GPS_SAT, gpsSat);
            }
        } catch (err) {
            errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "GPS Satellites data (" + err + ")");
        }
        try {
            let hdg = json["HDG true"].angle;
            events.publish(events.topicNames.TRUE_HDG, hdg);
        } catch (err) {
            errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "heading");
        }

        // Max leeway
        try {
            let maxlwy = json["Max Leeway"];
            if (maxlwy !== undefined) {
                events.publish(events.topicNames.MAX_LEEWAY_ANGLE, maxlwy);
            }
        } catch (err) {
            errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "Max Leeway");
        }

        // Declination "D": {
        //     "angle": 10.0
        //   },
        try {
            if (json.D !== undefined && json.D.angle !== undefined) {
                let decl = json.D.angle;
                if (decl !== undefined) {
                    events.publish(events.topicNames.DECL, decl);
                }
            } else {
                let decl = json["Default Declination"].angle;
                events.publish(events.topicNames.DECL, decl);
            }
        } catch (err) {
            errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "Declination");
        }
        try {
            let twd = json.TWD.angle;
            events.publish(events.topicNames.TWD, twd);
        } catch (err) {
            errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "TWD");
        }
        try {
            let twa = json.TWA.angle;
            events.publish(events.topicNames.TWA, twa);
        } catch (err) {
            errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "TWA");
        }
        try {
            let tws = json.TWS.speed;
            events.publish(events.topicNames.TWS, tws);
        } catch (err) {
            errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "TWS");
        }
        try {
            let waterTemp = json["Water Temperature"].temperature;
            events.publish(events.topicNames.WATER_TEMP, waterTemp);
        } catch (err) {
            errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "water temperature");
        }

        try {
            let airTemp = json["Air Temperature"].temperature;
            events.publish(events.topicNames.AIR_TEMP, airTemp);
        } catch (err) {
            errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "air temperature");
        }
        // Battery_Voltage, Relative_Humidity, Barometric_Pressure
        try {
            let baro = json["Barometric Pressure"].pressure;
            if (baro != 0) {
                events.publish(events.topicNames.PRMSL, baro);
            }
        } catch (err) {
            errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "PRMSL");
        }
        try {
            let hum = json["Relative Humidity"];
            if (hum > 0) {
                events.publish(events.topicNames.REL_HUM, hum);
            }
        } catch (err) {
            errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "Relative_Humidity");
        }
        try {
            let aws = json.AWS.speed;
            events.publish(events.topicNames.AWS, aws);
        } catch (err) {
            errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "AWS");
        }
        try {
            let awa = json.AWA.angle;
            events.publish(events.topicNames.AWA, awa);
        } catch (err) {
            errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "AWA");
        }
        try {
            let cdr = json.CDR.angle;
            events.publish(events.topicNames.CURRENT_DIR, cdr);
        } catch (err) {
            errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "CDR");
        }
        try {
            let cog = json.COG.angle;
            events.publish(events.topicNames.COG, cog);
        } catch (err) {
            errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "COG");
        }
        try {
            let cmg = json.CMG.angle;
            events.publish(events.topicNames.CMG, cmg);
        } catch (err) {
            errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "CMG");
        }
        try {
            let leeway = json.Leeway.angle;
            events.publish(events.topicNames.LEEWAY_ANGLE, leeway);
        } catch (err) {
            errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "Leeway");
        }
        try {
            let csp = json.CSP.speed;
            events.publish(events.topicNames.CURRENT_SPEED, csp);
        } catch (err) {
            errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "CSP");
        }
        // Buffered current
        try {
            let buffered = json['Current calculated with damping'];
            if (buffered !== undefined) {
                let keys = Object.keys(buffered);
                for (let i = 0; i < keys.length; i++) {
                    let k = keys[i];
				    // console.log("K:" + k);
                    let damp = buffered[k];
				    // console.log("Publishing csp-" + k);
                    events.publish(events.topicNames.DAMP_CSP_PREFIX + k, damp.speed.speed);
                    events.publish(events.topicNames.DAMP_CDR_PREFIX + k, damp.direction.angle);
                }
            }
        } catch (err) {
            console.log(err);
        }
        try {
            let sog = json.SOG.speed;
            events.publish(events.topicNames.SOG, sog);
        } catch (err) {
            errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "SOG");
        }
        // to-wp, vmg-wind, vmg-wp, b2wp
        try {
            let to_wp = json["To Waypoint"];
            let b2wp = json["Bearing to WP"].angle;
            let d2wp = json["Distance to WP"].distance;
            let vmg2wp = json["VMG to Waypoint"]
            events.publish(events.topicNames.TO_WP, {
                'to_wp': to_wp,
                'b2wp': b2wp,
                'd2wp': d2wp,
                'vmg2wp': vmg2wp
            });
        } catch (err) {
//		  console.log(err); // Absorb?
        }

        try {
            events.publish(events.topicNames.VMG, {
                'onwind': json["VMG on Wind"],
                'onwp': json["VMG to Waypoint"]
            });
        } catch (err) {
            errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "VMG");
        }
        try {
            events.publish(events.topicNames.LAST_NMEA, {'data': json['NMEA']});
        } catch (err) {
            errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "NMEA");
        }
        if (errMess !== undefined) {
            // console.log(errMess); // Absorb
        }
    } catch (err) {
        console.log(err);
    }
}
