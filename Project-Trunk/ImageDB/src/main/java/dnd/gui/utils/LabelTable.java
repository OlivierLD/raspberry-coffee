package dnd.gui.utils;

import dnd.gui.ctx.AppContext;
import dnd.gui.ctx.ImageAppListener;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LabelTable
		extends JPanel {

	private transient List<LabelDefinition> labelData;
	private final LabelTable instance = this;

	private static final String LABEL_NAME = "Tags";
	private static final String LABEL_NB_OCC = "Used";

	private static final String[] names = {LABEL_NAME, LABEL_NB_OCC};

	private transient Object[][] data = new Object[0][names.length];

	private transient TableModel dataModel;

	private JTable table;
	private final BorderLayout borderLayout1 = new BorderLayout();
	private final JPanel centerPanel = new JPanel();

	private final BorderLayout borderLayout2 = new BorderLayout();
	private JScrollPane centerScrollPane = null;

	private final JProgressBar progressBar = new JProgressBar();
	private final DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();

	public LabelTable(List<LabelDefinition> labelData) {
		rightRenderer.setHorizontalAlignment(JLabel.RIGHT);
		try {
			jbInit();
			if (labelData != null) {
				setLabelData(labelData);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void setValues() {
		try {
			this.data = new Object[0][names.length];
			if (this.labelData != null) {
				for (LabelDefinition id : this.labelData) {
					addLineInTable(id.getLabel(), id.getOccurences());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		((AbstractTableModel) this.dataModel).fireTableDataChanged();
		this.table.repaint();
	}

	private void jbInit() throws Exception {
		setLayout(this.borderLayout1);
		this.centerPanel.setLayout(this.borderLayout2);
		add(this.centerPanel, BorderLayout.CENTER);
		this.progressBar.setPreferredSize(new Dimension(200, 17));
		this.progressBar.setEnabled(false);
		initTable();
	}

	private void initTable() {
		this.dataModel = new AbstractTableModel() {
			public int getColumnCount() {
				return LabelTable.names.length;
			}
			public int getRowCount() {
				return data.length;
			}
			public Object getValueAt(int row, int col) {
				return data[row][col];
			}
			public String getColumnName(int column) {
				return LabelTable.names[column];
			}
			public Class getColumnClass(int c) {
				return getValueAt(0, c).getClass();
			}
			public boolean isCellEditable(int row, int col) {
				return col == 5;
			}
			public void setValueAt(Object aValue, int row, int column) {
				try {
					data[row][column] = aValue;
					fireTableCellUpdated(row, column);
				} catch (Exception ex) {
					JOptionPane.showMessageDialog(instance, ex.toString() + "\nrow:" + row + ", column:" + column, "setValueAt", JOptionPane.ERROR_MESSAGE);
				}
			}
		};
		this.table = new JTable(this.dataModel);

		this.table.getColumn(LABEL_NAME).setPreferredWidth(350);
		this.table.getColumn(LABEL_NB_OCC).setPreferredWidth(100);
		this.table.getColumn(LABEL_NB_OCC).setCellRenderer(rightRenderer);

		this.table.setAutoResizeMode(0);
		this.table.setSelectionMode(0);

		this.centerScrollPane = new JScrollPane(this.table);
		this.centerPanel.add(this.centerScrollPane, BorderLayout.CENTER);
	}

	private void addLineInTable(String tag, int nb) {
		int len = this.data.length;
		Object[][] newData = new Object[len + 1][names.length];
		for (int i = 0; i < len; i++) {
			for (int j = 0; j < names.length; j++) {
				newData[i][j] = this.data[i][j];
			}
		}
		newData[len][0] = tag;
		newData[len][1] = nb;
		this.data = newData;
		this.table.repaint();
	}

	public void setLabelData(List<LabelDefinition> labelData) {
		this.labelData = labelData;
		setValues();
	}

	public List<LabelDefinition> getLabelData() {
		return this.labelData;
	}
}
