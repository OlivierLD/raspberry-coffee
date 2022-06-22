package polarmaker.polars.smooth.gui.components.widgets;

import polarmaker.smooth.PolarsResourceBundle;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

public class EditSectionPanel extends JPanel {
	private GridBagLayout gridBagLayout1 = new GridBagLayout();
	private JLabel jLabel1 = new JLabel();
	private JTextField nameTextField = new JTextField();
	private JLabel jLabel2 = new JLabel();
	private JLabel jLabel3 = new JLabel();
	private JLabel jLabel4 = new JLabel();
	private JLabel jLabel5 = new JLabel();
	private transient SpinnerModel pDegreeModel = new SpinnerNumberModel(3, 1, Integer.MAX_VALUE, 1);
	private transient SpinnerModel cDegreeModel = new SpinnerNumberModel(3, 1, Integer.MAX_VALUE, 1);
	private transient SpinnerModel fTwaModel = new SpinnerNumberModel(5, 0, 180, 1);
	private transient SpinnerModel tTwaModel = new SpinnerNumberModel(5, 0, 180, 1);

	private JSpinner polarDegreeSpinner = new JSpinner(pDegreeModel);
	private JSpinner coeffDegreeSpinner = new JSpinner(cDegreeModel);
	private JSpinner fromTWASpinner = new JSpinner(fTwaModel);
	private JSpinner toTWASpinner = new JSpinner(tTwaModel);

	public EditSectionPanel() {
		try {
			jbInit();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void jbInit() throws Exception {
		this.setLayout(gridBagLayout1);
		jLabel1.setText(PolarsResourceBundle.getPolarsResourceBundle().getString("name"));
		nameTextField.setPreferredSize(new Dimension(200, 20));
		jLabel2.setText("PolarDegree:");
		jLabel3.setText("Coeff Degree:");
		jLabel4.setText("From TWA:");
		jLabel5.setText("To TWA:");
		polarDegreeSpinner.setMinimumSize(new Dimension(50, 19));
		polarDegreeSpinner.setPreferredSize(new Dimension(50, 19));
		coeffDegreeSpinner.setMinimumSize(new Dimension(50, 19));
		coeffDegreeSpinner.setPreferredSize(new Dimension(50, 19));
		fromTWASpinner.setMinimumSize(new Dimension(50, 19));
		fromTWASpinner.setPreferredSize(new Dimension(50, 19));
		toTWASpinner.setMinimumSize(new Dimension(50, 19));
		toTWASpinner.setPreferredSize(new Dimension(50, 19));
		this.add(jLabel1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
		this.add(nameTextField, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		this.add(jLabel2, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
		this.add(jLabel3, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
		this.add(jLabel4, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
		this.add(jLabel5, new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
		this.add(polarDegreeSpinner, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
		this.add(coeffDegreeSpinner, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
		this.add(fromTWASpinner, new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
		this.add(toTWASpinner, new GridBagConstraints(1, 4, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
	}

	public void setName(String s) {
		nameTextField.setText(s);
	}

	public String getName() {
		return nameTextField.getText();
	}

	public void setPolarDegree(int d) {
		polarDegreeSpinner.setValue(d);
		polarDegreeSpinner.repaint();
	}

	public void setCoeffDegree(int d) {
		coeffDegreeSpinner.setValue(d);
		coeffDegreeSpinner.repaint();
	}

	public void setFromTWA(int d) {
		fromTWASpinner.setValue(d);
		fromTWASpinner.repaint();
	}

	public void setToTWA(int d) {
		toTWASpinner.setValue(d);
		toTWASpinner.repaint();
	}

	public int getPolarDegree() {
		return ((Integer) polarDegreeSpinner.getValue()).intValue();
	}

	public int getCoeffDegree() {
		return ((Integer) coeffDegreeSpinner.getValue()).intValue();
	}

	public int getFromTWA() {
		return ((Integer) fromTWASpinner.getValue()).intValue();
	}

	public int getToTWA() {
		return ((Integer) toTWASpinner.getValue()).intValue();
	}
}
