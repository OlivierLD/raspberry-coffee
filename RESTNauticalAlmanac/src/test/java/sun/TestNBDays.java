package sun;

import implementation.almanac.AlmanacComputer;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestNBDays {

    @Test
    public void testNBDays() {
        int nbDays = AlmanacComputer.getNbDays(2020, 2); // Leap year
        assertEquals("2020 is a leap year", 29, nbDays);
        nbDays =  AlmanacComputer.getNbDays(2000, 2); // NOT Leap year
        assertEquals("2000 is NOT a leap year", 28, nbDays);
    }
}
