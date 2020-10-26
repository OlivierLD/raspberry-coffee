// Math has no prototype.
if (Math.toRadians === undefined) {
	Math.toRadians = (deg) => {
		return Math.PI * deg / 180;
	};
}

if (Math.toDegrees === undefined) {
	Math.toDegrees = (rad) => {
		return rad * 180 / Math.PI;
	};
}

/**
 * Computes the Increasing Latitude. Mercator formula.
 * @param lat in degrees
 * @return Increasing Latitude, in degrees.
 */
let getIncLat = (lat) => {
	let il = Math.log(Math.tan((Math.PI / 4) + (Math.toRadians(lat) / 2)));
	return Math.toDegrees(il);
};

let getInvIncLat = (il) => {
	let ret = Math.toRadians(il);
	ret = Math.exp(ret);
	ret = Math.atan(ret);
	ret -= (Math.PI / 4); // 0.78539816339744828D;
	ret *= 2;
	ret = Math.toDegrees(ret);
	return ret;
};

/**
 *
 * @param fromL start latitude, in degrees
 * @param fromG start longitude in degrees
 * @param heading heading
 * @param dist distance in nm
 * @returns { lat: L, lng: G }
 */
let deadReckoning = (fromL, fromG, heading, dist) => {
	let deltaL = (dist / 60) * Math.cos(Math.toRadians(heading));
	let l2 = fromL + deltaL;
	let lc1 = getIncLat(fromL);
	let lc2 = getIncLat(l2);
	let deltaLc = lc2 - lc1;
	let deltaG = deltaLc * Math.tan(Math.toRadians(heading));
	let g2 = fromG + deltaG;
	return { lat: l2, lng: g2 };
};

// Ratio on *one* degree, that is the trick.
let getIncLatRatio = (lat) => {
	if (lat === 0) {
		return 1;
	} else {
		let bottom = lat - 1;
		if (bottom < 0) {
			bottom = 0;
		}
		return ((lat - bottom) / (getIncLat(lat) - getIncLat(bottom)));
	}
};

let calculateEastG = (nLat, sLat, wLong, canvasW, canvasH) => {
	let deltaIncLat =  getIncLat(nLat) - getIncLat(sLat);

	let graphicRatio = canvasW / canvasH;
	let deltaG = Math.min(deltaIncLat * graphicRatio, 359);
	let eLong = wLong + deltaG;

	while (eLong > 180) {
		eLong -= 360;
	}
	return eLong;
};

// Main for tests
if (false) {
	let d = getIncLat(45);
	console.log("IncLat(45)=" + d);
	console.log("Rad(45)=" + Math.toRadians(45));

	console.log("IncLat(60)=" + getIncLat(60));
	console.log("Ratio at L=60:" + getIncLatRatio(60));

	console.log("-----------------------");
	for (let i = 0; i <= 90; i += 10) {
		console.log("Ratio at " + i + "=" + getIncLatRatio(i));
	}

	console.log("IncLat(90)=" + getIncLat(90));
}