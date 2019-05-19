package tests;

import java.lang.String;
import java.lang.reflect.Method;

public class ParserTest {
	private ParserTest instance = this;

	private static Method GENERIC_FAILURE_PARSER;
	private static Method GENERIC_SUCCESS_PARSER;
	private static Method INCOMING_MESSAGE_MANAGER;

	static {
		try {
			GENERIC_FAILURE_PARSER = ParserTest.class.getMethod("genericFailureParser", String.class);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		try {
			GENERIC_SUCCESS_PARSER = ParserTest.class.getMethod("genericSuccessParser", String.class);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		try {
			INCOMING_MESSAGE_MANAGER = ParserTest.class.getMethod("incomingMessageManager", String.class);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public enum ArduinoMessagePrefix {
		FONA_OK(">> FONA READY", "Good to go", GENERIC_SUCCESS_PARSER),
		INCOMING_MESS("+CMTI:", "Incoming message", INCOMING_MESSAGE_MANAGER),
		BAT_OK(">> BAT OK", "Read Battery", GENERIC_SUCCESS_PARSER),
		BAT_FAILED(">> BAT FAILED", "Read Battery failed", GENERIC_FAILURE_PARSER);

		private final String prefix;
		private final String meaning;
		private final Method parser;

		ArduinoMessagePrefix(String prefix, String meaning, Method parser) {
			this.prefix = prefix;
			this.meaning = meaning;
			this.parser = parser;
		}

		public String prefix() {
			return this.prefix;
		}

		public String meaning() {
			return this.meaning;
		}

		public Method parser() {
			return this.parser;
		}
	}

	public static void main(String... args) throws Exception {
		ParserTest pt = new ParserTest();

		pt.takeAction(">> BAT FAILED, c'est tout pete!");
		pt.takeAction(">> FONA READY");
		pt.takeAction(">> BAT OK");
		pt.takeAction("+CMTI: \"SM\",54");

		String message = ">> BAT:1234,98";
		String[] sa = message.substring(">> BAT:".length()).split(",");
		for (String s : sa) {
			System.out.println(s);
		}
		System.out.println("Battery: " + sa[0] + " mV, " + sa[1] + "%");
	}

	private void takeAction(String mess) throws Exception {
		ArduinoMessagePrefix amp = findCommand(mess);
		if (amp != null) {
			System.out.println(amp.meaning());
			Method parser = amp.parser();
			if (parser != null) {
				parser.invoke(instance, mess);
			}
		} else
			System.out.println("Command [" + mess + "] unknown.");
	}

	private static ArduinoMessagePrefix findCommand(String message) {
		ArduinoMessagePrefix ret = null;
		for (ArduinoMessagePrefix amp : ArduinoMessagePrefix.values()) {
			if (message.startsWith(amp.prefix())) {
				ret = amp;
				break;
			}
		}
		return ret;
	}

	public void genericSuccessParser(String message) {
		System.out.println("Generic success:" + message);
	}

	public void genericFailureParser(String message) {
		System.out.println("Generic failure:" + message);
	}

	public void incomingMessageManager(String message) {
		// +CMTI: "SM",3
		String[] sa = message.split(",");
		if (sa.length == 2) {
			// Build the command that will read the new message:
			String readMessCmd = "r|" + sa[1].trim();
			// TODO Send the command to the serial port
			System.out.println("Sending " + readMessCmd + " >>>");
		}
	}
}
