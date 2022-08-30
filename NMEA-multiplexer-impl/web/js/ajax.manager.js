/**
 * Uses ES6 Promises.
 * 
 * @author Olivier Le Diouris
 */
let forwardAjaxErrors = true;

function initAjax(forwardErrors) {
	if (forwardErrors !== undefined) {
		forwardAjaxErrors = forwardErrors;
	}
	let interval = setInterval(function () {
		fetch();
	}, 1000);
}

function getNMEAData() {

	let url = '/mux/cache',
			xhr = new XMLHttpRequest(),
			verb = 'GET',
			data = null,
			happyCode = 200,
			TIMEOUT = 10000;

	let promise = new Promise((resolve, reject) => {
		let xhr = new XMLHttpRequest();

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

		xhr.onload = () => {
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

function fetch() {
	let getData = getNMEAData();
	getData.then((value) => {
		// console.log("Done:", value);
		let json = JSON.parse(value);
		onMessage(json);
	}, (error, errmess) => {
		let message;
		if (errmess !== undefined) {
			let mess = JSON.parse(errmess);
			if (mess.message !== undefined) {
				message = mess.message;
			}
		}
		console.debug("Failed to get nmea data..." + (error !== undefined ? error : ' - ') + ', ' + (message !== undefined ? message : ' - '));
	});
}

const EVENT_FULL     = 'full';
const EVENT_POS      = 'pos';
const EVENT_BSP      = 'bsp';
const EVENT_LOG      = 'log';
const EVENT_GPS_TIME = 'gps-time';
const EVENT_HDG      = 'hdg';
const EVENT_TWD      = 'twd';
const EVENT_TWA      = 'twa';
const EVENT_TWS      = 'tws';
const EVENT_WT       = 'wt';
const EVENT_AT       = 'at';
const EVENT_PRMSL    = 'prmsl';
const EVENT_HUM      = 'hum';
const EVENT_AWS      = 'aws';
const EVENT_AWA      = 'awa';
const EVENT_CDR      = 'cdr';
const EVENT_COG      = 'cog';
const EVENT_SOG      = 'sog';
const EVENT_CMG      = 'cmg';
const EVENT_LEEWAY   = 'leeway';
const EVENT_CSP      = 'csg';
const EVENT_WP       = 'wp';
const EVENT_VMG      = 'vmg';
const EVENT_PRATE    = 'prate';
const EVENT_DEW      = 'dew';

function onMessage(json) {
	try {
		let errMess = "";

		try {
			events.publish(EVENT_FULL, json);
		} catch (err) {
			errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "full data");
		}

		try {
			let latitude = json.Position.lat;
//          console.log("latitude:" + latitude)
			let longitude = json.Position.lng;
//          console.log("Pt:" + latitude + ", " + longitude);
			events.publish(EVENT_POS, {
				'lat': latitude,
				'lng': longitude
			});
		} catch (err) {
			errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "position");
		}
		// Publishes
		try {
			let bsp = json.BSP.speed;
			events.publish(EVENT_BSP, bsp);
		} catch (err) {
			errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "boat speed");
		}
		try {
			let log = json.Log.distance;
			events.publish(EVENT_LOG, log);
		} catch (err) {
			errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "log (" + err + ")");
		}
		try {
			let gpsDate = json["GPS Date & Time"].date;
			events.publish(EVENT_GPS_TIME, gpsDate);
		} catch (err) {
			errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "GPS Date (" + err + ")");
		}

		try {
			let hdg = json["HDG true"].angle;
			events.publish(EVENT_HDG, hdg);
		} catch (err) {
			errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "heading");
		}
		try {
			let twd = json.TWD.angle;
			events.publish(ENEVT_TWD, twd);
		} catch (err) {
			errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "TWD");
		}
		try {
			let twa = json.TWA.angle;
			events.publish(EVENT_TWA, twa);
		} catch (err) {
			errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "TWA");
		}
		try {
			let tws = json.TWS.speed;
			events.publish(EVENT_TWS, tws);
		} catch (err) {
			errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "TWS");
		}

		try {
			let waterTemp = json["Water Temperature"].temperature;
			events.publish(EVENT_WT, waterTemp);
		} catch (err) {
			errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "water temperature");
		}

		try {
			let airTemp = json["Air Temperature"].temperature;
			events.publish(EVENT_AT, airTemp);
		} catch (err) {
			errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "air temperature");
		}
		// Battery_Voltage, Relative_Humidity, Barometric_Pressure
		try {
			let baro = json["Barometric Pressure"].pressure;
			if (baro != 0) {
				events.publish(EVENT_PRMSL, baro);
			}
		} catch (err) {
			errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "PRMSL");
		}
		try {
			let hum = json["Relative Humidity"];
			if (hum > 0) {
				events.publish(EVENT_HUM, hum);
			}
		} catch (err) {
			errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "Relative_Humidity");
		}
		try {
			let aws = json.AWS.speed;
			events.publish(EVENT_AWS, aws);
		} catch (err) {
			errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "AWS");
		}
		try {
			let awa = json.AWA.angle;
			events.publish(EVENT_AWA, awa);
		} catch (err) {
			errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "AWA");
		}
		try {
			let cdr = json.CDR.angle;
			events.publish(EVENT_CDR, cdr);
		} catch (err) {
			errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "CDR");
		}

		try {
			let cog = json.COG.angle;
			events.publish(EVENT_COG, cog);
		} catch (err) {
			errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "COG");
		}
		try {
			let cmg = json.CMG.angle;
			events.publish(EVENT_CMG, cmg);
		} catch (err) {
			errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "CMG");
		}
		try {
			let leeway = json.Leeway.angle;
			events.publish(EVENT_LEEWAY, leeway);
		} catch (err) {
			errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "Leeway");
		}
		try {
			let csp = json.CSP.speed;
			events.publish(EVENT_CSP, csp);
		} catch (err) {
			errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "CSP");
		}
		try {
			let sog = json.SOG.speed;
			events.publish(EVGENT_SOG, sog);
		} catch (err) {
			errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "SOG");
		}
		// to-wp, vmg-wind, vmg-wp, b2wp
		try {
			let to_wp = json["To Waypoint"];
			let b2wp = json["Bearing to WP"].angle;
			events.publish(EVENT_WP, {
				'to_wp': to_wp,
				'b2wp': b2wp
			});
		} catch (err) {
		}

		try {
			events.publish(EVENT_VMG, {
				'onwind': json["VMG on Wind"],
				'onwp': json["VMG to Waypoint"]
			});
		} catch (err) {
			errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "VMG");
		}

		try {
			let prate = json.prate;
			events.publish(EVENT_PRATE, prate);
		} catch (err) {
			errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "prate");
		}
		try {
			let dew = json.dewpoint;
			events.publish(EVENT_DEW, dew);
		} catch (err) {
			errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "dew");
		}

		if (errMess !== undefined && forwardAjaxErrors) {
			displayErr(errMess);
		}
	} catch (err) {
		displayErr(err);
	}
}
