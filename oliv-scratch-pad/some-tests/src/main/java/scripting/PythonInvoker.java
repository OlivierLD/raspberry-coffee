package scripting;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Use ProcessBuilder to invoke python, and read its output.
 */
public class PythonInvoker {

	public static void main(String... args) throws Exception {
		System.out.println(String.format("Running from %s", System.getProperty("user.dir")));
		ProcessBuilder processBuilder = new ProcessBuilder("python", "./src/main/python/hello.py");
		processBuilder.redirectErrorStream(true);
		Process process = processBuilder.start();
		InputStream inputStream = process.getInputStream();

		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
		boolean keepReading = true;
		while (keepReading) {
			String line = reader.readLine();
			if (line == null) {
				keepReading = false;
			} else {
				System.out.println(line);
			}
		}
		reader.close();
		inputStream.close();
		System.out.println("Done");
	}

}

