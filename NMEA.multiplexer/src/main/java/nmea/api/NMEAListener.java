package nmea.api;

import java.util.EventListener;

import nmea.api.NMEAEvent;

public abstract class NMEAListener implements EventListener {
	public void dataDetected(NMEAEvent e) {	}

	public void dataRead(NMEAEvent e) {	}

	public void stopReading(NMEAEvent e) {	}

	public void fireError(Throwable t) {	}
}