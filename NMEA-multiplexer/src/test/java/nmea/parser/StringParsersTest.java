package nmea.parser;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class StringParsersTest {
    
    /**
     * For tests
     *
     * @param args
     */
    public static void main(String... args) {

        StringParsers.listDispatcher();
        System.out.println("----------------------");

        String str = "";

        str = "2006-05-05T17:35:48.000Z";
        long ld = StringParsers.durationToDate(str);
        System.out.println(str + " => " + new Date(ld));
        try {
            str = "2006-05-05T17:35:48Z";
            ld = StringParsers.durationToDate(str);
            System.out.println(str + " => " + new Date(ld));
            str = "2006-05-05T17:35:48-10:00";
            ld = StringParsers.durationToDate(str);
            System.out.println(str + " => " + new Date(ld));
            str = "2006-05-05T17:35:48+10:00";
            ld = StringParsers.durationToDate(str);
            System.out.println(str + " => " + new Date(ld));
            str = "2006-05-05T17:35:48.000-09:30";
            ld = StringParsers.durationToDate(str);
            System.out.println(str + " => " + new Date(ld));
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        str = "$IIRMC,092551,A,1036.145,S,15621.845,W,04.8,317,,10,E,A*0D";
        RMC rmc = null;
        rmc = StringParsers.parseRMC(str);

        str = "\n     $GPRMC,172214.004,A,3739.8553,N,12222.8144,W,000.0,000.0,220309,,,A*7C  \n  ";

        str = "$GPRMC,123519,A,4807.038,N,01131.000,E,022.4,084.4,230394,003.1,W*6A";

        rmc = null; // parseRMC(str);
//  System.out.println("RMC:" + rmc);
//  System.out.println("RMC Done.");

        System.out.println("Lat from RMC :" + StringParsers.getLatFromRMC(str));
        System.out.println("Long from RMC:" + StringParsers.getLongFromRMC(str));
        System.out.println("COG from RMC :" + StringParsers.getCOGFromRMC(str));
        System.out.println("SOG from RMC :" + StringParsers.getSOGFromRMC(str));
        System.out.println("------------------");

        str = "$IIMWV,088,T,14.34,N,A*27";
        Wind w = StringParsers.parseMWV(str);
        System.out.println("Wind  :" + w);

        str = "$IIRMC,200914.00,A,3749.58,N,12228.33,W,6.90,025,,015,E,N*02";
        System.out.println("Valid:" + StringParsers.validCheckSum(str));

        rmc = StringParsers.parseRMC(str);
        System.out.println("Parsed:" + str);
        System.out.println("RMC:" + rmc);

        str = "$IIVWR,148.,L,02.4,N,01.2,M,04.4,K*XX";
        w = StringParsers.parseVWR(str);
        System.out.println("Wind  :" + w);

        str = "$IIVTG,054.7,T,034.4,M,005.5,N,010.2,K,A*XX";
        OverGround og = StringParsers.parseVTG(str);
        System.out.println("Over Ground:" + og);

        str = "$IIMWV,127.0,R,8.5,N,A*34";
        w = StringParsers.parseMWV(str);
        System.out.println("Wind  :" + w);

        str = "$IIVWR,036,R,08.3,N,,,,*6F";
        w = StringParsers.parseVWR(str);
        System.out.println("Wind  :" + w);

        str = "$aaVLW,123.45,N,12.34,N*hh";
        VLW vlw = StringParsers.parseVLW(str);
        System.out.println("Log - Cumul:" + vlw.getLog() + ", Daily:" + vlw.getDaily());

        str = "$xxMTW,+18.0,C*hh";
        double t = StringParsers.parseMTW(str);
        System.out.println("Temperature:" + t + "\272C");

        str = "$iiRMB,A,0.66,L,003,004,4917.24,N,12309.57,W,001.3,052.5,000.5,V*0B";
        RMB rmb = StringParsers.parseRMB(str);
        System.out.println("RMB:");
        if (rmb != null) {
            System.out.println("  XTE:" + rmb.getXte() + " nm (steer " + (rmb.getDts().equals("R") ? "Right" : "Left") + ")");
            System.out.println("  Origin     :" + rmb.getOwpid());
            System.out.println("  Destination:" + rmb.getDwpid() + " (" + rmb.getDest().toString() + ")");
            System.out.println("  DTD:" + rmb.getRtd() + " nm");
            System.out.println("  BTD:" + rmb.getBtd() + "\272T");
            System.out.println("  STD:" + rmb.getDcv() + " kts");
            System.out.println("  Status:" + (rmb.getAs().equals("V") ? "En route" : "Done"));
        } else
            System.out.println("No RMB Data");

        str = "$IIGLL,3739.854,N,12222.812,W,014003,A,A*49";
        GLL gll = StringParsers.parseGLL(str);
        System.out.println("Position:" + gll.getGllPos().toString() + ", Date:" + gll.getGllTime().toString());

        str = "$IIVTG,311.,T,,M,05.6,N,10.4,K,A*2F";
        og = StringParsers.parseVTG(str);
        System.out.println("Over Ground:" + og);

        str = "$IIVTG,,T,295,M,0.0,N,,*02";
        og = StringParsers.parseVTG(str);
        System.out.println("Over Ground:" + og);

        str = "$IIDPT,007.4,+1.0,*43";
        float depth = StringParsers.parseDPT(str, StringParsers.DEPTH_IN_METERS);
        System.out.println("Depth:" + depth);

        str = "$IIVWR,024,R,08.4,N,,,,*6B";
        w = StringParsers.parseVWR(str);

        str = "$IIHDM,125,M*3A";
        int h = StringParsers.parseHDM(str);
        System.out.println("HDM:" + h);
        str = "$IIHDT,131,T*3F";
        h = StringParsers.parseHDT(str);
        System.out.println("HDT:" + h);

        str = "$GPZDA,201530.00,04,07,2002,00,00*60";
        UTCDate utcZDA = StringParsers.parseZDA(str);
        System.out.println("UTC Time: " + utcZDA.toString());

        str = "$IIVTG,17.,T,M,7.9,N,,*36";
        og = StringParsers.parseVTG(str);
        System.out.println("Over Ground:" + og);

    /*
     * Cloning cable strings
     *
     $PICOA,90,00,REMOTE,ON*58
     $PICOA,90,02,REMOTE,ON*5A
     $PICOA,90,02,MODE,USB*18
     $PICOA,90,02,MODE,J3E*60
     $PICOA,90,02,RXF,8.415000*05
     $PICOA,90,02,TXF,8.415000*03
     */
        str = "$PICOA,90,00,REMOTE,ON*58";
        System.out.println("[" + str + "] is " + (StringParsers.validCheckSum(str) ? "" : "not ") + "valid.");

        System.out.println("String generation:");
        str = "PICOA,90,02,RXF,8.415000"; // Attention! No leading '$'
        int cs = StringParsers.calculateCheckSum(str);
        String cks = Integer.toString(cs, 16).toUpperCase();
        if (cks.length() < 2)
            cks = "0" + cks;
        str += ("*" + cks);
        System.out.println("With checksum: $" + str);

        str = "PICOA,90,02,TXF,8.415000";
        cs = StringParsers.calculateCheckSum(str);
        cks = Integer.toString(cs, 16).toUpperCase();
        if (cks.length() < 2)
            cks = "0" + cks;
        str += ("*" + cks);
        System.out.println("With checksum: $" + str);

        str = "$IIHDG,178.,,,,*77";
        HDG hdgData = StringParsers.parseHDG(str);
        System.out.println("Hdg:" + hdgData.getHeading() + ", d:" + hdgData.getDeviation() + " W:" + hdgData.getVariation());

        str = "$IIHDG,126,,,10,E*16";
        hdgData = StringParsers.parseHDG(str);
        System.out.println("Hdg:" + hdgData.getHeading() + ", d:" + hdgData.getDeviation() + " W:" + hdgData.getVariation());

        str = "$IIRMC,220526.00,A,3754.34,N,12223.20,W,3.90,250,,015,E,N*07";
        rmc = StringParsers.parseRMC(str);
        Date date = rmc.getRmcDate();
        Date time = rmc.getRmcTime();

        System.out.println("Date:" + date);
        System.out.println("Time:" + time);

        System.out.println("-----------------------------------");

        str = "$GPRMC,183333.000,A,4047.7034,N,07247.9938,W,0.66,196.21,150912,,,A*7C";
        rmc = StringParsers.parseRMC(str);
        date = rmc.getRmcDate();
        time = rmc.getRmcTime();

        System.out.println("Date:" + date);
        System.out.println("Time:" + time);

        str = "$GPRMC,183334.000,A,4047.7039,N,07247.9939,W,0.61,196.21,150912,,,A*70";
        rmc = StringParsers.parseRMC(str);
        date = rmc.getRmcDate();
        time = rmc.getRmcTime();

        System.out.println("Date:" + date);
        System.out.println("Time:" + time);

        System.out.println("------- GSV -------");
        str = "$GPGSV,3,1,11,03,03,111,00,04,15,270,00,06,01,010,00,13,06,292,00*74";
        Map<Integer, SVData> hm = StringParsers.parseGSV(str);
        if (hm == null)
            System.out.println("GSV wait...");
        str = "$GPGSV,3,2,11,14,25,170,00,16,57,208,39,18,67,296,40,19,40,246,00*74";
        hm = StringParsers.parseGSV(str);
        if (hm == null)
            System.out.println("GSV wait...");
        str = "$GPGSV,3,3,11,22,42,067,42,24,14,311,43,27,05,244,00,,,,*4D";
        hm = StringParsers.parseGSV(str);
        if (hm == null)
            System.out.println("GSV wait...");
        else {
            System.out.println(hm.size() + " Satellites in view:");
            for (Integer sn : hm.keySet()) {
                SVData svd = hm.get(sn);
                System.out.println("Satellite #" + svd.getSvID() + " Elev:" + svd.getElevation() + ", Z:" + svd.getAzimuth() + ", SNR:" + svd.getSnr() + "db");
            }
        }

        System.out.println("------- GGA -------");
        str = "$GPGGA,014457,3739.853,N,12222.821,W,1,03,5.4,1.1,M,-28.2,M,,*7E";
        List<Object> al = StringParsers.parseGGA(str);
        UTC utc = (UTC) al.get(0);
        GeoPos pos = (GeoPos) al.get(1);
        Integer nbs = (Integer) al.get(2);
        Double alt = (Double) al.get(3);
        System.out.println("UTC:" + utc.toString());
        System.out.println("Pos:" + pos.toString());
        System.out.println(nbs.intValue() + " Satellite(s) in use");
        System.out.println("Elevation:" + alt);

        str = "$GPGGA,183334.000,4047.7039,N,07247.9939,W,1,6,1.61,2.0,M,-34.5,M,,*6B";
        al = StringParsers.parseGGA(str);
        utc = (UTC) al.get(0);
        pos = (GeoPos) al.get(1);
        nbs = (Integer) al.get(2);
        alt = (Double) al.get(3);
        System.out.println("UTC:" + utc.toString());
        System.out.println("Pos:" + pos.toString());
        System.out.println(nbs.intValue() + " Satellite(s) in use");
        System.out.println("Elevation:" + alt);

        System.out.println("------- GSA -------");
        str = "$GPGSA,A,3,19,28,14,18,27,22,31,39,,,,,1.7,1.0,1.3*35";
        GSA gsa = StringParsers.parseGSA(str);
        System.out.println("- Mode: " + (gsa.getMode1().equals(GSA.ModeOne.Auto) ? "Automatic" : "Manual"));
        System.out.println("- Mode: " + (gsa.getMode2().equals(GSA.ModeTwo.NoFix) ? "No Fix" : (gsa.getMode2().equals(GSA.ModeTwo.TwoD) ? "2D" : "3D")));
        System.out.println("- Sat in View:" + gsa.getSvArray().size());
        System.out.println("-----------");
        str = "$GPGSA,A,2,,,,,,20,23,,,32,,,5.4,5.4,*1F";
        gsa = StringParsers.parseGSA(str);
        System.out.println("- Mode: " + (gsa.getMode1().equals(GSA.ModeOne.Auto) ? "Automatic" : "Manual"));
        System.out.println("- Mode: " + (gsa.getMode2().equals(GSA.ModeTwo.NoFix) ? "No Fix" : (gsa.getMode2().equals(GSA.ModeTwo.TwoD) ? "2D" : "3D")));
        System.out.println("- Sat in View:" + gsa.getSvArray().size());

        System.out.println("Test:" + GSA.ModeOne.Auto);

        str = "$IIRMC,022136,A,3730.092,N,12228.864,W,00.0,181,141113,15,E,A*1C";
        rmc = StringParsers.parseRMC(str);
        System.out.println("-> RMC date:" + rmc.getRmcDate().toString() + " (" + rmc.getRmcDate().getTime() + ")");

        str = "$IIRMC,144432.086,V,,,,,00.0,0.00,190214,,,N*48";
        rmc = StringParsers.parseRMC(str);
        try {
            System.out.println("-> RMC date:" + rmc.getRmcDate() + " (" + rmc.getRmcDate().getTime() + ")");
        } catch (Exception ex) {
            System.out.println("Expected:" + ex.toString());
        }

        str = "$PGACK,103*40";
        System.out.println("[" + str + "] is " + (StringParsers.validCheckSum(str) ? "" : "not ") + "valid.");
        str = "$PGTOP,11,2*6E";
        System.out.println("[" + str + "] is " + (StringParsers.validCheckSum(str) ? "" : "not ") + "valid.");

        str = "$PMTK010,002*2D";
        System.out.println("[" + str + "] is " + (StringParsers.validCheckSum(str) ? "" : "not ") + "valid........");

        str = "$IIMMB,29.9350,I,1.0136,B*78";
        System.out.println("[" + str + "] is " + (StringParsers.validCheckSum(str) ? "" : "not ") + "valid.");
        double pressure = StringParsers.parseMMB(str);
        System.out.println(" ==> " + pressure + " hPa");

        str = "$IIMTA,20.5,C*02";
        System.out.println("[" + str + "] is " + (StringParsers.validCheckSum(str) ? "" : "not ") + "valid.");
        double temp = StringParsers.parseMTA(str);
        System.out.println(" ==> " + temp + "\272");

        str = "$IIXDR,P,1.0136,B,BMP180,C,15.5,C,BMP180*58";
        System.out.println("[" + str + "] is " + (StringParsers.validCheckSum(str) ? "" : "not ") + "valid.");
        List<StringGenerator.XDRElement> xdr = StringParsers.parseXDR(str);
        for (StringGenerator.XDRElement x : xdr) {
            System.out.println(" => " + x.toString());
        }
        str = "$IIXDR,P,1.0136,B,0,C,15.5,C,1,H,65.5,P,2*6B";
        System.out.println("[" + str + "] is " + (StringParsers.validCheckSum(str) ? "" : "not ") + "valid.");
        xdr = StringParsers.parseXDR(str);
        for (StringGenerator.XDRElement x : xdr) {
            System.out.println(" => " + x.toString());
            if (x.getTypeNunit().equals(StringGenerator.XDRTypes.HUMIDITY)) {
                System.out.println("Humidity:" + x.getValue() + "%");
            }
        }

        str = "$IIXDR,A,180,D,PTCH,A,-154,D,ROLL*78";
        System.out.println("[" + str + "] is " + (StringParsers.validCheckSum(str) ? "" : "not ") + "valid.");
        xdr = StringParsers.parseXDR(str);
        for (StringGenerator.XDRElement x : xdr) {
            System.out.println(" => " + x.toString());
            if (x.getTypeNunit().equals(StringGenerator.XDRTypes.ANGULAR_DISPLACEMENT) &&
                    x.getTransducerName().equals(StringGenerator.XDR_PTCH)) {
                System.out.println("Pitch:" + x.getValue() + "\272");
            }
            if (x.getTypeNunit().equals(StringGenerator.XDRTypes.ANGULAR_DISPLACEMENT) &&
                    x.getTransducerName().equals(StringGenerator.XDR_ROLL)) {
                System.out.println("Roll:" + x.getValue() + "\272");
            }
        }

        str = "$GPRMC,123519,A,4807.038,N,01131.000,E,022.4,084.4,230394,003.1,W*6A";
        rmc = StringParsers.parseRMC(str);
        try {
            System.out.println("-> RMC date:" + rmc.getRmcDate() + " (" + rmc.getRmcDate().getTime() + ")");
        } catch (Exception ex) {
            System.out.println("Expected:" + ex.toString());
        }

        str = "$IIRMC,062658,A,1111.464,S,14235.335,W,05.6,226,231110,10,E,A*0A";
        rmc = StringParsers.parseRMC(str);
        try {
            System.out.println("-> RMC date:" + rmc.getRmcDate() + " (" + rmc.getRmcDate().getTime() + ")");
        } catch (Exception ex) {
            System.out.println("Expected:" + ex.toString());
        }
        System.out.println("Pos:" + rmc.getGp().lat + "/" + rmc.getGp().lng);

        // A bad one
        str = "$IIRMC,051811,A,3730.079,N,12228.853,W,,,070215,19.905,I,1.013,B,28.8,C,14.0,C,,,,,172.0,T,173.0,M,35.1,N,18.1,M*66";
        rmc = StringParsers.parseRMC(str);
        if (rmc != null) {
            try {
                System.out.println("-> RMC date:" + rmc.getRmcDate() + " (" + rmc.getRmcDate().getTime() + ")");
            } catch (Exception ex) {
                System.out.println("Expected:" + ex.toString());
            }
        } else
            System.out.println("Invalid string:" + str);

        str = "$$IIRMC,055549,A,3730.080,N,29.908,I,1.013,B,28.8,C,14.0,C,,,,,169.0,T,170.0,M,28.3,N,14.6,M*67";
        rmc = StringParsers.parseRMC(str);
        if (rmc != null) {
            try {
                System.out.println("-> RMC date:" + rmc.getRmcDate() + " (" + rmc.getRmcDate().getTime() + ")");
            } catch (Exception ex) {
                System.out.println("Expected:" + ex.toString());
            }
        } else
            System.out.println("Invalid string:" + str);

        str = "$RPMMB,29.9276,I,1.0133,B*LW,08200,N,000.0,N*59";
        System.out.println("[" + str + "] is " + (StringParsers.validCheckSum(str) ? "" : "not ") + "valid.");
        pressure = StringParsers.parseMMB(str);
        System.out.println(" ==> " + pressure + " hPa");

        System.out.println("Done");

        str = "$WSCMP,TWD,135,TWS,1.7,GUSTS,11.3,GUSTD,180,TWS_AVG2M,.4,TWD_AVG2M,74,GUSTS_10M,11.3,GUSTD_10M,180,HUM,46.67,TEMPC,24.31,RAIN,0,DAYLYRAIN,0,PRMSL,1010.2,BAT,4.2,LIGHT,2.07*00";
        boolean valid = StringParsers.validCheckSum(str);
        System.out.println("Chain is " + (valid ? "" : "not ") + "valid");

        str = "WSCMP,TWD,135,TWS,1.7,GUSTS,11.3,GUSTD,180,TWS_AVG2M,.4,TWD_AVG2M,74,GUSTS_10M,11.3,GUSTD_10M,180,HUM,46.67,TEMPC,24.31,RAIN,0,DAYLYRAIN,0,PRMSL,1010.2,BAT,4.2,LIGHT,2.07";
        cs = StringParsers.calculateCheckSum(str);
        cks = Integer.toString(cs, 16).toUpperCase();
        if (cks.length() < 2)
            cks = "0" + cks;
        str += ("*" + cks);
        System.out.println("With checksum: $" + str);

        str = "OSQAD,";
        cs = StringParsers.calculateCheckSum(str);
        cks = Integer.toString(cs, 16).toUpperCase();
        if (cks.length() < 2)
            cks = "0" + cks;
        str += ("*" + cks);
        System.out.println("With checksum: $" + str);

        str = "OSQSM,14153505547,This is my message";
        cs = StringParsers.calculateCheckSum(str);
        cks = Integer.toString(cs, 16).toUpperCase();
        if (cks.length() < 2)
            cks = "0" + cks;
        str += ("*" + cks);
        System.out.println("With checksum: $" + str);

        str = "$OSQDM,12*6B";
        valid = StringParsers.validCheckSum(str);
        System.out.println("Chain is " + (valid ? "" : "not ") + "valid");

        str = "$WIMDA,29.4473,I,0.9972,B,17.2,C,10.2,C,81.2,,,,,,,,,,,*75";
        str = "$RPMDA,30.177,I,1.022,B,17.5,C,9.0,C,89.1,,,,42.0,T,43.0,M,12.8,N,6.6,M*76";
        str = "$WSMDA,30.029,I,1.017,B,16.6,C,,,66.0,,,,12,T,,,3.1,N,1.6,M*6B";
        valid = StringParsers.validCheckSum(str);
        System.out.println("MDA Chain is " + (valid ? "" : "not ") + "valid");
        StringParsers.MDA mda = StringParsers.parseMDA(str);
        System.out.println("PAtm:" + (mda.pressBar * 1_000) + " mb, Air:" + mda.airT);
        System.out.println("Dew point:" + (mda.dewC == null ? "null" : "not null"));
        System.out.println("Water T:" + ((mda.waterT == null) ? "null" : mda.waterT) + " Rel Hum:" + ((mda.relHum == null) ? "null" : mda.relHum));

        str = "$CCMWV,194.0,T,018.5,N,A*3B\r\n";
        valid = StringParsers.validCheckSum(str);
        System.out.println("MWV Chain is " + (valid ? "" : "not ") + "valid");

        System.out.println(String.format("Device ID: %s ", StringParsers.getDeviceID(str)));
        System.out.println(String.format("Sentence ID: %s ", StringParsers.getSentenceID(str)));

        str = "$GPRMC,143040.000,V,3744.9330,N,12230.4192,W,000.0,000.0,060218,,,N*6A";
        valid = StringParsers.validCheckSum(str);
        System.out.println("RMC Chain is " + (valid ? "" : "not ") + "valid");
        rmc = StringParsers.parseRMC(str);
        System.out.println("Parsed");

        // Auto parse. Use listDispatchers
        System.out.println("=============");
        Arrays.asList(StringParsers.Dispatcher.values()).stream()
                .forEach(disp -> System.out.println(String.format("%s: %s", disp.key(), disp.description())));
        System.out.println("=============");

        str = "$GPRMC,123519,A,4807.038,N,01131.000,E,022.4,084.4,230394,003.1,W*6A";
        StringParsers.ParsedData auto = StringParsers.autoParse(str);
        if (auto == null) {
            System.out.println("AutoParse returned null");
        } else {
            System.out.println(String.format("AutoParse returned a %s", auto.getParsedData().getClass().getName()));
            System.out.println(auto.toString());
            if (auto.getParsedData() instanceof RMC) {
                System.out.println(String.format("-> As RMC: %s", ((RMC)auto.getParsedData()).toString()));
            }
        }

        str = "$GPRMC,12,58,325,06,24,46,227,27,25,22,310,,17,13,064,*78";
        valid = StringParsers.validCheckSum(str);
        System.out.println("RMC Chain is " + (valid ? "" : "not ") + "valid");
        rmc = StringParsers.parseRMC(str);
        System.out.println("Parsed");

        System.setProperty("rmc.date.offset", "7168");
        str = "$GPRMC,123519,A,4807.038,N,01131.000,E,022.4,084.4,230394,003.1,W*6A"; // The GPS with a date offset...
        System.out.println(StringParsers.parseRMCtoString(str));

        System.clearProperty("rmc.date.offset");
        str = "$GPRMC,012047.00,A,3744.93470,N,12230.42777,W,0.035,,030519,,,D*61\n"; // Small GSP-USB-Key
        System.out.println(String.format("From [%s], %s", str.trim(), StringParsers.parseRMCtoString(str)));

        String[] txt = new String[]{
                "$AITXT,01,01,91,FREQ,2087,2088*57",
                "$GPTXT,01,01,02,u-blox ag - www.u-blox.com*50",
                "$GPTXT,01,01,02,HW  UBX-G70xx   00070000 FF7FFFFFo*69",
                "$GPTXT,01,01,02,ROM CORE 1.00 (59842) Jun 27 2012 17:43:52*59",
                "$GPTXT,01,01,02,PROTVER 14.00*1E",
                "$GPTXT,01,01,02,ANTSUPERV=AC SD PDoS SR*20",
                "$GPTXT,01,01,02,ANTSTATUS=OK*3B",
                "$GPTXT,01,01,02,LLC FFFFFFFF-FFFFFFFF-FFFFFFFF-FFFFFFFF-FFFFFFFD*2C"
        };
        for (String s : txt) {
            System.out.println(String.format("%s => %s", s, StringParsers.parseTXT(s)));
        }

        System.out.println("\nDone!");
    }
    
}
