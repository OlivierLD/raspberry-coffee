package xml;

import oracle.xml.parser.v2.DOMParser;
import oracle.xml.parser.v2.XMLDocument;
import oracle.xml.parser.v2.XMLElement;
import oracle.xml.parser.v2.XSLProcessor;
import oracle.xml.parser.v2.XSLStylesheet;
import org.w3c.dom.Text;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;

public class XMLParserSampleOne {
	// Generate an XML Doc from scratch, and transform it into HTL, using XSL

	private static String title = "De l'ile d'Ouessant a la Pointe de Penmarc'h";
	private static String provider = "SHOM";
	private static Integer chartNo = 5316;
	private static Integer year = 1976;

	public static void openInBrowser(String page) throws Exception {
		String os = System.getProperty("os.name");
		if (os.indexOf("Windows") > -1) {
			String cmd = "";
			if (page.indexOf(" ") != -1) {
				cmd = "cmd /k start \"" + page + "\"";
			} else {
				cmd = "cmd /k start " + page + "";
			}
			System.out.println("Command:" + cmd);
			Runtime.getRuntime().exec(cmd); // Can contain blanks...
		} else if (os.indexOf("Linux") > -1) { // Assuming htmlview
			Runtime.getRuntime().exec("htmlview " + page);
		} else if (os.indexOf("Mac") > -1) {
			Runtime.getRuntime().exec("open " + page);
		} else {
			throw new RuntimeException("OS [" + os + "] not supported yet");
		}
	}

	public static void main(String... args) {
		XMLDocument doc = new XMLDocument();
		XMLElement root = (XMLElement) doc.createElement("selection-root");
		doc.appendChild(root);
		XMLElement chart = (XMLElement) doc.createElement("chart");
		chart.setAttribute("chart-no", chartNo.toString());
		chart.setAttribute("provider", provider);
		chart.setAttribute("year", year.toString());
		Text txt = doc.createTextNode("text#");
		chart.appendChild(txt);
		txt.setNodeValue(title);
		root.appendChild(chart);
		try {
			doc.print(System.out);
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}

		try {
			// in HTML
			URL xslURL = new File("xml" + File.separator + "charthtml.xsl").toURI().toURL();
			//  System.out.println("Transforming using " + xslURL.toString());
			DOMParser parser = new DOMParser();
			parser.parse(xslURL);
			XMLDocument xsldoc = parser.getDocument();
			// instantiate a stylesheet
			XSLProcessor processor = new XSLProcessor();
			processor.setBaseURL(xslURL);
			XSLStylesheet xslss = processor.newXSLStylesheet(xsldoc);

			// display any warnings that may occur
			processor.showWarnings(true);
			processor.setErrorStream(System.err);

			// Process XSL
			PrintWriter pw = new PrintWriter(new File("xml" + File.separator + "selection.html"));
			//  processor.setParam("xmlnx:url", "prm1", "value1");
			processor.processXSL(xslss, doc, pw);
			pw.close();
			openInBrowser("xml" + File.separator + "selection.html");

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
