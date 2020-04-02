import * as CelestialComputer from './longterm.almanac.js';
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

	let noPlanets = userDataObject.noPlanets || false;
	return CelestialComputer.calculate(year, month, day, hour, minute, second, delta_t, noPlanets);
}

window.sampleMain = sampleMain;
