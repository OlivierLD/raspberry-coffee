package utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class StaticUtil {
	private static final BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
	public static String userInput(String prompt) {
		String retString = "";
		System.err.print(prompt);
		try {
			retString = stdin.readLine();
		} catch (Exception e) {
			System.out.println(e);
			try {
				userInput("<Oooch/>");
			} catch (Exception exception) {
				exception.printStackTrace();
			}
		}
		return retString;
	}
}
