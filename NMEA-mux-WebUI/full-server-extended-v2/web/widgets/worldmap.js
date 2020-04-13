/**
 *
 * @author Olivier Le Diouris
 */

const projections = {
	anaximandre: {
		type: "ANAXIMANDRE"
	},
	mercator: {
		type: "MERCATOR"
	},
	globe: {
		type: "GLOBE"
	}
};
const tropicLat = 23.43698;

/**
 *
 * See custom properties in CSS.
 * =============================
 * @see https://developer.mozilla.org/en-US/docs/Web/CSS/--*
 * Relies on a rule named .worldmapdisplay, like that:
 *
 .worldmapdisplay {
    --canvasBackground: "rgba(0, 0, 100, 1.0)";
    --defaultPlotPointColor: "red";
    --travelColor: "gray";
    --arrowBodyColor: 'rgba(255, 255, 255, 0.5)';
    --globeBackground: "black";
    --globeGradientFrom: "navy";
    --globeGradientTo: "blue";
    --gridColor: 'rgba(0, 255, 255, 0.3)';
    --tropicColor: 'LightGray';
    --chartColor: 'cyan';
    --userPosColor: 'red';
    --sunColor: 'yellow';
    --sunArrowColor: 'rgba(255, 255, 0, 0.5)';
    --moonColor: 'white';
    --moonArrowColor: 'rgba(255, 255, 255, 0.5)';
    --ariesColor: 'LightGray';
    --venusColor: "orange";
    --marsColor: "red";
    --jupiterColor: "LightPink";
    --saturnColor: "LightYellow";
    --starsColor: "white";
    --nightColor: 'rgba(192, 192, 192, 0.3)';
    --displayPositionColor: 'white';
}
*/

var getWorldmapColorConfig = function() {
	var colorConfig = defaultWorldmapColorConfig;
	for (var s=0; s<document.styleSheets.length; s++) {
		try {
			for (var r = 0; document.styleSheets[s].cssRules !== null && r < document.styleSheets[s].cssRules.length; r++) {
				if (document.styleSheets[s].cssRules[r].selectorText === '.worldmapdisplay') {
					var cssText = document.styleSheets[s].cssRules[r].style.cssText;
					var cssTextElems = cssText.split(";");
					cssTextElems.forEach(function (elem) {
						if (elem.trim().length > 0) {
							var keyValPair = elem.split(":");
							var key = keyValPair[0].trim();
							var value = keyValPair[1].trim();
							switch (key) {
								case '--canvasBackground':
									colorConfig.canvasBackground = value;
									break;
								case '--defaultPlotPointColor':
									colorConfig.defaultPlotPointColor = value;
									break;
								case '--travelColor':
									colorConfig.travelColor = value;
									break;
								case '--arrowBodyColor':
									colorConfig.arrowBodyColor = value;
									break;
								case '--globeBackground':
									colorConfig.globeBackground = value;
									break;
								case '--globeGradientFrom':
									colorConfig.globeGradientFrom = value;
									break;
								case '--globeGradientTo':
									colorConfig.globeGradientTo = value;
									break;
								case '--gridColor':
									colorConfig.gridColor = value;
									break;
								case '--tropicColor':
									colorConfig.tropicColor = value;
									break;
								case '--chartLineWidth':
									colorConfig.chartLineWidth = value;
									break;
								case '--chartColor':
									colorConfig.chartColor = value;
									break;
								case '--userPosColor':
									colorConfig.userPosColor = value;
									break;
								case '--sunColor':
									colorConfig.sunColor = value;
									break;
								case '--sunArrowColor':
									colorConfig.sunArrowColor = value;
									break;
								case '--moonArrowColor':
									colorConfig.moonArrowColor = value;
									break;
								case '--ariesColor':
									colorConfig.ariesColor = value;
									break;
								case '--venusColor':
									colorConfig.venusColor = value;
									break;
								case '--marsColor':
									colorConfig.marsColor = value;
									break;
								case '--jupiterColor':
									colorConfig.jupiterColor = value;
									break;
								case '--saturnColor':
									colorConfig.saturnColor = value;
									break;
								case '--starsColor':
									colorConfig.starsColor = value;
									break;
								case '--nightColor':
									colorConfig.nightColor = value;
									break;
								case '--displayPositionColor':
									colorConfig.displayPositionColor = value;
									break;
								default:
									break;
							}
						}
					});
				}
			}
		} catch (err) {
			// Absorb?
		}
	}
	return colorConfig;
};

var defaultWorldmapColorConfig = {
  canvasBackground: "rgba(0, 0, 100, 1.0)",
  defaultPlotPointColor: "red",
  travelColor: "gray",
  arrowBodyColor: 'rgba(255, 255, 255, 0.5)',
  globeBackground: "black",
  globeGradientFrom: "navy",
  globeGradientTo: "blue",
  gridColor: 'rgba(0, 255, 255, 0.3)',
  tropicColor: 'LightGray',
  chartColor: 'cyan',
	chartLineWidth: 1,
  userPosColor: 'red',
  sunColor: 'yellow',
  sunArrowColor: 'rgba(255, 255, 0, 0.5)',
  moonColor: 'white',
  moonArrowColor: 'rgba(255, 255, 255, 0.5)',
  ariesColor: 'LightGray',
  venusColor: "orange",
  marsColor: "red",
  jupiterColor: "LightPink",
  saturnColor: "LightYellow",
  starsColor: "white",
  nightColor: 'rgba(192, 192, 192, 0.3)',
  displayPositionColor: 'white'
};

var worldmapColorConfig = defaultWorldmapColorConfig;

/**
 * @param cName
 * @param prj
 * @constructor
 */
function WorldMap (cName, prj) {

	worldmapColorConfig = getWorldmapColorConfig();

	var _west = -180,
			_east = 180,
			_north = 90,
			_south = -90;

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

	var posLabel = "Your position";

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
		posLabel = str;
	};

	var mouseMoveCallback;
	var mouseClickCallback;

	/*
	 func is a function taking an object as parameter, like:
	    {
	      x: x,      // Mouse abscissa on the canvas
	      y: y,      // Mouse ordinate on the canvas
	      lat: lat,  // latitude of the mouse on the chart
	      lng: lng   // longitude of the mouse on the chart
	    }
	 */
	this.setMouseMoveCallback = function(func) {
		mouseMoveCallback = func;
	};
	this.setMouseClickCallback = function(func) {
		mouseClickCallback = func;
	};

	// 2 custom functions (callbacks)
	var doBeforeDrawing, doAfterDrawing;

	/**
	 *
	 * @param before a function, taking canvas and context as parameters
	 */
	this.setBeforeDrawing = function(before) {
		doBeforeDrawing = before;
	};
	/**
	 *
	 * @param after a function, taking canvas and context as parameters
	 */
	this.setAfterDrawing = function(after) {
		doAfterDrawing = after;
	};

	var projectionSupported = function(value) {
		for  (var name in projections) {
			if (projections[name].type === value) {
				return true;
			}
		}
		return false;
	};

	var projection;

	this.setProjection = function(prj) {
		if (!projectionSupported(prj)) {
			throw { "err": "Projection [" + prj + "] not supported" };
		}
		projection = prj;
	};

	this.setProjection(prj);

	this.clear = function () {
		var canvas = document.getElementById(canvasName);
		if (canvas !== undefined) {
			var context = canvas.getContext('2d');
			// Cleanup
			context.fillStyle = worldmapColorConfig.canvasBackground;
			context.fillRect(0, 0, canvas.width, canvas.height);
		}
	};

	var fromPt, toPt;
	var animationID;

	/**
	 * Listens to clicks and mousemove on canvas.
	 */
	var addCanvasListener = function () {
		var canvas = document.getElementById(canvasName);
		var self = this;
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
						self.clear();
						self.drawWorldMap();
						self.plotPoint(canvasName, fromPt, worldmapColorConfig.defaultPlotPointColor);
					}
					if (fromPt === undefined) {
						fromPt = {"x": xClick, "y": yClick};
						self.plotPoint(canvasName, fromPt, worldmapColorConfig.defaultPlotPointColor);
					} else if (toPt === undefined) {
						toPt = {"x": xClick, "y": yClick};
						self.plotPoint(canvasName, toPt, worldmapColorConfig.defaultPlotPointColor);
						currentStep = 0;
						animationID = window.setInterval(function () {
							self.travel(canvasName, fromPt, toPt, 10);
						}, 100);
					}
				}, false);
	};

	var canvas = document.getElementById(canvasName);
	// var context = canvas.getContext('2d');
	// var self = this;

	canvas.addEventListener('mousemove', function(evt) {

		if (mouseMoveCallback !== undefined) {
			var rect = canvas.getBoundingClientRect();
			var x = Math.round(evt.clientX - rect.left);
			var y = Math.round(evt.clientY - rect.top);

//    console.log("Mouse: x=" + x + ", y=" + y);
			var pos = pointToPos(x, y);
			mouseMoveCallback({
				x: x,
				y: y,
				lat: pos.lat,
				lng: pos.lng
			});
		}
	}, 0);

	canvas.addEventListener('click', function(evt) {

		if (mouseClickCallback !== undefined) {
			var rect = canvas.getBoundingClientRect();
			var x = Math.round(evt.clientX - rect.left);
			var y = Math.round(evt.clientY - rect.top);

//    console.log("Mouse: x=" + x + ", y=" + y);
			var pos = pointToPos(x, y);
			mouseClickCallback({
				x: x,
				y: y,
				lat: pos.lat,
				lng: pos.lng
			});
		}
	}, 0);

	var pointToPos = function(x, y) {
		var gp = {};
		var l = 0.0;
		var g = 0.0;
		adjustBoundaries();
		if (_north != _south && _east != _west) {
			var gAmpl; // = Math.abs(_east - _west);
			for (gAmpl = _east - _west; gAmpl < 0; gAmpl += 360);
			var lAmpl = 0.0;
			switch (projection) {
				case projections.anaximandre.type:
					lAmpl = Math.abs(_north - _south);
					break;
				case projections.mercator.type:
					lAmpl = Math.abs(getIncLat(_north) - getIncLat(_south));
					break;
			}
			var graph2chartRatio = canvas.width / gAmpl;
			switch (projection) {
				default:
				case projections.globe.type:
					break;
				case projections.anaximandre.type:
				case projections.mercator.type:
					g = x / graph2chartRatio + _west;
					if (g < -180) {
						g += 360;
					}
					if (g > 180) {
						g -= 360;
					}
					break;
			}
			var incSouth = 0.0;
			switch (projection) {
				case projections.anaximandre.type:
					incSouth = _south;
					break;
				case projections.mercator.type:
					incSouth = getIncLat(_south);
					break;
			}
			var incLat =(canvas.height - y) / graph2chartRatio + incSouth;
			l = 0.0;
			switch (projection)
			{
				default:
					// return null
					break;
				case projections.anaximandre.type:
					incLat = (canvas.height - y) / (canvas.height / lAmpl) + incSouth;
					l = incLat;
					break;
				case projections.mercator.type:
					l = getInvIncLat(incLat);
					break;
			}
			gp = { lat: l, lng: g };
		}
		return gp;
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
	//  TODO Make sure that works
	this.travel = function (canvasName, from, to, nbStep) {
		var newX = from.x + (currentStep * (to.x - from.x) / nbStep);
		var newY = from.y + (currentStep * (to.y - from.y) / nbStep);
		plotPoint(canvasName, {"x": newX, "y": newY}, worldmapColorConfig.travelColor);
		currentStep++;
		if (currentStep > nbStep) {
			window.clearInterval(animationID);
		}
	};

	/*
	 * The right tilt is:
	 *   chartPanel.setGlobeViewRightLeftRotation(-(sunD * Math.sin(Math.toRadians(lhaSun))));
	 * An astro resource exists: GET /astro/positions-in-the-sky?at=2017-10-11T15:23:28
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

	this.getCanvasLocation = function (canvas, lat, lng) {
		var pt = posToCanvas(canvas, lat, lng); // TODO Manage projection
		return pt;
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
		_east = e;
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
		if (sign(_east) !== sign(_west) && sign(_east) === -1) {
			_west -= 360;
		}
	};

	/**
	 * For the Globe projection
	 *
	 * @param lat in degrees
	 * @param lng in degrees
	 */
	var getPanelPoint = function (lat, lng) {
		var pt = {};
		adjustBoundaries();
		if (_north !== _south && _east !== _west) {
			var gAmpl = _east - _west;
			while (gAmpl < 0) {
				gAmpl += 360;
			}
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
	};

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
			context.strokeStyle = worldmapColorConfig.arrowBodyColor;
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

	var drawGlobe = function (canvas, context, before) {
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

		// Black background.
		context.fillStyle = worldmapColorConfig.globeBackground;
		context.fillRect(0, 0, canvas.width, canvas.height);

		if (before !== undefined) {
			before(canvas, context);
		}

		// Circle
		var radius = Math.min(w / 2, h / 2) * defaultRadiusRatio;
		var grd = context.createRadialGradient(canvas.width / 2, canvas.height / 2, radius, 90, 60, radius);
		grd.addColorStop(0, worldmapColorConfig.globeGradientFrom);
		grd.addColorStop(1, worldmapColorConfig.globeGradientTo);

		context.fillStyle = grd; // "rgba(0, 0, 100, 1.0)"; // Dark blue

		context.arc(canvas.width / 2, canvas.height / 2, radius, 0, 2 * Math.PI);
		context.fill();

		globeViewOffset_X = Math.abs((globeView_ratio * opWidth) - w) / 2 - (globeView_ratio * minX);
		globeViewOffset_Y = Math.abs((globeView_ratio * opHeight) - h) / 2 - (globeView_ratio * minY);

		var gstep = 10; //Math.abs(_east - _west) / 60;
		var lstep = 10;  //Math.abs(_north - _south) / 10;

		context.lineWidth = 1;
		context.strokeStyle = worldmapColorConfig.gridColor; // 'cyan';

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
			context.fillStyle = worldmapColorConfig.tropicColor;
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
								previousPt = null; // Something better, maybe ?
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
					if (false && firstPt !== null && previousPt !== null) {
						context.lineTo(firstPt.x, firstPt.y); // close the loop
					}
					context.lineWidth = worldmapColorConfig.chartLineWidth;
					context.strokeStyle = worldmapColorConfig.chartColor;
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
			plot(context, userPos, worldmapColorConfig.userPosColor);
			context.fillStyle = worldmapColorConfig.userPosColor;
			context.fillText(posLabel, Math.round(userPos.x) + 3, Math.round(userPos.y) - 3);
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
					plot(context, sun, worldmapColorConfig.sunColor);
					context.fillStyle = worldmapColorConfig.sunColor;
					context.fillText("Sun", Math.round(sun.x) + 3, Math.round(sun.y) - 3);
					// Arrow, to the sun
					context.setLineDash([2]);
					context.strokeStyle = worldmapColorConfig.sunArrowColor;
					context.beginPath();
					context.moveTo(userPos.x, userPos.y);
					context.lineTo(sun.x, sun.y);
					context.stroke();
					context.closePath();
					context.setLineDash([0]); // Reset
					context.strokeStyle = worldmapColorConfig.sunColor;
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
						fillCircle(context, { x: sun.x + deltaX, y: sun.y + deltaY}, 6, worldmapColorConfig.sunColor);
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
					plot(context, moon, worldmapColorConfig.moonColor);
					context.fillStyle = worldmapColorConfig.moonColor;
					context.fillText("Moon", Math.round(moon.x) + 3, Math.round(moon.y) - 3);
					// Arrow, to the moon
					context.setLineDash([2]);
					context.strokeStyle = worldmapColorConfig.moonArrowColor;
					context.beginPath();
					context.moveTo(userPos.x, userPos.y);
					context.lineTo(moon.x, moon.y);
					context.stroke();
					context.closePath();
					context.setLineDash([0]); // Reset
					context.strokeStyle = worldmapColorConfig.moonColor;
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
						fillCircle(context, { x: moon.x + deltaX, y: moon.y + deltaY}, 5, worldmapColorConfig.moonColor);
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
			    positionBody(context, userPos, worldmapColorConfig.ariesColor, "Aries", 0, aries.gha, false);
			    positionBody(context, userPos, worldmapColorConfig.ariesColor, "Anti-Aries", 0, aries.gha + 180, false); // Libra?
			  }
			  // 2 - Other planets
			  var venus = findInList(astronomicalData.wanderingBodies, "name", "venus");
			  var mars = findInList(astronomicalData.wanderingBodies, "name", "mars");
			  var jupiter = findInList(astronomicalData.wanderingBodies, "name", "jupiter");
			  var saturn = findInList(astronomicalData.wanderingBodies, "name", "saturn");
			  if (venus !== null) {
			    positionBody(context, userPos, worldmapColorConfig.venusColor, "Venus", venus.decl, venus.gha);
			  }
			  if (mars !== null) {
			    positionBody(context, userPos, worldmapColorConfig.marsColor, "Mars", mars.decl, mars.gha);
			  }
			  if (jupiter !== null) {
			    positionBody(context, userPos, worldmapColorConfig.jupiterColor, "Jupiter", jupiter.decl, jupiter.gha);
			  }
			  if (saturn !== null) {
			    positionBody(context, userPos, worldmapColorConfig.saturnColor, "Saturn", saturn.decl, saturn.gha);
			  }
			}

			if (astronomicalData.stars !== undefined && withStars) {
				astronomicalData.stars.forEach(function(star, idx) {
					positionBody(context, userPos, worldmapColorConfig.starsColor, star.name, star.decl, star.gha, false, true);
				});
			}
		}
	};

	var drawFlatGrid = function(canvas, context) {
		context.lineWidth = 1;
		context.strokeStyle = worldmapColorConfig.gridColor; // 'cyan';

		var gstep = 10; //Math.abs(_east - _west) / 60;
		var lstep = 10;  //Math.abs(_north - _south) / 10;

		// Parallels
		for (var lat=-80; lat<=80; lat+=lstep) {
			var y = posToCanvas(canvas, lat, 0).y;
			context.beginPath();

			context.moveTo(0, y);
			context.lineTo(canvas.width, y);

			context.stroke();
			context.closePath();
		}
		// Meridians
		for (var lng=-180; lng<180; lng+=gstep) {
			var x = posToCanvas(canvas, 0, lng).x;
			context.beginPath();

			context.moveTo(x, 0);
			context.lineTo(x, canvas.height);

			context.stroke();
			context.closePath();
		}
	};

	var drawFlatTropics = function(canvas, context) {
		context.lineWidth = 1;
		context.strokeStyle = worldmapColorConfig.tropicColor;
		// Cancer
		var y = posToCanvas(canvas, tropicLat, 0).y;
		context.beginPath();
		context.moveTo(0, y);
		context.lineTo(canvas.width, y);
		context.stroke();
		context.closePath();
		// Capricorn
		y = posToCanvas(canvas, -tropicLat, 0).y;
		context.beginPath();
		context.moveTo(0, y);
		context.lineTo(canvas.width, y);
		context.stroke();
		context.closePath();
		// North polar circle
		y = posToCanvas(canvas, 90-tropicLat, 0).y;
		context.beginPath();
		context.moveTo(0, y);
		context.lineTo(canvas.width, y);
		context.stroke();
		context.closePath();
		// South polar circle
		y = posToCanvas(canvas, -90+tropicLat, 0).y;
		context.beginPath();
		context.moveTo(0, y);
		context.lineTo(canvas.width, y);
		context.stroke();
		context.closePath();
	};

	var drawFlatCelestialOptions = function(canvas, context) {
		if (astronomicalData !== {}) {
			if (astronomicalData.sun !== undefined && withSun) {
				context.save();
				var sunLng = haToLongitude(astronomicalData.sun.gha);
				plotPosToCanvas(astronomicalData.sun.decl, sunLng, "Sun", worldmapColorConfig.sunColor);

				if (withSunlight) {
					var from = {lat: toRadians(astronomicalData.sun.decl), lng: toRadians(sunLng)};
					drawFlatNight(canvas, context, from, userPosition, astronomicalData.sun.gha);
				}
				context.restore();
			}
			if (astronomicalData.moon !== undefined && withMoon) {
				context.save();
				var moonLng = haToLongitude(astronomicalData.moon.gha);
				plotPosToCanvas(astronomicalData.moon.decl, moonLng, "Moon", worldmapColorConfig.moonColor);
				if (withMoonlight) {
					var from = {lat: toRadians(astronomicalData.moon.decl), lng: toRadians(moonLng)};
					drawFlatNight(canvas, context, from, userPosition, astronomicalData.moon.gha);
				}
				context.restore();
			}
			if (astronomicalData.wanderingBodies !== undefined && withWanderingBodies) {
				// 1 - Ecliptic
				var aries = findInList(astronomicalData.wanderingBodies, "name", "aries");
				if (aries !== null) {
					// 1 - Draw Ecliptic
					var longitude = (aries.gha < 180) ? -aries.gha : 360 - aries.gha;
					longitude += 90; // Extremum
					while (longitude > 360) {
						longitude -= 360;
					}
					var ariesRad = { lat: toRadians(astronomicalData.eclipticObliquity), lng: toRadians(longitude) };
					var eclCenter = deadReckoning(ariesRad, 90 * 60, 0); // "Center" of the Ecliptic

					context.fillStyle = worldmapColorConfig.tropicColor;
					for (var hdg=0; hdg<360; hdg++) {
						var pt = deadReckoning(eclCenter, 90 * 60, hdg);
						var pp = posToCanvas(canvas, toDegrees(pt.lat), toRealLng(toDegrees(pt.lng)));
						context.fillRect(pp.x, pp.y, 1, 1);
					}

					plotPosToCanvas(0, haToLongitude(aries.gha), "Aries", worldmapColorConfig.ariesColor);
					plotPosToCanvas(0, haToLongitude(aries.gha + 180), "Anti-Aries", worldmapColorConfig.ariesColor);
				}
				// 2 - Other planets
				var venus = findInList(astronomicalData.wanderingBodies, "name", "venus");
				var mars = findInList(astronomicalData.wanderingBodies, "name", "mars");
				var jupiter = findInList(astronomicalData.wanderingBodies, "name", "jupiter");
				var saturn = findInList(astronomicalData.wanderingBodies, "name", "saturn");
				if (venus !== null) {
					plotPosToCanvas(venus.decl, haToLongitude(venus.gha), "Venus", worldmapColorConfig.venusColor);
				}
				if (mars !== null) {
					plotPosToCanvas(mars.decl, haToLongitude(mars.gha), "Mars", worldmapColorConfig.marsColor);
				}
				if (jupiter !== null) {
					plotPosToCanvas(jupiter.decl, haToLongitude(jupiter.gha), "Jupiter", worldmapColorConfig.jupiterColor);
				}
				if (saturn !== null) {
					plotPosToCanvas(saturn.decl, haToLongitude(saturn.gha), "Saturn", worldmapColorConfig.saturnColor);
				}
			}

			if (astronomicalData.stars !== undefined && withStars) {
				astronomicalData.stars.forEach(function(star, idx) {
					plotPosToCanvas(star.decl, haToLongitude(star.gha), star.name, worldmapColorConfig.starsColor);
				});
			}
		}
	};

	var drawMercatorChart = function (canvas, context) {

		if (withGrid) {
			drawFlatGrid(canvas, context);
		}

		if (withTropics) {
			drawFlatTropics(canvas, context);
		}

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
			if (firstPt !== null && Math.abs(previousPt.x - firstPt.x) < (canvas.width / 20) && Math.abs(previousPt.y - firstPt.y) < (canvas.height / 20)) {
				context.lineTo(firstPt.x, firstPt.y); // close the loop
			}
			context.lineWidth = worldmapColorConfig.chartLineWidth;
			context.strokeStyle = worldmapColorConfig.chartColor; // 'black';
			context.stroke();
			// context.fillStyle = "goldenrod";
			// context.fill();
			context.closePath();
		}
		// User position
		if (userPosition !== {}) {
			plotPosToCanvas(userPosition.latitude, userPosition.longitude, posLabel, worldmapColorConfig.userPosColor);
		}

		drawFlatCelestialOptions(canvas, context);
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

		context.fillStyle = worldmapColorConfig.tropicColor;
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
		context.fillStyle = worldmapColorConfig.nightColor;

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

	var toRealLng = function(lng) {
		var g = lng;
		while (g > 180) {
			g -= 360;
		}
		while (g < -180) {
			g += 360;
		}
		return g;
	};

	var drawFlatNight = function(canvas, context, from, user, gha) {
		const NINETY_DEGREES = 90 * 60; // in nm

		// context.lineWidth = 1;
		context.fillStyle = worldmapColorConfig.nightColor;

		var nightRim = [];
		// Calculate the night rim
		for (var i=0; i<360; i++) {
			var night = deadReckoning(from, NINETY_DEGREES, i);
			nightRim.push(night);
		}

		// Night limb
		// Find the first point (west) of the rim
		var first = 0;
		for (var x=0; x<nightRim.length; x++) {
			var lng = toRealLng(toDegrees(nightRim[x].lng));
//		console.log("Night lng: " + lng);
			if (lng > _west) {
				first = Math.max(0, x - 1);
				break;
			}
		}
		context.beginPath();
		var pt = posToCanvas(canvas, toDegrees(nightRim[first].lat), toRealLng(toDegrees(nightRim[first].lng)));
		context.moveTo(-10 /*pt.x*/, pt.y); // left++

		var go = true;

//	console.log("_west ", _west, "first", first);

		for (var idx=first; idx<360 && go === true; idx++) {
			pt = posToCanvas(canvas, toDegrees(nightRim[idx].lat), toRealLng(toDegrees(nightRim[idx].lng)));
			context.lineTo(pt.x, pt.y);

			// DEBUG
			// if (idx % 20 === 0) {
			// 	context.fillStyle = 'cyan';
			// 	context.fillText(idx, pt.x, pt.y);
			// }

	//  if (toRealLng(toDegrees(nightRim[idx].lng)) > _east) {
	// 	 go = false;
	//  }
		}
		if (go) {
			for (var idx=0; idx<360 && go === true; idx++) {
				if (toRealLng(toDegrees(nightRim[idx].lng)) > _east) {
					go = false;
				} else {
					pt = posToCanvas(canvas, toDegrees(nightRim[idx].lat), toRealLng(toDegrees(nightRim[idx].lng)));
					context.lineTo(pt.x, pt.y);
					// DEBUG
					// if (idx % 20 === 0) {
					// 	context.fillStyle = 'red';
					// 	context.fillText(idx, pt.x, pt.y);
					// }
				}
			}
		}
		context.lineTo(canvas.width + 10, pt.y); // right most

		// DEBUG
		// context.fillStyle = 'red';
		// context.fillText('Last', pt.x - 10, pt.y);

		if (from.lat > 0) { // N Decl, night is south
			context.lineTo(canvas.width, canvas.height); // bottom right
			context.lineTo(0, canvas.height);            // bottom left
		} else {            // S Decl, night is north
			context.lineTo(canvas.width, 0);             // top right
			context.lineTo(0, 0);                        // top left
		}
//	context.lineTo(firstPt.x, firstPt.y);
		context.fillStyle = worldmapColorConfig.nightColor;
		context.closePath();
		context.fill();
	};

	var drawAnaximandreChart = function (canvas, context) {
		// Square projection, Anaximandre.
		if (withGrid) {
			drawFlatGrid(canvas, context);
		}

		if (withTropics) {
			drawFlatTropics(canvas, context);
		}
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
			plotPosToCanvas(userPosition.latitude, userPosition.longitude, posLabel, worldmapColorConfig.userPosColor);
		}
		drawFlatCelestialOptions(canvas, context);
	};

	this.beforeDrawingWorldMap = function(canvas, context) {
	};

	this.afterDrawingWorldMap = function(canvas, context) {
	};

	/**
	 * Accessible from outside.
	 *
	 * @param clear
	 */
	this.drawWorldMap = function (clear) {
		if (clear === undefined) {
			clear = true;
		}
		//var start = new Date().getTime();

		var canvas = document.getElementById(canvasName);
		var context = canvas.getContext('2d');

		if (fullWorldMap === undefined) {
			console.log("You must load [WorldMapData.js] to display a chart.");
		} else {
			if (clear) {
				this.clear();
			}
			// Before?
			if (doBeforeDrawing !== undefined) {
				doBeforeDrawing(canvas, context);
			}
			try {
				switch (projection) {
					case undefined:
					case projections.anaximandre.type:
						drawAnaximandreChart(canvas, context);
						break;
					case projections.globe.type:
						drawGlobe(canvas, context, doBeforeDrawing); // TODO 3rd prm...
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
			context.fillStyle = worldmapColorConfig.displayPositionColor;
			context.font = "bold 16px Arial"; // "bold 40px Arial"
			context.fillText(strLat, 10, 18);
			context.fillText(strLng, 10, 38);
		}

		if (astronomicalData !== undefined && astronomicalData.deltaT !== undefined) {
			context.fillStyle = worldmapColorConfig.displayPositionColor;
			context.font = "12px Arial"; // "bold 40px Arial"
			var deltaT = "\u0394T=" + astronomicalData.deltaT + " s";
			context.fillText(deltaT, 10, canvas.height - 5);
		}

		//var end = new Date().getTime();
		//console.log("Operation completed in " + (end - start) + " ms.");
		// After?
		if (doAfterDrawing !== undefined) {
			doAfterDrawing(canvas, context);
		}
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
		plotPoint(canvasName, pt, (color !== undefined ? color : worldmapColorConfig.defaultPlotPointColor));
		if (label !== undefined) {
			try {
				var context = canvas.getContext('2d');
				// BG
				var metrics = context.measureText(label);
				var xLabel = Math.round(pt.x) + 3;
				var yLabel = Math.round(pt.y) - 3;

				// context.fillStyle = 'yellow'; // worldmapColorConfig.canvasBackground;
				// context.fillRect( xLabel, yLabel - 14, metrics.width, 14);
				// Text
				context.fillStyle = (color !== undefined ? color : worldmapColorConfig.defaultPlotPointColor);
				context.fillText(label, xLabel, yLabel);
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

	var posToCanvas = function (canvas, lat, lng) { // Anaximandre and Mercator

		_east = calculateEastG(_north, _south, _west, canvas.width, canvas.height);
		adjustBoundaries();

		var x, y;

		var gAmpl = _east - _west;
		while (gAmpl < 0) {
			gAmpl += 360;
		}
		var graph2chartRatio = canvas.width / gAmpl;
		var _lng = lng;
		if (Math.abs(_west) > 180 && Math.sign(_lng) !== Math.sign(_west) && Math.sign(_lng) > 0) {
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
		} else if (d < 0.0) {
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

