package primes;

public class PPCM {

    /**
     * LCM  - Lowest Common Multiple. PPCM is the same, in French.
     * @param n1
     * @param n2
     * @return
     */
    public static int ppcm(int n1, int n2) {
        int product = n1 * n2;
        int remainder = n1 % n2;
        while (remainder != 0) {
            n1 = n2;
            n2 = remainder;
            remainder = n1 % n2;
        }
        return product / n2;
    }

    // For tests
    public static void main(String... args) {
        int n1 = 60, n2 = 36;
        System.out.printf("PPCM(%d, %d) = %d\n", n1, n2, ppcm(n1, n2));
        n1 = 355; n2 = 113;
        System.out.printf("PPCM(%d, %d) = %d\n", n1, n2, ppcm(n1, n2));
    }
}
