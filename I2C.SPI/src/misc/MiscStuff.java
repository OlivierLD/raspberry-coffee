package misc;

public class MiscStuff
{
  public static void main(String[] args)
  {
    int i = (0x32 >> 1);
    System.out.println("(0x32 >> 1) i:" + Integer.toString(i) + " 0#" + Integer.toBinaryString(i) + " 0x" + Integer.toHexString(i) + " 0" + Integer.toOctalString(i));      
    i = (0x3C >> 1);
    System.out.println("(0x3C >> 1) i:" + Integer.toString(i) + " 0#" + Integer.toBinaryString(i) + " 0x" + Integer.toHexString(i) + " 0" + Integer.toOctalString(i));      
    //
    int on = 4095;
    System.out.println(Integer.toString(on) + " = " + 
                       Integer.toString((on >> 8) & 0xFFFF) + " " + Integer.toString((on & 0xFF) & 0xFFFF) + " .. " +
                       "0x" + lpad(Integer.toHexString((on >> 8) & 0xFFFF), 2, "0") + " " + lpad(Integer.toHexString((on & 0xFF) & 0xFFFF), 2, "0"));
    on = 3024;
    System.out.println(Integer.toString(on) + " = " + 
                       Integer.toString((on >> 8) & 0xFFFF) + " " + Integer.toString((on & 0xFF) & 0xFFFF) + " .. " +
                       "0x" + lpad(Integer.toHexString((on >> 8) & 0xFFFF), 2, "0") + " " + lpad(Integer.toHexString((on & 0xFF) & 0xFFFF), 2, "0"));
    on = 256;
    System.out.println(Integer.toString(on) + " = " + 
                       Integer.toString((on >> 8) & 0xFFFF) + " " + Integer.toString((on & 0xFF) & 0xFFFF) + " .. " +
                       "0x" + lpad(Integer.toHexString((on >> 8) & 0xFFFF), 2, "0") + " " + lpad(Integer.toHexString((on & 0xFF) & 0xFFFF), 2, "0"));
    on = 255;
    System.out.println(Integer.toString(on) + " = " + 
                       Integer.toString((on >> 8) & 0xFFFF) + " " + Integer.toString((on & 0xFF) & 0xFFFF) + " .. " +
                       "0x" + lpad(Integer.toHexString((on >> 8) & 0xFFFF), 2, "0") + " " + lpad(Integer.toHexString((on & 0xFF) & 0xFFFF), 2, "0"));
  }
  
  private static String lpad(String str, int len, String with)
  {
    String s = str;
    while (s.length() < len)
      s = with + s;
    return s;
  }
}
