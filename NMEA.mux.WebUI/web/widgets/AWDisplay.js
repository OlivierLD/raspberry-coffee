/*
 * @author Olivier Le Diouris
 */

function AWDisplay(cName, dSize, majorTicks, minorTicks, withDigits) {
	if (majorTicks === undefined) {
		majorTicks = 45;
	}
	if (minorTicks === undefined) {
		minorTicks = 0;
	}
	if (withDigits === undefined) {
		withDigits = false;
	}
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
			--outlined-port-starboard: false;
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
		var colorConfig = defaultAnalogColorConfig;
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
								case "--outlined-port-starboard":
									colorConfig.outlinedPortStarboard = (value === 'true');
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

	var defaultAnalogColorConfig = {
		bgColor: 'rgba(0, 0, 0, 0)', /* transparent, 'white', */
		digitColor: 'black',
		withGradient: true,
		displayBackgroundGradientFrom: 'LightGrey',
		displayBackgroundGradientTo: 'white',
		withDisplayShadow: false,
		shadowColor: 'rgba(0, 0, 0, 0.75)',
		outlineColor: 'DarkGrey',
		majorTickColor: 'black',
		minorTickColor: 'black',
		valueColor: 'grey',
		valueOutlineColor: 'black',
		valueNbDecimal: 1,
		handColor: 'red', // 'rgba(0, 0, 100, 0.25)',
		handOutlineColor: 'black',
		withHandShadow: true,
		knobColor: 'DarkGrey',
		knobOutlineColor: 'black',
		outlinedPortStarboard: false,
		font: 'Arial' /* 'Source Code Pro' */
	};

	var awDisplayColorConfig = defaultAnalogColorConfig; //

	if (events !== undefined) {
		events.subscribe('color-scheme-changed', function (val) {
//    console.log('Color scheme changed:', val);
			reloadColorConfig();
		});
	}
	awDisplayColorConfig = getColorConfig();

	var canvasName = cName;
	var displaySize = dSize;

	var scale = dSize / 100;

	var running = false;
	var previousValue = 0.0;
	var intervalID;
	var angleToDisplay = 0;
	var aws = 0;
	var incr = 1;
	var withBorder = true;
	var label;

	var instance = this;

//try { console.log('in the AWDisplay constructor for ' + cName + " (" + dSize + ")"); } catch (e) {}

	(function () {
		drawDisplay(canvasName, displaySize, previousValue);
	})(); // Invoked automatically

	this.setDisplaySize = function (ds) {
		scale = ds / 100;
		displaySize = ds;
		drawDisplay(canvasName, displaySize, previousValue);
	};

	this.setBorder = function (b) {
		withBorder = b;
	};

	this.setLabel = function (lbl) {
		label = lbl;
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
			previousValue = angleToDisplay;
		}
	};

	var on360 = function (angle) {
		var num = angle;
		while (num < 0)
			num += 360;
		return num;
	};

	this.setAWS = function (ws) {
		aws = ws;
	};

	this.animate = function () {
		var value;
		if (arguments.length === 1)
			value = arguments[0];
		else {
//    console.log("Generating random value");
			value = 360 * Math.random();
		}
//  console.log("Reaching Value :" + value + " from " + previousValue);
		diff = value - on360(previousValue);
		if (Math.abs(diff) > 180) // && sign(Math.cos(toRadians(value))))
		{
//    console.log("Diff > 180: new:" + value + ", prev:" + previousValue);
			if (value > on360(previousValue))
				value -= 360;
			else
				value += 360;
			diff = value - on360(previousValue);
		}
		angleToDisplay = on360(previousValue);

//  console.log(canvasName + " going from " + previousValue + " to " + value);

		incr = diff / 10;
//    if (diff < 0)
//      incr *= -1;
		if (intervalID)
			window.clearInterval(intervalID);
		intervalID = window.setInterval(function () {
			displayAndIncrement(value);
		}, 50);
	};

	function sign(x) {
		return x > 0 ? 1 : x < 0 ? -1 : 0;
	};

	function toRadians(d) {
		return Math.PI * d / 180;
	};

	function toDegrees(d) {
		return d * 180 / Math.PI;
	};

	var displayAndIncrement = function (finalValue) {
		//console.log('Tic ' + inc + ', ' + finalValue);
		drawDisplay(canvasName, displaySize, angleToDisplay);
		angleToDisplay += incr;
		if ((incr > 0 && angleToDisplay > finalValue) || (incr < 0 && angleToDisplay < finalValue)) {
			//  console.log('Stop!')
			window.clearInterval(intervalID);
			previousValue = finalValue;
			if (running)
				instance.animate();
			else
				drawDisplay(canvasName, displaySize, finalValue);
		}
	};

	this.setValue = function (val) {
		drawDisplay(canvasName, displaySize, val);
	};

	var reloadColor = false;
	var reloadColorConfig = function () {
//  console.log('Color scheme has changed');
		reloadColor = true;
	};

	function drawDisplay(displayCanvasName, displayRadius, displayValue) {
		if (reloadColor) {
			// In case the CSS has changed, dynamically.
			awDisplayColorConfig = getColorConfig();
			console.log("Changed theme:", awDisplayColorConfig);
		}
		reloadColor = false;

		var digitColor = awDisplayColorConfig.digitColor;

		var canvas = document.getElementById(displayCanvasName);
		var context = canvas.getContext('2d');
		context.clearRect(0, 0, canvas.width, canvas.height);

		var radius = displayRadius;

		// Cleanup
		context.fillStyle = awDisplayColorConfig.bgColor;
		context.fillRect(0, 0, canvas.width, canvas.height);

		context.beginPath();
		if (withBorder === true) {
			//  context.arc(x, y, radius, startAngle, startAngle + Math.PI, antiClockwise);
			context.arc(canvas.width / 2, radius + 10, radius, 0, 2 * Math.PI, false);
			context.lineWidth = 5;
		}
		if (awDisplayColorConfig.withGradient) {
			var grd = context.createLinearGradient(0, 5, 0, radius);
			grd.addColorStop(0, awDisplayColorConfig.displayBackgroundGradientFrom);// 0  Beginning
			grd.addColorStop(1, awDisplayColorConfig.displayBackgroundGradientTo);// 1  End
			context.fillStyle = grd;
		} else {
			context.fillStyle = awDisplayColorConfig.displayBackgroundGradientTo;
		}

		if (awDisplayColorConfig.withDisplayShadow) {
			context.shadowOffsetX = 3;
			context.shadowOffsetY = 3;
			context.shadowBlur = 3;
			context.shadowColor = awDisplayColorConfig.shadowColor;
		} else {
			context.shadowOffsetX = 0;
			context.shadowOffsetY = 0;
			context.shadowBlur = 0;
			context.shadowColor = undefined;
		}

		context.lineJoin = "round";
		context.fill();
		context.strokeStyle = awDisplayColorConfig.outlineColor;
		context.stroke();
		context.closePath();

		// Major Ticks
		context.beginPath();
		for (i = 0; i < 360; i += majorTicks) {
			xFrom = (canvas.width / 2) - ((radius * 0.95) * Math.cos(2 * Math.PI * (i / 360)));
			yFrom = (radius + 10) - ((radius * 0.95) * Math.sin(2 * Math.PI * (i / 360)));
			xTo = (canvas.width / 2) - ((radius * 0.85) * Math.cos(2 * Math.PI * (i / 360)));
			yTo = (radius + 10) - ((radius * 0.85) * Math.sin(2 * Math.PI * (i / 360)));
			context.moveTo(xFrom, yFrom);
			context.lineTo(xTo, yTo);
		}
		context.lineWidth = 3;
		context.strokeStyle = awDisplayColorConfig.majorTickColor;
		context.stroke();
		context.closePath();

		// Minor Ticks
		if (minorTicks > 0) {
			context.beginPath();
			for (i = 0; i <= 360; i += minorTicks) {
				xFrom = (canvas.width / 2) - ((radius * 0.95) * Math.cos(2 * Math.PI * (i / 360)));
				yFrom = (radius + 10) - ((radius * 0.95) * Math.sin(2 * Math.PI * (i / 360)));
				xTo = (canvas.width / 2) - ((radius * 0.90) * Math.cos(2 * Math.PI * (i / 360)));
				yTo = (radius + 10) - ((radius * 0.90) * Math.sin(2 * Math.PI * (i / 360)));
				context.moveTo(xFrom, yFrom);
				context.lineTo(xTo, yTo);
			}
			context.lineWidth = 1;
			context.strokeStyle = awDisplayColorConfig.minorTickColor;
			context.stroke();
			context.closePath();
		}

		// Numbers
		context.beginPath();
		for (i = 0; i < 360 && withDigits; i += majorTicks) {
			context.save();
			context.translate(canvas.width / 2, (radius + 10)); // canvas.height);
			context.rotate((2 * Math.PI * (i / 360)));
			context.font = "bold " + Math.round(scale * 15) + "px " + awDisplayColorConfig.font; // Like "bold 15px Arial"
			context.fillStyle = digitColor;
			str = i.toString();
			len = context.measureText(str).width;
			context.fillText(str, -len / 2, (-(radius * .8) + 10));
			context.restore();
		}
		context.closePath();

		// Arcs
		context.beginPath();
		x = canvas.width / 2;
		y = canvas.height / 2;
		context.lineWidth = 20;
		var top = 1.5 * Math.PI;
		var arcWidth = toRadians(120);

		// Starboard
		if (awDisplayColorConfig.outlinedPortStarboard === true) {
			context.beginPath();
			context.lineWidth = 2;
			context.strokeStyle = 'rgba(0, 255, 0, 0.75)';
			context.arc(x, y, radius * .75, 1.5 * Math.PI, top + arcWidth, false);
			context.arc(x, y, radius * .55, top + arcWidth, 1.5 * Math.PI, true);
			context.lineTo(x, y - (radius * .75));
			context.stroke();
			context.closePath();
		} else {
			context.beginPath();
			context.strokeStyle = 'rgba(0, 255, 0, 0.25)';
			context.arc(x, y, radius * .75, 1.5 * Math.PI, top + arcWidth, false);
			context.stroke();
			context.closePath();
		}
		// Port
		if (awDisplayColorConfig.outlinedPortStarboard === true) {
			context.beginPath();
			context.lineWidth = 2;
			context.strokeStyle = 'rgba(255, 0, 0, 0.75)';
			context.arc(x, y, radius * .75, 1.5 * Math.PI, top - arcWidth, true);
			context.arc(x, y, radius * .55, top - arcWidth, 1.5 * Math.PI, false);
			context.lineTo(x, y - (radius * .75));
			context.stroke();
			context.closePath();
		} else {
			context.beginPath();
			context.strokeStyle = 'rgba(255, 0, 0, 0.25)';
			context.arc(x, y, radius * .75, 1.5 * Math.PI, top - arcWidth, true);
			context.stroke();
			context.closePath();
		}

		// Value
//    var dv = displayValue;
//    while (dv > 360) dv -= 360;
//    while (dv < 0) dv += 360;
		text = aws.toFixed(1);
		len = 0;
		context.font = "bold " + Math.round(scale * 40) + "px " + awDisplayColorConfig.font; // "bold 40px Arial"
		var metrics = context.measureText(text);
		len = metrics.width;

		context.beginPath();
		context.fillStyle = awDisplayColorConfig.valueColor;
		context.fillText(text, (canvas.width / 2) - (len / 2), ((radius * .75) + 10));
		context.lineWidth = 1;
		context.strokeStyle = awDisplayColorConfig.valueOutlineColor;
		context.strokeText(text, (canvas.width / 2) - (len / 2), ((radius * .75) + 10)); // Outlined
		context.closePath();

		// Label ?
		if (label !== undefined) {
			var fontSize = 20;
			text = label;
			len = 0;
			context.font = "bold " + Math.round(scale * fontSize) + "px " + awDisplayColorConfig.font; // "bold 40px Arial"
			metrics = context.measureText(text);
			len = metrics.width;

			context.beginPath();
			context.fillStyle = 'rgba(255, 255, 255, 0.5)'; // TODO A CSS element analogDisplayColorConfig.valueColor;
			context.fillText(text, (canvas.width / 2) - (len / 2), (2 * radius - (fontSize * scale * 2.1)));
			context.lineWidth = 1;
			context.strokeStyle = awDisplayColorConfig.valueOutlineColor;
			context.strokeText(text, (canvas.width / 2) - (len / 2), (2 * radius - (fontSize * scale * 2.1))); // Outlined
			context.closePath();
		}

		// Hand
		context.beginPath();
		if (awDisplayColorConfig.withHandShadow) {
			context.shadowColor = awDisplayColorConfig.shadowColor;
			context.shadowOffsetX = 3;
			context.shadowOffsetY = 3;
			context.shadowBlur = 3;
		}
		// Center
		context.moveTo(canvas.width / 2, radius + 10);
		// Left
		x = (canvas.width / 2) - ((radius * 0.05) * Math.cos((2 * Math.PI * (displayValue / 360)))); //  - (Math.PI / 2))));
		y = (radius + 10) - ((radius * 0.05) * Math.sin((2 * Math.PI * (displayValue / 360)))); // - (Math.PI / 2))));
		context.lineTo(x, y);
		// Tip
		x = (canvas.width / 2) - ((radius * 0.90) * Math.cos(2 * Math.PI * (displayValue / 360) + (Math.PI / 2)));
		y = (radius + 10) - ((radius * 0.90) * Math.sin(2 * Math.PI * (displayValue / 360) + (Math.PI / 2)));
		context.lineTo(x, y);
		// Right
		x = (canvas.width / 2) - ((radius * 0.05) * Math.cos((2 * Math.PI * (displayValue / 360) + (2 * Math.PI / 2))));
		y = (radius + 10) - ((radius * 0.05) * Math.sin((2 * Math.PI * (displayValue / 360) + (2 * Math.PI / 2))));
		context.lineTo(x, y);

		context.closePath();
		context.fillStyle = awDisplayColorConfig.handColor;
		context.fill();
		context.lineWidth = 1;
		context.strokeStyle = awDisplayColorConfig.handOutlineColor;
		context.stroke();
		// Knob
		context.beginPath();
		context.arc((canvas.width / 2), (radius + 10), 7, 0, 2 * Math.PI, false);
		context.closePath();
		context.fillStyle = awDisplayColorConfig.knobColor;
		context.fill();
		context.strokeStyle = awDisplayColorConfig.knobOutlineColor;
		context.stroke();
	};
}