package calc.calculation.nauticalalmanacV2;

public class Core {
	/**
	 * @param context
	 * @param year
	 * @param month  1 - Jan, 2 - Feb, etc.
	 * @param day
	 * @param hour
	 * @param minute
	 * @param second
	 * @param deltaT
	 * @return
	 */
	public static void julianDate(ContextV2 context, int year, int month, int day, int hour, int minute, float second, double deltaT) {
		//var year, month, day, hour, minute, second, context.dayfraction, ly=0;
		context.dayfraction = ((double) hour + ((double) minute / 60D) + ((double) second / 3600D)) / 24D;
		// Calculating Julian date, century, and millennium
		//Julian dacontext.TE (UT1)
		if (month <= 2) {
			year -= 1;
			month += 12;
		}
		double A = Math.floor(year / 100D);
		double B = 2 - A + Math.floor(A / 4D);
		context.JD0h = Math.floor(365.25 * (year + 4716D)) + Math.floor(30.6001 * (month + 1)) + day + B - 1524.5;
		context.JD = context.JD0h + context.dayfraction;

		//Julian centuries (UT1) from 2000 January 0.5
		context.T = (context.JD - 2451545D) / 36525D;
		context.T2 = context.T * context.T;
		context.T3 = context.T * context.T2;
		context.T4 = context.T * context.T3;
		context.T5 = context.T * context.T4;

		//Julian ephemeris dacontext.TE (TDT)
		context.JDE = context.JD + deltaT / 86400D;

		//Julian centuries (TDT) from 2000 January 0.5
		context.TE = (context.JDE - 2451545D) / 36525D;
		context.TE2 = context.TE * context.TE;
		context.TE3 = context.TE * context.TE2;
		context.TE4 = context.TE * context.TE3;
		context.TE5 = context.TE * context.TE4;

		//Julian millenniums (TDT) from 2000 January 0.5
		context.Tau = 0.1 * context.TE;
		context.Tau2 = context.Tau * context.Tau;
		context.Tau3 = context.Tau * context.Tau2;
		context.Tau4 = context.Tau * context.Tau3;
		context.Tau5 = context.Tau * context.Tau4;
	}

	//GHA Aries, GAST, GMST, equation of the equinoxes

	public static void aries(ContextV2 context) {
		//Mean GHA Aries
		double GHAAmean = Utils.trunc(280.46061837 + 360.98564736629 * (context.JD - 2451545D) + 0.000387933 * context.T2 - context.T3 / 38710000D);

		//GMST
//  SidTm = OutSidTime(GHAAmean);

		//True GHA Aries
		context.GHAAtrue = Utils.trunc(GHAAmean + context.delta_psi * Utils.cosd(context.eps));

		//GAST
//  SidTa = OutSidTime(GHAAtrue);

		//Equation of the equinoxes
		double EoE = 240 * context.delta_psi * Utils.cosd(context.eps);
		String EoEout = Double.toString(Math.round(1000 * EoE) / 1000D);
		EoEout = " " + EoEout + "s";
	}


	//Calculations for the Sun
	public static void sun(ContextV2 context) {
		//Mean longitude of the Sun
		context.Lsun_mean = Utils.trunc(280.4664567 + 360007.6982779 * context.Tau + 0.03032028 * context.Tau2 + context.Tau3 / 49931D - context.Tau4 / 15299D - context.Tau5 / 1988000D);

		//Heliocentric longitude of the Earth
		context.Le = Earth.lEarth(context.Tau);

		//Geocentric longitude of the Sun
		context.Lsun_true = Utils.trunc(context.Le + 180 - 0.000025);

		//Heliocentric latitude of Earth
		context.Be = Earth.bEarth(context.Tau);

		//Geocentric latitude of the Sun
		context.beta = Utils.trunc(-context.Be);

		//Corrections
		double Lsun_prime = Utils.trunc(context.Le + 180 - 1.397 * context.TE - 0.00031 * context.TE2);

		context.beta = context.beta + 0.000011 * (Utils.cosd(Lsun_prime) - Utils.sind(Lsun_prime));

		//Distance Earth-Sun
		context.Re = Earth.rEarth(context.Tau);
		context.dES = 149597870.691 * context.Re;

		//Apparent longitude of the Sun
		context.lambda_sun = Utils.trunc(context.Lsun_true + context.delta_psi - 0.005691611 / context.Re);

		//Right ascension of the Sun, apparent
		context.RAsun = Math.toDegrees(Utils.trunc2(Math.atan2((Utils.sind(context.lambda_sun) * Utils.cosd(context.eps) - Utils.tand(context.beta) * Utils.sind(context.eps)), Utils.cosd(context.lambda_sun))));

		//Declination of the Sun, apparent
		context.DECsun = Math.toDegrees(Math.asin(Utils.sind(context.beta) * Utils.cosd(context.eps) + Utils.cosd(context.beta) * Utils.sind(context.eps) * Utils.sind(context.lambda_sun)));

		//GHA of the Sun
		context.GHAsun = Utils.trunc(context.GHAAtrue - context.RAsun);

		//Semidiameter of the Sun
		context.SDsun = 959.63 / context.Re;

		//Horizontal parallax of the Sun
		context.HPsun = 8.794 / context.Re;

		//Equation of time
		//EOT = 4*(Lsun_mean-0.0057183-0.0008-RAsun+delta_psi*cosd(eps));
		context.EoT = 4 * context.GHAsun + 720 - 1440 * context.dayfraction;
		if (context.EoT > 20) context.EoT -= 1440;
		if (context.EoT < -20) context.EoT += 1440;
	}

	public static void polaris(ContextV2 context) {
		//Equatorial coordinates of Polaris at 2000.0 (mean equinox and equator 2000.0)
		double RApol0 = 37.95293333;
		double DECpol0 = 89.26408889;

		//Proper motion per year
		double dRApol = 2.98155 / 3600D;
		double dDECpol = -0.0152 / 3600D;

		//Equatorial coordinates at Julian Dacontext.TE T (mean equinox and equator 2000.0)
		double RApol1 = RApol0 + 100 * context.TE * dRApol;
		double DECpol1 = DECpol0 + 100 * context.TE * dDECpol;

		//Mean obliquity of ecliptic at 2000.0 in degrees
		// double eps0_2000 = 23.439291111;

		//Transformation to ecliptic coordinates in radians (mean equinox and equator 2000.0)
		double lambdapol1 = Math.atan2((Utils.sind(RApol1) * Utils.cosd(context.EPS0_2000) + Utils.tand(DECpol1) * Utils.sind(context.EPS0_2000)), Utils.cosd(RApol1));
		double betapol1 = Math.asin(Utils.sind(DECpol1) * Utils.cosd(context.EPS0_2000) - Utils.cosd(DECpol1) * Utils.sind(context.EPS0_2000) * Utils.sind(RApol1));

		//Precession
		double eta = Math.toRadians(47.0029 * context.TE - 0.03302 * context.TE2 + 0.00006 * context.TE3) / 3600D;
		double PI0 = Math.toRadians(174.876384 - (869.8089 * context.TE + 0.03536 * context.TE2) / 3600D);
		double p0 = Math.toRadians(5029.0966 * context.TE + 1.11113 * context.TE2 - 0.0000006 * context.TE3) / 3600D;
		double A1 = Math.cos(eta) * Math.cos(betapol1) * Math.sin(PI0 - lambdapol1) - Math.sin(eta) * Math.sin(betapol1);
		double B1 = Math.cos(betapol1) * Math.cos(PI0 - lambdapol1);
		double C1 = Math.cos(eta) * Math.sin(betapol1) + Math.sin(eta) * Math.cos(betapol1) * Math.sin(PI0 - lambdapol1);
		double lambdapol2 = p0 + PI0 - Math.atan2(A1, B1);
		double betapol2 = Math.asin(C1);

		//Nutation in longitude
		lambdapol2 += Math.toRadians(context.delta_psi);

		//Aberration
		double dlambdapol = (context.e * context.kappa * Math.cos(context.pi0 - lambdapol2) - context.kappa * Math.cos(Math.toRadians(context.Lsun_true) - lambdapol2)) / Math.cos(betapol2);
		double dbetapol = -context.kappa * Math.sin(betapol2) * (Math.sin(Math.toRadians(context.Lsun_true) - lambdapol2) - context.e * Math.sin(context.pi0 - lambdapol2));

		lambdapol2 += dlambdapol;
		betapol2 += dbetapol;

		//Transformation back to equatorial coordinates in radians
		double RApol2 = Math.atan2((Math.sin(lambdapol2) * Utils.cosd(context.eps) - Math.tan(betapol2) * Utils.sind(context.eps)), Math.cos(lambdapol2));
		double DECpol2 = Math.asin(Math.sin(betapol2) * Utils.cosd(context.eps) + Math.cos(betapol2) * Utils.sind(context.eps) * Math.sin(lambdapol2));

		//Finals
		context.GHApol = context.GHAAtrue - Math.toDegrees(RApol2);
		context.GHApol = Utils.trunc(context.GHApol);
		context.RApol = Math.toDegrees(RApol2);
		context.DECpol = Math.toDegrees(DECpol2);
	}

	public static void starPos(ContextV2 context, String starName) {
		Star star = Star.getStar(starName);
		if (star != null) {
			//Read catalog
			double RAstar0 = 15D * star.getRa();
			double DECstar0 = star.getDec();
			double dRAstar = 15D * star.getDeltaRa() / 3600D;
			double dDECstar = star.getDeltaDec() / 3600D;
			double par = star.getPar() / 3600D;

			//Equatorial coordinates at Julian Date T (mean equinox and equator 2000.0)
			double RAstar1 = RAstar0 + context.TE * dRAstar;
			double DECstar1 = DECstar0 + context.TE * dDECstar;

			//Mean obliquity of ecliptic at 2000.0 in degrees
//    double eps0_2000 = 23.439291111;

			//Transformation to ecliptic coordinates in radians (mean equinox and equator 2000.0)
			double lambdastar1 = Math.atan2((Utils.sind(RAstar1) * Utils.cosd(context.EPS0_2000) + Utils.tand(DECstar1) * Utils.sind(context.EPS0_2000)), Utils.cosd(RAstar1));
			double betastar1 = Math.asin(Utils.sind(DECstar1) * Utils.cosd(context.EPS0_2000) - Utils.cosd(DECstar1) * Utils.sind(context.EPS0_2000) * Utils.sind(RAstar1));

			//Precession
			double eta = Math.toRadians(47.0029 * context.TE - 0.03302 * context.TE2 + 0.00006 * context.TE3) / 3600D;
			double PI0 = Math.toRadians(174.876384 - (869.8089 * context.TE + 0.03536 * context.TE2) / 3600D);
			double p0 = Math.toRadians(5029.0966 * context.TE + 1.11113 * context.TE2 - 0.0000006 * context.TE3) / 3600D;
			double A1 = Math.cos(eta) * Math.cos(betastar1) * Math.sin(PI0 - lambdastar1) - Math.sin(eta) * Math.sin(betastar1);
			double B1 = Math.cos(betastar1) * Math.cos(PI0 - lambdastar1);
			double C1 = Math.cos(eta) * Math.sin(betastar1) + Math.sin(eta) * Math.cos(betastar1) * Math.sin(PI0 - lambdastar1);
			double lambdastar2 = p0 + PI0 - Math.atan2(A1, B1);
			double betastar2 = Math.asin(C1);

			//Annual parallax
			double par_lambda = Math.toRadians(par * Math.sin(Math.toRadians(context.Lsun_true) - lambdastar2) / Math.cos(betastar2));
			double par_beta = -Math.toRadians(par * Math.sin(betastar2) * Math.cos(Math.toRadians(context.Lsun_true) - lambdastar2));

			lambdastar2 += par_lambda;
			betastar2 += par_beta;

			// Nutation in longitude
			lambdastar2 += Math.toRadians(context.delta_psi);

			// Aberration
//    double kappa = Math.toRadians(20.49552) / 3600D;
//    double pi0 = Math.toRadians(102.93735 + 1.71953 * context.TE + 0.00046 * context.TE2);
//    double e = 0.016708617 - 0.000042037 * context.TE - 0.0000001236 * context.TE2;

			double dlambdastar = (context.e * context.kappa * Math.cos(context.pi0 - lambdastar2) - context.kappa * Math.cos(Math.toRadians(context.Lsun_true) - lambdastar2)) / Math.cos(betastar2);
			double dbetastar = -context.kappa * Math.sin(betastar2) * (Math.sin(Math.toRadians(context.Lsun_true) - lambdastar2) - context.e * Math.sin(context.pi0 - lambdastar2));

			lambdastar2 += dlambdastar;
			betastar2 += dbetastar;

			// Transformation back to equatorial coordinates in radians
			double RAstar2 = Math.atan2((Math.sin(lambdastar2) * Utils.cosd(context.eps) - Math.tan(betastar2) * Utils.sind(context.eps)), Math.cos(lambdastar2));
			double DECstar2 = Math.asin(Math.sin(betastar2) * Utils.cosd(context.eps) + Math.cos(betastar2) * Utils.sind(context.eps) * Math.sin(lambdastar2));

			//Lunar distance of star
			context.starMoonDist = Math.toDegrees(Math.acos(Utils.sind(context.DECmoon) * Math.sin(DECstar2) + Utils.cosd(context.DECmoon) * Math.cos(DECstar2) * Utils.cosd(context.RAmoon - Math.toDegrees(RAstar2))));

			// Finals
			context.GHAstar = Utils.trunc(context.GHAAtrue - Math.toDegrees(RAstar2));
			context.SHAstar = Utils.trunc(360 - Math.toDegrees(RAstar2));
			context.DECstar = Math.toDegrees(DECstar2);
		} else
			System.out.println(starName + " not found in the catalog...");
	}

	public static String moonPhase(ContextV2 context) {
		String quarter = "";
		double x = context.lambdaMapp - context.lambda_sun;
		x = Utils.trunc(x);
		x = Math.round(10 * x) / 10;
		if (x == 0)
			quarter = " New";
		if (x > 0 && x < 90)
			quarter = " +cre";
		if (x == 90)
			quarter = " FQ";
		if (x > 90 && x < 180)
			quarter = " +gib";
		if (x == 180)
			quarter = " Full";
		if (x > 180 && x < 270)
			quarter = " -gib";
		if (x == 270)
			quarter = " LQ";
		if (x > 270 && x < 360)
			quarter = " -cre";
		return quarter;
	}

	public static int weekDay(ContextV2 context) {
		return (int) ((context.JD0h + 1.5) - 7 * Math.floor((context.JD0h + 1.5) / 7));
	}
}
