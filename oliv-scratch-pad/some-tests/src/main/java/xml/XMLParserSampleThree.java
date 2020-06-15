package xml;

import oracle.xml.parser.schema.XMLSchema;
import oracle.xml.parser.schema.XSDBuilder;
import oracle.xml.parser.v2.DOMParser;
import oracle.xml.parser.v2.NSResolver;
import oracle.xml.parser.v2.XMLDocument;
import oracle.xml.parser.v2.XMLElement;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.net.URL;
import java.util.Hashtable;

// Use a NameSpace Resolver, and XSD Validation
public class XMLParserSampleThree {
	private static int startFrom;
	private static final String SCHEMA_LOCATION = "xml" + File.separator + "wireframe.xsd";
	private static final String NAMESPACE = "http://donpedro.lediouris.net/wireframe";

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


	public static int generate(String fName) {
		return generate(fName,
				null,
				0.0D,
				0.0D,
				0.0D,
				1.0D,
				1.0D,
				1.0D,
				0);
	}

	public static int generate(String fName, FileWriter df) {
		return generate(fName,
				df,
				0.0D,
				0.0D,
				0.0D,
				1.0D,
				1.0D,
				1.0D,
				0);
	}

	public static int generate(String fName,
	                           FileWriter destFile,
	                           double addX,
	                           double addY,
	                           double addZ,
	                           double affX,
	                           double affY,
	                           double affZ,
	                           int startOffset) {
		File sourceDir = null;
		File source = null;
		startFrom = startOffset;
		DOMParser parser = new DOMParser();
		try {
			URL validatorStream = // new XMLParserSampleThree().getClass().getResource(SCHEMA_LOCATION);
					new File(SCHEMA_LOCATION).toURI().toURL();
			if (validatorStream == null) {
				throw new RuntimeException(String.format("Problem finding %s", SCHEMA_LOCATION));
			}
			source = new File(fName);
			URL docToValidate = source.toURI().toURL();
			sourceDir = source.getParentFile();
			parser.showWarnings(true);
			parser.setErrorStream(System.out);
			parser.setValidationMode(DOMParser.SCHEMA_VALIDATION);
			parser.setPreserveWhitespace(true);
			XSDBuilder xsdBuilder = new XSDBuilder();
			InputStream is = validatorStream.openStream();
			XMLSchema xmlSchema = (XMLSchema)xsdBuilder.build(is, null);
			parser.setXMLSchema(xmlSchema);
			URL doc = docToValidate;
			parser.parse(doc);
			/* XMLDocument valid = */
			parser.getDocument();
			if ("true".equals(System.getProperty("verbose", "false"))) {
				System.out.println("In ObjectMaker - " + source.getName() + " is valid");
			}
		} catch (Exception ex) {
			System.out.println(source.getName() + " is invalid...");
			ex.printStackTrace();
//          System.exit(1);
		}
		try {
			parser.parse(new FileReader(fName));
			XMLDocument doc = parser.getDocument();
			FileWriter fw = null;
			if (destFile == null) {
				String outputName = doc.getDocumentElement().getAttribute("name");
				String outputLocation = fName.substring(0, fName.lastIndexOf(File.separator) + 1);
				if ("true".equals(System.getProperty("verbose", "false"))) {
					System.out.println("Name:" + outputName);
					System.out.println("Output: [" + outputLocation + outputName + ".obj]");
				}
				File file = new File(outputLocation + outputName + ".obj");
				fw = new FileWriter(file);
				fw.write("# File " + outputName + "\n");
			} else {
				fw = destFile;
			}
			CustomResolver resolver = new CustomResolver();
			resolver.addNamespacePrefix("wf", NAMESPACE);
			NodeList nl = doc.selectNodes("/wf:data/wf:keel/wf:plot", resolver);
			fw.write("# Keel\n");
			int nbElements = 0;
			if (nl != null) {
				for (int i = 0; i < nl.getLength(); i++) {
					Node node = nl.item(i);
					double x = Double.parseDouble(((Element) node).getAttribute("x"));
					NodeList childs = node.getChildNodes();
					double z = 0.0D;
					if (childs != null) {
						for (int j = 0; j < childs.getLength(); j++) {
							Node kid = childs.item(j);
							if (kid.getNodeType() == 1) {
								z = Double.parseDouble(kid.getFirstChild().getNodeValue());
							}
						}
					}
					fw.write("v " + (affX * x + addX) + " " + addY + " " + (affZ * z + addZ) + "\n");
					nbElements++;
				}

				for (int i = 0; i < nbElements - 1; i++) {
					fw.write("f " + ++startFrom + " " + (startFrom + 1) + "\n");
				}
				if (nbElements > 0) {
					startFrom++;
				}
			}
			fw.write("# Deck\n");
			nl = doc.selectNodes("/wf:data/wf:deck/wf:part", resolver);
			nbElements = 0;
			if (nl != null) {
				for (int i = 0; i < nl.getLength(); i++) {
					NodeList pl = ((Element) nl.item(i)).getElementsByTagNameNS("http://donpedro.lediouris.net/wireframe", "plot");
					if (pl == null) {
						continue;
					}
					for (int j = 0; j < pl.getLength(); j++) {
						Node node = pl.item(j);
						double x = Double.parseDouble(((Element) node).getAttribute("x"));
						NodeList childs = node.getChildNodes();
						double z = 0.0D;
						if (childs != null) {
							for (int k = 0; k < childs.getLength(); k++) {
								Node kid = childs.item(k);
								if (kid.getNodeType() == 1) {
									z = Double.parseDouble(kid.getFirstChild().getNodeValue());
									nbElements++;
								}
							}
						}
						fw.write("v " + (x * affX + addX) + " " + addY + " " + (z * affZ + addZ) + "\n");
					}

					for (int j = 0; j < nbElements - 1; j++) {
						fw.write("f " + ++startFrom + " " + (startFrom + 1) + "\n");
					}
					if (nbElements > 0) {
						startFrom++;
					}
				}
			}
			nl = doc.selectNodes("/wf:data/wf:sheer/wf:plot", resolver);
			nbElements = 0;
			if (nl != null) {
				fw.write("# Sheer One\n");
				for (int i = 0; i < nl.getLength(); i++) {
					Node node = nl.item(i);
					if (node.getNodeType() == 1 && node.getNodeName() == "plot") {
						double x = Double.parseDouble(((Element) node).getAttribute("x"));
						double y = Double.parseDouble(((Element) node).getElementsByTagNameNS("http://donpedro.lediouris.net/wireframe", "y").item(0).getFirstChild().getNodeValue());
						double z = Double.parseDouble(((Element) node).getElementsByTagNameNS("http://donpedro.lediouris.net/wireframe", "z").item(0).getFirstChild().getNodeValue());
						fw.write("v " + (x * affX + addX) + " " + (y * affY + addY) + " " + (z * affZ + addZ) + "\n");
						nbElements++;
					}
				}

				for (int i = 0; i < nbElements - 1; i++) {
					fw.write("f " + ++startFrom + " " + (startFrom + 1) + "\n");
				}
				if (nbElements > 0) {
					startFrom++;
				}
				fw.write("# Sheer Two\n");
				nbElements = 0;
				for (int i = 0; i < nl.getLength(); i++) {
					Node node = nl.item(i);
					if (node.getNodeType() == 1 && node.getNodeName() == "plot") {
						double x = Double.parseDouble(((Element) node).getAttribute("x"));
						double y = Double.parseDouble(((Element) node).getElementsByTagNameNS("http://donpedro.lediouris.net/wireframe", "y").item(0).getFirstChild().getNodeValue());
						double z = Double.parseDouble(((Element) node).getElementsByTagNameNS("http://donpedro.lediouris.net/wireframe", "z").item(0).getFirstChild().getNodeValue());
						fw.write("v " + (x * affX + addX) + " " + (addY - y * affY) + " " + (z * affZ + addZ) + "\n");
						nbElements++;
					}
				}

				for (int i = 0; i < nbElements - 1; i++) {
					fw.write("f " + ++startFrom + " " + (startFrom + 1) + "\n");
				}
				if (nbElements > 0) {
					startFrom++;
				}
			}
			nl = doc.selectNodes("/wf:data/wf:forms/wf:form", resolver);
			nbElements = 0;
			if (nl != null) {
				for (int i = 0; i < nl.getLength(); i++) {
					Node node = nl.item(i);
					double x = Double.parseDouble(((Element) node).getAttribute("x"));
					fw.write("# Form " + (addX + x) + "\n");
					NodeList plots = node.getChildNodes();
					nbElements = 0;
					for (int j = 0; j < plots.getLength(); j++) {
						if (plots.item(j).getNodeType() != 1) {
							continue;
						}
						NodeList yz = plots.item(j).getChildNodes();
						double y = Double.MIN_VALUE;
						double z = Double.MIN_VALUE;
						label0:
						for (int k = 0; k < yz.getLength(); k++) {
							if (yz.item(k).getNodeType() != 1) {
								continue;
							}
							NodeList yzKids;
							if (yz.item(k).getNodeName().equals("y")) {
								yzKids = yz.item(k).getChildNodes();
								int l = 0;
								do {
									if (l >= yzKids.getLength()) {
										continue label0;
									}
									if (yzKids.item(l).getNodeType() == 3) {
										y = Double.parseDouble(yzKids.item(l).getNodeValue());
									}
									l++;
								} while (true);
							}
							if (!yz.item(k).getNodeName().equals("z")) {
								continue;
							}
							yzKids = yz.item(k).getChildNodes();
							for (int l = 0; l < yzKids.getLength(); l++) {
								if (yzKids.item(l).getNodeType() == 3) {
									z = Double.parseDouble(yzKids.item(l).getNodeValue());
								}
							}
						}

						if (y != Double.MIN_VALUE && z != Double.MIN_VALUE) {
							fw.write("v " + (x * affX + addX) + " " + (y * affY + addY) + " " + (z * affZ + addZ) + "\n");
							nbElements++;
						}
					}

					for (int j = 0; j < nbElements - 1; j++) {
						fw.write("f " + ++startFrom + " " + (startFrom + 1) + "\n");
					}
					if (nbElements > 0) {
						startFrom++;
					}
					nbElements = 0;
					for (int j = 0; j < plots.getLength(); j++) {
						NodeList yz = plots.item(j).getChildNodes();
						double y = Double.MIN_VALUE;
						double z = Double.MIN_VALUE;
						label1:
						for (int k = 0; k < yz.getLength(); k++) {
							if (yz.item(k).getNodeType() != 1) {
								continue;
							}
							NodeList yzKids;
							if (yz.item(k).getNodeName().equals("y")) {
								yzKids = yz.item(k).getChildNodes();
								int l = 0;
								do {
									if (l >= yzKids.getLength()) {
										continue label1;
									}
									if (yzKids.item(l).getNodeType() == 3) {
										y = Double.parseDouble(yzKids.item(l).getNodeValue());
									}
									l++;
								} while (true);
							}
							if (!yz.item(k).getNodeName().equals("z")) {
								continue;
							}
							yzKids = yz.item(k).getChildNodes();
							for (int l = 0; l < yzKids.getLength(); l++) {
								if (yzKids.item(l).getNodeType() == 3) {
									z = Double.parseDouble(yzKids.item(l).getNodeValue());
								}
							}
						}
						if (y != Double.MIN_VALUE && z != Double.MIN_VALUE) {
							fw.write("v " + (addX + x * affX) + " " + (addY - y * affY) + " " + (addZ + z * affZ) + "\n");
							nbElements++;
						}
					}
					for (int j = 0; j < nbElements - 1; j++) {
						fw.write("f " + ++startFrom + " " + (startFrom + 1) + "\n");
					}
					if (nbElements > 0) {
						startFrom++;
					}
				}

			}
			nl = doc.selectNodes("/wf:data/wf:waterlines/wf:wl", resolver);
			nbElements = 0;
			if (nl != null) {
				for (int i = 0; i < nl.getLength(); i++) {
					Node node = nl.item(i);
					if (node.getNodeType() != 1 || !node.getNodeName().equals("wl")) {
						continue;
					}
					double z = Double.parseDouble(((Element) node).getAttribute("z"));
					fw.write("# WaterLine " + (addZ + z) + "\n");
					NodeList plots = node.getChildNodes();
					nbElements = 0;
					for (int j = 0; j < plots.getLength(); j++) {
						NodeList xy = plots.item(j).getChildNodes();
						double x = Double.MIN_VALUE;
						double y = Double.MIN_VALUE;
						label2:
						for (int k = 0; k < xy.getLength(); k++) {
							if (xy.item(k).getNodeType() != 1) {
								continue;
							}
							NodeList xyKids;
							if (xy.item(k).getNodeName().equals("x")) {
								xyKids = xy.item(k).getChildNodes();
								int l = 0;
								do {
									if (l >= xyKids.getLength()) {
										continue label2;
									}
									if (xyKids.item(l).getNodeType() == 3) {
										x = Double.parseDouble(xyKids.item(l).getNodeValue());
									}
									l++;
								} while (true);
							}
							if (!xy.item(k).getNodeName().equals("y")) {
								continue;
							}
							xyKids = xy.item(k).getChildNodes();
							for (int l = 0; l < xyKids.getLength(); l++) {
								if (xyKids.item(l).getNodeType() == 3) {
									y = Double.parseDouble(xyKids.item(l).getNodeValue());
								}
							}
						}

						if (x != Double.MIN_VALUE && y != Double.MIN_VALUE) {
							fw.write("v " + (x * affX + addX) + " " + (y * affY + addY) + " " + (z * affZ + addZ) + "\n");
							nbElements++;
						}
					}

					for (int j = 0; j < nbElements - 1; j++) {
						fw.write("f " + ++startFrom + " " + (startFrom + 1) + "\n");
					}
					if (nbElements > 0) {
						startFrom++;
					}
					nbElements = 0;
					for (int j = 0; j < plots.getLength(); j++) {
						NodeList xy = plots.item(j).getChildNodes();
						double x = Double.MIN_VALUE;
						double y = Double.MIN_VALUE;
						label3:
						for (int k = 0; k < xy.getLength(); k++) {
							if (xy.item(k).getNodeType() != 1) {
								continue;
							}
							NodeList xyKids;
							if (xy.item(k).getNodeName().equals("x")) {
								xyKids = xy.item(k).getChildNodes();
								int l = 0;
								do {
									if (l >= xyKids.getLength()) {
										continue label3;
									}
									if (xyKids.item(l).getNodeType() == 3) {
										x = Double.parseDouble(xyKids.item(l).getNodeValue());
									}
									l++;
								} while (true);
							}
							if (!xy.item(k).getNodeName().equals("y")) {
								continue;
							}
							xyKids = xy.item(k).getChildNodes();
							for (int l = 0; l < xyKids.getLength(); l++) {
								if (xyKids.item(l).getNodeType() == 3) {
									y = Double.parseDouble(xyKids.item(l).getNodeValue());
								}
							}
						}

						if (x != Double.MIN_VALUE && y != Double.MIN_VALUE) {
							fw.write("v " + (x * affX + addX) + " " + (addY - y * affY) + " " + (addZ + z * affZ) + "\n");
							nbElements++;
						}
					}

					for (int j = 0; j < nbElements - 1; j++) {
						fw.write("f " + ++startFrom + " " + (startFrom + 1) + "\n");
					}
					if (nbElements > 0) {
						startFrom++;
					}
				}
			}
			nl = doc.selectNodes("/wf:data/wf:buttocks/wf:buttock", resolver);
			nbElements = 0;
			if (nl != null) {
				for (int i = 0; i < nl.getLength(); i++) {
					Node node = nl.item(i);
					double y = Double.parseDouble(((Element) node).getAttribute("y"));
					fw.write("# Buttock " + (addY + y) + "\n");
					NodeList parts = node.getChildNodes();
					for (int j = 0; j < parts.getLength(); j++) {
						if (parts.item(j).getNodeType() != 1) {
							continue;
						}
						nbElements = 0;
						NodeList plots = parts.item(j).getChildNodes();
						for (int k = 0; k < plots.getLength(); k++) {
							if (plots.item(k).getNodeType() != 1) {
								continue;
							}
							NodeList xz = plots.item(k).getChildNodes();
							double x = 0.0D;
							double z = 0.0D;
							label4:
							for (int l = 0; l < xz.getLength(); l++) {
								if (xz.item(l).getNodeType() != 1) {
									continue;
								}
								NodeList lastOne;
								int m;
								if (xz.item(l).getNodeName().equals("x")) {
									lastOne = xz.item(l).getChildNodes();
									m = 0;
									do {
										if (m >= lastOne.getLength())
											continue label4;
										if (lastOne.item(m).getNodeType() == 3) {
											x = Double.parseDouble(lastOne.item(m).getNodeValue());
											continue label4;
										}
										m++;
									} while (true);
								}
								if (!xz.item(l).getNodeName().equals("z")) {
									continue;
								}
								lastOne = xz.item(l).getChildNodes();
								m = 0;
								do {
									if (m >= lastOne.getLength()) {
										continue label4;
									}
									if (lastOne.item(m).getNodeType() == 3) {
										z = Double.parseDouble(lastOne.item(m).getNodeValue());
										continue label4;
									}
									m++;
								} while (true);
							}

							fw.write("v " + (addX + x * affX) + " " + (addY + y * affY) + " " + (addZ + z * affZ) + "\n");
							nbElements++;
						}

						for (int k = 0; k < nbElements - 1; k++) {
							fw.write("f " + ++startFrom + " " + (startFrom + 1) + "\n");
						}
						if (nbElements > 0) {
							startFrom++;
						}
					}

					nbElements = 0;
					for (int j = 0; j < parts.getLength(); j++) {
						if (parts.item(j).getNodeType() != 1) {
							continue;
						}
						nbElements = 0;
						NodeList plots = parts.item(j).getChildNodes();
						for (int k = 0; k < plots.getLength(); k++) {
							if (plots.item(k).getNodeType() != 1) {
								continue;
							}
							NodeList xz = plots.item(k).getChildNodes();
							double x = 0.0D;
							double z = 0.0D;
							label5:
							for (int l = 0; l < xz.getLength(); l++) {
								if (xz.item(l).getNodeType() != 1) {
									continue;
								}
								NodeList lastOne;
								int m;
								if (xz.item(l).getNodeName().equals("x")) {
									lastOne = xz.item(l).getChildNodes();
									m = 0;
									do {
										if (m >= lastOne.getLength()) {
											continue label5;
										}
										if (lastOne.item(m).getNodeType() == 3) {
											x = Double.parseDouble(lastOne.item(m).getNodeValue());
											continue label5;
										}
										m++;
									} while (true);
								}
								if (!xz.item(l).getNodeName().equals("z")) {
									continue;
								}
								lastOne = xz.item(l).getChildNodes();
								m = 0;
								do {
									if (m >= lastOne.getLength()) {
										continue label5;
									}
									if (lastOne.item(m).getNodeType() == 3) {
										z = Double.parseDouble(lastOne.item(m).getNodeValue());
										continue label5;
									}
									m++;
								} while (true);
							}

							fw.write("v " + (addX + x * affX) + " " + (addY - y * affY) + " " + (addZ + z * affZ) + "\n");
							nbElements++;
						}

						for (int k = 0; k < nbElements - 1; k++) {
							fw.write("f " + ++startFrom + " " + (startFrom + 1) + "\n");
						}
						if (nbElements > 0) {
							startFrom++;
						}
					}
				}
			}
			nl = doc.selectNodes("/wf:data/wf:modules/wf:module", resolver);
			if (nl != null) {
				for (int i = 0; i < nl.getLength(); i++) {
					nbElements = 0;
					Node node = nl.item(i);
					String name = ((Element) node).getAttribute("name");
					boolean sym = ((Element) node).getAttribute("symetric").equals("yes");
					fw.write("# Module " + name + "\n");
					NodeList plots = ((Element) node).getElementsByTagNameNS("http://donpedro.lediouris.net/wireframe", "plot");
					if (plots == null) {
						continue;
					}
					for (int j = 0; j < plots.getLength(); j++) {
						Element plot = (Element) plots.item(j);
						double x = Double.parseDouble(plot.getElementsByTagNameNS("http://donpedro.lediouris.net/wireframe", "x").item(0).getFirstChild().getNodeValue());
						double y = Double.parseDouble(plot.getElementsByTagNameNS("http://donpedro.lediouris.net/wireframe", "y").item(0).getFirstChild().getNodeValue());
						double z = Double.parseDouble(plot.getElementsByTagNameNS("http://donpedro.lediouris.net/wireframe", "z").item(0).getFirstChild().getNodeValue());
						fw.write("v " + (addX + x * affX) + " " + (addY + y * affY) + " " + (addZ + z * affZ) + "\n");
						nbElements++;
					}

					for (int j = 0; j < nbElements - 1; j++) {
						fw.write("f " + ++startFrom + " " + (startFrom + 1) + "\n");
					}
					if (nbElements > 0) {
						startFrom++;
					}
					nbElements = 0;
					if (!sym) {
						continue;
					}
					for (int j = 0; j < plots.getLength(); j++) {
						Element plot = (Element) plots.item(j);
						double x = Double.parseDouble(plot.getElementsByTagNameNS("http://donpedro.lediouris.net/wireframe", "x").item(0).getFirstChild().getNodeValue());
						double y = Double.parseDouble(plot.getElementsByTagNameNS("http://donpedro.lediouris.net/wireframe", "y").item(0).getFirstChild().getNodeValue());
						double z = Double.parseDouble(plot.getElementsByTagNameNS("http://donpedro.lediouris.net/wireframe", "z").item(0).getFirstChild().getNodeValue());
						fw.write("v " + (addX + x * affX) + " " + (addY - y * affY) + " " + (addZ + z * affZ) + "\n");
						nbElements++;
					}

					for (int j = 0; j < nbElements - 1; j++) {
						fw.write("f " + ++startFrom + " " + (startFrom + 1) + "\n");
					}
					if (nbElements > 0) {
						startFrom++;
					}
				}
			}
			nl = doc.selectNodes("/wf:data/wf:imports/wf:import", resolver);
			if (nl != null) {
				for (int i = 0; i < nl.getLength(); i++) {
					nbElements = 0;
					Node node = nl.item(i);
					String name = sourceDir.toString() + File.separator + ((XMLElement) node).getAttribute("source");
					fw.write("# Imported " + name + "\n");
					NodeList origin = ((Element) node).getElementsByTagNameNS("http://donpedro.lediouris.net/wireframe", "origin");
					double affineX = 1.0D;
					double affineY = 1.0D;
					double affineZ = 1.0D;
					NodeList affineTramsform = ((Element) node).getElementsByTagNameNS("http://donpedro.lediouris.net/wireframe", "affine-transform");
					if (affineTramsform != null) {
						for (int j = 0; j < affineTramsform.getLength(); j++) {
							Node affineNode = affineTramsform.item(j);
							if (affineNode.getNodeType() != 1) {
								continue;
							}
							NodeList coords = affineNode.getChildNodes();
							for (int k = 0; k < coords.getLength(); k++) {
								Node n = coords.item(k);
								if (n.getNodeType() == 1 && n.getNodeName().equals("x")) {
									affineX = Double.parseDouble(n.getFirstChild().getNodeValue());
								}
								if (n.getNodeType() == 1 && n.getNodeName().equals("y")) {
									affineY = Double.parseDouble(n.getFirstChild().getNodeValue());
								}
								if (n.getNodeType() == 1 && n.getNodeName().equals("z")) {
									affineZ = Double.parseDouble(n.getFirstChild().getNodeValue());
								}
							}
						}
					}
					if (origin == null) {
						continue;
					}
					for (int j = 0; j < origin.getLength(); j++) {
						Node originNode = origin.item(j);
						if (originNode.getNodeType() != 1) {
							continue;
						}
						double x = Double.MIN_VALUE;
						double y = Double.MIN_VALUE;
						double z = Double.MIN_VALUE;
						NodeList coords = originNode.getChildNodes();
						for (int k = 0; k < coords.getLength(); k++) {
							Node n = coords.item(k);
							if (n.getNodeType() == 1 && n.getNodeName().equals("x")) {
								x = affineX * Double.parseDouble(n.getFirstChild().getNodeValue());
							}
							if (n.getNodeType() == 1 && n.getNodeName().equals("y")) {
								y = affineY * Double.parseDouble(n.getFirstChild().getNodeValue());
							}
							if (n.getNodeType() == 1 && n.getNodeName().equals("z")) {
								z = affineZ * Double.parseDouble(n.getFirstChild().getNodeValue());
							}
						}
						int managed = generate(name, fw, x, y, z, affineX, affineY, affineZ, startFrom);
						startFrom = managed;
					}
				}
			}
			fw.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return startFrom;
	}

	public int getOffset() {
		return startFrom;
	}

	public static void main(String... args) {
		String fileName = "xml" + File.separator + "polars.xml";
		if (args.length > 0) {
			fileName = args[0];
		}
		System.out.println(String.format("Transforming %s", fileName));
		String version = DOMParser.getReleaseVersion();
		System.out.println("Using " + version);
		generate(fileName);
	}
}

