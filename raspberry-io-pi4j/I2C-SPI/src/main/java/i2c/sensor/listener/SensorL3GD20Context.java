package i2c.sensor.listener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * A singleton
 */
public class SensorL3GD20Context implements Serializable {
	private static SensorL3GD20Context context = null;
	private transient List<L3GD20Listener> sensorReaderListeners;

	private SensorL3GD20Context() {
		sensorReaderListeners = new ArrayList<>();
	}

	public static synchronized SensorL3GD20Context getInstance() {
		if (context == null) {
			context = new SensorL3GD20Context();
		}
		return context;
	}

	public List<L3GD20Listener> getReaderListeners() {
		return sensorReaderListeners;
	}

	public synchronized void addReaderListener(L3GD20Listener l) {
		if (!sensorReaderListeners.contains(l)) {
			sensorReaderListeners.add(l);
		}
	}

	public synchronized void removeReaderListener(L3GD20Listener l) {
		sensorReaderListeners.remove(l);
	}

	public void fireMotionDetected(double x, double y, double z) {
		for (L3GD20Listener l : sensorReaderListeners) {
			l.motionDetected(x, y, z);
		}
	}

	public void fireClose() {
		for (L3GD20Listener l : sensorReaderListeners) {
			l.close();
		}
	}
}
