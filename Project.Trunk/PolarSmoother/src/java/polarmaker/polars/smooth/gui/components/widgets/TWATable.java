package polarmaker.polars.smooth.gui.components.widgets;

import polarmaker.smooth.PolarsResourceBundle;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class TWATable extends JPanel {
	// Table Columns
	final static String TWA = "TWA";
	final static String BSP = "BSP";

	final String[] names = {TWA, BSP};
	// Table content
	Object[][] data = new Object[0][2];
	TableModel dataModel;
	JTable table;

	private BorderLayout borderLayout1 = new BorderLayout();
	private JScrollPane scrollPane = null;

	private JPanel tablePane = new JPanel();
	private JPanel bottomPanel = new JPanel();
	private JButton addButton = new JButton();
	private JButton delButton = new JButton();
	private JLabel topLabel = new JLabel("...");

	public TWATable() {
		try {
			jbInit();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void setTopLabel(String s) {
		topLabel.setText(s);
	}

	public Object[][] getData() {
		return data;
	}

	public void setData(Object[][] o) {
		data = o;
		((AbstractTableModel) dataModel).fireTableDataChanged();
	}

	private void jbInit() throws Exception {
		this.setLayout(borderLayout1);
		this.setPreferredSize(new Dimension(235, 200));
		addButton.setPreferredSize(new Dimension(100, 22));
		addButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				addButton_actionPerformed(e);
			}
		});
		delButton.setPreferredSize(new Dimension(100, 22));
		delButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				delButton_actionPerformed(e);
			}
		});
		delButton.setEnabled(false);
		this.add(topLabel, BorderLayout.NORTH);
		this.add(tablePane, BorderLayout.CENTER);
		addButton.setText(PolarsResourceBundle.getPolarsResourceBundle().getString("add-line"));
		delButton.setText(PolarsResourceBundle.getPolarsResourceBundle().getString("del-line"));
		bottomPanel.add(addButton, null);
		bottomPanel.add(delButton, null);
		this.add(bottomPanel, BorderLayout.SOUTH);
		initTable();
	}

	private void initTable() {
		// Init Table
		dataModel = new AbstractTableModel() {
			public int getColumnCount() {
				return names.length;
			}

			public int getRowCount() {
				return data.length;
			}

			public Object getValueAt(int row, int col) {
				return data[row][col];
			}

			public String getColumnName(int column) {
				return names[column];
			}

			public Class getColumnClass(int c) {
				if (c == 0) { // TWA
					return Integer.class;
				} else {      // BSP
					return Double.class;
				}
			}

			public boolean isCellEditable(int row, int col) {
				return true; // All editable
			}

			public void setValueAt(Object aValue, int row, int column) {
				data[row][column] = aValue;
				fireTableCellUpdated(row, column);
			}
		};
		// Create JTable
		table = new JTable(dataModel);
		scrollPane = new JScrollPane(table);
		scrollPane.setPreferredSize(new Dimension(120, 280));
		tablePane.setLayout(new BorderLayout());
		tablePane.add(scrollPane, BorderLayout.CENTER);
		SelectionListener listener = new SelectionListener(table);
		table.getSelectionModel().addListSelectionListener(listener);
		table.getColumnModel().getSelectionModel().addListSelectionListener(listener);
	}

	private void addLineInTable(Integer a,
	                            Double b) {
		int len = data.length;
		Object[][] newData = new Object[len + 1][names.length];
		for (int i = 0; i < len; i++) {
			for (int j = 0; j < names.length; j++) {
				newData[i][j] = data[i][j];
			}
		}
		newData[len][0] = a;
		newData[len][1] = b;
		data = newData;
		((AbstractTableModel) dataModel).fireTableDataChanged();
	}

	private void removeCurrentLine() {
		int selectedRow = table.getSelectedRow();
		if (selectedRow < 0) {
			JOptionPane.showMessageDialog(null, PolarsResourceBundle.getPolarsResourceBundle().getString("choose-to-remove"), PolarsResourceBundle.getPolarsResourceBundle().getString("removing"),
					JOptionPane.WARNING_MESSAGE);
		} else {
			int l = data.length;
			Object[][] newData = new Object[l - 1][names.length];
			int oldInd, newInd;
			newInd = 0;
			for (oldInd = 0; oldInd < l; oldInd++) {
				if (oldInd != selectedRow) {
					for (int j = 0; j < names.length; j++) {
						newData[newInd][j] = data[oldInd][j];
					}
					newInd++;
				}
			}
			data = newData;
			((AbstractTableModel) dataModel).fireTableDataChanged();
		}
	}

	private void addButton_actionPerformed(ActionEvent e) {
		addLineInTable(new Integer(0), new Double(0.0));
	}

	private void delButton_actionPerformed(ActionEvent e) {
		removeCurrentLine();
	}

	public class SelectionListener implements ListSelectionListener {
		JTable table;

		SelectionListener(JTable table) {
			this.table = table;
		}

		public void valueChanged(ListSelectionEvent e) {
			int selectedRow = table.getSelectedRow();
			if (selectedRow < 0) {
				delButton.setEnabled(false);
			} else {
				delButton.setEnabled(true);
			}
		}
	}
}
