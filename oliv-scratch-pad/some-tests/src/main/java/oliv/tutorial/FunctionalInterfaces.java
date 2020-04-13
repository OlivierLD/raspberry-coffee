package oliv.tutorial;

public class FunctionalInterfaces {

	@FunctionalInterface
	interface Converter<F, T> {
		T convert(F from);
	}

	@FunctionalInterface
	interface ProcessorTypeOne<A, B, C> {
		void process(A a, B b, C c);
	}

	private static Converter<String, Integer> converterOne = (from) -> Integer.valueOf(from);
	private static ProcessorTypeOne<String, String, String> converterTwo = FunctionalInterfaces::subProcessor;

	private ProcessorTypeOne<String, String, String> converterThree;

	public FunctionalInterfaces() {
		converterThree = this::instanceProcessor;
	}

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

	public void instanceProcessor(String a, String b, String c) {
		System.out.println(String.format("%s %s %s", reverse(a), reverse(b), reverse(c)));
	}

	public static void main(String... args) {

		InterfaceEnum one = InterfaceEnum.ONE;
		System.out.println("For " + one.interfaceName());
		Functional funcOne = one.processor();
		Object coreOne = funcOne.getCore();
		System.out.println("Core is a " + coreOne.getClass().getName());
		System.out.println(String.format("Returned %d", ((Converter) coreOne).convert("7654")));

		InterfaceEnum two = InterfaceEnum.TWO;
		System.out.println("For " + two.interfaceName());
		Functional funcTwo = two.processor();
		Object coreTwo = funcTwo.getCore();
		System.out.println("Core is a " + coreTwo.getClass().getName());
		((ProcessorTypeOne) coreTwo).process("ABCD", "KAYAK", "1234"); // Returns void
	}
}
