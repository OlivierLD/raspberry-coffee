package nmea.parser;

import java.util.Date;

public class StringGeneratorTest {
    public static void main(String... args) {
        String rmc = StringGenerator.generateRMC("II", new Date(), 38.2500, -122.5, 6.7, 210, 3d);
        System.out.println("Generated RMC:" + rmc);

        if (StringParsers.validCheckSum(rmc))
            System.out.println("Valid!");
        else
            System.out.println("Invalid...");

        String mwv = StringGenerator.generateMWV("II", 23.45, 110);
        System.out.println("Generated MWV:" + mwv);

        if (StringParsers.validCheckSum(mwv))
            System.out.println("Valid!");
        else
            System.out.println("Invalid...");

        String vhw = StringGenerator.generateVHW("II", 8.5, 110);
        System.out.println("Generated VHW:" + vhw);

        if (StringParsers.validCheckSum(vhw))
            System.out.println("Valid!");
        else
            System.out.println("Invalid...");

        String mmb = StringGenerator.generateMMB("II", 1013.6);
        System.out.println("Generated MMB:" + mmb);

        String mta = StringGenerator.generateMTA("II", 20.5);
        System.out.println("Generated MTA:" + mta);

        String xdr = StringGenerator.generateXDR("II", new StringGenerator.XDRElement(StringGenerator.XDRTypes.PRESSURE_B, 1.0136, "BMP180"));
        System.out.println("Generated XDR:" + xdr);
        xdr = StringGenerator.generateXDR("II",
                new StringGenerator.XDRElement(StringGenerator.XDRTypes.PRESSURE_B, 1.0136, "BMP180"),
                new StringGenerator.XDRElement(StringGenerator.XDRTypes.TEMPERATURE, 15.5, "BMP180"),
                new StringGenerator.XDRElement(StringGenerator.XDRTypes.HUMIDITY, 65.5, "HTU21DF"),
                new StringGenerator.XDRElement(StringGenerator.XDRTypes.GENERIC, 0.014270, "PRATE"));
        System.out.println("Generated XDR:" + xdr);

        xdr = StringGenerator.generateXDR("XX", new StringGenerator.XDRElement(StringGenerator.XDRTypes.VOLTAGE, 12.34, "TRINKET"));
        System.out.println("Generated XDR:" + xdr);

        System.out.println("Generating MDA...");
        String mda = StringGenerator.generateMDA("II", 1013.25, // PRMSL
                25,    // AIR TEMP
                12,    // WATER TEMP
                75,    // REL HUMIDITY
                50,    // ABS HUMIDITY
                9,    // DEW POINT (CELCIUS)
                270,    // TWD
                255,    // WIND DIR (MAG)
                12.0); // TWS
        if (StringParsers.validCheckSum(mda))
            System.out.println("Valid!");
        else
            System.out.println("Invalid...");
        System.out.println("Generated MDA:" + mda);
        mda = StringGenerator.generateMDA("II", 1013.25, // PRMSL
                25,    // AIR TEMP
                12,    // WATER TEMP
                75,    // REL HUMIDITY
                50,    // ABS HUMIDITY
                -Double.MAX_VALUE,    // DEW POINT (CELCIUS)
                270,    // TWD
                255,    // WIND DIR (MAG)
                12.0); // TWS
        if (StringParsers.validCheckSum(mda))
            System.out.println("Valid!");
        else
            System.out.println("Invalid...");
        System.out.println("Generated MDA:" + mda);

        double noValue = -Double.MAX_VALUE;
        mda = StringGenerator.generateMDA("WI", 1009, 31.7, noValue, noValue, noValue, noValue, 82.3, 72.3, 7.4);
        if (StringParsers.validCheckSum(mda))
            System.out.println("Valid!");
        else
            System.out.println("Invalid...");
        System.out.println("Generated MDA:" + mda);

        System.out.println("Another one...");
        mda = "$WIMDA,29.796,I,1.009,B,31.7,C,,,,,,,82.3,T,72.3,M,7.4,N,3.8,M*23";
        System.out.println("Copied MDA   :" + mda);
        if (StringParsers.validCheckSum(mda))
            System.out.println("Valid!");
        else
            System.out.println("Invalid...");

        String vwt = StringGenerator.generateVWT("II", 16, 96);
        System.out.println(vwt);

        String mwd = StringGenerator.generateMWD("II", 289, 20.9, 15.0);
        System.out.println(mwd);

        String zda = StringGenerator.generateZDA("GP", System.currentTimeMillis());
        System.out.println(zda);
        UTCDate utc = StringParsers.parseZDA(zda);
        System.out.println("ZDA:" + utc.toString());

        String gll = StringGenerator.generateGLL("XX", 37.7489, -122.5070, System.currentTimeMillis());
        System.out.println(gll);
        GLL parsedGLL = StringParsers.parseGLL(gll);
        GeoPos ll = parsedGLL.getGllPos();
        System.out.println(String.format(">> Pos %s", ll.toString()));
    }
}
