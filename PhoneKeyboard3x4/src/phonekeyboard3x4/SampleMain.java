package phonekeyboard3x4;

public class SampleMain
{
  @SuppressWarnings("oracle.jdeveloper.java.insufficient-catch-block")
  public static void main(String... args)
  {
    System.out.println("Hit the same key twice to exit");
    System.out.println("------------------------------");

    KeyboardController kbc = new KeyboardController();
    char prevchar = ' ';
    boolean go = true;
    while (go)
    {
      char c = kbc.getKey();
      System.out.println("At " + System.currentTimeMillis() + ", Char: " + c);
      if (c == prevchar)
        go = false;
      prevchar = c;
      try { Thread.sleep(200L); } catch (Exception ex) {}
    }
    System.out.println("Bye");
    kbc.shutdown();
  }
}
