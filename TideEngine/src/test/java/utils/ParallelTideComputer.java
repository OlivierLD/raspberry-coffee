package utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import tideengine.*;
import tideengine.contracts.BackendDataComputer;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Locale;
import java.util.Set;

public class ParallelTideComputer {

    private static BackendDataComputer xmlDataComputer = null;
    private static BackendDataComputer jsonDataComputer = null;

    private static Constituents xmlConstituentsObject = null;
    private static Stations xmlStationsObject = null;

    private static Constituents jsonConstituentsObject = null;
    private static Stations jsonStationsObject = null;

    public static void main(String... args) throws Exception {

        xmlDataComputer = new BackEndXMLTideComputer();
        jsonDataComputer = new BackEndJSONTideComputer();

        xmlDataComputer.connect();
        jsonDataComputer.connect();

        xmlConstituentsObject = xmlDataComputer.buildConstituents();
        xmlStationsObject = xmlDataComputer.getTideStations();

        jsonConstituentsObject = jsonDataComputer.buildConstituents();
        jsonStationsObject = jsonDataComputer.getTideStations();

        // Compare
        System.out.println("Comparing?");
        String stationName = "Patreksfj";
        TideStation xmlStation = null;
        if (xmlStation == null) { // Try match
            Set<String> keys = xmlStationsObject.getStations().keySet();
            for (String s : keys) {
                if (s.toLowerCase(Locale.ROOT).contains(stationName.toLowerCase(Locale.ROOT))) {
                    xmlStation = xmlStationsObject.getStations().get(s);
                    if (xmlStation != null) { // First one
                        break;
                    }
                }
            }
        }

        TideStation jsonStation = null;
        if (jsonStation == null) { // Try match
            Set<String> keys = jsonStationsObject.getStations().keySet();
            for (String s : keys) {
                if (s.toLowerCase(Locale.ROOT).contains(stationName.toLowerCase(Locale.ROOT))) {
                    jsonStation = jsonStationsObject.getStations().get(s);
                    if (jsonStation != null) { // First one
                        break;
                    }
                }
            }
        }
        final String ENCODING = "UTF-8"; // "ISO-8859-1";
        String name = xmlStation.getFullName();
        System.out.printf("XML : %s => %s\n", name, URLDecoder.decode(name, ENCODING));
        name = jsonStation.getFullName();
        System.out.printf("JSON: %s => %s\n", name, URLDecoder.decode(name, ENCODING));

        if (false) { // Set to true to regenerate the JSON from the XML (weird characters).
            ObjectMapper mapper = new ObjectMapper();
            FileOutputStream fos = new FileOutputStream(new File("stations.json"));
            mapper.writeValue(fos, xmlStationsObject);
            fos.flush();
            fos.close();
        }

        xmlDataComputer.disconnect();
        jsonDataComputer.disconnect();
    }
}
