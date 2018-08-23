package oliv.dynamic;

public class Loading {

	private static class ClassOne {
		private int arg;

		public ClassOne(Integer i) {
			this.arg = i;
		}

		public String toString() {
			return String.valueOf(this.arg);
		}
	}

	private static class ClassTwo {
		private int arg;

		public ClassTwo(Integer i) {
			this.arg = i;
		}

		public String toString() {
			return String.valueOf(this.arg);
		}
	}

	public static void main(String... args) {
		String one = "oliv.dynamic.Loading$ClassOne";

		Object objOne = new ClassOne(1);
		System.out.println(String.format("It's a %s", objOne.getClass().getName()));

		try {
			ClassOne dynOne = (ClassOne)Class.forName(one).getConstructor(Integer.class).newInstance(1);
			System.out.println("=> " + dynOne.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
		try { // Invalid cast
			ClassTwo dynTwo = (ClassTwo)Class.forName(one).getConstructor(Integer.class).newInstance(1);
			System.out.println("=> " + dynTwo.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("Done.");
	}
}
