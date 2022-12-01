/*
 * @author Olivier Le Diouris
 * Uses ES6 Promises for Ajax.
 *
 * Note: To add Basic Authorization, add header like
					headers.push({
						name: 'Authorization',
						value: 'Basic ' + btoa('username:password')
					});
 */

const DEBUG = false;

function initAjax() {
	let interval = setInterval(() => {
		fetch();
	}, 1000);
}

const DEFAULT_TIMEOUT = 60000; // 1 minute
/* global events */

/* Uses ES6 Promises */
function getPromise(
		url,                          // full api path
		timeout,                      // After that, fail.
		verb,                         // GET, PUT, DELETE, POST, etc
		headers,                      // Headers
		happyCode,                    // if met, resolve, otherwise fail.
		data = null,                  // payload, when needed (PUT, POST...)
		show = true) {                // Show the traffic [true]|false

	if (show === true) {
		document.body.style.cursor = 'wait';
	}

	if (DEBUG) {
		console.log(">>> Promise", verb, url);
	}

	let promise = new Promise((resolve, reject) => {
		let xhr = new XMLHttpRequest();
		let TIMEOUT = timeout;

		let req = verb + " " + url;
		if (data !== undefined && data !== null) {
			req += ("\n" + JSON.stringify(data, null, 2));
		}

		xhr.open(verb, url, true);
		// xhr.setRequestHeader("Content-type", "application/json");
		if (headers !== undefined && headers !== null) {
			headers.forEach(header => {
				xhr.setRequestHeader(header.name, header.value);
			});
		}
		try {
			if (data === undefined || data === null) {
				xhr.send();
			} else {
				xhr.send(JSON.stringify(data));
			}
		} catch (err) {
			console.log("Send Error ", err);
		}

		let requestTimer = setTimeout(() => {
			xhr.abort();
			let mess = { code: 408, message: 'Timeout' };
			reject(mess);
		}, TIMEOUT);

		xhr.onload = () => {
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
	return getPromise('/mux/cache', DEFAULT_TIMEOUT, 'GET', [{ name: 'Accept', value: 'application/json' }], 200, null, false);
}

function fetch() {
	let getData = getNMEAData();
	getData.then((value) => { // Resolve
//  console.log("Done:", value);
		try {
			let json = JSON.parse(value);
			onMessage(json);
		} catch (err) {
			console.log("Error:", err, ("\nfor value [" + value + "]"));
		}
	}, (error) => { // Reject
		console.log("Failed to get NMEA data..." + (error !== undefined && error.code !== undefined ? error.code : ' - ') + ', ' + (error !== undefined && error.message !== undefined ? error.message : ' - '));
	});
}

/**
 *
 * @param when UTC, Duration format: like "Y-m-dTH:i:s" -> 2018-09-10T10:09:00
 * @param position { lat: 37.7489, lng: -122.507 }
 * @param wandering true|false
 * @param stars true|false
 * @returns {Promise<any>}
 */
function getSkyGP(when, position, wandering, stars) {
	let url = "/astro/positions-in-the-sky";
	// Add date
	url += ("?at=" + when);
	url += ("&fromL=" + position.lat);
	url += ("&fromG=" + position.lng);
	// Wandering bodies
	if (wandering !== undefined && wandering === true) { // to minimize the size of the payload
		url += ("&wandering=true");
	}
	// Stars
	if (stars !== undefined && stars === true) { // to minimize the size of the payload
		url += ("&stars=true");
	}
	return getPromise(url, DEFAULT_TIMEOUT, 'GET', [{ name: 'Accept', value: 'application/json' }], 200, null, false);
}

/**
 *
 * @param when UTC, Duration format: like "Y-m-dTH:i:s" -> 2018-09-10T10:09:00
 * @param position { lat: 37.7489, lng: -122.507 }
 * @param wandering true|false
 * @param stars true|false
 * @returns {Promise<any>}
 */
function getAstroData(when, position, wandering, stars, callback) {
	let getData = getSkyGP(when, position, wandering, stars);
	getData.then((value) => { // resolve
		let json = JSON.parse(value);
		if (callback !== undefined) {
			callback(json);
		} else {
			console.log(JSON.stringify(json, null, 2));
		}
	}, (error) => { // reject
		console.log("Failed to get the Astro Data..." + (error !== undefined && error.code !== undefined ? error.code : ' - ') + ', ' + (error !== undefined && error.message !== undefined ? error.message : ' - '));
	});
}

function setUTC(epoch) {
	let url = "/mux/utc";
	let obj = { epoch: epoch };
	return getPromise(url, DEFAULT_TIMEOUT, 'PUT', [{ name: 'Content-Type', value: 'application/json' }], 200, obj, false);
}

function setPosition(lat, lng) {
	let url = "/mux/position";
	let obj = { lat: lat, lng: lng };
	return getPromise(url, DEFAULT_TIMEOUT, 'POST', [{ name: 'Content-Type', value: 'application/json' }], 200, obj, false);
}

function setUTCTime(epoch, callback) {
	let setData = setUTC(epoch);
	setData.then((value) => { // resolve
		if (value !== undefined && value !== null && value.length > 0) {
			let json = JSON.parse(value);
			if (callback !== undefined) {
				callback(json);
			} else {
				console.log(JSON.stringify(json, null, 2));
			}
		}
	}, (error) => { // reject
		console.log("Failed to set the UTC date and time..." + (error !== undefined && error.code !== undefined ? error.code : ' - ') + ', ' + (error !== undefined && error.message !== undefined ? error.message : ' - '));
	});
}

function setUserPos(lat, lng, callback) {
	let setData = setPosition(lat, lng);
	setData.then((value) => { // resolve
		if (value !== undefined && value !== null && value.length > 0) {
			let json = JSON.parse(value);
			if (callback !== undefined) {
				callback(json);
			} else {
				console.log(JSON.stringify(json, null, 2));
			}
		}
	}, (error) => { // reject
		console.log("Failed to set the UTC date and time..." + (error !== undefined && error.code !== undefined ? error.code : ' - ') + ', ' + (error !== undefined && error.message !== undefined ? error.message : ' - '));
	});
}

function getQueryParameterByName(name, url) {
	if (!url) url = window.location.href;
	name = name.replace(/[\[\]]/g, "\\$&");
	let regex = new RegExp("[?&]" + name + "(=([^&#]*)|&|#|$)"),
			results = regex.exec(url);
	if (!results) {
		return null;
	}
	if (!results[2]) {
		return '';
	}
	return decodeURIComponent(results[2].replace(/\+/g, " "));
}

// Takes care of re-broadcasting the data to whoever subscribed to it.
function onMessage(json) {

	events.publish('raw', json);

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
			// let date = new Date(Date.UTC(gdt.fmtDate.year, gdt.fmtDate.month - 1, gdt.fmtDate.day, gdt.fmtDate.hour, gdt.fmtDate.min, gdt.fmtDate.sec, 0));
			// let date = new Date(gdt.epoch); 
            // let gpsDate = new Date(gdt.epoch); // new Date(date.getTime() + date.getTimezoneOffset() * 60000);
            // let gpsDate =  new Date(Date.UTC(date.getUTCFullYear(), date.getUTCMonth(), date.getUTCDate(), date.getUTCHours(), date.getUTCMinutes(), date.getUTCSeconds()));
			let gpsDate = new Date(gdt.fmtDate.year, gdt.fmtDate.month - 1, gdt.fmtDate.day, gdt.fmtDate.hour, gdt.fmtDate.min, gdt.fmtDate.sec);
			// UTC dates
			events.publish('gps-time', gpsDate);
		} catch (err) {
			errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "GPS Date (" + err + ")");
		}
		try {
			let gpsSat = json["Satellites in view"];
			if (gpsSat !== undefined) {
				events.publish('gps-sat', gpsSat);
			}
		} catch (err) {
			errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "GPS Satellites data (" + err + ")");
		}

		try {
			let hdg = json["HDG true"].angle;
			events.publish('hdg', hdg);
		} catch (err) {
			errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "heading");
		}
		try {
			let twd = json.TWD.angle;
			events.publish('twd', twd);
		} catch (err) {
			errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "TWD");
		}
		try {
			let twa = json.TWA.angle;
			events.publish('twa', twa);
		} catch (err) {
			errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "TWA");
		}
		try {
			let tws = json.TWS.speed;
			events.publish('tws', tws);
		} catch (err) {
			errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "TWS");
		}

		try {
			let waterTemp = json["Water Temperature"].temperature;
			events.publish('wt', waterTemp);
		} catch (err) {
			errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "water temperature");
		}

		try {
			let airTemp = json["Air Temperature"].temperature;
			events.publish('at', airTemp);
		} catch (err) {
			errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "air temperature");
		}
		// Battery_Voltage, Relative_Humidity, Barometric_Pressure
		try {
			let baro = json["Barometric Pressure"].pressure;
			if (baro != 0) {
				events.publish('prmsl', baro);
			}
		} catch (err) {
			errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "PRMSL");
		}
		try {
			let hum = json["Relative Humidity"];
			if (hum > 0) {
				events.publish('hum', hum);
			}
		} catch (err) {
			errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "Relative_Humidity");
		}
		try {
			let aws = json.AWS.speed;
			events.publish('aws', aws);
		} catch (err) {
			errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "AWS");
		}
		try {
			let awa = json.AWA.angle;
			events.publish('awa', awa);
		} catch (err) {
			errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "AWA");
		}
		try {
			let cdr = json.CDR.angle;
			events.publish('cdr', cdr);
		} catch (err) {
			errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "CDR");
		}

		try {
			let cog = json.COG.angle;
			events.publish('cog', cog);
		} catch (err) {
			errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "COG");
		}
		try {
			let cmg = json.CMG.angle;
			events.publish('cmg', cmg);
		} catch (err) {
			errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "CMG");
		}
		try {
			let leeway = json.Leeway.angle;
			events.publish('leeway', leeway);
		} catch (err) {
			errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "Leeway");
		}
		try {
			let csp = json.CSP.speed;
			events.publish('csp', csp);
		} catch (err) {
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
		} catch (err) {
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
		} catch (err) {
//		console.log(err); // Absorb?
		}

		try {
			events.publish('vmg', {
				'onwind': json["VMG on Wind"],
				'onwp': json["VMG to Waypoint"]
			});
		} catch (err) {
			errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "VMG");
		}

		if (errMess !== undefined) {
			// console.log(errMess); // Absorb
		}
	} catch (err) {
		console.log(err);
	}
}
