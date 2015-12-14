package rangesensor;

public class JNI_HC_SR04
{
  public native void init();
  public native void init(int trigPin, int echoPin); // Uses the WiringPi numbers.
  public native double readRange();

  public static void main(String[] args) 
  {
    JNI_HC_SR04 jni = new JNI_HC_SR04();
    jni.init();
    boolean go = true;
    while (go)
    {
      double range = jni.readRange();
      System.out.println("Distance is " + (range * 100) + " cm");
      go = (range * 100 > 5);
    }
    System.out.println("Java is done.");
  }
  static 
  {
    System.loadLibrary("OlivHCSR04");
  }
}
