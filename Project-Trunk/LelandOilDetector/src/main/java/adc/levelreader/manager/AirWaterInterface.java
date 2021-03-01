package adc.levelreader.manager;

public interface AirWaterInterface {
    /**
     * @param channel  0 to 6
     * @param material SevenADCChannelsManager.AIR, WATER, or OIL
     * @param val      channel value in %
     */
    public void setTypeOfChannel(int channel, SevenADCChannelsManager.Material material, float val);

    public void setSurfaceDistance(double dist);

    public void setStarted(boolean b);
}