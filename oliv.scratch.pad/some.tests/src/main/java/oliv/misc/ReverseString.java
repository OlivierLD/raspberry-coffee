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
	}
}
