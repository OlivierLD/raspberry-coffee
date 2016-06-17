package adafruit.io;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;

/**
 * Created by Nav on 6/16/2016.
 */
public class Base64Util
{
  /**
   * Decode string to image
   * @param imageString The string to decode
   * @return decoded image
   */
  public static BufferedImage decodeToImage(String imageString) {

    BufferedImage image = null;
    byte[] imageByte;
    try {
      BASE64Decoder decoder = new BASE64Decoder();
      imageByte = decoder.decodeBuffer(imageString);
      ByteArrayInputStream bis = new ByteArrayInputStream(imageByte);
      image = ImageIO.read(bis);
      bis.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
    return image;
  }

  /**
   * Encode image to string
   * @param image The image to encode
   * @param type jpeg, bmp, ...
   * @return encoded string
   */
  public static String encodeToString(BufferedImage image, String type) {
    String imageString = null;
    ByteArrayOutputStream bos = new ByteArrayOutputStream();

    try {
      ImageIO.write(image, type, bos);
      byte[] imageBytes = bos.toByteArray();

      BASE64Encoder encoder = new BASE64Encoder();
      imageString = encoder.encode(imageBytes);

      bos.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return imageString;
  }

  public static void main (String args[]) throws IOException
  {

    String IMG_RADIX = "flower";

    BufferedImage img = ImageIO.read(new File(IMG_RADIX + ".jpg"));
    BufferedImage newImg;
    String imgstr;
    imgstr = encodeToString(img, "jpg");
    System.out.println(imgstr);
    FileOutputStream fos = new FileOutputStream("image.txt");
    fos.write(imgstr.getBytes());
    fos.close();
    newImg = decodeToImage(imgstr);
    ImageIO.write(newImg, "png", new File(IMG_RADIX + ".png"));
  }
}
