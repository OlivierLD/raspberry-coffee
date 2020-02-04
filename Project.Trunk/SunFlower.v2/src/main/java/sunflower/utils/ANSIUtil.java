package sunflower.utils;

import org.fusesource.jansi.AnsiConsole;

import static utils.StringUtils.rpad;
import static utils.StringUtils.lpad;

public class ANSIUtil {
	private final static boolean BOX_CHARACTERS = "true".equals(System.getProperty("ansi.boxes")); // In case special characters do not work

	public final static char ESC = '\u001b'; // (char) 27;

	public final static String ANSI_BLACK = "0";
	public final static String ANSI_RED = "1";
	public final static String ANSI_GREEN = "2";
	public final static String ANSI_YELLOW = "3";
	public final static String ANSI_BLUE = "4";
	public final static String ANSI_MAGENTA = "5";
	public final static String ANSI_CYAN = "6";
	public final static String ANSI_WHITE = "7";

	public static final String ANSI_CLS = ESC + "[2J";
	public static final String ANSI_ERASE_TO_EOL = ESC + "[K";
	public static final String ANSI_HOME = ESC + "[H";  // 0,0 Top left
	public static final String ANSI_HEAD = ESC + "[1G"; // Start of current line, position 1

	public static final String ANSI_NORMAL = ESC + "[0m";
	public static final String ANSI_BOLD = ESC + "[1m";
	public static final String ANSI_FAINT = ESC + "[2m";
	public static final String ANSI_ITALIC = ESC + "[3m";
	public static final String ANSI_UNDERLINE = ESC + "[4m";
	public static final String ANSI_BLINK = ESC + "[5m";
	public static final String ANSI_BLINK_FAST = ESC + "[6m";
	public static final String ANSI_REVERSE = ESC + "[7m";
	public static final String ANSI_CONCEAL = ESC + "[8m";
	public static final String ANSI_CROSSED_OUT = ESC + "[9m";

	public static final String ANSI_DEFAULT_TEXT = ESC + "[39m";
	public static final String ANSI_DEFAULT_BACKGROUND = ESC + "[49m";

	// Unicode box drawing
	public final static String SOLID_HORIZONTAL = "\u2500";
	public final static String SOLID_HORIZONTAL_BOLD = (BOX_CHARACTERS ? "\u2501" : "-");
	public final static String SOLID_VERTICAL = "\u2502";
	public final static String SOLID_VERTICAL_BOLD = (BOX_CHARACTERS ? "\u2503" : "|");
	public final static String DOTTED_HORIZONTAL = "\u2504";
	public final static String DOTTED_HORIZONTAL_BOLD = "\u2505";
	public final static String DOTTED_VERTICAL = "\u2506";
	public final static String DOTTED_VERTICAL_BOLD = "\u2507";

	public final static String TOP_LEFT_CORNER = "\u250c";
	public final static String TOP_LEFT_CORNER_BOLD = (BOX_CHARACTERS ? "\u250f" : "+");
	public final static String TOP_RIGHT_CORNER = "\u2510";
	public final static String TOP_RIGHT_CORNER_BOLD = (BOX_CHARACTERS ? "\u2513" : "+");

	public final static String BOTTOM_LEFT_CORNER = "\u2514";
	public final static String BOTTOM_LEFT_CORNER_BOLD = (BOX_CHARACTERS ? "\u2517" : "+");
	public final static String BOTTOM_RIGHT_CORNER = "\u2518";
	public final static String BOTTOM_RIGHT_CORNER_BOLD = (BOX_CHARACTERS ? "\u251b" : "+");

	public final static String LEFT_T = "\u251c";
	public final static String LEFT_T_BOLD = (BOX_CHARACTERS ? "\u2523" : "+");
	public final static String RIGHT_T = "\u2524";
	public final static String RIGHT_T_BOLD = (BOX_CHARACTERS ? "\u252b" : "+");
	public final static String TOP_T = "\u252c";
	public final static String TOP_T_BOLD = (BOX_CHARACTERS ? "\u2533" : "+");
	public final static String BOTTOM_T = "\u2534";
	public final static String BOTTOM_T_BOLD = (BOX_CHARACTERS ? "\u253b" : "+");

	public final static String CROSS = "\u253c";
	public final static String CROSS_BOLD = (BOX_CHARACTERS ? "\u254b" : "+");

	public final static String DOUBLE_LEFT_T = "\u2560";
	public final static String DOUBLE_RIGHT_T = "\u2563";

	public final static String TOP_LEFT_ROUND_CORNER = "\u256d";
	public final static String TOP_RIGHT_ROUND_CORNER = "\u256e";
	public final static String BOTTOM_RIGHT_ROUND_CORNER = "\u256f";
	public final static String BOTTOM_LEFT_ROUND_CORNER = "\u2570";

// And there is way more...

	public static String ansiLocate(int x, int y) {
		return String.format("%c[%d;%dH", ESC, y, x); // Yes, actually Y, X
	}

	/**
	 * Returns a string of nb times the str parameter.
	 * @param str the string to use
	 * @param nb number of times
	 * @return the expected string.
	 */
	private static String drawXChar(String str, int nb) {
		StringBuffer sb = new StringBuffer();
		for (int i=0; i<nb; i++) {
			sb.append(str);
		}
		return sb.toString();
	}

	private final static String PAD = ANSI_ERASE_TO_EOL;

	public static final String ANSI_AT55 = ESC + "[10;10H"; // Actually 10, 10
	public static final String ANSI_WHITE_ON_BLUE = ESC + "[37;44m";

	public static String ansiSetBackGroundColor(String color) {
		// ESC[40-47
		return String.format("%c[4%sm", ESC, color);
	}

	public static String ansiSetTextColor(String color) {
		// ESC[30-37
		return String.format("%c[3%sm", ESC, color);
	}

	public static String ansiSetTextAndBackgroundColor(String text, String bg) {
		// ESC[30-37;40-47
		return String.format("%c[3%s;4%sm", ESC, text, bg);
	}

	public static String superpose(String orig, String override) {
		byte[] ret = orig.getBytes();
		for (int i = 0; i < Math.min(orig.length(), override.length()); i++) {
			ret[i] = (byte) override.charAt(i);
		}
		return new String(ret);
	}

	// Position frame coordinates
	final static int START_POS_FRAME_AT = 1; // Line on screen
	final static int POS_COL_1 = 6;  // Col-1: Sun or Device, up to 6 characters
	final static int POS_COL_2 = 28; // Col-2: Date: 28 characters
	final static int POS_COL_3 = 6;  // Col-3 Z: 6 characters
	final static int POS_COL_4 = 5;  // Col-4 Elev: 5 characters

	final static int[] ONE_POS_ROW = {
			POS_COL_1,
			POS_COL_2,
			POS_COL_3,
			POS_COL_4
	};

	final static int SUN_POS_LINE = START_POS_FRAME_AT + 4;
	final static int DEVICE_POS_LINE = START_POS_FRAME_AT + 6;

	// Movement frame coordinates
	final static int START_MOV_FRAME_AT = 9; // Line on screen
	final static int MOV_COL_1 = 5;
	final static int MOV_COL_2 = 28;
	final static int MOV_COL_3 = 6;
	final static int MOV_COL_4 = 6;
	final static int MOV_COL_5 = 6;

	final static int[] ONE_MOV_ROW = {
			MOV_COL_1,
			MOV_COL_2,
			MOV_COL_3,
			MOV_COL_4,
			MOV_COL_5
	};

	final static int ELEV_MOV_LINE = START_MOV_FRAME_AT + 4;
	final static int Z_MOV_LINE = START_MOV_FRAME_AT + 6;

	// Info
	final static int START_INFO_FRAME_AT = 17; // Line on screen
	final static int INFO_COL_1 = 28;
	final static int INFO_COL_2 = 64;

	final static int[] ONE_INFO_ROW = {
			INFO_COL_1,
			INFO_COL_2
	};

	final static int INFO_LINE = START_INFO_FRAME_AT + 4;

	private static void displayValue(int x, int y, String str) {
		AnsiConsole.out.println(
				ansiLocate(x, y) + ANSI_NORMAL + ANSI_DEFAULT_BACKGROUND + ANSI_DEFAULT_TEXT + str);
	}
	// col is zero-based, in the rowIndexes array.
	private static void printValueInCol(String str, int line, int col, int[] rowIndexes) {
		int offset = 1;
		for (int i=0; i<col; i++) {
			offset += (rowIndexes[i] + 1);
		}
		offset += 1;
		displayValue(offset, line, str);
	}

	// Draw an empty frame for position (sun and device)
	public static void printPositionTable() {
		int line = START_POS_FRAME_AT; // Start from that line
		AnsiConsole.out.println(
				ansiLocate(1, line++) + ANSI_NORMAL + ANSI_DEFAULT_BACKGROUND + ANSI_ITALIC + ANSI_BOLD + "- Positions -" + ANSI_DEFAULT_TEXT + PAD);
		// Frame top
		AnsiConsole.out.println(
				ansiLocate(1, line++) + ANSI_NORMAL + ANSI_DEFAULT_BACKGROUND + ANSI_DEFAULT_TEXT +
						TOP_LEFT_CORNER_BOLD +
						// TOP_LEFT_ROUND_CORNER +
						drawXChar(SOLID_HORIZONTAL_BOLD, POS_COL_1) +
						TOP_T_BOLD +
						drawXChar(SOLID_HORIZONTAL_BOLD, POS_COL_2) +
						TOP_T_BOLD +
						drawXChar(SOLID_HORIZONTAL_BOLD, POS_COL_3) +
						TOP_T_BOLD +
						drawXChar(SOLID_HORIZONTAL_BOLD, POS_COL_4) +
						TOP_RIGHT_CORNER_BOLD +
						//	TOP_RIGHT_ROUND_CORNER +
						PAD);
		// Headers
		AnsiConsole.out.println(
				ansiLocate(1, line++) + ANSI_NORMAL + ANSI_DEFAULT_BACKGROUND + ANSI_DEFAULT_TEXT +
						SOLID_VERTICAL_BOLD +
						//	TOP_LEFT_ROUND_CORNER +
						rpad("", POS_COL_1) +
						SOLID_VERTICAL_BOLD +
						rpad(" Date", POS_COL_2) +
						SOLID_VERTICAL_BOLD +
						rpad(" Z", POS_COL_3) +
						SOLID_VERTICAL_BOLD +
						rpad("Elev", POS_COL_4) +
						SOLID_VERTICAL_BOLD +
						//	TOP_RIGHT_ROUND_CORNER +
						PAD);
		// Under the headers, separator
		AnsiConsole.out.println(
				ansiLocate(1, line++) + ANSI_NORMAL + ANSI_DEFAULT_BACKGROUND + ANSI_DEFAULT_TEXT +
						LEFT_T_BOLD +
						drawXChar(SOLID_HORIZONTAL_BOLD, POS_COL_1) +
						CROSS_BOLD +
						drawXChar(SOLID_HORIZONTAL_BOLD, POS_COL_2) +
						CROSS_BOLD +
						drawXChar(SOLID_HORIZONTAL_BOLD, POS_COL_3) +
						CROSS_BOLD +
						drawXChar(SOLID_HORIZONTAL_BOLD, POS_COL_4) +
						RIGHT_T_BOLD +
						PAD);
		// First data line, Sun
		AnsiConsole.out.println(
				ansiLocate(1, line++) + ANSI_NORMAL + ANSI_DEFAULT_BACKGROUND + ANSI_DEFAULT_TEXT +
						SOLID_VERTICAL_BOLD +
						//	TOP_LEFT_ROUND_CORNER +
						rpad("Sun", POS_COL_1) +
						SOLID_VERTICAL_BOLD +
						rpad("", POS_COL_2) +
						SOLID_VERTICAL_BOLD +
						rpad("", POS_COL_3) +
						SOLID_VERTICAL_BOLD +
						rpad("", POS_COL_4) +
						SOLID_VERTICAL_BOLD +
						//	TOP_RIGHT_ROUND_CORNER +
						PAD);
		// Separator
		AnsiConsole.out.println(
				ansiLocate(1, line++) + ANSI_NORMAL + ANSI_DEFAULT_BACKGROUND + ANSI_DEFAULT_TEXT +
						LEFT_T_BOLD +
						drawXChar(SOLID_HORIZONTAL_BOLD, POS_COL_1) +
						CROSS_BOLD +
						drawXChar(SOLID_HORIZONTAL_BOLD, POS_COL_2) +
						CROSS_BOLD +
						drawXChar(SOLID_HORIZONTAL_BOLD, POS_COL_3) +
						CROSS_BOLD +
						drawXChar(SOLID_HORIZONTAL_BOLD, POS_COL_4) +
						RIGHT_T_BOLD +
						PAD);
		// Second data line, Device
		AnsiConsole.out.println(
				ansiLocate(1, line++) + ANSI_NORMAL + ANSI_DEFAULT_BACKGROUND + ANSI_DEFAULT_TEXT +
						SOLID_VERTICAL_BOLD +
						//	TOP_LEFT_ROUND_CORNER +
						rpad("Device", POS_COL_1) +
						SOLID_VERTICAL_BOLD +
						rpad("", POS_COL_2) +
						SOLID_VERTICAL_BOLD +
						rpad("", POS_COL_3) +
						SOLID_VERTICAL_BOLD +
						rpad("", POS_COL_4) +
						SOLID_VERTICAL_BOLD +
						//	TOP_RIGHT_ROUND_CORNER +
						PAD);

		// Frame bottom
		AnsiConsole.out.println(
				ansiLocate(1, line++) + ANSI_NORMAL + ANSI_DEFAULT_BACKGROUND + ANSI_DEFAULT_TEXT +
						BOTTOM_LEFT_CORNER_BOLD +
						//	TOP_LEFT_ROUND_CORNER +
						drawXChar(SOLID_HORIZONTAL_BOLD, POS_COL_1) +
						BOTTOM_T_BOLD +
						drawXChar(SOLID_HORIZONTAL_BOLD, POS_COL_2) +
						BOTTOM_T_BOLD +
						drawXChar(SOLID_HORIZONTAL_BOLD, POS_COL_3) +
						BOTTOM_T_BOLD +
						drawXChar(SOLID_HORIZONTAL_BOLD, POS_COL_4) +
						BOTTOM_RIGHT_CORNER_BOLD +
						//	TOP_RIGHT_ROUND_CORNER +
						PAD);
	}

	public static void printSunPosDate(String str) {
		int col = 1;
		printValueInCol(rpad(str, ONE_POS_ROW[col]), SUN_POS_LINE, col, ONE_POS_ROW);
	}
	public static void printSunPosZ(String str) {
		int col = 2;
		printValueInCol(lpad(str, ONE_POS_ROW[col]), SUN_POS_LINE, col, ONE_POS_ROW);
	}
	public static void printSunPosElev(String str) {
		int col = 3;
		printValueInCol(lpad(str, ONE_POS_ROW[col]), SUN_POS_LINE, col, ONE_POS_ROW);
	}
	public static void printDevicePosDate(String str) {
		int col = 1;
		printValueInCol(lpad(str, ONE_POS_ROW[col]), DEVICE_POS_LINE, col, ONE_POS_ROW);
	}
	public static void printDevicePosZ(String str) {
		int col = 2;
		printValueInCol(lpad(str, ONE_POS_ROW[col]), DEVICE_POS_LINE, col, ONE_POS_ROW);
	}
	public static void printDevicePosElev(String str) {
		int col = 3;
		printValueInCol(lpad(str, ONE_POS_ROW[col]), DEVICE_POS_LINE, col, ONE_POS_ROW);
	}

	// Draw an empty frame for movements (Elevation and Azimuth)
	public static void printMovementTable() {
		int line = START_MOV_FRAME_AT; // Start from that line
		AnsiConsole.out.println(
				ansiLocate(1, line++) + ANSI_NORMAL + ANSI_DEFAULT_BACKGROUND + ANSI_ITALIC + ANSI_BOLD + "- Movements -" + ANSI_DEFAULT_TEXT + PAD);
		// Frame top
		AnsiConsole.out.println(
				ansiLocate(1, line++) + ANSI_NORMAL + ANSI_DEFAULT_BACKGROUND + ANSI_DEFAULT_TEXT +
						TOP_LEFT_CORNER_BOLD +
						//	TOP_LEFT_ROUND_CORNER +
						drawXChar(SOLID_HORIZONTAL_BOLD, MOV_COL_1) +
						TOP_T_BOLD +
						drawXChar(SOLID_HORIZONTAL_BOLD, MOV_COL_2) +
						TOP_T_BOLD +
						drawXChar(SOLID_HORIZONTAL_BOLD, MOV_COL_3) +
						TOP_T_BOLD +
						drawXChar(SOLID_HORIZONTAL_BOLD, MOV_COL_4) +
						TOP_T_BOLD +
						drawXChar(SOLID_HORIZONTAL_BOLD, MOV_COL_5) +
						TOP_RIGHT_CORNER_BOLD +
						//	TOP_RIGHT_ROUND_CORNER +
						PAD);
		// Headers
		AnsiConsole.out.println(
				ansiLocate(1, line++) + ANSI_NORMAL + ANSI_DEFAULT_BACKGROUND + ANSI_DEFAULT_TEXT +
						SOLID_VERTICAL_BOLD +
						//	TOP_LEFT_ROUND_CORNER +
						rpad("", MOV_COL_1) +
						SOLID_VERTICAL_BOLD +
						rpad(" Date", MOV_COL_2) +
						SOLID_VERTICAL_BOLD +
						rpad("from", MOV_COL_3) +
						SOLID_VERTICAL_BOLD +
						rpad("to", MOV_COL_4) +
						SOLID_VERTICAL_BOLD +
						rpad("diff", MOV_COL_5) +
						SOLID_VERTICAL_BOLD +
						//	TOP_RIGHT_ROUND_CORNER +
						PAD);
		// Under the headers, separator
		AnsiConsole.out.println(
				ansiLocate(1, line++) + ANSI_NORMAL + ANSI_DEFAULT_BACKGROUND + ANSI_DEFAULT_TEXT +
						LEFT_T_BOLD +
						drawXChar(SOLID_HORIZONTAL_BOLD, MOV_COL_1) +
						CROSS_BOLD +
						drawXChar(SOLID_HORIZONTAL_BOLD, MOV_COL_2) +
						CROSS_BOLD +
						drawXChar(SOLID_HORIZONTAL_BOLD, MOV_COL_3) +
						CROSS_BOLD +
						drawXChar(SOLID_HORIZONTAL_BOLD, MOV_COL_4) +
						CROSS_BOLD +
						drawXChar(SOLID_HORIZONTAL_BOLD, MOV_COL_5) +
						RIGHT_T_BOLD +
						PAD);
		// First data line, Elev
		AnsiConsole.out.println(
				ansiLocate(1, line++) + ANSI_NORMAL + ANSI_DEFAULT_BACKGROUND + ANSI_DEFAULT_TEXT +
						SOLID_VERTICAL_BOLD +
						//	TOP_LEFT_ROUND_CORNER +
						rpad("Elev.", MOV_COL_1) +
						SOLID_VERTICAL_BOLD +
						rpad("", MOV_COL_2) +
						SOLID_VERTICAL_BOLD +
						rpad("", MOV_COL_3) +
						SOLID_VERTICAL_BOLD +
						rpad("", MOV_COL_4) +
						SOLID_VERTICAL_BOLD +
						rpad("", MOV_COL_5) +
						SOLID_VERTICAL_BOLD +
						//	TOP_RIGHT_ROUND_CORNER +
						PAD);
		// Separator
		AnsiConsole.out.println(
				ansiLocate(1, line++) + ANSI_NORMAL + ANSI_DEFAULT_BACKGROUND + ANSI_DEFAULT_TEXT +
						LEFT_T_BOLD +
						drawXChar(SOLID_HORIZONTAL_BOLD, MOV_COL_1) +
						CROSS_BOLD +
						drawXChar(SOLID_HORIZONTAL_BOLD, MOV_COL_2) +
						CROSS_BOLD +
						drawXChar(SOLID_HORIZONTAL_BOLD, MOV_COL_3) +
						CROSS_BOLD +
						drawXChar(SOLID_HORIZONTAL_BOLD, MOV_COL_4) +
						CROSS_BOLD +
						drawXChar(SOLID_HORIZONTAL_BOLD, MOV_COL_5) +
						RIGHT_T_BOLD +
						PAD);
		// Second data line, Z
		AnsiConsole.out.println(
				ansiLocate(1, line++) + ANSI_NORMAL + ANSI_DEFAULT_BACKGROUND + ANSI_DEFAULT_TEXT +
						SOLID_VERTICAL_BOLD +
						//	TOP_LEFT_ROUND_CORNER +
						rpad("Z", MOV_COL_1) +
						SOLID_VERTICAL_BOLD +
						rpad("", MOV_COL_2) +
						SOLID_VERTICAL_BOLD +
						rpad("", MOV_COL_3) +
						SOLID_VERTICAL_BOLD +
						rpad("", MOV_COL_4) +
						SOLID_VERTICAL_BOLD +
						rpad("", MOV_COL_5) +
						SOLID_VERTICAL_BOLD +
						//	TOP_RIGHT_ROUND_CORNER +
						PAD);

		// Frame bottom
		AnsiConsole.out.println(
				ansiLocate(1, line++) + ANSI_NORMAL + ANSI_DEFAULT_BACKGROUND + ANSI_DEFAULT_TEXT +
						BOTTOM_LEFT_CORNER_BOLD +
						//	TOP_LEFT_ROUND_CORNER +
						drawXChar(SOLID_HORIZONTAL_BOLD, MOV_COL_1) +
						BOTTOM_T_BOLD +
						drawXChar(SOLID_HORIZONTAL_BOLD, MOV_COL_2) +
						BOTTOM_T_BOLD +
						drawXChar(SOLID_HORIZONTAL_BOLD, MOV_COL_3) +
						BOTTOM_T_BOLD +
						drawXChar(SOLID_HORIZONTAL_BOLD, MOV_COL_4) +
						BOTTOM_T_BOLD +
						drawXChar(SOLID_HORIZONTAL_BOLD, MOV_COL_5) +
						BOTTOM_RIGHT_CORNER_BOLD +
						//	TOP_RIGHT_ROUND_CORNER +
						PAD);
	}

	public static void printElevMovDate(String str) {
		int col = 1;
		printValueInCol(rpad(str, ONE_MOV_ROW[col]), ELEV_MOV_LINE, col, ONE_MOV_ROW);
	}
	public static void printElevMovFrom(String str) {
		int col = 2;
		printValueInCol(lpad(str, ONE_MOV_ROW[col]), ELEV_MOV_LINE, col, ONE_MOV_ROW);
	}
	public static void printElevMovTo(String str) {
		int col = 3;
		printValueInCol(lpad(str, ONE_MOV_ROW[col]), ELEV_MOV_LINE, col, ONE_MOV_ROW);
	}
	public static void printElevMovDiff(String str) {
		int col = 4;
		printValueInCol(lpad(str, ONE_MOV_ROW[col]), ELEV_MOV_LINE, col, ONE_MOV_ROW);
	}
	public static void printZMovDate(String str) {
		int col = 1;
		printValueInCol(rpad(str, ONE_MOV_ROW[col]), Z_MOV_LINE, col, ONE_MOV_ROW);
	}
	public static void printZMovFrom(String str) {
		int col = 2;
		printValueInCol(lpad(str, ONE_MOV_ROW[col]), Z_MOV_LINE, col, ONE_MOV_ROW);
	}
	public static void printZMovTo(String str) {
		int col = 3;
		printValueInCol(lpad(str, ONE_MOV_ROW[col]), Z_MOV_LINE, col, ONE_MOV_ROW);
	}
	public static void printZMovDiff(String str) {
		int col = 4;
		printValueInCol(lpad(str, ONE_MOV_ROW[col]), Z_MOV_LINE, col, ONE_MOV_ROW);
	}

	// Draw an empty frame for info
	public static void printInfoTable() {
		int line = START_INFO_FRAME_AT; // Start from that line
		AnsiConsole.out.println(
				ansiLocate(1, line++) + ANSI_NORMAL + ANSI_DEFAULT_BACKGROUND + ANSI_ITALIC + ANSI_BOLD + "- Status -" + ANSI_DEFAULT_TEXT + PAD);
		// Frame top
		AnsiConsole.out.println(
				ansiLocate(1, line++) + ANSI_NORMAL + ANSI_DEFAULT_BACKGROUND + ANSI_DEFAULT_TEXT +
						TOP_LEFT_CORNER_BOLD +
						//	TOP_LEFT_ROUND_CORNER +
						drawXChar(SOLID_HORIZONTAL_BOLD, INFO_COL_1) +
						TOP_T_BOLD +
						drawXChar(SOLID_HORIZONTAL_BOLD, INFO_COL_2) +
						TOP_RIGHT_CORNER_BOLD +
						//	TOP_RIGHT_ROUND_CORNER +
						PAD);
		// Headers
		AnsiConsole.out.println(
				ansiLocate(1, line++) + ANSI_NORMAL + ANSI_DEFAULT_BACKGROUND + ANSI_DEFAULT_TEXT +
						SOLID_VERTICAL_BOLD +
						//	TOP_LEFT_ROUND_CORNER +
						rpad(" Date", INFO_COL_1) +
						SOLID_VERTICAL_BOLD +
						rpad(" Info", INFO_COL_2) +
						SOLID_VERTICAL_BOLD +
						//	TOP_RIGHT_ROUND_CORNER +
						PAD);
		// Under the headers, separator
		AnsiConsole.out.println(
				ansiLocate(1, line++) + ANSI_NORMAL + ANSI_DEFAULT_BACKGROUND + ANSI_DEFAULT_TEXT +
						LEFT_T_BOLD +
						drawXChar(SOLID_HORIZONTAL_BOLD, INFO_COL_1) +
						CROSS_BOLD +
						drawXChar(SOLID_HORIZONTAL_BOLD, INFO_COL_2) +
						RIGHT_T_BOLD +
						PAD);
		// Only data line.
		AnsiConsole.out.println(
				ansiLocate(1, line++) + ANSI_NORMAL + ANSI_DEFAULT_BACKGROUND + ANSI_DEFAULT_TEXT +
						SOLID_VERTICAL_BOLD +
						//	TOP_LEFT_ROUND_CORNER +
						rpad("", INFO_COL_1) +
						SOLID_VERTICAL_BOLD +
						rpad("", INFO_COL_2) +
						SOLID_VERTICAL_BOLD +
						//	TOP_RIGHT_ROUND_CORNER +
						PAD);
		// Frame bottom
		AnsiConsole.out.println(
				ansiLocate(1, line++) + ANSI_NORMAL + ANSI_DEFAULT_BACKGROUND + ANSI_DEFAULT_TEXT +
						BOTTOM_LEFT_CORNER_BOLD +
						//	TOP_LEFT_ROUND_CORNER +
						drawXChar(SOLID_HORIZONTAL_BOLD, INFO_COL_1) +
						BOTTOM_T_BOLD +
						drawXChar(SOLID_HORIZONTAL_BOLD, INFO_COL_2) +
						BOTTOM_RIGHT_CORNER_BOLD +
						//	TOP_RIGHT_ROUND_CORNER +
						PAD);
	}
	public static void printInfoDate(String str) {
		int col = 0;
		printValueInCol(rpad(str, ONE_INFO_ROW[col]), INFO_LINE, col, ONE_INFO_ROW);
	}
	public static void printInfoMessage(String str) {
		int col = 1;
		printValueInCol(rpad(str, ONE_INFO_ROW[col]), INFO_LINE, col, ONE_INFO_ROW);
	}

	// Just an example
	public static void main(String... args) {
		AnsiConsole.systemInstall();
		AnsiConsole.out.println(ANSI_CLS);
		AnsiConsole.out.println(ANSI_AT55 + ANSI_REVERSE + "10,10 reverse : Hello world" + ANSI_NORMAL);
		AnsiConsole.out.println(ANSI_HOME + ANSI_WHITE_ON_BLUE + "WhiteOnBlue : Hello world" + ANSI_NORMAL);
		AnsiConsole.out.print(ANSI_BOLD + "Bold : Press return..." + ANSI_NORMAL);

		System.out.println();

		try {
			System.in.read(); // User hit [Return]
		} catch (Exception e) {
		}
		//  AnsiConsole.out.println(ANSI_CLS);
		AnsiConsole.out.println(ANSI_NORMAL + "Normal text and " + ANSI_WHITE_ON_BLUE + "bold" + ANSI_NORMAL + " text.");
		AnsiConsole.out.println(ANSI_NORMAL + "Normal " + ansiSetTextColor(ANSI_YELLOW) + "yellow" + ANSI_NORMAL + " text and " + ansiSetTextAndBackgroundColor(ANSI_WHITE, ANSI_BLACK) + "bold" + ANSI_NORMAL + " text.");

		System.out.println(ANSI_NORMAL + "Normal text and " + ANSI_WHITE_ON_BLUE + "bold" + ANSI_NORMAL + " text.");
		System.out.println(ANSI_NORMAL + "Normal " + ansiSetTextColor(ANSI_YELLOW) + "yellow" + ANSI_NORMAL + " text and " + ansiSetTextAndBackgroundColor(ANSI_WHITE, ANSI_BLACK) + "bold" + ANSI_NORMAL + " text.");

		System.out.println(ansiSetTextAndBackgroundColor(ANSI_GREEN, ANSI_RED) + "this concludes the " + ansiSetTextColor(ANSI_WHITE) + "Jansi" + ansiSetTextColor(ANSI_GREEN) + " demo" + ANSI_NORMAL);
		AnsiConsole.systemUninstall();
	}
}
