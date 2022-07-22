package misc;

import org.junit.Test;

import static junit.framework.TestCase.assertEquals;
import primes.PGCD;
import primes.PPCM;

public class PgcdAnPpcmTest {

    @Test
    public void one() {
        int n1 = 60, n2 = 36;
        int pgcd = PGCD.pgcd(n1, n2);
        assertEquals(String.format("Expecting 12, got %d", pgcd), 12, pgcd);
    }
    @Test
    public void two() {
        int n1 = 355, n2 = 113;
        int pgcd = PGCD.pgcd(n1, n2);
        assertEquals(String.format("Expecting 1, got %d", pgcd), 1, pgcd);
    }
    @Test
    public void three() {
        int n1 = 355, n2 = 113;
        int ppcm = PPCM.ppcm(n1, n2);
        assertEquals(String.format("Expecting 40,115, got %d", ppcm), 40_115, ppcm);
    }
    @Test
    public void four() {
        int n1 = 60, n2 = 36;
        int ppcm = PPCM.ppcm(n1, n2);
        assertEquals(String.format("Expecting 180, got %d", ppcm), 180, ppcm);
    }

}
