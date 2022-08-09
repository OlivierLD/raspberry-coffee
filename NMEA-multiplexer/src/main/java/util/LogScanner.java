package util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import nmea.parser.GeoPos;
import nmea.parser.RMC;
import nmea.parser.StringParsers;

/**
 * Small utility, to get the range (lat, long) of a log file...
 */
public class LogScanner {

	public static void main(String... args) {
		if (args.length < 1) {
			System.err.println("Please provide the log file name to scan as a first argument.");
			System.exit(1);
		}

		Map<String, Long> distinctSentences = new HashMap<>();
		double minLat = Double.MAX_VALUE, maxLat = -Double.MAX_VALUE,
						minLng = Double.MAX_VALUE, maxLng = -Double.MAX_VALUE;
		String fileName = args[0];
		try {
			BufferedReader bufferedReader = new BufferedReader(new FileReader(fileName));
			String line;
			long lineNum = 0L;
			boolean go = true;
			while (go) {
				line = bufferedReader.readLine();
				if (line == null) {
					go = false;
				} else {
					boolean valid = StringParsers.validCheckSum(line);
					if (valid) {
						String sentenceID = StringParsers.getSentenceID(line);
						Long nb = distinctSentences.get(sentenceID);
						nb = (nb == null ? 0 : nb + 1);
						distinctSentences.put(sentenceID, nb);

						switch (sentenceID) {
							case "RMC":
								RMC rmc = StringParsers.parseRMC(line);
								assert (rmc != null);
								GeoPos geoPos = rmc.getGp();
								Date rmcDate = rmc.getRmcDate();
								if (geoPos != null) {
									minLat = Math.min(minLat, geoPos.lat);
									maxLat = Math.max(maxLat, geoPos.lat);
									minLng = Math.min(minLng, geoPos.lng );
									maxLng = Math.max(maxLng, geoPos.lng);
								}
							default:
								break;
						}
					} else {
						System.err.printf("Invalid Checksum for [%s], line # %d\n", line, lineNum);
					}
					lineNum++;
				}
			}
			bufferedReader.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		System.out.println("Distinct sentences:");
		distinctSentences.keySet()
						.forEach(k ->  System.out.printf("%s -> %d sentence(s)\n", k, distinctSentences.get(k)) );

		System.out.printf("Latitude range [%f, %f]\n", minLat, maxLat);
		System.out.printf("Longitude range [%f, %f]\n", minLng, maxLng);
	}
}
