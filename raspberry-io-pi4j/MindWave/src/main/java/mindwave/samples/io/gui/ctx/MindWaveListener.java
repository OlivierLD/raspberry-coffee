package mindwave.samples.io.gui.ctx;

import java.util.EventListener;

public abstract class MindWaveListener implements EventListener {
	public void serialConnected() {
	}

	public void serialDisconnected() {
	}

	public void connect(String port, int br) {
	}

	public void disconnect() {
	}

	public void mindWaveStatus(String status) {
	}

	public void addRawData(short data) {
	}

	public void setMinRaw(int v) {
	}

	public void setMaxRaw(int v) {
	}

	public void setAttention(int v) {
	}

	public void setMeditation(int v) {
	}

	public void setSerialData(String str) {
	}

	public void parsing(byte[] ba) {
	}

	public void setAvg(int avg) {
	}

	public void eyeBlink() {
	}
}
