package dnd.gui.splash;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.MediaTracker;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JWindow;

public class SplashWindow
		extends JWindow {
	private static SplashWindow instance;
	private static final long serialVersionUID = 1L;
	private transient Image image;
	private boolean paintCalled = false;

	private GridBagLayout gridBagLayout1 = new GridBagLayout();
	private JLabel copyrightLabel = new JLabel();

	private SpecialProgressBar loadProgressBar = new SpecialProgressBar();
	private JLabel loadingLabel = new JLabel();

	private static final int H = 200;
	private static final int W = 325;

	private SplashWindow(Frame parent, Image image, JFrame parentFrame) {
		super(parent);
		this.image = image;

		MediaTracker mt = new MediaTracker(this);
		mt.addImage(image, 0);
		try {
			mt.waitForID(0);
		} catch (InterruptedException ie) {
			System.err.println(ie.toString());
		}

		if (mt.isErrorID(0)) {
			setSize(0, 0);
			System.err.println("Warning: SplashWindow couldn't load splash image.");
			synchronized (this) {
				this.paintCalled = true;
				notifyAll();
			}
			return;
		}

		Dimension screenDim = null;
		if (parentFrame == null) {
			screenDim = Toolkit.getDefaultToolkit().getScreenSize();
		} else {
			screenDim = parentFrame.getSize();
		}
		Dimension dim = new Dimension(325, 200);
		setSize(dim);
		int x = 0;
		int y = 0;
		if (parentFrame != null) {
			Point location = parentFrame.getLocation();
			x = location.x;
			y = location.y;
		}
		setLocation(x + (screenDim.width - dim.width) / 2, y + (screenDim.height - dim.height) / 2);

		JLayeredPane layer = new JLayeredPane();

		ImageIcon img = new ImageIcon(getClass().getResource("paperboat.png"));
		JLabel imgHolder = new JLabel(img);

		imgHolder.setBounds(0, 10, 325, 150);
		layer.add(imgHolder, JLayeredPane.DRAG_LAYER);

		JPanel itemHolder = new JPanel();
		itemHolder.setLayout(this.gridBagLayout1);
		itemHolder.setOpaque(false);

		this.copyrightLabel.setText("© OlivSoft, 2011");
		this.copyrightLabel.setForeground(Color.red);

		this.loadingLabel.setText("Loading Database...");
		this.loadingLabel.setForeground(Color.red);
		itemHolder.add(this.loadingLabel, new GridBagConstraints(0, 0, 2, 1, 0.0D, 0.0D, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(140, 0, 0, 0), 0, 0));

		this.loadProgressBar.setPreferredSize(new Dimension(250, 20));
		itemHolder.add(this.loadProgressBar, new GridBagConstraints(0, 1, 2, 1, 0.0D, 0.0D, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		itemHolder.add(this.copyrightLabel, new GridBagConstraints(0, 2, 2, 1, 0.0D, 0.0D, GridBagConstraints.SOUTHWEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));

		itemHolder.setBounds(0, 0, 325, 200);
		layer.add(itemHolder, JLayeredPane.PALETTE_LAYER);

		setContentPane(layer);

		MouseAdapter disposeOnClick = new MouseAdapter() {
			public void mouseClicked(MouseEvent evt) {
				synchronized (SplashWindow.this) {
					SplashWindow.this.paintCalled = true;
					SplashWindow.this.notifyAll();
				}
				SplashWindow.this.dispose();
			}
		};
		addMouseListener(disposeOnClick);
	}

	public void update(Graphics g)
	{
		paint(g);
	}

	public void paint(Graphics g) {
		super.paint(g);
		super.setBackground(Color.white);
		int w = getWidth();
		int h = getHeight();
		g.setColor(Color.RED);
		g.drawRoundRect(2, 2, w - 4, h - 4, 5, 5);

		if (!this.paintCalled) {
			this.paintCalled = true;
			synchronized (this) {
				notifyAll();
			}
		}
	}

	public static void splash(Image image)
	{
		splash(image, null);
	}

	public static void splash(Image image, JFrame parent) {
		if ((instance == null) && (image != null)) {
			Frame f = new Frame();
			instance = new SplashWindow(f, image, parent);
			instance.setVisible(true);

			if ((!EventQueue.isDispatchThread()) && (Runtime.getRuntime().availableProcessors() == 1)) {
				synchronized (instance) {
					while (!instance.paintCalled) {
						try {
							instance.wait();
						} catch (InterruptedException e) {
							System.err.println(e.toString());
						}
					}
				}
			}
		}
	}

	public static void splash(URL imageURL, JFrame parent) {
		if (imageURL != null) {
			splash(Toolkit.getDefaultToolkit().createImage(imageURL), parent);
		}
	}

	public static void disposeSplash() {
		if (instance != null) {
			instance.getOwner().dispose();
			instance = null;
		}
	}

	public static void invokeMain(String className, String... args) {
		try {
			Class.forName(className).getMethod("main", new Class[] {String[].class}).invoke(null, new Object[] {args});
		} catch (Exception e) {
			InternalError error = new InternalError("Failed to invoke main method");
			error.initCause(e);
			throw error;
		}
	}
}
