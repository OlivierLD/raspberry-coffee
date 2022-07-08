package tideengine;

import java.io.Serializable;

public class Harmonic implements Serializable {
	@SuppressWarnings("compatibility:4189758306527325811")
	private final static long serialVersionUID = 1L;

	private String name = "";
	private double amplitude = 0D;
	private double epoch = 0D;

	public Harmonic(String name, double ampl, double e) {
		this.name = name;
		this.amplitude = ampl;
		this.epoch = e;
	}

	public String getName() {
		return name;
	}

	public double getAmplitude() {
		return amplitude;
	}

	public double getEpoch() {
		return epoch;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setAmplitude(double amplitude) {
		this.amplitude = amplitude;
	}

	public void setEpoch(double epoch) {
		this.epoch = epoch;
	}
}

