
const TABS = ['one', 'two', 'three', 'four', 'five'];

function openTab(evt, tabNum) {
	let tabLinks = document.getElementsByClassName("tablinks");
	for (let i=0; i<tabLinks.length; i++) {
		tabLinks[i].className = tabLinks[i].className.replace(" active", ""); // Reset
	}
	for (let i=0; i<TABS.length; i++) {
		document.getElementById(TABS[i]).style.display = (i === tabNum) ? 'block' : 'none';
	}
	evt.currentTarget.className += " active";
}

function getQSPrm(prm) {
	let value;
	let loc = document.location.toString();
	if (loc.indexOf("?") > -1) {
		let qs = loc.substring(loc.indexOf("?") + 1);
		let prms = qs.split('&');
		for (let i=0; i<prms.length; i++) {
			let nv = prms[i].split('=');
			if (nv.length === 2) {
				if (nv[0] === prm) {
					return nv[1];
				}
			}
		}
	}
	return value;
}

function changeBG(value) {
	let bodyStyle = document.getElementsByTagName("body")[0].style;
	let htmlStyle = document.getElementsByTagName("html")[0].style;
	switch (value) {
		case 'WHITE':
			bodyStyle.background = 'white';
			htmlStyle.backgroundColor = 'white';
			break;
		case 'LIGHT':
			bodyStyle.background = 'radial-gradient(at top, white -8%, lightgrey 55%)';
			htmlStyle.backgroundColor = 'lightgrey';
			break;
		case 'DARK':
			bodyStyle.background = 'radial-gradient(at top, DarkGrey -8%, black 55%)';
			htmlStyle.backgroundColor = 'black';
			break;
		case 'BLACK':
			bodyStyle.background = 'black';
			htmlStyle.backgroundColor = 'black';
			break;
		default:
			break;
	}
	// World Map theme worldmap-display-02, worldmap-display, split-flap-display
	switch (value) {
		case "BLACK":
		case "DARK":
			applyClass('world-map-01', 'worldmap-display');
			applyClass('split-flap-display-00', 'split-flap-night');
			applyClass('split-flap-display-01', 'split-flap-night');
			applyClass('split-flap-display-02', 'split-flap-night');
			applyClass('split-flap-display-03', 'split-flap-night');
			break;
		case "LIGHT":
		case "WHITE":
			applyClass('world-map-01', 'worldmap-display-02');
			applyClass('split-flap-display-00', 'split-flap-day');
			applyClass('split-flap-display-01', 'split-flap-day');
			applyClass('split-flap-display-02', 'split-flap-day');
			applyClass('split-flap-display-03', 'split-flap-day');
			break;
	}
}

let headsUpExpanded = false;
function collapseExpandHeadsup() {
	let cmdElem = document.getElementById("head-up-command");
	let slidersElem = document.getElementById("heads-up-sliders");
	if (headsUpExpanded) {
		slidersElem.style.display = 'none';
		cmdElem.innerText = "+ Head up";
	} else {
		slidersElem.style.display = 'block';
		cmdElem.innerText = "- Head up";
	}
	headsUpExpanded = !headsUpExpanded;
}

let boatDataExpanded = true;
function expandCollapseBoatData() {
	boatDataExpanded = !boatDataExpanded;
	if (boatDataExpanded) {

		document.getElementById('boat-data').style.height = 'auto';
		document.getElementById('boat-data').style.opacity = '1';
		document.getElementById('boat-data').style.visibility = 'visible';

		// document.getElementById('row-1').style.display = 'grid';
		// document.getElementById('row-2').style.display = 'grid';
		document.getElementById('boat-data-switch').innerText = ' - Boat Data';
	} else {
		document.getElementById('boat-data').style.height = '0';
		document.getElementById('boat-data').style.opacity = '0';
		document.getElementById('boat-data').style.visibility = 'hidden';

		// document.getElementById('row-1').style.display = 'none';
		// document.getElementById('row-2').style.display = 'none';
		document.getElementById('boat-data-switch').innerText = ' + Boat Data';
	}
}
/**
 * Set data to the WebComponents
 * Assume that they all have a 'value' member.
 *
 * @param from The field containing the value to set
 * @param to The WebComponent to set the value to
 */
function setData(id, value) {
	let elem = document.getElementById(id);
	elem.value = value;                            // value corresponds to the 'set value(val) { ...', invokes the setter in the HTMLElement class
	elem.repaint();
}

let deviationCurve = null;

function devFromHdg(hdg) {
	let dev = 0;
	if (deviationCurve != null) {
		let prevIndex = 0;
		let deltaX = 10;
		for (let i=0; i<deviationCurve.length; i++) {
			if (deviationCurve[i][0] > hdg) {
				prevIndex = i - 1;
				deltaX = deviationCurve[prevIndex + 1][0] - deviationCurve[prevIndex][0];
				break;
			}
		}
		dev = deviationCurve[prevIndex][1];
		if ((prevIndex * 10) !== hdg) {
			let deltaY = deviationCurve[prevIndex + 1][1] - deviationCurve[prevIndex][1];
			let diff = (deltaY * ((hdg - (prevIndex * deltaX)) / deltaX));
			dev += diff;
		}
	}
	return dev;
}

function setHdgOnDevCurve(id, hdg) { // Triggered when HDG is updated (event.subscribe)
	let elem = document.getElementById(id);
	if (elem !== null && elem !== undefined && deviationCurve !== null) {
		let value = devFromHdg(hdg); // Find dev value with hdg in dev curve
		elem.value = (value < 0 ? "W " : "E ") + Math.abs(value).toFixed(1) + "°";
		elem.repaint();
	}
}

let storedHistory = [];

function setRawNMEA(sentence) {
	storedHistory.push(sentence);
	while (storedHistory.length > 50) {
		storedHistory.shift();
	}

	let content = '<pre>';
	storedHistory.forEach(str => {
		content += (str + '\n');
	});
	content += '</pre>';

	let nmea = document.getElementById('nmea-content');
	nmea.innerHTML = content;
	nmea.scrollTop = nmea.scrollHeight; // See last line

	// $("#raw-data").html(content);
	// $("#raw-data").scrollTop($("#raw-data")[0].scrollHeight);

}

function setBorder(cb, id) {
	document.getElementById(id).withBorder = cb.checked;
}

function setRose(cb, id) {
	document.getElementById(id).withRose = cb.checked;
}

function setMinMax(cb, id) {
	document.getElementById(id).withMinMax = cb.checked;
}

function setTransparency(wcId, cb) {
	document.getElementById(wcId).transparentGlobe = (cb.checked ? 'true' : 'false');
	document.getElementById(wcId).repaint();
}

function setSun(wcId, cb) {
	document.getElementById(wcId).withSun = (cb.checked ? 'true' : 'false');
	document.getElementById(wcId).repaint();
}

function setGrid(wcId, cb) {
	document.getElementById(wcId).withGrid = (cb.checked ? 'true' : 'false');
	document.getElementById(wcId).repaint();
}

function setMoon(wcId, cb) {
	document.getElementById(wcId).withMoon = (cb.checked ? 'true' : 'false');
	document.getElementById(wcId).repaint();
}

function setSunlight(wcId, cb) {
	document.getElementById(wcId).withSunlight = (cb.checked ? 'true' : 'false');
	document.getElementById(wcId).repaint();
}

function setMoonlight(wcId, cb) {
	document.getElementById(wcId).withMoonlight = (cb.checked ? 'true' : 'false');
	document.getElementById(wcId).repaint();
}

function setWanderingBodies(wcId, cb) {
	document.getElementById(wcId).withWanderingBodies = (cb.checked ? 'true' : 'false');
	document.getElementById(wcId).repaint();
}

function setStars(wcId, cb) {
	document.getElementById(wcId).withStars = (cb.checked ? 'true' : 'false');
	document.getElementById(wcId).repaint();
}

function setTropics(wcId, cb) {
	document.getElementById(wcId).withTropics = (cb.checked ? 'true' : 'false');
	document.getElementById(wcId).repaint();
}

function setProjection(id, radio) {
	document.getElementById(id).projection = radio.value;
	document.getElementById(id).repaint();
}

function setMapType(id, list) {
	document.getElementById(id).type = list.value;
	document.getElementById(id).repaint();
}

function setStarNames(id, cb) {
	document.getElementById(id).starNames = cb.checked;
	document.getElementById(id).repaint();
}

function setConstNames(id, cb) {
	document.getElementById(id).constellationNames = cb.checked;
	document.getElementById(id).repaint();
}

function setVisibleSky(id, cb) {
	document.getElementById(id).visibleSky = cb.checked;
	document.getElementById(id).repaint();
}

function setSkyGrid(id, cb) {
	document.getElementById(id).skyGrid = cb.checked;
	document.getElementById(id).repaint();
}

// Depends on the user position... Would not turn with the globe.
let gpsSatelliteData = undefined;

function plotSatellite(context, worldMap, userPos, satColor, name, satellite) {
	let sat = worldMap.getPanelPoint(satellite.lat, satellite.lng);
	let thisPointIsBehind = worldMap.isBehind(worldMap.toRadians(satellite.lat), worldMap.toRadians(satellite.lng - worldMap.globeViewLngOffset));
	if (!thisPointIsBehind || worldMap.transparentGlobe) {
		// Draw Satellite
		worldMap.plot(context, sat, satColor);
		context.fillStyle = satColor;
		context.fillText(name, Math.round(sat.x) + 3, Math.round(sat.y) - 3);
		// Arrow, to the satellite
		context.setLineDash([2]);
		context.strokeStyle = satColor;
		context.beginPath();
		context.moveTo(userPos.x, userPos.y);
		context.lineTo(sat.x, sat.y);
		context.stroke();
		context.closePath();
		context.setLineDash([0]); // Reset
		context.strokeStyle = satColor;
		let deltaX = sat.x - userPos.x;
		let deltaY = sat.y - userPos.y;
		context.beginPath();
		context.moveTo(sat.x, sat.y);
		context.lineTo(sat.x + deltaX, sat.y + deltaY);
		context.stroke();
		context.closePath();
		worldMap.fillCircle(context, {x: sat.x + deltaX, y: sat.y + deltaY}, 6, satColor);
	}
}

function plotSunMoonRoute(context, worldMap, routeData) {
	let lastPointToPlot = null;
	routeData.forEach(routePt => {
		let panelPoint = worldMap.getPanelPoint(routePt.p.latitude, routePt.p.longitude);
		let thisPointIsBehind = worldMap.isBehind(worldMap.toRadians(routePt.p.latitude), worldMap.toRadians(routePt.p.longitude - worldMap.globeViewLngOffset));
		if (!thisPointIsBehind || worldMap.transparentGlobe) {
			// Draw segment
			if (lastPointToPlot !== null) {
//					console.log("Plotting from ", lastPointToPlot, " to ", panelPoint);
				context.strokeStyle = 'lime';
				context.beginPath();
				context.moveTo(lastPointToPlot.x, lastPointToPlot.y);
				context.lineTo(panelPoint.x, panelPoint.y);
				context.stroke();
				context.closePath();
			}
			lastPointToPlot = panelPoint;
		} else {
			lastPointToPlot = null;
		}
	});
}

// More colors at https://www.w3schools.com/colors/colors_picker.asp
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
			c = 'lime';
		}
	}
	return c;
}

// AFTER callback on WorldMap
function callAfter(id) {
	document.getElementById(id).setDoAfter((worldMap, context) => {
		if (Object.keys(worldMap.userPosition).length > 0) {
			let userPos = worldMap.getPanelPoint(worldMap.userPosition.latitude, worldMap.userPosition.longitude);
			/*
			 * Display 4 geostationary satellites. Data provided below.
			 */
			const sats = [
				{name: "I-4 F1 Asia-Pacific", lng: 143.5},
				{name: "I-4 F2 EMEA", lng: 63.0},
				{name: "I-4 F3 Americas", lng: -97.6},
				{name: "Alphasat", lng: 24.9}];
			if (document.getElementById('geo-sat-01').checked) {
				let satColor = 'cyan';
				sats.forEach(gs => {
					plotSatellite(context, worldMap, userPos, satColor, gs.name, {lat: 0, lng: gs.lng});
				});
			}

			// GPS Satellites in view
			if (document.getElementById('gps-sat-01').checked && gpsSatelliteData !== undefined) {
				for (let sat in gpsSatelliteData) {
					let satellite = gpsSatelliteData[sat];
					let satellitePosition = worldMap.deadReckoningRadians({
						lat: worldMap.toRadians(worldMap.userPosition.latitude),
						lng: worldMap.toRadians(worldMap.userPosition.longitude)
					}, (90 - satellite.elevation) * 60, satellite.azimuth);
					plotSatellite(context, worldMap, userPos, getSNRColor(satellite.snr), sat, {
						lat: worldMap.toDegrees(satellitePosition.lat),
						lng: worldMap.toDegrees(satellitePosition.lng)
					});
				}
			}
			// Moon to Sun route
			if (document.getElementById('moon-sun-path-01').checked) {
				if (moonSunData !== undefined && moonSunData.length > 0) {
					// console.log("Plotting moon to sun route", moonSunData);
					plotSunMoonRoute(context, worldMap, moonSunData);
				}
			}
		}
	});
	document.getElementById(id).repaint();
}

// BEFORE callback on WorldMap
function callFirst(id) {
	document.getElementById(id).setDoFirst((worldMap, context) => {
//	console.log("Sun elevation:", sunAltitude);
		if (sunAltitude > -5 && sunAltitude < 5) { // Then change bg color
			let gradientRadius = 120 + ((5 - Math.abs(sunAltitude)) / 5) * ((Math.min(worldMap.height, worldMap.width) / 2) - 120);
//		console.log("Gradient Radius:", gradientRadius);
			let grd = context.createRadialGradient(worldMap.width / 2, worldMap.height / 2, 0.000, worldMap.width / 2, worldMap.height / 2, gradientRadius);
			// Add colors
			grd.addColorStop(0.000, 'rgba(34, 10, 10, 1.000)');
			grd.addColorStop(0.330, 'rgba(34, 10, 10, 1.000)');
			grd.addColorStop(0.340, 'rgba(255, 255, 255, 1.000)');
			grd.addColorStop(0.600, 'rgba(234, 189, 12, 1.000)');
			grd.addColorStop(1.000, 'rgba(0, 0, 0, 1.000)'); // 'rgba(35, 1, 4, 1.000)');
			worldMap.worldmapColorConfig.globeBackground = grd; // instead of black
		} else {
			worldMap.worldmapColorConfig.globeBackground = 'black';
		}
	});
	document.getElementById(id).repaint();
}

const DURATION_FMT = "Y-m-dTH:i:s";
const months = [ "Jan", "Feb", "Mar", "Apr", "May", "Jun",
	"Jul", "Aug", "Sep", "Oct", "Nov", "Dec" ];

// Moon symbol, was "\u263D"
const BODIES = [{
		name: "sun",
		display: {
			name: "Sun",
			symbol: "\u2609"
		}
	},{
		name: "moon",
		display: {
			name: "Moon",
			symbol: "\u263E"
		}
	},{
		name: "venus",
		display: {
			name: "Venus",
			symbol: "\u2640"
		}
	},
	{
		name: "mars",
		display: {
			name: "Mars",
			symbol: "\u2642"
		}
	}, {
		name: "jupiter",
		display: {
			name: "Jupiter",
			symbol: "\u2643"
		}
	}, {
		name: "saturn",
		display: {
			name: "Saturn",
			symbol: "\u2644"
		}
	}];

function bodyName(name) {
	for (let i=0; i<BODIES.length; i++) {
		if (name === BODIES[i].name) {
			return BODIES[i].display.name + ' ' + BODIES[i].display.symbol;
		}
	}
	return name;
}

function getLHA(gha, longitude) {
	let lha = gha + longitude;
	while (lha < 0) lha +=360;
	while (lha > 360) lha -= 360;
	return lha;
}

let sunAltitude = -90;
let moonSunData = [];

function astroCallback(data) {
//console.log("Astro Data:", data);

	// if (data.moonPhase !== undefined) {
	// 	console.log("Moon Phase:", data.moonPhase);
	// }

	let worldMap = document.getElementById('world-map-01');
	let skyMap = document.getElementById('sky-map-01');

	let lhaAries = getLHA(data.ghaAries, data.from.longitude);

	let sunLHA = getLHA(data.sun.gha, data.from.longitude);
	let moonLHA = getLHA(data.moon.gha, data.from.longitude);

	if (data.moonPhase !== undefined) { // Update MoonPhase WebComp
		// console.log("Moon Phase:", data.moonPhase);
		let moonPhase = document.getElementById('moon-phase-01');
		moonPhase.phase = data.moonPhase;
		if (data.moon.decl !== undefined && data.from.latitude !== undefined) {
			// Tilt calculation,
			moonPhase.phase = ((data.from.latitude < data.moon.decl) ? -1 : 1) * data.moonPhase;
			let alpha = 0;
			moonSunData = data.moonToSunSkyRoute;
			if (moonSunData !== undefined) {
				try {
					// alpha = moonSunData[0].z; // z=90: horizontal, toward right, alpha=0
					// alpha -= 90;
					// Take the first triangle, from the Moon.
					let deltaZ = moonSunData[1].wpFromPos.observed.z - moonSunData[0].wpFromPos.observed.z;
					let deltaElev = moonSunData[1].wpFromPos.observed.alt - moonSunData[0].wpFromPos.observed.alt;
					alpha = Math.toDegrees(Math.atan2(deltaZ, deltaElev));
					alpha += 90;
				} catch(error) {
					console.debug(error);
				}
			}
			let moonTilt = /*90 +*/ alpha; // 0: vertical. +: clockwise, -: counter-clockwise
			moonPhase.tilt = moonTilt;                                 // Update tilt on graphic
			moonPhase.title = `Tilt:${alpha.toFixed(1)}°`; // Update tooltip
			// Update small label
			moonPhase.smallLabel = `Tilt:${alpha.toFixed(1)}°`;
		} else {
			moonPhase.phase = data.moonPhase;
		}
		//moonPhase.repaint();
	}

	sunAltitude = data.sunObs.alt; // For the doBefore method

	let moonPos = {};
	let venusPos = {};
	let marsPos = {};
	let jupiterPos = {};
	let saturnPos = {};
	try {
		moonPos = {
			he: data.moonObs.alt,
			z: data.moonObs.z,
			phase: data.moonPhase
		};
	} catch (err) {
		// missing
	}
	try { // Notice the find method on the array (wanderingBodies)
		let venus = data.wanderingBodies.find(wb => { return wb.name === 'venus' }).fromPos.observed;
		venusPos = {
			he: venus.alt,
			z: venus.z
		};
	} catch (err) {
		// nope
	}
	try {
		let mars = data.wanderingBodies.find(wb => { return wb.name === 'mars' }).fromPos.observed;
		marsPos = {
			he: mars.alt,
			z: mars.z
		};
	} catch (err) {
		// nope
	}
	try {
		let jupiter = data.wanderingBodies.find(wb => { return wb.name === 'jupiter' }).fromPos.observed;
		jupiterPos = {
			he: jupiter.alt,
			z: jupiter.z
		};
	} catch (err) {
		// nope
	}
	try {
		let saturn = data.wanderingBodies.find(wb => { return wb.name === 'saturn' }).fromPos.observed;
		saturnPos = {
			he: saturn.alt,
			z: saturn.z
		};
	} catch (err) {
		// nope
	}

	/*
	TODO Other wandering bodies:
	data.wanderingBodies["name" = "venus"].fromPos.observed.(alt, z)
	data.wanderingBodies.find(wb => {
    return wb.name === 'venus'
  }).fromPos.observed.z

	 */

	let dataTable =
			'<table border="1" class="raw-table">' + '<tr><th>Body</th><th>D</th><th>GHA</th><th>LHA</th><th>Elev</th><th>Z</th></tr>' +
			'<tr><td align="left">' + bodyName("sun") + '</td><td>' + worldMap.decToSex(data.sun.decl, "NS") + '</td><td align="right">' + worldMap.decToSex(data.sun.gha) + '</td><td align="right">' + worldMap.decToSex(sunLHA) + '</td><td align="right">' +	worldMap.decToSex(data.sunObs.alt) + '</td><td align="right">' + worldMap.decToSex(data.sunObs.z) + '</td></tr>' +
			'<tr><td align="left">' + bodyName("moon") + '</td><td>' + worldMap.decToSex(data.moon.decl, "NS") + '</td><td align="right">' + worldMap.decToSex(data.moon.gha) + '</td><td align="right">' + worldMap.decToSex(moonLHA) + '</td><td align="right">' +	worldMap.decToSex(data.moonObs.alt) + '</td><td align="right">' + worldMap.decToSex(data.moonObs.z) + '</td></tr>';

	// Bonus: Update Sun Decl on the graph
	try {
		document.getElementById('decl-graph-01').value = worldMap.decToSex(data.sun.decl, "NS");
		document.getElementById('decl-graph-02').value = worldMap.decToSex(data.moon.decl, "NS");
	} catch (err) {
		// Absorb
	}

	if (data.wanderingBodies !== undefined) {
		for (let i=0; i<data.wanderingBodies.length; i++) {
			if (data.wanderingBodies[i].name !== "aries") {
				dataTable +=
				'<tr><td align="left">' + bodyName(data.wanderingBodies[i].name) + '</td><td align="right">' + worldMap.decToSex(data.wanderingBodies[i].decl, "NS") + '</td><td align="right">' + worldMap.decToSex(data.wanderingBodies[i].gha) + '</td><td>' + worldMap.decToSex(getLHA(data.wanderingBodies[i].gha, data.from.longitude)) + '</td><td align="right">' + worldMap.decToSex(data.wanderingBodies[i].fromPos.observed.alt) + '</td><td align="right">' + worldMap.decToSex(data.wanderingBodies[i].fromPos.observed.z) + '</td></tr>';
			}
		}
	}

	dataTable +=
			'<tr><td align="left">Aries &gamma;</td><td></td><td align="right">' + worldMap.decToSex(data.ghaAries) + '</td><td align="right">' + worldMap.decToSex(lhaAries) + '</td><td align="right">' + worldMap.decToSex(data.ariesObs.alt) + '</td><td align="right">' + worldMap.decToSex(data.ariesObs.z) + '</td></tr></table>';

	document.getElementById("sun-moon-data").innerHTML = dataTable;

	// Display solar date & time
	let solarDate = new Date(data.solarDate.year, data.solarDate.month - 1, data.solarDate.day, data.solarDate.hour, data.solarDate.min, data.solarDate.sec);
	let time = solarDate.format("H:i:s");
	setData('analog-watch-02', time);
	let date = solarDate.format("d-m-Y-l");
	setData('calendar-02', date);

	// Solar Data for the Solar path
	setData('split-flap-solar-display-00', time);
	setData('calendar-03', date);

	let utcDate = new Date(data.epoch);

	let sysDateFmt = utcDate.format('D d-M-Y H:i:s Z');
//console.log("System date %s", sysDateFmt);

	document.getElementById("split-flap-display-01")
			.value = sysDateFmt;

	// System date in sun path tab
	let systemTime = utcDate.format('H:i:s');
	setData('split-flap-system-display-00', systemTime);
	let timeOffset = utcDate.format('Z');
	setData('split-flap-system-display-01', timeOffset);
	//
	let systemDate = solarDate.format("d-m-Y-l");
	setData('calendar-04', systemDate);

	// utc-date Raw Data tab
	document.getElementById("utc-date").innerHTML = 'UTC: ' +
			utcDate.getUTCFullYear() + ' ' +
			months[utcDate.getUTCMonth()] + ' ' +
			(utcDate.getUTCDate() < 10 ? '0' : '') + utcDate.getUTCDate() + ' ' +
			(utcDate.getUTCHours() < 10 ? '0' : '') + utcDate.getUTCHours() + ':' +
			(utcDate.getUTCMinutes() < 10 ? '0' : '') + utcDate.getUTCMinutes() + ':' +
			(utcDate.getUTCSeconds() < 10 ? '0' : '') + utcDate.getUTCSeconds();

	// Solar Time Raw Data tab
	document.getElementById("solar-date").innerHTML = 'Solar Time: ' +
			data.solarDate.year + ' ' +
			months[data.solarDate.month - 1] + ' ' +
			(data.solarDate.day < 10 ? '0' : '') + data.solarDate.day + ' ' +
			(data.solarDate.hour < 10 ? '0' : '') + data.solarDate.hour + ':' +
			(data.solarDate.min < 10 ? '0' : '') + data.solarDate.min + ':' +
			(data.solarDate.sec < 10 ? '0' : '') + data.solarDate.sec;

	// Display transit time Raw Data tab
	document.getElementById("sun-transit").innerHTML = 'Sun Transit: ' +
			(data.tPass.hour < 10 ? '0' : '') + data.tPass.hour + ':' +
			(data.tPass.min < 10 ? '0' : '') + data.tPass.min + ':' +
			(data.tPass.sec < 10 ? '0' : '') + data.tPass.sec + ' ' +
			data.tPass.tz;

	// Extra data Raw Tab
	if (data.moonPhase !== undefined) {
		document.getElementById("moon-phase-rd").innerHTML = 'Moon Phase: ' + data.moonPhase + "°";
		if (data.moon.decl !== undefined && data.from.latitude !== undefined) {
			let alpha = 0; // Tilt from horizontal
			if (data.moonToSunSkyRoute !== undefined) {
				try {
					alpha = data.moonToSunSkyRoute[0].z; // z=90: horizontal, toward right, alpha=0. z=0, facing up, alpha=-90.
					// console.log(`Moon to Sun sky route: ${alpha.toFixed(4)}°`);
					alpha -= 90;
				} catch(error) {
					console.debug(error);
				}
			}
			let moonTilt = alpha;
			//document.getElementById("moon-tilt-rd").innerHTML = `Moon Tilt: ${Math.abs(moonTilt)}°, ${moonTilt>=0?"Right ":"Left "}`;
			document.getElementById("moon-tilt-rd").innerHTML = `Moon Tilt: ${moonTilt}°`;
		}
	}

	// tPass has only hh:mi:ss
	let tPass = new Date();
	tPass.setUTCHours(data.tPass.hour, data.tPass.min, data.tPass.sec);
	document.getElementById('sun-path-01').sunTransit = { time: tPass.getTime() };
	// console.log("Transit:", tPass);
	let now = new Date();
	document.getElementById('sun-path-01').now = { time: now.getTime() };
	if (moonPos !== {}) {
		document.getElementById('sun-path-01').moonPos = moonPos;
	}
	let planetDataAvailable = venusPos.he !== undefined && marsPos.he !== undefined && jupiterPos.he !== undefined && saturnPos.he !== undefined;
	document.getElementById('with-wb').disabled = !planetDataAvailable;
	let withWanderingBodies = planetDataAvailable && document.getElementById('with-wb').checked;
	document.getElementById('ecliptic').disabled = !withWanderingBodies;
	let withEcliptic = withWanderingBodies && document.getElementById('ecliptic').checked;
	let withMoonToSunRoute = document.getElementById('moon-sun-route').checked;
	if (!withEcliptic) {
		document.getElementById('sun-path-01').ariesGHA = undefined;
		document.getElementById('sun-path-01').eclipticObliquity = undefined;
	}
	if (withEcliptic && data.ghaAries !== undefined) {
		document.getElementById('sun-path-01').ariesGHA = data.ghaAries;
	}
	if (withEcliptic && data.eclipticObliquity !== undefined) {
		document.getElementById('sun-path-01').eclipticObliquity = data.eclipticObliquity;
	}

	document.getElementById('sun-path-01').moonToSunSkyRoute = withMoonToSunRoute ? data.moonToSunSkyRoute : undefined;

	if (venusPos !== {} && withWanderingBodies) {
		document.getElementById('sun-path-01').venusPos = venusPos;
	} else {
		document.getElementById('sun-path-01').venusPos = undefined;
	}
	if (marsPos !== {} && withWanderingBodies) {
		document.getElementById('sun-path-01').marsPos = marsPos;
	} else {
		document.getElementById('sun-path-01').marsPos = undefined;
	}
	if (jupiterPos !== {} && withWanderingBodies) {
		document.getElementById('sun-path-01').jupiterPos = jupiterPos;
	} else {
		document.getElementById('sun-path-01').jupiterPos = undefined;
	}
	if (saturnPos !== {} && withWanderingBodies) {
		document.getElementById('sun-path-01').saturnPos = saturnPos;
	} else {
		document.getElementById('sun-path-01').saturnPos = undefined;
	}
	worldMap.setAstronomicalData(data);
	worldMap.repaint();

	if (data.wanderingBodies !== undefined) {
		let wb = data.wanderingBodies;
		wb.push({ name: "sun", decl: data.sun.decl, gha: data.sun.gha});
		wb.push({ name: "moon", decl: data.moon.decl, gha: data.moon.gha});
		skyMap.wanderingBodies = true;
		skyMap.wanderingBodiesData = wb;
	} else {
		skyMap.withWanderingBodies = false;
	}

	skyMap.hemisphere = (data.from.latitude > 0 ? 'N' : 'S');
	skyMap.lhaAries = lhaAries;
	skyMap.latitude = Math.abs(data.from.latitude);
	skyMap.repaint();
}

function setTheme(className) {
	applyClass('compass-01', className);
	applyClass('tw-01', className);
	applyClass('bsp-01', className);
	applyClass('aw-01', className);
	applyClass('compass-rose-01', className);
	applyClass('analog-watch-01', className);
	applyClass('analog-watch-02', className);
}

function applyClass(id, className) {
	let widget = document.getElementById(id);
	if (widget !== null) {
		widget.className = className;
		widget.repaint();
	}
}

function toggleHeadsUp() {
	document.getElementById('nmea-widgets-1').classList.toggle('mirror-upside-down');
	document.getElementById('nmea-widgets-2').classList.toggle('mirror-upside-down');
	document.getElementById('sky-maps-1').classList.toggle('mirror-upside-down');
	document.getElementById('sun-path-1').classList.toggle('mirror-upside-down');
}

function setPadding(range) {
	let v = range.value;
	document.getElementById('nmea-widgets-1').style.setProperty("--padding", v + "px");
	document.getElementById('nmea-widgets-2').style.setProperty("--padding", v + "px");
	document.getElementById('sky-maps-1').style.setProperty("--padding", v + "px");
	document.getElementById('sun-path-1').style.setProperty("--padding", v + "px");
}

function setPerspective(range) {
	let v = range.value;
	document.getElementById('nmea-widgets-1').style.setProperty("--perspective", v + "em");
	document.getElementById('nmea-widgets-2').style.setProperty("--perspective", v + "em");
	document.getElementById('sky-maps-1').style.setProperty("--perspective", v + "em");
	document.getElementById('sun-path-1').style.setProperty("--perspective", v + "em");
}

function setRotateX(range) {
	let v = range.value;
	document.getElementById('nmea-widgets-1').style.setProperty("--rotateX", v + "deg");
	document.getElementById('nmea-widgets-2').style.setProperty("--rotateX", v + "deg");
	document.getElementById('sky-maps-1').style.setProperty("--rotateX", v + "deg");
	document.getElementById('sun-path-1').style.setProperty("--rotateX", v + "deg");
}

function setScaleY(scale) {
	let v = scale.value;
	document.body.style.setProperty("--scaleY", v);
	// document.getElementById('nmea-widgets-1').style.setProperty("--scaleY", v);
	// document.getElementById('nmea-widgets-2').style.setProperty("--scaleY", v);
	// document.getElementById('sky-maps-1').style.setProperty("--scaleY", v);
	// document.getElementById('sun-path-1').style.setProperty("--scaleY", v);
}


let aws = 0;
let awa = 0;
let tws = 0;
let twa = 0;
let hdg = 0;
let gpsTime = undefined;

let gpsPosition = undefined;
let withStars = false;
let withWanderingBodies = false;

const THEMES = {
	"day":        "analogdisplay-day",
	"night":      "analogdisplay-night",
	"cyan":       "analogdisplay-monochrome-cyan",
	"black":      "analogdisplay-monochrome-black",
	"white":      "analogdisplay-monochrome-white",
	"orange":     "analogdisplay-monochrome-orange",
	"yellow":     "analogdisplay-monochrome-yellow",
	"flat-gray":  "analogdisplay-flat-gray",
	"flat-black": "analogdisplay-flat-black"
};
const DISPLAYS = [
	'compass-01',
	'tw-01',
	'bsp-01',
	'aw-01',
	'analog-watch-01',
	'analog-watch-02'
		// TODO Dev Curve Style?
];

function devCurveCallback(elmt, context) {
	// console.log('In DevCurveCallback, hdg=', hdg);
	context.beginPath();
	context.lineWidth = 3;
	context.strokeStyle = 'red';
	// Assume it is a vertical graph
	// There must be a better way, but this will do for now.
	// TODO a getCoordinateFromValue in the GraphDisplay Web Component.
	context.moveTo(0, elmt._padding + ((elmt._height - (2 * elmt._padding)) * (hdg / 360)));
	context.lineTo(elmt._width, elmt._padding + ((elmt._height - (2 * elmt._padding)) * (hdg / 360)));
	context.stroke();
	context.closePath();
}

window.onload = () => {
	/* global initAjax */
	initAjax(); // Default. See later for a WebSocket option

	callFirst("world-map-01"); // Will change the background, based on the Sun's altitude
	callAfter('world-map-01'); // Adding Satellites plot.

	// Query String prms, border, bg, style, like ?border=n&bg=black&style=orange&boat-data=n
	let style = getQSPrm('style');
	let border = getQSPrm('border');
	let bg = getQSPrm('bg');
	let boatData = getQSPrm('boat-data');

	if (style !== undefined) {
		if (style === 'day' || style === 'night' || style === 'cyan' || style === 'black' || style === 'white' || style === 'orange' || style === 'yellow' || style === 'flat-gray' || style === 'flat-black') {
			setTheme(THEMES[style]);
			// Set selected value
			document.getElementById("widgets-style").value = THEMES[style];
		} else {
			console.log("Unknown style", style);
		}
	}

	if (bg !== undefined) {
		if (bg === 'black' || bg === 'dark' || bg === 'light' || bg === 'white') {
			changeBG(bg.toUpperCase());
			document.getElementById(bg).checked = true;
		} else {
			console.log("Unknown background", bg);
		}
	}

	if (border !== undefined) {
		if (border === 'y' || border === 'n') {
			DISPLAYS.forEach((id, idx) => {
				let element = document.getElementById(id);
				if (element !== null) {
					element.withBorder = (border === 'y');
				}
			});
			// Check/uncheck boxes
			let cbs = document.getElementsByClassName('border-cb');
			for (let i = 0; i < cbs.length; i++) {
				cbs[i].checked = (border === 'y');
			}
		} else {
			console.log("Unknown border", border);
		}
	}

	if (boatData !== undefined) {
		if (/* boatData === 'y' || */ boatData === 'n') {
			expandCollapseBoatData();
		}
	}

	let sunPath = document.getElementById('sun-path-01');
	if (sunPath !== null && sunPath !== undefined) {
		let tiltSlider = document.getElementById('sun-path-tilt-slide'); // to use the arrows keys to move the sliders...
		let zSlider = document.getElementById('sun-path-z-slide');

		sunPath.addEventListener(
				'keydown',
				evt => { // ArrowRight, ArrowUp, ArrowLeft, ArrowDown
					if (evt.key !== undefined) {
//					console.log("1 Key:", evt.key, "Code:", evt.keyCode);
						if (evt.key === 'ArrowUp') {
//						console.log('ArrowUp!');
							let val = parseInt(tiltSlider.value);
//						console.log('Tilt is now ' + val);
							if (val < 90) {
								let newVal = val + 1;
								tiltSlider.value = newVal.toString();
								setNewTilt(tiltSlider, 'sun-path-01');
							}
						} else if (evt.key === 'ArrowDown') {
//						console.log('ArrowDown!');
							let val = parseInt(tiltSlider.value);
//						console.log('Tilt is now ' + val);
							if (val > -90) {
								let newVal = val - 1;
								tiltSlider.value = newVal.toString();
								setNewTilt(tiltSlider, 'sun-path-01');
							}
						} else if (evt.key === 'ArrowRight') {
							let val = parseInt(zSlider.value);
							if (val < 90) {
								let newVal = val + 1;
								zSlider.value = newVal.toString();
								setNewZOffset(zSlider, 'sun-path-01');
							}
						} else if (evt.key === 'ArrowLeft') {
							let val = parseInt(zSlider.value);
							if (val > -90) {
								let newVal = val - 1;
								zSlider.value = newVal.toString();
								setNewZOffset(zSlider, 'sun-path-01');
							}
						}
					} else {
						console.log('... Bad.');
					}
				}, false);
	}

	let devCurve = document.getElementById('compass-deviation');
	if (devCurve !== null && devCurve !== undefined) {
		// Get curve data
		let getData = requestDevCurve();
		getData.then((value) => { // Resolve
			let devData = JSON.parse(value);
			// console.log('Deviation data:', JSON.stringify(devData, null, 2));
			deviationCurve = devData;
			let finalData = {
				withGrid: true,
				withXLabels: true,
				withYLabels: true,
				minX: 0,
				maxX: 360,
				minY: 0,
				maxY: 0,
				thickX: null,
				thickY: 0,
				data: [
					{
						name: 'Deviation dev on Compass Headings',
						lineColor: 'lime',
						fillColor: null,
						thickness: 3,
						x: [],
						values: [] // Same cardinality as x
					}, { // dev on HDG
						name: 'Deviation dev on Magnetic Headings',
						lineColor: 'cyan',
						fillColor: null, // With gradient ?
						thickness: 1,
						x: [],
						values: [] // Same cardinality as x
					}
				]
			};
			let mini = Number.MAX_VALUE;
			let maxi = -Number.MAX_VALUE;
			let x = [], y = [], x2 = [], y2 = [];
			devData.forEach(tuple => {
				let hdg = tuple[0];
				let dev = tuple[1];
				mini = Math.min(mini, dev);
				maxi = Math.max(maxi, dev);
				x.push(hdg);
				y.push(dev);
				x2.push(hdg - dev);
				y2.push(dev);
			});
			finalData.data[0].x = x;
			finalData.data[0].values = y;
			finalData.data[1].x = x2;
			finalData.data[1].values = y2;
			finalData.minY = Math.min(mini, -3);
			finalData.maxY = Math.max(maxi, 3);
			console.log("DevCurve data:", finalData);
			devCurve.data = finalData;
			let amplitude = finalData.maxY - finalData.minY;
			devCurve.vgrid = Math.floor(finalData.minY).toFixed(0) + ":" + (amplitude < 7 ? "1" : "2");
			devCurve.hgrid = "0:45";
			devCurve.setDoAfter((elmt, context) => {
				devCurveCallback(elmt, context);
			});
			devCurve.repaint();
		}, (error) => { // Error
			console.log('Error getting dev curve:', error);
		});
	}
};

function displayErr(err) {
	if (err !== undefined) {
		console.log(err);
	}
}
