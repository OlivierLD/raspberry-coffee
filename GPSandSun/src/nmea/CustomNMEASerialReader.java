package nmea;

import com.pi4j.io.serial.Serial;
import com.pi4j.io.serial.SerialDataEvent;
import com.pi4j.io.serial.SerialDataListener;
import com.pi4j.io.serial.SerialFactory;
import ocss.nmea.api.NMEAEvent;
import ocss.nmea.api.NMEAListener;
import ocss.nmea.api.NMEAReader;

import java.util.List;

public class CustomNMEASerialReader
		extends NMEAReader {
	private int baudRate = 4800;
	private CustomNMEASerialReader instance = this;

	public CustomNMEASerialReader(List<NMEAListener> al, int br) {
		super(al);
		baudRate = br;
	}

	@Override
	public void read() {
		if (System.getProperty("verbose", "false").equals("true"))
			System.out.println("From " + this.getClass().getName() + " Reading Serial Port.");

		super.enableReading();

		// Opening Serial port
		try {
			final Serial serial = SerialFactory.createInstance();

			// create and register the serial data listener
			serial.addListener(new SerialDataListener() {
				@Override
				public void dataReceived(SerialDataEvent event) {
					//  System.out.print(/*"Read:\n" + */ event.getData());
					instance.fireDataRead(new NMEAEvent(this, event.getData()));
				}
			});
			String port = System.getProperty("port.name", Serial.DEFAULT_COM_PORT);
			if (System.getProperty("verbose", "false").equals("true"))
				System.out.println("Opening port [" + port + "]");
			serial.open(port, baudRate);
			// Reading on Serial Port
			if (System.getProperty("verbose", "false").equals("true"))
				System.out.println("Port is " + (serial.isOpen() ? "" : "NOT ") + "open.");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}

