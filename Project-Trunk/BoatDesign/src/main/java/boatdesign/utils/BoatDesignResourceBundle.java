package boatdesign.utils;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class BoatDesignResourceBundle {
    //                                Package               File Base name
    private final static String baseName = "boatdesign.utils." + "bd";
    private static ResourceBundle resourceBundle;

    private final static boolean BUNDLE_VERBOSE = "true".equals(System.getProperty("bundle-verbose"));

    private BoatDesignResourceBundle() {
    }

    public static synchronized ResourceBundle getBoatDesignResourceBundle() {
        if (BoatDesignResourceBundle.resourceBundle == null) {
            try {
                BoatDesignResourceBundle.resourceBundle = ResourceBundle.getBundle(baseName);
                if (BUNDLE_VERBOSE) {
                    System.out.println("ResourceBundle created");
                }
            } catch (MissingResourceException mre) {
                if (BUNDLE_VERBOSE) {
                    System.err.println("Missing Resource:" + mre.getMessage());
                }
            }
        } else if (BUNDLE_VERBOSE) {
            System.out.println("ResourceBundle reused");
        }
        return BoatDesignResourceBundle.resourceBundle;
    }

    public static String buildMessage(String id) {
        return buildMessage(id, null);
    }

    public static String buildMessage(String id, String[] data) {
        String mess = "";
        try {
            mess = BoatDesignResourceBundle.getBoatDesignResourceBundle().getString(id);
            for (int i = 0; data != null && i < data.length; i++) {
                String toReplace = String.format("{$%d}", (i + 1));
//            System.out.println("Replacing " + toReplace + " with " + data[i] + " in " + mess);
                mess = mess.replace(toReplace, data[i]);
//            mess = replaceString(mess, toReplace, data[i]);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return mess;
    }
}
