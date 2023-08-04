package gauss;

import java.security.InvalidParameterException;

/**
 * See https://fr.wikipedia.org/wiki/Fonction_gaussienne,
 *     http://apmep.poitiers.free.fr/IMG/pdf/LA_COURBE_DE_GAUSS.pdf
 */
public class GaussCurve {

    public static double gauss_v1(double mu, double sigma, double x) throws InvalidParameterException {
        return gauss(mu, sigma, 1.0, x);
    }
    public static double gauss_v1(double mu, double sigma, double height, double x) throws InvalidParameterException {
        if (sigma == 0) {
            throw new InvalidParameterException("Sigma should not be equal to zero.");
        }
        return height * (1d / (sigma * Math.sqrt(2 * Math.PI))) * Math.exp(- Math.pow((x - mu) / sigma, 2d) / 2);
    }

    /**
     * As seen in https://www.google.com/search?q=equation+d%27une+courbe+de+gauss&sxsrf=AB5stBjSlrM7084Vxzr9bSHIBquuek-crA%3A1690993878670&ei=1oTKZIS0KIbpkdUPkPO8oAo&ved=0ahUKEwiE99GLs76AAxWGdKQEHZA5D6QQ4dUDCA8&uact=5&oq=equation+d%27une+courbe+de+gauss&gs_lp=Egxnd3Mtd2l6LXNlcnAiHmVxdWF0aW9uIGQndW5lIGNvdXJiZSBkZSBnYXVzczIEECMYJ0iEGlDoCFihFHABeAGQAQCYAVOgAaMCqgEBNLgBA8gBAPgBAcICChAAGEcY1gQYsAPCAgcQIxiwAhgn4gMEGAAgQYgGAZAGCA&sclient=gws-wiz-serp
     *
     * @param curvePeak curve peak value
     * @param peakAbs   curve peak abscissa
     * @param stDev     standard deviation
     * @param x         abscissa
     * @return ordinate
     */
    public static double gauss(double curvePeak, double peakAbs, double stDev, double x) {
        return curvePeak * Math.exp(-(x - peakAbs)*(x - peakAbs)/(2 * stDev * stDev));
    }
    public static void main(String... args) {
        double mu = 45.0;
        double sigma = 10.0;
        double height = 250;
        double y = gauss_v1(mu, sigma, height, 45);
        System.out.printf("f_v1(45) = %f\n", y);

        mu = 45.0;
        sigma = 15;
        height = 400.0;
        y = gauss_v1(mu, sigma, height, 45);
        System.out.printf("f_v1(45) = %f\n", y);
        y = gauss_v1(mu, sigma, height, 25);
        System.out.printf("f_v1(25) = %f\n", y);

        double a = 10d;
        double b = 45d;
        double c = 5d;
        y = gauss(a, b, c, 45);
        System.out.printf("f(45) = %f\n", y);
        y = gauss(a, b, c, 25);
        System.out.printf("f(25) = %f\n", y);
        y = gauss(a, b, c, 65);
        System.out.printf("f(65) = %f\n", y);
    }
}
