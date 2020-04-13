"use strict";

let CelestialComputer = require('./longterm.almanac.js');

function sampleMain(userDataObject) {
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

	return CelestialComputer.calculate(year, month, day, hour, minute, second, delta_t);
}

let now = new Date();
let sampleData = {
	utcyear: now.getUTCFullYear(),
	utcmonth: now.getUTCMonth() + 1, // Zero based
	utcday: now.getUTCDate(),
	utchour: now.getUTCHours(),
	utcminute: now.getUTCMinutes(),
	utcsecond: now.getUTCSeconds(),
	deltaT: 69.01
};

let testResult = sampleMain(sampleData);
console.log("Calculation done %d-%d-%d %d:%d:%d UTC :", sampleData.utcyear, sampleData.utcmonth, sampleData.utcday, sampleData.utchour, sampleData.utcminute, sampleData.utcsecond);
console.log("Result:\n", JSON.stringify(testResult, null, 2));
