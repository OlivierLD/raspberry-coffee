package oliv.lambda;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * More specific samples, with REST path matching.
 */
public class Lambda102 {

	private static boolean pathMatches(String pattern, String path) {
		boolean match = true;
		String[] patternElem = pattern.split("/");
		String[] pathElem = path.split("/");

		if (patternElem.length == pathElem.length) {
			for (int i = 0; match && i < patternElem.length; i++) {
				if (patternElem[i].startsWith("{") && patternElem[i].endsWith("}")) {
					match = true;
				} else {
					match = patternElem[i].equals(pathElem[i]);
				}
			}
		} else {
			match = false;
		}
		return match;
	}

	private static List<String> getPrmValues(String pattern, String path) {
		List<String> returned = new ArrayList<>();
		String[] patternElem = pattern.split("/");
		String[] pathElem = path.split("/");

		if (patternElem.length == pathElem.length) {
			for (int i = 0; i < patternElem.length; i++) {
				if (patternElem[i].startsWith("{") && patternElem[i].endsWith("}")) {
					returned.add(pathElem[i]);
				}
			}
		}
		return returned;
	}

	enum ResourceProcessor {
		RESOURCE_ONE("GET", "/one/{x}/two/{y}", Lambda102::mirror),
		RESOURCE_TWO("POST", "/one/{x}/two/{y}", Lambda102::mirror),
		RESOURCE_THREE("PUT", "/one/{x}/two/{y}", Lambda102::mirror),
		RESOURCE_FOUR("DELETE", "/first/{prm}", Lambda102::mirror);

		private final String verb;
		private final String path;
		private final Function<List<String>, String> fn;

		ResourceProcessor(String verb, String path, Function<List<String>, String> fn) {
			this.verb = verb;
			this.path = path;
			this.fn = fn;
		}

		public String verb() {
			return this.verb;
		}

		public String path() {
			return this.path;
		}

		public Function<List<String>, String> fn() {
			return this.fn;
		}
	}

	private static String mirror(List<String> s) {
		return s.stream().collect(Collectors.joining(","));
	}

	private static void processRequest(String verb, String resource) {

		for (ResourceProcessor rp : ResourceProcessor.values()) {
			if (rp.verb().equals(verb) && pathMatches(rp.path(), resource)) {
				String processed = rp.fn().apply(getPrmValues(rp.path(), resource));
				System.out.println(">> Processed:" + processed);
				return;
			}
		}
		System.out.println(verb + ", " + resource + " : Not found!!");
	}

	public static void main(String... args) {
		String verb = "GET";
		String resource = "/one/1/two/2";
		processRequest(verb, resource);

		verb = "GET";
		resource = "/one/1/two/2/three";
		processRequest(verb, resource);

		verb = "DELETE";
		resource = "/first/pouet-pouet";
		processRequest(verb, resource);
	}
}
