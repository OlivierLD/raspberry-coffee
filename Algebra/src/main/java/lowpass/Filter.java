package lowpass;

public class Filter {
    public final static double ALPHA = 0.015D;

    public static double lowPass(double alpha, double value, double acc) {
        return (value * alpha) + (acc * (1 - alpha));
    }

}
