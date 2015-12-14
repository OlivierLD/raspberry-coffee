package mindwave.samples.io.gui;

import gnu.io.CommPortIdentifier;

import java.awt.BorderLayout;
import java.awt.Dimension;

import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;

public class MindWaveFrame
  extends JFrame
{
  private BorderLayout borderLayout1 = new BorderLayout();
  private JTabbedPane tabbedPane = new JTabbedPane();  
  private MindWavePanel mwPanel = new MindWavePanel(this);
  private MindWaveRawPanel rawPanel = new MindWaveRawPanel();

  private MindWaves parent;
  
  public MindWaveFrame(MindWaves parent)
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
  }

  private void jbInit()
    throws Exception
  {
    this.getContentPane().setLayout(borderLayout1);
    this.setSize( new Dimension(1200, 500) );
    this.setTitle("Mind Waves");
    
//  this.getContentPane().add(mwPanel, BorderLayout.CENTER);
    this.getContentPane().add(tabbedPane, BorderLayout.CENTER);
    tabbedPane.add("Parsed", mwPanel);
    tabbedPane.add("Raw", rawPanel);    
  }
  
  public void setPortList(Map<String, CommPortIdentifier> pm)
  {
    mwPanel.setPortList(pm);
  }
}
