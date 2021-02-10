package oliv.resource;

import java.util.Locale;

public class ResourceBundle {
    public static void main(String... args) {
        // -Duser.country=US -Duser.language=en
//        System.setProperty("user.country", "FR");
//        System.setProperty("user.language", "fr");

//        Locale.setDefault(new Locale("fr", "FR"));

        try {
            System.out.println(ResourceBundleHelper.buildMessage("title"));
        } catch (NullPointerException npe) {
            System.out.println("Look at that!");
        }
    }
}
