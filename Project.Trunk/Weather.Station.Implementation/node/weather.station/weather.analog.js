/*
 * @author Olivier Le Diouris
 */
var displayTWD, displayTWS, displayGUST, thermometer,
		displayBaro, displayHum, displayRain, dewTemp;

var animate = false;

var init = function () {
	displayTWD = new Direction('twdCanvas', 100, 45, 5, true);
	displayTWS = new AnalogDisplay('twsCanvas', 100, 65, 10, 1, true, 40);
	displayTWS.setWithMinMax(true);
	displayGUST = new AnalogDisplay('gustCanvas', 100, 65, 10, 1, true, 40);
	displayGUST.setWithMinMax(true);
	thermometer = new Thermometer('tmpCanvas', 200);
	displayBaro = new AnalogDisplay('baroCanvas', 100, 1040, 10, 1, true, 40, 980);
	displayHum = new AnalogDisplay('humCanvas', 100, 100, 10, 1, true, 40);
	displayRain = new Pluviometer('rainCanvas');
	dewTemp = new Thermometer('dewCanvas', 200);
};

var initWS = function () {
	var connection;
	// if user is running mozilla then use it's built-in WebSocket
	//  window.WebSocket = window.WebSocket || window.MozWebSocket;  // TODO otherwise, fall back
	var ws = window.WebSocket || window.MozWebSocket;  // TODO otherwise, fall back
	// if browser doesn't support WebSocket, just show some notification and exit
	//  if (!window.WebSocket)
	if (!ws) {
		alert('Sorry, but your browser does not support WebSockets.'); // TODO? Fallback
		return;
	}
	// open connection
	var rootUri = "ws://" + (document.location.hostname === "" ? "localhost" : document.location.hostname) + ":" +
			(document.location.port === "" ? "9876" : document.location.port);
	console.log(rootUri);
	try {
		connection = new WebSocket(rootUri); // 'ws://localhost:9876');
		connection.onopen = function () {
			console.log('Connected.')
		};
		connection.onerror = function (error) {
			// just in there were some problems with connection...
			alert('Sorry, but there is some problem with your connection or the server is down.');
		};
		connection.onmessage = function (message) {
			//  console.log('onmessage:' + JSON.stringify(message.data));
			var data = JSON.parse(message.data); // TODO Raw direction voltage?
			setValues(data);
		};
	} catch (err) {
		console.log(">>> Connection:" + err);
	}
};

var initAjax = function () {
	var interval = setInterval(function () {
				pingWeatherServer();
			},
			1000);
};

var pingWeatherServer = function () {
	try {
		var xhr = new XMLHttpRequest(); // json
		xhr.onreadystatechange = function () {
			if (xhr.readyState === 4 && xhr.status === 200) {
				var doc = JSON.parse(xhr.responseText);
				setValues(doc);
			}
		}
		xhr.open("GET", "/getJsonData", true);
		xhr.send();
		// doc = JSON.parse(xhr.responseText); // If sync
		// setValues(doc);
	} catch (err) {
		console.log(err);
	}
};

var changeBorder = function (b) {
	displayTWD.setBorder(b);
	displayTWS.setBorder(b);
	displayGUST.setBorder(b);
	displayBaro.setBorder(b);
	displayHum.setBorder(b);
};

var TOTAL_WIDTH = 800;

var resizeDisplays = function (wwidth) {
	displayTWS.setDisplaySize(80 * (Math.min(wwidth, TOTAL_WIDTH) / TOTAL_WIDTH));
	displayGUST.setDisplaySize(80 * (Math.min(wwidth, TOTAL_WIDTH) / TOTAL_WIDTH));
	displayTWD.setDisplaySize(80 * (Math.min(wwidth, TOTAL_WIDTH) / TOTAL_WIDTH));
	thermometer.setDisplaySize(160 * (Math.min(wwidth, TOTAL_WIDTH) / TOTAL_WIDTH));
	displayBaro.setDisplaySize(80 * (Math.min(wwidth, TOTAL_WIDTH) / TOTAL_WIDTH));
	displayHum.setDisplaySize(80 * (Math.min(wwidth, TOTAL_WIDTH) / TOTAL_WIDTH));
	dewTemp.setDisplaySize(160 * (Math.min(wwidth, TOTAL_WIDTH) / TOTAL_WIDTH));
};

var twdArray = [];
var TWD_ARRAY_MAX_LEN = 100;  // TODO Make it a prm

var toRadians = function (deg) {
	return deg * (Math.PI / 180);
};

var toDegrees = function (rad) {
	return rad * (180 / Math.PI);
};

var averageDir = function (va) {
	var sumCos = 0, sumSin = 0;
	var len = va.length;
//var sum = 0;
	for (var i = 0; i < len; i++) {
//  sum += va[i];
		sumCos += Math.cos(toRadians(va[i]));
		sumSin += Math.sin(toRadians(va[i]));
	}
	var avgCos = sumCos / len;
	var avgSin = sumSin / len;

	var aCos = toDegrees(Math.acos(avgCos));
//var aSin = toDegrees(Math.asin(avgSin));
	var avg = aCos;
	if (avgSin < 0) {
		avg = 360 - avg;
	}
	return avg;
//return sum / len;
};

var MONTHS = [
	"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
];

var setValues = function (doc) {
	var date = new Date();
	var lastUpdateDate = document.getElementById('update-date');
	var lastUpdateTime = document.getElementById('update-time');
	if (lastUpdateDate !== undefined && lastUpdateDate !== undefined) {
		var fmtDate = date.getDate() + "-" + MONTHS[date.getMonth()] + "-" + date.getFullYear();
		lastUpdateDate.innerHTML = "<i>" + fmtDate + "</i>";
		var fmtTime = lpad(date.getHours().toString(), '0', 2) + ":" +
				lpad(date.getMinutes().toString(), '0', 2) + ":" +
				lpad(date.getSeconds().toString(), '0', 2);
		lastUpdateTime.innerHTML = "<i>" + fmtTime + "</i>";
	}
	try {
		var errMess = "";
		var json = doc;
		// Displays
		try {
			var twd = parseFloat(json.dir.toFixed(0)) % 360;
			// Damping
			twdArray.push(twd);
			while (twdArray.length > TWD_ARRAY_MAX_LEN) {
				twdArray = twdArray.slice(1); // Drop first element (0).
				//    console.log(">>> TWD Len:" + twdArray.length);
			}
			// Average
//    displayTWD.animate(averageDir(twdArray));
			displayTWD.setValue(averageDir(twdArray));
			document.getElementById('winddir-ok').checked = true;
		} catch (err) {
			errMess += ((errMess.length > 0 ? "\n" : "") + "Problem with TWD...");
//    displayTWD.animate(0.0);
			displayTWD.setValue(0.0);
			document.getElementById('winddir-ok').checked = false;
		}

		try {
			var tws = parseFloat(json.speed.toFixed(2));
			if (animate) {
				displayTWS.animate(tws);
			} else {
				displayTWS.setValue(tws);
			}
			document.getElementById('windspeed-ok').checked = true;
		} catch (err) {
			errMess += ((errMess.length > 0 ? "\n" : "") + "Problem with TWS...");
//    displayTWS.animate(0.0);
			displayTWS.setValue(0.0);
			document.getElementById('windspeed-ok').checked = false;
		}
		try {
			var gust = parseFloat(json.gust.toFixed(2));
			if (animate) {
				displayGUST.animate(gust);
			} else {
				displayGUST.setValue(gust);
			}
		} catch (err) {
			errMess += ((errMess.length > 0 ? "\n" : "") + "Problem with TWS gust...");
//    displayGUST.animate(0.0);
			displayGUST.setValue(0.0);
		}
		try {
			var temp = parseFloat(json.temp.toFixed(1));
//    thermometer.animate(temp);
			thermometer.setValue(temp);
			document.getElementById('airtemp-ok').checked = true;
		} catch (err) {
			errMess += ((errMess.length > 0 ? "\n" : "") + "Problem with temperature...");
//    thermometer.animate(0.0);
			thermometer.setValue(0.0);
			document.getElementById('airtemp-ok').checked = false;
		}
		try {
			var baro = parseFloat(json.press / 100);
			if (!isNaN(baro) && baro != 0) {
				if (animate) {
					displayBaro.animate(baro);
				} else {
					displayBaro.setValue(baro);
				}
			} else {
				displayBaro.setValue(980.0);
			}
			document.getElementById('press-ok').checked = (!isNaN(baro) && baro != 0);
		} catch (err) {
			document.getElementById('press-ok').checked = false;
//    errMess += ((errMess.length > 0?"\n":"") + "Problem with air Barometric_Pressure...");
//    displayBaro.animate(0.0);
			displayBaro.setValue(980.0);
		}
		try {
			if (json.hum !== undefined) {
				var hum = parseFloat(json.hum);
				document.getElementById('humCanvas').style.display = 'inline';
				if (hum > 0) {
					if (animate) {
						displayHum.animate(hum);
					} else {
						displayHum.setValue(hum);
					}
					document.getElementById('hum-ok').checked = true;
				}
			} else {
				document.getElementById('humCanvas').style.display = 'none';
			}
		} catch (err) {
			errMess += ((errMess.length > 0 ? "\n" : "") + "Problem with air Relative_Humidity...");
			document.getElementById('humCanvas').style.display = 'none';
//    displayHum.animate(0.0);
			displayHum.setValue(0.0);
			document.getElementById('hum-ok').checked = false;
		}
		try {
			var rain = parseFloat(json.rain.toFixed(2));
			if (animate) {
				displayRain.animate(rain);
			} else {
				displayRain.setValue(rain);
			}
			document.getElementById('rain-ok').checked = true;
		} catch (err) {
			errMess += ((errMess.length > 0 ? "\n" : "") + "Problem with Rain...");
//    displayTWS.animate(0.0);
			displayRain.setValue(0.0);
			document.getElementById('rain-ok').checked = false;
		}
		try {
			var dew = parseFloat(json.dew.toFixed(1));
//    thermometer.animate(cpu);
			dewTemp.setValue(dew);
			document.getElementById('dew-ok').checked = true;
		} catch (err) {
			errMess += ((errMess.length > 0 ? "\n" : "") + "Problem with CPU temperature...");
//    thermometer.animate(0.0);
			dewTemp.setValue(0.0);
			document.getElementById('dew-ok').checked = false;
		}

		if (errMess !== undefined) {
			document.getElementById("err-mess").innerHTML = errMess;
		}
	} catch (err) {
		document.getElementById("err-mess").innerHTML = err;
	}
};

var lpad = function (str, pad, len) {
	while (str.length < len) {
		str = pad + str;
	}
	return str;
};
