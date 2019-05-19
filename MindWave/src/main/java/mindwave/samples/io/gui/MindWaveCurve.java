package mindwave.samples.io.gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;

import java.awt.Graphics2D;
import java.awt.Point;

import java.awt.RenderingHints;
import java.awt.Stroke;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import mindwave.MindWaveController;

import mindwave.samples.io.gui.ctx.MindWaveContext;
import mindwave.samples.io.gui.ctx.MindWaveListener;

public class MindWaveCurve
  extends JPanel
{
  private List<Short> wave = new ArrayList<Short>();
  private final static int WIDTH = 1_000;
  
  protected transient Stroke dotted  = new BasicStroke(1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1f, new float[] {10f}, 0f);
  protected transient Stroke dotted2 = new BasicStroke(1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1f, new float[] {5f}, 0f);
  protected transient Stroke origStroke = null;
  
  public MindWaveCurve()
  {
    MindWaveContext.getInstance().addListener(new MindWaveListener()
    {
      public void addRawData(short data) 
      {
//      System.out.println("Adding " + data);
        wave.add(data);
//      System.out.println("Size " + wave.size());
        while (wave.size() > WIDTH)
          wave.remove(0);
        repaint();
      }
    });  
  }
  
  private boolean blinking = false;
  
  public void paintComponent(Graphics gr)
  {
    Graphics2D g2d = (Graphics2D)gr;
    g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);      
    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);      

    gr.setColor(Color.black);
    gr.fillRect(0, 0, this.getWidth(), this.getHeight());
    if (wave.size() > 1)
    {      
      long sum = 0;
      short min = 0; // Integer.MAX_VALUE;
      short max = Short.MIN_VALUE;
      for (int y : wave)
      {
        sum += y;
  //    min = (short)Math.min(min, y);
        max = (short)Math.max(max, y);
      }
      short avg = (short)Math.round((double)sum / wave.size());
      MindWaveContext.getInstance().fireMinRaw(min);
      MindWaveContext.getInstance().fireMaxRaw(max);
      MindWaveContext.getInstance().fireAvg(avg);
      gr.setColor(Color.cyan);
      float xFact = (float)this.getWidth() / (float)wave.size();
      float yFact = (float)(this.getHeight() / 1f) / (float)(Math.max(Math.abs(min), Math.abs(max)));
      
      if (!blinking && wave.get(wave.size() - 1) > MindWaveController.EYE_BLINK_THRESHOLD) 
      {
        blinking = true;
        MindWaveContext.getInstance().fireEyeBlink();
      }
      else if (blinking && wave.get(wave.size() - 1) < MindWaveController.EYE_BLINK_THRESHOLD)
      {
        blinking = false;
      }
      
      origStroke = g2d.getStroke();
      g2d.setStroke(dotted);
      int yZero = Math.round(this.getHeight() / 1); //  - (yFact * wave.get(i)));
      gr.drawLine(0, yZero, this.getWidth(), yZero);
      g2d.setStroke(dotted2);
      gr.setColor(new Color(Color.yellow.getRed(), Color.yellow.getGreen(), Color.yellow.getBlue(), 100));
      for (int x=0; x<Math.max(Math.abs(min), Math.abs(max)); x+= 100)
      {
        if (x > 0)
        {
          yZero = (int)(Math.round(this.getHeight() / 1) - (yFact * x));
          gr.drawLine(0, yZero, this.getWidth(), yZero);
          gr.drawString(Integer.toString(x), 5, yZero - 1);
          yZero = (int)(Math.round(this.getHeight() / 1) - (yFact * -x));
          gr.drawLine(0, yZero, this.getWidth(), yZero);
          gr.drawString(Integer.toString(-x), 5, yZero - 1);
        }
      }
      
      g2d.setStroke(origStroke);
      gr.setColor(Color.green);
      Point previous = null;
      for (int i=0; i<wave.size(); i++)
      {
        int x = Math.round(xFact * i);
        int y = Math.round((this.getHeight() / 1) - (yFact * wave.get(i)));
        Point pt = new Point(x, y);
        if (previous != null)
          gr.drawLine(previous.x, previous.y, pt.x, pt.y);
        previous = pt;
      }
      
      // Smoothed, w: 100
      int smoothWidth = 100;
      double[] smoothed = new double[wave.size()];
      for (int i=0; i<wave.size(); i++)
      {
        double d = 0;
        int k = 0;
        for (int j=i-(smoothWidth/2); j<(i+(smoothWidth/2)); j++)
        {
          k = j; 
          if (k < 0) k = 0;
          if (k >= wave.size()) k = wave.size() - 1;
          d += wave.get(k);
        }
        smoothed[i] = d / smoothWidth;
      }
      // Draw smoothed curve
      gr.setColor(Color.red);
      previous = null;
      for (int i=0; i<smoothed.length; i++)
      {
        int x = Math.round(xFact * i);
        int y = (int)Math.round((this.getHeight() / 1) - (yFact * smoothed[i]));
        Point pt = new Point(x, y);
        if (previous != null)
          gr.drawLine(previous.x, previous.y, pt.x, pt.y);
        previous = pt;
      }
    }
  }
}
