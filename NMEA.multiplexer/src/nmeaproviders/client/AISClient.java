package nmeaproviders.client;

import nmea.ais.AISParser;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Map;
import java.util.HashMap;

public class AISClient {
	public static void main(String[] args) {
		try {
			Map<Integer, AISParser.AISRecord> map = new HashMap<>();

			URL aisSFBayURL = new URL("http://207.7.148.216:9009");
			InputStream aisIS = aisSFBayURL.openStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(aisIS));
			String line = "";
			while (line != null) {
				line = br.readLine();
				if (line != null) {
					if (!line.startsWith("#")) {
						try {
							AISParser.AISRecord rec = AISParser.parseAIS(line);
							if (rec != null) {
								map.put(rec.getMmsi(), rec);
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
