package oliv.streams;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

public class NumericStreams {

	public static void main(String... args) {

		List<Double> gusts = new ArrayList<>();
		gusts.add(10D);
		gusts.add(20D);
		gusts.add(15D);
		gusts.add(13D);
		gusts.add(14D);
		gusts.add(100D);
		List<Double> doubleList = DoubleStream.iterate(0, n -> n + 0.1).limit(gusts.size()).boxed().collect(Collectors.toList());
		doubleList.stream().forEach(d -> System.out.println(String.valueOf(d)));
		//.toArray(new Double[gusts.size()]);

		doubleList.toArray();

	}
}
