package oliv.misc;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class GenericTest {

	private static <T> String genericFunction(T module, Function<T, Class> customProcess) {
		System.out.println(String.format("Received a %s", module.getClass().getName()));
		String ret = "-";
		if (customProcess != null) {
			Class cls = customProcess.apply(module);
			ret = cls.getName();
		}
		return ret;
	}

	private static class InnerClass {
		String thing;
		public InnerClass thing(String thing) {
			this.thing = thing;
			return this;
		}
	}

	public static void main(String... args) {

		Map<String, String> map = new HashMap<>();
		InnerClass innerClass = new InnerClass();

		String s = genericFunction(map, prm -> {
			prm.put("Akeu", "Coucou"); // Values are not used.
			return prm.getClass();
		});
		System.out.println("=> " + s);

		String s2 = genericFunction(innerClass, prm -> {
			prm = prm.thing("thing");
			return prm.getClass();
		});
		System.out.println("=> " + s2);
	}
}
