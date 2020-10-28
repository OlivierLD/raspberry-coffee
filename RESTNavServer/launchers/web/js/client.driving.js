"use strict";

if (Math.toDegrees === undefined) {
	Math.toDegrees = (rad) => {
		return rad * (180 / Math.PI);
	}
}

if (Math.toRadians === undefined) {
	Math.toRadians = (deg) => {
		return deg * (Math.PI / 180);
	}
}

function lpad(str, len, pad) {
	let padded = str;
	while (padded.length < len) {
		padded = (pad !== undefined ? pad : ' ') + padded;
	}
	return padded;
}

function decToSex(val, ns_ew) {
	let absVal = Math.abs(val);
	let intValue = Math.floor(absVal);
	let dec = absVal - intValue;
	dec *= 60;
	let s = "";
	if (val < 0) {
		s += (ns_ew === 'NS' ? 'S' : 'W');
	} else {
		s += (ns_ew === 'NS' ? 'N' : 'E');
	}
	s += " ";
	s += intValue + /*"°"*/ "&deg;" + lpad(dec.toFixed(2), 5, '0') + "'";
//  s = intValue + String.fromCharCode(176) + dec.toFixed(2) + "'";
	return s;
}

function onMessage(json) {
	document.getElementById("raw-json").innerText = JSON.stringify(json, null, 2);

	if (json.Position !== undefined) {
		try {
			// clear("mapCanvas");
			// drawWorldMap("mapCanvas");
		} catch (absorb) {
		}
		plotPositionOnChart({lat: json.Position.lat, lng: json.Position.lng});
	}
	try {
		document.getElementById("fixdate").innerHTML = json["GPS Date & Time"].date;
	} catch (err) {
		console.log("Err", err);
	}
	try {
		nmeaID.innerHTML = '<b>' + json.NMEA + '</b>';
	} catch (err) {
		console.log("Err", err);
	}
	if (json["Satellites in view"] !== undefined) {
		generateSatelliteData(json["Satellites in view"]);
		// Satellites on the chart
		if (json.Position !== undefined) {
			plotSatellitesOnChart({lat: json.Position.lat, lng: json.Position.lng}, json["Satellites in view"]);
		}
	}
	if (json.COG !== undefined) {
		rose.setValue(Math.round(json.COG.angle));
	}
	if (json.SOG !== undefined) {
		displayBSP.setValue(json.SOG.speed * (1.852 / 1.609)); // Apply coeff for speed. Knots to mph
	}
}

function generateSatelliteData(sd) {
	let html = "<table cellspacing='10'>";
	html += "<tr><th>PRN</th><th>Alt.</th><th>Z</th><th>snr</th></tr>";
	if (sd !== undefined) {
		// Send to plotter here.
		if (satellitesPlotter !== undefined) {
			satellitesPlotter.setSatellites(sd);
		}

		for (let sat in sd) {
			html += "<tr>" +
					"<td align='center' bgcolor='black' style='color: " + getSNRColor(sd[sat].snr) + ";'>" + sd[sat].svID +
					"</td><td align='right'>" + sd[sat].elevation +
					"&deg;</td><td align='right'>" + sd[sat].azimuth +
					"&deg;</td><td align='right'>" + sd[sat].snr + "</td></tr>";
		}
	}
	html += "</table>";
	satData.innerHTML = html;
}

function deadReckoning(from, dist, route) {
	let deltaL = Math.toRadians(dist / 60) * Math.cos(Math.toRadians(route));
	let l2 = from.lat + Math.toDegrees(deltaL);
	let deltaG = Math.toRadians(dist / (60 * Math.cos(Math.toRadians(from.lat + l2) / 2))) * Math.sin(Math.toRadians(route)); // 2009-mar-10
	let g2 = from.lng + Math.toDegrees(deltaG);
	while (g2 > 180) {
		g2 = 360 - g2;
	}
	while (g2 < -180) {
		g2 += 360;
	}
	return {lat: l2, lng: g2};
}

function plotSatellitesOnChart(pos, sd) {
	if (sd !== undefined) {
		for (let sat in sd) {
			let satellite = sd[sat];
			let satellitePosition = deadReckoning(pos, (90 - satellite.elevation) * 60, satellite.azimuth);
			//  console.log("Plotting sat " + satellite.svID + " at " + JSON.stringify(satellitePosition));
			plotSatelliteOnChart(satellitePosition, satellite.svID, getSNRColor(satellite.snr));
		}
	}
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

/*
function decToSex(val, ns_ew) {
	let absVal = Math.abs(val);
	let intValue = Math.floor(absVal);
	let dec = absVal - intValue;
	let i = intValue;
	dec *= 60;
	let s = i + String.fromCharCode(176) + dec.toFixed(2) + "'";

	if (val < 0) {
		s += (ns_ew === 'NS' ? 'S' : 'W');
	} else {
		s += (ns_ew === 'NS' ? 'N' : 'E');
	}
	return s;
}
*/

function displayMessage(mess) {
	let messList = statusFld.innerHTML;
	messList = (((messList !== undefined && messList.length) > 0 ? messList + '<br>' : '') + mess);
	statusFld.innerHTML = messList;
	statusFld.scrollTop = statusFld.scrollHeight; // Scroll down
}

function resetStatus() {
	statusFld.innerHTML = "";
}

function setConnectionStatus(ok) {
	let title = document.getElementById("title");
	if (title !== undefined) {
		title.style.color = (ok === true ? 'green' : 'red');
	}
}

let dataCacheClient = new cacheClient(onMessage, 222); // 2nd prm: between pings

function pushAltitudeData(alt) {
	if (false && altitudeData.length < (INIT_SIZE - 1)) {
		altitudeData.splice(0, 1);
		altitudeData.push(new Tuple(altitudeData.length, alt));
	} else {
		altitudeData.push(new Tuple(altitudeData.length, alt));
	}
	if (GRAPH_MAX_LEN !== undefined && altitudeData.length > GRAPH_MAX_LEN) {
		while (altitudeData.length > GRAPH_MAX_LEN) {
			altitudeData.splice(0, 1);
		}
	}
}

function getAltitudeData() {
	// No REST traffic for this one.
	let getData = getRunData();
	getData.then((value) => {
		let json = JSON.parse(value);
		let alt = json.alt.altitude;
		pushAltitudeData(alt);
	}, (error, errmess) => {
		let message;
		if (errmess !== undefined) {
			if (errmess.message !== undefined) {
				message = errmess.message;
			} else {
				message = errmess;
			}
		}
		errManager.display("Failed to get the run data..." + (error !== undefined ? error : ' - ') + ', ' + (message !== undefined ? message : ' - '));
		pushAltitudeData(0);
	});
}
