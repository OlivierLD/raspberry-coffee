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
function getColorConfig() {
	let colorConfig = defaultGraphColorConfig;
	for (let s = 0; s < document.styleSheets.length; s++) {
		try {
			for (let r = 0; document.styleSheets[s].cssRules !== null && r < document.styleSheets[s].cssRules.length; r++) {
				if (document.styleSheets[s].cssRules[r].selectorText === '.graphdisplay') {
					let cssText = document.styleSheets[s].cssRules[r].style.cssText;
					let cssTextElems = cssText.split(";");
					cssTextElems.forEach(function (elem) {
						if (elem.trim().length > 0) {
							let keyValPair = elem.split(":");
							let key = keyValPair[0].trim();
							let value = keyValPair[1].trim();
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
			// Absorb
		}
	}
	return colorConfig;
}

const defaultGraphColorConfig = {
	tooltipColor: "rgba(250, 250, 210, .7)",
	tooltipTextColor: "black",
	withBGGradient: true,
	bgGradientFrom: 'rgba(0,0,0,0)',
	bgGradientTo: 'cyan',
	bgColorNoGradient: "LightGray",
	horizontalGridColor: "gray",
	horizontalGridTextColor: "black",
	verticalGridColor: "gray",
	verticalGridTextColor: "black",
	rawDataLineColor: "green",
	fillRawData: true,
	rawDataFillColor: "rgba(0, 255, 0, 0.35)",
	smoothDataLineColor: "red",
	fillSmoothData: true,
	smoothDataFillColor: "rgba(255, 0, 0, 0.35)",
	clickedIndexColor: 'orange',
	font: 'Arial'
};
let graphColorConfig = defaultGraphColorConfig;

function Graph(cName,       // Canvas Name
               graphData,   // x,y tuple array
               callback,    // Callback on mouseclick
               unitLabel) {      // Unit label, for display

	let instance = this;

	graphColorConfig = getColorConfig();

	let xScale, yScale;
	let minx, miny, maxx, maxy;
	let requiredMinX, requiredMaxX, requiredMinY, requiredMaxY;

	let context;

	let unit = unitLabel;
	let lastClicked;

	let withRawData = true;
	let withTooltip = false;
	let withSmoothing = false;

	let withSprayPoints = false;
	let spraying = false;

	this.setRawData = function (rd) {
		withRawData = rd;
	};
	this.setTooltip = function (tt) {
		withTooltip = tt;
	};
	this.setSmoothing = function (sm) {
		withSmoothing = sm;
	};

	this.setSprayPoints = function (sp) {
		withSprayPoints = sp;
	};

	this.setBoundaries = function (minX, maxX, minY, maxY) {
		minx = minX;
		maxx = maxX;
		miny = minY;
		maxy = maxY;
		requiredMinX = minX;
		requiredMaxX = maxX;
		requiredMinY = minY;
		requiredMaxY = maxY;
		setScales();
	};

	this.getData = function () {
		return graphData;
	};

	let canvas = document.getElementById(cName);

	canvas.addEventListener('click', function (evt) {
		let x = evt.pageX - canvas.offsetLeft;
		let y = evt.pageY - canvas.offsetTop;

		let coords = relativeMouseCoords(evt, canvas);
		x = coords.x;
		y = coords.y;
//    console.log("Mouse: x=" + x + ", y=" + y);

		let idx = Math.round(x / xScale);
		if (idx < graphData.length) {
			if (callback !== undefined && callback !== null) {
				callback(idx);
			}
			lastClicked = idx;
		}
	});

	canvas.addEventListener('mousedown', function (evt) {
		if (withSprayPoints === true) {
			console.log('Start spraying');
			canvas.style.cursor = 'crosshair';
			spraying = true;
		}
	});

	canvas.addEventListener('mouseup', function (evt) {
		if (withSprayPoints === true && spraying === true) {
			console.log('Stop spraying');
			canvas.style.cursor = 'default';
			spraying = false;
		}
	});

	canvas.addEventListener('mousemove', function (evt) {
		if (withSprayPoints === true && spraying === true) {
			let x = evt.pageX - canvas.offsetLeft;
			let y = evt.pageY - canvas.offsetTop;

			let coords = relativeMouseCoords(evt, canvas);
			x = coords.x;
			y = coords.y; // - 30; // TODO Find where this 30 comes from...

//       console.log("Spraying at x=" + x + ", " + (minx + (x / xScale)) + ", y=" + y + ", " + (maxy - (y / yScale)) + ", xScale:" + xScale + ", yScale:" + yScale);
			let centerX = (minx + (x / xScale));
			let centerY = (maxy - (y / yScale));

			let nbPointsInSpray = 30;
			let sprayRadius = .25;
			for (let i = 0; i < nbPointsInSpray; i++) {
				let direction = Math.random() * 360;
				let radius = sprayRadius * Math.random();

				let difX = radius * Math.cos(direction / 180 * Math.PI);
				let difY = radius * Math.sin(direction / 180 * Math.PI);
				graphData.push({"x": centerX + difX, "y": centerY + difY});
			}
			graphData.push({"x": centerX, "y": centerY});

			instance.drawPoints(cName, graphData);
		}

		if (withTooltip === true) {
			let x = evt.pageX - canvas.offsetLeft;
			let y = evt.pageY - canvas.offsetTop;

			let coords = relativeMouseCoords(evt, canvas);
			x = coords.x;
			y = coords.y;
//    console.log("Mouse: x=" + x + ", y=" + y);

			let idx = xScale !== 0 ? Math.round(x / xScale) : 0;
			if (idx < graphData.length) {
				let str = [];
				try {
					str.push("Pos:" + idx);
					str.push(graphData[idx].getY() + " " + unit);
					//      console.log("Bubble:" + str);
				} catch (err) {
					console.log(JSON.stringify(err));
				}

				//    context.fillStyle = '#000';
				//    context.fillRect(0, 0, w, h);
				instance.drawGraph(cName, graphData, lastClicked);
				let tooltipW = 80, nblines = str.length;
				context.fillStyle = graphColorConfig.tooltipColor;
//      context.fillStyle = 'yellow';
				let fontSize = 10;
				let x_offset = 10, y_offset = 10;

				if (x > (canvas.getContext('2d').canvas.clientWidth / 2)) {
					x_offset = -(tooltipW + 10);
				}
				if (y > (canvas.getContext('2d').canvas.clientHeight / 2)) {
					y_offset = -(10 + 6 + (nblines * fontSize));
				}
				context.fillRect(x + x_offset, y + y_offset, tooltipW, 6 + (nblines * fontSize)); // Background
				context.fillStyle = graphColorConfig.tooltipTextColor;
				context.font = /*'bold ' +*/ fontSize + 'px verdana';
				for (let i = 0; i < str.length; i++) {
					context.fillText(str[i], x + x_offset + 5, y + y_offset + (3 + (fontSize * (i + 1)))); //, 60);
				}
			}
		}
	});

	let relativeMouseCoords = function (event, element) {
		let totalOffsetX = 0;
		let totalOffsetY = 0;
		let canvasX = 0;
		let canvasY = 0;
		let currentElement = element;

		do {
			totalOffsetX += currentElement.offsetLeft - currentElement.scrollLeft;
			totalOffsetY += currentElement.offsetTop - currentElement.scrollTop;
		} while (currentElement = currentElement.offsetParent)

		canvasX = event.pageX - totalOffsetX;
		canvasY = event.pageY - totalOffsetY;

		return {x: canvasX, y: canvasY};
	};

	this.minX = function (data) {
		let min = Number.MAX_VALUE;
		for (let i = 0; i < data.length; i++) {
			min = Math.min(min, (data[i].getX !== undefined ? data[i].getX() : data[i].x));
		}
		return min;
	};

	this.minY = function (data) {
		let min = Number.MAX_VALUE;
		for (let i = 0; i < data.length; i++) {
			min = Math.min(min, (data[i].getY !== undefined ? data[i].getY() : data[i].y));
		}
		return min;
	};

	this.maxX = function (data) {
		let max = Number.MIN_VALUE;
		for (let i = 0; i < data.length; i++) {
			max = Math.max(max, (data[i].getX !== undefined ? data[i].getX() : data[i].x));
		}
		return max;
	};

	this.maxY = function (data) {
		let max = Number.MIN_VALUE;
		for (let i = 0; i < data.length; i++) {
			max = Math.max(max, (data[i].getY !== undefined ? data[i].getY() : data[i].y));
		}
		return max;
	};

	this.getMinMax = function (data) {
		let mini = Math.floor(this.minY(data));
		let maxi = Math.ceil(this.maxY(data));

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

	let f = function (x, coeffs) {
		let value = 0;
		let maxDegree = coeffs.length - 1;
		for (let deg = 0; deg < coeffs.length; deg++) {
			value += (coeffs[deg] * Math.pow(x, maxDegree - deg));
		}
		return value;
	};

	this.drawPoints = function (displayCanvasName, data, coeffs) {
		context = canvas.getContext('2d');

		let width = context.canvas.clientWidth;
		let height = context.canvas.clientHeight;

		if (width === 0 || height === 0) { // Not visible
			return;
		}
		if (data.length > 0) {
			this.init(data, true);
		}

		if (minx !== undefined && maxx !== undefined && miny !== undefined && maxy !== undefined) {
			// Set the canvas size from its container.
			canvas.width = width;
			canvas.height = height;

			document.getElementById(displayCanvasName).title = data.length + " elements, X:[" + minx + ", " + maxx + "] Y:[" + miny + ", " + maxy + "]";
			let gridXStep = (maxx - minx) < 5 ? 1 : Math.round((maxx - minx) / 5);
			let gridYStep = (maxy - miny) < 5 ? 1 : Math.round((maxy - miny) / 5);

			// Clear
			context.fillStyle = "white";
			context.fillRect(0, 0, width, height);
			// Horizontal grid (Data Unit)
			for (let i = Math.round(miny); gridYStep > 0 && i < maxy; i += gridYStep) {
				context.beginPath();
				context.lineWidth = 1;
				context.strokeStyle = graphColorConfig.horizontalGridColor;
				context.moveTo(0, height - (i - miny) * yScale);
				context.lineTo(width, height - (i - miny) * yScale);
				context.stroke();

				context.save();
				context.font = "bold 10px " + graphColorConfig.font;
				context.fillStyle = graphColorConfig.horizontalGridTextColor;
				let str = i.toString();
				let len = context.measureText(str).width;
				context.fillText(str, width - (len + 2), height - ((i - miny) * yScale) - 2);
				context.restore();
				context.closePath();
			}

			// Vertical grid (index)
			for (let i = Math.round(minx); gridXStep > 0 && i < maxx; i += gridXStep) {
				context.beginPath();
				context.lineWidth = 1;
				context.strokeStyle = graphColorConfig.verticalGridColor;
				context.moveTo((i - minx) * xScale, 0);
				context.lineTo((i - minx) * xScale, height);
				context.stroke();

				// Rotate the whole context, and then write on it (that's why we need the translate)
				context.save();
				context.translate((i - minx) * xScale, height);
				context.rotate(-Math.PI / 2);
				context.font = "bold 10px " + graphColorConfig.font;
				context.fillStyle = graphColorConfig.verticalGridTextColor;
				let str = i.toString();
				let len = context.measureText(str).width;
				context.fillText(str, 2, -1); //i * xScale, cHeight - (len));
				context.restore();
				context.closePath();
			}
		}

		if (data.length < 1) {
			return; // No data
		}

		// Plot points here
		context.fillStyle = '#ff0000';
		for (let i = 0; i < data.length; i++) {
//        console.log("Plotting x:" + data[i].x + ", y:" + data[i].y + " to " + (data[i].x - minx) * xScale + ":" + (height - (data[i].y - miny) * yScale));
			context.fillRect((data[i].x - minx) * xScale, height - (data[i].y - miny) * yScale, 1, 1);
		}

		if (coeffs !== undefined) {
			context.beginPath();
			context.lineWidth = 3;
			context.strokeStyle = 'blue'; // graphColorConfig.smoothDataLineColor;
			let previousPoint;
			let stepX = (maxx - minx) / 1000;
			for (let x = minx; x < maxx; x += stepX) {
				let y = f(x, coeffs);
				if (previousPoint === undefined) {
					context.moveTo((x - minx) * xScale, height - ((y - miny) * yScale));
				} else {
					context.lineTo((x - minx) * xScale, height - ((y - miny) * yScale));
				}
				previousPoint = {x: x, y: y};
			}
//        context.closePath();
			context.stroke();

			context.font = "bold 14px Arial";
			context.fillStyle = 'black';
			let str = "Equation goes here";
			let progressX = 10;
			let degree = coeffs.length - 1;
			for (let c = 0; c < coeffs.length; c++) {
				context.font = "bold 14px Arial";
				str = ((c === 0 ? " " : (coeffs[c] >= 0 ? " + " : " ")) + coeffs[c] + ((degree - c > 0) ? " x" : ""));
				let len = context.measureText(str).width;
				context.fillText(str, progressX, 12);
				progressX += len;
				if (degree - c > 1) {
					context.font = "bold 8px Arial";
					str = (degree - c).toString();
					len = context.measureText(str).width;
					context.fillText(str, progressX, 8);
					progressX += len;
				}
			}
		}
	};

	this.drawGraph = function (displayCanvasName, data, idx) {

		if (data.length < 2) {
			return;
		}

		context = canvas.getContext('2d');

		let width = context.canvas.clientWidth;
		let height = context.canvas.clientHeight;

		if (width === 0 || height === 0) { // Not visible
			return;
		}
		this.init(data);

		// Set the canvas size from its container.
		canvas.width = width;
		canvas.height = height;

		let _idxX;
		if (idx !== undefined) {
			_idxX = idx * xScale;
		}

		document.getElementById(displayCanvasName).title = data.length + " elements, X:[" + minx + ", " + maxx + "] Y:[" + miny + ", " + maxy + "]";

		let gridXStep = Math.round(data.length / 10);
		let gridYStep = (maxy - miny) < 5 ? 1 : Math.round((maxy - miny) / 5);

		// Sort the tuples (on X, time)
//   data.sort(sortTupleX);

		let smoothData = data;
		let _smoothData = [];
		let smoothWidth = 20;
		if (smoothData.length >= smoothWidth) {
			for (let i = 0; i < smoothData.length; i++) {
				let yAccu = 0;
				for (let acc = i - (smoothWidth / 2); acc < i + (smoothWidth / 2); acc++) {
					let y;
					if (acc < 0) {
						y = smoothData[0].getY();
					} else if (acc > (smoothData.length - 1)) {
						y = smoothData[smoothData.length - 1].getY();
					} else {
						y = smoothData[acc].getY();
					}
					yAccu += y;
				}
				yAccu = yAccu / smoothWidth;
				_smoothData.push(new Tuple(i, yAccu));
//          console.log("I:" + smoothData[i].getX() + " y from " + smoothData[i].getY() + " becomes " + yAccu);
			}
		}
		// Clear
		context.fillStyle = "white";
		context.fillRect(0, 0, width, height);

		smoothData = _smoothData;
		if (graphColorConfig.withBGGradient === false) {
			context.fillStyle = graphColorConfig.bgColorNoGradient;
			context.fillRect(0, 0, width, height);
		} else {
			let grV = context.createLinearGradient(0, 0, 0, height);
			grV.addColorStop(0, graphColorConfig.bgGradientFrom);
			grV.addColorStop(1, graphColorConfig.bgGradientTo);

			context.fillStyle = grV;
			context.fillRect(0, 0, width, height);
		}
		// Horizontal grid (Data Unit)
		for (let i = Math.round(miny); gridYStep > 0 && i < maxy; i += gridYStep) {
			context.beginPath();
			context.lineWidth = 1;
			context.strokeStyle = graphColorConfig.horizontalGridColor;
			context.moveTo(0, height - (i - miny) * yScale);
			context.lineTo(width, height - (i - miny) * yScale);
			context.stroke();

			context.save();
			context.font = "bold 10px " + graphColorConfig.font;
			context.fillStyle = graphColorConfig.horizontalGridTextColor;
			let str = i.toString() + " " + unit;
			let len = context.measureText(str).width;
			context.fillText(str, width - (len + 2), height - ((i - miny) * yScale) - 2);
			context.restore();
			context.closePath();
		}

		// Vertical grid (index)
		for (let i = gridXStep; i < data.length; i += gridXStep) {
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
			let str = i.toString();
			let len = context.measureText(str).width;
			context.fillText(str, 2, -1); //i * xScale, cHeight - (len));
			context.restore();
			context.closePath();
		}

		if (withRawData && data.length > 0) {
			context.beginPath();
			context.lineWidth = 1;
			context.strokeStyle = graphColorConfig.rawDataLineColor;

			let previousPoint = data[0];
			context.moveTo((0 - minx) * xScale, height - (data[0].getY() - miny) * yScale);
			for (let i = 1; i < data.length; i++) {
				//  context.moveTo((previousPoint.getX() - minx) * xScale, cHeight - (previousPoint.getY() - miny) * yScale);
				context.lineTo((i - minx) * xScale, height - (data[i].getY() - miny) * yScale);
				//  context.stroke();
				previousPoint = data[i];
			}
			context.lineTo(width, height);
			context.lineTo(0, height);
			context.closePath();
			context.stroke();
			if (graphColorConfig.fillRawData === true) {
				context.fillStyle = graphColorConfig.rawDataFillColor;
				context.fill();
			}
		}

		if (withSmoothing) {
			data = smoothData;
			if (data !== undefined && data.length > 0) {

				context.beginPath();
				context.lineWidth = 3;
				context.strokeStyle = graphColorConfig.smoothDataLineColor;
				previousPoint = data[0];
				context.moveTo((0 - minx) * xScale, height - (data[0].getY() - miny) * yScale);
				for (let i = 1; i < data.length; i++) {
//              context.moveTo((previousPoint.getX() - minx) * xScale, cHeight - (previousPoint.getY() - miny) * yScale);
					context.lineTo((i - minx) * xScale, height - (data[i].getY() - miny) * yScale);
//              context.stroke();
					previousPoint = data[i];
				}
				// Close the shape, bottom
				context.lineTo(width, height);
				context.lineTo(0, height);

				context.closePath();
				context.stroke();
				if (graphColorConfig.fillSmoothData === true) {
					context.fillStyle = graphColorConfig.smoothDataFillColor;
					context.fill();
				}
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

	let setScales = function () {
		if (maxx !== minx) {
			xScale = canvas.getContext('2d').canvas.clientWidth / (maxx - minx);
		}
		if (maxy !== miny) {
			yScale = canvas.getContext('2d').canvas.clientHeight / (maxy - miny);
		}
	};

	this.init = function (dataArray, points) {
		if (dataArray.length > 0) {
			let minMax = this.getMinMax(dataArray);

			if (requiredMinX !== undefined &&
					requiredMaxX !== undefined &&
					requiredMinY !== undefined &&
					requiredMaxY !== undefined) {
				miny = Math.min(requiredMinY, minMax.mini);
				maxy = Math.max(requiredMaxY, minMax.maxi);
			} else {
				miny = minMax.mini;
				maxy = minMax.maxi;
			}

			if (points === true) {
				if (requiredMinX !== undefined &&
						requiredMaxX !== undefined &&
						requiredMinY !== undefined &&
						requiredMaxY !== undefined) {
					minx = Math.min(requiredMinX, Math.floor(this.minX(dataArray)));
					maxx = Math.max(requiredMaxX, Math.ceil(this.maxX(dataArray)));
				} else {
					minx = Math.floor(this.minX(dataArray));
					maxx = Math.ceil(this.maxX(dataArray));
				}

			} else {
				minx = 0; // instance.minX(dataArray);
				maxx = dataArray.length - 1; //instance.maxX(dataArray);
			}
			setScales();
		}
	};

	(function () {
		instance.init(graphData);
		instance.drawGraph(cName, graphData);
	})(); // Invoked automatically when new is invoked.
}

function Tuple(_x, _y) {
	let x = _x;
	let y = _y;

	this.getX = function () {
		return x;
	};
	this.getY = function () {
		return y;
	};
}

function sortTupleX(t1, t2) {
	if (t1.getX() < t2.getX()) {
		return -1;
	}
	if (t1.getX() > t2.getX()) {
		return 1;
	}
	return 0;
}
