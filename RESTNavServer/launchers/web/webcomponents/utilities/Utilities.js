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

export function toRadians(deg) {
	return deg * (Math.PI / 180);
}

export function toDegrees(rad) {
	return rad * (180 / Math.PI);
}

/**
 *
 * @param from GeoPoint, L & G in Degrees
 * @param dist distance in nm
 * @param route route in Degrees
 * @return DR Position, L & G in Degrees
 */
export function deadReckoning(start, dist, bearing) {
	let radianDistance = toRadians(dist / 60);
	let finalLat = (Math.asin((Math.sin(toRadians(start.lat)) * Math.cos(radianDistance)) +
			(Math.cos(toRadians(start.lat)) * Math.sin(radianDistance) * Math.cos(toRadians(bearing)))));
	let finalLng = toRadians(start.lng) + Math.atan2(Math.sin(toRadians(bearing)) * Math.sin(radianDistance) * Math.cos(toRadians(start.lat)),
			Math.cos(radianDistance) - Math.sin(toRadians(start.lat)) * Math.sin(finalLat));
	finalLat = toDegrees(finalLat);
	finalLng = toDegrees(finalLng);

	return {lat: finalLat, lng: finalLng};
}

export function decToSex(val, ns_ew) {
	let absVal = Math.abs(val);
	let intValue = Math.floor(absVal);
	let dec = absVal - intValue;
	let i = intValue;
	dec *= 60;
//    var s = i + "°" + dec.toFixed(2) + "'";
//    var s = i + String.fromCharCode(176) + dec.toFixed(2) + "'";
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
