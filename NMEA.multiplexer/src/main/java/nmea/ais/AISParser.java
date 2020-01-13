package nmea.ais;

import nmea.parser.StringParsers;
import utils.StringUtils;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AIS: Automatic Identification System
 * Work in Progress
 * Good doc at https://gpsd.gitlab.io/gpsd/AIVDM.html
 * https://www.navcen.uscg.gov/?pageName=AISFAQ
 * On-line decoder at https://www.aggsoft.com/ais-decoder.htm
 */
public class AISParser {
	public final static boolean verbose = "true".equals(System.getProperty("ais.verbose"));
	/*
   * !AIVDM,1,1,,A,15NB>cP03jG?l`<EaV0`MFO000S>,0*39
   * ^      ^ ^  ^ ^                            ^ ^
   * |      | |  | |                            | NMEA Checksum
   * |      | |  | |                            End of message. Number of fill bits for padding.
   * |      | |  | Encoded AIS Data
   * |      | |  AIS Channel (A or B)
   * |      | Sentence Number
   * |      Number of sentences
   * NMEA Message type, for AIS
   */

	public enum AISData { // Generic, first 3 fields.
		MESSAGE_TYPE(0, 6, "Message Type"),
		REPEAT_INDICATOR(6, 8, "Repeat Indicator"),
		MMSI(8, 38, "userID (MMSI)");

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

	public enum AISDataType123 { // For types 1, 2 and 3. Position Report Class A
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

		AISDataType123(int from, int to, String desc) {
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

	public enum AISDataType4 { // Base Station Report
		MESSAGE_TYPE(0, 6, "Message Type"),
		REPEAT_INDICATOR(6, 8, "Repeat Indicator"),
		MMSI(8, 38, "userID (MMSI)"),
		UTC_YEAR(38, 52, "UTC Year"),
		UTC_MONTH(52, 56, "UTC Month"),
		UTC_DAY(56, 61, "UTC Day"),
		UTC_HOUR(61, 66, "UTC Hour"),
		UTC_MINUTE(66, 72, "UTC Minute"),
		UTC_SECOND(72, 78, "UTC Second"),
		POS_ACC(78, 79, "Position Accuracy"),
		LONGITUDE(79, 107, "Longitude"),
		LATITUDE(107, 134, "Latitude");

		private static final long serialVersionUID = 1L;

		private final int from;          // start offset
		private final int to;            // end offset
		public final String description; // Description

		AISDataType4(int from, int to, String desc) {
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

	// Type 5 (Boat data), on 2 sentences.
	public enum AISDataType5 { // Static and Voyage Related Data
		MESSAGE_TYPE(0, 6, "Message Type"),
		REPEAT_INDICATOR(6, 8, "Repeat Indicator"),
		MMSI(8, 38, "userID (MMSI)"),
		AIS_VERSION(38, 40, "AIS Version"),
		IMO_NUMBER(40, 70, "IMO Number"),
		CALL_SIGN(70, 112, "Call Sign"),
		VESSEL_NAME(112, 232, "Vessel Name"),
		SHIP_TYPE(232, 240, "Ship Type"),
		DIM_TO_BOW(240, 249, "Dimension to Bow"),
		DIM_TO_STERN(249, 258, "Dimension to Stern"),
		DIM_TO_PORT(258, 264, "Dimension to Port"),
		DIM_TO_STBD(264, 270, "Dimension to Starboard"),
		EPFD(270, 274, "EPFD"),
		ETA_MONTH(274, 278, "ETA Month"),
		ETA_DAY(278, 283, "ETA Day"),
		ETA_HOUR(283, 288, "ETA Hour"),
		ETA_MINUTE(288, 294, "ETA Minute"),
		DRAUGHT(294, 302, "Draught"),
		DESTINATION(302, 422, "Destination"),
		DTE(422, 423, "DTE"),
		SPARE(423, 424, "Spare");

		private static final long serialVersionUID = 1L;

		private final int from;          // start offset
		private final int to;            // end offset
		public final String description; // Description

		AISDataType5(int from, int to, String desc) {
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

	public enum AISDataType8 { // Binary Broadcast Message
		MESSAGE_TYPE(0, 6, "Message Type"),
		REPEAT_INDICATOR(6, 8, "Repeat Indicator"),
		MMSI(8, 38, "userID (MMSI)"),
		SPARE(38, 40, "Spare"),
		DES_AREA_CODE(40, 50, "Designated Area Code"),
		FUNC_ID(50, 56, "Functional ID"),
		DATA(56, 953, "Data"); // May be shorter than 952

		private static final long serialVersionUID = 1L;

		private final int from;          // start offset
		private final int to;            // end offset
		public final String description; // Description

		AISDataType8(int from, int to, String desc) {
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

	public enum AISDataType15 { // Interrogation
		MESSAGE_TYPE(0, 6, "Message Type"),
		REPEAT_INDICATOR(6, 8, "Repeat Indicator"),
		MMSI(8, 38, "userID (MMSI)"),
		SPARE_1(38, 40, "Spare"),
		INTERROGATED_MMSI(40, 70, "Interrogated MMSI"),
		FIRST_MESSAGE_TYPE(70, 76, "First message type"),
		FIRST_SLOT_OFFSET(76, 88, "First slot offset"),
		SPARE_2(88, 90, "Spare"),
		SECOND_MESSAGE_TYPE(90, 96, "Second message type"),
		SECOND_SLOT_OFFSET(96, 108, "Second slot offset"),
		SPARE_3(108, 110, "Spare"),
		INTERROGATED_MMSI_2(110, 140, "Interrogated MMSI (2)"),
		FIRST_MESSAGE_TYPE_2(140, 146, "First message type (2)"),
		FIRST_SLOT_OFFSET_2(146, 158, "First slot offset (2)"),
		SPARE_4(158, 160, "Spare");

		private static final long serialVersionUID = 1L;

		private final int from;          // start offset
		private final int to;            // end offset
		public final String description; // Description

		AISDataType15(int from, int to, String desc) {
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

	public enum AISDataType20 { // Data Link Management Message
		MESSAGE_TYPE(0, 6, "Message Type"),
		REPEAT_INDICATOR(6, 8, "Repeat Indicator"),
		MMSI(8, 38, "userID (MMSI)"),
		SPARE(38, 40, "Spare"),
		OFFSET_1(40, 52, "Offset 1"),
		RESERVED_1(52, 56, "Reserved"),
		TIMEOUT_1(56, 59, "Timeout 1"),
		INCREMENT_1(59, 70, "Increment 1"),
		OFFSET_2(70, 82, "Offset 2"),
		RESERVED_2(82, 86, "Reserved"),
		TIMEOUT_2(86, 89, "Timeout 2"),
		INCREMENT_2(89, 100, "Increment 2"),
		OFFSET_3(100, 112, "Offset 3"),
		RESERVED_3(112, 116, "Reserved"),
		TIMEOUT_3(116, 119, "Timeout 3"),
		INCREMENT_3(119, 130, "Increment 3"),
		OFFSET_4(130, 142, "Offset 4"),
		RESERVED_4(142, 146, "Reserved"),
		TIMEOUT_4(146, 149, "Timeout 4"),
		INCREMENT_4(149, 160, "Increment 4");

		private static final long serialVersionUID = 1L;

		private final int from;          // start offset
		private final int to;            // end offset
		public final String description; // Description

		AISDataType20(int from, int to, String desc) {
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

	public final static String AIS_PREFIX = "!AIVDM"; // TODO Other Talker IDs, AIVDM, AIVDO

	private final static int PREFIX_POS = 0;
	private final static int NB_SENTENCES_POS = 1;
	private final static int CURR_SENTENCE_IDX = 2;
	// Pos 3, Sequential Message ID for multi-sentence messages
	private final static int AIS_CHANNEL_POS = 4;
	private final static int AIS_DATA_POS = 5;

	public static int getMessageType(String sentence) {
		String[] dataElement = sentence.split(",");
		if (!dataElement[PREFIX_POS].equals(AIS_PREFIX)) {
			throw new RuntimeException("Unmanaged AIS Prefix [" + dataElement[PREFIX_POS] + "].");
		}
		String aisData = dataElement[AIS_DATA_POS];
		String binString = encodedAIStoBinaryString(aisData);

		int messageType = 0;
		// Get message type
		if (AISData.MESSAGE_TYPE.to() < binString.length()) {
			String binStr = binString.substring(AISData.MESSAGE_TYPE.from(), AISData.MESSAGE_TYPE.to());
			messageType = Integer.parseInt(binStr, 2);
		}
		return messageType;
	}

	private static StringBuffer unfinishedSentence = null;

	public static AISRecord parseAIS(String sentence) throws AISException {
		boolean valid = StringParsers.validCheckSum(sentence);
		if (!valid) {
			throw new RuntimeException("Invalid AIS Data (Bad checksum) for [" + sentence + "]");
		}
		String[] dataElement = sentence.split(",");
		if (!dataElement[PREFIX_POS].equals(AIS_PREFIX)) {
			throw new RuntimeException("Unmanaged AIS Prefix [" + dataElement[PREFIX_POS] + "].");
		}

		boolean multipleMessage = false;
		boolean multipleMessageReady = false;
		if (!dataElement[NB_SENTENCES_POS].equals("1")) { // More than 1 message.
			multipleMessage = true;
			if ("1".equals(dataElement[CURR_SENTENCE_IDX])) {
				unfinishedSentence = new StringBuffer();
			} else {
				if (unfinishedSentence == null) {
					// Incoherent state
					throw new AISException(String.format("Incoherent state: Message #%s, without having seen #1", dataElement[CURR_SENTENCE_IDX]));
				}
			}
		  if (dataElement[NB_SENTENCES_POS].equals(dataElement[CURR_SENTENCE_IDX])) {
			  multipleMessageReady = true;
		  }
//			throw new AISException(String.format("String [%s], more than 1 message (%s). Not managed yet.", sentence, dataElement[NB_SENTENCES_POS]));
			// return null;
		}

		AISRecord aisRecord = new AISRecord(System.currentTimeMillis());
		String aisData = dataElement[AIS_DATA_POS];
//  System.out.println("[" + aisData + "]");
		String binString = encodedAIStoBinaryString(aisData);
//  System.out.println(binString);
		if (multipleMessage) {
			unfinishedSentence.append(binString);
		}
		if (multipleMessage && multipleMessageReady) {
			binString = unfinishedSentence.toString();
		}

		int messageType = 0;
		// Get message type
		if (multipleMessage && !multipleMessageReady) {
			aisRecord = null;
		} else {
			if (AISData.MESSAGE_TYPE.to() < binString.length()) {
				String binStr = binString.substring(AISData.MESSAGE_TYPE.from(), AISData.MESSAGE_TYPE.to());
				messageType = Integer.parseInt(binStr, 2);
			}
		}

		switch (messageType) {
			case 0:
				// Multiple message, not ready yet
				break;
			case 1:
			case 2:
			case 3:
				for (AISDataType123 a : AISDataType123.values()) {
					if (a.to() < binString.length()) {
						String binStr = binString.substring(a.from(), a.to());
						int intValue = Integer.parseInt(binStr, 2);
						if (a.equals(AISDataType123.LATITUDE) || a.equals(AISDataType123.LONGITUDE)) {
							if ((a.equals(AISDataType123.LATITUDE) && intValue == (91 * 600_000)) ||
									(a.equals(AISDataType123.LONGITUDE) && intValue == (181 * 600_000))) {
								intValue = 0;
							} else if ((a.equals(AISDataType123.LATITUDE) && intValue > (90 * 600_000)) ||
									(a.equals(AISDataType123.LONGITUDE) && intValue > (180 * 600_000))) {
								intValue = -Integer.parseInt(twosComplement(new StringBuffer(binStr)), 2);
							}
						} else if (a.equals(AISDataType123.ROT)) {
							if (intValue > 128) {
								intValue = -Integer.parseInt(twosComplement(new StringBuffer(binStr)), 2);
							}
						}
						setAISData(a, aisRecord, intValue);
						if (verbose) {
							System.out.println(String.format("Data %s, %s, %d chars becomes %d",
									a,
									binStr,
									binStr.length(),
									intValue));
						}
					} else if (verbose) {
						System.out.println(">> Out of binString");
					}
				}
				break;
			case 4:
				for (AISDataType4 a : AISDataType4.values()) {
					if (a.to() < binString.length()) {
						String binStr = binString.substring(a.from(), a.to());
						int intValue = Integer.parseInt(binStr, 2);
						if (a.equals(AISDataType4.LATITUDE) || a.equals(AISDataType4.LONGITUDE)) {
							if ((a.equals(AISDataType4.LATITUDE) && intValue == (91 * 600_000)) ||
									(a.equals(AISDataType4.LONGITUDE) && intValue == (181 * 600_000))) {
								intValue = 0;
							} else if ((a.equals(AISDataType4.LATITUDE) && intValue > (90 * 600_000)) ||
									(a.equals(AISDataType4.LONGITUDE) && intValue > (180 * 600_000))) {
								intValue = -Integer.parseInt(twosComplement(new StringBuffer(binStr)), 2);
							}
						}
						setAISData(a, aisRecord, intValue);
						if (verbose) {
							System.out.println(String.format("Data %s, %s, %d chars becomes %d",
									a,
									binStr,
									binStr.length(),
									intValue));
						}
					} else if (verbose) {
						System.out.println(">> Out of binString");
					}
				}
				break;
			case 5:
				if (!multipleMessage) {
					// Bizarre
					throw new AISException("Type 5 and only 1 message?...");
				}
				if (multipleMessageReady) { // then process
					for (AISDataType5 a : AISDataType5.values()) {
						if (a.to() < binString.length()) {
							String binStr = binString.substring(a.from(), a.to());
							if (a.equals(AISDataType5.CALL_SIGN) || a.equals(AISDataType5.VESSEL_NAME) || a.equals(AISDataType5.DESTINATION)) {
								setAISData(a, aisRecord, decode6BitCharacters(binStr));
							} else {
								int intValue = Integer.parseInt(binStr, 2);
								setAISData(a, aisRecord, intValue);
							}
						} else if (verbose) {
							System.out.println(">> Out of binString");
						}
					}
					// After processing, reset buffer
					unfinishedSentence = null;
				} else {
					aisRecord = null;
				}
				break;
			case 8:
//				if (multipleMessageReady) { // then process. TODO See the multi-messages cases.
					for (AISDataType8 a : AISDataType8.values()) {
						if (a.to() < binString.length()) {
							String binStr = binString.substring(a.from(), a.to());
							if (a.equals(AISDataType8.DATA)) {
								setAISData(a, aisRecord, binStr);
							} else {
								int intValue = Integer.parseInt(binStr, 2);
								setAISData(a, aisRecord, intValue);
							}
						} else if (a.equals(AISDataType8.DATA)) {
							String binStr = binString.substring(a.from()); // Bin data can be shorter than 952
							if (a.equals(AISDataType8.DATA)) {
								setAISData(a, aisRecord, binStr);
							}
						} else if (verbose) {
							System.out.println(">> Out of binString");
						}
					}
					// After processing, reset buffer
					unfinishedSentence = null;
//				} else {
//					aisRecord = null;
//				}
				break;
			case 15:
				for (AISDataType15 a : AISDataType15.values()) {
					if (a.to() < binString.length()) {
						String binStr = binString.substring(a.from(), a.to());
						int intValue = Integer.parseInt(binStr, 2);
						setAISData(a, aisRecord, intValue);
						if (verbose) {
							System.out.println(String.format("Data %s, %s, %d chars becomes %d",
									a,
									binStr,
									binStr.length(),
									intValue));
						}
					} else if (verbose) {
						System.out.println(">> Out of binString");
					}
				}
				break;
			case 20:
				for (AISDataType20 a : AISDataType20.values()) {
					if (a.to() < binString.length()) {
						String binStr = binString.substring(a.from(), a.to());
						int intValue = Integer.parseInt(binStr, 2);
						setAISData(a, aisRecord, intValue);
						if (verbose) {
							System.out.println(String.format("Data %s, %s, %d chars becomes %d",
									a,
									binStr,
									binStr.length(),
									intValue));
						}
					} else if (verbose) {
						System.out.println(">> Out of binString");
					}
				}
				break;
			default:
				throw new AISException(String.format("Message type %d. Not managed yet.", messageType));
				// break;
		}
		return aisRecord;

	}

	public static class AISException extends Exception {
		public AISException() {
			super();
		}
		public AISException(String mess) {
			super(mess);
		}
	}
	/**
	 * 2's complement, for negative numbers.
	 *
	 * @param binStr binary String
	 * @return the 2's complement value
	 */
	private static String twosComplement(StringBuffer binStr) {
		int len = binStr.length();

		// Traverse the string to get first '1' from the last of string
		int i;
		for (i = len-1 ; i >= 0 ; i--) {
			if (binStr.charAt(i) == '1') {
				break;
			}
		}

		// If there exists no '1' concat 1 at the
		// starting of string
		if (i == -1) {
			return "1" + binStr;
		}

		// Continue traversal after the position of first '1'
		for (int k = i-1 ; k >= 0; k--) {
			// Just flip the values
			if (binStr.charAt(k) == '1') {
				binStr.replace(k, k + 1, "0");
			} else {
				binStr.replace(k, k + 1, "1");
			}
		}
		return binStr.toString();
	}

	private static void setAISData(AISDataType123 a, AISRecord ar, int value) {
		if (a.equals(AISDataType123.MESSAGE_TYPE)) {
			ar.setMessageType(value);
		} else if (a.equals(AISDataType123.REPEAT_INDICATOR)) {
			ar.setRepeatIndicator(value);
		} else if (a.equals(AISDataType123.MMSI)) {
			ar.setMMSI(value);
		} else if (a.equals(AISDataType123.NAV_STATUS)) {
			ar.setNavStatus(value);
		} else if (a.equals(AISDataType123.ROT)) {
			ar.setRot(value);
		} else if (a.equals(AISDataType123.SOG)) {
			ar.setSog(value);
		} else if (a.equals(AISDataType123.POS_ACC)) {
			ar.setPosAcc(value);
		} else if (a.equals(AISDataType123.LONGITUDE)) {
			ar.setLongitude(value);
		} else if (a.equals(AISDataType123.LATITUDE)) {
			ar.setLatitude(value);
		} else if (a.equals(AISDataType123.COG)) {
			ar.setCog(value);
		} else if (a.equals(AISDataType123.HDG)) {
			ar.setHdg(value);
		} else if (a.equals(AISDataType123.TIME_STAMP)) {
//			System.out.println(String.format("AIS TIME_STAMP: %d", value));
			ar.setUtc(value);
		}
	}

	private static void setAISData(AISDataType4 a, AISRecord ar, int value) {
		if (a.equals(AISDataType4.MESSAGE_TYPE)) {
			ar.setMessageType(value);
		} else if (a.equals(AISDataType4.REPEAT_INDICATOR)) {
			ar.setRepeatIndicator(value);
		} else if (a.equals(AISDataType4.MMSI)) {
			ar.setMMSI(value);
		} else if (a.equals(AISDataType4.UTC_YEAR)) {
			ar.setUtcYear(value);
		} else if (a.equals(AISDataType4.UTC_MONTH)) {
			ar.setUtcMonth(value);
		} else if (a.equals(AISDataType4.UTC_DAY)) {
			ar.setUtcDay(value);
		} else if (a.equals(AISDataType4.UTC_HOUR)) {
			ar.setUtcHour(value);
		} else if (a.equals(AISDataType4.UTC_MINUTE)) {
			ar.setUtcMinute(value);
		} else if (a.equals(AISDataType4.UTC_SECOND)) {
			ar.setUtcSecond(value);
		} else if (a.equals(AISDataType4.POS_ACC)) {
			ar.setPosAcc(value);
		} else if (a.equals(AISDataType4.LONGITUDE)) {
			ar.setLongitude(value);
		} else if (a.equals(AISDataType4.LATITUDE)) {
			ar.setLatitude(value);
		}
	}

	private static void setAISData(AISDataType5 a, AISRecord ar, int value) {
		if (a.equals(AISDataType5.MESSAGE_TYPE)) {
			ar.setMessageType(value);
		} else if (a.equals(AISDataType5.REPEAT_INDICATOR)) {
			ar.setRepeatIndicator(value);
		} else if (a.equals(AISDataType5.MMSI)) {
			ar.setMMSI(value);
		} else if (a.equals(AISDataType5.AIS_VERSION)) {
			ar.setAisVersion(value);
		} else if (a.equals(AISDataType5.IMO_NUMBER)) {
			ar.setImoNumber(value);
		} else if (a.equals(AISDataType5.SHIP_TYPE)) {
			ar.setShipType(value);
		} else if (a.equals(AISDataType5.DIM_TO_BOW)) {
			ar.setDimToBow(value);
		} else if (a.equals(AISDataType5.DIM_TO_STERN)) {
			ar.setDimToStern(value);
		} else if (a.equals(AISDataType5.DIM_TO_PORT)) {
			ar.setDimToPort(value);
		} else if (a.equals(AISDataType5.DIM_TO_STBD)) {
			ar.setDimToStbd(value);
		} else if (a.equals(AISDataType5.ETA_MONTH)) {
			ar.setEtaMonth(value);
		} else if (a.equals(AISDataType5.ETA_DAY)) {
			ar.setEtaDay(value);
		} else if (a.equals(AISDataType5.ETA_HOUR)) {
			ar.setEtaHour(value);
		} else if (a.equals(AISDataType5.ETA_MINUTE)) {
			ar.setEtaMinute(value);
		} else if (a.equals(AISDataType5.DRAUGHT)) {
			ar.setDraught(value);
		}
	}
	private static void setAISData(AISDataType5 a, AISRecord ar, String value) {
		if (a.equals(AISDataType5.CALL_SIGN)) {
			ar.setCallSign(value);
		} else if (a.equals(AISDataType5.VESSEL_NAME)) {
			ar.setVesselName(value);
		} else if (a.equals(AISDataType5.DESTINATION)) {
			ar.setDestination(value);
		}
	}

	private static void setAISData(AISDataType8 a, AISRecord ar, int value) {
		if (a.equals(AISDataType8.MESSAGE_TYPE)) {
			ar.setMessageType(value);
		} else if (a.equals(AISDataType8.REPEAT_INDICATOR)) {
			ar.setRepeatIndicator(value);
		} else if (a.equals(AISDataType8.MMSI)) {
			ar.setMMSI(value);
		} else if (a.equals(AISDataType8.DES_AREA_CODE)) {
			ar.setDesignatedAreaCode(value);
		} else if (a.equals(AISDataType8.FUNC_ID)) {
			ar.setFuncId(value);
		}
	}
	private static void setAISData(AISDataType8 a, AISRecord ar, String value) {
		if (a.equals(AISDataType8.DATA)) {
			ar.setBinData(value);
		}
	}

	private static void setAISData(AISDataType15 a, AISRecord ar, int value) {
		if (a.equals(AISDataType15.MESSAGE_TYPE)) {
			ar.setMessageType(value);
		} else if (a.equals(AISDataType15.REPEAT_INDICATOR)) {
			ar.setRepeatIndicator(value);
		} else if (a.equals(AISDataType15.MMSI)) {
			ar.setMMSI(value);
		} else if (a.equals(AISDataType15.INTERROGATED_MMSI)) {
			ar.setInterrogatedMMSI(value);
		} else if (a.equals(AISDataType15.FIRST_MESSAGE_TYPE)) {
			ar.setFirstMessageType(value);
		} else if (a.equals(AISDataType15.FIRST_SLOT_OFFSET)) {
			ar.setFirstSlotOffset(value);
		} else if (a.equals(AISDataType15.SECOND_MESSAGE_TYPE)) {
			ar.setSecondMessageType(value);
		} else if (a.equals(AISDataType15.INTERROGATED_MMSI_2)) {
			ar.setInterrogatedMMSI2(value);
		} else if (a.equals(AISDataType15.FIRST_MESSAGE_TYPE_2)) {
			ar.setFirstMessageType2(value);
		} else if (a.equals(AISDataType15.FIRST_SLOT_OFFSET_2)) {
			ar.setFirstSlotOffset2(value);
		}
	}

	private static void setAISData(AISDataType20 a, AISRecord ar, int value) {
		if (a.equals(AISDataType20.MESSAGE_TYPE)) {
			ar.setMessageType(value);
		} else if (a.equals(AISDataType20.REPEAT_INDICATOR)) {
			ar.setRepeatIndicator(value);
		} else if (a.equals(AISDataType20.MMSI)) {
			ar.setMMSI(value);
		} else if (a.equals(AISDataType20.OFFSET_1)) {
			ar.setOffset1(value);
		} else if (a.equals(AISDataType20.TIMEOUT_1)) {
			ar.setTimeout1(value);
		} else if (a.equals(AISDataType20.INCREMENT_1)) {
			ar.setIncrement1(value);
		} else if (a.equals(AISDataType20.OFFSET_2)) {
			ar.setOffset2(value);
		} else if (a.equals(AISDataType20.TIMEOUT_2)) {
			ar.setTimeout2(value);
		} else if (a.equals(AISDataType20.INCREMENT_2)) {
			ar.setIncrement2(value);
		} else if (a.equals(AISDataType20.OFFSET_3)) {
			ar.setOffset3(value);
		} else if (a.equals(AISDataType20.TIMEOUT_3)) {
			ar.setTimeout3(value);
		} else if (a.equals(AISDataType20.INCREMENT_3)) {
			ar.setIncrement3(value);
		} else if (a.equals(AISDataType20.OFFSET_4)) {
			ar.setOffset4(value);
		} else if (a.equals(AISDataType20.TIMEOUT_4)) {
			ar.setTimeout4(value);
		} else if (a.equals(AISDataType20.INCREMENT_4)) {
			ar.setIncrement4(value);
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

	private static String decode6BitCharacters(String binString) {
		int len = binString.length() / 6;
		StringBuilder text = new StringBuilder();
		for (int i = 0; i < len; i++) {
			String oneBinChar = binString.substring(i * 6, (i + 1) * 6);
			int cc = Integer.parseInt(oneBinChar, 2);
			cc += (cc < 32) ? 64 : 0;
			text.append((char)cc);
		}
		return text.toString().replace('`', ' ');
	}

	private enum MonthSizeOption {
		LONG, SHORT
	};
	private final static String[] SHORT_MONTHS = {
			"Jan", "Feb", "Mar", "Apr", "May", "Jun",
			"Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
	};
	private final static String[] LONG_MONTHS = {
			"January", "February", "March", "April", "May", "June",
			"July", "August", "September", "October", "November", "December"
	};
	private static String decodeMonth(int m) {
		return decodeMonth(m, MonthSizeOption.SHORT);
	}
	private static String decodeMonth(int m, MonthSizeOption option) {
		return ((m>=1 && m<=12) ? (option.equals(MonthSizeOption.SHORT) ? SHORT_MONTHS[m - 1] : LONG_MONTHS[m - 1]) : "--");
	}

	public static class AISRecord {
		private int messageType;      // Mandatory
		private int repeatIndicator;  // Mandatory
		private int MMSI;             // Mandatory
		private long recordTimeStamp; // Mandatory

		Map<String, Object> recordContent = new HashMap<>();

		private static final String NAV_STATUS = "NavStatus";
		private static final String ROT = "rot";
		private static final String SOG = "sog";
		private static final String COG = "cog";
		private static final String HDG = "hdg";
		private static final String POS_ACC = "PosAcc";
		private static final String LONGITUDE = "longitude";
		private static final String LATITUDE = "latitude";
		private static final String UTC = "utc";
		private static final String UTC_YEAR = "UtcYear";
		private static final String UTC_MONTH = "UtcMonth";
		private static final String UTC_DAY = "UtDay";
		private static final String UTC_HOUR = "UtcHour";
		private static final String UTC_MINUTE = "UtcMinute";
		private static final String UTC_SECOND = "UtcSecond";
		private static final String AIS_VERSION = "AisVersion";
		private static final String IMO_NUMBER = "ImoNumber";
		private static final String CALL_SIGN = "CallSign";
		private static final String VESSEL_NAME = "VesselName";
		private static final String SHIP_TYPE = "ShipType";
		private static final String DIM_TO_BOW = "Dim2Bow";
		private static final String DIM_TO_STERN = "Dim2Stern";
		private static final String DIM_TO_PORT = "Dim2Port";
		private static final String DIM_TO_STBD = "Dim2Stbd";
		private static final String ETA_MONTH = "EtaMonth";
		private static final String ETA_DAY = "EtaDay";
		private static final String ETA_HOUR = "EtaHour";
		private static final String ETA_MINUTE = "EtaMinute";
		private static final String DRAUGHT = "Draught";
		private static final String DESTINATION = "Destination";

		private static final String INTERROGATED_MMSI = "InterrogatedMMSI";
		private static final String FIRST_MESSAGE_TYPE = "FirstMessageType";
		private static final String FIRST_SLOT_OFFSET = "FirstSlotOffset";
		private static final String SECOND_MESSAGE_TYPE = "SecondMessageType";
		private static final String SECOND_SLOT_OFFSET = "SecondSlotOffset";
		private static final String INTERROGATED_MMSI_2 = "InterrogatedMMSI2";
		private static final String FIRST_MESSAGE_TYPE_2 = "FirstMessageType2";
		private static final String FIRST_SLOT_OFFSET_2 = "FirstSlotOffset2";

		private static final String OFFSET_1 = "offset1";
		private static final String TIMEOUT_1 = "timeout1";
		private static final String INCREMENT_1 = "increment1";
		private static final String OFFSET_2 = "offset2";
		private static final String TIMEOUT_2 = "timeout2";
		private static final String INCREMENT_2 = "increment2";
		private static final String OFFSET_3 = "offset3";
		private static final String TIMEOUT_3 = "timeout3";
		private static final String INCREMENT_3 = "increment3";
		private static final String OFFSET_4 = "offset4";
		private static final String TIMEOUT_4 = "timeout4";
		private static final String INCREMENT_4 = "increment4";

		private static final String DESIGNATED_AREA_CODE = "DesAreaCode";
		private static final String FUNC_ID = "FuncID";
		private static final String BIN_DATA = "BinData";


		AISRecord(long now) {
			super();
			recordTimeStamp = now;
		}

		public int getAisVersion() {
			try {
				return (int)recordContent.get(AIS_VERSION);
			} catch (Exception ex) {
				return 0;
			}
		}

		public void setAisVersion(int ais_version) {
			recordContent.put(AIS_VERSION, ais_version);
		}

		public int getDesignatedAreaCode() {
			try {
				return (int)recordContent.get(DESIGNATED_AREA_CODE);
			} catch (Exception ex) {
				return 0;
			}
		}

		public void setDesignatedAreaCode(int area_code) {
			recordContent.put(DESIGNATED_AREA_CODE, area_code);
		}

		public int getFuncId() {
			try {
				return (int)recordContent.get(FUNC_ID);
			} catch (Exception ex) {
				return 0;
			}
		}

		public void setFuncId(int func_id) {
			recordContent.put(FUNC_ID, func_id);
		}

		public int getImoNumber() {
			try {
				return (int)recordContent.get(IMO_NUMBER);
			} catch (Exception ex) {
				return 0;
			}
		}

		public void setImoNumber(int imo_number) {
			recordContent.put(AIS_VERSION, imo_number);
		}

		public String getBinData() {
			try {
				return (String)recordContent.get(BIN_DATA);
			} catch (Exception ex) {
				return "";
			}
		}

		public void setBinData(String binData) {
			recordContent.put(BIN_DATA, binData);
		}

		public String getCallSign() {
			try {
				return (String)recordContent.get(CALL_SIGN);
			} catch (Exception ex) {
				return "";
			}
		}

		public void setCallSign(String callSign) {
			recordContent.put(CALL_SIGN, callSign);
		}

		public String getVesselName() {
			try {
				return (String)recordContent.get(VESSEL_NAME);
			} catch (Exception ex) {
				return "";
			}
		}

		public void setVesselName(String vesselName) {
			recordContent.put(VESSEL_NAME, vesselName);
		}

		public int getShipType() {
			try {
				return (int)recordContent.get(SHIP_TYPE);
			} catch (Exception ex) {
				return 0;
			}
		}

		public void setShipType(int ship_type) {
			recordContent.put(SHIP_TYPE, ship_type);
		}

		public int getDimToBow() {
			try {
				return (int)recordContent.get(DIM_TO_BOW);
			} catch (Exception ex) {
				return 0;
			}
		}

		public void setDimToBow(int dim_to_bow) {
			recordContent.put(DIM_TO_BOW, dim_to_bow);
		}

		public int getDimToStern() {
			try {
				return (int)recordContent.get(DIM_TO_STERN);
			} catch (Exception ex) {
				return 0;
			}
		}

		public void setDimToStern(int dim_to_stern) {
			recordContent.put(DIM_TO_STERN, dim_to_stern);
		}

		public int getDimToPort() {
			try {
				return (int)recordContent.get(DIM_TO_PORT);
			} catch (Exception ex) {
				return 0;
			}
		}

		public void setDimToPort(int dim_to_port) {
			recordContent.put(DIM_TO_PORT, dim_to_port);
		}

		public int getDimToStbd() {
			try {
				return (int)recordContent.get(DIM_TO_STBD);
			} catch (Exception ex) {
				return 0;
			}
		}

		public void setDimToStbd(int dim_to_stbd) {
			recordContent.put(DIM_TO_STBD, dim_to_stbd);
		}

		public int getEtaMonth() {
			try {
				return (int)recordContent.get(ETA_MONTH);
			} catch (Exception ex) {
				return 0;
			}
		}

		public void setEtaMonth(int eta_month) {
			recordContent.put(ETA_MONTH, eta_month);
		}

		public int getEtaDay() {
			try {
				return (int)recordContent.get(ETA_DAY);
			} catch (Exception ex) {
				return 0;
			}
		}

		public void setEtaDay(int eta_day) {
			recordContent.put(ETA_DAY, eta_day);
		}

		public int getEtaHour() {
			try {
				return (int)recordContent.get(ETA_HOUR);
			} catch (Exception ex) {
				return 0;
			}
		}

		public void setEtaHour(int eta_hour) {
			recordContent.put(ETA_HOUR, eta_hour);
		}

		public int getEtaMinute() {
			try {
				return (int)recordContent.get(ETA_MINUTE);
			} catch (Exception ex) {
				return 0;
			}
		}

		public void setEtaMinute(int eta_minute) {
			recordContent.put(ETA_MINUTE, eta_minute);
		}

		public int getDraught() {
			try {
				return (int)recordContent.get(DRAUGHT);
			} catch (Exception ex) {
				return 0;
			}
		}

		public void setDraught(int draught) {
			recordContent.put(DRAUGHT, draught);
		}

		public String getDestination() {
			try {
				return (String)recordContent.get(DESTINATION);
			} catch (Exception ex) {
				return "";
			}
		}

		public void setDestination(String destination) {
			recordContent.put(DESTINATION, destination);
		}

		public int getOffset1() {
			try {
				return (int)recordContent.get(OFFSET_1);
			} catch (Exception ex) {
				return 0;
			}
		}

		public void setOffset1(int offset1) {
			recordContent.put(OFFSET_1, offset1);
		}

		public int getTimeout1() {
			try {
				return (int)recordContent.get(TIMEOUT_1);
			} catch (Exception ex) {
				return 0;
			}
		}

		public void setTimeout1(int timeout1) {
			recordContent.put(TIMEOUT_1, timeout1);
		}

		public int getIncrement1() {
			try {
				return (int)recordContent.get(INCREMENT_1);
			} catch (Exception ex) {
				return 0;
			}
		}

		public void setIncrement1(int increment1) {
			recordContent.put(INCREMENT_1, increment1);
		}

		public int getOffset2() {
			return (int)recordContent.get(OFFSET_2);
		}

		public void setOffset2(int offset2) {
			recordContent.put(OFFSET_2, offset2);
		}

		public int getTimeout2() {
			try {
				return (int)recordContent.get(TIMEOUT_2);
			} catch (Exception ex) {
				return 0;
			}
		}

		public void setTimeout2(int timeout2) {
			recordContent.put(TIMEOUT_2, timeout2);
		}

		public int getIncrement2() {
			try {
				return (int)recordContent.get(INCREMENT_2);
			} catch (Exception ex) {
				return 0;
			}
		}

		public void setIncrement2(int increment2) {
			recordContent.put(INCREMENT_2, increment2);
		}

		public int getOffset3() {
			try {
				return (int)recordContent.get(OFFSET_3);
			} catch (Exception ex) {
				return 0;
			}
		}

		public void setOffset3(int offset3) {
			recordContent.put(OFFSET_3, offset3);
		}

		public int getTimeout3() {
			try {
				return (int)recordContent.get(TIMEOUT_3);
			} catch (Exception ex) {
				return 0;
			}
		}

		public void setTimeout3(int timeout3) {
			recordContent.put(TIMEOUT_3, timeout3);
		}

		public int getIncrement3() {
			try {
				return (int)recordContent.get(INCREMENT_3);
			} catch (Exception ex) {
				return 0;
			}
		}

		public void setIncrement3(int increment3) {
			recordContent.put(INCREMENT_3, increment3);
		}

		public int getOffset4() {
			try {
				return (int)recordContent.get(OFFSET_4);
			} catch (Exception ex) {
				return 0;
			}
		}

		public void setOffset4(int offset4) {
			recordContent.put(OFFSET_4, offset4);
		}

		public int getTimeout4() {
			try {
				return (int)recordContent.get(TIMEOUT_4);
			} catch (Exception ex) {
				return 0;
			}
		}

		public void setTimeout4(int timeout4) {
			recordContent.put(TIMEOUT_4, timeout4);
		}

		public int getIncrement4() {
			try {
				return (int)recordContent.get(INCREMENT_4);
			} catch (Exception ex) {
				return 0;
			}
		}

		public void setIncrement4(int increment4) {
			recordContent.put(INCREMENT_4, increment4);
		}

		public int getInterrogatedMMSI() {
			try {
				return (int)recordContent.get(INTERROGATED_MMSI);
			} catch (Exception ex) {
				return 0;
			}
		}

		public void setInterrogatedMMSI(int interrogatedMMSI) {
			recordContent.put(INTERROGATED_MMSI, interrogatedMMSI);
		}

		public int getFirstMessageType() {
			try {
				return (int)recordContent.get(FIRST_MESSAGE_TYPE);
			} catch (Exception ex) {
				return 0;
			}
		}

		public void setFirstMessageType(int firstMessageType) {
			recordContent.put(FIRST_MESSAGE_TYPE, firstMessageType);
		}

		public int getFirstSlotOffset() {
			try {
				return (int)recordContent.get(FIRST_SLOT_OFFSET);
			} catch (Exception ex) {
				return 0;
			}
		}

		public void setFirstSlotOffset(int firstSlotOffset) {
			recordContent.put(FIRST_SLOT_OFFSET, firstSlotOffset);
		}

		public int getSecondMessageType() {
			try {
				return (int)recordContent.get(SECOND_MESSAGE_TYPE);
			} catch (Exception ex) {
				return 0;
			}
		}

		public void setSecondMessageType(int secondMessageType) {
			recordContent.put(SECOND_MESSAGE_TYPE, secondMessageType);
		}

		public int getSecondSlotOffset() {
			try {
				return (int)recordContent.get(SECOND_SLOT_OFFSET);
			} catch (Exception ex) {
				return 0;
			}
		}

		public void setSecondSlotOffset(int secondSlotOffset) {
			recordContent.put(SECOND_SLOT_OFFSET, secondSlotOffset);
		}

		public int getInterrogatedMMSI2() {
			try {
				return (int)recordContent.get(INTERROGATED_MMSI_2);
			} catch (Exception ex) {
				return 0;
			}
		}

		public void setInterrogatedMMSI2(int interrogatedMMSI2) {
			recordContent.put(INTERROGATED_MMSI_2, interrogatedMMSI2);
		}

		public int getFirstMessageType2() {
			try {
				return (int)recordContent.get(FIRST_MESSAGE_TYPE_2);
			} catch (Exception ex) {
				return 0;
			}
		}

		public void setFirstMessageType2(int firstMessageType2) {
			recordContent.put(FIRST_MESSAGE_TYPE_2, firstMessageType2);
		}

		public int getFirstSlotOffset2() {
			try {
				return (int)recordContent.get(FIRST_SLOT_OFFSET_2);
			} catch (Exception ex) {
				return 0;
			}
		}

		public void setFirstSlotOffset2(int firstSlotOffset2) {
			recordContent.put(FIRST_SLOT_OFFSET_2, firstSlotOffset2);
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
			recordContent.put(NAV_STATUS, navStatus);
		}

		public int getNavStatus() {
			try {
				return (int) recordContent.get(NAV_STATUS);
			} catch (Exception ex) {
				return 0;
			}
		}

		public void setRot(int rot) {
			recordContent.put(ROT, rot);
		}

		public int getRot() {
			try {
				return (int)recordContent.get(ROT);
			} catch (Exception ex) {
				return 0;
			}
		}

		public void setSog(int sog) {
			recordContent.put(SOG, (sog / 10f));
		}

		public float getSog() {
			try {
				return (float)recordContent.get(SOG);
			} catch (Exception ex) {
				return 0;
			}
		}

		public void setPosAcc(int posAcc) {
			recordContent.put(POS_ACC, posAcc);
		}

		public int getPosAcc() {
			try {
				return (int)recordContent.get(POS_ACC);
			} catch (Exception ex) {
				return 0;
			}
		}

		public void setLongitude(int longitude) {
			recordContent.put(LONGITUDE, (longitude / 600_000f));
		}

		public float getLongitude() {
			try {
				return (float)recordContent.get(LONGITUDE);
			} catch (Exception ex) {
				return 0;
			}
		}

		public void setLatitude(int latitude) {
			recordContent.put(LATITUDE, (latitude / 600_000f));
		}

		public float getLatitude() {
			try {
				return (float) recordContent.get(LATITUDE);
			} catch (Exception ex) {
				return 0;
			}
		}

		public void setCog(int cog) {
			recordContent.put(COG, (cog / 10f));
		}

		public float getCog() {
			try {
				return (float)recordContent.get(COG);
			} catch (Exception ex) {
				return 0;
			}
		}

		public void setHdg(int hdg) {
			recordContent.put(HDG, hdg);
		}

		public int getHdg() {
			try {
				return (int)recordContent.get(HDG);
			} catch (Exception ex) {
				return 0;
			}
		}

		public void setUtc(int utc) {
			recordContent.put(UTC, utc);
		}

		public int getUtc() {
			try {
				return (int)recordContent.get(UTC);
			} catch (Exception ex) {
				return 0;
			}
		}

		public void setUtcYear(int d) {
			recordContent.put(UTC_YEAR, d);
		}
		public int getUtcYear() {
			try {
				return (int)recordContent.get(UTC_YEAR);
			} catch (Exception ex) {
				return 0;
			}
		}

		public void setUtcMonth(int d) {
			recordContent.put(UTC_MONTH, d);
		}
		public int getUtcMonth() {
			try {
				return (int)recordContent.get(UTC_MONTH);
			} catch (Exception ex) {
				return 0;
			}
		}

		public void setUtcDay(int d) {
			recordContent.put(UTC_DAY, d);
		}
		public int getUtcDay() {
			try {
				return (int)recordContent.get(UTC_DAY);
			} catch (Exception ex) {
				return 0;
			}
		}

		public void setUtcHour(int d) {
			recordContent.put(UTC_HOUR, d);
		}
		public int getUtcHour() {
			try {
				return (int)recordContent.get(UTC_HOUR);
			} catch (Exception ex) {
				return 0;
			}
		}

		public void setUtcMinute(int d) {
			recordContent.put(UTC_MINUTE, d);
		}
		public int getUtcMinute() {
			try {
				return (int)recordContent.get(UTC_MINUTE);
			} catch (Exception ex) {
				return 0;
			}
		}

		public void setUtcSecond(int d) {
			recordContent.put(UTC_SECOND, d);
		}
		public int getUtcSecond() {
			try {
				return (int)recordContent.get(UTC_SECOND);
			} catch (Exception ex) {
				return 0;
			}
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

		static String decodeType(int type) {
			String strType = "";
			switch (type) {
				case 20:
					strType = "WIG";
					break;
				case 21:
					strType = "WIG - Cat A";
					break;
				case 22:
					strType = "WIG - Cat B";
					break;
				case 23:
					strType = "WIG - Cat C";
					break;
				case 24:
					strType = "WIG - Cat D";
					break;
				case 30:
					strType = "Fishing";
					break;
				case 31:
					strType = "Towing";
					break;
				case 32:
					strType = "Towing (Big)";
					break;
				case 33:
					strType = "Dredging";
					break;
				case 34:
					strType = "Diving Ops";
					break;
				case 35:
					strType = "Military Ops";
					break;
				case 36:
					strType = "Sailing";
					break;
				case 37:
					strType = "Pleasure Craft";
					break;
				case 40:
					strType = "High Speed Craft";
					break;
				case 41:
					strType = "High Speed Craft - Cat A";
					break;
				case 42:
					strType = "High Speed Craft - Cat B";
					break;
				case 43:
					strType = "High Speed Craft - Cat C";
					break;
				case 44:
					strType = "High Speed Craft - Cat D";
					break;
				case 50:
					strType = "Pilot";
					break;
				case 51:
					strType = "Search and Rescue vessel";
					break;
				case 52:
					strType = "Tug";
					break;
				case 53:
					strType = "Port Tender";
					break;
				case 54:
					strType = "Anti-pollution Equipment";
					break;
				case 55:
					strType = "Law Enforcement";
					break;
				case 58:
					strType = "Medical Transport";
					break;
				case 59:
					strType = "Non Combatant";
					break;
				case 60:
					strType = "Passenger Ship";
					break;
				case 61:
					strType = "Passenger Ship - Cat A";
					break;
				case 62:
					strType = "Passenger Ship - Cat B";
					break;
				case 63:
					strType = "Passenger Ship - Cat C";
					break;
				case 64:
					strType = "Passenger Ship - Cat D";
					break;
				case 70:
				case 79:
					strType = "Cargo Ship";
					break;
				case 71:
					strType = "Cargo Ship - Cat A";
					break;
				case 72:
					strType = "Cargo Ship - Cat B";
					break;
				case 73:
					strType = "Cargo Ship - Cat C";
					break;
				case 74:
					strType = "Cargo Ship - Cat D";
					break;
				case 80:
				case 89:
					strType = "Tanker";
					break;
				case 81:
					strType = "Tanker - Cat A";
					break;
				case 82:
					strType = "Tanker - Cat B";
					break;
				case 83:
					strType = "Tanker - Cat C";
					break;
				case 84:
					strType = "Tanker - Cat D";
					break;
				case 90:
				case 99:
					strType = "Other";
					break;
				case 91:
					strType = "Other - Cat A";
					break;
				case 92:
					strType = "Other - Cat B";
					break;
				case 93:
					strType = "Other - Cat C";
					break;
				case 94:
					strType = "Other - Cat D";
					break;
				case 0:
				default:
					strType = "Not available";
					break;
			}

			return strType;
		}

		@Override
		public String toString() {
			String str = "";
			switch (messageType) {
				case 1:
				case 2:
				case 3:
					str = String.format("Type:%d, Repeat:%d, MMSI:%d, status:%s, rot:%d, Pos:%f/%f (Acc:%d), COG:%f, SOG:%f, HDG:%d, TimeStamp: %d.",
							messageType,
							repeatIndicator,
							MMSI,
							decodeStatus(getNavStatus()),
							getRot(),
							getLatitude(),
							getLongitude(),
							getPosAcc(),
							getCog(),
							getSog(),
							getHdg(),
							getUtc());
					break;
				case 4:
					str = String.format("Type:%d, Repeat:%d, MMSI:%d, Pos:%f/%f, UTC %s-%s-%s %s:%s:%s.",
							messageType,
							repeatIndicator,
							MMSI,
							getLatitude(),
							getLongitude(),
							StringUtils.lpad(String.valueOf(getUtcYear()), 4, "0"),
							decodeMonth(getUtcMonth()),
							StringUtils.lpad(String.valueOf(getUtcDay()), 2, "0"),
							StringUtils.lpad(String.valueOf(getUtcHour()), 2, "0"),
							StringUtils.lpad(String.valueOf(getUtcMinute()), 2, "0"),
							StringUtils.lpad(String.valueOf(getUtcSecond()), 2, "0"));
					break;
				case 5:
					str = String.format("Type:%d, Repeat:%d, MMSI:%d, CallSign: %s, Name:%s, type: %s, Length: %d, Width: %d, Draught: %.02f, ETA: %s-%s @ %s:%s, Destination: %s.",
							messageType,
							repeatIndicator,
							MMSI,
							getCallSign().trim(),
							getVesselName().trim(),
							decodeType(getShipType()),
							getDimToBow() + getDimToStern(),
							getDimToPort() + getDimToStbd(),
							(getDraught() / 10f),
							decodeMonth(getEtaMonth()),
							StringUtils.lpad(String.valueOf(getEtaDay()), 2, "0"),
							StringUtils.lpad(String.valueOf(getEtaHour()), 2, "0"),
							StringUtils.lpad(String.valueOf(getEtaMinute()), 2, "0"),
							getDestination().trim());
					break;
				case 8:
					str = String.format("Type:%d, Repeat:%d, MMSI:%d, Des.Area Code %d, FuncID %d, Data: %s",
							messageType,
							repeatIndicator,
							MMSI,
							getDesignatedAreaCode(),
							getFuncId(),
							getBinData());
					break;
				case 15:
					str = String.format("Type:%d, Repeat:%d, MMSI:%d, Int MMSI %d, 1st MessType %d, 1st SlotOffset %d, 2nd MessType %d, 2nd SlotOffset %d, Int MMSI(2) %d, 1st MessType(2) %d, 1st SlotOffset(2) %d.",
							messageType,
							repeatIndicator,
							MMSI,
							getInterrogatedMMSI(),
							getFirstMessageType(),
							getFirstSlotOffset(),
							getSecondMessageType(),
							getSecondSlotOffset(),
							getInterrogatedMMSI2(),
							getFirstMessageType2(),
							getFirstSlotOffset2());
					break;
				case 20:
					str = String.format("Type:%d, Repeat:%d, MMSI:%d, Offset1: %d, Timeout1: %d, Incr1: %d, Offset2: %d, Timeout2: %d, Incr2: %d, Offset3: %d, Timeout3: %d, Incr3: %d, Offset4: %d, Timeout4: %d, Incr4: %d.",
							messageType,
							repeatIndicator,
							MMSI,
							getOffset1(),
							getTimeout1(),
							getIncrement1(),
							getOffset2(),
							getTimeout2(),
							getIncrement2(),
							getOffset3(),
							getTimeout3(),
							getIncrement3(),
							getOffset4(),
							getTimeout4(),
							getIncrement4());
					break;
				default:
					break;
			}
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

		// 2's complement
		String tc = twosComplement(new StringBuffer("1000100"));
		System.out.println(">> " + tc);
		assert "11111011".equals(tc);
		tc = twosComplement(new StringBuffer("00000101"));
		System.out.println(">> " + tc);
		assert "0111100".equals(tc);

		String ais; // Error in !AIVDM,1,1,,B,?03Ovk1E6T50D00,2*1E
		if (args.length > 0) {
			try {
				System.out.println(parseAIS(args[0]));
			} catch (AISException t) {
				System.err.println(t.toString());
			}
		} else {
			ais = "!AIVDM,1,1,,A,14eG;o@034o8sd<L9i:a;WF>062D,0*7D";
			try {
				System.out.println(parseAIS(ais));
			} catch (Throwable t) {
				System.err.println(t.toString());
			}

			ais = "!AIVDM,1,1,,A,15NB>cP03jG?l`<EaV0`MFO000S>,0*39";
			try {
				System.out.println(parseAIS(ais));
			} catch (AISException t) {
				System.err.println(t.toString());
			}

			ais = "!AIVDM,1,1,,B,177KQJ5000G?tO`K>RA1wUbN0TKH,0*5C";
			try {
				System.out.println(parseAIS(ais));
			} catch (AISException t) {
				System.err.println(t.toString());
			}

			ais = "!AIVDM,1,1,,A,D03Ovk06AN>40Hffp00Nfp0,2*52";
			try {
				System.out.println(parseAIS(ais));
			} catch (AISException t) {
				System.err.println(t.toString());
			}

			ais = "!AIVDM,2,2,2,B,RADP,0*10";
			try {
				System.out.println(parseAIS(ais));
			} catch (Exception ex) {
				System.err.println(ex.toString());
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
				"!AIVDM,1,1,,A,403Ovk1v@CPO`o>jNrEdjEO02<45,0*01",
				// From PI4J
				"!AIVDM,1,1,,A,D03Ovk0m9N>4g@ffpfpNfp0,2*38",
				"!AIVDM,1,1,,B,D03Ovk0s=N>4g<ffpfpNfp0,2*5D",
				"!AIVDM,1,1,,A,?03Ovk1Gcv1`D00,2*3C",
				"!AIVDM,1,1,,B,?03Ovk1CpiT0D00,2*02",
				"!AIVDM,1,1,,A,D03Ovk06AN>40Hffp00Nfp0,2*52",
				"!AIVDM,1,1,,B,D03Ovk0<EN>40Dffp00Nfp0,2*53",
				// From Serial IO
				"!AIVDM,1,1,,B,?03Ovk20AG54D00,2*08",
				"!AIVDM,1,1,,A,?03Ovk20AG54D00,2*0B",
				"!AIVDM,1,1,,A,D03Ovk06AN>40Hffp00Nfp0,2*52",
				"!AIVDM,1,1,,B,D03Ovk0<EN>40Dffp00Nfp0,2*53",
				"!AIVDM,1,1,,A,15MU>f002Bo?5cHE`@2qOG`>0@43,0*5B",

				"!AIVDM,1,1,,A,D03Ovk1T1N>5N8ffqMhNfp0,2*6A",
				"!AIVDM,1,1,,B,D03Ovk1b5N>5N4ffqMhNfp0,2*57",
				"!AIVDM,1,1,,B,403Ovk1v@EG3Do>jOBEdjE?02<4=,0*02",
				"!AIVDM,1,1,,B,13P<DT012Fo>er2EW:CRd28T0@<I,0*5C",
				"!AIVDM,1,1,,A,?03Ovk0sNMB0D00,2*3C",
				"!AIVDM,1,1,,B,13P<DT0wBGo>f<TEW;0BdR8t0D24,0*15",
				"!AIVDM,1,1,,B,13P<DT0w2Go>fO<EW;f2d29D08K6,0*0E",
				"!AIVDM,1,1,,A,?03Ovk1GTTnPD00,2*46",
				"!AIVDM,1,1,,B,13P<DT00jHo>fihEW<LRcR9d0HQl,0*56",
				"!AIVDM,1,1,,A,D03Ovk06AN>40Hffp00Nfp0,2*52",
				"!AIVDM,1,1,,A,403Ovk1v@EG40o>jNpEdjBg02806,0*15",
				"!AIVDM,1,1,,B,403Ovk1v@EG40o>jNpEdjBg02808,0*18",
				"!AIVDM,1,1,,A,13P<DT012Ho>fs6EW<kBcj800@2:,0*2F",
				"!AIVDM,1,1,,A,?03Ovk1:9Ob`D00,2*71",
				"!AIVDM,1,1,,A,?03Ovk0sNMB0D00,2*3C",
				"!AIVDM,1,1,,A,13P<DT00BHo>g?FEW=U2cj8H0D24,0*5E",
				"!AIVDM,1,1,,A,D03Ovk1T1N>5N8ffqMhNfp0,2*6A",
				"!AIVDM,1,1,,B,D03Ovk1b5N>5N4ffqMhNfp0,2*57",
				"!AIVDM,1,1,,A,403Ovk1v@EG4Do>jNbEdjDw028;l,0*34",
				"!AIVDM,1,1,,B,403Ovk1v@EG4Do>jNbEdjDw028;n,0*35",
				"!AIVDM,1,1,,B,13P<DT00BIo>gG<EW=p2d28R0<23,0*41",
				// Multiple
				"!AIVDM,2,1,6,B,55T6aT42AGrO<ELCJ20t<D4r0Pu0F22222222216CPIC94DfNBEDp3hB,0*0A",
				"!AIVDM,2,2,6,B,p88888888888880,2*69"
		);
		aisFromDaisy.forEach(aisStr -> {
			try {
				System.out.println(parseAIS(aisStr));
			} catch (AISException t) {
				System.err.println(t.toString());
			}
		});
		System.out.println("------------------");

		if (true) {
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
									System.out.println(String.format(">> null AIS Record for %s", line));
								}
							} catch (AISException ex) {
								System.err.println("For [" + line + "]: " + ex.toString());
							} catch (Exception ex) {
								System.err.println("For [" + line + "]: ");
								ex.printStackTrace();
							}
						} else if (!line.startsWith("#")) {
							// TODO else parse NMEA?
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
}
