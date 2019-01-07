"use strict";

var SunFlowerClient = function (dataManager, bp) {

	var onMessage = dataManager; // Client function
	var betweenPing = 1000;
	if (bp !== undefined) {
		betweenPing = bp;
	}

	var DEFAULT_TIMEOUT = 10000;

	var getDeferred = function (
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

		var requestTimer = setTimeout(function () {
			xhr.abort();
			var mess = {message: 'Timeout'};
			deferred.reject(408, mess);
		}, TIMEOUT);

		xhr.onload = function () {
			clearTimeout(requestTimer);
			if (xhr.status === happyCode) {
				deferred.resolve(xhr.response);
			} else {
				deferred.reject(xhr.status, xhr.response);
			}
		};
		return deferred.promise();
	};

	var getSunFlowerData = function () {
		var deferred = $.Deferred(),  // a jQuery deferred
				url = '/sun-flower/all',
				xhr = new XMLHttpRequest(),
				TIMEOUT = 10000;

		xhr.open('GET', url, true);
		xhr.setRequestHeader("Content-type", "application/json");
		try {
			xhr.send();
		} catch (err) {
			throw err;
		}

		var requestTimer = setTimeout(function () {
			xhr.abort();
			deferred.reject(408, {message: 'Timeout'});
		}, TIMEOUT);

		xhr.onload = function () {
			clearTimeout(requestTimer);
			if (xhr.status === 200) {
				deferred.resolve(xhr.response);
			} else {
				deferred.reject(xhr.status, xhr.response);
			}
		};
		return deferred.promise();
	};

	this.requestSunPath = function (pos) {
		var url = "/astro/sun-path-today";
		return getDeferred(url, DEFAULT_TIMEOUT, 'POST', 200, pos, false);
	};

	this.requestSunData= function (pos) {
		var url = "/astro/sun-now";
		return getDeferred(url, DEFAULT_TIMEOUT, 'POST', 200, pos, false);
	};


//  Executed at startup
	(function () {
		// Long poll
		setInterval(function () {
			fetch();
		}, betweenPing);
	})();

	var fetch = function () {
		var getData = getSunFlowerData();
		getData.done(function (value) {
			//  console.log("Done:", value);
			var json = JSON.parse(value);
			onMessage(json);
		});
		getData.fail(function (error, errmess) {
			var message;
			if (errmess !== undefined) {
				try {
					var mess = JSON.parse(errmess);
					if (mess.message !== undefined) {
						message = mess.message;
					}
				} catch (err) {
					//  console.log(errmess);
				}
			}
			console.log("Failed to get sunflower data..." + (error !== undefined ? error : ' - ') + ', ' + (message !== undefined ? message : ' - '));
		});

		if (pathAndDataOK === true) {

				if (lat !== undefined && lng !== undefined) {
					getSunData({latitude: lat, longitude: lng});
			//	getSunPath({position: {latitude: lat, longitude: lng}}); // Default step: 10, sibling of position.
					pathAndDataOK = true;
				} else {
					getSunData(null);
			//	getSunPath({}); // Default position by system variables, step: 10, sibling of position.
					pathAndDataOK = true;
				}
		}

	};

};
