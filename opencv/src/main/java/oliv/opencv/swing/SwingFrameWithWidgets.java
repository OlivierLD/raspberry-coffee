package oliv.opencv.swing;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Toolkit;

/**
 * A Canvas & Frame, in Swing.
 */
public class SwingFrameWithWidgets extends JFrame {

	private SwingImagePanel swingImagePanel;
	private Image image = null;
	private JScrollPane scrollPane = null;

	private JCheckBox grayCheckBox = null;
	private JCheckBox blurrCheckBox = null;
	private JCheckBox threshedCheckBox = null;
	private JCheckBox cannyCheckBox = null;
	private JCheckBox contoursCheckBox = null;

	private JSlider gaussSlider = null;


	public SwingFrameWithWidgets(int origW, int origH) {
		initComponents();
		this.setSize(new Dimension(origW, origH));
		this.setPreferredSize(new Dimension(origW, origH));
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

		this.getContentPane().setLayout(new BorderLayout());

		scrollPane = new JScrollPane(swingImagePanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		this.getContentPane().add(scrollPane, BorderLayout.CENTER);

		JPanel bottomPanel = new JPanel();
		bottomPanel.setLayout(new GridBagLayout());
		bottomPanel.setBorder(BorderFactory.createTitledBorder("Transformations"));

		grayCheckBox = new JCheckBox("To Gray");
		grayCheckBox.setSelected(false);

		blurrCheckBox = new JCheckBox("To Gaussian Blurr");
		blurrCheckBox.setSelected(false);

		threshedCheckBox = new JCheckBox("To Threshed");
		threshedCheckBox.setSelected(false);

		cannyCheckBox = new JCheckBox("To Canny Edges");
		cannyCheckBox.setSelected(false);

		contoursCheckBox = new JCheckBox("With Contours");
		contoursCheckBox.setSelected(false);

		gaussSlider = new JSlider(JSlider.HORIZONTAL, 1, 51, 15);
		gaussSlider.setEnabled(true);
		gaussSlider.addChangeListener(changeEvent -> {
			// dummy
		});
		gaussSlider.setToolTipText("Gaussian Kernel size");

		bottomPanel.add(grayCheckBox, new GridBagConstraints(0,
				0,
				1,
				1,
				1.0,
				0.0,
				GridBagConstraints.WEST,
				GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));

		bottomPanel.add(blurrCheckBox, new GridBagConstraints(0,
				1,
				1,
				1,
				1.0,
				0.0,
				GridBagConstraints.WEST,
				GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
		bottomPanel.add(gaussSlider, new GridBagConstraints(1,
				1,
				1,
				1,
				1.0,
				0.0,
				GridBagConstraints.EAST,
				GridBagConstraints.HORIZONTAL,
				new Insets(0, 0, 0, 0), 0, 0));

		bottomPanel.add(threshedCheckBox, new GridBagConstraints(0,
				2,
				1,
				1,
				1.0,
				0.0,
				GridBagConstraints.WEST,
				GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
		bottomPanel.add(cannyCheckBox, new GridBagConstraints(0,
				3,
				1,
				1,
				1.0,
				0.0,
				GridBagConstraints.WEST,
				GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
		bottomPanel.add(contoursCheckBox, new GridBagConstraints(0,
				4,
				1,
				1,
				1.0,
				0.0,
				GridBagConstraints.WEST,
				GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));

		this.add(bottomPanel, BorderLayout.SOUTH);

		this.pack();
	}

	public void plot(Image img) {
		plot(img, null);
	}
	public void plot(Image img, String title) {
		if (title != null) {
			this.setTitle(title);
		}
		this.image = img;
		swingImagePanel.plot(this.image);
	}

	public boolean isGrayChecked() {
		return this.grayCheckBox.isSelected();
	}
	public boolean isBlurrChecked() {
		return this.blurrCheckBox.isSelected();
	}
	public boolean isThreshedChecked() {
		return this.threshedCheckBox.isSelected();
	}
	public boolean isCannyChecked() {
		return this.cannyCheckBox.isSelected();
	}
	public boolean isContoursChecked() {
		return this.contoursCheckBox.isSelected();
	}
	public int getGaussianKernelSize() {
		int slider = gaussSlider.getValue();
		if (slider % 2 != 1) {
			slider += 1;
		}
		return slider;
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
