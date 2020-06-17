package utils;

import oracle.xml.parser.v2.DOMParser;
import oracle.xml.parser.v2.NSResolver;
import oracle.xml.parser.v2.XMLDocument;
import oracle.xml.parser.v2.XMLElement;
import oracle.xml.parser.v2.XSLException;
import oracle.xml.parser.v2.XSLProcessor;
import oracle.xml.parser.v2.XSLStylesheet;
import oracle.xml.util.XMLException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.Map;

public class XMLUtils {

	private static boolean justUntypedNamedElements = "true".equals(System.getProperty("narrow.it", "true"));

	private static final String XSD_NAMESPACE = "http://www.w3.org/2001/XMLSchema";

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
			nsHash = new Hashtable<String, String>();
		}
	}

	static CustomResolver resolver;

	static {
		resolver = new CustomResolver();
		resolver.addNamespacePrefix("xsd", XSD_NAMESPACE); // Will be used in selectNodes
	}

	static DOMParser parser = new DOMParser();

	static {
		try {
			parser.showWarnings(true);
			parser.setErrorStream(System.out);
			parser.setValidationMode(DOMParser.SCHEMA_VALIDATION);
			parser.setPreserveWhitespace(true);
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	private static String lpad(String s, String with, int len) {
		String str = s;
		while (str.length() < len) {
			str = with + str;
		}
		return str;
	}

	static Map<String, Object> drillDown(XMLElement from, XMLDocument doc, int level) throws XSLException {

		Map<String, Object> map = new LinkedHashMap<>();

		Node nameAtt = from.getAttributes().getNamedItem("name");
		Node typeAtt = from.getAttributes().getNamedItem("type");
		Node refAtt = from.getAttributes().getNamedItem("ref");

		String DEFAULT_NODE_NAME = "no-node-name";
		String nodeName = DEFAULT_NODE_NAME;
		if (!justUntypedNamedElements) {
			System.out.println(String.format("A. (%s) -> %s %s: %s, name:%s, type:%s, ref:%s",
					lpad(String.valueOf(level), "0", 2),
					lpad("", "-", 2 * level),
					from.getClass().getName(),
					from.getNodeName(),
					(nameAtt != null ? nameAtt.getNodeValue() : "[no name]"),
					(typeAtt != null ? typeAtt.getNodeValue() : "[no type]"),
					(refAtt != null ? refAtt.getNodeValue() : "[no ref]")));
		} else if (justUntypedNamedElements && nameAtt != null && typeAtt == null) {
			System.out.println(String.format("B. (%s) -> %s name:%s",
					lpad(String.valueOf(level), "0", 2),
					lpad("", "-", 2 * level),
					nameAtt.getNodeValue()));
			nodeName = nameAtt.getNodeValue();
		}
		// Has an xsd:ref?
		if (refAtt != null) {
			String refName = refAtt.getNodeValue();
			// System.out.println(String.format(" ++ (%d) %s Resolving %s", level, lpad("", "-", 2 * level), refName));
			NodeList references = doc.selectNodes(String.format("/xsd:schema/xsd:element[@name='%s']", refName), resolver);
			for (int i=0; i<references.getLength(); i++) {
				Node node = references.item(i);
				if (node instanceof XMLElement) {
					// TODO Fix that one too
					map.put(nodeName + "-1-(" + i + ")" + node.getNodeName(), drillDown((XMLElement)node, doc, level + 1));
				} else {
					// WTF??
					System.err.println(String.format("!! %s id not an XMLElement, it is a %s...", node.getNodeName(), node.getClass().getName()));
				}
			}
		} else if (typeAtt == null) {
			NodeList children = from.selectNodes("./xsd:*", resolver);
			for (int i=0; i<children.getLength(); i++) {
				Node node = children.item(i);
				if (node instanceof XMLElement) {
					String nodeKey = "";
					if (!nodeName.equals(DEFAULT_NODE_NAME)) {
						nodeKey = "N:" + nodeName;
					} else {
						nodeKey = (nameAtt == null ? "XSD:" /*"[no-name]"*/ : "V:" + nameAtt.getNodeValue());
					}
					nodeKey += ((nodeKey.length() == 0 ? "" : " ") + "(#" + String.valueOf(i) + ", " + node.getNodeName() + ") [B]");

					map.put(nodeKey, drillDown((XMLElement)node, doc, level + 1));
				} else {
					// WTF??
					System.err.println(String.format("!! %s id not an XMLElement, it is a %s...", node.getNodeName(), node.getClass().getName()));
				}
			}
		} else if (typeAtt != null) {
			System.out.println(String.format("C. (%s) %s- Resolving element %s => %s",
					lpad(String.valueOf(level), "0", 2),
					lpad("", "-",2 * (level + 1)),
					(nameAtt == null ? "[no-name]" : nameAtt.getNodeValue()),
					typeAtt.getNodeValue()));
			String tNodeKey = "";
			if (!nodeName.equals(DEFAULT_NODE_NAME)) {
				tNodeKey = "N:" + nodeName + " (T)";
			} else {
				tNodeKey = "T:" + nameAtt.getNodeValue();
			}
			tNodeKey += " [C]";
			map.put(tNodeKey, typeAtt.getNodeValue());
			/*      |         |
			 *      |         InvoiceType
			 *      Invoice
			 *
			 */
			// Check if the type is in this schema
			NodeList references = doc.selectNodes(String.format("/xsd:schema//xsd:complexType[@name='%s']", typeAtt.getNodeValue()), resolver);
			for (int i = 0; i < references.getLength(); i++) {
				Node node = references.item(i);
				if (node instanceof XMLElement) {
					String nodeKey = "";
					if (!nodeName.equals(DEFAULT_NODE_NAME)) {
						nodeKey = ("N:" + nodeName);
					} else {
						nodeKey = "V:" + (nameAtt == null ? "[no-name]" : nameAtt.getNodeValue());
					}
					nodeKey += (" (#" + String.valueOf(i) + ") [A]");

					map.put(nodeKey, drillDown((XMLElement) node, doc, level + 1)); // TODO type.getNodeValue()
				} else {
					// WTF??
					System.err.println(String.format("!! %s id not an XMLElement, it is a %s...", node.getNodeName(), node.getClass().getName()));
				}
			}
		}
		return map;
	}

	public static Map<String, Object> processSchema(byte[] schemaContent) throws IOException, XMLException, XSLException, SAXException {
		Map<String, Object> map = new LinkedHashMap<>();

		parser.parse(new StringReader(new String(schemaContent)));
		XMLDocument parsedSchema = parser.getDocument();

		Element documentElement = parsedSchema.getDocumentElement();
		String rootTagName = documentElement.getTagName();
		System.out.println(String.format("Document root is %s (tag), %s (local), %s (node)", rootTagName, documentElement.getLocalName(), documentElement.getNodeName()));

		// Get first children list
		NodeList firstChildList = parsedSchema.selectNodes("/xsd:schema/xsd:element", resolver);
		System.out.println(String.format("Root (element) children: %d", firstChildList.getLength()));
		for (int i=0; i<firstChildList.getLength(); i++) {
			Node node = firstChildList.item(i);
			if (node instanceof XMLElement) {
				map.put("Item-" + String.valueOf(i), drillDown((XMLElement)node, parsedSchema, 1));
			} else {
				// WTF??
				System.err.println(String.format("!! %s id not an XMLElement, it is a %s...", node.getNodeName(), node.getClass().getName()));
			}
		}
		return map;
	}

	public static byte[] applyStylesheet(byte[] xmlContent) throws XSLException, IOException, SAXException {
		URL xslURL = XMLUtils.class.getResource("xml2json.xsl");
		parser.parse(xslURL);
		XMLDocument xsldoc = parser.getDocument();
		// instantiate a stylesheet
		XSLProcessor processor = new XSLProcessor();
		processor.setBaseURL(xslURL);
		XSLStylesheet xslss = processor.newXSLStylesheet(xsldoc);

		// display any warnings that may occur
		processor.showWarnings(true);
		processor.setErrorStream(System.err);

		parser.parse(new StringReader(new String(xmlContent)));
		XMLDocument doc = parser.getDocument();

		// Process XSL
		//  processor.setParam("xmlnx:url", "prm1", "value1");
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		processor.processXSL(xslss, doc, baos);
		byte[] result = baos.toByteArray();
		return result;
	}
}
