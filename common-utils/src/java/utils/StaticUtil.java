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

	public static byte[] appendByteArrays(byte c[], byte b[], int n) {
		int newLength = c != null ? c.length + n : n;
		byte newContent[] = new byte[newLength];
		if (c != null) {
			for (int i = 0; i < c.length; i++)
				newContent[i] = c[i];
		}
		int offset = (c != null ? c.length : 0);
		for (int i = 0; i < n; i++)
			newContent[offset + i] = b[i];
		return newContent;
	}

	public static void main(String... args) {
		String akeu = userInput("Tell me > ");
		System.out.println(akeu);
	}

}
