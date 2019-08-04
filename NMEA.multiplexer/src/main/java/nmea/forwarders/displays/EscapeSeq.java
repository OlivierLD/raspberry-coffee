package nmea.forwarders.displays;

public class EscapeSeq {
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
	public static final String ANSI_HOME = ESC + "[H"; // 0,0
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

	public static final String ANSI_AT55 = ESC + "[10;10H"; // Actually 10, 10
	public static final String ANSI_WHITEONBLUE = ESC + "[37;44m";

	public static String ansiSetBackGroundColor(String color) {
		// ESC[40-47
		return ESC + "[4" + color + "m";
	}

	public static String ansiSetTextColor(String color) {
		// ESC[30-37
		return ESC + "[3" + color + "m";
	}

	public static String ansiSetTextAndBackgroundColor(String text, String bg) {
		// ESC[30-37;40-47
		return ESC + "[3" + text + ";4" + bg + "m";
	}

	public static String ansiLocate(int x, int y) {
		return ESC + "[" + Integer.toString(y) + ";" + Integer.toString(x) + "H"; // Actually Y, X
	}

	public static String superpose(String orig, String override) {
		byte[] ret = orig.getBytes();
		for (int i = 0; i < Math.min(orig.length(), override.length()); i++)
			ret[i] = (byte) override.charAt(i);
		return new String(ret);
	}
}