package calc.calculation.nauticalalmanac;

public class Moon {
	public static void compute() {
		// Mean longitude of the moon
		double Lmm = Utils.trunc(218.3164591 + 481267.88134236 * Context.TE - 0.0013268 * Context.TE2 + Context.TE3 / 538841 - Context.TE4 / 65194000);

		//Mean elongation of the moon
		double D = Utils.trunc(297.8502042 + 445267.1115168 * Context.TE - 0.00163 * Context.TE2 + Context.TE3 / 545868 - Context.TE4 / 113065000);

		// Mean anomaly of the sun
		double Msm = Utils.trunc(357.5291092 + 35999.0502909 * Context.TE - 0.0001536 * Context.TE2 + Context.TE3 / 24490000);

		//Mean anomaly of the moon
		double Mmm = Utils.trunc(134.9634114 + 477198.8676313 * Context.TE + 0.008997 * Context.TE2 + Context.TE3 / 69699 - Context.TE4 / 14712000);

		//Mean distance of the moon from ascending node
		double F = Utils.trunc(93.2720993 + 483202.0175273 * Context.TE - 0.0034029 * Context.TE2 - Context.TE3 / 3526000 + Context.TE4 / 863310000);

		//Corrections
		double A1 = Utils.trunc(119.75 + 131.849 * Context.TE);
		double A2 = Utils.trunc(53.09 + 479264.29 * Context.TE);
		double A3 = Utils.trunc(313.45 + 481266.484 * Context.TE);
		double fE = 1 - 0.002516 * Context.TE - 0.0000074 * Context.TE2;
		double fE2 = fE * fE;

		//Periodic terms for the moon:
		//Longitude and distance
		double[][] ld = {
						new double[]{0, 0, 1, 0, 6288774, -20905355},
						new double[]{2, 0, -1, 0, 1274027, -3699111},
						new double[]{2, 0, 0, 0, 658314, -2955968},
						new double[]{0, 0, 2, 0, 213618, -569925},
						new double[]{0, 1, 0, 0, -185116, 48888},
						new double[]{0, 0, 0, 2, -114332, -3149},
						new double[]{2, 0, -2, 0, 58793, 246158},
						new double[]{2, -1, -1, 0, 57066, -152138},
						new double[]{2, 0, 1, 0, 53322, -170733},
						new double[]{2, -1, 0, 0, 45758, -204586},
						new double[]{0, 1, -1, 0, -40923, -129620},
						new double[]{1, 0, 0, 0, -34720, 108743},
						new double[]{0, 1, 1, 0, -30383, 104755},
						new double[]{2, 0, 0, -2, 15327, 10321},
						new double[]{0, 0, 1, 2, -12528, 0},
						new double[]{0, 0, 1, -2, 10980, 79661},
						new double[]{4, 0, -1, 0, 10675, -34782},
						new double[]{0, 0, 3, 0, 10034, -23210},
						new double[]{4, 0, -2, 0, 8548, -21636},
						new double[]{2, 1, -1, 0, -7888, 24208},
						new double[]{2, 1, 0, 0, -6766, 30824},
						new double[]{1, 0, -1, 0, -5163, -8379},
						new double[]{1, 1, 0, 0, 4987, -16675},
						new double[]{2, -1, 1, 0, 4036, -12831},
						new double[]{2, 0, 2, 0, 3994, -10445},
						new double[]{4, 0, 0, 0, 3861, -11650},
						new double[]{2, 0, -3, 0, 3665, 14403},
						new double[]{0, 1, -2, 0, -2689, -7003},
						new double[]{2, 0, -1, 2, -2602, 0},
						new double[]{2, -1, -2, 0, 2390, 10056},
						new double[]{1, 0, 1, 0, -2348, 6322},
						new double[]{2, -2, 0, 0, 2236, -9884},
						new double[]{0, 1, 2, 0, -2120, 5751},
						new double[]{0, 2, 0, 0, -2069, 0},
						new double[]{2, -2, -1, 0, 2048, -4950},
						new double[]{2, 0, 1, -2, -1773, 4130},
						new double[]{2, 0, 0, 2, -1595, 0},
						new double[]{4, -1, -1, 0, 1215, -3958},
						new double[]{0, 0, 2, 2, -1110, 0},
						new double[]{3, 0, -1, 0, -892, 3258},
						new double[]{2, 1, 1, 0, -810, 2616},
						new double[]{4, -1, -2, 0, 759, -1897},
						new double[]{0, 2, -1, 0, -713, -2117},
						new double[]{2, 2, -1, 0, -700, 2354},
						new double[]{2, 1, -2, 0, 691, 0},
						new double[]{2, -1, 0, -2, 596, 0},
						new double[]{4, 0, 1, 0, 549, -1423},
						new double[]{0, 0, 4, 0, 537, -1117},
						new double[]{4, -1, 0, 0, 520, -1571},
						new double[]{1, 0, -2, 0, -487, -1739},
						new double[]{2, 1, 0, -2, -399, 0},
						new double[]{0, 0, 2, -2, -381, -4421},
						new double[]{1, 1, 1, 0, 351, 0},
						new double[]{3, 0, -2, 0, -340, 0},
						new double[]{4, 0, -3, 0, 330, 0},
						new double[]{2, -1, 2, 0, 327, 0},
						new double[]{0, 2, 1, 0, -323, 1165},
						new double[]{1, 1, -1, 0, 299, 0},
						new double[]{2, 0, 3, 0, 294, 0},
						new double[]{2, 0, -1, -2, 0, 8752}
				};

		double[][] lat = {
						new double[]{0, 0, 0, 1, 5128122},
						new double[]{0, 0, 1, 1, 280602},
						new double[]{0, 0, 1, -1, 277693},
						new double[]{2, 0, 0, -1, 173237},
						new double[]{2, 0, -1, 1, 55413},
						new double[]{2, 0, -1, -1, 46271},
						new double[]{2, 0, 0, 1, 32573},
						new double[]{0, 0, 2, 1, 17198},
						new double[]{2, 0, 1, -1, 9266},
						new double[]{0, 0, 2, -1, 8822},
						new double[]{2, -1, 0, -1, 8216},
						new double[]{2, 0, -2, -1, 4324},
						new double[]{2, 0, 1, 1, 4200},
						new double[]{2, 1, 0, -1, -3359},
						new double[]{2, -1, -1, 1, 2463},
						new double[]{2, -1, 0, 1, 2211},
						new double[]{2, -1, -1, -1, 2065},
						new double[]{0, 1, -1, -1, -1870},
						new double[]{4, 0, -1, -1, 1828},
						new double[]{0, 1, 0, 1, -1794},
						new double[]{0, 0, 0, 3, -1749},
						new double[]{0, 1, -1, 1, -1565},
						new double[]{1, 0, 0, 1, -1491},
						new double[]{0, 1, 1, 1, -1475},
						new double[]{0, 1, 1, -1, -1410},
						new double[]{0, 1, 0, -1, -1344},
						new double[]{1, 0, 0, -1, -1335},
						new double[]{0, 0, 3, 1, 1107},
						new double[]{4, 0, 0, -1, 1021},
						new double[]{4, 0, -1, 1, 833},
						new double[]{0, 0, 1, -3, 777},
						new double[]{4, 0, -2, 1, 671},
						new double[]{2, 0, 0, -3, 607},
						new double[]{2, 0, 2, -1, 596},
						new double[]{2, -1, 1, -1, 491},
						new double[]{2, 0, -2, 1, -451},
						new double[]{0, 0, 3, -1, 439},
						new double[]{2, 0, 2, 1, 422},
						new double[]{2, 0, -3, -1, 421},
						new double[]{2, 1, -1, 1, -366},
						new double[]{2, 1, 0, 1, -351},
						new double[]{4, 0, 0, 1, 331},
						new double[]{2, -1, 1, 1, 315},
						new double[]{2, -2, 0, -1, 302},
						new double[]{0, 0, 1, 3, -283},
						new double[]{2, 1, 1, -1, -229},
						new double[]{1, 1, 0, -1, 223},
						new double[]{1, 1, 0, 1, 223},
						new double[]{0, 1, -2, -1, -220},
						new double[]{2, 1, -1, -1, -220},
						new double[]{1, 0, 1, 1, -185},
						new double[]{2, -1, -2, -1, 181},
						new double[]{0, 1, 2, 1, -177},
						new double[]{4, 0, -2, -1, 176},
						new double[]{4, -1, -1, -1, 166},
						new double[]{1, 0, 1, -1, -164},
						new double[]{4, 0, 1, -1, 132},
						new double[]{1, 0, -1, -1, -119},
						new double[]{4, -1, 0, -1, 115},
						new double[]{2, -2, 0, 1, 107}
				};
		//Reading periodic terms
		double fD, fD2, fM, fM2, fMm, fMm2, fF, fF2, coeffs, coeffs2, coeffc, f, f2, sumL = 0, sumR = 0, sumB = 0;

		for (int x = 0; x < ld.length; x++) {
			fD = ld[x][0];
			fM = ld[x][1];
			fMm = ld[x][2];
			fF = ld[x][3];
			coeffs = ld[x][4];
			coeffc = ld[x][5];
			if (fM == 1 || fM == -1) {
				f = fE;
			} else if (fM == 2 || fM == -2) {
				f = fE2;
			} else {
				f = 1;
			}
			sumL += f * coeffs * Utils.sind(fD * D + fM * Msm + fMm * Mmm + fF * F);
			sumR += f * coeffc * Utils.cosd(fD * D + fM * Msm + fMm * Mmm + fF * F);
			fD2 = lat[x][0];
			fM2 = lat[x][1];
			fMm2 = lat[x][2];
			fF2 = lat[x][3];
			coeffs2 = lat[x][4];
			if (fM2 == 1 || fM2 == -1) {
				f2 = fE;
			} else if (fM2 == 2 || fM2 == -2) {
				f2 = fE2;
			} else {
				f2 = 1;
			}
			sumB += f2 * coeffs2 * Utils.sind(fD2 * D + fM2 * Msm + fMm2 * Mmm + fF2 * F);
		}

		// Corrections
		sumL = sumL + 3_958 * Utils.sind(A1) + 1_962 * Utils.sind(Lmm - F) + 318 * Utils.sind(A2);
		sumB = sumB - 2_235 * Utils.sind(Lmm) + 382 * Utils.sind(A3) + 175 * Utils.sind(A1 - F) + 175 * Utils.sind(A1 + F) + 127 * Utils.sind(Lmm - Mmm) - 115 * Utils.sind(Lmm + Mmm);

		// Longitude of the moon
		double lambdaMm = Utils.trunc(Lmm + sumL / 1_000_000D);

		// Latitude of the moon
		double betaM = sumB / 1_000_000D;

		// Distance earth-moon
		double dEM = 385_000.56 + sumR / 1_000D;

		// Apparent longitude of the moon
		Context.lambdaMapp = lambdaMm + Context.delta_psi;

		// Right ascension of the moon, apparent
		Context.RAmoon = Math.toDegrees(Utils.trunc2(Math.atan2((Utils.sind(Context.lambdaMapp) * Utils.cosd(Context.eps) - Utils.tand(betaM) * Utils.sind(Context.eps)), Utils.cosd(Context.lambdaMapp))));

		// Declination of the moon
		Context.DECmoon = Math.toDegrees(Math.asin(Utils.sind(betaM) * Utils.cosd(Context.eps) + Utils.cosd(betaM) * Utils.sind(Context.eps) * Utils.sind(Context.lambdaMapp)));

		// GHA of the moon
		Context.GHAmoon = Utils.trunc(Context.GHAAtrue - Context.RAmoon);

		// Horizontal parallax of the moon
		Context.HPmoon = Math.toDegrees(3_600D * Math.asin(6_378.14 / dEM));

		// Semi-diameter of the moon
		Context.SDmoon = Math.toDegrees(3_600D * Math.asin(1_738 / dEM));

		// Geocentric angular distance between moon and sun
		Context.LDist = Math.toDegrees(Math.acos(Utils.sind(Context.DECmoon) * Utils.sind(Context.DECsun) + Utils.cosd(Context.DECmoon) * Utils.cosd(Context.DECsun) * Utils.cosd(Context.RAmoon - Context.RAsun)));

		// Phase angle
		double radianPhase = Math.atan2(Context.dES * Utils.sind(Context.LDist), (dEM - Context.dES * Utils.cosd(Context.LDist)));
		Context.moonPhase = Math.toDegrees(radianPhase);

		// Illumination of the moon's disk
		Context.k_moon = 100D * (1 + Math.cos(radianPhase)) / 2D;

		Context.moonEoT = 4 * Context.GHAmoon + 720 - 1_440 * Context.dayfraction;
		if (Context.moonEoT > 20) {
			Context.moonEoT -= 1_440;
		}
		if (Context.moonEoT < -20) {
			Context.moonEoT += 1_440;
		}
	}
}
