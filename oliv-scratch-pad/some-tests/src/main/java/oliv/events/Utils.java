package oliv.events;

public class Utils {
    public static String rpad(String s, int len) {
        return rpad(s, len, " ");
    }

    public static String rpad(String s, int len, String pad) {
        String str = s;
        while (str.length() < len) {
            str += pad;
        }
        return str;
    }
}
