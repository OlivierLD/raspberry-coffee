package hanoitower.events;

import java.util.ArrayList;
import java.util.List;

public class HanoiContext {

	private static HanoiContext instance = null;
	private List<HanoiEventListener> applicationListeners;
	private int discMoveInterval = 50;

	private HanoiContext() {
		applicationListeners = new ArrayList<>();
	}

	public static synchronized HanoiContext getInstance() {
		if (instance == null) {
			instance = new HanoiContext();
		}
		return instance;
	}

	public List<HanoiEventListener> getListeners() {
		return applicationListeners;
	}

	public synchronized void addApplicationListener(HanoiEventListener l) {
		if (!getListeners().contains(l))
			getListeners().add(l);
	}

	public synchronized void removeApplicationListener(HanoiEventListener l) {
		getListeners().remove(l);
	}

	public void fireMoveRequired(String from, String to) {
		for (int i = 0; i < instance.getListeners().size(); i++) {
			HanoiEventListener l = instance.getListeners().get(i);
			l.moveRequired(from, to);
		}
	}

	public void fireStartComputation() {
		for (int i = 0; i < instance.getListeners().size(); i++) {
			HanoiEventListener l = instance.getListeners().get(i);
			l.startComputation();
		}
	}

	public void fireComputationCompleted() {
		for (int i = 0; i < instance.getListeners().size(); i++) {
			HanoiEventListener l = instance.getListeners().get(i);
			l.computationCompleted();
		}
	}

	public void fireSetNbDisc(int n) {
		for (int i = 0; i < instance.getListeners().size(); i++) {
			HanoiEventListener l = instance.getListeners().get(i);
			l.setNbDisc(n);
		}
	}

	public void setDiscMoveInterval(int value) {
		this.discMoveInterval = value;
	}

	public int getDiscMoveInterval() {
		return this.discMoveInterval;
	}
}
