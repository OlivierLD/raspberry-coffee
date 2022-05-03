/*
 * @author Olivier Le Diouris
 * This is NOT a Web Component.
 * Works on non-web-components-savvy browsers
 */
"use strict";

if (Math.toDegrees === undefined) {
	Math.toDegrees = (rad) => {
		return rad * (180 / Math.PI);
	};
}

if (Math.toRadians === undefined) {
	Math.toRadians = (deg) => {
		return deg * (Math.PI / 180);
	};
}

if (Math.sign === undefined) {
	Math.sign = (x) => {
		return x > 0 ? 1 : x < 0 ? -1 : 0;
	};
}

function Direction(cName, dSize, majorTicks, minorTicks, withRose, windArrow) {
	if (majorTicks === undefined) {
		majorTicks = 45;
	}
	if (minorTicks === undefined) {
		minorTicks = 0;
	}
	if (withRose === undefined) {
		withRose = false;
	}
	if (windArrow === undefined) {
		windArrow = false;
	}

	/**
	 * Recurse from the top down, on styleSheets and cssRules
	 *
	 * document.styleSheets[0].cssRules[2].selectorText returns ".analogdisplay"
	 * document.styleSheets[0].cssRules[2].cssText returns ".analogdisplay { --hand-color: red;  --face-color: white; }"
	 * document.styleSheets[0].cssRules[2].style.cssText returns "--hand-color: red; --face-color: white;"
	 *
	 * spine-case to camelCase
	 */
	function getColorConfig() {
		let colorConfig = defaultAnalogColorConfig;
		for (let s = 0; s < document.styleSheets.length; s++) {
//		console.log("Walking though ", document.styleSheets[s]);
			for (let r = 0; document.styleSheets[s].cssRules !== null && r < document.styleSheets[s].cssRules.length; r++) {
//			console.log(">>> ", document.styleSheets[s].cssRules[r].selectorText);
				if (document.styleSheets[s].cssRules[r].selectorText === '.analogdisplay') {
//				console.log("  >>> Found it!");
					let cssText = document.styleSheets[s].cssRules[r].style.cssText;
					let cssTextElems = cssText.split(";");
					cssTextElems.forEach((elem) => {
						if (elem.trim().length > 0) {
							let keyValPair = elem.split(":");
							let key = keyValPair[0].trim();
							let value = keyValPair[1].trim();
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

	let defaultAnalogColorConfig = {
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
		valueNbDecimal: 0,
		handColor: 'red', // 'rgba(0, 0, 100, 0.25)',
		handOutlineColor: 'black',
		withHandShadow: true,
		knobColor: 'DarkGrey',
		knobOutlineColor: 'black',
		font: 'Arial' /* 'Source Code Pro' */
	};

	let directionColorConfig = defaultAnalogColorConfig;

	directionColorConfig = getColorConfig();

	let canvasName = cName;
	let displaySize = dSize;

	let scale = dSize / 100;

	this.previousValue = 0.0;
	this.intervalID = 0;
	this.valueToDisplay = 0;
	this.incr = 1;
	this.busy = false;
	let withBorder = true;

	let label;
	let instance = this;

	let cbBefore, cbAfter;

	this.setDisplaySize = function (ds) {
		scale = ds / 100;
		displaySize = ds;
		this.drawDisplay(canvasName, displaySize, instance.previousValue);
	};

	this.setCbBefore = function(cb) {
		cbBefore = cb;
	}
	this.setCbAfter = function(cb) {
		cbAfter = cb;
	}

	this.repaint = function () {
		this.drawDisplay(canvasName, displaySize, this.previousValue);
	};

	this.setBorder = function (b) {
		withBorder = b;
	};

	this.setLabel = function (lbl) {
		label = lbl;
	};

	let on360 = function (angle) {
		let num = parseFloat(angle);
		while (num < 0) {
			num += 360;
		}
		while (num > 360) {
			num -= 360;
		}
		return num;
	};

	let reloadColor = false;

	this.drawDisplay = function (displayCanvasName, displayRadius, displayValue) {
		if (reloadColor) {
			// In case the CSS has changed, dynamically.
			directionColorConfig = getColorConfig();
			console.log("Changed theme:", directionColorConfig);
		}
		reloadColor = false;

		let digitColor = directionColorConfig.digitColor;

		let canvas = document.getElementById(displayCanvasName);
		let context = canvas.getContext('2d');
		context.clearRect(0, 0, canvas.width, canvas.height);

		let radius = displayRadius;

		// Cleanup
		context.fillStyle = directionColorConfig.bgColor;
		context.fillRect(0, 0, canvas.width, canvas.height);

		if (cbBefore !== undefined) {
			// TODO Implement
		}

		context.beginPath();
		if (withBorder === true) {
			//context.arc(x, y, radius, startAngle, startAngle + Math.PI, antiClockwise);
			context.arc(canvas.width / 2, (canvas.height / 2), radius, 0, 2 * Math.PI, false);
			context.lineWidth = 5;
		}
		if (directionColorConfig.withGradient) {
			let grd = context.createLinearGradient(0, 5, 0, radius);
			grd.addColorStop(0, directionColorConfig.displayBackgroundGradientFrom);// 0  Beginning
			grd.addColorStop(1, directionColorConfig.displayBackgroundGradientTo);  // 1  End
			context.fillStyle = grd;
		} else {
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

		var xFrom, yFrom, xTo, yTo;
		// Major Ticks
		context.beginPath();
		for (let i = 0; i < 360; i += majorTicks) {
			xFrom = (canvas.width / 2) - ((radius * 0.95) * Math.cos(2 * Math.PI * (i / 360)));
			yFrom = (canvas.height / 2) - ((radius * 0.95) * Math.sin(2 * Math.PI * (i / 360)));
			xTo = (canvas.width / 2) - ((radius * 0.85) * Math.cos(2 * Math.PI * (i / 360)));
			yTo = (canvas.height / 2) - ((radius * 0.85) * Math.sin(2 * Math.PI * (i / 360)));
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
			for (let i = 0; i <= 360; i += minorTicks) {
				xFrom = (canvas.width / 2) - ((radius * 0.95) * Math.cos(2 * Math.PI * (i / 360)));
				yFrom = (canvas.height / 2) - ((radius * 0.95) * Math.sin(2 * Math.PI * (i / 360)));
				xTo = (canvas.width / 2) - ((radius * 0.90) * Math.cos(2 * Math.PI * (i / 360)));
				yTo = (canvas.height / 2) - ((radius * 0.90) * Math.sin(2 * Math.PI * (i / 360)));
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
			let outsideRadius = radius * 0.6;
			let insideRadius = radius * 0.1;

//    context.arc(canvas.width / 2, radius + 10, outsideRadius, 0, 2 * Math.PI, false);
//    context.arc(canvas.width / 2, radius + 10, insideRadius,  0, 2 * Math.PI, false);

			// NS/EW axis, the origin is -90 (W)
			let N = (0 + 90) % 360;
			let S = (180 + 90) % 360;
			let E = (90 + 90) % 360;
			let W = (270 + 90) % 360;

			let NE = (45 + 90) % 360;
			let SE = (135 + 90) % 360;
			let NW = (315 + 90) % 360;
			let SW = (225 + 90) % 360;

			// N-S
			xFrom = (canvas.width / 2) - (outsideRadius * Math.cos(2 * Math.PI * (N / 360)));
			yFrom = (canvas.height / 2) - (outsideRadius * Math.sin(2 * Math.PI * (N / 360)));
			xTo = (canvas.width / 2) - (outsideRadius * Math.cos(2 * Math.PI * (S / 360)));
			yTo = (canvas.height / 2) - (outsideRadius * Math.sin(2 * Math.PI * (S / 360)));
			context.moveTo(xFrom, yFrom);
			context.lineTo(xTo, yTo);
			// E-W
			xFrom = (canvas.width / 2) - (outsideRadius * Math.cos(2 * Math.PI * (E / 360)));
			yFrom = (canvas.height / 2) - (outsideRadius * Math.sin(2 * Math.PI * (E / 360)));
			xTo = (canvas.width / 2) - (outsideRadius * Math.cos(2 * Math.PI * (W / 360)));
			yTo = (canvas.height / 2) - (outsideRadius * Math.sin(2 * Math.PI * (W / 360)));
			context.moveTo(xFrom, yFrom);
			context.lineTo(xTo, yTo);
			// NE-SW
			xFrom = (canvas.width / 2) - (outsideRadius * 0.9 * Math.cos(2 * Math.PI * (NE / 360)));
			yFrom = (canvas.height / 2) - (outsideRadius * 0.9 * Math.sin(2 * Math.PI * (NE / 360)));
			xTo = (canvas.width / 2) - (outsideRadius * 0.9 * Math.cos(2 * Math.PI * (SW / 360)));
			yTo = (canvas.height / 2) - (outsideRadius * 0.9 * Math.sin(2 * Math.PI * (SW / 360)));
			context.moveTo(xFrom, yFrom);
			context.lineTo(xTo, yTo);
			// NW-SE
			xFrom = (canvas.width / 2) - (outsideRadius * 0.9 * Math.cos(2 * Math.PI * (NW / 360)));
			yFrom = (canvas.height / 2) - (outsideRadius * 0.9 * Math.sin(2 * Math.PI * (NW / 360)));
			xTo = (canvas.width / 2) - (outsideRadius * 0.9 * Math.cos(2 * Math.PI * (SE / 360)));
			yTo = (canvas.height / 2) - (outsideRadius * 0.9 * Math.sin(2 * Math.PI * (SE / 360)));
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
		for (let i = 0; i < 360; i += majorTicks) {
			context.save();
			context.translate(canvas.width / 2, (canvas.height / 2)); // canvas.height);
			context.rotate((2 * Math.PI * (i / 360)));
			context.font = "bold " + Math.round(scale * 15) + "px Arial"; // Like "bold 15px Arial"
			context.fillStyle = digitColor;
			let str = i.toString();
			let len = context.measureText(str).width;
			context.fillText(str, -len / 2, (-(radius * .8) + 10));
			context.lineWidth = 1;
			context.strokeStyle = directionColorConfig.valueOutlineColor;
			context.strokeText(str, -len / 2, (-(radius * .8) + 10)); // Outlined
			context.restore();
		}
		context.closePath();
		// Value
		let dv = parseFloat(displayValue);
		while (dv > 360) dv -= 360;
		while (dv < 0) dv += 360;
		let text = '';
		try {
			text = dv.toFixed(directionColorConfig.valueNbDecimal);
		} catch (err) {
			console.log(err);
		}
		let len = 0;
		context.font = "bold " + Math.round(scale * 40) + "px " + directionColorConfig.font; // "bold 40px Arial"
		let metrics = context.measureText(text);
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
			let fontSize = 20;
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
		context.moveTo(canvas.width / 2, (canvas.height / 2));
		// Left
		let x = (canvas.width / 2) - ((radius * 0.05) * Math.cos((2 * Math.PI * (displayValue / 360)))); //  - (Math.PI / 2))));
		let y = (canvas.height / 2) - ((radius * 0.05) * Math.sin((2 * Math.PI * (displayValue / 360)))); // - (Math.PI / 2))));
		context.lineTo(x, y);
		if (windArrow !== true) { // Regular needle
			// Tip
			x = (canvas.width / 2) - ((radius * 0.90) * Math.cos(2 * Math.PI * (displayValue / 360) + (Math.PI / 2)));
			y = (canvas.height / 2) - ((radius * 0.90) * Math.sin(2 * Math.PI * (displayValue / 360) + (Math.PI / 2)));
			context.lineTo(x, y);
		} else {                    // Then draw wind arrow
			/*
			    +-+
			    | |
			    | |
			  +-+ +-+
			   \   /
			    + +
			    | |
			    | |
			    +-+
			 */

			let arrowPoints = [
				{x: -radius * 0.04, y: -radius * 0.30}, // Left pointy side of the arrow head
				{x: -radius * 0.20, y: -radius * 0.60}, // Left back fat side of the arrow head
				{x: -radius * 0.04, y: -radius * 0.60}, // Left back narrow side of the arrow head
				{x: -radius * 0.04, y: -radius * 0.90}, // Left tip
				{x: +radius * 0.04, y: -radius * 0.90}, // Right tip
				{x: +radius * 0.04, y: -radius * 0.60}, // Right back narrow side of the arrow head
				{x: +radius * 0.20, y: -radius * 0.60}, // Right back fat side of the arrow head
				{x: +radius * 0.04, y: -radius * 0.30}  // Right pointy side of the arrow head
			];
			let radAngle = Math.toRadians(dv); // + (Math.PI / 2);
			// Apply rotation to the points of the needle
			arrowPoints.forEach(pt => {
				x = (canvas.width / 2) + ((pt.x * Math.cos(radAngle)) - (pt.y * Math.sin(radAngle)));
				y = (canvas.height / 2) + ((pt.x * Math.sin(radAngle)) + (pt.y * Math.cos(radAngle)));
				context.lineTo(x, y);
			});
		}
		// Right
		x = (canvas.width / 2) - ((radius * 0.05) * Math.cos((2 * Math.PI * (displayValue / 360) + (2 * Math.PI / 2))));
		y = (canvas.height / 2) - ((radius * 0.05) * Math.sin((2 * Math.PI * (displayValue / 360) + (2 * Math.PI / 2))));
		context.lineTo(x, y);

		context.closePath();
		context.fillStyle = directionColorConfig.handColor;
		context.fill();
		context.lineWidth = 1;
		context.strokeStyle = directionColorConfig.handOutlineColor;
		context.stroke();
		// Knob
		context.beginPath();
		context.arc((canvas.width / 2), (canvas.height / 2), 7, 0, 2 * Math.PI, false);
		context.closePath();
		context.fillStyle = directionColorConfig.knobColor;
		context.fill();
		context.strokeStyle = directionColorConfig.knobOutlineColor;
		context.stroke();

		if (cbAfter !== undefined) {
			x = (canvas.width / 2) - ((radius * 0.90) * Math.cos(2 * Math.PI * (displayValue / 360) + (Math.PI / 2)));
			y = (canvas.height / 2) - ((radius * 0.90) * Math.sin(2 * Math.PI * (displayValue / 360) + (Math.PI / 2)));
			cbAfter(context, radius, displayValue, { x: x, y: y });
		}

	};

	this.drawSpike = function (canvas, radius, outsideRadius, insideRadius, angle, context) {
		let xFrom = (canvas.width / 2) - (outsideRadius * Math.cos(2 * Math.PI * (angle / 360)));
		let yFrom = (canvas.height / 2) - (outsideRadius * Math.sin(2 * Math.PI * (angle / 360)));
		//
		let xTo = (canvas.width / 2) - (insideRadius * Math.cos(2 * Math.PI * ((angle - 90) / 360)));
		let yTo = (canvas.height / 2) - (insideRadius * Math.sin(2 * Math.PI * ((angle - 90) / 360)));
		context.moveTo(xFrom, yFrom);
		context.lineTo(xTo, yTo);
		//
		xTo = (canvas.width / 2) - (insideRadius * Math.cos(2 * Math.PI * ((angle + 90) / 360)));
		yTo = (canvas.height / 2) - (insideRadius * Math.sin(2 * Math.PI * ((angle + 90) / 360)));
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
