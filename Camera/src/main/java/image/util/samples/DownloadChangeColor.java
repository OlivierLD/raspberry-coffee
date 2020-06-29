package image.util.samples;

import images.ImageHTTPClient;
import images.ImageUtil;

import java.awt.Color;
import java.awt.Desktop;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.text.NumberFormat;

public class DownloadChangeColor {

    public static void main(String... args)
            throws Exception {
        final String FAX_NAME_1 = "http://www.opc.ncep.noaa.gov/P_sfc_full_ocean.gif";

        final String IMG_NAME_2 = "NOAA_sfc_1.png";
        final String IMG_NAME_3 = "NOAA_sfc_2.png";

        long before = System.currentTimeMillis();
        Image fax = ImageHTTPClient.getFax(FAX_NAME_1, "web/fax.png");
        BufferedImage bimg = ImageUtil.toBufferedImage(fax);
        long after = System.currentTimeMillis();
        long reading = after - before;
        NumberFormat nf = NumberFormat.getInstance();
        System.out.println(String.format("Downloaded in %s ms", nf.format(reading)));

        int width = bimg.getWidth();
        int height = bimg.getHeight();
        System.out.println("Image size is " + width + " x " + height);

        // Transparent blue, sharpen
        BufferedImage bimg2 = ImageUtil.switchColorAndMakeColorTransparent(bimg, Color.black, Color.blue, Color.white, ImageUtil.SHARPEN);
        ImageUtil.writeImageToFile(bimg2, "png", "web" + File.separator + IMG_NAME_2);

        // Transparent red, blur
        bimg2 = ImageUtil.switchColorAndMakeColorTransparent(bimg, Color.black, Color.red, Color.white, ImageUtil.BLUR);
        ImageUtil.writeImageToFile(bimg2, "png", "web" + File.separator + IMG_NAME_3);

        // Transform template
        BufferedReader br = new BufferedReader(new FileReader("web" + File.separator + "template.html"));
        BufferedWriter bw = new BufferedWriter(new FileWriter("web" + File.separator + "index.html"));
        String line = "";
        while (line != null) {
            line = br.readLine();
            if (line != null) {
                if (line.contains("<FAX_1>")) {
                    line = line.replace("<FAX_1>", "fax.png");
                }
                if (line.contains("<FAX_2>")) {
                    line = line.replace("<FAX_2>", IMG_NAME_2);
                }
                if (line.contains("<FAX_3>")) {
                    line = line.replace("<FAX_3>", IMG_NAME_3);
                }
                bw.write(line + "\n");
            }
        }
        br.close();
        bw.close();

        Desktop.getDesktop().browse(new File("./web/index.html").toURI());
    }
}
