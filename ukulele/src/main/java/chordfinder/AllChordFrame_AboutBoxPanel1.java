package chordfinder;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;


public class AllChordFrame_AboutBoxPanel1
				extends JPanel {
	private JLabel labelTitle = new JLabel();
	private JLabel labelAuthor = new JLabel();
	private JLabel labelCopyright = new JLabel();
	private JLabel labelCompany = new JLabel();
	private GridBagLayout layoutMain = new GridBagLayout();
	private Border border = BorderFactory.createEtchedBorder();

	public AllChordFrame_AboutBoxPanel1() {
		try {
			jbInit();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void jbInit()
					throws Exception {
		setLayout(this.layoutMain);
		setBorder(this.border);
		this.labelTitle.setText("Ukulele 101");
		this.labelAuthor.setText("Olivier LeDiouris");
		this.labelCopyright.setText("Copyright OlivSoft, 2012");
		this.labelCompany.setText("OlivSoft strikes again");
		add(this.labelTitle, new GridBagConstraints(0, 0, 1, 1, 0.0D, 0.0D, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 15, 0, 15), 0, 0));
		add(this.labelAuthor, new GridBagConstraints(0, 1, 1, 1, 0.0D, 0.0D, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 15, 0, 15), 0, 0));
		add(this.labelCopyright, new GridBagConstraints(0, 2, 1, 1, 0.0D, 0.0D, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 15, 0, 15), 0, 0));
		add(this.labelCompany, new GridBagConstraints(0, 3, 1, 1, 0.0D, 0.0D, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 15, 5, 15), 0, 0));
	}
}
