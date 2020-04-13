package oliv.annotations.interceptors;

public class WithMethodInterceptor {

	@SecurityCheck(permission = "GoAhead")
	public String doSomething(String someString) {
		return new StringBuilder(someString).reverse().toString();
	}

	public static void main(String... args) {
		System.out.println("Let's go");
		WithMethodInterceptor interceptor = new WithMethodInterceptor();
		String upsideDown = interceptor.doSomething("The quick brown fox jumps over the lazy dog");
		System.out.println(String.format("=> %s", upsideDown));
		System.out.println("Done.");
	}
}
