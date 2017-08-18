package oliv.streams;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Stream from a byte[]
 * Not 100% obvious
 */
public class ByteArrayStream {
	public static void main(String... args) {
		byte[] ba = new byte[]{
				0x00, 0x01, 0x03, 0x67
		};

		String concat = IntStream.range(0, ba.length) // This is a for (int i=0; i<ba.length; i++)
				.map(idx -> ba[idx])
				.boxed()
				.map(b -> String.format("%02X", (b & 0xFF)))
				.collect(Collectors.joining(" "));

		System.out.println(concat);
	}
}
