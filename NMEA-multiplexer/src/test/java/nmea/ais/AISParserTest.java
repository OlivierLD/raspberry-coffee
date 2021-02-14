package nmea.ais;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Arrays;
import java.util.List;

public class AISParserTest {

    public static void main(String... args) throws Exception {

        AISParser aisParser = new AISParser();

        String ais; // Error in !AIVDM,1,1,,B,?03Ovk1E6T50D00,2*1E
        if (args.length > 0) {
            if (args[0].equalsIgnoreCase("--inter")) {
                System.out.println("Enter q to quit");
                boolean keepAsking = true;
                while(keepAsking) {
                    String userInput = utils.StaticUtil.userInput("Your AIS sentence > ");
                    if ("Q".equalsIgnoreCase(userInput)) {
                        keepAsking = false;
                    } else {
                        try {
                            System.out.println(aisParser.parseAIS(userInput));
                        } catch (Exception t) {
                            t.printStackTrace();
                        }
                    }
                }
            } else {
                try {
                    System.out.println(aisParser.parseAIS(args[0]));
                } catch (AISParser.AISException t) {
                    System.err.println(t.toString());
                }
            }
        } else {
            // 2's complement
            String tc = AISParser.twosComplement(new StringBuffer("1000100"));
            System.out.println(">> " + tc);
            assert "11111011".equals(tc);
            tc = AISParser.twosComplement(new StringBuffer("00000101"));
            System.out.println(">> " + tc);
            assert "0111100".equals(tc);

            ais = "!AIVDM,1,1,,A,14eG;o@034o8sd<L9i:a;WF>062D,0*7D";
            try {
                System.out.println(aisParser.parseAIS(ais));
            } catch (Throwable t) {
                System.err.println(t.toString());
            }

            ais = "!AIVDM,1,1,,A,15NB>cP03jG?l`<EaV0`MFO000S>,0*39";
            try {
                System.out.println(aisParser.parseAIS(ais));
            } catch (AISParser.AISException t) {
                System.err.println(t.toString());
            }

            ais = "!AIVDM,1,1,,B,177KQJ5000G?tO`K>RA1wUbN0TKH,0*5C";
            try {
                System.out.println(aisParser.parseAIS(ais));
            } catch (AISParser.AISException t) {
                System.err.println(t.toString());
            }

            ais = "!AIVDM,1,1,,A,D03Ovk06AN>40Hffp00Nfp0,2*52";
            try {
                System.out.println(aisParser.parseAIS(ais));
            } catch (AISParser.AISException t) {
                System.err.println(t.toString());
            }

            ais = "!AIVDM,2,2,2,B,RADP,0*10";
            try {
                System.out.println(aisParser.parseAIS(ais));
            } catch (Exception ex) {
                System.err.println(ex.toString());
            }
            System.out.println("--- From dAISy ---");
            // From the dAISy device
//			List<String> aisFromDaisy = Arrays.asList(
//					"!AIVDM,1,1,,A,403Ovk1v@CPI`o>jNnEdjEg0241T,0*5F",
//					"!AIVDM,1,1,,A,D03Ovk06AN>40Hffp00Nfp0,2*52",
//					"!AIVDM,1,1,,A,D03Ovk0m9N>4g@ffpfpNfp0,2*38",
//					"!AIVDM,1,1,,B,D03Ovk0s=N>4g<ffpfpNfp0,2*5D",
//					"!AIVDM,1,1,,B,403Ovk1v@CPN`o>jO8EdjDw02@GT,0*1F",
//					"!AIVDM,1,1,,A,403Ovk1v@CPO`o>jNrEdjEO02<45,0*01",
//					// From PI4J
//					"!AIVDM,1,1,,A,D03Ovk0m9N>4g@ffpfpNfp0,2*38",
//					"!AIVDM,1,1,,B,D03Ovk0s=N>4g<ffpfpNfp0,2*5D",
//					"!AIVDM,1,1,,A,?03Ovk1Gcv1`D00,2*3C",
//					"!AIVDM,1,1,,B,?03Ovk1CpiT0D00,2*02",
//					"!AIVDM,1,1,,A,D03Ovk06AN>40Hffp00Nfp0,2*52",
//					"!AIVDM,1,1,,B,D03Ovk0<EN>40Dffp00Nfp0,2*53",
//					// From Serial IO
//					"!AIVDM,1,1,,B,?03Ovk20AG54D00,2*08",
//					"!AIVDM,1,1,,A,?03Ovk20AG54D00,2*0B",
//					"!AIVDM,1,1,,A,D03Ovk06AN>40Hffp00Nfp0,2*52",
//					"!AIVDM,1,1,,B,D03Ovk0<EN>40Dffp00Nfp0,2*53",
//					"!AIVDM,1,1,,A,15MU>f002Bo?5cHE`@2qOG`>0@43,0*5B",
//
//					"!AIVDM,1,1,,A,D03Ovk1T1N>5N8ffqMhNfp0,2*6A",
//					"!AIVDM,1,1,,B,D03Ovk1b5N>5N4ffqMhNfp0,2*57",
//					"!AIVDM,1,1,,B,403Ovk1v@EG3Do>jOBEdjE?02<4=,0*02",
//					"!AIVDM,1,1,,B,13P<DT012Fo>er2EW:CRd28T0@<I,0*5C",
//					"!AIVDM,1,1,,A,?03Ovk0sNMB0D00,2*3C",
//					"!AIVDM,1,1,,B,13P<DT0wBGo>f<TEW;0BdR8t0D24,0*15",
//					"!AIVDM,1,1,,B,13P<DT0w2Go>fO<EW;f2d29D08K6,0*0E",
//					"!AIVDM,1,1,,A,?03Ovk1GTTnPD00,2*46",
//					"!AIVDM,1,1,,B,13P<DT00jHo>fihEW<LRcR9d0HQl,0*56",
//					"!AIVDM,1,1,,A,D03Ovk06AN>40Hffp00Nfp0,2*52",
//					"!AIVDM,1,1,,A,403Ovk1v@EG40o>jNpEdjBg02806,0*15",
//					"!AIVDM,1,1,,B,403Ovk1v@EG40o>jNpEdjBg02808,0*18",
//					"!AIVDM,1,1,,A,13P<DT012Ho>fs6EW<kBcj800@2:,0*2F",
//					"!AIVDM,1,1,,A,?03Ovk1:9Ob`D00,2*71",
//					"!AIVDM,1,1,,A,?03Ovk0sNMB0D00,2*3C",
//					"!AIVDM,1,1,,A,13P<DT00BHo>g?FEW=U2cj8H0D24,0*5E",
//					"!AIVDM,1,1,,A,D03Ovk1T1N>5N8ffqMhNfp0,2*6A",
//					"!AIVDM,1,1,,B,D03Ovk1b5N>5N4ffqMhNfp0,2*57",
//					"!AIVDM,1,1,,A,403Ovk1v@EG4Do>jNbEdjDw028;l,0*34",
//					"!AIVDM,1,1,,B,403Ovk1v@EG4Do>jNbEdjDw028;n,0*35",
//					"!AIVDM,1,1,,B,13P<DT00BIo>gG<EW=p2d28R0<23,0*41",
//					// Multiple messages
//					"!AIVDM,2,1,6,B,55T6aT42AGrO<ELCJ20t<D4r0Pu0F22222222216CPIC94DfNBEDp3hB,0*0A",
//					"!AIVDM,2,2,6,B,p88888888888880,2*69"
//			);

            List<String> aisFromDaisy = Arrays.asList(
                    "!AIVDM,1,1,,B,?03Ovk1GQ4ePD00,2*2B",
                    "!AIVDM,1,1,,B,D03Ovk0s=N>4g<ffpfpNfp0,2*5D",
                    "!AIVDM,1,1,,A,403Ovk1vBN>r`o>jO>EdjJ?028GR,0*7F",
                    "!AIVDM,1,1,,B,403Ovk1vBN>r`o>jO>EdjJ?028GT,0*7A",
                    "!AIVDM,1,1,,A,403Ovk1vBN>s0o>jO8EdjJ?025kd,0*3F",
                    "!AIVDM,1,1,,B,403Ovk1vBN>s0o>jO8EdjJ?025kd,0*3C",
                    "!AIVDM,1,1,,A,D03Ovk1T1N>5N8ffqMhNfp0,2*6A",
                    "!AIVDM,1,1,,B,D03Ovk1b5N>5N4ffqMhNfp0,2*57",
                    "!AIVDM,1,1,,A,403Ovk1vBN>sDo>jO0EdjJO025kd,0*33",
                    "!AIVDM,1,1,,B,403Ovk1vBN?0Do>jNnEdjIg020S:,0*65"
            );

            aisFromDaisy.forEach(aisStr -> {
                try {
                    System.out.println(aisParser.parseAIS(aisStr));
                } catch (AISParser.AISException t) {
                    System.err.println(t.toString());
                }
            });
            System.out.println("------------------");

            if (true) {
//				String dataFileName = "sample.data/ais.nmea";
                String dataFileName = "sample.data/pb.ais";
                if (args.length > 0) {
                    dataFileName = args[0];
                }
                File file = new File(dataFileName);
                if (file.exists()) {
                    try {
                        BufferedReader br = new BufferedReader(new FileReader(dataFileName));
                        System.out.println(String.format("---- From data file %s ----", dataFileName));
                        String line = "";
                        while (line != null) {
                            line = br.readLine();
                            if (line != null) {
                                if (!line.startsWith("#") && line.startsWith(AISParser.AIS_PREFIX)) {
                                    try {
                                        AISParser.AISRecord aisRecord = aisParser.parseAIS(line);
                                        if (aisRecord != null) {
                                            System.out.println(aisRecord);
                                        } else {
                                            System.out.println(String.format(">> null AIS Record for %s", line));
                                        }
                                    } catch (AISParser.AISException ex) {
                                        System.err.println("For [" + line + "]: " + ex.toString());
                                    } catch (Exception ex) {
                                        System.err.println("For [" + line + "]: ");
                                        ex.printStackTrace();
                                    }
                                } else if (!line.startsWith("#")) {
                                    // TODO else parse NMEA?
                                    System.out.println(String.format("\tNon AIS String... %s", line));
                                }
                            }
                        }
                        br.close();
                    } catch (FileNotFoundException fnfe) {
                        fnfe.printStackTrace();
                    }
                } else {
                    System.out.println(String.format(" >> File %s not found, provide AIS Data file name as runtime arg.", dataFileName));
                }
            }
        }
    }

}
