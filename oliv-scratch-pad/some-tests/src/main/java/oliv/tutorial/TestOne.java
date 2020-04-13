package oliv.tutorial;

public class TestOne {

	@FunctionalInterface
	interface Converter<F, T> {
		T convert(F from);
	}

	private static void doNothing() {
	}

	public static String DUMMY_CRAP = "Duh";
	protected String PROTECTED = "protected";
	private String PRIVATE = "private";

	public static void main(String[] argbs) {
		Converter<String, Integer> converter = (from) -> Integer.valueOf(from);
		Integer converted = converter.convert("123");
		System.out.println(converted);    // 123

		Converter<String, Integer> converter2 = Integer::valueOf;
		Integer converted2 = converter2.convert("123");
		System.out.println(converted2);   // 123
	}
}
