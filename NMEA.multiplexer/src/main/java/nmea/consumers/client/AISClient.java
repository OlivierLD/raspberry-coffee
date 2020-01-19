package nmea.consumers.client;

import nmea.ais.AISParser;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Map;
import java.util.HashMap;

/**
 * Not a Consumer, just a sample.
 * Any regular one is AIS-aware (Serial, File, TCP, etc).
 *
 * See System variable -Dno.ais
 */
public class AISClient {
	/*
	 * Set proxy at runtime if needed -Dhttp.proxyHost, -Dhttp.proxyPort
	 */
	public static void main(String... args) {
		AISParser aisParser = new AISParser();
		try {
			Map<Integer, AISParser.AISRecord> map = new HashMap<>();

			String aisUrl = "http://ais.exploratorium.edu:80";
			// "http://207.7.148.216:9009"
			URL aisSFBayURL = new URL(aisUrl);
			InputStream aisIS = aisSFBayURL.openStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(aisIS));
			String line = "";
			while (line != null) {
				line = br.readLine();
				if (line != null) {
					if (!line.startsWith("#")) {
						try {
							AISParser.AISRecord rec = aisParser.parseAIS(line);
							if (rec != null) {
								map.put(rec.getMMSI(), rec);
								System.out.println("(" + map.size() + " boat(s)) " + rec.toString());
							}
						} catch (Exception ex) {
							System.err.println(ex.toString());
						}
					}
				}
			}
			System.out.println("Done.");
			br.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
