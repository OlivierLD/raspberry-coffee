package polarmaker.polars.smooth.gui.components.tree;

import oracle.xml.parser.v2.DOMParser;
import oracle.xml.parser.v2.XMLDocument;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.swing.JOptionPane;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class JTreeUtil {
	public static PolarTreeNode buildTree(PolarTreeNode root,
	                                      String fName) {
		DOMParser parser = new DOMParser();
		try {
			try {
				parser.parse(new FileInputStream(fName));
			} catch (FileNotFoundException fnfe) {
				System.out.println(fName + " not found...");
				return (root == null ? new PolarTreeNode("Polars") : root);
			} catch (Exception other) {
				System.err.println(other.toString());
				JOptionPane.showMessageDialog(null, other.toString(), "Parsing Tree", JOptionPane.ERROR_MESSAGE);
				return null;
			}
			XMLDocument doc = parser.getDocument();
			Node docRoot = (Node) doc.getDocumentElement();
			String modelName = "";
			if (!docRoot.getNodeName().equals("polar-data")) {
				JOptionPane.showMessageDialog(null, "Unexpected root " + docRoot.getNodeName() + " in " + fName, "Parsing Tree", JOptionPane.ERROR_MESSAGE);
				System.out.println("Unexpected root " + docRoot.getNodeName() + " in " + fName);
				return null;
			} else {
				modelName = docRoot.getAttributes().getNamedItem("model").getNodeValue();
			}
			if (root == null) {
				root = new PolarTreeNode(modelName);
			}

//    System.out.println("Building JTree from XML");
			NodeList nodes = docRoot.getChildNodes();
			if (nodes != null && nodes.getLength() > 0) {
				for (int i = 0; i < nodes.getLength(); i++) {
					Node n = nodes.item(i);
					xmlToJTree(root, n);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return root;
	}

	private static void xmlToJTree(PolarTreeNode parent, Node node) throws Exception {
		if (node.getNodeName().equals("polar-data")) {
			NamedNodeMap nnm = node.getAttributes();
			String name = nnm.getNamedItem("model").getNodeValue();
			PolarTreeNode model = new PolarTreeNode(name);
			parent.add(model);
			NodeList nl = node.getChildNodes();
			if (nl != null) {
				for (int i = 0; i < nl.getLength(); i++) {
					xmlToJTree(model, nl.item(i));
				}
			}
		} else if (node.getNodeName().equals("polar-section")) {
			NamedNodeMap nnm = node.getAttributes();
			String name = nnm.getNamedItem("name").getNodeValue();
			try {
				PolarTreeNode model = new PolarTreeNode(name,
						Integer.parseInt(nnm.getNamedItem("polar-degree").getNodeValue()),
						Integer.parseInt(nnm.getNamedItem("coeff-degree").getNodeValue()),
						Integer.parseInt(nnm.getNamedItem("from-twa").getNodeValue()),
						Integer.parseInt(nnm.getNamedItem("to-twa").getNodeValue()));
				parent.add(model);
				NodeList nl = node.getChildNodes();
				if (nl != null) {
					for (int i = 0; i < nl.getLength(); i++) {
						xmlToJTree(model, nl.item(i));
					}
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		} else if (node.getNodeName().equals("tws")) {
			NamedNodeMap nnm = node.getAttributes();
			double value_1 = Double.parseDouble(nnm.getNamedItem("value").getNodeValue());

			double value_2 = Double.MIN_VALUE;
			double value_3 = Double.MIN_VALUE;
			double value_4 = Double.MIN_VALUE;
			double value_5 = Double.MIN_VALUE;
			double value_6 = Double.MIN_VALUE;
			double value_7 = Double.MIN_VALUE;

			try {
				value_2 = Double.parseDouble(nnm.getNamedItem("upwind-speed").getNodeValue());
			} catch (Exception ignore) {
			}
			try {
				value_3 = Double.parseDouble(nnm.getNamedItem("upwind-twa").getNodeValue());
			} catch (Exception ignore) {
			}
			try {
				value_4 = Double.parseDouble(nnm.getNamedItem("upwind-vmg").getNodeValue());
			} catch (Exception ignore) {
			}
			try {
				value_5 = Double.parseDouble(nnm.getNamedItem("downwind-speed").getNodeValue());
			} catch (Exception ignore) {
			}
			try {
				value_6 = Double.parseDouble(nnm.getNamedItem("downwind-twa").getNodeValue());
			} catch (Exception ignore) {
			}
			try {
				value_7 = Double.parseDouble(nnm.getNamedItem("downwind-vmg").getNodeValue());
			} catch (Exception ignore) {
			}
			PolarTreeNode tws = new PolarTreeNode(value_1,
					value_2,
					value_3,
					value_4,
					value_5,
					value_6,
					value_7);
			parent.add(tws);
			NodeList nl = node.getChildNodes();
			if (nl != null) {
				for (int i = 0; i < nl.getLength(); i++) {
					xmlToJTree(tws, nl.item(i));
				}
			}
		} else if (node.getNodeName().equals("twa")) {
			NamedNodeMap nnm = node.getAttributes();
			String twa = nnm.getNamedItem("value").getNodeValue();
			String bsp = nnm.getNamedItem("bsp").getNodeValue();
			int i_twa = Integer.parseInt(twa);
			double d_bsp = Double.parseDouble(bsp);
			PolarTreeNode wa = new PolarTreeNode(i_twa, d_bsp);
			parent.add(wa);
			NodeList nl = node.getChildNodes();
			if (nl != null) { // Should not happen
				for (int i = 0; i < nl.getLength(); i++) {
					xmlToJTree(wa, nl.item(i));
				}
			}
		}
	}
}
