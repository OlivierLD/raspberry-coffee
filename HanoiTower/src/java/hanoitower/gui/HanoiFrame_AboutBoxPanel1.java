package hanoitower.gui;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

public class HanoiFrame_AboutBoxPanel1 extends JPanel {

	public HanoiFrame_AboutBoxPanel1() {
		labelTitle = new JLabel();
		labelAuthor = new JLabel();
		labelCopyright = new JLabel();
		labelCompany = new JLabel();
		layoutMain = new GridBagLayout();
		border = BorderFactory.createEtchedBorder();
		try {
			jbInit();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void jbInit()
			throws Exception {
		setLayout(layoutMain);
		setBorder(border);
		labelTitle.setText("Tour de Hanoi");
		labelAuthor.setText("T & T");
		labelCopyright.setText("Copyright 2012");
		labelCompany.setText("Lyc\351e Francais de San Francisco");
		add(labelTitle, new GridBagConstraints(0, 0, 1, 1, 0.0D, 0.0D, 17, 0, new Insets(5, 15, 0, 15), 0, 0));
		add(labelAuthor, new GridBagConstraints(0, 1, 1, 1, 0.0D, 0.0D, 17, 0, new Insets(0, 15, 0, 15), 0, 0));
		add(labelCopyright, new GridBagConstraints(0, 2, 1, 1, 0.0D, 0.0D, 17, 0, new Insets(0, 15, 0, 15), 0, 0));
		add(labelCompany, new GridBagConstraints(0, 3, 1, 1, 0.0D, 0.0D, 17, 0, new Insets(0, 15, 5, 15), 0, 0));
	}

	private static final long serialVersionUID = 1L;
	private JLabel labelTitle;
	private JLabel labelAuthor;
	private JLabel labelCopyright;
	private JLabel labelCompany;
	private GridBagLayout layoutMain;
	private transient Border border;
}
