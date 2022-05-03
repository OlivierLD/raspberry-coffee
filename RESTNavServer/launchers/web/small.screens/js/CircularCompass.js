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

function CircularCompass(cName, dSize, majorTicks, minorTicks, withRose) {

	if (majorTicks === undefined) {
		majorTicks = 45;
	}
	if (minorTicks === undefined) {
		minorTicks = 0;
	}
	if (withRose === undefined) {
		withRose = false;
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
		let colorConfig = defaultCircularCompassColorConfig;
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

	let defaultCircularCompassColorConfig = {
		bgColor: 'transparent', /* transparent, 'white', */
		digitColor: 'lime',
		withGradient: false,
		displayBackgroundGradientFrom: 'LightGrey',
		displayBackgroundGradientTo: 'transparent',
		displayLineColor: 'lime',
		labelFillColor: 'green',
		withDisplayShadow: false,
		shadowColor: 'rgba(0, 0, 0, 0.75)',
		outlineColor: 'lime',
		majorTickColor: 'lime',
		minorTickColor: 'lime',
		valueColor: 'green',
		valueOutlineColor: 'lime',
		valueNbDecimal: 0,
		crosshairColor: 'red', // 'rgba(0, 0, 100, 0.25)',
		knobColor: 'red',
		knobOutlineColor: 'cyan',
		font: 'Arial' /* 'Source Code Pro' */
	};

	let circularCompassColorConfig = defaultCircularCompassColorConfig;

	circularCompassColorConfig = getColorConfig();

	let canvasName = cName;
	let displaySize = dSize;

	let scale = dSize / 100;

	this.previousValue = 0.0;
	let withBorder = true;

	let label;
	let instance = this;

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
	let reloadColorConfig = function () {
//  console.log('Color scheme has changed');
		reloadColor = true;
	};

	this.drawDisplay = function (displayCanvasName, displayRadius, displayValue) {
		if (reloadColor) {
			// In case the CSS has changed, dynamically.
			circularCompassColorConfig = getColorConfig();
			console.log("Changed theme:", circularCompassColorConfig);
		}
		reloadColor = false;

		let digitColor = circularCompassColorConfig.digitColor;

		let canvas = document.getElementById(displayCanvasName);
		let context = canvas.getContext('2d');
		context.clearRect(0, 0, canvas.width, canvas.height);

		let radius = displayRadius;

		// Cleanup
		context.fillStyle = circularCompassColorConfig.bgColor;
		context.fillRect(0, 0, canvas.width, canvas.height);

		context.beginPath();
		if (withBorder === true) {
			//context.arc(x, y, radius, startAngle, startAngle + Math.PI, antiClockwise);
			context.arc(canvas.width / 2, (canvas.height / 2), radius, 0, 2 * Math.PI, false);
			context.lineWidth = 5;
		}
		if (circularCompassColorConfig.withGradient) {
			let grd = context.createLinearGradient(0, 5, 0, radius);
			grd.addColorStop(0, circularCompassColorConfig.displayBackgroundGradientFrom);// 0  Beginning
			grd.addColorStop(1, circularCompassColorConfig.displayBackgroundGradientTo);  // 1  End
			context.fillStyle = grd;
		} else {
			context.fillStyle = circularCompassColorConfig.displayBackgroundGradientTo;
		}

		if (circularCompassColorConfig.withDisplayShadow) {
			context.shadowOffsetX = 3;
			context.shadowOffsetY = 3;
			context.shadowBlur = 3;
			context.shadowColor = circularCompassColorConfig.shadowColor;
		} else {
			context.shadowOffsetX = 0;
			context.shadowOffsetY = 0;
			context.shadowBlur = 0;
			context.shadowColor = undefined;
		}
		context.lineJoin = "round";
		context.fill();
		context.strokeStyle = circularCompassColorConfig.outlineColor;
		context.stroke();
		context.closePath();

		var xFrom, yFrom, xTo, yTo;
		// Major Ticks
		context.beginPath();
		for (let i = 0; i < 360; i += majorTicks) {
			let heading = i - displayValue;
			xFrom = (canvas.width / 2) - ((radius * 0.95) * Math.cos(2 * Math.PI * (heading / 360)));
			yFrom = (canvas.height / 2) - ((radius * 0.95) * Math.sin(2 * Math.PI * (heading / 360)));
			xTo = (canvas.width / 2) - ((radius * 0.85) * Math.cos(2 * Math.PI * (heading / 360)));
			yTo = (canvas.height / 2) - ((radius * 0.85) * Math.sin(2 * Math.PI * (heading / 360)));
			context.moveTo(xFrom, yFrom);
			context.lineTo(xTo, yTo);
		}
		context.lineWidth = 3;
		context.strokeStyle = circularCompassColorConfig.majorTickColor;
		context.stroke();
		context.closePath();

		// Minor Ticks
		if (minorTicks > 0) {
			context.beginPath();
			for (let i = 0; i <= 360; i += minorTicks) {
				let heading = i - displayValue;
				xFrom = (canvas.width / 2) - ((radius * 0.95) * Math.cos(2 * Math.PI * (heading / 360)));
				yFrom = (canvas.height / 2) - ((radius * 0.95) * Math.sin(2 * Math.PI * (heading / 360)));
				xTo = (canvas.width / 2) - ((radius * 0.90) * Math.cos(2 * Math.PI * (heading / 360)));
				yTo = (canvas.height / 2) - ((radius * 0.90) * Math.sin(2 * Math.PI * (heading / 360)));
				context.moveTo(xFrom, yFrom);
				context.lineTo(xTo, yTo);
			}
			context.lineWidth = 1;
			context.strokeStyle = circularCompassColorConfig.minorTickColor;
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
			let N = (0 + 90 - displayValue) % 360;
			let S = (180 + 90 - displayValue) % 360;
			let E = (90 + 90 - displayValue) % 360;
			let W = (270 + 90 - displayValue) % 360;

			let NE = (45 + 90 - displayValue) % 360;
			let SE = (135 + 90 - displayValue) % 360;
			let NW = (315 + 90 - displayValue) % 360;
			let SW = (225 + 90 - displayValue) % 360;

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

			context.strokeStyle = circularCompassColorConfig.displayLineColor;
			context.stroke();
			context.closePath();
		}

		// Numbers
		let replaceWithCardPoints = true;
		let cardPoints = ["N", "NE", "E", "SE", "S", "SW", "W", "NW"];

		context.beginPath();
		for (let i = 0; i < 360; i += majorTicks) {
			let heading = i - displayValue;
			context.save();
			context.translate(canvas.width / 2, (canvas.height / 2)); // canvas.height);
			context.rotate((2 * Math.PI * (heading / 360)));
			context.font = "bold " + Math.round(scale * 15) + "px Arial"; // Like "bold 15px Arial"
			context.fillStyle = digitColor;
			var str;
			if (replaceWithCardPoints) {
				if (i % 45 === 0) {
					str = cardPoints[i / 45];
				} else {
					str = i.toString();
				}
			} else {
				str = i.toString();
			}

			let len = context.measureText(str).width;
			context.fillText(str, -len / 2, (-(radius * .8) + 10));
			context.lineWidth = 1;
			context.strokeStyle = circularCompassColorConfig.valueOutlineColor;
			context.strokeText(str, -len / 2, (-(radius * .8) + 10)); // Outlined
			context.restore();
		}
		context.closePath();
		// Value
		let dv = parseFloat(displayValue);
		while (dv > 360) dv -= 360;
		while (dv < 0) dv += 360;
		var text = '';
		try {
			text = dv.toFixed(circularCompassColorConfig.valueNbDecimal);
		} catch (err) {
			console.log(err);
		}
		let len = 0;
		context.font = "bold " + Math.round(scale * 40) + "px " + circularCompassColorConfig.font; // "bold 40px Arial"
		let metrics = context.measureText(text);
		len = metrics.width;

		context.beginPath();
		context.fillStyle = circularCompassColorConfig.valueColor;
		context.fillText(text, (canvas.width / 2) - (len / 2), ((radius * .75) + 10));
		context.lineWidth = 1;
		context.strokeStyle = circularCompassColorConfig.valueOutlineColor;
		context.strokeText(text, (canvas.width / 2) - (len / 2), ((radius * .75) + 10)); // Outlined
		context.closePath();

		// Label ?
		if (label !== undefined) {
			let fontSize = 20;
			text = label;
			len = 0;
			context.font = "bold " + Math.round(scale * fontSize) + "px " + circularCompassColorConfig.font; // "bold 40px Arial"
			metrics = context.measureText(text);
			len = metrics.width;

			context.beginPath();
			context.fillStyle = circularCompassColorConfig.labelFillColor;
			context.fillText(text, (canvas.width / 2) - (len / 2), (2 * radius - (fontSize * scale * 2.1)));
			context.lineWidth = 1;
			context.strokeStyle = circularCompassColorConfig.valueOutlineColor;
			context.strokeText(text, (canvas.width / 2) - (len / 2), (2 * radius - (fontSize * scale * 2.1))); // Outlined
			context.closePath();
		}

		// No Hand, cross-hair, fixed.
		context.beginPath();
		context.lineWidth = 1;
		context.strokeStyle = circularCompassColorConfig.crosshairColor;
		// Left
		context.moveTo((canvas.width / 2) - 3, (canvas.height / 2));
		context.lineTo((canvas.width / 2) - 3, 10);
		context.stroke();
		// Right
		context.moveTo((canvas.width / 2) + 3, (canvas.height / 2));
		context.lineTo((canvas.width / 2) + 3, 10);
		context.stroke();

		context.closePath();

		// Knob
		context.beginPath();
		context.arc((canvas.width / 2), (canvas.height / 2), 7, 0, 2 * Math.PI, false);
		context.closePath();
		context.fillStyle = circularCompassColorConfig.knobColor;
		context.fill();
		context.strokeStyle = circularCompassColorConfig.knobOutlineColor;
		context.stroke();
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
