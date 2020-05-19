package nmea.api;

import java.util.Properties;

public interface Multiplexer {
	public void onData(String mess);
	public void setVerbose(boolean b);
	public void setEnableProcess(boolean b);
	public boolean getEnableProcess();
	public void stopAll();
//	public Properties getMuxProperties();
}
