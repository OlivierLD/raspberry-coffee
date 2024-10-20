import raspiradar.RasPiJNIRadar;
import java.util.function.Consumer;
import java.util.Map;

/**
 * Warning:
 * Imported libraries must be compiled with 'sourceCompatibility = 1.8'
 * Note: JNI libs (*.so) can be added using "Sketch > Add File..." as well, no need to mess with the -Djava.library.path.
 *
 * See raspiradar.RasPiJNIRadar
 */

RasPiJNIRadar radar = null;
Map<Integer, Double> echos = new HashMap<Integer, Double>(181);

// Processing does not support lambdas (yet)...
class DataConsumer implements Consumer<RasPiJNIRadar.DirectionAndRange> {
  void accept(RasPiJNIRadar.DirectionAndRange data) {
    // Build a map of echos here
    println(String.format("Processing >> Bearing %s%02d, distance %.02f m", (data.direction() < 0 ? "-" : "+"), Math.abs(data.direction()), data.range()));
    echos.put(data.direction(), data.range());
  }
}

Consumer<RasPiJNIRadar.DirectionAndRange> dataConsumer = new DataConsumer();

int inc = 1;
int bearing = 0;
double dist = 0;
int hitExtremity = 0;

long delay = 100L;

color bgcolor = color (0, 0, 0);
color gridcolor = color (0, 0, 0);
color sweepercolor = color (102, 250, 81);

void setup() {
  size(960, 480);
  try {
   radar = new RasPiJNIRadar(false, 15);
   radar.setDataConsumer(dataConsumer);
  } catch (Throwable ex) {
    ex.printStackTrace();
    println(String.format("LibPath: [%s]", System.getProperty("java.library.path")));
  }
  frameRate(20f); // 10 per second
}

void draw() {
  try {
    Thread bearingThread = new Thread() {
      public void run() {
        radar.setAngle(bearing);
      }
    };
    bearingThread.start();
    // Measure distance here, broadcast it with bearing.
    dist = radar.readDistance();
    // Consumer
    radar.consumeData(new RasPiJNIRadar.DirectionAndRange(bearing, dist));

    bearing += inc;
    if (bearing > 90 || bearing < -90) { // then flip
      hitExtremity += 1;
      inc *= -1;
      bearing += (2 * inc);
    }
  } catch (Exception ex) {
    ex.printStackTrace();
  }
  background(bgcolor);
  grid();
  sweeper();
  circle();
  for (Integer key : echos.keySet()) {
    double range = echos.get(key);
    double scale = (height / 0.1); // m to 10cm
    int x = (int)(Math.round(range * Math.cos(Math.toRadians(key + 90)) * scale));
    int y = (int)(Math.round(range * Math.sin(Math.toRadians(key + 90)) * scale));
    plotEcho((width / 2) + x, height - y);
  }
  textSize(16);
  fill(255);
  text(String.format("%s%02d\272, range %.02f m", (bearing < 0 ? "-" : "+"), Math.abs(bearing), dist), 10, 20);
}

// Fill the circle
void circle(){
  fill(color(102, 250, 81, 60));
  ellipse(width/2, height, width, 2 * height);
}

void grid(){
  stroke(color(250, 247, 247, 50)); // color(250, 247, 247, 50) = #faf7f7, .5
  strokeWeight(2);
  line(width/2, height, width/2, 0);       // verticlal axis
  line(0, height - 1, width, height - 1);  // horizontal axis
  strokeWeight(1);
  noFill();
  for (int i = 1; i <=10; i++) {
    ellipse(width/2, height, i * (width / 10), i * (2 * height / 10));
  }
}

void sweeper(){
  float beam = (float)Math.toRadians(bearing + 90); // map(millis(), 0, 2000, 0, PI);
  strokeWeight(7);
  float f = 0.01;
  for (int i=38; i>=1; i--) {
    stroke(sweepercolor, 2*i);
    line(width/2, height, (width/2 + cos(beam - (f / 2)) * (height * 0.98)), (height - sin(beam - (f / 2)) * (height * 0.98)));
    f += 0.01;
  }
}

void plotEcho(int x, int y){
  ellipse(x, y, 10, 10);
}

void dispose() {
  if (radar != null) {
    radar.stop();
    radar.free();
  }
  println("Bye now");
  println(String.format("... was running from [%s]", System.getProperty("user.dir")));
}
