package camera;

public class SnapShot
{
  // Snaspshot with the Raspberry Pi camera board
//private final static String SNAPSHOT_COMMAND = "raspistill -rot 180 --width 200 --height 150 --timeout 1 --output snap" + i + ".jpg --nopreview";

  // For a webcam
  // Requires sudo apt-get install fswebcam
  // See http://www.raspberrypi.org/documentation/usage/webcams/ for some doc.
//private final static String SNAPSHOT_COMMAND = "fswebcam snap" + i + ".jpg";

  // Slow motion:
  private final static String SNAPSHOT_COMMAND = "raspivid -w 640 -h 480 -fps 90 -t 30000 -o vid.h264";

  public static void main(String... args) throws Exception
  {
    Runtime rt = Runtime.getRuntime();
    for (int i=0; i<10; i++)
    {
      long before = System.currentTimeMillis();
      Process snap = rt.exec("fswebcam snap" + i + ".jpg");
      snap.waitFor(); // Sync
      long after = System.currentTimeMillis();
      System.out.println("Snapshot #" + i + " done in " + Long.toString(after - before) + " ms.");
      // Detect brightest spot here
      // TODO Analyze image here. Determine brightest color. => findSpot
    }
  }
}
