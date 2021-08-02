package tideengine.publisher;

import calc.GeomUtil;
import tideengine.BackEndTideComputer;
import tideengine.TideStation;
import tideengine.TideUtilities;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Optional;

public class TidePublisher {

	// Script names in a System variable. they must be in the xsl folder.
	public final static String AGENDA_TABLE = System.getProperty("publishagenda.script", "publishagenda.sh");
	public final static String TIDE_TABLE = System.getProperty("publishtide.script", "publishtide.sh");
	public final static String MOON_CALENDAR = System.getProperty("publishlunarcalendar.script", "publishlunarcalendar.sh");

	/**
	 * @param ts         TideStation
	 * @param timeZoneId TimeZone ID to use
	 * @param sm         Start Month
	 * @param sy         Start Year
	 * @param nb         Number of q (see below)
	 * @param q          quantity. Calendar.MONTH or Calendar.YEAR
	 * @param utu        Unit to use
	 * @param sPrm       Special parameters
	 */
	public static String publish(
			TideStation ts,
			String timeZoneId,
			int sm,
			int sy,
			int nb,
			int q,
			String utu,
			TideUtilities.SpecialPrm sPrm,
			String scriptToRun)
			throws Exception {

		final TideUtilities.SpecialPrm specialBGPrm = sPrm;

		System.out.println("Starting month:" + sm + ", year:" + sy);
		System.out.println("For " + nb + " " + (q == Calendar.MONTH ? "month(s)" : "year(s)"));

		GregorianCalendar start = new GregorianCalendar(sy, sm, 1);
		GregorianCalendar end = (GregorianCalendar) start.clone();
		end.add(q, nb);
		boolean loop = true;
		PrintStream out = System.out;
		String radical = "";
		String prefix = (scriptToRun == null ? TIDE_TABLE : scriptToRun);
		try {
			File tempFile = File.createTempFile(prefix + ".data.", ".xml");
			out = new PrintStream(new FileOutputStream(tempFile));
			radical = tempFile.getAbsolutePath();
			radical = radical.substring(0, radical.lastIndexOf(".xml"));
		    System.out.println("Writing data in " + tempFile.getAbsolutePath());
		} catch (Exception ex) {
			System.err.println("Error creating temp file");
			ex.printStackTrace();
			throw ex;
		}

		try {
			out.println("<tide station='" + URLDecoder.decode(ts.getFullName(), "UTF-8").replace("'", "&apos;") +
					"' station-time-zone='" + ts.getTimeZone() +
					"' print-time-zone='" + timeZoneId +
					"' station-lat='" + GeomUtil.decToSex(ts.getLatitude(), GeomUtil.SWING, GeomUtil.NS, GeomUtil.TRAILING_SIGN).replace("'", "&apos;") +
					"' station-lng='" + GeomUtil.decToSex(ts.getLongitude(), GeomUtil.SWING, GeomUtil.EW, GeomUtil.TRAILING_SIGN).replace("'", "&apos;") + "'>");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		while (loop) {
			if (start.equals(end)) {
				loop = false;
			} else {
				out.println("  <period month='" + (start.get(Calendar.MONTH) + 1) + "' year='" + start.get(Calendar.YEAR) + "'>");
				try {
					System.out.println("Calculating tide for " + start.getTime().toString());
					TideForOneMonth.tideForOneMonth(out,
							timeZoneId,
							start.get(Calendar.YEAR),
							start.get(Calendar.MONTH) + 1, // Base: 1
							ts.getFullName(),
							(utu == null ? ts.getUnit() : utu),
							BackEndTideComputer.buildSiteConstSpeed(),
							TideForOneMonth.XML_FLAVOR,
							specialBGPrm);
					start.add(Calendar.MONTH, 1);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				out.println("  </period>");
			}
		}
		out.println("</tide>");
		out.close();
		System.out.println("Generation completed.");
		// Ready for transformation
		try {
			String cmd = "." + File.separator + "xsl" + File.separator + (scriptToRun == null ? TIDE_TABLE : scriptToRun) + " " + radical;
			System.out.println("Command:" + cmd);
			Process p = Runtime.getRuntime().exec(cmd);
			int exitStatus = p.waitFor();
			System.out.println("Script completed, status " + exitStatus);
			System.out.println(String.format("See %s.pdf", radical));
			cmd = String.format("mv %s.pdf web", radical);
			p = Runtime.getRuntime().exec(cmd);
			exitStatus = p.waitFor();
			System.out.printf("Command [%s] completed, status %s\n", cmd, exitStatus);

			return "." + radical.substring(radical.lastIndexOf(File.separator)) + ".pdf";
		} catch (Exception ex) {
			throw ex;
		}
	}

	public static String publish(String stationName, int startMonth, int startYear, int nb, int quantity)
			throws Exception {
		return publish(stationName, startMonth, startYear, nb, quantity, null);
	}

	public static String publish(String stationName, int startMonth, int startYear, int nb, int quantity, String script)
			throws Exception {
		TideStation ts = null;
		try {
			Optional<TideStation> optTs = BackEndTideComputer.getStationData()
					.stream()
					.filter(station -> station.getFullName().equals(stationName))
					.findFirst();
			if (!optTs.isPresent()) {
				throw new Exception(String.format("Station [%s] not found.", stationName));
			} else {
				ts = optTs.get();
				return publish(ts, ts.getTimeZone(), startMonth, startYear, nb, quantity, null, null, script);
			}
		} catch (Exception ex) {
			throw ex;
		}
	}

	/**
	 * For tests
	 *
	 * @param args unused.
	 */
	public static void main(String... args) {
		try {
			BackEndTideComputer.connect();
			BackEndTideComputer.setVerbose("true".equals(System.getProperty("tide.verbose", "false")));
//			String f = publish(
//					URLEncoder.encode("Ocean Beach, California", "UTF-8").replace("+", "%20"),
//					Calendar.SEPTEMBER,
//					2017,
//					1,
//					Calendar.MONTH);
			String f = publish(
					URLEncoder.encode("Ocean Beach, California", "UTF-8").replace("+", "%20"),
					Calendar.JANUARY,
					2019,
					1,
					Calendar.YEAR,
					"publishagenda.sh");
			System.out.println(String.format("%s generated", f));
			System.out.println("Done!");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
