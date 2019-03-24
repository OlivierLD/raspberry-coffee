package main;

import com.pi4j.io.gpio.PinState;

public interface Probe {
	public double getHumidity();
	public double getTemperature();
	public void setHumidity(double humidity);
	public void setTemperature(double temperature);
	public PWSParameters getPWSParameters();
	public PinState getRelayState();
	public void setRelayState(PinState state);
	public void setPWSParameters(PWSParameters data);
	public String getStatus();
	public Long getLastWateringTime();

}
