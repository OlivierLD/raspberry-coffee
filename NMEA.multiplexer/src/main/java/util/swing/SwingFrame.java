package util.swing;

import nmea.parser.GeoPos;

import javax.swing.JFrame;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.util.List;

/**
 * A Canvas & Frame, in Swing.
 */
public class SwingFrame extends JFrame {
	private SwingFrame instance = this;
	private SwingPanel swingPanel;

	private List<GeoPos> positions = null;

	public SwingFrame(List<GeoPos> positions) {
		this.positions = positions;
		initComponents();
		this.setSize(new Dimension(400, 400));
		this.setPreferredSize(new Dimension(400, 400));
		this.setTitle("Positions");

		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension frameSize = this.getSize();
		if (frameSize.height > screenSize.height) {
			frameSize.height = screenSize.height;
		}
		if (frameSize.width > screenSize.width) {
			frameSize.width = screenSize.width;
		}
		this.setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);
		this.setVisible(true);

		final SwingFrame instance = this;
	}

	/**
	 * This method is called from within the constructor to
	 * initialize the form.
	 */
	private void initComponents() {
		swingPanel = new SwingPanel();
		swingPanel.setPointColor(Color.RED);

		this.addWindowListener(new java.awt.event.WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent evt) {
				exitForm(evt);
			}
		});
		this.add(swingPanel, BorderLayout.CENTER);

		this.pack();
	}

	private void display() {
		swingPanel.repaint();
	}

	public void doYourJob() {
		SwingFrame lcd = instance;
    instance.repaint();
		swingPanel.plot(this.positions);
	}

	/**
	 * Exit the Application
	 */
	private void exitForm(java.awt.event.WindowEvent evt) {
		System.out.println("Bye");
		System.exit(0);
	}

	/**
	 * @param args the command line arguments
	 */
	public static void main(String... args) {
		SwingFrame frame = new SwingFrame(null);
		frame.setVisible(true);

		frame.doYourJob();
	}
}
