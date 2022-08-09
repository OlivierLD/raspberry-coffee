package util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TextToSpeech {
	private final static Map<String, String> speechTools = new HashMap<>();
	// See this: https://howtoraspberrypi.com/make-talk-raspberry-pi-espeak/
	static{
		speechTools.put("Mac OS X", "say \"%s\"");
		speechTools.put("Linux", "espeak -a 200 \"%s\" --stdout | aplay");
	}

	public static void speak(String text) {
		String osName = System.getProperty("os.name");
		List<String> commands = new ArrayList<>();
		try {
			switch (osName) {
				case "Mac OS X":
					commands.add("say");
					commands.add("\"" + text + "\"");
					Runtime.getRuntime().exec(commands.toArray(new String[0]));
					break;
				case "Linux":
					commands.add("/bin/bash");
					commands.add("-c");
					commands.add("\"espeak -a 200 '" + text + "' --stdout | aplay\""); // No single quote in the message!!
					commands.forEach(System.out::println); // Verbose
					Process process = Runtime.getRuntime().exec(commands.toArray(new String[0]));
					BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
					String line;
					while ((line = in.readLine()) != null) {
						System.out.println(line);
					}
					in.close();
					break;
				default:
					throw new RuntimeException("No speech tool found in this os [" + osName + "]");
//					break;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
