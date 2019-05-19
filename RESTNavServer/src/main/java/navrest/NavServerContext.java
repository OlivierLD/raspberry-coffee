package navrest;

import java.util.HashMap;
import java.util.Map;

/**
 * A Singleton, used to store whatever value in the NavServer context.
 * aka a Dump.
 */
public class NavServerContext {

	private static NavServerContext instance = null;
	private Map<String, Object> map = null;

	private NavServerContext() {
		this.map = new HashMap<>();
	}

	public static NavServerContext getInstance() {
		if (instance == null) {
			instance = new NavServerContext();
		}
		return instance;
	}

	public void put(String key, Object value) {
		this.map.put(key, value);
	}

	public Object get(String key) {
		return this.map.get(key);
	}
}
