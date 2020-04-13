package oliv.streams;

import java.util.stream.IntStream;

public class ImmutableLoop {
	public static void main(String... args) {

		IntStream.range(0, 10)
				.forEach(idx -> {
					System.out.println(String.format("idx:%d", idx));
				});
	}
}
