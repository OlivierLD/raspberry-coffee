package raspisamples.adc;

import adc.ADCContext;
import adc.ADCListener;
import adc.ADCObserver;

/**
 * A two-channel listener
 * An example, for inspiration.
 */
public class JoyStick
{
  private static ADCObserver.MCP3008_input_channels channel[] = null;
  private final int[] channelValues = new int[] { 0, 0 }; // (0..100)
  
  private JoyStickClient joyStickClient = null;
  private int prevUDValue = 0, prevLRValue = 0;
  
  public JoyStick(JoyStickClient jsc) throws Exception
  {
    System.out.println(">> Channel MCP3008 #0: Up-Down");
    System.out.println(">> Channel MCP3008 #1: Left-Right");

    joyStickClient = jsc;
    channel = new ADCObserver.MCP3008_input_channels[] 
    {
      ADCObserver.MCP3008_input_channels.CH0, // UD
      ADCObserver.MCP3008_input_channels.CH1  // LR
    };
    final ADCObserver obs = new ADCObserver(channel);
    
    ADCContext.getInstance().addListener(new ADCListener()
       {
         @Override
         public void valueUpdated(ADCObserver.MCP3008_input_channels inputChannel, int newValue) 
         {
           int ch = inputChannel.ch();
           int volume = (int)(newValue / 10.23); // [0, 1023] ~ [0x0000, 0x03FF] ~ [0&0, 0&1111111111]
           if ("true".equals(System.getProperty("verbose", "false")))
             System.out.println("\tServo channel:" + ch + ", value " + newValue + ", vol. " + volume + " %.");
           
           channelValues[ch] = volume;
           if (ch == channel[0].ch() && volume != prevUDValue)
             joyStickClient.setUD(volume);
           if (ch == channel[1].ch() && volume != prevLRValue)
             joyStickClient.setLR(volume);
         }
       });
    obs.start();         
    
    Runtime.getRuntime().addShutdownHook(new Thread()
       {
         public void run()
         {
           if (obs != null)
             obs.stop();
         }
       });    
  }
}
