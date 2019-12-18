package oliv.misc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ImmutableList {

	public static void givenUsingTheJdk_whenUnmodifiableListIsCreated_thenNotModifiable() {
		List<String> list = new ArrayList<>(Arrays.asList("one", "two", "three"));
		List<String> unmodifiableList = Collections.unmodifiableList(list);
		unmodifiableList.add("four");
	}

	public static void main(String... args) {
		try {
			givenUsingTheJdk_whenUnmodifiableListIsCreated_thenNotModifiable();
		} catch (UnsupportedOperationException uoe) {
			System.err.println("Oops... As expected");
		}
	}
}
