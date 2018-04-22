package oliv.nesting;

import java.util.ArrayList;
import java.util.List;

/**
 * From array to list
 */
public class NestingTest {
	public static void main(String... args) {
		System.out.println("Let's go");

		Wrapper wrap = new Wrapper(new String[]{"A", "B", "C"});
		wrap.crack();
		wrap.getResult().forEach(System.out::println);
	}

	private abstract static class ClassOne {
		public abstract void work(String stuff);
	}

	private static class Wrapper {
		private String[] args;
		private final List<String> result;

		public Wrapper(String[] things) {
			this.args = things;
			result = new ArrayList<>();
		}

		public void crack() {
			ClassOne c1Impl = new ClassOne() {
				public void work(String s) {
					result.add(s);
				}
			};
			for (String s : args) {
				c1Impl.work(s);
			}
		}

		public List<String> getResult() {
			return result;
		}
	}
}
