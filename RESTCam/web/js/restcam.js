"use strict";

var DEFAULT_TIMEOUT = 60000; // 60000 ms: 1 minute

var displayErr = function(mess) {
	if (displayMess !== undefined) {
		displayMess(mess);
	} else {
		console.log(mess);
	}
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
	if (data === undefined || data === null) {
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

var snap = function(name) {
	var url = "/cam/snap";
	// Add name
	url += ("?name=" + name);
	return getDeferred(url, DEFAULT_TIMEOUT, 'POST', 200, null, false);
};

var takeSnap = function(name, callback) {
	var getData = snap(name);
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
		displayErr("Failed to Take snapshot..." + (error !== undefined ? error : ' - ') + ', ' + (message !== undefined ? message : ' - '));
	});
};

var camPostition = function() {
	var url = "/cam/position";
	return getDeferred(url, DEFAULT_TIMEOUT, 'GET', 200, null, false);
};

var getCamPos = function(callback) {
	var getData = camPostition();
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
		displayErr("Failed to get cam position..." + (error !== undefined ? error : ' - ') + ', ' + (message !== undefined ? message : ' - '));
	});
};

var setCamTiltPostition = function(data) {
	var url = "/cam/tilt";
	return getDeferred(url, DEFAULT_TIMEOUT, 'POST', 200, data, false);
};

var setTiltCam = function(data, callback) {
	var getData = setCamTiltPostition(data);
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
		displayErr("Failed to set cam position (tilt)..." + (error !== undefined ? error : ' - ') + ', ' + (message !== undefined ? message : ' - '));
	});
};

var setCamHeadingPostition = function(data) {
	var url = "/cam/heading";
	return getDeferred(url, DEFAULT_TIMEOUT, 'POST', 200, data, false);
};

var setHeadingCam = function(data, callback) {
	var getData = setCamHeadingPostition(data);
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
		displayErr("Failed to set cam position (heading)..." + (error !== undefined ? error : ' - ') + ', ' + (message !== undefined ? message : ' - '));
	});
};

