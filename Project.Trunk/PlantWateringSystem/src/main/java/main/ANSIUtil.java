package main;

import com.pi4j.io.gpio.PinState;
import org.fusesource.jansi.AnsiConsole;

import java.util.Date;

import static utils.StringUtils.rpad;
import static utils.TimeUtil.fmtDHMS;
import static utils.TimeUtil.msToHMS;

/**
 * Find more ANSI box drawing codes at https://en.wikipedia.org/wiki/Box-drawing_character
 */
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
	public static final String ANSI_HOME = ESC + "[H"; // 0,0 Top left
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
	public final static String TOP_T_BOLD = "\u2533";
	public final static String BOTTOM_T = "\u2534";
	public final static String BOTTOM_T_BOLD = "\u253b";

	public final static String CROSS = "\u253c";
	public final static String CROSS_BOLD = "\u254b";

	public final static String DOUBLE_LEFT_T = "\u2560";
	public final static String DOUBLE_RIGHT_T = "\u2563";

	public final static String TOP_LEFT_ROUND_CORNER = "\u256d";
	public final static String TOP_RIGHT_ROUND_CORNER = "\u256e";
	public final static String BOTTOM_RIGHT_ROUND_CORNER = "\u256f";
	public final static String BOTTOM_LEFT_ROUND_CORNER = "\u2570";

// And there is way more...

	public static String ansiLocate(int x, int y) {
		return ESC + "[" + Integer.toString(y) + ";" + Integer.toString(x) + "H"; // Actually Y, X
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

	private final static int FRAME_WIDTH = 50;
	/**
	 * Box codes are available at https://en.wikipedia.org/wiki/Box-drawing_character
	 * Display the data in an ANSI box, refreshed every time is is displayed.
	 */
	public static void displayAnsiData(
			int humidityThreshold,
			long wateringDuration,
			long resumeSensorWatchAfter,
			double temperature,
			double humidity,
			String message,
			boolean withRESTServer,
			int httpPort,
			Long lastWatering,
			PinState relayState) {
		AnsiConsole.out.println(ANSIUtil.ANSI_CLS);
		int line = 1; // Start from that line
		// Frame top
		AnsiConsole.out.println(ansiLocate(1, line++) + ANSI_NORMAL + ANSI_DEFAULT_BACKGROUND + ANSI_DEFAULT_TEXT +
				TOP_LEFT_CORNER_BOLD +
		//	TOP_LEFT_ROUND_CORNER +
				drawXChar(SOLID_HORIZONTAL_BOLD, FRAME_WIDTH) +
				TOP_RIGHT_CORNER_BOLD +
		//	TOP_RIGHT_ROUND_CORNER +
				PAD);
		// Title. Note: The italic escape code is correct. But it does not work on all platforms.
		AnsiConsole.out.println(ansiLocate(1, line++) + ANSI_DEFAULT_BACKGROUND + ANSI_DEFAULT_TEXT + SOLID_VERTICAL_BOLD  + ANSI_BOLD + ANSI_ITALIC + rpad("              PLANT WATERING SYSTEM ", FRAME_WIDTH) + ANSI_NORMAL + SOLID_VERTICAL_BOLD + PAD);
		// Separator
		AnsiConsole.out.println(ansiLocate(1, line++) + ANSI_NORMAL + ANSI_DEFAULT_BACKGROUND + ANSI_DEFAULT_TEXT +
				LEFT_T_BOLD +
				drawXChar(SOLID_HORIZONTAL_BOLD, FRAME_WIDTH) +
				RIGHT_T_BOLD +
				PAD);
		// Program parameters
		AnsiConsole.out.println(ansiLocate(1, line++) + ANSI_NORMAL + ANSI_DEFAULT_BACKGROUND + ANSI_DEFAULT_TEXT + SOLID_VERTICAL_BOLD +
				rpad(String.format(" Start watering under %d%% of humidity.", humidityThreshold), FRAME_WIDTH) + SOLID_VERTICAL_BOLD +
				PAD);
		AnsiConsole.out.println(ansiLocate(1, line++) + ANSI_NORMAL + ANSI_DEFAULT_BACKGROUND + ANSI_DEFAULT_TEXT + SOLID_VERTICAL_BOLD +
				rpad(String.format(" Water during %s", fmtDHMS(msToHMS(wateringDuration * 1_000))), FRAME_WIDTH) + SOLID_VERTICAL_BOLD +
				PAD);
		AnsiConsole.out.println(ansiLocate(1, line++) + ANSI_NORMAL + ANSI_DEFAULT_BACKGROUND + ANSI_DEFAULT_TEXT + SOLID_VERTICAL_BOLD +
				rpad(String.format(" Resume sensor watch %s after watering.", fmtDHMS(msToHMS(resumeSensorWatchAfter * 1_000))), FRAME_WIDTH) + SOLID_VERTICAL_BOLD +
				PAD);
		// Separator
		AnsiConsole.out.println(ansiLocate(1, line++) + ANSI_NORMAL + ANSI_DEFAULT_BACKGROUND + ANSI_DEFAULT_TEXT +
				LEFT_T_BOLD +
				drawXChar(SOLID_HORIZONTAL_BOLD, FRAME_WIDTH) +
				RIGHT_T_BOLD +
				PAD);
		// REST ?
		AnsiConsole.out.println(ansiLocate(1, line++) + ANSI_NORMAL + ANSI_DEFAULT_BACKGROUND + ANSI_DEFAULT_TEXT + SOLID_VERTICAL_BOLD +
				rpad(String.format(" REST Server%s", (withRESTServer ? String.format(" on port %d", httpPort) : ": no")), FRAME_WIDTH) + SOLID_VERTICAL_BOLD +
				PAD);
		// Separator
		AnsiConsole.out.println(ansiLocate(1, line++) + ANSI_NORMAL + ANSI_DEFAULT_BACKGROUND + ANSI_DEFAULT_TEXT +
				LEFT_T_BOLD +
				drawXChar(SOLID_HORIZONTAL_BOLD, FRAME_WIDTH) +
				RIGHT_T_BOLD +
				PAD);
		// Sensor Data
		AnsiConsole.out.println(ansiLocate(1, line++) + ANSI_NORMAL + ANSI_DEFAULT_BACKGROUND + ANSI_DEFAULT_TEXT + SOLID_VERTICAL_BOLD +
				rpad(String.format(" Temp: %.02f C, Hum: %.02f%%", temperature, humidity), FRAME_WIDTH) + SOLID_VERTICAL_BOLD +
				PAD);
		// Separator
		AnsiConsole.out.println(ansiLocate(1, line++) + ANSI_NORMAL + ANSI_DEFAULT_BACKGROUND + ANSI_DEFAULT_TEXT +
				LEFT_T_BOLD +
				drawXChar(SOLID_HORIZONTAL_BOLD, FRAME_WIDTH) +
				RIGHT_T_BOLD +
				PAD);
		// Last Watering
		AnsiConsole.out.println(ansiLocate(1, line++) + ANSI_NORMAL + ANSI_DEFAULT_BACKGROUND + ANSI_DEFAULT_TEXT + SOLID_VERTICAL_BOLD +
				rpad(String.format(" Last watering: %s", (lastWatering == null ? "none" : new Date(lastWatering).toString())), FRAME_WIDTH) + SOLID_VERTICAL_BOLD +
				PAD);
		// Separator
		AnsiConsole.out.println(ansiLocate(1, line++) + ANSI_NORMAL + ANSI_DEFAULT_BACKGROUND + ANSI_DEFAULT_TEXT +
				LEFT_T_BOLD +
				drawXChar(SOLID_HORIZONTAL_BOLD, FRAME_WIDTH) +
				RIGHT_T_BOLD +
				PAD);
		// Relay Status
		AnsiConsole.out.println(ansiLocate(1, line++) + ANSI_NORMAL + ANSI_DEFAULT_BACKGROUND + ANSI_DEFAULT_TEXT + SOLID_VERTICAL_BOLD +
				rpad(String.format(" Valve Status: %s", (relayState.isHigh() ? "Closed" : "Opened" )), FRAME_WIDTH) + SOLID_VERTICAL_BOLD +
				PAD);
		// Separator
		AnsiConsole.out.println(ansiLocate(1, line++) + ANSI_NORMAL + ANSI_DEFAULT_BACKGROUND + ANSI_DEFAULT_TEXT +
				LEFT_T_BOLD +
				drawXChar(SOLID_HORIZONTAL_BOLD, FRAME_WIDTH) +
				RIGHT_T_BOLD +
				PAD);
		// Message
		AnsiConsole.out.println(ansiLocate(1, line++) + ANSI_NORMAL + ANSI_DEFAULT_BACKGROUND + ANSI_DEFAULT_TEXT + SOLID_VERTICAL_BOLD +
				rpad(String.format(" %s", message), FRAME_WIDTH) + SOLID_VERTICAL_BOLD +
				PAD);
		// Frame bottom
		AnsiConsole.out.println(ansiLocate(1, line++) + ANSI_NORMAL + ANSI_DEFAULT_BACKGROUND + ANSI_DEFAULT_TEXT +
				BOTTOM_LEFT_CORNER_BOLD +
	//		BOTTOM_LEFT_ROUND_CORNER +
				drawXChar(SOLID_HORIZONTAL_BOLD, FRAME_WIDTH) +
				BOTTOM_RIGHT_CORNER_BOLD +
	//		BOTTOM_RIGHT_ROUND_CORNER +
				PAD);
	}
}

