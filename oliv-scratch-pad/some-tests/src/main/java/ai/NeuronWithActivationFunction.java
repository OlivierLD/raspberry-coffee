package ai;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

public class NeuronWithActivationFunction extends ArtificialNeuron {
	public NeuronWithActivationFunction(List<Double> weights, double bias) {
		super(weights, bias);
	}

	public double compute(List<Double> xValues, Function<Double, Double> activation) throws Exception {
		double y = super.compute(xValues);
		// Activation function here
		return activation.apply(y);
	}

	public static void main(String... args) {
		NeuronWithActivationFunction neuron = new NeuronWithActivationFunction(Arrays.asList(new Double[] { 1.5, -2d, 1d }), 6d);
		try {
			double y = neuron.compute(Arrays.asList(new Double[]{ 10d, 6d, 8d }), Math::tanh);
			System.out.println(String.format("=> y = %f", y));
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
