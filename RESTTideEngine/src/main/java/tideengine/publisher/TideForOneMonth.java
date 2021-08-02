package tideengine.publisher;

import calc.calculation.AstroComputerV2;
import tideengine.BackEndTideComputer;
import tideengine.Coefficient;
import tideengine.TideStation;
import tideengine.TideUtilities;
import tideengine.TideUtilities.SpecialPrm;
import tideengine.TideUtilities.TimedValue;

import java.io.PrintStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

/**
 * Tide Publisher
 * with Moon phases
 */
public class TideForOneMonth {

	public final static int HIGH_TIDE = 0;
	public final static int LOW_TIDE  = 1;

	public final static int MONDAY    = 0;
	public final static int TUESDAY   = 1;
	public final static int WEDNESDAY = 2;
	public final static int THURSDAY  = 3;
	public final static int FRIDAY    = 4;
	public final static int SATURDAY  = 5;
	public final static int SUNDAY    = 6;

	public final static double LUNAR_MONTH_IN_DAYS = 29.53059; // This is an average... Good doc here: https://earthsky.org/astronomy-essentials/lengths-of-lunar-months-in-2019

	private final static SimpleDateFormat SDF = new SimpleDateFormat("EEE dd MMM yyyy");
	private final static SimpleDateFormat TF = new SimpleDateFormat("HH:mm z");

	private final static NumberFormat DF2_1 = new DecimalFormat("00.0");
	private final static NumberFormat DF2 = new DecimalFormat("00");
	private final static NumberFormat DF3 = new DecimalFormat("000");

//private final static boolean verbose = true;

	/**
	 * Just for tests
	 *
	 * @param args -month MM -year YYYY. For month, 1=Jan, 2=Feb,..., 12=Dec.
	 * @throws Exception
	 */
	public static void main_(String... args) throws Exception {
		String yearStr = null;
		String monthStr = null;

		int year = -1;
		int month = -1;

		if (args.length != 4) {
			throw new RuntimeException("Wrong number of arguments: -year 2011 -month 2, for Feb 2011.");
		} else {
			for (int i = 0; i < args.length; i++) {
				if (args[i].equals("-year")) {
					yearStr = args[i + 1];
				} else if (args[i].equals("-month")) {
					monthStr = args[i + 1];
				}
			}
			try {
				year = Integer.parseInt(yearStr);
			} catch (NumberFormatException nfe) {
				throw (nfe);
			}
			try {
				month = Integer.parseInt(monthStr);
			} catch (NumberFormatException nfe) {
				throw (nfe);
			}
		}
//  long before = System.currentTimeMillis();
//  BackEndTideComputer.setVerbose(verbose);
//  XMLDocument constituents = BackEndXMLTideComputer.loadDOM(CONSTITUENT_FILE);
//  long after = System.currentTimeMillis();
//  if (verbose) System.out.println("DOM loading took " + Long.toString(after - before) + " ms");

		List<Coefficient> constSpeed = BackEndTideComputer.buildSiteConstSpeed();

		String location = "Oyster Point Marina";
//  String location = "Adelaide";
		System.out.println("-- " + location + " --");
		System.out.println("Date and time zone:" + "Etc/UTC");
		tideForOneMonth(System.out, "Etc/UTC", year, month, location, "meters", constSpeed);
	}

	public static void tideForOneMonth(PrintStream out,
	                                   String timeZone,
	                                   int year,
	                                   int month,
	                                   String location,
	                                   String unitToUse,
	                                   List<Coefficient> constSpeed) throws Exception {
		tideForOneMonth(out, timeZone, year, month, location, unitToUse, constSpeed, TEXT_FLAVOR, null);
	}

	public final static int TEXT_FLAVOR = 0;
	public final static int XML_FLAVOR = 1;

	public static void tideForOneMonth(PrintStream out,
	                                   String timeZone,
	                                   int year,
	                                   int month,
	                                   String location,
	                                   String unitToUse,
	                                   List<Coefficient> constSpeed,
	                                   int flavor,
	                                   SpecialPrm sPrm) throws Exception {
		AstroComputerV2 acv2 = new AstroComputerV2();
		// TideStation
		int nextMonth = (month == 12) ? 0 : month;
		Calendar firstDay = new GregorianCalendar(year, month - 1, 1);
		Calendar now = firstDay;

		TideStation ts = BackEndTideComputer.findTideStation(location, now.get(Calendar.YEAR));
		int prevYear = now.get(Calendar.YEAR);
		boolean loop = true;
		int prevMoonAge = 0; // Workaround...
		while (loop) {
			String mess = now.getTime().toString();
			System.out.println(" -- " + mess);
			// If year changes, recompute TideStation
			if (now.get(Calendar.YEAR) != prevYear) {
				ts = BackEndTideComputer.findTideStation(location, now.get(Calendar.YEAR));
			}
			List<TimedValue> timeAL = tideForOneDay(acv2, now, timeZone, ts, constSpeed, unitToUse);
			Calendar utcCal = (Calendar) now.clone();
			utcCal.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
			// System.out.println("UTC Date:" + utcCal.getTime());
			//acv2.setDateTime(utcCal.getTime().getTime());

			double moonPhase = acv2.getMoonPhase(utcCal.get(Calendar.YEAR),
					utcCal.get(Calendar.MONTH) + 1,
					utcCal.get(Calendar.DAY_OF_MONTH),
					utcCal.get(Calendar.HOUR_OF_DAY),
					utcCal.get(Calendar.MINUTE),
					utcCal.get(Calendar.SECOND));
			double[] rsSun = acv2.sunRiseAndSet(ts.getLatitude(), ts.getLongitude());
			// acv2.EpochAndZ[] rsSun = acv2.sunRiseAndSetEpoch(ts.getLatitude(), ts.getLongitude());

			Calendar sunRise = new GregorianCalendar();
			sunRise.setTimeZone(TimeZone.getTimeZone(ts.getTimeZone()));
			sunRise.set(Calendar.YEAR, now.get(Calendar.YEAR));
			sunRise.set(Calendar.MONTH, now.get(Calendar.MONTH));
			sunRise.set(Calendar.DAY_OF_MONTH, now.get(Calendar.DAY_OF_MONTH));
			sunRise.set(Calendar.SECOND, 0);

			double r = rsSun[acv2.UTC_RISE_IDX] /* + Utils.daylightOffset(sunRise) */ + acv2.getTimeZoneOffsetInHours(TimeZone.getTimeZone(timeZone /*ts.getTimeZone()*/), sunRise.getTime());
			int min = (int) ((r - ((int) r)) * 60);
			sunRise.set(Calendar.MINUTE, min);
			sunRise.set(Calendar.HOUR_OF_DAY, (int) r);

			Calendar sunTransit = new GregorianCalendar();
			sunTransit.setTimeZone(TimeZone.getTimeZone(ts.getTimeZone()));
			sunTransit.set(Calendar.YEAR, now.get(Calendar.YEAR));
			sunTransit.set(Calendar.MONTH, now.get(Calendar.MONTH));
			sunTransit.set(Calendar.DAY_OF_MONTH, now.get(Calendar.DAY_OF_MONTH));
			sunTransit.set(Calendar.SECOND, 0);

			if (ts != null) {
				double tPass = acv2.getSunMeridianPassageTime(ts.getLatitude(), ts.getLongitude());
				r = tPass + acv2.getTimeZoneOffsetInHours(TimeZone.getTimeZone(timeZone), sunTransit.getTime());
				min = (int) ((r - ((int) r)) * 60);
				sunTransit.set(Calendar.MINUTE, min);
				sunTransit.set(Calendar.HOUR_OF_DAY, (int) r);
			}

			// Sun Altitude at Transit time


			Calendar sunSet = new GregorianCalendar();
			sunSet.setTimeZone(TimeZone.getTimeZone(ts.getTimeZone()));
			sunSet.set(Calendar.YEAR, now.get(Calendar.YEAR));
			sunSet.set(Calendar.MONTH, now.get(Calendar.MONTH));
			sunSet.set(Calendar.DAY_OF_MONTH, now.get(Calendar.DAY_OF_MONTH));
			sunSet.set(Calendar.SECOND, 0);
			r = rsSun[acv2.UTC_SET_IDX] /* + Utils.daylightOffset(sunSet) */ + acv2.getTimeZoneOffsetInHours(TimeZone.getTimeZone(timeZone/*ts.getTimeZone()*/), sunSet.getTime());
			min = (int) ((r - ((int) r)) * 60);
			sunSet.set(Calendar.MINUTE, min);
			sunSet.set(Calendar.HOUR_OF_DAY, (int) r);

			// Moon rise and set
			double[] rsMoon = acv2.moonRiseAndSet(ts.getLatitude(), ts.getLongitude());
			Calendar moonRise = new GregorianCalendar();
			moonRise.setTimeZone(TimeZone.getTimeZone(ts.getTimeZone()));
			moonRise.set(Calendar.YEAR, now.get(Calendar.YEAR));
			moonRise.set(Calendar.MONTH, now.get(Calendar.MONTH));
			moonRise.set(Calendar.DAY_OF_MONTH, now.get(Calendar.DAY_OF_MONTH));
			moonRise.set(Calendar.SECOND, 0);

			r = rsMoon[acv2.UTC_RISE_IDX] /* + Utils.daylightOffset(sunRise) */ + acv2.getTimeZoneOffsetInHours(TimeZone.getTimeZone(timeZone /*ts.getTimeZone()*/), moonRise.getTime());
			min = (int) ((r - ((int) r)) * 60);
			moonRise.set(Calendar.MINUTE, min);
			moonRise.set(Calendar.HOUR_OF_DAY, (int) r);

			Calendar moonSet = new GregorianCalendar();
			moonSet.setTimeZone(TimeZone.getTimeZone(ts.getTimeZone()));
			moonSet.set(Calendar.YEAR, now.get(Calendar.YEAR));
			moonSet.set(Calendar.MONTH, now.get(Calendar.MONTH));
			moonSet.set(Calendar.DAY_OF_MONTH, now.get(Calendar.DAY_OF_MONTH));
			moonSet.set(Calendar.SECOND, 0);
			r = rsMoon[acv2.UTC_SET_IDX] /* + Utils.daylightOffset(sunSet) */ + acv2.getTimeZoneOffsetInHours(TimeZone.getTimeZone(timeZone/*ts.getTimeZone()*/), moonSet.getTime());
			min = (int) ((r - ((int) r)) * 60);
			moonSet.set(Calendar.MINUTE, min);
			moonSet.set(Calendar.HOUR_OF_DAY, (int) r);

			int phaseInDay = 0; // (int) Math.round(moonPhase / (360d / 28d)); //  + 1;
//			if (moonPhase <= ((360d / LUNAR_MONTH_IN_DAYS) / 2d) || moonPhase > (360d - ((360d / LUNAR_MONTH_IN_DAYS) / 2d))) {
//				phaseInDay = 29; // Full Moon
//			} else {
				for (int day=0; day<=29; day++) {
					if (moonPhase >= (day * (360d / LUNAR_MONTH_IN_DAYS)) - ((360d / LUNAR_MONTH_IN_DAYS) / 2d) && moonPhase < (day * (360d / LUNAR_MONTH_IN_DAYS)) + ((360d / LUNAR_MONTH_IN_DAYS) / 2d)) {
						phaseInDay = day;
						break;
					}
				}
//			}
			if ((phaseInDay == 29 || phaseInDay == 15 || phaseInDay == 7 || phaseInDay == 22) && (prevMoonAge == phaseInDay)) {
				phaseInDay += 1;
			}
			prevMoonAge = phaseInDay;
//			System.out.println(String.format("\tMoon Age at %s: %d, for phase %f", SDF.format(now.getTime()), phaseInDay, moonPhase));

//			if (phaseInDay > 28) {
//				phaseInDay = 28;
//			}
//			if (phaseInDay < 1) {
//				phaseInDay = 1;
//			}
			if (timeZone != null) {
				TF.setTimeZone(TimeZone.getTimeZone(timeZone));
			}
//			System.out.println(">> Calculating transit elev at " + acv2.getCalculationDateTime().getTime());
			double sunElevAtTransit = acv2.getSunElevAtTransit(ts.getLatitude(), ts.getLongitude());

			if (flavor == TEXT_FLAVOR) {
				out.println("- " + SDF.format(now.getTime()) + " - Moon Age:" + DF2.format(phaseInDay));
				for (TimedValue tv : timeAL) {
					out.println(tv.getType() + ": " + TF.format(tv.getCalendar().getTime()) + " : " + TideUtilities.DF22.format(tv.getValue()) + " " + unitToUse);
				}
			} else if (flavor == XML_FLAVOR) {
				String specialBG = "specBG='n' ";
				if (sPrm != null) {
//        System.out.println("Special BG required");
					for (TimedValue tv : timeAL) {
						double tideHour = tv.getCalendar().get(Calendar.HOUR_OF_DAY) + (tv.getCalendar().get(Calendar.MINUTE) / 60d);
						System.out.println("Tidetype:" + tv.getType() + " hour:" + tideHour + " within [" + sPrm.getFromHour() + ", " + sPrm.getToHour() + "], weekday=" + tv.getCalendar().get(Calendar.DAY_OF_WEEK));
						if (((sPrm.getTideType() == HIGH_TIDE && tv.getType().equals("HW")) ||
								(sPrm.getTideType() == LOW_TIDE && tv.getType().equals("LW"))) &&
								(tideHour >= sPrm.getFromHour() && tideHour <= sPrm.getToHour())) {
							boolean go = true;
							// Week days
							if (sPrm.getWeekdays() != null) {
								int day = tv.getCalendar().get(Calendar.DAY_OF_WEEK);
								int[] dd = sPrm.getWeekdays();
								System.out.println("See if " + day + " is in [");
								for (int d : dd) {
									System.out.print(d + " ");
								}
								System.out.println();
								go = ((day == Calendar.MONDAY && dd[MONDAY] == 1) ||
										(day == Calendar.TUESDAY && dd[TUESDAY] == 1) ||
										(day == Calendar.WEDNESDAY && dd[WEDNESDAY] == 1) ||
										(day == Calendar.THURSDAY && dd[THURSDAY] == 1) ||
										(day == Calendar.FRIDAY && dd[FRIDAY] == 1) ||
										(day == Calendar.SATURDAY && dd[SATURDAY] == 1) ||
										(day == Calendar.SUNDAY && dd[SUNDAY] == 1));
							}
							if (go) {
								specialBG = "specBG='y' ";
								break;
							}
						}
					}
				}
				double riseZ = rsSun[acv2.RISE_Z_IDX],
						setZ = rsSun[acv2.SET_Z_IDX];

				out.println("<date val='" + SDF.format(now.getTime()) + "' " + specialBG +
						"moon-phase='" + DF2.format(phaseInDay) +
						"' sun-rise='" + TF.format(sunRise.getTime()) +
						"' sun-rise-Z='" + DF3.format(riseZ) +
						"' sun-transit='" + (sunTransit != null ? TF.format(sunTransit.getTime()) : "") +
						"' sun-elev-at-transit='" + DF2_1.format(sunElevAtTransit) +
						"' sun-set='" + TF.format(sunSet.getTime()) +
						"' sun-set-Z='" + DF3.format(setZ) +
						"' moon-rise='" + TF.format(moonRise.getTime()) +
						"' moon-set='" + TF.format(moonSet.getTime()) + "'>");
				for (TimedValue tv : timeAL) {
					if ("Slack".equals(tv.getType())) {
						out.println("  <plot type='" + tv.getType() + "' date='" + TF.format(tv.getCalendar().getTime()) + "'/>");
					} else {
						out.println("  <plot type='" + tv.getType() + "' date='" + TF.format(tv.getCalendar().getTime()) + "' height='" + TideUtilities.DF22.format(tv.getValue()) + "' unit='" + unitToUse + "'/>");
					}
				}
				out.println("</date>");
			}

			now.add(Calendar.DAY_OF_MONTH, 1);
			if (now.get(Calendar.MONTH) == nextMonth) {
				loop = false;
			}
		}
		System.out.println("Ok!");
	}

	public static List<TimedValue> tideForOneDay(AstroComputerV2 acv2,
												 Calendar now,
	                                             String timeZone,
	                                             String location,
	                                             List<Coefficient> constSpeed,
	                                             String unitToUse) throws Exception {
		TideStation ts = BackEndTideComputer.findTideStation(location, now.get(Calendar.YEAR));
		return tideForOneDay(acv2, now, timeZone, ts, constSpeed, unitToUse);
	}

	public static List<TimedValue> tideForOneDay(AstroComputerV2 acv2,
												 Calendar now,
	                                             String timeZone,
	                                             TideStation ts,
	                                             List<Coefficient> constSpeed,
	                                             String unitToUse) throws Exception {
		List<TimedValue> timeAL = null;
		final int RISING = 1;
		final int FALLING = -1;

		double low1 = Double.NaN;
		double low2 = Double.NaN;
		double high1 = Double.NaN;
		double high2 = Double.NaN;
		Calendar low1Cal = null;
		Calendar low2Cal = null;
		Calendar high1Cal = null;
		Calendar high2Cal = null;
		List<TimedValue> slackList = null;
		int trend = 0;

		slackList = new ArrayList<>();
		double previousWH = Double.NaN;
		for (int h = 0; h < 24; h++) {
			for (int m = 0; m < 60; m++) {
				Calendar cal = new GregorianCalendar(now.get(Calendar.YEAR),
						now.get(Calendar.MONTH),
						now.get(Calendar.DAY_OF_MONTH),
						h, m);
				if (timeZone != null) {
					cal.setTimeZone(TimeZone.getTimeZone(timeZone));
				}
				double wh = TideUtilities.getWaterHeight(ts, constSpeed, cal);
				if (Double.isNaN(previousWH)) {
					previousWH = wh;
				} else {
					if (trend == 0) {
						if (previousWH > wh) {
							trend = -1;
						} else if (previousWH < wh) {
							trend = 1;
						}
					} else {
						if (ts.isCurrentStation()) {
							if ((previousWH > 0 && wh <= 0) || (previousWH < 0 && wh >= 0)) {
								slackList.add(new TimedValue("Slack", cal, 0d));
							}
						}
						switch (trend) {
							case RISING: {
								Calendar prev = (Calendar) cal.clone();
								prev.add(Calendar.MINUTE, -1);
								if (acv2.getTimeZoneOffsetInHours(TimeZone.getTimeZone(timeZone), cal.getTime()) ==
										acv2.getTimeZoneOffsetInHours(TimeZone.getTimeZone(timeZone), prev.getTime())) {
									if (previousWH > wh) // Now going down
									{
										if (Double.isNaN(high1)) {
											high1 = previousWH;
											cal.add(Calendar.MINUTE, -1);
											high1Cal = cal;
										} else {
											high2 = previousWH;
											cal.add(Calendar.MINUTE, -1);
											high2Cal = cal;
										}
										trend = FALLING; // Now falling
									}
								}
							}
							break;
							case FALLING: {
								Calendar prev = (Calendar) cal.clone();
								prev.add(Calendar.MINUTE, -1);
								if (AstroComputerV2.getTimeZoneOffsetInHours(TimeZone.getTimeZone(timeZone), cal.getTime()) ==
										AstroComputerV2.getTimeZoneOffsetInHours(TimeZone.getTimeZone(timeZone), prev.getTime())) {
									if (previousWH < wh) { // Now going up
										if (Double.isNaN(low1)) {
											low1 = previousWH;
											cal.add(Calendar.MINUTE, -1);
											low1Cal = cal;
										} else {
											low2 = previousWH;
											cal.add(Calendar.MINUTE, -1);
											low2Cal = cal;
										}
										trend = RISING; // Now rising
									}
								}
							}
							break;
						}
					}
					previousWH = wh;
				}
			}
		}
		timeAL = new ArrayList<>(4);
		if (low1Cal != null) {
			timeAL.add(new TimedValue(ts.isTideStation() ? "LW" : "ME", low1Cal, low1));
		}
		if (low2Cal != null) {
			timeAL.add(new TimedValue(ts.isTideStation() ? "LW" : "ME", low2Cal, low2));
		}
		if (high1Cal != null) {
			timeAL.add(new TimedValue(ts.isTideStation() ? "HW" : "MF", high1Cal, high1));
		}
		if (high2Cal != null) {
			timeAL.add(new TimedValue(ts.isTideStation() ? "HW" : "MF", high2Cal, high2));
		}
		if (ts.isCurrentStation() && slackList != null && slackList.size() > 0) {
			timeAL.addAll(slackList);
		}
		Collections.sort(timeAL);
		return timeAL;
	}
}
