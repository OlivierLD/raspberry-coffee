package nmea.utils;

import nmea.parser.StringParsers;

import java.util.Arrays;

public class AutoParser {
	public static void main(String... args) {
		if (args.length == 0) {
			System.out.println("I need at least one string to parse as parameter...");
		} else {
			Arrays.asList(args)
					.stream()
					.forEach(nmea -> {
				try {
					System.out.printf("Parsing [%s]\n", nmea);
					StringParsers.ParsedData obj = StringParsers.autoParse(nmea);
					if (obj != null) {
						System.out.printf(">> Parsed >> %s\n", obj.getParsedData().toString());
					} else {
						System.out.println(">> null");
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			});
		}
	}
}
