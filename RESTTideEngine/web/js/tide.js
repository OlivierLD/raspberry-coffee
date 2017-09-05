"use strict";

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
	xhr.setRequestHeader("Content-type", "application/json");
	if (data === undefined) {
		xhr.send();
	} else {
		xhr.send(JSON.stringify(data));
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

var DEFAULT_TIMEOUT = 10000;

var getCurrentTime = function() {
	var url = "/utc";
	return getDeferred(url, DEFAULT_TIMEOUT, 'GET', 200, null, false);
};

var getTideStations = function(offset, limit, filter) {
	var url = "/tide-stations";
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
	var url = "/tide-stations/" + encodeURIComponent(filter);
	return getDeferred(url, DEFAULT_TIMEOUT, 'GET', 200, null, false);
};

var DURATION_FMT = "Y-m-dTH:i:s";
var getTideTable = function(station, tz, step, unit, withDetails) {
	var url = "/tide-stations/" + encodeURIComponent(station) + "/wh";
	if (withDetails === true) {
		url += "/details";
	}
	// From and To parameters
	var now = new Date();
	var from = new Date(now.getFullYear(), now.getMonth(), now.getDate(), 0, 0, 0, 0);
	var to = new Date(from.getTime() + (3600 * 24 * 1000) + 1000); // + 24h and 1s
	var fromPrm = (new Date(from)).format(DURATION_FMT);
	var toPrm = (new Date(to)).format(DURATION_FMT);
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

var getSunData = function(lat, lng) {
	var url = "/sun-now";
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

var tideTable = function(station, tz, step, unit, withDetails, callback) {
	var getData = getTideTable(station, tz, step, unit, withDetails);
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
