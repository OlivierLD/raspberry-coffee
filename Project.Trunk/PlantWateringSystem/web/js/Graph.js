/*
 * @author Olivier Le Diouris
 */
/*
 * See custom properties in CSS.
 * =============================
 * @see https://developer.mozilla.org/en-US/docs/Web/CSS/--*
 * Relies on a rule named .graphdisplay, like that:
 *
 .graphdisplay {

 --tooltip-color: rgba(250, 250, 210, .7);
 --tooltip-text-color: black;
 --with-bg-gradient: true;
 --bg-gradient-from: rgba(0,0,0,0);
 --bg-gradient-to: cyan;

 --bg-color: LightGray;

 --horizontal-grid-color: gray;
 --horizontal-grid-text-color: black;
 --vertical-grid-color: gray;
 --vertical-grid-text-color: black;

 --raw-data-line-color: green;
 --fill-raw-data: true;
 --raw-data-fill-color: rgba(0, 255, 0, 0.35);

 --smooth-data-line-color: red;
 --fill-smooth-data: true;
 --smooth-data-fill-color: rgba(0, 255, 0, 0.35);

 --clicked-index-color: orange;

 --font: Arial;
 }
 */

/**
 * Recurse from the top down, on styleSheets and cssRules
 *
 * document.styleSheets[0].cssRules[2].selectorText returns ".analogdisplay"
 * document.styleSheets[0].cssRules[2].cssText returns ".analogdisplay { --hand-color: red;  --face-color: white; }"
 * document.styleSheets[0].cssRules[2].style.cssText returns "--hand-color: red; --face-color: white;"
 */
var getColorConfig = function () {
	var colorConfig = defaultGraphColorConfig;
	for (var s = 0; s < document.styleSheets.length; s++) {
		try {
			for (var r = 0; document.styleSheets[s].cssRules !== null && r < document.styleSheets[s].cssRules.length; r++) {
				if (document.styleSheets[s].cssRules[r].selectorText === '.graphdisplay') {
					var cssText = document.styleSheets[s].cssRules[r].style.cssText;
					var cssTextElems = cssText.split(";");
					cssTextElems.forEach(function (elem) {
						if (elem.trim().length > 0) {
							var keyValPair = elem.split(":");
							var key = keyValPair[0].trim();
							var value = keyValPair[1].trim();
							switch (key) {
								case '--tooltip-color':
									colorConfig.tooltipColor = value;
									break;
								case '--tooltip-text-color':
									colorConfig.tooltipTextColor = value;
									break;
								case '--with-bg-gradient':
									colorConfig.withBGGradient = (value === 'true');
									break;
								case '--bg-gradient-from':
									colorConfig.bgGradientFrom = value;
									break;
								case '--bg-gradient-to':
									colorConfig.bgGradientTo = value;
									break;
								case '--bg-color':
									colorConfig.bgColorNoGradient = value;
									break;
								case '--horizontal-grid-color':
									colorConfig.horizontalGridColor = value;
									break;
								case '--horizontal-grid-text-color':
									colorConfig.horizontalGridTextColor = value;
									break;
								case '--vertical-grid-color':
									colorConfig.verticalGridColor = value;
									break;
								case '--vertical-grid-text-color':
									colorConfig.verticalGridTextColor = value;
									break;
								case '--raw-data-line-color':
									colorConfig.rawDataLineColor = value;
									break;
								case '--fill-raw-data':
									colorConfig.fillRawData = (value === 'true');
									break;
								case '--raw-data-fill-color':
									colorConfig.rawDataFillColor = value;
									break;
								case '--smooth-data-line-color':
									colorConfig.smoothDataLineColor = value;
									break;
								case '--fill-smooth-data':
									colorConfig.fillSmoothData = (value === 'true');
									break;
								case '--smooth-data-fill-color':
									colorConfig.smoothDataFillColor = value;
									break;
								case '--clicked-index-color':
									colorConfig.clickedIndexColor = value;
									break;
								case '--font':
									colorConfig.font = value;
									break;
								default:
									break;
							}
						}
					});
				}
			}
		} catch (err) {
			// absorb
		}
	}
	return colorConfig;
};

var defaultGraphColorConfig = {
	tooltipColor: "rgba(250, 250, 210, .7)",
	tooltipTextColor: "black",
	withBGGradient: false,
	bgGradientFrom: 'rgba(0,0,0,0)',
	bgGradientTo: 'cyan',
	bgColorNoGradient: "white",
	horizontalGridColor: "gray",
	horizontalGridTextColor: "black",
	verticalGridColor: "gray",
	verticalGridTextColor: "black",
	rawDataLineColor: "red",
	fillRawData: false,
	rawDataFillColor: "rgba(0, 255, 0, 0.15)",
	smoothDataLineColor: "red",
	fillSmoothData: true,
	smoothDataFillColor: "rgba(255, 0, 0, 0.35)",
	clickedIndexColor: 'orange',
	font: 'Arial'
};
var graphColorConfig = defaultGraphColorConfig;

function Graph(cName,       // Canvas Name
               graphData,   // x,y tuple array
               callback,    // Callback on mouseclick
               unit,        // Unit label, for display
               minMax) {    // { min: a, max: b }, to have static boundaries

	var instance = this;
	var staticMinimum, staticMaximum;

	if (minMax !== undefined) {
		if (minMax.min !== undefined) {
			staticMinimum = minMax.min;
		}
		if (minMax.max !== undefined) {
			staticMaximum = minMax.max;
		}
	}

// 	if (events) {
// 		events.subscribe('color-scheme-changed', function (val) {
// //    console.log('Color scheme changed:', val);
// 			reloadColorConfig();
// 		});
// 	}

	graphColorConfig = getColorConfig();

	var xScale, yScale;
	var minx, miny, maxx, maxy;
	var context;

	var unit = unit;
	var lastClicked;

	var withRawData = true;
	var withTooltip = false;
	var withSmoothing = false;

	this.setRawData = function (rd) {
		withRawData = rd;
	};
	this.setTooltip = function (tt) {
		withTooltip = tt;
	};
	this.setSmoothing = function (sm) {
		withSmoothing = sm;
	};

	var canvas = document.getElementById(cName);

	canvas.addEventListener('click', function (evt) {
		var x = evt.pageX - canvas.offsetLeft;
		var y = evt.pageY - canvas.offsetTop;

		var coords = relativeMouseCoords(evt, canvas);
		x = coords.x;
		y = coords.y;
//    console.log("Mouse: x=" + x + ", y=" + y);

		var idx = Math.round(x / xScale);
		if (idx < graphData.length) {
			if (callback !== undefined && callback !== null) {
				callback(idx);
			}
			lastClicked = idx;
		}
	}, 0);

	canvas.addEventListener('mousemove', function (evt) {
		//
	}, 0);

	var relativeMouseCoords = function (event, element) { // TODO See in worldmap.js how this is done
		var totalOffsetX = 0;
		var totalOffsetY = 0;
		var canvasX = 0;
		var canvasY = 0;
		var currentElement = element;

		do {
			totalOffsetX += currentElement.offsetLeft - currentElement.scrollLeft;
			totalOffsetY += currentElement.offsetTop - currentElement.scrollTop;
		} while (currentElement = currentElement.offsetParent)

		canvasX = event.pageX - totalOffsetX;
		canvasY = event.pageY - totalOffsetY;

		return {x: canvasX, y: canvasY};
	};

	this.minX = function (data) {
		var min = Number.MAX_VALUE;
		for (var i = 0; i < data.length; i++) {
			min = Math.min(min, data[i].getX());
		}
		return min;
	};

	this.minY = function (data) {
		var min = Number.MAX_VALUE;
		for (var i = 0; i < data.length; i++) {
			min = Math.min(min, data[i]);
		}
		return min;
	};

	this.maxX = function (data) {
		var max = Number.MIN_VALUE;
		for (var i = 0; i < data.length; i++) {
			max = Math.max(max, data[i].getX());
		}
		return max;
	};

	this.maxY = function (data) {
		var max = Number.MIN_VALUE;
		for (var i = 0; i < data.length; i++) {
			max = Math.max(max, data[i]);
		}
		return max;
	};

	this.getMinMax = function (data) {
		var mini = (staticMinimum !== undefined ? staticMinimum : Math.floor(this.minY(data)));
		var maxi = (staticMaximum !== undefined ? staticMaximum : Math.ceil(this.maxY(data)));

		if (Math.abs(maxi - mini) < 5) { // To have a significant Y scale.
			maxi += 3;
			if (mini > 0) {
				mini -= 1;
			} else {
				maxi += 1;
			}
		}
		return {mini: mini, maxi: maxi};
	};

	var reloadColor = false;
	var reloadColorConfig = function () {
//  console.log('Color scheme has changed');
		reloadColor = true;
	};

	/*
	 * threshold: {
	    "humidityThreshold": 70,
	    "wateringTime": 10,
	    "resumeWatchAfter": 120
		}
	 */
	this.drawGraph = function (displayCanvasName, data, idx, threshold) {

		if (reloadColor) {
			// In case the CSS has changed, dynamically.
			getColorConfig();
		}
		reloadColor = false;

		if (data.length < 2) {
			return;
		}

		context = canvas.getContext('2d');

		var width = context.canvas.clientWidth;
		var height = context.canvas.clientHeight;

		if (width === 0 || height === 0) { // Not visible
			return;
		}
		this.init(data);

		// Set the canvas size from its container.
		canvas.width = width;
		canvas.height = height;

		var _idxX;
		if (idx !== undefined) {
			_idxX = idx * xScale;
		}
		var _minY = miny;
		if (threshold !== undefined && threshold.humidityThreshold !== undefined) {
			_minY = Math.min(miny, threshold.humidityThreshold - 2);
			yScale = canvas.getContext('2d').canvas.clientHeight / (maxy - _minY);
		}

		document.getElementById(displayCanvasName).title = data.length + " elements, [" + _minY + ", " + maxy + "]";

		var gridXStep = Math.round(data.length / 10);
		var gridYStep = (maxy - _minY) < 5 ? 1 : Math.round((maxy - _minY) / 5);

		// Clear
		context.fillStyle = "white";
		context.fillRect(0, 0, width, height);

		if (graphColorConfig.withBGGradient === false) {
			context.fillStyle = graphColorConfig.bgColorNoGradient;
			context.fillRect(0, 0, width, height);
		} else {
			var grV = context.createLinearGradient(0, 0, 0, height);
			grV.addColorStop(0, graphColorConfig.bgGradientFrom);
			grV.addColorStop(1, graphColorConfig.bgGradientTo);

			context.fillStyle = grV;
			context.fillRect(0, 0, width, height);
		}
		// Horizontal grid (Data Unit)
		for (var i = Math.floor(_minY); gridYStep > 0 && i < maxy; i += gridYStep) {
			context.beginPath();
			context.lineWidth = 1;
			context.strokeStyle = graphColorConfig.horizontalGridColor;
			context.moveTo(0, height - (i - _minY) * yScale);
			context.lineTo(width, height - (i - _minY) * yScale);
			context.stroke();

			context.save();
			context.font = "bold 10px " + graphColorConfig.font;
			context.fillStyle = graphColorConfig.horizontalGridTextColor;
			var str = i.toString() + " " + unit;
			var len = context.measureText(str).width;
			context.fillText(str, width - (len + 2), height - ((i - _minY) * yScale) - 2);
			context.restore();
			context.closePath();
		}

		// Vertical grid (index)
		for (var i = gridXStep; i < data.length; i += gridXStep) {
			context.beginPath();
			context.lineWidth = 1;
			context.strokeStyle = graphColorConfig.verticalGridColor;
			context.moveTo(i * xScale, 0);
			context.lineTo(i * xScale, height);
			context.stroke();

			// Rotate the whole context, and then write on it (that's why we need the translate)
			context.save();
			context.translate(i * xScale, height);
			context.rotate(-Math.PI / 2);
			context.font = "bold 10px " + graphColorConfig.font;
			context.fillStyle = graphColorConfig.verticalGridTextColor;
			var str = i.toString();
			var len = context.measureText(str).width;
			context.fillText(str, 2, -1); //i * xScale, cHeight - (len));
			context.restore();
			context.closePath();
		}

		if (threshold !== undefined) {
			if (threshold.humidityThreshold !== undefined) {
				humThreshold = threshold.humidityThreshold;
				context.beginPath();
				context.lineWidth = 3;
				context.strokeStyle = 'blue';
				context.moveTo(0, height - (humThreshold - _minY) * yScale);
				context.lineTo(width, height - (humThreshold - _minY) * yScale);
				context.stroke();
				context.closePath();
			}
		}

		// Data
		if (withRawData && data.length > 0) {
			context.beginPath();
			context.lineWidth = 3;
			context.strokeStyle = graphColorConfig.rawDataLineColor;

			var previousPoint = data[0];
			context.moveTo((0 - minx) * xScale, height - (data[0] - _minY) * yScale);
			for (var i = 1; i < data.length; i++) {
				//  context.moveTo((previousPoint.getX() - minx) * xScale, cHeight - (previousPoint - _minY) * yScale);
				context.lineTo((i - minx) * xScale, height - (data[i] - _minY) * yScale);
				//  context.stroke();
				previousPoint = data[i];
			}
			if (graphColorConfig.fillRawData === true) {
				context.lineTo(width, height);
				context.lineTo(0, height);
				context.closePath();
			}
			context.stroke();
			if (graphColorConfig.fillRawData === true) {
				context.fillStyle = graphColorConfig.rawDataFillColor;
				context.fill();
			}
		}

		if (idx !== undefined) {
			context.beginPath();
			context.lineWidth = 1;
			context.strokeStyle = graphColorConfig.clickedIndexColor;
			context.moveTo(_idxX, 0);
			context.lineTo(_idxX, height);
			context.stroke();
			context.closePath();
		}
	};

	this.init = function (dataArray) {
		if (dataArray.length > 0) {
			var minMax = this.getMinMax(dataArray);
			miny = minMax.mini;
			maxy = minMax.maxi;

			minx = 0; // instance.minX(dataArray);
			maxx = dataArray.length - 1; //instance.maxX(dataArray);

			if (maxx !== minx) {
				xScale = canvas.getContext('2d').canvas.clientWidth / (maxx - minx);
			}
			if (maxy !== miny) {
				yScale = canvas.getContext('2d').canvas.clientHeight / (maxy - miny);
			}
		}
	};

	(function () {
		instance.init(graphData);
		instance.drawGraph(cName, graphData);
	})(); // Invoked automatically when new is invoked.
};
