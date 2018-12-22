"use strict";
/*
 * @author Olivier Le Diouris
 * Uses ES6 Promises
 */
let initAjax = function () {
	let interval = setInterval(function () {
		fetch();
	}, 1000);
};

const DEFAULT_TIMEOUT = 60000; // 1 minute
/* global events */

/* Uses ES6 Promises */
function getPromise(
		url,                          // full api path
		timeout,                      // After that, fail.
		verb,                         // GET, PUT, DELETE, POST, etc
		happyCode,                    // if met, resolve, otherwise fail.
		data = null,                  // payload, when needed (PUT, POST...)
		show = true) {                // Show the traffic [true]|false

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
			let mess = { code: 408, message: 'Timeout' };
			reject(mess);
		}, TIMEOUT);

		xhr.onload = function () {
			clearTimeout(requestTimer);
			if (xhr.status === happyCode) {
				resolve(xhr.response);
			} else {
				reject({ code: xhr.status, message: xhr.response });
			}
		};
	});
	return promise;
}

function getNMEAData() {
	return getPromise('/mux/cache', DEFAULT_TIMEOUT, 'GET', 200, null, false);
}

function fetch() {
	let getData = getNMEAData();
	getData.then(function (value) { // resolve
		let json = JSON.parse(value);
		onMessage(json);
	}, function (error) { // reject
		console.log("Failed to get the Sight Reduction result..." + (error !== undefined && error.code !== undefined ? error.code : ' - ') + ', ' + (error !== undefined && error.message !== undefined ? error.message : ' - '));
	});
}

function onMessage(json) {
	try {
		let errMess = "";

		try {
			let latitude = json.Position.lat;
//    console.log("latitude:" + latitude)
			let longitude = json.Position.lng;
//    console.log("Pt:" + latitude + ", " + longitude);
			events.publish('pos', {
				'lat': latitude,
				'lng': longitude
			});
		} catch (err) {
			errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "position");
		}
		// Displays
		try {
			let bsp = json.BSP.speed;
			events.publish('bsp', bsp);
		} catch (err) {
			errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "boat speed");
		}
		try {
			let log = json.Log.distance;
			events.publish('log', log);
		} catch (err) {
			errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "log (" + err + ")");
		}
		try {
			let gdt = json["GPS Date & Time"];
			let gpsDate = new Date(gdt.fmtDate.year, gdt.fmtDate.month - 1, gdt.fmtDate.day, gdt.fmtDate.hour, gdt.fmtDate.min, gdt.fmtDate.sec, 0);
			// UTC dates
			events.publish('gps-time', gpsDate);
		} catch (err) {
			errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "GPS Date (" + err + ")");
		}

		try {
			let hdg = json["HDG true"].angle;
			events.publish('hdg', hdg);
		}
		catch (err) {
			errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "heading");
		}
		try {
			let twd = json.TWD.angle;
			events.publish('twd', twd);
		}
		catch (err) {
			errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "TWD");
		}
		try {
			let twa = json.TWA.angle;
			events.publish('twa', twa);
		}
		catch (err) {
			errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "TWA");
		}
		try {
			let tws = json.TWS.speed;
			events.publish('tws', tws);
		}
		catch (err) {
			errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "TWS");
		}

		try {
			let waterTemp = json["Water Temperature"].temperature;
			events.publish('wt', waterTemp);
		}
		catch (err) {
			errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "water temperature");
		}

		try {
			let airTemp = json["Air Temperature"].temperature;
			events.publish('at', airTemp);
		}
		catch (err) {
			errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "air temperature");
		}
		// Battery_Voltage, Relative_Humidity, Barometric_Pressure
		try {
			let baro = json["Barometric Pressure"].pressure;
			if (baro != 0) {
				events.publish('prmsl', baro);
			}
		}
		catch (err) {
			errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "PRMSL");
		}
		try {
			let hum = json["Relative Humidity"];
			if (hum > 0) {
				events.publish('hum', hum);
			}
		}
		catch (err) {
			errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "Relative_Humidity");
		}
		try {
			let aws = json.AWS.speed;
			events.publish('aws', aws);
		}
		catch (err) {
			errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "AWS");
		}
		try {
			let awa = json.AWA.angle;
			events.publish('awa', awa);
		}
		catch (err) {
			errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "AWA");
		}
		try {
			let cdr = json.CDR.angle;
			events.publish('cdr', cdr);
		}
		catch (err) {
			errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "CDR");
		}

		try {
			let cog = json.COG.angle;
			events.publish('cog', cog);
		}
		catch (err) {
			errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "COG");
		}
		try {
			let cmg = json.CMG.angle;
			events.publish('cmg', cmg);
		}
		catch (err) {
			errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "CMG");
		}
		try {
			let leeway = json.Leeway.angle;
			events.publish('leeway', leeway);
		}
		catch (err) {
			errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "Leeway");
		}
		try {
			let csp = json.CSP.speed;
			events.publish('csp', csp);
		}
		catch (err) {
			errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "CSP");
		}

		// Buffered current
		try {
			let buffered = json['Current calculated with damping'];
			if (buffered !== undefined) {
				let keys = Object.keys(buffered);
				for (let i=0; i<keys.length; i++) {
					let k = keys[i];
//				console.log("K:" + k);
					let damp = buffered[k];
//				console.log("Publishing csp-" + k);
					events.publish("csp-" + k, damp.speed.speed);
					events.publish("cdr-" + k, damp.direction.angle);
				}
			}
		} catch (err) {
			console.log(err);
		}

		try {
			let sog = json.SOG.speed;
			events.publish('sog', sog);
		}
		catch (err) {
			errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "SOG");
		}
		// to-wp, vmg-wind, vmg-wp, b2wp
		try {
			let to_wp = json["To Waypoint"];
			let b2wp = json["Bearing to WP"].angle;
			events.publish('wp', {
				'to_wp': to_wp,
				'b2wp': b2wp
			});
		}
		catch (err) {
		}

		try {
			events.publish('vmg', {
				'onwind': json["VMG on Wind"],
				'onwp': json["VMG to Waypoint"]
			});

		}
		catch (err) {
			errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "VMG");
		}

		if (errMess !== undefined)
			displayErr(errMess);
	}
	catch (err) {
		displayErr(err);
	}
}
