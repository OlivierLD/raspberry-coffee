package polarmaker.polars.smooth.gui.components.widgets;

import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.text.DecimalFormat;

public class TWSPanel
		extends JPanel {
	private BorderLayout borderLayout1 = new BorderLayout();
	private JPanel topPanel = new JPanel();
	private JLabel jLabel1 = new JLabel();
	private JTabbedPane tabbedPane = new JTabbedPane();

	private TWATable twaTable = new TWATable();
	private VMGPanel vmgPanel = new VMGPanel();
	private JFormattedTextField twsField =
			new JFormattedTextField(new DecimalFormat("#0.##"));
	private GridBagLayout gridBagLayout1 = new GridBagLayout();

	public TWSPanel() {
		try {
			jbInit();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void jbInit()
			throws Exception {
		this.setLayout(borderLayout1);
		topPanel.setLayout(gridBagLayout1);
		jLabel1.setText("TWS: ");
		twsField.setPreferredSize(new Dimension(50, 20));
		twsField.setHorizontalAlignment(JTextField.TRAILING);
		this.add(topPanel, BorderLayout.NORTH);
		this.add(tabbedPane, BorderLayout.CENTER);
		tabbedPane.add("TWA", twaTable);
		tabbedPane.add("VMG", vmgPanel);
		topPanel.add(jLabel1,
				new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
						new Insets(0, 0, 0, 0), 0, 6));
		topPanel.add(twsField,
				new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
						new Insets(0, 0, 0, 0), 0, 0));
	}

	public Object[][] getTwaData() {
		return twaTable.getData();
	}

	public void setTwaData(Object[][] o) {
		twaTable.setData(o);
	}

	public void setTWS(double d) {
		twsField.setText(Double.toString(d));
	}

	public double getTWS() throws Exception {
		return Double.parseDouble(twsField.getText());
	}

	public void setVmgUp(double d) {
		vmgPanel.setVmgUp(d);
	}

	public double getVmgUp() throws Exception {
		return vmgPanel.getVmgUp();
	}

	public void setVmgDown(double d) {
		vmgPanel.setVmgDown(d);
	}

	public double getVmgDown() throws Exception {
		return vmgPanel.getVmgDown();
	}

	public void setBspVmgUp(double d) {
		vmgPanel.setBspVmgUp(d);
	}

	public double getBspVmgUp() throws Exception {
		return vmgPanel.getBspVmgUp();
	}

	public void setBspVmgDown(double d) {
		vmgPanel.setBspVmgDown(d);
	}

	public double getBspVmgDown() throws Exception {
		return vmgPanel.getBspVmgDown();
	}

	public void setTwaVmgUp(double d) {
		vmgPanel.setTwaVmgUp(d);
	}

	public double getTwaVmgUp() throws Exception {
		return vmgPanel.getTwaVmgUp();
	}

	public void setTwaVmgDown(double d) {
		vmgPanel.setTwaVmgDown(d);
	}

	public double getTwaVmgDown() throws Exception {
		return vmgPanel.getTwaVmgDown();
	}

	public void setTWSTopLabel(String s) {
		twaTable.setTopLabel(s);
	}
}
