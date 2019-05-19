package poc.bulkpanel;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagLayout;
import java.text.DecimalFormat;

public class OneGRIBTablePanel
		extends JPanel {
	private BorderLayout borderLayout1 = new BorderLayout();
	private JPanel topPanel = new JPanel();
	private JPanel bottomPanel = new JPanel();
	private JPanel centerPane = new JPanel();

	private float min, max;

	private String[] names = null;
	private transient TableModel dataModel;
	private transient Object[][] data = new Object[0][0];

	private JTable table;
	private JScrollPane scrollPane;
	private BorderLayout borderLayout2 = new BorderLayout();
	private GridBagLayout gridBagLayout1 = new GridBagLayout();
	private JLabel titleLabel = new JLabel();

	public OneGRIBTablePanel() {
		try {
			jbInit();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void jbInit()
			throws Exception {
		this.setLayout(borderLayout1);
		this.setSize(new Dimension(302, 250));
		bottomPanel.setLayout(gridBagLayout1);
		centerPane.setLayout(borderLayout2);
		//  fileNameLabel.setText(" ");
		titleLabel.setText("Title...");
		titleLabel.setFont(new Font("Tahoma", 1, 11));
		topPanel.setLayout(new BorderLayout());
		//  topPanel.add(fileNameLabel, null);
		topPanel.add(titleLabel, BorderLayout.WEST);
		this.add(topPanel, BorderLayout.NORTH);
		this.add(bottomPanel, BorderLayout.SOUTH);
		this.add(centerPane, BorderLayout.CENTER);
	}

	private void initTable() {
		dataModel = new AbstractTableModel() {
			public int getColumnCount() {
				return names.length;
			}

			public int getRowCount() {
				return data == null ? 0 : data.length;
			}

			public Object getValueAt(int row, int col) {
				return data[row][col];
			}

			public String getColumnName(int column) {
				return names[column];
			}

			public Class getColumnClass(int c) {
//          System.out.println("Class requested column " + c + ", type:" + getValueAt(0, c).getClass());
				return getValueAt(0, c).getClass();
			}

			public boolean isCellEditable(int row, int col) {
				return false;
			}

			public void setValueAt(Object aValue, int row, int column) {
				data[row][column] = aValue;
			}
		};
		table = new JTable(dataModel) {
			/* For the tooltip text */
			public Component prepareRenderer(TableCellRenderer renderer,
			                                 int rowIndex, int vColIndex) {
				Component c = super.prepareRenderer(renderer, rowIndex, vColIndex);
				if (c instanceof JComponent) {
					JComponent jc = (JComponent) c;
					try {
						jc.setToolTipText("Row " + Integer.toString(rowIndex + 1) +
								", Col " + Integer.toString(vColIndex + 1));
					} catch (Exception ex) {
						System.err.println("TablePanel:" + ex.getMessage());
					}
				}
				return c;
			}
		};
		scrollPane = new JScrollPane(table);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		centerPane.add(scrollPane, BorderLayout.CENTER);

		for (int i = 0; i < table.getColumnCount(); i++) {
			TableColumn column = table.getColumn(table.getColumnName(i));
			column.setCellRenderer(new CustomTableCellRenderer());
		}
	}

	public void setData(Object[][] newData) {
		names = new String[newData[0].length];
		for (int i = 0; i < newData[0].length; i++)
			names[i] = "[" + Integer.toString(i + 1) + "]";
		data = newData;
		initTable();
		((AbstractTableModel) dataModel).fireTableDataChanged();
	}

	public void setText(String str) {
		titleLabel.setText(str);
	}

	public void setMin(float min) {
		this.min = min;
	}

	public float getMin() {
		return min;
	}

	public void setMax(float max) {
		this.max = max;
	}

	public float getMax() {
		return max;
	}

	public class CustomTableCellRenderer
			extends JLabel
			implements TableCellRenderer {
		Object curValue = null;

		private final DecimalFormat SMALL = new DecimalFormat("0.00000");
		private final DecimalFormat BIG = new DecimalFormat("######0.00");

		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			curValue = value;
			return this;
		}

		public void paintComponent(Graphics g) {
			if (curValue != null) {
				String s = "";
				if (curValue instanceof Number) {
					Number num = (Number) curValue;
					if (Math.abs(num.doubleValue()) < 1) {
						if (num.doubleValue() == 0d)
							s = "0";
						else
							s = SMALL.format(num.doubleValue());
					} else
						s = BIG.format(num.doubleValue());
				} else
					s = curValue.toString();
				g.drawString(s, 1, getHeight() - 1);
			}
		}
	}
}
