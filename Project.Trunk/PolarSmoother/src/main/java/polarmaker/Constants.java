package polarmaker;

import oracle.xml.parser.v2.DOMParser;
import oracle.xml.parser.v2.XMLDocument;
import oracle.xml.parser.v2.XMLElement;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import polarmaker.polars.main.PolarSmoother;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

public class Constants {
	private final static String CONFIG_FILE_NAME = "config" + File.separator + "polar-config.xml";

	private static final String CONFIG_NAMESPACE = "urn:oliv-nmea";
	public final static String DATA_NAMESPACE_URI = "http://www.olivsoft.com/polar-data";

	public final static String POLAR_FUNCTION_NS_URI = "http://www.olivsoft.com/polars";

	public final static String OBJ_FILE_NAME = "polars.obj";

	// Default values
	private static int maxBoatSpeedForPolars = 15;
	private static int minWindSpeed = 5;
	private static int maxWindSpeed = 25;

	public final static int DEFAULT_POLAR_DEGREE = 5;
	public final static int DEFAULT_COEFF_DEGREE = 3;

	public final static int MIN_WIND_ANGLE = 20;
	public final static int MAX_WIND_ANGLE = 180;

	private static String lastOpenFile = "";

	private static DOMParser parser = new DOMParser();

	public static void readConstants() {
		File config = new File(CONFIG_FILE_NAME);
		if (config.exists()) {
			try {
				parser.parse(config.toURI().toURL());
				XMLDocument xmlconf = parser.getDocument();
				PolarSmoother.LocalNSResolver lnsr = new PolarSmoother.LocalNSResolver();
				lnsr.addNamespacePrefix("pol", CONFIG_NAMESPACE);

				lastOpenFile = ((XMLElement) xmlconf.selectNodes("/pol:config", lnsr).item(0)).getAttribute("last-open-file");

				NodeList nl = xmlconf.selectNodes("/pol:config/pol:smoothing-config", lnsr);
				for (int i = 0; i < nl.getLength(); i++) {
					XMLElement elmt = (XMLElement) nl.item(i);
					NodeList data = elmt.getChildNodes();
					for (int j = 0; j < data.getLength(); j++) {
						Node val = data.item(j);
						if (val.getNodeType() == Node.ELEMENT_NODE) {
							int factor = Integer.parseInt(val.getFirstChild().getNodeValue());
							if (val.getNodeName().equals("max-boat-speed-for-polars")) {
								Constants.setMaxBoatSpeedForPolars(factor);
							} else if (val.getNodeName().equals("min-wind-speed")) {
								Constants.setMinWindSpeed(factor);
							} else if (val.getNodeName().equals("max-wind-speed")) {
								Constants.setMaxWindSpeed(factor);
							}
						}
					}
				}
			} catch (Exception xmle) {
				xmle.printStackTrace();
			}
		}
	}

	public static void storeConstants() {
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(CONFIG_FILE_NAME));
			bw.write(
					"<?xml version=\"1.0\"?>\n" +
							"<config xmlns=\"" + CONFIG_NAMESPACE + "\" last-open-file=\"" + lastOpenFile + "\">\n" +
							"  <smoothing-config>\n" +
							"    <max-boat-speed-for-polars>" + Integer.toString(maxBoatSpeedForPolars) + "</max-boat-speed-for-polars>\n" +
							"    <min-wind-speed>" + Integer.toString(minWindSpeed) + "</min-wind-speed>\n" +
							"    <max-wind-speed>" + Integer.toString(maxWindSpeed) + "</max-wind-speed>\n" +
							"  </smoothing-config>\n" +
							"</config>");
			bw.flush();
			bw.close();
		} catch (Exception e) {
			System.err.println("Problem writing config file:");
			e.printStackTrace();
		}
	}

	public static void setMaxBoatSpeedForPolars(int maxBoatSpeedForPolars) {
		Constants.maxBoatSpeedForPolars = maxBoatSpeedForPolars;
	}

	public static int getMaxBoatSpeedForPolars() {
		return maxBoatSpeedForPolars;
	}

	public static void setMinWindSpeed(int minWindSpeed) {
		Constants.minWindSpeed = minWindSpeed;
	}

	public static int getMinWindSpeed() {
		return minWindSpeed;
	}

	public static void setMaxWindSpeed(int maxWindSpeed) {
		Constants.maxWindSpeed = maxWindSpeed;
	}

	public static int getMaxWindSpeed() {
		return maxWindSpeed;
	}

	public static void setLastOpenFile(String lastOpenFile) {
		Constants.lastOpenFile = lastOpenFile;
	}

	public static String getLastOpenFile() {
		return lastOpenFile;
	}
}
