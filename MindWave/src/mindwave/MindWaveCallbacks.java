package mindwave;

public interface MindWaveCallbacks
{
  public void mindWaveConnected(MindWaveController.DeviceID did);
  public void mindWaveDisconnected(MindWaveController.DeviceID did);
  public void mindWaveStandby(MindWaveController.StbyStatus ss);
  public void mindWaveAccessDenied();
  public void mindWaveNotFound();
  public void mindWaveRawWave(MindWaveController.RawWave rw);
  public void mindWavePoorSignal(MindWaveController.PoorSignal ps);
  public void mindWaveBatteryLevel(MindWaveController.BatteryLevel bl);
  public void mindWaveHeartRate(MindWaveController.HeartRate hr);
  public void mindWave8BitRaw(MindWaveController.EightBitRaw ebr);
  public void mindWaveRawMarker(MindWaveController.RawMarker rm);
  public void mindWaveAttention(MindWaveController.Attention att);
  public void mindWaveMeditation(MindWaveController.Meditation med);
  public void mindWaveAsicEegPower(MindWaveController.AsicEegPower aep);
  public void mindWaveUnknowType(byte t);  
  public void mindWaveError(Throwable t);
}
