package sevensegdisplay;

import com.pi4j.io.i2c.I2CFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SevenSegment
{
  private LEDBackPack display = null;
  /*
   *  The seven segments of each digit are represented by 7 bits on a byte:
   *
   *               --   0&00000001
   *  0&00100000  |  |  0&00000010
   *               --   0&01000000
   *  0&00010000  |  |  0&00000100
   *  0&00001000   --.  0&10000000 <- this is the dot, at the bottom right of each matrix
   */
  public final static byte TOP          = 0&00000001; // 0x01
  public final static byte TOP_RIGHT    = 0&00000010; // 0x02
  public final static byte BOTTOM_RIGHT = 0&00000100; // 0x04
  public final static byte BOTTOM       = 0&00001000; // 0x08
  public final static byte BOTTOM_LEFT  = 0&00010000; // 0x10
  public final static byte TOP_LEFT     = 0&00100000; // 0x20
  public final static byte MIDDLE       = 0&01000000; // 0x40
  public final static byte DOT          = 0&10000000; // 0x80

  /*
   * Examples:
   *   8 = TOP | MIDDLE | BOTTOM | TOP_LEFT | TOP_RIGHT | BOTTOM_RIGHT | BOTTOM_LEFT
   *   7 = TOP | TOP_RIGHT | BOTTOM_RIGHT
   *   etc.
   */

  private final static int[] digits = { 0x3F, 0x06, 0x5B, 0x4F, 0x66, 0x6D, 0x7D, 0x07, 0x7F, 0x6F, // 0..9
                                        0x77, 0x7C, 0x39, 0x5E, 0x79, 0x71 };                       // A..F

  public final static Map<String, Byte> ALL_CHARS = new HashMap<>();
  //  0x00, 0x80, 0x80, 0x80, 0x80, 0x80, 0x80, 0x02, /*   ! " # $ % & '  */
  //  0x80, 0x0f, 0x80, 0x80, 0x04, 0x40, 0x80, 0x80, /* ( ) * + , - . /  */
  //  0x3F, 0x06, 0x5B, 0x4F, 0x66, 0x6D, 0x7D, 0x07, /* 0 1 2 3 4 5 6 7  */
  //  0x7F, 0x6F, 0x80, 0x80, 0x80, 0x48, 0x80, 0x27, /* 8 9 : ; < = > ?  */
  //  0x80, 0x77, 0x7c, 0x39, 0x5e, 0x79, 0x71, 0x3d, /* @ A B C D E F G  */
  //  0x76, 0x30, 0x1E, 0x76, 0x38, 0x15, 0x37, 0x3f, /* H I J K L M N O  */
  //  0x73, 0x67, 0x31, 0x6d, 0x78, 0x3e, 0x1C, 0x2A, /* P Q R S T U V W  */
  //  0x76, 0x6e, 0x5b, 0x39, 0x80, 0x0F, 0x80, 0x08, /* X Y Z [ \ ] ^ _  */
  //  0x80, 0x5f, 0x7c, 0x58, 0x5e, 0x7b, 0x71, 0x6F, /* ` a b c d e f g  */
  //  0x74, 0x30, 0x0E, 0x76, 0x06, 0x15, 0x54, 0x5c, /* h i j k l m n o  */
  //  0x73, 0x67, 0x50, 0x6d, 0x78, 0x1c, 0x1c, 0x2A, /* p q r s t u v w  */
  //  0x76, 0x6e, 0x5b, 0x39, 0x80, 0x0F, 0x80, 0x08  /* x y z { | } ~    */


  static { // FYI, 0x80 is the dot, displayed instead of "undisplayable" characters.
    ALL_CHARS.put(" ",  (byte)0x00);
    ALL_CHARS.put("!",  DOT);
    ALL_CHARS.put("\"", DOT);
    ALL_CHARS.put("#",  DOT);
    ALL_CHARS.put("$",  DOT);
    ALL_CHARS.put("%",  DOT);
    ALL_CHARS.put("&",  DOT);
    ALL_CHARS.put("'",  (byte)0x02);
    ALL_CHARS.put("(",  (byte)0x39);
    ALL_CHARS.put(")",  (byte)0x0f);
    ALL_CHARS.put("*",  DOT);
    ALL_CHARS.put("+",  DOT);
    ALL_CHARS.put(",",  (byte)0x04);
    ALL_CHARS.put("-",  (byte)0x40);
    ALL_CHARS.put(".",  DOT);
    ALL_CHARS.put("0",  (byte)0x3f);
    ALL_CHARS.put("1",  (byte)0x06);
    ALL_CHARS.put("2",  (byte)0x5b);
    ALL_CHARS.put("3",  (byte)0x4f);
    ALL_CHARS.put("4",  (byte)0x66);
    ALL_CHARS.put("5",  (byte)0x6d);
    ALL_CHARS.put("6",  (byte)0x7d);
    ALL_CHARS.put("7",  (byte)0x07);
    ALL_CHARS.put("8",  (byte)0x7f);
    ALL_CHARS.put("9",  (byte)0x6f);
    ALL_CHARS.put(":",  DOT);
    ALL_CHARS.put(";",  DOT);
    ALL_CHARS.put("<",  DOT);
    ALL_CHARS.put("=",  (byte)0x48);
    ALL_CHARS.put(">",  DOT);
    ALL_CHARS.put("?",  (byte)0x27);
    ALL_CHARS.put("@",  DOT);
    ALL_CHARS.put("A",  (byte)0x77);
    ALL_CHARS.put("B",  (byte)0x7c);
    ALL_CHARS.put("C",  (byte)0x39);
    ALL_CHARS.put("D",  (byte)0x5e);
    ALL_CHARS.put("E",  (byte)0x79);
    ALL_CHARS.put("F",  (byte)0x71);
    ALL_CHARS.put("G",  (byte)0x3d);
    ALL_CHARS.put("H",  (byte)0x76);
    ALL_CHARS.put("I",  (byte)0x30);
    ALL_CHARS.put("J",  (byte)0x1e);
    ALL_CHARS.put("K",  (byte)0x76);
    ALL_CHARS.put("L",  (byte)0x38);
    ALL_CHARS.put("M",  (byte)0x15);
    ALL_CHARS.put("N",  (byte)0x37);
    ALL_CHARS.put("O",  (byte)0x3f);
    ALL_CHARS.put("P",  (byte)0x73);
    ALL_CHARS.put("Q",  (byte)0x67);
    ALL_CHARS.put("R",  (byte)0x31);
    ALL_CHARS.put("S",  (byte)0x6d);
    ALL_CHARS.put("T",  (byte)0x78);
    ALL_CHARS.put("U",  (byte)0x3e);
    ALL_CHARS.put("V",  (byte)0x1c);
    ALL_CHARS.put("W",  (byte)0x2a);
    ALL_CHARS.put("X",  (byte)0x76);
    ALL_CHARS.put("Y",  (byte)0x6e);
    ALL_CHARS.put("Z",  (byte)0x5b);
    ALL_CHARS.put("[",  (byte)0x39);
    ALL_CHARS.put("\\", DOT);
    ALL_CHARS.put("]",  (byte)0x0f);
    ALL_CHARS.put("^",  DOT);
    ALL_CHARS.put("_",  (byte)0x08);
    ALL_CHARS.put("`",  DOT);
    ALL_CHARS.put("a",  (byte)0x5f);
    ALL_CHARS.put("b",  (byte)0x7c);
    ALL_CHARS.put("c",  (byte)0x58);
    ALL_CHARS.put("d",  (byte)0x5e);
    ALL_CHARS.put("e",  (byte)0x7b);
    ALL_CHARS.put("f",  (byte)0x71);
    ALL_CHARS.put("g",  (byte)0x6f);
    ALL_CHARS.put("h",  (byte)0x74);
    ALL_CHARS.put("i",  (byte)0x30);
    ALL_CHARS.put("j",  (byte)0x0e);
    ALL_CHARS.put("k",  (byte)0x76);
    ALL_CHARS.put("l",  (byte)0x06);
    ALL_CHARS.put("m",  (byte)0x15);
    ALL_CHARS.put("n",  (byte)0x54);
    ALL_CHARS.put("o",  (byte)0x5c);
    ALL_CHARS.put("p",  (byte)0x73);
    ALL_CHARS.put("q",  (byte)0x67);
    ALL_CHARS.put("r",  (byte)0x50);
    ALL_CHARS.put("s",  (byte)0x6d);
    ALL_CHARS.put("t",  (byte)0x78);
    ALL_CHARS.put("u",  (byte)0x1c);
    ALL_CHARS.put("v",  (byte)0x1c);
    ALL_CHARS.put("w",  (byte)0x2a);
    ALL_CHARS.put("x",  (byte)0x76);
    ALL_CHARS.put("y",  (byte)0x6e);
    ALL_CHARS.put("z",  (byte)0x5b);
    ALL_CHARS.put("{",  (byte)0x39);
    ALL_CHARS.put("|",  (byte)0x30);
    ALL_CHARS.put("}",  (byte)0x0f);
    ALL_CHARS.put("~",  DOT);
  }

  public SevenSegment() throws I2CFactory.UnsupportedBusNumberException {
    display = new LEDBackPack(0x70);
  }

  public SevenSegment(int addr) throws I2CFactory.UnsupportedBusNumberException {
    display = new LEDBackPack(addr, false);
  }

  public SevenSegment(int addr, boolean b) throws I2CFactory.UnsupportedBusNumberException {
    display = new LEDBackPack(addr, b);
  }

  /*
   * Sets a digit using the raw 16-bit value
   */
  public void writeDigitRaw(int charNumber, int value) throws IOException {
    if (charNumber > 7) {
      return;
    }
    // Set the appropriate digit
    this.display.setBufferRow(charNumber, value);
  }

  public void writeDigitRaw(int charNumber, String value) throws IOException {
    if (charNumber > 7) {
	    return;
    }
    if (value.trim().length() > 1) {
	    return;
    }
    // Set the appropriate digit
    int byteValue = ALL_CHARS.get(value);
    this.display.setBufferRow(charNumber, byteValue);
  }

  /*
   * Sets a single decimal or hexademical value (0..9 and A..F)
   */
  public void writeDigit(int charNumber, int value) throws IOException {
    writeDigit(charNumber, value, false);
  }

  public void writeDigit(int charNumber, int value, boolean dot) throws IOException {
    if (charNumber > 7) {
	    return;
    }
    if (value > 0xF) {
	    return;
    }
    // Set the appropriate digit
    this.display.setBufferRow(charNumber, digits[value] | (dot ? 0x01 << 7 : 0x00));
  }

  /*
   * Enables or disables the colon character
   */
  public void setColon() throws IOException {
    setColon(true);
  }

  public void setColon(boolean state) throws IOException {
    // Warning: This function assumes that the colon is character '2',
    // which is the case on 4 char displays, but may need to be modified
    // if another display type is used
    if (state) {
	    this.display.setBufferRow(2, 0xFFFF);
    } else {
	    this.display.setBufferRow(2, 0);
    }
  }

  public void clear() throws IOException {
    this.display.clear();
  }
}
