package lcd.utils;

import java.util.HashMap;
import java.util.Map;

public class CharacterMatrixes {
	// All characters MUST be 10 pixels high,
	// for one character, all lines have the same length (variable from one character to another)

	// TODO See the fonts in spi.lcd.waveshare.fonts

	public final static int FONT_SIZE = 10;
	public final static Map<String, String[]> characters = new HashMap<>();

	static {
		characters.put(" ", new String[]{
				"   ",
				"   ",
				"   ",
				"   ",
				"   ",
				"   ",
				"   ",
				"   ",
				"   ",
				"   "});

		characters.put(".", new String[]{
				"    ",
				"    ",
				"    ",
				"    ",
				"    ",
				"    ",
				"    ",
				" XX ",
				"    ",
				"    "});

		characters.put("'", new String[]{
				"     ",
				"  XX ",
				"  X  ",
				" X   ",
				"     ",
				"     ",
				"     ",
				"     ",
				"     ",
				"     "});

		characters.put("\"", new String[]{
				"     ",
				"     ",
				" X X ",
				" X X ",
				" X X ",
				"     ",
				"     ",
				"     ",
				"     ",
				"     "});

		characters.put("!", new String[]{
				"     ",
				"     ",
				" XX  ",
				" XX  ",
				" XX  ",
				" XX  ",
				"     ",
				" XX  ",
				"     ",
				"     "});

		characters.put("?", new String[]{
				"       ",
				"       ",
				"  XXX  ",
				" X  XX ",
				"   XX  ",
				"  XX   ",
				"       ",
				"  XX   ",
				"       ",
				"       "});

		characters.put("\u00b0", new String[]{
				"     ",
				" XXX ",
				" X X ",
				" XXX ",
				"     ",
				"     ",
				"     ",
				"     ",
				"     ",
				"     "});

		characters.put("-", new String[]{
				"      ",
				"      ",
				"      ",
				"      ",
				" XXXX ",
				"      ",
				"      ",
				"      ",
				"      "});

		characters.put("+", new String[]{
				"        ",
				"        ",
				"        ",
				"    X   ",
				"    X   ",
				"  XXXXX ",
				"    X   ",
				"    X   ",
				"        ",
				"        "});

		characters.put("=", new String[]{
				"      ",
				"      ",
				"      ",
				"      ",
				" XXXX ",
				"      ",
				" XXXX ",
				"      ",
				"      ",
				"      "});

		characters.put(":", new String[]{
				"    ",
				"    ",
				"    ",
				"    ",
				" XX ",
				"    ",
				" XX ",
				"    ",
				"    ",
				"    "});

		characters.put("@", new String[]{
				"       ",
				"       ",
				"  XXX  ",
				" XX  X ",
				" X  XX ",
				" X X X ",
				" X X X ",
				" XX XXX",
				"  XXX  ",
				"       "});

		characters.put("#", new String[]{
				"      ",
				"      ",
				" X X  ",
				" X X  ",
				"XXXXX ",
				" X X  ",
				"XXXXX ",
				" X X  ",
				" X X  ",
				"      "});

		characters.put("$", new String[]{
				"  X   ",
				" XXXX ",
				"XX  X ",
				"XXXX  ",
				" XXXX ",
				"   XX ",
				"XX XX ",
				" XXXX ",
				"  X   ",
				"      "});

		characters.put("%", new String[]{
				"      ",
				"XXX   ",
				"X X X ",
				"XXXX  ",
				"  X   ",
				" XXXX ",
				"X X X ",
				"  XXX ",
				"      ",
				"      "});

		characters.put("^", new String[]{"     ",
				"  X  ",
				" XXX ",
				"XX XX",
				"     ",
				"     ",
				"     ",
				"     ",
				"     ",
				"     "});

		characters.put("&", new String[]{
				"       ",
				"       ",
				"  XXX  ",
				" XX    ",
				"  XX   ",
				" XXXXX ",
				"XX XX  ",
				" XXXXX ",
				"       ",
				"       "});

		characters.put("*", new String[]{
				"     ",
				"  X  ",
				"XXXX ",
				" XX  ",
				"X  X ",
				"     ",
				"     ",
				"     ",
				"     ",
				"     "});

		characters.put("(", new String[]{
				"     ",
				"   X ",
				"  X  ",
				" XX  ",
				" XX  ",
				" XX  ",
				" XX  ",
				"  X  ",
				"   X ",
				"     "});

		characters.put(")", new String[]{
				"      ",
				"  X   ",
				"   X  ",
				"   XX ",
				"   XX ",
				"   XX ",
				"   XX ",
				"   X  ",
				"  X   ",
				"      "});

		characters.put("_", new String[]{
				"       ",
				"       ",
				"       ",
				"       ",
				"       ",
				"       ",
				"       ",
				"       ",
				"       ",
				"XXXXXX "});

		characters.put("<", new String[]{
				"      ",
				"      ",
				"   XX ",
				"  XX  ",
				" XX   ",
				"  XX  ",
				"   XX ",
				"      ",
				"      ",
				"      "});

		characters.put(">", new String[]{
				"      ",
				"      ",
				" XX   ",
				"  XX  ",
				"   XX ",
				"  XX  ",
				" XX   ",
				"      ",
				"      ",
				"      "});

		characters.put("{", new String[]{
				"      ",
				"   XX ",
				"  XX  ",
				"  XX  ",
				" XX   ",
				"  XX  ",
				"  XX  ",
				"  XX  ",
				"   XX ",
				"      "});

		characters.put("}", new String[]{
				"      ",
				" XX   ",
				"  XX  ",
				"  XX  ",
				"   XX ",
				"  XX  ",
				"  XX  ",
				"  XX  ",
				" XX   ",
				"      "});

		characters.put("|", new String[]{
				"      ",
				"  X   ",
				"  X   ",
				"  X   ",
				"  X   ",
				"  X   ",
				"  X   ",
				"  X   ",
				"  X   ",
				"      "});

		characters.put("[", new String[]{
				"      ",
				"  XXX ",
				"  XX  ",
				"  XX  ",
				"  XX  ",
				"  XX  ",
				"  XX  ",
				"  XX  ",
				"  XXX ",
				"      "});

		characters.put("]", new String[]{
				"     ",
				" XXX ",
				"  XX ",
				"  XX ",
				"  XX ",
				"  XX ",
				"  XX ",
				"  XX ",
				" XXX ",
				"     "});

		characters.put("\\", new String[]{
				"      ",
				" X    ",
				" X    ",
				"  X   ",
				"  X   ",
				"   X  ",
				"   X  ",
				"    X ",
				"    X ",
				"      "});

		characters.put("/", new String[]{
				"      ",
				"    X ",
				"    X ",
				"   X  ",
				"   X  ",
				"  X   ",
				"  X   ",
				" X    ",
				" X    ",
				"      "});

		characters.put(",", new String[]{
				"    ",
				"    ",
				"    ",
				"    ",
				"    ",
				"    ",
				"    ",
				" XX ",
				" X  ",
				"X   "});

		characters.put(";", new String[]{
				"    ",
				"    ",
				"    ",
				"    ",
				" XX ",
				"    ",
				"    ",
				" XX ",
				" X  ",
				"X   "});


		characters.put("\272", new String[]{ // Degree sign
				"    ",
				"XXX ",
				"X X ",
				"XXX ",
				"    ",
				"    ",
				"    ",
				"    ",
				"    ",
				"    "});

		characters.put("0", new String[]{
				"      ",
				" XXX  ",
				"XX XX ",
				"XX XX ",
				"XX XX ",
				"XX XX ",
				"XX XX ",
				" XXX  ",
				"      ",
				"      "});

		characters.put("1", new String[]{
				"      ",
				"  XX  ",
				"XXXX  ",
				"  XX  ",
				"  XX  ",
				"  XX  ",
				"  XX  ",
				"XXXXXX",
				"      ",
				"      "});

		characters.put("2", new String[]{
				"      ",
				" XXX  ",
				"XX XX ",
				"   XX ",
				"  XX  ",
				" XX   ",
				"XX XX ",
				"XXXXX ",
				"      ",
				"      "});

		characters.put("3", new String[]{
				"      ",
				" XXX  ",
				"XX XX ",
				"   XX ",
				" XXX  ",
				"   XX ",
				"XX XX ",
				" XXX  ",
				"      ",
				"      "});

		characters.put("4", new String[]{
				"      ",
				"   XX ",
				"  XXX ",
				" X XX ",
				"XX XX ",
				"XXXXXX",
				"   XX ",
				"   XX ",
				"      ",
				"      "});

		characters.put("5", new String[]{
				"      ",
				"XXXXX ",
				"XX    ",
				"XXXX  ",
				"XX XX ",
				"   XX ",
				"X  XX ",
				"XXXX  "});

		characters.put("6", new String[]{
				"      ",
				" XXX  ",
				"XX XX ",
				"XX    ",
				"XXXX  ",
				"XX XX ",
				"XX XX ",
				" XXX  ",
				"      ",
				"      "});

		characters.put("7", new String[]{
				"      ",
				"XXXXX ",
				"XX XX ",
				"   XX ",
				"  XX  ",
				"  XX  ",
				" XX   ",
				"XX    "});

		characters.put("8", new String[]{
				"      ",
				" XXX  ",
				"XX XX ",
				"XX XX ",
				" XXX  ",
				"XX XX ",
				"XX XX ",
				" XXX  ",
				"      ",
				"      "});

		characters.put("9", new String[]{
				"      ",
				" XXX  ",
				"XX XX ",
				"XX XX ",
				" XXXX ",
				"   XX ",
				"XX XX ",
				" XXX  ",
				"      ",
				"      "});

		characters.put("a", new String[]{
				"      ",
				"      ",
				"      ",
				" XXX  ",
				"XX XX ",
				" XXXX ",
				"XX XX ",
				"XXXXXX",
				"      ",
				"      "});

		characters.put("b", new String[]{
				"       ",
				"XXX    ",
				" XX    ",
				" XXXX  ",
				" XX XX ",
				" XX XX ",
				" XX XX ",
				"XXXXX  ",
				"       ",
				"       "});

		characters.put("c", new String[]{
				"      ",
				"      ",
				"      ",
				" XXX  ",
				"XX XX ",
				"XX    ",
				"XX XX ",
				" XXX  ",
				"      ",
				"      "});

		characters.put("d", new String[]{
				"      ",
				"  XXX ",
				"   XX ",
				" XXXX ",
				"XX XX ",
				"XX XX ",
				"XX XX ",
				" XXXXX",
				"      ",
				"      "});

		characters.put("e", new String[]{
				"      ",
				"      ",
				"      ",
				" XXX  ",
				"XX XX ",
				"XXXXX ",
				"XX    ",
				" XXXX ",
				"      ",
				"      "});

		characters.put("f", new String[]{
				"     ",
				"  XXX",
				" XX  ",
				"XXXXX",
				" XX  ",
				" XX  ",
				" XX  ",
				"XXXX ",
				"     ",
				"     "});

		characters.put("g", new String[]{
				"     ",
				"     ",
				"     ",
				" XX X",
				"XX XX",
				"XX XX",
				"XX XX",
				" XXXX",
				"   XX",
				"XXXX "});

		characters.put("h", new String[]{
				"       ",
				"XXX    ",
				" XX    ",
				" XXXX  ",
				" XX XX ",
				" XX XX ",
				" XX XX ",
				" XX XX ",
				"       ",
				"       "});

		characters.put("i", new String[]{
				"      ",
				"  XX  ",
				"      ",
				"XXXX  ",
				"  XX  ",
				"  XX  ",
				"  XX  ",
				"XXXXXX",
				"      ",
				"      "});

		characters.put("j", new String[]{
				"     ",
				"  XX ",
				"     ",
				"XXXX ",
				"  XX ",
				"  XX ",
				"  XX ",
				"  XX ",
				"  XX ",
				"XXX  "});

		characters.put("k", new String[]{
				"      ",
				"XXX   ",
				" XX   ",
				" XX XX",
				" XXXX ",
				" XXX  ",
				" XXXX ",
				" XX XX",
				"      ",
				"      "});

		characters.put("l", new String[]{
				"     ",
				"XXXX ",
				"  XX ",
				"  XX ",
				"  XX ",
				"  XX ",
				"  XX ",
				"XXXXX",
				"     ",
				"     "});

		characters.put("m", new String[]{
				"      ",
				"      ",
				"      ",
				"XXXXX ",
				" XXXXX",
				" X X X",
				" X X X",
				" X X X",
				"      ",
				"      "});

		characters.put("n", new String[]{
				"       ",
				"       ",
				"       ",
				"XX XX  ",
				" XX XX ",
				" XX XX ",
				" XX XX ",
				" XX XX ",
				"       ",
				"       "});

		characters.put("o", new String[]{
				"     ",
				"     ",
				"     ",
				" XXX ",
				"XX XX",
				"XX XX",
				"XX XX",
				" XXX ",
				"     ",
				"     "});

		characters.put("p", new String[]{
				"       ",
				"       ",
				"       ",
				"XXXXX  ",
				" XX XX ",
				" XX XX ",
				" XX XX ",
				" XXXX  ",
				" XX    ",
				"XXXX   "});

		characters.put("q", new String[]{
				"      ",
				"      ",
				"      ",
				" XX X ",
				"XX XX ",
				"XX XX ",
				"XX XX ",
				" XXXX ",
				"   XX ",
				"  XXXX"});

		characters.put("r", new String[]{
				"      ",
				"      ",
				"      ",
				"XX XXX",
				" XXX X",
				" XX   ",
				" XX   ",
				"XXXX  ",
				"      ",
				"      "});

		characters.put("s", new String[]{
				"      ",
				"      ",
				"      ",
				" XXXX ",
				"XXX   ",
				" XXXX ",
				"   XXX",
				"XXXXX ",
				"      ",
				"      "});

		characters.put("t", new String[]{
				"     ",
				" XX  ",
				" XX  ",
				"XXXXX",
				" XX  ",
				" XX  ",
				" XX X",
				"  XXX",
				"     ",
				"     "});

		characters.put("u", new String[]{
				"      ",
				"      ",
				"      ",
				"XXX XX",
				" XX XX",
				" XX XX",
				" XX XX",
				"  XXXX",
				"      ",
				"      "});

		characters.put("v", new String[]{
				"       ",
				"       ",
				"       ",
				"XXX XXX",
				" XX XX ",
				"  XXX  ",
				"  XXX  ",
				"   X   ",
				"       ",
				"       "});

		characters.put("w", new String[]{
				"      ",
				"      ",
				"      ",
				"X X XX",
				"X X X ",
				"XXXXX ",
				" XXXX ",
				"  X X ",
				"      ",
				"      "});

		characters.put("x", new String[]{
				"      ",
				"      ",
				"      ",
				"XXX XX",
				" XXXX ",
				"  XX  ",
				" XXXXX",
				"XX  XX",
				"      ",
				"      "});

		characters.put("y", new String[]{
				"       ",
				"       ",
				"       ",
				"XXX XXX",
				" XX XX ",
				" XX XX ",
				"  X X  ",
				"  XXX  ",
				"  XX   ",
				"XXX    "});

		characters.put("z", new String[]{
				"     ",
				"     ",
				"     ",
				"XXXXX",
				"X XX ",
				" XX  ",
				"XX XX",
				"XXXXX",
				"     ",
				"     "});

		characters.put("A", new String[]{
				"      ",
				"      ",
				" XXXX ",
				"  XXX ",
				"  X X ",
				" XXXXX",
				" XX XX",
				"XXX XX",
				"       ",
				"       "});

		characters.put("B", new String[]{
				"       ",
				"       ",
				"XXXXX  ",
				" XX XX ",
				" XXXX  ",
				" XX XX ",
				" XX XX ",
				"XXXXX  ",
				"       ",
				"       "});

		characters.put("C", new String[]{
				"     ",
				"     ",
				" XXXX",
				"XX XX",
				"XX   ",
				"XX   ",
				"XX XX",
				" XXX ",
				"     ",
				"     "});

		characters.put("D", new String[]{
				"      ",
				"      ",
				"XXXXX ",
				" XX XX",
				" XX XX",
				" XX XX",
				" XX XX",
				"XXXXX ",
				"      ",
				"      "});

		characters.put("E", new String[]{
				"      ",
				"      ",
				"XXXXXX",
				" XX   ",
				" XXXX ",
				" XX   ",
				" XX XX",
				"XXXXXX",
				"      ",
				"      "});

		characters.put("F", new String[]{
				"      ",
				"      ",
				"XXXXXX",
				" XX   ",
				" XXXX ",
				" XX   ",
				" XX   ",
				"XXXX  ",
				"      ",
				"      "});

		characters.put("G", new String[]{
				"     ",
				"     ",
				" XXX ",
				"XX XX",
				"XX   ",
				"XXXXX",
				"XX XX",
				" XXXX",
				"     ",
				"     "});

		characters.put("H", new String[]{
				"      ",
				"      ",
				"XXX XX",
				" XX XX",
				" XXXXX",
				" XX XX",
				" XX XX",
				"XXX XX",
				"      ",
				"      "});

		characters.put("I", new String[]{
				"      ",
				"      ",
				"XXXXX ",
				"  XX  ",
				"  XX  ",
				"  XX  ",
				"  XX  ",
				"XXXXX ",
				"      ",
				"      "});

		characters.put("J", new String[]{
				"     ",
				"     ",
				" XXXX",
				"  XX ",
				"  XX ",
				"X XX ",
				"X XX ",
				"XXX  ",
				"     ",
				"     "});

		characters.put("K", new String[]{
				"      ",
				"      ",
				"XXX XX",
				" XX X ",
				" XXX  ",
				" XXXX ",
				" XX XX",
				"XXXX X",
				"      ",
				"      "});

		characters.put("L", new String[]{
				"      ",
				"      ",
				"XXXX  ",
				" XX   ",
				" XX   ",
				" XX   ",
				" XX XX",
				"XXXXXX",
				"      ",
				"      "});

		characters.put("M", new String[]{
				"       ",
				"       ",
				"XX   XX",
				" XX XX ",
				" XX XX ",
				" XXXXX ",
				" X X X ",
				"XX X XX",
				"       ",
				"       "});

		characters.put("N", new String[]{
				"      ",
				"      ",
				"XX XXX",
				"XXX X ",
				"XXX X ",
				"XX XX ",
				"XX XX ",
				"XX  X ",
				"      ",
				"      "});

		characters.put("O", new String[]{
				"      ",
				"      ",
				" XXX  ",
				"XX XX ",
				"XX XX ",
				"XX XX ",
				"XX XX ",
				" XXX  ",
				"      ",
				"      "});

		characters.put("P", new String[]{
				"       ",
				"       ",
				"XXXXX  ",
				" XX XX ",
				" XX XX ",
				" XXXX  ",
				" XX    ",
				"XXXX   ",
				"       ",
				"       "});

		characters.put("Q", new String[]{
				"     ",
				"     ",
				" XXX ",
				"XX XX",
				"XX XX",
				"XX XX",
				"XX XX",
				" XXX ",
				"   XX",
				"     "});

		characters.put("R", new String[]{
				"       ",
				"       ",
				"XXXXX  ",
				" XX XX ",
				" XX XX ",
				" XXXX  ",
				" XX XX ",
				"XXXX XX",
				"       ",
				"       "});

		characters.put("S", new String[]{
				"     ",
				"     ",
				" XXXX",
				"XX  X",
				"XXXX  ",
				"  XXX",
				"X  XX",
				"XXXX ",
				"     ",
				"     "});

		characters.put("T", new String[]{
				"      ",
				"      ",
				"XXXXXX",
				"X XX X",
				"  XX  ",
				"  XX  ",
				"  XX  ",
				" XXXX ",
				"      ",
				"      "});

		characters.put("U", new String[]{
				"       ",
				"       ",
				"XXX XXX",
				" XX XX ",
				" XX XX ",
				" XX XX ",
				" XX XX ",
				"  XXX  ",
				"       ",
				"       "});

		characters.put("V", new String[]{
				"       ",
				"       ",
				"XXX XXX",
				" XX XX ",
				" XX XX ",
				"  XXX  ",
				"  XXX  ",
				"   X   ",
				"       ",
				"       "});

		characters.put("W", new String[]{
				"       ",
				"       ",
				"XX X XX",
				" X X X ",
				" X X X ",
				" XXXXX ",
				"  XXX  ",
				"  X X  ",
				"       ",
				"       "});

		characters.put("X", new String[]{
				"      ",
				"      ",
				"XX  XX",
				" XXXX ",
				"  XX  ",
				"  XX  ",
				" XXXX ",
				"XX  X ",
				"      ",
				"      "});

		characters.put("Y", new String[]{
				"      ",
				"      ",
				"XX  XX",
				"XX  XX",
				" XXXX ",
				"  XX  ",
				"  XX  ",
				" XXXX ",
				"      ",
				"      "});

		characters.put("Z", new String[]{
				"     ",
				"     ",
				"XXXXX",
				"XX XX",
				"  XX ",
				" XX  ",
				"XX XX",
				"XXXXX",
				"     ",
				"     "});
	}

	public static boolean[][] getLeds(String letter) {
		boolean[][] leds = null;
		String[] dots = characters.get(letter);
		if (dots != null) {
			leds = new boolean[dots[0].length()][8];
			for (int line = 0; line < dots.length; line++) {
				String s = dots[line];
				for (int col = 0; col < s.length(); col++) {
					leds[col][line] = (s.charAt(col) == 'X');
				}
			}
		}
		return leds;
	}
}
