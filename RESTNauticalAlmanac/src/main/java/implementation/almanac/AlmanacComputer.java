package implementation.almanac;


import java.io.PrintStream;

import java.util.Calendar;
import java.util.Date;

import calc.calculation.nauticalalmanac.Anomalies;
import calc.calculation.nauticalalmanac.Context;
import calc.calculation.nauticalalmanac.Core;
import calc.calculation.nauticalalmanac.Jupiter;
import calc.calculation.nauticalalmanac.Mars;
import calc.calculation.nauticalalmanac.Moon;
import calc.calculation.nauticalalmanac.Saturn;
import calc.calculation.nauticalalmanac.Star;
import calc.calculation.nauticalalmanac.Venus;

import calc.GeomUtil;

public class AlmanacComputer {
	private final static boolean verbose = true;

	private static int year = 0, month = 0, day = 0, hour = 0, minute = 0;
	private static float second = 0f, deltaT = 0f;

	public final static int JANUARY = 1;
	public final static int FEBRUARY = 2;
	public final static int MARCH = 3;
	public final static int APRIL = 4;
	public final static int MAY = 5;
	public final static int JUNE = 6;
	public final static int JULY = 7;
	public final static int AUGUST = 8;
	public final static int SEPTEMBER = 9;
	public final static int OCTOBER = 10;
	public final static int NOVEMBER = 11;
	public final static int DECEMBER = 12;

	private static void displayHelp() {
		System.out.println("-- Parameters --");
		System.out.println("Optional: -help");
		System.out.println("Generates the output you are reading.");
		System.out.println("Mandatory: -type can be 'continuous' or 'from-to'");
		System.out.println("----------------------------------------");
		System.out.println("- if type is 'continuous' --------------");
		System.out.println(" you must provide -year ");
		System.out.println(" then you can provide -month ");
		System.out.println(" then you can provide -day ");
		System.out.println("Example: " + AlmanacComputer.class.getName() + " -type continuous -year 2009");
		System.out.println("  would generate the almanac for the whole 2009 year");
		System.out.println("Example: " + AlmanacComputer.class.getName() + " -type continuous -year 2009 -month 2");
		System.out.println("  would generate the almanac for the whole month of February 2009");
		System.out.println("Example: " + AlmanacComputer.class.getName() + " -type continuous -year 2009 -month 2 -day 20");
		System.out.println("  would generate the almanac for February the 20th, 2009");
		System.out.println("----------------------------------------");
		System.out.println("- if type is 'from-to' -----------------");
		System.out.println(" you must provide -from-year ");
		System.out.println(" you must provide -from-month ");
		System.out.println(" you must provide -from-day ");
		System.out.println(" you must provide -to-year ");
		System.out.println(" you must provide -to-month ");
		System.out.println(" you must provide -to-day ");
		System.out.println("Example: " + AlmanacComputer.class.getName() + " -type from-to -from-year 2010 -from-month 6 -from-day 25 -to-year 2011 -to-month 5 -to-day 31 ");
		System.out.println("  would generate the almanac between June the 25th, 2010 and May the 31st, 2011");
		System.out.println("----------------------------------------");
	}

	private static PrintStream out = null;

	/*
	 * An example of a main.
	 * Generates the xml output for the requested period.
	 */
	public static void main(String... args) {
		String help = getPrm(args, "-help");
		if (help != null) {
			displayHelp();
			System.exit(0);
		}

		String output = getPrm(args, "-out");
		if (output == null)
			out = System.out;
		else {
			try {
				out = new PrintStream(output);
			} catch (Exception ex) {
				System.out.println("Generating output:");
				System.out.println(ex.toString());
			}
		}
		String type = getPrm(args, "-type"); // continuous or from-to
		if (type == null) {
			displayHelp();
			throw new RuntimeException("-type parameter is mandatory.\nIt can be 'continuous' from 'from-to'");
		}

		String yearPrm = getPrm(args, "-year");
		String monthPrm = getPrm(args, "-month");
		String dayPrm = getPrm(args, "-day");

		String fromYearPrm = getPrm(args, "-from-year");
		String fromMonthPrm = getPrm(args, "-from-month");
		String fromDayPrm = getPrm(args, "-from-day");

		String toYearPrm = getPrm(args, "-to-year");
		String toMonthPrm = getPrm(args, "-to-month");
		String toDayPrm = getPrm(args, "-to-day");

		minute = 0;
		second = 0;
		/*
		 * 68.8033: June 1, 2017
		 * See http://maia.usno.navy.mil/ser7/ser7.dat
		 *     http://maia.usno.navy.mil/
		 *     http://maia.usno.navy.mil/ser7/deltat.data
		 */
		deltaT = Float.parseFloat(System.getProperty("deltaT", "68.8033"));
//  out.println("DeltaT:" + deltaT);
		if (yearPrm == null && "continuous".equals(type)) {
			displayHelp();
			throw new RuntimeException("Must have a year...");
		}

		int fromYear = 0, fromMonth = 0, fromDay = 0, toYear = 0, toMonth = 0, toDay = 0;

		if ("from-to".equals(type)) {
			if ((fromYearPrm == null || fromYearPrm.trim().isEmpty()) ||
					(fromMonthPrm == null || fromMonthPrm.trim().isEmpty()) ||
					(fromDayPrm == null || fromDayPrm.trim().isEmpty()) ||
					(toYearPrm == null || toYearPrm.trim().isEmpty()) ||
					(toMonthPrm == null || toMonthPrm.trim().isEmpty()) ||
					(toDayPrm == null || toDayPrm.trim().isEmpty())) {
				displayHelp();
				throw new RuntimeException("Some mandatory parameter(s) missing.");
			}
			try {
				fromYear = Integer.parseInt(fromYearPrm);
				fromMonth = Integer.parseInt(fromMonthPrm);
				fromDay = Integer.parseInt(fromDayPrm);

				toYear = Integer.parseInt(toYearPrm);
				toMonth = Integer.parseInt(toMonthPrm);
				toDay = Integer.parseInt(toDayPrm);

				Calendar cal = Calendar.getInstance();
				cal.set(Calendar.YEAR, fromYear);
				cal.set(Calendar.MONTH, fromMonth - 1);
				cal.set(Calendar.DAY_OF_MONTH, fromDay);
				Date f = cal.getTime();

				cal.set(Calendar.YEAR, toYear);
				cal.set(Calendar.MONTH, toMonth - 1);
				cal.set(Calendar.DAY_OF_MONTH, toDay);
				Date t = cal.getTime();

				if (f.compareTo(t) > 0) {
					throw new RuntimeException(String.format("Wrong chronology for the dates (from: %s and to: %s).", f.toString(), t.toString()));
				}
			} catch (NumberFormatException nfe) {
				throw nfe;
			}
		}
		int startYear = 0;
		int startMonth = JANUARY, endMonth = DECEMBER;
		int startDay = 1, endDay = 0;

		if ("continuous".equals(type))
			startYear = Integer.parseInt(yearPrm);
		else {
			startYear = fromYear;
			startMonth = fromMonth;
			startDay = fromDay;
		}
		if (monthPrm != null && "continuous".equals(type)) {
			startMonth = Integer.parseInt(monthPrm);
			endMonth = startMonth;
		}
		if (dayPrm != null && "continuous".equals(type)) {
			startDay = Integer.parseInt(dayPrm);
			endDay = startDay;
		}
		initPrevValues();

		boolean continuous = "continuous".equals(type);
		boolean fromTo = "from-to".equals(type);

		out.println("<almanac xmlns='urn:nautical-almanac' deltaT='" + deltaT + "' type='" + type + "'>");
		boolean keepLooping = true;
		year = startYear;
		while (keepLooping) {
			out.println("<year value='" + year + "'>");
			for (int m = (startMonth - 1); keepLooping && ((fromTo && m < 12) || (continuous && m <= (endMonth - 1))); m++) {
				month = m + 1;
//      while (month > 12) month -= 12;
				out.println("<month value='" + month + "'>");
				if (dayPrm == null)
					endDay = getNbDays(year, month);
				for (int d = (startDay - 1); keepLooping && d < endDay; d++) {
					day = d + 1;
					if (verbose) System.out.println("Computing " + year + "-" + month + "-" + day);
					out.println("<day value='" + day + "'>");
					for (int h = 0; h <= 24; h++) {
						hour = h;
						AlmanacComputer.calculate();
						AlmanacComputer.xmlOutput();
					}
					out.println("</day>");
					if ("from-to".equals(type)) {
						Calendar cal = Calendar.getInstance();
						cal.set(Calendar.YEAR, year);
						cal.set(Calendar.MONTH, m);
						cal.set(Calendar.DAY_OF_MONTH, day);
						Date current = cal.getTime();

						cal.set(Calendar.YEAR, toYear);
						cal.set(Calendar.MONTH, toMonth - 1);
						cal.set(Calendar.DAY_OF_MONTH, toDay);
						Date t = cal.getTime();

						if (current.compareTo(t) >= 0)
							keepLooping = false;
					}
				} // Day loop
				out.println("</month>");
				if (fromTo)
					startDay = 1;
			} // Month loop
			out.println("</year>");
			if (continuous)
				keepLooping = false;
			else {
				year += 1;
				startMonth = JANUARY;
			}
		}
		out.println("</almanac>");
	}

	public static void main_1(String... args) {
		year = 2012;
		month = SEPTEMBER;
		day = 21;
		hour = 24;
		deltaT = 66.7708f;

		AlmanacComputer.calculate();

		System.out.println("-- 2012-09-21 24:00:00 --");
		System.out.println("GHA Sun: " + Context.GHAsun);
		System.out.println("D Sun: " + Context.DECsun);

		day = 22;
		hour = 00;

		AlmanacComputer.calculate();

		System.out.println("-- 2012-09-22 00:00:00 --");
		System.out.println("GHA Sun: " + Context.GHAsun);
		System.out.println("D Sun: " + Context.DECsun);
	}

	private static final String getPrm(String[] args, String prm) {
		String ret = null;
		for (int i = 0; i < args.length; i++) {
			if (args[i].equals(prm)) {
				ret = "";
				try {
					ret = args[i + 1];
				} catch (ArrayIndexOutOfBoundsException aioobe) {
					System.err.println("No value for " + prm + "?");
					System.err.println(aioobe.toString());
				}
				break;
			}
		}
		return ret;
	}

	private static final int[] dayPerMonth = new int[]
			{31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};

	public static int getNbDays(int y, int m) {
		int nd = 0;
		if (m != 2)
			nd = dayPerMonth[m - 1];
		else {
			nd = 28;
			boolean leap = false;
			if (y % 4 == 0) // Leap
			{
				leap = true;
				if (y % 100 == 0) // Not leap
				{
					leap = false;
					if (y % 400 == 0)
						leap = true;
				}
			}
			if (leap)
				nd = 29;
		}
		return nd;
	}

	public static void calculate(int y, int m, int d, int h, int mi, float s, double dT) {
		year = y;
		month = m;
		day = d;
		hour = h;
		minute = mi;
		second = s;
		deltaT = (float) dT;
		calculate();
	}

	public static void calculate() {
		Core.julianDate(year, month, day, hour, minute, second, deltaT);
		Anomalies.nutation();
		Anomalies.aberration();

		Core.aries();
		Core.sun();

		Moon.compute(); // Important! Moon is used for lunar distances, by planets and stars.

		Venus.compute();
		Mars.compute();
		Jupiter.compute();
		Saturn.compute();

		Core.polaris();
		Core.moonPhase();
		Core.weekDay();
	}

	private final static int SUN = 0;
	private final static int MOON = 1;
	private final static int VENUS = 2;
	private final static int MARS = 3;
	private final static int JUPITER = 4;
	private final static int SATURN = 5;
	private final static int ARIES = 6;

	private final static int NB_BODIES = 7;

	private static double[] prevGHA = new double[NB_BODIES];
	private static double[] prevDec = new double[NB_BODIES];

	private static double[] prevLunar = new double[NB_BODIES];
	private static double[] prevStarLunars = new double[Star.getCatalog().length];

	private static double prevEOT = Double.MAX_VALUE;

	private final static void initPrevValues() {
		for (int i = 0; i < prevGHA.length; i++) {
			prevGHA[i] = Double.MAX_VALUE;
			prevDec[i] = Double.MAX_VALUE;
			prevLunar[i] = Double.MAX_VALUE;
		}
		for (int i = 0; i < prevStarLunars.length; i++)
			prevStarLunars[i] = Double.MAX_VALUE;
	}

	private static void xmlOutput() {
		double[] deltaGHA = new double[]{0d, 0d, 0d, 0d, 0d, 0d, 0d};
		double[] deltaD = new double[]{0d, 0d, 0d, 0d, 0d, 0d, 0d};

		double[] deltaLunar = new double[]{0d, 0d, 0d, 0d, 0d, 0d};
		double[] deltaStarLunar = new double[Star.getCatalog().length];
		for (int i = 0; i < Star.getCatalog().length; i++)
			deltaStarLunar[i] = 0d;

		double deltaEOT = 0d;

		out.println("<data hour='" + hour + "' minute='" + minute + "' second='" + second + "'>");
		int bodyIndex = SUN;
		if (prevGHA[bodyIndex] != Double.MAX_VALUE) {
			deltaGHA[bodyIndex] = Context.GHAsun - prevGHA[bodyIndex];
			deltaD[bodyIndex] = Math.abs(Context.DECsun - prevDec[bodyIndex]);
			while (deltaGHA[bodyIndex] < 0) deltaGHA[bodyIndex] += 360d;

			deltaLunar[bodyIndex] = Math.abs(Context.LDist - prevLunar[bodyIndex]);

			deltaEOT = Context.EoT - prevEOT;
		}
		//Sun
		out.println("<body name='Sun'");
		out.println("      GHA='" + Context.GHAsun + "'");
		// SHA
		double shaSun = Context.GHAAtrue - Context.GHAsun;
		while (shaSun < 0) shaSun += 360;
		while (shaSun > 360) shaSun += 360;
		out.println("      SHA='" + shaSun + "'");
		out.println("      varGHA='" + ((prevGHA[bodyIndex] != Double.MAX_VALUE) ? deltaGHA[bodyIndex] : "") + "'");
		out.println("      RA='" + Context.RAsun + "'");
		out.println("      Dec='" + Context.DECsun + "'");
		out.println("      varD='" + ((prevGHA[bodyIndex] != Double.MAX_VALUE) ? deltaD[bodyIndex] : "") + "'");
		out.println("      sd-minute='" + (Context.SDsun / 60.0) + "'");
		out.println("      hp-minute='" + (Context.HPsun / 60.0) + "'");
		//Equation of time
		out.println("      EoT-in-minutes='" + Context.EoT + "'");
		out.println("      delta-EoT-in-minutes='" + (prevEOT != Double.MAX_VALUE ? deltaEOT : "") + "'");
		out.println("      t-pass-in-hours='" + (12f - (Context.EoT / 60f)) + "'");
		out.println("      moonDist='" + Context.LDist + "'");
		out.println("      delta-lunar='" + ((prevLunar[bodyIndex] != Double.MAX_VALUE) ? deltaLunar[bodyIndex] : "") + "'");

		if (hour < 24) {
			prevGHA[bodyIndex] = Context.GHAsun;
			prevDec[bodyIndex] = Context.DECsun;

			prevLunar[bodyIndex] = Context.LDist;

			prevEOT = Context.EoT;
		}
		String sign = "";
		if (Context.EoT < 0)
			sign = "-";
		else
			sign = "+";
		Context.EoT = Math.abs(Context.EoT);
		int EOTmin = (int) Math.floor(Context.EoT);
		int EOTsec = (int) Math.round(600 * (Context.EoT - EOTmin)) / 10;
		if (EOTmin == 0)
			out.print("      eot='" + sign + EOTsec + "s'");
		else
			out.print("      eot='" + sign + EOTmin + "m " + EOTsec + "s'");

		out.println("/>");

		bodyIndex = MOON;
		if (prevGHA[bodyIndex] != Double.MAX_VALUE) {
			deltaGHA[bodyIndex] = Context.GHAmoon - prevGHA[bodyIndex];
			deltaD[bodyIndex] = Math.abs(Context.DECmoon - prevDec[bodyIndex]);
			while (deltaGHA[bodyIndex] < 0) deltaGHA[bodyIndex] += 360d;
		}
		//Moon
		out.println("<body name='Moon'");
		out.println("      GHA='" + Context.GHAmoon + "'");
		// SHA
		double shaMoon = Context.GHAAtrue - Context.GHAmoon;
		while (shaMoon < 0) shaMoon += 360;
		while (shaMoon > 360) shaMoon += 360;
		out.println("      SHA='" + shaMoon + "'");
		out.println("      varGHA='" + ((prevGHA[bodyIndex] != Double.MAX_VALUE) ? deltaGHA[bodyIndex] : "") + "'");
		out.println("      RA='" + Context.RAmoon + "'");
		out.println("      Dec='" + Context.DECmoon + "'");
		out.println("      varD='" + ((prevGHA[bodyIndex] != Double.MAX_VALUE) ? deltaD[bodyIndex] : "") + "'");
		out.println("      sd-minute='" + (Context.SDmoon / 60.0) + "'");
		out.println("      hp-minute='" + (Context.HPmoon / 60.0) + "'");
		out.println("      illum='" + Context.k_moon + "%" + Core.moonPhase() + "'");
//  out.println("      phase-in-degrees='" + Context.moonPhase + "'");
		out.println("      sun-moon='" + Context.LDist + "'");
		double phase = Context.lambdaMapp - Context.lambda_sun;
		if (phase < 0d) phase += 360d;
		out.println("      phase-in-degrees='" + phase + "'");
		out.println("      age-in-days='" + (phase * 28D / 360D) + "'");
		//Equation of time
//  out.println("      EoT-in-hours='" + (Context.moonEoT / 60d) + "'");
//  out.println("      EoT-in-minutes='" + Context.moonEoT + "'");
//  out.println("      t-pass-in-hours='" + (12f - (Context.moonEoT / 60f)) + "'");
		sign = "";
		if (Context.moonEoT < 0)
			sign = "-";
		else
			sign = "+";
		Context.moonEoT = Math.abs(Context.moonEoT);
		int EOThour = (int) Math.floor(Context.moonEoT / 60d);
		EOTmin = (int) Math.floor(Context.moonEoT - (EOThour * 60));
		EOTsec = (int) Math.round(600 * (Context.moonEoT - (EOThour * 60) - EOTmin)) / 10;
		out.print("      eot='" + sign + EOThour + "h " + EOTmin + "m " + EOTsec + "s'");

		out.println("/>");
		if (hour < 24) {
			prevGHA[bodyIndex] = Context.GHAmoon;
			prevDec[bodyIndex] = Context.DECmoon;
		}

		//Aries
		bodyIndex = ARIES;
		if (prevGHA[bodyIndex] != Double.MAX_VALUE) {
			deltaGHA[bodyIndex] = Context.GHAAtrue - prevGHA[bodyIndex];
			while (deltaGHA[bodyIndex] < 0) deltaGHA[bodyIndex] += 360d;
		}
		out.println("<body name='Aries'");
		out.println("      GHA='" + Context.GHAAtrue + "'");
		out.println("      varGHA='" + ((prevGHA[bodyIndex] != Double.MAX_VALUE) ? deltaGHA[bodyIndex] : "") + "'/>");
		if (hour < 24) {
			prevGHA[bodyIndex] = Context.GHAAtrue;
		}

		out.println("<planets>");
		//Venus
		bodyIndex = VENUS;
		if (prevGHA[bodyIndex] != Double.MAX_VALUE) {
			deltaGHA[bodyIndex] = Context.GHAvenus - prevGHA[bodyIndex];
			deltaD[bodyIndex] = Math.abs(Context.DECvenus - prevDec[bodyIndex]);
			while (deltaGHA[bodyIndex] < 0) deltaGHA[bodyIndex] += 360d;
			deltaLunar[bodyIndex] = Math.abs(Context.moonVenusDist - prevLunar[bodyIndex]);
		}
		out.println("<body name='Venus'");
		out.println("      GHA='" + Context.GHAvenus + "'");
		// SHA
		double shaVenus = Context.GHAAtrue - Context.GHAvenus;
		while (shaVenus < 0) shaVenus += 360;
		while (shaVenus > 360) shaVenus += 360;
		out.println("      SHA='" + shaVenus + "'");
		out.println("      varGHA='" + ((prevGHA[bodyIndex] != Double.MAX_VALUE) ? deltaGHA[bodyIndex] : "") + "'");
		out.println("      RA='" + Context.RAvenus + "'");
		out.println("      Dec='" + Context.DECvenus + "'");
		out.println("      varD='" + ((prevGHA[bodyIndex] != Double.MAX_VALUE) ? deltaD[bodyIndex] : "") + "'");
		out.println("      sd='" + Context.SDvenus + "'");
		out.println("      hp='" + Context.HPvenus + "'");
		out.println("      moonDist='" + Context.moonVenusDist + "'");
		out.println("      delta-lunar='" + ((prevLunar[bodyIndex] != Double.MAX_VALUE) ? deltaLunar[bodyIndex] : "") + "'");
		out.println("      illum='" + Context.k_venus + "%'/>");
		if (hour < 24) {
			prevGHA[bodyIndex] = Context.GHAvenus;
			prevDec[bodyIndex] = Context.DECvenus;
			prevLunar[bodyIndex] = Context.moonVenusDist;
		}

		//Mars
		bodyIndex = MARS;
		if (prevGHA[bodyIndex] != Double.MAX_VALUE) {
			deltaGHA[bodyIndex] = Context.GHAmars - prevGHA[bodyIndex];
			deltaD[bodyIndex] = Math.abs(Context.DECmars - prevDec[bodyIndex]);
			while (deltaGHA[bodyIndex] < 0) deltaGHA[bodyIndex] += 360d;
			deltaLunar[bodyIndex] = Math.abs(Context.moonMarsDist - prevLunar[bodyIndex]);
		}
		out.println("<body name='Mars'");
		out.println("      GHA='" + Context.GHAmars + "'");
		// SHA
		double shaMars = Context.GHAAtrue - Context.GHAmars;
		while (shaMars < 0) shaMars += 360;
		while (shaMars > 360) shaMars += 360;
		out.println("      SHA='" + shaMars + "'");
		out.println("      varGHA='" + ((prevGHA[bodyIndex] != Double.MAX_VALUE) ? deltaGHA[bodyIndex] : "") + "'");
		out.println("      RA='" + Context.RAmars + "'");
		out.println("      Dec='" + Context.DECmars + "'");
		out.println("      varD='" + ((prevGHA[bodyIndex] != Double.MAX_VALUE) ? deltaD[bodyIndex] : "") + "'");
		out.println("      sd='" + Context.SDmars + "'");
		out.println("      hp='" + Context.HPmars + "'");
		out.println("      moonDist='" + Context.moonMarsDist + "'");
		out.println("      delta-lunar='" + ((prevLunar[bodyIndex] != Double.MAX_VALUE) ? deltaLunar[bodyIndex] : "") + "'");
		out.println("      illum='" + Context.k_mars + "%'/>");
		if (hour < 24) {
			prevGHA[bodyIndex] = Context.GHAmars;
			prevDec[bodyIndex] = Context.DECmars;
			prevLunar[bodyIndex] = Context.moonMarsDist;
		}

		//Jupiter
		bodyIndex = JUPITER;
		if (prevGHA[bodyIndex] != Double.MAX_VALUE) {
			deltaGHA[bodyIndex] = Context.GHAjupiter - prevGHA[bodyIndex];
			deltaD[bodyIndex] = Math.abs(Context.DECjupiter - prevDec[bodyIndex]);
			while (deltaGHA[bodyIndex] < 0) deltaGHA[bodyIndex] += 360d;
			deltaLunar[bodyIndex] = Math.abs(Context.moonJupiterDist - prevLunar[bodyIndex]);
		}
		out.println("<body name='Jupiter'");
		out.println("      GHA='" + Context.GHAjupiter + "'");
		// SHA
		double shaJupiter = Context.GHAAtrue - Context.GHAjupiter;
		while (shaJupiter < 0) shaJupiter += 360;
		while (shaJupiter > 360) shaJupiter += 360;
		out.println("      SHA='" + shaJupiter + "'");
		out.println("      varGHA='" + ((prevGHA[bodyIndex] != Double.MAX_VALUE) ? deltaGHA[bodyIndex] : "") + "'");
		out.println("      RA='" + Context.RAjupiter + "'");
		out.println("      Dec='" + Context.DECjupiter + "'");
		out.println("      varD='" + ((prevGHA[bodyIndex] != Double.MAX_VALUE) ? deltaD[bodyIndex] : "") + "'");
		out.println("      sd='" + Context.SDjupiter + "'");
		out.println("      hp='" + Context.HPjupiter + "'");
		out.println("      moonDist='" + Context.moonJupiterDist + "'");
		out.println("      delta-lunar='" + ((prevLunar[bodyIndex] != Double.MAX_VALUE) ? deltaLunar[bodyIndex] : "") + "'");
		out.println("      illum='" + Context.k_jupiter + "%'/>");
		if (hour < 24) {
			prevGHA[bodyIndex] = Context.GHAjupiter;
			prevDec[bodyIndex] = Context.DECjupiter;
			prevLunar[bodyIndex] = Context.moonJupiterDist;
		}

		//Saturn
		bodyIndex = SATURN;
		if (prevGHA[bodyIndex] != Double.MAX_VALUE) {
			deltaGHA[bodyIndex] = Context.GHAsaturn - prevGHA[bodyIndex];
			deltaD[bodyIndex] = Math.abs(Context.DECsaturn - prevDec[bodyIndex]);
			while (deltaGHA[bodyIndex] < 0) deltaGHA[bodyIndex] += 360d;
			deltaLunar[bodyIndex] = Math.abs(Context.moonSaturnDist - prevLunar[bodyIndex]);
		}
		out.println("<body name='Saturn'");
		out.println("      GHA='" + Context.GHAsaturn + "'");
		// SHA
		double shaSatrun = Context.GHAAtrue - Context.GHAsaturn;
		while (shaSatrun < 0) shaSatrun += 360;
		while (shaSatrun > 360) shaSatrun += 360;
		out.println("      SHA='" + shaSatrun + "'");
		out.println("      varGHA='" + ((prevGHA[bodyIndex] != Double.MAX_VALUE) ? deltaGHA[bodyIndex] : "") + "'");
		out.println("      RA='" + Context.RAsaturn + "'");
		out.println("      Dec='" + Context.DECsaturn + "'");
		out.println("      varD='" + ((prevGHA[bodyIndex] != Double.MAX_VALUE) ? deltaD[bodyIndex] : "") + "'");
		out.println("      sd='" + Context.SDsaturn + "'");
		out.println("      hp='" + Context.HPsaturn + "'");
		out.println("      moonDist='" + Context.moonSaturnDist + "'");
		out.println("      delta-lunar='" + ((prevLunar[bodyIndex] != Double.MAX_VALUE) ? deltaLunar[bodyIndex] : "") + "'");
		out.println("      illum='" + Context.k_saturn + "%'/>");
		if (hour < 24) {
			prevGHA[bodyIndex] = Context.GHAsaturn;
			prevDec[bodyIndex] = Context.DECsaturn;
			prevLunar[bodyIndex] = Context.moonSaturnDist;
		}

		out.println("</planets>");

		out.println("<stars>");

		//Polaris
		//  out.println("Polaris");
		//  out.println("GHA:" + GeomUtil.decToSex(Context.GHApol, DEGREE_OPTION, GeomUtil.NONE) + "\t(" + GeomUtil.formatDMS(Context.GHApol, "\370") + ")");
		//  out.println("RA: " + GeomUtil.formatInHours(Context.RApol));
		//  out.println("Dec:" + GeomUtil.decToSex(Context.DECpol, DEGREE_OPTION, GeomUtil.NS) + "\t(" + GeomUtil.formatDMS(Context.DECpol, "\370") + ")");

		for (int i = 0; i < Star.getCatalog().length; i++) {
			out.println("<body name='" + escapeXML(Star.getCatalog()[i].getStarName()) + "'");
			out.println("      loc='" + Star.getCatalog()[i].getConstellation() + "'"); // Like alpha Canis Majoris
			Core.starPos(Star.getCatalog()[i].getStarName());
			if (prevStarLunars[i] != Double.MAX_VALUE)
				deltaStarLunar[i] = Math.abs(Context.starMoonDist - prevStarLunars[i]);
			out.println("      GHA='" + Context.GHAstar + "'");
			out.println("      SHA='" + Context.SHAstar + "'");
			out.println("      Dec='" + Context.DECstar + "'");
			out.println("      lunar-dist='" + Context.starMoonDist + "'");
			out.println("      delta-lunar='" + ((prevStarLunars[i] != Double.MAX_VALUE) ? deltaStarLunar[i] : "") + "'/>");
			if (hour < 24)
				prevStarLunars[i] = Context.starMoonDist;
		}

		out.println("</stars>");

		out.println("<misc-data>");
		//Obliquity of Ecliptic
		out.println("  <mean-obl-of-ecl>" + Context.eps0 + "</mean-obl-of-ecl>");
		out.println("  <true-obl-of-ecl>" + Context.eps + "</true-obl-of-ecl>");

		//Lunar Distance of Sun
		out.println("  <sun-lunar-distance>" + Context.LDist + "</sun-lunar-distance>");

		String[] dow =
				{"sunday", "monday", "tuesday", "wednesday", "thursday", "friday", "saturday"};

		out.println("  <dow>" + dow[Core.weekDay()] + "</dow>");

		out.println("  <dpsi>" + (3600f * Context.delta_psi) + "</dpsi>");
		out.println("  <deps>" + (3600f * Context.delta_eps) + "</deps>");
		out.println("  <obliq>" + Context.OoE + "</obliq>");
		out.println("  <true-obliq>" + Context.tOoE + "</true-obliq>");
		out.println("  <julian-day>" + Context.JD + "</julian-day>");
		out.println("  <julian-ephem-day>" + Context.JDE + "</julian-ephem-day>");
		out.println("</misc-data>");

		out.println("<rise-set>");
//  for (double lat=-55; lat<75; lat+=5)
		for (double lat = 75; lat >= -55; lat -= 5) {
			double[] rs = AlmanacComputer.sunRiseAndSet(lat);
			out.println("<latitude val='" + Integer.toString((int) lat) + "'>");
			out.println("<sun>");
			if (Double.compare(rs[0], Double.NaN) == 0 || Double.compare(rs[1], Double.NaN) == 0)
				out.println("  <none/>");
			else {
				//  out.println(rs[0] + " - " + rs[1]);
				out.println("  <rise z='" + Integer.toString((int) Math.round(rs[2])) + "'>" + GeomUtil.formatHM(rs[0]) + "</rise><set z='" + Integer.toString((int) Math.round(rs[3])) + "'>" + GeomUtil.formatHM(rs[1]) + "</set>");
			}
			out.println("</sun>");
			rs = AlmanacComputer.moonRiseAndSet(lat);
			out.println("<moon>");
			if (Double.compare(rs[0], Double.NaN) == 0 || Double.compare(rs[1], Double.NaN) == 0)
				out.println("  <none/>");
			else {
				//  out.println(rs[0] + " - " + rs[1]);
				out.println("  <rise>" + GeomUtil.formatHM(rs[0]) + "</rise><set>" + GeomUtil.formatHM(rs[1]) + "</set>");
			}
			out.println("</moon>");
			out.println("</latitude>");
		}
		out.println("</rise-set>");

		out.println("</data>");
	}

	private static String escapeXML(String str) {
		return str.replaceAll("'", "&apos;").replaceAll("\"", "&quot;").replaceAll("<", "&lt;").replaceAll(">", "&gt;");
	}

//  private static String unescapeXML(String str) {
//    return str.replaceAll("&apos;", "'").replaceAll("&quot;", "\"").replaceAll("&lt;", "<").replaceAll("&gt;", ">");
//  }

	public static double[] sunRiseAndSet(double latitude) {
		return sunRiseAndSet(latitude, 0L);
	}

	public static double[] sunRiseAndSet(double latitude, double longitude) {
//  out.println("Sun HP:" + Context.HPsun);
//  out.println("Sun SD:" + Context.SDsun);
		double h0 = (Context.HPsun / 3600d) - (Context.SDsun / 3600d) - (34d / 60d);
//  out.println("Sin Sun H0:" + Math.sin(Math.toRadians(h0)));
		double cost = Math.sin(Math.toRadians(h0)) - (Math.tan(Math.toRadians(latitude)) * Math.tan(Math.toRadians(Context.DECsun)));
		double t = Math.acos(cost);

		boolean alt = false;
		if (alt) {
			// 0.83, apparent diameter of the sun on the horizon
			cost = (Math.sin(Math.toRadians(-0.83)) - (Math.sin(Math.toRadians(latitude)) * Math.sin(Math.toRadians(Context.DECsun)))) /
					(Math.cos(Math.toRadians(latitude)) * Math.cos(Math.toRadians(Context.DECsun)));
			t = Math.acos(cost);
		}

		// TASK Look into this next line...
//  out.println("Sun GHA: " + Context.GHAsun);
//  double lon = - Context.GHAsun; // - Math.toDegrees(t);
//  double lon = Context.GHAsun; // - Math.toDegrees(t);
		double lon = longitude;
		while (lon < -180D) {
			lon += 360D;
		}
//  out.println("Lon:" + lon + ", Eot:" + Context.EoT + " (" + (Context.EoT / 60D) + ")" + ", t:" + Math.toDegrees(t));
		double utRise = 12D - (Context.EoT / 60D) - (lon / 15D) - (Math.toDegrees(t) / 15D);
		double utSet = 12D - (Context.EoT / 60D) - (lon / 15D) + (Math.toDegrees(t) / 15D);

		double Z = Math.acos((Math.sin(Math.toRadians(Context.DECsun)) + (0.0145 * Math.sin(Math.toRadians(latitude)))) /
				(0.9999 * Math.cos(Math.toRadians(latitude))));
		Z = Math.toDegrees(Z);

		return new double[]{utRise, utSet, Z, 360d - Z};
	}

	public static double[] moonRiseAndSet(double latitude) {
		return moonRiseAndSet(latitude, 0);
	}

	public static double[] moonRiseAndSet(double latitude, double longitude) {
//  out.println("Moon HP:" + (Context.HPmoon / 60) + "'");
//  out.println("Moon SD:" + (Context.SDmoon / 60) + "'");
		double h0 = (Context.HPmoon / 3600d) - (Context.SDmoon / 3600d) - (34d / 60d);
//  out.println("Moon H0:" + h0);
		double cost = Math.sin(Math.toRadians(h0)) - (Math.tan(Math.toRadians(latitude)) * Math.tan(Math.toRadians(Context.DECmoon)));
		double t = Math.acos(cost);
		// TASK Look into this next line...
		//  out.println("Sun GHA: " + Context.GHAsun);
		//  double lon = - Context.GHAsun; // - Math.toDegrees(t);
		//  double lon = Context.GHAsun; // - Math.toDegrees(t);
		double lon = longitude;
		while (lon < -180D) {
			lon += 360D;
		}
//  out.println("Moon Eot:" + Context.moonEoT + " (" + (Context.moonEoT / 60D) + ")" + ", t:" + Math.toDegrees(t));
		double utRise = 12D - (Context.moonEoT / 60D) - (lon / 15D) - (Math.toDegrees(t) / 15D);
		while (utRise < 0) {
			utRise += 24;
		}
		while (utRise > 24) {
			utRise -= 24;
		}
		double utSet = 12D - (Context.moonEoT / 60D) - (lon / 15D) + (Math.toDegrees(t) / 15D);
		while (utSet < 0) {
			utSet += 24;
		}
		while (utSet > 24) {
			utSet -= 24;
		}

		return new double[]{utRise, utSet};
	}
}
