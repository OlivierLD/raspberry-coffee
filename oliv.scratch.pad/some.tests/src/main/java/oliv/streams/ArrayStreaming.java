package oliv.streams;

import java.util.Arrays;
import java.util.stream.Collectors;

public class ArrayStreaming {
	public static void main(String... args) {
		int[] array = { 1, 2, 3, 4, 5, 6 };

		Arrays.stream(array)
				.boxed()
				.forEach(obj -> {
					System.out.println(obj);
				});

		String str = Arrays.stream(array)
				.boxed()
				.map(String::valueOf)
				.collect(Collectors.joining(", "));
		System.out.println(str);
	}

}
