package client;

import java.util.HashMap;
import java.util.Map;

public class SpeechTools {
  private final static Map<String, String> speechTools = new HashMap<>();
  static{
    speechTools.put("Mac OS X", "say");
    speechTools.put("Linux", "espeak");
  }

  public static void speak(String text) {
    String speechTool = speechTools.get(System.getProperty("os.name"));
    if (speechTool == null) {
      throw new RuntimeException("No speech tool found in this os [" + System.getProperty("os.name") + "]");
    }
    try {
      Runtime.getRuntime().exec(new String[] { speechTool, text });
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  public static void main(String... args) {
    System.out.println("OS is [" + System.getProperty("os.name") + "]");
//	speak("You got a message from 415-745-5209. Do you wan to read it?");
    speak("Oh hello Pussycat, what's you doing up there?");
  }
}
