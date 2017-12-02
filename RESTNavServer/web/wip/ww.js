/*
 * @author Olivier Le Diouris
 *
 * Shows how to use the WorldMap object
 * and the REST APIs of the img service.
 * Displays faxes.
 */
"use strict";

var worldMap;
var currentDate;

var errManager = function(mess) {
	console.log(mess);
};

var DEFAULT_TIMEOUT = 60000;

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

var init = function () {
	worldMap = new WorldMap('mapCanvas', 'MERCATOR');

	worldMap.setNorth(75);
	worldMap.setSouth(-75);
	worldMap.setWest(-179);
	worldMap.setEast(179); // Recalculated, anyway.

	worldMap.setUserPosition({ latitude: position.lat, longitude: position.lng });

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

var requestComposite = function(requestPayload) {
	var url = "/img/download-and-transform";
	return getDeferred(url, DEFAULT_TIMEOUT, 'POST', 200, requestPayload, false);
};

var getCompositeData = function(options, compositeData, callback) {
	var getData = requestComposite(options);
	getData.done(function(value) {
		if (callback === undefined) {
			try {
				// Do something smart
				console.log(value);
			} catch (err) {
				errManager(err + '\nFor\n' + value);
			}
		} else {
			callback(value, compositeData);
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
		errManager("Failed to get composite data data..." + (error !== undefined ? error : ' - ') + ', ' + (message !== undefined
				? message : ' - '));
	});
};

var gribData = {};

// Callback for GRIBs
var after = function(canvas, context) {
	console.log("Now drawing GRIB");
	if (gribData !== {}) {  // TODO Identify date and type
		var date, type;       // TODO Remove that...
		drawGrib(canvas, context, gribData, date, type);
	}
};

