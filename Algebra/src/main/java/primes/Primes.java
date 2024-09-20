package primes;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Decomposition into prime factors.
 */
public class Primes {

    public enum OutputOption {
        NONE, HTML, MARKDOWN, LATEX
    }

    private final static boolean VERBOSE = "true".equals(System.getProperty("primes.verbose"));

    private static void addToMap(Map<Integer, Integer> primeMap, int n) {
        primeMap.merge(n, 1, Integer::sum);
    }

    /**
     * The main method. Does the actual decomposition.
     *
     * @param n The number to decompose
     * @return The Map holding the decomposition:
     *          key: the prime number
     *          value: its power
     *
     * Example:
     * - n = 315
     * - returns {3=2, 5=1, 7=1}
     * To be read : 315 = 3^2 * 5^1 * 7^1
     */
    public static Map<Integer, Integer> primeFactors(int n) {
        Map<Integer, Integer> primeMap = new HashMap<>();

        // Print the number of 2s that divides n
        while (n % 2 == 0) {
            if (VERBOSE) {
                System.out.print(2 + " ");
            }
            addToMap(primeMap, 2);
            n /= 2;
        }

        // n must be odd at this point.  So we can skip one element (Note i = i + 2)
        for (int i = 3; i <= Math.sqrt(n); i += 2) {
            // While i divides n, print i and divide n
            while (n % i == 0) {
                if (VERBOSE) {
                    System.out.print(i + " ");
                }
                addToMap(primeMap, i);
                n /= i;
            }
        }

        // This condition is to handle the case when n is a prime number greater than 2
        if (n > 2) {
            if (VERBOSE) {
                System.out.print(n);
            }
            addToMap(primeMap, n);
        }
        if (VERBOSE) {
            System.out.println();
        }
        return primeMap;
    }

    public static String spitMapOut(int value, Map<Integer, Integer> primeMap) {
        return spitMapOut(value, primeMap, OutputOption.NONE);
    }
    public static String spitMapOut(int value, Map<Integer, Integer> primeMap, OutputOption outputOption) {
        final String collected = primeMap.keySet()
                .stream().map(prime -> {
                    String output;
                    switch (outputOption) {
                        case MARKDOWN:
                        case HTML:
                            output = String.format("(%d<sup>%d</sup>)", prime, primeMap.get(prime));
                            break;
                        case LATEX:
                            output = String.format("(%d^{%d})", prime, primeMap.get(prime));
                            break;
                        case NONE:
                        default:
                            output = String.format("(%d^%d)", prime, primeMap.get(prime));
                    }
                    return output;
                })
                .collect(Collectors.joining((outputOption == OutputOption.MARKDOWN || outputOption == OutputOption.HTML) ?
                        " &times; " :
                        (outputOption == OutputOption.LATEX ? " \\times " : " x ")));
        String finalResult;
        switch (outputOption) {
            case MARKDOWN:
                finalResult = String.format("`%s = %s`", NumberFormat.getInstance().format(value) ,collected);
                break;
            case HTML:
                finalResult = String.format("<code>%s = %s</code>", NumberFormat.getInstance().format(value) ,collected);
                break;
            case LATEX:
                finalResult = String.format("$ %s = %s $", NumberFormat.getInstance().format(value) ,collected);
                break;
            case NONE:
            default:
                finalResult = String.format("%s = %s", NumberFormat.getInstance().format(value) ,collected);
        }
        return finalResult;
    }

    // For tests
    public static void main(String... args) {
        int n = 315;
        System.out.println(spitMapOut(n, primeFactors(n)));
        n = 144;
        System.out.println(spitMapOut(n, primeFactors(n), OutputOption.MARKDOWN));
        n = 2_205_000;
        System.out.println(spitMapOut(n, primeFactors(n), OutputOption.LATEX));
    }
}
