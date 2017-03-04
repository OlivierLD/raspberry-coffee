package jnisample;

public class HelloWorld {
	private native void print(); // Tells javah to build the stub

	public static void main(String[] args) {
		new HelloWorld().print();
	}

	static {
		System.loadLibrary("HelloWorld");
	}
}