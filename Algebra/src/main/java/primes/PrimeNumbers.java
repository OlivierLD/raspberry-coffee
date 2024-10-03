package primes;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PrimeNumbers {

    public static boolean isPrime(long num) {
        boolean isPrime = false;
        int count = 0;
        // Check for divisibility from 2 up to i/2
        for (int j = 2; j <= num / 2; j++) {
            if (num % j == 0) {
                count++;  // Increment if 'num' is divisible by 'j'
                break;    // Exit loop if a divisor is found. We don't need more than one.
            }
        }
        // If the count is 0, 'i' is prime
        if (count == 0) {
            isPrime = true;
        }
        return isPrime;
    }

    /**
     * Get the first nb prime numbers
     * @param nb
     * @return
     */
    public static List<Long> getPrimes(int nb) {
        List<Long> primeList = new ArrayList<>();
        int howMany = 0;
        int i = 1;
        while (howMany < nb) {
            int count = 0;  // Reset counter for each 'i'
            // Check for divisibility from 2 up to i/2
            for (int j = 2; j <= i / 2; j++) {
                if (i % j == 0) {
                    count++;  // Increment if 'i' is divisible by 'j'
                    break;    // Exit loop if a divisor is found
                }
            }
            // If the count is 0, 'i' is prime
            if (count == 0) {
                howMany++;
                primeList.add((long)i);  // Add the prime number to the list
            }
            i++;
        }
        return primeList;
    }
    /*
     * Spits out all the prime numbers, below 'n'.
     */
    public static void main(String... args) {
        int num = 3_000;  // Define the upper limit (n)
        int count;        // Initialize counter for divisibility checks
        int nbPrime = 0;

        // Iterate from 1 up to 'num' to identify prime numbers
        for (int i = 1; i <= num; i++) {
            count = 0;  // Reset counter for each 'i'
            // Check for divisibility from 2 up to i/2
            for (int j = 2; j <= i / 2; j++) {
                if (i % j == 0) {
                    count++;  // Increment if 'i' is divisible by 'j'
                    break;    // Exit loop if a divisor is found
                }
            }
            // If the count is 0, 'i' is prime
            if (count == 0) {
                nbPrime++;
                System.out.printf("#%d . %d\n", nbPrime, i);  // Output the prime number
            }
        }
        System.out.printf("Found %d prime numbers below %d\n", nbPrime, num);

        final List<Long> primes = getPrimes(500);
        System.out.printf("500 first primes: %s\n", primes.stream().map(l -> l.toString()).collect(Collectors.joining(", ")));

        System.out.printf("Is 499 prime ? %b\n", isPrime(499));
        System.out.printf("Is 1789 prime ? %b\n", isPrime(1789));

        List<Long> list = List.of(499L, 1789L, 437L);
        list.forEach(n -> System.out.printf("Is %d prime ? %b\n", n, isPrime(n)));
    }
}
