package oliv.misc;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class SwitchTest {

	private static <T> String genericFunction(T module, Function<T, Class> customProcess) {
		System.out.println(String.format("Received a %s", module.getClass().getName()));
		String ret = "Yo!";
		if (customProcess != null) {
			Class cls = customProcess.apply(module);
			ret = cls.getName();
		}
		return ret;
	}

	public static void main(String... args) {

		Map<String, String> map = new HashMap<>();

		String s = genericFunction(map, prm -> {
			prm.put("Akeu", "Coucou");
			return prm.getClass();
		});
		System.out.println("=> " + s);
	}

}
