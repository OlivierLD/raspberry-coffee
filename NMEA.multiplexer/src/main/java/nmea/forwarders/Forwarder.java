package nmea.forwarders;

import java.util.Properties;

public interface Forwarder {
	void write(byte[] mess); // Receives data
	default void init() {}   // Called after the setProperties, in case some re driving the Forwarder's initialization.
	void close();
	void setProperties(Properties props);

	Object getBean();
}
