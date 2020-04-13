/*
 *  Navigation Helper - Triangulation, and all this kind of things...
 *
 *  Inputs come from the NMEA Station:
 *
 *  BSP (aka STW), HDG (true, magnetic, or compass, usually compass), AWS, AWA, SOG, COG
 *  With
 *  - BSP Coeff
 *  - AWS Coeff
 *  - HDG Offset
 *  - AWA Offset
 *
 *  External input, estimated or what not:
 *  - Max Leeway - From the navigator.
 *  - Declination (possibly from RMC), deviation, from the dev curve
 */

if (Math.toRadians === undefined) {
	Math.toRadians = function(deg) {
		return deg * (Math.PI / 180);
	};
}

if (Math.toDegrees === undefined) {
	Math.toDegrees = function(rad) {
		return rad * (180 / Math.PI);
	};
}

let NavigationHelper = {

	twCalculator: function (
			aws, awsCoeff,
			awa, awaOffset,
			hdg, hdgOffset,
			sog, cog) {
		let twa = 0, tws = -1, twd = 0;
		try {
			// Warning, the MHU is carried by the boat, that has the HDG...
			// Only if the boat is moving (i.e. SOG > 0)
			let diffCogHdg = 0;
			if (sog > 0) {
				diffCogHdg = (cog - (hdg + hdgOffset));
				while (diffCogHdg < 0) {
					diffCogHdg += 360;
				}
				if (diffCogHdg > 180) {
					diffCogHdg -= 360;
				}
			}
			let awaOnCOG = (awa + awaOffset) - diffCogHdg;
			let d = ((aws * awsCoeff) * Math.cos(Math.toRadians(awaOnCOG))) - (sog);
			let h = ((aws * awsCoeff) * Math.sin(Math.toRadians(awaOnCOG)));
			tws = Math.sqrt((d * d) + (h * h));
			let twaOnCOG = Math.toDegrees(Math.acos(d / tws));
			if (Math.abs(awaOnCOG) > 180 || awaOnCOG < 0) {
				twaOnCOG = 360 - twaOnCOG;
			}
			if (sog > 0) {
				twd = cog + twaOnCOG;
			} else {
				twd = hdg + twaOnCOG;
			}
			while (twd > 360) {
				twd -= 360;
			}
			while (twd < 0) {
				twd += 360;
			}
			twa = twaOnCOG + diffCogHdg;
			if (twa > 180) {
				twa -= 360;
			}
			//    console.log("DiffCOG-HDG:" + diffCogHdg + ", AWA on COG:" + awaOnCOG + ", TWAonCOG:" + twaOnCOG);
		} catch (oops) {
			console.log(oops);
		}
		return {'twa': twa, 'tws': tws, 'twd': twd};
	},

	currentCalculator: function (
			bsp, bspCoeff,
			hdg, hdgOffset,
			leeway,
			sog, cog) {
		let cdr = 0, csp = 0;

		let rsX = ((bsp * bspCoeff) * Math.sin(Math.toRadians((hdg + hdgOffset) + leeway)));
		let rsY = -((bsp * bspCoeff) * Math.cos(Math.toRadians((hdg + hdgOffset) + leeway)));

		let rfX = (sog * Math.sin(Math.toRadians(cog)));
		let rfY = -(sog * Math.cos(Math.toRadians(cog)));
		let a = (rsX - rfX);
		let b = (rfY - rsY);
		csp = Math.sqrt((a * a) + (b * b));
		cdr = this.directionFinder(a, b);

		return {'cdr': cdr, 'csp': csp};
	},

	directionFinder: function(x, y) {
		let dir = 0.0;
		if (y != 0) {
			dir = Math.toDegrees(Math.atan(x / y));
		}
		if (x <= 0 || y <= 0) {
			if (x > 0 && y < 0) {
				dir += 180;
			} else if (x < 0 && y > 0) {
				dir += 360;
			} else if (x < 0 && y < 0) {
				dir += 180;
			} else if (x == 0) {
				if (y > 0) {
					dir = 0.0;
				} else {
					dir = 180;
				}
			} else if (y == 0) {
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
	},

	leewayEvaluator: function (awa, maxLeeway) {
		let _awa = awa;
		if (_awa < 0) {
			_awa += 360;
		}
		let leeway = 0;
		if (_awa < 90 || _awa > 270) {
			let leewayAngle = maxLeeway * Math.cos(Math.toRadians(awa));
			if (_awa < 90) {
				leewayAngle = -leewayAngle;
			}
			leeway = leewayAngle;
		}
//console.log("For AWA:" + awa + ", leeway:" + leeway);
		return leeway;
	},

	variationCalculator: function (D, d) {
		return D + d;
	},

	hdgFromHdc: function (hdc, D, d) {
		return (hdc + this.variationCalculator(D, d));
	},

	vmgCalulator: function(sog, cog, twd, twa, bsp, hdg, b2wp) {
		let vmgWind = null;
		let vmgWayPoint = null;
		try {
			if (sog != null && cog != null && twd != null) {
				let twa = twd - cog;
				if (sog > 0) { // Try with GPS Data first
					vmgWind = sog * Math.cos(Math.toRadians(twa));
				} else {
					try {
						if (bsp > 0) {
							vmgWind = bsp * Math.cos(Math.toRadians(twa));
						}
					} catch (error) {
						vmgWind = 0;
					}
				}
				if (b2wp != null) {
					if (sog > 0) {
						let angle = b2wp - cog;
						vmgWayPoint = sog * Math.cos(Math.toRadians(angle));
					} else {
						angle = b2wp - hdg;
						vmgWayPoint = bsp * Math.cos(Math.toRadians(angle));
					}
				}
			}
		} catch (error) {
			console.log(error);
		}
		return { 'vmgWind': vmgWind, 'vmgWayPoint': vmgWayPoint };
	}
};
