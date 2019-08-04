package polarmaker.polars.smooth.gui.components.widgets;

import polarmaker.smooth.PolarsResourceBundle;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

public class EditRootPanel extends JPanel {
	private GridBagLayout gridBagLayout1 = new GridBagLayout();
	private JLabel jLabel1 = new JLabel();
	private JTextField name = new JTextField();

	public EditRootPanel() {
		try {
			jbInit();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void jbInit() throws Exception {
		this.setLayout(gridBagLayout1);
		jLabel1.setText(PolarsResourceBundle.getPolarsResourceBundle().getString("name"));
		name.setPreferredSize(new Dimension(200, 20));
		this.add(jLabel1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		this.add(name, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
	}

	public void setName(String s) {
		name.setText(s);
	}

	public String getName() {
		return name.getText();
	}
}
