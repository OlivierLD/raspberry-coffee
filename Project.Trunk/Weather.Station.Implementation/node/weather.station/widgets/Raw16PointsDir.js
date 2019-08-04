/*
 * @author Olivier Le Diouris
 */

var raw16pointsColorConfigWhite = {
	bgColor: 'white',
	withGradient: false,
	displayBackgroundGradient: {from: 'LightGrey', to: 'white'},
	valueColor: 'black',
	font: 'Digi Font' /* 'Source Code Pro' */
};

var raw16pointsColorConfigBlack = {
	bgColor: 'black',
	withGradient: false,
	displayBackgroundGradient: {from: 'DarkGrey', to: 'black'},
	valueColor: 'cyan',
	font: 'Digi Font'
};
var raw16pointsColorConfig = raw16pointsColorConfigBlack;

const cardValues = [
	{ name: 'N', value: 0 },
	{ name: 'NNE', value: 22.5 },
	{ name: 'NE', value: 45 },
	{ name: 'ENE', value: 67.5 },
	{ name: 'E', value: 90 },
	{ name: 'ESE', value: 112.5 },
	{ name: 'SE', value: 135 },
	{ name: 'SSE', value: 157.5 },
	{ name: 'S', value: 180 },
	{ name: 'SSW', value: 202.5 },
	{ name: 'SW', value: 225 },
	{ name: 'WSW', value: 247.5 },
	{ name: 'W', value: 270 },
	{ name: 'WNW', value: 292.5 },
	{ name: 'NW', value: 315 },
	{ name: 'NNW', value: 337.5 },
];

function Raw16PointsDir(cName, dSize) {


	var canvasName = cName;
	var displaySize = dSize;

	var scale = dSize / 200;

	this.valueToDisplay = 0;

	var instance = this;

	this.setDisplaySize = function (ds) {
		scale = ds / 200;
		displaySize = ds;
		this.drawDisplay(canvasName, displaySize);
	};

	function getStyleRuleValue(style, selector, sheet) {
		var sheets = typeof sheet !== 'undefined' ? [sheet] : document.styleSheets;
		for (var i = 0, l = sheets.length; i < l; i++) {
			var sheet = sheets[i];
			try {
				if (!sheet.cssRules) {
					continue;
				}
			} catch (err) {
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

	/**
	 *
	 * @param displayCanvasName
	 * @param displayRadius
	 * @param displayValue a cardValue (see above), like { name: 'WSW', value: 247.5 }
	 */
	this.drawDisplay = function (displayCanvasName, displayRadius, displayValue) {
		var schemeColor = getStyleRuleValue('color', '.display-scheme');
//  console.log(">>> DEBUG >>> color:" + schemeColor);
		if (schemeColor === 'black')
			raw16pointsColorConfig = raw16pointsColorConfigBlack;
		else if (schemeColor === 'white')
			raw16pointsColorConfig = raw16pointsColorConfigWhite;

		var canvas = document.getElementById(displayCanvasName);
		var context = canvas.getContext('2d');

		var radius = displayRadius;

		// Cleanup
		//context.fillStyle = "#ffffff";
		context.fillStyle = raw16pointsColorConfig.bgColor;
//  context.fillStyle = "transparent";
		context.fillRect(0, 0, canvas.width, canvas.height);
		//context.fillStyle = 'rgba(255, 255, 255, 0.0)';
		//context.fillRect(0, 0, canvas.width, canvas.height);

		context.beginPath();
		if (raw16pointsColorConfig.withGradient) {
			var grd = context.createLinearGradient(0, 5, 0, radius);
			grd.addColorStop(0, raw16pointsColorConfig.displayBackgroundGradient.from);// 0  Beginning
			grd.addColorStop(1, raw16pointsColorConfig.displayBackgroundGradient.to);  // 1  End
			context.fillStyle = grd;
		} else {
			context.fillStyle = raw16pointsColorConfig.displayBackgroundGradient.to;
		}

		// Value
		var text = displayValue.name;
		len = 0;
		context.font = /*"bold " +*/ Math.round(scale * 80) + "px " + raw16pointsColorConfig.font; // "bold 40px Arial"
		var metrics = context.measureText(text);
		len = metrics.width;

		context.beginPath();
		context.fillStyle = raw16pointsColorConfig.valueColor;
		context.fillText(text, (canvas.width / 2) - (len / 2), ((canvas.height / 2) + (Math.round(scale * 40) / 2)));
		context.closePath();

		// LEDs
		for (var led=0; led<cardValues.length; led++) {
			var angle = toRadians(cardValues[led].value + 90);
			// Led centrer
			var xLedCenter = (canvas.width / 2) - ((radius * 0.95) * Math.cos(angle));
			var yLedCenter = (canvas.height / 2) - ((radius * 0.95) * Math.sin(angle));
			var color = (displayValue.value === cardValues[led].value ? 'red' : 'gray');
			fillLed(context, { x: xLedCenter, y: yLedCenter }, 6, color);
		}

	};

	var fillLed = function(context, pt, radius, color) {
		// let grd = context.createRadialGradient(pt.x - (radius / 3), pt.y - (radius / 3), radius / 3, pt.x, pt.y, radius);
		// grd.addColorStop(0, this.marqueeColorConfig.fgColor.from);
		// grd.addColorStop(1, this.marqueeColorConfig.fgColor.to);

		context.beginPath();
		context.fillStyle = color;
//	context.arc(pt.x - (radius / 2), pt.y - (radius / 2), radius, 0, radius * Math.PI);
		context.arc(pt.x, pt.y, radius, 0, radius * Math.PI);
		context.fill();
		context.closePath();
	};

	this.setValue = function (val) {
		instance.drawDisplay(canvasName, displaySize, val);
	};

	(function () {
		instance.drawDisplay(canvasName, displaySize, cardValues[0]);
	})(); // Invoked automatically
}
