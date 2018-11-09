package polarmaker.polars.smooth.gui.components.widgets;

import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

public class ExtrapolationPanel
		extends JPanel {
	private GridBagLayout gridBagLayout1 = new GridBagLayout();
	private JRadioButton divideRadioButton = new JRadioButton();
	private JRadioButton multiplyRadioButton = new JRadioButton();
	private JLabel jLabel1 = new JLabel();
	private JTextField factorTextField = new JTextField();

	private ButtonGroup group = new ButtonGroup();

	public ExtrapolationPanel() {
		try {
			jbInit();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void jbInit()
			throws Exception {
		this.setLayout(gridBagLayout1);
		divideRadioButton.setText("Divide");
		multiplyRadioButton.setText("Multiply");
		multiplyRadioButton.setSelected(true);
		group.add(divideRadioButton);
		group.add(multiplyRadioButton);

		jLabel1.setText("by");
		factorTextField.setPreferredSize(new Dimension(60, 20));
		factorTextField.setMinimumSize(new Dimension(60, 20));
		factorTextField.setText("1.1");
		factorTextField.setHorizontalAlignment(JTextField.CENTER);
		this.add(divideRadioButton,
				new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
						new Insets(0, 0, 0, 0), 0, 0));
		this.add(multiplyRadioButton,
				new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
						new Insets(0, 0, 0, 0), 0, 0));
		this.add(jLabel1, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
		this.add(factorTextField, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
	}

	public double getFactor() {
		double val = 1.1;
		try {
			val = Double.parseDouble(factorTextField.getText());
			if (divideRadioButton.isSelected())
				val = 1D / val;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return val;
	}
}
