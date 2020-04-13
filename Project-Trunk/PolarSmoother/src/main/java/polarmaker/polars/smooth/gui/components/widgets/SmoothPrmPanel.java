package polarmaker.polars.smooth.gui.components.widgets;

import polarmaker.smooth.PolarsResourceBundle;

import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.text.DecimalFormat;

public class SmoothPrmPanel extends JPanel {
	private GridBagLayout gridBagLayout1 = new GridBagLayout();
	private DecimalFormat df = new DecimalFormat("##0");
	private JLabel jLabel3 = new JLabel();
	private JFormattedTextField maxBspTextField = new JFormattedTextField(df);
	private JFormattedTextField minTWSTextField = new JFormattedTextField(df);
	private JFormattedTextField maxTWSTextField = new JFormattedTextField(df);
	private JLabel jLabel4 = new JLabel();
	private JLabel jLabel5 = new JLabel();

	public SmoothPrmPanel() {
		try {
			jbInit();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void jbInit() throws Exception {
		this.setLayout(gridBagLayout1);
		jLabel3.setText(PolarsResourceBundle.getPolarsResourceBundle().getString("max-boat-speed"));
		maxBspTextField.setPreferredSize(new Dimension(30, 20));
		maxBspTextField.setHorizontalAlignment(JTextField.RIGHT);
		maxBspTextField.setText("15");
		minTWSTextField.setPreferredSize(new Dimension(30, 20));
		minTWSTextField.setHorizontalAlignment(JTextField.RIGHT);
		minTWSTextField.setText("5");
		maxTWSTextField.setPreferredSize(new Dimension(30, 20));
		maxTWSTextField.setHorizontalAlignment(JTextField.RIGHT);
		maxTWSTextField.setText("25");
		jLabel4.setText(PolarsResourceBundle.getPolarsResourceBundle().getString("min-wind-speed"));
		jLabel5.setText(PolarsResourceBundle.getPolarsResourceBundle().getString("max-wind-speed"));
		this.add(jLabel3, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
		this.add(maxBspTextField, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
		this.add(minTWSTextField, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
		this.add(maxTWSTextField, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
		this.add(jLabel4, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
		this.add(jLabel5, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
	}

	public int getMaxBSP() throws Exception {
		return Integer.parseInt(maxBspTextField.getText());
	}

	public int getMinTWS() throws Exception {
		return Integer.parseInt(minTWSTextField.getText());
	}

	public int getMaxTWS() throws Exception {
		return Integer.parseInt(maxTWSTextField.getText());
	}

	public void setMaxBSP(int i) {
		maxBspTextField.setText(Integer.toString(i));
	}

	public void setMinTWS(int i) {
		minTWSTextField.setText(Integer.toString(i));
	}

	public void setMaxTWS(int i) {
		maxTWSTextField.setText(Integer.toString(i));
	}
}
