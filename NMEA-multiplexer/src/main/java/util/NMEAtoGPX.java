package util;

import nmea.parser.RMC;
import nmea.parser.StringParsers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

/**
 * GPX generator, from NMEA log.
 * GPX is compatible with Navigation software like OpenCPN.
 * GPs eXchange format.
 */
public class NMEAtoGPX {
	private final static Map<String, Integer> map = new HashMap<>();
	private final static SimpleDateFormat UTC_MASK = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

	static {
		UTC_MASK.setTimeZone(TimeZone.getTimeZone("etc/UTC"));
	}

	private static void transform(String fileInName,
	                              String fileOutName) throws Exception {
		BufferedReader br = new BufferedReader(new FileReader(fileInName));
		String line = "";

		BufferedWriter bw = new BufferedWriter(new FileWriter(fileOutName));
		bw.write("<?xml version=\"1.0\"?>\n" +
				"<gpx version=\"1.1\" creator=\"OpenCPN\" " +
				"     xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
				"     xmlns=\"http://www.topografix.com/GPX/1/1\" " +
				"     xmlns:gpxx=\"http://www.garmin.com/xmlschemas/GpxExtensions/v3\" " +
				"     xsi:schemaLocation=\"http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd\" " +
				"     xmlns:opencpn=\"http://www.opencpn.org\">\n" +
				"  <trk>\n" +
				"    <extensions>\n" +
				"      <opencpn:guid>21180000-44ac-4218-a090-ed331f980000</opencpn:guid>\n" +
				"      <opencpn:viz>1</opencpn:viz>\n" +
				"    </extensions>\n" +
				"    <trkseg>\n");

		while (line != null) {
			line = br.readLine();
			if (line != null) {
				if (line.startsWith("$") && line.length() > 6) {
					String prefix = line.substring(3, 6);
					Integer nb = map.get(prefix);
					map.put(prefix, (nb == null) ? (1) : (nb + 1));
					// Specific
					if ("RMC".equals(prefix)) {
						if (StringParsers.validCheckSum(line)) {
							RMC rmc = StringParsers.parseRMC(line);
							if (rmc != null && rmc.getRmcTime() != null && rmc.isValid()) {
								bw.write("      <trkpt lat=\"" + rmc.getGp().lat + "\" lon=\"" + rmc.getGp().lng + "\">\n" +
										"        <time>" + UTC_MASK.format(rmc.getRmcTime()) + "</time>\n" +
										"      </trkpt>\n");
							}
						}
					}
				}
			}
		}
		br.close();
		bw.write("    </trkseg>\n" +
				"  </trk>\n" +
				"</gpx>");
		bw.close();
	}

	public static void main(String... args) {
		if (args.length == 0) {
			throw new IllegalArgumentException("Please provide the name of the file to analyze as first parameter");
		}

		try {
			String inputFileName = args[0];
			String outputFileName = inputFileName + ".gpx";
			NMEAtoGPX.transform(inputFileName, outputFileName);
			System.out.printf("\nGenerated file %s is ready.\n", outputFileName);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		map.keySet().forEach(key -> System.out.printf("%s: %d records\n", key, map.get(key)));
	}
}
