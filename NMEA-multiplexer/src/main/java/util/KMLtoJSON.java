package util;

import java.io.*;

/**
 * KML, to an array of arrays (LeafLetJS)
 *
 * @deprecated Use XMLtoJSON instead.
 */
public class KMLtoJSON {
    public static void main(String... args) {
        String fileName = "/Users/olivierlediouris/oliv/web.site/donpedro/journal/trip/GPX/the.full.trip.kml";
        String outputFileName = "/Users/olivierlediouris/oliv/web.site/donpedro/journal/trip/GPX/the.full.trip.json";
        File file = new File(fileName);
        if (!file.exists()) {
            throw new RuntimeException(String.format("File Not Found: %s", fileName));
        }
        String line = "";
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            BufferedWriter bw = new BufferedWriter(new FileWriter(outputFileName));
            bw.write("[");
            boolean go = true;
            boolean readyToRead = false;
            while (go) {
                line = br.readLine();
                if (line.trim().startsWith("<LineString>")) { // Ready to read
                    System.out.println("Ready!!");
                    readyToRead = true;
                }
                if (line == null || line.trim().startsWith("<coordinates>") && readyToRead) {
                    go = false;
                }
                if (line.trim().startsWith("<coordinates>") && readyToRead) {
                    System.out.println("Found it!");
                    String[] array = line.trim().substring("<coordinates>".length()).split(",");
                    bw.write(String.format("[%s,%s]", array[1], array[0]));
                }
            }
            go = true;
            while (go) {
                line = br.readLine();
                if (line == null || line.trim().startsWith("</coordinates>")) {
                    go = false;
                } else {
                    String[] array = line.trim().split(",");
                    if (array.length == 3) {
                        bw.write(String.format(",[%s,%s]", array[1], array[0]));
                        bw.flush();
                    }
                }
            }
            bw.write("]\n");
            bw.close();
            br.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
