package dnd.gui.splash;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import javax.swing.JPanel;

public class SpecialProgressBar
		extends JPanel {
	private static final long serialVersionUID = 1L;
	SpecialProgressBar instance = this;

	public SpecialProgressBar() {
		try {
			jbInit();
			Thread thread = new Thread()
			{

				public void run()
				{
					for (;;) {
						SpecialProgressBar.this.instance.repaint();
						try {
							Thread.sleep(100L);
						} catch (Exception ex) {
							ex.printStackTrace();
						}
					}
				}
			};
			thread.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void jbInit() throws Exception {
		setLayout(null);
		setSize(new Dimension(350, 25));
	}

	public void paintComponent(Graphics g) {
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, getWidth(), getHeight());
		g.setColor(Color.BLACK);
		g.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
		g.setColor(Color.GREEN);
		for (int i = 0; i < 50; i++) {
			int x = (int) Math.round(Math.random() * getWidth());
			g.drawLine(x, 0, x, getHeight());
		}
	}
}
