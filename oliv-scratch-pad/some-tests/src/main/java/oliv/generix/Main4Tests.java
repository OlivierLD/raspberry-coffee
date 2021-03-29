package oliv.generix;

// import java.util.HashMap;
import java.util.Arrays;
import java.util.Map;

public class Main4Tests {

	public static void main(String... args) {

		GenericHandler<String> strHandler = GenericHandler.of("That's a string");

//		Map<String, String> map = new HashMap<>();
//		map.put("ONE", "one");
//		map.put("TWO", "two");

		Map<String, String> map = Map.of(
				"ONE", "one",
				"TWO", "two");
		GenericHandler mapHandler = GenericHandler.of(map);

		GenericHandler stuffHandler = GenericHandler.of(
				Arrays.asList("Duh", Math.PI, Integer.MAX_VALUE)
		);

		System.out.println("First handler :" + strHandler.get());
		System.out.println("Second handler:" + mapHandler.get());
		System.out.println("Third handler :" + stuffHandler.get());
	}
}
