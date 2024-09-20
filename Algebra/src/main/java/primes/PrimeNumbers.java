package primes;

public class PrimeNumbers {

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
                System.out.println(". " + i);  // Output the prime number
            }
        }
        System.out.printf("Found %d prime numbers below %d\n", nbPrime, num);
    }
}
