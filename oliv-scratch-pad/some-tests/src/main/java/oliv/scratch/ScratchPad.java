package oliv.scratch;

import utils.DumpUtil;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;

public class ScratchPad {

    public static void main(String... args) {
        Map<String, String> map = Map.of("Key", "value", "Akeu", "Coucou");
        map.keySet().forEach(k -> System.out.printf("%s -> %s %n", k, map.get(k)));

        File nmeaFile = new File("/Users/olivierlediouris/Desktop/titus/titus.nmea");
        ByteArrayOutputStream bos = null;
        try {
            FileInputStream fis = new FileInputStream(nmeaFile);
            byte[] buffer = new byte[1_024];
            bos = new ByteArrayOutputStream();
            int len;
            while ((len = fis.read(buffer)) != -1) {
                bos.write(buffer, 0, len);
            }
        } catch (IOException ioe) {
            System.err.println(ioe.getMessage());
        }
        byte[] bytes = bos.toByteArray();
        String[] dd = DumpUtil.dualDump(bytes);
        for (String l : dd) {
            System.out.println(l);
        }

    }
}
