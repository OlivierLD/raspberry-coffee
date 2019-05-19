package polarmaker.polars.smooth.gui;

import polarmaker.polars.main.PolarSmoother;
import polarmaker.smooth.PolarsResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseEvent;

public class AboutPolarSmoother extends JPanel {
	private transient Border border = BorderFactory.createEtchedBorder();
	private GridBagLayout layoutMain = new GridBagLayout();
	private JLabel labelCompany = new JLabel();
	private JLabel labelCopyright = new JLabel();
	private JLabel labelAuthor = new JLabel();
	private JLabel labelTitle = new JLabel();

	public AboutPolarSmoother() {
		try {
			jbInit();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void jbInit() throws Exception {
		this.setLayout(layoutMain);
		this.setBorder(border);
		labelTitle.setText(PolarsResourceBundle.getPolarsResourceBundle().getString("frame-title"));
		labelAuthor.setText("version " + PolarSmoother.VERSION_NUMBER);
		labelCopyright.setText("Copyright 2012");
		labelCompany.setText("<html><a href='http://donpedro.lediouris.net'>The Don Pedro Project</a></html>");
		labelCompany.addMouseListener(new java.awt.event.MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				labelCompany_mouseClicked(e);
			}
		});
		this.add(labelTitle, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 15, 0, 15), 0, 0));
		this.add(labelAuthor, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 15, 0, 15), 0, 0));
		this.add(labelCopyright, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 15, 0, 15), 0, 0));
		this.add(labelCompany, new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
				new Insets(0, 15, 10, 15), 0, 0));
	}

	private void labelCompany_mouseClicked(MouseEvent e) {
		// TODO Other OS...
		try {
			Runtime.getRuntime().exec("cmd /k start http://donpedro.lediouris.net");
		} catch (Exception ignore) {
		}
	}
}
