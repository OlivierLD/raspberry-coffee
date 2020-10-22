"use strict";

// TODO Move to ES6

const DEFAULT_TIMEOUT = 60000;

// var errManager = console.log;
let errManager = (mess) => {
    let content = $("#error").html();
    $("#error").html((content.length > 0 ? content + "<br/>" : "") + new Date() + ": " + mess);
    let div = document.getElementById("error");
    div.scrollTop = div.scrollHeight;
};

// var messManager = console.log;
let messManager = (mess) => {
    let content = $("#messages").html();
    $("#messages").html((content.length > 0 ? content + "<br/>" : "") + new Date() + ": " + mess);
    let div = document.getElementById("messages");
    div.scrollTop = div.scrollHeight;
};

let getQueryParameterByName = (name, url) => {
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
};

let getDeferred = (
    url,                          // full api path
    timeout,                      // After that, fail.
    verb,                         // GET, PUT, DELETE, POST, etc
    happyCode,                    // if met, resolve, otherwise fail.
    data,                         // payload, when needed (PUT, POST...)
    show) => {                    // Show the traffic [true]|false
    if (show === undefined) {
        show = true;
    }
    if (show === true) {
        document.body.style.cursor = 'wait';
    }
    let deferred = $.Deferred(),  // a jQuery deferred
//			url = url,
        xhr = new XMLHttpRequest(),
        TIMEOUT = timeout;

    let req = verb + " " + url;
    if (data !== undefined && data !== null) {
        req += ("\n" + JSON.stringify(data, null, 2));
    }

    xhr.open(verb, url, true);
    xhr.setRequestHeader("Content-type", "application/json");
    if (data === undefined || data === null) {
        xhr.send();
    } else {
        try {
            console.debug(`URL: ${url}, verb ${verb}, Payload ${JSON.stringify(data)}`);
            xhr.send(JSON.stringify(data));
        } catch (err) {
            console.debug(`URL: ${url}, verb ${verb}, Payload ${JSON.stringify(data)}, error: ${JSON.stringify(error)}`);
        }
    }

    let requestTimer = setTimeout(() => {
        xhr.abort();
        let mess = {message: 'Timeout'};
        deferred.reject(408, mess);
    }, TIMEOUT);

    xhr.onload = () => {
        clearTimeout(requestTimer);
        if (xhr.status === happyCode) {
            deferred.resolve(xhr.response);
        } else {
            console.debug(`Rejecting ${url}, code ${xhr.status}`);
            deferred.reject(xhr.status, xhr.response);
        }
    };
    return deferred.promise();
};

let getCurrentTime = () => {
    let url = "/astro/utc";
    return getDeferred(url, DEFAULT_TIMEOUT, 'GET', 200, null, false);
};

let getTideStations = (offset, limit, filter) => {
    var url = "/tide/tide-stations";
    if (filter !== undefined) {
        url += ('/' + encodeURIComponent(filter)); // Was filter=XXX
    }
    if (!isNaN(parseInt(offset))) {
        url += ("?offset=" + offset);
    }
    if (!isNaN(parseInt(limit))) {
        url += ((url.indexOf("?") > -1 ? "&" : "?") + "limit=" + limit);
    }
    return getDeferred(url, DEFAULT_TIMEOUT, 'GET', 200, null, false);
};

let getTideStationsFiltered = (filter) => {
    let url = "/tide/tide-stations/" + encodeURIComponent(filter);
    return getDeferred(url, DEFAULT_TIMEOUT, 'GET', 200, null, false);
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
    return getDeferred(url, DEFAULT_TIMEOUT, 'POST', 200, pos, false);
};

let requestSunMoonData = (from, to, tz, pos) => {
    let url = "/astro/sun-moon-dec-alt?from=" + from + "&to=" + to + "&tz=" + encodeURIComponent(tz);
    return getDeferred(url, DEFAULT_TIMEOUT, 'POST', 200, pos, false);
};

let DURATION_FMT = "Y-m-dTH:i:s";
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
    return getDeferred(url, DEFAULT_TIMEOUT, 'POST', 200, data, false);
};

let getPublishedDoc = (station, options) => {
    let url = "/tide/publish/" + encodeURIComponent(station);
    return getDeferred(url, DEFAULT_TIMEOUT, 'POST', 200, options, false);
};

let getPublishedAgendaDoc = (station, options) => {
    let url = "/tide/publish/" + encodeURIComponent(station) + "?agenda=y";
    return getDeferred(url, DEFAULT_TIMEOUT, 'POST', 200, options, false);
};

let getPublishedMoonCal = (station, options) => {
    let url = "/tide/publish/" + encodeURIComponent(station) + "/moon-cal";
    return getDeferred(url, DEFAULT_TIMEOUT, 'POST', 200, options, false);
};

let getSunData = (lat, lng) => {
    let url = "/astro/sun-now";
    let data = {}; // Payload
    data.latitude = lat;
    data.longitude = lng;
    return getDeferred(url, DEFAULT_TIMEOUT, 'POST', 200, data, false);
};

let tideStations = (offset, limit, filter, callback) => {
    let getData = getTideStations(offset, limit, filter);
    getData.done((value) => {
        let json = JSON.parse(value);
        // Do something smart
        messManager("Got " + json.length + " stations.");
        if (callback === undefined) {
            json.forEach((ts, idx) => {
                try {
                    json[idx] = decodeURI(decodeURIComponent(ts));
                } catch (err) {
                    console.log("Oops:" + ts);
                }
            });
            $("#result").html("<pre>" + JSON.stringify(json, null, 2) + "</pre>");
        } else {
            callback(json);
        }
    });
    getData.fail((error, errmess) => {
        let message;
        if (errmess !== undefined) {
            if (errmess.message !== undefined) {
                message = errmess.message;
            } else {
                message = errmess;
            }
        }
        errManager("Failed to get the station list..." + (error !== undefined ? error : ' - ') + ', ' + (message !== undefined ? message : ' - '));
    });
};

let tideStationsFiltered = (filter) => {
    let getData = getTideStationsFiltered(filter);
    getData.done((value) => {
        let json = JSON.parse(value);
        // Do something smart
        messManager("Got " + json.length + " station(s)");
        json.forEach((ts, idx) => {
            try {
                ts.fullName = decodeURIComponent(decodeURIComponent(ts.fullName));
                ts.nameParts.forEach((np, i) => {
                    ts.nameParts[i] = decodeURIComponent(decodeURIComponent(np));
                });
            } catch (err) {
                console.log("Oops:" + ts);
            }
        });
        $("#result").html("<pre>" + JSON.stringify(json, null, 2) + "</pre>");
    });
    getData.fail((error, errmess) => {
        let message;
        if (errmess !== undefined) {
            if (errmess.message !== undefined) {
                message = errmess.message;
            } else {
                message = errmess;
            }
        }
        errManager("Failed to get the station list..." + (error !== undefined ? error : ' - ') + ', ' + (message !== undefined ? message : ' - '));
    });
};

let getDayLightData = (from, to, tz, pos, callback) => {
    let getData = requestDaylightData(from, to, tz, pos);
    getData.done((value) => {
        let json = JSON.parse(value);
        if (callback === undefined) {
            // Do something smart
            messManager("Got " + json);
            $("#result").html("<pre>" + JSON.stringify(json, null, 2) + "</pre>");
        } else {
            callback(json);
        }
    });
    getData.fail((error, errmess) => {
        let message;
        if (errmess !== undefined) {
            if (errmess.message !== undefined) {
                message = errmess.message;
            } else {
                message = errmess;
            }
        }
        errManager("Failed to get Sun data..." + (error !== undefined ? error : ' - ') + ', ' + (message !== undefined ? message : ' - '));
    });
};

let getSunMoonCurves = (from, to, tz, pos, callback) => {
    let getData = requestSunMoonData(from, to, tz, pos);
    getData.done((value) => {
        let json = JSON.parse(value);
        if (callback === undefined) {
            // Do something smart
            messManager("Got " + json);
            $("#result").html("<pre>" + JSON.stringify(json, null, 2) + "</pre>");
        } else {
            callback(json);
        }
    });
    getData.fail((error, errmess) => {
        let message;
        if (errmess !== undefined) {
            if (errmess.message !== undefined) {
                message = errmess.message;
            } else {
                message = errmess;
            }
        }
        errManager("Failed to get Sun & Moon data..." + (error !== undefined ? error : ' - ') + ', ' + (message !== undefined ? message : ' - '));
    });
};

let showTime = () => {
    let getData = getCurrentTime();
    getData.done((value) => {
        var json = JSON.parse(value);
        // Do something smart
        $("#result").html("<pre>" + JSON.stringify(json, null, 2) + "</pre>");
    });
    getData.fail((error, errmess) => {
        let message;
        if (errmess !== undefined) {
            if (errmess.message !== undefined) {
                message = errmess.message;
            } else {
                message = errmess;
            }
        }
        errManager("Failed to get the station list..." + (error !== undefined ? error : ' - ') + ', ' + (message !== undefined ? message : ' - '));
    });
};

let lastRequiredDate;
let lastRequiredStation;

let tideTable = (station, at, tz, step, unit, withDetails, nbDays, callback) => {
    lastRequiredStation = station;
    lastRequiredDate = at;
    let getData = getTideTable(station, at, tz, step, unit, withDetails, nbDays);
    getData.done((value) => {
        if (callback === undefined) {
            try {
                let json = JSON.parse(value);
                // Do something smart
                json.stationName = decodeURIComponent(decodeURIComponent(json.stationName));
                $("#result").html("<pre>" + JSON.stringify(json, null, 2) + "</pre>");
            } catch (err) {
                errManager(err + '\nFor\n' + value);
            }
        } else {
            callback(value);
        }
    });
    getData.fail((error, errmess) => {
        let message;
        if (errmess !== undefined) {
            if (errmess.message !== undefined) {
                message = errmess.message;
            } else {
                message = errmess;
            }
        }
        errManager("Failed to get the station data..." + (error !== undefined ? error : ' - ') + ', ' + (message !== undefined ? message : ' - '));
    });
};

let publishTable = (station, options, callback) => {
    let getData = getPublishedDoc(station, options);
    getData.done((value) => {
        if (callback === undefined) {
            try {
                // Do something smart
                $("#result").html("<pre>" + value + "</pre>");
            } catch (err) {
                errManager(err + '\nFor\n' + value);
            }
        } else {
            callback(value);
        }
    });
    getData.fail((error, errmess) => {
        let message;
        if (errmess !== undefined) {
            if (errmess.message !== undefined) {
                message = errmess.message;
            } else {
                message = errmess;
            }
        }
        errManager("Failed publish station data..." + (error !== undefined ? error : ' - ') + ', ' + (message !== undefined
            ? message : ' - '));
    });
};

let publishAgenda = (station, options, callback) => {
    let getData = getPublishedAgendaDoc(station, options);
    getData.done((value) => {
        if (callback === undefined) {
            try {
                // Do something smart
                $("#result").html("<pre>" + value + "</pre>");
            } catch (err) {
                errManager(err + '\nFor\n' + value);
            }
        } else {
            callback(value);
        }
    });
    getData.fail((error, errmess) => {
        let message;
        if (errmess !== undefined) {
            if (errmess.message !== undefined) {
                message = errmess.message;
            } else {
                message = errmess;
            }
        }
        errManager(`Failed publish station data...${(error !== undefined ? error : ' - ') + ', ' + (message !== undefined
            ? message : ' - ')}`);
    });
};

let publishMoonCal = (station, options, callback) => {
    let getData = getPublishedMoonCal(station, options);
    getData.done((value) => {
        if (callback === undefined) {
            try {
                // Do something smart
                $("#result").html("<pre>" + value + "</pre>");
            } catch (err) {
                errManager(err + '\nFor\n' + value);
            }
        } else {
            callback(value);
        }
    });
    getData.fail((error, errmess) => {
        let message;
        if (errmess !== undefined) {
            if (errmess.message !== undefined) {
                message = errmess.message;
            } else {
                message = errmess;
            }
        }
        errManager("Failed publish station data..." + (error !== undefined ? error : ' - ') + ', ' + (message !== undefined
            ? message : ' - '));
    });
};

let sunData = (from, to, tz, position, callback) => {
    let getData = getDayLightData(from, to, tz, position, callback); // getSunData(lat, lng);
    getData.done((value) => {
        if (callback === undefined) {
            try {
                let json = JSON.parse(value);
                // Do something smart
                let strLat = decToSex(json.lat, "NS");
                let strLng = decToSex(json.lng, "EW");
                let strDecl = decToSex(json.decl, "NS");
                let strGHA = decToSex(json.gha);

                $("#result").html("<pre>" +
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
    });
    getData.fail((error, errmess) => {
        let message;
        if (errmess !== undefined) {
            if (errmess.message !== undefined) {
                message = errmess.message;
            } else {
                message = errmess;
            }
        }
        errManager("Failed to get the station data..." + (error !== undefined ? error : ' - ') + ', ' + (message !== undefined ? message : ' - '));
    });
};
