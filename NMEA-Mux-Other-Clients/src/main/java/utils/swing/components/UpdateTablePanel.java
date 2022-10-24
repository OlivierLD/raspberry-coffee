package utils.swing.components;


import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import java.awt.*;
import java.util.Date;
import java.util.List;


public class UpdateTablePanel
        extends JPanel {
    // Table Columns
    private final static String RESOURCE = "Resource";
    private final static String CURRENT = "Current";
    private final static String NEW = "New";
    private final static String REQUIRE_RESTART = "Requ.Restart";

    final static String[] names = {RESOURCE,
            CURRENT,
            NEW,
            REQUIRE_RESTART};
    // Table content
    private transient Object[][] data = new Object[0][names.length];

    private transient TableModel dataModel;
    private JTable table;

    private BorderLayout borderLayout1 = new BorderLayout();
    private JPanel centerPanel = new JPanel();
    private BorderLayout borderLayout2 = new BorderLayout();
    private JScrollPane centerScrollPane = null; // new JScrollPane();
    private JPanel topPanel = new JPanel();
    private JLabel jLabel1 = new JLabel();

    public UpdateTablePanel(String s, List<String[]> tableLines) {
        try {
            jbInit();
            jLabel1.setText(s);
            setValues(tableLines);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setValues(List<String[]> tableLines) {
        try {
            for (String[] line : tableLines) {
                addLineInTable(line[1], line[3], line[4], line[2]);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void jbInit() throws Exception {
        this.setLayout(borderLayout1);
        this.setSize(new Dimension(580, 170));
        this.setPreferredSize(new Dimension(580, 170));
        centerPanel.setLayout(borderLayout2);

        this.add(centerPanel, BorderLayout.CENTER);
        this.add(topPanel, BorderLayout.NORTH);
        topPanel.add(jLabel1, null);
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
                return getValueAt(0, c).getClass();
            }

            public boolean isCellEditable(int row, int col) {
                return false;
            }

            public void setValueAt(Object aValue, int row, int column) {
                data[row][column] = aValue;
                fireTableCellUpdated(row, column);
            }
        };
        // Create JTable
        table = new JTable(dataModel);

        centerScrollPane = new JScrollPane(table);
        centerPanel.add(centerScrollPane, BorderLayout.CENTER);
    }

    private void addLineInTable(String resource,
                                String current,
                                String newDate,
                                String restart) {
        int len = data.length;
        Object[][] newData = new Object[len + 1][names.length];
        for (int i = 0; i < len; i++) {
            for (int j = 0; j < names.length; j++)
                newData[i][j] = data[i][j];
        }
        newData[len][0] = resource;
        newData[len][1] = (current == null) ? " - " : new Date(Long.parseLong(current)).toString();
        newData[len][2] = new Date(Long.parseLong(newDate)).toString();
        newData[len][3] = new Boolean(restart);
        data = newData;
        ((AbstractTableModel) dataModel).fireTableDataChanged();
        table.repaint();
    }

}