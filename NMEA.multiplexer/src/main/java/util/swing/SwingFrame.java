package util.swing;

import util.LogAnalyzer;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.border.LineBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * A Canvas & Frame, in Swing.
 */
public class SwingFrame extends JFrame {
	private SwingPanel swingPanel;

	private List<LogAnalyzer.DatedPosition> positions = null;

	private JSlider fromSlider = null;
	private JSlider toSlider = null;
	private final int MAX_SLIDER = 1_000;
	private JCheckBox allAtOnce = null;
	private JButton plotButton = null;

	public SwingFrame(List<LogAnalyzer.DatedPosition> positions) {
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
	}

	/**
	 * This method is called from within the constructor to
	 * initialize the form.
	 */
	private void initComponents() {
		swingPanel = new SwingPanel();
		swingPanel.setPointColor(Color.RED);

		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(java.awt.event.WindowEvent evt) {
				System.out.println("Bye!");
				exitForm(evt);
			}
		});
		this.add(swingPanel, BorderLayout.CENTER);
		JPanel bottomPanel = new JPanel();
		bottomPanel.setLayout(new GridBagLayout());
		bottomPanel.setBorder(new LineBorder(Color.GRAY, 1));

		// Populate bottom panel, plotButton (zoom in and out, execute), sliders (from, to), etc
		JLabel fromLabel = new JLabel("From");
		fromSlider = new JSlider(JSlider.HORIZONTAL, 0, MAX_SLIDER, 0);
		fromSlider.setEnabled(true);
		fromSlider.addChangeListener(changeEvent -> {
//			System.out.println("From listener" + changeEvent);
//			fromSlider.setToolTipText(String.format("%d", fromSlider.getValue()));
			setTooltipText(fromSlider);
			manageSliders(FROM);
		});
		setTooltipText(fromSlider);

		JLabel toLabel = new JLabel("To");
		toSlider = new JSlider(JSlider.HORIZONTAL, 0, MAX_SLIDER, MAX_SLIDER);
		toSlider.setEnabled(true);
//		toSlider.setValue(1000);
		toSlider.addChangeListener(changeEvent -> {
//			System.out.println("To listener" + changeEvent);
//			toSlider.setToolTipText(String.format("%d", toSlider.getValue()));
			setTooltipText(toSlider);
			manageSliders(TO);
		});
		setTooltipText(toSlider);

		bottomPanel.add(fromLabel, new GridBagConstraints(0,
				0,
				1,
				1,
				0.0,
				0.0,
				GridBagConstraints.EAST,
				GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
		bottomPanel.add(fromSlider, new GridBagConstraints(1,
				0,
				1,
				1,
				1.0,
				0.0,
				GridBagConstraints.EAST,
				GridBagConstraints.HORIZONTAL,
				new Insets(0, 0, 0, 0), 0, 0));

		bottomPanel.add(toLabel, new GridBagConstraints(0,
				1,
				1,
				1,
				0.0,
				0.0,
				GridBagConstraints.EAST,
				GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
		bottomPanel.add(toSlider, new GridBagConstraints(1,
				1,
				1,
				1,
				1.0,
				0.0,
				GridBagConstraints.EAST,
				GridBagConstraints.HORIZONTAL,
				new Insets(0, 0, 0, 0), 0, 0));

		allAtOnce = new JCheckBox("All at once");
		allAtOnce.setSelected(true);
		bottomPanel.add(allAtOnce, new GridBagConstraints(0,
				2,
				2,
				1,
				0.0,
				0.0,
				GridBagConstraints.EAST,
				GridBagConstraints.HORIZONTAL,
				new Insets(0, 0, 0, 0), 0, 0));


		plotButton = new JButton("Plot");
		plotButton.addActionListener((actionEvent) -> {
//			System.out.println("Click event:" + actionEvent);
			List<LogAnalyzer.DatedPosition> toPlot = null;
			if (positions != null) {
				int indexFrom = (int)Math.floor((double)positions.size() * ((double)fromSlider.getValue() / (double)MAX_SLIDER));
				indexFrom = (indexFrom >= positions.size()) ? indexFrom - 1 : indexFrom;
				int indexTo = (int)Math.floor((double)positions.size() * ((double)toSlider.getValue() / (double)MAX_SLIDER));
				indexTo = (indexTo >= positions.size()) ? indexTo - 1 : indexTo;
				final int _from = indexFrom, _to = indexTo;
				toPlot = IntStream.range(0, positions.size())
						.filter(i -> (i >= _from && i <= _to))
						.mapToObj(i -> positions.get(i))
						.collect(Collectors.toList());
				this.plot(toPlot, !allAtOnce.isSelected());
			}
		});

		bottomPanel.add(plotButton, new GridBagConstraints(0,
				3,
				2,
				1,
				0.0,
				0.0,
				GridBagConstraints.CENTER,
				GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));

		this.add(bottomPanel, BorderLayout.SOUTH);

		this.pack();
	}

	private void setTooltipText(JSlider slider) {
		int value = slider.getValue();
		String tooltip = String.valueOf(value);
		if (positions != null) {
			int index = (int)Math.floor((double)positions.size() * ((double)value / (double)MAX_SLIDER));
			index = (index >= positions.size()) ? index - 1 : index;
//			System.out.println("Tooltip: " + index);
			tooltip = positions.get(index).getDate().toString();
		}
		slider.setToolTipText(tooltip);
	}

	private final String FROM = "from";
	private final String TO = "to";

	private void manageSliders(String origin) {
		int valueFrom = fromSlider.getValue();
		int valueTo = toSlider.getValue();
//		System.out.println(String.format("Origin: %s => From = %d, To = %d", origin, valueFrom, valueTo));
		if (origin.equals(FROM)) {
			if (valueFrom > valueTo) {
//				System.out.println("Increasing TO value");
				toSlider.setValue(valueFrom);
				toSlider.repaint();
			}
		} else if (origin.equals(TO)) {
			if (valueTo < valueFrom) {
//				System.out.println("Increasing FROM value");
				fromSlider.setValue(valueTo);
				fromSlider.repaint();
			}
		}
	}

	private Consumer<Object> plotCallback = (obj) -> {
		System.out.println("Button Callback!");
		plotButton.setEnabled(true);
		plotButton.repaint();
	};

	public void plot() {
		plot(this.positions);
	}
	public void plot(List<LogAnalyzer.DatedPosition> pos) {
		plot(pos, !allAtOnce.isSelected());
	}
	public void plot(List<LogAnalyzer.DatedPosition> pos, boolean progressing) {
		Consumer<Object> callback = null;
		if (progressing) {
			callback = plotCallback;
			// Disable/enable plot plotButton
			plotButton.setEnabled(false);
		}
		swingPanel.plot(pos, progressing, callback);
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
		SwingFrame frame = new SwingFrame(null);
		frame.setVisible(true);

		frame.plot(null);
	}
}
