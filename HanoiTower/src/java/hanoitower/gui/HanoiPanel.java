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
import java.util.Iterator;
import java.util.Set;

public class HanoiPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	private HanoiPanel instance;
	private int nbDisc;
	private int nbMove;
	private static HanoiContext.Stand hanoiStand = null;
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

	public HanoiPanel() {
		this(5);
	}

	public HanoiPanel(int i) {
		instance = this;
		this.nbMove = 0;
		this.gradientPaint = null;
		this.discInFlight = null;
		this.inFlightX = 0;
		this.inFlightY = 0;
		this.oneDiscMaxWidth = 0;
		this.oneDiscThickness = 0;
		this.nbDisc = i;
		try {
			this.jbInit();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void setNbDisc(int i) {
		this.nbDisc = i;
		this.initialize();
	}

	private void jbInit() throws Exception {
		setLayout(null);
		HanoiContext.getInstance().addApplicationListener(new HanoiEventListener() {

      public void moveRequired(String from, String to) {
        instance.nbMove++;
        System.out.println((new StringBuilder()).append("Moving from ").append(from).append(" to ").append(to).toString());
	      HanoiContext.Post fromPost = HanoiPanel.hanoiStand.getPost(from);
	      HanoiContext.Post toPost = HanoiPanel.hanoiStand.getPost(to);
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
          });
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

	private void moveDiscSmoothly(Integer disc, HanoiContext.Post from, HanoiContext.Post to) {
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
			for (Iterator iterator = keys.iterator(); iterator.hasNext(); ) {
				String k = (String) iterator.next();
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

		int nbStep = HanoiContext.getInstance().getDiscMoveInterval(); // Smaller is faster. 1..100
		for (int i = 0; i < nbStep; i++) {
			nbStep = HanoiContext.getInstance().getDiscMoveInterval();
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

		if (this.nbMove != 0) {
			gr.setColor(Color.green);
			String txt = String.format("Move %d/%d", this.nbMove, (int)(Math.pow(2, this.nbDisc) - 1));
			gr.drawString(txt, 5, 20);
		}
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
				Integer d = ((HanoiContext.Post) hanoiStand.getPosts().get(k)).getDiscAt(i);
				int postAxisX = (postIdx + 1) * (getWidth() / 4);
				if (d == null) {
					d = 0;
				}
				if (d.intValue() > 0) {
					drawDisc(gr, d, postAxisX, discBaseY);
				}
				postIdx++;
			}

		}

	}

	private void drawDisc(Graphics gr, Integer disc, int centerX, int bottomY) {
		if (gradient && gradientPaint == null) {
			gradientPaint = new GradientPaint(0.0F, 0.0F, Color.blue, getWidth(), getHeight(), Color.yellow);
		}
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
		String number = String.valueOf(disc);
		Font f = gr.getFont();
		int fontSize = (int)Math.round(50 * (4f / nbDisc));
		gr.setFont(f.deriveFont((float)fontSize));
		int numberLen = gr.getFontMetrics(gr.getFont()).stringWidth(number);
		if (!persp) {
			gr.drawRect(centerX - discWidth / 2, bottomY - oneDiscThickness, discWidth, oneDiscThickness);
			// Draw disc #
			gr.setColor(Color.cyan);
			gr.drawString(number, centerX - (numberLen / 2), bottomY- ((oneDiscThickness - fontSize) / 2));
		} else {
			gr.drawOval(centerX - discWidth / 2, bottomY - (int) ((((double) disc.intValue() / (double) nbDisc) * (double) oneDiscThickness) / 2D) - oneDiscThickness, discWidth, (int) (((double) disc.intValue() / (double) nbDisc) * (double) oneDiscThickness));
			gr.drawLine(centerX - discWidth / 2, bottomY - oneDiscThickness, centerX - discWidth / 2, bottomY);
			gr.drawLine(centerX + discWidth / 2, bottomY - oneDiscThickness, centerX + discWidth / 2, bottomY);
			gr.drawArc(centerX - discWidth / 2, bottomY - (int) ((((double) disc.intValue() / (double) nbDisc) * (double) oneDiscThickness) / 2D), discWidth, (int) (((double) disc.intValue() / (double) nbDisc) * (double) oneDiscThickness), 0, -180);
			// Draw disc #
			gr.setColor(Color.cyan);
			gr.drawString(number, centerX - (numberLen / 2), bottomY - ((oneDiscThickness - fontSize) / 2) + (int) ((((double) disc.intValue() / (double) nbDisc) * (double) oneDiscThickness) / 2D));
		}
		gr.setFont(f);
	}

	public void initialize() {
		System.out.println("Initializing");
		String post1 = System.getProperty("post.one", "A");
		String post2 = System.getProperty("post.two", "B");
		String post3 = System.getProperty("post.three", "C");
		hanoiStand = new HanoiContext.Stand(post1, post2, post3);
		String initialPost = System.getProperty("initial.post", "A");
		hanoiStand.initStand(nbDisc, initialPost);
	}

	public synchronized void startSolving() {
		initialize();
		System.out.println(String.format("Starting solving, anticipating %d moves.", (int)(Math.pow(2, this.nbDisc) - 1)));
		this.nbMove = 0;
		BackendAlgorithm.move(this.nbDisc, "A", "C", "B");
		System.out.println((new StringBuilder())
				.append("Finished in ")
				.append(this.nbMove)
				.append(" moves.").toString());
		HanoiContext.getInstance().fireComputationCompleted();
	}
}
