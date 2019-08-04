package oliv.opencv.swing;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * A Canvas & Frame, in Swing.
 */
public class SwingFrame extends JFrame {

	private SwingImagePanel swingImagePanel;
	private Image image = null;
	private JScrollPane scrollPane = null;

	public SwingFrame() {
		initComponents();
		this.setSize(new Dimension(400, 500));
		this.setPreferredSize(new Dimension(400, 500));
		this.setTitle("OpenCV");

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
	}

	/**
	 * This method is called from within the constructor to
	 * initialize the form.
	 */
	private void initComponents() {
		swingImagePanel = new SwingImagePanel();

		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent evt) {
				System.out.println("Bye!");
				exitForm(evt);
			}
		});

		this.getContentPane().setLayout(new BorderLayout());

		scrollPane = new JScrollPane(swingImagePanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		this.getContentPane().add(scrollPane, BorderLayout.CENTER);
		this.pack();
	}

	public void plot(Image img, String title) {
		this.setTitle(title);
		this.image = img;
		swingImagePanel.plot(this.image);
	}

	/**
	 * Exit the Application
	 */
	private void exitForm(WindowEvent evt) {
		System.out.println("Done");
		System.exit(0);
	}

	/**
	 * @param args the command line arguments
	 */
	public static void main(String... args) {
		SwingFrame frame = new SwingFrame();
		frame.setVisible(true);

		frame.plot(null, "Dummy");
	}
}
