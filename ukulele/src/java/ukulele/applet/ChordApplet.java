package ukulele.applet;

import chordfinder.AllChordPanel;
import chordfinder.UkuleleChordFinder;
import ctx.AppContext;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import javax.swing.JApplet;
import javax.swing.JPanel;









public class ChordApplet
  extends JApplet
{
  private BorderLayout borderLayout1 = new BorderLayout();
  private JPanel allChordPanel = new AllChordPanel();





  private void jbInit()
    throws Exception
  {
    getContentPane().setLayout(this.borderLayout1);
    setSize(new Dimension(820, 525));
    getContentPane().add(this.allChordPanel, "Center");
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
