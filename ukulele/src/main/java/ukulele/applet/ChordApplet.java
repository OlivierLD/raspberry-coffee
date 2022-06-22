package ukulele.applet;

import chordfinder.AllChordPanel;
import chordfinder.UkuleleChordFinder;
import ctx.AppContext;
import java.applet.Applet;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JApplet;
import javax.swing.JFrame;
import javax.swing.JPanel;

@SuppressWarnings("deprecation")
public class ChordApplet
				extends JApplet {
	private BorderLayout borderLayout1 = new BorderLayout();
	private JPanel allChordPanel = new AllChordPanel();


	private void jbInit()
					throws Exception {
		getContentPane().setLayout(this.borderLayout1);
		setSize(new Dimension(820, 525));
		getContentPane().add(this.allChordPanel, BorderLayout.CENTER);
	}

	public void init() {
		String lang;
		try {
			lang = getParameter("lang");
		} catch (Exception ex) {
			lang = "EN";
		}
		AppContext.getInstance().fireUserLanguage(lang);
		try {
			jbInit();
		} catch (Exception e) {
			e.printStackTrace();
		}
		this.repaint();
	}


	private void jButton1_actionPerformed(ActionEvent e) {
		UkuleleChordFinder.main((String[])null);
	}

	public static void main(String... args) {
		System.out.println("You can also try to run chordfinder.UkuleleChordFinder");
		JFrame frame = new JFrame("Ukulele Chord Finder, 1008 Chords. Tuned G C E A");
		frame.setSize(820, 525);

		final Applet applet = new ChordApplet();

		frame.getContentPane().add(applet);
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent we) {
				applet.stop();
				applet.destroy();
				System.exit(0);
			}
		});

		frame.setVisible(true);
		applet.init();
		applet.start();
	}
}
