package tideengine.publisher;

import calc.GeomUtil;
import tideengine.BackEndTideComputer;
import tideengine.TideStation;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Optional;

public class TidePublisher {

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
	public static void publish(TideStation ts, String timeZoneId, int sm, int sy, int nb, int q, String utu, TideForOneMonth.SpecialPrm sPrm) {

		final TideForOneMonth.SpecialPrm specialBGPrm = sPrm;

		System.out.println("Starting month:" + sm + ", year:" + sy);
		System.out.println("For " + nb + " " + (q == Calendar.MONTH ? "month(s)" : "year(s)"));

		Thread printThread = new Thread(() -> {
			GregorianCalendar start = new GregorianCalendar(sy, sm, 1);
			GregorianCalendar end = (GregorianCalendar) start.clone();
			end.add(q, nb);
			boolean loop = true;
			PrintStream out = System.out;
			String radical = "";
			try {
				File tempFile = File.createTempFile("tide.data.", ".xml");
				out = new PrintStream(new FileOutputStream(tempFile));
				radical = tempFile.getAbsolutePath();
				radical = radical.substring(0, radical.lastIndexOf(".xml"));
//            System.out.println("Writing data in " + tempFile.getAbsolutePath());
			} catch (Exception ex) {
				System.err.println("Error creating temp file");
				ex.printStackTrace();
				return;
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
				if (start.equals(end))
					loop = false;
				else {
					out.println("  <period month='" + (start.get(Calendar.MONTH) + 1) + "' year='" + start.get(Calendar.YEAR) + "'>");
					try {
						System.out.println("Calculating tide for " + start.getTime().toString());
						TideForOneMonth.tideForOneMonth(out,
								timeZoneId,
								start.get(Calendar.YEAR),
								start.get(Calendar.MONTH) + 1, // Base: 1
								ts.getFullName(),
								utu,
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
				String cmd = "." + File.separator + "xsl" + File.separator + "publishtide " + radical;
				System.out.println("Command:" + cmd);
				Process p = Runtime.getRuntime().exec(cmd);
				int exitStatus = p.waitFor();
				System.out.println("Script completed, status " + exitStatus);
				System.out.println(String.format("See %s.pdf", radical));
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			// Also see // https://mvnrepository.com/artifact/org.apache.xmlgraphics/fop, but does it stream like the Oracle one?
//		compile group: 'org.apache.xmlgraphics', name: 'fop', version: '2.2'

			// This should do it:
//			FOProcessor processor = new FOProcessor();
//			// set XML input file
//			processor.setData("c:\\temp\\check_data.xml");
//			// set XSL input file
//			processor.setTemplate("c:\\temp\\check.xsl");
//			// set the output format
//			processor.setOutputFormat(FOProcessor.FORMAT_PDF);
//			//set output file
//			processor.setOutput("c:\\temp\\check.pdf");
//			// Now we process, have to surround with a try-catch block thou
//			try
//			{
//				// Process !
//				processor.generate();
//			}
//			catch (XDOException e)
//			{
//				e.printStackTrace();
//			}

		});
		printThread.start();
	}

	public static void publish(String stationName, int startMonth, int startYear, int nb, int quantity) {
		TideStation ts = null;
		try {
			Optional<TideStation> optTs = BackEndTideComputer.getStationData()
					.stream()
					.filter(station -> station.getFullName().equals(stationName))
					.findFirst();
			if (!optTs.isPresent()) {
				// TODO Barf
				System.out.println(String.format("Station [%s] not found.", stationName));
			} else {
				ts = optTs.get();
				publish(ts, ts.getTimeZone(), startMonth, startYear, nb, quantity, null, null);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * For tests
	 * @param args unused.
	 */
	public static void main(String... args) {
		try {
			BackEndTideComputer.connect();
			BackEndTideComputer.setVerbose("true".equals(System.getProperty("tide.verbose", "false")));
			publish(URLEncoder.encode("Ocean Beach, California", "UTF-8").replace("+", "%20"), Calendar.SEPTEMBER, 2017, 1, Calendar.MONTH);
			System.out.println("Done!");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
