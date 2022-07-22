package primes;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class Primes {

    private final static boolean VERBOSE = "true".equals(System.getProperty("primes.verbose"));

    private static void addToMap(Map<Integer, Integer> primeMap, int n) {
        primeMap.merge(n, 1, Integer::sum);
    }

    public static Map<Integer, Integer> primeFactors(int n) {
        Map<Integer, Integer> primeMap = new HashMap<>();

        // Print the number of 2s that divide n
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
        final String collected = primeMap.keySet().stream().map(prime -> String.format("(%d^%d)", prime, primeMap.get(prime))).collect(Collectors.joining(" x "));
        return String.format("%s = %s", NumberFormat.getInstance().format(value) ,collected);
    }

    // For tests
    public static void main(String... args) {
        int n = 315;
        System.out.println(spitMapOut(n, primeFactors(n)));
        n = 144;
        System.out.println(spitMapOut(n, primeFactors(n)));
        n = 2_205_000;
        System.out.println(spitMapOut(n, primeFactors(n)));
    }
}
