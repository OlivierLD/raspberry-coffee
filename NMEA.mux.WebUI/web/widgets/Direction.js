/*
 * @author Olivier Le Diouris
 */

function Direction(cName, dSize, majorTicks, minorTicks, withRose) {
	if (majorTicks === undefined) {
		majorTicks = 45;
	}
	if (minorTicks === undefined) {
		minorTicks = 0;
	}
	if (withRose === undefined) {
		withRose = false;
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
	var getColorConfig = function() {
		var colorConfig = defaultAnalogColorConfig;
		for (var s=0; s<document.styleSheets.length; s++) {
//		console.log("Walking though ", document.styleSheets[s]);
			for (var r=0; document.styleSheets[s].cssRules !== null && r<document.styleSheets[s].cssRules.length; r++) {
//			console.log(">>> ", document.styleSheets[s].cssRules[r].selectorText);
				if (document.styleSheets[s].cssRules[r].selectorText === '.analogdisplay') {
//				console.log("  >>> Found it!");
					var cssText = document.styleSheets[s].cssRules[r].style.cssText;
					var cssTextElems = cssText.split(";");
					cssTextElems.forEach(function(elem) {
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

	var defaultAnalogColorConfig = {
		bgColor: 'rgba(0, 0, 0, 0)', /* transparent, 'white', */
		digitColor: 'black',
		withGradient: true,
		displayBackgroundGradientFrom: 'LightGrey',
		displayBackgroundGradientTo: 'white',
		displayLineColor: 'rgba(0, 0, 0, 0.5)',
		labelFillColor: 'rgba(255, 255, 255, 0.5)',
		withDisplayShadow: true,
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
		font: 'Arial' /* 'Source Code Pro' */
	};

	var directionColorConfig = defaultAnalogColorConfig;

	if (events !== undefined) {
		events.subscribe('color-scheme-changed', function(val) {
//    console.log('Color scheme changed:', val);
			reloadColorConfig();
		});
	}
	directionColorConfig = getColorConfig();

	var canvasName = cName;
	var displaySize = dSize;

	var scale = dSize / 100;

	var running = false;
	this.previousValue = 0.0;
	this.intervalID = 0;
	this.valueToDisplay = 0;
	this.incr = 1;
	this.busy = false;
	var withBorder = true;

	var label;
	var instance = this;

//try { console.log('in the Direction constructor for ' + cName + " (" + dSize + ")"); } catch (e) {}

	this.setDisplaySize = function (ds) {
		scale = ds / 100;
		displaySize = ds;
		this.drawDisplay(canvasName, displaySize, instance.previousValue);
	};

	this.repaint = function () {
		this.drawDisplay(canvasName, displaySize, this.previousValue);
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
			window.clearInterval(this.intervalID);
			this.intervalID = 0;
			this.previousValue = this.valueToDisplay;
		}
	};

	var on360 = function (angle) {
		var num = parseFloat(angle);
		while (num < 0)
			num += 360;
		while (num > 360)
			num -= 360;
		return num;
	};

	this.animate = function () {
		var value;
		if (arguments.length === 1)
			value = arguments[0];
		else {
//    console.log("Generating random value");
			value = 360 * Math.random();
		}
//  console.log("Reaching Value :" + value + " from " + this.previousValue);
		diff = value - on360(this.previousValue);
		if (Math.abs(diff) > 180) // && sign(Math.cos(toRadians(value))))
		{
//    console.log("Diff > 180: new:" + value + ", prev:" + this.previousValue);
			if (value > on360(this.previousValue))
				value -= 360;
			else
				value += 360;
			diff = value - on360(this.previousValue);
		}
		this.valueToDisplay = on360(this.previousValue);

//  console.log(canvasName + " going from " + this.previousValue + " to " + value);

		this.incr = diff / 10;
//    if (diff < 0)
//      incr *= -1;
		if (this.intervalID && this.intervalID !== 0)
			window.clearInterval(this.intervalID);
		if (this.incr !== 0 && !this.busy) {
			if (canvasName === 'twdCanvas')
				console.log('Starting animation between ' + this.previousValue + ' and ' + value + ', step ' + this.incr);
			this.busy = true;
			this.intervalID = window.setTimeout(function () {
				instance.displayAndIncrement(value);
			}, 50);
		}
	};

	var sign = function (x) {
		return x > 0 ? 1 : x < 0 ? -1 : 0;
	};
	var toRadians = function (d) {
		return Math.PI * d / 180;
	};

	var toDegrees = function (d) {
		return d * 180 / Math.PI;
	};

	this.displayAndIncrement = function (finalValue) {
		//console.log('Tic ' + inc + ', ' + finalValue);
		this.drawDisplay(canvasName, displaySize, this.valueToDisplay);
		this.valueToDisplay += this.incr;
		if (canvasName === 'twdCanvas') { // DEBUG!
			console.log('       displayAndIncrement curr:' + this.valueToDisplay.toFixed(2) + ', final:' + finalValue + ', step ' + this.incr);
		}
		if ((this.incr > 0 && this.valueToDisplay.toFixed(2) >= finalValue) || (this.incr < 0 && this.valueToDisplay.toFixed(2) <= finalValue)) {
			if (canvasName === 'twdCanvas')
				console.log('Stop, ' + finalValue + ' reached, steps were ' + this.incr);
			//  console.log('Stop!')
			window.clearInterval(this.intervalID);
			this.intervalID = 0;
			this.previousValue = on360(finalValue);
			if (running)
				instance.animate();
			else
				this.drawDisplay(canvasName, displaySize, finalValue); // Final state
			this.busy = false; // Free!
		}
		else {
			window.setTimeout(function () {
				instance.displayAndIncrement(finalValue);
			}, 50);
		}
	};

	var reloadColor = false;
	var reloadColorConfig = function() {
//  console.log('Color scheme has changed');
		reloadColor = true;
	};

	this.drawDisplay = function (displayCanvasName, displayRadius, displayValue) {
		if (reloadColor) {
			// In case the CSS has changed, dynamically.
			directionColorConfig = getColorConfig();
			console.log("Changed theme:", directionColorConfig);
		}
		reloadColor = false;

		var digitColor = directionColorConfig.digitColor;

		var canvas = document.getElementById(displayCanvasName);
		var context = canvas.getContext('2d');
		context.clearRect(0, 0, canvas.width, canvas.height);

		var radius = displayRadius;

		// Cleanup
		context.fillStyle = directionColorConfig.bgColor;
		context.fillRect(0, 0, canvas.width, canvas.height);

		context.beginPath();
		if (withBorder === true) {
			//context.arc(x, y, radius, startAngle, startAngle + Math.PI, antiClockwise);
			context.arc(canvas.width / 2, radius + 10, radius, 0, 2 * Math.PI, false);
			context.lineWidth = 5;
		}
		if (directionColorConfig.withGradient) {
			var grd = context.createLinearGradient(0, 5, 0, radius);
			grd.addColorStop(0, directionColorConfig.displayBackgroundGradientFrom);// 0  Beginning
			grd.addColorStop(1, directionColorConfig.displayBackgroundGradientTo);  // 1  End
			context.fillStyle = grd;
		}
		else {
			context.fillStyle = directionColorConfig.displayBackgroundGradientTo;
		}

		if (directionColorConfig.withDisplayShadow) {
			context.shadowOffsetX = 3;
			context.shadowOffsetY = 3;
			context.shadowBlur = 3;
			context.shadowColor = directionColorConfig.shadowColor;
		} else {
			context.shadowOffsetX = 0;
			context.shadowOffsetY = 0;
			context.shadowBlur = 0;
			context.shadowColor = undefined;
		}
		context.lineJoin = "round";
		context.fill();
		context.strokeStyle = directionColorConfig.outlineColor;
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
		context.strokeStyle = directionColorConfig.majorTickColor;
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
			context.strokeStyle = directionColorConfig.minorTickColor;
			context.stroke();
			context.closePath();
		}

		// with rose?
		if (withRose === true) {
			context.beginPath();

			context.lineWidth = 1;
			var outsideRadius = radius * 0.6;
			var insideRadius = radius * 0.1;

//    context.arc(canvas.width / 2, radius + 10, outsideRadius, 0, 2 * Math.PI, false);
//    context.arc(canvas.width / 2, radius + 10, insideRadius,  0, 2 * Math.PI, false);

			// NS/EW axis, the origin is -90 (W)
			var N = (0 + 90) % 360;
			var S = (180 + 90) % 360;
			var E = (90 + 90) % 360;
			var W = (270 + 90) % 360;

			var NE = (45 + 90) % 360;
			var SE = (135 + 90) % 360;
			var NW = (315 + 90) % 360;
			var SW = (225 + 90) % 360;

			// N-S
			xFrom = (canvas.width / 2) - (outsideRadius * Math.cos(2 * Math.PI * (N / 360)));
			yFrom = (radius + 10) - (outsideRadius * Math.sin(2 * Math.PI * (N / 360)));
			xTo = (canvas.width / 2) - (outsideRadius * Math.cos(2 * Math.PI * (S / 360)));
			yTo = (radius + 10) - (outsideRadius * Math.sin(2 * Math.PI * (S / 360)));
			context.moveTo(xFrom, yFrom);
			context.lineTo(xTo, yTo);
			// E-W
			xFrom = (canvas.width / 2) - (outsideRadius * Math.cos(2 * Math.PI * (E / 360)));
			yFrom = (radius + 10) - (outsideRadius * Math.sin(2 * Math.PI * (E / 360)));
			xTo = (canvas.width / 2) - (outsideRadius * Math.cos(2 * Math.PI * (W / 360)));
			yTo = (radius + 10) - (outsideRadius * Math.sin(2 * Math.PI * (W / 360)));
			context.moveTo(xFrom, yFrom);
			context.lineTo(xTo, yTo);
			// NE-SW
			xFrom = (canvas.width / 2) - (outsideRadius * 0.9 * Math.cos(2 * Math.PI * (NE / 360)));
			yFrom = (radius + 10) - (outsideRadius * 0.9 * Math.sin(2 * Math.PI * (NE / 360)));
			xTo = (canvas.width / 2) - (outsideRadius * 0.9 * Math.cos(2 * Math.PI * (SW / 360)));
			yTo = (radius + 10) - (outsideRadius * 0.9 * Math.sin(2 * Math.PI * (SW / 360)));
			context.moveTo(xFrom, yFrom);
			context.lineTo(xTo, yTo);
			// NW-SE
			xFrom = (canvas.width / 2) - (outsideRadius * 0.9 * Math.cos(2 * Math.PI * (NW / 360)));
			yFrom = (radius + 10) - (outsideRadius * 0.9 * Math.sin(2 * Math.PI * (NW / 360)));
			xTo = (canvas.width / 2) - (outsideRadius * 0.9 * Math.cos(2 * Math.PI * (SE / 360)));
			yTo = (radius + 10) - (outsideRadius * 0.9 * Math.sin(2 * Math.PI * (SE / 360)));
			context.moveTo(xFrom, yFrom);
			context.lineTo(xTo, yTo);

			this.drawSpike(canvas, radius, outsideRadius, insideRadius, N, context);
			this.drawSpike(canvas, radius, outsideRadius, insideRadius, S, context);
			this.drawSpike(canvas, radius, outsideRadius, insideRadius, E, context);
			this.drawSpike(canvas, radius, outsideRadius, insideRadius, W, context);

			this.drawSpike(canvas, radius, outsideRadius * 0.9, insideRadius, NE, context);
			this.drawSpike(canvas, radius, outsideRadius * 0.9, insideRadius, SE, context);
			this.drawSpike(canvas, radius, outsideRadius * 0.9, insideRadius, SW, context);
			this.drawSpike(canvas, radius, outsideRadius * 0.9, insideRadius, NW, context);

			context.strokeStyle = directionColorConfig.displayLineColor;
			context.stroke();
			context.closePath();
		}

		// Numbers
		context.beginPath();
		for (i = 0; i < 360; i += majorTicks) {
			context.save();
			context.translate(canvas.width / 2, (radius + 10)); // canvas.height);
			context.rotate((2 * Math.PI * (i / 360)));
			context.font = "bold " + Math.round(scale * 15) + "px Arial"; // Like "bold 15px Arial"
			context.fillStyle = digitColor;
			str = i.toString();
			len = context.measureText(str).width;
			context.fillText(str, -len / 2, (-(radius * .8) + 10));
			context.lineWidth = 1;
			context.strokeStyle = directionColorConfig.valueOutlineColor;
			context.strokeText(str, -len / 2, (-(radius * .8) + 10)); // Outlined
			context.restore();
		}
		context.closePath();
		// Value
		var dv = parseFloat(displayValue);
		while (dv > 360) dv -= 360;
		while (dv < 0) dv += 360;
		try {
			text = dv.toFixed(directionColorConfig.valueNbDecimal);
		} catch (err) {
			console.log(err);
		}
		len = 0;
		context.font = "bold " + Math.round(scale * 40) + "px " + directionColorConfig.font; // "bold 40px Arial"
		var metrics = context.measureText(text);
		len = metrics.width;

		context.beginPath();
		context.fillStyle = directionColorConfig.valueColor;
		context.fillText(text, (canvas.width / 2) - (len / 2), ((radius * .75) + 10));
		context.lineWidth = 1;
		context.strokeStyle = directionColorConfig.valueOutlineColor;
		context.strokeText(text, (canvas.width / 2) - (len / 2), ((radius * .75) + 10)); // Outlined
		context.closePath();

		// Label ?
		if (label !== undefined) {
			var fontSize = 20;
			text = label;
			len = 0;
			context.font = "bold " + Math.round(scale * fontSize) + "px " + directionColorConfig.font; // "bold 40px Arial"
			metrics = context.measureText(text);
			len = metrics.width;

			context.beginPath();
			context.fillStyle = directionColorConfig.labelFillColor;
			context.fillText(text, (canvas.width / 2) - (len / 2), (2 * radius - (fontSize * scale * 2.1)));
			context.lineWidth = 1;
			context.strokeStyle = directionColorConfig.valueOutlineColor;
			context.strokeText(text, (canvas.width / 2) - (len / 2), (2 * radius - (fontSize * scale * 2.1))); // Outlined
			context.closePath();
		}

		// Hand
		context.beginPath();
		if (directionColorConfig.withHandShadow) {
			context.shadowColor = directionColorConfig.shadowColor;
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
		context.fillStyle = directionColorConfig.handColor;
		context.fill();
		context.lineWidth = 1;
		context.strokeStyle = directionColorConfig.handOutlineColor;
		context.stroke();
		// Knob
		context.beginPath();
		context.arc((canvas.width / 2), (radius + 10), 7, 0, 2 * Math.PI, false);
		context.closePath();
		context.fillStyle = directionColorConfig.knobColor;
		context.fill();
		context.strokeStyle = directionColorConfig.knobOutlineColor;
		context.stroke();
	};

	this.drawSpike = function (canvas, radius, outsideRadius, insideRadius, angle, context) {
		var xFrom = (canvas.width / 2) - (outsideRadius * Math.cos(2 * Math.PI * (angle / 360)));
		var yFrom = (radius + 10) - (outsideRadius * Math.sin(2 * Math.PI * (angle / 360)));
		//
		var xTo = (canvas.width / 2) - (insideRadius * Math.cos(2 * Math.PI * ((angle - 90) / 360)));
		var yTo = (radius + 10) - (insideRadius * Math.sin(2 * Math.PI * ((angle - 90) / 360)));
		context.moveTo(xFrom, yFrom);
		context.lineTo(xTo, yTo);
		//
		xTo = (canvas.width / 2) - (insideRadius * Math.cos(2 * Math.PI * ((angle + 90) / 360)));
		yTo = (radius + 10) - (insideRadius * Math.sin(2 * Math.PI * ((angle + 90) / 360)));
		context.moveTo(xFrom, yFrom);
		context.lineTo(xTo, yTo);

	};

	this.setValue = function (val) {
		instance.drawDisplay(canvasName, displaySize, val);
	};

	(function () {
		instance.drawDisplay(canvasName, displaySize, instance.previousValue);
	})(); // Invoked automatically
}
