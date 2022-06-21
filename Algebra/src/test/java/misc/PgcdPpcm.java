package misc;

import static pgcd.PGCD.pgcd;
import static ppcm.PPCM.ppcm;

public class PgcdPpcm {

    public final static void main(String... args) {
        int n1 = 60, n2 = 36;
        System.out.printf("PGCD(%d, %d) = %d\n", n1, n2, pgcd(n1, n2));
        n1 = 355; n2 = 113;
        System.out.printf("PGCD(%d, %d) = %d\n", n1, n2, pgcd(n1, n2));
        n1 = 60; n2 = 36;
        System.out.printf("PPCM(%d, %d) = %d\n", n1, n2, ppcm(n1, n2));
        n1 = 355; n2 = 113;
        System.out.printf("PPCM(%d, %d) = %d\n", n1, n2, ppcm(n1, n2));
    }
}
