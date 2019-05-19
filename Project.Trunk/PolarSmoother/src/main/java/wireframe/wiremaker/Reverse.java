package wireframe.wiremaker;

import oracle.xml.parser.v2.XMLDocument;
import oracle.xml.parser.v2.XMLElement;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;

public class Reverse {
	private final static String NS_URI = "http://donpedro.lediouris.net/wireframe";
//private static DOMParser parser = new DOMParser();

	private final static int KEEL = 1;
	private final static int SHEER = 2;
	private final static int DECK = 3;
	private final static int FORM = 4;
	private final static int BUTTOCK = 5;
	private final static int WATERLINE = 6;
	private final static int MODULE = 7;

	public static void main(String[] args) {
		int currentType = 0;
		try {
			if (args.length < 2) {
				System.out.println("Provide the obj file name as 1st parameter");
				System.exit(1);
			}
			String dataName = args[0].substring(args[0].lastIndexOf(File.separator) + 1);
			dataName = dataName.substring(0, dataName.indexOf("."));
			System.out.println("Data:" + dataName);

			XMLDocument xml = new XMLDocument();
			XMLElement root = (XMLElement) xml.createElementNS(NS_URI, "data");
			xml.appendChild(root);
			root.setAttribute("name", dataName);

			XMLElement subRoot = null;

			BufferedReader br = new BufferedReader(new FileReader(args[0]));
			String line = "";
			while ((line = br.readLine()) != null) {
				if (line.startsWith("#")) {
					if (line.startsWith("# File")) {

					} else if (line.startsWith("# Keel")) {
						currentType = KEEL;
						subRoot = (XMLElement) xml.createElementNS(NS_URI, "keel");
						root.appendChild(subRoot);
					} else if (line.startsWith("# Deck")) {
						currentType = DECK;
						subRoot = (XMLElement) xml.createElementNS(NS_URI, "deck");
						root.appendChild(subRoot);
						XMLElement subRoot2 = (XMLElement) xml.createElementNS(NS_URI, "part");
						subRoot2.setAttribute("id", "1");
						subRoot.appendChild(subRoot2);
						subRoot = subRoot2;
					} else if (line.startsWith("# Sheer")) {
						currentType = SHEER;
						subRoot = (XMLElement) xml.createElementNS(NS_URI, "sheer");
						root.appendChild(subRoot);
					} else if (line.startsWith("# Form")) {
						currentType = FORM;
					} else if (line.startsWith("# WaterLine")) {
						currentType = WATERLINE;
					} else if (line.startsWith("# Buttock")) {
						currentType = BUTTOCK;
					} else if (line.startsWith("# Module")) {
						currentType = MODULE;
					} else {
						System.out.println("Unkown : " + line);
					}
				} else { // Data
					if (line.startsWith("v ")) {
						String[] data = line.split(" ");
						double x = Double.parseDouble(data[1]);
						double y = Double.parseDouble(data[2]);
						double z = Double.parseDouble(data[3]);

						switch (currentType) {
							case KEEL: {
								XMLElement plot = (XMLElement) xml.createElementNS(NS_URI, "plot");
								subRoot.appendChild(plot);
								plot.setAttribute("x", Double.toString(x));
								XMLElement cote = (XMLElement) xml.createElementNS(NS_URI, "z");
								plot.appendChild(cote);
								Text txt = xml.createTextNode("#text");
								txt.setNodeValue(Double.toString(z));
								cote.appendChild(txt);
							}
							break;
							case SHEER: {
								XMLElement plot = (XMLElement) xml.createElementNS(NS_URI, "plot");
								subRoot.appendChild(plot);
								plot.setAttribute("x", Double.toString(x));
								XMLElement ord = (XMLElement) xml.createElementNS(NS_URI, "y");
								plot.appendChild(ord);
								Text txt = xml.createTextNode("#text");
								txt.setNodeValue(Double.toString(y));
								ord.appendChild(txt);
								XMLElement cote = (XMLElement) xml.createElementNS(NS_URI, "z");
								plot.appendChild(cote);
								txt = xml.createTextNode("#text");
								txt.setNodeValue(Double.toString(z));
								cote.appendChild(txt);
							}
							break;
							case DECK: {
								XMLElement plot = (XMLElement) xml.createElementNS(NS_URI, "plot");
								subRoot.appendChild(plot);
								plot.setAttribute("x", Double.toString(x));
								XMLElement cote = (XMLElement) xml.createElementNS(NS_URI, "z");
								plot.appendChild(cote);
								Text txt = xml.createTextNode("#text");
								txt.setNodeValue(Double.toString(z));
								cote.appendChild(txt);
							}
							break;
							// TODO Implement...
							case FORM:
							case WATERLINE:
							case BUTTOCK:
							case MODULE:
							default:
								System.out.println("Later...");
								break;
						}
					}
				}
			}
			br.close();

			FileOutputStream fos = new FileOutputStream(args[1]);
			xml.print(fos);
			fos.close();
			xml.print(System.out);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
