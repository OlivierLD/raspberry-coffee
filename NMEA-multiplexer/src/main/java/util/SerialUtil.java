package util;

import gnu.io.CommPortIdentifier;
import nmea.consumers.reader.SerialReader;

import java.util.Enumeration;

public class SerialUtil {

	public static void main(String... args) {
		Enumeration enumeration = CommPortIdentifier.getPortIdentifiers();
		int nbp = 0;
		System.out.println("\n----- Serial Port List -----");
		while (enumeration.hasMoreElements()) {
			CommPortIdentifier cpi = (CommPortIdentifier) enumeration.nextElement();
			System.out.println(String.format("Port: %s, %s, %s.",
					cpi.getName(),
					SerialReader.readablePortType(cpi.getPortType()),
					(cpi.isCurrentlyOwned() ? String.format("(owned by %s)", cpi.getCurrentOwner()) : "free")));
			nbp++;
		}
		System.out.println("Found " + nbp + " port(s)");
		System.out.println("----------------------------");
	}
}
