package tideengine;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;


public class Stations implements Serializable {
	private final static long serialVersionUID = 1L;

	private Map<String, TideStation> stations = new HashMap<>();

	public Stations() {
	}

	public Stations(Map<String, TideStation> stations) {
		this.stations = stations;
	}

	public Map<String, TideStation> getStations() {
		return stations;
	}
}
