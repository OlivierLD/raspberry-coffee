"use strict";

/*
 For debugging,
 set DEBUG to true.

 TODO Move to ES6
 */
var DEBUG = false;
var VERBOSE = false;
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

if (typeof(getDir) === "undefined") {
/**
 * Get wind direction from ugrd, vgrd
 * @param x ugrd
 * @param y vgrd
 * @returns {number} Direction in degrees [0..360]
 */
var getDir = function(x, y) {
	var dir = 0.0;
	if (y !== 0) {
		dir = toDegrees(Math.atan(x / y));
	}
	if (x <= 0 || y <= 0) {
		if (x > 0 && y < 0) {
			dir += 180;
		} else if (x < 0 && y > 0) {
			dir += 360;
		} else if (x < 0 && y < 0) {
			dir += 180;
		} else if (x === 0) {
			if (y > 0) {
				dir = 0.0;
			} else {
				dir = 180;
			}
		} else if (y === 0) {
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
}

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
const WIND_ARROW_TRANSPARENCY = 0.3;

var drawWindArrow = function(context, at, twd, tws) {

	context.lineWidth = 1;

	var roundTWS = Math.round(tws);
	var dTWD = Math.toRadians(twd);

	context.strokeStyle = 'rgba(0, 0, 255, ' + WIND_ARROW_TRANSPARENCY.toString() + ')';

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

var getBGColor = function(value, type) {
	var color = 'white';
	switch (type) {
		case 'wind': // blue, [0..80]
			color = 'rgba(0, 0, 255, ' + Math.min((value / 80), 1) + ')';
			break;
		case 'prmsl': // red, 101300, [95000..104000], inverted
			color = 'rgba(255, 0, 0,' + (1 - Math.min((value - 95000) / (104000 - 95000), 1)) + ')';
			break;
		case 'hgt': // blue, 5640, [4700..6000], inverted
			color = 'rgba(0, 0, 255, ' + (1 - Math.min((value - 4700) / (6000 - 4700), 1)) + ')';
			break;
		case 'prate': // black, [0..30]. Unit is Kg x m-2 x s-1, which is 1mm.s-1. Turned into mm/h
			var max = 30;
			var mm_per_hour = value * 3600;
			var transp = 	Math.min(((mm_per_hour) / max), 1);
			var blue = Math.max(255 - (mm_per_hour * (255 / max)), 0).toFixed(0);
			// if (mm_per_hour > 20) {
			// 	console.log(`>> Value: ${mm_per_hour} => Blue: ${blue}`);
			// }
			color = `rgba(0, 0, ${blue}, ${transp.toFixed(2)})`; // max 30 mm/h
			break;
		case 'tmp': // blue, to red, [233..323] (Celcius [-40..50]). [-40..0] -> blue. [0..50] -> red
			if (value <= 273) { // lower than 0 C
				color = 'rgba(0, 0, 255,' + (1 - Math.min((value - 233) / (273 - 233), 1)) + ')'; // Blue
			} else {
				color = 'rgba(255, 0, 0,' + Math.min((value - 273) / (323 - 273), 1) + ')'; // Red
			}
			break;
		case 'htsgw': // green, [0..15]
			color = 'rgba(0, 100, 0,' + Math.min((value) / 15, 1) + ')';
			break;
		default:
			break;
	}
	return color;
};

var bestRouteToPlot = undefined;
var plotBestRoute = function(canvas, context) {
//console.log("Plotting the best computed route: ", bestRouteToPlot);
	var waypoints = bestRouteToPlot.waypoints;
	context.save();
	context.strokeStyle = 'orange';
	context.lineWidth = 3;
	context.beginPath();
	for (var i=0; i<waypoints.length; i++) {
//	console.log("Plot", waypoints[i].position.latitude + " / " + waypoints[i].position.longitude);
		var canvasPt = worldMap.getCanvasLocation(canvas, waypoints[i].position.latitude, waypoints[i].position.longitude);
		console.log();
		if (i === 0) {
			context.lineTo(canvasPt.x, canvasPt.y);
		} else {
			context.lineTo(canvasPt.x, canvasPt.y);
		}
	}
	context.stroke();
	context.closePath();
	context.restore();
};

// Invoked by the callback
var drawGrib = function(canvas, context, gribData, date, type) {
	var oneDateGRIB = gribData[0]; // Default

	// Look for the right date
	for (var i=0; i<gribData.length; i++) {
		if (gribData[i].gribDate.formattedUTCDate === date) {
			oneDateGRIB = gribData[i];
			break;
		}
	}

	// Base this on the type.
	var data = {}; // ugrd, vgrd
	// Look for the right data
	switch (type) {
		case 'wind': // Hybrid type
			for (var i = 0; i < oneDateGRIB.typedData.length; i++) {
				if (oneDateGRIB.typedData[i].gribType.type === 'ugrd') {
					data.x = oneDateGRIB.typedData[i].data;
				} else if (oneDateGRIB.typedData[i].gribType.type === 'vgrd') {
					data.y = oneDateGRIB.typedData[i].data;
				}
			}
			break;
		case 'hgt': // 500mb, gpm
		case 'tmp': // Air temp, K
		case 'prmsl': // Atm Press, Pa
		case 'htsgw': // Wave Height, m
		case 'prate': // Precipitation rate, kg/m^2/s
			for (var i = 0; i < oneDateGRIB.typedData.length; i++) {
				if (oneDateGRIB.typedData[i].gribType.type === type) {
					data.x = oneDateGRIB.typedData[i].data;
				}
			}
			break;
		default:
			break;
	}
	if (VERBOSE) {
		console.log(">> Type %s, Date: %s", type, date);
		console.log("   Dim (W x H) : %d x %d", data.x[0].length, data.x.length);
	}

	var maxTWS = 0;

	for (var hGRIB=0; hGRIB<oneDateGRIB.gribDate.height; hGRIB++) {
		// Actual width... Waves Height has a different lng step.
		var stepX = oneDateGRIB.gribDate.stepx;
		var width = oneDateGRIB.gribDate.width;

		// Find the typedData
		var typedData;
		for (var t=0; t<oneDateGRIB.typedData.length; t++) {
			if (type === oneDateGRIB.typedData[t].gribType.type) {
				typedData = oneDateGRIB.typedData[t];
				break;
			}
		}

		if (typedData !== undefined && typedData.data[0].length !== oneDateGRIB.gribDate.width) {
			width = typedData.data[0].length;
			stepX *= (oneDateGRIB.gribDate.width / typedData.data[0].length);
		}

		for (var wGRIB=0; wGRIB<width; wGRIB++) {
			// Evaluate the cell (lat/lng): [0][0] is bottom left (SW).
			// 1. Cell BG
			var bottomLeft = worldMap.getCanvasLocation(canvas,
					oneDateGRIB.gribDate.bottom + ((oneDateGRIB.gribDate.stepy * hGRIB)),
					ajustedLongitude(oneDateGRIB.gribDate.left, (stepX * wGRIB)));
			var bottomRight = worldMap.getCanvasLocation(canvas,
					oneDateGRIB.gribDate.bottom + ((oneDateGRIB.gribDate.stepy * hGRIB)),
					ajustedLongitude(oneDateGRIB.gribDate.left, (stepX * wGRIB) + (stepX)));
			var topLeft = worldMap.getCanvasLocation(canvas,
					oneDateGRIB.gribDate.bottom + ((oneDateGRIB.gribDate.stepy * hGRIB) + (oneDateGRIB.gribDate.stepy)),
					ajustedLongitude(oneDateGRIB.gribDate.left, (stepX * wGRIB)));
			var topRight = worldMap.getCanvasLocation(canvas,
					oneDateGRIB.gribDate.bottom + ((oneDateGRIB.gribDate.stepy * hGRIB) + (oneDateGRIB.gribDate.stepy)),
					ajustedLongitude(oneDateGRIB.gribDate.left, (stepX * wGRIB) + (stepX)));

			var gribValue;
			if (type === 'wind') {
				gribValue = getSpeed(data.x[hGRIB][wGRIB], data.y[hGRIB][wGRIB]);
			} else {
				gribValue = data.x[hGRIB][wGRIB];
			}

			// BG Color
			context.fillStyle = getBGColor(gribValue, type);
			if (VERBOSE && type === 'htsgw' && gribValue > 4) {
				console.log(">> Cell (X, Y) (%d, %d): %s => %f", wGRIB, hGRIB, type, gribValue);
			}
			context.fillRect(topLeft.x, topLeft.y, bottomRight.x - topLeft.x, bottomRight.y - topLeft.y);
//		context.stroke();

			if (type === 'wind') {
				// Center of the cell
				var lng = ajustedLongitude(oneDateGRIB.gribDate.left, (oneDateGRIB.gribDate.stepx * wGRIB) + (oneDateGRIB.gribDate.stepx / 2));
				var lat  = oneDateGRIB.gribDate.bottom + ((oneDateGRIB.gribDate.stepy * hGRIB) + (oneDateGRIB.gribDate.stepy / 2));
				// data
				var dir = getDir(data.x[hGRIB][wGRIB], data.y[hGRIB][wGRIB]);
				var speed = gribValue;
		    // console.log("%f / %f (cell %d, %d), dir %s, speed %f kn (u: %f, v: %f)", lat, lng, hGRIB, wGRIB, dir.toFixed(0), speed, data.x[hGRIB][wGRIB], data.y[hGRIB][wGRIB]);

				var canvasPt = worldMap.getCanvasLocation(canvas, lat, lng);
				drawWindArrow(context, canvasPt, dir, speed);

				maxTWS = Math.max(maxTWS, speed);
			}

			// DEBUG, print cell coordinates IN the cell.
			if (DEBUG) {
				var label = "h:" + hGRIB;
				context.fillStyle = 'black';
				context.font = "8px Courier";
				context.fillText(label, topLeft.x + 1, topLeft.y + 9);
				var label = "w:" + wGRIB;
				context.fillText(label, topLeft.x + 1, topLeft.y + 18);
			}
		}
	}
	console.log("Max TWS: %d kn", maxTWS);
	try {
		document.getElementById('max-wind').innerText = `Max GRIB TWS: ${maxTWS.toFixed(2)} kn`;
	} catch (err) {}
	// Is there a route to draw here?
	if (bestRouteToPlot !== undefined) {
		plotBestRoute(canvas, context);
	}
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
