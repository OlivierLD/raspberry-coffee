package astro.sample;

import utils.DumpUtil;

public class DumpUtilTest {
    public static void main(String... args) {
        String forTests = "$GPGSA,A,3,07,17,30,11,28,13,01,19,,,,,2.3,1.4,1.9*3D";
        String[] dd = DumpUtil.dualDump(forTests);
        for (String l : dd) {
            System.out.println(l);
        }
        System.out.println("--- W H O   C A L L E D   M E ---");
        DumpUtil.whoCalledMe().stream().forEach(System.out::println);
    }
}
