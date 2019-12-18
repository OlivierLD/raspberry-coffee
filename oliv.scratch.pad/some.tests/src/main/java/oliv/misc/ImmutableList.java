package oliv.misc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ImmutableList {

	public static void createImmutableList() {
		List<String> list = new ArrayList<>(Arrays.asList("one", "two", "three"));
		List<String> unmodifiableList = Collections.unmodifiableList(list);
		System.out.println(String.format("List: %s", unmodifiableList.stream().collect(Collectors.joining(", "))));
		System.out.println("List created and made immutable. Trying to add to it.");
		unmodifiableList.add("four");
	}

	public static void main(String... args) {
		try {
			createImmutableList();
		} catch (UnsupportedOperationException uoe) {
			System.err.println("Oops... As expected");
		}
	}
}
