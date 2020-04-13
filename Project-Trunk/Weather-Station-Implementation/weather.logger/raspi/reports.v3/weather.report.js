/*
 * Weather station data parser
 * By OlivSoft
 * olivier@lediouris.net

 Sample data:
  [{
        "time": "2015-07-05 23:58:41",
        "wdir": 0,
        "gust": 0.00,
        "ws": 0.00,
        "rain": 0.000,
        "press": 1012.3800000,
        "atemp": 22.800,
        "hum": 72.499,
        "dew": 33.600
    }, {
        "time": "2015-07-06 00:08:40",
        "wdir": 270,
        "gust": 7.95,
        "ws": 4.74,
        "rain": 0.000,
        "press": 1012.4200000,
        "atemp": 22.700,
        "hum": 73.781,
        "dew": 32.000
    }, {...
      ]
 */

let JSONParser = {
	nmeaData: [],
	position: {},

	/*
		data look like
		 [{
				"time": "2015-07-05 23:58:41",
				"wdir": 0,
				"gust": 0.00,
				"ws": 0.00,
				"rain": 0.000,
				"press": 1012.3800000,
				"atemp": 22.800,
				"hum": 72.499,
				"dew": 33.600
		}, {..} ]
	*/

	parse: function (wsJSONContent, cb, cb2) {
		JSONParser.nmeaData = [];
		let linkList = "";
		// For timestamps like 2015-07-05 23:58:41
		let regExp = new RegExp("(\\d{4})-(\\d{2})-(\\d{2})\\s(\\d{2}):(\\d{2}):(\\d{2})");

		for (let i = 0; i < wsJSONContent.length; i++) {
			let date = wsJSONContent[i].time;
			let dd = null;
			let matches = regExp.exec(date);
			if (matches !== null) {
				let y = matches[1];
				let mo = matches[2];
				let d = matches[3];
				let h = matches[4];
				let mi = matches[5];
				let s = matches[6];
				dd = new Date(y, mo - 1, d, h, mi, s, 0);
				// console.log("\t>> new date: ", d);
			}
			let prmsl = wsJSONContent[i].press;
			let tws = wsJSONContent[i].ws;
			let gust = wsJSONContent[i].gust;
			let twd = wsJSONContent[i].wdir;
			let rain = wsJSONContent[i].rain;
			let temp = wsJSONContent[i].atemp;
			let hum = wsJSONContent[i].hum;
			let dew = wsJSONContent[i].dew;

//		console.info("Line:" + date + ":" + dd);
			JSONParser.nmeaData.push(new NMEAData(dd, prmsl, tws, gust, twd, rain, temp, hum, dew));
		}
	}
};

function NMEAData(date, prmsl, tws, gust, twd, rain, atemp, hum, dew) {
	let nmeaDate = date;
	let nmeaPrmsl = prmsl;
	let nmeaTws = tws;
	let nmeaGust = gust;
	let nmeaTwd = twd;
	let nmeaRain = rain;
	let nmeaTemp = atemp;
	let nmeaHum = hum;
	let nmeaDew = dew;

	this.getNMEADate = function () {
		return nmeaDate;
	};

	this.getNMEAPrmsl = function () {
		return nmeaPrmsl;
	};

	this.getNMEATws = function () {
		return nmeaTws;
	};

	this.getNMEAGust = function () {
		return nmeaGust;
	};

	this.getNMEATwd = function () {
		return nmeaTwd;
	};

	this.getNMEARain = function () {
		return nmeaRain;
	};

	this.getNMEATemp = function () {
		return nmeaTemp;
	};

	this.getNMEAHum = function () {
		return nmeaHum;
	};

	this.getNMEADew = function () {
		return nmeaDew;
	};
};

const DEBUG = true;
const DEFAULT_TIMEOUT = 60000; // 1 minute

/* global events */

/* Uses ES6 Promises */
function getPromise(
		url,                          // full api path
		timeout,                      // After that, fail.
		verb,                         // GET, PUT, DELETE, POST, etc
		happyCode,                    // if met, resolve, otherwise fail.
		data = null,                  // payload, when needed (PUT, POST...)
		show = false) {               // Show the traffic [true]|false

	if (show === true) {
		document.body.style.cursor = 'wait';
	}

	if (DEBUG) {
		console.log(">>> Promise", verb, url);
	}

	let promise = new Promise(function (resolve, reject) {
		let xhr = new XMLHttpRequest();
		let TIMEOUT = timeout;

		let req = verb + " " + url;
		if (data !== undefined && data !== null) {
			req += ("\n" + JSON.stringify(data, null, 2));
		}

		xhr.open(verb, url, true);
		xhr.setRequestHeader("Content-type", "application/json");
		try {
			if (data === undefined || data === null) {
				xhr.send();
			} else {
				xhr.send(JSON.stringify(data));
			}
		} catch (err) {
			console.log("Send Error ", err);
		}

		let requestTimer = setTimeout(function () {
			xhr.abort();
			let mess = {code: 408, message: 'Timeout'};
			reject(mess);
		}, TIMEOUT);

		xhr.onload = function () {
			clearTimeout(requestTimer);
			if (xhr.status === happyCode) {
				resolve(xhr.response);
			} else {
				reject({code: xhr.status, message: xhr.response});
			}
		};
	});
	return promise;
}

