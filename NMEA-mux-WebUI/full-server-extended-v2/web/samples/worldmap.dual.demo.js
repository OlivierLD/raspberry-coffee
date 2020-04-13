/*
 * @author Olivier Le Diouris
 *
 * Shows how to use the WorldMap object
 * and the REST APIs of the Astro service.
 * Displays a GLOBE
 */
"use strict";

var worldMap;
var currentDate;

var errManager = function(mess) {
	console.log(mess);
};

var init = function () {
	worldMap = new WorldMap('mapCanvas', 'GLOBE');

	// For Mercator
	// worldMap.setNorth(75);
	// worldMap.setSouth(-75);
	// worldMap.setWest(127.5);
};

var DEFAULT_TIMEOUT = 60000;

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

var getSkyGP = function(when) {
	var url = "/astro/positions-in-the-sky";
	// Add date
	url += ("?at=" + when);
	url += ("&fromL=" + position.lat);
	url += ("&fromG=" + position.lng);
	// Wandering bodies
	if ($("#WWB")[0].checked) { // to minimize the size of the payload
		url += ("&wandering=true");
	}
	// Stars
	if ($("#WS")[0].checked) { // to minimize the size of the payload
		url += ("&stars=true");
	}
	return getDeferred(url, DEFAULT_TIMEOUT, 'GET', 200, null, false);
};

var getAstroData = function(when, callback) {
	var getData = getSkyGP(when);
	getData.done(function(value) {
		var json = JSON.parse(value);
		if (callback !== undefined) {
			callback(json);
		} else {
			console.log(JSON.stringify(json, null, 2));
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
		errManager("Failed to get the Astro Data..." + (error !== undefined ? error : ' - ') + ', ' + (message !== undefined ? message : ' - '));
	});
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

/*
 *  Demo features
 */

var position = {
	lat: 37.7489,
	lng: -122.5070
};

const MINUTE = 60000; // in ms.

var getCurrentUTCDate = function() {
	var date = new Date();
	var offset = date.getTimezoneOffset() * MINUTE; // in millisecs

	return new Date().getTime() + offset; // - (6 * 3600 * 1000);
};

var initAjax = function () {

	currentDate = getCurrentUTCDate();
	console.log("Starting (now) at " + new Date(currentDate).format("Y-M-d H:i:s UTC"));

	var interval = setInterval(function () {
		tickClock();
	}, 100);

	var intervalGPS = setInterval(function () {
		tickGPS();
	}, 1000) // 1 sec;

};

var tickClock = function () {

	var moveFast = true, erratic = false;

	var mf = getQueryParameterByName("move-fast");
	moveFast = (mf === "true");

	if (moveFast) {
		// Changed position
		position.lng += 1;
		if (position.lng > 360) position.lng -= 360;
		if (position.lng > 180) position.lng -= 360;

		if (erratic) {
			var plus = (Math.random() > 0.5);
			position.lat += (Math.random() * (plus ? 1 : -1));
			if (position.lat > 90) position.lat = 180 - position.lat;
			if (position.lat < -90) position.lat = -180 + position.lat;
		}
	}
	var json = {
		Position: {
			lat: position.lat,
			lng: position.lng
		}
	};
	onMessage(json); // Position
};

var tickGPS = function () {

	var moveFast = true;
	var mf = getQueryParameterByName("move-fast");
	moveFast = (mf === "true");

	var json = {
		GPS: new Date(currentDate)
	};
	onMessage(json); // Date

	if (moveFast) {
		currentDate += (1 * MINUTE);
	} else {
		currentDate = getCurrentUTCDate();
	}

	var mess = "Time is now " + new Date(currentDate).format("Y-M-d H:i:s UTC");
	var dateField = document.getElementById("current-date");
	if (dateField !== undefined) {
		dateField.innerText = mess;
	} else {
		console.log(mess);
	}
};

var onMessage = function (json) {
	try {
		var errMess = "";

		try {
			if (json.Position !== undefined) {
				var latitude = json.Position.lat;
	//    console.log("latitude:" + latitude)
				var longitude = json.Position.lng;
	//    console.log("Pt:" + latitude + ", " + longitude);
				events.publish('pos', {
					'lat': latitude,
					'lng': longitude
				});
			}
		} catch (err) {
			errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "position");
		}
		try {
		  if (json.GPS !== undefined) {
				var gpsDate = json.GPS;
				events.publish('gps-time', gpsDate);
			}
		} catch (err) {
			errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "GPS Date (" + err + ")");
		}
		if (errMess !== undefined)
			displayErr(errMess);
	}
	catch (err) {
		displayErr(err);
	}
};
