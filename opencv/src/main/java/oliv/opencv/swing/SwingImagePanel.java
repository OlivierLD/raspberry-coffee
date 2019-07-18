package oliv.opencv.swing;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;

public class SwingImagePanel
		extends javax.swing.JPanel {

	private Image image = null;

	public SwingImagePanel() {
		this.setPreferredSize(new Dimension(800, 600));
		this.setSize(new Dimension(800, 600));
		this.clear();
	}

	public void clear() {
		this.repaint();
	}

	public void plot(Image img) {
		this.image = img;
		Dimension newDimension = new Dimension(img.getWidth(null), img.getHeight(null));
		this.setPreferredSize(newDimension);
		this.setSize(newDimension);
		repaint();
	}

	private final static boolean DEBUG = false;

	@Override
	public void paintComponent(Graphics gr) {
		gr.setColor(Color.white);
		gr.fillRect(0, 0, this.getWidth(), this.getHeight());
		Graphics2D g2d = (Graphics2D) gr;
		if (this.image != null) {
			g2d.drawImage(this.image, 0, 0, null);
		}
	}
}
