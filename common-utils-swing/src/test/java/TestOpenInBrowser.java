import utils.SwingStaticUtil;

public class TestOpenInBrowser {

    private final static String URL_TO_OPEN = "http://olivierld.github.io";

    public static void main(String... args) {
        try {
            SwingStaticUtil.openInBrowser(URL_TO_OPEN);
            System.out.println("You should see it, now.");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
