/*
 * Sends data to the SSD1306. The valkue sent to the screen is displayed as text in the Processing Frame.
 * Using Sketch > Add File..., select I2C.SPI/build/libs/I2C-SPI-1.0-all.jar
 */

// import lcd.oled.SSD1306; // Not mandatory in Processing.

int value;

final int NB_LINES = 32;
final int NB_COLS = 128;

final int BLACK = 0;
final int WHITE = 255;

final ScreenBuffer.Mode SCREEN_FLAVOR = ScreenBuffer.Mode.WHITE_ON_BLACK;

SSD1306 oled;
ScreenBuffer sb;

void setup() {
  frameRate(4); // fps. Default is 60. Slow down to 4, to be able to read.
  size(200, 150); // (WIDTH, HEIGHT);
  stroke(BLACK);
  noFill();
  textSize(72);

  try {
    oled = new SSD1306(SSD1306.SSD1306_I2C_ADDRESS);
    oled.begin();
    oled.clear();
  } catch (Exception ex) {
    oled = null;
    println("Cannot find the device, moving on without it.");
  }
}

void draw() {
  background(BLACK);
  fill(WHITE);
  value = (int)Math.floor(1023 * Math.random());
  text(String.format("%04d", value), 10, 100);

  if (sb == null) {
    sb = new ScreenBuffer(NB_COLS, NB_LINES);
  }
  sb.clear(SCREEN_FLAVOR);

  String text = String.format("- %04d -", value);
  int fontFactor = 3;
  int len = sb.strlen(text) * fontFactor;
  sb.text(text, 62 - (len / 2), 11, fontFactor, SCREEN_FLAVOR);

  if (oled != null) {
    oled.setBuffer(sb.getScreenBuffer());
    oled.display();
  }
}

void dispose() {
  if (oled != null) {
    sb.clear();
    oled.clear(); // Blank screen
    oled.setBuffer(sb.getScreenBuffer());
    oled.display();
    oled.shutdown();
  }
  println("Bye!");
}
