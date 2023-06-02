package gsg.SwingUtils.fullui;

import gsg.SwingUtils.Box3D;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.text.DecimalFormat;
import java.util.function.BiConsumer;

/**
 * A Box3D in a JFrame, in Swing.
 * With all kinds of widgets to interact with the Box3D.
 */
public class ThreeDFrameWithWidgetsV2
		extends JFrame {

	private final Box3D box3D;
	private ThreeDPanelWithWidgets threeDPanel;

	public final static int DEFAULT_WIDTH = 800;
	public final static int DEFAULT_HEIGHT = 800;

	public ThreeDFrameWithWidgetsV2(Box3D box3D) {
		this(box3D, DEFAULT_WIDTH, DEFAULT_HEIGHT, null, false);
	}
	public ThreeDFrameWithWidgetsV2(Box3D box3D, boolean showAnimate) {
		this(box3D, DEFAULT_WIDTH, DEFAULT_HEIGHT, null, showAnimate);
	}


	public ThreeDFrameWithWidgetsV2(Box3D box3D, String title) {
		this(box3D, DEFAULT_WIDTH, DEFAULT_HEIGHT, title, false);
	}

	public ThreeDFrameWithWidgetsV2(Box3D box3D, int width, int height) {
		this(box3D, width, height, null, false);
	}
	public ThreeDFrameWithWidgetsV2(Box3D box3D, int width, int height, String title, boolean showAnimate) {
		this.box3D = box3D;
		this.threeDPanel = new ThreeDPanelWithWidgets(box3D, width, height, title, showAnimate);

		initComponents();
		this.setSize(new Dimension(width, height));  // Maybe conflicting...
		this.setPreferredSize(new Dimension(width, height));
		this.setTitle(title == null ? "Box3D demo - Figure is draggable" : title);

		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension frameSize = this.getSize();
		if (frameSize.height > screenSize.height) {
			frameSize.height = screenSize.height;
		}
		if (frameSize.width > screenSize.width) {
			frameSize.width = screenSize.width;
		}
		this.setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setVisible(true);
	}

	/**
	 * This method is called from within the constructor to
	 * initialize the form.
	 */
	private void initComponents() {
		this.getContentPane().setLayout(new BorderLayout());
		this.add(threeDPanel, BorderLayout.CENTER);
		this.pack();
	}
}
