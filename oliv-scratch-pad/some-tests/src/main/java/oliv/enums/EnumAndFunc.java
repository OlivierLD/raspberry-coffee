package oliv.enums;

import java.util.function.Function;

/**
 * Functions as member of an enum.
 */
public class EnumAndFunc {

	private static String echo(String str) {
		return str.toString();
	}

	private static String reverse(String str) {
		String s = str;
		StringBuffer sb = new StringBuffer();
		for (int i = s.length(); i > 0; i--) {
			sb.append(s.charAt(i - 1));
		}
		return sb.toString();
	}

	// Those 4 are used as key in the enum below.
	private static class EchoClass {
	}

	private static class ReverseClass {
	}

	private static class AllUpperClass {
	}

	private static class AllLowerClass {
	}

	public enum OlivType {

		TYPE_ONE(EchoClass.class, "Identical", EnumAndFunc::echo),
		TYPE_TWO(ReverseClass.class, "Reverse", EnumAndFunc::reverse),
		TYPE_THREE(AllUpperClass.class, "Upper", String::toUpperCase),
		TYPE_FOUR(AllLowerClass.class, "Lower", String::toLowerCase);

		private final Class id;
		private final String description;
		private final Function<String, String> fn;

		OlivType(Class id, String desc, Function<String, String> fn) {
			this.id = id;
			this.description = desc;
			this.fn = fn;
		}

		public Class id() {
			return this.id;
		}

		public String description() {
			return this.description;
		}

		public Function<String, String> fn() {
			return this.fn;
		}
	}

	public static void main(String... args) {
		String str = "AbCdEfGhIjKlMnOpQrStUvWxYz";
		System.out.println(String.format("%s: (%s) \t %s", OlivType.TYPE_ONE.id.getName(), OlivType.TYPE_ONE.description(), OlivType.TYPE_ONE.fn().apply(str)));
		System.out.println(String.format("%s: (%s) \t %s", OlivType.TYPE_TWO.id.getName(), OlivType.TYPE_TWO.description(), OlivType.TYPE_TWO.fn().apply(str)));
		System.out.println(String.format("%s: (%s) \t %s", OlivType.TYPE_THREE.id.getName(), OlivType.TYPE_THREE.description(), OlivType.TYPE_THREE.fn().apply(str)));
		System.out.println(String.format("%s: (%s) \t %s", OlivType.TYPE_FOUR.id.getName(), OlivType.TYPE_FOUR.description(), OlivType.TYPE_FOUR.fn().apply(str)));
	}
}
