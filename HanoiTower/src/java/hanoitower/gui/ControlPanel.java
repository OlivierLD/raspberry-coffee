package hanoitower.gui;

import hanoitower.events.HanoiContext;
import hanoitower.events.HanoiEventListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;

public class ControlPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	private GridBagLayout gridBagLayout1;
	private JLabel nbDiscLabel;
	private transient SpinnerModel model2D;
	private JSpinner nbDiscSpinner;
	private JButton goButton;

	public ControlPanel() {
		gridBagLayout1 = new GridBagLayout();
		nbDiscLabel = new JLabel();
		model2D = new SpinnerNumberModel(1, 1, 50, 1);
		nbDiscSpinner = new JSpinner(model2D);
		goButton = new JButton();
		try {
			jbInit();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void jbInit() throws Exception {
		HanoiContext.getInstance().addApplicationListener(new HanoiEventListener() {

      public void computationCompleted() {
        nbDiscLabel.setEnabled(true);
        nbDiscSpinner.setEnabled(true);
        goButton.setEnabled(true);
      }
		});
		setLayout(gridBagLayout1);
		nbDiscLabel.setText("Nb Discs:");
		nbDiscSpinner.addChangeListener(evt -> {
		  JSpinner spinner = (JSpinner) evt.getSource();
		  Object value = spinner.getValue();
		  if (value instanceof Integer) {
		    Integer d = (Integer) value;
		    HanoiContext.getInstance().fireSetNbDisc(d.intValue());
		  }
		});
		nbDiscSpinner.setValue(4);
		goButton.setText("Go!");
		goButton.addActionListener(e -> goButton_actionPerformed(e));
		add(nbDiscLabel, new GridBagConstraints(0, 0, 1, 1, 0.0D, 0.0D, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 3), 0, 0));
		add(nbDiscSpinner, new GridBagConstraints(1, 0, 1, 1, 0.0D, 0.0D, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		add(goButton, new GridBagConstraints(2, 0, 1, 1, 0.0D, 0.0D, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 10, 0, 0), 0, 0));
	}

	private void goButton_actionPerformed(ActionEvent e) {
		nbDiscLabel.setEnabled(false);
		nbDiscSpinner.setEnabled(false);
		goButton.setEnabled(false);
		HanoiContext.getInstance().fireStartComputation();
	}

	private void nbDiscSpinner_propertyChange(PropertyChangeEvent e) {
		int nb = ((Integer) nbDiscSpinner.getValue()).intValue();
		System.out.println("From the spinner");
		HanoiContext.getInstance().fireSetNbDisc(nb);
	}

}
