package sevensegdisplay.samples;

import java.io.IOException;

import sevensegdisplay.SevenSegment;

public class OnOffSample
{
  public static void main(String[] args) throws IOException
  {
    SevenSegment segment = new SevenSegment(0x70, true);

    for (int i=0; i<5; i++)
    {
      // Notice the digit index: 0, 1, 3, 4. 2 is the column ":"
      segment.writeDigit(0, 8, true);
      segment.writeDigit(1, 8, true);
      segment.writeDigit(3, 8, true);
      segment.writeDigit(4, 8, true);
      segment.setColon();
      try { Thread.sleep(1000L); } catch (InterruptedException ie){}
      segment.clear();
      try { Thread.sleep(1000L); } catch (InterruptedException ie){}
    }
  }
}
