package oliv.tutorial;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class Streams {

	private final static String LOREM_IPSUM = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.";

	private final static List<String> WORDS = Arrays.asList(LOREM_IPSUM.split(" "));

	public static void main(String... args) {
		System.out.println(String.format("Found %d word(s).", WORDS.parallelStream().count()));

		for (String s : WORDS) {
			System.out.println(s);
		}
		System.out.println("==============");
		WORDS.forEach(w -> System.out.println(w));
		System.out.println("==============");
		WORDS.forEach(System.out::println);
		System.out.println("==============");
		Stream<String> stream = WORDS.stream();
		stream.filter(w -> w.charAt(0) == w.toUpperCase().charAt(0)).forEach(System.out::println);

		WORDS.stream()
				.filter(w -> w.charAt(0) == w.toLowerCase().charAt(0))
				.forEach(System.out::println);
	}
}
