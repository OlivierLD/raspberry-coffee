package oliv.json;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class JsonQL {

	private final static boolean DEBUG = false;

	private final static String QUERY_PREFIX = "-q:";
	private final static String FULL_QUERY_PREFIX = "--query:";


	// TODO "Where" syntax like /.*_DATA[elevation > 1]/date

	private static void processQuery(JSONObject json, String[] queryPath, List<String> actualPath, int level) {

		String patternStr = queryPath[level];
		Pattern pattern = Pattern.compile(patternStr);

		json.keySet().forEach(k -> {
			if (DEBUG) {
				System.out.printf("Level %d, key: %s\n", level, k);
			}
			Matcher matcher = pattern.matcher(k);
//			System.out.println("Match:" + matcher.matches());
			List<String> thisPath = new ArrayList<>(); // A new one
			thisPath.addAll(actualPath);
			if (matcher.matches()) {
				thisPath.add(k);
				Object obj = json.get(k);
				if (DEBUG) {
					System.out.println(String.format("Applying [%s] to %s", queryPath[level], json.toString(2)));
				}
				String fullPath = thisPath.stream().collect(Collectors.joining("/"));
				if (obj == null) {
					System.out.println("Not found...");
				} else {
					if (obj instanceof JSONObject) {

						JSONObject jsonObject = (JSONObject)obj;
						if (DEBUG || level == (queryPath.length - 1)) {
							System.out.println(jsonObject.toString(2));
						}
						if (level < (queryPath.length - 1)) {
							processQuery(jsonObject, queryPath, thisPath, level + 1);
						}
					} else {
						if (obj instanceof String) {
							System.out.println(String.format(">> Query result for %s: %s", fullPath, (String) obj));
						} else if (obj instanceof Double) {
							System.out.println(String.format(">> Query result for %s: %f", fullPath, (Double) obj));
						} else if (obj instanceof Float) {
							System.out.println(String.format(">> Query result for %s: %f", fullPath, (Float) obj));
						} else if (obj instanceof Integer) {
							System.out.println(String.format(">> Query result for %s: %d", fullPath, (Integer) obj));
						} else if (obj instanceof Long) {
							System.out.println(String.format(">> Query result for %s: %d", fullPath, (Long) obj));
						} else {
							System.out.println(String.format("We have a %s...", obj.getClass().getName()));
							System.out.println("Later..."); // TODO Do it
						}
					}
				}
			}
		});
	}

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

		if (DEBUG) {
			System.out.println("Read from java:" + sb.toString()); // If DEBUG
		}

		if (args.length == 0) {
			throw new IllegalArgumentException("Need parameters.");
		}

		String query = "";
		for (int i=0; i<args.length; i++) {
			System.out.println(String.format("> %d: %s", (i+1), args[i]));
			if (args[i].startsWith(QUERY_PREFIX)) {
				query = args[i].substring(QUERY_PREFIX.length());
			} else if (args[i].startsWith(FULL_QUERY_PREFIX)) {
				query = args[i].substring(FULL_QUERY_PREFIX.length());
			}
		}

		JSONObject jsonObject = null;
		// stdin: json
		try {
			jsonObject = new JSONObject(sb.toString());
			if (DEBUG) {
				System.out.println(String.format("JSON: %s", jsonObject.toString(2)));
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		if (!query.isEmpty()) {
			if (jsonObject != null) {
				// Process query
				String[] pathElem = query.split("/");
				processQuery(jsonObject, pathElem, new ArrayList<String>(), 0);
			} else {
				System.out.println("No JSON...");
			}
		} else {
			System.out.println("No query...");
		}

	}
}
