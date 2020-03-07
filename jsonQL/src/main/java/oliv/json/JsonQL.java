package oliv.json;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class JsonQL {

	public static void main(String... args) {

		StringBuffer sb = new StringBuffer();
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
			String line;
			while ((line = in.readLine()) != null) {
				sb.append(line);
			}

		} catch (IOException ioe) {
			ioe.printStackTrace();
		}

		System.out.println("Read from java:" + sb.toString());

		if (args.length == 0) {
			throw new IllegalArgumentException("Need parameters.");
		}

		for (int i=0; i<args.length; i++) {
			System.out.println(String.format("> %d: %s", (i+1), args[i]));
		}

		// 1st param: json
		try {
			JSONObject jsonObject = new JSONObject(sb.toString());
			System.out.println(String.format("JSON: %s", jsonObject.toString(2)));
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
