package polarmaker.polars.smooth.gui.components.widgets;

import polarmaker.polars.util.StaticUtil;
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

public class EditTWAPanel extends JPanel {
	private GridBagLayout gridBagLayout1 = new GridBagLayout();
	private JLabel jLabel1 = new JLabel();
	private JLabel jLabel2 = new JLabel();
	private JFormattedTextField twaTextField = new JFormattedTextField(new DecimalFormat("##0"));
	private JFormattedTextField bspTextField = new JFormattedTextField(new DecimalFormat("#0.##"));

	public EditTWAPanel() {
		try {
			jbInit();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void jbInit() throws Exception {
		this.setLayout(gridBagLayout1);
		jLabel1.setText(PolarsResourceBundle.getPolarsResourceBundle().getString("true-wind-angle"));
		jLabel2.setText(PolarsResourceBundle.getPolarsResourceBundle().getString("boat-speed"));
		twaTextField.setText("0");
		twaTextField.setPreferredSize(new Dimension(40, 20));
		twaTextField.setHorizontalAlignment(JTextField.RIGHT);
		bspTextField.setText("0.0");
		bspTextField.setPreferredSize(new Dimension(40, 20));
		bspTextField.setHorizontalAlignment(JTextField.RIGHT);
		this.add(jLabel1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		this.add(jLabel2, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		this.add(twaTextField, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		this.add(bspTextField, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
	}

	public void setBsp(double d) {
		bspTextField.setText(Double.toString(d));
	}

	public void setTwa(int i) {
		twaTextField.setText(Integer.toString(i));
	}

	public double getBsp() throws Exception {
		return StaticUtil.parseDouble(bspTextField.getText());
	}

	public int getTwa() throws Exception {
		return Integer.parseInt(twaTextField.getText());
	}
}
