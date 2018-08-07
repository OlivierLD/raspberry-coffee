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
