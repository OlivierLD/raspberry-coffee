package oliv.tutorial;

public class FunctionalInterfacesBigger {

	@FunctionalInterface
	interface Converter<F, T> {
		T convert(F from);
	}

	@FunctionalInterface
	interface ProcessorTypeOne<A, B, C> {
		void process(A a, B b, C c);
	}

	private static Converter<String, Integer> converterOne = (from) -> Integer.valueOf(from);
	private static ProcessorTypeOne<String, String, String> converterTwo = FunctionalInterfacesBigger::subProcessor;

	private static class Functional<T> {
		private T core;

		public Functional(T core) {
			this.core = core;
		}

		public T getCore() {
			return this.core;
		}
	}

	private enum InterfaceEnum {

		ONE("One", new Functional(converterOne)),
		TWO("Two", new Functional(converterTwo));

		private final String interfaceName;
		private final Functional processor;

		InterfaceEnum(String name, Functional func) {
			this.interfaceName = name;
			this.processor = func;
		}

		public String interfaceName() {
			return this.interfaceName;
		}

		public Functional processor() {
			return this.processor;
		}
	}

	;

	public static String reverse(String s) {
		String reversed = "";

		for (int i = s.length() - 1; i >= 0; i--) {
			reversed += s.charAt(i);
		}
		return reversed;
	}

	public static void subProcessor(String a, String b, String c) {
		System.out.println(String.format("%s %s %s", reverse(a), reverse(b), reverse(c)));
	}

	public static void main(String... args) {
		Converter<String, Integer> converter = (from) -> Integer.valueOf(from);

		Integer converted = converter.convert("1234");
		System.out.println(String.format("Converted -> %d", converted));

		Converter<String, Integer> converterTwo = Integer::parseInt;
		converted = converterTwo.convert("2345");
		System.out.println(String.format("Converted -> %d", converted));

		ProcessorTypeOne<String, String, String> processor = (one, two, three) -> {
			System.out.println(String.format("%s %s %s", one, two, three));
		};

		processor.process("Akeu", "Coucou", "Larigou");

		ProcessorTypeOne<String, String, String> procTwo = FunctionalInterfacesBigger::subProcessor;
		procTwo.process("Akeu", "Coucou", "Larigou");

		System.out.println("procTwo is a " + procTwo.getClass().getName());

		{
			InterfaceEnum one = InterfaceEnum.ONE;
			System.out.println("For " + one.interfaceName());
			Functional func = one.processor();
			Object core = func.getCore();
			System.out.println("Core is a " + core.getClass().getName());
			System.out.println(String.format("Returned %d", ((Converter) core).convert("7654")));
		}
		{
			InterfaceEnum two = InterfaceEnum.TWO;
			System.out.println("For " + two.interfaceName());
			Functional func = two.processor();
			Object core = func.getCore();
			System.out.println("Core is a " + core.getClass().getName());
			((ProcessorTypeOne) core).process("ABCD", "KAYAK", "1234"); // Returns void
		}
		String palindrome = "ESOPE RESTE ELU PAR CETTE CRAPULE ET SE REPOSE";
		System.out.println(String.format("Reversing %s", palindrome));
		System.out.println(String.format("Reversed: %s", reverse(palindrome)));
	}
}
