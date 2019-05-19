package nmea.consumers.client;

public interface ClientBean {
	String getType();
	boolean getVerbose();
	String[] getDeviceFilters();
	String[] getSentenceFilters();
}
