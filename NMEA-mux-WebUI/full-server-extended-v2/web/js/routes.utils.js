/**
 * @author Olivier LeDiouris
 */
var GreatCircle = function (from, to) {

	var dirs = {
		TO_NORTH: 0,
		TO_SOUTH: 1,
		TO_EAST:  2,
		TO_WEST:  3
	};

	var ewDir, nsDir;

	this.start = from;
	this.arrival = to;

	var route = [];

	/**
	 *
	 * @param pt { let: L, lng: G } angles in radians.
	 */
	this.setStart = function (pt) {
		this.start = pt;
	};
	this.setStartInDegrees = function (pt) {
		this.setStart({lat: toRadians(pt.lat), lng: toRadians(pt.lng)});
	};

	/**
	 *
	 * @param pt { let: L, lng: G } angles in radians.
	 */
	this.setArrival = function (pt) {
		this.arrival = pt;
	};
	this.setArrivalInDegrees = function (pt) {
		this.setArrival({lat: toRadians(pt.lat), lng: toRadians(pt.lng)});
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
		var deltaG = this.arrival.lng - this.start.lng;
		route = [];
		var pt = this.start;
		var interval = deltaG / nbPts; // TODO Make sure deltaG > 0
		for (var g = this.start.lng; route.length <= nbPts; g += interval) {
			var deltag = this.arrival.lng - g;
			var tgStartAngle = Math.sin(deltag) / (Math.cos(pt.lat) * Math.tan(this.arrival.lat) - Math.sin(pt.lat) * Math.cos(deltag));
			var ptL = Math.atan(Math.tan(pt.lat) * Math.cos(interval) + Math.sin(interval) / (tgStartAngle * Math.cos(pt.lat)));
			var ptG = g + interval;
			if (ptG > Math.PI) {
				ptG -= (2 * Math.PI);
			}
			if (ptG < -Math.PI) {
				ptG += (2 * Math.PI);
			}
			var routePt = {lat: ptL, lng: ptG};
			var ari = Math.abs(toDegrees(Math.atan(tgStartAngle)));
			var _nsDir = (routePt.lat > pt.lat) ? dirs.TO_NORTH : dirs.TO_SOUTH;
			var arrG = routePt.lng;
			var staG = pt.lng;
			if (sign(arrG) !== sign(staG)) {
				if (sign(arrG) > 0) {
					arrG -= (2 * Math.PI);
				} else {
					arrG = Math.PI - arrG;
				}
			}
			var _ewDir = (arrG > staG) ? dirs.TO_EAST : dirs.TO_WEST;
			var _start = 0;
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
var toRadians = function (deg) {
	return deg * (Math.PI / 180);
};

var toDegrees = function (rad) {
	return rad * (180 / Math.PI);
};

var sign = function (d) {
	var s = 0;
	if (d > 0.0) {
		s = 1;
	}
	if (d < 0.0) {
		s = -1;
	}
	return s;
};

var getGCDistance = function (from, to) {
	if (from === undefined || to === undefined) {
		throw ({err: "From and To are required"});
	}
	var cos = Math.sin(from.lat) * Math.sin(to.lat) + Math.cos(from.lat) * Math.cos(to.lat) * Math.cos(to.lng - from.lng);
	return Math.acos(cos);
};

var getGCDistanceInDegrees = function (from, to) {
	return toDegrees(getGCDistance(from, to));
};

var getGCDistanceInNM = function (from, to) {
	return (getGCDistanceInDegrees(from, to) * 60);
};

/**
 * Rhumbline aka loxodrome
 *
 * Points coordinates in Radians
 * returned value in radians
 */
var calculateRhumLine = function (from, to) {
	var nsDir = (to.lat > from.lat) ? dirs.TO_NORTH : dirs.TO_SOUTH;
	var arrG = to.lng;
	var staG = from.lng;
	if (sign(arrG) !== sign(staG) && Math.abs(arrG - staG) > Math.PI) {
		if (sign(arrG) > 0) {
			arrG -= (2 * Math.PI);
		} else {
			arrG = Math.PI - arrG;
		}
	}
	var ewDir = (arrG - staG > 0.0) ? dirs.TO_EAST : dirs.TO_WEST;
	var deltaL = toDegrees((to.lat - from.lat)) * 60;
	var radianDeltaG = to.lng - from.lng;
	if (Math.abs(radianDeltaG) > Math.PI) {
		radianDeltaG = (2 * Math.PI) - Math.abs(radianDeltaG);
	}
	var deltaG = Math.abs(toDegrees(radianDeltaG) * 60);
	var startLC = Math.log(Math.tan((Math.PI / 4) + from.lat / 2));
	var arrLC = Math.log(Math.tan((Math.PI / 4) + to.lat / 2));
	var deltaLC = 3437.7467707849396 * (arrLC - startLC);
	var rv;
	if (deltaLC !== 0) {
		rv = Math.atan(deltaG / deltaLC);
	} else if (radianDeltaG > 0) {
		rv = (Math.PI / 2);
	} else {
		rv = (3 * Math.PI / 2);
	}
	var dLoxo;
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
};

/**
 *
 * @param from GeoPoint, L & G in Radians
 * @param dist distance in nm
 * @param route route in degrees
 * @return DR Position, L & G in Radians
 */
var deadReckoning = function (from, dist, route) {
	var radianDistance = toRadians(dist / 60);
	var finalLat = (Math.asin((Math.sin(from.lat) * Math.cos(radianDistance)) +
								            (Math.cos(from.lat) * Math.sin(radianDistance) * Math.cos(toRadians(route)))));
	var finalLng = from.lng + Math.atan2(Math.sin(toRadians(route)) * Math.sin(radianDistance) * Math.cos(from.lat),
																			 Math.cos(radianDistance) - Math.sin(from.lat) * Math.sin(finalLat));
	return ({lat: finalLat, lng: finalLng});
};

var toDegreePt = function(pt) {
	return { lat: toDegrees(pt.lat), lng: toDegrees(pt.lng) };
};

var decToSex = function (val, ns_ew) {
	var absVal = Math.abs(val);
	var intValue = Math.floor(absVal);
	var dec = absVal - intValue;
	var i = intValue;
	dec *= 60;
//    var s = i + "°" + dec.toFixed(2) + "'";
//    var s = i + String.fromCharCode(176) + dec.toFixed(2) + "'";
	var s = "";
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
};

/**
 * Get the direction
 *
 * @param x horizontal displacement
 * @param y vertical displacement
 * @return the angle, in degrees
 */
var getDir = function(x, y) {
	var dir = 0.0;
	if (y !== 0) {
		dir = toDegrees(Math.atan(x / y));
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
};

if (false) {
// Main for tests
	var from = {lat: toRadians(45), lng: toRadians(-130)};
	var dist = 55;
	var heading = 270;
	var dr = deadReckoning(from, dist, heading);
	console.log("Starting from ", decToSex(toDegrees(from.lat), 'NS'), decToSex(toDegrees(from.lng), 'EW'), "heading", heading + "\272", "for", dist, "miles");
	console.log('Reaching ', decToSex(toDegrees(dr.lat), 'NS'), decToSex(toDegrees(dr.lng), 'EW'));

	console.log("\nGC Test");
// 37 38', -122 46'
	from = {lat: toRadians(37.63333), lng: toRadians(-122.76666)};
	to = {lat: toRadians(20), lng: toRadians(-150)};
	console.log("Starting from ", decToSex(toDegrees(from.lat), 'NS'), decToSex(toDegrees(from.lng), 'EW'));
	console.log("Going to      ", decToSex(toDegrees(to.lat), 'NS'), decToSex(toDegrees(to.lng), 'EW'));

	var gc = new GreatCircle(from, to);
	var route = gc.calculateGreatCircle(20);
	route.forEach(function (pt) {
		console.log("Z", pt.ari.toFixed(1), ", ", decToSex(toDegrees(pt.pt.lat), "NS"), decToSex(toDegrees(pt.pt.lng), "EW"));
	});
	console.log("GC distance", gc.getDistanceInNM().toFixed(0), "nm");

	console.log("\nRhumbline test");
	var loxo = calculateRhumLine(from, to);
	console.log("Heading", toDegrees(loxo.heading).toFixed(0) + '\272', "Dist:", loxo.dist.toFixed(1), "nm");
}
