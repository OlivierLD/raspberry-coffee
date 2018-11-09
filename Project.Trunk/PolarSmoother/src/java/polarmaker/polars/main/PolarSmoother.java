package polarmaker.polars.main;

import oracle.xml.parser.schema.XMLSchema;
import oracle.xml.parser.schema.XSDBuilder;
import oracle.xml.parser.v2.DOMParser;
import oracle.xml.parser.v2.NSResolver;
import oracle.xml.parser.v2.XMLParser;
import polarmaker.Constants;
import polarmaker.polars.smooth.gui.PolarSmootherGUI;
import polarmaker.polars.smooth.gui.components.polars.CoeffForPolars;

import javax.swing.UIManager;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Smoothes polars.
 * <p>
 * Files with extension .polar-data are the data (logged points) used for the smoothing.
 * Files with extension .polar-coeff contain the smoothed curves coefficients, used to calculate routing(s).
 * <b>Files with extension .pol are the polars use by MaxSea and LogiSail.</b>
 * Files with extension .obj are used to visualize the .pol in 3D.
 * <p>
 * polar-data -> polar-coeff    --> pol
 * --> obj
 */

/*
 * One tab for each curve, or one tab to gather (rule) them all ?
 */
public class PolarSmoother {
	public final static String VERSION_NUMBER = "4.0.0.0"; // Apr-20, 2016

	public final static String PRODUCT_ID = "polar_smoother." + VERSION_NUMBER;

	private static final String SCHEMA_LOCATION = "polar-data.xsd";
	private final static DOMParser parser = new DOMParser();

	public PolarSmoother() {
	}

	/**
	 * @param x     abscissa
	 * @param coeff array of coefficient. index 0: highest degree
	 * @return the value of the polynomial
	 */
	public static final double f(double x, double[] coeff) {
		double y = 0.0;
		for (int i = 0; i < coeff.length; i++) {
			y += (coeff[i] * Math.pow(x, (coeff.length - (i + 1))));
		}
		return y;
	}

	public static void validatePolarDocument(String fName)
			throws Exception {
//  URL validatorStream = new File(SCHEMA_LOCATION).toURL();
		URL validatorStream = PolarSmoother.class.getResource(SCHEMA_LOCATION);
		File source = new File(fName);
		URL docToValidate = source.toURI().toURL();
		//  File sourceDir           = source.getParentFile();

		parser.showWarnings(true);
		parser.setErrorStream(System.out);
		parser.setValidationMode(XMLParser.SCHEMA_VALIDATION);
		parser.setPreserveWhitespace(true);
		XSDBuilder xsdBuilder = new XSDBuilder();
		InputStream is = validatorStream.openStream();
		XMLSchema xmlSchema = (XMLSchema) xsdBuilder.build(is, null);
		parser.setXMLSchema(xmlSchema);

		URL doc = docToValidate;

		parser.parse(doc);
		/* XMLDocument valid = */
		parser.getDocument();
		System.out.println(source.getName() + " is valid");
		Constants.setLastOpenFile(fName);
		Constants.storeConstants();
	}


	public static void generateXMLforObj(String model,
	                                     FileWriter fw,
	                                     double[][] coeffDeg,
	                                     int req_degree_polar,
	                                     int minTWA,
	                                     int maxTWA)
			throws Exception {
		Map<String, CoeffForPolars> map = new HashMap<String, CoeffForPolars>();
		map.put(model, new CoeffForPolars(coeffDeg, req_degree_polar, minTWA, maxTWA));
		generateXMLforObj(model, fw, map);
	}

	public static void generateXMLforObj(String model,
	                                     FileWriter fw,
	                                     Map<String, CoeffForPolars> map)
			throws Exception {
		// Locale, for the dot or comma separator for the decimals
		Locale locale = Locale.getDefault();
		Locale.setDefault(new Locale("en", "US"));

		fw.write("<data xmlns=\"http://donpedro.lediouris.net/wireframe\" name=\"" + model + "\">\n");
		DecimalFormat fmt = new DecimalFormat("##0.000000000");
		// Axis
		fw.write("  <keel>\n");
		fw.write("	  <plot x=\"0\">\n");
		fw.write("      <z>0</z>\n");
		fw.write("    </plot>\n");
		fw.write("    <plot x=\"" + Integer.toString(Constants.getMaxWindSpeed() + Constants.getMinWindSpeed()) + "\">\n");
		fw.write("      <z>0</z>\n");
		fw.write("    </plot>\n");
		fw.write("  </keel>\n");

		fw.write("  <forms>\n");
		// Polars
		Set<String> keys = map.keySet();
		for (String k : keys) {
//    System.out.println("3D Generation, getting data for [" + k + "]");
			CoeffForPolars cf3d = map.get(k);
			int req_degree_polar = cf3d.getPolarDegree();
			double[][] coeffDeg = cf3d.getCoeffDeg();
			int minTWA = cf3d.getFromTwa();
			int maxTWA = cf3d.getToTwa();

			double[] actualCoeff = new double[req_degree_polar + 1];

			for (int ws = Constants.getMinWindSpeed(); ws <= Constants.getMaxWindSpeed(); ws++) {
				for (int j = 0; j < (req_degree_polar + 1); j++) {
					actualCoeff[j] = f(/*(double)*/ws, coeffDeg[j]);
				}

				fw.write("    <form x=\"" + ws + "\">\n");
				int id = 0;
				for (int twa = minTWA; twa <= maxTWA; twa += 2) {
					double bsp = f(/*(double)*/twa, actualCoeff);
					fw.write("      <plot id=\"" + (++id) + "\">\n");
					fw.write("        <y>" +
							fmt.format(bsp * Math.sin(Math.toRadians(/*(double)*/twa))) +
							"</y>\n");
					fw.write("        <z>" +
							fmt.format(bsp * Math.cos(Math.toRadians(/*(double)*/twa))) +
							"</z>\n");
					fw.write("      </plot>\n");
				}
				fw.write("    </form>\n");
			}
		}
		fw.write("  </forms>\n");

		fw.write("  <modules>\n");
		keys = map.keySet();
		for (String k : keys) {
//    System.out.println("3D Generation, getting data for [" + k + "]");
			CoeffForPolars cf3d = map.get(k);
			int req_degree_polar = cf3d.getPolarDegree();
			double[][] coeffDeg = cf3d.getCoeffDeg();
			int minTWA = cf3d.getFromTwa();
			int maxTWA = cf3d.getToTwa();

			double[] actualCoeff = new double[req_degree_polar + 1];

			// Diagonals
			int step = 30;
			for (int twa = minTWA; twa <= maxTWA; twa += step) {
				fw.write("    <module name=\"Diagonal-" + Integer.toString(twa) +
						"\" symetric=\"yes\">\n");
				int plot = 0;
				for (int ws = Constants.getMinWindSpeed(); ws <= Constants.getMaxWindSpeed(); ws++) {
					for (int j = 0; j < (req_degree_polar + 1); j++)
						actualCoeff[j] = f(/*(double)*/ws, coeffDeg[j]);
					double bsp = f(/*(double)*/twa, actualCoeff);
					fw.write("      <plot id=\"" + (++plot) + "\">\n");
					fw.write("        <x>" + Integer.toString(ws) + "</x>\n");
					fw.write("        <y>" +
							fmt.format(bsp * Math.sin(Math.toRadians(/*(double)*/twa))) +
							"</y>\n");
					fw.write("        <z>" +
							fmt.format(bsp * Math.cos(Math.toRadians(/*(double)*/twa))) +
							"</z>\n");
					fw.write("      </plot>\n");
				}
				fw.write("    </module>\n");
			}

			if (maxTWA % step != 0) {
				fw.write("    <module name=\"Diagonal-" + Integer.toString(maxTWA) +
						"\" symetric=\"yes\">\n");
				int plot = 0;
				for (int ws = Constants.getMinWindSpeed(); ws <= Constants.getMaxWindSpeed(); ws++) {
					for (int j = 0; j < (req_degree_polar + 1); j++) {
						actualCoeff[j] = f(/*(double)*/ws, coeffDeg[j]);
					}
					double bsp = f(/*(double)*/maxTWA, actualCoeff);
					fw.write("      <plot id=\"" + (++plot) + "\">\n");
					fw.write("        <x>" + Integer.toString(ws) + "</x>\n");
					fw.write("        <y>" + fmt.format(bsp * Math.sin(Math.toRadians(/*(double)*/maxTWA))) + "</y>\n");
					fw.write("        <z>" + fmt.format(bsp * Math.cos(Math.toRadians(/*(double)*/maxTWA))) + "</z>\n");
					fw.write("      </plot>\n");
				}
				fw.write("    </module>\n");
			}
		}
		fw.write("  </modules>\n");
		fw.write("</data>\n");
		fw.flush();
		fw.close();
		Locale.setDefault(locale); // set it back
	}

	public static class LocalNSResolver
			implements NSResolver {
		Hashtable<String, String> nsHash = new Hashtable<String, String>();

		// Return namespace, after the prefix

		public String resolveNamespacePrefix(String prefix) {
			return nsHash.get(prefix);
		}

		public void addNamespacePrefix(String prefix, String ns) {
			nsHash.put(prefix, ns);
		}
	}

	public static void main(String[] args) {
		System.out.println("-------------------------------------");
		System.out.println("Starting: " + PRODUCT_ID);
		System.out.println("-------------------------------------");
		// Read config file(s)
		Constants.readConstants();
		try {
			if (System.getProperty("swing.defaultlaf") == null)
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}
		new PolarSmootherGUI();
	}
}
