package nmea.forwarders;

import java.util.Properties;

public interface Forwarder {
	void write(byte[] mess); // Receives data
	void close();
	void setProperties(Properties props);

	Object getBean();
}
