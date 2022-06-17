package dnd.gui.utils;

import dnd.gui.ctx.AppContext;
import dnd.gui.ctx.ImageAppListener;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

public class ImageTable
		extends JPanel {
	private static final long serialVersionUID = -2306956983424909244L;
	private transient List<ImageDefinition> imageData;
	private ImageTable instance = this;

	private static final String IMAGE_NAME = "Name";

	private static final String IMAGE_TYPE = "Type";

	private static final String IMAGE_WIDTH = "Width";
	private static final String IMAGE_HEIGHT = "Height";
	private static final String IMAGE_CREATED = "Created";
	private static final String IMAGE_LABELS = "Tags";
	private static final String[] names = {IMAGE_NAME, IMAGE_TYPE, IMAGE_WIDTH, IMAGE_HEIGHT, IMAGE_CREATED, IMAGE_LABELS};

	private static final int[] columnSort = {0, 0, 0, 0, 0};

	private transient Object[][] data = new Object[0][names.length];

	private transient TableModel dataModel;

	private JTable table;
	private BorderLayout borderLayout1 = new BorderLayout();
	private JPanel centerPanel = new JPanel();
	private JPanel bottomPanel = new JPanel();
	private JPanel buttonPanel = new JPanel();
	private JButton removeButton = new JButton("Remove");
	private JButton extractButton = new JButton("Extract");
	private BorderLayout borderLayout2 = new BorderLayout();
	private JScrollPane centerScrollPane = null;
	private JPanel topPanel = new JPanel();
	private JLabel filterLabel = new JLabel();
	private JTextField filterTextField = new JTextField();
	private JRadioButton orCheckBox = new JRadioButton("or");
	private JRadioButton andCheckBox = new JRadioButton("and");
	private ButtonGroup buttonGroup = new ButtonGroup();
	private JProgressBar progressBar = new JProgressBar();

	private GridBagLayout gridBagLayout1 = new GridBagLayout();
	private JLabel statusLabel = new JLabel();
	private BorderLayout borderLayout3 = new BorderLayout();

	private ImageIcon up = new ImageIcon(getClass().getResource("up.png"));
	private ImageIcon down = new ImageIcon(getClass().getResource("down.png"));
	private JCheckBox untaggedCheckBox = new JCheckBox();

	public ImageTable(List<ImageDefinition> imageData) {
		try {
			jbInit();
			if (imageData != null) {
				setImageData(imageData);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void setValues() {
		int nbl = 0;
		try {
			this.data = new Object[0][names.length];
			if (this.imageData != null) {
				for (ImageDefinition id : this.imageData) {
					addLineInTable(id.getName(), id.getType(), id.getW(), id.getH(), id.getCreated(), id.getTags());
					nbl++;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		((AbstractTableModel) this.dataModel).fireTableDataChanged();
		this.table.repaint();
		setTableStatusLabel(Integer.toString(nbl) + "/" + Integer.toString(ImageDBUtils.getNbRecTotal()) + " image(s)");
	}

	private void jbInit() throws Exception {
		AppContext.getInstance().addApplicationListener(new ImageAppListener() {
			public void refreshFromDB() {
				List<ImageDefinition> imageList = ImageDBUtils.populateImageList(AppContext.getInstance().getConn(), ImageTable.columnSort);
				setImageData(imageList);
			}

			public void setStatusLabel(String str)
			{
				setTableStatusLabel(str);
			}

			public void activateProgressBar(boolean b) {
				progressBar.setEnabled(b);
				if (!b) {
					progressBar.setValue(0);
					progressBar.setStringPainted(false);
				}
				progressBar.repaint();
			}

			public void setProgressBar(int v, int max) {
				progressBar.setMaximum(max);
				progressBar.setValue(v);
				progressBar.setStringPainted(true);
				progressBar.setString(Integer.toString(v) + "/" + Integer.toString(max));
				progressBar.repaint();
			}
		});
		setLayout(this.borderLayout1);
		this.centerPanel.setLayout(this.borderLayout2);
		this.bottomPanel.setLayout(this.borderLayout3);
		this.topPanel.setLayout(this.gridBagLayout1);
		this.filterLabel.setText("Filter:");
		this.statusLabel.setText("Ready");
		add(this.centerPanel, BorderLayout.CENTER);
		this.bottomPanel.add(this.statusLabel, BorderLayout.WEST);
		this.progressBar.setPreferredSize(new Dimension(200, 17));
		this.progressBar.setEnabled(false);
		this.bottomPanel.add(this.progressBar, BorderLayout.EAST);
		this.buttonPanel.add(this.removeButton, null);
		this.buttonPanel.add(this.extractButton, null);
		this.bottomPanel.add(this.buttonPanel, BorderLayout.NORTH);
		this.extractButton.setEnabled(false);
		this.removeButton.setEnabled(false);
		this.extractButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				extractSelectedRows();
			}
		});
		this.removeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				removeSelectedRows();
			}

		});
		add(this.bottomPanel, BorderLayout.SOUTH);
		this.topPanel.add(this.filterLabel, new GridBagConstraints(0, 0, 1, 1, 0.0D, 0.0D, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));

		this.topPanel.add(this.filterTextField, new GridBagConstraints(1, 0, 2, 1, 1.0D, 0.0D, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));

		this.topPanel.add(this.orCheckBox, new GridBagConstraints(0, 1, 1, 1, 0.0D, 0.0D, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 4, 0, 0), 0, 0));

		this.topPanel.add(this.andCheckBox, new GridBagConstraints(1, 1, 1, 1, 0.0D, 0.0D, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));

		this.topPanel.add(this.untaggedCheckBox, new GridBagConstraints(2, 1, 1, 1, 0.0D, 0.0D, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 10, 0, 0), 0, 0));

		this.untaggedCheckBox.setSelected(false);
		this.untaggedCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				filterLabel.setEnabled(!untaggedCheckBox.isSelected());
				filterTextField.setEnabled(!untaggedCheckBox.isSelected());
				filterTextField.setEditable(!untaggedCheckBox.isSelected());
				orCheckBox.setEnabled(!untaggedCheckBox.isSelected());
				andCheckBox.setEnabled(!untaggedCheckBox.isSelected());
				setSelection();
			}
		});
		this.filterTextField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {}
		});
		this.filterTextField.getDocument().addDocumentListener(new DocumentListener() {
			public void insertUpdate(DocumentEvent e)
			{
				setSelection();
			}
			public void removeUpdate(DocumentEvent e)
			{
				setSelection();
			}
			public void changedUpdate(DocumentEvent e)
			{
				setSelection();
			}
		});
		this.filterTextField.setToolTipText("Enter selection criteria, like part of the name, a tag...");
		this.orCheckBox.setSelected(true);
		this.andCheckBox.setSelected(false);
		this.buttonGroup.add(this.orCheckBox);
		this.buttonGroup.add(this.andCheckBox);
		this.orCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				setSelection();
			}
		});
		this.andCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				setSelection();
			}
		});
		add(this.topPanel, BorderLayout.NORTH);
		initTable();

		SelectionListener listener = new SelectionListener(this.table) {

		};

//		this.table.getModel().addTableModelListener(new TableModelListener() {
//			public void tableChanged(TableModelEvent e) {
//				if (e.getType() == TableModelEvent.UPDATE) {
//					System.out.println(e.getColumn() + ", " + e.getFirstRow() + ", " + e.getLastRow());
//				}
//			}
//		});

		this.table.getSelectionModel().addListSelectionListener(listener);
		this.table.getColumnModel().getSelectionModel().addListSelectionListener(listener);
		this.table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

		this.filterLabel.setEnabled(this.imageData != null);
		this.filterTextField.setEnabled(this.imageData != null);

		this.table.addMouseListener(new MouseAdapter() {
			public void mouseReleased(MouseEvent e) {
				int sr;
				if (e.getClickCount() == 1 /* 2 */) {
					sr = table.getSelectedRow();
					if (sr >= 0) {
						for (ImageDefinition id : imageData) {
							if (id.getName().equals(data[sr][0])) {
								AppContext.getInstance().fireDisplayImage(id.getName()); // Display image here
								break;
							}
						}
					}
				}
			}
		});
		JTableHeader header = this.table.getTableHeader();
		header.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent evt) {
				JTable table = ((JTableHeader) evt.getSource()).getTable();
				TableColumnModel colModel = table.getColumnModel();

				int vColIndex = colModel.getColumnIndexAtX(evt.getX());

				if (vColIndex == -1) {
					return;
				}
				if (vColIndex < 5) {
					boolean shift = false;
					boolean ctrl = false;
					int mask = evt.getModifiers();
					if ((mask & 0x1) != 0) {
						shift = true;
					}
					if ((mask & 0x2) != 0) {
						ctrl = true;
					}
					ImageTable.columnSort[vColIndex] = 1;
					ImageIcon icon = up;

					if (ctrl) {
						ImageTable.columnSort[vColIndex] = 0;
						icon = null;
					}
					if (shift) {
						ImageTable.columnSort[vColIndex] = -1;
						icon = down;
					}
					table.getColumnModel().getColumn(vColIndex).setHeaderValue(new ImageTable.TextAndIcon(ImageTable.names[vColIndex], icon));

					setSelection();
				}

				Rectangle headerRect = table.getTableHeader().getHeaderRect(vColIndex);
				if (vColIndex == 0) {
					headerRect.width -= 3;
				} else {
					headerRect.grow(-3, 0);
				}
				if (!headerRect.contains(evt.getX(), evt.getY())) {
					int vLeftColIndex = vColIndex;
					if (evt.getX() < headerRect.x) {
						vLeftColIndex--;
					}
				}
			}
		});
		ColumnHeaderToolTips tips = new ColumnHeaderToolTips();
		String[] columnToolTips =
				{"<html><b>Image Name</b><br>Click: sort, ascending<br>Shift + Click: sort, descending<br>Ctrl + Click: no sort</html>",
						"<html><b>Image type</b><br>Click: sort, ascending<br>Shift + Click: sort, descending<br>Ctrl + Click: no sort</html>",
						"<html><b>Image width</b><br>Click: sort, ascending<br>Shift + Click: sort, descending<br>Ctrl + Click: no sort</html>",
						"<html><b>Image height</b><br>Click: sort, ascending<br>Shift + Click: sort, descending<br>Ctrl + Click: no sort</html>",
						"<html><b>Image Creation Date</b><br>Click: sort, ascending<br>Shift + Click: sort, descending<br>Ctrl + Click: no sort</html>",
						"<html><b>Image Tags</b>, comma separated</html>"};

		for (int i = 0; i < names.length; i++) {
			tips.setToolTip(header.getColumnModel().getColumn(i), columnToolTips[i]);
		}
		header.addMouseMotionListener(tips);
		this.untaggedCheckBox.setText("Untagged images only");
	}

	private void initTable() {
		this.dataModel = new AbstractTableModel() {
			public int getColumnCount() {
				return ImageTable.names.length;
			}
			public int getRowCount() {
				return data.length;
			}
			public Object getValueAt(int row, int col) {
				return data[row][col];
			}
			public String getColumnName(int column) {
				return ImageTable.names[column];
			}
			public Class getColumnClass(int c) {
				return getValueAt(0, c).getClass();
			}
			public boolean isCellEditable(int row, int col) {
				return col == 5;
			}
			public void setValueAt(Object aValue, int row, int column) {
				try {
					if (column == 5) {
						if (System.getProperty("verbose", "false").equals("true")) System.out.println("Updating [" + data[row][0] + "] with " + aValue);
						if (aValue != null) {
							String[] labels = ((String) aValue).trim().split(",");
							ImageDBUtils.updateTags(AppContext.getInstance().getConn(), (String) data[row][0], labels);
						}
					}
					data[row][column] = aValue;
					fireTableCellUpdated(row, column);
				} catch (Exception ex) {
					JOptionPane.showMessageDialog(instance, ex.toString() + "\nrow:" + row + ", column:" + column, "setValueAt", 0);
				}
			}
		};
		this.table = new JTable(this.dataModel);

		this.table.getColumn(IMAGE_NAME).setPreferredWidth(75);
		this.table.getColumn(IMAGE_TYPE).setPreferredWidth(25);
		this.table.getColumn(IMAGE_TYPE).setCellRenderer(new CenteredStringCellRenderer());
		this.table.getColumn(IMAGE_WIDTH).setPreferredWidth(35);
		this.table.getColumn(IMAGE_WIDTH).setCellRenderer(new CenteredStringCellRenderer());
		this.table.getColumn(IMAGE_HEIGHT).setPreferredWidth(35);
		this.table.getColumn(IMAGE_HEIGHT).setCellRenderer(new CenteredStringCellRenderer());
		this.table.getColumn(IMAGE_CREATED).setPreferredWidth(75);
		this.table.getColumn(IMAGE_CREATED).setCellRenderer(new CenteredStringCellRenderer());
		this.table.getColumn(IMAGE_LABELS).setPreferredWidth(300);

		for (int i = 0; i < 5; i++) {
			this.table.getTableHeader().getColumnModel().getColumn(i).setHeaderRenderer(new IconHeaderRenderer());
		}
		this.table.setAutoResizeMode(0);

		this.table.setSelectionMode(0);

		this.centerScrollPane = new JScrollPane(this.table);
		this.centerPanel.add(this.centerScrollPane, BorderLayout.CENTER);
	}

	private void addLineInTable(String name, String type, int w, int h, String created, String tags) {
		int len = this.data.length;
		Object[][] newData = new Object[len + 1][names.length];
		for (int i = 0; i < len; i++) {
			for (int j = 0; j < names.length; j++) {
				newData[i][j] = this.data[i][j];
			}
		}
		newData[len][0] = name;
		newData[len][1] = type;
		newData[len][2] = new Integer(w);
		newData[len][3] = new Integer(h);
		newData[len][4] = created;
		newData[len][5] = tags;
		this.data = newData;
		((AbstractTableModel) this.dataModel).fireTableDataChanged();
		this.table.repaint();
	}

	private void setSelection() {
		String filter = this.filterTextField.getText();
		int logicalConnector = ImageDBUtils.OR_CONNECTOR;
		if (this.andCheckBox.isSelected()) {
			logicalConnector = ImageDBUtils.AND_CONNECTOR;
		}
		List<ImageDefinition> data = null;
		if (this.untaggedCheckBox.isSelected()) {
			data = ImageDBUtils.untaggedImageList(AppContext.getInstance().getConn());
		} else {
			data = ImageDBUtils.filterImageList(AppContext.getInstance().getConn(), filter, logicalConnector, columnSort);
		}
		setImageData(data);
	}

	public void setImageData(List<ImageDefinition> imageData) {
		this.imageData = imageData;
		this.filterLabel.setEnabled(this.imageData != null);
		this.filterTextField.setEnabled(this.imageData != null);
		setValues();
	}

	public List<ImageDefinition> getImageData() {
		return this.imageData;
	}

	public void extractSelectedRows() {
		int[] selectedRows = this.table.getSelectedRows();

		List<String> selectedImages = new ArrayList<>(selectedRows.length);
		for (int i = 0; i < selectedRows.length; i++) {
			selectedImages.add((String) this.data[selectedRows[i]][0]);
		}
		for (String in : selectedImages) {
			ImageDBUtils.extractImage(AppContext.getInstance().getConn(), in);
		}
		JOptionPane.showMessageDialog(this, "Extracted images are in the directory " + System.getProperty("user.dir"), "Extraction", 1);
	}

	public void removeSelectedRows() {
		int[] selectedRows = this.table.getSelectedRows();

		int resp = JOptionPane.showConfirmDialog(this, "Delete " + selectedRows.length + " image(s) ?", "Delete images", 0, 3);
		if (resp == 0) {
			List<String> selectedImages = new ArrayList<>(selectedRows.length);
			for (int i = 0; i < selectedRows.length; i++) {
				selectedImages.add((String) this.data[selectedRows[i]][0]);
			}
			for (String in : selectedImages) {
				ImageDBUtils.deleteImage(AppContext.getInstance().getConn(), in);
			}
			AppContext.getInstance().fireRefreshFromDB();
		}
	}

	public void setTableStatusLabel(String s) {
		this.statusLabel.setText(s);
		this.statusLabel.repaint();
	}

	public class SelectionListener
			implements ListSelectionListener {
		JTable table;

		SelectionListener(JTable table)
		{
			this.table = table;
		}

		public void valueChanged(ListSelectionEvent lse) {
			int selectedRow = this.table.getSelectedRow();
			removeButton.setEnabled(selectedRow >= 0);
			extractButton.setEnabled(selectedRow >= 0);
			if (selectedRow < 0) {

			} else {
					for (ImageDefinition id : imageData) {
						if (id.getName().equals(data[selectedRow][0])) {
							AppContext.getInstance().fireDisplayImage(id.getName()); // Display image here
							break;
						}
					}
				}

			}
		}
//}

	class CenteredStringCellRenderer
			extends DefaultTableCellRenderer {
		CenteredStringCellRenderer() {}

		public Component getTableCellRendererComponent(JTable table, Object value, boolean selected, boolean focused, int row, int column) {
			super.setHorizontalAlignment(0);
			super.getTableCellRendererComponent(table, value, selected, focused, row, column);

			return this;
		}
	}

	class TextAndIcon {
		String text;
		Icon icon;

		TextAndIcon(String text, Icon icon) {
			this.text = text;
			this.icon = icon;
		}
	}

	class IconHeaderRenderer
			extends DefaultTableCellRenderer {
		IconHeaderRenderer() {}

		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			if (table != null) {
				JTableHeader header = table.getTableHeader();
				if (header != null) {
					setForeground(header.getForeground());
					setBackground(header.getBackground());
					setFont(header.getFont());
				}
			}

			if ((value instanceof ImageTable.TextAndIcon)) {
				setIcon(((ImageTable.TextAndIcon) value).icon);
				setText(((ImageTable.TextAndIcon) value).text);
			} else {
				setText(value == null ? "" : value.toString());
				setIcon(null);
			}
			setBorder(UIManager.getBorder("TableHeader.cellBorder"));
			setHorizontalAlignment(0);
			return this;
		}
	}

	class ColumnHeaderToolTips
			extends MouseMotionAdapter {
		TableColumn curCol;

		Map tips = new HashMap();

		ColumnHeaderToolTips() {}

		@SuppressWarnings("unchecked")
		public void setToolTip(TableColumn col, String tooltip) {
			if (tooltip == null) {
				this.tips.remove(col);
			} else {
				this.tips.put(col, tooltip);
			}
		}

		public void mouseMoved(MouseEvent evt) {
			TableColumn col = null;
			JTableHeader header = (JTableHeader) evt.getSource();
			JTable table = header.getTable();
			TableColumnModel colModel = table.getColumnModel();
			int vColIndex = colModel.getColumnIndexAtX(evt.getX());

			if (vColIndex >= 0) {
				col = colModel.getColumn(vColIndex);
			}
			if (col != this.curCol) {
				header.setToolTipText((String) this.tips.get(col));
				this.curCol = col;
			}
		}
	}
}
