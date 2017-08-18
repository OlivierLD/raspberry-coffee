package oliv.func;

import java.util.function.Consumer;

public class ConsumerOne {

	enum ConsumerList {
		ONE("one", ConsumerOne::optionOne),
		TWO("two", ConsumerOne::optionTwo);

		private String label;
		private Consumer<String> consumer;

		ConsumerList(String label, Consumer<String> consumer) {
			this.label = label;
			this.consumer = consumer;
		}

		public String label() {
			return this.label;
		}

		public Consumer<String> consumer() {
			return this.consumer;
		}
	}

	private static void optionOne(String str) {
		System.out.println(String.format("Option One: %s!", str));
	}

	private static void optionTwo(String str) {
		System.out.println(String.format("Option Two: %s?", str));
	}

	public static void main(String... args) {
		String str = "kwak";

		ConsumerList.ONE.consumer().accept(str);
		ConsumerList.TWO.consumer().accept(str);
	}
}
