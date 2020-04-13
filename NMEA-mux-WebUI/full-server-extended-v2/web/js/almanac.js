"use strict";
// TODO Move to ES6

var DEFAULT_TIMEOUT = 60000;

// var errManager = console.log;
var errManager = function(mess) {
	var content = $("#error").html();
	$("#error").html((content.length > 0 ? content + "<br/>" : "") + new Date() + ": " + mess);
	try {
		flipTab('error-tab');
	} catch (err) {
		console.log(err);
	}
	var div = document.getElementById("error");
	div.scrollTop = div.scrollHeight;
};

// var messManager = console.log;
var messManager = function(mess) {
	var content = $("#messages").html();
	$("#messages").html((content.length > 0 ? content + "<br/>" : "") + new Date() + ": " + mess);
	try {
		flipTab('message-tab');
	} catch (err) {
		console.log(err);
	}
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
	xhr.setRequestHeader("Content-type", "application/json");
	if (data === undefined) {
		xhr.send();
	} else {
		xhr.send(JSON.stringify(data));
	}

	if (TIMEOUT > 0) {
		// requestTimer will be used to clear the timeout in case the request comes back in time.
		var requestTimer = setTimeout(function () {
			xhr.abort();
			var mess = {message: 'Timeout'};
			deferred.reject(408, mess);
		}, TIMEOUT);
	}

	xhr.onload = function() {
		if (requestTimer !== undefined) {
			clearTimeout(requestTimer);
		}
		if (xhr.status === happyCode) {
			deferred.resolve(xhr.response);
		} else {
			deferred.reject(xhr.status, xhr.response);
		}
	};
	return deferred.promise();
};

var DURATION_FMT = "Y-m-dTH:i:s";

var getPerpetualDoc = function(options) {
	var url = "/astro/publish/perpetual";
	return getDeferred(url, DEFAULT_TIMEOUT, 'POST', 200, options, false);
};

var getAlmanac = function(options) {
	var url = "/astro/publish/almanac";
//return getDeferred(url, DEFAULT_TIMEOUT, 'POST', 200, options, false);
	return getDeferred(url, -1, 'POST', 200, options, false);
};

var getLunar = function(options) {
	var url = "/astro/publish/lunar";
	return getDeferred(url, DEFAULT_TIMEOUT, 'POST', 200, options, false);
};

var publishPerpetual = function(options, callback) {
	var getData = getPerpetualDoc(options);
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
		errManager("Failed publish perpetual almanac data..." + (error !== undefined ? error : ' - ') + ', ' + (message !== undefined
		? message : ' - '));
	});
};

var publishAlmanac = function(options, callback) {
	var getData = getAlmanac(options);
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
		errManager("Failed publishing Almanac..." + (error !== undefined ? error : ' - ') + ', ' + (message !== undefined
				? message : ' - '));
	});
};

var publishLunar= function(options, callback) {
	var getData = getLunar(options);
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
		errManager("Failed publishing Lunar..." + (error !== undefined ? error : ' - ') + ', ' + (message !== undefined
				? message : ' - '));
	});
};
