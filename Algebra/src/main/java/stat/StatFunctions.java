package stat;

import java.util.Arrays;
import java.util.List;

/**
 * Good resources at https://www.scribbr.com/statistics/standard-deviation/
 * Also see https://www.calculator.net/standard-deviation-calculator.html
 */
public class StatFunctions {

    /**
     * Mean
     * @param dataset array of doubles
     * @return the mean
     */
    public static double mean(double... dataset) {
        int size = dataset.length; // Arrays.stream(dataset).count();
        double sum = Arrays.stream(dataset).sum();
        return (sum / size);
    }
    public static double mean(List<Double> dataset) {
        return dataset.stream().mapToDouble(Double::doubleValue).average().getAsDouble();
    }

    private static double deviationFromMean(double score, double mean) {
        return score - mean;
    }

    /**
     * Variance
     * @param dataset array of doubles
     * @return the variance
     *
     * TODO The sample for population/sample (length, length - 1)
     */
    public static double variance(double... dataset) {
        double mean = mean(dataset);
        double sqSum = Arrays.stream(dataset).map(x -> Math.pow(deviationFromMean(x, mean), 2)).sum();
        return sqSum / (dataset.length - 1);
    }

    public static double variance(List<Double> dataset) {
        double mean = mean(dataset);
        double sqSum = dataset.stream().mapToDouble(x -> Math.pow(deviationFromMean(x, mean), 2)).sum();
        return sqSum / (dataset.size() - 1);
    }

    /**
     * Standard Deviation
     * @param dataset array of doubles
     * @return the standard deviation
     */
//    public static double standardDeviation(double[] dataset) {
//        return Math.sqrt(variance(dataset));
//    }
    public static double standardDeviation(double... dataset) {
        return Math.sqrt(variance(dataset));
    }
    public static double standardDeviation(List<Double> dataset) {
        return Math.sqrt(variance(dataset));
    }
    /**
     * For tests. Use --details as CLI parameter to see more details
     *
     * @param args See in the code below (--details)
     */
    public static void main(String... args) {
        double[] dataset = new double[] { 46, 69, 32, 60, 52, 41 };
                                        // { 10, 12, 23, 23, 16, 23, 21, 16 };
        boolean details = false;
        for (String arg : args) {
            if ("--details".equals(arg)) {
                details = true;
            }
        }
        if (details) {
            double mean = mean(dataset);
            System.out.printf("Mean: %s%n", mean);
            Arrays.stream(dataset).forEach(x -> System.out.printf("%.03f, dev: %f%n", x, deviationFromMean(x, mean)));
            System.out.println("--------------------------------");
            Arrays.stream(dataset).forEach(x -> System.out.printf("%.03f, sqr-dev: %f%n", x, Math.pow(deviationFromMean(x, mean), 2)));
            System.out.println("--------------------------------");
            double sqSum = Arrays.stream(dataset).map(x -> Math.pow(deviationFromMean(x, mean), 2)).sum();
            System.out.printf("Square Sum: %f%n", sqSum);
            double variance = sqSum / (dataset.length - 1);
            System.out.printf("Variance: %f%n", variance);
            System.out.printf("StdDev: %f%n", Math.sqrt(variance));
        }
        // StdDev
        System.out.printf("1 - Direct Std-Dev: %f%n", standardDeviation(dataset));
        System.out.printf("2 - Direct Std-Dev: %f%n", standardDeviation(46, 69, 32, 60, 52, 41));
        System.out.printf("3 - Direct Std-Dev: %f%n", standardDeviation(List.of(46d, 69d, 32d, 60d, 52d, 41d)));

    }
}
