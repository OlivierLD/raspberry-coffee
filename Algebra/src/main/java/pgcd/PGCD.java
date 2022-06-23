package pgcd;

public class PGCD {

    public static int pgcd(int n1, int n2) {
        while (n1 != n2) {
            if (n1 > n2) {
                n1 -= n2;
            } else {
                n2 -= n1;
            }
        }
        return n2;
    }

    public static void main(String... args) {
        int n1 = 60, n2 = 36;
        System.out.printf("PGCD(%d, %d) = %d\n", n1, n2, pgcd(n1, n2));
        n1 = 355; n2 = 113;
        System.out.printf("PGCD(%d, %d) = %d\n", n1, n2, pgcd(n1, n2));
    }
}
