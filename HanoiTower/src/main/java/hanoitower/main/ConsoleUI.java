package hanoitower.main;

import hanoitower.BackendAlgorithm;
import hanoitower.events.HanoiContext;
import hanoitower.events.HanoiEventListener;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ConsoleUI {

	private static int nbMove = 0;
	private static Stand hanoiStand = null;

	private static class Post {

		public Integer getDiscAt(int i) {
			Integer d = null;
			if (i < getDiscCount())
				d = discList.get(i);
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
			discList = new ArrayList<>();
		}
	}

	private static class Stand {

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
				for (x = 0; x < oneDiscWidth / 2; x++) {
					line = (new StringBuilder()).append(line).append(" ").toString();
				}
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
			posts = new LinkedHashMap<>(3);
			nbDisc = 0;
			posts.put(nameOne, new Post());
			posts.put(nameTwo, new Post());
			posts.put(nameThree, new Post());
		}
	}


	public ConsoleUI() {
		HanoiContext.getInstance().addApplicationListener(new HanoiEventListener() {
      public void moveRequired(String from, String to) {
        ConsoleUI.nbMove++;
        System.out.println((new StringBuilder())
		        .append("Moving from ")
		        .append(from).append(" to ")
		        .append(to)
		        .toString());
        Post fromPost = ConsoleUI.hanoiStand.getPost(from);
        Post toPost = ConsoleUI.hanoiStand.getPost(to);
        Integer discToMove = fromPost.getTopDisc();
        fromPost.removeTopDisc();
        toPost.add(discToMove);
        System.out.println(ConsoleUI.hanoiStand.toString());
      }
    });
	}

	public static void main(String... args) {
		new ConsoleUI();
		int nbDisc = 5;
		if (args.length > 0) {
			try {
				nbDisc = Integer.parseInt(args[0]);
			} catch (Exception ex) {
				System.err.println(ex.toString());
			}
		}
		System.out.println(String.format("Anticipating %d moves...", (int)(Math.pow(2, nbDisc) - 1)));

		hanoiStand = new Stand("A", "B", "C");
		hanoiStand.initStand(nbDisc, "A");
		System.out.println("Moving the tower from A to C");
		System.out.println(hanoiStand.toString());
		BackendAlgorithm.move(nbDisc, "A", "C", "B");
		System.out.println((new StringBuilder())
				.append("Finished in ")
				.append(nbMove)
				.append(" moves.")
				.toString());
	}
}
