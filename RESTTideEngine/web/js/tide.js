"use strict";

var DEFAULT_TIMEOUT = 60000;

// var errManager = console.log;
var errManager = function(mess) {
	var content = $("#error").html();
	$("#error").html((content.length > 0 ? content + "<br/>" : "") + new Date() + ": " + mess);
	var div = document.getElementById("error");
	div.scrollTop = div.scrollHeight;
};

// var messManager = console.log;
var messManager = function(mess) {
	var content = $("#messages").html();
	$("#messages").html((content.length > 0 ? content + "<br/>" : "") + new Date() + ": " + mess);
	var div = document.getElementById("messages");
	div.scrollTop = div.scrollHeight;
};

var getQueryParameterByName = function(name, url) {
	if (!url) url = window.location.href;
	name = name.replace(/[\[\]]/g, "\\$&");
	var regex = new RegExp("[?&]" + name + "(=([^&#]*)|&|#|$)"),
			results = regex.exec(url);
	if (!results) return null;
	if (!results[2]) return '';
	return decodeURIComponent(results[2].replace(/\+/g, " "));
};

var getDeferred = function(
		url,                          // full api path
		timeout,                      // After that, fail.
		verb,                         // GET, PUT, DELETE, POST, etc
		happyCode,                    // if met, resolve, otherwise fail.
		data,                         // payload, when needed (PUT, POST...)
		show) {                       // Show the traffic [true]|false
	if (show === undefined) {
		show = true;
	}
	if (show === true) {
		document.body.style.cursor = 'wait';
	}
	var deferred = $.Deferred(),  // a jQuery deferred
			url = url,
			xhr = new XMLHttpRequest(),
			TIMEOUT = timeout;

	var req = verb + " " + url;
	if (data !== undefined && data !== null) {
		req += ("\n" + JSON.stringify(data, null, 2));
	}

	xhr.open(verb, url, true);
	xhr.setRequestHeader("Content-type", "application/json"); // I know, hard-coded.
	// if (data === undefined || data === null) {
	if (data) {
		xhr.send(JSON.stringify(data));
	} else {
		xhr.send();
	}

	var requestTimer = setTimeout(function() {
		xhr.abort();
		var mess = { message: 'Timeout' };
		deferred.reject(408, mess);
	}, TIMEOUT);

	xhr.onload = function() {
		clearTimeout(requestTimer);
		if (xhr.status === happyCode) {
			deferred.resolve(xhr.response);
		} else {
			deferred.reject(xhr.status, xhr.response);
		}
	};
	return deferred.promise();
};

var getCurrentTime = function() {
	var url = "/astro/utc";
	return getDeferred(url, DEFAULT_TIMEOUT, 'GET', 200, null, false);
};

var getTideStations = function(offset, limit, filter) {
	var url = "/tide/tide-stations";
	if (! isNaN(parseInt(offset))) {
		url += ("?offset=" + offset);
	}
	if (! isNaN(parseInt(limit))) {
		url += ((url.indexOf("?") > -1 ? "&" : "?") + "limit=" + limit);
	}
	if (filter !== undefined) {
		url += ((url.indexOf("?") > -1 ? "&" : "?") + "filter=" + encodeURIComponent(filter));
	}
	return getDeferred(url, DEFAULT_TIMEOUT, 'GET', 200, null, false);
};

var getTideStationsFiltered = function(filter) {
	var url = "/tide/tide-stations/" + encodeURIComponent(filter);
	return getDeferred(url, DEFAULT_TIMEOUT, 'GET', 200, null, false);
};

/**
 * POST /astro/sun-between-dates?from=2017-09-01T00:00:00&to=2017-09-02T00:00:01&tz=Europe%2FParis
 * 		payload { latitude: 37.76661945, longitude: -122.5166988 }
 * @param from
 * @param to
 * @param tz
 * @param pos
 */
var requestDaylightData = function(from, to, tz, pos) {
	var url = "/astro/sun-between-dates?from=" + from + "&to=" + to + "&tz=" + encodeURIComponent(tz);
	return getDeferred(url, DEFAULT_TIMEOUT, 'POST', 200, pos, false);
};

var requestSunMoontData= function(from, to, tz, pos) {
	var url = "/astro/sun-moon-dec-alt?from=" + from + "&to=" + to + "&tz=" + encodeURIComponent(tz);
	return getDeferred(url, DEFAULT_TIMEOUT, 'POST', 200, pos, false);
};

var DURATION_FMT = "Y-m-dTH:i:s";

// Also encodes parenthesis and other stuff
function fixedEncodeURIComponent(str) {
  return encodeURIComponent(str).replace(/[!'()*]/g, function(c) {
    return '%' + c.charCodeAt(0).toString(16);
  });
}

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
var getTideTable = function(station, at, tz, step, unit, withDetails, nbDays) {
	if (nbDays === undefined) {
		nbDays = 1;
	}
	var url = "/tide/tide-stations/" + fixedEncodeURIComponent(station) + "/wh";
	if (withDetails === true) {
		url += "/details";
	}
	// From and To parameters
	var now = new Date();
	var year = (at !== undefined && at.year !== undefined ? at.year : now.getFullYear());
	var month = (at !== undefined && at.month !== undefined ? at.month - 1 : now.getMonth());
	var day = (at !== undefined && at.day !== undefined ? at.day : now.getDate());
	var from = new Date(year, month, day, 0, 0, 0, 0);
	var to = new Date(from.getTime() + (nbDays * 3600 * 24 * 1000) + 1000); // + (x * 24h) and 1s
	var fromPrm = from.format(DURATION_FMT);
	var toPrm = to.format(DURATION_FMT);
	url += ("?from=" + fromPrm + "&to=" + toPrm);

	var data = null; // Payload
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

var getPublishedDoc = function(station, options) {
	var url = "/publish/" + encodeURIComponent(station);
	return getDeferred(url, DEFAULT_TIMEOUT, 'POST', 200, options, false);
};

var getSunData = function(lat, lng) {
	var url = "/astro/sun-now";
	var data = {}; // Payload
	data.latitude = lat;
	data.longitude = lng;
	return getDeferred(url, DEFAULT_TIMEOUT, 'POST', 200, data, false);
};

var tideStations = function(offset, limit, filter, callback) {
	var getData = getTideStations(offset, limit, filter);
	getData.done(function(value) {
		var json = JSON.parse(value);
		// Do something smart
		messManager("Got " + json.length + " stations.");
		if (callback === undefined) {
			json.forEach(function (ts, idx) {
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
	getData.fail(function(error, errmess) {
		var message;
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

var tideStationsFiltered = function(filter) {
	var getData = getTideStationsFiltered(filter);
	getData.done(function(value) {
		var json = JSON.parse(value);
		// Do something smart
		messManager("Got " + json.length + " station(s)");
		json.forEach(function(ts, idx) {
			try {
				ts.fullName = decodeURIComponent(decodeURIComponent(ts.fullName));
				ts.nameParts.forEach(function(np, i) {
					ts.nameParts[i] = decodeURIComponent(decodeURIComponent(np));
				});
			} catch (err) {
				console.log("Oops:" + ts);
			}
		});
		$("#result").html("<pre>" + JSON.stringify(json, null, 2) + "</pre>");
	});
	getData.fail(function(error, errmess) {
		var message;
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

var getSunData = function(from, to, tz, pos, callback) {
	var getData = requestDaylightData(from, to, tz, pos);
	getData.done(function(value) {
		var json = JSON.parse(value);
		if (callback === undefined) {
			// Do something smart
			messManager("Got " + json);
			$("#result").html("<pre>" + JSON.stringify(json, null, 2) + "</pre>");
		} else {
			callback(json);
		}
	});
	getData.fail(function(error, errmess) {
		var message;
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

var getSunMoonCurves = function(from, to, tz, pos, callback) {
	var getData = requestSunMoontData(from, to, tz, pos);
	getData.done(function(value) {
		var json = JSON.parse(value);
		if (callback === undefined) {
			// Do something smart
			messManager("Got " + json);
			$("#result").html("<pre>" + JSON.stringify(json, null, 2) + "</pre>");
		} else {
			callback(json);
		}
	});
	getData.fail(function(error, errmess) {
		var message;
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

var showTime = function() {
	var getData = getCurrentTime();
	getData.done(function(value) {
		var json = JSON.parse(value);
		// Do something smart
		$("#result").html("<pre>" + JSON.stringify(json, null, 2) + "</pre>");
	});
	getData.fail(function(error, errmess) {
		var message;
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

var tideTable = function(station, at, tz, step, unit, withDetails, nbDays, callback) {
	var getData = getTideTable(station, at, tz, step, unit, withDetails, nbDays);
	getData.done(function(value) {
		if (callback === undefined) {
			try {
				var json = JSON.parse(value);
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
	getData.fail(function(error, errmess) {
		var message;
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

var publishTable = function(station, options, callback) {
	var getData = getPublishedDoc(station, options);
	getData.done(function(value) {
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
	getData.fail(function(error, errmess) {
		var message;
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

var sunData = function(lat, lng, callback) {
	var getData = getSunData(lat, lng);
	getData.done(function(value) {
		if (callback === undefined) {
			try {
				var json = JSON.parse(value);
				// Do something smart
				var strLat = decToSex(json.lat, "NS");
				var strLng = decToSex(json.lng, "EW");
				var strDecl = decToSex(json.decl, "NS");
				var strGHA = decToSex(json.gha);

				$("#result").html("<pre>" +
						JSON.stringify(json, null, 2) +
						"<br/>" +
						( strLat + " / " + strLng) +
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
	getData.fail(function(error, errmess) {
		var message;
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
