package nmea.forwarders;

import context.ApplicationContext;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import nmea.parser.StringGenerator.XDRElement;
import nmea.parser.StringGenerator.XDRTypes;
import nmea.parser.StringParsers;

/**
 * This is a <i>Custom</i> forwarder.
 * It intends to gather data from 2 different machines (2 RPi with sensors).
 * The sentences have the same Sentence ID, and different Device IDs.
 *
 * Requires:
 * <table border='1'>
 *   <caption>Description</caption>
 *   <tr>
 *     <td>Forwarder Class</td>
 *     <td>nmea.forwarders.InOutWriter</td>
 *   </tr>
 * </table>
 *
 */
public class InOutDataWriter implements Forwarder {

	public InOutDataWriter() throws Exception {
		// Make sure the cache has been initialized.
		if (ApplicationContext.getInstance().getDataCache() == null) {
			throw new RuntimeException("Init the Cache first. See the properties file used at startup."); // Oops
		}
	}

	@Override
	public void write(byte[] message) {
		// Receive message here, and create new entries in the cache, based on device prefix.
		String str = new String(message);
//	System.out.println(">>>> Mess:" + str);
		if (StringParsers.validCheckSum(str)) {
			String deviceId = StringParsers.getDeviceID(str);
			String sentenceId = StringParsers.getSentenceID(str);
			switch (deviceId) {
				case "01": // Outside
					if (sentenceId.equals("MTA")) { // Air temp
						double outsideTemp = StringParsers.parseMTA(str);
//					System.out.println(">>> Outside temp:" + outsideTemp);
						ApplicationContext.getInstance().getDataCache().put("x.outside.temp", outsideTemp);
					}
					if (sentenceId.equals("XDR")) {
						List<XDRElement> xdr = StringParsers.parseXDR(str);
						Optional<XDRElement> xdrElementOptional = xdr.stream()
										.filter(x -> XDRTypes.HUMIDITY.equals(x.getTypeNunit()))
										.findFirst();
						if (xdrElementOptional.isPresent()) {
							double outsideHum = xdrElementOptional.get().getValue();
							ApplicationContext.getInstance().getDataCache().put("x.outside.hum", outsideHum);
						}
					}
					break;
				case "02": // Inside
					if (sentenceId.equals("MTA")) { // Air temp
						double insideTemp = StringParsers.parseMTA(str);
//					System.out.println(">>> Inside temp:" + insideTemp);
						ApplicationContext.getInstance().getDataCache().put("x.inside.temp", insideTemp);
					}
					if (sentenceId.equals("XDR")) {
						List<XDRElement> xdr = StringParsers.parseXDR(str);
						Optional<XDRElement> xdrElementOptional = xdr.stream()
										.filter(x -> XDRTypes.HUMIDITY.equals(x.getTypeNunit()))
										.findFirst();
						if (xdrElementOptional.isPresent()) {
							double insideHum = xdrElementOptional.get().getValue();
							ApplicationContext.getInstance().getDataCache().put("x.inside.hum", insideHum);
						}
					}
					break;
				default:
					break;
			}
		}
	}

	@Override
	public void close() {
		System.out.println("- Stop writing special data to the cache. (" + this.getClass().getName() + ")");
	}

	private static class InOutBean {
		private String cls;
		private String type = "in-out";

		public InOutBean(InOutDataWriter instance) {
			cls = instance.getClass().getName();
		}
	}

	@Override
	public Object getBean() {
		return new InOutBean(this);
	}

	@Override
	public void setProperties(Properties props) {
	}
}
