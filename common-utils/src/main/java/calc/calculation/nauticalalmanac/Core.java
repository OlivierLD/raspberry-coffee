package calc.calculation.nauticalalmanac;

public class Core {
	/**
	 * @param year
	 * @param month  1 - Jan, 2 - Feb, etc.
	 * @param day
	 * @param hour
	 * @param minute
	 * @param second
	 * @param deltaT
	 * @return
	 */
	public static void julianDate(int year, int month, int day, int hour, int minute, float second, double deltaT) {
		//var year, month, day, hour, minute, second, Context.dayfraction, ly=0;
		Context.dayfraction = ((double) hour + ((double) minute / 60D) + ((double) second / 3600D)) / 24D;
		// Calculating Julian date, century, and millennium
		//Julian daContext.TE (UT1)
		if (month <= 2) {
			year -= 1;
			month += 12;
		}
		double A = Math.floor(year / 100D);
		double B = 2 - A + Math.floor(A / 4D);
		Context.JD0h = Math.floor(365.25 * (year + 4716D)) + Math.floor(30.6001 * (month + 1)) + day + B - 1524.5;
		Context.JD = Context.JD0h + Context.dayfraction;

		//Julian centuries (UT1) from 2000 January 0.5
		Context.T = (Context.JD - 2451545D) / 36525D;
		Context.T2 = Context.T * Context.T;
		Context.T3 = Context.T * Context.T2;
		Context.T4 = Context.T * Context.T3;
		Context.T5 = Context.T * Context.T4;

		//Julian ephemeris daContext.TE (TDT)
		Context.JDE = Context.JD + deltaT / 86400D;

		//Julian centuries (TDT) from 2000 January 0.5
		Context.TE = (Context.JDE - 2451545D) / 36525D;
		Context.TE2 = Context.TE * Context.TE;
		Context.TE3 = Context.TE * Context.TE2;
		Context.TE4 = Context.TE * Context.TE3;
		Context.TE5 = Context.TE * Context.TE4;

		//Julian millenniums (TDT) from 2000 January 0.5
		Context.Tau = 0.1 * Context.TE;
		Context.Tau2 = Context.Tau * Context.Tau;
		Context.Tau3 = Context.Tau * Context.Tau2;
		Context.Tau4 = Context.Tau * Context.Tau3;
		Context.Tau5 = Context.Tau * Context.Tau4;
	}

	//GHA Aries, GAST, GMST, equation of the equinoxes

	public static void aries() {
		//Mean GHA Aries
		double GHAAmean = Utils.trunc(280.46061837 + 360.98564736629 * (Context.JD - 2451545D) + 0.000387933 * Context.T2 - Context.T3 / 38710000D);

		//GMST
//  SidTm = OutSidTime(GHAAmean);

		//True GHA Aries
		Context.GHAAtrue = Utils.trunc(GHAAmean + Context.delta_psi * Utils.cosd(Context.eps));

		//GAST
//  SidTa = OutSidTime(GHAAtrue);

		//Equation of the equinoxes
		double EoE = 240 * Context.delta_psi * Utils.cosd(Context.eps);
		String EoEout = Double.toString(Math.round(1000 * EoE) / 1000D);
		EoEout = " " + EoEout + "s";
	}


	//Calculations for the Sun
	public static void sun() {
		//Mean longitude of the Sun
		Context.Lsun_mean = Utils.trunc(280.4664567 + 360007.6982779 * Context.Tau + 0.03032028 * Context.Tau2 + Context.Tau3 / 49931D - Context.Tau4 / 15299D - Context.Tau5 / 1988000D);

		//Heliocentric longitude of the Earth
		Context.Le = Earth.lEarth(Context.Tau);

		//Geocentric longitude of the Sun
		Context.Lsun_true = Utils.trunc(Context.Le + 180 - 0.000025);

		//Heliocentric latitude of Earth
		Context.Be = Earth.bEarth(Context.Tau);

		//Geocentric latitude of the Sun
		Context.beta = Utils.trunc(-Context.Be);

		//Corrections
		double Lsun_prime = Utils.trunc(Context.Le + 180 - 1.397 * Context.TE - 0.00031 * Context.TE2);

		Context.beta = Context.beta + 0.000011 * (Utils.cosd(Lsun_prime) - Utils.sind(Lsun_prime));

		//Distance Earth-Sun
		Context.Re = Earth.rEarth(Context.Tau);
		Context.dES = 149597870.691 * Context.Re;

		//Apparent longitude of the Sun
		Context.lambda_sun = Utils.trunc(Context.Lsun_true + Context.delta_psi - 0.005691611 / Context.Re);

		//Right ascension of the Sun, apparent
		Context.RAsun = Math.toDegrees(Utils.trunc2(Math.atan2((Utils.sind(Context.lambda_sun) * Utils.cosd(Context.eps) - Utils.tand(Context.beta) * Utils.sind(Context.eps)), Utils.cosd(Context.lambda_sun))));

		//Declination of the Sun, apparent
		Context.DECsun = Math.toDegrees(Math.asin(Utils.sind(Context.beta) * Utils.cosd(Context.eps) + Utils.cosd(Context.beta) * Utils.sind(Context.eps) * Utils.sind(Context.lambda_sun)));

		//GHA of the Sun
		Context.GHAsun = Utils.trunc(Context.GHAAtrue - Context.RAsun);

		//Semidiameter of the Sun
		Context.SDsun = 959.63 / Context.Re;

		//Horizontal parallax of the Sun
		Context.HPsun = 8.794 / Context.Re;

		//Equation of time
		//EOT = 4*(Lsun_mean-0.0057183-0.0008-RAsun+delta_psi*cosd(eps));
		Context.EoT = 4 * Context.GHAsun + 720 - 1440 * Context.dayfraction;
		if (Context.EoT > 20) Context.EoT -= 1440;
		if (Context.EoT < -20) Context.EoT += 1440;
	}

	public static void polaris() {
		//Equatorial coordinates of Polaris at 2000.0 (mean equinox and equator 2000.0)
		double RApol0 = 37.95293333;
		double DECpol0 = 89.26408889;

		//Proper motion per year
		double dRApol = 2.98155 / 3600D;
		double dDECpol = -0.0152 / 3600D;

		//Equatorial coordinates at Julian DaContext.TE T (mean equinox and equator 2000.0)
		double RApol1 = RApol0 + 100 * Context.TE * dRApol;
		double DECpol1 = DECpol0 + 100 * Context.TE * dDECpol;

		//Mean obliquity of ecliptic at 2000.0 in degrees
		// double eps0_2000 = 23.439291111;

		//Transformation to ecliptic coordinates in radians (mean equinox and equator 2000.0)
		double lambdapol1 = Math.atan2((Utils.sind(RApol1) * Utils.cosd(Context.EPS0_2000) + Utils.tand(DECpol1) * Utils.sind(Context.EPS0_2000)), Utils.cosd(RApol1));
		double betapol1 = Math.asin(Utils.sind(DECpol1) * Utils.cosd(Context.EPS0_2000) - Utils.cosd(DECpol1) * Utils.sind(Context.EPS0_2000) * Utils.sind(RApol1));

		//Precession
		double eta = Math.toRadians(47.0029 * Context.TE - 0.03302 * Context.TE2 + 0.00006 * Context.TE3) / 3600D;
		double PI0 = Math.toRadians(174.876384 - (869.8089 * Context.TE + 0.03536 * Context.TE2) / 3600D);
		double p0 = Math.toRadians(5029.0966 * Context.TE + 1.11113 * Context.TE2 - 0.0000006 * Context.TE3) / 3600D;
		double A1 = Math.cos(eta) * Math.cos(betapol1) * Math.sin(PI0 - lambdapol1) - Math.sin(eta) * Math.sin(betapol1);
		double B1 = Math.cos(betapol1) * Math.cos(PI0 - lambdapol1);
		double C1 = Math.cos(eta) * Math.sin(betapol1) + Math.sin(eta) * Math.cos(betapol1) * Math.sin(PI0 - lambdapol1);
		double lambdapol2 = p0 + PI0 - Math.atan2(A1, B1);
		double betapol2 = Math.asin(C1);

		//Nutation in longitude
		lambdapol2 += Math.toRadians(Context.delta_psi);

		//Aberration
		double dlambdapol = (Context.e * Context.kappa * Math.cos(Context.pi0 - lambdapol2) - Context.kappa * Math.cos(Math.toRadians(Context.Lsun_true) - lambdapol2)) / Math.cos(betapol2);
		double dbetapol = -Context.kappa * Math.sin(betapol2) * (Math.sin(Math.toRadians(Context.Lsun_true) - lambdapol2) - Context.e * Math.sin(Context.pi0 - lambdapol2));

		lambdapol2 += dlambdapol;
		betapol2 += dbetapol;

		//Transformation back to equatorial coordinates in radians
		double RApol2 = Math.atan2((Math.sin(lambdapol2) * Utils.cosd(Context.eps) - Math.tan(betapol2) * Utils.sind(Context.eps)), Math.cos(lambdapol2));
		double DECpol2 = Math.asin(Math.sin(betapol2) * Utils.cosd(Context.eps) + Math.cos(betapol2) * Utils.sind(Context.eps) * Math.sin(lambdapol2));

		//Finals
		Context.GHApol = Context.GHAAtrue - Math.toDegrees(RApol2);
		Context.GHApol = Utils.trunc(Context.GHApol);
		Context.RApol = Math.toDegrees(RApol2);
		Context.DECpol = Math.toDegrees(DECpol2);
	}

	public static void starPos(String starName) {
		Star star = Star.getStar(starName);
		if (star != null) {
			//Read catalog
			double RAstar0 = 15D * star.getRa();
			double DECstar0 = star.getDec();
			double dRAstar = 15D * star.getDeltaRa() / 3600D;
			double dDECstar = star.getDeltaDec() / 3600D;
			double par = star.getPar() / 3600D;

			//Equatorial coordinates at Julian Date T (mean equinox and equator 2000.0)
			double RAstar1 = RAstar0 + Context.TE * dRAstar;
			double DECstar1 = DECstar0 + Context.TE * dDECstar;

			//Mean obliquity of ecliptic at 2000.0 in degrees
//    double eps0_2000 = 23.439291111;

			//Transformation to ecliptic coordinates in radians (mean equinox and equator 2000.0)
			double lambdastar1 = Math.atan2((Utils.sind(RAstar1) * Utils.cosd(Context.EPS0_2000) + Utils.tand(DECstar1) * Utils.sind(Context.EPS0_2000)), Utils.cosd(RAstar1));
			double betastar1 = Math.asin(Utils.sind(DECstar1) * Utils.cosd(Context.EPS0_2000) - Utils.cosd(DECstar1) * Utils.sind(Context.EPS0_2000) * Utils.sind(RAstar1));

			//Precession
			double eta = Math.toRadians(47.0029 * Context.TE - 0.03302 * Context.TE2 + 0.00006 * Context.TE3) / 3600D;
			double PI0 = Math.toRadians(174.876384 - (869.8089 * Context.TE + 0.03536 * Context.TE2) / 3600D);
			double p0 = Math.toRadians(5029.0966 * Context.TE + 1.11113 * Context.TE2 - 0.0000006 * Context.TE3) / 3600D;
			double A1 = Math.cos(eta) * Math.cos(betastar1) * Math.sin(PI0 - lambdastar1) - Math.sin(eta) * Math.sin(betastar1);
			double B1 = Math.cos(betastar1) * Math.cos(PI0 - lambdastar1);
			double C1 = Math.cos(eta) * Math.sin(betastar1) + Math.sin(eta) * Math.cos(betastar1) * Math.sin(PI0 - lambdastar1);
			double lambdastar2 = p0 + PI0 - Math.atan2(A1, B1);
			double betastar2 = Math.asin(C1);

			//Annual parallax
			double par_lambda = Math.toRadians(par * Math.sin(Math.toRadians(Context.Lsun_true) - lambdastar2) / Math.cos(betastar2));
			double par_beta = -Math.toRadians(par * Math.sin(betastar2) * Math.cos(Math.toRadians(Context.Lsun_true) - lambdastar2));

			lambdastar2 += par_lambda;
			betastar2 += par_beta;

			//Nutation in longitude
			lambdastar2 += Math.toRadians(Context.delta_psi);

			//Aberration
//    double kappa = Math.toRadians(20.49552) / 3600D;
//    double pi0 = Math.toRadians(102.93735 + 1.71953 * Context.TE + 0.00046 * Context.TE2);
//    double e = 0.016708617 - 0.000042037 * Context.TE - 0.0000001236 * Context.TE2;

			double dlambdastar = (Context.e * Context.kappa * Math.cos(Context.pi0 - lambdastar2) - Context.kappa * Math.cos(Math.toRadians(Context.Lsun_true) - lambdastar2)) / Math.cos(betastar2);
			double dbetastar = -Context.kappa * Math.sin(betastar2) * (Math.sin(Math.toRadians(Context.Lsun_true) - lambdastar2) - Context.e * Math.sin(Context.pi0 - lambdastar2));

			lambdastar2 += dlambdastar;
			betastar2 += dbetastar;

			//Transformation back to equatorial coordinates in radians
			double RAstar2 = Math.atan2((Math.sin(lambdastar2) * Utils.cosd(Context.eps) - Math.tan(betastar2) * Utils.sind(Context.eps)), Math.cos(lambdastar2));
			double DECstar2 = Math.asin(Math.sin(betastar2) * Utils.cosd(Context.eps) + Math.cos(betastar2) * Utils.sind(Context.eps) * Math.sin(lambdastar2));

			//Lunar distance of star
			Context.starMoonDist = Math.toDegrees(Math.acos(Utils.sind(Context.DECmoon) * Math.sin(DECstar2) + Utils.cosd(Context.DECmoon) * Math.cos(DECstar2) * Utils.cosd(Context.RAmoon - Math.toDegrees(RAstar2))));

			//Finals
			Context.GHAstar = Utils.trunc(Context.GHAAtrue - Math.toDegrees(RAstar2));
			Context.SHAstar = Utils.trunc(360 - Math.toDegrees(RAstar2));
			Context.DECstar = Math.toDegrees(DECstar2);
		} else
			System.out.println(starName + " not found in the catalog...");
	}

	public static String moonPhase() {
		String quarter = "";
		double x = Context.lambdaMapp - Context.lambda_sun;
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

	public static int weekDay() {
		return (int) ((Context.JD0h + 1.5) - 7 * Math.floor((Context.JD0h + 1.5) / 7));
	}

	public static void main(String... args) {
		julianDate(2009, 4, 20, 0, 0, 0f, 65.5);
		System.out.println("DayFraction:" + Context.dayfraction);
		julianDate(2009, 4, 20, 0, 10, 0f, 65.5);
		System.out.println("DayFraction:" + Context.dayfraction);
		julianDate(2009, 4, 20, 0, 20, 0f, 65.5);
		System.out.println("DayFraction:" + Context.dayfraction);
		julianDate(2009, 4, 20, 0, 30, 0f, 65.5);
		System.out.println("DayFraction:" + Context.dayfraction);
		julianDate(2009, 4, 20, 0, 40, 0f, 65.5);
		System.out.println("DayFraction:" + Context.dayfraction);
		julianDate(2009, 4, 20, 0, 50, 0f, 65.5);
		System.out.println("DayFraction:" + Context.dayfraction);
	}
}
