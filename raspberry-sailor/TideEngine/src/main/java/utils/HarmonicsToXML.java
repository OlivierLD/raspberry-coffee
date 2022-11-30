package utils;

import oracle.xml.parser.v2.XMLDocument;
import oracle.xml.parser.v2.XMLElement;
import org.w3c.dom.Text;

import javax.annotation.Nonnull;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.net.URL;

/**
 * Turns files like harmonics_06_14_2004.txt into their XML counterpart, easier to use.
 */
public class HarmonicsToXML {

	private final static boolean verbose = "true".equals(System.getProperty("verbose", "false"));

	// Those two are used only to generate and write the data files.
	public final static String CONSTITUENT_FILE = /* "xml.data" + File.separator + */ "constituents.xml";
	public final static String STATION_FILE = /* "xml.data" + File.separator + */ "stations.xml";

	public static void main(String... args) {
		String[] data = new String[] {
				"./harmonics/harmonics_06_14_2004.txt",
				"./harmonics/harmonics_06_14_2004_fr.txt"
		};
		try {
			generateXML(data);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Generates XML files after txt files (like harmonics_06_14_2004.txt)
	 *
	 * @param harmonicName
	 * @throws Exception
	 */
	public static void generateXML(@Nonnull String[] harmonicName) throws Exception {
		final String LONGITUDE = "!longitude:";
		final String LATITUDE = "!latitude:";

		PrintWriter writer = null;
		XMLDocument stationDoc = null;
		long nbStations = 0;

		for (int h = 0; h < harmonicName.length; h++) {
			URL harmonic = new File(harmonicName[h]).toURI().toURL();
			if (verbose) {
				System.out.println("Processing URL:" + harmonic.toString());
			}

			BufferedReader br = new BufferedReader(new FileReader(harmonic.getFile()));

			//  String idxLine = Integer.toString(0) + "\t" + harmonicName + "";
			//  bw.write(idxLine + "\n");

			String line = "";

			XMLDocument doc = new XMLDocument();
			XMLElement root = (XMLElement) doc.createElement("constituents");
			doc.appendChild(root);
			XMLElement speedConst = (XMLElement) doc.createElement("speed-constituents");
			root.appendChild(speedConst);
			// Step one: constituents
			while (line != null) {
				line = br.readLine();
				if (line != null && line.indexOf("# Number of constituents") > -1) { // Aha!
					line = br.readLine();
					/* int nbElements = */
					Integer.parseInt(line);  // Not used for now... we just keep looping
					boolean skipComments = true;
					while (skipComments) {
						line = br.readLine();
						if (line == null || !line.startsWith("#")) {
							skipComments = false;
						}
					}
					boolean loopOnCoeff = true;
					int coeffIndex = 1;
					while (loopOnCoeff) {
						String[] constSpeed = line.split(" ");
						String coeffName = constSpeed[0];
						String coeffValue = "";
						for (int i = 1; i < constSpeed.length; i++) {
							if (!constSpeed[i].trim().isEmpty()) {
								coeffValue = constSpeed[i];
								break;
							}
						}
						XMLElement coeff = (XMLElement) doc.createElement("const-speed"); // Speed in degree per solar hour
						speedConst.appendChild(coeff);
						coeff.setAttribute("idx", Integer.toString(coeffIndex++));
						XMLElement name = (XMLElement) doc.createElement("coeff-name");
						XMLElement value = (XMLElement) doc.createElement("coeff-value");
						coeff.appendChild(name);
						coeff.appendChild(value);
						Text nameValue = doc.createTextNode("#text");
						nameValue.setNodeValue(coeffName);
						name.appendChild(nameValue);
						Text valueValue = doc.createTextNode("#text");
						valueValue.setNodeValue(coeffValue);
						value.appendChild(valueValue);

						line = br.readLine();
						if (line == null || line.startsWith("#")) {
							loopOnCoeff = false;
						}
					}
					// Look for the origin of the years
					boolean keepLooping = true;
					while (keepLooping) {
						if (!line.startsWith("#")) {
							keepLooping = false;
						} else {
							line = br.readLine();
						}
					}
					int yearOrigin = Integer.parseInt(line);
					// Look for the number of years in the file for equilibrium
					line = br.readLine();
					keepLooping = true;
					while (keepLooping) {
						if (!line.startsWith("#")) {
							keepLooping = false;
						} else {
							line = br.readLine();
						}
					}
					int nbYears = Integer.parseInt(line);
					// Now, loop on the coeffs
					line = br.readLine();
					keepLooping = true;
					while (keepLooping) {
						if (line.startsWith("*END*")) {
							keepLooping = false;
						} else {
							String coeffName = line.trim();
							String xPath = "/constituents/speed-constituents/const-speed[./coeff-name = '" + coeffName + "']";
							XMLElement coeffElement = (XMLElement) doc.selectNodes(xPath).item(0);
							XMLElement yearsHolder = (XMLElement) doc.createElement("equilibrium-arguments");
							coeffElement.appendChild(yearsHolder);
							int y = yearOrigin;
							loopOnCoeff = true;
							while (loopOnCoeff) {
								line = br.readLine();
								String[] coeff = line.split(" ");
								for (int i = 0; i < coeff.length; i++) {
									try {
										String s = coeff[i].trim();
										if (s.length() > 0) {
											/* double d = */
											Double.parseDouble(s);
											XMLElement equ = (XMLElement) doc.createElement("equilibrium");
											equ.setAttribute("year", Integer.toString(y++));
											yearsHolder.appendChild(equ);
											Text value = doc.createTextNode("#text");
											value.setNodeValue(s);
											equ.appendChild(value);
										}
									} catch (NumberFormatException nfe) {
										loopOnCoeff = false;
										break;
									}
								}
							}
						}
					}
					// Look for the number of years in the file for node factors
					line = br.readLine();
					keepLooping = true;
					while (keepLooping) {
						if (!line.startsWith("#")) {
							keepLooping = false;
						} else {
							line = br.readLine();
						}
					}
					nbYears = Integer.parseInt(line);
					// Now, loop on the coeffs
					line = br.readLine();
					keepLooping = true;
					while (keepLooping) {
						if (line.startsWith("*END*")) {
							keepLooping = false;
						} else {
							String coeffName = line.trim();
							String xPath = "/constituents/speed-constituents/const-speed[./coeff-name = '" + coeffName + "']";
							XMLElement coeffElement = (XMLElement) doc.selectNodes(xPath).item(0);
							XMLElement yearsHolder = (XMLElement) doc.createElement("node-factors");
							coeffElement.appendChild(yearsHolder);
							int y = yearOrigin;
							loopOnCoeff = true;
							while (loopOnCoeff) {
								line = br.readLine();
								String[] coeff = line.split(" ");
								for (int i = 0; i < coeff.length; i++) {
									try {
										String s = coeff[i].trim();
										if (s.length() > 0) {
											/* double d = */
											Double.parseDouble(s);
											XMLElement fact = (XMLElement) doc.createElement("factor");
											fact.setAttribute("year", Integer.toString(y++));
											yearsHolder.appendChild(fact);
											Text value = doc.createTextNode("#text");
											value.setNodeValue(s);
											fact.appendChild(value);
										}
									} catch (NumberFormatException nfe) {
										loopOnCoeff = false;
										break;
									}
								}
							}
						}
					}
				}
			}
			br.close();
			// Spit out result here
			writer = new PrintWriter(new FileWriter(CONSTITUENT_FILE));
			doc.print(writer);
			writer.close();

			// Step two: Stations
			if (stationDoc == null) {
				stationDoc = new XMLDocument();
				root = (XMLElement) stationDoc.createElement("stations");
				stationDoc.appendChild(root);
			} else {
				root = (XMLElement) stationDoc.getDocumentElement();
			}
			br = new BufferedReader(new FileReader(harmonic.getFile())); // Re-open
			line = "";
			while (line != null) {
				line = br.readLine();
				if (line != null) {
					//      System.out.println("-> [" + line + "]");
					if (line.indexOf(LONGITUDE) > -1) {
						nbStations++;
						double lng = Double.parseDouble(line.substring(line.indexOf(LONGITUDE) + LONGITUDE.length()));
						//        System.out.println("Longitude:" + lng);
						// Now, latitude
						line = br.readLine();
						if (line.indexOf(LATITUDE) > -1) { // As expected
							XMLElement station = (XMLElement) stationDoc.createElement("station");
							root.appendChild(station);

							XMLElement position = (XMLElement) stationDoc.createElement("position");

							double lat = Double.parseDouble(line.substring(line.indexOf(LATITUDE) + LATITUDE.length()));
							//          System.out.println("Latitude:" + lat);
							position.setAttribute("latitude", Double.toString(lat));
							position.setAttribute("longitude", Double.toString(lng));
							// Station name
							line = br.readLine();
							station.setAttribute("name", line.trim());
							String[] stationElement = line.split(",");
							XMLElement names = (XMLElement) stationDoc.createElement("name-collection");

							for (int i = stationElement.length - 1; i >= 0; i--) {
								//            System.out.println("-> " + stationElement[i].trim());
								XMLElement namePart = (XMLElement) stationDoc.createElement("name-part");
								namePart.setAttribute("rnk", Integer.toString(stationElement.length - i));
								namePart.setAttribute("name", stationElement[i].trim());
								names.appendChild(namePart);
							}
							// Station Zone and time offset
							line = br.readLine();
							String[] zoneElement = line.split(" ");
							//          System.out.println("Time offset: " + zoneElement[0]);
							//          System.out.println("Zone :" + zoneElement[1].substring(1)); // To skip the ":" at the beginning
							XMLElement tz = (XMLElement) stationDoc.createElement("time-zone");
							tz.setAttribute("offset", zoneElement[0]);
							tz.setAttribute("name", zoneElement[1].substring(1));
							// Base Height
							line = br.readLine();
							//          System.out.println("Base Height: " + line);
							XMLElement baseHeight = (XMLElement) stationDoc.createElement("base-height");
							String[] bh = line.split(" ");
							String val = bh[0];
							String unit = bh[1];
							baseHeight.setAttribute("value", val);
							baseHeight.setAttribute("unit", unit);

							station.appendChild(names);
							station.appendChild(position);
							station.appendChild(tz);
							station.appendChild(baseHeight);

							XMLElement data = (XMLElement) stationDoc.createElement("station-data");
							station.appendChild(data);

							// Now, coefficients
							boolean keepLooping = true;
							int rnk = 1;
							while (keepLooping) {
								line = br.readLine();
								if (line == null || line.startsWith("#")) {
									keepLooping = false;
								} else {
									XMLElement harm = (XMLElement) stationDoc.createElement("harmonic-coeff");
									data.appendChild(harm);
									harm.setAttribute("rnk", Integer.toString(rnk++));
									String[] coeffElement = line.split(" ");
									harm.setAttribute("name", coeffElement[0]);
									//              System.out.print(" -> Coeff: ");
									int coeffFound = 0;
									for (int i = 1; i < coeffElement.length; i++) {
										if (!coeffElement[i].trim().isEmpty()) {
											coeffFound++;
											if (coeffFound == 1) {
												harm.setAttribute("amplitude", coeffElement[i].trim());
											} else if (coeffFound == 2) {
												harm.setAttribute("epoch", coeffElement[i].trim());
											}
//                    System.out.print(coeffElement[i] + " ");
										}
									}
								}
							}
						} else {
							System.out.println("Warning! No " + LATITUDE + " found after " + LONGITUDE);
						}
					} // else keep looping
				}
			}
			br.close();
		}
		// Spit out result here
		writer = new PrintWriter(new FileWriter(STATION_FILE));
		stationDoc.print(writer);
		writer.close();
		if (verbose) {
			System.out.println("Done, " + nbStations + " Station(s)");
		}
	}
}
