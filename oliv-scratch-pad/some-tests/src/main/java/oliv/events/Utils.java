package oliv.events;

import java.net.InetAddress;
import java.net.UnknownHostException;

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

    public static String getHostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            return null;
        }
    }
}
