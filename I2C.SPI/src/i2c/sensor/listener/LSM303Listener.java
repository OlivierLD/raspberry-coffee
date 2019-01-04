package i2c.sensor.listener;

import java.util.EventListener;

public abstract class LSM303Listener implements EventListener {
	public void dataDetected(double accX, double accY, double accZ, double magX, double magY, double magZ, double heading, double pitch, double roll) {
	}

	public void close() {
	}
}
