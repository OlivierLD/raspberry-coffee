package util;

import calc.GeomUtil;
import nmea.parser.GeoPos;
import nmea.parser.RMC;
import nmea.parser.StringParsers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static nmea.parser.StringParsers.GGA_ALT_IDX;
import static utils.TimeUtil.fmtDHMS;
import static utils.TimeUtil.msToHMS;

/**
 * Turn NMEA log data into KML
 * @see <a href="https://www.google.com/search?q=load+a+kml+file+in+google+earth+on+mac&rlz=1C5CHFA_enUS756US756&oq=load+a+kml+file+in+google+earth+on+mac&aqs=chrome..69i57.7973j0j4&sourceid=chrome&ie=UTF-8">this</a>.
 * KML: Keyhole Markup Language (Keyhole was acquired by Google, and became Google Maps)
 */
public class NMEAtoKML {
	private static SimpleDateFormat SDF = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss z");

	private final static double KNOTS_TO_KMH = 1.852;

	private final static long SEC  = 1_000L;
	private final static long MIN  = 60 * SEC;
	private final static long HOUR = 60 * MIN;
	private final static long DAY  = 24 * HOUR;

	private final static String TITLE_PREFIX = "--title:";
	private final static String SUB_TITLE_PREFIX = "--sub-title:";

	public static void main(String... args) {
		if (args.length == 0) {
			throw new IllegalArgumentException("Please provide the name of the file to analyze as first parameter");
		}

		String title = "";
		String subTitle = "";

		if (args.length > 1) {
			for (int i=1; i<args.length; i++) {
				if (args[i].startsWith(TITLE_PREFIX)) {
					title = args[i].substring(TITLE_PREFIX.length());
				} else if (args[i].startsWith(SUB_TITLE_PREFIX)) {
					subTitle = args[i].substring(SUB_TITLE_PREFIX.length());
				}
			}
		}

		if (title.length() == 0) {
			title = String.format("Generated %s", SDF.format(new Date()));
		}
		if (subTitle.length() == 0) {
			subTitle = String.format("- %s -", SDF.format(new Date()));
		}

		try {
			String inputFileName = args[0];
			BufferedReader br = new BufferedReader(new FileReader(inputFileName));
			String outputFileName = inputFileName + ".kml";

			BufferedWriter bw = new BufferedWriter(new FileWriter(outputFileName));
			// KML start
			bw.write(String.format("<?xml version = '1.0' encoding = 'UTF-8'?>\n" +
					"<kml xmlns=\"http://earth.google.com/kml/2.0\" \n" +
					"     xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
					"     xsi:schemaLocation=\"http://earth.google.com/kml/2.0 ../xsd/kml21.xsd\">\n" +
					"   <Document>\n" +
					"      <name>%s</name>\n", title));
			bw.write(String.format("      <Placemark>\n" +
					"          <name>%s</name>\n" +
					"          <visibility>1</visibility>\n" +
					"          <open>0</open>\n" +
					"          <Style>\n" +
					"             <LineStyle>\n" +
					"                <width>3</width>\n" +
					"                <color>ff00ffff</color>\n" +
					"             </LineStyle>\n" +
					"             <PolyStyle>\n" +
					"                <color>7f00ff00</color>\n" +
					"             </PolyStyle>\n" +
					"          </Style>\n" +
					"          <LineString>\n" +
					"             <extrude>1</extrude>\n" +
					"             <tessellate>1</tessellate>\n" +
					"             <altitudeMode>clampToGround</altitudeMode>\n" +
					"             <coordinates>\n", subTitle));

			// Accumulators and others
			GeoPos previousPos = null;
			double distanceInKm = 0d;
			double minLat = Double.MAX_VALUE, maxLat = -Double.MAX_VALUE;
			double minLng = Double.MAX_VALUE, maxLng = -Double.MAX_VALUE;
			double maxSpeed = -Double.MAX_VALUE;
			double minAlt = Double.MAX_VALUE, maxAlt = -Double.MAX_VALUE;
			long nbRec = 0L, totalNbRec = 0L;
			Date start = null;
			GeoPos startPos = null;
			Date arrival = null;
			String line;
			double alt = -Double.MAX_VALUE;
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
									if (startPos == null) {
										startPos = gp;
									}
									String coordinates = String.format("%f,%f,%f", gp.lng, gp.lat, (alt != -Double.MAX_VALUE ? alt : 0d));
									bw.write(coordinates + "\n");

									minLat = Math.min(minLat, gp.lat);
									maxLat = Math.max(maxLat, gp.lat);
									minLng = Math.min(minLng, gp.lng);
									maxLng = Math.max(maxLng, gp.lng);
									if (previousPos != null) {
										double distance = GeomUtil.haversineKm(previousPos.lat, previousPos.lng, gp.lat, gp.lng);
//									System.out.println(String.format("Step: %.03f km between %s and %s (%s)",
//													distance,
//													previousPos.toString(),
//													gp.toString(),
//													SDF.format(rmcTime)));
										distanceInKm += distance;
									}
									previousPos = gp;
								}
								maxSpeed = Math.max(maxSpeed, rmc.getSog());
							}
						} else if (id.equals("GGA")) {
							nbRec++;
							List<Object> gga = StringParsers.parseGGA(line);
							if (gga != null) {
								alt = (Double)gga.get(GGA_ALT_IDX);
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

			bw.write(
					"            </coordinates>\n" +
							"         </LineString>\n" +
							"      </Placemark>\n" +
							"   </Document>\n" +
							"</kml>\n");
			bw.close();

			// Display summary
			assert (startPos != null && start != null && arrival != null);
			System.out.println(String.format("Started %s from %s", SDF.format(start), startPos.toString()));
			System.out.println(String.format("Arrived %s at %s", SDF.format(arrival), previousPos.toString()));
			System.out.println(String.format("%s record(s) out of %s. Total distance: %.03f km, in %s. Avg speed:%.03f km/h",
					NumberFormat.getInstance().format(nbRec),
					NumberFormat.getInstance().format(totalNbRec),
					distanceInKm,
					fmtDHMS(msToHMS(arrival.getTime() - start.getTime())),
					distanceInKm / ((arrival.getTime() - start.getTime()) / ((double)HOUR))));
			System.out.println(String.format("Max Speed: %.03f km/h", maxSpeed * KNOTS_TO_KMH));
			System.out.println(String.format("Min alt: %.02f m, Max alt: %.02f m, delta %.02f m", minAlt, maxAlt, (maxAlt - minAlt)));
			System.out.println(String.format("Top-Left    :%s", new GeoPos(maxLat, minLng).toString()));
			System.out.println(String.format("Bottom-Right:%s", new GeoPos(minLat, maxLng).toString()));

			System.out.println(String.format("\nGenerated file %s is ready.", outputFileName));
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

}
