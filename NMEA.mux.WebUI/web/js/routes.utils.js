// "use strict";
/**
 * @author Olivier LeDiouris
 */
let dirs = {
	TO_NORTH: 0,
	TO_SOUTH: 1,
	TO_EAST:  2,
	TO_WEST:  3
};

let GreatCircle = function (from, to) {

	let ewDir, nsDir;

	this.start = from;
	this.arrival = to;

	let route = [];

	/**
	 *
	 * @param pt { let: L, lng: G } angles in radians.
	 */
	this.setStart = function (pt) {
		this.start = pt;
	};
	this.setStartInDegrees = function (pt) {
		this.setStart({lat: Math.toRadians(pt.lat), lng: Math.toRadians(pt.lng)});
	};

	/**
	 *
	 * @param pt { let: L, lng: G } angles in radians.
	 */
	this.setArrival = function (pt) {
		this.arrival = pt;
	};
	this.setArrivalInDegrees = function (pt) {
		this.setArrival({lat: Math.toRadians(pt.lat), lng: Math.toRadians(pt.lng)});
	};

	this.calculateGreatCircle = function (nbPts) {
		// Make sure start and arrival are set.
		if (this.start === undefined || this.arrival === undefined) {
			throw ({err: "Start and Arrival are required"});
		}
		nsDir = (this.arrival.lat > this.start.lat) ? dirs.TO_NORTH : dirs.TO_SOUTH;
		ewDir = (this.arrival.lng > this.start.lng) ? dirs.TO_EAST : dirs.TO_WEST;

		if (Math.abs(this.arrival.lng - this.start.lng) > Math.PI) {
			if (ewDir === dirs.TO_EAST) {
				ewDir = dirs.TO_WEST;
				this.arrival.lng -= (2 * Math.PI);
			} else {
				ewDir = dirs.TO_EAST;
				this.arrival.lng += (2 * Math.PI);
			}
		}
		let deltaG = this.arrival.lng - this.start.lng;
		route = [];
		let pt = this.start;
		let interval = deltaG / nbPts; // TODO Make sure deltaG > 0
		for (let g = this.start.lng; route.length <= nbPts; g += interval) {
			let deltag = this.arrival.lng - g;
			let tgStartAngle = Math.sin(deltag) / (Math.cos(pt.lat) * Math.tan(this.arrival.lat) - Math.sin(pt.lat) * Math.cos(deltag));
			let ptL = Math.atan(Math.tan(pt.lat) * Math.cos(interval) + Math.sin(interval) / (tgStartAngle * Math.cos(pt.lat)));
			let ptG = g + interval;
			if (ptG > Math.PI) {
				ptG -= (2 * Math.PI);
			}
			if (ptG < -Math.PI) {
				ptG += (2 * Math.PI);
			}
			let routePt = {lat: ptL, lng: ptG};
			let ari = Math.abs(Math.toDegrees(Math.atan(tgStartAngle)));
			let _nsDir = (routePt.lat > pt.lat) ? dirs.TO_NORTH : dirs.TO_SOUTH;
			let arrG = routePt.lng;
			let staG = pt.lng;
			if (Math.sign(arrG) !== Math.sign(staG)) {
				if (Math.sign(arrG) > 0) {
					arrG -= (2 * Math.PI);
				} else {
					arrG = Math.PI - arrG;
				}
			}
			let _ewDir = (arrG > staG) ? dirs.TO_EAST : dirs.TO_WEST;
			let _start = 0;
			if (_nsDir === dirs.TO_SOUTH) {
				_start = 180;
				if (_ewDir === dirs.TO_EAST) {
					ari = _start - ari;
				} else {
					ari = _start + ari;
				}
			} else {
				if (_ewDir == dirs.TO_EAST) {
					ari = _start + ari;
				} else {
					ari = _start - ari;
				}
			}
			while (ari < 0.0) ari += 360;

			route.push({pt: pt, ari: (this.arrival.lat === pt.lat && this.arrival.lng === pt.lng) ? null : ari});
			pt = routePt;
		}
		return route;
	};

	/**
	 * GreatCircle (orthodromic) distance
	 * @return in radians
	 */
	this.getDistance = function () {
		if (start === undefined || arrival === undefined) {
			throw ({err: "Start and Arrival are required"});
		}
		return getGCDistance(this.start, this.arrival);
	};

	this.getDistanceInDegrees = function () {
		return getGCDistanceInDegrees(this.start, this.arrival);
	};

	this.getDistanceInNM = function () {
		return getGCDistanceInNM(this.start, this.arrival);
	};

};

/* Static Utils */
if (Math.toRadians === undefined) {
	Math.toRadians = (deg) => {
		return deg * (Math.PI / 180);
	}
}

if (Math.toDegrees === undefined) {
	Math.toDegrees = (rad) => {
		return rad * (180 / Math.PI);
	}
}

if (Math.sign === undefined) {
	Math.sign = (d) => {
		let s = 0;
		if (d > 0.0) {
			s = 1;
		}
		if (d < 0.0) {
			s = -1;
		}
		return s;
	}
}

function getGCDistance(from, to) {
	if (from === undefined || to === undefined) {
		throw ({err: "From and To are required"});
	}
	let cos = Math.sin(from.lat) * Math.sin(to.lat) + Math.cos(from.lat) * Math.cos(to.lat) * Math.cos(to.lng - from.lng);
	return Math.acos(cos);
}

function getGCDistanceInDegrees(from, to) {
	return Math.toDegrees(getGCDistance(from, to));
};

let getGCDistanceInNM = function (from, to) {
	return (getGCDistanceInDegrees(from, to) * 60);
};

/**
 * Rhumbline aka loxodrome
 *
 * Points coordinates in Radians
 * returned value in radians
 */
function calculateRhumLine(from, to) {
	let nsDir = (to.lat > from.lat) ? dirs.TO_NORTH : dirs.TO_SOUTH;
	let arrG = to.lng;
	let staG = from.lng;
	if (Math.sign(arrG) !== Math.sign(staG) && Math.abs(arrG - staG) > Math.PI) {
		if (Math.sign(arrG) > 0) {
			arrG -= (2 * Math.PI);
		} else {
			arrG = Math.PI - arrG;
		}
	}
	let ewDir = (arrG - staG > 0.0) ? dirs.TO_EAST : dirs.TO_WEST;
	let deltaL = Math.toDegrees((to.lat - from.lat)) * 60;
	let radianDeltaG = to.lng - from.lng;
	if (Math.abs(radianDeltaG) > Math.PI) {
		radianDeltaG = (2 * Math.PI) - Math.abs(radianDeltaG);
	}
	let deltaG = Math.abs(Math.toDegrees(radianDeltaG) * 60);
	let startLC = Math.log(Math.tan((Math.PI / 4) + from.lat / 2));
	let arrLC = Math.log(Math.tan((Math.PI / 4) + to.lat / 2));
	let deltaLC = 3437.7467707849396 * (arrLC - startLC);
	let rv;
	if (deltaLC !== 0) {
		rv = Math.atan(deltaG / deltaLC);
	} else if (radianDeltaG > 0) {
		rv = (Math.PI / 2);
	} else {
		rv = (3 * Math.PI / 2);
	}
	let dLoxo;
	if (deltaL !== 0) {
		dLoxo = deltaL / Math.cos(rv);
	} else {
		dLoxo = deltaG * Math.cos(from.lat); // TASK Make sure that's right...
	}
	dLoxo = Math.abs(dLoxo);
	rv = Math.abs(rv);
	if (ewDir === dirs.TO_EAST) {
		if (nsDir !== dirs.TO_NORTH) {
			rv = Math.PI - rv;
		}
	} else if (deltaLC !== 0) {
		if (nsDir === dirs.TO_NORTH) {
			rv = (2 * Math.PI) - rv;
		} else {
			rv = Math.PI + rv;
		}
	}
	while (rv >= (2 * Math.PI)) rv -= (2 * Math.PI);
	return ({heading: rv, dist: dLoxo});
}

/**
 *
 * @param from GeoPoint, L & G in Radians
 * @param dist distance in nm
 * @param route route in degrees
 * @return DR Position, L & G in Radians
 */
function deadReckoning(from, dist, route) {
	let radianDistance = Math.toRadians(dist / 60);
	let finalLat = (Math.asin((Math.sin(from.lat) * Math.cos(radianDistance)) +
								            (Math.cos(from.lat) * Math.sin(radianDistance) * Math.cos(Math.toRadians(route)))));
	let finalLng = from.lng + Math.atan2(Math.sin(Math.toRadians(route)) * Math.sin(radianDistance) * Math.cos(from.lat),
																			 Math.cos(radianDistance) - Math.sin(from.lat) * Math.sin(finalLat));
	return ({lat: finalLat, lng: finalLng});
}

function toDegreePt(pt) {
	return { lat: Math.toDegrees(pt.lat), lng: Math.toDegrees(pt.lng) };
}

function decToSex(val, ns_ew) {
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
	s += i + "\272" + dec.toFixed(2) + "'";

	return s;
}

/**
 * Get the direction
 *
 * @param x horizontal displacement
 * @param y vertical displacement
 * @return the angle, in degrees
 */
function getDir(x, y) {
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
	while (dir >= 360) dir -= 360;
	return dir;
}

if (false) {
// Main for tests
	let from = {lat: Math.toRadians(45), lng: Math.toRadians(-130)};
	let dist = 55;
	let heading = 270;
	let dr = deadReckoning(from, dist, heading);
	console.log("Starting from ", decToSex(Math.toDegrees(from.lat), 'NS'), decToSex(Math.toDegrees(from.lng), 'EW'), "heading", heading + "\272", "for", dist, "miles");
	console.log('Reaching ', decToSex(Math.toDegrees(dr.lat), 'NS'), decToSex(Math.toDegrees(dr.lng), 'EW'));

	console.log("\nGC Test");
// 37 38', -122 46'
	from = {lat: Math.toRadians(37.63333), lng: Math.toRadians(-122.76666)};
	let to = {lat: Math.toRadians(20), lng: Math.toRadians(-150)};
	console.log("Starting from ", decToSex(Math.toDegrees(from.lat), 'NS'), decToSex(Math.toDegrees(from.lng), 'EW'));
	console.log("Going to      ", decToSex(Math.toDegrees(to.lat), 'NS'), decToSex(Math.toDegrees(to.lng), 'EW'));

	let gc = new GreatCircle(from, to);
	let route = gc.calculateGreatCircle(20);
	route.forEach(function (pt) {
		console.log("Z", pt.ari.toFixed(1), ", ", decToSex(Math.toDegrees(pt.pt.lat), "NS"), decToSex(Math.toDegrees(pt.pt.lng), "EW"));
	});
	console.log("GC distance", gc.getDistanceInNM().toFixed(0), "nm");

	console.log("\nRhumbline test");
	let loxo = calculateRhumLine(from, to);
	console.log("Heading", Math.toDegrees(loxo.heading).toFixed(0) + '\272', "Dist:", loxo.dist.toFixed(1), "nm");
}
