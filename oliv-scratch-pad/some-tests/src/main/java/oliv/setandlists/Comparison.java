package oliv.setandlists;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Comparison {

	public static void main(String... args) {
		List<Integer> li1 = Arrays.asList(10, 20, 30, 40);
		List<Integer> li2 = Arrays.asList(20, 10, 40, 30);

		System.out.println("li1 = li2: " + li1.equals(li2));

		Set<Integer> set1 = li1.stream().collect(Collectors.toSet());
		Set<Integer> set2 = li2.stream().collect(Collectors.toSet());

		li1.stream().collect(Collectors.toSet());

		System.out.println("set1 = set2: " + set1.equals(set2));
	}
}
