package rangesensor;

/**
 * Uses WiringPI, bridged with javah.
 * The pure Java implementation (see {@link StandaloneHC_SR04}.java) seems to have problems
 * with the nano seconds required here.
 */
public class JNI_HC_SR04 {

  /*
  Default pins, in the C code:

#define GPIO23     4
#define GPIO24     5

using namespace std;

static int trigger = GPIO23;
static int echo    = GPIO24;
   */
  public native void init();
  public native void init(int trigPin, int echoPin); // Uses the WiringPi numbers. See default above.
  public native double readRange();

  public static void main(String... args) {
    JNI_HC_SR04 jni_hc_sr04 = new JNI_HC_SR04();
    jni_hc_sr04.init(); // With default prms. See above.
    System.out.println("Initialized. Get closer than 5cm to stop.");
    boolean go = true;
    while (go) {
      double range = jni_hc_sr04.readRange(); // in meters.
      System.out.println(String.format("Distance is %.2f cm.", (range * 100)));
      go = (range * 100 > 5); // Stops when range is less than 5 cm.
    }
    System.out.println("Java is done, bye now.");
  }
  static {
    System.loadLibrary("OlivHCSR04");
  }
}
