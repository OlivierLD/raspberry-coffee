import java.util.function.Consumer;
import java.util.Map;

boolean WITH_BEEP = false;

public static class DirectionAndRange {
  double range;
  int direction;

  public DirectionAndRange() { }
  public DirectionAndRange(int direction, double range) {
    this.direction = direction;
    this.range = range;
  }
  public DirectionAndRange direction(int direction) {
    this.direction = direction;
    return this;
  }
  public DirectionAndRange range(double range) {
    this.range = range;
    return this;
  }
  public int direction() {
    return this.direction;
  }
  public double range() {
    return this.range;
  }
}

Map<Integer, Double> echos = new HashMap<Integer, Double>(181);

// Processing does not support lambdas (yet)...
class DataConsumer implements Consumer<DirectionAndRange> {
  void accept(DirectionAndRange data) {
    // Build a map of echos here
//  println(String.format("Processing >> Bearing %s%02d, distance %.02f cm", (data.direction() < 0 ? "-" : "+"), Math.abs(data.direction()), data.range()));
    echos.put(data.direction(), data.range());
  }
}

Consumer<DirectionAndRange> dataConsumer = new DataConsumer();

int inc = 1;
int bearing = 0;
double dist = 0;
int hitExtremity = 0;

color bgcolor = color (0, 0, 0);
color gridcolor = color (0, 0, 0);
color sweepercolor = color (102, 250, 81);

void setup() {

  println(String.format("Running from [%s]", System.getProperty("user.dir")));

  size(960, 480);
  frameRate(20f); // 20 per second
}

double range = 100D;
Double simulateUserRange() {
  double inc = Math.random();
  int sign = System.nanoTime() % 2 == 0 ? +1 : -1;
  range += (sign * inc);
  if (range < 0 || range > 100) {
    range -= (2 * (sign * inc));
  }
  return range;
}

void draw() {
  try {
    // 2 separate threads for setting the angle and reading the distance
    Thread setter = new Thread() {
      public void run() {
    //  radar.setAngle(bearing);
      }
    };
    setter.start();
    Thread getter = new Thread() {
      public void run() {
        // Measure distance here, broadcast it witdouble dist = h bearing.
        dist = simulateUserRange();
        // Consumer
        dataConsumer.accept(new DirectionAndRange(bearing, dist));
      }
    };
    getter.start();

    if (WITH_BEEP && bearing % 30 == 0) {
      java.awt.Toolkit.getDefaultToolkit().beep();
    }
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
  synchronized (echos) {
    for (Integer key : echos.keySet()) {
      double range = echos.get(key);
      double scale = (height / 100.0); // full radius: 100 cm
      int x = (int)(Math.round(range * Math.cos(Math.toRadians(key + 90)) * scale));
      int y = (int)(Math.round(range * Math.sin(Math.toRadians(key + 90)) * scale));
      plotEcho((width / 2) + x, height - y);
    }
  }
  textSize(16);
  fill(255);
  text(String.format("%s%02d\272, range %.02f cm", (bearing < 0 ? "-" : "+"), Math.abs(bearing), dist), 10, 20);
}

// Fill the circle
void circle(){
  fill(color(102, 250, 81, 60));
  ellipse(width/2, height, width, 2 * height);
}

void grid(){
  stroke(color(250, 247, 247, 50)); // color(250, 247, 247, 50) = #faf7f7, .5
  strokeWeight(2);
  line(width/2, height, width/2, 0);       // vertical axis
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
  println("Bye now");
  println(String.format("... was running from [%s]", System.getProperty("user.dir")));
}
