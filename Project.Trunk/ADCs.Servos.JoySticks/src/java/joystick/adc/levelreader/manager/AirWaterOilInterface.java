package joystick.adc.levelreader.manager;

public interface AirWaterOilInterface {
	/**
	 * @param channel  0 to 6
	 * @param material SevenADCChannelsManager.AIR, WATER, or OIL
	 * @param val      channel value in %
	 */
	public void setTypeOfChannel(int channel, SevenADCChannelsManager.Material material, float val);
}
