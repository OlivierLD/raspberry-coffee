package oliv.streams;

import java.util.ArrayList;
import java.util.List;

public class FlatMapSample {

	public static void main(String... args) {
		List<List<String>> tree = new ArrayList<>();

		List<String> firstBranch = new ArrayList<>();
		firstBranch.add("One");
		firstBranch.add("Two");
		firstBranch.add("Three");

		tree.add(firstBranch);

		List<String> secondBranch = new ArrayList<>();
		secondBranch.add("A");
		secondBranch.add("B");
		secondBranch.add("C");
		secondBranch.add("D");

		tree.add(secondBranch);

		tree.stream()
				.flatMap(List::stream)
				.forEach(System.out::println);
	}
}
