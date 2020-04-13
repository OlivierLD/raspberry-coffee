package oliv.misc;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ReverseString {
	public static void main(String... arg) {
		String str = "Oracle";

		String reversed = IntStream.range(0, str.getBytes().length)
				.map(idx -> str.getBytes()[str.getBytes().length - (idx + 1)])
				.boxed()
				.map(b -> String.format("%c", b))
				.collect(Collectors.joining(""));

		System.out.println(String.format("%s becomes %s", str, reversed));
		// Another method
		System.out.println(String.format("%s becomes %s", str, new StringBuffer(str).reverse().toString()));

		IntStream.range(1, 29)
				.boxed()
				.map(idx -> String.format("<img src=\"images/phase%02d.gif\" id=\"phase-%02d\" />", idx, idx))
				.forEach(System.out::println);
	}
}
