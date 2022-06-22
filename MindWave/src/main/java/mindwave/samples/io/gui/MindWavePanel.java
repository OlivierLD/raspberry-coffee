package mindwave.samples.io.gui;

import gnu.io.CommPortIdentifier;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;

import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import java.awt.Insets;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.text.NumberFormat;

import java.util.Map;

import java.util.Set;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import mindwave.samples.io.gui.ctx.MindWaveContext;
import mindwave.samples.io.gui.ctx.MindWaveListener;
import mindwave.samples.io.gui.widgets.HalfDisplay;

public class MindWavePanel
  extends JPanel
{
  private final BorderLayout borderLayout1 = new BorderLayout();
  private final JPanel bottomPanel = new JPanel();
  private final GridBagLayout gridBagLayout1 = new GridBagLayout();
  private final JLabel portLabel = new JLabel();
  private final JLabel brLabel = new JLabel();
  private final JPanel centerPanel = new MindWaveCurve();
  private final JComboBox<String> portComboBox = new JComboBox<>();
  private final JButton connectButton = new JButton();
  private final JComboBox<Integer> brComboBox = new JComboBox<>();
  private final JLabel statusLabel = new JLabel();
  
  private boolean serialConnected = false;

  private final MindWaveFrame parent;
  private final JLabel minLabel = new JLabel();
  private final JLabel minValue = new JLabel();
  private final JLabel maxLabel = new JLabel();
  private final JLabel maxValue = new JLabel();
  private final JLabel attentionLabel = new JLabel();
  private final JLabel meditationLabel = new JLabel();
  private final JLabel attentionValue = new JLabel();
  private final JLabel meditationValue = new JLabel();
  private final JPanel displayPanelHolder = new JPanel(new BorderLayout());
  private final JPanel displayPanel = new JPanel();
  private final GridBagLayout gridBagLayout2 = new GridBagLayout();
  private final HalfDisplay attentionDisplay  = new HalfDisplay(100.0, 25d, 1, true);
  private final HalfDisplay meditationDisplay = new HalfDisplay(100.0, 25d, 1, true);
  private final JLabel serialDataLabel = new JLabel();
  private final JLabel avgLabel = new JLabel();
  private final JLabel avgValue = new JLabel();
  private final JLabel blinkLabel = new JLabel();

  public MindWavePanel(MindWaveFrame parent)
  {
    this.parent = parent;
    try
    {
      jbInit();
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
    
    MindWaveContext.getInstance().addListener(new MindWaveListener()
    {
      @Override
      public void serialConnected() 
      {
        connectButton.setText("Disconnect");
        serialConnected = true;
      }
      @Override
      public void serialDisconnected() 
      { 
        connectButton.setText("Connect");
        serialConnected = false;
      }     
      @Override
      public void mindWaveStatus(String status)
      {
        statusLabel.setText("Status:" + status);
        statusLabel.repaint();
      }
      @Override
      public void setMinRaw(int v)
      {
        minValue.setText(NumberFormat.getInstance().format(v));
      }
      @Override
      public void setMaxRaw(int v)
      {
        maxValue.setText(NumberFormat.getInstance().format(v));
      }
      @Override
      public void setAvg(int v)
      {
        avgValue.setText(NumberFormat.getInstance().format(v));
      }
      @Override
      public void setAttention(int v) 
      {
        attentionValue.setText(NumberFormat.getInstance().format(v));
        attentionDisplay.setSpeed(v);
      }
      @Override
      public void setMeditation(int v) 
      {
        meditationValue.setText(NumberFormat.getInstance().format(v));
        meditationDisplay.setSpeed(v);
      }
      @Override
      public void setSerialData(String str)
      {
        serialDataLabel.setText(str);
      }
      @Override
      public void eyeBlink()
      {
        Thread blink = new Thread(() -> {
          System.out.println("Blinking!!");
          setBlinkLabel("Blink!");
          try { Thread.sleep(1_000L); } catch (Exception ex) {}
          setBlinkLabel("");
        });
        blink.start();
      }
    });
  }

  private void jbInit()
    throws Exception
  {
    this.setLayout(borderLayout1);
    this.setSize(new Dimension(920, 415));
    this.setPreferredSize(new Dimension(70, 100));
    this.setMinimumSize(new Dimension(70, 70));
    bottomPanel.setLayout(gridBagLayout1);
    portLabel.setText("Port");
    brLabel.setText("Baud");
    portComboBox.setMinimumSize(new Dimension(100, 21));
    portComboBox.setPreferredSize(new Dimension(100, 21));
    connectButton.setText("Connect");
    connectButton.addActionListener(this::connectButton_actionPerformed);
    brComboBox.setMinimumSize(new Dimension(100, 21));
    brComboBox.setPreferredSize(new Dimension(100, 21));
    statusLabel.setText("Status: None");
    minLabel.setText("Min Value:");
    minValue.setText("0");
    minValue.setFont(new Font("Tahoma", Font.PLAIN, 11));
    maxLabel.setText("Max Value:");
    maxValue.setText("0");
    maxValue.setFont(new Font("Tahoma", Font.PLAIN, 11));
    attentionLabel.setText("Attention:");
    meditationLabel.setText("Meditation:");
    attentionValue.setText("0");
    attentionValue.setFont(new Font("Tahoma", Font.PLAIN, 11));
    meditationValue.setText("0");
    meditationValue.setFont(new Font("Tahoma", Font.PLAIN, 11));
    displayPanel.setLayout(gridBagLayout2);
    brComboBox.removeAllItems();
    brComboBox.addItem(1200);
    brComboBox.addItem(2400);
    brComboBox.addItem(4800);
    brComboBox.addItem(9600);
    brComboBox.addItem(19200);
    brComboBox.addItem(38400);
    brComboBox.addItem(57600);
    brComboBox.addItem(115200);

    attentionDisplay.setPreferredSize(new Dimension(200, 140));
    attentionDisplay.setLabel("Attention");
    meditationDisplay.setPreferredSize(new Dimension(200, 140));
    meditationDisplay.setLabel("Meditation");

    serialDataLabel.setText("Serial Data");
    serialDataLabel.setFont(new Font("Courier New", Font.PLAIN, 11));
    avgLabel.setText("Average:");
    avgValue.setText("0");
    avgValue.setFont(new Font("Tahoma", Font.PLAIN, 11));
    blinkLabel.setText("-");
    blinkLabel.setFont(new Font("Tahoma", Font.PLAIN, 30));
    blinkLabel.setForeground(Color.red);
    bottomPanel.add(portLabel,
                    new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
                                           new Insets(0, 0, 0, 10), 0, 0));
    bottomPanel.add(brLabel,
                    new GridBagConstraints(3, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
                                           new Insets(0, 10, 0, 5), 0, 0));
    bottomPanel.add(portComboBox,
                    new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
                                           new Insets(0, 0, 0, 0), 0, 0));
    bottomPanel.add(connectButton,
                    new GridBagConstraints(5, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
                                           new Insets(0, 10, 0, 0), 0, 0));
    bottomPanel.add(brComboBox,
                    new GridBagConstraints(4, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
                                           new Insets(0, 0, 0, 10), 0, 0));
    bottomPanel.add(statusLabel,
                    new GridBagConstraints(6, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE,
                                           new Insets(0, 10, 0, 0), 0, 0));
    bottomPanel.add(minLabel,
                    new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
                                           new Insets(0, 0, 0, 0), 0, 0));
    bottomPanel.add(minValue,
                    new GridBagConstraints(2, 2, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
                                           new Insets(0, 0, 0, 10), 0, 0));
    bottomPanel.add(maxLabel,
                    new GridBagConstraints(3, 2, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
                                           new Insets(0, 0, 0, 0), 0, 0));
    bottomPanel.add(maxValue,
                    new GridBagConstraints(4, 2, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
                                           new Insets(0, 0, 0, 10), 0, 0));
    bottomPanel.add(attentionLabel,
                    new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
                                           new Insets(0, 0, 0, 0), 0, 0));
    bottomPanel.add(meditationLabel,
                    new GridBagConstraints(3, 3, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
                                           new Insets(0, 0, 0, 0), 0, 0));
    bottomPanel.add(attentionValue,
                    new GridBagConstraints(2, 3, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
                                           new Insets(0, 0, 0, 10), 0, 0));
    bottomPanel.add(meditationValue,
                    new GridBagConstraints(4, 3, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
                                           new Insets(0, 0, 0, 10), 0, 0));
    bottomPanel.add(serialDataLabel,
                    new GridBagConstraints(1, 0, 6, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
                                           new Insets(0, 0, 0, 0), 0, 0));
    bottomPanel.add(avgLabel,
                    new GridBagConstraints(5, 2, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
                                           new Insets(0, 0, 0, 0), 0, 0));
    bottomPanel.add(avgValue,
                    new GridBagConstraints(6, 2, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
                                           new Insets(0, 0, 0, 0), 0, 0));
    displayPanel.add(attentionDisplay,
                     new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.NORTH, GridBagConstraints.NONE,
                                            new Insets(-10, 0, 0, 0), 0, 0));

    displayPanel.add(meditationDisplay,
                     new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.NORTH, GridBagConstraints.NONE,
                                            new Insets(-10, 0, 0, 0), 0, 0));
    displayPanel.add(blinkLabel,
                     new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.NORTH, GridBagConstraints.NONE,
                                            new Insets(10, 0, 0, 0), 0, 0));
    this.add(bottomPanel, BorderLayout.SOUTH);
    this.add(centerPanel, BorderLayout.CENTER);
    this.add(displayPanelHolder, BorderLayout.WEST);
    displayPanelHolder.add(displayPanel, BorderLayout.NORTH);

    connectButton.setEnabled(false);
  }

  public void paintComponent(Graphics gr)
  {
    centerPanel.repaint();
  }

  private void setBlinkLabel(String s)
  {
    blinkLabel.setText(s);  
    blinkLabel.repaint();
  }
  
  private void connectButton_actionPerformed(ActionEvent e)
  {
    if (!serialConnected)
    {
      String port = portComboBox.getSelectedItem().toString();
      int br      = (Integer) brComboBox.getSelectedItem();
      MindWaveContext.getInstance().fireConnect(port, br);
    }
    else
    {
      MindWaveContext.getInstance().fireDisconnect();
    }
  }
  
  public void setPortList(Map<String, CommPortIdentifier> pm)
  {
    portComboBox.removeAllItems();
    Set<String> ports = pm.keySet();
    for (String port: ports)
      portComboBox.addItem(port);
    connectButton.setEnabled(true);
  }
}
