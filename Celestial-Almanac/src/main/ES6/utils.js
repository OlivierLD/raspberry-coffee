"use strict";


// Sine of angles in degrees
export function sind(x) {
	return Math.sin(Math.toRadians(x));
}

// Cosine of angles in degrees
export function cosd(x) {
	return Math.cos(Math.toRadians(x));
}

// Tangent of angles in degrees
export function tand(x) {
	return Math.tan(Math.toRadians(x));
}

// Normalize large angles
// Degrees
export function norm360Deg(x) {
	while (x < 0) {
		x += 360;
	}
	return x % 360;
}

// Radians
export function norm2PiRad(x) {
	while (x < 0) {
		x += (2 * Math.PI);
	}
	return x % (2 * Math.PI);
}

// Cosine of normalized angle (in radians)
export function cost(x) {
	return Math.cos(norm2PiRad(x));
}

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

/*
exports.sind = sind;
exports.cosd = cosd;
exports.tand = tand;
exports.norm360Deg = norm360Deg;
exports.norm2PiRad = norm2PiRad;
exports.cost = cost;
*/