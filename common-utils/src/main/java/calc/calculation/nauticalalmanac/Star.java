package calc.calculation.nauticalalmanac;

public class Star {
    private final static Star[] CATALOG = new Star[]{
            //       Name              Constellation pos            RA             Dec             deltaRA     deltaD  par
            new Star("Acamar", "theta Eridani", 2.9710266670, -40.3047138890, -0.3910, 1.9400, 0.0280),
            new Star("Achenar", "alfa Eridani", 1.6285700000, -57.2367166670, 1.1730, -3.4700, 0.0230),
            new Star("Acrux", "alpha Crux", 12.4432975000, -63.0990500000, -0.5240, -1.2100, 0.0000),
            new Star("Adhara", "epsilon Canis Majoris", 6.9770966670, -28.9720833330, 0.0310, 0.2800, 0.0000),
            new Star("Aldebaran", "alpha Tauri", 4.5986769440, 16.5092750000, 0.4390, -18.9700, 0.0480),
            new Star("Alioth", "epsilon Ursae Majoris", 12.9004855560, 55.9598527780, 1.3280, -0.5800, 0.0090),
            new Star("Alkaid", "eta Ursae Majoris", 13.7923427780, 49.3133194440, -1.2490, -1.0900, 0.0350),
            new Star("Al Na'ir", "zeta Centauri", 22.1372222220, -46.9609972220, 1.2590, -15.1000, 0.0510),
            new Star("Alnilam", "epsilon Orionis", 5.6035580560, -1.2019500000, 0.0060, -0.2400, 0.0000),
            new Star("Alphard", "alpha Hydrae", 9.4597908330, -8.6586527780, -0.0930, 3.2800, 0.0170),
            new Star("Alphecca", "alpha Coronae Borealis", 15.5781322220, 26.7147055560, 0.9060, -8.8600, 0.0430),
            new Star("Alpheratz", "alpha Andromedae", 0.1397958330, 29.0904388890, 1.0390, -16.3300, 0.0240),
            new Star("Altair", "alpha Aquilae", 19.8463894440, 8.8683416670, 3.6290, 38.6300, 0.1981),
            new Star("Ankaa", "alpha Phoenicis", 0.4380638890, -42.3060583330, 1.8330, -39.5700, 0.0350),
            new Star("Antares", "alpha Scorpii", 16.4901219440, -26.4319861110, -0.0710, -2.0300, 0.0190),
            new Star("Arcturus", "alpha Bootis", 14.2610213890, 19.1824194440, -7.7140, -199.8400, 0.0900),
            new Star("Atria", "alpha Trianguli Australis", 16.8110747220, -69.0277277780, 0.2600, -3.4000, 0.0240),
            new Star("Avior", "epsilon Carinae", 8.3752313890, -59.5095861110, -0.3460, 1.4400, 0.0000),
            new Star("Bellatrix", "gamma Orionis", 5.4188491670, 6.3496500000, -0.0590, -1.3900, 0.0260),
            new Star("Betelgeuse", "alpha Orionis", 5.9195297220, 7.4070416670, 0.1730, 0.8700, 0.0050),
            new Star("Canopus", "alpha Carinae", 6.3991997220, -52.6956944440, 0.2450, 2.0700, 0.0180),
            new Star("Capella", "alpha Aurigae", 5.2781536110, 45.9980277780, 0.7280, -42.4700, 0.0730),
            new Star("Deneb", "alpha Cygni", 20.6905325000, 45.2803638890, 0.0270, 0.2300, 0.0000),
            new Star("Denebola", "beta Leonis", 11.8176611110, 14.572041667, -3.4220, -11.4100, 0.0760),
            new Star("Diphda", "beta Ceti", 0.7264922220, -17.9866166670, 1.6370, 3.2500, 0.0570),
            new Star("Dubhe", "alpha Ursae Majoris", 11.0621294440, 61.7508944440, -1.6750, -6.6500, 0.0310),
            new Star("Elnath", "beta Tauri", 5.4381975000, 28.6074083330, 0.1690, -17.5100, 0.0180),
            new Star("Eltanin", "gamma Draconis", 17.9434352780, 51.4889472220, -0.0810, -1.9400, 0.0170),
            new Star("Enif", "epsilon Pegasi", 21.7364344440, 9.8749777780, 0.2070, -0.0600, 0.0060),
            new Star("Fomalhaut", "alpha Piscis Austrini", 22.9608486110, -29.6222500000, 2.5510, -16.4700, 0.1440),
            new Star("Gacrux", "gamma Crucis", 12.5194247220, -57.1131944440, 0.2850, -26.2300, 0.0000),
            new Star("Gienah", "gamma Corvi", 12.2634350000, -17.5419361110, -1.1240, 2.3300, 0.0000),
            new Star("Hadar", "beta Centauri", 14.0637244440, -60.3729972220, -0.4260, -1.9300, 0.0160),
            new Star("Hamal", "alpha Arietis", 2.1195563890, 23.4624055560, 1.3830, -14.8300, 0.0430),
            new Star("Kaus Australis", "epsilon Sagittarii", 18.4028686110, -34.3846472220, -0.3090, -12.4100, 0.0150),
            new Star("Kochab", "beta Ursae Minoris", 14.8450961110, 74.1554944440, -0.7630, 1.2200, 0.0310),
            new Star("Markab", "alpha Pegasi", 23.0793494440, 15.2052500000, 0.4360, -4.2500, 0.0300),
            new Star("Menkar", "alpha Ceti", 3.0379925000, 4.0897027780, -0.0630, -7.8000, 0.0090),
            new Star("Menkent", "theta Centauri", 14.1113752780, -36.3700083330, -4.2930, -51.9000, 0.0590),
            new Star("Miaplacidus", "beta Carinae", 9.2199880560, -69.7172083330, -3.1080, 10.7800, 0.0380),
            new Star("Mirfak", "alpha Persei", 3.4053791670, 49.8612055560, 0.2460, -2.4600, 0.0290),
            new Star("Nunki", "sigma Sagitarii", 18.9210900000, -26.2967305560, 0.0990, -5.4200, 0.0000),
            new Star("Peacock", "alpha Pavonis", 20.4274588890, -56.735055560, 0.0820, -8.9100, 0.0000),
            new Star("Polaris", "alpha Ursae Minoris", 2.5301955560, 89.2640888890, 19.8770, -1.520, 0.0070),
            new Star("Pollux", "beta Geminorum", 7.7552627780, 28.0261833330, -4.7400, -4.5900, 0.0930),
            new Star("Procyon", "alpha Canis Minoris", 7.6550313890, 5.2250166670, -4.7550, -102.2900, 0.2880),
            new Star("Rasalhague", "alpha Ophiuchi", 17.5822433330, 12.5600388890, 0.8220, -22.6400, 0.0560),
            new Star("Regulus", "alpha Leonis", 10.1395319440, 11.9671916670, -1.6930, 0.6400, 0.0390),
            new Star("Rigel", "beta Orionis", 5.2422966670, -8.2016611110, 0.0030, -0.1300, 0.0130),
            new Star("Rigil Kent", "alpha Centauri", 14.6599680560, -60.8354000000, -49.8260, 69.9300, 0.7516),
            new Star("Sabik", "eta Ophiuchi", 17.1729669440, -15.7249194400, 0.2600, 9.5000, 0.0520),
            new Star("Schedar", "alpha Cassiopeiae", 0.6751250000, 56.5373500000, 0.6360, -3.1900, 0.0160),
            new Star("Shaula", "lambda Scorpii", 17.5601483330, -37.1038111110, -0.0110, -2.9200, 0.0000),
            new Star("Sirius", "alpha Canis Majoris", 6.7524641670, -16.7161083330, -3.8470, -120.5300, 0.3751),
            new Star("Spica", "alpha Virginis", 13.4198852780, -11.1613083330, -0.2780, -2.8300, 0.0210),
            new Star("Suhail", "lambda Velorum", 9.1332711110, -43.4326055560, -0.1720, 1.2700, 0.0150),
            new Star("Vega", "alpha Lyrae", 18.6156477780, 38.7836583330, 1.7260, 28.6100, 0.1230),
            new Star("Zubenelgenubi", "alpha Librae", 14.8479758330, -16.0417833330, -0.7340, -6.6800, 0.0490)
    };

    public static Star[] getCatalog() {
        return CATALOG.clone();
    }

    private String starName = "", constellationPlace = "";
    private double ra, dec, deltaRa, deltaDec, par;

    public Star(String name,
                String constellation,
                double ra,
                double dec,
                double deltaRa,
                double deltaDec,
                double par) {
        this.starName = name;
        this.constellationPlace = constellation;
        this.ra = ra;
        this.dec = dec;
        this.deltaRa = deltaRa;
        this.deltaDec = deltaDec;
        this.par = par;
    }

    public static Star getStar(String name) {
        Star star = null;
        for (int i = 0; i < CATALOG.length; i++) {
            if (CATALOG[i].getStarName().equals(name)) {
                star = CATALOG[i];
                break;
            }
        }
        return star;
    }

    public void setStarName(String starName) {
        this.starName = starName;
    }

    public String getStarName() {
        return starName;
    }

    public double getRa() {
        return ra;
    }

    public double getDec() {
        return dec;
    }

    public double getDeltaRa() {
        return deltaRa;
    }

    public double getDeltaDec() {
        return deltaDec;
    }

    public double getPar() {
        return par;
    }

    public void setConstellation(String constellation) {
        this.constellationPlace = constellation;
    }

    public String getConstellation() {
        return constellationPlace;
    }
}
