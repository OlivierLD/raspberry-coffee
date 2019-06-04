package ai;

import java.util.Arrays;
import java.util.List;

public class ArtificialNeuron {
	private double bias = 0;
	private List<Double> weights;

	public ArtificialNeuron(List<Double> weights, double bias) {
		this.weights = weights;
		this.bias = bias;
	}

	public double compute(List<Double> xValues) throws Exception {
		if (xValues.size() != this.weights.size()) {
			throw new RuntimeException("Size mismatch");
		}
		double yValue = 0;
		for (int i=0; i<xValues.size(); i++) {
			yValue += (xValues.get(i) * this.weights.get(i));
		}
		yValue += this.bias;
		return yValue;
	}

	public static void main(String... args) {
		ArtificialNeuron neuron = new ArtificialNeuron( Arrays.asList(new Double[] { 1.5, -2d, 1d }), 6d);
		try {
			double y = neuron.compute(Arrays.asList(new Double[]{10d, 6d, 8d}));
			System.out.println(String.format("=> y = %f", y));
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
