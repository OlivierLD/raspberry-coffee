package poc.bulkpanel;

import calc.GeomUtil;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

public class GribDatePanel
		extends JPanel {
	private BorderLayout borderLayout1 = new BorderLayout();
	private JTabbedPane dateTabbedPane = new JTabbedPane();
	private JPanel topPanel = new JPanel();
	private GridBagLayout gridBagLayout1 = new GridBagLayout();
	private JLabel widthLabel = new JLabel();
	private JLabel widthValueLabel = new JLabel();
	private JLabel heightLabel = new JLabel();
	private JLabel heightValueLabel = new JLabel();
	private JLabel stepXLabel = new JLabel();
	private JLabel stepXValueLabel = new JLabel();
	private JLabel stepYLabel = new JLabel();
	private JLabel stepYValueLabel = new JLabel();
	private JLabel topLabel = new JLabel();
	private JLabel jLabel9 = new JLabel();
	private JLabel leftLabel = new JLabel();
	private JLabel bottomLabel = new JLabel();
	private JLabel rightLabel = new JLabel();
	private JLabel topValueLabel = new JLabel();
	private JLabel leftValueLabel = new JLabel();
	private JLabel bottomValueLabel = new JLabel();
	private JLabel rightValueLabel = new JLabel();

	public GribDatePanel() {
		try {
			jbInit();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void jbInit()
			throws Exception {
		this.setLayout(borderLayout1);
		dateTabbedPane.setTabPlacement(JTabbedPane.BOTTOM);
		topPanel.setLayout(gridBagLayout1);
		widthLabel.setText("width:");
		widthValueLabel.setText("XXX");
		heightLabel.setText("height:");
		heightValueLabel.setText("XXX");
		stepXLabel.setText("stepX:");
		stepXValueLabel.setText("XXX");
		stepYLabel.setText("stepY:");
		stepYValueLabel.setText("XXX");
		topLabel.setText("top:");
		jLabel9.setText("jLabel9");
		leftLabel.setText("left:");
		bottomLabel.setText("bottom:");
		rightLabel.setText("right:");
		topValueLabel.setText("XX XX.XX X");
		leftValueLabel.setText("XX XX.XX X");
		bottomValueLabel.setText("XX XX.XX X");
		rightValueLabel.setText("XX XX.XX X");
		this.add(dateTabbedPane, BorderLayout.CENTER);
		topPanel.add(widthLabel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
		topPanel.add(widthValueLabel, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
		topPanel.add(heightLabel, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
				new Insets(0, 10, 0, 0), 0, 0));
		topPanel.add(heightValueLabel, new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
		topPanel.add(stepXLabel, new GridBagConstraints(4, 0, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
				new Insets(0, 10, 0, 0), 0, 0));
		topPanel.add(stepXValueLabel, new GridBagConstraints(5, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
		topPanel.add(stepYLabel, new GridBagConstraints(6, 0, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
				new Insets(0, 10, 0, 0), 0, 0));
		topPanel.add(stepYValueLabel, new GridBagConstraints(7, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
		topPanel.add(topLabel, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
		topPanel.add(leftLabel, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
		topPanel.add(bottomLabel, new GridBagConstraints(4, 1, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
				new Insets(0, 10, 0, 0), 0, 0));
		topPanel.add(rightLabel, new GridBagConstraints(4, 2, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
				new Insets(0, 10, 0, 0), 0, 0));
		topPanel.add(topValueLabel, new GridBagConstraints(1, 1, 3, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
		topPanel.add(leftValueLabel, new GridBagConstraints(1, 2, 3, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
		topPanel.add(bottomValueLabel, new GridBagConstraints(5, 1, 3, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
		topPanel.add(rightValueLabel, new GridBagConstraints(5, 2, 3, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
		this.add(topPanel, BorderLayout.NORTH);
	}

	public JTabbedPane getDateTabbedPane() {
		return dateTabbedPane;
	}

	public void setWidth(int width) {
		widthValueLabel.setText(Integer.toString(width));
	}

	public void setHeight(int height) {
		heightValueLabel.setText(Integer.toString(height));
	}

	public void setStepX(double d) {
		stepXValueLabel.setText(Double.toString(d));
	}

	public void setStepY(double d) {
		stepYValueLabel.setText(Double.toString(d));
	}

	public void setTop(double d) {
		topValueLabel.setText(GeomUtil.decToSex(d, GeomUtil.SWING, GeomUtil.NS));
	}

	public void setBottom(double d) {
		bottomValueLabel.setText(GeomUtil.decToSex(d, GeomUtil.SWING, GeomUtil.NS));
	}

	public void setLeft(double d) {
		leftValueLabel.setText(GeomUtil.decToSex(d, GeomUtil.SWING, GeomUtil.EW));
	}

	public void setRight(double d) {
		rightValueLabel.setText(GeomUtil.decToSex(d, GeomUtil.SWING, GeomUtil.EW));
	}
}
