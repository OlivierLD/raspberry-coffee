package util;

import nmea.parser.NMEAComposite;
import nmea.parser.StringParsers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Generates a CSV file from a log file (nmea LogFile)
 */
public class NMEAtoCSV {

	private final static boolean VERBOSE = "true".equals(System.getProperty("verbose"));

	private final static String SEPARATOR = ";";
	private final static String BREAK_ON_STRING = "RMC"; // Default

	private static String dataMapToCSVLine(List<String> dataToWrite, Map<String, String> map, String separator) {
		StringBuffer line = new StringBuffer();
		dataToWrite.forEach(key -> line.append(line.length() > 0 ? separator : "").append(map.get(key)));
		return line.toString();
	}

	private final static String INPUT_PRM_PREFIX = "--in:";
	private final static String OUTPUT_PRM_PREFIX = "--out:";
	private final static String STRINGS_PRM_PREFIX = "--data:";
	private final static String BREAK_AT_PRM_PREFIX = "--break-at:";

	public static void main(String... args) {

		System.out.println(String.format("Running from %s.", System.getProperty("user.dir")));

		String nmeaFileName = "";
		String csvOutputName = "";

		String nmeaStringsToParse = ""; // = "RMC,HDG,VHW,MWV,MTW"; // "RMC,MTA,MTW,MMB,DBT,HDM,VHW,MWD";
		String breakAt = BREAK_ON_STRING; // Defaulted

		for (String arg : args) {
			if (arg.startsWith(INPUT_PRM_PREFIX)) {
				nmeaFileName = arg.substring(INPUT_PRM_PREFIX.length());
			} else if (arg.startsWith(OUTPUT_PRM_PREFIX)) {
				csvOutputName = arg.substring(OUTPUT_PRM_PREFIX.length());
			} else if (arg.startsWith(STRINGS_PRM_PREFIX)) {
				nmeaStringsToParse = arg.substring(STRINGS_PRM_PREFIX.length());
			} else if (arg.startsWith(BREAK_AT_PRM_PREFIX)) {
				breakAt = arg.substring(BREAK_AT_PRM_PREFIX.length());
			}
		}

		if (nmeaFileName.isEmpty() ||
				csvOutputName.isEmpty() ||
				nmeaStringsToParse.isEmpty()) {
			System.err.println("Require all --in:, --out: and --data: arguments\n" +
					"like --in:./sample-data/my.logging.nmea --out:./today.csv --data:RMC,HDG,VHW,MWV,MTW");
			throw new IllegalArgumentException("Require --in:, --out: and --data: arguments.");
		}

		Map<String, String> oneCsvLine = new HashMap<>();
		StringBuffer fullCsvHeader = new StringBuffer();

		List<String> dataToWrite = Arrays.asList(nmeaStringsToParse.split(","));
		AtomicInteger ai = new AtomicInteger(0);
		List<String> badStrings = new ArrayList<>();
		dataToWrite.forEach(key -> {
			if (VERBOSE) {
				System.out.println(String.format("String %s", key));
			}
			StringParsers.Dispatcher dispatcher = StringParsers.findDispatcherByKey(key);
			if (dispatcher == null) {
				System.err.println(String.format("No parser found for %s", key));
				ai.incrementAndGet();
				badStrings.add(key);
			} else {
				Class returnedType = dispatcher.returnedType();
				System.out.println(String.format("Will retain %s, %s", key, dispatcher.description()));
				if (VERBOSE) {
					System.out.println(String.format("Parsing %s returns %s: %s",
							key,
							returnedType.getName(),
							NMEAComposite.class.isAssignableFrom(returnedType) ||
									returnedType.equals(Double.class) ||
									returnedType.equals(Float.class) ||
									returnedType.equals(Integer.class) ||
									returnedType.equals(String.class) ? "OK" : "not OK"));
				}
				if (!NMEAComposite.class.isAssignableFrom(returnedType)) {
					if (returnedType.equals(Double.class) ||
							returnedType.equals(Float.class) ||
							returnedType.equals(Integer.class) ||
							returnedType.equals(String.class)) { // Almost scalar, will do.
						String oneColHeader = key.toLowerCase();
						fullCsvHeader.append(String.format("%s%s", (fullCsvHeader.length() > 0 ? SEPARATOR : ""), oneColHeader));
						oneCsvLine.put(key, null);
					} else {
						System.err.println(String.format("No proper type returned after parsing %s", key));
						ai.incrementAndGet();
						badStrings.add(key);
					}
				} else {
					try {
						String header = (String)returnedType.getDeclaredMethod("getCsvHeader", String.class).invoke(null, SEPARATOR); // 1st arg, no Obj, method is static
						if (VERBOSE) {
							System.out.println(String.format("CSV Header [%s]", header));
						}
						fullCsvHeader.append(String.format("%s%s", (fullCsvHeader.length() > 0 ? SEPARATOR : ""), header));
						oneCsvLine.put(key, null);
					} catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | IllegalArgumentException exc) {
						exc.printStackTrace();
					}
				}
			}
		});
//		System.out.println(String.format("CSV Header: %s", fullCsvHeader.toString()));
		if (ai.get() > 0) {
			System.err.println(String.format("%d  un-managed string(s):", ai.get()));
			String badOnes = String.join(", ", badStrings);
			System.err.println(badOnes);
			throw new IllegalArgumentException(String.format("Not suited for CSV: %s", badOnes));
		}

		// Last check: is breakAt in the strings list
		if (!dataToWrite.contains(breakAt)) {
			throw new IllegalArgumentException(String.format("Break At Sentence [%s] is not in the Sentence list [%s]",
					breakAt,
					String.join(", ", dataToWrite)));
		}
		System.out.println(String.format("Will turn %s into %s, using strings %s, breaking at %s",
				nmeaFileName,
				csvOutputName,
				nmeaStringsToParse,
				breakAt));
		// Tests for now
//		System.out.println(RMC.getCsvHeader(SEPARATOR));

		try {
			BufferedReader input = new BufferedReader(new FileReader(nmeaFileName));
			BufferedWriter output = new BufferedWriter(new FileWriter(csvOutputName));
			// Write CSV header
			output.write(fullCsvHeader.toString() + "\n");

			String line;
			while ((line = input.readLine()) != null) {
				if (line.trim().length() > 0) {
					try {
						StringParsers.ParsedData parsedData = StringParsers.autoParse(line);
						if (dataToWrite.contains(parsedData.getSentenceId())) {
							// System.out.println(String.format("Managing %s", parsedData.getSentenceId()));
							// Populate the map here
							if (parsedData.getParsedData() instanceof NMEAComposite) {
								String data = ((NMEAComposite)parsedData.getParsedData()).getCsvData(SEPARATOR);
								oneCsvLine.put(parsedData.getSentenceId(), data);
							} else if (parsedData.getParsedData() instanceof Double ||
									parsedData.getParsedData() instanceof Float ||
									parsedData.getParsedData() instanceof Integer ||
									parsedData.getParsedData() instanceof String) {
								String data = String.valueOf(parsedData.getParsedData());
								oneCsvLine.put(parsedData.getSentenceId(), data);
							}
							if (parsedData.getSentenceId().equals(breakAt)) { // Print one line
								String csvLine = dataMapToCSVLine(dataToWrite, oneCsvLine, SEPARATOR);
								output.write(csvLine + "\n");
							}
						}

					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			}
			input.close();
			output.flush();
			output.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
