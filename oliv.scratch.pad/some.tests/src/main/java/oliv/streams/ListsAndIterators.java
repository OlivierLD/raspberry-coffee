package oliv.streams;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ListsAndIterators {
	public static void main(String... args) {
		// Populate
		List<Double> list = new ArrayList<>();
		for (int i=0; i<200; i++) {
			list.add(200 * Math.random());
		}
		// List the Java 8 way:
		list.forEach(System.out::println);
		System.out.println("-------");
		// List the Java 7 way
		Iterator<Double> iterator = list.iterator();
		while (iterator.hasNext()) {
			System.out.println(iterator.next());
		}
	}
}
