package misc;

import org.junit.Test;

import static junit.framework.TestCase.assertTrue;
import pgcd.PGCD;
import ppcm.PPCM;

public class PgcdPpcm {

    @Test
    public void one() {
        int n1 = 60, n2 = 36;
        int pgcd = PGCD.pgcd(n1, n2);
        assertTrue(String.format("Expecting 12, got %d", pgcd), pgcd == 12);
    }
    @Test
    public void two() {
        int n1 = 355, n2 = 113;
        int pgcd = PGCD.pgcd(n1, n2);
        assertTrue(String.format("Expecting 1, got %d", pgcd), pgcd == 1);
    }
    @Test
    public void three() {
        int n1 = 355, n2 = 113;
        int ppcm = PPCM.ppcm(n1, n2);
        assertTrue(String.format("Expecting 40,115, got %d", ppcm), ppcm == 40_115);
    }
    @Test
    public void four() {
        int n1 = 60, n2 = 36;
        int ppcm = PPCM.ppcm(n1, n2);
        assertTrue(String.format("Expecting 180, got %d", ppcm), ppcm == 180);
    }

}
