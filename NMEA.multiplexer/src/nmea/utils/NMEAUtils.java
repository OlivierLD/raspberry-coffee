package nmea.utils;

public class NMEAUtils
{
  public final static int ALL_IN_HEXA = 0;
  public final static int CR_NL       = 1;  
  
  public static String translateEscape(String str, int option)
  {
    String s = null;
    StringBuffer sb = new StringBuffer();
    for (int i=0; i<str.length(); i++)
    {
      if (option == CR_NL)
      {
        if (str.charAt(i) == (char)0x0A) // [NL], \n, [LF]
          sb.append("[LF]");
        else if (str.charAt(i) == (char)0x0D) // [CR], \r
          sb.append("[CR]");
        else
          sb.append(str.charAt(i));
      }
      else
      {
        String c = Integer.toHexString((int)str.charAt(i) & 0xFF).toUpperCase();  
        sb.append(lpad(c, 2, "0") + " ");
      }
    }    
    return sb.toString();
  }
  
  private final static String lpad(String s, int len, String pad)
  {
    String str = s;
    while (str.length() < len)
      str = pad + str;
    return str;
  }
  
  public static void main(String[] args)
  {
    String data = "Akeu CoucouA*FG\r\n";
    System.out.println(translateEscape(data, ALL_IN_HEXA));
    System.out.println(translateEscape(data, CR_NL));
  }
}
