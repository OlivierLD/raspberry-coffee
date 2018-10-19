/*
 * @author Olivier Le Diouris
 */

function Thermometer(cName, dSize, minValue, maxValue, majorTicks, minorTicks) {
	if (minValue === undefined)
		minValue = -20;
	if (maxValue === undefined)
		maxValue = 50;
	if (majorTicks === undefined)
		majorTicks = 10;
	if (minorTicks === undefined)
		minorTicks = 1;

	/*
	 * See custom properties in CSS.
	 * =============================
	 * @see https://developer.mozilla.org/en-US/docs/Web/CSS/
	 * Relies on a rule named .graphdisplay, like that:
	 *
	 .analogdisplay {
			--bg-color: rgba(0, 0, 0, 0);
			--digit-color: black;
			--with-gradient: true;
			--display-background-gradient-from: LightGrey;
			--display-background-gradient-to: white;
			--display-line-color: rgba(255, 255, 255, 0.5);
			--label-fill-color: rgba(255, 255, 255, 0.5);
			--with-display-shadow: false;
			--shadow-color: rgba(0, 0, 0, 0.75);
			--outline-color: DarkGrey;
			--major-tick-color: black;
			--minor-tick-color: black;
			--value-color: grey;
			--value-outline-color: black;
			--value-nb-decimal: 1;
			--hand-color: red;
			--hand-outline-color: black;
			--with-hand-shadow: true;
			--knob-color: DarkGrey;
			--knob-outline-color: black;
			--font: Arial;
		}
	 */

	/**
	 * Recurse from the top down, on styleSheets and cssRules
	 *
	 * document.styleSheets[0].cssRules[2].selectorText returns ".analogdisplay"
	 * document.styleSheets[0].cssRules[2].cssText returns ".analogdisplay { --hand-color: red;  --face-color: white; }"
	 * document.styleSheets[0].cssRules[2].style.cssText returns "--hand-color: red; --face-color: white;"
	 *
	 * spine-case to camelCase
	 */
	var getColorConfig = function () {
		var colorConfig = defaultThermometerColorConfig;
		for (var s = 0; s < document.styleSheets.length; s++) {
//		console.log("Walking though ", document.styleSheets[s]);
			for (var r = 0; document.styleSheets[s].cssRules !== null && r < document.styleSheets[s].cssRules.length; r++) {
//			console.log(">>> ", document.styleSheets[s].cssRules[r].selectorText);
				if (document.styleSheets[s].cssRules[r].selectorText === '.analogdisplay') {
//				console.log("  >>> Found it!");
					var cssText = document.styleSheets[s].cssRules[r].style.cssText;
					var cssTextElems = cssText.split(";");
					cssTextElems.forEach(function (elem) {
						if (elem.trim().length > 0) {
							var keyValPair = elem.split(":");
							var key = keyValPair[0].trim();
							var value = keyValPair[1].trim();
							switch (key) {
								case '--bg-color':
									colorConfig.bgColor = value;
									break;
								case '--digit-color':
									colorConfig.digitColor = value;
									break;
								case '--with-gradient':
									colorConfig.withGradient = (value === 'true');
									break;
								case '--display-background-gradient-from':
									colorConfig.displayBackgroundGradientFrom = value;
									break;
								case '--display-background-gradient-to':
									colorConfig.displayBackgroundGradientTo = value;
									break;
								case '--display-line-color':
									colorConfig.displayLineColor = value;
									break;
								case '--label-fill-color':
									colorConfig.labelFillColor = value;
									break;
								case '--with-display-shadow':
									colorConfig.withDisplayShadow = (value === 'true');
									break;
								case '--shadow-color':
									colorConfig.shadowColor = value;
									break;
								case '--outline-color':
									colorConfig.outlineColor = value;
									break;
								case '--major-tick-color':
									colorConfig.majorTickColor = value;
									break;
								case '--minor-tick-color':
									colorConfig.minorTickColor = value;
									break;
								case '--value-color':
									colorConfig.valueColor = value;
									break;
								case '--value-outline-color':
									colorConfig.valueOutlineColor = value;
									break;
								case '--value-nb-decimal':
									colorConfig.valueNbDecimal = value;
									break;
								case '--hand-color':
									colorConfig.handColor = value;
									break;
								case '--hand-outline-color':
									colorConfig.handOutlineColor = value;
									break;
								case '--with-hand-shadow':
									colorConfig.withHandShadow = (value === 'true');
									break;
								case '--knob-color':
									colorConfig.knobColor = value;
									break;
								case '--knob-outline-color':
									colorConfig.knobOutlineColor = value;
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
		}
		return colorConfig;
	};

	var defaultThermometerColorConfig = {
		bgColor: 'rgba(0, 0, 0, 0)', /*'white',*/
		digitColor: 'red',
		withGradient: true,
		displayBackgroundGradientFrom: 'LightGrey',
		displayBackgroundGradientTo: 'white',
		withDisplayShadow: true,
		shadowColor: 'rgba(0, 0, 0, 0.75)',
		majorTickColor: 'DarkGrey',
		minorTickColor: 'DarkGrey',
		valueColor: 'LightRed',
		valueOutlineColor: 'black',
		valueNbDecimal: 2,
		font: 'Arial' /* 'Source Code Pro' */
	};

	var thermometerColorConfig = defaultThermometerColorConfig;

	if (events !== undefined) {
		events.subscribe('color-scheme-changed', function(val) {
//    console.log('Color scheme changed:', val);
			reloadColorConfig();
		});
	}
	thermometerColorConfig = getColorConfig();

	var canvasName = cName;
	var displaySize = dSize;

	var running = false;
	var previousValue = 0.0;
	var intervalID;
	var valueToDisplay = 0;

	var instance = this;

//try { console.log('in the Thermometer constructor for ' + cName + " (" + dSize + ")"); } catch (e) {}

	(function () {
		drawDisplay(canvasName, displaySize, previousValue);
	})(); // Invoked automatically

	this.repaint = function () {
		drawDisplay(canvasName, displaySize, previousValue);
	};

	this.setDisplaySize = function (ds) {
		displaySize = ds;
		drawDisplay(canvasName, displaySize, previousValue);
	};

	this.startStop = function (buttonName) {
//  console.log('StartStop requested on ' + buttonName);
		var button = document.getElementById(buttonName);
		running = !running;
		button.value = (running ? "Stop" : "Start");
		if (running) {
			this.animate();
		} else {
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

		if (diff > 0) {
			incr = 0.01 * maxValue;
		} else {
			incr = -0.01 * maxValue;
		}
		intervalID = window.setInterval(function () {
			displayAndIncrement(incr, value);
		}, 50);
	};

	var displayAndIncrement = function (inc, finalValue) {
		//console.log('Tic ' + inc + ', ' + finalValue);
		drawDisplay(canvasName, displaySize, valueToDisplay);
		valueToDisplay += inc;
		if ((inc > 0 && valueToDisplay > finalValue) || (inc < 0 && valueToDisplay < finalValue)) {
			//  console.log('Stop!')
			window.clearInterval(intervalID);
			previousValue = finalValue;
			if (running) {
				instance.animate();
			} else {
				drawDisplay(canvasName, displaySize, finalValue);
			}
		}
	};

	var reloadColor = false;
	var reloadColorConfig = function() {
//  console.log('Color scheme has changed');
		reloadColor = true;
	};

	function drawDisplay(displayCanvasName, displayRadius, displayValue) {
		if (reloadColor) {
			// In case the CSS has changed, dynamically.
			thermometerColorConfig = getColorConfig();
			console.log("Changed theme:", thermometerColorConfig);
		}
		reloadColor = false;

		var digitColor = thermometerColorConfig.digitColor;

		var canvas = document.getElementById(displayCanvasName);
		var context = canvas.getContext('2d');
		context.clearRect(0, 0, canvas.width, canvas.height);

		var radius = 10; // The ball at the bottom. The tube is (radius / 2) wide.

		// Cleanup
		context.fillStyle = thermometerColorConfig.bgColor;
		context.fillRect(0, 0, canvas.width, canvas.height);

		// Bottom of the tube at (canvas.height - 10 - radius)
		var bottomTube = (canvas.height - 10 - radius);
		var topTube = 40;// Top of the tube at y = 20

		var tubeLength = bottomTube - topTube;

		// Major Ticks
		context.beginPath();
		for (i = 0; i <= (maxValue - minValue); i += majorTicks) {
			xFrom = (canvas.width / 2) - 20;
			yFrom = bottomTube - ((tubeLength) * (i / (maxValue - minValue)));
			xTo = (canvas.width / 2) + 20;
			yTo = yFrom;
			context.moveTo(xFrom, yFrom);
			context.lineTo(xTo, yTo);
		}
		context.lineWidth = 1;
		context.strokeStyle = thermometerColorConfig.majorTickColor;
		context.stroke();
		context.closePath();

		// Minor Ticks
		if (minorTicks > 0) {
			context.beginPath();
			for (i = 0; i <= (maxValue - minValue); i += minorTicks) {
				xFrom = (canvas.width / 2) - 15;
				yFrom = bottomTube - ((tubeLength) * (i / (maxValue - minValue)));
				xTo = (canvas.width / 2) + 15;
				yTo = yFrom;
				context.moveTo(xFrom, yFrom);
				context.lineTo(xTo, yTo);
			}
			context.lineWidth = 1;
			context.strokeStyle = thermometerColorConfig.minorTickColor;
			context.stroke();
			context.closePath();
		}

		// Tube
		context.beginPath();
		//context.arc(x, y, radius, startAngle, startAngle + Math.PI, antiClockwise);
		context.arc(canvas.width / 2, canvas.height - 10 - radius, radius, 5 * Math.PI / 4, 7 * Math.PI / 4, true);
		context.lineTo((canvas.width / 2) + (radius * Math.cos(Math.PI / 4)), topTube); // right side of the tube
		context.arc(canvas.width / 2, topTube, (radius / 2), 0, Math.PI, true);
		context.lineWidth = 1;

		if (thermometerColorConfig.withGradient) {
			var grd = context.createLinearGradient(0, 5, 0, radius);
			grd.addColorStop(0, thermometerColorConfig.displayBackgroundGradientFrom); // 0  Beginning
			grd.addColorStop(1, thermometerColorConfig.displayBackgroundGradientTo);   // 1  End
			context.fillStyle = grd;
		} else {
			context.fillStyle = thermometerColorConfig.displayBackgroundGradientTo;
		}
		if (thermometerColorConfig.withDisplayShadow) {
			context.shadowBlur = 0;
			context.shadowColor = thermometerColorConfig.shadowColor; // 'black';
		} else {
			context.shadowColor = "transparent";
		}
		context.lineJoin = "round";
		context.fill();
		context.strokeStyle = 'DarkGrey';
		context.stroke();
		context.closePath();

		// Numbers
		context.beginPath();
		for (i = minValue; i <= maxValue; i += majorTicks) {
			xTo = (canvas.width / 2) + 20;
			yTo = bottomTube - ((tubeLength) * ((i - minValue) / (maxValue - minValue)));

			context.font = "bold 10px Arial";
			context.fillStyle = digitColor;
			str = i.toString();
			len = context.measureText(str).width;
			context.fillText(str, xTo, yTo + 3); // 5: half font size
		}
		context.closePath();

		// Value
		text = displayValue.toFixed(thermometerColorConfig.valueNbDecimal);
		len = 0;
		context.font = "bold 20px Arial";
		var metrics = context.measureText(text);
		len = metrics.width;

		context.beginPath();
		context.fillStyle = thermometerColorConfig.valueColor;
		context.fillText(text, (canvas.width / 2) - (len / 2), ((radius * .75) + 10));
		context.lineWidth = 1;
		context.strokeStyle = thermometerColorConfig.valueOutlineColor;
		context.strokeText(text, (canvas.width / 2) - (len / 2), ((radius * .75) + 10)); // Outlined
		context.closePath();

		// Liquid in the tube
		context.beginPath();
		//context.arc(x, y, radius, startAngle, startAngle + Math.PI, antiClockwise);
		context.arc(canvas.width / 2, canvas.height - 10 - (radius * 0.75), (radius * 0.75), 5 * Math.PI / 4, 7 * Math.PI / 4, true);
		var y = bottomTube - ((tubeLength) * ((displayValue - minValue) / (maxValue - minValue)));

		context.lineTo((canvas.width / 2) + ((radius * 0.75) * Math.cos(Math.PI / 4)), y); // right side of the tube
		context.lineTo((canvas.width / 2) - ((radius * 0.75) * Math.cos(Math.PI / 4)), y); // top of the liquid

		context.lineWidth = 1;

		context.save();
		var _grd = context.createLinearGradient(0, topTube, 0, tubeLength);
		_grd.addColorStop(0, 'red');    // 0  Beginning
		_grd.addColorStop(0.6, 'red');
		_grd.addColorStop(0.8, 'blue');
		_grd.addColorStop(1, 'navy');   // 1  End
		context.fillStyle = _grd;

		context.shadowBlur = 20;
		context.shadowColor = 'black';

		context.lineJoin = "round";
		context.fill();
		context.strokeStyle = 'DarkGrey';
		context.stroke();
		context.closePath();

		context.restore();
	};

	this.setValue = function (val) {
		drawDisplay(canvasName, displaySize, val);
	};
}