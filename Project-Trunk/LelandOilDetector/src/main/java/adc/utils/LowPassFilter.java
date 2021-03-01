package adc.utils;

import java.util.ArrayList;
import java.util.List;

public class LowPassFilter {
    public static double[] lowPass(double[] input, double alfa) {
        if (alfa < 0 || alfa > 1) {
            throw new RuntimeException("alfa must be in [0..1]");
        }
        double[] output = new double[input.length];
        for (int i = 0; i < input.length; i++) {
          output[i] = input[i];
        }
        for (int i = 1; i < input.length; i++) {
          output[i] = (output[i - 1] + (alfa * (input[i] - output[i - 1])));
        }
        return output;
    }

    public static List<Double> lowPass(List<Double> input, double alfa) {
        if (alfa < 0 || alfa > 1) {
            throw new RuntimeException("alfa must be in [0..1]");
        }
        double[] output = new double[input.size()];
        for (int i = 0; i < input.size(); i++) {
          output[i] = input.get(i);
        }
        for (int i = 1; i < input.size(); i++) {
          output[i] = (output[i - 1] + (alfa * (input.get(i) - output[i - 1])));
        }
        List<Double> outList = new ArrayList<Double>(input.size());
        for (double d : output) {
          outList.add(d);
        }
        return outList;
    }

    public static List<Integer> lowPass2(List<Integer> input, double alfa) {
        if (alfa < 0 || alfa > 1) {
            throw new RuntimeException("alfa must be in [0..1]");
        }
        int[] output = new int[input.size()];
        for (int i = 0; i < input.size(); i++) {
          output[i] = input.get(i);
        }
        for (int i = 1; i < input.size(); i++) {
          output[i] = (int) Math.round(output[i - 1] + (alfa * (input.get(i) - output[i - 1])));
        }
        List<Integer> outList = new ArrayList<Integer>(input.size());
        for (int d : output) {
          outList.add(d);
        }
        return outList;
    }
}
