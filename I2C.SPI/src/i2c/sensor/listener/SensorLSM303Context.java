package i2c.sensor.listener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * A singleton
 */
public class SensorLSM303Context implements Serializable {
	private static SensorLSM303Context context = null;
	private transient List<LSM303Listener> sensorReaderListeners;

	private SensorLSM303Context() {
		sensorReaderListeners = new ArrayList<>();
	}

	public static synchronized SensorLSM303Context getInstance() {
		if (context == null) {
			context = new SensorLSM303Context();
		}
		return context;
	}

	public List<LSM303Listener> getReaderListeners() {
		return sensorReaderListeners;
	}

	public synchronized void addReaderListener(LSM303Listener l) {
		if (!sensorReaderListeners.contains(l)) {
			sensorReaderListeners.add(l);
		}
	}

	public synchronized void removeReaderListener(L3GD20Listener l) {
		sensorReaderListeners.remove(l);
	}

	public void fireDataDetected(float accX, float accY, float accZ, float magX, float magY, float magZ, float heading, float pitch, float roll) {
		for (LSM303Listener l : sensorReaderListeners) {
			l.dataDetected(accX, accY, accZ, magX, magY, magZ, heading, pitch, roll);
		}
	}

	public void fireClose() {
		for (LSM303Listener l : sensorReaderListeners) {
			l.close();
		}
	}
}
