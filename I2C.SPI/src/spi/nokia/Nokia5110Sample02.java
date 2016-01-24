package spi.nokia;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import spi.oled.ScreenBuffer;

public class Nokia5110Sample02
{
  private static boolean go = true;
  
  public static void main(String[] args)
  {
    NumberFormat NF = new DecimalFormat("00.00");
    final Nokia5110 lcd = new Nokia5110();
    lcd.begin();

    Runtime.getRuntime().addShutdownHook(new Thread()
                                         {
                                           public void run()
                                           {
                                             lcd.shutdown();  
                                             System.out.println("\nExiting");
                                             go = false;
                                           }
                                         });

    ScreenBuffer sb = new ScreenBuffer(84, 48);
    sb.clear(ScreenBuffer.Mode.BLACK_ON_WHITE);
    while (go)
    {
      sb.clear(ScreenBuffer.Mode.WHITE_ON_BLACK);
      sb.text("BSP", 2, 9, ScreenBuffer.Mode.WHITE_ON_BLACK);
      double bsp = Math.random() * 10.0;
      String speed = NF.format(bsp);
      sb.text(speed, 2, 19, 2, ScreenBuffer.Mode.WHITE_ON_BLACK);
      lcd.setScreenBuffer(sb.getScreenBuffer());
      lcd.display();
    }
  }
}
