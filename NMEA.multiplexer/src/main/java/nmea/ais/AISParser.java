package nmea.ais;

import nmea.parser.StringParsers;
import utils.StringUtils;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Arrays;
import java.util.List;

/**
 * Work in Progress
 */
public class AISParser {
	public final static boolean verbose = "true".equals(System.getProperty("ais.verbose"));
	/*
   * !AIVDM,1,1,,A,15NB>cP03jG?l`<EaV0`MFO000S>,0*39
   * ^      ^ ^  ^ ^                            ^ ^
   * |      | |  | |                            | NMEA Checksum
   * |      | |  | |                            End of message
   * |      | |  | Encoded AIS Data
   * |      | |  AIS Channel (A or B)
   * |      | Sentence Number
   * |      Number of sentences
   * NMEA Message type, for AIS
   */

  /*
   * AIS: Automatic Identification System
   *
   * See http://gpsd.berlios.de/AIVDM.html
   *     http://catb.org/gpsd/AIVDM.html
   *
AIS Message Type 1:
  1-6     Message Type
  7-8     Repeat Indicator
  9-38    userID (MMSI)
  39-42   Navigation Satus
  43-50   Rate of Turn (ROT)
  51-60   Speed Over Ground (SOG)
  61-61   Position Accuracy
  62-89   Longitude
  90-116  latitude
  117-128 Course Over Ground (COG)
  129-137 True Heading (HDG)
  138-143 Time Stamp (UTC Seconds)
  144-146 Regional RESERVED
  147-148 Spare
  149-149 Receiver Autonomous Integrity Monitoring (RAIM)
  149-151 SOTDMA Sync State
  152-154 SOTDMA Slot Timeout
  155-168 SOTDMA Slot Offset

AIS Message type 2:
  1-6     Message Type
  7-8     Repeat Indicator
  9-38    userID (MMSI)
  39-42   Navigation Satus
  43-50   Rate of Turn (ROT)
  51-60   Speed Over Ground (SOG)
  61-61   Position Accuracy
  62-89   Longitude
  90-116  latitude
  117-128 Course Over Ground (COG)
  129-137 True Heading (HDG)
  138-143 Time Stamp (UTC Seconds)
  144-146 Regional RESERVED
  147-148 Spare
  149-149 Receiver Autonomous Integrity Monitoring (RAIM)
  149-151 SOTDMA Sync State
  152-154 SOTDMA Slot Timeout
  155-168 SOTDMA Slot Offset
   */

	public enum AISData {
		MESSAGE_TYPE(0, 6, "Message Type"),
		REPEAT_INDICATOR(6, 8, "Repeat Indicator"),
		MMSI(8, 38, "userID (MMSI)"),
		NAV_STATUS(38, 42, "Navigation Status"),
		ROT(42, 50, "Rate of Turn"),
		SOG(50, 60, "Speed Over Ground"),
		POS_ACC(60, 61, "Position Accuracy"),
		LONGITUDE(61, 89, "Longitude"),
		LATITUDE(89, 116, "Latitude"),
		COG(116, 128, "Course Over Ground"),
		HDG(128, 137, "True Heading"),
		TIME_STAMP(137, 143, "Time Stamp (UTC Seconds)");

		private static final long serialVersionUID = 1L;

		private final int from;          // start offset
		private final int to;            // end offset
		public final String description; // Description

		AISData(int from, int to, String desc) {
			this.from = from;
			this.to = to;
			this.description = desc;
		}

		public int from() {
			return from;
		}

		public int to() {
			return to;
		}

		public String description() {
			return description;
		}
	}

	public final static String AIS_PREFIX = "!AIVDM";
	private final static int PREFIX_POS = 0;
	private final static int NB_SENTENCES_POS = 1;
	private final static int AIS_DATA_POS = 5;

	public static AISRecord parseAIS(String sentence) {
		boolean valid = StringParsers.validCheckSum(sentence);
		if (!valid) {
			throw new RuntimeException("Invalid AIS Data (Bad checksum) for [" + sentence + "]");
		}
		String[] dataElement = sentence.split(",");
		if (!dataElement[PREFIX_POS].equals(AIS_PREFIX)) {
			throw new RuntimeException("Unmanaged AIS Prefix [" + dataElement[PREFIX_POS] + "].");
		}

		if (!dataElement[NB_SENTENCES_POS].equals("1")) { // More than 1 message: Not Managed
			throw new RuntimeException(String.format("String [%s], more than 1 message (%s). Not managed yet.", sentence, dataElement[NB_SENTENCES_POS]));
			// return null;
		}

		AISRecord aisRecord = new AISRecord(System.currentTimeMillis());
		String aisData = dataElement[AIS_DATA_POS];
//  System.out.println("[" + aisData + "]");
		String binString = encodedAIStoBinaryString(aisData);
//  System.out.println(binString);

		for (AISData a : AISData.values()) {
			if (a.to() < binString.length()) {
				String binStr = binString.substring(a.from(), a.to());
				int intValue = Integer.parseInt(binStr, 2);
				if (a.equals(AISData.LATITUDE) || a.equals(AISData.LONGITUDE)) {
					if ((a.equals(AISData.LATITUDE) && intValue != (91 * 600_000) && intValue > (90 * 600_000)) ||
							(a.equals(AISData.LONGITUDE) && intValue != (181 * 600_000) && intValue > (180 * 600_000))) {
						intValue = -Integer.parseInt(neg(binStr), 2);
					}
				} else if (a.equals(AISData.ROT)) {
					if (intValue > 128) {
						intValue = -Integer.parseInt(neg(binStr), 2);
					}
				}
				setAISData(a, aisRecord, intValue);
				if (verbose) {
					System.out.println(a + " [" + binStr + "] becomes [" + intValue + "]");
				}
			} else if (verbose) {
				System.out.println(">> Out of binString");
			}
		}
//		if (aisRecord.getMmsi() == 368031880) {
//			System.out.println("AIS:" + aisData);
//			System.out.println(aisRecord.toString());
//		}
		return aisRecord;
	}

	/**
	 * 2's complement, for negative numbers.
	 *
	 * @param binStr binary String
	 * @return the 2's complement value
	 */
	private static String neg(String binStr) {
		StringBuilder s = new StringBuilder();
		for (int i = 0; i < binStr.length(); i++) {
			s.append(binStr.charAt(i) == '0' ? '1' : '0');
		}
		return s.toString();
	}

	private static void setAISData(AISData a, AISRecord ar, int value) {
		if (a.equals(AISData.MESSAGE_TYPE)) {
			ar.setMessageType(value);
		} else if (a.equals(AISData.REPEAT_INDICATOR)) {
			ar.setRepeatIndicator(value);
		} else if (a.equals(AISData.MMSI)) {
			ar.setMMSI(value);
		} else if (a.equals(AISData.NAV_STATUS)) {
			ar.setNavStatus(value);
		} else if (a.equals(AISData.ROT)) {
			ar.setRot(value);
		} else if (a.equals(AISData.SOG)) {
			ar.setSog(value);
		} else if (a.equals(AISData.POS_ACC)) {
			ar.setPosAcc(value);
		} else if (a.equals(AISData.LONGITUDE)) {
			ar.setLongitude(value);
		} else if (a.equals(AISData.LATITUDE)) {
			ar.setLatitude(value);
		} else if (a.equals(AISData.COG)) {
			ar.setCog(value);
		} else if (a.equals(AISData.HDG)) {
			ar.setHdg(value);
		} else if (a.equals(AISData.TIME_STAMP)) {
			ar.setUtc(value);
		}
	}

	private static String encodedAIStoBinaryString(String encoded) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < encoded.length(); i++) {
			int c = encoded.charAt(i);
			c -= 48;
			if (c > 40) {
				c -= 8;
			}
			String bin = StringUtils.lpad(Integer.toBinaryString(c), 6, "0");
			sb.append(bin);
			if (verbose) {
				System.out.println(encoded.charAt(i) + " becomes " + bin + " (" + c + ")");
			}
//    sb.append(" ");
		}
		return sb.toString();
	}

	public static class AISRecord {
		private int messageType;
		private int repeatIndicator;
		private int MMSI;
		private int navStatus;
		private int rot;
		private float sog;
		private int posAcc;
		private float longitude;
		private float latitude;
		private float cog;
		private int hdg;
		private int utc;
		private long recordTimeStamp;

		AISRecord(long now) {
			super();
			recordTimeStamp = now;
		}

		public void setMessageType(int messageType) {
			this.messageType = messageType;
		}

		public int getMessageType() {
			return messageType;
		}

		public void setRepeatIndicator(int repeatIndicator) {
			this.repeatIndicator = repeatIndicator;
		}

		public int getRepeatIndicator() {
			return repeatIndicator;
		}

		public void setMMSI(int MMSI) {
			this.MMSI = MMSI;
		}

		public int getMMSI() {
			return MMSI;
		}

		public void setNavStatus(int navStatus) {
			this.navStatus = navStatus;
		}

		public int getNavStatus() {
			return navStatus;
		}

		public void setRot(int rot) {
			this.rot = rot;
		}

		public int getRot() {
			return rot;
		}

		public void setSog(int sog) {
			this.sog = (sog / 10f);
		}

		public float getSog() {
			return sog;
		}

		public void setPosAcc(int posAcc) {
			this.posAcc = posAcc;
		}

		public int getPosAcc() {
			return posAcc;
		}

		public void setLongitude(int longitude) {
			this.longitude = (longitude / 600_000f);
		}

		public float getLongitude() {
			return longitude;
		}

		public void setLatitude(int latitude) {
			this.latitude = (latitude / 600_000f);
		}

		public float getLatitude() {
			return latitude;
		}

		public void setCog(int cog) {
			this.cog = (cog / 10f);
		}

		public float getCog() {
			return cog;
		}

		public void setHdg(int hdg) {
			this.hdg = hdg;
		}

		public int getHdg() {
			return hdg;
		}

		public void setUtc(int utc) {
			this.utc = utc;
		}

		public int getUtc() {
			return utc;
		}

		static String decodeStatus(int stat) {
			String status = "";
			switch (stat) {
				case 0:
					status = "Under way using engine";
					break;
				case 1:
					status = "At anchor";
					break;
				case 2:
					status = "Not under command";
					break;
				case 3:
					status = "Restricted maneuverability";
					break;
				case 4:
					status = "Constrained by her draught";
					break;
				case 5:
					status = "Moored";
					break;
				case 6:
					status = "Aground";
					break;
				case 7:
					status = "Engaged in fishing";
					break;
				case 8:
					status = "Under way sailing";
					break;
				case 9:
				case 10:
				case 11:
				case 12:
				case 13:
				case 14:
					status = "Reserved for future...";
					break;
				case 15:
				default:
					status = "Not defined";
					break;
			}
			return status;
		}

		@Override
		public String toString() {
			String str = "";
			str = "Type:" + messageType + ", Repeat:" + repeatIndicator + ", MMSI:" + MMSI + ", status:" + decodeStatus(navStatus) + ", rot:" + rot +
							", Pos:" + latitude + "/" + longitude + " (Acc:" + posAcc + "), COG:" + cog + ", SOG:" + sog + ", HDG:" + hdg;
			return str;
		}

		public void setRecordTimeStamp(long recordTimeStamp) {
			this.recordTimeStamp = recordTimeStamp;
		}

		public long getRecordTimeStamp() {
			return recordTimeStamp;
		}
	}

	public static void main(String... args) throws Exception {
		String ais; // Error in !AIVDM,1,1,,B,?03Ovk1E6T50D00,2*1E
		if (args.length > 0) {
			System.out.println(parseAIS(args[0]));
		} else {
			ais = "!AIVDM,1,1,,A,14eG;o@034o8sd<L9i:a;WF>062D,0*7D";
			System.out.println(parseAIS(ais));

			ais = "!AIVDM,1,1,,A,15NB>cP03jG?l`<EaV0`MFO000S>,0*39";
			System.out.println(parseAIS(ais));

			ais = "!AIVDM,1,1,,B,177KQJ5000G?tO`K>RA1wUbN0TKH,0*5C";
			System.out.println(parseAIS(ais));

			ais = "!AIVDM,1,1,,A,D03Ovk06AN>40Hffp00Nfp0,2*52";
			System.out.println(parseAIS(ais));

			ais = "!AIVDM,2,2,2,B,RADP,0*10";
			try {
				System.out.println(parseAIS(ais));
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		System.out.println("--- From dAISy ---");
		// From the dAISy device
		List<String> aisFromDaisy = Arrays.asList(
				"!AIVDM,1,1,,A,403Ovk1v@CPI`o>jNnEdjEg0241T,0*5F",
				"!AIVDM,1,1,,A,D03Ovk06AN>40Hffp00Nfp0,2*52",
				"!AIVDM,1,1,,A,D03Ovk0m9N>4g@ffpfpNfp0,2*38",
				"!AIVDM,1,1,,B,D03Ovk0s=N>4g<ffpfpNfp0,2*5D",
				"!AIVDM,1,1,,B,403Ovk1v@CPN`o>jO8EdjDw02@GT,0*1F",
				"!AIVDM,1,1,,A,403Ovk1v@CPO`o>jNrEdjEO02<45,0*01");
		aisFromDaisy.forEach(aisStr -> System.out.println(parseAIS(aisStr)));
		System.out.println("------------------");

		String dataFileName = "sample.data/ais.nmea";
		if (args.length > 0) {
			dataFileName = args[0];
		}
		try {
			BufferedReader br = new BufferedReader(new FileReader(dataFileName));
			System.out.println(String.format("---- From data file %s ----", dataFileName));
			String line = "";
			while (line != null) {
				line = br.readLine();
				if (line != null) {
					if (!line.startsWith("#") && line.startsWith(AIS_PREFIX)) {
						try {
							AISRecord aisRecord = parseAIS(line);
							if (aisRecord != null) {
								System.out.println(aisRecord);
							} else {
								System.out.println(String.format(">> NULL AIS Record for %s", line));
							}
						} catch (Exception ex) {
							System.err.println("For [" + line + "], " + ex.toString());
						}
					} else if (!line.startsWith("#")) {
						// TODO else parse NMEA
						System.out.println(String.format("Non AIS String... %s", line));
					}
				}
			}
			br.close();
		} catch (FileNotFoundException fnfe) {
			fnfe.printStackTrace();
		}
	}
}
