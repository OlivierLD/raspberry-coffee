package spi.lcd.nokia.samples;

import lcd.ScreenBuffer;
import spi.lcd.nokia.Nokia5110;

public class Nokia5110Sample
{
  public static void main(String[] args)
  {
    System.out.println("Starting");
    Nokia5110 lcd = new Nokia5110();
    lcd.begin();
    
    lcd.clear();
    lcd.display();
    System.out.println("Ready");

    lcd.setScreenBuffer(Nokia5110.ADAFRUIT_LOGO);
//  lcd.data(Nokia5110.ADAFRUIT_LOGO);
    System.out.println("Displaying...");
    lcd.display();
    System.out.println("Displayed");
    try { Thread.sleep(5_000L); } catch (Exception ex) { ex.printStackTrace(); }
    
    ScreenBuffer sb = new ScreenBuffer(84, 48);
    sb.clear(ScreenBuffer.Mode.BLACK_ON_WHITE);
    sb.text("Hello Nokia!",  5, 20, ScreenBuffer.Mode.BLACK_ON_WHITE);
    sb.text("I speak Java!", 5, 30, ScreenBuffer.Mode.BLACK_ON_WHITE);
    lcd.setScreenBuffer(sb.getScreenBuffer());
    lcd.display();
    try { Thread.sleep(2_000); } catch (Exception ex) {}

    sb.clear(ScreenBuffer.Mode.WHITE_ON_BLACK);
    sb.text("Hello Nokia!",  5, 20, ScreenBuffer.Mode.WHITE_ON_BLACK);
    sb.text("I speak Java!", 5, 30, ScreenBuffer.Mode.WHITE_ON_BLACK);
    lcd.setScreenBuffer(sb.getScreenBuffer());
    lcd.display();
    try { Thread.sleep(2_000); } catch (Exception ex) {}

    sb.clear();
    for (int i=0; i<8; i++)
    {
      sb.rectangle(1 + (i*2), 1 + (i*2), 83 - (i*2), 47 - (i*2));
//    lcd.setScreenBuffer(sb.getScreenBuffer());          
//    lcd.display();
  //  try { Thread.sleep(100); } catch (Exception ex) {}
    }
    lcd.setScreenBuffer(sb.getScreenBuffer());          
    lcd.display();
    try { Thread.sleep(1_000); } catch (Exception ex) {}

    sb.clear(ScreenBuffer.Mode.WHITE_ON_BLACK);
    sb.text("Pi=", 2, 9, ScreenBuffer.Mode.WHITE_ON_BLACK);
    sb.text("3.1415926", 2, 19, 2, ScreenBuffer.Mode.WHITE_ON_BLACK);
    lcd.setScreenBuffer(sb.getScreenBuffer());
    lcd.display();
//  sb.dumpScreen();
    try { Thread.sleep(5_000); } catch (Exception ex) {}

    sb.clear(ScreenBuffer.Mode.WHITE_ON_BLACK);
    sb.text("Pi=", 2, 9, ScreenBuffer.Mode.WHITE_ON_BLACK, true);
    sb.text("3.1415926", 2, 19, 2, ScreenBuffer.Mode.WHITE_ON_BLACK, true);
    lcd.setScreenBuffer(sb.getScreenBuffer());
    lcd.display();
    //  sb.dumpScreen();
    try { Thread.sleep(5_000); } catch (Exception ex) {}

    lcd.clear();
    lcd.display();
    lcd.shutdown();
    System.out.println("Done");
  }
}
