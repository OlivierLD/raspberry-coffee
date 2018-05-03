package hanoitower.gui;

import hanoitower.BackendAlgorithm;
import hanoitower.events.HanoiContext;
import hanoitower.events.HanoiEventListener;
import hanoitower.gui.bg.Images;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Area;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class HanoiPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	private HanoiPanel instance;
	private int nbDisc;
	private int nbMove;
	private static Stand hanoiStand = null;
	private static boolean persp = true;
	private static boolean smooth = true;
	private static boolean texture = true;
	private static boolean gradient = true;
	private static boolean transparent = true;
	private transient GradientPaint gradientPaint;
	private Integer discInFlight;
	private int inFlightX;
	private int inFlightY;
	private int oneDiscMaxWidth;
	private int oneDiscThickness;

	static {
		try {
			persp = "true".equals(System.getProperty("with.perspective", "true"));
			smooth = "true".equals(System.getProperty("with.smooth", "true"));
			texture = "true".equals(System.getProperty("with.texture", "true"));
			gradient = "true".equals(System.getProperty("with.gradient", "true"));
			transparent = "true".equals(System.getProperty("with.transparency", "true"));
		} catch (Exception ex) {
			System.err.println(ex.toString());
		}
	}

	private static class Post {

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
			posts = new LinkedHashMap<>(3);
			nbDisc = 0;
			posts.put(nameOne, new Post());
			posts.put(nameTwo, new Post());
			posts.put(nameThree, new Post());
		}
	}


	public HanoiPanel() {
		this(5);
	}

	public HanoiPanel(int i) {
		instance = this;
		nbDisc = 5;
		nbMove = 0;
		gradientPaint = null;
		discInFlight = null;
		inFlightX = 0;
		inFlightY = 0;
		oneDiscMaxWidth = 0;
		oneDiscThickness = 0;
		nbDisc = i;
		try {
			jbInit();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void setNbDisc(int i) {
		nbDisc = i;
		initialize();
	}

	private void jbInit() throws Exception {
		setLayout(null);
		HanoiContext.getInstance().addApplicationListener(new HanoiEventListener() {

      public void moveRequired(String from, String to) {
        nbMove++;
        System.out.println((new StringBuilder()).append("Moving from ").append(from).append(" to ").append(to).toString());
        Post fromPost = HanoiPanel.hanoiStand.getPost(from);
        Post toPost = HanoiPanel.hanoiStand.getPost(to);
        Integer discToMove = fromPost.getTopDisc();
        Integer otherDisc = toPost.getTopDisc();
        if (otherDisc != null && otherDisc.intValue() != 0 && otherDisc.intValue() < discToMove.intValue()) {
          JOptionPane.showMessageDialog(instance, (new StringBuilder()).append("Un-authorized move!!!\n").append(discToMove.toString()).append(" cannot go on top of ").append(otherDisc.toString()).toString(), "Error", 0);
          if ("true".equals(System.getProperty("fail.on.forbidden.move", "false"))) {
	          throw new RuntimeException((new StringBuilder()).append("Un-authorized move, ").append(discToMove.toString()).append(" cannot go on top of ").append(otherDisc.toString()).toString());
          }
        }
        if (HanoiPanel.smooth) {
	        instance.moveDiscSmoothly(discToMove, fromPost, toPost);
        }
        fromPost.removeTopDisc();
        toPost.add(discToMove);
        try {
          SwingUtilities.invokeAndWait(() -> {
            repaint();
            try {
              Thread.sleep(100L);
            } catch (Exception ex) {
              ex.printStackTrace();
            }
          }
          );
        } catch (Exception ex) {
          ex.printStackTrace();
        }
      }

      public void setNbDisc(int i) {
        instance.setNbDisc(i);
        instance.repaint();
      }

      public void startComputation() {
        Thread solver = new Thread(() -> instance.startSolving());
        solver.start();
      }
    });
		initialize();
	}

	private void moveDiscSmoothly(Integer disc, Post from, Post to) {
		discInFlight = disc;
		int fromX = 0;
		int toX = 0;
		int fromY = 0;
		int toY = 0;
		Set keys = hanoiStand.getPosts().keySet();
		for (int i = 0; i < nbDisc; i++) {
			int discBaseY = getHeight() - (i + 1) * oneDiscThickness;
			if (i == from.getDiscIdx(disc)) {
				fromY = discBaseY;
			}
			if (i == to.getDiscCount()) {
				toY = discBaseY;
			}
			int postIdx = 0;
			for (Iterator i$ = keys.iterator(); i$.hasNext(); ) {
				String k = (String) i$.next();
				int postAxisX = (postIdx + 1) * (getWidth() / 4);
				if (from.equals(hanoiStand.getPosts().get(k))) {
					fromX = postAxisX;
				}
				if (to.equals(hanoiStand.getPosts().get(k))) {
					toX = postAxisX;
				}
				postIdx++;
			}

		}

		int nbStep = 20; // Smaller is faster
		for (int i = 0; i < nbStep; i++) {
			double x = (double) fromX + (double) (i * (toX - fromX)) / (double) nbStep;
			double y = ((double) fromY + (double) (i * (toY - fromY)) / (double) nbStep) - (double) oneDiscThickness * (double) Math.min(i, nbStep - i) * 0.05D;
			inFlightX = (int) Math.round(x);
			inFlightY = (int) Math.round(y);
			try {
				SwingUtilities.invokeAndWait(() -> repaint());
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		discInFlight = null;
	}

	public void paintComponent(Graphics gr) {
		Graphics2D g2d = (Graphics2D) gr;
		g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		if (texture) {
			ImageIcon bgImage = new ImageIcon(Images.class.getResource("carbon.fiber.1280x800.png"));
			g2d.drawImage(bgImage.getImage(), 0, 0, null);
		} else {
			gr.setColor(Color.white);
			gr.fillRect(0, 0, getWidth(), getHeight());
		}
		if (!persp) {
			drawStand(gr);
		} else {
			drawStandNames(gr);
		}
		drawDiscs(gr);
	}

	private void drawStand(Graphics gr) {
		int postWidth = 4;
		gr.setColor(Color.orange);
		for (int i = 0; i < 3; i++) {
			int postAxisX = (i + 1) * (getWidth() / 4);
			gr.fillRect(postAxisX - postWidth / 2, 0, postWidth, getHeight());
		}

	}

	private void drawStandNames(Graphics gr) {
		gr.setColor(Color.red);
		Set names = hanoiStand.getPosts().keySet();
		Font f = gr.getFont();
		gr.setFont(f.deriveFont(1, getHeight() / 3));
		Iterator iterator = names.iterator();
		for (int i = 0; i < 3; i++) {
			int postAxisX = (i + 1) * (getWidth() / 4);
			String name = (String) iterator.next();
			int l = gr.getFontMetrics(gr.getFont()).stringWidth(name);
			gr.drawString(name, postAxisX - l / 2, 30 + getHeight() / 2);
		}

		gr.setFont(f);
	}

	private void drawDiscs(Graphics gr) {
		oneDiscMaxWidth = getWidth() / 4;
		oneDiscThickness = getHeight() / (nbDisc + 2);
		Set keys = hanoiStand.getPosts().keySet();
		for (int i = 0; i < nbDisc; i++) {
			int discBaseY = getHeight() - (i + 1) * oneDiscThickness;
			int postIdx = 0;
			for (Iterator i$ = keys.iterator(); i$.hasNext(); ) {
				String k = (String) i$.next();
				Integer d = ((Post) hanoiStand.getPosts().get(k)).getDiscAt(i);
				int postAxisX = (postIdx + 1) * (getWidth() / 4);
				if (d == null)
					d = 0;
				if (d.intValue() > 0)
					drawDisc(gr, d, postAxisX, discBaseY);
				postIdx++;
			}

		}

	}

	private void drawDisc(Graphics gr, Integer disc, int centerX, int bottomY) {
		if (gradient && gradientPaint == null)
			gradientPaint = new GradientPaint(0.0F, 0.0F, Color.blue, getWidth(), getHeight(), Color.yellow);
		int discWidth = disc.intValue() * (oneDiscMaxWidth / nbDisc) - disc.intValue();
		if (discInFlight != null && disc.equals(discInFlight)) {
			centerX = inFlightX;
			bottomY = inFlightY;
		}
		if (!gradient) {
			gr.setColor(Color.blue);
		} else {
			((Graphics2D) gr).setPaint(gradientPaint);
		}
		if (transparent) {
			((Graphics2D) gr).setComposite(AlphaComposite.getInstance(3, 0.5F));
		}
		if (!persp) {
			gr.fillRect(centerX - discWidth / 2, bottomY - oneDiscThickness, discWidth, oneDiscThickness);
		} else {
			java.awt.Shape top = new java.awt.geom.Ellipse2D.Double(centerX - discWidth / 2, bottomY - (int) ((((double) disc.intValue() / (double) nbDisc) * (double) oneDiscThickness) / 2D) - oneDiscThickness, discWidth, ((double) disc.intValue() / (double) nbDisc) * (double) oneDiscThickness);
			java.awt.Shape middle = new java.awt.geom.Rectangle2D.Double(centerX - discWidth / 2, bottomY - oneDiscThickness, discWidth, oneDiscThickness);
			java.awt.Shape bottom = new java.awt.geom.Ellipse2D.Double(centerX - discWidth / 2, bottomY - (int) ((((double) disc.intValue() / (double) nbDisc) * (double) oneDiscThickness) / 2D), discWidth, ((double) disc.intValue() / (double) nbDisc) * (double) oneDiscThickness);
			Area areaOne = new Area(top);
			Area areaTwo = new Area(middle);
			Area areaThree = new Area(bottom);
			areaOne.add(areaTwo);
			areaOne.add(areaThree);
			((Graphics2D) gr).fill(areaOne);
		}
		if (transparent) {
			((Graphics2D) gr).setComposite(AlphaComposite.getInstance(3, 1.0F));
		}
		gr.setColor(Color.black);
		if (!persp) {
			gr.drawRect(centerX - discWidth / 2, bottomY - oneDiscThickness, discWidth, oneDiscThickness);
		} else {
			gr.drawOval(centerX - discWidth / 2, bottomY - (int) ((((double) disc.intValue() / (double) nbDisc) * (double) oneDiscThickness) / 2D) - oneDiscThickness, discWidth, (int) (((double) disc.intValue() / (double) nbDisc) * (double) oneDiscThickness));
			gr.drawLine(centerX - discWidth / 2, bottomY - oneDiscThickness, centerX - discWidth / 2, bottomY);
			gr.drawLine(centerX + discWidth / 2, bottomY - oneDiscThickness, centerX + discWidth / 2, bottomY);
			gr.drawArc(centerX - discWidth / 2, bottomY - (int) ((((double) disc.intValue() / (double) nbDisc) * (double) oneDiscThickness) / 2D), discWidth, (int) (((double) disc.intValue() / (double) nbDisc) * (double) oneDiscThickness), 0, -180);
		}
	}

	public void initialize() {
		System.out.println("Initializing");
		String post1 = System.getProperty("post.one", "A");
		String post2 = System.getProperty("post.two", "B");
		String post3 = System.getProperty("post.three", "C");
		hanoiStand = new Stand(post1, post2, post3);
		String initialPost = System.getProperty("initial.post", "A");
		hanoiStand.initStand(nbDisc, initialPost);
	}

	public synchronized void startSolving() {
		initialize();
		System.out.println("Starting solving");
		nbMove = 0;
		BackendAlgorithm.move(nbDisc, "A", "C", "B");
		System.out.println((new StringBuilder())
				.append("Finished in ")
				.append(nbMove)
				.append(" moves.").toString());
		HanoiContext.getInstance().fireComputationCompleted();
	}
}
