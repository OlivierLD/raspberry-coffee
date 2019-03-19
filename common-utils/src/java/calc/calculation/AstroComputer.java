package calc.calculation;

import calc.calculation.nauticalalmanac.*;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public class AstroComputer {
	private static int year = 0, month = 0, day = 0, hour = 0, minute = 0, second = 0;
	private static double deltaT = 66.4749d; // 2011. Overridden by deltaT system variable.

	// Updated after the calculate invocation.
	public static synchronized double getDeltaT() {
		return deltaT;
	}

	public static synchronized void setDateTime(int y, int m, int d, int h, int mi, int s) {
		year = y;
		month = m;
		day = d;
		hour = h;
		minute = mi;
		second = s;
	}

	/**
	 * Time are UTC
	 *
	 * @param y  year
	 * @param m  Month. Attention: Jan=1, Dec=12 !!!! Does NOT start with 0.
	 * @param d  day
	 * @param h  hour
	 * @param mi minute
	 * @param s  second
	 * @return Phase in Degrees
	 */
	public static synchronized double getMoonPhase(int y, int m, int d, int h, int mi, int s) {
		double phase = 0f;
		year = y;
		month = m;
		day = d;
		hour = h;
		minute = mi;
		second = s;

		calculate();
		phase = Context.lambdaMapp - Context.lambda_sun;
		while (phase < 0d) phase += 360d;
		return phase;
	}

	public static synchronized void calculate(int y, int m, int d, int h, int mi, int s) {
		setDateTime(y, m, d, h, mi, s);
		calculate();
	}

	public static synchronized void calculate() {
		deltaT = Double.parseDouble(System.getProperty("deltaT", Double.toString(deltaT)));
		Core.julianDate(year, month, day, hour, minute, second, deltaT);
		Anomalies.nutation();
		Anomalies.aberration();

		Core.aries();
		Core.sun();

		Moon.compute();

		Venus.compute();
		Mars.compute();
		Jupiter.compute();
		Saturn.compute();
		// Core.polaris();
		Core.moonPhase();
		// Core.weekDay();
	}

	public final static int UTC_RISE_IDX = 0;
	public final static int UTC_SET_IDX = 1;
	public final static int RISE_Z_IDX = 2;
	public final static int SET_Z_IDX = 3;

	/**
	 * The calculate() method must have been invoked before.
	 *
	 * @param latitude
	 * @return the time of rise and set of the body (Sun in that case).
	 * @see http://aa.usno.navy.mil/data/docs/RS_OneYear.php
	 * @see http://www.jgiesen.de/SunMoonHorizon/
	 */
	public static synchronized double[] sunRiseAndSet(double latitude, double longitude) {
		//  out.println("Sun HP:" + Context.HPsun);
		//  out.println("Sun SD:" + Context.SDsun);
		double h0 = (Context.HPsun / 3600d) - (Context.SDsun / 3600d); // - (34d / 60d);
//  System.out.println(">>> DEBUG >>> H0:" + h0 + ", Sin Sun H0:" + Math.sin(Math.toRadians(h0)));
		double cost = Math.sin(Math.toRadians(h0)) - (Math.tan(Math.toRadians(latitude)) * Math.tan(Math.toRadians(Context.DECsun)));
		double t = Math.acos(cost);
		double lon = longitude;

//  while (lon < -180D)
//    lon += 360D;
		//  out.println("Lon:" + lon + ", Eot:" + Context.EoT + " (" + (Context.EoT / 60D) + ")" + ", t:" + Math.toDegrees(t));
		double utRise = 12D - (Context.EoT / 60D) - (lon / 15D) - (Math.toDegrees(t) / 15D);
		double utSet = 12D - (Context.EoT / 60D) - (lon / 15D) + (Math.toDegrees(t) / 15D);

		// Based on http://en.wikipedia.org/wiki/Sunrise_equation
		//double phi = Math.toRadians(latitude);
		//double delta = Math.toRadians(Context.DECsun);
		//double omega = Math.acos(- Math.tan(phi) * Math.tan(delta));
		//utRise = 12D - (Context.EoT / 60D) - (lon / 15D) - (Math.toDegrees(omega) / 15D);
		//utSet  = 12D - (Context.EoT / 60D) - (lon / 15D) + (Math.toDegrees(omega) / 15D);

		double Z = Math.acos((Math.sin(Math.toRadians(Context.DECsun)) + (0.0145 * Math.sin(Math.toRadians(latitude)))) /
				(0.9999 * Math.cos(Math.toRadians(latitude))));
		Z = Math.toDegrees(Z);

		return new double[]{utRise, utSet, Z, 360d - Z};
	}

	/**
	 * @param latitude  in degrees
	 * @param longitude in degrees
	 * @return meridian passage time in hours.
	 */
	public static double getSunMeridianPassageTime(double latitude, double longitude) {
		double t = (12d - (Context.EoT / 60d));
		double deltaG = longitude / 15D;
		return t - deltaG;
	}

	public static synchronized double[] sunRiseAndSet_wikipedia(double latitude, double longitude) {
		double cost = Math.tan(Math.toRadians(latitude)) * Math.tan(Math.toRadians(Context.DECsun));
		double t = Math.acos(cost);
		double lon = longitude;
		double utRise = 12D - (Context.EoT / 60D) - (lon / 15D) - (Math.toDegrees(t) / 15D);
		double utSet = 12D - (Context.EoT / 60D) - (lon / 15D) + (Math.toDegrees(t) / 15D);

		double Z = Math.acos((Math.sin(Math.toRadians(Context.DECsun)) + (0.0145 * Math.sin(Math.toRadians(latitude)))) /
				(0.9999 * Math.cos(Math.toRadians(latitude))));
		Z = Math.toDegrees(Z);

		return new double[]{utRise, utSet, Z, 360d - Z};
	}

	/**
	 * See http://aa.usno.navy.mil/data/docs/RS_OneYear.php
	 * <br>
	 * See http://www.jgiesen.de/SunMoonHorizon/
	 */
	public static synchronized double[] moonRiseAndSet(double latitude, double longitude) {
		//  out.println("Moon HP:" + (Context.HPmoon / 60) + "'");
		//  out.println("Moon SD:" + (Context.SDmoon / 60) + "'");
		double h0 = (Context.HPmoon / 3600d) - (Context.SDmoon / 3600d) - (34d / 60d);
		//  out.println("Moon H0:" + h0);
		double cost = Math.sin(Math.toRadians(h0)) - (Math.tan(Math.toRadians(latitude)) * Math.tan(Math.toRadians(Context.DECmoon)));
		double t = Math.acos(cost);
		double lon = longitude;
		while (lon < -180D)
			lon += 360D;
		//  out.println("Moon Eot:" + Context.moonEoT + " (" + (Context.moonEoT / 60D) + ")" + ", t:" + Math.toDegrees(t));
		double utRise = 12D - (Context.moonEoT / 60D) - (lon / 15D) - (Math.toDegrees(t) / 15D);
		while (utRise < 0)
			utRise += 24;
		while (utRise > 24)
			utRise -= 24;
		double utSet = 12D - (Context.moonEoT / 60D) - (lon / 15D) + (Math.toDegrees(t) / 15D);
		while (utSet < 0)
			utSet += 24;
		while (utSet > 24)
			utSet -= 24;

		return new double[]{utRise, utSet};
	}

	public static synchronized double getMoonIllum() {
		return Context.k_moon;
	}

	public static synchronized void setDeltaT(double deltaT) {
		System.out.println("...DeltaT set to " + deltaT);
		AstroComputer.deltaT = deltaT;
	}

	public static final synchronized double getTimeZoneOffsetInHours(TimeZone tz) {
		return getTimeZoneOffsetInHours(tz, new Date());
	}

	public static final synchronized double getTimeZoneOffsetInHours(TimeZone tz, Date when) {
		double d = 0;
		if (false) {
			SimpleDateFormat sdf = new SimpleDateFormat("Z");
			sdf.setTimeZone(tz);
			String s = sdf.format(new Date());
			if (s.startsWith("+"))
				s = s.substring(1);
			int i = Integer.parseInt(s);
			d = (int) (i / 100);
			int m = (int) (i % 100);
			d += (m / 60d);
		} else
			d = (tz.getOffset(when.getTime()) / (3600d * 1000d));

		return d;
	}

	public static final synchronized double getTimeOffsetInHours(String timeOffset) {
//  System.out.println("Managing:" + timeOffset);
		double d = 0d;
		String[] hm = timeOffset.split(":");
		int h = Integer.parseInt(hm[0]);
		int m = Integer.parseInt(hm[1]);
		if (h > 0)
			d = h + (m / 60d);
		if (h < 0)
			d = h - (m / 60d);
		return d;
	}

	public final static int SUN_ALT_IDX = 0;
	public final static int SUN_Z_IDX = 1;
	public final static int MOON_ALT_IDX = 2;
	public final static int MOON_Z_IDX = 3;
	public final static int LHA_ARIES_IDX = 4;

	public static synchronized double[] getSunMoon(int y, int m, int d, int h, int mi, int s, double lat, double lng) {
		double[] values = new double[5];
		year = y;
		month = m;
		day = d;
		hour = h;
		minute = mi;
		second = s;

		calculate();
		SightReductionUtil sru = new SightReductionUtil();
		sru.setL(lat);
		sru.setG(lng);

		// Sun
		sru.setAHG(Context.GHAsun);
		sru.setD(Context.DECsun);
		sru.calculate();
		values[SUN_ALT_IDX] = sru.getHe();
		values[SUN_Z_IDX] = sru.getZ();
		// Moon
		sru.setAHG(Context.GHAmoon);
		sru.setD(Context.DECmoon);
		sru.calculate();
		values[MOON_ALT_IDX] = sru.getHe();
		values[MOON_Z_IDX] = sru.getZ();

		double ahl = Context.GHAAtrue + lng;
		while (ahl < 0.0)
			ahl += 360.0;
		while (ahl > 360.0)
			ahl -= 360.0;
		values[LHA_ARIES_IDX] = ahl;

		return values;
	}

	public static synchronized double getSunAlt(int y, int m, int d, int h, int mi, int s, double lat, double lng) {
		double value = 0d;
		year = y;
		month = m;
		day = d;
		hour = h;
		minute = mi;
		second = s;

		calculate();
		SightReductionUtil sru = new SightReductionUtil();
		sru.setL(lat);
		sru.setG(lng);

		// Sun
		sru.setAHG(Context.GHAsun);
		sru.setD(Context.DECsun);
		sru.calculate();
		value = sru.getHe();

		return value;
	}

	public static synchronized double getMoonAlt(int y, int m, int d, int h, int mi, int s, double lat, double lng) {
		double value = 0d;
		year = y;
		month = m;
		day = d;
		hour = h;
		minute = mi;
		second = s;

		calculate();
		SightReductionUtil sru = new SightReductionUtil();
		sru.setL(lat);
		sru.setG(lng);

		// Moon
		sru.setAHG(Context.GHAmoon);
		sru.setD(Context.DECmoon);
		sru.calculate();
		value = sru.getHe();

		return value;
	}

	public final static int HE_SUN_IDX = 0;
	public final static int HE_MOON_IDX = 1;
	public final static int DEC_SUN_IDX = 2;
	public final static int DEC_MOON_IDX = 3;
	public final static int MOON_PHASE_IDX = 4;

	/**
	 * Returns Altitude and Declination, for Sun and Moon,
	 * for a given UTC time, at a given location.
	 *
	 * @param y year
	 * @param m month (like Calendar.MONTH + 1, Jan is 1)
	 * @param d day of the month
	 * @param h hour of the day
	 * @param mi minutes
	 * @param s seconds
	 * @param lat latitude
	 * @param lng longitude
	 * @return an array of 4 doubles. See HE_SUN_IDX, HE_MOON_IDX, DEC_SUN_IDX and DEC_MOON_IDX.
	 */
	public static synchronized double[] getSunMoonAltDecl(int y, int m, int d, int h, int mi, int s, double lat, double lng) {
		double[] values = new double[5];
		year = y;
		month = m;
		day = d;
		hour = h;
		minute = mi;
		second = s;

//  System.out.println(y + "-" + month + "-" + day + " " + h + ":" + mi + ":" + s);

		calculate();
		SightReductionUtil sru = new SightReductionUtil();
		sru.setL(lat);
		sru.setG(lng);

		// Sun
		sru.setAHG(Context.GHAsun);
		sru.setD(Context.DECsun);
		sru.calculate();
		values[HE_SUN_IDX] = sru.getHe();
		// Moon
		sru.setAHG(Context.GHAmoon);
		sru.setD(Context.DECmoon);
		sru.calculate();
		values[HE_MOON_IDX] = sru.getHe();

		values[DEC_SUN_IDX] = Context.DECsun;
		values[DEC_MOON_IDX] = Context.DECmoon;

		double moonPhase = getMoonPhase(y, m, d, h, m, s);
		values[MOON_PHASE_IDX] = moonPhase;

		return values;
	}

	/**
	 * Warning: Context must have been initialized!
	 *
	 * @return
	 */
	public static synchronized double getSunDecl() {
		return Context.DECsun;
	}

	public static synchronized double getSunGHA() {
		return Context.GHAsun;
	}

	/**
	 * Warning: Context must have been initialized!
	 *
	 * @return
	 */
	public static synchronized double getMoonDecl() {
		return Context.DECmoon;
	}

	public static synchronized double getMoonGHA() {
		return Context.GHAmoon;
	}

	public static synchronized double getVenusDecl() {
		return Context.DECvenus;
	}

	public static synchronized double getMarsDecl() {
		return Context.DECmars;
	}

	public static synchronized double getJupiterDecl() {
		return Context.DECjupiter;
	}

	public static synchronized double getSaturnDecl() {
		return Context.DECsaturn;
	}

	public static synchronized double getAriesGHA() {
		return Context.GHAAtrue;
	}

	public static synchronized double getVenusGHA() {
		return Context.GHAvenus;
	}

	public static synchronized double getMarsGHA() {
		return Context.GHAmars;
	}

	public static synchronized double getJupiterGHA() {
		return Context.GHAjupiter;
	}

	public static synchronized double getSaturnGHA() {
		return Context.GHAsaturn;
	}

	public static synchronized double getMeanObliquityOfEcliptic() {
		return Context.eps0;
	}

	public static synchronized double ghaToLongitude(double gha) {
		double longitude = 0;
		if (gha < 180)
			longitude = -gha;
		if (gha >= 180)
			longitude = 360 - gha;
		return longitude;
	}

	public static void main(String... args) {
		System.out.println("Moon phase:" + getMoonPhase(2011, 8, 22, 12, 00, 00));
		System.out.println("TimeOffset:" + getTimeOffsetInHours("-09:30"));
		String[] tz = new String[]{"Pacific/Marquesas", "America/Los_Angeles", "GMT", "Europe/Paris", "Europe/Moscow", "Australia/Sydney", "Australia/Adelaide"};
		for (int i = 0; i < tz.length; i++)
			System.out.println("TimeOffset for " + tz[i] + ":" + getTimeZoneOffsetInHours(TimeZone.getTimeZone(tz[i])));

		System.out.println("TZ:" + TimeZone.getTimeZone(tz[0]).getDisplayName() + ", " + (TimeZone.getTimeZone(tz[0]).getOffset(new Date().getTime()) / (3_600d * 1_000)));

		String timeZone = "America/Los_Angeles";
		Calendar cal = GregorianCalendar.getInstance();
		System.out.println("On " + cal.getTime() + ", TimeOffset for " + timeZone + ":" + getTimeZoneOffsetInHours(TimeZone.getTimeZone(timeZone), cal.getTime()));
		double d = TimeZone.getTimeZone(timeZone).getOffset(cal.getTime().getTime()) / (3_600d * 1_000d);
//  System.out.println("TimeOffset for " + timeZone + ":" +  d);
		cal.set(Calendar.MONTH, Calendar.DECEMBER);
		cal.getTime();
		System.out.println("On " + cal.getTime() + ", TimeOffset for " + timeZone + ":" + getTimeZoneOffsetInHours(TimeZone.getTimeZone(timeZone), cal.getTime()));
		d = TimeZone.getTimeZone(timeZone).getOffset(cal.getTime().getTime()) / (3_600d * 1_000d);
//  System.out.println("TimeOffset for " + timeZone + ":" +  d);
	}
}
