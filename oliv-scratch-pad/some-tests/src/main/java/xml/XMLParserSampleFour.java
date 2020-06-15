package xml;

import oracle.xml.parser.v2.DOMParser;
import oracle.xml.parser.v2.NSResolver;
import oracle.xml.parser.v2.XMLDocument;
import oracle.xml.parser.v2.XMLElement;
import oracle.xml.parser.v2.XSLException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.net.URL;
import java.util.Hashtable;

// Parsing an XML Schema to have its structure
public class XMLParserSampleFour {

	private static boolean justUntypedNamedElements = true;

	private static final String SCHEMA_LOCATION = "xml" + File.separator + "wireframe.xsd";
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

	private static String lpad(String s, String with, int len) {
		String str = s;
		while (str.length() < len) {
			str = with + str;
		}
		return str;
	}

	static void drillDown(XMLElement from, XMLDocument doc, int level) throws XSLException {

		Node name = from.getAttributes().getNamedItem("name");
		Node type = from.getAttributes().getNamedItem("type");
		Node ref = from.getAttributes().getNamedItem("ref");

		if (!justUntypedNamedElements) {
			System.out.println(String.format("(%s) -> %s %s: %s, name:%s, type:%s, ref:%s",
					lpad(String.valueOf(level), "0", 2),
					lpad("", "-", 2 * level),
					from.getClass().getName(),
					from.getNodeName(),
					(name != null ? name.getNodeValue() : "[no name]"),
					(type != null ? type.getNodeValue() : "[no type]"),
					(ref != null ? ref.getNodeValue() : "[no ref]")));
		} else if (justUntypedNamedElements && name != null && type == null) {
			System.out.println(String.format("(%s) -> %s: name:%s",
					lpad(String.valueOf(level), "0", 2),
					lpad("", "-", 2 * level),
					name.getNodeValue()));
		}
		// Has an xsd:ref?
		if (ref != null) {
			String refName = ref.getNodeValue();
			// System.out.println(String.format(" ++ (%d) %s Resolving %s", level, lpad("", "-", 2 * level), refName));
			NodeList references = doc.selectNodes(String.format("/xsd:schema/xsd:element[@name='%s']", refName), resolver);
			for (int i=0; i<references.getLength(); i++) {
				Node node = references.item(i);
				if (node instanceof XMLElement) {
					drillDown((XMLElement)node, doc, level + 1);
				} else {
					// WTF??
					System.err.println(String.format("!! %s id not an XMLElement, it is a %s...", node.getNodeName(), node.getClass().getName()));
				}
			}
		} else if (type == null) {
			NodeList children = from.selectNodes("./xsd:*", resolver);
			for (int i=0; i<children.getLength(); i++) {
				Node node = children.item(i);
				if (node instanceof XMLElement) {
					drillDown((XMLElement)node, doc, level + 1);
				} else {
					// WTF??
					System.err.println(String.format("!! %s id not an XMLElement, it is a %s...", node.getNodeName(), node.getClass().getName()));
				}
			}
		} else if (type != null) {
			System.out.println(String.format(" >> (%s) %s - Resolving element %s => %s",
					lpad(String.valueOf(level), "0", 2),
					lpad("", "-",2 * (level + 1)),
					(name == null ? "[no-name]" : name.getNodeValue()),
					type.getNodeValue()));
			// Check if the type is in this schema
			NodeList references = doc.selectNodes(String.format("/xsd:schema//xsd:complexType[@name='%s']", type.getNodeValue()), resolver);
			for (int i = 0; i < references.getLength(); i++) {
				Node node = references.item(i);
				if (node instanceof XMLElement) {
					drillDown((XMLElement) node, doc, level + 1);
				} else {
					// WTF??
					System.err.println(String.format("!! %s id not an XMLElement, it is a %s...", node.getNodeName(), node.getClass().getName()));
				}
			}
		}
	}

	public static void main(String... args)  throws Exception {

		resolver = new CustomResolver();
		resolver.addNamespacePrefix("xsd", XSD_NAMESPACE); // Will be used in selectNodes

		URL schemaUrl = new File(SCHEMA_LOCATION).toURI().toURL();

		DOMParser parser = new DOMParser();
		parser.showWarnings(true);
		parser.setErrorStream(System.out);
		parser.setValidationMode(DOMParser.SCHEMA_VALIDATION);
		parser.setPreserveWhitespace(true);

		// TODO Validate the schema structure, against the schema's schema.

		// Parse the Schema
		parser.parse(schemaUrl);
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
				drillDown((XMLElement)node, parsedSchema, 1);
			} else {
				// WTF??
				System.err.println(String.format("!! %s id not an XMLElement, it is a %s...", node.getNodeName(), node.getClass().getName()));
			}
		}
		System.out.println("Done");
	}
}
