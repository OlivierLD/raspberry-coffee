package utils;

import java.util.ArrayList;
import java.util.List;

public class StringUtils {
	/**
	 * Right pad, with blanks
	 *
	 * @param s String to pad
	 * @param len padding string
	 * @return padded string
	 */
	public static String rpad(String s, int len) {
		return rpad(s, len, " ");
	}

	public static String rpad(String s, int len, String pad) {
		String str = s;
		while (str.length() < len) {
			str += pad;
		}
		return str;
	}

	/**
	 * Left pad, with blanks
	 *
	 * @param s String to pad
	 * @param len padding string
	 * @return padded string
	 */
	public static String lpad(String s, int len) {
		return lpad(s, len, " ");
	}

	public static String lpad(String s, int len, String pad) {
		String str = s;
		while (str.length() < len) {
			str = pad + str;
		}
		return str;
	}

	/**
	 * Workaround: in some cases (NMEA String from a zip?), some NULs sneak in the strings...
	 * Streaming Bytes is not really done with Java8 Streams...
	 */
	public static String removeNullsFromString(String str) {
		List<Byte> strBytes = new ArrayList<>();
		byte[] bytesFromDegrees = str.getBytes();
		boolean foundNull = false;
		for (int i = 0; i < bytesFromDegrees.length; i++) {
			if (bytesFromDegrees[i] != (byte) 0) {
				strBytes.add(bytesFromDegrees[i]);
			} else {
				foundNull = true;
			}
		}
		// Verbose?
		if (foundNull && "true".equals(System.getProperty("string.null.verbose"))) {
			System.err.println("Found Null(s) in String:");
			DumpUtil.displayDualDump(str, System.err);
		}
		byte[] newStrBA = new byte[strBytes.size()];
		for (int i = 0; i < strBytes.size(); i++) {
			newStrBA[i] = strBytes.get(i).byteValue();
		}
		String cleanString = new String(newStrBA);
		if (foundNull && "true".equals(System.getProperty("string.null.verbose"))) {
			System.err.println(">>> Turned into:");
			DumpUtil.displayDualDump(cleanString, System.err);
		}
		return cleanString;
	}

}
