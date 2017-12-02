"use strict";

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

var requestGRIB = function(gribRequest, cb) {
	var getData = getGRIB(gribRequest);
	getData.done(function(value) {
		var json = JSON.parse(value);
		// Do something smart here.
		if (cb !== undefined) {
			cb(json);
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
		errManager("Failed to get the GRIB..." + (error !== undefined ? error : ' - ') + ', ' + (message !== undefined ? message : ' - '));
	});
};

var getSpeed = function(x, y) {
	var tws = Math.sqrt((x * x) + (y * y));
	tws *= 3.600; // m/s to km/h
	tws /= 1.852; // km/h to knots
	return tws;
};

/**
 * Get wind direction from ugrd, vgrd
 * @param x ugrd
 * @param y vgrd
 * @returns {number} Direction in degrees [0..360]
 */
var getDir = function(x, y) {
	var dir = 0.0;
	if (y != 0)
		dir = toDegrees(Math.atan(x / y));
	if (x <= 0 || y <= 0) {
		if (x > 0 && y < 0) {
			dir += 180;
		} else if (x < 0 && y > 0) {
			dir += 360;
		} else if (x < 0 && y < 0) {
			dir += 180;
		} else if (x == 0) {
			if (y > 0) {
				dir = 0.0;
			} else {
				dir = 180;
			}
		} else if (y == 0) {
			if (x > 0) {
				dir = 90;
			} else {
				dir = 270;
			}
		}
	}
	dir += 180;
	while (dir >= 360) {
		dir -= 360;
	}
	return dir;
};

var toRadians = function (deg) {
	return deg * (Math.PI / 180);
};

var toDegrees = function (rad) {
	return rad * (180 / Math.PI);
};

var GRIBTypes = {
	surfaceWind: 'SFC_WIND'
};

var ajustedLongitude = function(leftBoundary, eastIncrease) {
	var lng = leftBoundary + eastIncrease;
	lng = lng % 360;
	if (lng > 180) {
		lng -= 360;
	}
	return lng;
};

const ARROW_LENGTH = 20;

var drawWindArrow = function(context, at, twd, tws) {

	context.lineWidth = 1;

	var roundTWS = Math.round(tws);
	var dTWD = Math.toRadians(twd);

	context.strokeStyle = 'rgba(0, 0, 255, 0.75)';

	var x = at.x;
	var y = at.y;

	// Arrow
	var featherX = ARROW_LENGTH * Math.sin(dTWD);
	var featherY = ARROW_LENGTH * Math.cos(dTWD);
	context.beginPath();
	context.moveTo(x, y);
	context.lineTo(x + featherX, y - featherY);
	context.closePath();
	context.stroke();

	// Feathers
	var origin = ARROW_LENGTH;
	while (roundTWS >= 50) {
		roundTWS -= 50;
		var featherStartX = x + (origin * Math.sin(dTWD));
		var featherStartY = y - (origin * Math.cos(dTWD));
		var featherEndX = featherStartX + (10 * Math.sin(dTWD + Math.toRadians(60)));
		var featherEndY = featherStartY - (10 * Math.cos(dTWD + Math.toRadians(60)));
		var featherStartX2 = x + ((origin - 5) * Math.sin(dTWD));
		var featherStartY2 = y - ((origin - 5) * Math.cos(dTWD));
		origin -= 5;

		context.beginPath();
		context.moveTo(featherStartX, featherStartY);
		context.lineTo(featherEndX, featherEndY);
		context.lineTo(featherStartX2, featherStartY2);
		context.closePath();
		context.fill();
	}
	while (roundTWS >= 10) {
		roundTWS -= 10;
		var featherStartX = x + (origin * Math.sin(dTWD));
		var featherStartY = y - (origin * Math.cos(dTWD));
		var featherEndX = featherStartX + (7 * Math.sin(dTWD + Math.toRadians(60)));
		var featherEndY = featherStartY - (7 * Math.cos(dTWD + Math.toRadians(60)));

		context.beginPath();
		context.moveTo(featherStartX, featherStartY);
		context.lineTo(featherEndX, featherEndY);
		context.closePath();
		context.stroke();
		origin -= 3;
	}
	if (roundTWS >= 5) {
		var featherStartX = x + (origin * Math.sin(dTWD));
		var featherStartY = y - (origin * Math.cos(dTWD));
		var featherEndX = featherStartX + (4 * Math.sin(dTWD + Math.toRadians(60)));
		var featherEndY = featherStartY - (4 * Math.cos(dTWD + Math.toRadians(60)));

		context.beginPath();
		context.moveTo(featherStartX, featherStartY);
		context.lineTo(featherEndX, featherEndY);
		context.closePath();
		context.stroke();
	}
};

// Invoked by the callback
var drawGrib = function(canvas, context, gribData, date, type) {
	var oneDateGRIB = gribData[0]; // Default

	for (var i=0; i<gribData.length; i++) {
		if (gribData[i].gribDate.formattedUTCDate === date) {
			oneDateGRIB = gribData[i];
			break;
		}
	}

	// TODO Base this on the type. This is for the Surface Wind
	var data = {}; // ugrd, vgrd
	for (var i=0; i<oneDateGRIB.typedData.length; i++) {
		if (oneDateGRIB.typedData[i].gribType.type === 'ugrd') {
			data.x = oneDateGRIB.typedData[i].data;
		} else if (oneDateGRIB.typedData[i].gribType.type === 'vgrd') {
			data.y = oneDateGRIB.typedData[i].data;
		}
	}
	console.log("Width :", data.x[0].length);
	console.log("Height:", data.x.length);

	var maxTWS = 0;

	for (var hGRIB=0; hGRIB<oneDateGRIB.gribDate.height; hGRIB++) {
		for (var wGRIB=0; wGRIB<oneDateGRIB.gribDate.width; wGRIB++) {
			// Evaluate the cell (lat/lng): [0][0] is bottom left (SW).
			// 1. Cell BG
			var bottomLeft = worldMap.getCanvasLocation(canvas,
					oneDateGRIB.gribDate.bottom + ((oneDateGRIB.gribDate.stepy * hGRIB)),
					ajustedLongitude(oneDateGRIB.gribDate.left, (oneDateGRIB.gribDate.stepx * wGRIB)));
			var bottomRight = worldMap.getCanvasLocation(canvas,
					oneDateGRIB.gribDate.bottom + ((oneDateGRIB.gribDate.stepy * hGRIB)),
					ajustedLongitude(oneDateGRIB.gribDate.left, (oneDateGRIB.gribDate.stepx * wGRIB) + (oneDateGRIB.gribDate.stepx)));
			var topLeft = worldMap.getCanvasLocation(canvas,
					oneDateGRIB.gribDate.bottom + ((oneDateGRIB.gribDate.stepy * hGRIB) + (oneDateGRIB.gribDate.stepy)),
					ajustedLongitude(oneDateGRIB.gribDate.left, (oneDateGRIB.gribDate.stepx * wGRIB)));
			var topRight = worldMap.getCanvasLocation(canvas,
					oneDateGRIB.gribDate.bottom + ((oneDateGRIB.gribDate.stepy * hGRIB) + (oneDateGRIB.gribDate.stepy)),
					ajustedLongitude(oneDateGRIB.gribDate.left, (oneDateGRIB.gribDate.stepx * wGRIB) + (oneDateGRIB.gribDate.stepx)));

			// Center of the cell
			var lng = ajustedLongitude(oneDateGRIB.gribDate.left, (oneDateGRIB.gribDate.stepx * wGRIB) + (oneDateGRIB.gribDate.stepx / 2));
			var lat  = oneDateGRIB.gribDate.bottom + ((oneDateGRIB.gribDate.stepy * hGRIB) + (oneDateGRIB.gribDate.stepy / 2));
			// data
			var dir = getDir(data.x[hGRIB][wGRIB], data.y[hGRIB][wGRIB]);
			var speed = getSpeed(data.x[hGRIB][wGRIB], data.y[hGRIB][wGRIB]);
//		console.log("%f / %f, dir %s, speed %f kn", lat, lng, dir.toFixed(0), speed);

			// BG Color
			context.fillStyle = 'rgba(0, 0, 255, ' + Math.min((speed / 50), 1) + ')';
			context.fillRect(topLeft.x, topLeft.y, bottomRight.x - topLeft.x, bottomRight.y - topLeft.y);
//		context.stroke();

			var canvasPt = worldMap.getCanvasLocation(canvas, lat, lng);
			drawWindArrow(context, canvasPt, dir, speed);

			maxTWS = Math.max(maxTWS, speed);
		}
	}
	console.log("Max TWS: %d kn", maxTWS);
};

// For tests
/*
var gribData = [
	{
		"gribDate": {
			"date": "Nov 30, 2017, 12:00:00 PM",
			"epoch": 1512072000000,
			"formattedUTCDate": "2017-11-30 12:00:00 UTC",
			"height": 56,
			"width": 61,
			"stepx": 2,
			"stepy": 2,
			"top": 65,
			"bottom": -45,
			"left": 130,
			"right": -110
		},
		"typedData": [
			{
				"gribType": {
					"type": "hgt",
					"desc": "Geopotential height",
					"unit": "gpm",
					"min": 4919.866,
					"max": 5899.366
				},
				"data": [
					[
						5480.386,
						5474.5264,
						5472.706,
...
						5073.241
					]
				]
			}
		]
	}
];
drawGrib(null, null, gribData, null, null);
*/
