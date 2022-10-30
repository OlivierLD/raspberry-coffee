import * as CelestialComputer from './longterm.almanac.js';
import { 
	sightReduction, 
	getGCDistance, 
	getGCDistanceDegreesNM, 
	calculateGreatCircle, 
	getMoonTilt,
	parseDuration
} from './utils.js';

// import * as CelestialComputer from './lib/celestial-computer.min.js';
// let CelestialComputer = require('./longterm.almanac.js');

export function sampleMain(userDataObject) {
	let year = userDataObject.utcyear;
	let	month = userDataObject.utcmonth;
	if (month < 1 || month > 12) {
		throw new Error("Month out of range! Restart calculation.");
	}
	let day = userDataObject.utcday;
	if (day < 1 || day > 31) {
		throw new Error("Day out of range! Restart calculation.");
	}
	let leap = CelestialComputer.isLeapYear(year);
	if (month === 2 && day > 28 && !leap) {
		throw new Error("February has only 28 days! Restart calculation.");
	}
	if (month === 2 && day > 29 && leap) {
		throw new Error("February has only 29 days in a leap year! Restart calculation.");
	}
	if (month === 4 && day > 30) {
		throw new Error("April has only 30 days! Restart calculation.");
	}
	if (month === 6 && day > 30) {
		throw new Error("June has only 30 days! Restart calculation.");
	}
	if (month === 9 && day > 30) {
		throw new Error("September has only 30 days! Restart calculation.");
	}
	if (month === 11 && day > 30) {
		throw new Error("November has only 30 days! Restart calculation.");
	}
	let hour = userDataObject.utchour;
	let minute = userDataObject.utcminute;
	let second = userDataObject.utcsecond;

	let delta_t = userDataObject.deltaT;

	delta_t = CelestialComputer.calculateDeltaT(year, month); // Recompute for current date (year and month). More accurate ;)
	// console.log("DeltaT is now %f", delta_t);

	let noPlanets = userDataObject.noPlanets || false;
	return CelestialComputer.calculate(year, month, day, hour, minute, second, delta_t, noPlanets);
}

// Expose required functions
window.sampleMain = sampleMain;
window.gridSquare = CelestialComputer.gridSquare;
window.sightReduction = CelestialComputer.sightReduction; // Note: This is the one in utils.js
window.getGCDistance = getGCDistance;
window.getGCDistanceDegreesNM = getGCDistanceDegreesNM;
window.calculateGreatCircle = calculateGreatCircle;
window.getMoonTilt = getMoonTilt;
window.getSunMeridianPassageTime = CelestialComputer.getSunMeridianPassageTime;
window.decimalToDMS = CelestialComputer.decimalToDMS;
//window.getSunDataForAllDay = CelestialComputer.getSunDataForAllDay;

let STANDALONE = false;
let STANDALONE_2 = true;

if (STANDALONE) {
	console.log("SRU Test:" + JSON.stringify(sightReduction(37.5,-122.3, 80, 22)));
	console.log(`GC Dist (in miles):${ 60.0 * Math.toDegrees(getGCDistance({lat: Math.toRadians(37), lng: Math.toRadians(-122)}, {lat: Math.toRadians(47), lng: Math.toRadians(-3)}))}`);
	console.log(`GC Dist (in miles):${ getGCDistanceDegreesNM({lat:37, lng: -122}, {lat: 47, lng: -3})}`);

	let from = { lat: Math.toRadians(19.0), lng: Math.toRadians(-160.0) }; // Cook
	let to = { lat: Math.toRadians(19.0), lng: Math.toRadians(-170.0) }; // Niue
	let route = calculateGreatCircle(from, to, 20);
	route.forEach(rp => {
		console.log(`Pt: ${Math.toDegrees(rp.point.lat)}/${Math.toDegrees(rp.point.lng)}, Z:${rp.z}`);
	});
	// Moon tilt
	// Obs {lat: 37.7489, lng: -122.507}
	// Moon: GHA: 77.40581427333474, Dec: 11.184778568111762
	// Sun: GHA: 28.50281942727125, Dec: 17.07827750394256
	let moonTilt = getMoonTilt({lat: 37.7489, lng: -122.507}, 
								{gha: 28.50281942727125, dec: 17.07827750394256}, 
								{gha: 77.40581427333474, dec: 11.184778568111762});
	console.log(`Moon Tilt: ${moonTilt}`);

	console.log("End of test");
}

if (STANDALONE_2) {
	// let date = "2011-02-06T14:41:42.000Z";
	// let lat = -10.761383333333333, lng = -156.24046666666666;

	// let date = "2022-03-20T10:41:42.000Z";
	// let lat = 47.661667, lng = -2.758167;

	let date = "2022-10-30T09:12:45.000Z";
	let lat = 37.0, lng = -122.0;

	let duration = parseDuration(date);

	// let year = 2011, month = 2, day = 6, hour = 14, minute = 41, second = 42;
	let year = duration.year, month = duration.month, day = duration.day, hour = duration.hour, minute = duration.minute, second = duration.second;
	let delta_t = CelestialComputer.calculateDeltaT(year, month);

	let userDataObject = {
		utcyear: year,
		utcmonth: month,
		utcday: day,
		utchour: hour,
		utcminute: minute,
		utcsecond: second,
		deltaT: delta_t, // 69.2201,
		noPlanets: false
	};
	let result = sampleMain(userDataObject);
	console.log(`Result: ${JSON.stringify(result, null, 2)}`);
	let sr = sightReduction(lat, lng, result.sun.GHA.raw, result.sun.DEC.raw);
	let tt = getSunMeridianPassageTime(lat, lng, result.EOT.raw);
	let dms = decimalToDMS(tt);
	console.log(`Transit Time: ${year}:${month}:${day} ${dms.hours}:${dms.minutes}:${dms.seconds}`);

	console.log(`Sun HP: ${result.sun.HP.raw}, SD: ${result.sun.SD.raw}`);

    let sunBodyData = CelestialComputer.getSunDataForDate(delta_t, 
		                                                  date, 
														  lat, 
														  lng, 
														  result.epoch,
														  result.sun.DEC.raw, 
														  result.sun.GHA.raw,
														  result.sun.HP.raw, 
														  result.sun.SD.raw, 
														  result.EOT.raw);

    let solarDate = CelestialComputer.getSolarDateAtPos(lat, lng, result.EOT.raw, year, month, day, hour, minute, second, delta_t);
	console.log(`Solar Date at ${lat}/${lng}: ${solarDate.year}-${solarDate.month}-${solarDate.day} ${solarDate.hour}:${solarDate.minute}:${solarDate.second}`);

    // returned by the Java equivalent
    // let expectedReturn = {           
	// 	"epoch" : 1297003302000,
	// 	"lat" : -10.761383333333333,
	// 	"lng" : -156.24046666666666,
	// 	"body" : "Sun",
	// 	"decl" : -15.600935281704992,
	// 	"gha" : 36.91262094944125,
	// 	"altitude" : -24.409078512906643,
	// 	"z" : 112.76000558231725,
	// 	"eot" : -14.087231881717116,
	// 	"riseTime" : 1297009612725,
	// 	"setTime" : 1297054265725,
	// 	"sunTransitTime" : 1297031940719,
	// 	"riseZ" : 105.86454561140924,
	// 	"setZ" : 254.29796783140625
	//   };

	  let expectedReturn = {
		"epoch" : 1647772902000,
		"lat" : 47.661667,
		"lng" : -2.758167,
		"body" : "Sun",
		"decl" : -0.08003528415023496,
		"gha" : 338.55586541758504,
		"altitude" : 37.82670224870528,
		"z" : 148.73335028994708,
		"eot" : -7.382407987207898,
		"riseTime" : 1647757154906,
		"setTime" : 1647800317906,
		"sunTransitTime" : 1647778710904,
		"riseZ" : 90.22625189343447,
		"setZ" : 270.0677844943642
	  };

	console.log("-- Test (2),\n" + JSON.stringify(sunBodyData, null, 2));

	// Value tests
	const MAX_DIFF = 10e-6;
	const MAX_EPOCH_DIFF = 1000;
	const MAX_Z_DIFF = 1;
	console.log(`Epoch is ${sunBodyData.epoch === expectedReturn.epoch ? '' : 'not '}OK`);	
	console.log(`Decl is ${Math.abs(sunBodyData.decl - expectedReturn.decl) < MAX_DIFF ? '' : 'not '}OK`);	
	console.log(`GHA is ${Math.abs(sunBodyData.gha - expectedReturn.gha) < MAX_DIFF ? '' : 'not '}OK`);	
	console.log(`Elev is ${Math.abs(sunBodyData.elev - expectedReturn.altitude) < MAX_DIFF ? '' : 'not '}OK`);	
	console.log(`Z is ${Math.abs(sunBodyData.z - expectedReturn.z) < MAX_DIFF ? '' : 'not '}OK`);	
	console.log(`EoT is ${Math.abs(sunBodyData.eot - expectedReturn.eot) < MAX_EPOCH_DIFF ? '' : 'not '}OK`);	
	console.log(`Rise Epoch is ${Math.abs(sunBodyData.riseTime === expectedReturn.riseTime) < MAX_EPOCH_DIFF ? '' : 'not '}OK`);	
	console.log(`Set Epoch is ${Math.abs(sunBodyData.setTime === expectedReturn.setTime) < MAX_EPOCH_DIFF ? '' : 'not '}OK`);	
	console.log(`TT is ${Math.abs(sunBodyData.sunTransitTime === expectedReturn.sunTransitTime) < MAX_EPOCH_DIFF ? '' : 'not '}OK`);	
	console.log(`Rise Z is ${Math.abs(sunBodyData.riseZ - expectedReturn.riseZ) < MAX_Z_DIFF ? '' : 'not '}OK`);	
	console.log(`Set Z is ${Math.abs(sunBodyData.setZ - expectedReturn.setZ) < MAX_Z_DIFF ? '' : 'not '}OK`);	

	console.log(`Sun Rise   : ${new Date(sunBodyData.riseTime)}`);
	console.log(`Sun Transit: ${new Date(sunBodyData.sunTransitTime)}`);
	console.log(`Sun Set    : ${new Date(sunBodyData.setTime)}`);

	const STEP = 20; // in mminutes
	let sunPath = CelestialComputer.getSunDataForAllDay(sunBodyData, delta_t, lat, lng, STEP, result.epoch);  // result.epoch, is the same as sunBodyData.epoch

	console.log(`Sun Path: ${JSON.stringify(sunPath, null, 2)}`);

	// Note: that one takes time.
	// Will be used to feed a <sun-path> web component (WiP).
	if (false) {
		let rs = CelestialComputer.sunRiseAndSetEpoch(delta_t, 
													year, 
													month, 
													day, 
													lat, 
													lng, 
													result.sun.DEC.raw, 
													result.sun.HP.raw, 
													result.sun.SD.raw, 
													result.EOT.raw);

		console.log("End of Test (2)," + JSON.stringify(rs, null, 2));
	}
	console.log("End of Test (2).");
}
