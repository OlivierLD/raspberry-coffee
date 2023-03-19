/**
 *
 * Misc utilities used all over the place.
 */

function rgbToHex(r, g, b) {
	return "#" + ((1 << 24) + (r << 16) + (g << 8) + b).toString(16).slice(1);
}

function hexToRgb(hex) {
	let result = /^#?([a-f\d]{2})([a-f\d]{2})([a-f\d]{2})$/i.exec(hex);
	if (result) {
		let r = parseInt(result[1], 16);
		let g = parseInt(result[2], 16);
		let b = parseInt(result[3], 16);
		return `${r},${g},${b}`; //return 23,14,45 -> reformat if needed
	}
	return null;
}
// console.log(rgbToHex(10, 54, 120)); //#0a3678
// console.log(hexToRgb("#0a3678"));//"10,54,120"

export function divideTransparencyBy(color, factor) {
	let transparency = 1.0;
	if (color.startsWith("#")) {
		return `rgba(${hexToRgb(color)}, ${transparency / factor})`;
	} else if (color.startsWith("rgb(")) {
		let values = color.trim().substring('rgb('.length, color.trim().indexOf(')'));
		return `rgba(${values}, ${transparency / factor})`;
	} else if (color.startsWith("rgba(")) {
		let values = color.trim().substring('rgb('.length, color.trim().indexOf(')'));
		let valueArray = values.split(",");
		transparency = parseFloat(valueArray[3]);
		return `rgba(${valueArray[0].trim()}, ${valueArray[1].trim()}, ${valueArray[2].trim()}, ${transparency / factor})`;
	} else {
		return null; // Cannot find color values...
	}
}

export function lpad(str, len, pad) {
	let s = str;
	while (s.length < len) {
		s = (pad === undefined ? ' ' : pad) + s;
	}
	return s;
}

export function rpad(str, len, pad) {
	let s = str;
	while (s.length < len) {
		s += (pad === undefined ? ' ' : pad);
	}
	return s;
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

/**
 *
 * @param start { lat: xx, lng: xx }, L & G in Degrees
 * @param dist distance in nm
 * @param bearing route in Degrees
 * @return DR Position, L & G in Degrees
 */
export function deadReckoning(start, dist, bearing) {
	let radianDistance = Math.toRadians(dist / 60);
	let finalLat = (Math.asin((Math.sin(Math.toRadians(start.lat)) * Math.cos(radianDistance)) +
			(Math.cos(Math.toRadians(start.lat)) * Math.sin(radianDistance) * Math.cos(Math.toRadians(bearing)))));
	let finalLng = Math.toRadians(start.lng) + Math.atan2(Math.sin(Math.toRadians(bearing)) * Math.sin(radianDistance) * Math.cos(Math.toRadians(start.lat)),
			Math.cos(radianDistance) - Math.sin(Math.toRadians(start.lat)) * Math.sin(finalLat));
	finalLat = Math.toDegrees(finalLat);
	finalLng = Math.toDegrees(finalLng);

	return {lat: finalLat, lng: finalLng};
}

const TO_NORTH = 0;
const TO_SOUTH = 1;
const TO_EAST = 2;
const TO_WEST = 3;

/**
 * All in Radians
 *
 * @param start
 * @param arrival
 * @param nbPoints
 * @returns {Array}
 */
export function calculateGreatCircle(start, arrival, nbPoints) {
	let deltaG = arrival.lng - start.lng;
	let route = [];
	let interval = deltaG / nbPoints;
	let smallStart = { lat: start.lat, lng: start.lng };

	for (let g = start.lng; route.length <= nbPoints; g += interval) {
		let deltag = arrival.lng - g;
		let tanStartAngle = Math.sin(deltag) / (Math.cos(smallStart.lat) * Math.tan(arrival.lat) - Math.sin(smallStart.lat) * Math.cos(deltag));
		let smallL = Math.atan(Math.tan(smallStart.lat) * Math.cos(interval) + Math.sin(interval) / (tanStartAngle * Math.cos(smallStart.lat)));
		let rpG = g + interval;
		if (rpG > Math.PI) {
			rpG -= (2 * Math.PI);
		}
		if (rpG < -Math.PI) {
			rpG = (2 * Math.PI) + rpG;
		}
		let routePoint = { lat: smallL, lng: rpG };
		let ari = Math.toDegrees(Math.atan(tanStartAngle));
		if (ari < 0.0) {
			ari = Math.abs(ari);
		}
		var _nsDir;
		if (routePoint.lat > smallStart.lat) {
			_nsDir = TO_NORTH;
		} else {
			_nsDir = TO_SOUTH;
		}
		let arrG = routePoint.lng;
		let staG = smallStart.lng;
		if (Math.sign(arrG) !== Math.sign(staG)) {
			if (Math.sign(arrG) > 0) {
				arrG -= (2 * Math.PI);
			} else {
				arrG = Math.PI - arrG;
			}
		}
		var _ewDir;
		if (arrG > staG) {
			_ewDir = TO_EAST;
		} else {
			_ewDir = TO_WEST;
		}
		let _start = 0.0;
		if (_nsDir === TO_SOUTH) {
			_start = 180;
			if (_ewDir === TO_EAST) {
				ari = _start - ari;
			} else {
				ari = _start + ari;
			}
		} else {
			if (_ewDir === TO_EAST) {
				ari = _start + ari;
			} else {
				ari = _start - ari;
			}
		}
		while (ari < 0.0) {
			ari += 360;
		}
		route.push({ pos: smallStart, z: (arrival === smallStart) ? null : ari });
		smallStart = routePoint;
	}
	return route;
}

export function calculateGreatCircleInDegrees(start, arrival, nbPoints) {
	let radRoute = calculateGreatCircle(
			{ lat: Math.toRadians(start.lat), lng: Math.toRadians(start.lng) },
			{ lat: Math.toRadians(arrival.lat), lng: Math.toRadians(arrival.lng) },
			nbPoints);
	let degRoute = [];
	radRoute.forEach(pt => {
		degRoute.push({ pos: { lat: Math.toDegrees(pt.pos.lat), lng: Math.toDegrees(pt.pos.lng) }, z: pt.z });
	});
	return degRoute;
}

export function decToSex(val, ns_ew) {
	let absVal = Math.abs(val);
	let intValue = Math.floor(absVal);
	let dec = absVal - intValue;
	let i = intValue;
	dec *= 60;
//    let s = i + "°" + dec.toFixed(2) + "'";
//    let s = i + String.fromCharCode(176) + dec.toFixed(2) + "'";
	let s = "";
	if (ns_ew !== undefined) {
		if (val < 0) {
			s += (ns_ew === 'NS' ? 'S' : 'W');
		} else {
			s += (ns_ew === 'NS' ? 'N' : 'E');
		}
		s += " ";
	} else {
		if (val < 0) {
			s += '-'
		}
	}
	s += i + "°" + dec.toFixed(2) + "'";

	return s;
}

/**
 * Warning: this one can add 180 to the direction. (See below)
 * @param x
 * @param y
 * @return direction, [0..360[
 */
export function getDir(x, y) {
	let direction = 180 + Math.toDegrees(Math.atan2(x, y));
	while (direction < 0) {
		direction += 360;
	}
	direction %= 360;
	return direction;
}
