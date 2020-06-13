package xml;

import oracle.xml.parser.v2.DOMParser;
import oracle.xml.parser.v2.XMLDocument;
import oracle.xml.parser.v2.XMLElement;
import oracle.xml.parser.v2.XSLProcessor;
import oracle.xml.parser.v2.XSLStylesheet;
import org.w3c.dom.Text;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;

public class parsingSampleTwo {

	// XML to JSON using XSLT
	public static void main(String... args) {
		try {
			XMLDocument doc;
			DOMParser parser = new DOMParser();
			parser.parse(new FileReader("sampledata.xml"));
			doc = parser.getDocument();

			URL xslURL = new File("xmltojson.xsl").toURI().toURL();
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
			//  processor.setParam("xmlnx:url", "prm1", "value1");
			processor.processXSL(xslss, doc, System.out);

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
