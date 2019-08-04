/*
 * @author Olivier Le Diouris
 * Uses jQuery Deferreds
 */
var initAjax = function () {
	var interval = setInterval(function () {
		fetch();
	}, 1000);
};

var getNMEAData = function () {
	var deferred = $.Deferred(),  // a jQuery deferred
			url = '/mux/cache',
			xhr = new XMLHttpRequest(),
			TIMEOUT = 10000;

	xhr.open('GET', url, true);
	xhr.setRequestHeader("Content-type", "application/json");
	xhr.send();

	var requestTimer = setTimeout(function () {
		xhr.abort();
		deferred.reject(408, {message: 'Timeout'});
	}, TIMEOUT);

	xhr.onload = function () {
		clearTimeout(requestTimer);
		if (xhr.status === 200) {
			deferred.resolve(xhr.response);
		} else {
			deferred.reject(xhr.status, xhr.response);
		}
	};
	return deferred.promise();
};

var fetch = function () {
	var getData = getNMEAData();
	getData.done(function (value) {
//      console.log("Done:", value);
		var json = JSON.parse(value);
		onMessage(json);
	});
	getData.fail(function (error, errmess) {
		var message;
		if (errmess !== undefined) {
			var mess = JSON.parse(errmess);
			if (mess.message !== undefined) {
				message = mess.message;
			}
		}
		alert("Failed to get NMEA data..." + (error !== undefined ? error : ' - ') + ', ' + (message !== undefined ? message : ' - '));
	});
};

var onMessage = function (json) {
	try {
		var errMess = "";

		try {
			var latitude = json.Position.lat;
//          console.log("latitude:" + latitude)
			var longitude = json.Position.lng;
//          console.log("Pt:" + latitude + ", " + longitude);
			events.publish('pos', {
				'lat': latitude,
				'lng': longitude
			});
		} catch (err) {
			errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "position");
		}
		// Displays
		try {
			var bsp = json.BSP.speed;
			events.publish('bsp', bsp);
		} catch (err) {
			errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "boat speed");
		}
		try {
			var log = json.Log.distance;
			events.publish('log', log);
		} catch (err) {
			errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "log (" + err + ")");
		}
		try {
			var gdt = json["GPS Date & Time"];
			var gpsDate = new Date(gdt.fmtDate.year, gdt.fmtDate.month - 1, gdt.fmtDate.day, gdt.fmtDate.hour, gdt.fmtDate.min, gdt.fmtDate.sec, 0);
			// UTC dates
			events.publish('gps-time', gpsDate);
		} catch (err) {
			errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "GPS Date (" + err + ")");
		}

		try {
			var hdg = json["HDG true"].angle;
			events.publish('hdg', hdg);
		}
		catch (err) {
			errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "heading");
		}
		try {
			var twd = json.TWD.angle;
			events.publish('twd', twd);
		}
		catch (err) {
			errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "TWD");
		}
		try {
			var twa = json.TWA.angle;
			events.publish('twa', twa);
		}
		catch (err) {
			errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "TWA");
		}
		try {
			var tws = json.TWS.speed;
			events.publish('tws', tws);
		}
		catch (err) {
			errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "TWS");
		}

		try {
			var waterTemp = json["Water Temperature"].temperature;
			events.publish('wt', waterTemp);
		}
		catch (err) {
			errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "water temperature");
		}

		try {
			var airTemp = json["Air Temperature"].temperature;
			events.publish('at', airTemp);
		}
		catch (err) {
			errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "air temperature");
		}
		// Battery_Voltage, Relative_Humidity, Barometric_Pressure
		try {
			var baro = json["Barometric Pressure"].pressure;
			if (baro != 0) {
				events.publish('prmsl', baro);
			}
		}
		catch (err) {
			errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "PRMSL");
		}
		try {
			var hum = json["Relative Humidity"];
			if (hum > 0) {
				events.publish('hum', hum);
			}
		}
		catch (err) {
			errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "Relative_Humidity");
		}
		try {
			var aws = json.AWS.speed;
			events.publish('aws', aws);
		}
		catch (err) {
			errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "AWS");
		}
		try {
			var awa = json.AWA.angle;
			events.publish('awa', awa);
		}
		catch (err) {
			errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "AWA");
		}
		try {
			var cdr = json.CDR.angle;
			events.publish('cdr', cdr);
		}
		catch (err) {
			errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "CDR");
		}

		try {
			var cog = json.COG.angle;
			events.publish('cog', cog);
		}
		catch (err) {
			errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "COG");
		}
		try {
			var cmg = json.CMG.angle;
			events.publish('cmg', cmg);
		}
		catch (err) {
			errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "CMG");
		}
		try {
			var leeway = json.Leeway.angle;
			events.publish('leeway', leeway);
		}
		catch (err) {
			errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "Leeway");
		}
		try {
			var csp = json.CSP.speed;
			events.publish('csp', csp);
		}
		catch (err) {
			errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "CSP");
		}

		// Buffered current
		try {
			var buffered = json['Current calculated with damping'];
			if (buffered !== undefined) {
				var keys = Object.keys(buffered);
				for (var i=0; i<keys.length; i++) {
					var k = keys[i];
//				console.log("K:" + k);
					var damp = buffered[k];
//				console.log("Publishing csp-" + k);
					events.publish("csp-" + k, damp.speed.speed);
					events.publish("cdr-" + k, damp.direction.angle);
				}
			}
		} catch (err) {
			console.log(err);
		}

		try {
			var sog = json.SOG.speed;
			events.publish('sog', sog);
		}
		catch (err) {
			errMess += ((errMess.length > 0 ? ", " : "Cannot read ") + "SOG");
		}
		// to-wp, vmg-wind, vmg-wp, b2wp
		try {
			var to_wp = json["To Waypoint"];
			var b2wp = json["Bearing to WP"].angle;
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
};
