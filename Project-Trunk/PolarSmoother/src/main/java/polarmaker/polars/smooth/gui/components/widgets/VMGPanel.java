package polarmaker.polars.smooth.gui.components.widgets;

import polarmaker.polars.util.StaticUtil;
import polarmaker.smooth.PolarsResourceBundle;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.text.NumberFormat;

public class VMGPanel extends JPanel {
	private final static NumberFormat DF_2D = new DecimalFormat("##0.00");

	private GridBagLayout gridBagLayout1 = new GridBagLayout();
	private JLabel jLabel2 = new JLabel();
	private JLabel jLabel3 = new JLabel();
	private JLabel jLabel4 = new JLabel();
	private JLabel jLabel5 = new JLabel();
	private JLabel jLabel6 = new JLabel();
	private JLabel jLabel7 = new JLabel();
	private JLabel jLabel8 = new JLabel();
	private JLabel jLabel9 = new JLabel();
	private JTextField bspVmgUp = new JTextField();
	private JTextField twaVmgUp = new JTextField();
	private JTextField vmgUp = new JTextField();
	private JTextField bspVmgDown = new JTextField();
	private JTextField twaVmgDown = new JTextField();
	private JTextField vmgDown = new JTextField();

	public VMGPanel() {
		try {
			jbInit();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void jbInit() throws Exception {
		this.setLayout(gridBagLayout1);
		jLabel2.setText(PolarsResourceBundle.getPolarsResourceBundle().getString("upwind"));
		jLabel2.setFont(new Font("Tahoma", 3, 11));
		jLabel3.setText(PolarsResourceBundle.getPolarsResourceBundle().getString("bsp-at-best-vmg"));
		jLabel4.setText(PolarsResourceBundle.getPolarsResourceBundle().getString("twa-at-best-vmg"));
		jLabel5.setText(PolarsResourceBundle.getPolarsResourceBundle().getString("best-vmg"));
		jLabel6.setText(PolarsResourceBundle.getPolarsResourceBundle().getString("downwind"));
		jLabel6.setFont(new Font("Tahoma", 3, 11));
		jLabel7.setText(PolarsResourceBundle.getPolarsResourceBundle().getString("bsp-at-best-vmg"));
		jLabel8.setText(PolarsResourceBundle.getPolarsResourceBundle().getString("twa-at-best-vmg"));
		jLabel9.setText(PolarsResourceBundle.getPolarsResourceBundle().getString("best-vmg"));
		bspVmgUp.setPreferredSize(new Dimension(50, 20));
		bspVmgUp.setHorizontalAlignment(JTextField.RIGHT);
		twaVmgUp.setPreferredSize(new Dimension(50, 20));
		twaVmgUp.setHorizontalAlignment(JTextField.RIGHT);
		vmgUp.setPreferredSize(new Dimension(50, 20));
		vmgUp.setHorizontalAlignment(JTextField.RIGHT);
		bspVmgDown.setPreferredSize(new Dimension(50, 20));
		bspVmgDown.setHorizontalAlignment(JTextField.RIGHT);
		twaVmgDown.setPreferredSize(new Dimension(50, 20));
		twaVmgDown.setHorizontalAlignment(JTextField.RIGHT);
		vmgDown.setPreferredSize(new Dimension(50, 20));
		vmgDown.setHorizontalAlignment(JTextField.RIGHT);
		this.add(jLabel2,
				new GridBagConstraints(0, 0, 2, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
						new Insets(0, 0, 0, 0), 0, 0));
		this.add(jLabel3,
				new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
						new Insets(0, 0, 0, 0), 0, 0));
		this.add(jLabel4,
				new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
						new Insets(0, 0, 0, 0), 0, 0));
		this.add(jLabel5,
				new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
						new Insets(0, 0, 0, 0), 0, 0));
		this.add(jLabel6,
				new GridBagConstraints(0, 4, 2, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
						new Insets(0, 0, 0, 0), 0, 0));
		this.add(jLabel7,
				new GridBagConstraints(0, 5, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
						new Insets(0, 0, 0, 0), 0, 0));
		this.add(jLabel8,
				new GridBagConstraints(0, 6, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
						new Insets(0, 0, 0, 0), 0, 0));
		this.add(jLabel9,
				new GridBagConstraints(0, 7, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
						new Insets(0, 0, 0, 0), 0, 0));
		this.add(bspVmgUp,
				new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
						new Insets(0, 0, 0, 0), 0, 0));
		this.add(twaVmgUp,
				new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
						new Insets(0, 0, 0, 0), 0, 0));
		this.add(vmgUp,
				new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
						new Insets(0, 0, 0, 0), 0, 0));
		this.add(bspVmgDown,
				new GridBagConstraints(1, 5, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
						new Insets(0, 0, 0, 0), 0, 0));
		this.add(twaVmgDown,
				new GridBagConstraints(1, 6, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
						new Insets(0, 0, 0, 0), 0, 0));
		this.add(vmgDown,
				new GridBagConstraints(1, 7, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
						new Insets(0, 0, 0, 0), 0, 0));
		// Up
		bspVmgUp.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)  {// [Return] in the field
				setDataUp(FROM_BSP);
			}
		});
		bspVmgUp.getDocument().addDocumentListener(new DocumentListener() {
			public void insertUpdate(DocumentEvent e) {
				setDataUp(FROM_BSP);
			}

			public void removeUpdate(DocumentEvent e) {
				setDataUp(FROM_BSP);
			}

			public void changedUpdate(DocumentEvent e) {
				setDataUp(FROM_BSP);
			}
		});
		twaVmgUp.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)  {// [Return] in the field
				setDataUp(FROM_TWA);
			}
		});
		twaVmgUp.getDocument().addDocumentListener(new DocumentListener() {
			public void insertUpdate(DocumentEvent e) {
				setDataUp(FROM_TWA);
			}

			public void removeUpdate(DocumentEvent e) {
				setDataUp(FROM_TWA);
			}

			public void changedUpdate(DocumentEvent e) {
				setDataUp(FROM_TWA);
			}
		});
		vmgUp.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)  { // [Return] in the field
				setDataUp(FROM_VMG);
			}
		});
		vmgUp.getDocument().addDocumentListener(new DocumentListener() {
			public void insertUpdate(DocumentEvent e) {
				setDataUp(FROM_VMG);
			}

			public void removeUpdate(DocumentEvent e) {
				setDataUp(FROM_VMG);
			}

			public void changedUpdate(DocumentEvent e) {
				setDataUp(FROM_VMG);
			}
		});
		// Down
		bspVmgDown.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { // [Return] in the field
				setDataDown(FROM_BSP);
			}
		});
		bspVmgDown.getDocument().addDocumentListener(new DocumentListener() {
			public void insertUpdate(DocumentEvent e) {
				setDataDown(FROM_BSP);
			}

			public void removeUpdate(DocumentEvent e) {
				setDataDown(FROM_BSP);
			}

			public void changedUpdate(DocumentEvent e) {
				setDataDown(FROM_BSP);
			}
		});
		twaVmgDown.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { // [Return] in the field
				setDataDown(FROM_TWA);
			}
		});
		twaVmgDown.getDocument().addDocumentListener(new DocumentListener() {
			public void insertUpdate(DocumentEvent e) {
				setDataDown(FROM_TWA);
			}

			public void removeUpdate(DocumentEvent e) {
				setDataDown(FROM_TWA);
			}

			public void changedUpdate(DocumentEvent e) {
				setDataDown(FROM_TWA);
			}
		});
		vmgDown.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { // [Return] in the field
				setDataDown(FROM_VMG);
			}
		});
		vmgDown.getDocument().addDocumentListener(new DocumentListener() {
			public void insertUpdate(DocumentEvent e) {
				setDataDown(FROM_VMG);
			}

			public void removeUpdate(DocumentEvent e) {
				setDataDown(FROM_VMG);
			}

			public void changedUpdate(DocumentEvent e) {
				setDataDown(FROM_VMG);
			}
		});
	}

	private final static int FROM_BSP = 0;
	private final static int FROM_TWA = 1;
	private final static int FROM_VMG = 2;

	private void setDataUp(int option) {
		if (option == FROM_BSP) {
			if (twaVmgUp.getText().length() > 0 && bspVmgUp.getText().length() > 0) {
				double twa = Double.parseDouble(twaVmgUp.getText());
				double bsp = Double.parseDouble(bspVmgUp.getText());
				double vmg = bsp * Math.cos(Math.toRadians(twa));
				try {
					vmgUp.setText(DF_2D.format(vmg));
				} catch (IllegalStateException ex) {
				}
			}
		} else if (option == FROM_TWA) {
			if (bspVmgUp.getText().length() > 0 && twaVmgUp.getText().length() > 0) {
				double twa = Double.parseDouble(twaVmgUp.getText());
				double bsp = Double.parseDouble(bspVmgUp.getText());
				double vmg = bsp * Math.cos(Math.toRadians(twa));
				try {
					vmgUp.setText(DF_2D.format(vmg));
				} catch (IllegalStateException ex) {
				}
			}
		} else if (option == FROM_VMG) {
			if (twaVmgUp.getText().length() > 0 && vmgUp.getText().length() > 0) {
				double twa = Double.parseDouble(twaVmgUp.getText());
				double vmg = Double.parseDouble(vmgUp.getText());
				double bsp = vmg / Math.cos(Math.toRadians(twa));
				try {
					bspVmgUp.setText(DF_2D.format(bsp));
				} catch (IllegalStateException ex) {
				}
			}
		}
	}

	private void setDataDown(int option) {
		if (option == FROM_BSP) {
			if (twaVmgDown.getText().length() > 0 && bspVmgDown.getText().length() > 0) {
				double twa = Double.parseDouble(twaVmgDown.getText());
				double bsp = Double.parseDouble(bspVmgDown.getText());
				double vmg = -bsp * Math.cos(Math.toRadians(twa));
				try {
					vmgDown.setText(DF_2D.format(vmg));
				} catch (IllegalStateException ex) {
				}
			}
		} else if (option == FROM_TWA) {
			if (bspVmgDown.getText().length() > 0 && twaVmgDown.getText().length() > 0) {
				double twa = Double.parseDouble(twaVmgDown.getText());
				double bsp = Double.parseDouble(bspVmgDown.getText());
				double vmg = -bsp * Math.cos(Math.toRadians(twa));
				try {
					vmgDown.setText(DF_2D.format(vmg));
				} catch (IllegalStateException ex) {
				}
			}
		} else if (option == FROM_VMG) {
			if (twaVmgDown.getText().length() > 0 && vmgDown.getText().length() > 0) {
				double twa = Double.parseDouble(twaVmgDown.getText());
				double vmg = Double.parseDouble(vmgDown.getText());
				double bsp = -vmg / Math.cos(Math.toRadians(twa));
				try {
					bspVmgDown.setText(DF_2D.format(bsp));
				} catch (IllegalStateException ex) {
				}
			}
		}
	}

	public void setBspVmgUp(double d) {
		bspVmgUp.setText(d == Double.MIN_VALUE ? "" : Double.toString(d));
	}

	public void setTwaVmgUp(double d) {
		twaVmgUp.setText(d == Double.MIN_VALUE ? "" : Double.toString(d));
	}

	public void setVmgUp(double d) {
		vmgUp.setText(d == Double.MIN_VALUE ? "" : Double.toString(d));
	}

	public void setBspVmgDown(double d) {
		bspVmgDown.setText(d == Double.MIN_VALUE ? "" : Double.toString(d));
	}

	public void setTwaVmgDown(double d) {
		twaVmgDown.setText(d == Double.MIN_VALUE ? "" : Double.toString(d));
	}

	public void setVmgDown(double d) {
		vmgDown.setText(d == Double.MIN_VALUE ? "" : Double.toString(d));
	}

	public double getBspVmgUp() throws Exception {
		return bspVmgUp.getText().equals("") ? Double.MIN_VALUE : StaticUtil.parseDouble(bspVmgUp.getText());
	}

	public double getTwaVmgUp() throws Exception {
		return twaVmgUp.getText().equals("") ? Double.MIN_VALUE : StaticUtil.parseDouble(twaVmgUp.getText());
	}

	public double getVmgUp() throws Exception {
		return vmgUp.getText().equals("") ? Double.MIN_VALUE : StaticUtil.parseDouble(vmgUp.getText());
	}

	public double getBspVmgDown() throws Exception {
		return bspVmgDown.getText().equals("") ? Double.MIN_VALUE : StaticUtil.parseDouble(bspVmgDown.getText());
	}

	public double getTwaVmgDown() throws Exception {
		return twaVmgDown.getText().equals("") ? Double.MIN_VALUE : StaticUtil.parseDouble(twaVmgDown.getText());
	}

	public double getVmgDown() throws Exception {
		return vmgDown.getText().equals("") ? Double.MIN_VALUE : StaticUtil.parseDouble(vmgDown.getText());
	}
}
