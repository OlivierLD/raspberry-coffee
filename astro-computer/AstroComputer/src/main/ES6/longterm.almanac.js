"use strict";
/**
 * @author OliverLD
 * Adapted in ES6 from Henning Umland's original code.
 * https://www.celnav.de/
 * http://www.titulosnauticos.net/astro/
 *
 * DeltaT can be found at http://maia.usno.navy.mil/ (will be back to life in 2020... in your dreams)
 * Also known as CelestialComputer.
 */

if (Math.toRadians === undefined) {
	Math.toRadians = (deg) => {
		return deg * (Math.PI / 180);
	};
}

if (Math.toDegrees === undefined) {
	Math.toDegrees = (rad) => {
		return rad * (180 / Math.PI);
	};
}

import * as Utils from './utils.js';

import * as Earth from './earth.js';
import * as Venus from './venus.js';
import * as Mars from './mars.js';
import * as Jupiter from './jupiter.js';
import * as Saturn from './saturn.js';

import STAR_CATALOG from './stars.js';

const EPS0_2000 = 23.439291111; // Reference

// Global Variables
let T, T2, T3, T4, T5, TE, TE2, TE3, TE4, TE5, Tau, Tau2, Tau3, Tau4, Tau5, deltaT,
	eps0, eps, deltaPsi, deltaEps, Le, Be, Re, kappa, pi0, e, lambdaSun, RASun, DECSun,
	GHASun, SDSun, HPSun, EoT, fmtEoT, EoE, EoEout, Lsun_true, Lsun_prime, dES, dayFraction, GHAAmean,
	RAVenus, DECVenus, GHAVenus, SDVenus, HPVenus, RAMars, DECMars, GHAMars, SDMars, HPMars,
	RAJupiter, DECJupiter, GHAJupiter, SDJupiter, HPJupiter, RASaturn, DECSaturn, GHASaturn, SDSaturn, HPSaturn,
	RAMoon, DECMoon, GHAMoon, SDMoon, HPMoon, RAPol, DECPol, GHAPol, OoE, tOoE, LDist,
	JD0h, JD, JDE, lambdaMapp, SidTm, GHAAtrue, SidTa,
	moonPhaseAngle = 0, moonPhase = "", DoW = "", illumMoon, illumVenus, illumMars, illumJupiter, illumSaturn,
	epoch;

/**
 * Main function
 * @param year Number, UTC year
 * @param month Number, UTC month, [1..12]
 * @param day Number, UTC day of month
 * @param hour Number, UTC hour
 * @param minute Number, UTC minute
 * @param second Number, UTC second
 * @param delta_t Number, DeltaT
 */
export function calculate(year, month, day, hour, minute, second, delta_t, noPlanets=false, withStars=true) {

	epoch = new Date(`${year}-${lpad(month, '0', 2)}-${lpad(day, '0', 2)} ${lpad(hour, '0', 2)}:${lpad(minute, '0', 2)}:${lpad(second, '0', 2)} GMT+0000`).getTime();

	calculateJulianDate(year, month, day, hour, minute, second, delta_t);
	calculateNutation();
	calculateAberration();
	calculateAries();
	calculateSun();
	if (!noPlanets) {
		calculateVenus();
		calculateMars();
		calculateJupiter();
		calculateSaturn();
	}
	calculateMoon();
	calculatePolaris();
	calculateMoonPhase();
	calculateWeekDay();
	return gatherOutput(noPlanets, withStars);
}

export function isLeapYear(year) {
	let ly = false;
	if (year / 4 - Math.floor(year / 4) === 0) {
		ly = true;
	}
	if (year / 100 - Math.floor(year / 100) === 0) {
		ly = false;
	}
	if (year / 400 - Math.floor(year / 400) === 0) {
		ly = true;
	}
	return ly;
}

/**
 * Input data conversion and reworking
 * All data are UTC data (except detlaT)
 **
 * @param year Number, UTC year
 * @param month Number, UTC month, [1..12]
 * @param day Number, UTC day of month
 * @param hour Number, UTC hour
 * @param minute Number, UTC minute
 * @param second Number, UTC second
 * @param delta_t Number, DeltaT
 */
function calculateJulianDate(year, month, day, hour, minute, second, delta_t) {

	dayFraction = (hour + minute / 60 + second / 3600) / 24;
	if (dayFraction < 0 || dayFraction > 1) {
		throw new Error("Time out of range! Restart calculation.");
	}
	deltaT = delta_t;

	// Calculating Julian date, century, and millennium

	// Julian date (UT1)
	if (month <= 2) {
		year -= 1;
		month += 12;
	}
	let A = Math.floor(year / 100);
	let B = 2 - A + Math.floor(A / 4);
	JD0h = Math.floor(365.25 * (year + 4716)) + Math.floor(30.6001 * (month + 1)) + day + B - 1524.5;

	JD = JD0h + dayFraction;

	// Julian centuries (UT1) from 2000 January 0.5
	T = (JD - 2451545) / 36525;
	T2 = T * T;
	T3 = T * T2;
	T4 = T * T3;
	T5 = T * T4;

	// Julian ephemeris date (TDT)
	JDE = JD + deltaT / 86400;

	// Julian centuries (TDT) from 2000 January 0.5
	TE = (JDE - 2451545) / 36525;
	TE2 = TE * TE;
	TE3 = TE * TE2;
	TE4 = TE * TE3;
	TE5 = TE * TE4;

	// Julian millenniums (TDT) from 2000 January 0.5
	Tau = 0.1 * TE;
	Tau2 = Tau * Tau;
	Tau3 = Tau * Tau2;
	Tau4 = Tau * Tau3;
	Tau5 = Tau * Tau4;
}

// Output Hour Angle
function outHA(x) {
	let GHAdeg = Math.floor(x);
	let GHAmin = Math.floor(60 * (x - GHAdeg));
	let GHAsec = Math.round(3600 * (x - GHAdeg - GHAmin / 60));
	if (GHAsec === 60) {
		GHAsec = 0;
		GHAmin += 1;
	}

	if (GHAmin === 60) {
		GHAmin = 0;
		GHAdeg += 1;
	}

	if (GHAdeg === 0) {
		GHAdeg = "000";
	} else if (GHAdeg < 10) {
		GHAdeg = "00" + GHAdeg;
	} else if (GHAdeg < 100) {
		GHAdeg = "0" + GHAdeg;
	}
	if (GHAmin === 0) {
		GHAmin = "00";
	} else if (GHAmin < 10) {
		GHAmin = "0" + GHAmin;
	}
	if (GHAsec < 10) {
		GHAsec = "0" + GHAsec;
	}
	return GHAdeg + "°" + " " + GHAmin + "'" + " " + GHAsec + "\"";
}

// Output Right Ascension
function outRA(x) {
	let t = x / 15;
	let RAh = Math.floor(t);
	let RAmin = Math.floor(60 * (t - RAh));
	let RAsec = Math.round(10 * (3600 * (t - RAh - RAmin / 60))) / 10;
	if (RAsec === 60) {
		RAsec = 0;
		RAmin += 1;
	}
	if (RAmin === 60) {
		RAmin = 0;
		RAh += 1;
	}
	if (RAh === 0) {
		RAh = "00";
	} else if (RAh < 10) {
		RAh = "0" + RAh;
	}
	if (RAmin === 0) {
		RAmin = "00";
	} else if (RAmin < 10) {
		RAmin = "0" + RAmin;
	}
	if (RAsec < 10) {
		RAsec = "0" + RAsec;
	}
	return RAh + "h" + " " + RAmin + "m" + " " + RAsec + "s";
}

// Equation of Time
function outEoT(x) {
	let sign = "";
	if (x < 0) {
		sign = "-";
	} else {
		sign = "+";
	}
	let EoT = Math.abs(x);
	let EOTmin = Math.floor(EoT);
	let EOTsec = Math.round(600 * (EoT - EOTmin)) / 10;
	if (EOTsec - Math.floor(EOTsec) === 0) {
		EOTsec += ".0";
	}
	if (EOTmin === 0) {
		EoT = " " + sign + " " + EOTsec + "s";
	} else {
		EoT = " " + sign + " " + EOTmin + "m " + EOTsec + "s";
	}
	return EoT;
}

// Output Obliquity of Ecliptic
function outECL(x) {
	let ECLdeg = Math.floor(x);
	let ECLmin = Math.floor(60 * (x - ECLdeg));
	let ECLsec = Math.round(3600000 * (x - ECLdeg - ECLmin / 60)) / 1000;
	if (ECLsec === 60) {
		ECLsec = 0;
		ECLmin += 1;
	}
	if (ECLmin === 60) {
		ECLmin = 0;
		ECLdeg += 1;
	}
	if (ECLmin === 0) {
		ECLmin = "00";
	} else if (ECLmin < 10) {
		ECLmin = "0" + ECLmin;
	}
	if (ECLsec < 10) {
		ECLsec = "0" + ECLsec;
	}
	return ECLdeg + "°" + " " + ECLmin + "'" + " " + ECLsec + "\"";
}

// Output Sidereal Time
function outSideralTime(x) {
	let GMSTdecimal = x / 15;
	let GMSTh = Math.floor(GMSTdecimal);
	let GMSTmdecimal = 60 * (GMSTdecimal - GMSTh);
	let GMSTm = Math.floor(GMSTmdecimal);
	let GMSTsdecimal = 60 * (GMSTmdecimal - GMSTm);
	let GMSTs = Math.round(1000 * GMSTsdecimal) / 1000;
	if (GMSTs - Math.floor(GMSTs) === 0) {
		GMSTs += ".000";
	} else if (10 * GMSTs - Math.floor(10 * GMSTs) === 0) {
		GMSTs += "00";
	} else if (100 * GMSTs - Math.floor(100 * GMSTs) === 0) {
		GMSTs += "0";
	}
	return GMSTh + "h" + " " + GMSTm + "m" + " " + GMSTs + "s";
}

// Output Declination
function outDec(x) {
	let name = "N", signDEC = 0;
	if (x < 0) {
		signDEC = -1;
		name = "S";
	} else {
		signDEC = 1;
		name = "N";
	}
	let DEC = Math.abs(x);
	let DECdeg = Math.floor(DEC);
	let DECmin = Math.floor(60 * (DEC - DECdeg));
	let DECsec = Math.round(3600 * (DEC - DECdeg - DECmin / 60));
	if (DECsec === 60) {
		DECsec = 0;
		DECmin += 1;
	}
	if (DECmin === 60) {
		DECmin = 0;
		DECdeg += 1;
	}
	if (DECdeg === 0) {
		DECdeg = "00";
	} else if (DECdeg < 10) {
		DECdeg = "0" + DECdeg;
	}
	if (DECmin === 0) {
		DECmin = "00";
	} else if (DECmin < 10) {
		DECmin = "0" + DECmin;
	}
	if (DECsec < 10) {
		DECsec = "0" + DECsec;
	}
	return name + "   " + DECdeg + "°" + " " + DECmin + "'" + " " + DECsec + "\"";
}

// Output SD and HP
function outSdHp(x) {
	x = Math.round(10 * x) / 10;
	if (x - Math.floor(x) === 0) {
		x += ".0";
	}
	return x + "\"";
}

// Astronomical functions
// Nutation, obliquity of the ecliptic
function calculateNutation() {
	// IAU 1980 calculateNutation theory:

	// Mean anomaly of the Moon
	let Mm = 134.962981389 + 198.867398056 * TE + Utils.norm360Deg(477000 * TE) + 0.008697222222 * TE2 + TE3 / 56250;

	// Mean anomaly of the Sun
	let M = 357.527723333 + 359.05034 * TE + Utils.norm360Deg(35640 * TE) - 0.0001602777778 * TE2 - TE3 / 300000;

	// Mean distance of the Moon from ascending node
	let F = 93.271910277 + 82.017538055 * TE + Utils.norm360Deg(483120 * TE) - 0.0036825 * TE2 + TE3 / 327272.7273;

	// Mean elongation of the Moon
	let D = 297.850363055 + 307.11148 * TE + Utils.norm360Deg(444960 * TE) - 0.001914166667 * TE2 + TE3 / 189473.6842;

	// Longitude of the ascending node of the Moon
	let omega = 125.044522222 - 134.136260833 * TE - Utils.norm360Deg(1800 * TE) + 0.002070833333 * TE2 + TE3 / 450000;

	// Periodic terms for nutation
	let nut = [
		[  0,  0,  0,  0,  1, -171996, -174.2,  92025,  8.9 ],
		[  0,  0,  2, -2,  2,  -13187,   -1.6,   5736, -3.1 ],
		[  0,  0,  2,  0,  2,   -2274,   -0.2,    977, -0.5 ],
		[  0,  0,  0,  0,  2,    2062,    0.2,   -895,  0.5 ],
		[  0, -1,  0,  0,  0,   -1426,    3.4,     54, -0.1 ],
		[  1,  0,  0,  0,  0,     712,    0.1,     -7,  0.0 ],
		[  0,  1,  2, -2,  2,    -517,    1.2,    224, -0.6 ],
		[  0,  0,  2,  0,  1,    -386,   -0.4,    200,  0.0 ],
		[  1,  0,  2,  0,  2,    -301,    0.0,    129, -0.1 ],
		[  0, -1,  2, -2,  2,     217,   -0.5,    -95,  0.3 ],
		[ -1,  0,  0,  2,  0,     158,    0.0,     -1,  0.0 ],
		[  0,  0,  2, -2,  1,     129,    0.1,    -70,  0.0 ],
		[ -1,  0,  2,  0,  2,     123,    0.0,    -53,  0.0 ],
		[  1,  0,  0,  0,  1,      63,    0.1,    -33,  0.0 ],
		[  0,  0,  0,  2,  0,      63,    0.0,     -2,  0.0 ],
		[ -1,  0,  2,  2,  2,     -59,    0.0,     26,  0.0 ],
		[ -1,  0,  0,  0,  1,     -58,   -0.1,     32,  0.0 ],
		[  1,  0,  2,  0,  1,     -51,    0.0,     27,  0.0 ],
		[ -2,  0,  0,  2,  0,     -48,    0.0,      1,  0.0 ],
		[ -2,  0,  2,  0,  1,      46,    0.0,    -24,  0.0 ],
		[  0,  0,  2,  2,  2,     -38,    0.0,     16,  0.0 ],
		[  2,  0,  2,  0,  2,     -31,    0.0,     13,  0.0 ],
		[  2,  0,  0,  0,  0,      29,    0.0,     -1,  0.0 ],
		[  1,  0,  2, -2,  2,      29,    0.0,    -12,  0.0 ],
		[  0,  0,  2,  0,  0,      26,    0.0,     -1,  0.0 ],
		[  0,  0,  2, -2,  0,     -22,    0.0,      0,  0.0 ],
		[ -1,  0,  2,  0,  1,      21,    0.0,    -10,  0.0 ],
		[  0,  2,  0,  0,  0,      17,   -0.1,      0,  0.0 ],
		[  0,  2,  2, -2,  2,     -16,    0.1,      7,  0.0 ],
		[ -1,  0,  0,  2,  1,      16,    0.0,     -8,  0.0 ],
		[  0,  1,  0,  0,  1,     -15,    0.0,      9,  0.0 ],
		[  1,  0,  0, -2,  1,     -13,    0.0,      7,  0.0 ],
		[  0, -1,  0,  0,  1,     -12,    0.0,      6,  0.0 ],
		[  2,  0, -2,  0,  0,      11,    0.0,      0,  0.0 ],
		[ -1,  0,  2,  2,  1,     -10,    0.0,      5,  0.0 ],
		[  1,  0,  2,  2,  2,      -8,    0.0,      3,  0.0 ],
		[  0, -1,  2,  0,  2,      -7,    0.0,      3,  0.0 ],
		[  0,  0,  2,  2,  1,      -7,    0.0,      3,  0.0 ],
		[  1,  1,  0, -2,  0,      -7,    0.0,      0,  0.0 ],
		[  0,  1,  2,  0,  2,       7,    0.0,     -3,  0.0 ],
		[ -2,  0,  0,  2,  1,      -6,    0.0,      3,  0.0 ],
		[  0,  0,  0,  2,  1,      -6,    0.0,      3,  0.0 ],
		[  2,  0,  2, -2,  2,       6,    0.0,     -3,  0.0 ],
		[  1,  0,  0,  2,  0,       6,    0.0,      0,  0.0 ],
		[  1,  0,  2, -2,  1,       6,    0.0,     -3,  0.0 ],
		[  0,  0,  0, -2,  1,      -5,    0.0,      3,  0.0 ],
		[  0, -1,  2, -2,  1,      -5,    0.0,      3,  0.0 ],
		[  2,  0,  2,  0,  1,      -5,    0.0,      3,  0.0 ],
		[  1, -1,  0,  0,  0,       5,    0.0,      0,  0.0 ],
		[  1,  0,  0, -1,  0,      -4,    0.0,      0,  0.0 ],
		[  0,  0,  0,  1,  0,      -4,    0.0,      0,  0.0 ],
		[  0,  1,  0, -2,  0,      -4,    0.0,      0,  0.0 ],
		[  1,  0, -2,  0,  0,       4,    0.0,      0,  0.0 ],
		[  2,  0,  0, -2,  1,       4,    0.0,     -2,  0.0 ],
		[  0,  1,  2, -2,  1,       4,    0.0,     -2,  0.0 ],
		[  1,  1,  0,  0,  0,      -3,    0.0,      0,  0.0 ],
		[  1, -1,  0, -1,  0,      -3,    0.0,      0,  0.0 ],
		[ -1, -1,  2,  2,  2,      -3,    0.0,      1,  0.0 ],
		[  0, -1,  2,  2,  2,      -3,    0.0,      1,  0.0 ],
		[  1, -1,  2,  0,  2,      -3,    0.0,      1,  0.0 ],
		[  3,  0,  2,  0,  2,      -3,    0.0,      1,  0.0 ],
		[ -2,  0,  2,  0,  2,      -3,    0.0,      1,  0.0 ],
		[  1,  0,  2,  0,  0,       3,    0.0,      0,  0.0 ],
		[ -1,  0,  2,  4,  2,      -2,    0.0,      1,  0.0 ],
		[  1,  0,  0,  0,  2,      -2,    0.0,      1,  0.0 ],
		[ -1,  0,  2, -2,  1,      -2,    0.0,      1,  0.0 ],
		[  0, -2,  2, -2,  1,      -2,    0.0,      1,  0.0 ],
		[ -2,  0,  0,  0,  1,      -2,    0.0,      1,  0.0 ],
		[  2,  0,  0,  0,  1,       2,    0.0,     -1,  0.0 ],
		[  3,  0,  0,  0,  0,       2,    0.0,      0,  0.0 ],
		[  1,  1,  2,  0,  2,       2,    0.0,     -1,  0.0 ],
		[  0,  0,  2,  1,  2,       2,    0.0,     -1,  0.0 ],
		[  1,  0,  0,  2,  1,      -1,    0.0,      0,  0.0 ],
		[  1,  0,  2,  2,  1,      -1,    0.0,      1,  0.0 ],
		[  1,  1,  0, -2,  1,      -1,    0.0,      0,  0.0 ],
		[  0,  1,  0,  2,  0,      -1,    0.0,      0,  0.0 ],
		[  0,  1,  2, -2,  0,      -1,    0.0,      0,  0.0 ],
		[  0,  1, -2,  2,  0,      -1,    0.0,      0,  0.0 ],
		[  1,  0, -2,  2,  0,      -1,    0.0,      0,  0.0 ],
		[  1,  0, -2, -2,  0,      -1,    0.0,      0,  0.0 ],
		[  1,  0,  2, -2,  0,      -1,    0.0,      0,  0.0 ],
		[  1,  0,  0, -4,  0,      -1,    0.0,      0,  0.0 ],
		[  2,  0,  0, -4,  0,      -1,    0.0,      0,  0.0 ],
		[  0,  0,  2,  4,  2,      -1,    0.0,      0,  0.0 ],
		[  0,  0,  2, -1,  2,      -1,    0.0,      0,  0.0 ],
		[ -2,  0,  2,  4,  2,      -1,    0.0,      1,  0.0 ],
		[  2,  0,  2,  2,  2,      -1,    0.0,      0,  0.0 ],
		[  0, -1,  2,  0,  1,      -1,    0.0,      0,  0.0 ],
		[  0,  0, -2,  0,  1,      -1,    0.0,      0,  0.0 ],
		[  0,  0,  4, -2,  2,       1,    0.0,      0,  0.0 ],
		[  0,  1,  0,  0,  2,       1,    0.0,      0,  0.0 ],
		[  1,  1,  2, -2,  2,       1,    0.0,     -1,  0.0 ],
		[  3,  0,  2, -2,  2,       1,    0.0,      0,  0.0 ],
		[ -2,  0,  2,  2,  2,       1,    0.0,     -1,  0.0 ],
		[ -1,  0,  0,  0,  2,       1,    0.0,     -1,  0.0 ],
		[  0,  0, -2,  2,  1,       1,    0.0,      0,  0.0 ],
		[  0,  1,  2,  0,  1,       1,    0.0,      0,  0.0 ],
		[ -1,  0,  4,  0,  2,       1,    0.0,      0,  0.0 ],
		[  2,  1,  0, -2,  0,       1,    0.0,      0,  0.0 ],
		[  2,  0,  0,  2,  0,       1,    0.0,      0,  0.0 ],
		[  2,  0,  2, -2,  1,       1,    0.0,     -1,  0.0 ],
		[  2,  0, -2,  0,  1,       1,    0.0,      0,  0.0 ],
		[  1, -1,  0, -2,  0,       1,    0.0,      0,  0.0 ],
		[ -1,  0,  0,  1,  1,       1,    0.0,      0,  0.0 ],
		[ -1, -1,  0,  2,  1,       1,    0.0,      0,  0.0 ],
		[  0,  1,  0,  1,  0,       1,    0.0,      0,  0.0 ]
	];

	// Reading periodic terms
	let fMm, fM, fF, fD, f_omega, dp = 0, de = 0, x;

	x = 0;
	while (x < nut.length) {
		fMm = nut[x][0];
		fM = nut[x][1];
		fF = nut[x][2];
		fD = nut[x][3];
		f_omega = nut[x][4];
		dp += (nut[x][5] + TE * nut[x][6]) * Utils.sind(fD * D + fM * M + fMm * Mm + fF * F + f_omega * omega);
		de += (nut[x][7] + TE * nut[x][8]) * Utils.cosd(fD * D + fM * M + fMm * Mm + fF * F + f_omega * omega);
		x++;
	}

	// Corrections (Herring, 1987)
	let corr = [
		[ 0, 0, 0, 0, 1,-725, 417, 213, 224 ],
		[ 0, 1, 0, 0, 0, 523,  61, 208, -24 ],
		[ 0, 0, 2,-2, 2, 102,-118, -41, -47 ],
		[ 0, 0, 2, 0, 2, -81,   0,  32,   0 ]
	];
	x = 0;
	while (x < corr.length) {
		fMm = corr[x][0];
		fM = corr[x][1];
		fF = corr[x][2];
		fD = corr[x][3];
		f_omega = corr[x][4];
		dp += 0.1 * (corr[x][5] * Utils.sind(fD * D + fM * M + fMm * Mm + fF * F + f_omega * omega) + corr[x][6] * Utils.cosd(fD * D + fM * M + fMm * Mm + fF * F + f_omega * omega));
		de += 0.1 * (corr[x][7] * Utils.cosd(fD * D + fM * M + fMm * Mm + fF * F + f_omega * omega) + corr[x][8] * Utils.sind(fD * D + fM * M + fMm * Mm + fF * F + f_omega * omega));
		x++;
	}


	// calculateNutation in longitude
	deltaPsi = dp / 36000000;

	// calculateNutation in obliquity
	deltaEps = de / 36000000;

	// Mean obliquity of the ecliptic
	eps0 = (84381.448 - 46.815 * TE - 0.00059 * TE2 + 0.001813 * TE3) / 3600;

	// True obliquity of the ecliptic
	eps = eps0 + deltaEps;
}

// aberration
function calculateAberration() {
	kappa = Math.toRadians(20.49552 / 3600);
	pi0 = Math.toRadians(102.93735 + 1.71953 * TE + 0.00046 * TE2);
	e = 0.016708617 - 0.000042037 * TE - 0.0000001236 * TE2;
}

// GHA Aries, GAST, GMST, equation of the equinoxes
function calculateAries() {
	// Mean GHA Aries
	GHAAmean = Utils.norm360Deg(280.46061837 + 360.98564736629 * (JD - 2451545) + 0.000387933 * T2 - T3 / 38710000);

	// GMST
	SidTm = outSideralTime(GHAAmean);

	// True GHA Aries
	GHAAtrue = Utils.norm360Deg(GHAAmean + deltaPsi * Utils.cosd(eps));

	// GAST
	SidTa = outSideralTime(GHAAtrue);

	// Equation of the equinoxes
	EoE = 240 * deltaPsi * Utils.cosd(eps);
	EoEout = Math.round(1000 * EoE) / 1000;
	// EoEout = " " + EoEout + "s";
}

// Calculations for the Sun
function calculateSun() {
	// Mean longitude of the Sun
	let Lsun_mean = Utils.norm360Deg(280.4664567 + 360007.6982779 * Tau + 0.03032028 * Tau2 + Tau3 / 49931 - Tau4 / 15299 - Tau5 / 1988000);

	// Heliocentric longitude of the Earth
	Le = Earth.lEarth(Tau);

	// Geocentric longitude of the Sun
	Lsun_true = Utils.norm360Deg(Le + 180 - 0.000025);

	// Heliocentric latitude of Earth
	Be = Earth.bEarth(Tau);

	// Geocentric latitude of the Sun
	let beta = Utils.norm360Deg(-Be);

	// Corrections
	Lsun_prime = Utils.norm360Deg(Le + 180 - 1.397 * TE - 0.00031 * TE2);

	beta = beta + 0.000011 * (Utils.cosd(Lsun_prime) - Utils.sind(Lsun_prime));

	// Distance Earth-Sun
	Re = Earth.rEarth(Tau);
	dES = 149597870.691 * Re;

	// Apparent longitude of the Sun
	lambdaSun = Utils.norm360Deg(Lsun_true + deltaPsi - 0.005691611 / Re);

	// Right ascension of the Sun, apparent
	RASun = Math.toDegrees(Utils.norm2PiRad(Math.atan2((Utils.sind(lambdaSun) * Utils.cosd(eps) - Utils.tand(beta) * Utils.sind(eps)), Utils.cosd(lambdaSun))));

	// Declination of the Sun, apparent
	DECSun = Math.toDegrees(Math.asin(Utils.sind(beta) * Utils.cosd(eps) + Utils.cosd(beta) * Utils.sind(eps) * Utils.sind(lambdaSun)));

	// GHA of the Sun
	GHASun = Utils.norm360Deg(GHAAtrue - RASun);

	// Semidiameter of the Sun
	SDSun = 959.63 / Re;

	//Horizontal parallax of the Sun
	HPSun = 8.794 / Re;

	// Equation of time
	// EoT = 4*(Lsun_mean-0.0057183-0.0008-RASun+deltaPsi*Utils.cosd(eps));
	EoT = 4 * GHASun + 720 - 1440 * dayFraction;
	if (EoT > 20) {
		EoT -= 1440;
	}
	if (EoT < -20) {
		EoT += 1440;
	}
}

// Calculations for Venus
function calculateVenus() {
	// Heliocentric spherical coordinates
	let L = Venus.lVenus(Tau);
	let B = Venus.bVenus(Tau);
	let R = Venus.rVenus(Tau);

	// Rectangular coordinates
	let x = R * Utils.cosd(B) * Utils.cosd(L) - Re * Utils.cosd(Be) * Utils.cosd(Le);
	let y = R * Utils.cosd(B) * Utils.sind(L) - Re * Utils.cosd(Be) * Utils.sind(Le);
	let z = R * Utils.sind(B) - Re * Utils.sind(Be);

	// Geocentric spherical coordinates
	let lambda = Math.atan2(y, x);
	let beta = Math.atan(z / Math.sqrt(x * x + y * y));

	// Distance from Earth / light time
	let d = Math.sqrt(x * x + y * y + z * z);
	let lt = 0.0057755183 * d;

	// Time correction
	let Tau_corr = (JDE - lt - 2451545) / 365250;

	// Coordinates corrected for light time
	L = Venus.lVenus(Tau_corr);
	B = Venus.bVenus(Tau_corr);
	R = Venus.rVenus(Tau_corr);
	x = R * Utils.cosd(B) * Utils.cosd(L) - Re * Utils.cosd(Be) * Utils.cosd(Le);
	y = R * Utils.cosd(B) * Utils.sind(L) - Re * Utils.cosd(Be) * Utils.sind(Le);
	z = R * Utils.sind(B) - Re * Utils.sind(Be);

	lambda = Math.atan2(y, x);
	beta = Math.atan(z / Math.sqrt(x * x + y * y));

	// aberration
	let dlambda = (e * kappa * Math.cos(pi0 - lambda) - kappa * Math.cos(Math.toRadians(Lsun_true) - lambda)) / Math.cos(beta);
	let dbeta = -kappa * Math.sin(beta) * (Math.sin(Math.toRadians(Lsun_true) - lambda) - e * Math.sin(pi0 - lambda));

	lambda += dlambda;
	beta += dbeta;

	// FK5
	let lambda_prime = lambda - Math.toRadians(1.397) * TE - Math.toRadians(0.00031) * TE2;

	dlambda = Math.toRadians(-0.09033) / 3600 + Math.toRadians(0.03916) / 3600 * (Math.cos(lambda_prime) + Math.sin(lambda_prime)) * Math.tan(beta);
	dbeta = Math.toRadians(0.03916) / 3600 * (Math.cos(lambda_prime) - Math.sin(lambda_prime));

	lambda += dlambda;
	beta += dbeta;

	// calculateNutation in longitude
	lambda += Math.toRadians(deltaPsi);

	// Right ascension, apparent
	RAVenus = Math.toDegrees(Utils.norm2PiRad(Math.atan2((Math.sin(lambda) * Utils.cosd(eps) - Math.tan(beta) * Utils.sind(eps)), Math.cos(lambda))));

	// Declination of Venus, apparent
	DECVenus = Math.toDegrees(Math.asin(Math.sin(beta) * Utils.cosd(eps) + Math.cos(beta) * Utils.sind(eps) * Math.sin(lambda)));

	// GHA of Venus
	GHAVenus = Utils.norm360Deg(GHAAtrue - RAVenus);

	// Semi-diameter of Venus (including cloud layer)
	SDVenus = 8.41 / d;

	// Horizontal parallax of Venus
	HPVenus = 8.794 / d;

	// Illumination of the planet's disk
	let k = 100 * (1 + ((R - Re * Utils.cosd(B) * Utils.cosd(L - Le)) / d)) / 2;
	illumVenus = Math.round(10 * k) / 10;
}

// Calculations for Mars
function calculateMars() {
	// Heliocentric coordinates
	let L = Mars.lMars(Tau);
	let B = Mars.bMars(Tau);
	let R = Mars.rMars(Tau);

	// Rectangular coordinates
	let x = R * Utils.cosd(B) * Utils.cosd(L) - Re * Utils.cosd(Be) * Utils.cosd(Le);
	let y = R * Utils.cosd(B) * Utils.sind(L) - Re * Utils.cosd(Be) * Utils.sind(Le);
	let z = R * Utils.sind(B) - Re * Utils.sind(Be);

	// Geocentric coordinates
	let lambda = Math.atan2(y, x);
	let beta = Math.atan(z / Math.sqrt(x * x + y * y));

	// Distance from earth / light time
	let d = Math.sqrt(x * x + y * y + z * z);
	let lt = 0.0057755183 * d;

	// Time correction
	let Tau_corr = (JDE - lt - 2451545) / 365250;

	// Coordinates corrected for light time
	L = Mars.lMars(Tau_corr);
	B = Mars.bMars(Tau_corr);
	R = Mars.rMars(Tau_corr);
	x = R * Utils.cosd(B) * Utils.cosd(L) - Re * Utils.cosd(Be) * Utils.cosd(Le);
	y = R * Utils.cosd(B) * Utils.sind(L) - Re * Utils.cosd(Be) * Utils.sind(Le);
	z = R * Utils.sind(B) - Re * Utils.sind(Be);

	lambda = Math.atan2(y, x);
	beta = Math.atan(z / Math.sqrt(x * x + y * y));

	// aberration
	let dlambda = (e * kappa * Math.cos(pi0 - lambda) - kappa * Math.cos(Math.toRadians(Lsun_true) - lambda)) / Math.cos(beta);
	let dbeta = -kappa * Math.sin(beta) * (Math.sin(Math.toRadians(Lsun_true) - lambda) - e * Math.sin(pi0 - lambda));

	lambda += dlambda;
	beta += dbeta;

	// FK5
	let lambda_prime = lambda - Math.toRadians(1.397) * TE -  Math.toRadians(0.00031) * TE2;

	dlambda =  Math.toRadians(-0.09033) / 3600 +  Math.toRadians(0.03916) / 3600 * (Math.cos(lambda_prime) + Math.sin(lambda_prime)) * Math.tan(beta);
	dbeta =  Math.toRadians(0.03916) / 3600 * (Math.cos(lambda_prime) - Math.sin(lambda_prime));

	lambda += dlambda;
	beta += dbeta;

	// calculateNutation in longitude
	lambda +=  Math.toRadians(deltaPsi);

	// Right ascension, apparent
	RAMars = Math.toDegrees(Utils.norm2PiRad(Math.atan2((Math.sin(lambda) * Utils.cosd(eps) - Math.tan(beta) * Utils.sind(eps)), Math.cos(lambda))));

	// Declination of Mars, apparent
	DECMars = Math.toDegrees(Math.asin(Math.sin(beta) * Utils.cosd(eps) + Math.cos(beta) * Utils.sind(eps) * Math.sin(lambda)));

	//GHA of Mars
	GHAMars = Utils.norm360Deg(GHAAtrue - RAMars);

	// Semi-diameter of Mars
	SDMars = 4.68 / d;

	// Horizontal parallax of Mars
	HPMars = 8.794 / d;

	// Illumination of the planet's disk
	let k = 100 * (1 + ((R - Re * Utils.cosd(B) * Utils.cosd(L - Le)) / d)) / 2;
	illumMars = Math.round(10 * k) / 10;
}

// Calculations for Jupiter
function calculateJupiter() {
	// Heliocentric coordinates
	let L = Jupiter.lJupiter(Tau);
	let B = Jupiter.bJupiter(Tau);
	let R = Jupiter.rJupiter(Tau);

	// Rectangular coordinates
	let x = R * Utils.cosd(B) * Utils.cosd(L) - Re * Utils.cosd(Be) * Utils.cosd(Le);
	let y = R * Utils.cosd(B) * Utils.sind(L) - Re * Utils.cosd(Be) * Utils.sind(Le);
	let z = R * Utils.sind(B) - Re * Utils.sind(Be);

	// Geocentric coordinates
	let lambda = Math.atan2(y, x);
	let beta = Math.atan(z / Math.sqrt(x * x + y * y));

	// Distance from earth / light time
	let d = Math.sqrt(x * x + y * y + z * z);
	let lt = 0.0057755183 * d;

	// Time correction
	let Tau_corr = (JDE - lt - 2451545) / 365250;

	// Coordinates corrected for light time
	L = Jupiter.lJupiter(Tau_corr);
	B = Jupiter.bJupiter(Tau_corr);
	R = Jupiter.rJupiter(Tau_corr);
	x = R * Utils.cosd(B) * Utils.cosd(L) - Re * Utils.cosd(Be) * Utils.cosd(Le);
	y = R * Utils.cosd(B) * Utils.sind(L) - Re * Utils.cosd(Be) * Utils.sind(Le);
	z = R * Utils.sind(B) - Re * Utils.sind(Be);

	lambda = Math.atan2(y, x);
	beta = Math.atan(z / Math.sqrt(x * x + y * y));

	// aberration
	let dlambda = (e * kappa * Math.cos(pi0 - lambda) - kappa * Math.cos( Math.toRadians(Lsun_true) - lambda)) / Math.cos(beta);
	let dbeta = -kappa * Math.sin(beta) * (Math.sin( Math.toRadians(Lsun_true) - lambda) - e * Math.sin(pi0 - lambda));

	lambda += dlambda;
	beta += dbeta;

	// FK5
	let lambda_prime = lambda -  Math.toRadians(1.397) * TE -  Math.toRadians(0.00031) * TE2;

	dlambda =  Math.toRadians(-0.09033) / 3600 +  Math.toRadians(0.03916) / 3600 * (Math.cos(lambda_prime) + Math.sin(lambda_prime)) * Math.tan(beta);
	dbeta =  Math.toRadians(0.03916) / 3600 * (Math.cos(lambda_prime) - Math.sin(lambda_prime));

	lambda += dlambda;
	beta += dbeta;

	// calculateNutation in longitude
	lambda +=  Math.toRadians(deltaPsi);

	// Right ascension, apparent
	RAJupiter = Math.toDegrees(Utils.norm2PiRad(Math.atan2((Math.sin(lambda) * Utils.cosd(eps) - Math.tan(beta) * Utils.sind(eps)), Math.cos(lambda))));

	// Declination of Jupiter, apparent
	DECJupiter = Math.toDegrees(Math.asin(Math.sin(beta) * Utils.cosd(eps) + Math.cos(beta) * Utils.sind(eps) * Math.sin(lambda)));

	// GHA of Jupiter
	GHAJupiter = Utils.norm360Deg(GHAAtrue - RAJupiter);

	// Semi-diameter of Jupiter (equatorial)
	SDJupiter = 98.44 / d;

	// Horizontal parallax of Jupiter
	HPJupiter = 8.794 / d;

	// Illumination of the planet's disk
	let k = 100 * (1 + ((R - Re * Utils.cosd(B) * Utils.cosd(L - Le)) / d)) / 2;
	illumJupiter = Math.round(10 * k) / 10;
}

// Calculations for Saturn
function calculateSaturn() {
	// Heliocentric coordinates
	let L = Saturn.lSaturn(Tau);
	let B = Saturn.bSaturn(Tau);
	let R = Saturn.rSaturn(Tau);

	// Rectangular coordinates
	let x = R * Utils.cosd(B) * Utils.cosd(L) - Re * Utils.cosd(Be) * Utils.cosd(Le);
	let y = R * Utils.cosd(B) * Utils.sind(L) - Re * Utils.cosd(Be) * Utils.sind(Le);
	let z = R * Utils.sind(B) - Re * Utils.sind(Be);

	// Geocentric coordinates
	let lambda = Math.atan2(y, x);
	let beta = Math.atan(z / Math.sqrt(x * x + y * y));

	// Distance from earth / light time
	let d = Math.sqrt(x * x + y * y + z * z);
	let lt = 0.0057755183 * d;

	// Time correction
	let Tau_corr = (JDE - lt - 2451545) / 365250;

	// Coordinates corrected for light time
	L = Saturn.lSaturn(Tau_corr);
	B = Saturn.bSaturn(Tau_corr);
	R = Saturn.rSaturn(Tau_corr);
	x = R * Utils.cosd(B) * Utils.cosd(L) - Re * Utils.cosd(Be) * Utils.cosd(Le);
	y = R * Utils.cosd(B) * Utils.sind(L) - Re * Utils.cosd(Be) * Utils.sind(Le);
	z = R * Utils.sind(B) - Re * Utils.sind(Be);

	lambda = Math.atan2(y, x);
	beta = Math.atan(z / Math.sqrt(x * x + y * y));

	// aberration
	let dlambda = (e * kappa * Math.cos(pi0 - lambda) - kappa * Math.cos( Math.toRadians(Lsun_true) - lambda)) / Math.cos(beta);
	let dbeta = -kappa * Math.sin(beta) * (Math.sin( Math.toRadians(Lsun_true) - lambda) - e * Math.sin(pi0 - lambda));

	lambda += dlambda;
	beta += dbeta;

	// FK5
	let lambda_prime = lambda -  Math.toRadians(1.397) * TE -  Math.toRadians(0.00031) * TE2;
	dlambda =  Math.toRadians(-0.09033) / 3600 +  Math.toRadians(0.03916) / 3600 * (Math.cos(lambda_prime) + Math.sin(lambda_prime)) * Math.tan(beta);
	dbeta =  Math.toRadians(0.03916) / 3600 * (Math.cos(lambda_prime) - Math.sin(lambda_prime));

	lambda += dlambda;
	beta += dbeta;

	// calculateNutation in longitude
	lambda +=  Math.toRadians(deltaPsi);

	// Right ascension, apparent
	RASaturn = Math.toDegrees(Utils.norm2PiRad(Math.atan2((Math.sin(lambda) * Utils.cosd(eps) - Math.tan(beta) * Utils.sind(eps)), Math.cos(lambda))));

	// Declination of Saturn, apparent
	DECSaturn = Math.toDegrees(Math.asin(Math.sin(beta) * Utils.cosd(eps) + Math.cos(beta) * Utils.sind(eps) * Math.sin(lambda)));

	// GHA of Saturn
	GHASaturn = Utils.norm360Deg(GHAAtrue - RASaturn);

	// Semi-diameter of Saturn (equatorial)
	SDSaturn = 82.73 / d;

	// Horizontal parallax of Saturn
	HPSaturn = 8.794 / d;

	// Illumination of the planet's disk
	let k = 100 * (1 + ((R - Re * Utils.cosd(B) * Utils.cosd(L - Le)) / d)) / 2;
	illumSaturn = Math.round(10 * k) / 10;
}

// Calculations for the moon
function calculateMoon() {
	// Mean longitude of the moon
	let Lmm = Utils.norm360Deg(218.3164591 + 481267.88134236 * TE - 0.0013268 * TE2 + TE3 / 538841 - TE4 / 65194000);

	// Mean elongation of the moon
	let D = Utils.norm360Deg(297.8502042 + 445267.1115168 * TE - 0.00163 * TE2 + TE3 / 545868 - TE4 / 113065000);

	// Mean anomaly of the sun
	let Msm = Utils.norm360Deg(357.5291092 + 35999.0502909 * TE - 0.0001536 * TE2 + TE3 / 24490000);

	// Mean anomaly of the moon
	let Mmm = Utils.norm360Deg(134.9634114 + 477198.8676313 * TE + 0.008997 * TE2 + TE3 / 69699 - TE4 / 14712000);

	// Mean distance of the moon from ascending node
	let F = Utils.norm360Deg(93.2720993 + 483202.0175273 * TE - 0.0034029 * TE2 - TE3 / 3526000 + TE4 / 863310000);

	// Corrections
	let A1 = Utils.norm360Deg(119.75 + 131.849 * TE);
	let A2 = Utils.norm360Deg(53.09 + 479264.29 * TE);
	let A3 = Utils.norm360Deg(313.45 + 481266.484 * TE);
	let fE = 1 - 0.002516 * TE - 0.0000074 * TE2;
	let fE2 = fE * fE;

	// Periodic terms for the moon:

	// Longitude and distance
	let ld = [
		[ 0,  0,  1,  0, 6288774, -20905355 ],
		[ 2,  0, -1,  0, 1274027,  -3699111 ],
		[ 2,  0,  0,  0,  658314,  -2955968 ],
		[ 0,  0,  2,  0,  213618,   -569925 ],
		[ 0,  1,  0,  0, -185116,     48888 ],
		[ 0,  0,  0,  2, -114332,     -3149 ],
		[ 2,  0, -2,  0,   58793,    246158 ],
		[ 2, -1, -1,  0,   57066,   -152138 ],
		[ 2,  0,  1,  0,   53322,   -170733 ],
		[ 2, -1,  0,  0,   45758,   -204586 ],
		[ 0,  1, -1,  0,  -40923,   -129620 ],
		[ 1,  0,  0,  0,  -34720,    108743 ],
		[ 0,  1,  1,  0,  -30383,    104755 ],
		[ 2,  0,  0, -2,   15327,     10321 ],
		[ 0,  0,  1,  2,  -12528,         0 ],
		[ 0,  0,  1, -2,   10980,     79661 ],
		[ 4,  0, -1,  0,   10675,    -34782 ],
		[ 0,  0,  3,  0,   10034,    -23210 ],
		[ 4,  0, -2,  0,    8548,    -21636 ],
		[ 2,  1, -1,  0,   -7888,     24208 ],
		[ 2,  1,  0,  0,   -6766,     30824 ],
		[ 1,  0, -1,  0,   -5163,     -8379 ],
		[ 1,  1,  0,  0,    4987,    -16675 ],
		[ 2, -1,  1,  0,    4036,    -12831 ],
		[ 2,  0,  2,  0,    3994,    -10445 ],
		[ 4,  0,  0,  0,    3861,    -11650 ],
		[ 2,  0, -3,  0,    3665,     14403 ],
		[ 0,  1, -2,  0,   -2689,     -7003 ],
		[ 2,  0, -1,  2,   -2602,         0 ],
		[ 2, -1, -2,  0,    2390,     10056 ],
		[ 1,  0,  1,  0,   -2348,      6322 ],
		[ 2, -2,  0,  0,    2236,     -9884 ],
		[ 0,  1,  2,  0,   -2120,      5751 ],
		[ 0,  2,  0,  0,   -2069,         0 ],
		[ 2, -2, -1,  0,    2048,     -4950 ],
		[ 2,  0,  1, -2,   -1773,      4130 ],
		[ 2,  0,  0,  2,   -1595,         0 ],
		[ 4, -1, -1,  0,    1215,     -3958 ],
		[ 0,  0,  2,  2,   -1110,         0 ],
		[ 3,  0, -1,  0,    -892,      3258 ],
		[ 2,  1,  1,  0,    -810,      2616 ],
		[ 4, -1, -2,  0,     759,     -1897 ],
		[ 0,  2, -1,  0,    -713,     -2117 ],
		[ 2,  2, -1,  0,    -700,      2354 ],
		[ 2,  1, -2,  0,     691,         0 ],
		[ 2, -1,  0, -2,     596,         0 ],
		[ 4,  0,  1,  0,     549,     -1423 ],
		[ 0,  0,  4,  0,     537,     -1117 ],
		[ 4, -1,  0,  0,     520,     -1571 ],
		[ 1,  0, -2,  0,    -487,     -1739 ],
		[ 2,  1,  0, -2,    -399,         0 ],
		[ 0,  0,  2, -2,    -381,     -4421 ],
		[ 1,  1,  1,  0,     351,         0 ],
		[ 3,  0, -2,  0,    -340,         0 ],
		[ 4,  0, -3,  0,     330,         0 ],
		[ 2, -1,  2,  0,     327,         0 ],
		[ 0,  2,  1,  0,    -323,      1165 ],
		[ 1,  1, -1,  0,     299,         0 ],
		[ 2,  0,  3,  0,     294,         0 ],
		[ 2,  0, -1, -2,       0,      8752 ]
	];

	let lat = [
		[ 0,  0,  0,  1, 5128122 ],
		[ 0,  0,  1,  1,  280602 ],
		[ 0,  0,  1, -1,  277693 ],
		[ 2,  0,  0, -1,  173237 ],
		[ 2,  0, -1,  1,   55413 ],
		[ 2,  0, -1, -1,   46271 ],
		[ 2,  0,  0,  1,   32573 ],
		[ 0,  0,  2,  1,   17198 ],
		[ 2,  0,  1, -1,    9266 ],
		[ 0,  0,  2, -1,    8822 ],
		[ 2, -1,  0, -1,    8216 ],
		[ 2,  0, -2, -1,    4324 ],
		[ 2,  0,  1,  1,    4200 ],
		[ 2,  1,  0, -1,   -3359 ],
		[ 2, -1, -1,  1,    2463 ],
		[ 2, -1,  0,  1,    2211 ],
		[ 2, -1, -1, -1,    2065 ],
		[ 0,  1, -1, -1,   -1870 ],
		[ 4,  0, -1, -1,    1828 ],
		[ 0,  1,  0,  1,   -1794 ],
		[ 0,  0,  0,  3,   -1749 ],
		[ 0,  1, -1,  1,   -1565 ],
		[ 1,  0,  0,  1,   -1491 ],
		[ 0,  1,  1,  1,   -1475 ],
		[ 0,  1,  1, -1,   -1410 ],
		[ 0,  1,  0, -1,   -1344 ],
		[ 1,  0,  0, -1,   -1335 ],
		[ 0,  0,  3,  1,    1107 ],
		[ 4,  0,  0, -1,    1021 ],
		[ 4,  0, -1,  1,     833 ],
		[ 0,  0,  1, -3,     777 ],
		[ 4,  0, -2,  1,     671 ],
		[ 2,  0,  0, -3,     607 ],
		[ 2,  0,  2, -1,     596 ],
		[ 2, -1,  1, -1,     491 ],
		[ 2,  0, -2,  1,    -451 ],
		[ 0,  0,  3, -1,     439 ],
		[ 2,  0,  2,  1,     422 ],
		[ 2,  0, -3, -1,     421 ],
		[ 2,  1, -1,  1,    -366 ],
		[ 2,  1,  0,  1,    -351 ],
		[ 4,  0,  0,  1,     331 ],
		[ 2, -1,  1,  1,     315 ],
		[ 2, -2,  0, -1,     302 ],
		[ 0,  0,  1,  3,    -283 ],
		[ 2,  1,  1, -1,    -229 ],
		[ 1,  1,  0, -1,     223 ],
		[ 1,  1,  0,  1,     223 ],
		[ 0,  1, -2, -1,    -220 ],
		[ 2,  1, -1, -1,    -220 ],
		[ 1,  0,  1,  1,    -185 ],
		[ 2, -1, -2, -1,     181 ],
		[ 0,  1,  2,  1,    -177 ],
		[ 4,  0, -2, -1,     176 ],
		[ 4, -1, -1, -1,     166 ],
		[ 1,  0,  1, -1,    -164 ],
		[ 4,  0,  1, -1,     132 ],
		[ 1,  0, -1, -1,    -119 ],
		[ 4, -1,  0, -1,     115 ],
		[ 2, -2,  0,  1,     107 ]
	];

	// Reading periodic terms
	let fD, fD2, fM, fM2, fMm, fMm2, fF, fF2, coeffs, coeffs2, coeffc, f, f2, sumL = 0, sumR = 0, sumB = 0, x = 0;

	while (x < lat.length) {
		fD = ld[x][0];
		fM = ld[x][1];
		fMm = ld[x][2];
		fF = ld[x][3];
		coeffs = ld[x][4];
		coeffc = ld[x][5];
		if (fM === 1 || fM === -1) {
			f = fE;
		} else if (fM === 2 || fM === -2) {
			f = fE2;
		} else {
			f = 1;
		}
		sumL += f * coeffs * Utils.sind(fD * D + fM * Msm + fMm * Mmm + fF * F);
		sumR += f * coeffc * Utils.cosd(fD * D + fM * Msm + fMm * Mmm + fF * F);
		fD2 = lat[x][0];
		fM2 = lat[x][1];
		fMm2 = lat[x][2];
		fF2 = lat[x][3];
		coeffs2 = lat[x][4];
		if (fM2 === 1 || fM2 === -1) {
			f2 = fE;
		} else if (fM2 === 2 || fM2 === -2) {
			f2 = fE2;
		} else {
			f2 = 1;
		}
		sumB += f2 * coeffs2 * Utils.sind(fD2 * D + fM2 * Msm + fMm2 * Mmm + fF2 * F);
		x++;
	}

	// Corrections
	sumL = sumL + 3958 * Utils.sind(A1) + 1962 * Utils.sind(Lmm - F) + 318 * Utils.sind(A2);
	sumB = sumB - 2235 * Utils.sind(Lmm) + 382 * Utils.sind(A3) + 175 * Utils.sind(A1 - F) + 175 * Utils.sind(A1 + F) + 127 * Utils.sind(Lmm - Mmm) - 115 * Utils.sind(Lmm + Mmm);

	// Longitude of the moon
	let lambdaMm = Utils.norm360Deg(Lmm + sumL / 1000000);

	// Latitude of the moon
	let betaM = sumB / 1000000;

	// Distance earth-moon
	let dEM = 385000.56 + sumR / 1000;

	// Apparent longitude of the moon
	lambdaMapp = lambdaMm + deltaPsi;

	// Right ascension of the moon, apparent
	RAMoon = Math.toDegrees(Utils.norm2PiRad(Math.atan2((Utils.sind(lambdaMapp) * Utils.cosd(eps) - Utils.tand(betaM) * Utils.sind(eps)), Utils.cosd(lambdaMapp))));

	// Declination of the moon
	DECMoon = Math.toDegrees(Math.asin(Utils.sind(betaM) * Utils.cosd(eps) + Utils.cosd(betaM) * Utils.sind(eps) * Utils.sind(lambdaMapp)));

	// GHA of the moon
	GHAMoon = Utils.norm360Deg(GHAAtrue - RAMoon);

	// Horizontal parallax of the moon
	HPMoon = Math.toDegrees(3600 * Math.asin(6378.14 / dEM));

	// Semi-diameter of the moon
	SDMoon = Math.toDegrees(3600 * Math.asin(1738 / dEM));

	// Geocentric angular distance between moon and sun
	LDist = Math.toDegrees(Math.acos(Utils.sind(DECMoon) * Utils.sind(DECSun) + Utils.cosd(DECMoon) * Utils.cosd(DECSun) * Utils.cosd(RAMoon - RASun)));

	//Phase angle
	let i = Math.atan2(dES * Utils.sind(LDist), (dEM - dES * Utils.cosd(LDist)));

	//Illumination of the moon's disk
	let k = 100 * (1 + Math.cos(i)) / 2;
	illumMoon = k;    // Math.round(10 * k) / 10;
}

// Ephemerides of Polaris
function calculatePolaris() {
	// Equatorial coordinates of Polaris at 2000.0 (mean equinox and equator 2000.0)
	let RApol0 = 37.95293333;
	let DECpol0 = 89.26408889;

	// Proper motion per year
	let dRApol = 2.98155 / 3600;
	let dDECpol = -0.0152 / 3600;

	// Equatorial coordinates at Julian Date T (mean equinox and equator 2000.0)
	let RApol1 = RApol0 + 100 * TE * dRApol;
	let DECpol1 = DECpol0 + 100 * TE * dDECpol;

	// Mean obliquity of ecliptic at 2000.0 in degrees
	// const eps0_2000 = 23.439291111;

	// Transformation to ecliptic coordinates in radians (mean equinox and equator 2000.0)
	let lambdapol1 = Math.atan2((Utils.sind(RApol1) * Utils.cosd(EPS0_2000) + Utils.tand(DECpol1) * Utils.sind(EPS0_2000)), Utils.cosd(RApol1));
	let betapol1 = Math.asin(Utils.sind(DECpol1) * Utils.cosd(EPS0_2000) - Utils.cosd(DECpol1) * Utils.sind(EPS0_2000) * Utils.sind(RApol1));

	// Precession
	let eta =  Math.toRadians(47.0029 * TE - 0.03302 * TE2 + 0.00006 * TE3) / 3600;
	let PI0 =  Math.toRadians(174.876384 - (869.8089 * TE + 0.03536 * TE2) / 3600);
	let p0 =  Math.toRadians(5029.0966 * TE + 1.11113 * TE2 - 0.0000006 * TE3) / 3600;

	let A1 = Math.cos(eta) * Math.cos(betapol1) * Math.sin(PI0 - lambdapol1) - Math.sin(eta) * Math.sin(betapol1);
	let B1 = Math.cos(betapol1) * Math.cos(PI0 - lambdapol1);
	let C1 = Math.cos(eta) * Math.sin(betapol1) + Math.sin(eta) * Math.cos(betapol1) * Math.sin(PI0 - lambdapol1);
	let lambdapol2 = p0 + PI0 - Math.atan2(A1, B1);
	let betapol2 = Math.asin(C1);

	// calculateNutation in longitude
	lambdapol2 +=  Math.toRadians(deltaPsi);

	// aberration
	let dlambdapol = (e * kappa * Math.cos(pi0 - lambdapol2) - kappa * Math.cos( Math.toRadians(Lsun_true) - lambdapol2)) / Math.cos(betapol2);
	let dbetapol = -kappa * Math.sin(betapol2) * (Math.sin( Math.toRadians(Lsun_true) - lambdapol2) - e * Math.sin(pi0 - lambdapol2));

	lambdapol2 += dlambdapol;
	betapol2 += dbetapol;

	// Transformation back to equatorial coordinates in radians
	let RApol2 = Math.atan2((Math.sin(lambdapol2) * Utils.cosd(eps) - Math.tan(betapol2) * Utils.sind(eps)), Math.cos(lambdapol2));
	let DECpol2 = Math.asin(Math.sin(betapol2) * Utils.cosd(eps) + Math.cos(betapol2) * Utils.sind(eps) * Math.sin(lambdapol2));

	// Finals
	GHAPol = GHAAtrue - Math.toDegrees(RApol2);
	GHAPol = Utils.norm360Deg(GHAPol);
	RAPol = Math.toDegrees(RApol2);
	DECPol = Math.toDegrees(DECpol2);
}

// Calculation of the phase of the Moon
function calculateMoonPhase() {
	let x = lambdaMapp - lambdaSun;
	x = Utils.norm360Deg(x);
	x = Math.round(10 * x) / 10;
	moonPhaseAngle = x;
	if (x === 0) {
		moonPhase = " New";
	}
	if (x > 0 && x < 90) {
		moonPhase = " +cre";
	}
	if (x === 90) {
		moonPhase = " FQ";
	}
	if (x > 90 && x < 180) {
		moonPhase = " +gib";
	}
	if (x === 180) {
		moonPhase = " Full";
	}
	if (x > 180 && x < 270) {
		moonPhase = " -gib";
	}
	if (x === 270) {
		moonPhase = " LQ";
	}
	if (x > 270 && x < 360) {
		moonPhase = " -cre";
	}
}

// Day of the week
function calculateWeekDay() {
	JD0h += 1.5;
	let res = JD0h - 7 * Math.floor(JD0h / 7);
	if (res === 0) {
		DoW = "SUN";
	}
	if (res === 1) {
		DoW = "MON";
	}
	if (res === 2) {
		DoW = "TUE";
	}
	if (res === 3) {
		DoW = "WED";
	}
	if (res === 4) {
		DoW = "THU";
	}
	if (res === 5) {
		DoW = "FRI";
	}
	if (res === 6) {
		DoW = "SAT";
	}
}

/**
 * Y parameter (not year) for deltaT computing.
 *
 * @param year
 * @param month in [1..12]
 * @return
 */
function getY(year, month) {
	if (year < -1999 || year > 3000) {
		throw new Error("Year must be in [-1999, 3000]");
	} else {
		return (year + ((month - 0.5) / 12));
	}
}

/**
 * Since the usual (the ones I used to used) on-line resources are not always available, obsolete,
 * or expecting some serious revamping, here is a method to calculate deltaT out of thin air.
 *
 * See https://astronomy.stackexchange.com/questions/19172/obtaining-deltat-for-use-in-software
 * See values at https://eclipse.gsfc.nasa.gov/SEcat5/deltat.html#tab1 and
 *               https://eclipse.gsfc.nasa.gov/SEcat5/deltat.html#tab2
 *
 * @param year from -1999 to +3000
 * @param month in [1..12], NOT in [0..11]
 * @return
 */
export function calculateDeltaT(year, month) {
	if (year < -1999 || year > 3000) {
		throw new Error("Year must be in [-1999, 3000]");
	}
	if (month < 1 || month > 12) {
		throw new Error("Month must be in [1, 12]");
	}

	let deltaT = 0;
	let y = getY(year, month);

	if (year < -500) {
		let u = (y - 1820.0) / 100.0;
		deltaT = -20.0 + (32.0 * (u * u));
	} else if (year < 500) {
		let u = y / 100.0;
		deltaT = 10583.6
				+ (-1014.41 * u)
				+ (33.78311 * Math.pow(u, 2))
				+ (-5.952053 * Math.pow(u, 3))
				+ (-0.1798452 * Math.pow(u, 4))
				+ (0.022174192 * Math.pow(u, 5))
				+ (0.0090316521 * Math.pow(u, 6));
	} else if (year < 1600) {
		let u = (y - 1000.0) / 100.0;
		deltaT = 1574.2
				+ (-556.01 * u)
				+ (71.23472 * Math.pow(u, 2))
				+ (0.319781 * Math.pow(u, 3))
				+ (-0.8503463 * Math.pow(u, 4))
				+ (-0.005050998 * Math.pow(u, 5))
				+ (0.0083572073 * Math.pow(u, 6));
	} else if (year < 1700) {
		let t = y - 1600.0;
		deltaT = 120
				+ (-0.9808 * t)
				+ (-0.01532 * Math.pow(t, 2))
				+ (Math.pow(t, 3) / 7129);
	} else if (year < 1800) {
		let t = y - 1700.0;
		deltaT = 8.83
				+ 0.1603 * t
				+ (-0.0059285 * Math.pow(t, 2))
				+ (0.00013336 * Math.pow(t, 3))
				+ (Math.pow(t, 4) / -1174000);
	} else if (year < 1860) {
		let t = y - 1800.0;
		deltaT = 13.72
				+ (-0.332447 * t)
				+ (0.0068612 * Math.pow(t, 2))
				+ (0.0041116 * Math.pow(t, 3))
				+ (-0.00037436 * Math.pow(t, 4))
				+ (0.0000121272 * Math.pow(t, 5))
				+ (-0.0000001699 * Math.pow(t, 6))
				+ (0.000000000875 * Math.pow(t, 7));
	} else if (year < 1900) {
		let t = y - 1860.0;
		deltaT = 7.62 +
				(0.5737 * t)
				+ (-0.251754 * Math.pow(t, 2))
				+ (0.01680668 * Math.pow(t, 3))
				+ (-0.0004473624 * Math.pow(t, 4))
				+ (Math.pow(t, 5) / 233174);
	} else if (year < 1920) {
		let t = y - 1900;
		deltaT = -2.79
				+ (1.494119 * t)
				+ (-0.0598939 * Math.pow(t, 2))
				+ (0.0061966 * Math.pow(t, 3))
				+ (-0.000197 * Math.pow(t, 4));
	} else if (year < 1941) {
		let t = y - 1920;
		deltaT = 21.20
				+ (0.84493 * t)
				+ (-0.076100 * Math.pow(t, 2))
				+ (0.0020936 * Math.pow(t, 3));
	} else if (year < 1961) {
		let t = y - 1950;
		deltaT = 29.07
				+ (0.407 * t)
				+ (Math.pow(t, 2) / -233)
				+ (Math.pow(t, 3) / 2547);
	} else if (year < 1986) {
		let t = y - 1975;
		deltaT = 45.45
				+ (1.067 * t)
				+ (Math.pow(t, 2) / -260)
				+ (Math.pow(t, 3) / -718);
	} else if (year < 2005) {
		let t = y - 2000;
		deltaT = 63.86
				+ (0.3345 * t)
				+ (-0.060374 * Math.pow(t, 2))
				+ (0.0017275 * Math.pow(t, 3))
				+ (0.000651814 * Math.pow(t, 4))
				+ (0.00002373599 * Math.pow(t, 5));
	} else if (year < 2050) {
		let t = y - 2000;
		deltaT = 62.92
				+ (0.32217 * t)
				+ (0.005589 * Math.pow(t, 2));
	} else if (year < 2150) {
		deltaT = -20
				+ (32 * Math.pow((y - 1820) / 100, 2))
				+ (-0.5628 * (2150 - y));
	} else {
		let u = (y - 1820) / 100;
		deltaT = -20
				+ (32 * Math.pow(u, 2));
	}

	return deltaT;
}

function getStar(name) {
	for (let i=0; i<STAR_CATALOG.length; i++) {
		let s = STAR_CATALOG[i];
		if (s.name === name) {
			return s;
		}
	}
	return null;
}

function getStarPos(starName) {
	let star = getStar(starName);
	if (star !== null) {
		// Read catalog
		let RAstar0 = 15.0 * star.ra;
		let DECstar0 = star.dec;
		let dRAstar = 15.0 * star.deltaRA / 3600.0;
		let dDECstar = star.deltaD / 3600.0;
		let par = star.par / 3600.0;

		//Equatorial coordinates at Julian Date T (mean equinox and equator 2000.0)
		let RAstar1 = RAstar0 + TE * dRAstar;
		let DECstar1 = DECstar0 + TE * dDECstar;

		//Mean obliquity of ecliptic at 2000.0 in degrees
//    let eps0_2000 = 23.439291111;

		//Transformation to ecliptic coordinates in radians (mean equinox and equator 2000.0)
		let lambdastar1 = Math.atan2((Utils.sind(RAstar1) * Utils.cosd(EPS0_2000) + Utils.tand(DECstar1) * Utils.sind(EPS0_2000)), Utils.cosd(RAstar1));
		let betastar1 = Math.asin(Utils.sind(DECstar1) * Utils.cosd(EPS0_2000) - Utils.cosd(DECstar1) * Utils.sind(EPS0_2000) * Utils.sind(RAstar1));

		//Precession
		let eta = Math.toRadians(47.0029 * TE - 0.03302 * TE2 + 0.00006 * TE3) / 3600.0;
		let PI0 = Math.toRadians(174.876384 - (869.8089 * TE + 0.03536 * TE2) / 3600.0);
		let p0 = Math.toRadians(5029.0966 * TE + 1.11113 * TE2 - 0.0000006 * TE3) / 3600.0;
		let A1 = Math.cos(eta) * Math.cos(betastar1) * Math.sin(PI0 - lambdastar1) - Math.sin(eta) * Math.sin(betastar1);
		let B1 = Math.cos(betastar1) * Math.cos(PI0 - lambdastar1);
		let C1 = Math.cos(eta) * Math.sin(betastar1) + Math.sin(eta) * Math.cos(betastar1) * Math.sin(PI0 - lambdastar1);
		let lambdastar2 = p0 + PI0 - Math.atan2(A1, B1);
		let betastar2 = Math.asin(C1);

		//Annual parallax
		let par_lambda = Math.toRadians(par * Math.sin(Math.toRadians(Lsun_true) - lambdastar2) / Math.cos(betastar2));
		let par_beta = -Math.toRadians(par * Math.sin(betastar2) * Math.cos(Math.toRadians(Lsun_true) - lambdastar2));

		lambdastar2 += par_lambda;
		betastar2 += par_beta;

		// Nutation in longitude
		lambdastar2 += Math.toRadians(deltaPsi);

		// Aberration
//    let kappa = Math.toRadians(20.49552) / 3600.0;
//    let pi0 = Math.toRadians(102.93735 + 1.71953 * TE + 0.00046 * TE2);
//    let e = 0.016708617 - 0.000042037 * TE - 0.0000001236 * TE2;

		let dlambdastar = (e * kappa * Math.cos(pi0 - lambdastar2) - kappa * Math.cos(Math.toRadians(Lsun_true) - lambdastar2)) / Math.cos(betastar2);
		let dbetastar = -kappa * Math.sin(betastar2) * (Math.sin(Math.toRadians(Lsun_true) - lambdastar2) - e * Math.sin(pi0 - lambdastar2));

		lambdastar2 += dlambdastar;
		betastar2 += dbetastar;

		// Transformation back to equatorial coordinates in radians
		let RAstar2 = Math.atan2((Math.sin(lambdastar2) * Utils.cosd(eps) - Math.tan(betastar2) * Utils.sind(eps)), Math.cos(lambdastar2));
		let DECstar2 = Math.asin(Math.sin(betastar2) * Utils.cosd(eps) + Math.cos(betastar2) * Utils.sind(eps) * Math.sin(lambdastar2));

		// Lunar distance of star TODO Manage it.
		let starMoonDist = Math.toDegrees(Math.acos(Utils.sind(DECMoon) * Math.sin(DECstar2) + Utils.cosd(DECMoon) * Math.cos(DECstar2) * Utils.cosd(RAMoon - Math.toDegrees(RAstar2))));

		// Finals
		let GHAstar = GHAAtrue - Math.toDegrees(RAstar2);
		let SHAstar = 360 - Math.toDegrees(RAstar2);
		let DECstar = Math.toDegrees(DECstar2);
		return {
			gha: GHAstar,
			sha: SHAstar,
			dec: DECstar
		}
	} else {
		console.log(starName + " not found in the catalog...");
		return null;
	}
}

export function gridSquare(lat, lng) {
	return Utils.gridSquare(lat, lng);
}

export function sightReduction(lat, lng, ahg, dec) {
	return Utils.sightReduction(lat, lng, ahg, dec);
}

// Data output. Might be tweaked to fit user's needs.
function gatherOutput(noPlanets=false, withStars=false) {
	// Sun
	let fmtGHASun = outHA(GHASun);
	let fmtRASun = outRA(RASun);
	let fmtDECSun = outDec(DECSun);
	let fmtSDSun = outSdHp(SDSun);
	let fmtHPSun = outSdHp(HPSun);

	// Venus
	let fmtGHAVenus = noPlanets ? '' : outHA(GHAVenus);
	let fmtRAVenus = noPlanets ? '' : outRA(RAVenus);
	let fmtDECVenus = noPlanets ? '' : outDec(DECVenus);
	let fmtSDVenus = noPlanets ? '' : outSdHp(SDVenus);
	let fmtHPVenus = noPlanets ? '' : outSdHp(HPVenus);

	// Mars
	let fmtGHAMars = noPlanets ? '' : outHA(GHAMars);
	let fmtRAMars = noPlanets ? '' : outRA(RAMars);
	let fmtDECMars = noPlanets ? '' : outDec(DECMars);
	let fmtSDMars = noPlanets ? '' : outSdHp(SDMars);
	let fmtHPMars = noPlanets ? '' : outSdHp(HPMars);

	// Jupiter
	let fmtGHAJupiter = noPlanets ? '' : outHA(GHAJupiter);
	let fmtRAJupiter = noPlanets ? '' : outRA(RAJupiter);
	let fmtDECJupiter = noPlanets ? '' : outDec(DECJupiter);
	let fmtSDJupiter = noPlanets ? '' : outSdHp(SDJupiter);
	let fmtHPJupiter = noPlanets ? '' : outSdHp(HPJupiter);

	// Saturn
	let fmtGHASaturn = noPlanets ? '' : outHA(GHASaturn);
	let fmtRASaturn = noPlanets ? '' : outRA(RASaturn);
	let fmtDECSaturn = noPlanets ? '' : outDec(DECSaturn);
	let fmtSDSaturn = noPlanets ? '' : outSdHp(SDSaturn);
	let fmtHPSaturn = noPlanets ? '' : outSdHp(HPSaturn);

	// Polaris
	let fmtGHAPolaris = noPlanets ? '' : outHA(GHAPol);
	let fmtRAPolaris = noPlanets ? '' : outRA(RAPol);
	let fmtDECPolaris = noPlanets ? '' : outDec(DECPol);

	// Moon
	let fmtGHAMoon = outHA(GHAMoon);
	let fmtRAMoon = outRA(RAMoon);
	let fmtDECMoon = outDec(DECMoon);
	let fmtSDMoon = outSdHp(SDMoon);
	let fmtHPMoon = outSdHp(HPMoon);

	// Obliquity of Ecliptic
	OoE = outECL(eps0);
	tOoE = outECL(eps);

	// Equation of time
	fmtEoT = outEoT(EoT);

	// Lunar Distance of Sun
	let fmtLDist = outHA(LDist);

	let outForm = {};

	// "epoch": 1589036261000,
	outForm.epoch = epoch;

	outForm.deltaT = deltaT;

    let aries = {}; // Aries RA = 0, by definition.
    aries.GHA = {
        raw: GHAAtrue,
        fmt: outHA(GHAAtrue)
    };
    outForm.aries = aries;

	let sun = {};
	sun.GHA = {
		raw: GHASun,
		fmt: fmtGHASun
	};
	sun.RA = {
		raw: RASun,
		fmt: fmtRASun
	};
	sun.DEC = {
		raw: DECSun,
		fmt: fmtDECSun
	};
	sun.SD = {
		raw: SDSun,
		fmt: fmtSDSun
	};
	sun.HP = {
		raw: HPSun,
		fmt: fmtHPSun
	};
	outForm.sun = sun;

	outForm.EOT = {
		raw: EoT,
		fmt: fmtEoT
	};

	let moon = {};
	moon.GHA = {
		raw: GHAMoon,
		fmt: fmtGHAMoon
	};
	moon.RA = {
		raw: RAMoon,
		fmt: fmtRAMoon
	};
	moon.DEC = {
		raw: DECMoon,
		fmt: fmtDECMoon
	};
	moon.SD = {
		raw: SDMoon,
		fmt: fmtSDMoon
	};
	moon.HP = {
		raw: HPMoon,
		fmt: fmtHPMoon
	};
	moon.illum = illumMoon;
	moon.phase = {
		phase: moonPhase,
		phaseAngle: moonPhaseAngle
	};
	outForm.moon = moon;

	if (!noPlanets) {
		let venus = {};
		venus.GHA = {
			raw: GHAVenus,
			fmt: fmtGHAVenus
		};
		venus.RA = {
			raw: RAVenus,
			fmt: fmtRAVenus
		};
		venus.DEC = {
			raw: DECVenus,
			fmt: fmtDECVenus
		};
		venus.SD = {
			raw: SDVenus,
			fmt: fmtSDVenus
		};
		venus.HP = {
			raw: HPVenus,
			fmt: fmtHPVenus
		};
		venus.illum = illumVenus;
		outForm.venus = venus;

		let mars = {};
		mars.GHA = {
			raw: GHAMars,
			fmt: fmtGHAMars
		};
		mars.RA = {
			raw: RAMars,
			fmt: fmtRAMars
		};
		mars.DEC = {
			raw: DECMars,
			fmt: fmtDECMars
		};
		mars.SD = {
			raw: SDMars,
			fmt: fmtSDMars
		};
		mars.HP = {
			raw: HPMars,
			fmt: fmtHPMars
		};
		mars.illum = illumMars;
		outForm.mars = mars;

		let jupiter = {};
		jupiter.GHA = {
			raw: GHAJupiter,
			fmt: fmtGHAJupiter
		};
		jupiter.RA = {
			raw: RAJupiter,
			fmt: fmtRAJupiter
		};
		jupiter.DEC = {
			raw: DECJupiter,
			fmt: fmtDECJupiter
		};
		jupiter.SD = {
			raw: SDJupiter,
			fmt: fmtSDJupiter
		};
		jupiter.HP = {
			raw: HPJupiter,
			fmt: fmtHPJupiter
		};
		jupiter.illum = illumJupiter;
		outForm.jupiter = jupiter;

		let saturn = {};
		saturn.GHA = {
			raw: GHASaturn,
			fmt: fmtGHASaturn
		};
		saturn.RA = {
			raw: RASaturn,
			fmt: fmtRASaturn
		};
		saturn.DEC = {
			raw: DECSaturn,
			fmt: fmtDECSaturn
		};
		saturn.SD = {
			raw: SDSaturn,
			fmt: fmtSDSaturn
		};
		saturn.HP = {
			raw: HPSaturn,
			fmt: fmtHPSaturn
		};
		saturn.illum = illumSaturn;
		outForm.saturn = saturn;

		let polaris = {};
		polaris.GHA = {
			raw: GHAPol,
			fmt: fmtGHAPolaris
		};
		polaris.RA = {
			raw: RAPol,
			fmt: fmtRAPolaris
		};
		polaris.DEC = {
			raw: DECPol,
			fmt: fmtDECPolaris
		};
		outForm.polaris = polaris;
	}

	outForm.sidTmean = {
		raw: GHAAmean,
		fmt: SidTm
	};
	outForm.sidTapp = {
		raw: GHAAtrue,
		fmt: SidTa
	};
	outForm.EoEquin = EoEout;
	outForm.dPsi =  Math.round(3600000 * deltaPsi) / 1000; // + "''";
	outForm.dEps = Math.round(3600000 * deltaEps) / 1000;  // + "''";
	outForm.obliq = {
		raw: eps0,
		fmt: OoE
	};
	outForm.trueObliq = {
		raw: eps,
		fmt: tOoE
	};
	outForm.julianDay = Math.round(1000000 * JD) / 1000000;
	outForm.julianEphemDay = Math.round(1000000 * JDE) / 1000000;
	outForm.lunarDist = {
		raw: LDist,
		fmt: fmtLDist
	};
	outForm.dayOfWeek = DoW;

	if (withStars) {
	    let stars = [];
		STAR_CATALOG.forEach(star => {
			let starPos = getStarPos(star.name);
			if (starPos !== null) {
			    stars.push({
			        name: star.name,
					decl: starPos.dec,
					gha: starPos.gha
			    });
			}
		});
		outForm.stars = stars;
	}

	return outForm;
}

/**
 * 
 * @param {float} lat 
 * @param {float} lng 
 * @param {float} sunEoT 
 * @returns decimal hours, UTC.
 */
 export function getSunMeridianPassageTime(latitude, longitude, sunEoT) {
	let t = (12.0 - (sunEoT / 60.0));
	let deltaG = longitude / 15.0;
	return t - deltaG;
};

/**
 * 
 * @param {float} lat 
 * @param {float} lng 
 * @param {float} sunEoT 
 * @param {int} year
 * @param {int} month [1..12]
 * @param {int} day
 * @param {int} hour
 * @param {int} minute
 * @param {int} second
 * @param {float} delta_t
 * @returns The solar date and time (NO time-zone)
 */
 export function getSolarDateAtPos(latitude, longitude, sunEoT, year, month, day, hour, minute, second, delta_t) {
	let eotInHours = getSunMeridianPassageTime(latitude, longitude, sunEoT);

	let currentDate = new Date(`${year}-${lpad(month, '0', 2)}-${lpad(day, '0', 2)} ${lpad(hour, '0', 2)}:${lpad(minute, '0', 2)}:${lpad(second, '0', 2)} GMT+0000`);

	let ms = currentDate.getTime();
	let solar = new Date(ms + Math.round((12 - eotInHours) * 3600000));
	return {
		year: solar.getUTCFullYear(),
		month: solar.getUTCMonth() + 1,
		day: solar.getUTCDate(),
		hour: solar.getUTCHours(),
		minute: solar.getUTCMinutes(),
		second: solar.getUTCSeconds()
	};
};

/**
 * 
 * @param {float} decimalHours 
 * @returns a JSON object like { hours: {float}, minutes: {float}, seconds: {float} }
 */
export function decimalToDMS(decimalHours) {
	let hours = Math.floor(decimalHours);
	let min = (decimalHours - hours) * 60;
	let sec = (min - Math.floor(min)) * 60;
	return {
		hours: hours,
		minutes: Math.floor(min),
		seconds: sec };
};

function lpad(str, pad, len) {
    while (str.length < len) {
        str = pad + str;
    }
    return str;
}

export function sunRiseAndSetEpoch(delta_t, year, month, day, latitude, longitude, decSun, hpSun, sdSun, sunEoT) {
	let h0 = (hpSun / 3600) - (sdSun / 3600); // - (34d / 60d);
	let cost = Utils.sind(h0) - Utils.tand(latitude) * Utils.tand(decSun);
	let t = Math.acos(cost);
	let lon = longitude;

	let utRise = 12.0 - (sunEoT / 60.0) - (lon / 15.0) - (Math.toDegrees(t) / 15.0);
	let utSet  = 12.0 - (sunEoT / 60.0) - (lon / 15.0) + (Math.toDegrees(t) / 15.0);

	let Z = Math.acos((Utils.sind(decSun) + (0.0145 * Utils.sind(latitude))) / (0.9999 * Utils.cosd(latitude)));
	Z = Math.toDegrees(Z);

	let zRise = Z;
	let zSet = (360.0 - Z);

	// Sun rise
	let dms = decimalToDMS(utRise);
	// let rise = new Date(`${year}-${lpad(month, '0', 2)}-${lpad(day, '0', 2)} ${lpad(dms.hours, '0', 2)}:${lpad(dms.minutes, '0', 2)}:${lpad(Math.floor(dms.seconds), '0', 2)} GMT+0000`);
	let rise = new Date(`${year}-${lpad(month, '0', 2)}-${lpad(day, '0', 2)} 00:00:00 GMT+0000`);
	let riseEpoch = rise.getTime() + (dms.hours * 3600 * 1000) + (dms.minutes * 60 * 1000) + (dms.seconds * 1000);
	rise.setTime(riseEpoch);
	// epoch: rise.getTime();
	// let resultForRise = calculate(year, month, day, dms.hours, dms.minutes, dms.seconds, delta_t, true, false);
	let resultForRise = calculate(rise.getUTCFullYear(), rise.getUTCMonth() + 1, rise.getUTCDate(), rise.getUTCHours(), rise.getUTCMinutes(), rise.getUTCSeconds(), delta_t, true, false);
	// Fine tuning
	// console.log(`Before fine tune: Sun GHA: ${resultForRise.sun.GHA.raw}, DEC: ${resultForRise.sun.DEC.raw}`);
	let sr = Utils.sightReduction(latitude, longitude, resultForRise.sun.GHA.raw, resultForRise.sun.DEC.raw);

	if (sr.alt !== 0) { // if elevation (altitude) not 0, then adjust
		while (sr.alt > 0) {
			
			// console.log(`From ${rise}, alt ${sr.alt}`);

			let _epoch = rise.getTime() - (10 * 1000);
			rise.setTime(_epoch);
			
			// console.log(`Decreasing date to ${rise}`);
			// console.log(`Set as: ${rise.getUTCFullYear()}, ${rise.getUTCMonth() + 1}, ${rise.getUTCDate()}, ${rise.getUTCHours()}, ${rise.getUTCMinutes()}, ${rise.getUTCSeconds()}`);

			resultForRise = calculate(rise.getUTCFullYear(), 
									  rise.getUTCMonth() + 1, 
			                          rise.getUTCDate(), 
									  rise.getUTCHours(), 
									  rise.getUTCMinutes(), 
									  rise.getUTCSeconds(), 
									  delta_t, true, false);
			// console.log(`Fine tuning (1): Sun GHA: ${resultForRise.sun.GHA.raw}, DEC: ${resultForRise.sun.DEC.raw}`);
			sr = Utils.sightReduction(latitude, longitude, resultForRise.sun.GHA.raw, resultForRise.sun.DEC.raw);
		}
		// Starting tuning
		while (sr.alt < 0) {
			
			// console.log(`From ${rise}, alt ${sr.alt}`);

			let _epoch = rise.getTime() + (1 * 1000);
			rise.setTime(_epoch);
			
			// console.log(`Increasing date to ${rise}`);
			// console.log(`Set as: ${rise.getUTCFullYear()}, ${rise.getUTCMonth() + 1}, ${rise.getUTCDate()}, ${rise.getUTCHours()}, ${rise.getUTCMinutes()}, ${rise.getUTCSeconds()}`);

			resultForRise = calculate(rise.getUTCFullYear(), 
									  rise.getUTCMonth() + 1, 
			                          rise.getUTCDate(), 
									  rise.getUTCHours(), 
									  rise.getUTCMinutes(), 
									  rise.getUTCSeconds(), 
									  delta_t, true, false);

			// console.log(`Fine tuning (2): Sun GHA: ${resultForRise.sun.GHA.raw}, DEC: ${resultForRise.sun.DEC.raw}`);
			sr = Utils.sightReduction(latitude, longitude, resultForRise.sun.GHA.raw, resultForRise.sun.DEC.raw);

			// console.log(`Now From ${rise}, alt ${sr.alt}`);
		}
		zRise = sr.Z;

		// console.log(`Z: ${sr.Z}, elev: ${sr.alt}`)
	}

	// Sun set
	dms = decimalToDMS(utSet);
	// let set = new Date(`${year}-${lpad(month, '0', 2)}-${lpad(day, '0', 2)} ${lpad(dms.hours, '0', 2)}:${lpad(dms.minutes, '0', 2)}:${lpad(Math.floor(dms.seconds), '0', 2)} GMT+0000`);
	let set = new Date(`${year}-${lpad(month, '0', 2)}-${lpad(day, '0', 2)} 00:00:00 GMT+0000`);
	let setEpoch = rise.getTime() + (dms.hours * 3600 * 1000) + (dms.minutes * 60 * 1000) + (dms.seconds * 1000);
	set.setTime(setEpoch);
	// epoch: set.getTime();
	// let resultForSet = calculate(year, month, day, dms.hours, dms.minutes, dms.seconds, delta_t, true, false);
	let resultForSet = calculate(set.getUTCFullYear(), set.getUTCMonth() + 1, set.getUTCDate(), set.getUTCHours(), set.getUTCMinutes(), set.getUTCSeconds(), delta_t, true, false);

	// Fine tuning
	// console.log(`Before fine tune: Sun GHA: ${resultForSet.sun.GHA.raw}, DEC: ${resultForSet.sun.DEC.raw}`);
	sr = Utils.sightReduction(latitude, longitude, resultForSet.sun.GHA.raw, resultForSet.sun.DEC.raw);

	if (sr.alt !== 0) { // Elevation not 0, then adjust
		while (sr.alt < 0) {
			
			// console.log(`From ${set}, alt ${sr.alt}`);

			let _epoch = set.getTime() - (10 * 1000);
			set.setTime(_epoch);
			
			// console.log(`Decreasing date to ${set}`);
			// console.log(`Set as: ${set.getUTCFullYear()}, ${set.getUTCMonth() + 1}, ${set.getUTCDate()}, ${set.getUTCHours()}, ${set.getUTCMinutes()}, ${set.getUTCSeconds()}`);

			resultForSet = calculate(set.getUTCFullYear(), 
									 set.getUTCMonth() + 1, 
									 set.getUTCDate(), 
									 set.getUTCHours(), 
									 set.getUTCMinutes(), 
									 set.getUTCSeconds(), 
									 delta_t, true, false);
			// console.log(`Fine tuning (1): Sun GHA: ${resultForSet.sun.GHA.raw}, DEC: ${resultForSet.sun.DEC.raw}`);
			sr = Utils.sightReduction(latitude, longitude, resultForSet.sun.GHA.raw, resultForSet.sun.DEC.raw);
		}
		// Starting tuning
		while (sr.alt > 0) {
			
			// console.log(`From ${set}, alt ${sr.alt}`);

			let _epoch = set.getTime() + (1 * 1000);
			set.setTime(_epoch);
			
			// console.log(`Increasing date to ${set}`);
			// console.log(`Set as: ${set.getUTCFullYear()}, ${set.getUTCMonth() + 1}, ${set.getUTCDate()}, ${set.getUTCHours()}, ${set.getUTCMinutes()}, ${set.getUTCSeconds()}`);

			resultForSet = calculate(set.getUTCFullYear(), 
									 set.getUTCMonth() + 1, 
			                         set.getUTCDate(), 
									 set.getUTCHours(), 
									 set.getUTCMinutes(), 
									 set.getUTCSeconds(), 
									 delta_t, true, false);

			// console.log(`Fine tuning (2): Sun GHA: ${resultForSet.sun.GHA.raw}, DEC: ${resultForSet.sun.DEC.raw}`);
			sr = Utils.sightReduction(latitude, longitude, resultForSet.sun.GHA.raw, resultForSet.sun.DEC.raw);

			// console.log(`Now From ${set}, alt ${sr.alt}`);
		}
		zSet = sr.Z;
		// console.log(`Z: ${sr.Z}, elev: ${sr.alt}`)
	}

	return {
		rise: {
			epoch: rise.getTime(),
			z: zRise
		}, set: {
			epoch: set.getTime(),
			z: zSet
		}
	};
};

/**
 * Sun Data for given date and observer's position
 * @param {float} delta_t 
 * @param {string} duration Duration Format of current date-time
 * @param {float} latitude 
 * @param {float} longitude 
 * @param {float} epoch 
 * @param {float} decSun 
 * @param {float} ghaSun 
 * @param {float} hpSun 
 * @param {float} sdSun 
 * @param {float} sunEoT 
 * @returns { see below... }
 */
export function getSunDataForDate(delta_t, 
								  duration, 
								  latitude, 
								  longitude, 
								  epoch,
								  decSun, 
								  ghaSun,
								  hpSun, 
								  sdSun, 
								  sunEoT) {

	let parsed = Utils.parseDuration(duration);
	// let time = new Date(`${parsed.year}-${lpad(parsed.month, '0', 2)}-${lpad(parsed.day, '0', 2)} ${lpad(parsed.hour, '0', 2)}:${lpad(parsed.minute, '0', 2)}:${lpad(parseFloat(parsed.second).toFixed(0), '0', 2)} GMT+0000`); // TODO Z->GMT
	// let epoch = time.getTime();

	// Calculate Sight Reduction, Sun Transit, rise, set (epoch and Z)
	let sr = Utils.sightReduction(latitude, longitude, ghaSun, decSun);
	let tt = getSunMeridianPassageTime(latitude, longitude, sunEoT);
	let dms = decimalToDMS(tt);
	let transitTime = new Date(`${parsed.year}-${lpad(parsed.month, '0', 2)}-${lpad(parsed.day, '0', 2)} ${lpad(dms.hours, '0', 2)}:${lpad(dms.minutes, '0', 2)}:${lpad(dms.seconds.toFixed(0), '0', 2)} GMT+0000`); // TODO Z->GMT
	let transitEpoch = transitTime.getTime();

	let sunRiseAndSet = sunRiseAndSetEpoch(delta_t, parsed.year, parsed.month, parsed.day, latitude, longitude, decSun, hpSun, sdSun, sunEoT);

	let bodyData = {
		epoch: epoch,
		lat: latitude,
		lng: longitude,
		body: "Sun",
		decl: decSun,
		gha: ghaSun,
		elev: sr.alt,
		z: sr.Z,
		eot: sunEoT,
		riseTime: sunRiseAndSet.rise.epoch,
		setTime: sunRiseAndSet.set.epoch, 		
		sunTransitTime: transitEpoch,  // epoch
		riseZ: sunRiseAndSet.rise.z,
		setZ:  sunRiseAndSet.set.z
	};
	return bodyData;
};

/**
 * 
 * @param {obj} bodyData returned by getSunDataForDate
 * @param {float} delta_t 
 * @param {float} latitude 
 * @param {float} longitude 
 * @param {number} step in minutes (default 10 used if null)
 * @param {number} refDate epoch
 * @returns [ { epoch: {number}, he: {number}, z: {number} } ]
 */
export function getSunDataForAllDay(bodyData, delta_t, latitude, longitude, step, refDate) {
	if (bodyData === null) {
		throw { error: 'bodyData should not be null' };
	}
	let from = bodyData.riseTime; // if sunTransitTime exists: from (transit-time - 12) to (transit-time + 12) ?
	let to = bodyData.setTime;

	if (bodyData.sunTransitTime !== 0) { // Parameter for the full path, or just positive elevations?
		from = bodyData.sunTransitTime - (12 * 3_600_000);
		to = bodyData.sunTransitTime + (12 * 3_600_000);
	}
	const _STEP_MINUTES = 1_000 * 60 * (step == null ? 10 : step); // In ms. Default 10 minutes.
	let sunPath = [];

	// Now calculate the Sun Path
	for (let time=from; time<=to; time += _STEP_MINUTES) {
		let current = new Date(time);
		let currentResult = calculate(current.getUTCFullYear(), 
									  current.getUTCMonth() + 1, 
									  current.getUTCDate(), 
									  current.getUTCHours(), 
									  current.getUTCMinutes(), 
									  current.getUTCSeconds(), 
									  delta_t, true, false);

		let sr = Utils.sightReduction(latitude, longitude, currentResult.sun.GHA.raw, currentResult.sun.DEC.raw);
		sunPath.push({ epoch: time, he: sr.alt, z: sr.Z });
	}
	return sunPath;
};

/*
exports.calculate = calculate;
exports.isLeapYear = isLeapYear;
*/
