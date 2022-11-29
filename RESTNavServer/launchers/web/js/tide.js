"use strict";

const DEFAULT_TIMEOUT = 60000;

// let errManager = console.log;
let errManager = (mess) => {
    let errDiv = document.getElementById("error");
    let content =  errDiv.innerHTML;
    errDiv.innerHTML = (content.length > 0 ? content + "<br/>" : "") + new Date() + ": " + mess;
    errDiv.scrollTop = errDiv.scrollHeight;
};

// let messManager = console.log;
let messManager = (mess) => {
    let div = document.getElementById("messages");
    let content = div.innerHTML;
    div.innerHTML = (content.length > 0 ? content + "<br/>" : "") + new Date() + ": " + mess;
    div.scrollTop = div.scrollHeight;
};

let getQueryParameterByName = (name, url) => {
    if (!url) {
        url = window.location.href;
    }
    name = name.replace(/[\[\]]/g, "\\$&");   // Nice ;)
    let regex = new RegExp("[?&]" + name + "(=([^&#]*)|&|#|$)"),
        results = regex.exec(url);
    if (!results) {
        return null;
    }
    if (!results[2]) {
        return '';
    }
    return decodeURIComponent(results[2].replace(/\+/g, " "));
};

/* Uses ES6 Promises */
let getPromise = (
    url,                          // full api path
    timeout,                      // After that, fail.
    verb,                         // GET, PUT, DELETE, POST, etc
    happyCodes,                   // An array of int. If met, resolve, otherwise fail.
    data = null) => {        // payload, when needed (PUT, POST...)

    return new Promise((resolve, reject) => {
        let xhr = new XMLHttpRequest();
        let TIMEOUT = timeout;

        // let req = verb + " " + url;
        // if (data !== undefined && data !== null) {
        //     req += ("\n" + JSON.stringify(data, null, 2));
        // }

        xhr.open(verb, url, true);
        xhr.setRequestHeader("Content-type", "application/json"); // TODO If not provided
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
            if (happyCodes.includes(xhr.status)) { // happyCodes is an array
                resolve(xhr.response);
            } else {
                reject({code: xhr.status, message: xhr.response});
            }
        };
    });
}

let getCurrentTime = () => {
    let url = "/astro/utc";
    return getPromise(url, DEFAULT_TIMEOUT, 'GET', [200]);
};

let getTideStations = (offset, limit, filter) => {
    let url = "/tide/tide-stations";
    if (filter !== undefined) {
        url += ('/' + encodeURIComponent(filter)); // Was filter=XXX
    }
    if (!isNaN(parseInt(offset))) {
        url += ("?offset=" + offset);
    }
    if (!isNaN(parseInt(limit))) {
        url += ((url.indexOf("?") > -1 ? "&" : "?") + "limit=" + limit);
    }
    return getPromise(url, DEFAULT_TIMEOUT, 'GET', [200]);
};

let getTideStationsFiltered = (filter) => {
    let url = "/tide/tide-stations/" + encodeURIComponent(filter);
    return getPromise(url, DEFAULT_TIMEOUT, 'GET', [200]);
};

/**
 * POST /astro/sun-between-dates?from=2017-09-01T00:00:00&to=2017-09-02T00:00:01&tz=Europe%2FParis
 *        payload { latitude: 37.76661945, longitude: -122.5166988 }
 * @param from
 * @param to
 * @param tz
 * @param pos
 */
let requestDaylightData = (from, to, tz, pos) => {
    let url = "/astro/sun-between-dates?from=" + from + "&to=" + to + "&tz=" + encodeURIComponent(tz);
    return getPromise(url, DEFAULT_TIMEOUT, 'POST', [200, 201], pos);
};

let requestSunMoonData = (from, to, tz, pos) => {
    let url = "/astro/sun-moon-dec-alt?from=" + from + "&to=" + to + "&tz=" + encodeURIComponent(tz);
    return getPromise(url, DEFAULT_TIMEOUT, 'POST', [200, 201], pos);
};

const DURATION_FMT = "Y-m-dTH:i:s";
/**
 *
 * @param station
 * @param at A json Object like { year: 2017, month: 09, day: 06 }. month 09 is Sep.
 * @param tz
 * @param step
 * @param unit
 * @param withDetails
 * @param nbDays
 */
let getTideTable = (station, at, tz, step, unit, withDetails, nbDays) => {
    if (nbDays === undefined) {
        nbDays = 1;
    }
    let url = "/tide/tide-stations/" + encodeURIComponent(station) + "/wh";
    if (withDetails === true) {
        url += "/details";
    }
    // From and To parameters
    let now = new Date();
    let year = (at !== undefined && at.year !== undefined ? at.year : now.getFullYear());
    let month = (at !== undefined && at.month !== undefined ? at.month - 1 : now.getMonth());
    let day = (at !== undefined && at.day !== undefined ? at.day : now.getDate());
    let from = new Date(year, month, day, 0, 0, 0, 0);
    let to = new Date(from.getTime() + (nbDays * 3600 * 24 * 1000) + 1000); // + (x * 24h) and 1s
    let fromPrm = from.format(DURATION_FMT);
    let toPrm = to.format(DURATION_FMT);
    url += ("?from=" + fromPrm + "&to=" + toPrm);

    let data = null; // Payload
    if (tz.length > 0 || step.length > 0 || unit.length > 0) {
        data = {};
        if (tz.length > 0) {
            data.timezone = tz;
        }
        if (step.length > 0) {
            data.step = parseInt(step);
        }
        if (unit.length > 0) {
            data.unit = unit;
        }
    }
    return getPromise(url, DEFAULT_TIMEOUT, 'POST', [200, 201], data);
};

let getPublishedDoc = (station, options) => {
    let url = "/tide/publish/" + encodeURIComponent(station);
    return getPromise(url, DEFAULT_TIMEOUT, 'POST', [200, 201], options);
};

/**
 * station: tide station name (full)
 * options: { startYear: 2022, startMonth: [0..11], nb: 1..x, quantity: MONTH|YEAR   }
 */
let getPublishedAgendaDoc = (station, options) => {
    let url = "/tide/publish/" + encodeURIComponent(station) + "?agenda=y";
    return getPromise(url, DEFAULT_TIMEOUT, 'POST', [200, 201], options);
};

let getPublishedSunAgendaDoc = (options) => {
    let url = "/tide/publish-sun";
    return getPromise(url, DEFAULT_TIMEOUT, 'POST', [200, 201], options);
};

let getPublishedMoonCal = (station, options) => {
    let url = "/tide/publish/" + encodeURIComponent(station) + "/moon-cal";
    return getPromise(url, DEFAULT_TIMEOUT, 'POST', [200, 201], options);
};

let getSunData = (lat, lng) => {
    let url = "/astro/sun-now";
    let data = {}; // Payload
    data.latitude = lat;
    data.longitude = lng;
    return getPromise(url, DEFAULT_TIMEOUT, 'POST', [200, 201], data);
};

let tideStations = (offset, limit, filter, callback) => {
    let getData = getTideStations(offset, limit, filter);
    getData.then((value) => {
        let json = JSON.parse(value);
        // Do something smart
        messManager("Got " + json.length + " stations.");
        if (callback === undefined) {
            json.forEach((ts, idx) => {
                try {
                    // json[idx] = decodeURI(decodeURIComponent(ts));
                    if ('string' === typeof(ts)) {
                        json[idx] = decodeURI(decodeURIComponent(ts));
                    } else {
                        json[idx].fullName = decodeURI(decodeURIComponent(ts.fullName));
                    }
                } catch (err) {
                    console.log("Oops:" + ts);
                }
            });
            document.getElementById("result").innerHTML = ("<pre>" + JSON.stringify(json, null, 2) + "</pre>");
        } else {
            callback(json);
        }
    }, (error, errMess) => {
        let message;
        if (errMess !== undefined) {
            if (errMess.message !== undefined) {
                message = errMess.message;
            } else {
                message = errMess;
            }
        }
        errManager("Failed to get the station list..." + (error !== undefined ? JSON.stringify(error, null, 2) : ' - ') + ', ' + (message !== undefined ? JSON.stringify(message, null, 2) : ' - '));
    });
};

let tideStationsFiltered = (filter) => {
    let getData = getTideStationsFiltered(filter);
    getData.then((value) => {
        let json = JSON.parse(value);
        // Do something smart
        messManager("Got " + json.length + " station(s)");
        json.forEach((ts /*, idx */) => {
            try {
                ts.fullName = decodeURIComponent(decodeURIComponent(ts.fullName));
                ts.nameParts.forEach((np, i) => {
                    ts.nameParts[i] = decodeURIComponent(decodeURIComponent(np));
                });
            } catch (err) {
                console.log("Oops:" + ts);
            }
        });
        document.getElementById("result").innerHTML = ("<pre>" + JSON.stringify(json, null, 2) + "</pre>");
    }, (error, errMess) => {
        let message;
        if (errMess !== undefined) {
            if (errMess.message !== undefined) {
                message = errMess.message;
            } else {
                message = errMess;
            }
        }
        // errManager("Failed to get the station list..." + (error !== undefined ? error : ' - ') + ', ' + (message !== undefined ? message : ' - '));
        errManager("Failed to get the station list..." + (error !== undefined ? JSON.stringify(error, null, 2) : ' - ') + ', ' + (message !== undefined ? JSON.stringify(message, null, 2) : ' - '));

    });
};

let getDayLightData = (from, to, tz, pos, callback) => {
    let getData = requestDaylightData(from, to, tz, pos);
    getData.then((value) => {
        let json = JSON.parse(value);
        if (callback === undefined) {
            // Do something smart
            messManager("Got " + json);
            document.getElementById("result").innerHTML = ("<pre>" + JSON.stringify(json, null, 2) + "</pre>");
        } else {
            callback(json);
        }
    }, (error, errMess) => {
        let message;
        if (errMess !== undefined) {
            if (errMess.message !== undefined) {
                message = errMess.message;
            } else {
                message = errMess;
            }
        }
        errManager("Failed to get Sun Data..." + (error !== undefined ? JSON.stringify(error, null, 2) : ' - ') + ', ' + (message !== undefined ? JSON.stringify(message, null, 2) : ' - '));
    });
};

let getSunMoonCurves = (from, to, tz, pos, callback) => {
    let getData = requestSunMoonData(from, to, tz, pos);
    getData.then((value) => {
        let json = JSON.parse(value);
        if (callback === undefined) {
            // Do something smart
            messManager("Got " + json);
            document.getElementById("result").innerHTML = ("<pre>" + JSON.stringify(json, null, 2) + "</pre>");
        } else {
            callback(json);
        }
    }, (error, errMess) => {
        let message;
        if (errMess !== undefined) {
            if (errMess.message !== undefined) {
                message = errMess.message;
            } else {
                message = errMess;
            }
        }
        errManager("Failed to get Sun & Moon data..." + (error !== undefined ? JSON.stringify(error, null, 2) : ' - ') + ', ' + (message !== undefined ? JSON.stringify(message, null, 2) : ' - '));
    });
};

let showTime = () => {
    let getData = getCurrentTime();
    getData.then((value) => {
        let json = JSON.parse(value);
        // Do something smart
        document.getElementById("result").innerHTML = ("<pre>" + JSON.stringify(json, null, 2) + "</pre>");
    }, (error, errMess) => {
        let message;
        if (errMess !== undefined) {
            if (errMess.message !== undefined) {
                message = errMess.message;
            } else {
                message = errMess;
            }
        }
        errManager("Failed to get the station list.." + (error !== undefined ? JSON.stringify(error, null, 2) : ' - ') + ', ' + (message !== undefined ? JSON.stringify(message, null, 2) : ' - '));
    });
};

let lastRequiredDate;
let lastRequiredStation;

let tideTable = (station, at, tz, step, unit, withDetails, nbDays, callback) => {
    lastRequiredStation = station;
    lastRequiredDate = at;
    let getData = getTideTable(station, at, tz, step, unit, withDetails, nbDays);
    getData.then((value) => {
        if (callback === undefined) {
            try {
                let json = JSON.parse(value);
                // Do something smart
                json.stationName = decodeURIComponent(decodeURIComponent(json.stationName));
                document.getElementById("result").innerHTML = ("<pre>" + JSON.stringify(json, null, 2) + "</pre>");
            } catch (err) {
                errManager(err + '\nFor\n' + value);
            }
        } else {
            callback(value);
        }
    }, (error, errMess) => {
        let message;
        if (errMess !== undefined) {
            if (errMess.message !== undefined) {
                message = errMess.message;
            } else {
                message = errMess;
            }
        }
        errManager("Failed to get the station data..." + (error !== undefined ? JSON.stringify(error, null, 2) : ' - ') + ', ' + (message !== undefined ? JSON.stringify(message, null, 2) : ' - '));
    });
};

let publishTable = (station, options, callback) => {
    let getData = getPublishedDoc(station, options);
    getData.then((value) => {
        if (callback === undefined) {
            try {
                // Do something smart
                document.getElementById("result").innerHTML = ("<pre>" + value + "</pre>");
            } catch (err) {
                errManager(err + '\nFor\n' + value);
            }
        } else {
            callback(value);
        }
    }, (error, errMess) => {
        let message;
        if (errMess !== undefined) {
            if (errMess.message !== undefined) {
                message = errMess.message;
            } else {
                message = errMess;
            }
        }
        errManager("Failed publish station data..." + (error !== undefined ? error : ' - ') + ', ' + (message !== undefined
            ? message : ' - '));
    });
};

let publishAgenda = (station, options, callback) => {
    let getData = getPublishedAgendaDoc(station, options);
    getData.then((value) => {
        if (callback === undefined) {
            try {
                // Do something smart
                document.getElementById("result").innerHTML = ("<pre>" + value + "</pre>");
            } catch (err) {
                errManager(err + '\nFor\n' + value);
            }
        } else {
            callback(value);
        }
    }, (error, errMess) => {
        let message;
        if (errMess !== undefined) {
            if (errMess.message !== undefined) {
                message = errMess.message;
            } else {
                message = errMess;
            }
        }
        errManager(`Failed publish station data...${(error !== undefined ? error : ' - ') + ', ' + (message !== undefined
            ? message : ' - ')}`);
    });
};

let publishSunAgenda = (options, callback) => {
    let getData = getPublishedSunAgendaDoc(options);
    getData.then((value) => {
        if (callback === undefined) {
            try {
                // Do something smart
                document.getElementById("result").innerHTML = ("<pre>" + value + "</pre>");
            } catch (err) {
                errManager(err + '\nFor\n' + value);
            }
        } else {
            callback(value);
        }
    }, (error, errMess) => {
        let message;
        if (errMess !== undefined) {
            if (errMess.message !== undefined) {
                message = errMess.message;
            } else {
                message = errMess;
            }
        }
        errManager(`Failed publish station data...${(error !== undefined ? error : ' - ') + ', ' + (message !== undefined
            ? message : ' - ')}`);
    });
};

let publishMoonCal = (station, options, callback) => {
    let getData = getPublishedMoonCal(station, options);
    getData.then((value) => {
        if (callback === undefined) {
            try {
                // Do something smart
                document.getElementById("result").innerHTML = ("<pre>" + value + "</pre>");
            } catch (err) {
                errManager(err + '\nFor\n' + value);
            }
        } else {
            callback(value);
        }
    }, (error, errMess) => {
        let message;
        if (errMess !== undefined) {
            if (errMess.message !== undefined) {
                message = errMess.message;
            } else {
                message = errMess;
            }
        }
        errManager("Failed publish station data..." + (error !== undefined ? error : ' - ') + ', ' + (message !== undefined
            ? message : ' - '));
    });
};

let sunData = (from, to, tz, position, callback) => {
    let getData = getDayLightData(from, to, tz, position, callback); // getSunData(lat, lng);
    getData.then((value) => {
        if (callback === undefined) {
            try {
                let json = JSON.parse(value);
                // Do something smart
                let strLat  = decToSex(json.lat,  "NS");
                let strLng  = decToSex(json.lng,  "EW");
                let strDecl = decToSex(json.decl, "NS");
                let strGHA  = decToSex(json.gha);

                document.getElementById("result").innerHTML = ("<pre>" +
                    JSON.stringify(json, null, 2) +
                    "<br/>" +
                    (strLat + " / " + strLng) +
                    "<br/>" +
                    new Date(json.epoch) +
                    "<br/>" +
                    ("Dec: " + strDecl) +
                    "<br/>" +
                    ("GHA: " + strGHA) +
                    "<br/>" +
                    ("Meridian Pass. Time: " + hoursDecimalToHMS(json.eot) + " UTC") +
                    "<br/>" +
                    ("Rise: " + hoursDecimalToHMS(json.riseTime) + " UTC") +
                    "<br/>" +
                    ("Set: " + hoursDecimalToHMS(json.setTime) + " UTC") +
                    "</pre>");
            } catch (err) {
                errManager(err + '\nFor\n' + value);
            }
        } else {
            callback(value);
        }
    }, (error, errMess) => {
        let message;
        if (errMess !== undefined) {
            if (errMess.message !== undefined) {
                message = errMess.message;
            } else {
                message = errMess;
            }
        }
        errManager("Failed to get the station data..." + (error !== undefined ? JSON.stringify(error, null, 2) : ' - ') + ', ' + (message !== undefined ? JSON.stringify(message, null, 2) : ' - '));
    });
};
