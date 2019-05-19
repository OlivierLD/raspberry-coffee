package mindwave.samples.io.gui.ctx;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.List;


public class MindWaveContext implements Serializable
{
  private static MindWaveContext context = null;  
  private transient List<MindWaveListener> mwListeners = null;

  
  private MindWaveContext()
  {
    mwListeners = new ArrayList<MindWaveListener>(2); // 2: Initial Capacity
  }
    
  public static synchronized MindWaveContext getInstance()
  {
    if (context == null)
      context = new MindWaveContext();    
    return context;
  }
    
  public synchronized List<MindWaveListener> getListeners()
  {
    return mwListeners;
  }    

  public synchronized void addListener(MindWaveListener l)
  {
    synchronized (mwListeners)
    {
      if (!mwListeners.contains(l))
      {
        mwListeners.add(l);
      }
    }
  }

  public synchronized void removeListener(MindWaveListener l)
  {
    mwListeners.remove(l);
  }
  
  // Fire Methods
  public void fireConnect(String port, int br)
  {
    for (MindWaveListener l : mwListeners)
      l.connect(port, br);
  }
  public void fireDisconnect()
  {
    for (MindWaveListener l : mwListeners)
      l.disconnect();
  }
  public void fireSerialConnected()
  {
    for (MindWaveListener l : mwListeners)
      l.serialConnected();
  }
  public void fireSerialDisconnected()
  {
    for (MindWaveListener l : mwListeners)
      l.serialDisconnected();
  }
  public void fireMindWaveStatus(String status)
  {    
    for (MindWaveListener l : mwListeners)
      l.mindWaveStatus(status);
  }
  public void fireAddRawData(short rd)
  {    
    for (MindWaveListener l : mwListeners)
      l.addRawData(rd);
  }
  public void fireMinRaw(int v)
  {
    for (MindWaveListener l : mwListeners)
      l.setMinRaw(v);
  }
  public void fireMaxRaw(short v)
  {
    for (MindWaveListener l : mwListeners)
      l.setMaxRaw(v);
  }
  public void fireAvg(int v)
  {
    for (MindWaveListener l : mwListeners)
      l.setAvg(v);
  }
  public void fireAttention(int v)
  {
    for (MindWaveListener l : mwListeners)
      l.setAttention(v);
  }
  public void fireRelaxation(int v)
  {
    for (MindWaveListener l : mwListeners)
      l.setMeditation(v);
  }
  public void fireSerialData(String s)
  {
    for (MindWaveListener l : mwListeners)
      l.setSerialData(s);
  }
  public void fireParsing(byte[] ba)
  {
    for (MindWaveListener l : mwListeners)
      l.parsing(ba);
  }
  public void fireEyeBlink()
  {
    for (MindWaveListener l : mwListeners)
      l.eyeBlink();
  }
}
