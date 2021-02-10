package oliv.resource;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class ResourceBundleHelper {
    // properties files live in the 'resources' folder.
    //                                Package            File Base name
    private static String baseName = "oliv.resource." + "rb"; // Optional suffix
    private static ResourceBundle resourceBundle;

    private ResourceBundleHelper() {
    }

    public static synchronized ResourceBundle getResourceBundleHelper() {
        if (resourceBundle == null) {
            try {
                resourceBundle = ResourceBundle.getBundle(baseName);
//              System.out.println("ResourceBundle created");
            } catch (MissingResourceException mre) {
                if (true) { // verbose of some sort...
                    System.err.println("Missing Resource: " + mre.getMessage());
                }
            }
//        } else {
//            System.out.println("ResourceBundle reused");
        }
        return resourceBundle;
    }

    public static String buildMessage(String id) {
        return buildMessage(id, null);
    }

    public static String buildMessage(String id, String[] data) {
        String mess = ResourceBundleHelper.getResourceBundleHelper().getString(id);
        for (int i = 0; data != null && i < data.length; i++) {
            String toReplace = String.format("{$%d}", (i + 1));
//            System.out.println("Replacing " + toReplace + " with " + data[i] + " in " + mess);
            mess = mess.replace(toReplace, data[i]);
//            mess = replaceString(mess, toReplace, data[i]);
        }
        return mess;
    }

}
