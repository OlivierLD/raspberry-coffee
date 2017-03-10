package ukulele.applet;

import chordfinder.UkuleleChordFinder;
import ctx.AppContext;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import javax.swing.JApplet;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import section.one.KeyChordPanel;
import section.one.PrincipalChordPanel;
import section.one.TonalRegionChordPanel;
import section.one.VampChordPanel;
import ukulele.ChordPanel;








public class ChordAppletTwo
  extends JApplet
{
  private BorderLayout borderLayout1 = new BorderLayout();

  private JPanel keyChordPanel = new KeyChordPanel();
  private JPanel vampChordPanel = new VampChordPanel();
  private JPanel principalChordPanel = new PrincipalChordPanel();
  private JPanel tonalChordPanel = new TonalRegionChordPanel();
  private ChordPanel chordIdentifierPanel = new ChordPanel();

  private JTabbedPane tabbedPane = new JTabbedPane();





  private void jbInit()
    throws Exception
  {
    getContentPane().setLayout(this.borderLayout1);
    setSize(new Dimension(820, 525));
    getContentPane().add(this.tabbedPane, "Center");
    this.tabbedPane.add("Keys", this.keyChordPanel);
    this.tabbedPane.add("Vamp Chords", this.vampChordPanel);
    this.tabbedPane.add("Principal Chords", this.principalChordPanel);
    this.tabbedPane.add("Tonal Regions Chart", this.tonalChordPanel);
    this.chordIdentifierPanel.setChordMode(2);
    this.tabbedPane.add("Chord Identifier", this.chordIdentifierPanel);
  }

  public void init()
  {
    String lang = getParameter("lang");
    AppContext.getInstance().fireUserLanguage(lang);
    try
    {
      jbInit();
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }










  private void jButton1_actionPerformed(ActionEvent e)
  {
    UkuleleChordFinder.main(null);
  }
}
