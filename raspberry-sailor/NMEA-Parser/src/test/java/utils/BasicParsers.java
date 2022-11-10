package utils;

import nmea.parser.StringGenerator;
import nmea.parser.StringParsers;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.fail;

public class BasicParsers {

    @Before
    public void init() {
//        System.setProperty("nmea.parser.verbose", "true");
    }

    @Test
    public void parseMTA_01() {
        String mta = "$BMMTA,21.4,C*0D\r\n";
        final boolean valid = StringParsers.validCheckSum(mta);
        if (valid) {
            final double value = StringParsers.parseMTA(mta);
            System.out.printf("MTA Value: %f\n", value);
        } else {
            System.out.println("Invalid sentence!");
            fail("Invalid sentence!");
        }
    }

    @Test
    public void parseMMB_01() {
        String mmb = "$BMMMB,30.1432,I,1.0207,B*75\r\n";
        final boolean valid = StringParsers.validCheckSum(mmb);
        if (valid) {
            final double value = StringParsers.parseMMB(mmb);
            System.out.printf("MMB Value: %f\n", value);
        } else {
            System.out.println("Invalid sentence!");
            fail("Invalid sentence!");
        }
    }

    @Test
    public void parseXDR_01() {
        String xdr = "$BMXDR,C,21.4,C,0,P,102067,P,1*5B\r\n";
        final boolean valid = StringParsers.validCheckSum(xdr);
        if (valid) {
            final List<StringGenerator.XDRElement> xdrElements = StringParsers.parseXDR(xdr);
            xdrElements.forEach(xdrEl -> System.out.printf("XDR: Sensor: %s, Type %s, Value: %f\n", xdrEl.getTransducerName(), xdrEl.getTypeNunit().type(), xdrEl.getValue()));
        } else {
            System.out.println("Invalid sentence!");
            fail("Invalid sentence!");
        }
    }
}
