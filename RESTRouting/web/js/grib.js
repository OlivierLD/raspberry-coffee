"use strict";

// TODO Move to ES6

var DEFAULT_TIMEOUT = 60000;

// var errManager = console.log;
var errManager = function(mess) {
	var content = $("#error").html();
	if (content !== undefined) {
		$("#error").html((content.length > 0 ? content + "<br/>" : "") + new Date() + ": " + mess);
		var div = document.getElementById("error");
		div.scrollTop = div.scrollHeight;
	} else {
		console.log(mess);
	}
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

var getGRIB = function(request) {
	var url = "/grib/get-data";
	return getDeferred(url, DEFAULT_TIMEOUT, 'POST', 200, request, false);
};

var requestGRIB = function(gribRequest) {
	var getData = getGRIB(gribRequest);
	getData.done(function(value) {
		var json = JSON.parse(value);
		// Do something smart here.
		console.log("GRIB Data:", json);
		$("#result").html("<pre>" +
				JSON.stringify(json, null, 2) +
				"</pre>");
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
		errManager("Failed to get the GRIB..." + (error !== undefined ? error : ' - ') + ', ' + (message !== undefined ? message : ' - '));
	});
};
