package nmea;

import com.pi4j.io.serial.*;
import nmea.api.NMEAEvent;
import nmea.api.NMEAListener;
import nmea.api.NMEAReader;

import java.util.List;

public class CustomNMEASerialReader extends NMEAReader {
    private int baudRate = 4800;
    private CustomNMEASerialReader instance = this;

    public CustomNMEASerialReader(List<NMEAListener> al, int br) {
        super(al);
        baudRate = br;
    }

    // @Override
    public void read() {
        if (System.getProperty("verbose", "false").equals("true")) {
			System.out.println("From " + this.getClass().getName() + " Reading Serial Port.");
		}
        super.enableReading();

        // Opening Serial port
        try {
            final Serial serial = SerialFactory.createInstance();

            // create and register the serial data listener
            serial.addListener(new SerialDataEventListener() {
                @Override
                public void dataReceived(SerialDataEvent event) {
                    //  System.out.print(/*"Read:\n" + */ event.getData());
                    instance.fireDataRead(new NMEAEvent(this, event.toString()));
                }
            });
            String port = System.getProperty("port.name", Serial.DEFAULT_COM_PORT);
            if (System.getProperty("verbose", "false").equals("true")) {
				System.out.println("Opening port [" + port + "]");
			}
            serial.open(port, baudRate);
            // Reading on Serial Port
            if (System.getProperty("verbose", "false").equals("true")) {
				System.out.println("Port is " + (serial.isOpen() ? "" : "NOT ") + "open.");
			}
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void startReader() throws Exception {
        // TODO
    }

    @Override
    public void closeReader() throws Exception {
        // TODO
    }
}

