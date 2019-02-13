package spi.lcd.waveshare.fonts;

public abstract class Font {
	public abstract int getWidth();
	public abstract int getHeight();
	public abstract int[] getCharacters();

	public int strlen(String str) {
		int nbChar = str.length();
		return nbChar * this.getWidth();
	}
}
