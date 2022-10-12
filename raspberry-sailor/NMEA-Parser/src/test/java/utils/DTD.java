package utils;

import nmea.parser.StringParsers;
import org.junit.Test;

import static org.junit.Assert.fail;

public class DTD {

    @Test
    public void testDurationToDate() {
        String duration_01 = "2022-10-12T07:30:07";
        String duration_02 = "2022-10-12T07:30:07.123";
        try {
            final long l1 = StringParsers.durationToDate(duration_01);
            final long l2 = StringParsers.durationToDate(duration_02);
            System.out.printf("L1: %d, L2: %d\n", l1, l2);
        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }
}
