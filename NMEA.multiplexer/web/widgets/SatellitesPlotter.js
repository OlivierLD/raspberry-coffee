/*
 * @author Olivier Le Diouris
 */
const spAnalogDisplayColorConfigWhite = {
	bgColor: 'white',
	digitColor: 'black',
	withGradient: true,
	displayBackgroundGradient: {from: 'LightGrey', to: 'white'},
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

const spAnalogDisplayColorConfigBlack = {
	bgColor: 'black',
	digitColor: 'white', // 'cyan',
	withGradient: true,
	displayBackgroundGradient: {from: 'DarkGrey', to: 'black'},
	shadowColor: 'black',
	outlineColor: 'DarkGrey',
	majorTickColor: 'white',
	minorTickColor: 'white',
	valueColor: 'white',
	valueOutlineColor: 'black',
	valueNbDecimal: 1,
	handColor: 'rgba(255, 0, 0, 0.4)', // 'rgba(0, 0, 100, 0.25)',
	handOutlineColor: 'red', // 'blue',
	withHandShadow: true,
	knobColor: '#8ed6ff', // Kind of blue
	knobOutlineColor: 'blue',
	font: 'Arial'
};
let spAnalogDisplayColorConfig = spAnalogDisplayColorConfigBlack; // analogDisplayColorConfigBlack; // White is the default

function SatellitesPlotter(cName,                     // Canvas Name
                           dSize) {                   // Display radius
	let scale = dSize / 100;

	let canvasName = cName;
	let displaySize = dSize;

	let withBorder = true;
	let instance = this;

	let satellites = [];

	(function () {
		drawDisplay(canvasName, displaySize);
	})(); // Invoked automatically

	this.setBorder = function (b) {
		withBorder = b;
	};

	this.repaint = function () {
		drawDisplay(canvasName, displaySize);
	};

	this.setDisplaySize = function (ds) {
		scale = ds / 100;
		displaySize = ds;
		drawDisplay(canvasName, displaySize);
	};

	function getStyleRuleValue(style, selector, sheet) {
		let sheets = typeof sheet !== 'undefined' ? [sheet] : document.styleSheets;
		for (let i = 0, l = sheets.length; i < l; i++) {
			let sheet = sheets[i];
			try {
				if (!sheet.cssRules) {
					continue;
				}
			} catch (error) {
				continue;
			}
			for (let j = 0, k = sheet.cssRules.length; j < k; j++) {
				let rule = sheet.cssRules[j];
				if (rule.selectorText && rule.selectorText.split(',').indexOf(selector) !== -1) {
					return rule.style[style];
				}
			}
		}
		return null;
	}

	function drawDisplay(displayCanvasName, displayRadius) {

		let schemeColor = getStyleRuleValue('color', '.display-scheme');
		if (schemeColor === 'black') {
			spAnalogDisplayColorConfig = analogDisplayColorConfigBlack;
		} else if (schemeColor === 'white') {
			spAnalogDisplayColorConfig = analogDisplayColorConfigWhite;
		}

		let canvas = document.getElementById(displayCanvasName);
		let center = {
			x: canvas.width / 2,
			y: (canvas.height / 2) - 10
		};

		let context = canvas.getContext('2d');

		let radius = displayRadius;

		// Cleanup
		//context.fillStyle = "#ffffff";
		context.fillStyle = spAnalogDisplayColorConfig.bgColor;
		//context.fillStyle = "transparent";
		context.fillRect(0, 0, canvas.width, canvas.height);
		//context.fillStyle = 'rgba(255, 255, 255, 0.0)';
		//context.fillRect(0, 0, canvas.width, canvas.height);

		context.beginPath();
		if (withBorder === true) {
			context.arc(center.x, radius + 10, radius, 0, (2 * Math.PI), false);
			context.lineWidth = 5;
		}

		if (spAnalogDisplayColorConfig.withGradient) {
			let grd = context.createLinearGradient(0, 5, 0, radius);
			grd.addColorStop(0, spAnalogDisplayColorConfig.displayBackgroundGradient.from);// 0  Beginning
			grd.addColorStop(1, spAnalogDisplayColorConfig.displayBackgroundGradient.to);  // 1  End
			context.fillStyle = grd;
		} else {
			context.fillStyle = spAnalogDisplayColorConfig.displayBackgroundGradient.to;
		}

		if (spAnalogDisplayColorConfig.withDisplayShadow) {
			context.shadowOffsetX = 3;
			context.shadowOffsetY = 3;
			context.shadowBlur = 3;
			context.shadowColor = spAnalogDisplayColorConfig.shadowColor;
		}
		context.lineJoin = "round";
		context.fill();
		context.strokeStyle = spAnalogDisplayColorConfig.outlineColor;
		context.stroke();
		context.closePath();

		// Axis: N-S, E-W, NE-SW, NW-SE
		context.lineWidth = 1;
		context.setLineDash([3, 3]); // 3px dash, 3px space
		context.beginPath();
		// N-S
		context.moveTo(center.x, center.y - radius); // N
		context.lineTo(center.x, center.y + radius); // S
		// E-W
		context.moveTo(center.x - radius, center.y); // W
		context.lineTo(center.x + radius, center.y); // E
		// NW-SE
		context.moveTo(center.x - (radius * Math.sin(Math.PI / 4)), center.y - (radius * Math.sin(Math.PI / 4))); // NW
		context.lineTo(center.x + (radius * Math.sin(Math.PI / 4)), center.y + (radius * Math.sin(Math.PI / 4))); // SE
		// NE-SW
		context.moveTo(center.x - (radius * Math.sin(Math.PI / 4)), center.y + (radius * Math.sin(Math.PI / 4))); // NE
		context.lineTo(center.x + (radius * Math.sin(Math.PI / 4)), center.y - (radius * Math.sin(Math.PI / 4))); // SW

		// Altitude circles 30, 60.
		context.moveTo(center.x + (radius / 3), center.y); // 0 degrees is actually E
		context.arc(center.x, center.y, radius / 3, 0, 2 * Math.PI, false); // 60 degrees
		context.arc(center.x, center.y, 2 * radius / 3, 0, 2 * Math.PI, false); // 30 degrees

		context.stroke();
		context.closePath();

		context.setLineDash([0]); // 3px dash, 3px space

		// Plot satellites.
		const SAT_RADIUS = 6;
		if (satellites !== undefined) {
			for (var satNum in satellites) {
				context.beginPath();

				context.fillStyle = getSNRColor(satellites[satNum].snr);
//              let satCircleRadius = radius * (Math.cos(toRadians(demoSat[i].el)));
				let satCircleRadius = radius * ((90 - satellites[satNum].elevation) / 90);
				let centerSat = {
					x: center.x + (satCircleRadius * Math.sin(toRadians(satellites[satNum].azimuth))),
					y: center.y - (satCircleRadius * Math.cos(toRadians(satellites[satNum].azimuth)))
				};
				context.arc(centerSat.x, centerSat.y, SAT_RADIUS, 0, 2 * Math.PI, false);

				let text = satellites[satNum].svID;
				context.font = "bold " + Math.round(scale * 12) + "px " + spAnalogDisplayColorConfig.font; // "bold 40px Arial"
				let metrics = context.measureText(text);
				let len = metrics.width;

				context.fill();
				context.stroke();
				context.fillText(text, centerSat.x - (len / 2), centerSat.y - SAT_RADIUS - 2);

				context.closePath();
			}
		}
	}

	/**
	 *
	 * @param sat like this:
	 *
{
  "3": {
    "svID": 3,
    "elevation": 42,
    "azimuth": 178,
    "snr": 44
  },
  "4": {
    "svID": 4,
    "elevation": 13,
    "azimuth": 52,
    "snr": 31
  },
  "6": {
    "svID": 6,
    "elevation": 11,
    "azimuth": 277,
    "snr": 26
  },
  "7": {
    "svID": 7,
    "elevation": 42,
    "azimuth": 259,
    "snr": 34
  },
  "8": {
    "svID": 8,
    "elevation": 2,
    "azimuth": 142,
    "snr": 0
  },
  "9": {
    "svID": 9,
    "elevation": 54,
    "azimuth": 319,
    "snr": 35
  },
  "16": {
    "svID": 16,
    "elevation": 47,
    "azimuth": 66,
    "snr": 42
  },
  "22": {
    "svID": 22,
    "elevation": 18,
    "azimuth": 164,
    "snr": 29
  },
  "23": {
    "svID": 23,
    "elevation": 74,
    "azimuth": 38,
    "snr": 38
  },
  "26": {
    "svID": 26,
    "elevation": 24,
    "azimuth": 45,
    "snr": 31
  },
  "27": {
    "svID": 27,
    "elevation": 8,
    "azimuth": 111,
    "snr": 26
  },
  "30": {
    "svID": 30,
    "elevation": 7,
    "azimuth": 246,
    "snr": 22
  }
}
	 */
	this.setSatellites = function (sat) {
		satellites = sat;
		drawDisplay(canvasName, displaySize);
	};
}

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

function getSNRColor(snr) {
	let c = 'lightGray';
	if (snr !== undefined && snr !== null) {
		if (snr > 0) {
			c = 'red';
		}
		if (snr > 10) {
			c = 'orange';
		}
		if (snr > 20) {
			c = 'yellow';
		}
		if (snr > 30) {
			c = 'lightGreen';
		}
		if (snr > 40) {
			c = 'green';
		}
	}
	return c;
}
