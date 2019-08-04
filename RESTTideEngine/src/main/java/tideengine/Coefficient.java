package tideengine;

import java.io.Serializable;


public class Coefficient implements Serializable {
	@SuppressWarnings("compatibility:-3501902787423374405")
	private final static long serialVersionUID = 1L;
	private String name = "";
	private double value = 0D;

	public Coefficient(String name, double d) {
		this.name = name;
		this.value = d;
	}

	public String getName() {
		return name;
	}

	public double getValue() {
		return value;
	}
}
