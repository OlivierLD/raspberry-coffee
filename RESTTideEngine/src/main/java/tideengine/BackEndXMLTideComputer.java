package tideengine;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BackEndXMLTideComputer {
	public final static String ARCHIVE_STREAM = "xml/xml.zip";
	public final static String CONSTITUENTS_ENTRY = "constituents.xml";
	public final static String STATIONS_ENTRY = "stations.xml";

	private static boolean verbose = false;

	static Constituents buildConstituents() throws Exception {
		SpeedConstituentFinder scf = new SpeedConstituentFinder();
		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser saxParser = factory.newSAXParser();
			InputSource is = BackEndTideComputer.getZipInputSource(ARCHIVE_STREAM, CONSTITUENTS_ENTRY);
			saxParser.parse(is, scf);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return scf.getConstituents();
	}

	public static Stations getTideStations() throws Exception {
		return new Stations(getStationData());
	}

	public static Map<String, TideStation> getStationData() throws Exception {
		Map<String, TideStation> stationData = new HashMap<>();
		StationFinder sf = new StationFinder(stationData);
		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser saxParser = factory.newSAXParser();
			InputSource is = BackEndTideComputer.getZipInputSource(ARCHIVE_STREAM, STATIONS_ENTRY);
			saxParser.parse(is, sf);
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return stationData;
	}

	public static List<TideStation> getStationData(Stations stations) throws Exception {
		long before = System.currentTimeMillis();
		List<TideStation> stationData = new ArrayList<>();
		Set<String> keys = stations.getStations().keySet();
		for (String k : keys) {
			try {
				stationData.add(stations.getStations().get(k));
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		long after = System.currentTimeMillis();
		if (verbose) {
			System.out.println("Finding all the stations took " + Long.toString(after - before) + " ms");
		}

		return stationData;
	}

	public static TideStation reloadOneStation(String stationName) throws Exception {
		StationFinder sf = new StationFinder();
		sf.setStationName(stationName);
		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser saxParser = factory.newSAXParser();
			InputSource is = BackEndTideComputer.getZipInputSource(ARCHIVE_STREAM, STATIONS_ENTRY);
			saxParser.parse(is, sf);
		} catch (DoneWithSiteException dwse) {
			System.err.println(dwse.getLocalizedMessage());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return sf.getTideStation();
	}

	public static void setVerbose(boolean verbose) {
		BackEndXMLTideComputer.verbose = verbose;
	}

	public static class StationFinder extends DefaultHandler {
		private String stationName = "";
		private TideStation ts = null;
		private Map<String, TideStation> stationMap = null;

		public void setStationName(String sn) {
			this.stationName = sn;
		}

		public StationFinder() { }

		public StationFinder(Map<String, TideStation> map) {
			this.stationMap = map;
		}

		public TideStation getTideStation() {
			return ts;
		}

		private boolean foundStation = false;
		private boolean foundNameCollection = false;
		private boolean foundStationData = false;

		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes)
				throws SAXException {
//    super.startElement(uri, localName, qName, attributes);
			if (!foundStation && "station".equals(qName)) {
				String name = attributes.getValue("name");
				if (name.contains(this.stationName)) {
					foundStation = true;
					ts = new TideStation();
					try {
						ts.setFullName(URLEncoder.encode(URLDecoder.decode(name, "ISO-8859-1"), "UTF-8").replace("+", "%20"));
					} catch (UnsupportedEncodingException uee) {
						uee.printStackTrace();
					}
				}
			} else if (foundStation) {
				if ("name-collection".equals(qName)) {
					foundNameCollection = true;
				} else if ("name-part".equals(qName) && foundNameCollection) {
					try {
						ts.getNameParts().add(URLEncoder.encode(URLDecoder.decode(attributes.getValue("name"), "ISO-8859-1"), "UTF-8").replace("+", "%20"));
					} catch (UnsupportedEncodingException uee) {
						uee.printStackTrace();
					}
				} else if ("position".equals(qName)) {
					ts.setLatitude(Double.parseDouble(attributes.getValue("latitude")));
					ts.setLongitude(Double.parseDouble(attributes.getValue("longitude")));
				} else if ("time-zone".equals(qName)) {
					ts.setTimeZone(attributes.getValue("name"));
					ts.setTimeOffset(attributes.getValue("offset"));
				} else if ("base-height".equals(qName)) {
					ts.setBaseHeight(Double.parseDouble(attributes.getValue("value")));
					ts.setUnit(attributes.getValue("unit"));
				} else if ("station-data".equals(qName)) {
					foundStationData = true;
				} else if (foundStationData && "harmonic-coeff".equals(qName)) {
					String name = attributes.getValue("name");
					double amplitude = Double.parseDouble(attributes.getValue("amplitude"));
					double epoch = Double.parseDouble(attributes.getValue("epoch")) * TideUtilities.COEFF_FOR_EPOCH;
					Harmonic h = new Harmonic(name, amplitude, epoch);
					ts.getHarmonics().add(h);
				}
			}
		}

		@Override
		public void endElement(String uri, String localName, String qName)
				throws SAXException {
			super.endElement(uri, localName, qName);
			if (foundStation && "station".equals(qName)) {
				foundStation = false;
				if (stationMap == null) {
					throw new DoneWithSiteException("Done with it.");
				} else {
					stationMap.put(ts.getFullName(), ts);
				}
			} else if (foundNameCollection && "name-collection".equals(qName)) {
				foundNameCollection = false;
			} else if (foundStationData && "station-data".equals(qName)) {
				foundStationData = false;
			}
		}
	}

	public static class SpeedConstituentFinder extends DefaultHandler {
		private Constituents.ConstSpeed constituent = null;
		private Constituents constituents = null;

		public SpeedConstituentFinder() {
			constituents = new Constituents();
		}

		public Constituents getConstituents() {
			return constituents;
		}

		private boolean foundConstituent = false;
		private boolean foundCoeffName = false;
		private boolean foundCoeffValue = false;

		private boolean foundEquilibrium = false;
		private boolean foundFactor = false;

		private String coeffName = null;
		private int coeffIdx = -1;
		private double coeffValue = Double.NaN;

		private double value = 0D;
		private int year = -1;

		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes)
				throws SAXException {
			//  super.startElement(uri, localName, qName, attributes);
			if (!foundConstituent && "const-speed".equals(qName)) {
				foundConstituent = true;
				coeffIdx = Integer.parseInt(attributes.getValue("idx"));
			} else if (foundConstituent && "coeff-name".equals(qName)) {
				foundCoeffName = true;
			} else if (foundConstituent && "coeff-value".equals(qName)) {
				foundCoeffValue = true;
			} else if (foundConstituent) {
				if ("equilibrium".equals(qName)) {
					foundEquilibrium = true;
					year = Integer.parseInt(attributes.getValue("year"));
				} else if ("factor".equals(qName)) {
					foundFactor = true;
					year = Integer.parseInt(attributes.getValue("year"));
				}
			}
		}

		@Override
		public void endElement(String uri, String localName, String qName)
				throws SAXException {
			super.endElement(uri, localName, qName);

			if (coeffName != null && coeffIdx != -1 && !Double.isNaN(coeffValue)) {
				constituent = new Constituents.ConstSpeed(coeffIdx, coeffName, coeffValue);
				coeffName = null;
				coeffIdx = -1;
				coeffValue = Double.NaN;
			}

			if (foundConstituent && "const-speed".equals(qName)) {
				foundConstituent = false;
				coeffName = null;
				coeffIdx = -1;
				coeffValue = Double.NaN;
				constituents.getConstSpeedMap().put(constituent.getCoeffName(), constituent);
			} else if ("coeff-name".equals(qName)) {
				foundCoeffName = false;
			} else if ("coeff-value".equals(qName)) {
				foundCoeffValue = false;
			}
			if ("equilibrium".equals(qName)) {
				constituent.getEquilibrium().put(new Integer(year), value);
				foundEquilibrium = false;
			} else if ("factor".equals(qName)) {
				constituent.getFactors().put(new Integer(year), value);
				foundFactor = false;
			}
		}

		public void characters(char ch[], int start, int length)
				throws SAXException {
			String str = new String(ch).substring(start, start + length).trim();
			if (foundCoeffName) {
				coeffName = str;
			} else if (foundCoeffValue) {
				coeffValue = Double.parseDouble(str);
			} else if (foundEquilibrium) {
				value = Double.parseDouble(str);
			} else if (foundFactor) {
				value = Double.parseDouble(str);
			}
		}
	}

	public static class DoneWithSiteException extends SAXException {
		@SuppressWarnings("compatibility:4149777882983149063")
		public final static long serialVersionUID = 1L;

		public DoneWithSiteException(String s) {
			super(s);
		}
	}
}
