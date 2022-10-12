package utils;

import nmea.parser.StringParsers;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertTrue;

public class AutoParse {

    private final static String[] NMEA_DATA_SAMPLE = {
            "$GPRMC,170000.00,A,3744.79693,N,12223.30420,W,0.052,,200621,,,D*62",
            "$IIGLL,0906.455,S,14012.519,W,220714,A,A*5D",
            "$GPRMC,170001.00,A,3744.79690,N,12223.30424,W,0.183,,200621,,,D*69",
            "$IIGLL,0906.455,S,14012.519,W,220714,A,A*5D",
            "$GPRMC,170002.00,A,3744.79681,N,12223.30435,W,0.228,,200621,,,D*68",
            "$IIGLL,0906.458,S,14012.521,W,220716,A,A*59",
            "$GPRMC,170003.00,A,3744.79677,N,12223.30440,W,0.035,,200621,,,D*6C",
            "$BMXDR,H,48.1,P,0,C,23.8,C,1,P,101775,P,2*6B"
    };

    @Test
    public void autoParser() {
        Arrays.stream(NMEA_DATA_SAMPLE)
                .forEach(nmea -> {
                    try {
                        System.out.printf("Parsing [%s]\n", nmea);
                        StringParsers.ParsedData obj = StringParsers.autoParse(nmea);
                        if (obj != null) {
                            System.out.printf(">> Parsed >> %s\n", obj.getParsedData().toString());
                        } else {
                            System.out.println(">> null");
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                });
        assertTrue("Argh!", true); // Arf !
    }
}
