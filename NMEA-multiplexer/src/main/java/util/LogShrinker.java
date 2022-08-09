package util;

import nmea.parser.RMC;
import nmea.parser.StringParsers;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * Small utility, remove the first records where speed is zero, as well as the last ones...
 */
public class LogShrinker {

	public static void main(String... args) {
		if (args.length < 1) {
			System.err.println("Please provide the log file name to scan as a first argument.");
			System.exit(1);
		}

		String fileName = args[0];
		try {
			BufferedReader bufferedReader = new BufferedReader(new FileReader(fileName));
			String line;
			long lineNum = 0L;
			long starsAtRecNo = 0L;
			long endsAtRecNo = 0L;
			boolean started = false;
			boolean stopped = false;
			boolean go = true;
			while (go) {
				line = bufferedReader.readLine();
				if (line == null) {
					go = false;
				} else {
					boolean valid = StringParsers.validCheckSum(line);
					if (valid) {
						String sentenceID = StringParsers.getSentenceID(line);
						switch (sentenceID) {
							case "RMC":
								RMC rmc = StringParsers.parseRMC(line);
								assert (rmc != null);
								double speed = rmc.getSog();
								if (speed > 0) {
									started = true;
								}
								if (!started) {
									starsAtRecNo = lineNum;
								}
								if (speed == 0.0 && !stopped) {
									endsAtRecNo = lineNum;
								}
								if (speed == 0.0) {
									stopped = true;
								} else {
									stopped = false;
									endsAtRecNo = 0;
								}
								break;
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
			System.out.printf("On %d records, start moving at %d (last zero-speed record), stop at %d\n", lineNum, starsAtRecNo + 1, endsAtRecNo + 1);
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}
}
