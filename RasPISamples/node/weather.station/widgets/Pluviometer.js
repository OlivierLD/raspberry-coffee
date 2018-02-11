/*
 * @author Olivier Le Diouris
 */
var pluviometerColorConfigWhite = {
			withShadow: true,
			shadowColor: 'LightGrey',
			scaleColor: 'black',
			bgColor: 'white',
			majorTickColor: 'LightGrey',
			minorTickColor: 'DarkGrey',
			valueOutlineColor: 'black',
			valueColor: 'DarkGrey',
			tubeOutlineColor: 'pink',
			hgOutlineColor: 'DarkGrey',
			font: 'Arial'
		};

var pluviometerColorConfigBlack = {
			withShadow: true,
			shadowColor: 'black',
			scaleColor: 'LightGrey',
			bgColor: 'black',
			majorTickColor: 'LightGrey',
			minorTickColor: 'DarkGrey',
			valueOutlineColor: 'black',
			valueColor: 'LightGrey',
			tubeOutlineColor: 'pink',
			hgOutlineColor: 'DarkGrey',
			font: 'Arial'
		};

var pluviometerColorConfig = pluviometerColorConfigWhite; // White is the default

function Pluviometer(cName, minValue, maxValue, majorTicks, minorTicks) {
	if (minValue === undefined)
		minValue = 0;
	if (maxValue === undefined)
		maxValue = 5; //20;
	if (majorTicks === undefined)
		majorTicks = 1;
	if (minorTicks === undefined)
		minorTicks = 0.25;

	var canvasName = cName;

	var running = false;
	var previousValue = 0.0;
	var intervalID;
	var valueToDisplay = 0;

	var instance = this;

//try { console.log('in the pluviometer constructor for ' + cName + " (" + dSize + ")"); } catch (e) {}

	(function () {
		drawDisplay(canvasName, previousValue);
	})(); // Invoked automatically

	this.repaint = function () {
		drawDisplay(canvasName, previousValue);
	};

	this.startStop = function (buttonName) {
//  console.log('StartStop requested on ' + buttonName);
		var button = document.getElementById(buttonName);
		running = !running;
		button.value = (running ? "Stop" : "Start");
		if (running)
			this.animate();
		else {
			window.clearInterval(intervalID);
			previousValue = valueToDisplay;
		}
	};

	this.animate = function () {
		var value;
		if (arguments.length === 1)
			value = arguments[0];
		else {
//    console.log("Generating random value");
			value = minValue + ((maxValue - minValue) * Math.random());
		}
//  console.log("Reaching Value :" + value + " from " + previousValue);
		diff = value - previousValue;
		valueToDisplay = previousValue;

//  console.log(canvasName + " going from " + previousValue + " to " + value);

		if (diff > 0)
			incr = 1; // 0.1 * maxValue; // 0.01 is nicer, but too slow...
		else
			incr = -1; // -0.1 * maxValue;
		intervalID = window.setInterval(function () {
			displayAndIncrement(incr, value);
		}, 50);
	};

	var displayAndIncrement = function (inc, finalValue) {
		//console.log('Tic ' + inc + ', ' + finalValue);
		drawDisplay(canvasName, valueToDisplay);
		valueToDisplay += inc;
		if ((inc > 0 && valueToDisplay > finalValue) || (inc < 0 && valueToDisplay < finalValue)) {
			//  console.log('Stop!')
			window.clearInterval(intervalID);
			previousValue = finalValue;
			if (running)
				instance.animate();
			else
				drawDisplay(canvasName, finalValue);
		}
	};

	function getStyleRuleValue(style, selector, sheet) {
		var sheets = typeof sheet !== 'undefined' ? [sheet] : document.styleSheets;
		for (var i = 0, l = sheets.length; i < l; i++) {
			var sheet = sheets[i];
			if (!sheet.cssRules) {
				continue;
			}
			for (var j = 0, k = sheet.cssRules.length; j < k; j++) {
				var rule = sheet.cssRules[j];
				if (rule.selectorText && rule.selectorText.split(',').indexOf(selector) !== -1) {
					return rule.style[style];
				}
			}
		}
		return null;
	};

	function drawDisplay(displayCanvasName, displayValue) {
		var schemeColor = getStyleRuleValue('color', '.display-scheme');
//  console.log(">>> DEBUG >>> color:" + schemeColor);
		if (schemeColor === 'black')
			pluviometerColorConfig = pluviometerColorConfigBlack;
		else if (schemeColor === 'white')
			pluviometerColorConfig = pluviometerColorConfigWhite;

		var digitColor = pluviometerColorConfig.scaleColor;

		var canvas = document.getElementById(displayCanvasName);
		var context = canvas.getContext('2d');

		// Cleanup
		context.fillStyle = pluviometerColorConfig.bgColor;
		//context.fillStyle = "#ffffff";
		//context.fillStyle = "LightBlue";
		//context.fillStyle = "transparent";
		context.fillRect(0, 0, canvas.width, canvas.height);
		//context.fillStyle = 'rgba(255, 255, 255, 0.0)';
		//context.fillRect(0, 0, canvas.width, canvas.height);

		//context.fillStyle = "transparent";
		// Bottom of the tube at (canvas.height - 10)
		var bottomTube = (canvas.height - 10);
		var topTube = 20;// Top of the tube at y = 20

		var tubeLength = bottomTube - topTube;
		var tubeWidth = tubeLength / 5;
		var xFrom, xTo, yFrom, yTo;

		// Tube
		context.beginPath();
		//context.arc(x, y, radius, startAngle, startAngle + Math.PI, antiClockwise);
		var x = (canvas.width / 2) - (1.5 * (tubeWidth / 2));
		var y = bottomTube;
		context.moveTo(x, y);    // bottom left
		x = (canvas.width / 2) + (1.5 * (tubeWidth / 2));
		context.lineTo(x, y); // bottom right
		x = (canvas.width / 2) + (tubeWidth / 2);
		y = bottomTube - 5;
		context.lineTo(x, y); // Right, just above the foot
		y = topTube;
		context.lineTo(x, y); // Top right
		x = (canvas.width / 2) - (1.5 * (tubeWidth / 2));
		context.lineTo(x, y); // Top left, with the bill
		y = topTube + 10;
		x = (canvas.width / 2) - (tubeWidth / 2);
		context.lineTo(x, y); // Left, under the bill
		y = bottomTube - 5;
		context.lineTo(x, y); // Left, just above the foot
		x = (canvas.width / 2) - (1.5 * (tubeWidth / 2));
		y = bottomTube;
		context.lineTo(x, y); // Back to base

		context.lineWidth = 1;
		context.stroke();

		var grd = context.createLinearGradient(0, 5, 0, tubeLength);
		grd.addColorStop(0, 'LightGrey'); // 0  Beginning. black
		grd.addColorStop(1, 'white');     // 1  End. LightGrey
		context.fillStyle = grd;

		if (pluviometerColorConfig.withShadow) {
			context.shadowOffsetX = 3;
			context.shadowOffsetY = 3;
			context.shadowBlur = 3;
			context.shadowColor = pluviometerColorConfig.shadowColor;
		}

		context.lineJoin = "round";
		context.fill();
		context.strokeStyle = pluviometerColorConfig.tubeOutlineColor; // Tube outline color
		context.stroke();
		context.closePath();

		bottomTube -= 5;
		topTube -= 5;
		tubeLength -= 10;

		// Liquid in the tube
		context.beginPath();
		x = (canvas.width / 2) - (0.9 * (tubeWidth / 2));
		y = bottomTube;
		context.moveTo(x, y);   // bottom left
		x = (canvas.width / 2) + (0.9 * (tubeWidth / 2));
		context.lineTo(x, y);   // bottom right
		y = bottomTube - ((tubeLength) * (displayValue / (maxValue - minValue)));
		context.lineTo(x, y);   // top right
		x = (canvas.width / 2) - (0.9 * (tubeWidth / 2));
		context.lineTo(x, y);   // top left

		context.lineWidth = 1;

		var _grd = context.createLinearGradient(0, topTube, 0, tubeLength);
		// Colors are hard-coded
		_grd.addColorStop(0, 'navy');   // 0  Beginning, top
		_grd.addColorStop(0.5, 'blue');
		_grd.addColorStop(1, 'cyan');   // 1  End, bottom
		context.fillStyle = _grd;

//  context.shadowBlur  = 20;
//  context.shadowColor = 'black';

		context.lineJoin = "round";
		context.fill();
		context.strokeStyle = pluviometerColorConfig.hgOutlineColor;
		context.stroke();
		context.closePath();

		// Major Ticks
		context.beginPath();
		for (i = 0; i <= (maxValue - minValue); i += majorTicks) {
			xFrom = (canvas.width / 2) + (tubeWidth / 2);
			yFrom = bottomTube - ((tubeLength) * (i / (maxValue - minValue)));
			xTo = xFrom - 20;
			yTo = yFrom;
			context.moveTo(xFrom, yFrom);
			context.lineTo(xTo, yTo);
		}
		context.lineWidth = 1;
		context.strokeStyle = pluviometerColorConfig.majorTickColor;
		context.stroke();
		context.closePath();

		// Minor Ticks
		if (minorTicks > 0) {
			context.beginPath();
			for (i = 0; i <= (maxValue - minValue); i += minorTicks) {
				xFrom = (canvas.width / 2) + (tubeWidth / 2);
				yFrom = bottomTube - ((tubeLength) * (i / (maxValue - minValue)));
				xTo = xFrom - 10;
				yTo = yFrom;
				context.moveTo(xFrom, yFrom);
				context.lineTo(xTo, yTo);
			}
			context.lineWidth = 1;
			context.strokeStyle = pluviometerColorConfig.minorTickColor;
			context.stroke();
			context.closePath();
		}

		// Numbers
		context.beginPath();
		for (i = minValue; i <= maxValue; i += majorTicks) {
			xTo = (canvas.width / 2) + 20;
			yTo = bottomTube - ((tubeLength) * ((i - minValue) / (maxValue - minValue)));
			;
			context.font = "bold 10px " + pluviometerColorConfig.font;
			context.fillStyle = digitColor;
			str = i.toString();
			len = context.measureText(str).width;
			context.fillText(str, xTo, yTo + 3); // 5: half font size
		}
		context.closePath();

		// Value
//  displayValue = 5.3; // for tests
		text = displayValue.toFixed(2);
		len = 0;
		context.font = "bold 12px " + pluviometerColorConfig.font;
		var metrics = context.measureText(text);
		len = metrics.width;

		context.beginPath();
		context.fillStyle = pluviometerColorConfig.valueColor;
		context.fillText(text, (canvas.width / 2) - (len / 2), 10);
		context.lineWidth = 1;
		context.strokeStyle = pluviometerColorConfig.valueOutlineColor;
		context.strokeText(text, (canvas.width / 2) - (len / 2), 10); // Outlined
		context.closePath();
	};

	this.setValue = function (val) {
		drawDisplay(canvasName, val);
	};
}
