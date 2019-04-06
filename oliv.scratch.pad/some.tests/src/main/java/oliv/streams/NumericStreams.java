package oliv.streams;

import java.util.ArrayList;
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
		List<Double> doubleList = DoubleStream.iterate(0, n -> n + 0.1) // <- Aha!
				.limit(gusts.size())                                              // Cardinality
				.boxed()                                                          // Double <-> double
				.collect(Collectors.toList());
		doubleList.stream()
				.forEach(d -> System.out.println(String.format("%.03f", d)));
		double[] xData = doubleList.stream()
				.mapToDouble(Double::doubleValue)
				.toArray();
		System.out.println("Final double[]:");
		for (int i=0; i<xData.length; i++) {
			System.out.print(String.format("%03f ", xData[i]));
		}
		System.out.println();
	}
}
