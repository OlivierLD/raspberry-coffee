package oliv.ca;

import java.util.Arrays;
import java.util.stream.Collectors;

public class ruleGenerator {

	public static int[] getRule(int value) {
		if (value < 0 || value > 255) {
			throw new RuntimeException("[0..255], please");
		}
		int[] array = new int[8];
		for (int i=0; i<8; i++) {
			array[i] = (value & (1 << (7 - i))) == 0 ? 0 : 1;
		}
		return array;
	}

	public static void main(String... args) {
		int val = 234;
		int[] rule = getRule(val);
		String strRule = Arrays.stream(rule)
				.boxed()
				.map(String::valueOf)
				.collect(Collectors.joining(", "));

		System.out.println(String.format("%d = %s => %s", val, Integer.toBinaryString(val), strRule));
	}
}
