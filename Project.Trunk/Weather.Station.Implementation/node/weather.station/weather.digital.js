// App specific code

var nbMessReceived = 0;

var displayRawDir;

var MONTHS = [
	"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
];

var init = function () {

	displayRawDir = new Raw16PointsDir('rawDirCanvas', 100);

	ws.onopen = function () {
		try {
			var text;
			try {
				text = 'Message:';
			} catch (err) {
				text = '<small>Connected</small>';
			}
			promptFld.innerHTML = text;
			if (nbMessReceived === 0) {
				statusFld.innerHTML = "";
			}
			statusFld.innerHTML += ((nbMessReceived === 0 ? "" : "<br>") + "<small>" +
					(new Date()).format("d-M-Y H:i:s._ Z") + "</small>:<font color='blue'>" + ' Connection opened.' + "</font>");
			statusFld.scrollTop = statusFld.scrollHeight;
			nbMessReceived++;
		} catch (err) {
		}
	};
	ws.onerror = function (error) {
		if (nbMessReceived === 0) {
			statusFld.innerHTML = "";
		}
		statusFld.innerHTML += ((nbMessReceived === 0 ? "" : "<br>") + "<small>" +
				(new Date()).format("d-M-Y H:i:s._ Z") + "</small>:<font color='red'>" + error.err + "</font>");
		statusFld.scrollTop = statusFld.scrollHeight;
		nbMessReceived++;
	};
	ws.onmessage = function (message) { // message/event
		var date = new Date();
		var lastUpdateDate = document.getElementById('update-date');
		var lastUpdateTime = document.getElementById('update-time');
		if (lastUpdateDate !== undefined && lastUpdateDate !== undefined) {
			var fmtDate = date.format("d-M-Y");
			lastUpdateDate.innerHTML = "<i>" + fmtDate + "</i>";
			var fmtTime = date.format("H:i:s Z")
			lastUpdateTime.innerHTML = "<i>" + fmtTime + "</i>";
		}

		var json = {};
		if (typeof(message.data) === 'string') {
			try {
				json = JSON.parse(message.data);
				/*
{
  avgdir:210,
  dew:6.273222382523795,
  dir:210,
  gust:0,
  hum:56.568665,
  press:101630,
  rain:0,
  speed:0,
  temp:14.8,
  volts:0.9240000247955322 // See voltageToDegrees below
}				 */
				var dir = json.dir;
				var ws = json.speed;
				var gst = json.gust;
				var press = json.press;
				var rain = json.rain;
				var dew = json.dew;
				var tempOut = json.temp;
				var humOut = json.hum;

				var rawWinDir = voltageToDegrees(json.volts);
//			console.log(rawWinDir);
				if (rawWinDir.card !== '-') {
//				document.getElementById('raw-wd').innerHTML = rawWinDir.card;
					displayRawDir.drawDisplay('rawDirCanvas', 45, { name: rawWinDir.card, value: rawWinDir.val });
				}

				twdArray.push(dir);
				while (twdArray.length > TWD_ARRAY_MAX_LEN) {
					twdArray = twdArray.slice(1); // Drop first element (0).
					//    console.log(">>> TWD Len:" + twdArray.length);
				}
				// Average
				var avg = averageDir(twdArray);

				document.getElementById('wind-dir').innerHTML = (dir.toFixed(0)); // + "&deg;");
				document.getElementById('wind-dir-avg').innerHTML = (avg.toFixed(0)); // + "&deg;");
				document.getElementById('wind-speed').innerHTML = (ws.toFixed(2)); // + " kt");
				document.getElementById('wind-gust').innerHTML = (gst.toFixed(2)); // + " kt");

				document.getElementById('prmsl').innerHTML = ((press/100).toFixed(1)); // + "&deg;");
				document.getElementById('hum-out').innerHTML = (humOut.toFixed(1)); // + "&deg;");
				document.getElementById('temp-out').innerHTML = (tempOut.toFixed(1)); // + "&deg;");
				document.getElementById('dew').innerHTML = (dew.toFixed(1)); // + "&deg;");
				document.getElementById('rain').innerHTML = (rain.toFixed(1)); // + "&deg;");
			} catch (e) {
				console.log(e);
				console.log('This doesn\'t look like a valid JSON: ' + message.data);
			}
		}
	};
	ws.onclose = function () {
		if (nbMessReceived === 0) {
			statusFld.innerHTML = "";
		}
		statusFld.innerHTML += ((nbMessReceived === 0 ? "" : "<br>") + "<small>" +
				(new Date()).format("d-M-Y H:i:s._ Z") + "</small>:<font color='blue'>" + ' Connection closed' + "</font>");
	};
};

var send = function (mess) {
	ws.send(mess);
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

var Voltage = {
	V3_3: {adjustment: 0.66},
	V5: {adjustment: 1.0}
};
const VARY_VALUE = 0.05;

var fuzzyCompare = function(thisValue, thatValue) {
	var b = false;
	if (thatValue > (thisValue * (1.0 - VARY_VALUE)) &&
			thatValue < (thisValue * (1.0 + VARY_VALUE))) {
		b = true;
	}
	return b;
};

var voltageToDegrees = function(value, v) {
	if (v === undefined) {
		v = Voltage.V3_3;
	}
	if (fuzzyCompare(3.84 * v.adjustment, value)) {
		return { val: 0, card: 'N' };
	}
	if (fuzzyCompare(1.98 * v.adjustment, value)) {
		return { val: 22.5, card: 'NNE' };
	}
	if (fuzzyCompare(2.25 * v.adjustment, value)) {
		return { val: 45, card: 'NE' };
	}
	if (fuzzyCompare(0.41 * v.adjustment, value)) {
		return { val: 67.5, card: 'ENE' };
	}
	if (fuzzyCompare(0.45 * v.adjustment, value)) {
		return { val: 90.0, card: 'E' };
	}
	if (fuzzyCompare(0.32 * v.adjustment, value)) {
		return { val: 112.5, card: 'ESE' };
	}
	if (fuzzyCompare(0.90 * v.adjustment, value)) {
		return { val: 135.0, card: 'SE' };
	}
	if (fuzzyCompare(0.62 * v.adjustment, value)) {
		return { val: 157.5, card: 'SSE' };
	}
	if (fuzzyCompare(1.40 * v.adjustment, value)) {
		return { val: 180, card: 'S' };
	}
	if (fuzzyCompare(1.19 * v.adjustment, value)) {
		return { val: 202.5, card: 'SSW' };
	}
	if (fuzzyCompare(3.08 * v.adjustment, value)) {
		return { val: 225, card: 'SW' };
	}
	if (fuzzyCompare(2.93 * v.adjustment, value)) {
		return { val: 247.5, card: 'WSW' };
	}
	if (fuzzyCompare(4.62 * v.adjustment, value)) {
		return { val: 270.0, card: 'W' };
	}
	if (fuzzyCompare(4.04 * v.adjustment, value)) {
		return { val: 292.5, card: 'WNW' };
	}
	if (fuzzyCompare(4.34 * v.adjustment, value)) { // chart in manufacturers documentation seems wrong
		return { val: 315.0, card: 'NW' };
	}
	if (fuzzyCompare(3.43 * v.adjustment, value)) {
		return { val: 337.5, card: 'NNW' };
	}
	// Oops
	return { val: 0, card: '-' };
}

var getClass = function (obj) {
	if (obj && typeof obj === 'object' &&
			Object.prototype.toString.call(obj) !== '[object Array]' &&
			obj.constructor) {
		var arr = obj.constructor.toString().match(/function\s*(\w+)/);
		if (arr && arr.length === 2) {
			return arr[1];
		}
	}
	return false;
};

// Date formatting
// Provide month names
Date.prototype.getMonthName = function () {
	var month_names = [
		'January',
		'February',
		'March',
		'April',
		'May',
		'June',
		'July',
		'August',
		'September',
		'October',
		'November',
		'December'
	];

	return month_names[this.getMonth()];
};

// Provide month abbreviation
Date.prototype.getMonthAbbr = function () {
	var month_abbrs = [
		'Jan',
		'Feb',
		'Mar',
		'Apr',
		'May',
		'Jun',
		'Jul',
		'Aug',
		'Sep',
		'Oct',
		'Nov',
		'Dec'
	];

	return month_abbrs[this.getMonth()];
};

// Provide full day of week name
Date.prototype.getDayFull = function () {
	var days_full = [
		'Sunday',
		'Monday',
		'Tuesday',
		'Wednesday',
		'Thursday',
		'Friday',
		'Saturday'
	];
	return days_full[this.getDay()];
};

// Provide full day of week name
Date.prototype.getDayAbbr = function () {
	var days_abbr = [
		'Sun',
		'Mon',
		'Tue',
		'Wed',
		'Thur',
		'Fri',
		'Sat'
	];
	return days_abbr[this.getDay()];
};

// Provide the day of year 1-365
Date.prototype.getDayOfYear = function () {
	var onejan = new Date(this.getFullYear(), 0, 1);
	return Math.ceil((this - onejan) / 86400000);
};

// Provide the day suffix (st,nd,rd,th)
Date.prototype.getDaySuffix = function () {
	var d = this.getDate();
	var sfx = ["th", "st", "nd", "rd"];
	var val = d % 100;

	return (sfx[(val - 20) % 10] || sfx[val] || sfx[0]);
};

// Provide Week of Year
Date.prototype.getWeekOfYear = function () {
	var onejan = new Date(this.getFullYear(), 0, 1);
	return Math.ceil((((this - onejan) / 86400000) + onejan.getDay() + 1) / 7);
};

// Provide if it is a leap year or not
Date.prototype.isLeapYear = function () {
	var yr = this.getFullYear();
	if ((parseInt(yr) % 4) === 0) {
		if (parseInt(yr) % 100 === 0) {
			if (parseInt(yr) % 400 !== 0) {
				return false;
			}
			if (parseInt(yr) % 400 === 0) {
				return true;
			}
		}
		if (parseInt(yr) % 100 !== 0) {
			return true;
		}
	}
	if ((parseInt(yr) % 4) !== 0) {
		return false;
	}
};

// Provide Number of Days in a given month
Date.prototype.getMonthDayCount = function () {
	var month_day_counts = [
		31,
		this.isLeapYear() ? 29 : 28,
		31,
		30,
		31,
		30,
		31,
		31,
		30,
		31,
		30,
		31
	];

	return month_day_counts[this.getMonth()];
};

// format provided date into this.format format
Date.prototype.format = function (dateFormat) {
	// break apart format string into array of characters
	dateFormat = dateFormat.split("");

	var date = this.getDate(),
			month = this.getMonth(),
			hours = this.getHours(),
			minutes = this.getMinutes(),
			seconds = this.getSeconds(),
			milli = this.getTime() % 1000,
			tzOffset = -(this.getTimezoneOffset() / 60);

	var lpad = function (s, w, len) {
		var str = s;
		while (str.length < len) {
			str = w + str;
		}
		return str;
	};

	// get all date properties ( based on PHP date object functionality )
	var date_props = {
				d: date < 10 ? '0' + date : date,
				D: this.getDayAbbr(),
				j: this.getDate(),
				l: this.getDayFull(),
				S: this.getDaySuffix(),
				w: this.getDay(),
				z: this.getDayOfYear(),
				W: this.getWeekOfYear(),
				F: this.getMonthName(),
				m: month < 10 ? '0' + (month + 1) : month + 1,
				M: this.getMonthAbbr(),
				n: month + 1,
				t: this.getMonthDayCount(),
				L: this.isLeapYear() ? '1' : '0',
				Y: this.getFullYear(),
				y: this.getFullYear() + ''.substring(2, 4),
				a: hours > 12 ? 'pm' : 'am',
				A: hours > 12 ? 'PM' : 'AM',
				g: hours % 12 > 0 ? hours % 12 : 12,
				G: hours > 0 ? hours : "12",
				h: hours % 12 > 0 ? hours % 12 : 12,
				H: hours < 10 ? '0' + hours : hours,
				i: minutes < 10 ? '0' + minutes : minutes,
				s: seconds < 10 ? '0' + seconds : seconds,
				Z: "UTC" + (tzOffset > 0 ? "+" : "") + tzOffset,
				_: lpad(milli, '0', 3)
			};

	// loop through format array of characters and add matching data else add the format character (:,/, etc.)
	var date_string = "";
	for (var i = 0; i < dateFormat.length; i++) {
		var f = dateFormat[i];
		if (f.match(/[a-zA-Z|_]/g)) {
			date_string += date_props[f] ? date_props[f] : '';
		} else {
			date_string += f;
		}
	}
	return date_string;
};
