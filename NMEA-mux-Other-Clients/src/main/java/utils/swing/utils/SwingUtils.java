package utils.swing.utils;

import java.awt.Font;
import java.awt.FontFormatException;
import java.io.IOException;
import java.io.InputStream;

public class SwingUtils {
    public static Font tryToLoadFont(String fontName, Object parent) {
        // final String RESOURCE_PATH = "resources" + "/"; // A slash! Not File.Separator, it is a URL.
        try {
            String fontRes = /*RESOURCE_PATH +*/ fontName;
            InputStream fontDef = null;
            if (parent != null) {
                fontDef = parent.getClass().getClassLoader().getResourceAsStream(fontRes);
            } else {
                fontDef = SwingUtils.class.getClassLoader().getResourceAsStream(fontRes);
            }
            if (fontDef == null) {
                throw new NullPointerException("Could not find font resource \"" + fontName +
                        "\"\n\t\tin \"" + fontRes +
                        "\"\n\t\tfor \"" + parent.getClass().getName() +
                        "\"\n\t\ttry: " + parent.getClass().getResource(fontRes));
            } else {
                return Font.createFont(Font.TRUETYPE_FONT, fontDef);
            }
        } catch (FontFormatException e) {
            System.err.println("getting font " + fontName);
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("getting font " + fontName);
            e.printStackTrace();
        }
        return null;
    }
}
