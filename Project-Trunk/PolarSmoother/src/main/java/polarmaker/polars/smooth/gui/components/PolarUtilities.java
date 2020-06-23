package polarmaker.polars.smooth.gui.components;

import oracle.xml.parser.v2.DOMParser;
import oracle.xml.parser.v2.NSResolver;
import oracle.xml.parser.v2.XMLDocument;
import oracle.xml.parser.v2.XMLElement;
import org.w3c.dom.NodeList;
import polarmaker.Constants;
import polarmaker.polars.main.PolarSmoother;
import polarmaker.polars.smooth.gui.components.polars.CoeffForPolars;

import javax.swing.JFileChooser;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PolarUtilities {
	public static double calculateSTW(double[][] coeffDeg, double tws, double twa) {
		int req_degree_polar = coeffDeg.length - 1;
		double[] actualCoeff = new double[req_degree_polar + 1];

		for (int j = 0; j < (req_degree_polar + 1); j++) {
			actualCoeff[j] = PolarSmoother.f(tws, coeffDeg[j]);
		}
		double bsp = PolarSmoother.f(twa, actualCoeff);

		return bsp;
	}

	public static double getBSP(List<CoeffForPolars> coefflist, double tws, double twa) {
		double bsp = 0d;
		for (CoeffForPolars cf3d : coefflist) {
			if (twa >= cf3d.getFromTwa() && twa <= cf3d.getToTwa()) {
				double stw = PolarUtilities.calculateSTW(cf3d.getCoeffDeg(), tws, twa);
				bsp = Math.max(bsp, stw);
			}
		}
		return bsp;
	}

	public static List<CoeffForPolars> buildCoeffList(XMLDocument doc) {
		List<CoeffForPolars> list = new ArrayList<CoeffForPolars>();
		try {
			NSResolver nsr = new NSResolver() {
				public String resolveNamespacePrefix(String string) {
					return Constants.POLAR_FUNCTION_NS_URI;
				}
			};
			NodeList section = doc.selectNodes("//pol:polar-coeff-function", nsr);
			for (int i = 0; i < section.getLength(); i++) {
				XMLElement s = (XMLElement) section.item(i);
				int polDeg = Integer.parseInt(s.getAttribute("polar-degree"));
				int cDeg = Integer.parseInt(s.getAttribute("polar-coeff-degree"));
				int fromTwa = Integer.parseInt(s.getAttribute("from-twa"));
				int toTwa = Integer.parseInt(s.getAttribute("to-twa"));
				double[][] coeffDeg = new double[polDeg + 1][cDeg + 1];
				NodeList pcNode = s.selectNodes("./pol:polar-coeff", nsr);
				if (pcNode.getLength() != (polDeg + 1)) {
					System.out.println("Invalid degree for polars: expected [" + polDeg + " + 1], found [" + pcNode.getLength() + "]");
				} else {
					for (int j = 0; j < pcNode.getLength(); j++) {
						XMLElement pCoeff = (XMLElement) pcNode.item(j);
						int degAtt = Integer.parseInt(pCoeff.getAttribute("degree"));
						if (degAtt != (polDeg - j)) {
							System.out.println("Invalid polar coeff degree");
						} else {
							NodeList coeffNode = pCoeff.selectNodes("pol:coeff", nsr);
							if (coeffNode.getLength() != (cDeg + 1)) {
								System.out.println("Invalid degree for coeff: expected [" + cDeg + " + 1], found [" + coeffNode.getLength() + "]");
							} else {
								for (int k = 0; k < coeffNode.getLength(); k++) {
									XMLElement coeff = (XMLElement) coeffNode.item(k);
									int cDegAtt = Integer.parseInt(coeff.getAttribute("degree"));
									if (cDegAtt != (cDeg - k)) {
										System.out.println("Invalid coeff degree");
									} else {
										double d = Double.parseDouble(coeff.getText());  // coeff.getTextContent());
										coeffDeg[j][k] = d;
									}
								}
							}
						}
					}
				}

				list.add(new CoeffForPolars(coeffDeg, polDeg, fromTwa, toTwa));
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return list;
	}

	public static String chooseFile(int mode,
	                                String flt,
	                                String desc,
	                                String title,
	                                String buttonLabel) {
		String fileName = "";
		JFileChooser chooser = new JFileChooser();
		if (title != null) {
			chooser.setDialogTitle(title);
		}
		if (buttonLabel != null) {
			chooser.setApproveButtonText(buttonLabel);
		}
		ToolFileFilter filter = new ToolFileFilter(flt, desc);
		chooser.addChoosableFileFilter(filter);
		chooser.setFileFilter(filter);

		chooser.setFileSelectionMode(mode);
		// Set current directory
		File f = new File(".");
		String currPath = f.getAbsolutePath();
		f = new File(currPath.substring(0, currPath.lastIndexOf(File.separator)));
		chooser.setCurrentDirectory(f);

		int retval = chooser.showOpenDialog(null);
		switch (retval) {
			case JFileChooser.APPROVE_OPTION:
				fileName = chooser.getSelectedFile().toString();
				break;
			case JFileChooser.CANCEL_OPTION:
				break;
			case JFileChooser.ERROR_OPTION:
				break;
		}
		return fileName;
	}

	public static String makeSureExtensionIsOK(String filename, String extension) {
		if (!filename.toLowerCase().endsWith(extension)) {
			filename += extension;
		}
		return filename;
	}

	public static String makeSureExtensionIsOK(String filename, String[] extension, String defaultExtension) {
		boolean extensionExists = false;
		for (int i = 0; i < extension.length; i++) {
			if (filename.toLowerCase().endsWith(extension[i].toLowerCase())) {
				extensionExists = true;
				break;
			}
		}
		if (!extensionExists) {
			filename += defaultExtension;
		}
		return filename;
	}

	public static void main(String... args) throws Exception {
		String fName = "test.polar-coeff";
		if (args.length > 0) {
			fName = args[0];
		}
		// Test the coeff list generation
		DOMParser parser = new DOMParser();
		parser.parse(new File(fName).toURI().toURL());
		XMLDocument doc = parser.getDocument();
		List<CoeffForPolars> list = buildCoeffList(doc);
		// Test:
		double bsp = getBSP(list, 20d, 87d);
		System.out.println("TWA 87, TWS 20, STW=" + bsp);
	}
}
