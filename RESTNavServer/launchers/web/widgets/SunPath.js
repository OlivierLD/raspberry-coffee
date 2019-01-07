/*
 * @author Olivier Le Diouris
 */
"use strict";

if (Math.toRadians === undefined) {
	Math.toRadians = (deg) => {
		return deg * (Math.PI / 180);
	};
}

function SunPath(cName) {

	/**
	 * Needs Data to represent the Sun path:
	 * Sun Path Data can be obtained from http://localhost:9999/astro/sun-path-today /POST
	 * Current Sun position, with rise and set: http://localhost:9999/astro/sun-now /POST
	 *
	 * To feed the Sun Data, use:
	 sunPath.setSunDataNow(json);
	 sunPath.repaint();
	 *
	 * To feed the sun Path, use:
	 sunPath.setSunPath(json);
	 sunPath.repaint();
	 *
	 * Figure tilt can be changed (for perspective), use:
	 sunPath.setTilt(val); // val in [-90..90]
	 sunPath.repaint();
	 *
	 sunPath can be obtained from POST /astro/sun-path-today
	 Request Payload like { position: { latitude: 37.76661945, longitude: -122.5166988 }, step: 10 }
	 step: 10 is the default value
	 If not position is provided, default.mux.latitude and default.mux.longitude will be used instead, if they exist (error otherwise).
	 Minimum payload is {}.

	 Sample response:
 [
	 {
    "epoch": 1526993906000,
    "alt": -0.2099868844664718,
    "z": 63.606812406293436
  },
	 {
    "epoch": 1526994506000,
    "alt": 1.5725579156196041,
    "z": 65.12679994159187
  },
	 {
    "epoch": 1526995106000,
    "alt": 3.3769591507214347,
    "z": 66.62202808307599
  },
	 {
    "epoch": 1526995706000,
    "alt": 5.2016579554516245,
    "z": 68.09509263145956
  },
	 {
    "epoch": 1526996306000,
    "alt": 7.045171637599468,
    "z": 69.54860826085613
  },
	 {
    "epoch": 1526996906000,
    "alt": 8.90608707106982,
    "z": 70.98521652239666
  },
	 {
    "epoch": 1526997506000,
    "alt": 10.7830534265924,
    "z": 72.40759587016622
  },
	 {
    "epoch": 1526998106000,
    "alt": 12.674774950839689,
    "z": 73.81847434523108
  },
	 {
    "epoch": 1526998706000,
    "alt": 14.58000365007892,
    "z": 75.22064493383381
  },
	 {
    "epoch": 1526999306000,
    "alt": 16.497531826233377,
    "z": 76.61698379624755
  },
	 {
    "epoch": 1526999906000,
    "alt": 18.42618439806157,
    "z": 78.01047166274785
  },
	 {
    "epoch": 1527000506000,
    "alt": 20.364810785800085,
    "z": 79.40421870679711
  },
	 {
    "epoch": 1527001106000,
    "alt": 22.31227702589541,
    "z": 80.80149398703578
  },
	 {
    "epoch": 1527001706000,
    "alt": 24.26745601706344,
    "z": 82.20575873277927
  },
	 {
    "epoch": 1527002306000,
    "alt": 26.229218321531746,
    "z": 83.62070619823055
  },
	 {
    "epoch": 1527002906000,
    "alt": 28.196421348207615,
    "z": 85.05030775046349
  },
	 {
    "epoch": 1527003506000,
    "alt": 30.16789742176711,
    "z": 86.49886707912646
  },
	 {
    "epoch": 1527004106000,
    "alt": 32.14244026439827,
    "z": 87.97108407744167
  },
	 {
    "epoch": 1527004706000,
    "alt": 34.11878946091102,
    "z": 89.47213042537287
  },
	 {
    "epoch": 1527005306000,
    "alt": 36.09561236563241,
    "z": 91.00773937888256
  },
	 {
    "epoch": 1527005906000,
    "alt": 38.07148263991452,
    "z": 92.5843127443934
  },
	 {
    "epoch": 1527006506000,
    "alt": 40.04485534157959,
    "z": 94.209049456021
  },
	 {
    "epoch": 1527007106000,
    "alt": 42.014035476556565,
    "z": 95.89009878172664
  },
	 {
    "epoch": 1527007706000,
    "alt": 43.97714124147301,
    "z": 97.63674584837959
  },
	 {
    "epoch": 1527008306000,
    "alt": 45.93205814252986,
    "z": 99.45963455698741
  },
	 {
    "epoch": 1527008906000,
    "alt": 47.87638244113664,
    "z": 101.37103626346723
  },
	 {
    "epoch": 1527009506000,
    "alt": 49.80735076732526,
    "z": 103.38517278483485
  },
	 {
    "epoch": 1527010106000,
    "alt": 51.721752026294446,
    "z": 105.5186027588207
  },
	 {
    "epoch": 1527010706000,
    "alt": 53.61581666744556,
    "z": 107.79067941795196
  },
	 {
    "epoch": 1527011306000,
    "alt": 55.4850770129682,
    "z": 110.22408396411828
  },
	 {
    "epoch": 1527011906000,
    "alt": 57.324191858498935,
    "z": 112.84543091624622
  },
	 {
    "epoch": 1527012506000,
    "alt": 59.12672470464755,
    "z": 115.68591964049823
  },
	 {
    "epoch": 1527013106000,
    "alt": 60.88486885277489,
    "z": 118.78197623420235
  },
	 {
    "epoch": 1527013706000,
    "alt": 62.58910959967611,
    "z": 122.17575975632079
  },
	 {
    "epoch": 1527014306000,
    "alt": 64.22782206479397,
    "z": 125.91530036312844
  },
	 {
    "epoch": 1527014906000,
    "alt": 65.78681636436573,
    "z": 130.05386162104537
  },
	 {
    "epoch": 1527015506000,
    "alt": 67.24887106296073,
    "z": 134.64787132682525
  },
	 {
    "epoch": 1527016106000,
    "alt": 68.59334857792707,
    "z": 139.7524770762683
  },
	 {
    "epoch": 1527016706000,
    "alt": 69.79606891593049,
    "z": 145.41360328901172
  },
	 {
    "epoch": 1527017306000,
    "alt": 70.82972279723717,
    "z": 151.65568458986596
  },
	 {
    "epoch": 1527017906000,
    "alt": 71.66518032738175,
    "z": 158.46561591250043
  },
	 {
    "epoch": 1527018506000,
    "alt": 72.27399097230337,
    "z": 165.7763978348453
  },
	 {
    "epoch": 1527019106000,
    "alt": 72.63201897982606,
    "z": 173.45767882667383
  },
	 {
    "epoch": 1527019706000,
    "alt": 72.72350680384675,
    "z": 181.3218707806489
  },
	 {
    "epoch": 1527020306000,
    "alt": 72.54423055836675,
    "z": 189.14969797520763
  },
	 {
    "epoch": 1527020906000,
    "alt": 72.10241505749144,
    "z": 196.72837293726573
  },
	 {
    "epoch": 1527021506000,
    "alt": 71.41698091962526,
    "z": 203.88708596619713
  },
	 {
    "epoch": 1527022106000,
    "alt": 70.51392705866238,
    "z": 210.5163872885313
  },
	 {
    "epoch": 1527022706000,
    "alt": 69.42228323831496,
    "z": 216.56841130430126
  },
	 {
    "epoch": 1527023306000,
    "alt": 68.17080380572354,
    "z": 222.0441898190897
  },
	 {
    "epoch": 1527023906000,
    "alt": 66.7858742985252,
    "z": 226.9767816064943
  },
	 {
    "epoch": 1527024506000,
    "alt": 65.29053479194843,
    "z": 231.41624947446195
  },
	 {
    "epoch": 1527025106000,
    "alt": 63.704280119859746,
    "z": 235.41883878798131
  },
	 {
    "epoch": 1527025706000,
    "alt": 62.04329615332145,
    "z": 239.04029645835809
  },
	 {
    "epoch": 1527026306000,
    "alt": 60.32088600855201,
    "z": 242.33232088945616
  },
	 {
    "epoch": 1527026906000,
    "alt": 58.54794003152568,
    "z": 245.34104946102016
  },
	 {
    "epoch": 1527027506000,
    "alt": 56.73337634134839,
    "z": 248.10673224046548
  },
	 {
    "epoch": 1527028106000,
    "alt": 54.884521922325725,
    "z": 250.6640257636111
  },
	 {
    "epoch": 1527028706000,
    "alt": 53.0074301240562,
    "z": 253.04256147259633
  },
	 {
    "epoch": 1527029306000,
    "alt": 51.10713688304214,
    "z": 255.26760108765885
  },
	 {
    "epoch": 1527029906000,
    "alt": 49.18786614949623,
    "z": 257.3606776204307
  },
	 {
    "epoch": 1527030506000,
    "alt": 47.253193036757025,
    "z": 259.3401782446223
  },
	 {
    "epoch": 1527031106000,
    "alt": 45.30617322207758,
    "z": 261.22185304900444
  },
	 {
    "epoch": 1527031706000,
    "alt": 43.34944569039383,
    "z": 263.0192487774731
  },
	 {
    "epoch": 1527032306000,
    "alt": 41.3853145750893,
    "z": 264.744073546755
  },
	 {
    "epoch": 1527032906000,
    "alt": 39.41581479262191,
    "z": 266.4065010228446
  },
	 {
    "epoch": 1527033506000,
    "alt": 37.44276425374744,
    "z": 268.01542372591217
  },
	 {
    "epoch": 1527034106000,
    "alt": 35.46780742077168,
    "z": 269.5786622038156
  },
	 {
    "epoch": 1527034706000,
    "alt": 33.49244972918296,
    "z": 271.1031396967581
  },
	 {
    "epoch": 1527035306000,
    "alt": 31.518086537288667,
    "z": 272.5950269036466
  },
	 {
    "epoch": 1527035906000,
    "alt": 29.546027121076158,
    "z": 274.05986277640625
  },
	 {
    "epoch": 1527036506000,
    "alt": 27.57751486493755,
    "z": 275.5026555587792
  },
	 {
    "epoch": 1527037106000,
    "alt": 25.613744454971783,
    "z": 276.92796760545053
  },
	 {
    "epoch": 1527037706000,
    "alt": 23.655876703232806,
    "z": 278.3399868660604
  },
	 {
    "epoch": 1527038306000,
    "alt": 21.705051635474486,
    "z": 279.7425872785133
  },
	 {
    "epoch": 1527038906000,
    "alt": 19.762399457518548,
    "z": 281.139380529669
  },
	 {
    "epoch": 1527039506000,
    "alt": 17.8290516805221,
    "z": 282.53375930685945
  },
	 {
    "epoch": 1527040106000,
    "alt": 15.906150043427512,
    "z": 283.9289351574024
  },
	 {
    "epoch": 1527040706000,
    "alt": 13.994855406684971,
    "z": 285.3279705352584
  },
	 {
    "epoch": 1527041306000,
    "alt": 12.09635599506581,
    "z": 286.73380639506433
  },
	 {
    "epoch": 1527041906000,
    "alt": 10.211875256363399,
    "z": 288.1492858642048
  },
	 {
    "epoch": 1527042506000,
    "alt": 8.342679440591583,
    "z": 289.5771744802884
  },
	 {
    "epoch": 1527043106000,
    "alt": 6.490084986222032,
    "z": 291.0201773574161
  },
	 {
    "epoch": 1527043706000,
    "alt": 4.655465891970013,
    "z": 292.48095344860377
  },
	 {
    "epoch": 1527044306000,
    "alt": 2.8402603835320046,
    "z": 293.96212766140843
  },
	 {
    "epoch": 1527044906000,
    "alt": 1.0459787359533554,
    "z": 295.4662994436908
  }
	 ]
	 */
	let sunPath = undefined;
	/* sunNow can be obtained from POST /astro/sun-now
	   Request payload (optional) is a GeoPoint, represented in Json as { latitude: 37.76661945, longitude: -122.5166988 }.
	   If payload is null, default.mux.latitude and default.mux.longitude will be used instead, if they exist (error otherwise). Payload
	   used for the computation is returned in the response payload, anyway.

	Response sample:
{
  "epoch": 1527006247015,
  "lat": 37.76661945,
  "lng": -122.5166988,
  "body": "Sun",
  "decl": 20.463700660620308,
  "gha": 66.85965851342138,
  "altitude": 39.19342734948766,
  "z": 93.50129805928104,
  "eot": 20.112413796883015,
  "riseTime": 1526993906000,
  "setTime": 1527045303000,
  "riseZ": 63.028849635612275,
  "setZ": 296.9711503643877
}
	 */
	let sunNow = undefined;

	let sunHe = undefined, sunZ = undefined;

	let tilt = -10; // [-90..90], back and forth
	let side =   0; // Left and right

	// TODO Latitude lower than Sun D, south hemisphere and between tropics.

	let addToZ = 180;  // 180 when pointing South (Sun in the South at noon). Combined with left right rotation
	let invertX =  1;  // +1/-1 . +1 when pointing south

	let rotation = 0;
	let reloadColor = false;

	/*
	 * See custom properties in CSS.
	 * =============================
	 * @see https://developer.mozilla.org/en-US/docs/Web/CSS/
	 * Relies on a rule named .sunpath, like that:
	 *
	 .sunpath {
			--bg-color: black;
			--with-gradient: true;
			--display-background-gradient-from: LightGrey;
			--display-background-gradient-to: black;
			--value-color: grey;
			--value-outline-color: black;
			--font: Arial;
		}
	 */

	let defaultColorConfig = {
		bgColor: 'black', // Used if withGradient = false
		withGradient: true,
		displayBackgroundGradientFrom: 'LightGrey',
		displayBackgroundGradientTo: 'black',
		font: 'Arial',
		sunColor: 'yellow',
		cardPointColor: 'white',
		altitudeValueColor: 'cyan',
		gridColor: 'rgba(0, 205, 205, 0.75)',
		baseColor: 'rgba(0, 205, 205, 0.5)'
	};
	let colorConfig = defaultColorConfig;

	if (events !== undefined) {
		events.subscribe('color-scheme-changed', function (val) {
//    console.log('Color scheme changed:', val);
			reloadColorConfig();
		});
	}
//colorConfig = getColorConfig(); // TODO

	let canvasName = cName;
//try { console.log('in the AnalogDisplay constructor for ' + cName + " (" + dSize + ")"); } catch (e) {}

	(function () {
		drawDisplay(canvasName);
	})(); // Invoked automatically

	let reloadColorConfig = function() {
//  console.log('Color scheme has changed');
		reloadColor = true;
	};

	this.setSunDataNow = function(sunData) {
		sunNow = sunData;
		this.setSunPos({ he: sunNow.altitude, z: sunNow.z })
	};

	this.setSunPath = function(sunData) {
		sunPath = sunData;
	};

	this.setSunPos = function(sunPos) {
		sunHe = sunPos.he;
		sunZ = sunPos.z;
	}

	this.repaint = function() {
		drawDisplay(cName);
	};

	this.setTilt = function(t) {
		tilt = t;
	};
	this.getTilt = function() {
		return tilt;
	};

	this.setRotation = function(t) {
		rotation = t;
	};
	this.getRotation = function() {
		return rotation;
	};

	/**
	 * Used to draw a globe
	 * alpha, then beta
	 *
	 * @param lat in degrees
	 * @param lng in degrees
	 * @return x, y, z. Cartesian coordinates.
	 */
	function rotateBothWays(lat, lng, rotationAroundY, rotationAroundX, addToLng) {

		let x = Math.cos(Math.toRadians(lat)) * Math.sin(Math.toRadians(lng + addToLng));
		let y = Math.sin(Math.toRadians(lat));
		let z = Math.cos(Math.toRadians(lat)) * Math.cos(Math.toRadians(lng + addToLng));

		let alfa = Math.toRadians(rotationAroundY); // in plan (x, z), y unchanged.
		let beta = Math.toRadians(rotationAroundX); // in plan (y, z), x unchanged. Tilt.
		/*
		 * Note:
		 * x is the x of the screen
		 * y is the y of the screen
		 * z goes through the screen
		 *
		 *                      |  cos a -sin a  0 |  a > 0 : counter-clockwise
		 * Rotation plan x, y:  |  sin a  cos a  0 |
		 *                      |    0     0     1 |
		 *
		 *                      | 1    0      0    |  b > 0 : towards user
		 * Rotation plan y, z:  | 0  cos b  -sin b |
		 *                      | 0  sin b   cos b |
		 *
		 *  | x |   | cos a -sin a  0 |   | 1   0      0    |   | x |   |  cos a  (-sin a * cos b) (sin a * sin b) |
		 *  | y | * | sin a  cos a  0 | * | 0  cos b -sin b | = | y | * |  sin a  (cos a * cos b) (-cos a * sin b) |
		 *  | z |   |  0      0     1 |   | 0  sin b  cos b |   | z |   |   0          sin b           cos b       |
		 */

		// All in once
		let _x = (x * Math.cos(alfa)) - (y * Math.sin(alfa) * Math.cos(beta)) + (z * Math.sin(alfa) * Math.sin(beta));
		let _y = (x * Math.sin(alfa)) + (y * Math.cos(alfa) * Math.cos(beta)) - (z * Math.cos(alfa) * Math.sin(beta));
		let _z = (y * Math.sin(beta)) + (z * Math.cos(beta));

		return {x: _x, y: _y, z: _z};
	}

	function drawDisplay(displayCanvasName) {
		if (reloadColor) {
			// In case the CSS has changed, dynamically.
			colorConfig = getColorConfig();
			console.log("Changed theme:", colorConfig);
		}
		reloadColor = false;

		let canvas = document.getElementById(displayCanvasName);
		let context = canvas.getContext('2d');

		let radius = (canvas.width / 2) * .8;

		if (colorConfig.withGradient) {
			let grd = context.createLinearGradient(0, 5, 0, radius);
			grd.addColorStop(0, colorConfig.displayBackgroundGradientFrom); // 0  Beginning
			grd.addColorStop(1, colorConfig.displayBackgroundGradientTo);   // 1  End
			context.fillStyle = grd;
		} else {
			context.fillStyle = colorConfig.bgColor;
		}

		// Background
		context.fillRect(0, 0, canvas.width, canvas.height);

		let center = {
			x: canvas.width / 2,
			y: canvas.height / 2
		};

		// Base
		let minZ = sunNow !== undefined ? 10 * Math.floor(sunNow.riseZ / 10) : 90,
				maxZ = sunNow !== undefined ? 10 * Math.ceil(sunNow.setZ / 10) : 270;

		context.strokeStyle = colorConfig.gridColor;
		context.lineWidth = 3;
		let panelPoint = rotateBothWays(rotation, minZ, side, tilt, addToZ);
		// Base
		context.beginPath();
		context.moveTo(center.x + (panelPoint.x * radius * invertX), center.y - (panelPoint.y * radius));
		for (let alfa=minZ; alfa<=maxZ; alfa += 1) {
//		console.log("Base rotation", rotation);
			panelPoint = rotateBothWays(rotation, alfa, side, tilt, addToZ);
			context.lineTo(center.x + (panelPoint.x * radius * invertX), center.y - (panelPoint.y * radius));
		}
		context.stroke();
		context.closePath();
		// Fill the base
		context.fillStyle = colorConfig.baseColor;
		context.fill();

		// Close the base
		context.lineWidth = 1;
		panelPoint = rotateBothWays(rotation, minZ, side, tilt, addToZ);
		context.beginPath();
		context.moveTo(center.x + (panelPoint.x * radius * invertX), center.y - (panelPoint.y * radius));
		context.lineTo(center.x, center.y);
		panelPoint = rotateBothWays(rotation, maxZ, side, tilt, addToZ);
		context.lineTo(center.x + (panelPoint.x * radius * invertX), center.y - (panelPoint.y * radius));
		context.stroke();
		context.closePath();
		// Fill the base
		context.fillStyle = colorConfig.baseColor;
		context.fill();

		// Indicate S (or N: TODO)
		context.save();
		let fontSize = 20;
		context.font = "bold " + Math.round(fontSize) + "px Arial";
		context.fillStyle = colorConfig.cardPointColor;
		let len = 0;

		panelPoint = rotateBothWays(rotation, 180, side, tilt, addToZ);
		context.moveTo(center.x + (panelPoint.x * radius * invertX), center.y - (panelPoint.y * radius));
		let text = "S";
		let metrics = context.measureText(text);
		len = metrics.width;
		context.fillText(text, center.x + (panelPoint.x * radius * invertX) - (len / 2), center.y - (panelPoint.y * radius));

		panelPoint = rotateBothWays(rotation, 90, side, tilt, addToZ);
		context.moveTo(center.x + (panelPoint.x * radius * invertX), center.y - (panelPoint.y * radius));
		text = "E";
		metrics = context.measureText(text);
		len = metrics.width;
		context.fillText(text, center.x + (panelPoint.x * radius * invertX) - (len / 2), center.y - (panelPoint.y * radius));

		panelPoint = rotateBothWays(rotation, 270, side, tilt, addToZ);
		context.moveTo(center.x + (panelPoint.x * radius * invertX), center.y - (panelPoint.y * radius));
		text = "W";
		metrics = context.measureText(text);
		len = metrics.width;
		context.fillText(text, center.x + (panelPoint.x * radius * invertX) - (len / 2), center.y - (panelPoint.y * radius));

		context.restore();

		// Meridians.
		for (let alfa = minZ; alfa <= maxZ; alfa += 10) { // Longitude
			panelPoint = rotateBothWays(rotation, alfa, side, tilt, addToZ);
			context.beginPath();
			context.moveTo(center.x + (panelPoint.x * radius * invertX), center.y - (panelPoint.y * radius));
			for (let beta = 0; beta <= 90; beta += 1) { // Latitude
				panelPoint = rotateBothWays(beta + rotation, alfa, side, tilt, addToZ);
				context.lineTo(center.x + (panelPoint.x * radius * invertX), center.y - (panelPoint.y * radius));
			}
			context.stroke();
			context.closePath();
		}

		// Parallels
		for (let alfa = 10; alfa < 90; alfa += 10) { // Latitude
			panelPoint = rotateBothWays(alfa + rotation, minZ, side, tilt, addToZ);
			context.beginPath();
			context.moveTo(center.x + (panelPoint.x * radius * invertX), center.y - (panelPoint.y * radius));
			for (let beta = minZ; beta <= maxZ; beta += 1) { // Longitude
				panelPoint = rotateBothWays(alfa + rotation, beta, side, tilt, addToZ);
				context.lineTo(center.x + (panelPoint.x * radius * invertX), center.y - (panelPoint.y * radius));
			}
			context.stroke();
			context.closePath();
			// Altitude Labels
			text = alfa + "°";
			context.fillStyle = colorConfig.altitudeValueColor;
			context.beginPath();
			panelPoint = rotateBothWays(alfa + rotation, 180, side, tilt, addToZ);
			context.moveTo(center.x + (panelPoint.x * radius * invertX), center.y - (panelPoint.y * radius));
			metrics = context.measureText(text);
			len = metrics.width;
			context.fillText(text, center.x + (panelPoint.x * radius * invertX) - (len / 2), center.y - (panelPoint.y * radius));
			context.closePath();
		}

		// Sun Path
		if (sunPath !== undefined) {
			context.lineWidth = 3;
			context.strokeStyle = colorConfig.sunColor;
			panelPoint = rotateBothWays(sunPath[0].alt + rotation, sunPath[0].z, side, tilt, addToZ);
			context.beginPath();
			for (let i = 1; i < sunPath.length; i++) {
				panelPoint = rotateBothWays(sunPath[i].alt + rotation, sunPath[i].z, side, tilt, addToZ);
				context.lineTo(center.x + (panelPoint.x * radius * invertX), center.y - (panelPoint.y * radius));
			}
			context.stroke();
			context.closePath();
			context.lineWidth = 1;
		}
		// Current Sun Pos.
		if (sunHe !== undefined && sunZ !== undefined) {
			panelPoint = rotateBothWays(rotation, sunZ, side, tilt, addToZ); // Horizon under the Sun
			// Center to horizon
			context.beginPath();
			context.moveTo(center.x, center.y);
			context.lineTo(center.x + (panelPoint.x * radius * invertX), center.y - (panelPoint.y * radius));
			context.stroke();
			context.closePath();
			// Up to the Sun
			context.beginPath();
			context.moveTo(center.x + (panelPoint.x * radius * invertX), center.y - (panelPoint.y * radius));
			for (let alt = 0; alt <= sunHe; alt++) {
				panelPoint = rotateBothWays(alt + rotation, sunZ, side, tilt, addToZ);
				context.lineTo(center.x + (panelPoint.x * radius * invertX), center.y - (panelPoint.y * radius));
			}
			panelPoint = rotateBothWays(sunHe + rotation, sunZ, side, tilt, addToZ);
			context.lineTo(center.x + (panelPoint.x * radius * invertX), center.y - (panelPoint.y * radius));
			context.stroke();
			context.closePath();
			// Draw the Sun
			context.fillStyle = colorConfig.sunColor;
			context.beginPath();
			context.arc(center.x + (panelPoint.x * radius * invertX), center.y - (panelPoint.y * radius), 10, 2 * Math.PI, false);
			context.fill();
			context.closePath();
			// Dotted line to center
			context.save();
			context.setLineDash([5, 3]);
			context.beginPath();
			context.moveTo(center.x + (panelPoint.x * radius * invertX), center.y - (panelPoint.y * radius));
			context.lineTo(center.x, center.y);
			context.stroke();
			context.closePath();
			context.restore();
			// Display values
			context.save();
			fontSize = 14;
			context.font = "" + Math.round(fontSize) + "px Arial";
			context.fillText("Alt:" + sunHe.toFixed(2) + "°", 10, 20);
			context.fillText("Z  :" + sunZ.toFixed(2) + "°", 10, 40);
			context.restore();
		}
	}
}
