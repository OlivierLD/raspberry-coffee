package section.one;

import chords.ChordList;
import chords.ChordUtil;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.io.PrintStream;
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


public class TonalRegionChordPanel
		extends JPanel
		implements PanelWithContent {
	private static final String[] colName = {" <- 5th - ", " <- 5th - ", " ", " - 5th -> ", " - 5th -> "};

	private static final String[][] chordData = {
					{"Db Gb Ab7", "Ab Db Eb7", "Eb Ab Bb7", "Bb Eb F7", "F Bb C"},
					{"Bbm Ebm Fm7", "Fm Bbm Cm7", "Cm Fm Gm7", "Gm Cm Dm7", "Dm Gm Fm7"},
					{"Bb Eb F7", "F Bb C7", "C F G7", "G C D7", "D G F7"},
					{"Gm Cm Dm7", "Dm Gm Am7", "Am Dm Em7", "Em Am Dm7", "Bm Em F#m7"},
					{"G C F7", "D G A7", "A D E7", "E A B7", "B E F#"}
	};

	private static final int NB_COLUMNS = colName.length;

	private transient Object[][] data = chordData;

	private transient TableModel dataModel;
	private JTable table;
	private JScrollPane scrollPane = null;

	public TonalRegionChordPanel() {
		try {
			jbInit();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public JTable getJTable() {
		return this.table;
	}
	private void jbInit()
					throws Exception {
		initTable();
		setData(this.data);
	}


	private void initTable() {
		this.dataModel = new AbstractTableModel() {

			public int getColumnCount() {
				return TonalRegionChordPanel.NB_COLUMNS;
			}

			public int getRowCount() {
				return TonalRegionChordPanel.this.data.length;
			}

			public Object getValueAt(int row, int col) {
				return TonalRegionChordPanel.this.data[row][col];
			}

			public String getColumnName(int column) {
				return TonalRegionChordPanel.colName[column];
			}

			public Class getColumnClass(int c) {
				return String.class;
			}

			public boolean isCellEditable(int row, int col) {
				return true;
			}

			public void setValueAt(Object aValue, int row, int column) {
				TonalRegionChordPanel.this.data[row][column] = aValue;
				fireTableCellUpdated(row, column);
			}

		};
		this.table = new JTable(this.dataModel);
		this.scrollPane = new JScrollPane(this.table);
		this.scrollPane.setPreferredSize(new Dimension(360, 280));
		setLayout(new BorderLayout());
		add(this.scrollPane, BorderLayout.CENTER);


		for (int i = 0; i < NB_COLUMNS; i++) {
			TableColumn tc = this.table.getColumnModel().getColumn(i);
			tc.setCellRenderer(new ThreeChordTableCellRenderer());
			tc.setCellEditor(new ChordTableCellEditor());
			tc.setPreferredWidth(295);
		}
		this.table.setSelectionMode(0);
		this.table.setAutoResizeMode(0);

		this.table.setRowHeight(150);
	}

	private void setData(Object[][] data) {
		this.data = data;
		((AbstractTableModel) this.dataModel).fireTableDataChanged();
	}


	public class ThreeChordTableCellRenderer
					extends ThreeChordPanel
					implements TableCellRenderer {
		public ThreeChordTableCellRenderer() {
		}


		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			if (value != null) {
				setData((String) value);
				super.setToolTipText(value.toString());
			} else {
				setData((String) null);
				super.setToolTipText("0 0 0 0");
			}
			repaint();

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
			if ((value != null) && ((value instanceof String))) {
				System.out.println("String value " + value.toString());
				String[] ca = value.toString().split(" ");
				Chord[] chord = new Chord[ca.length];
				for (int i = 0; i < ca.length; i++) {
					chord[i] = ((Chord) ChordList.findChord(ca[i]).get(0));
				}
				for (Chord c : chord) {
					if (c != null)
						ChordUtil.playChord(c);
				}
				ChordUtil.playChord(chord[0]);
			}
			return null;
		}
	}
}
