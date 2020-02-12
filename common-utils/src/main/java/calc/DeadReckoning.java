package calc;

import java.text.DecimalFormat;

/**
 * Dead Reckoning
 * <p>
 * Call sequence:
 * <pre>
 *   DeadReckoning dr = new DeadReckoning();
 *
 *
 * </pre>
 */
public class DeadReckoning {
	private Double dHe;
	private Double dZ;
	private double AHG;
	private double D;
	private double L;
	private double G;
	private static final DecimalFormat df = new DecimalFormat("##0.000");
	static double horizonDip;
	static double refraction;
	static double pa;
	public static final int UPPER_LIMB = 0;
	public static final int LOWER_LIMB = 1;
	public static final int NEAR_LIMB = 2;
	public static final int FAR_LIMB = 3;
	public static final int NO_LIMB = -1;

	public static class Correction {

		public double getDipCorrection() {
			return dipCorrection;
		}

		public void setDipCorrection(double dipCorrection) {
			this.dipCorrection = dipCorrection;
		}

		public double getEyeHeight() {
			return eyeHeight;
		}

		public void setEyeHeight(double eyeHeight) {
			this.eyeHeight = eyeHeight;
		}

		public double getIndex() {
			return index;
		}

		public void setIndex(double index) {
			this.index = index;
		}

		public double getParallax() {
			return parallax;
		}

		public double getParallax2() {
			return parallax2;
		}

		public void setParallax(double parallax) {
			this.parallax = parallax;
		}

		public void setParallax2(double parallax2) {
			this.parallax2 = parallax2;
		}

		public double getRefraction() {
			return refraction;
		}

		public void setRefraction(double refraction) {
			this.refraction = refraction;
		}

		public double getSemiDiameter() {
			return semiDiameter;
		}

		public void setSemiDiameter(double semiDiameter) {
			this.semiDiameter = semiDiameter;
		}

		double index;
		double eyeHeight;
		double dipCorrection;
		double refraction;
		double parallax;
		double parallax2;
		double semiDiameter;

		public Correction() {
			index = 0.0D;
			eyeHeight = 0.0D;
			dipCorrection = 0.0D;
			refraction = 0.0D;
			parallax = 0.0D;
			parallax2 = 0.0D;
			semiDiameter = 0.0D;
		}
	}

	public DeadReckoning() {
		dHe = null;
		dZ = null;
	}

	public DeadReckoning(double dAHG,
	                     double dD,
	                     double dL,
	                     double dG) {
		dHe = null;
		dZ = null;
		AHG = dAHG;
		D = dD;
		L = dL;
		G = dG;
	}

	public DeadReckoning calculate() {
		double AHL = AHG + G;
		if (AHL < 0.0D) {
			AHL = 360D + AHL;
		}
		double sinL = Math.sin(Math.toRadians(L));
		double sinD = Math.sin(Math.toRadians(D));
		double cosL = Math.cos(Math.toRadians(L));
		double cosD = Math.cos(Math.toRadians(D));
		double cosAHL = Math.cos(Math.toRadians(AHL));
		double sinHe = sinL * sinD + cosL * cosD * cosAHL;
		double He = Math.toDegrees(Math.asin(sinHe));
		dHe = new Double(He);
		double P = AHL >= 180D ? 360D - AHL : AHL;
		double sinP = Math.sin(Math.toRadians(P));
		double cosP = Math.cos(Math.toRadians(P));
		double tanD = Math.tan(Math.toRadians(D));
		double tanZ = sinP / (cosL * tanD - sinL * cosP);
		double Z = Math.toDegrees(Math.atan(tanZ));
		if (AHL < 180D) {
			if (Z < 0.0D) {
				Z = 180D - Z;
			} else {
				Z = 360D - Z;
			}
		} else if (Z < 0.0D) {
			Z = 180D + Z;
		}
//  else
//    Z = Z;
		dZ = new Double(Z);
		return this;
	}

	public Double getHe() {
		return dHe;
	}

	public Double getZ() {
		return dZ;
	}

	public void setAHG(double ahg) {
		AHG = ahg;
	}

	public void setD(double d) {
		D = d;
	}

	public void setL(double l) {
		L = l;
	}

	public void setG(double g) {
		G = g;
	}

	public double getHorizonDip() {
		return horizonDip;
	}

	public double getRefraction() {
		return refraction;
	}

	public double getPa() {
		return pa;
	}

	public static double getHorizonDip(double eyeHeight) {
		return 1.76D * Math.sqrt(eyeHeight);
	}

	public static double getRefraction(double alt) {
		double r = 0.0D;
		r = 1.0D / Math.tan(Math.toRadians(alt + 7.31D / (alt + 4.4D)));
		r -= 0.06D * Math.sin(Math.toRadians((14.7D * r + 13D) / 60D));
		return r;
	}

	public static double observedAltitude(double instrAltitude,
	                                      double eyeHeight,
	                                      double hp,
	                                      double sd,
	                                      double lat,
	                                      double Z,
	                                      int limb,
	                                      boolean
			                                      artificialHorizon,
	                                      Correction corrections,
	                                      boolean verbose) {
		double observedAltitude = instrAltitude + getAltitudeCorrection(instrAltitude, eyeHeight, hp, sd, lat, Z, limb, artificialHorizon, corrections, verbose);
		return observedAltitude;
	}

	public static void main(String... args) {
//  getSunCorrectionTable();
//  getMoonCorrectionTable();
//  getPlanetStarsCorrectionTable();
		double corr = getAltitudeCorrection(7d,
				2d,
				0.1,
				16d,
				Double.MAX_VALUE,
				Double.MAX_VALUE,
				LOWER_LIMB,
				false,
				null,
				true);
		System.out.println("Correction:" + (corr * 60d) + "'");
	}

	public static void getSunCorrectionTable() {
		double obsAltitude[] = {7D, 7.333333333333333D, 7.6666666666666599D,
				8D, 8.3333333333333321D, 8.6666666666666607D,
				9D, 9.3333333333333321D, 9.6666666666666607D,
				10D, 10.33333333333333D, 10.666666666666661D,
				11D, 11.5D, 12D, 12.5D, 13D, 13.5D, 14D, 15D,
				16D, 17D, 18D, 19D, 20D, 22D, 24D, 26D, 28D, 30D,
				32D, 34D, 36D, 38D, 40D, 45D, 50D, 55D, 60D, 70D,
				80D, 90D};
		double eyeHeight[] = {0.0D, 2D, 4D, 6D, 8D, 10D, 12D, 14D, 16D, 18D, 20D, 22D, 24D};

		double sd = 16d;
		double hp = 0.1;

		System.out.println("<?xml version='1.0' encoding='utf-8'?>");
		System.out.println("<sun-corrections>");
		// Horizon Dip
		System.out.println("  <horizon-dips>");
		for (int i = 0; i < eyeHeight.length; i++) {
			double hd = getHorizonDip(eyeHeight[i]);
			System.out.println("    <horizon-dip eye-height='" + eyeHeight[i] + "' dip='" + df.format(hd) + "'/>");
		}
		System.out.println("  </horizon-dips>");
		for (int i = 0; i < obsAltitude.length; i++) {
			System.out.println("  <obs-altitude value=\"" + GeomUtil.decToSex(obsAltitude[i], GeomUtil.SWING, GeomUtil.NONE, GeomUtil.TRAILING_SIGN, true) + "\">");
			for (int j = 0; j < eyeHeight.length; j++) {
				double corr = getAltitudeCorrection(obsAltitude[i],
						eyeHeight[j],
						hp,
						sd,
						Double.MIN_VALUE,
						Double.MIN_VALUE,
						LOWER_LIMB,
						false,
						null,
						false);
				System.out.println("    <corr eye-height=\"" + eyeHeight[j] + "\">" + /*GeomUtil.decToSex(corr, GeomUtil.SWING, GeomUtil.NONE)*/ df.format(corr * 60d) + "</corr>");
			}

			System.out.println("  </obs-altitude>");
		}

		System.out.println("</sun-corrections>");
	}

	public static void getMoonCorrectionTable() {
		double obsAltitude[] = {5d, 5.5, 6d, 6.5, 7D, 7.5, 8D, 8.5, 9D,
				10D, 11D, 12d, 13d, 14D, 15D, 16D, 17D, 18D, 19D,
				20D, 21d, 22D, 23d, 24D, 25d, 26D, 27d, 28D, 29d,
				30D, 31d, 32D, 33d, 34D, 35d, 36D, 37d, 38D, 39d,
				40D, 41d, 42d, 43d, 44d, 45D, 46d, 47d, 48d, 49d,
				50D, 51d, 52D, 53d, 54d, 55d, 56d, 57d, 58d, 59d,
				60D, 61d, 62d, 63d, 64d, 65d, 66d, 76d, 68d, 69d,
				70D, 71d, 72d, 73d, 74d, 75d, 76d, 77d, 78d, 79d,
				80D, 81d, 82d, 83d, 84d, 85d, 86d, 87d, 88d, 90D};

		double eyeHeight[] = {0.0D, 2D, 4D, 6D, 8D, 10D, 12D, 14D, 16D, 18D, 20D, 22D, 24D};

		double horParallax[] = {54, 55, 55.5, 56, 56.5, 57, 57.5, 58, 58.5, 59, 59.5, 60, 61};
		double moonDiameter[] = {29.4, 30d, 30.3, 30.6, 30.8, 31.1, 31.4, 31.7, 32d, 32.2, 32.5, 32.8, 33.3};

		double sd = 0d;

		System.out.println("<?xml version='1.0' encoding='utf-8'?>");
		System.out.println("<moon-corrections>");

		// Horizon Dip
		System.out.println("  <horizon-dips>");
		for (int i = 0; i < eyeHeight.length; i++) {
			double hd = getHorizonDip(eyeHeight[i]);
			System.out.println("    <horizon-dip eye-height='" + eyeHeight[i] + "' dip='" + df.format(hd) + "'/>");
		}
		System.out.println("  </horizon-dips>");
		// Parallax + Refraction
		for (int i = 0; i < obsAltitude.length; i++) {
			System.out.println("  <obs-altitude value=\"" + GeomUtil.decToSex(obsAltitude[i], GeomUtil.SWING, GeomUtil.NONE, GeomUtil.TRAILING_SIGN, true) + "\">");
			for (int j = 0; j < horParallax.length; j++) {
				// Refraction + Parallax
				double corr = getAltitudeCorrection(obsAltitude[i],
						0d, // Eye Height
						horParallax[j],
						sd,
						Double.MIN_VALUE,
						Double.MIN_VALUE,
						NO_LIMB,
						false,
						null,
						false);
				corr += ((moonDiameter[j] / 2d) / 60d);
				System.out.println("    <corr-ref-pa hp=\"" + horParallax[j] + "\">" + /*GeomUtil.decToSex(corr, GeomUtil.SWING, GeomUtil.NONE)*/ df.format(corr * 60d) + "'</corr-ref-pa>");
			}

			System.out.println("  </obs-altitude>");
		}
		// Semi diameter, from the almanac
		System.out.println("</moon-corrections>");
	}

	public static void getPlanetStarsCorrectionTable() {
		double obsAltitude[] = {7D, 7.333333333333333D, 7.6666666666666599D,
				8D, 8.3333333333333321D, 8.6666666666666607D,
				9D, 9.3333333333333321D, 9.6666666666666607D,
				10D, 10.33333333333333D, 10.666666666666661D,
				11D, 11.5D, 12D, 12.5D, 13D, 13.5D, 14D, 15D,
				16D, 17D, 18D, 19D, 20D, 22D, 24D, 26D, 28D, 30D,
				32D, 34D, 36D, 38D, 40D, 45D, 50D, 55D, 60D, 70D,
				80D, 90D};
		double eyeHeight[] = {0.0D, 2D, 4D, 6D, 8D, 10D, 12D, 14D, 16D, 18D, 20D, 22D, 24D};

		double sd = 0d;
		double hp = 0d;

		System.out.println("<?xml version='1.0' encoding='utf-8'?>");
		System.out.println("<planets-stars-corrections>");
		for (int i = 0; i < obsAltitude.length; i++) {
			System.out.println("  <obs-altitude value=\"" + GeomUtil.decToSex(obsAltitude[i], GeomUtil.SWING, GeomUtil.NONE, GeomUtil.TRAILING_SIGN, true) + "\">");
			for (int j = 0; j < eyeHeight.length; j++) {
				double corr = getAltitudeCorrection(obsAltitude[i],
						eyeHeight[j],
						hp,
						sd,
						Double.MIN_VALUE,
						Double.MIN_VALUE,
						NO_LIMB,
						false,
						null,
						false);
				System.out.println("    <corr eye-height=\"" + eyeHeight[j] + "\">" + /*GeomUtil.decToSex(corr, GeomUtil.SWING, GeomUtil.NONE)*/ df.format(corr * 60d) + "'</corr>");
			}

			System.out.println("  </obs-altitude>");
		}

		System.out.println("</planets-stars-corrections>");
	}

	public static double getAltitudeCorrection(double instrAltitude,
	                                           double eyeHeight,
	                                           double hp,
	                                           double sd,
	                                           double lat,
	                                           double Z,
	                                           int limb,
	                                           boolean artificialHorizon,
	                                           Correction corrections,
	                                           boolean verbose) {
		double correction = 0.0D;
		if (corrections != null) {
			corrections.setEyeHeight(eyeHeight);
		}
		if (!artificialHorizon) {
			horizonDip = getHorizonDip(eyeHeight);
			if (corrections != null) {
				corrections.setDipCorrection(horizonDip);
			}
			correction -= horizonDip / 60D;
		}
		if (verbose) {
			System.out.println("Original Altitude:" + df.format(instrAltitude));
		}
		double observedAltitude = instrAltitude + correction;
		if (verbose && !artificialHorizon) {
			System.out.println("-> With Hor.Dip :" + df.format(observedAltitude) + " (Horizon Dip:" + df.format(horizonDip) + "')");
		}
		refraction = getRefraction(observedAltitude);
		if (corrections != null) {
			corrections.setRefraction(refraction);
		}
		correction -= refraction / 60D;
		observedAltitude = instrAltitude + correction;
		if (verbose) {
			System.out.println("-> With Refr    :" + df.format(observedAltitude) + " (Refraction:" + df.format(refraction) + "')");
		}
		double pa = 0.0D;
		pa = getParallax(hp, observedAltitude, lat, Z, corrections);
		correction += pa;
		observedAltitude = instrAltitude + correction;
		if (verbose) {
			System.out.println("-> With Parallax:" + df.format(observedAltitude) + " (Parallax:" + df.format(pa * 57.295779513082323D) + "')");
		}
		if (limb == LOWER_LIMB) {
			correction += sd / 60D;
			if (corrections != null) {
				corrections.setSemiDiameter(sd);
			}
			if (verbose) {
				System.out.println("  Semi-Diameter:" + df.format(sd) + "'");
			}
		} else if (limb == UPPER_LIMB) {
			correction -= sd / 60D;
			if (corrections != null) {
				corrections.setSemiDiameter(-sd);
			}
			if (verbose) {
				System.out.println("  Semi-Diameter:" + df.format(-sd) + "'");
			}
		}
		observedAltitude = instrAltitude + correction;
		if (verbose) {
			System.out.println("-> With Semi-Diam:" + df.format(observedAltitude));
			System.out.println("- Total Correction:" + df.format(correction));
		}
		return correction;
	}

	public static double getParallax(double hp,
	                                 double obsAlt,
	                                 double latObs,
	                                 double Z,
	                                 Correction corr) {
		double p = 0.0D;
		double ob = 0.0D;
		p = Math.asin(Math.sin(Math.toRadians(hp / 60D)) * Math.cos(Math.toRadians(obsAlt)));
		if (latObs != Double.MIN_VALUE && Z != Double.MIN_VALUE) {
			ob = (hp / 298D) * (Math.sin((double) 2 * Math.toRadians(latObs)) * Math.cos(Math.toRadians(Z)) * Math.sin(Math.toRadians(obsAlt)) - Math.pow(Math.sin(Math.toRadians(latObs)), 2D) * Math.cos(Math.toRadians(obsAlt)));
			ob /= 60D;
			ob = Math.toRadians(ob);
		}
		if (corr != null) {
			corr.setParallax(Math.toDegrees(p));
			corr.setParallax2(Math.toDegrees(ob));
		}
		return Math.toDegrees(p + ob);
	}

	public static double observedToApparentAltitude(double obsAlt, double hp, double latObs, double Z, boolean verbose) {
		double parallax = getParallax(hp, obsAlt, latObs, Z, null);
		double appAlt = obsAlt - parallax;
		if (verbose) {
			System.out.println("Observed:" + df.format(obsAlt));
			System.out.println(" for hp:" + df.format(hp) + ", parallax:" + df.format(parallax) + ", app. alt:" + df.format(appAlt));
		}
		double refraction = getRefraction(appAlt);
		appAlt += refraction / 60D;
		if (verbose) {
			System.out.println(" refraction:" + df.format(refraction / 60D) + ", app. alt:" + df.format(appAlt));
		}
		return appAlt;
	}

	public static double getDistance(double declBodyOne,
	                                 double ghaBodyOne,
	                                 double declBodyTwo,
	                                 double ghaBodyTwo) {
		double dist = 0.0D;
		double coDeclBodyOne = ((90D - declBodyOne) * Math.PI) / 180D;
		double coDeclBodyTwo = ((90D - declBodyTwo) * Math.PI) / 180D;
		double deltaGHA = (Math.abs(ghaBodyOne - ghaBodyTwo) * Math.PI) / 180D;
		dist = Math.toDegrees(Math.acos(Math.cos(coDeclBodyOne) * Math.cos(coDeclBodyTwo) + Math.sin(coDeclBodyOne) * Math.sin(coDeclBodyTwo) * Math.cos(deltaGHA)));
//  dist *= 57.295779513082323D;
		return dist;
	}

	public static double getMoonSD(double hp, double appAlt) {
		double calcSd = 0.0D;
		calcSd = (0.2725D * (hp / 60D)) / ((double) 1 - Math.sin(((hp / 60D) * Math.PI) / 180D) * Math.sin((appAlt * Math.PI) / 180D));
		return calcSd;
	}

	public static double getDeltaZ(double appDist, double appAltBodyOne, double appAltBodyTwo) {
		double dz = 0.0D;
		double dzBodyOne = ((90D - appAltBodyOne) * Math.PI) / 180D;
		double dzBodyTwo = ((90D - appAltBodyTwo) * Math.PI) / 180D;
		dz = Math.acos((Math.cos((appDist * Math.PI) / 180D) - Math.cos(dzBodyOne) * Math.cos(dzBodyTwo)) / (Math.sin(dzBodyOne) * Math.sin(dzBodyTwo)));
		return (dz * 180D) / Math.PI;
	}

	public static double getObsDist(double altBodyOne, double altBodyTwo, double deltaZ) {
		double od = 0.0D;
		double dzOne = ((90D - altBodyOne) * Math.PI) / 180D;
		double dzTwo = ((90D - altBodyTwo) * Math.PI) / 180D;
		od = Math.acos(Math.cos(dzOne) * Math.cos(dzTwo) + Math.sin(dzOne) * Math.sin(dzTwo) * Math.cos((deltaZ * Math.PI) / 180D));
		return (od * 180D) / Math.PI;
	}
}
