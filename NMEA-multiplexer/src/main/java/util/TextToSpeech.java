package util;

import java.util.HashMap;
import java.util.Map;

public class TextToSpeech {
	private static Map<String, String> speechTools = new HashMap<>();
	// See this: https://howtoraspberrypi.com/make-talk-raspberry-pi-espeak/
	static{
		speechTools.put("Mac OS X", "say \"%s\"");
		speechTools.put("Linux", "espeak -a 200 \"%s\" --stdout | aplay");
	}

	public static void speak(String text) {
		String osName = System.getProperty("os.name");
		try {
			switch (osName) {
				case "Mac OS X":
					Runtime.getRuntime().exec(new String[] { "say", "\"" + text + "\"" });
					break;
				case "Linux":
					Runtime.getRuntime().exec(new String[] { "/bin/bash", "-c", "espeak -a 200 \"" + text + "\" --stdout | aplay" });
					break;
				default:
					throw new RuntimeException("No speech tool found in this os [" + osName + "]");
//					break;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public static void main(String... args) {
		System.out.println("OS is [" + System.getProperty("os.name") + "]");
//	speak("You got a message from 415-745-5209. Do you wan to read it?");
		speak("Oh hello Pussycat, what's you doing up there?");
	}
}
