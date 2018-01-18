package jnisample;

public class HelloWorld {

	private static class SomeObject {
		private String name;
		private int someNumber;

		public SomeObject(String name, int num) {
			this.name = name;
			this.someNumber = num;
		}
	}

	// 'native' tells javah to build the stub
	private native void print();
	private native int manageObject(SomeObject so);

	public static void main(String... args) {
		HelloWorld hw = new HelloWorld();
		hw.print();
		hw.manageObject(new SomeObject("Oliv", 1));
	}

	static {
		System.loadLibrary("HelloWorld");
	}
}
