/**
 *
 * Misc utilities used all over the place.
 */
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

// TODO See Math.atan2
export function getDir(x, y) {
	let dir = 0.0;
	if (y !== 0) {
		dir = Math.toDegrees(Math.atan(x / y));
	}
	if (x <= 0 || y <= 0) {
		if (x > 0 && y < 0) {
			dir += 180;
		} else if (x < 0 && y > 0) {
			dir += 360;
		} else if (x < 0 && y < 0) {
			dir += 180;
		} else if (x === 0) {
			if (y > 0) {
				dir = 0.0;
			} else {
				dir = 180;
			}
		} else if (y === 0) {
			if (x > 0) {
				dir = 90;
			} else {
				dir = 270;
			}
		}
	}
	dir += 180;
	while (dir >= 360) {
		dir -= 360;
	}
	return dir;
}
