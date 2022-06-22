package hanoitower.gui.applet;

import hanoitower.gui.PanelHolder;

import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;

@SuppressWarnings("deprecation")
public class HanoiApplet extends JApplet {

	private JPanel panelHolder;

	public HanoiApplet() {
		panelHolder = new PanelHolder();
	}

	private void jbInit()
			throws Exception {
		getContentPane().setLayout(new BorderLayout());
		setSize(new Dimension(944, 300));
		getContentPane().add(panelHolder, BorderLayout.CENTER);
	}

	public void init() {
		try {
			jbInit();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String... args) {
		HanoiApplet applet = new HanoiApplet();
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.getContentPane().add(applet, BorderLayout.CENTER);
		frame.setTitle("Hanoi Applet Frame");
		applet.init();
		applet.start();
		frame.setSize(800, 300);
		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension frameSize = frame.getSize();
		frame.setLocation((d.width - frameSize.width) / 2, (d.height - frameSize.height) / 2);
		frame.setVisible(true);
	}
}
