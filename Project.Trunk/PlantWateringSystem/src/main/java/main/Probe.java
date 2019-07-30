package main;

import com.pi4j.io.gpio.PinState;
import java.util.List;

public interface Probe {
	double getHumidity();
	double getTemperature();
	void setHumidity(double humidity);
	void setTemperature(double temperature);
	void resumeWatering();
	PWSParameters getPWSParameters();
	PinState getRelayState();
	void setRelayState(PinState state);
	void setPWSParameters(PWSParameters data);
	String getStatus();
	Long getLastWateringTime();
	List<Double> getRecentData();
}
