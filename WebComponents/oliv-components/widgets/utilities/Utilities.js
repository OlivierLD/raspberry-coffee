/**
 *
 * Misc utilities used all over the place.
 */
export function lpad(str, len, pad) {
	let s = str;
	while (s.length < len) {
		s = pad + s;
	}
	return s;
}

export function toRadians(deg) {
	return deg * (Math.PI / 180);
}

export function toDegrees(rad) {
	return rad * (180 / Math.PI);
}
