package tideengine.tests;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/*

Rahiroa (Rangiroa) Island => Apia, Samoa Islands

???C33o?Apia, Samoa Islands                                                                       ?? ??? ?Wxtide32                     Wxtide32                     Rahiroa ?Rangiroa? Island                                                                 ?????D????????8??D;?????
0123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789
0         1         2         3         4         5         6         7         8         9         0         1         2         3         4         5         6         7         8         9         0         1         2         3         4         5         6         7
0                                                                                                   10                                                                                                  20

 */

/**
 * This is just a test, not a unit-test
 */
public class SecondaryStationsTest {
	private final static NumberFormat NF = new DecimalFormat("###0");

	private final static List<Character> PUNCTUATION = Arrays.asList(',', ';', '.', '!', '(', ')');

	private final static int LINE_SIZE = 278;

	private final static int REF_STATION_NAME_START_OFFSET = 8; // Zero-based
	private final static int REF_STATION_NAME_END_OFFSET = 97;
	private final static int SECONDARY_STATION_NAME_START_OFFSET = 164;
	private final static int SECONDARY_STATION_NAME_END_OFFSET = 253;

	public static void main(String... args) throws Exception {
		String datFileName = "2Wxtide32.dat"; // "Base de données personelle.dat";
		File datFile = new File(datFileName);
		FileInputStream br = new FileInputStream(datFile);
		boolean go = true;
		int nbl = 0;
		byte[] ba = new byte[LINE_SIZE];
		int offset = 0;
		while (go) {
			int s = br.read(ba, 0, LINE_SIZE);
			if (s >= 0) {
				offset += s;
				nbl++;

				String refStationName = bytesToString(ba, REF_STATION_NAME_START_OFFSET, REF_STATION_NAME_END_OFFSET);
				String secondaryStation = bytesToString(ba, SECONDARY_STATION_NAME_START_OFFSET, SECONDARY_STATION_NAME_END_OFFSET);

//      System.out.println(secondaryStation.trim() + " => " + refStationName.trim());

				if (refStationName.trim().contains("Apia")) {
//        System.out.println(secondaryStation.trim() + " => " + refStationName.trim());
					if (secondaryStation.contains("Rangiroa")) {
						System.out.println(secondaryStation.trim() + " => " + refStationName.trim());

						System.out.println("F #1:" + bytesToDouble(ba, 0, 7) +
								", #2:" + bytesToDouble(ba, 98, 105) +
								", #3:" + bytesToDouble(ba, 254, 261) +
								", #4:" + bytesToDouble(ba, 262, 269) +
								", #5:" + bytesToDouble(ba, 270, 277));

						System.out.println("D #1:" + bytesToDouble(ba, 254, 269) +
								", F #2:" + bytesToDouble(ba, 270, 277));
						System.out.println("F #1:" + bytesToDouble(ba, 254, 261) +
								", D #2:" + bytesToDouble(ba, 262, 277));

						System.out.println("S #1:" + bytesToInt(ba, 0, 3) + ", #2:" + bytesToInt(ba, 4, 7) +
								", #3:" + bytesToInt(ba, 98, 101) + ", #4:" + bytesToInt(ba, 102, 105) +
								", #5:" + bytesToInt(ba, 254, 257) + ", #6:" + bytesToInt(ba, 258, 261) +
								", #7:" + bytesToInt(ba, 262, 265) + ", #8:" + bytesToInt(ba, 266, 269) +
								", #9:" + bytesToInt(ba, 270, 273) + ", #10:" + bytesToInt(ba, 274, 277));

						System.out.println("I #1:" + bytesToInt(ba, 0, 7) +
								", #2:" + bytesToInt(ba, 98, 105) +
								", #3:" + bytesToInt(ba, 254, 261) +
								", #4:" + bytesToInt(ba, 262, 269) +
								", #5:" + bytesToInt(ba, 270, 277));
						System.out.println();

						System.out.println("Characters");
						for (int i = 0; i < LINE_SIZE; i++) {
							char c = (char) ba[i];
							if (Character.isSpaceChar(c) || Character.isLetterOrDigit(c) || PUNCTUATION.contains(c)) {
								System.out.print(c);
							} else {
								System.out.print("?");
							}
						}
						System.out.println();
						System.out.println("Hexa");
						for (int i = 0; i < LINE_SIZE; i++) {
							System.out.print(lpad(Integer.toHexString(ba[i] & 0xff).toUpperCase(), 2, "0") + " ");
						}
						System.out.println();
						System.out.println("Unsigned");
						for (int i = 0; i < LINE_SIZE; i++) {
							System.out.print(Integer.toString(ba[i] & 0xff) + " ");
						}
						System.out.println();
						System.out.println("Signed");
						for (int i = 0; i < LINE_SIZE; i++) {
							System.out.print(Integer.toString(ba[i]) + " ");
						}
						System.out.println();
					}
				}
			} else {
				go = false;
			}
		}
		System.out.println("Found " + nbl + " line(s)");
		br.close();
	}

	private final static String bytesToString(byte[] raw, int fromOffset, int toOffset) {
		String s = "";
		int len = toOffset - fromOffset;
		byte[] ba = new byte[len];
		for (int i = 0; i < len; i++) {
			ba[i] = raw[fromOffset + i];
		}
		s = new String(ba);
		return s;
	}

	private final static double bytesToDouble(byte[] raw, int fromOffset, int toOffset) {
		int len = toOffset - fromOffset + 1;
		byte[] ba = new byte[len];
		for (int i = 0; i < len; i++) {
			ba[i] = raw[fromOffset + i];
		}
		DataInputStream dis = new DataInputStream(new ByteArrayInputStream(ba));
		double d = 0d;
		try {
			d = (len == 8) ? dis.readFloat() : dis.readDouble();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return d;
	}

	private final static int bytesToInt(byte[] raw, int fromOffset, int toOffset) {
		int len = toOffset - fromOffset + 1;
		byte[] ba = new byte[len];
		for (int i = 0; i < len; i++) {
			ba[i] = raw[fromOffset + i];
		}
		DataInputStream dis = new DataInputStream(new ByteArrayInputStream(ba));
		int d = 0;
		try {
			d = (len == 4) ? dis.readShort() : dis.readInt();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return d;
	}

	private static String lpad(String s, int len, String with) {
		String str = s;
		while (str.length() < len) {
			str = with + str;
		}
		return str;
	}
}
