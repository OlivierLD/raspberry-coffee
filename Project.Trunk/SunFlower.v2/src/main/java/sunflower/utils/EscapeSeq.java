package sunflower.utils;

import org.fusesource.jansi.AnsiConsole;

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
	public static final String ANSI_HOME = ESC + "[H";  // 0,0
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

	private final static String[] SOME_TEXT = {
			"What happens when the boat sends an email:",
			"On the boat, you compose your email, and you put it in your outbox.",
			"Then you turn your SSB on, and you use the SailMail client program to contact a land SailMail station.",
			"When the contact is established, the messages sitting in the outbox go through the modem and the SSB to ",
			"be streamed to the land station. On receive, the land station then turns the messages back into digital files,",
			"and uses its Internet connection to post them on the web. From there, it's the usual email story."
	};

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
		for (int i = 0; i < Math.min(orig.length(), override.length()); i++) {
			ret[i] = (byte) override.charAt(i);
		}
		return new String(ret);
	}

	// An example
	public static void main_(String... args) {
		String str80 = "                                                                                ";
		AnsiConsole.systemInstall();
		AnsiConsole.out.println(ANSI_CLS);

		// Display 5 rows, like an horizontal bar chart
		for (int i = 0; i < 20; i++) {
			int value1 = (int) Math.round(Math.random() * 80);
			int value2 = (int) Math.round(Math.random() * 80);
			int value3 = (int) Math.round(Math.random() * 80);
			int value4 = (int) Math.round(Math.random() * 80);
			int value5 = (int) Math.round(Math.random() * 80);

			String str1 = "";
			for (int j = 0; j < value1; j++) {
				str1 += ".";
			}
			String str2 = "";
			for (int j = 0; j < value2; j++) {
				str2 += ".";
			}
			String str3 = "";
			for (int j = 0; j < value3; j++) {
				str3 += ".";
			}
			String str4 = "";
			for (int j = 0; j < value4; j++) {
				str4 += ".";
			}
			String str5 = "";
			for (int j = 0; j < value5; j++) {
				str5 += ".";
			}

			str1 = superpose(str1, "Cell 1:" + Integer.toString(value1));
			str2 = superpose(str2, "Cell 2:" + Integer.toString(value2));
			str3 = superpose(str3, "Cell 3:" + Integer.toString(value3));
			str4 = superpose(str4, "Cell 4:" + Integer.toString(value4));
			str5 = superpose(str5, "Cell 5:" + Integer.toString(value5));

//    AnsiConsole.out.println(ANSI_CLS);
			AnsiConsole.out.println(ansiLocate(0, 1) + ANSI_NORMAL + ANSI_DEFAULT_BACKGROUND + ANSI_DEFAULT_TEXT + str80);
			AnsiConsole.out.println(ansiLocate(0, 1) + ansiSetTextAndBackgroundColor(ANSI_WHITE, ANSI_RED) + ANSI_BOLD + str1 + ANSI_NORMAL + ANSI_DEFAULT_BACKGROUND + ANSI_DEFAULT_TEXT);
			AnsiConsole.out.println(ansiLocate(0, 2) + ANSI_NORMAL + ANSI_DEFAULT_BACKGROUND + ANSI_DEFAULT_TEXT + str80);
			AnsiConsole.out.println(ansiLocate(0, 2) + ansiSetTextAndBackgroundColor(ANSI_WHITE, ANSI_WHITE) + ANSI_BOLD + str2 + ANSI_NORMAL + ANSI_DEFAULT_BACKGROUND + ANSI_DEFAULT_TEXT);
			AnsiConsole.out.println(ansiLocate(0, 3) + ANSI_NORMAL + ANSI_DEFAULT_BACKGROUND + ANSI_DEFAULT_TEXT + str80);
			AnsiConsole.out.println(ansiLocate(0, 3) + ansiSetTextAndBackgroundColor(ANSI_WHITE, ANSI_YELLOW) + ANSI_BOLD + str3 + ANSI_NORMAL + ANSI_DEFAULT_BACKGROUND + ANSI_DEFAULT_TEXT);
			AnsiConsole.out.println(ansiLocate(0, 4) + ANSI_NORMAL + ANSI_DEFAULT_BACKGROUND + ANSI_DEFAULT_TEXT + str80);
			AnsiConsole.out.println(ansiLocate(0, 4) + ansiSetTextAndBackgroundColor(ANSI_WHITE, ANSI_GREEN) + ANSI_BOLD + str4 + ANSI_NORMAL + ANSI_DEFAULT_BACKGROUND + ANSI_DEFAULT_TEXT);
			AnsiConsole.out.println(ansiLocate(0, 5) + ANSI_NORMAL + ANSI_DEFAULT_BACKGROUND + ANSI_DEFAULT_TEXT + str80);
			AnsiConsole.out.println(ansiLocate(0, 5) + ansiSetTextAndBackgroundColor(ANSI_WHITE, ANSI_BLUE) + ANSI_BOLD + str5 + ANSI_NORMAL + ANSI_DEFAULT_BACKGROUND + ANSI_DEFAULT_TEXT);

			try {
				Thread.sleep(1_000L);
			} catch (Exception ex) {
			}
		}

		System.out.println(ansiSetTextAndBackgroundColor(ANSI_GREEN, ANSI_RED) + "this concludes the " + ansiSetTextColor(ANSI_WHITE) + "Jansi" + ansiSetTextColor(ANSI_GREEN) + " demo" + ANSI_NORMAL);
		AnsiConsole.systemUninstall();
	}

	public static void main(String... args) {
		AnsiConsole.systemInstall();
		AnsiConsole.out.println(ANSI_CLS);
		AnsiConsole.out.println(ANSI_AT55 + ANSI_REVERSE + "10,10 reverse : Hello world" + ANSI_NORMAL);
		AnsiConsole.out.println(ANSI_HOME + ANSI_WHITEONBLUE + "WhiteOnBlue : Hello world" + ANSI_NORMAL);
		AnsiConsole.out.print(ANSI_BOLD + "Bold : Press return..." + ANSI_NORMAL);

		System.out.println();

		try {
			System.in.read(); // User hit [Return]
		} catch (Exception e) {
		}
		//  AnsiConsole.out.println(ANSI_CLS);
		AnsiConsole.out.println(ANSI_NORMAL + "Normal text and " + ANSI_WHITEONBLUE + "bold" + ANSI_NORMAL + " text.");
		AnsiConsole.out.println(ANSI_NORMAL + "Normal " + ansiSetTextColor(ANSI_YELLOW) + "yellow" + ANSI_NORMAL + " text and " + ansiSetTextAndBackgroundColor(ANSI_WHITE, ANSI_BLACK) + "bold" + ANSI_NORMAL + " text.");

		System.out.println(ANSI_NORMAL + "Normal text and " + ANSI_WHITEONBLUE + "bold" + ANSI_NORMAL + " text.");
		System.out.println(ANSI_NORMAL + "Normal " + ansiSetTextColor(ANSI_YELLOW) + "yellow" + ANSI_NORMAL + " text and " + ansiSetTextAndBackgroundColor(ANSI_WHITE, ANSI_BLACK) + "bold" + ANSI_NORMAL + " text.");

		for (String line : SOME_TEXT) {
			System.out.print(ANSI_HEAD + line);
			try {
				Thread.sleep(1_000L);
			} catch (Exception ex) {
			}
		}
		System.out.println();

		System.out.println(ansiSetTextAndBackgroundColor(ANSI_GREEN, ANSI_RED) + "this concludes the " + ansiSetTextColor(ANSI_WHITE) + "Jansi" + ansiSetTextColor(ANSI_GREEN) + " demo" + ANSI_NORMAL);
		AnsiConsole.systemUninstall();
	}
}
