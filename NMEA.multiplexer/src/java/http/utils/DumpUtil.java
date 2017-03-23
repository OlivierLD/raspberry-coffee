package http.utils;

import nmea.utils.NMEAUtils;

import java.io.PrintStream;
import java.util.Arrays;

public class DumpUtil {
	private final static int LINE_LEN = 16;

	public static void displayDualDump(byte[] ba) {
		displayDualDump(ba, null);
	}

	public static void displayDualDump(String str) {
		displayDualDump(str, null);
	}

	public static void displayDualDump(String str, PrintStream ps) {
		displayDualDump(str.getBytes(), ps);
	}

	public static void displayDualDump(byte[] ba, PrintStream ps) {
		PrintStream out = (ps != null ? ps : System.out);
		String[] sa = DumpUtil.dualDump(ba);
		if (sa != null) {
			Arrays.stream(sa).forEach(str -> out.println("\t" + str));
		}
	}

	public static String[] dualDump(String str) {
		byte[] ba = str.getBytes();
		return dualDump(ba);
	}

	/*
	 * Readable + HexASCII code.
	 * @see LINE_LEN member
	 */
	public static String[] dualDump(byte[] ba) {
		int dim = ba.length / LINE_LEN;
		String[] result = new String[dim + 3]; // 2 first lines are labels
		String first = "     ";
		for (int i=0; i<LINE_LEN; i++) {
			first += (NMEAUtils.lpad(Integer.toHexString(i & 0xFF).toUpperCase(), 2, "x") + " ");
		}
		result[0] = first;
		String second = "---+-";
		for (int i=0; i<LINE_LEN; i++) {
			second += "---";
		}
		second += "-+--";
		for (int i=0; i<LINE_LEN; i++) {
			second += "-";
		}
		result[1] = second;

		for (int l = 0; l < (dim + 1); l++) {
			String lineLeft = (NMEAUtils.lpad(Integer.toHexString(l & 0xFF).toUpperCase(), 2, "0") + " | ");
			String lineRight = "";
			int start = l * LINE_LEN;
			for (int c = start; c < Math.min(start + LINE_LEN, ba.length); c++) {
				lineLeft += (NMEAUtils.lpad(Integer.toHexString(ba[c] & 0xFF).toUpperCase(), 2, "0") + " ");
				lineRight += (isAsciiPrintable((char) ba[c]) ? (char) ba[c] : ".");
			}
			lineLeft = NMEAUtils.rpad(lineLeft, (3 * LINE_LEN) + 5, " ");
			result[l + 2] = lineLeft + " |  " + lineRight;
		}
		return result;
	}

	public static String dumpHexMess(byte[] mess) {
		String line = "";
		for (int i = 0; i < mess.length; i++)
			line += (NMEAUtils.lpad(Integer.toHexString(mess[i] & 0xFF).toUpperCase(), 2, "0") + " ");
		return line.trim();
	}

	/**
	 * Might not work with some encodings...
	 *
	 * @param ch The character to test
	 * @return true when printable.
	 */
	public static boolean isAsciiPrintable(char ch) {
		return ch >= 32 && ch < 127;
	}

	public static void main(String... args) {
		String forTests = "$GPGSA,A,3,07,17,30,11,28,13,01,19,,,,,2.3,1.4,1.9*3D";
		String[] dd = dualDump(forTests);
		for (String l : dd) {
			System.out.println(l);
		}
	}
}
