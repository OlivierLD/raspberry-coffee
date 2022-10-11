package tideengine;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class Constituents implements Serializable {
	@SuppressWarnings("compatibility:9177290185319880922")
	private final static long serialVersionUID = 1L;
	private final Map<String, ConstSpeed> constSpeedMap = new LinkedHashMap<>();

	public Map<String, ConstSpeed> getConstSpeedMap() {
		return constSpeedMap;
	}

	public static class ConstSpeed implements Serializable {
		@SuppressWarnings("compatibility:-5425660072213952784")
		private final static long serialVersionUID = 1L;

		private int idx = 0;
		private String coeffName = "";
		private double coeffValue = 0d;
		private final Map<Integer, Double> equilibrium = new HashMap<>();
		private final Map<Integer, Double> factors = new HashMap<>();

		public ConstSpeed(int idx, String name, double val) {
			this.idx = idx;
			this.coeffName = name;
			this.coeffValue = val;
		}

		public void putEquilibrium(int year, double val) {
			equilibrium.put(year, val);
		}

		public void putFactor(int year, double val) {
			factors.put(year, val);
		}

		public String getCoeffName() {
			return coeffName;
		}

		public double getCoeffValue() {
			return coeffValue;
		}

		public Map<Integer, Double> getEquilibrium() {
			return equilibrium;
		}

		public Map<Integer, Double> getFactors() {
			return factors;
		}
	}
}
