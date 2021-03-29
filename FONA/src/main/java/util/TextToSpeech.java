package util;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class TextToSpeech {
	private static Map<String, Consumer<String>> speechTools = new HashMap<>();

	static Consumer<String> say = message -> {
		try {
			// User say -v ? for a list of voices.
//			Runtime.getRuntime().exec(new String[] { "say", "-v", "Thomas", "\"" + message + "\"" }); // French
			Runtime.getRuntime().exec(new String[] { "say", "-v", "Alex", "\"" + message + "\"" });  // English
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	};

	static Consumer<String> espeak = message -> {
		try {
			Runtime.getRuntime().exec(new String[] { "espeak", "\"" + message + "\"" });
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	};

	static {
		speechTools.put("Mac OS X", say);
		speechTools.put("Linux", espeak);
	}

	public static void speak(String text) {
		Consumer<String> speechTool = speechTools.get(System.getProperty("os.name"));
		if (speechTool == null) {
			throw new RuntimeException("No speech tool found in this os [" + System.getProperty("os.name") + "]");
		}
		try {
			speechTool.accept(text);
//			Runtime.getRuntime().exec(new String[] { speechTool, "\"" + text + "\"" });
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public static void main(String... args) {
		System.out.println("OS is [" + System.getProperty("os.name") + "]");
//	speak("You got a message from 415-745-5209. Do you wan to read it?");
		speak("Oh hello Pussycat, what's you doing up there?");
//	 	speak("Tu as un message, tu veux que je te le lise ?");
	}
}
