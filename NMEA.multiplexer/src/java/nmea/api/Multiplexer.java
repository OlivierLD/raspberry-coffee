package nmea.api;

public interface Multiplexer {
	public void onData(String mess);
	public void setVerbose(boolean b);
}
