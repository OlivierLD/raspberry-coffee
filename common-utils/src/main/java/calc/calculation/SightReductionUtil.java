package calc.calculation;

import calc.GeomUtil;

import java.text.DecimalFormat;

/**
 * Astronomical Navigation Tools.
 * <br>
 * Dead Reckoning : Estimated Altitude et Azimuth.
 * <br>
 * This is a Java Bean.
 * <br>
 * Input parameters :
 * <ul>
 * <li type="disc">GHA - Greenwich Hour Angle</li>
 * <li type="disc">Declination</li>
 * <li type="disc">Estimated Latitude</li>
 * <li type="disc">Estimated Longitude</li>
 * </ul>
 * Output data :
 * <ul>
 * <li type="disc">Estimated Altitude</li>
 * <li type="disc">Azimuth</li>
 * </ul>
 * Test with :
 * <ul>
 * <li>GHA = 321&deg;57.9</li>
 * <li>D = 13&deg;57.5 N</li>
 * <li>L = 46&deg;38 N</li>
 * <li>G  = 4&deg;06 W</li>
 * </ul>
 * Result should be
 * <ul>
 * <li type="disc">Ea 42 02</li>
 * <li type="disc">Z 119</li>
 * </ul>
 * This call only performs calculations. No user interface is provided.
 *
 * @author olivier@lediouris.net
 * @version 1.0.0
 */
public class SightReductionUtil {
	private Double dHe = null;
	private Double dZ = null;

	private double AHG;
	private double D;
	private double L;
	private double G;

	private final static DecimalFormat df = new DecimalFormat("##0.000");

	/**
	 * Constructor.
	 * Call it and then use getHe() and getZ() to retrieve result.<br>
	 * Call after this one setAHG(), setD(), setL() and setG()
	 */
	public SightReductionUtil() {
	}

	/**
	 * Constructor.
	 * Call it and then use getHe() and getZ() to retrieve result.
	 *
	 * @param dAHG Greenwich Hour Angle
	 * @param dD   Declination
	 * @param dL   Estimated Latitude
	 * @param dG   Estimated Longitude
	 */
	public SightReductionUtil(double dAHG,
	                          double dD,
	                          double dL,
	                          double dG) {
		this.AHG = dAHG;
		this.D = dD;
		this.L = dL;
		this.G = dG;
	}

	public void calculate(double ahg, double d) {
		setAHG(ahg);
		setD(d);
		calculate();
	}

	public void calculate(double l, double g, double ahg, double d) {
		setL(l);
		setG(g);
		calculate(ahg, d);
	}

	/**
	 * Performs the required calculations, after the AHG, D, L and G.
	 * he and Z are after that ready to be retrieved.
	 */
	public void calculate() {
		double AHL = this.AHG + this.G;
		while (AHL < 0.0) AHL = 360.0 + AHL;
		// Formula to solve : sin He = sin L sin D + cos L cos D cos AHL
		double sinL = Math.sin(Math.toRadians(this.L));
		double sinD = Math.sin(Math.toRadians(this.D));
		double cosL = Math.cos(Math.toRadians(this.L));
		double cosD = Math.cos(Math.toRadians(this.D));
		double cosAHL = Math.cos(Math.toRadians(AHL));

		double sinHe = (sinL * sinD) + (cosL * cosD * cosAHL);
		double He = Math.toDegrees(Math.asin(sinHe));
//  System.out.println("Hauteur Estimee : " + GeomUtil.decToSex(He));
		dHe = He;

		// Formula to solve : tg Z = sin P / cos L tan D - sin L cos P
		double P = (AHL < 180.0) ? AHL : (360.0 - AHL);
		double sinP = Math.sin(Math.toRadians(P));
		double cosP = Math.cos(Math.toRadians(P));
		double tanD = Math.tan(Math.toRadians(this.D));
		double tanZ = sinP / ((cosL * tanD) - (sinL * cosP));
		double Z = Math.toDegrees(Math.atan(tanZ));

		if (AHL < 180.0) { // vers l'West
			if (Z < 0.0) { // sud vers nord
				Z = 180.0 - Z;
			} else {         // Nord vers Sud
				Z = 360.0 - Z;
			}
		} else {           // vers l'Est
			if (Z < 0.0) { // sud vers nord
				Z = 180.0 + Z;
//    } else {       // nord vers sud
//      Z = Z;
			}
		}
//  System.out.println("Azimut : " + GeomUtil.decToSex(Z));
		dZ = Z;
	}

	/**
	 * Returns Hauteur estim&eacute;e after calculation.
	 * This value is decimal. Use GeomUtil.decToSex(getHe()) to read it in DegMinSec.
	 *
	 * @see GeomUtil
	 */
	public Double getHe() {
		return dHe;
	}

	/**
	 * Returns Azimuth after calculation.
	 * This value is decimal. Use GeomUtil.decToSex(getZ()) to read it in DegMinSec.
	 *
	 * @see GeomUtil
	 */
	public Double getZ() {
		return dZ;
	}

	/**
	 * Set the AHG before calculation
	 *
	 * @param ahg the AHG to set
	 */
	public void setAHG(double ahg) {
		this.AHG = ahg;
	}

	/**
	 * Set the D before calculation
	 *
	 * @param d the D to set
	 */
	public void setD(double d) {
		this.D = d;
	}

	/**
	 * Set the L before calculation
	 *
	 * @param l the L to set
	 */
	public void setL(double l) {
		this.L = l;
	}

	/**
	 * Set the G before calculation
	 *
	 * @param g the G to set
	 */
	public void setG(double g) {
		this.G = g;
	}

	/**
	 * Corrections
	 */
	static double horizonDip = 0.0;
	static double refraction = 0.0;
	static double pa = 0.0;

	public final static int UPPER_LIMB = 0;
	public final static int LOWER_LIMB = 1;
	public final static int NEAR_LIMB = 2;
	public final static int FAR_LIMB = 3;
	public final static int NO_LIMB = -1; // Stars & Planets

	public double getHorizonDip() {
		return horizonDip;
	}

	public double getRefraction() {
		return refraction;
	}

	public double getPa() {
		return pa;
	}

	public static double getHorizonDip(double eyeHeight) { // In meters
		return 1.76 * Math.sqrt(eyeHeight);
	}

	public static double getRefraction(double alt) { // Works, according to the Correction Tables...
		return 0.97127 * Math.tan(Math.toRadians(90.0 - alt)) -
					 0.00137 * Math.pow(Math.tan(Math.toRadians(90.0 - alt)), 3.0);
	}

//  public static double getRefraction(double alt)
//  {
//    double r = 0.0D;
//    r = 1.0D / (Math.tan(Math.toRadians(alt)) + (7.31D / (alt + 4.4D)));
//    r -= 0.06D * Math.sin(Math.toRadians(((14.7D * r) + 13D) / 60d));
//    return r; // Result in minutes
//  }

	/**
	 * Returns the Observed Altitude of a celestial body
	 * <br>
	 * We left in stand by for now:
	 * <ul>
	 * <li type="disc">Oblate Spheroid (Earth is not a sphere)</li>
	 * <li type="disc">Barometric Correction</li>
	 * </ul>
	 *
	 * @param appAltitude The one you want to correct <b>in degrees</b>
	 * @param eyeHeight   Height of the eye above water, <b>in meters</b>
	 * @param hp          Horizontal parallax, <b>in minutes</b>
	 * @param sd          Semi diameter of the celestial body, <b>in minutes</b>
	 * @param limb        Upper or Lower limb
	 * @return the Observed Altitude
	 * @see SightReductionUtil#UPPER_LIMB
	 * @see SightReductionUtil#LOWER_LIMB
	 */
	public static double observedAltitude(double appAltitude,
	                                      double eyeHeight, // meters
	                                      double hp,
	                                      double sd,
	                                      int limb) {
		return observedAltitude(appAltitude,
				eyeHeight,
				hp,
				sd,
				limb,
				false);
	}

	public static double observedAltitude(double appAltitude,
	                                      double hp,
	                                      double sd,
	                                      boolean verbose) {
		/**
		 * With an artificial horizon.
		 * No semi-diameter correction.
		 * No horizon dip correction
		 *
		 * instrument altitude is to be divided by 2
		 */
		return observedAltitude(appAltitude / 2.0, 0.0, hp, sd, NO_LIMB, true, verbose);
	}

	/**
	 * Correction for Instrumental to Observed Altitude
	 *
	 * @param appAltitude Instrumental Altitude
	 * @param eyeHeight   Eye Height above sea level
	 * @param hp          Horizontal Parallax
	 * @param sd          Semi Diameter
	 * @param limb        UPPER or LOWER limb
	 * @param verbose     more info
	 * @return Observed Altitude
	 */
	public static double observedAltitude(double appAltitude,
	                                      double eyeHeight, // meters
	                                      double hp,
	                                      double sd,
	                                      int limb,
	                                      boolean verbose) {
		return observedAltitude(appAltitude, eyeHeight, hp, sd, limb, false, verbose);
	}

	/**
	 * @param appAltitude       in degrees
	 * @param eyeHeight         in meters
	 * @param hp                Horizontal Parallax in degrees
	 * @param sd                Semi-Diameter in degrees
	 * @param limb              upper, lower, none
	 * @param artificialHorizon true/false
	 * @param verbose           true/false
	 * @return Observed Altitude in degrees
	 */
	public static double observedAltitude(double appAltitude,
	                                      double eyeHeight,
	                                      double hp,
	                                      double sd,
	                                      int limb,
	                                      boolean artificialHorizon,
	                                      boolean verbose) {
		double correction = 0.0;
		// Dip of horizon, in minutes
		if (!artificialHorizon) {
			horizonDip = getHorizonDip(eyeHeight);
			correction -= (horizonDip / 60.0);
		}
		if (verbose) {
			System.out.println("Original Altitude:" + df.format(appAltitude));
		}
		double observedAltitude = appAltitude + correction;
		if (verbose && !artificialHorizon) {
			System.out.println("-> With Hor.Dip :" + df.format(observedAltitude) + " (Horizon Dip for " + eyeHeight + "m:" + df.format(horizonDip) + "', total correction:" + df.format(correction * 60d) + "')");
		}
		// Refraction
		refraction = getRefraction(observedAltitude);
		correction -= (refraction / 60.0);
		observedAltitude = appAltitude + correction;
		if (verbose) {
			System.out.println("-> With Refr    :" + df.format(observedAltitude) + " (Refraction:" + df.format(refraction) + "', total correction:" + df.format(correction * 60d) + "')");
		}
		// Barometric & temp correction - stby for now

		// Parallax
		double rpa = 0.0; // Parallax in altitude, radians
//  double pa  = 0.0;
		pa = getParallax(hp, observedAltitude);
		rpa = Math.toRadians(pa);
		// Earth is not a sphere...
		double ob = 0.0; // Oblate Spheroid
	  /* Stby */
		rpa += ob;

		correction += (pa);
		observedAltitude = appAltitude + correction;
		if (verbose) {
			System.out.println("-> With Parallax:" + df.format(observedAltitude) + " (Parallax for hp " + (hp * 60) + ":" + df.format(pa * 60d) + "', total correction:" + df.format(correction * 60d) + "')");
		}
		// Semi diameter
		if (limb == LOWER_LIMB) {
			correction += (sd); // Lower Limb;
			if (verbose) {
				System.out.println("  Semi-Diameter:" + df.format(sd * 60d) + "'");
			}
		} else if (limb == UPPER_LIMB) {
			correction -= (sd); // Upper Limb;
			if (verbose) {
				System.out.println("  Semi-Diameter:" + df.format(-sd * 60d) + "'");
			}
		}

		observedAltitude = appAltitude + correction;
		if (verbose) {
			System.out.println("-> With Semi-Diam:" + df.format(observedAltitude) + ", total correction:" + df.format(correction * 60d) + "'");
			System.out.println("- Total Correction:" + df.format(correction) + "\272, " + GeomUtil.decToSex(correction, GeomUtil.SHELL, GeomUtil.NONE));
		}
		return observedAltitude;
	}

	/**
	 * @param hp     Horizontal Parallax (in degrees)
	 * @param obsAlt Observed Altitude (in degrees)
	 * @return parallax in degrees
	 */
	public static double getParallax(double hp, double obsAlt) {
		double p = 0.0;
		p = Math.toDegrees(Math.asin(Math.sin(Math.toRadians(hp)) * Math.cos(Math.toRadians(obsAlt))));
//  p += (0.0033528 * hp * (Math.sin(Math.toRadians(2*latObs) * Math.cos(Zn) * Math.sin(Math.toRadians(obsAlt)) - Math.sin(Math.toRadians(latObs)) * Math.sin(Math.toRadians(latObs)) * Math.cos(Math.toRadians(obsAlt)))));
		return p;
	}

	public static double observedToApparentAltitude(double obsAlt,
	                                                double hp,
	                                                boolean verbose) {
		double parallax = getParallax(hp, obsAlt);
		double appAlt = obsAlt - parallax;

		if (verbose) {
			System.out.println("Observed:" + df.format(obsAlt));
			System.out.println(" for hp:" + df.format(hp) + ", parallax:" + df.format(parallax) + ", app. alt:" + df.format(appAlt));
		}
		double refraction = getRefraction(appAlt);
		appAlt += (refraction / 60.0);
		if (verbose) {
			System.out.println(" refraction:" + df.format(refraction / 60.0) + ", app. alt:" + df.format(appAlt));
		}
		return appAlt;
	}

	/**
	 * @param hp     Horizontal Parallax in minutes
	 * @param appAlt App Altitude, in degrees
	 * @return the correcter SD, in degrees
	 */
	public static double getMoonSD(double hp,
	                               double appAlt) {
		double calcSd = 0.0;
		calcSd = (0.2725 * (hp / 60.0)) / (1 - (Math.sin(Math.toRadians(hp / 60.0)) * Math.sin(Math.toRadians(appAlt))));
		return calcSd;
	}

	public static double getDistance(double decBodyOne,
	                                 double decBodyTwo,
	                                 double ghaBodyOne,
	                                 double ghaBodyTwo) {
		double ld = Math.acos((Math.sin(decBodyOne) * Math.sin(decBodyTwo)) + (Math.cos(decBodyOne) * Math.cos(decBodyTwo) * Math.cos(Math.abs(ghaBodyTwo - ghaBodyOne))));
		return Math.toDegrees(ld);
	}

	/**
	 * @param instrAltitude     Instrument Altitude in degrees
	 * @param eyeHeight         Eye height in meters
	 * @param hp                Horizontal parallax in degrees
	 * @param sd                Semi-diameter in degrees
	 * @param limb              UPPER_LIMB, LOWER_LIMB, NO_LIMB
	 * @param artificialHorizon true/false
	 * @param verbose           true/false
	 * @return Altitude correction, in degrees
	 */
	public static double getAltitudeCorrection(double instrAltitude,
	                                           double eyeHeight,
	                                           double hp,
	                                           double sd,
	                                           int limb,
	                                           boolean artificialHorizon,
	                                           boolean verbose) {
		double correction = 0.0D;
		if (!artificialHorizon) {
			horizonDip = getHorizonDip(eyeHeight);
			correction -= (horizonDip / 60D);
		}
		if (verbose) {
			System.out.println("Original Altitude:" + df.format(instrAltitude));
		}
		double observedAltitude = instrAltitude + correction;
		if (verbose && !artificialHorizon) {
			System.out.println("-> With Hor.Dip :" + df.format(observedAltitude) + " (Horizon Dip:" + df.format(horizonDip) + "')");
		}
		refraction = getRefraction(observedAltitude);
		correction -= (refraction / 60D);
		observedAltitude = instrAltitude + correction;
		if (verbose) {
			System.out.println("-> With Refr    :" + df.format(observedAltitude) + " (Refraction:" + df.format(refraction) + "')");
		}
		double pa = 0.0D;
		pa = getParallax(hp, observedAltitude);
		correction += pa;
		observedAltitude = instrAltitude + correction;
		if (verbose) {
			System.out.println("-> With Parallax:" + df.format(observedAltitude) + " (Parallax:" + df.format(pa * 57.295779513082323D) + "')");
		}
		if (limb == LOWER_LIMB) {
			correction += sd;
			if (verbose) {
				System.out.println("  Semi-Diameter:" + df.format(sd * 60d) + "'");
			}
		} else if (limb == UPPER_LIMB) {
			correction -= sd;
			if (verbose) {
				System.out.println("  Semi-Diameter:" + df.format(-sd * 60d) + "'");
			}
		}
		observedAltitude = instrAltitude + correction;
		if (verbose) {
			System.out.println("-> With Semi-Diam:" + df.format(observedAltitude));
			System.out.println("- Total Correction:" + df.format(correction) + "\272");
		}
		return correction;
	}

	/**
	 * All values in degrees, in and out
	 * Implementation of the Young's formula
	 *
	 * @param hMoon
	 * @param appHMoon
	 * @param hBody
	 * @param appHBody
	 * @param obsDist
	 * @return
	 */
	public static double clearLunarDistance(double hMoon, double appHMoon, double hBody, double appHBody, double obsDist) {
		double cosHm_cosHb = Math.cos(Math.toRadians(hMoon)) * Math.cos(Math.toRadians(hBody));
		double cosHmApp_cosHbApp = Math.cos(Math.toRadians(appHMoon)) * Math.cos(Math.toRadians(appHBody));

		double cosDapp_cosHMappHBapp = Math.cos(Math.toRadians(obsDist)) + Math.cos(Math.toRadians(appHMoon + appHBody));
		double cosHmHb = Math.cos(Math.toRadians(hMoon + hBody));

		double cosD = (cosHm_cosHb / cosHmApp_cosHbApp) * cosDapp_cosHMappHBapp - cosHmHb;

		return Math.toDegrees(Math.acos(cosD));
	}

	public static void main2(String... args) {
		double corr = getAltitudeCorrection(7d,
				2d,
				0.1 / 60d,
				16d / 60d,
				LOWER_LIMB,
				false,
				true);
		System.out.println("Correction:" + (corr * 60d) + "'");
	}
}
