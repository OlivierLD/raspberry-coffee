package encryption;

public class Basics02 {

    /**
     * This is a "one way function" (fonction a sens unique)
     * @param base
     * @param mod
     * @param x
     * @return
     */
    public static long powXmodY(long base, long mod, long x) {
        return (long)(Math.pow(base, x) % mod);
    }

    private static void page328() {
        // Page 328
        long base = 3;
        long mod = 7;
        for (int i=1; i<=6; i++) {
            System.out.printf("%d ^ %d mod %d: %d\n", base, i, mod, powXmodY(base, mod, i));
        }
    }

    private static void page329() {
        // Page 329
        long aliceNumber = 3;
        long bernardNumber = 6;

        long base = 7;
        long mod = 11;

        long alpha = powXmodY(base, mod, aliceNumber);
        long beta  = powXmodY(base, mod, bernardNumber);

        System.out.printf("Step 2 : Alice %d, Bernard %d\n", alpha, beta);

        // Alice sends alpha to Bernard
        // Bernard sends beta to Alice

        // What Alice and Bernard have to agree on is the couple (base, mod), and the way to use them (powXmodY)

        long aliceCalcStep4 = powXmodY(beta, mod, aliceNumber);
        long bernardCalcStep4 = powXmodY(alpha, mod, bernardNumber);

        System.out.printf("Key should be (the same), for Alice %d, for Bernard %d.\n", aliceCalcStep4, bernardCalcStep4);
    }
    public static void main(String[] args) {

        page328();
        System.out.println("---");
        page329();

    }
}
