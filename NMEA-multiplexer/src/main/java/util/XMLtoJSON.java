package util;

import oracle.xml.parser.v2.DOMParser;
import oracle.xml.parser.v2.NSResolver;
import oracle.xml.parser.v2.XMLDocument;
import oracle.xml.parser.v2.XMLElement;
import org.json.JSONObject;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;

/**
 * KML, to an array of arrays (LeafLetJS)
 */
public class XMLtoJSON {
    static class CustomResolver
            implements NSResolver {

        public String resolveNamespacePrefix(String prefix) {
            return nsHash.get(prefix);
        }

        public void addNamespacePrefix(String prefix, String ns) {
            nsHash.put(prefix, ns);
        }

        Hashtable<String, String> nsHash;

        CustomResolver() {
            nsHash = new Hashtable<>();
        }
    }

    static CustomResolver resolver;

    private final static String KML_NAMESPACE = "http://earth.google.com/kml/2.0";

    public static void main(String... args) throws Exception {
        String fileName = String.format("%s/oliv/web.site/donpedro/journal/trip/GPX/the.full.trip.kml", System.getProperty("user.dir"));
        String outputFileName = String.format("%s/oliv/web.site/donpedro/journal/trip/GPX/the.full.trip.json", System.getProperty("user.dir"));
        File file = new File(fileName);
        if (!file.exists()) {
            throw new RuntimeException(String.format("File Not Found: %s", fileName));
        }

        resolver = new CustomResolver();
        resolver.addNamespacePrefix("kml", KML_NAMESPACE); // Will be used in selectNodes

        URL gpxUrl = new File(fileName).toURI().toURL();

        DOMParser parser = new DOMParser();
        parser.showWarnings(true);
        parser.setErrorStream(System.out);
        // parser.setValidationMode(DOMParser.SCHEMA_VALIDATION);
        parser.setPreserveWhitespace(true);

        parser.parse(gpxUrl);
        XMLDocument parsedGPX = parser.getDocument();

        Element documentElement = parsedGPX.getDocumentElement();
        String rootTagName = documentElement.getTagName();
        System.out.printf("Document root is %s (tag), %s (local), %s (node)\n", rootTagName, documentElement.getLocalName(), documentElement.getNodeName());

        String coordinatesPath = "/kml:kml/kml:Document/kml:Placemark/kml:LineString/kml:coordinates/text()";
        NodeList nodeList = parsedGPX.selectNodes(coordinatesPath, resolver); // selectSingleNode could do too.
        System.out.printf("Selected %d node(s)\n", nodeList.getLength());
        String content = nodeList.item(0).getNodeValue();
        System.out.printf("Content: %s\n", content);
        String[] dataLines = content.split("\n");
        System.out.printf("%d lines of data\n", dataLines.length);
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(outputFileName));
            bw.write("[");

            List<String> finalData = new ArrayList<>();
            Arrays.stream(dataLines).forEach(line -> {
                if (!line.trim().isEmpty()) {
                    String[] array = line.split(",");
                    if (array.length == 3) {
                        finalData.add(String.format("[%s,%s]", array[1].trim(), array[0].trim()));
                    } else {
                        System.out.println("Ooops [" + line + "]");
                    }
                }
            });
            bw.write(String.join(", ", finalData));

            bw.write("]\n");

            // Places
            List<String> placeList = new ArrayList<>();
            String placesPath = "kml:kml/kml:Document/kml:Folder/kml:Placemark";
            nodeList = parsedGPX.selectNodes(placesPath, resolver);
            System.out.printf("Selected %d Placemark node(s)\n", nodeList.getLength());
            for (int i=0; i<nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                String name = ((XMLElement) node).selectSingleNode("kml:name/text()", resolver).getNodeValue();
                Node descNode = ((XMLElement) node).selectSingleNode("kml:description/text()", resolver);
                String description = (descNode != null) ? descNode.getNodeValue() : null;
                if (description != null) {
                    description = description.trim();
                    while (description.contains("\n ")) {
                        description = description.replace("\n ", "\n");
                    }
//                    description = description.replace("<\\\\//", "<//");
                }
                String latitude = null;
                String longitude = null;
                if (((XMLElement) node).selectSingleNode("kml:LookAt", resolver) != null) {
                    try {
                        latitude = ((XMLElement) node).selectSingleNode("kml:LookAt/kml:latitude/text()", resolver).getNodeValue();
                    } catch (NullPointerException oops) {
                        // Absorb
                    }
                    try {
                        longitude = ((XMLElement) node).selectSingleNode("kml:LookAt/kml:longitude/text()", resolver).getNodeValue();
                    } catch (NullPointerException oops) {
                        // Absorb
                    }
                } else {
                    try {
                        latitude = ((XMLElement) node).selectSingleNode("kml:Camera/kml:latitude/text()", resolver).getNodeValue();
                    } catch (NullPointerException oops) {
                        // Absorb
                    }
                    try {
                        longitude = ((XMLElement) node).selectSingleNode("kml:Camera/kml:longitude/text()", resolver).getNodeValue();
                    } catch (NullPointerException oops) {
                        // Absorb
                    }
                }
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("name", name);
                jsonObject.put("description", description);
                jsonObject.put("latitude", latitude);
                jsonObject.put("longitude", longitude);
                //
                placeList.add(jsonObject.toString());
//                System.out.println(jsonObject.toString(2));
            }
            bw.write("\n[");
            bw.write(String.join(", ", placeList));
            bw.write("]\n");

            bw.close();


        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
