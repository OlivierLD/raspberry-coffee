package util;

import calc.GeomUtil;
import nmea.parser.GeoPos;
import nmea.parser.RMC;
import nmea.parser.StringParsers;
import util.swing.SwingFrame;

import java.awt.HeadlessException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static nmea.parser.StringParsers.GGA_ALT_IDX;

/**
 * Analyze a log file
 * Time, distance
 */
public class LogAnalyzer {

	private static SimpleDateFormat SDF = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss z");

	private final static long SEC = 1_000L;
	private final static long MIN = 60 * SEC;
	private final static long HOUR = 60 * MIN;
	private final static long DAY = 24 * HOUR;

	private static String msToHMS(long ms) {
		String str = "";
		long remainder = ms;
		int days = (int) (remainder / DAY);
		remainder -= (days * DAY);
		int hours = (int) (remainder / HOUR);
		remainder -= (hours * HOUR);
		int minutes = (int) (remainder / MIN);
		remainder -= (minutes * MIN);
		float seconds = (float) (remainder / SEC);
		if (days > 0) {
			str = days + " day(s) ";
		}
		if (hours > 0 || !str.trim().isEmpty()) {
			str += hours + " hour(s) ";
		}
		if (minutes > 0 || !str.trim().isEmpty()) {
			str += minutes + " minute(s) ";
		}
		str += seconds + " sec(s)";
		return str.trim();
	}

	enum SpeedUnit {
		KN(1, "kn"),
		KMH(1.852f, "km/h");

		private final float knToUnit;
		private final String unitLabel;

		SpeedUnit(float adjust, String label) {
			this.knToUnit = adjust;
			this.unitLabel = label;
		}

		public float convert() {
			return this.knToUnit;
		}

		public String label() {
			return this.unitLabel;
		}
	}

	private static SpeedUnit unitToUse = SpeedUnit.KMH;

	public static class DatedPosition {
		GeoPos position;
		Date date;

		DatedPosition position(GeoPos position) {
			this.position = position;
			return this;
		}

		DatedPosition date(Date date) {
			this.date = date;
			return this;
		}

		public GeoPos getPosition() {
			return this.position;
		}

		public Date getDate() {
			return this.date;
		}
	}

	public static void main(String... args) {

		String speedUnit = System.getProperty("speed.unit");
		if (speedUnit != null) {
			switch (speedUnit) {
				case "KMH":
					unitToUse = SpeedUnit.KMH;
					break;
				case "KN":
					unitToUse = SpeedUnit.KN;
					break;
				default:
					System.out.println(String.format("Unknown unit [%s], defaulting to knots.", speedUnit));
					unitToUse = SpeedUnit.KN;
					break;
			}
		}

		if (args.length == 0) {
			throw new IllegalArgumentException("Please provide the name of the file to analyze as first parameter");
		}
		List<DatedPosition> positions = new ArrayList<>();
		try {
			BufferedReader br = new BufferedReader(new FileReader(args[0]));

			// TODO Option to check chronology and continuity
			BufferedWriter bw = new BufferedWriter(new FileWriter("stat.csv"));

			// Accumulators and others
			GeoPos previousPos = null;
			double distanceInKm = 0d;
			double minLat = Double.MAX_VALUE, maxLat = -Double.MAX_VALUE;
			double minLng = Double.MAX_VALUE, maxLng = -Double.MAX_VALUE;
			double maxSpeed = -Double.MAX_VALUE;
			double minAlt = Double.MAX_VALUE, maxAlt = -Double.MAX_VALUE;
			// For stat file
			long previousDate = -1L;
			long statLineNo = 0;

			bw.write("Idx;time;deltaT;deltaDist;deltaT(2);\n");
			statLineNo += 1;

			long minLatIdx = -1,
				 	 minLngIdx = -1,
					 maxLatIdx = -1,
					 maxLngIdx = -1;

			long nbRec = 0L, totalNbRec = 0L;
			Date start = null;
			Date arrival = null;
			String line;
			boolean keepReading = true;
			while (keepReading) {
				line = br.readLine();
				if (line == null) {
					keepReading = false;
				} else {
					if (StringParsers.validCheckSum(line)) {
						totalNbRec++;
						String id = StringParsers.getSentenceID(line);
						if (id.equals("RMC")) {
							nbRec++;
							RMC rmc = StringParsers.parseRMC(line);
							assert (rmc != null);
							if (rmc.isValid()) {
								// Get date, speed, position (for distance)
								Date rmcDate = rmc.getRmcDate();
								Date rmcTime = rmc.getRmcTime();

								if (start == null) {
									start = rmcTime;
								} else {
									arrival = rmcTime;
								}
								GeoPos gp = rmc.getGp();
								if (gp != null) {
									if (rmcTime != null) {
										positions.add(new DatedPosition()
												.date(rmcTime)
												.position(gp));
									}
									if (gp.lat < minLat) {
										minLat = gp.lat;
										minLatIdx = totalNbRec - 1;
									}
									if (gp.lat > maxLat) {
										maxLat = gp.lat;
										maxLatIdx = totalNbRec - 1;
									}
									if (gp.lng < minLng) {
										minLng = gp.lng;
										minLngIdx = totalNbRec - 1;
									}
									if (gp.lng > maxLng) {
										maxLng = gp.lng;
										maxLngIdx = totalNbRec - 1;
									}
									assert (rmcTime != null);
									if (previousPos != null) {
										double distance = GeomUtil.haversineKm(previousPos.lat, previousPos.lng, gp.lat, gp.lng);
//									System.out.println(String.format("Step: %.03f km between %s and %s (%s)",
//													distance,
//													previousPos.toString(),
//													gp.toString(),
//													SDF.format(rmcTime)));
										distanceInKm += distance;
										bw.write(String.format("%d;%d;%d;%f;=(B%d-B%d);\n", (totalNbRec - 1), rmcTime.getTime(), (rmcTime.getTime() - previousDate), distance, (statLineNo + 1), statLineNo));
										//                                                                                                                                      |
										//                                                                                                                                      Based on 1, not 0.
									} else {
										bw.write(String.format("%d;%d;%d;;;\n", (totalNbRec - 1), rmcTime.getTime(), (rmcTime.getTime() - previousDate)));
									}
									statLineNo += 1;
									previousPos = gp;
									previousDate = rmcTime.getTime();
								}
								maxSpeed = Math.max(maxSpeed, rmc.getSog());
							}
						} else if (id.equals("GGA")) {
							nbRec++;
							List<Object> gga = StringParsers.parseGGA(line);
							if (gga != null) {
								double alt = (Double) gga.get(GGA_ALT_IDX);
								maxAlt = Math.max(maxAlt, alt);
								minAlt = Math.min(minAlt, alt);
							}
						}
						// More Sentence IDs ?..
					} else {
						System.out.println(String.format("Invalid data [%s]", line));
					}
				}
			}
			br.close();
			bw.close();
			System.out.println("Checkout the spreadsheet stat.csv.");

			// Display summary
			assert (start != null && arrival != null);
			System.out.println(String.format("Started %s", SDF.format(start)));
			System.out.println(String.format("Arrived %s", SDF.format(arrival)));
			System.out.println(String.format("%s record(s) out of %s. Total distance: %.03f km, in %s. Avg speed:%.03f km/h",
					NumberFormat.getInstance().format(nbRec),
					NumberFormat.getInstance().format(totalNbRec),
					distanceInKm,
					msToHMS(arrival.getTime() - start.getTime()),
					distanceInKm / ((arrival.getTime() - start.getTime()) / ((double) HOUR))));
			System.out.println(String.format("Max Speed: %.03f %s", maxSpeed * unitToUse.convert(), unitToUse.label()));
			System.out.println(String.format("Min alt: %.02f m, Max alt: %.02f m, delta %.02f m", minAlt, maxAlt, (maxAlt - minAlt)));
			System.out.println(String.format("Top-Left    :%s (%f / %f)", new GeoPos(maxLat, minLng).toString(), maxLat, minLng));
			System.out.println(String.format("Bottom-Right:%s (%f / %f)", new GeoPos(minLat, maxLng).toString(), minLat, maxLng));
			System.out.println(String.format("Min Lat record idx: %d", minLatIdx));
			System.out.println(String.format("Max Lat record idx: %d", maxLatIdx));
			System.out.println(String.format("Min Lng record idx: %d", minLngIdx));
			System.out.println(String.format("Max Lng record idx: %d", maxLngIdx));

			try {
				// A Map on a canvas?
				SwingFrame frame = new SwingFrame(positions);
				frame.setVisible(true);
				frame.plot();
			} catch (HeadlessException he) {
				System.out.println("Headless Exception. Try in a graphical environment to visualize the data.");
			}
		} catch (IOException ioe) {
			System.err.println("From " + System.getProperty("user.dir"));
			ioe.printStackTrace();
		}
	}
}
