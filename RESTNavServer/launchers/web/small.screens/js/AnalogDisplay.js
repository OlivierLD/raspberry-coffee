/*
 * @author Olivier LeDiouris
 * This is NOT a Web Component.
 * Works on non-web-components-savvy browsers
 */
"use strict";

const ANALOG_DEBUG = false;

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

function AnalogDisplay(cName,                     // Canvas Name
                       dSize,                     // Display radius
                       maxValue,                  // default 10
                       majorTicks,                // default 1
                       minorTicks,                // default 0
                       withDigits,                // default true, boolean
                       overlapOver180InDegree,    // default 0 (will display half circle), beyond horizontal, in degrees, before 0, after 180
                       startValue,                // default 0, In case it is not 0
                       nbDecimal) {               // default 1, nb decimals in the value display

	if (maxValue === undefined) {
		maxValue = 10;
	}
	if (majorTicks === undefined) {
		majorTicks = 1;
	}
	if (minorTicks === undefined) {
		minorTicks = 0;
	}
	if (withDigits === undefined) {
		withDigits = true;
	}
	if (overlapOver180InDegree === undefined) {
		overlapOver180InDegree = 0;
	}
	if (startValue === undefined) {
		startValue = 0;
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
					cssTextElems.forEach(function (elem) {
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
	}

	const defaultAnalogColorConfig = {
		bgColor: 'rgba(0, 0, 0, 0)', /* transparent, 'white', */
		digitColor: 'black',
		withGradient: true,
		displayBackgroundGradientFrom: 'LightGrey',
		displayBackgroundGradientTo: 'white',
		displayLineColor: 'rgba(0, 0, 0, 0.5)',
		labelFillColor: 'rgba(255, 255, 255, 0.5)',
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
		font: 'Arial' /* 'Source Code Pro' */
	};

	let analogDisplayColorConfig = defaultAnalogColorConfig; // analogDisplayColorConfigBlack; // White is the default

	if (nbDecimal === undefined) {
		nbDecimal = analogDisplayColorConfig.valueNbDecimal;
	}

	analogDisplayColorConfig = getColorConfig();

	let scale = dSize / 100;

	let canvasName = cName;
	let displaySize = dSize; // Radius

	let previousValue = startValue;
	let nbDec = nbDecimal;
	let withBorder = true;

	let withMinMax = false;
	let miniVal = 10000000;
	let maxiVal = -10000000;

	let label;
	let digits; // Number of digits to display in the lower part, like daily log value...
	let digitValue;

	let instance = this;

	(function () {
		drawDisplay(canvasName, displaySize, previousValue);
	})(); // Invoked automatically

	this.repaint = () => {
		drawDisplay(canvasName, displaySize, previousValue);
	};

	this.setWithMinMax = (b) => {
		withMinMax = b;
	};
	this.setNbDec = (nb) => {
		nbDec = nb;
	};
	this.setBorder = (b) => {
		withBorder = b;
	};
	this.setLabel = (lbl) => {
		label = lbl;
	};
	this.setDigits = (d) => {
		digits = d;
	};
	this.setDigitValue = (val) => {
		digitValue = val;
	};

	this.repaint = () => {
		drawDisplay(canvasName, displaySize, previousValue);
	};

	this.setDisplaySize = (ds) => {
		scale = ds / 100;
		displaySize = ds;
		drawDisplay(canvasName, displaySize, previousValue);
	};

	function drawDisplay(displayCanvasName, displayRadius, displayValue) {

		let digitColor = analogDisplayColorConfig.digitColor;

		let canvas = document.getElementById(displayCanvasName);
		let context = canvas.getContext('2d');
		context.clearRect(0, 0, canvas.width, canvas.height);

		let radius = displayRadius;

		// Cleanup
		context.fillStyle = analogDisplayColorConfig.bgColor;
		context.fillRect(0, 0, canvas.width, canvas.height);

		let totalAngle = (Math.PI + (2 * (Math.toRadians(overlapOver180InDegree))));

		context.beginPath();

		if (withBorder === true) {
			//  context.arc(x, y, radius, startAngle, startAngle + Math.PI, antiClockwise);
			context.arc(canvas.width / 2, canvas.height / 2, radius, Math.PI - Math.toRadians(overlapOver180InDegree > 0 ? 90 : 0), (2 * Math.PI) + Math.toRadians(overlapOver180InDegree > 0 ? 90 : 0), false);
			context.lineWidth = 5;
		}

		if (analogDisplayColorConfig.withGradient) {
			let grd = context.createLinearGradient(0, 5, 0, radius);
			grd.addColorStop(0, analogDisplayColorConfig.displayBackgroundGradientFrom);// 0  Beginning
			grd.addColorStop(1, analogDisplayColorConfig.displayBackgroundGradientTo);  // 1  End
			context.fillStyle = grd;
		} else {
			context.fillStyle = analogDisplayColorConfig.displayBackgroundGradientTo;
		}
		if (analogDisplayColorConfig.withDisplayShadow) {
			context.shadowOffsetX = 3;
			context.shadowOffsetY = 3;
			context.shadowBlur = 3;
			context.shadowColor = analogDisplayColorConfig.shadowColor;
		} else {
			context.shadowOffsetX = 0;
			context.shadowOffsetY = 0;
			context.shadowBlur = 0;
			context.shadowColor = undefined;
		}
		context.lineJoin = "round";
		context.fill();
		context.strokeStyle = analogDisplayColorConfig.outlineColor;
		context.stroke();
		context.closePath();

		// Min-max?
		if (withMinMax && miniVal < maxiVal) {
			context.beginPath();

			let ___minAngle = (totalAngle * ((miniVal - startValue) / (maxValue - startValue))) - Math.toRadians(overlapOver180InDegree) - (Math.PI);
			let ___maxAngle = (totalAngle * ((maxiVal - startValue) / (maxValue - startValue))) - Math.toRadians(overlapOver180InDegree) - (Math.PI);

			//Center
			context.moveTo(canvas.width / 2, canvas.height / 2);
			context.arc(canvas.width / 2, canvas.height / 2, radius * 0.75,
					(___minAngle),
					(___maxAngle),
					false);

//    context.arc(288, 75, 70, 0, Math.PI, false);
			context.closePath();
			context.lineWidth = 1;
			context.fillStyle = 'gray';
			context.fill();
//    context.strokeStyle = '#550000';
//    context.stroke();
		}

		// Major Ticks
		context.beginPath();
		for (let i = 0; i <= (maxValue - startValue); i += majorTicks) {
			let currentAngle = (totalAngle * (i / (maxValue - startValue))) - Math.toRadians(overlapOver180InDegree);
			let xFrom = (canvas.width / 2) - ((radius * 0.95) * Math.cos(currentAngle));
			let yFrom = (canvas.height / 2) - ((radius * 0.95) * Math.sin(currentAngle));
			let xTo = (canvas.width / 2) - ((radius * 0.85) * Math.cos(currentAngle));
			let yTo = (canvas.height / 2) - ((radius * 0.85) * Math.sin(currentAngle));
			context.moveTo(xFrom, yFrom);
			context.lineTo(xTo, yTo);
		}
		context.lineWidth = 3;
		context.strokeStyle = analogDisplayColorConfig.majorTickColor;
		context.stroke();
		context.closePath();

		// Minor Ticks
		if (minorTicks > 0) {
			context.beginPath();
			for (let i = 0; i <= (maxValue - startValue); i += minorTicks) {
				let _currentAngle = (totalAngle * (i / (maxValue - startValue))) - Math.toRadians(overlapOver180InDegree);

				let xFrom = (canvas.width / 2) - ((radius * 0.95) * Math.cos(_currentAngle));
				let yFrom = (canvas.height / 2) - ((radius * 0.95) * Math.sin(_currentAngle));
				let xTo = (canvas.width / 2) - ((radius * 0.90) * Math.cos(_currentAngle));
				let yTo = (canvas.height / 2) - ((radius * 0.90) * Math.sin(_currentAngle));
				context.moveTo(xFrom, yFrom);
				context.lineTo(xTo, yTo);
			}
			context.lineWidth = 1;
			context.strokeStyle = analogDisplayColorConfig.minorTickColor;
			context.stroke();
			context.closePath();
		}

		// Numbers
		if (withDigits) {
			context.beginPath();
			for (let i = 0; i <= (maxValue - startValue); i += majorTicks) {
				context.save();
				context.translate(canvas.width / 2, (canvas.height / 2)); // canvas.height);
				let __currentAngle = (totalAngle * (i / (maxValue - startValue))) - Math.toRadians(overlapOver180InDegree);
//      context.rotate((Math.PI * (i / maxValue)) - (Math.PI / 2));
				context.rotate(__currentAngle - (Math.PI / 2));
				context.font = "bold " + Math.round(scale * 15) + "px " + analogDisplayColorConfig.font; // Like "bold 15px Arial"
				context.fillStyle = digitColor;
				let str = (i + startValue).toString();
				let len = context.measureText(str).width;
				context.fillText(str, -len / 2, (-(radius * .8) + 10));
				context.lineWidth = 1;
				context.strokeStyle = analogDisplayColorConfig.valueOutlineColor;
				context.strokeText(str, -len / 2, (-(radius * .8) + 10)); // Outlined
				context.restore();
			}
			context.closePath();
		}

		// Value
		let text = displayValue.toFixed(nbDec);
//  text = displayValue.toFixed(nbDecimal); // analogDisplayColorConfig.valueNbDecimal);
		context.font = "bold " + Math.round(scale * 40) + "px " + analogDisplayColorConfig.font; // "bold 40px Arial"
		let metrics = context.measureText(text);
		let len = metrics.width;

		context.beginPath();
		context.fillStyle = analogDisplayColorConfig.valueColor;
		context.fillText(text, (canvas.width / 2) - (len / 2), ((radius * .75) + 10));
		context.lineWidth = 1;
		context.strokeStyle = analogDisplayColorConfig.valueOutlineColor;
		context.strokeText(text, (canvas.width / 2) - (len / 2), ((radius * .75) + 10)); // Outlined
		context.closePath();

		// Label ?
		if (label !== undefined) {
			if (ANALOG_DEBUG) {
				console.log('Label', label);
			}
			let fontSize = 20;
			text = label;
			context.font = "bold " + Math.round(scale * fontSize) + "px " + analogDisplayColorConfig.font; // "bold 40px Arial"
			metrics = context.measureText(text);
			len = metrics.width;

			context.beginPath();
			context.fillStyle = analogDisplayColorConfig.labelFillColor;
			context.fillText(text, (canvas.width / 2) - (len / 2), (2 * radius - (fontSize * scale * 2.1)));
			context.lineWidth = 1;
			context.strokeStyle = analogDisplayColorConfig.valueOutlineColor;
			context.strokeText(text, (canvas.width / 2) - (len / 2), (2 * radius - (fontSize * scale * 2.1))); // Outlined
			context.closePath();
		} else {
			if (ANALOG_DEBUG) {
				console.log('No label');
			}
		}

		// Digits? Note: not compatible with label (above), would hide it. Example: Log Value
		if (digits !== undefined) {
			let oneDigitWidth = (canvas.width / 3) / digits;
			let oneDigitHeight = oneDigitWidth * 1.4;

			if (analogDisplayColorConfig.withGradient) {
				let start = 1.025 * (canvas.height / 2);
				let grd = context.createLinearGradient(0, start, 0, start + oneDigitHeight);
				grd.addColorStop(0, analogDisplayColorConfig.displayBackgroundGradientTo);   // 0  Beginning
				grd.addColorStop(1, analogDisplayColorConfig.displayBackgroundGradientFrom); // 1  End
				context.fillStyle = grd;
			} else {
				context.fillStyle = analogDisplayColorConfig.displayBackgroundGradientTo;
			}

			// The rectangles around each digit
			let digitOrigin = (canvas.width / 2) - ((digits * oneDigitWidth) / 2);
			for (let i = 0; i < digits; i++) {
				context.beginPath();

				let x = digitOrigin + (i * oneDigitWidth);
				let y = 1.025 * (canvas.height / 2);
				context.fillRect(x, y, oneDigitWidth, oneDigitHeight);
				context.lineWidth = 1;
				context.strokeStyle = analogDisplayColorConfig.displayLineColor;
				context.rect(x, y, oneDigitWidth, oneDigitHeight);
				context.stroke();
				context.closePath();
			}

			context.shadowOffsetX = 0;
			context.shadowOffsetY = 0;
			context.shadowBlur = 0;

			// Value
			if (digitValue !== undefined) {
				text = digitValue.toFixed(0);
				while (text.length < digits) {
					text = '0' + text;
				}
				let fontSize = Math.round(scale * 14);
				for (let i = 0; i < digits; i++) {
					len = 0;
					context.font = "bold " + fontSize + "px Arial"; // "bold 40px Arial"
					let txt = text.substring(i, i + 1);
					let metrics = context.measureText(txt);
					len = metrics.width;
					let x = digitOrigin + (i * oneDigitWidth);
					let y = 1.025 * (canvas.height / 2);
					context.beginPath();
					context.fillStyle = analogDisplayColorConfig.valueColor;
					context.fillText(txt, x + (oneDigitWidth / 2) - (len / 2), y + (oneDigitHeight / 2) + (fontSize / 2));
					context.lineWidth = 1;
					context.strokeStyle = analogDisplayColorConfig.valueOutlineColor;
					context.strokeText(txt, x + (oneDigitWidth / 2) - (len / 2), y + (oneDigitHeight / 2) + (fontSize / 2)); // Outlined
					context.closePath();
				}
			}
		}

		// Hand
		context.beginPath();
		if (analogDisplayColorConfig.withHandShadow) {
			context.shadowColor = analogDisplayColorConfig.shadowColor;
			context.shadowOffsetX = 3;
			context.shadowOffsetY = 3;
			context.shadowBlur = 3;
		}
		// Center
		context.moveTo(canvas.width / 2, canvas.height / 2);

		let ___currentAngle = (totalAngle * ((displayValue - startValue) / (maxValue - startValue))) - Math.toRadians(overlapOver180InDegree);
		// Left
		let x = (canvas.width / 2) - ((radius * 0.05) * Math.cos((___currentAngle - (Math.PI / 2))));
		let y = (canvas.height / 2) - ((radius * 0.05) * Math.sin((___currentAngle - (Math.PI / 2))));
		context.lineTo(x, y);
		// Tip
		x = (canvas.width / 2) - ((radius * 0.90) * Math.cos(___currentAngle));
		y = (canvas.height / 2) - ((radius * 0.90) * Math.sin(___currentAngle));
		context.lineTo(x, y);
		// Right
		x = (canvas.width / 2) - ((radius * 0.05) * Math.cos((___currentAngle + (Math.PI / 2))));
		y = (canvas.height / 2) - ((radius * 0.05) * Math.sin((___currentAngle + (Math.PI / 2))));
		context.lineTo(x, y);

		context.closePath();
		context.fillStyle = analogDisplayColorConfig.handColor;
		context.fill();
		context.lineWidth = 1;
		context.strokeStyle = analogDisplayColorConfig.handOutlineColor;
		context.stroke();
		// Knob
		context.beginPath();
		context.arc((canvas.width / 2), (canvas.height / 2), 7, 0, 2 * Math.PI, false);
		context.closePath();
		context.fillStyle = analogDisplayColorConfig.knobColor;
		context.fill();
		context.strokeStyle = analogDisplayColorConfig.knobOutlineColor;
		context.stroke();
	}

	this.setValue = (val) => {
		if (withMinMax) {
			miniVal = Math.min(val, miniVal);
			maxiVal = Math.max(val, maxiVal);
		}
		drawDisplay(canvasName, displaySize, val);
	};
}
