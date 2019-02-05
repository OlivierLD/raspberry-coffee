package spi.lcd.waveshare.fonts;

public abstract class Font {
	public abstract int getWidth();
	public abstract int getHeight();
	public abstract int[] getCharacters();
}
