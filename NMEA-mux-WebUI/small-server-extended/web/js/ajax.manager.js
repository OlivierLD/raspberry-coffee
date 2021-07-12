/*
 * @author Olivier Le Diouris
 */
let forwardAjaxErrors = true;
let verbose = false; // For debug.

displayErr = (mess) => {
	console.log(mess);
};

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
			happyCode = [200, 201],
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
			let happy = true;
			if (typeof(happyCode) === 'number') {
				happy = (xhr.status === happyCode);
			} else {
				happy = happyCode.indexOf(xhr.status) >= 0;
			}
			if (happy) {
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
		if (verbose) {
		    console.log("Done:", value);
		}
		let json = JSON.parse(value);
		onMessage(json);
	}, (error) => {
		console.debug("Failed to get nmea data..." + (error !== undefined ? JSON.stringify(error) : ' - '));
	});
}

function onMessage(json) {
	try {
		let errMess = "";

        events.publish('raw', json);

		try {
		    if (json.Position) {
                let latitude = json.Position.lat;
    //          console.log("latitude:" + latitude)
                let longitude = json.Position.lng;
    //          console.log("Pt:" + latitude + ", " + longitude);
                events.publish('pos', {
                    'lat': latitude,
                    'lng': longitude
                });
			}
		} catch (err) {
			errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "position");
		}
		// Displays
		try {
		    if (json.BSP) {
                let bsp = json.BSP.speed;
                events.publish('bsp', bsp);
			}
		} catch (err) {
			errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "boat speed");
		}
		try {
		    if (json.Log) {
                let log = json.Log.distance;
                events.publish('log', log);
			}
		} catch (err) {
			errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "log (" + err + ")");
		}
		try {
		    if (json["GPS Date & Time"]) {
                let gpsDate = json["GPS Date & Time"].fmtDate;
                events.publish('gps-time', gpsDate);
			} else {
			    events.publish('gps-time', null);
			}
		} catch (err) {
			errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "GPS Date (" + err + ")");
		}

		try {
		    if (json["Solar Time"]) {
                let solarDate = json["Solar Time"].fmtDate;
                events.publish('solar-time', solarDate);
			} else {
			    events.publish('solar-time', null);
			}
		} catch (err) {
			errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "GPS Date (" + err + ")");
		}

		try {
		    if (json["HDG true"]) {
                let hdg = json["HDG true"].angle;
                events.publish('hdg', hdg);
			}
		} catch (err) {
			errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "true heading");
		}
		try {
		    if (json["HDG c."]) {
                let hdg = json["HDG c."].angle;
                events.publish('hdg', hdg); // TODO See declination, json["Default Declination"]
			}
		} catch (err) {
			errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "compass heading");
		}
		try {
		    if (json["HDG mag."]) {
                let hdg = json["HDG mag."].angle;
                events.publish('hdg', hdg); // TODO See declination
			}
		} catch (err) {
			errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "magnetic heading");
		}
		try {
		    if (json.TWD) {
                let twd = json.TWD.angle;
                events.publish('twd', twd);
			}
		} catch (err) {
			errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "TWD");
		}
		try {
		    if (json.TWA) {
                let twa = json.TWA.angle;
                events.publish('twa', twa);
			}
		} catch (err) {
			errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "TWA");
		}

		try {
		    if (json.TWS) {
                let tws = json.TWS.speed;
                events.publish('tws', tws);
			}
		} catch (err) {
			errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "TWS");
		}

		try {
		    if (json["Water Temperature"]) {
                let waterTemp = json["Water Temperature"].temperature;
                events.publish('wt', waterTemp);
			}
		} catch (err) {
			errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "water temperature");
		}

		try {
		    if (json["Air Temperature"]) {
                let airTemp = json["Air Temperature"].temperature;
                events.publish('at', airTemp);
			}
		} catch (err) {
			errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "air temperature");
		}
		// Battery_Voltage, Relative_Humidity, Barometric_Pressure
		try {
		    if (json["Barometric Pressure"]) {
                let baro = json["Barometric Pressure"].pressure;
                if (baro != 0) {
                    events.publish('prmsl', baro);
                }
			}
		} catch (err) {
			errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "PRMSL");
		}
		try {
			let hum = json["Relative Humidity"];
			if (hum && hum > 0) {
				events.publish('hum', hum);
			}
		} catch (err) {
			errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "Relative_Humidity");
		}

		try {
		    let satData = json["Satellites in view"];
		    if (satData) {
		        events.publish('gps-sat', satData);
		    }
		} catch (err) {
			errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "Satellites in view");
		}

		try {
		    if (json.AWS) {
                let aws = json.AWS.speed;
                events.publish('aws', aws);
			}
		} catch (err) {
			errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "AWS");
		}
		try {
		    if (json.AWA) {
                let awa = json.AWA.angle;
                events.publish('awa', awa);
			}
		} catch (err) {
			errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "AWA");
		}
		try {
		    if (json.CDR) {
                let cdr = json.CDR.angle;
                events.publish('cdr', cdr);
			}
		} catch (err) {
			errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "CDR");
		}

		try {
		    if (json.COG) {
                let cog = json.COG.angle;
                events.publish('cog', cog);
			}
		} catch (err) {
			errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "COG");
		}
		try {
		    if (json.CMG) {
                let cmg = json.CMG.angle;
                events.publish('cmg', cmg);
			}
		} catch (err) {
			errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "CMG");
		}
		try {
		    if (json.Leeway) {
                let leeway = json.Leeway.angle;
                events.publish('leeway', leeway);
			}
		} catch (err) {
			errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "Leeway");
		}
		try {
		    if (json.CSP) {
                let csp = json.CSP.speed;
                events.publish('csp', csp);
			}
		} catch (err) {
			errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "CSP");
		}
		try {
		    if (json.SOG) {
                let sog = json.SOG.speed;
                events.publish('sog', sog);
			}
		} catch (err) {
			errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "SOG");
		}
		// to-wp, vmg-wind, vmg-wp, b2wp
		try {
		    if (json["To Waypoint"] && json["Bearing to WP"]) {
                let to_wp = json["To Waypoint"];
                let b2wp = json["Bearing to WP"].angle;
                events.publish('wp', {
                    'to_wp': to_wp,
                    'b2wp': b2wp
                });
			}
		} catch (err) {
		}

		try {
		    if (json["VMG on Wind"] || json["VMG to Waypoint"]) {
                events.publish('vmg', {
                    'onwind': json["VMG on Wind"],
                    'onwp': json["VMG to Waypoint"]
                });
			}
		} catch (err) {
			errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "VMG");
		}

		try {
			let prate = json.prate;
			if (prate) {
    			events.publish('prate', prate);
			}
		} catch (err) {
			errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "prate");
		}
		try {
		    if (json.dewpoint) {
                let dew = json.dewpoint;
                events.publish('dew', dew);
			}
		} catch (err) {
			errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "dew");
		}

		if (errMess !== undefined && errMess.trim().length > 0 && forwardAjaxErrors) {
			displayErr(errMess);
		}
	} catch (err) {
		displayErr(err);
	}
}
