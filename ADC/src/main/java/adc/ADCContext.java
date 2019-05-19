package adc;

import java.util.ArrayList;
import java.util.List;

public class ADCContext {
	private static ADCContext instance = null;
	private List<ADCListener> listeners = null;

	private ADCContext() {
		listeners = new ArrayList<>();
	}

	public synchronized static ADCContext getInstance() {
		if (instance == null) {
			instance = new ADCContext();
		}
		return instance;
	}

	public void addListener(ADCListener listener) {
		if (!listeners.contains(listener)) {
			listeners.add(listener);
		}
	}

	public void removeListener(ADCListener listener) {
		if (listeners.contains(listener)) {
			listeners.remove(listener);
		}
	}

	public List<ADCListener> getListeners() {
		return this.listeners;
	}

	public void fireValueChanged(ADCObserver.MCP3008_input_channels channel, int newValue) {
		for (ADCListener listener : listeners) {
			listener.valueUpdated(channel, newValue);
		}
	}
}
