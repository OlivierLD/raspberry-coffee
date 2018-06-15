package main;

/**
 * Find more ANSI box drawing codes at https://en.wikipedia.org/wiki/Box-drawing_character
 */
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
	public final static String SOLID_HORIZONTAL_BOLD = "\u2501";
	public final static String SOLID_VERTICAL = "\u2502";
	public final static String SOLID_VERTICAL_BOLD = "\u2503";
	public final static String DOTTED_HORIZONTAL = "\u2504";
	public final static String DOTTED_HORIZONTAL_BOLD = "\u2505";
	public final static String DOTTED_VERTICAL = "\u2506";
	public final static String DOTTED_VERTICAL_BOLD = "\u2507";

	public final static String TOP_LEFT_CORNER = "\u250c";
	public final static String TOP_LEFT_CORNER_BOLD = "\u250f";
	public final static String TOP_RIGHT_CORNER = "\u2510";
	public final static String TOP_RIGHT_CORNER_BOLD = "\u2513";

	public final static String BOTTOM_LEFT_CORNER = "\u2514";
	public final static String BOTTOM_LEFT_CORNER_BOLD = "\u2517";
	public final static String BOTTOM_RIGHT_CORNER = "\u2518";
	public final static String BOTTOM_RIGHT_CORNER_BOLD = "\u251b";

	public final static String LEFT_T = "\u251c";
	public final static String LEFT_T_BOLD = "\u2523";
	public final static String RIGHT_T = "\u2524";
	public final static String RIGHT_T_BOLD = "\u252b";
	public final static String TOP_T = "\u252c";
	public final static String TOP_T_BOLD = "\u2533";
	public final static String BOTTOM_T = "\u2534";
	public final static String BOTTOM_T_BOLD = "\u253b";

	public final static String CROSS = "\u253c";
	public final static String CROSS_BOLD = "\u254b";

	public final static String DOUBLE_LEFT_T = "\u2560";
	public final static String DOUBLE_RIGHT_T = "\u2563";

// And there is way more...

	public static String ansiLocate(int x, int y) {
		return ESC + "[" + Integer.toString(y) + ";" + Integer.toString(x) + "H"; // Actually Y, X
	}
}

