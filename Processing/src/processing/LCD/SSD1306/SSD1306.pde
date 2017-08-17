/*
 * Emulates the SSD1306.
 * Using Sketch > Add File..., select I2C.SPI/build/libs/I2C.SPI-1.0-all.jar
 */
int value;

final int NB_LINES = 32;
final int NB_COLS = 128;

final int BLACK = 0;
final int WHITE = 255;
final int GRAY = 100;
final color RED = color(255, 0, 0);

final int WIDTH = 1280;
final int HEIGHT = 320;

final int CELL_SIZE = 10;

final ScreenBuffer.Mode SCREEN_FLAVOR = ScreenBuffer.Mode.WHITE_ON_BLACK;

ScreenBuffer sb;

void setup() {
  frameRate(4); // fps. Default is 60. Slow down to 4, to be able to read.
  initLeds();
  size(1280, 320); // (WIDTH, HEIGHT); 
  stroke(BLACK);
  noFill();
  textSize(72);  
}

void draw() {
  background(BLACK);
  fill(WHITE);
  value = (int)Math.floor(1023 * Math.random());  // Simulation
//text(String.format("%04d", value), 10, 100);
  
  stroke(GRAY);
  // Vertical grid
  for (int i=1; i<NB_COLS; i++) {
    int abs = i * (int)(WIDTH / NB_COLS);
    line(abs, 0, abs, HEIGHT);
  }
  // Horizontal grid
  for (int i=0; i<NB_LINES; i++) {
    int ord = i * (int)(HEIGHT / NB_LINES);
    line(0, ord, WIDTH, ord);
  }
  
  // Character display
  if (sb == null) {
    sb = new ScreenBuffer(NB_COLS, NB_LINES);
    sb.clear(SCREEN_FLAVOR);
  }

  // TODO: From user entry (keyboard?)
  boolean random = true;
  if (!random) {
    sb.text("ScreenBuffer", 2, 9, SCREEN_FLAVOR);
    sb.text(NB_COLS + " x " + NB_LINES + " for LCD", 2, 19, SCREEN_FLAVOR);
    sb.text("I speak Java!", 2, 29, SCREEN_FLAVOR);
  } else {
    String text = String.format("- %04d -", value);
    int fontFactor = 3;
    int len = sb.strlen(text) * fontFactor;
    sb.text(text, 62 - (len / 2), 11, fontFactor, SCREEN_FLAVOR);
  }
  
  this.setBuffer(sb.getScreenBuffer());
  this.display();
}

void dispose() {
  println("Bye!");
}

void display() {
  fill(RED);
  boolean[][] leds = getLedOnOff();
  for (int col=0; col<leds.length; col++) {
    for (int line=0; line<leds[col].length; line++) {
      if (leds[col][line]) {
        int x = (CELL_SIZE * col) + (CELL_SIZE / 2);
        int y = (CELL_SIZE * line) + (CELL_SIZE / 2);
        ellipse(x, y, 8, 8);
      }
    }
  }
}

private boolean[][] ledOnOff;

void setLedOnOff(boolean[][] ledOnOff) {
  this.ledOnOff = ledOnOff;
}

boolean[][] getLedOnOff() {
  return ledOnOff;
}

void initLeds() {
  ledOnOff = new boolean[NB_COLS][NB_LINES];
  for (int r = 0; r < NB_LINES; r++) {
    for (int c = 0; c < NB_COLS; c++)
      ledOnOff[c][r] = false;
  }
}

void setBuffer(int[] screenbuffer) {
  // This displays the buffer top to bottom, instead of left to right
  char[][] screenMatrix = new char[NB_LINES][NB_COLS];
  for (int i = 0; i < NB_COLS; i++) {
    // Line is a vertical line, its length is NB_LINES / 8
    String line = ""; 
    for (int l = (NB_LINES / 8) - 1; l >= 0; l--) {
      line += StringUtils.lpad(Integer.toBinaryString(screenbuffer[i + (l * NB_COLS)]), 8, "0").replace('0', ' ').replace('1', 'X');
    }
//  println(line);

    for (int c = 0; c < line.length(); c++) {
      try {
        char mc = line.charAt(c);
        screenMatrix[c][i] = mc;
      } catch (Exception ex) {
        println("Line:" + line + " (" + line.length() + " character(s))");
        ex.printStackTrace();
      }
    }
  }
  // Display the screen matrix, as it should be seen
  boolean[][] matrix = this.getLedOnOff();
  for (int i = 0; i < NB_LINES; i++) {
    for (int j = 0; j < NB_COLS; j++) {
      matrix[j][NB_LINES - 1 - i] = (screenMatrix[i][j] == 'X' ? true : false);
    }
  }
  this.setLedOnOff(matrix);
}