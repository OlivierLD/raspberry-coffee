package tests;

import tideengine.BackEndTideComputer;
import tideengine.Coefficient;
import tideengine.TideStation;
import tideengine.TideUtilities;
import tideengine.TideUtilities.TimedValue;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * This is just a test, not a unit-test.
 * Direct access, no REST here.
 */
public class SampleMain {
	private final static SimpleDateFormat SDF = new SimpleDateFormat("yyy-MMM-dd HH:mm z (Z)");

	private final static BackEndTideComputer backEndTideComputer = new BackEndTideComputer();

	public static void main(String... args) throws Exception {
		System.out.println(args.length + " Argument(s)...");
		boolean xmlTest = true;

		if (xmlTest) {
			System.out.println("XML Tests");
			backEndTideComputer.connect();
			backEndTideComputer.setVerbose(false);

			// Some tests
			if (true) {
				TideStation ts = null;

				long before = 0;
				long after = 0;

				List<Coefficient> constSpeed = BackEndTideComputer.buildSiteConstSpeed();

				Calendar now = GregorianCalendar.getInstance();
				String location = null;
				if (true) {
//					System.setProperty("tide.verbose", "true");
//					location = URLEncoder.encode("Port Townsend", StandardCharsets.UTF_8).replace("+", "%20");
					location = URLEncoder.encode("Port-Navalo", StandardCharsets.UTF_8).replace("+", "%20");
					ts = backEndTideComputer.findTideStation(location, now.get(Calendar.YEAR));
					if (ts != null) {
						now.setTimeZone(TimeZone.getTimeZone(ts.getTimeZone()));
						if (ts != null) {
							if (true) {
								double[] mm = TideUtilities.getMinMaxWH(ts, constSpeed, now);
								System.out.println("At " + location + " in " + now.get(Calendar.YEAR) + ", min : " + TideUtilities.DF22PLUS.format(mm[TideUtilities.MIN_POS]) + " " + ts.getUnit() + ", max : " + TideUtilities.DF22PLUS.format(mm[TideUtilities.MAX_POS]) + " " + ts.getDisplayUnit());
							}
							double wh = TideUtilities.getWaterHeight(ts, constSpeed, now);
							System.out.println((ts.isTideStation() ? "Water Height" : "Current Speed") + " in " + location + " at " + now.getTime().toString() + " : " + TideUtilities.DF22PLUS.format(wh) + " " + ts.getDisplayUnit());
							if (false && ts.isTideStation()) {
								System.out.println((ts.isTideStation() ? "Water Height" : "Current Speed") + " in " + location + " at " + now.getTime().toString() + " : " + TideUtilities.DF22PLUS.format(TideUtilities.getWaterHeightIn(wh, ts, TideStation.METERS)) + " " + TideStation.METERS);
							}
						}
					} else {
						System.out.printf("%s not found :( \n", location);
					}
				}
				if (true) {
					location = URLEncoder.encode("Fare Ute Point", StandardCharsets.UTF_8).replace("+", "%20");
					ts = backEndTideComputer.findTideStation(location, now.get(Calendar.YEAR));
					if (ts != null) {
						now.setTimeZone(TimeZone.getTimeZone(ts.getTimeZone()));
						double wh = TideUtilities.getWaterHeight(ts, constSpeed, now);
						System.out.println((ts.isTideStation() ? "Water Height" : "Current Speed") + " in " + location + " at " + now.getTime().toString() + " : " + TideUtilities.DF22PLUS.format(wh) + " " + ts.getDisplayUnit());
					}

					location = URLEncoder.encode("Oyster Point Marina, San Francisco Bay, California", StandardCharsets.UTF_8).replace("+", "%20");
					ts = backEndTideComputer.findTideStation(location, now.get(Calendar.YEAR));
					if (ts != null) {
						now.setTimeZone(TimeZone.getTimeZone(ts.getTimeZone()));
						double wh = TideUtilities.getWaterHeight(ts, constSpeed, now);
						System.out.println((ts.isTideStation() ? "Water Height" : "Current Speed") + " in " + location + " at " + now.getTime().toString() + " : " + TideUtilities.DF22PLUS.format(wh) + " " + ts.getDisplayUnit());
					}

					location = URLEncoder.encode("Shediac Bay, New Brunswick", StandardCharsets.UTF_8).replace("+", "%20");
					ts = backEndTideComputer.findTideStation(location, now.get(Calendar.YEAR));
					if (ts != null) {
						now.setTimeZone(TimeZone.getTimeZone(ts.getTimeZone()));
						double wh = TideUtilities.getWaterHeight(ts, constSpeed, now);
						System.out.println((ts.isTideStation() ? "Water Height" : "Current Speed") + " in " + location + " at " + now.getTime().toString() + " : " + TideUtilities.DF22PLUS.format(wh) + " " + ts.getDisplayUnit());
					} else {
						System.out.println("-- " + location + " not found...");
					}

					location = URLEncoder.encode("Alcatraz (North Point)", StandardCharsets.UTF_8).replace("+", "%20");
					ts = backEndTideComputer.findTideStation(location, now.get(Calendar.YEAR));
					if (ts != null) {
						now.setTimeZone(TimeZone.getTimeZone(ts.getTimeZone()));
						double wh = TideUtilities.getWaterHeight(ts, constSpeed, now);
						System.out.println((ts.isTideStation() ? "Water Height" : "Current Speed") + " in " + location + " at " + now.getTime().toString() + " : " + TideUtilities.DF22PLUS.format(wh) + " " + ts.getDisplayUnit());
					}

					location = URLEncoder.encode("Cape Cod Canal, Massachusetts Current", StandardCharsets.UTF_8).replace("+", "%20");
					ts = backEndTideComputer.findTideStation(location, now.get(Calendar.YEAR));
					if (ts != null) {
						now.setTimeZone(TimeZone.getTimeZone(ts.getTimeZone()));
						double wh = TideUtilities.getWaterHeight(ts, constSpeed, now);
						System.out.println((ts.isTideStation() ? "Water Height" : "Current Speed") + " in " + location + " at " + now.getTime().toString() + " : " + TideUtilities.DF22PLUS.format(wh) + " " + ts.getDisplayUnit());
					}

					// Oyster Point for today (every 30 minutes)
					location = URLEncoder.encode("Oyster Point Marina", StandardCharsets.UTF_8).replace("+", "%20");
					ts = backEndTideComputer.findTideStation(location, now.get(Calendar.YEAR));
					if (ts != null) {
//          TimeZone tz = TimeZone.getDefault();
						now.setTimeZone(TimeZone.getTimeZone(ts.getTimeZone()));
						before = System.currentTimeMillis();
						for (int h = 0; h < 24; h++) {
							for (int m = 0; m < 60; m += 30) {
								Calendar cal = new GregorianCalendar(now.get(Calendar.YEAR),
										now.get(Calendar.MONTH),
										now.get(Calendar.DAY_OF_MONTH),
										h, m);

								double wh = TideUtilities.getWaterHeight(ts, constSpeed, cal);
								TimeZone.setDefault(TimeZone.getTimeZone("127")); // for UTC display
								System.out.println((ts.isTideStation() ? "Water Height" : "Current Speed") + " in " + location + " at " + cal.getTime().toString() + " : " + TideUtilities.DF22PLUS.format(wh) + " " + ts.getDisplayUnit());
//              TimeZone.setDefault(tz);
							}
						}
						after = System.currentTimeMillis();
						System.out.println("Calculation AND Display took " + Long.toString(after - before) + " ms");
					}

					// A test, CSV format, for a spreadsheet
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm");
					// location = "Oyster Point Marina";
					// ts = BackEndTideComputer.findTideStation(location, now.get(Calendar.YEAR), constituents);
					if (ts != null) {
						// TimeZone tz = TimeZone.getDefault();
						System.out.println("Date;Height (ft)");
						before = System.currentTimeMillis();
						for (int h = 0; h < 24; h++) {
							for (int m = 0; m < 60; m += 30) {
								Calendar cal = new GregorianCalendar(now.get(Calendar.YEAR),
										now.get(Calendar.MONTH),
										now.get(Calendar.DAY_OF_MONTH),
										h, m);

								double wh = TideUtilities.getWaterHeight(ts, constSpeed, cal);
								// TimeZone.setDefault(TimeZone.getTimeZone("127")); // for UTC display
								System.out.println(sdf.format(cal.getTime()) + ";" + TideUtilities.DF22PLUS.format(wh));
								// TimeZone.setDefault(tz);
							}
						}
						after = System.currentTimeMillis();
						System.out.println("Calculation AND Display took " + Long.toString(after - before) + " ms");
					}

					// A test, High and Low water at Oyster Point
					// location = "Oyster Point Marina";
					// ts = BackEndTideComputer.findTideStation(location, now.get(Calendar.YEAR), constituents);
					if (ts != null) {
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
						int trend = 0;

						double previousWH = Double.NaN;
						before = System.currentTimeMillis();
						for (int h = 0; h < 24; h++) {
							for (int m = 0; m < 60; m++) {
								Calendar cal = new GregorianCalendar(now.get(Calendar.YEAR),
										now.get(Calendar.MONTH),
										now.get(Calendar.DAY_OF_MONTH),
										h, m);

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
										switch (trend) {
											case RISING:
												if (previousWH > wh) { // Now going down
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
												break;
											case FALLING:
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
												break;
										}
									}
									previousWH = wh;
								}
							}
						}
						after = System.currentTimeMillis();
						System.out.println("High-Low water Calculation took " + Long.toString(after - before) + " ms");
						System.out.println("-- " + URLDecoder.decode(location, StandardCharsets.UTF_8) + " --");

						List<TimedValue> timeAL = new ArrayList<>(4);
						if (low1Cal != null) {
							timeAL.add(new TimedValue("LW", low1Cal, low1));
						}
						if (low2Cal != null) {
							timeAL.add(new TimedValue("LW", low2Cal, low2));
						}
						if (high1Cal != null) {
							timeAL.add(new TimedValue("HW", high1Cal, high1));
						}
						if (high2Cal != null) {
							timeAL.add(new TimedValue("HW", high2Cal, high2));
						}
						Collections.sort(timeAL);

						for (TimedValue tv : timeAL) {
							System.out.println(tv.getType() + " " + tv.getCalendar().getTime().toString() + " : " + TideUtilities.DF22PLUS.format(tv.getValue()) + " " + ts.getDisplayUnit());
						}
					}
				}
				// Kodiak for today (every 30 minutes)
				location = URLEncoder.encode("Kodiak, Women's Bay, Alaska", StandardCharsets.UTF_8).replace("+", "%20");
				ts = backEndTideComputer.findTideStation(location, now.get(Calendar.YEAR));

				if (true) {
					now.setTimeZone(TimeZone.getTimeZone(ts.getTimeZone()));
					double[] mm = TideUtilities.getMinMaxWH(ts, constSpeed, now);
					System.out.println("At " + URLDecoder.decode(location, StandardCharsets.UTF_8) + " in " + now.get(Calendar.YEAR) + ", min : " + TideUtilities.DF22PLUS.format(mm[TideUtilities.MIN_POS]) + " " + ts.getUnit() + ", max : " + TideUtilities.DF22PLUS.format(mm[TideUtilities.MAX_POS]) + " " + ts.getDisplayUnit());
				}

				now.setTimeZone(TimeZone.getTimeZone(ts.getTimeZone()));
				if (ts != null) {
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
					int trend = 0;

					double previousWH = Double.NaN;
					before = System.currentTimeMillis();
					for (int h = 0; h < 24; h++) {
						for (int m = 0; m < 60; m++) {
							Calendar cal = new GregorianCalendar(now.get(Calendar.YEAR),
									now.get(Calendar.MONTH),
									now.get(Calendar.DAY_OF_MONTH),
									h, m);
							cal.setTimeZone(TimeZone.getTimeZone(ts.getTimeZone()));
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
									switch (trend) {
										case RISING:
											if (previousWH > wh) { // Now going down
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
											break;
										case FALLING:
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
											break;
									}
								}
								previousWH = wh;
							}
						}
					}
					after = System.currentTimeMillis();
					System.out.println("High-Low water Calculation took " + Long.toString(after - before) + " ms");
					System.out.println("-- " + URLDecoder.decode(location, StandardCharsets.UTF_8) + " --");

					List<TimedValue> timeAL = new ArrayList<>(4);
					if (low1Cal != null) {
						timeAL.add(new TimedValue("LW", low1Cal, low1));
					}
					if (low2Cal != null) {
						timeAL.add(new TimedValue("LW", low2Cal, low2));
					}
					if (high1Cal != null) {
						timeAL.add(new TimedValue("HW", high1Cal, high1));
					}
					if (high2Cal != null) {
						timeAL.add(new TimedValue("HW", high2Cal, high2));
					}
					Collections.sort(timeAL);

					SDF.setTimeZone(TimeZone.getTimeZone(ts.getTimeZone()));
					for (TimedValue tv : timeAL) {
						System.out.println(tv.getType() + " " + SDF.format(tv.getCalendar().getTime()) + " : " + TideUtilities.DF22PLUS.format(tv.getValue()) + " " + ts.getDisplayUnit());
					}
				}
				if (false) {
					List<String[]> hcList = TideUtilities.getStationHarmonicConstituents(ts, constSpeed);
					for (String[] line : hcList) {
						System.out.println(line[0] + " " + line[1] + " " + line[2] + " " + line[3] + " " + line[4]);
					}
				}
				if (false && ts != null) {
//        TimeZone tz = TimeZone.getDefault();
					before = System.currentTimeMillis();
					for (int h = 0; h < 24; h++) {
						for (int m = 0; m < 60; m += 30) {
							Calendar cal = new GregorianCalendar(now.get(Calendar.YEAR),
									now.get(Calendar.MONTH),
									now.get(Calendar.DAY_OF_MONTH),
									h, m);

							double wh = TideUtilities.getWaterHeight(ts, constSpeed, cal);
							TimeZone.setDefault(TimeZone.getTimeZone("127")); // for UTC display
							System.out.println((ts.isTideStation() ? "Water Height" : "Current Speed") + " in " + location + " at " + cal.getTime().toString() + " : " + TideUtilities.DF22PLUS.format(wh) + " " + ts.getDisplayUnit());
//            TimeZone.setDefault(tz);
						}
					}
					after = System.currentTimeMillis();
					System.out.println("Calculation AND Display took " + Long.toString(after - before) + " ms");
				}
//      else
//        System.out.println(location + " not found...");
			}
			if (true) {
				List<Coefficient> constSpeed = BackEndTideComputer.buildSiteConstSpeed();
				// Tide table for one day:
				Calendar now = GregorianCalendar.getInstance(TimeZone.getTimeZone("America/Los_Angeles"));
				String location = null;
				if (true) {
					location = URLEncoder.encode("Ocean Beach, California", StandardCharsets.UTF_8).replace("+", "%20");
					TideStation ts = backEndTideComputer.findTideStation(location, now.get(Calendar.YEAR));
					now.setTimeZone(TimeZone.getTimeZone(ts.getTimeZone()));
					if (ts != null) {
						if (true) {
							double[] mm = TideUtilities.getMinMaxWH(ts, constSpeed, now);
							System.out.println("At " + URLDecoder.decode(location, StandardCharsets.UTF_8) + " on " + now.getTime() + ", min : " + TideUtilities.DF22PLUS.format(mm[TideUtilities.MIN_POS]) + " " + ts.getUnit() + ", max : " + TideUtilities.DF22PLUS.format(mm[TideUtilities.MAX_POS]) + " " + ts.getDisplayUnit());
							System.out.println("------------");
						}
						List<TimedValue> table = TideUtilities.getTideTableForOneDay(ts, constSpeed, now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH), null);

						for (TimedValue tv : table) {
							System.out.println(tv.getType() + " " + tv.getCalendar().getTime() + " : " + TideUtilities.DF22PLUS.format(tv.getValue()) + " " + ts.getDisplayUnit());
						}

						System.out.println("Done");
					}
				}
			}
			backEndTideComputer.disconnect();
		}
	}
}
