package hanoitower.events;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

	public static class Post {

		public int getDiscIdx(Integer i) {
			int idx = -1;
			int progress = 0;
			Iterator i$ = discList.iterator();
			do {
				if (!i$.hasNext())
					break;
				Integer d = (Integer) i$.next();
				if (d.equals(i)) {
					idx = progress;
					break;
				}
				progress++;
			} while (true);
			return idx;
		}

		public Integer getDiscAt(int i) {
			Integer d = null;
			if (i < getDiscCount())
				d = (Integer) discList.get(i);
			return d;
		}

		public void add(Integer disc) {
			discList.add(disc);
		}

		public void removeTopDisc() {
			if (getDiscCount() == 0) {
				return;
			} else {
				discList.remove(getDiscCount() - 1);
				return;
			}
		}

		public Integer getTopDisc() {
			if (getDiscCount() == 0) {
				return 0;
			} else {
				return discList.get(getDiscCount() - 1);
			}
		}

		public int getDiscCount() {
			return discList.size();
		}

		private List<Integer> discList;

		public Post() {
			discList = null;
			discList = new ArrayList<>();
		}
	}

	public static class Stand {

		public Map getPosts() {
			return posts;
		}

		public void initStand(int nb, String post) {
			nbDisc = nb;
			Post p = (Post) posts.get(post);
			for (int i = nb; i > 0; i--) {
				p.add(Integer.valueOf(i));
			}
		}

		public Post getPost(String name) {
			return (Post) posts.get(name);
		}

		public String toString() {
			String display = "\n";
			int oneDiscWidth = 2 * nbDisc;
			Set keys = posts.keySet();
			for (int i = nbDisc - 1; i >= 0; i--) {
				String line = " ";
				for (Iterator i$ = keys.iterator(); i$.hasNext(); ) {
					String k = (String) i$.next();
					Integer d = ((Post) posts.get(k)).getDiscAt(i);
					if (d == null) {
						d = 0;
					}
					int x;
					for (x = oneDiscWidth / 2; x > d.intValue(); x--) {
						line = (new StringBuilder()).append(line).append(" ").toString();
					}
					for (x = 0; x < d.intValue(); x++) {
						line = (new StringBuilder()).append(line).append("_").toString();
					}
					line = (new StringBuilder()).append(line).append("|").toString();
					for (x = 0; x < d.intValue(); x++) {
						line = (new StringBuilder()).append(line).append("_").toString();
					}
					x = oneDiscWidth / 2;
					while (x > d.intValue()) {
						line = (new StringBuilder()).append(line).append(" ").toString();
						x--;
					}
				}

				display = (new StringBuilder()).append(display).append(line).append(" \n").toString();
			}

			String line = "\n ";
			for (Iterator i$ = keys.iterator(); i$.hasNext(); ) {
				String k = (String) i$.next();
				int x;
				for (x = 0; x < oneDiscWidth / 2; x++)
					line = (new StringBuilder()).append(line).append(" ").toString();

				line = (new StringBuilder()).append(line).append(k).toString();
				x = 0;
				while (x < oneDiscWidth / 2) {
					line = (new StringBuilder()).append(line).append(" ").toString();
					x++;
				}
			}

			display = (new StringBuilder()).append(display).append(line).append(" \n").toString();
			return display;
		}

		Map<String, Post> posts;
		int nbDisc;

		public Stand(String nameOne, String nameTwo, String nameThree) {
			this.posts = new LinkedHashMap<>(3);
			this.nbDisc = 0;
			posts.put(nameOne, new Post());
			posts.put(nameTwo, new Post());
			posts.put(nameThree, new Post());
		}
	}



}
