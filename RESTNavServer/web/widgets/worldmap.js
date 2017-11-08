/**
 *
 * @author Olivier Le Diouris
 */

const projections = {
	anaximandre: { type: "ANAXIMANDRE"},
	mercator: { type: "MERCATOR"},
	globe: { type: "GLOBE"}
};
const tropicLat = 23.43698;

function WorldMap (cName, prj) {

	var defaultRadiusRatio = 0.6;

	var canvasName = cName;
	var withGrid = true;
	var withSun = true;
	var withMoon = true;
	var withSunlight = false;
	var withMoonlight = false;
	var withWanderingBodies = false;
	var withStars = false;
	var withTropics = false;

	var label = "Your position";

	this.setWithGrid = function(b) {
		withGrid = b;
	};
	this.setWithSun = function(b) {
		withSun = b;
	};
	this.setWithMoon = function(b) {
		withMoon = b;
	};
	this.setWithSunLight = function(b) {
		withSunlight = b;
	};
	this.setWithMoonLight = function(b) {
		withMoonlight = b;
	};
	this.setWithWanderingBodies = function(b) {
		withWanderingBodies = b;
	};
	this.setWithStars = function(b) {
		withStars = b;
	};
	this.setWithTropics = function(b) {
	  withTropics = b;
	};

	this.setPositionLabel = function(str) {
		label = str;
	};

	var projectionSupported = function(value) {
		for  (var name in projections) {
			if (projections[name].type === value) {
				return true;
			}
		}
		return false;
	};

	if (!projectionSupported(prj)) {
		throw { "err": "Projection [" + prj + "] not supported" };
	}
	var projection = prj; // Make sure it's available in the list

	this.clear = function () {
		var canvas = document.getElementById(canvasName);
		if (canvas !== undefined) {
			var context = canvas.getContext('2d');
			// Cleanup
			context.fillStyle = "rgba(0, 0, 100, 10.0)";
			context.fillRect(0, 0, canvas.width, canvas.height);
		}
	};

	var fromPt, toPt;
	var animationID;

	/**
	 * Listens to clicks on canvas.
	 */
	var addCanvasListener = function () {
		var canvas = document.getElementById(canvasName);
		canvas.addEventListener("click", // "dblclick", "mousedown", "mouseup", "mousemove"
				function (event) {
//        console.log("Click on Canvas, event=" + (event == undefined?"undefined":("OK:" + event.clientX + ", " + event.clientY)));
					var xClick;
					var yClick;

					if (event.pageX || event.pageY) {
						xClick = event.pageX;
						yClick = event.pageY;
					} else {
						xClick = event.clientX + document.body.scrollLeft + document.documentElement.scrollLeft;
						yClick = event.clientY + document.body.scrollTop + document.documentElement.scrollTop;
					}
					xClick -= canvas.offsetLeft;
					yClick -= canvas.offsetTop;

					if (toPt !== undefined) {
						fromPt = toPt; // Swap
						toPt = undefined;
						clear();
						drawWorldMap();
						plotPoint(canvasName, fromPt, "red");
					}
					if (fromPt === undefined) {
						fromPt = {"x": xClick, "y": yClick};
						plotPoint(canvasName, fromPt, "red");
					} else if (toPt === undefined) {
						toPt = {"x": xClick, "y": yClick};
						plotPoint(canvasName, toPt, "red");
						currentStep = 0;
						animationID = window.setInterval(function () {
							travel(canvasName, fromPt, toPt, 10);
						}, 100);
					}
				}, false);
	};

	var plotPoint = function (canvasName, pt, color) {
		var canvas = document.getElementById(canvasName);
		var context = canvas.getContext('2d');
		plot(context, pt, color);
	};

	var plot = function (context, pt, color) {
		context.beginPath();
		context.fillStyle = color;
		context.arc(pt.x, pt.y, 2, 0, 2 * Math.PI);
		context.stroke();
		context.fill();
		context.closePath();
	};

	var fillCircle = function(context, pt, radius, color) {
		context.beginPath();
		context.fillStyle = color;
		context.arc(pt.x, pt.y, radius, 0, radius * Math.PI);
//	context.stroke();
		context.fill();
		context.closePath();
	};

	var currentStep = 0;
	this.travel = function (canvasName, from, to, nbStep) {
		var newX = from.x + (currentStep * (to.x - from.x) / nbStep);
		var newY = from.y + (currentStep * (to.y - from.y) / nbStep);
		plotPoint(canvasName, {"x": newX, "y": newY}, "gray");
		currentStep++;
		if (currentStep > nbStep) {
			window.clearInterval(animationID);
		}
	};

	/*
	 * The right tilt is:
	 *   chartPanel.setGlobeViewRightLeftRotation(-(sunD * Math.sin(Math.toRadians(lhaSun))));
	 * An astro resource exists: GET /positions-in-the-sky?at=2017-10-11T15:23:28
	 */
	var globeViewRightLeftRotation = -tropicLat; // Tilt
	var globeViewForeAftRotation = 0; // Observer's latitude
	var globeViewLngOffset = 0;       // Observer's longitude

	var userPosition = {};

	this.setUserPosition = function (pos) {
		userPosition = pos;
		globeViewLngOffset = pos.longitude;
		globeViewForeAftRotation = pos.latitude;
	};

	this.setUserLatitude = function(val) {
		globeViewForeAftRotation = val;
		userPosition.latitude = val;
	};

	/*
	 * Example:
	 * {
			"epoch": 1507735408000,
			"sun": {
				"decl": -7.24973497969111,
				"gha": 54.205900687527475
			},
			"moon": {
				"decl": 19.60210295782154,
				"gha": 153.87494008457838
			}
		}
	 */
	var astronomicalData = {};
	var getAstronomicalData = function() {
		return astronimicalData;
	};

	/**
	 * Angles in degrees
	 * @param data like { D: declination, GHA: hourAngle }
	 */
	this.setSunPosition = function(data) {
		astronomicalData.sun = data;
	};

	/**
	 * Angles in degrees
	 * @param data like { D: declination, GHA: hourAngle }
	 */
	this.setMoonPosition = function(data) {
		astronomicalData.moon = data;
	};

	/**
	 * Angles in degrees
	 * @param data like described above
	 */
	this.setAstronomicalData = function(data) {
		astronomicalData = data;
//	console.log("Received", data);
		try {
			var at = new Date(data.epoch);
//		console.log("At", at.format("Y-M-d H:i:s"));
		} catch (err) {
			console.log(err);
		}
		if (data.sun !== undefined) {
			// set .setGlobeViewRightLeftRotation(-(sunD * Math.sin(Math.toRadians(lhaSun))));
			if (userPosition !== {}) {
				var lhaSun = data.sun.gha + userPosition.longitude;
				while (lhaSun > 360) lhaSun -= 360;
				while (lhaSun < 0) lhaSun += 360;
				globeViewRightLeftRotation = -(data.sun.decl * Math.sin(toRadians(lhaSun)));
//			console.log("Tilt is now", globeViewRightLeftRotation);
			}
		}
	};

	/**
	 * Used to draw a globe
	 * alpha, then beta
	 *
	 * @param lat in radians
	 * @param lng in radians
	 * @return x, y, z. Cartesian coordinates.
	 */
	var rotateBothWays = function (lat, lng) {
		var x = Math.cos(lat) * Math.sin(lng);
		var y = Math.sin(lat);
		var z = Math.cos(lat) * Math.cos(lng);

		var alfa = toRadians(globeViewRightLeftRotation); // in plan (x, y), z unchanged, earth inclination on its axis
		var beta = toRadians(globeViewForeAftRotation);   // in plan (y, z), x unchanged, latitude of the eye
		/*
		 * x is the x of the screen
		 * y is the y of the screen
		 * z goes through the screen
		 *
		 *                      |  cos a -sin a  0 |  a > 0 : counter-clockwise
		 * Rotation plan x, y:  |  sin a  cos a  0 |
		 *                      |    0     0     1 |
		 *
		 *                      | 1    0      0    |  b > 0 : towards user
		 * Rotation plan y, z:  | 0  cos b  -sin b |
		 *                      | 0  sin b   cos b |
		 *
		 *  | x |   | cos a -sin a  0 |   | 1   0      0    |   | x |   |  cos a  (-sin a * cos b) (sin a * sin b) |
		 *  | y | * | sin a  cos a  0 | * | 0  cos b -sin b | = | y | * |  sin a  (cos a * cos b) (-cos a * sin b) |
		 *  | z |   |  0      0     1 |   | 0  sin b  cos b |   | z |   |   0          sin b           cos b       |
		 */

		// All in once
		var _x = (x * Math.cos(alfa)) - (y * Math.sin(alfa) * Math.cos(beta)) + (z * Math.sin(alfa) * Math.sin(beta));
		var _y = (x * Math.sin(alfa)) + (y * Math.cos(alfa) * Math.cos(beta)) - (z * Math.cos(alfa) * Math.sin(beta));
		var _z = (y * Math.sin(beta)) + (z * Math.cos(beta));

		return {x: _x, y: _y, z: _z};
	};

	var _west = -180,
			_east = 180,
			_north = 90,
			_south = -90;

	this.setNorth = function(n) {
		_north = n;
	};

	this.setSouth = function(s) {
		_south = s;
	};

	this.setWest = function(w) {
		_west = w;
	};

	this.setEast = function(e) {
		_east = en;
	};

	var transparent = false;

	this.setTransparent = function(b) {
		transparent = b;
	};

	var isTransparentGlobe = function () {
		return transparent;
	};

	this.getCanvasHeight = function() {
		return document.getElementById(canvasName).height;
	};
	this.getCanvasWidth = function() {
		return document.getElementById(canvasName).width;
	};
	this.setCanvasWidth = function(w) {
		var canvas = document.getElementById(canvasName);
		canvas.width = w;
	};
	this.setCanvasHeight = function(h) {
		var canvas = document.getElementById(canvasName);
		canvas.height = h;
	};

	this.setZoomRatio = function(zr) {
		defaultRadiusRatio = Math.min(zr, 1);
	};
	this.getZoomRatio = function() {
		return defaultRadiusRatio;
	};
	this.resetZoomRatio = function() {
		defaultRadiusRatio = 0.6;
	};

	/**
	 *
	 * rotate.x Canvas abscissa
	 * rotate.y Canvas ordinate
	 * rotate.z -: behind the canvas, +: in front of the canvas
	 *
	 * @param lat in radians
	 * @param lng in radians
	 * @returns {boolean}
	 */
	var isBehind = function (lat, lng) {
		var rotated = rotateBothWays(lat, lng);
		return (rotated.z < 0.0);
	};

	var adjustBoundaries = function () {
		if (sign(_east) != sign(_west) && sign(_east) == -1) {
			_west -= 360;
		}
	};

	/**
	 *
	 * @param lat in degrees
	 * @param lng in degrees
	 */
	var getPanelPoint = function (lat, lng) {
		var pt = {};
		adjustBoundaries();
		if (_north !== _south && _east !== _west) {
			for (var gAmpl = _east - _west; gAmpl < 0; gAmpl += 360) ;
			var graph2chartRatio = w / gAmpl;
			var _lng = lng;
			if (Math.abs(_west) > 180 && sign(_lng) !== sign(_west) && sign(_lng) > 0) {
				_lng -= 360;
			}
			if (gAmpl > 180 && _lng < 0 && _west > 0) {
				_lng += 360;
			}
			if (gAmpl > 180 && _lng >= 0 && _west > 0 && _lng < _east) {
				_lng += (_west + (gAmpl - _east));
			}
			var rotated = rotateBothWays(toRadians(lat), toRadians(_lng - globeViewLngOffset));
			var x = Math.round(globeView_ratio * rotated.x);
			x += globeViewOffset_X;
			var y = Math.round(globeView_ratio * rotated.y);
			y = globeViewOffset_Y - y;
			pt = {x: x, y: y};
		}
		return pt;
	}

	var vGrid = 5,
			hGrid = 5;
	var w, h;

	var haToLongitude = function(ha) {
		var lng = - ha;
		if (lng < -180) {
			lng += 360;
		}
		return lng;
	};

	var positionBody = function(context, userPos, color, name, decl, gha, drawCircle, isStar) {
		isStar = isStar || false;
		context.save();
		var lng = haToLongitude(gha);
		var body = getPanelPoint(decl, lng);
		var thisPointIsBehind = isBehind(toRadians(decl), toRadians(lng - globeViewLngOffset));
		if (!thisPointIsBehind || isTransparentGlobe()) {
			// Draw Body
			plot(context, body, color);
			context.fillStyle = color;
			if (!isStar) { // Body name, on the ground
				context.fillText(name, Math.round(body.x) + 3, Math.round(body.y) - 3);
			}
			// Arrow, to the body
			context.setLineDash([2]);
			context.strokeStyle = 'rgba(255, 255, 255, 0.5)';
			context.beginPath();
			context.moveTo(userPos.x, userPos.y);
			context.lineTo(body.x, body.y);
			context.stroke();
			context.closePath();
			context.setLineDash([0]); // Reset
			context.strokeStyle = color;
			var deltaX = body.x - userPos.x;
			var deltaY = body.y - userPos.y;
			context.beginPath();
			context.moveTo(body.x, body.y);
			context.lineTo(body.x + deltaX, body.y + deltaY);
			context.stroke();
			if (isStar) { // Body name, in the sky
				context.font = "10px Arial";
				var metrics = context.measureText(name);
				len = metrics.width;
				context.fillText(name, Math.round(body.x + deltaX) - (len / 2), Math.round(body.y + deltaY));
			}
			context.closePath();
			if (drawCircle === undefined && drawCircle !== false) {
				fillCircle(context, { x: body.x + deltaX, y: body.y + deltaY}, 3, color);
			}
		}
		context.restore();
	};

	var drawGlobe = function (canvas, context) {
		var minX = Number.MAX_VALUE;
		var maxX = -Number.MAX_VALUE;
		var minY = Number.MAX_VALUE;
		var maxY = -Number.MAX_VALUE;

		w = canvas.width;
		h = canvas.height;

		var gOrig = Math.ceil(_west);
		var gProgress = gOrig;

		if (gProgress % vGrid !== 0) {
			gProgress = ((gProgress / vGrid) + 1) * vGrid;
		}
		var go = true;

		var __south = -90;
		var __north = 90;

		while (go) {
			for (var _lat = __south; _lat <= __north; _lat += 5) {
				var rotated = rotateBothWays(toRadians(_lat), toRadians(gProgress));

				var dx = rotated.x;
				var dy = rotated.y;
//    console.log("dx:" + dx + ", dy:" + dy);
				if (dx < minX) minX = dx;
				if (dx > maxX) maxX = dx;
				if (dy < minY) minY = dy;
				if (dy > maxY) maxY = dy;
			}
			gProgress += vGrid;
			if (gProgress > _east) {
				go = false;
			}
		}

		gOrig = Math.ceil(__south);
		var lProgress = gOrig;
		if (lProgress % hGrid !== 0) {
			lProgress = ((lProgress / hGrid) + 1) * hGrid;
		}
		go = true;
		while (go) {
			var rotated = rotateBothWays(toRadians(lProgress), toRadians(_west));
			var dx = rotated.x;
			var dy = rotated.y;
//  console.log("dx:" + dx + ", dy:" + dy);
			minX = Math.min(minX, dx);
			maxX = Math.max(maxX, dx);
			minY = Math.min(minY, dy);
			maxY = Math.max(maxY, dy);
			rotated = rotateBothWays(toRadians(lProgress), toRadians(_east));
			dx = rotated.x;
			dy = rotated.y;
//  console.log("dx:" + dx + ", dy:" + dy);
			minX = Math.min(minX, dx);
			maxX = Math.max(maxX, dx);
			minY = Math.min(minY, dy);
			maxY = Math.max(maxY, dy);
			lProgress += hGrid;

//	console.log("MinX, MaxX, MinY, MaxY ", minX, maxX, minY, maxY);
			if (lProgress > __north) {
				go = false;
			}
		}
//console.log("MinX:" + minX + ", MaxX:" + maxX + ", MinY:" + minY + ", MaxY:" + maxY);
		var opWidth = Math.abs(maxX - minX);
		var opHeight = Math.abs(maxY - minY);
		globeView_ratio = Math.min(w / opWidth, h / opHeight) * defaultRadiusRatio; // 0.9, not to take all the space...

		// Black background
		context.fillStyle = "black";
		context.fillRect(0, 0, canvas.width, canvas.height);

		// Circle
		var radius = Math.min(w / 2, h / 2) * defaultRadiusRatio;
		var grd = context.createRadialGradient(canvas.width / 2, canvas.height / 2, radius, 90, 60, radius);
		grd.addColorStop(0, "navy");
		grd.addColorStop(1, "blue");

		context.fillStyle = grd; // "rgba(0, 0, 100, 10.0)"; // Dark blue

		context.arc(canvas.width / 2, canvas.height / 2, radius, 0, 2 * Math.PI);
		context.fill();

		globeViewOffset_X = Math.abs((globeView_ratio * opWidth) - w) / 2 - (globeView_ratio * minX);
		globeViewOffset_Y = Math.abs((globeView_ratio * opHeight) - h) / 2 - (globeView_ratio * minY);

		var gstep = 10; //Math.abs(_east - _west) / 60;
		var lstep = 10;  //Math.abs(_north - _south) / 10;

		context.lineWidth = 1;
		context.strokeStyle = 'rgba(0, 255, 255, 0.3)'; // 'cyan';

		if (withGrid) {
			context.save();
			// Meridians
			for (var i = Math.min(_east, _west); i < Math.max(_east, _west); i += gstep) {
				var previous = null;
				context.beginPath();
				for (var j = Math.min(_south, _north) + (lstep / 5); j < Math.max(_south, _north); j += (lstep / 5)) {
					var p = getPanelPoint(j, i);

					var thisPointIsBehind = isBehind(toRadians(j), toRadians(i - globeViewLngOffset));

					if (!isTransparentGlobe() && thisPointIsBehind) {
						previous = null;
					} else {
						if (previous !== null) {
							if (Math.abs(previous.x - p.x) < (canvas.width / 2) && Math.abs(previous.y - p.y) < (canvas.height / 2)) {
								context.lineTo(p.x, p.y);
							}
						} else {
							context.moveTo(p.x, p.y);
						}
						previous = p;
					}
				}
				context.stroke();
				context.closePath();
			}

			// Parallels
			for (var j = Math.min(_south, _north) + lstep; j < Math.max(_south, _north); j += lstep) {
				var previous = null;
				context.beginPath();
				for (var i = Math.min(_east, _west); i <= Math.max(_east, _west); i += gstep) {
					var p = getPanelPoint(j, i);
					var thisPointIsBehind = isBehind(toRadians(j), toRadians(i - globeViewLngOffset));

					if (!isTransparentGlobe() && thisPointIsBehind) {
						previous = null;
					} else {
						if (previous !== null) {
							if (Math.abs(previous.x - p.x) < (canvas.width / 2) && Math.abs(previous.y - p.y) < (canvas.height / 2)) {
								context.lineTo(p.x, p.y);
							}
						} else {
							context.moveTo(p.x, p.y);
						}
						previous = p;
					}
				}
				context.stroke();
				context.closePath();
			}
			context.restore();
		}

		if (withTropics) {
			// Cancer
			context.fillStyle = 'LightGray';
			for (var lng = 0; lng<360; lng++) {
				var p = getPanelPoint(tropicLat, lng);
				var thisPointIsBehind = isBehind(toRadians(tropicLat), toRadians(lng - globeViewLngOffset));

				if (isTransparentGlobe() || !thisPointIsBehind) {
					context.fillRect(p.x, p.y, 1, 1);
				}
			}
			// Capricorn
			for (var lng = 0; lng<360; lng++) {
				var p = getPanelPoint(-tropicLat, lng);
				var thisPointIsBehind = isBehind(toRadians(-tropicLat), toRadians(lng - globeViewLngOffset));

				if (isTransparentGlobe() || !thisPointIsBehind) {
					context.fillRect(p.x, p.y, 1, 1);
				}
			}
			// North Polar Circle
			for (var lng = 0; lng<360; lng++) {
				var p = getPanelPoint(90 - tropicLat, lng);
				var thisPointIsBehind = isBehind(toRadians(90 - tropicLat), toRadians(lng - globeViewLngOffset));

				if (isTransparentGlobe() || !thisPointIsBehind) {
					context.fillRect(p.x, p.y, 1, 1);
				}
			}
			// South Polar Circle
			for (var lng = 0; lng<360; lng++) {
				var p = getPanelPoint(tropicLat - 90, lng);
				var thisPointIsBehind = isBehind(toRadians(tropicLat - 90), toRadians(lng - globeViewLngOffset));

				if (isTransparentGlobe() || !thisPointIsBehind) {
					context.fillRect(p.x, p.y, 1, 1);
				}
			}
		}

		// Chart
		context.save();
		if (fullWorldMap === undefined) {
			console.log("You must load [WorldMapData.js] to display a chart.");
		} else {
			try {
				var worldTop = fullWorldMap.top;
				var section = worldTop.section; // We assume top has been found.

//      console.log("Found " + section.length + " section(s).")
				for (var i = 0; i < section.length; i++) {
					var point = section[i].point;
					if (point !== undefined) {
						var firstPt = null;
						var previousPt = null;
						context.beginPath();
						for (var p = 0; p < point.length; p++) {
							var lat = parseFloat(point[p].Lat);
							var lng = parseFloat(point[p].Lng);
							if (lng < -180) lng += 360;
							if (lng > 180) lng -= 360;

							var thisPointIsBehind = isBehind(toRadians(lat), toRadians(lng - globeViewLngOffset));
							var drawIt = true;
							if (!isTransparentGlobe() && thisPointIsBehind) {
								drawIt = false;
								previousPt = null; // TODO Something better
							}
							var pt = getPanelPoint(lat, lng);
							if (previousPt === null) { // p === 0) {
								context.moveTo(pt.x, pt.y);
								firstPt = pt;
								previousPt = pt;
							} else {
								if (Math.abs(previousPt.x - pt.x) < (canvas.width / 2) && Math.abs(previousPt.y - pt.y) < (canvas.height / 2)) {
									context.lineTo(pt.x, pt.y);
									previousPt = pt;
								}
							}
						}
					}
					if (false && firstPt !== null && previousPt != null) {
						context.lineTo(firstPt.x, firstPt.y); // close the loop
					}
					context.lineWidth = 1;
					context.strokeStyle = 'cyan';
					context.stroke();
					context.closePath();
				}
			} catch (ex) {
				console.log("Oops:" + ex);
			}
		}
		context.restore();

		// User position
		if (userPosition !== {}) {
			var userPos = getPanelPoint(userPosition.latitude, userPosition.longitude);
			plot(context, userPos, "red");
			context.fillStyle = "red";
			context.fillText(label, Math.round(userPos.x) + 3, Math.round(userPos.y) - 3);
		}
		// Celestial bodies?
		if (astronomicalData !== {}) {
			if (astronomicalData.sun !== undefined && withSun) {
				context.save();
				var sunLng = haToLongitude(astronomicalData.sun.gha);
				var sun = getPanelPoint(astronomicalData.sun.decl, sunLng);
				var thisPointIsBehind = isBehind(toRadians(astronomicalData.sun.decl), toRadians(sunLng - globeViewLngOffset));
				if (!thisPointIsBehind || isTransparentGlobe()) {
					// Draw Sun
					plot(context, sun, "yellow");
					context.fillStyle = "yellow";
					context.fillText("Sun", Math.round(sun.x) + 3, Math.round(sun.y) - 3);
					// Arrow, to the sun
					context.setLineDash([2]);
					context.strokeStyle = 'rgba(255, 255, 0, 0.5)';
					context.beginPath();
					context.moveTo(userPos.x, userPos.y);
					context.lineTo(sun.x, sun.y);
					context.stroke();
					context.closePath();
					context.setLineDash([0]); // Reset
					context.strokeStyle = 'yellow';
					var deltaX = sun.x - userPos.x;
					var deltaY = sun.y - userPos.y;
					context.beginPath();
					context.moveTo(sun.x, sun.y);
					context.lineTo(sun.x + deltaX, sun.y + deltaY);
					context.stroke();
					context.closePath();
					if (false) {
						var img = document.getElementById("sun-png"); // 13x13
						var direction = getDir(deltaX, -deltaY);
						var imgXOffset = 7 * Math.sin(toRadians(direction));
						var imgYOffset = 7 * Math.cos(toRadians(direction));
						context.drawImage(img, sun.x + deltaX + Math.ceil(imgXOffset), sun.y + deltaY - Math.ceil(imgYOffset));
					} else {
						fillCircle(context, { x: sun.x + deltaX, y: sun.y + deltaY}, 6, 'yellow');
					}
				}
				// Route to sun?
				// context.lineWidth = 1;
				// context.strokeStyle = "yellow";
				// drawRhumbline(canvas, context, userPosition, { lat: astronomicalData.sun.decl, lng: sunLng })
				// Sunlight
				if (withSunlight) {
					var from = {lat: toRadians(astronomicalData.sun.decl), lng: toRadians(sunLng)};
					drawNight(canvas, context, from, userPosition, astronomicalData.sun.gha);
				}
				context.restore();
			}
			if (astronomicalData.moon !== undefined && withMoon) {
				context.save();
				var moonLng = haToLongitude(astronomicalData.moon.gha);
				var moon = getPanelPoint(astronomicalData.moon.decl, moonLng);
				var thisPointIsBehind = isBehind(toRadians(astronomicalData.moon.decl), toRadians(moonLng - globeViewLngOffset));
				if (!thisPointIsBehind || isTransparentGlobe()) {
					// Draw Moon
					plot(context, moon, "white");
					context.fillStyle = "white";
					context.fillText("Moon", Math.round(moon.x) + 3, Math.round(moon.y) - 3);
					// Arrow, to the moon
					context.setLineDash([2]);
					context.strokeStyle = 'rgba(255, 255, 255, 0.5)';
					context.beginPath();
					context.moveTo(userPos.x, userPos.y);
					context.lineTo(moon.x, moon.y);
					context.stroke();
					context.closePath();
					context.setLineDash([0]); // Reset
					context.strokeStyle = 'white';
					var deltaX = moon.x - userPos.x;
					var deltaY = moon.y - userPos.y;
					context.beginPath();
					context.moveTo(moon.x, moon.y);
					context.lineTo(moon.x + deltaX, moon.y + deltaY);
					context.stroke();
					context.closePath();
					if (false) {
						var img = document.getElementById("moon-png");
						var direction = getDir(deltaX, -deltaY);
						var imgXOffset = 7 * Math.sin(toRadians(direction));
						var imgYOffset = 7 * Math.cos(toRadians(direction));
						context.drawImage(img, moon.x + deltaX + Math.ceil(imgXOffset), moon.y + deltaY - Math.ceil(imgYOffset));
					} else {
						fillCircle(context, { x: moon.x + deltaX, y: moon.y + deltaY}, 5, 'white');
					}
				}
				// Moonlight
				if (withMoonlight) {
					var from = {lat: toRadians(astronomicalData.moon.decl), lng: toRadians(moonLng)};
					drawNight(canvas, context, from, userPosition, astronomicalData.moon.gha);
				}
				context.restore();
			}
			if (astronomicalData.wanderingBodies !== undefined && withWanderingBodies) {
			  // 1 - Ecliptic
			  var aries = findInList(astronomicalData.wanderingBodies, "name", "aries");
			  if (aries !== null) {
			    drawEcliptic(canvas, context, aries.gha, astronomicalData.eclipticObliquity);
			    positionBody(context, userPos, "LightGray", "Aries", 0, aries.gha, false);
			    positionBody(context, userPos, "LightGray", "Anti-Aries", 0, aries.gha + 180, false); // Libra?
			  }
			  // 2 - Other planets
			  var venus = findInList(astronomicalData.wanderingBodies, "name", "venus");
			  var mars = findInList(astronomicalData.wanderingBodies, "name", "mars");
			  var jupiter = findInList(astronomicalData.wanderingBodies, "name", "jupiter");
			  var saturn = findInList(astronomicalData.wanderingBodies, "name", "saturn");
			  if (venus !== null) {
			    positionBody(context, userPos, "orange", "Venus", venus.decl, venus.gha);
			  }
			  if (mars !== null) {
			    positionBody(context, userPos, "red", "Mars", mars.decl, mars.gha);
			  }
			  if (jupiter !== null) {
			    positionBody(context, userPos, "LightPink", "Jupiter", jupiter.decl, jupiter.gha);
			  }
			  if (saturn !== null) {
			    positionBody(context, userPos, "LightYellow", "Saturn", saturn.decl, saturn.gha);
			  }
			}

			if (astronomicalData.stars !== undefined && withStars) {
				astronomicalData.stars.forEach(function(star, idx) {
					positionBody(context, userPos, "white", star.name, star.decl, star.gha, false, true);
				});
			}
		}
	};

	var drawMercatorChart = function (canvas, context) {
		// TODO This is a copy of Anaximandre, fix this. (DeDup)

		// TODO Grid, tropics, astro, sun/moon light.

		var worldTop = fullWorldMap.top;
		var section = worldTop.section; // We assume top has been found.

//    console.log("Found " + section.length + " section(s).")
		for (var i = 0; i < section.length; i++) {
			var point = section[i].point;
			if (point !== undefined) {
				var firstPt = null;
				var previousPt = null;
				context.beginPath();
				for (var p = 0; p < point.length; p++) {
					var lat = parseFloat(point[p].Lat);
					var lng = parseFloat(point[p].Lng);
					if (lng < -180) lng += 360;
					if (lng > 180) lng -= 360;
					var pt = posToCanvas(canvas, lat, lng);
					if (p === 0) {
						context.moveTo(pt.x, pt.y);
						firstPt = pt;
						previousPt = pt;
					} else {
						if (Math.abs(previousPt.x - pt.x) < (canvas.width / 2) && Math.abs(previousPt.y - pt.y) < (canvas.height / 2)) {
							context.lineTo(pt.x, pt.y);
							previousPt = pt;
						} else { // Too far apart
							firstPt = pt;
							context.moveTo(pt.x, pt.y);
							previousPt = pt;
						}
					}
				}
			}
			// if (firstPt !== null) {
			// 	context.lineTo(firstPt.x, firstPt.y); // close the loop
			// }
			context.lineWidth = 1;
			context.strokeStyle = 'cyan'; // 'black';
			context.stroke();
			// context.fillStyle = "goldenrod";
			// context.fill();
			context.closePath();
		}
		// User position
		if (userPosition !== {}) {
//		var userPos = getPanelPoint(userPosition.latitude, userPosition.longitude);
			plotPosToCanvas(userPosition.latitude, userPosition.longitude, "Your position");
		}
	};

	var findInList = function(array, member, value) {
		for (var idx=0; idx<array.length; idx++) {
			if (array[idx][member] !== undefined && array[idx][member] === value) {
				return array[idx];
			}
		}
		return null;
	};

	var drawEcliptic = function(canvas, context, ariesGHA, obl) {
		var longitude = (ariesGHA < 180) ? -ariesGHA : 360 - ariesGHA;
		longitude += 90; // Extremum
    while (longitude > 360) {
      longitude -= 360;
    }
		var aries = { lat: toRadians(obl), lng: toRadians(longitude) };
		var eclCenter = deadReckoning(aries, 90 * 60, 0); // "Center" of the Ecliptic

		context.fillStyle = "white";
		for (var hdg=0; hdg<360; hdg++) {
			var pt = deadReckoning(eclCenter, 90 * 60, hdg);
			var pp = getPanelPoint(toDegrees(pt.lat), toDegrees(pt.lng));

			var thisPointIsBehind = isBehind(pt.lat, pt.lng - toRadians(globeViewLngOffset));

			if (isTransparentGlobe() || !thisPointIsBehind) {
				context.fillRect(pp.x, pp.y, 1, 1);
			}
		}
	};

	var drawRhumbline = function(canvas, context, userPosition, body) {
		var gc = new GreatCircle(
				{ lat: toRadians(userPosition.latitude), lng: toRadians(userPosition.longitude) },
				{ lat: toRadians(body.lat), lng: toRadians(body.lng) });
		var pts = gc.calculateGreatCircle(10);
		pts.forEach(function(item, idx) {
			var pt = getPanelPoint(toDegrees(item.pt.lat), toDegrees(item.pt.lng));
			if (idx === 0) {
				context.moveTo(pt.x, pt.y);
			} else {
				if (!isBehind(item.pt.lat, item.pt.lng - toRadians(globeViewLngOffset))) {
					context.lineTo(pt.x, pt.y);
				}
			}
		});
		context.stroke();
	};

	var drawNight = function(canvas, context, from, user, gha) {
		const NINETY_DEGREES = 90 * 60; // in nm

		var firstVisible = -1;
		const VISIBLE = 1;
		const INVISIBLE = 2;
		var visibility = 0;

		// context.lineWidth = 1;
		context.fillStyle = 'rgba(192, 192, 192, 0.3)';

		// find first visible point of the night limb
		for (var i=0; i<360; i++) {
			var night = deadReckoning(from, NINETY_DEGREES, i);
			var visible = isBehind(night.lat, night.lng - toRadians(globeViewLngOffset)) ? INVISIBLE : VISIBLE;
			if (visible === VISIBLE && visibility === INVISIBLE) { // Just became visible
				firstVisible = i;
				break;
			}
			visibility = visible;
		}

		context.beginPath();
		// Night limb
		var firstPt, lastPt;
		for (var dir=firstVisible; dir<firstVisible+360; dir++) {
			var dr = deadReckoning(from, NINETY_DEGREES, dir);
			var borderPt = getPanelPoint(toDegrees(dr.lat), toDegrees(dr.lng));
			if (dir === firstVisible) {
				context.moveTo(borderPt.x, borderPt.y);
				firstPt = borderPt;
			} else {
				if (!isBehind(dr.lat, dr.lng - toRadians(globeViewLngOffset))) {
					lastPt = borderPt;
					context.lineTo(borderPt.x, borderPt.y);
				}
			}
		}
		// Earth limb
		var center = { x: canvas.width / 2, y: canvas.height / 2};
		var startAngle = getDir(lastPt.x - center.x, center.y - lastPt.y);
		var arrivalAngle = getDir(firstPt.x - center.x, center.y - firstPt.y);

		var lhaSun = gha + user.longitude;
		while (lhaSun < 0) lhaSun += 360;
		while (lhaSun > 360) lhaSun -= 360;

		var clockwise = true;  // From the bottom
		if (lhaSun < 90 || lhaSun > 270) {  // Observer in the light
			clockwise = (lhaSun > 270);
		} else {                            // Observer in the dark
			clockwise = (lhaSun > 180);
		}
		if ((startAngle > 270 || startAngle < 90) && arrivalAngle > 90 && arrivalAngle < 270) {
			clockwise = !clockwise;
		}

		var inc = 1; // Clockwise
		var firstBoundary, lastBoundary;

		if (clockwise) {
			firstBoundary = Math.floor(startAngle);
			lastBoundary = Math.ceil(arrivalAngle);
			while (lastBoundary < firstBoundary) lastBoundary += 360;
		} else {
			inc = -1;
			firstBoundary = Math.ceil(startAngle);
			lastBoundary = Math.floor(arrivalAngle);
			while (lastBoundary > firstBoundary) firstBoundary += 360;
		}

		var userPos = { lat: toRadians(user.latitude), lng: toRadians(user.longitude) };
		for (var i=firstBoundary; (inc>0 && i<=lastBoundary) || (inc<0 && i>=lastBoundary); i+=inc) {
			var limb = deadReckoning(userPos, NINETY_DEGREES, i);
			var limbPt = getPanelPoint(toDegrees(limb.lat), toDegrees(limb.lng));
			context.lineTo(limbPt.x, limbPt.y);
		}
		context.closePath();
		context.fill();
	};

	var drawAnaximandreChart = function (canvas, context) {
		// Square projection, Anaximandre.
		var worldTop = fullWorldMap.top;
		var section = worldTop.section; // We assume top has been found.

//    console.log("Found " + section.length + " section(s).")
		for (var i = 0; i < section.length; i++) {
			var point = section[i].point;
			if (point !== undefined) {
				var firstPt = null;
				var previousPt = null;
				context.beginPath();
				for (var p = 0; p < point.length; p++) {
					var lat = parseFloat(point[p].Lat);
					var lng = parseFloat(point[p].Lng);
					if (lng < -180) lng += 360;
					if (lng > 180) lng -= 360;
					var pt = posToCanvas(canvas, lat, lng);
					if (p === 0) {
						context.moveTo(pt.x, pt.y);
						firstPt = pt;
						previousPt = pt;
					} else {
						if (Math.abs(previousPt.x - pt.x) < (canvas.width / 2) && Math.abs(previousPt.y - pt.y) < (canvas.height / 2)) {
							context.lineTo(pt.x, pt.y);
							previousPt = pt;
						}
					}
				}
			}
			if (firstPt !== null) {
				context.lineTo(firstPt.x, firstPt.y); // close the loop
			}
			context.lineWidth = 1;
			context.strokeStyle = 'black';
			context.stroke();
			context.fillStyle = "goldenrod";
			context.fill();
			context.closePath();
		}
		// User position
		if (userPosition !== {}) {
			var userPos = getPanelPoint(userPosition.latitude, userPosition.longitude);
			plotPosToCanvas(userPos.latitude, userPos.longitude, "Your position");
		}
	};

	this.drawWorldMap = function () {
		//var start = new Date().getTime();

		var canvas = document.getElementById(canvasName);
		var context = canvas.getContext('2d');

		if (fullWorldMap === undefined) {
			console.log("You must load [WorldMapData.js] to display a chart.");
		} else {
			try {
				switch (projection) {
					case undefined:
					case projections.anaximandre.type:
						drawAnaximandreChart(canvas, context);
						break;
					case projections.globe.type:
						drawGlobe(canvas, context);
						break;
					case projections.mercator.type:
						drawMercatorChart(canvas, context);
						break;
					default:
						console.log("Projection %s not available yet", projection);
						break;
				}
			} catch (ex) {
				console.log("Oops:" + ex);
			}
		}
		// Print position
		if (userPosition !== {}) {
			var strLat = decToSex(userPosition.latitude, "NS");
			var strLng = decToSex(userPosition.longitude, "EW");
			context.fillStyle = "cyan";
			context.font = "bold 16px Arial"; // "bold 40px Arial"
			context.fillText(strLat, 10, 18);
			context.fillText(strLng, 10, 38);
		}

		if (astronomicalData !== undefined && astronomicalData.deltaT !== undefined) {
			context.fillStyle = "cyan";
			context.font = "12px Arial"; // "bold 40px Arial"
			var deltaT = "\u0394T=" + astronomicalData.deltaT + " s";
			context.fillText(deltaT, 10, canvas.height - 5);
		}

//var end = new Date().getTime();
//console.log("Operation completed in " + (end - start) + " ms.");
	};

	/**
	 * For Anaximandre and Mercator
	 *
	 * @param lat
	 * @param lng
	 * @param label
	 * @param color
	 */
	var plotPosToCanvas = function (lat, lng, label, color) {
		var canvas = document.getElementById(canvasName);
		var pt = posToCanvas(canvas, lat, lng);
		plotPoint(canvasName, pt, (color !== undefined ? color : "red"));
		if (label !== undefined) {
			try {
				var context = canvas.getContext('2d');
				context.fillStyle = (color !== undefined ? color : "red");
				context.fillText(label, Math.round(pt.x) + 3, Math.round(pt.y) - 3);
			} catch (err) { // Firefox has some glitches here
				if (console.log !== undefined) {
					if (err.message !== undefined && err.name !== undefined) {
						console.log(err.message + " " + err.name);
					} else {
						console.log(err);
					}
				}
			}
		}
	};

	var posToCanvas = function (canvas, lat, lng) { // Anaximandre ans Mercator

		_east = calculateEastG(_north, _south, _west, canvas.width, canvas.height);
		adjustBoundaries();

		var x, y;

		var gAmpl;
		for (gAmpl = _east - _west; gAmpl < 0; gAmpl += 360);
		var graph2chartRatio = canvas.width / gAmpl;
		var _lng = lng;
		if (Math.abs(_west) > 180 && Math.sign(_lng) != Math.sign(_west) && Math.sign(_lng) > 0) {
			_lng -= 360;
		}
		if (gAmpl > 180 && _lng < 0 && _west > 0) {
			_lng += 360;
		}
		if (gAmpl > 180 && _lng >= 0 && _west > 0 && _lng < _east) {
			_lng += (_west + (gAmpl - _east));
		}

		var incSouth = 0, incLat = 0;

		switch (projection) {
			case undefined:
			case projections.anaximandre.type:
		//	x = (180 + lng) * (canvas.width / 360);
				x = ((_lng - _west) * graph2chartRatio);
				incSouth = _south;
				incLat = lat;
		//	y = canvas.height - ((lat + 90) * canvas.height / 180);
				y = canvas.height - ((incLat - incSouth) * (canvas.height / (_north - _south)));
				break;
			case projections.mercator.type:
				// Requires _north, _south, _east, _west
				x = ((_lng - _west) * graph2chartRatio);
				incSouth = getIncLat(_south);
				incLat = getIncLat(lat);
				y = canvas.height - ((incLat - incSouth) * graph2chartRatio);
				break;
		}

		return {"x": x, "y": y};
	};

	var sign = function (d) {
		var s = 0;
		if (d > 0.0) {
			s = 1;
		}
		if (d < 0.0) {
			s = -1;
		}
		return s;
	};

	var toRadians = function (deg) {
		return deg * (Math.PI / 180);
	};

	var toDegrees = function (rad) {
		return rad * (180 / Math.PI);
	};
};

