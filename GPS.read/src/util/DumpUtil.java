package util;

public class DumpUtil
{
  private final static int LINE_LEN = 16;

  public static String[] dualDump(String str)
  {
    byte[] ba = str.getBytes();
    String[] result = null;
    int dim = ba.length / LINE_LEN;
    result = new String[dim + 1];
    for (int l=0; l<(dim + 1); l++)
    {
      String lineLeft  = "";
      String lineRight = "";
      int start = l * LINE_LEN;
      for (int c=start; c<Math.min(start + LINE_LEN - 1, ba.length); c++)
      {
        lineLeft  += (lpad(Integer.toHexString(ba[c]).toUpperCase(), 2, "0") + " ");
        lineRight += (isAsciiPrintable(str.charAt(c)) ? str.charAt(c) : ".");
      }
      lineLeft = rpad(lineLeft, 3 * LINE_LEN, " ");
      result[l] = lineLeft + "    " + lineRight;
    }
    return result;
  }

  /**
   * Might not work with some encodings...
   * @param ch
   * @return
   */
  public static boolean isAsciiPrintable(char ch)
  {
    return ch >= 32 && ch < 127;
  }

  private static String lpad(String s, int len, String with)
  {
    while (s.length() < len)
      s = with + s;
    return s;
  }

  private static String rpad(String s, int len, String with)
  {
    while (s.length() < len)
      s += with;
    return s;
  }

  public static void main(String[] args)
  {
    String s = "ABCDEFGHIJKLMNO\r\r\n\000PQakeu\000coucou!$%&&*^#";

    String[] sa = dualDump(s);
    for (String str : sa)
      System.out.println(str);

    System.out.println("-----------");
    String mess = "+CMTI: \"ME\",75\r\n";
    System.out.println(mess.substring(mess.lastIndexOf(",") + 1, mess.lastIndexOf("\r\n")));
  }
}
