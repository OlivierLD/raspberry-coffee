package logfile;

import nmea.parser.RMC;
import nmea.parser.StringParsers;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintStream;

/**
 * To use to process an NMEA log file.
 * Turns it into an array of Json objects like { 'hdm': 111, 'dev': -2 }
 */
public class Processor {
	public static void main(String... args) throws Exception {
		long nbRec = 0;
		double decl = -Double.MAX_VALUE; // Override with system property

		PrintStream out = System.out;

		String systemPropDecl = System.getProperty("default.declination");
		String fName = System.getProperty("log.file.name", "2010-11-03.Taiohae.nmea");
		String outputStr = System.getProperty("output.file.name");
		if (outputStr != null) {
			out = new PrintStream(outputStr);
		}

		if (systemPropDecl != null) {
			decl = Double.parseDouble(systemPropDecl);
		}
		out.print("[");

		double standingHDM = -Double.MAX_VALUE;
		double standingCOG = -Double.MAX_VALUE;

		boolean boatIsMoving = false;

		BufferedReader br = new BufferedReader(new FileReader(fName));
		String line = "";
		while (line != null) {
			line = br.readLine();
			if (line != null) {
				if (line.startsWith("$") && line.length() > 6) {
					StringParsers.ParsedData parsedData = StringParsers.autoParse(line);
					if ("HDG".equals(parsedData.getSentenceId())) {
						double data[] = (double[]) parsedData.getParsedData();
						double hdg = data[0];
						double hdm = hdg;
						if (data[2] != -Double.MAX_VALUE) {
							decl = data[2];
						}
						if (decl != -Double.MAX_VALUE) {
							hdm += decl;
						}
						standingHDM = hdm;
					} else if ("RMC".equals(parsedData.getSentenceId())) {
						RMC rmc = (RMC)parsedData.getParsedData();
						double cog = rmc.getCog();
						double rmcDecl = rmc.getDeclination();
						if (rmcDecl != -Double.MAX_VALUE) {
							decl = rmcDecl;
						}
						standingCOG = cog;
						double sog = rmc.getSog();
						boatIsMoving = (sog > 0);
					}
				}
				if (boatIsMoving && standingCOG != -Double.MAX_VALUE && standingHDM != -Double.MAX_VALUE) {
					double dev = standingHDM - standingCOG;
					while (dev > 180) {
						dev -= 360;
					}
					while (dev < -180) {
						dev += 360;
					}
					out.print(String.format("%s{ \"hdm\": %f, \"dev\": %f }", (nbRec > 0 ? "," : ""), standingHDM, dev));
					standingCOG = -Double.MAX_VALUE;
					standingHDM = -Double.MAX_VALUE;
					nbRec++;
				}
			}
		}
		br.close();
		out.println("]");
		if (outputStr != null) {
			out.close();
		}
		System.out.println(String.format("Written %d tuples.", nbRec));
	}
}
