package ctx;

import java.util.ArrayList;
import java.util.List;

public class AppContext {
	private static AppContext instance = null;
	private List<AppListener> appListeners = new ArrayList<>();

	public static synchronized AppContext getInstance() {
		if (instance == null) {
			instance = new AppContext();
		}
		return instance;
	}

	public void addAppListener(AppListener l) {
		if (!this.appListeners.contains(l)) {
			this.appListeners.add(l);
		}
	}

	public void removeAppListener(AppListener l) {
		if (this.appListeners.contains(l)) {
			this.appListeners.remove(l);
		}
	}

	public void fireUserLanguage(String s) {
		for (AppListener al : this.appListeners) {
			al.setUserLanguage(s);
		}
	}
}
