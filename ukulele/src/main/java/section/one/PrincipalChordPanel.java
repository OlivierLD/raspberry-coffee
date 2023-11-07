package section.one;

import chords.ChordList;
import chords.ChordUtil;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.EventObject;
import java.util.List;
import javax.swing.AbstractCellEditor;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import ukulele.Chord;
import ukulele.ChordPanel;


public class PrincipalChordPanel
		extends JPanel
		implements PanelWithContent {
	private static final String[] colName = {"I", "IV", "V7"};

	private static final String[][] chordData = {
					{"C", "F", "G7"},
					{"G", "C", "D7"},
					{"D", "G", "A7"},
					{"A", "D", "E7"},
					{"F", "Bb", "C7"},
					{"Bb", "Eb", "F7"}
	};

	private static final int NB_COLUMNS = colName.length;
	private static final int MAX_ROW = chordData.length;

	private transient Object[][] data = new Object[0][NB_COLUMNS];

	private transient TableModel dataModel;

	private JTable table;
	private JScrollPane scrollPane = null;

	public PrincipalChordPanel() {
		try {
			jbInit();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public JTable getJTable() {
		return this.table;
	}
	private void jbInit()
					throws Exception {
		initTable();

		this.data = new Chord[MAX_ROW][NB_COLUMNS];

		for (int row = 0; row < MAX_ROW; row++) {
			for (int col = 0; col < NB_COLUMNS; col++) {
				this.data[row][col] = null;
			}
		}
		for (int row = 0; row < chordData.length; row++) {
			for (int column = 0; column < chordData[row].length; column++) {
				String key = chordData[row][column];

				Chord chord = (Chord) ChordList.findChord(key).get(0);
				this.data[row][column] = chord;
			}
		}
		setData(this.data);
	}


	private void initTable() {
		this.dataModel = new AbstractTableModel() {

			public int getColumnCount() {
				return PrincipalChordPanel.NB_COLUMNS;
			}

			public int getRowCount() {
				return PrincipalChordPanel.this.data.length;
			}

			public Object getValueAt(int row, int col) {
				return PrincipalChordPanel.this.data[row][col];
			}

			public String getColumnName(int column) {
				return PrincipalChordPanel.colName[column];
			}

			public Class getColumnClass(int c) {
				return Chord.class;
			}

			public boolean isCellEditable(int row, int col) {
				return true;
			}

			public void setValueAt(Object aValue, int row, int column) {
				PrincipalChordPanel.this.data[row][column] = aValue;
				fireTableCellUpdated(row, column);
			}

		};
		this.table = new JTable(this.dataModel);
		this.scrollPane = new JScrollPane(this.table);
		this.scrollPane.setPreferredSize(new Dimension(120, 280));
		setLayout(new BorderLayout());
		add(this.scrollPane, BorderLayout.CENTER);


		for (int i = 0; i < NB_COLUMNS; i++) {
			TableColumn tc = this.table.getColumnModel().getColumn(i);
			tc.setCellRenderer(new ChordTableCellRenderer());
			tc.setCellEditor(new ChordTableCellEditor());
		}
		this.table.setSelectionMode(0);
		this.table.setAutoResizeMode(0);

		this.table.setRowHeight(150);
	}

	private void setData(Object[][] data) {
		this.data = data;
		((AbstractTableModel) this.dataModel).fireTableDataChanged();
	}


	public class ChordTableCellRenderer
					extends ChordPanel
					implements TableCellRenderer {
		public ChordTableCellRenderer() {
		}


		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			if (value != null) {
				setChord((Chord) value);
				super.setToolTipText(getChord().toString());
			} else {
				setChord(null);
				super.setToolTipText("0 0 0 0");
			}
			return this;
		}

		public void paintComponent(Graphics g) {
			super.paintComponent(g);
		}
	}

	public class ChordTableCellEditor extends AbstractCellEditor implements TableCellEditor {
		public ChordTableCellEditor() {
		}

		public boolean isCellEditable(EventObject evt) {
			return true;
		}

		public Object getCellEditorValue() {
			return null;
		}

		public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
			if ((value != null) && ((value instanceof Chord)))
				ChordUtil.playChord((Chord) value);
			if (value == null)
				ChordUtil.playChord(new Chord("", new int[]{0, 0, 0, 0}));
			return null;
		}
	}
}
