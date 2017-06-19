package util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import nmea.parser.GeoPos;
import nmea.parser.RMC;
import nmea.parser.StringParsers;

/**
 * Analyze a log file
 * Time, distance
 */
public class LogAnalyzer {

	private static SimpleDateFormat SDF = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss z");

	private final static double kmEquatorialEarthRadius = 6378.1370D;

	private static double haversineKm(double lat1, double long1, double lat2, double long2) {
		double dlong = Math.toRadians(long2 - long1);
		double dlat = Math.toRadians(lat2 - lat1);
		double a = Math.pow(Math.sin(dlat / 2.0), 2) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.pow(Math.sin(dlong / 2.0), 2);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
		double d = /* 6367 */ kmEquatorialEarthRadius * c;

		return d;
	}

	private final static long SEC  = 1000L;
	private final static long MIN  = 60 * SEC;
	private final static long HOUR = 60 * MIN;
	private final static long DAY  = 24 * HOUR;

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
		if (days > 0)
			str = days + " day(s) ";
		if (hours > 0 || str.trim().length() > 0)
			str += hours + " hour(s) ";
		if (minutes > 0 || str.trim().length() > 0)
			str += minutes + " minute(s) ";
		str += seconds + " sec(s)";
		return str.trim();
	}

	public static void main(String... args) {
		if (args.length == 0) {
			throw new IllegalArgumentException("Provide the name of the file to analyze as first parameter");
		}
		try {
			BufferedReader br = new BufferedReader(new FileReader(args[0]));
			// Accumulators and others
			GeoPos previousPos = null;
			double distanceInKm = 0d;
			double minLat = Double.MAX_VALUE, maxLat = -Double.MAX_VALUE;
			double minLng = Double.MAX_VALUE, maxLng = -Double.MAX_VALUE;
			double maxSpeed = -Double.MAX_VALUE;
			long nbRec = 0L, totalNbRec = 0L;
			Date start = null;
			Date arrival = null;
			String line = "";
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
							// Get date, speed, position (for distance)
							Date rmcDate = rmc.getRmcDate();
							Date rmcTime = rmc.getRmcTime();

							if (start == null) {
								start = rmcTime;
							} else {
								arrival = rmcTime;
							}
							GeoPos gp = rmc.getGp();
							minLat = Math.min(minLat, gp.lat);
							maxLat = Math.max(maxLat, gp.lat);
							minLng = Math.min(minLng, gp.lng);
							maxLng = Math.max(maxLng, gp.lng);
							if (previousPos != null) {
								double distance = haversineKm(previousPos.lat, previousPos.lng, gp.lat, gp.lng);
//									System.out.println(String.format("Step: %.03f km between %s and %s (%s)",
//													distance,
//													previousPos.toString(),
//													gp.toString(),
//													SDF.format(rmcTime)));
								distanceInKm += distance;
							}
							previousPos = gp;
							maxSpeed = Math.max(maxSpeed, rmc.getSog());
						}
						// More Sentence IDs ?..
					} else {
						System.out.println(String.format("Invalid data [%s]", line));
					}
				}
			}
			br.close();

			// Display summary
			System.out.println(String.format("Started %s", SDF.format(start)));
			System.out.println(String.format("Arrived %s", SDF.format(arrival)));
			System.out.println(String.format("%s record(s) out of %s. Total distance: %.03f km, in %s. Avg speed:%.03f km/h",
							NumberFormat.getInstance().format(nbRec),
							NumberFormat.getInstance().format(totalNbRec),
							distanceInKm,
							msToHMS(arrival.getTime() - start.getTime()),
							distanceInKm / ((arrival.getTime() - start.getTime()) / ((double)HOUR))));
			System.out.println(String.format("Max Speed: %.03f km/h", maxSpeed * 1.852));
			System.out.println(String.format("Top-Left    :%s", new GeoPos(maxLat, minLng).toString()));
			System.out.println(String.format("Bottom-Right:%s", new GeoPos(minLat, maxLng).toString()));

		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}
}
