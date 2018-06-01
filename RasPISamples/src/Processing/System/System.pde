// System resolution, with a GUI

import java.util.List;
import java.util.ArrayList;

int REQUITED_SMOOTHING_DEGREE = 2;

static class Point {
  int x;
  int y;
  public Point(int x, int y) {
    this.x = x;
    this.y = y;
  }
}

List<Point> points = new ArrayList<Point>();
double [] coeffs = null;

final int WHITE = 255;
final int BLACK =   0;
final int BLUE = color(0, 0, 255);
final int RED = color(255, 0, 0);

final int BUTTON_COLOR = BLACK;
final int BUTTON_HIGHLIGHT = color(128);
final int BUTTON_CLICKED = color(200);

final String BUTTON_RESOLVE_LABEL = "Resolve";
final String BUTTON_CLEAR_LABEL   = "Clear";

int buttonResolveColor = BUTTON_COLOR;
int buttonClearColor = BUTTON_COLOR;

final int buttonFontSize = 14;
final int buttonTextPadding = 3;
final int buttonMargin = 5;

final int buttonResolvePosX = 10;
final int buttonResolvePosY = 10;
final int buttonResolveHeight = buttonFontSize + (2 * buttonTextPadding);
int buttonResolveWidth;

int buttonClearPosX, buttonClearWidth;
final int buttonClearPosY = 10;
final int buttonClearHeight = buttonFontSize + (2 * buttonTextPadding);

boolean buttonResolveOver = false;
boolean buttonClearOver = false;

void setup() {
  
  size(640, 640);

}

void draw() {
  background(WHITE);
  fill(BLACK);
  text("Drag the mouse to spray points, then click [Resolve]. Degree is " + String.valueOf(REQUITED_SMOOTHING_DEGREE), 10, height - 20); 
  
  // Points
  if (points.size() > 0) {
    stroke(BLUE);
    for (Point pt : points) { // No Java 8 :( No stream().forEach()
      point(pt.x, pt.y);
    }
  }
  // Curve?
  if (coeffs != null) {
    stroke(RED);
    Point prevPt = null;
    for (int x=0; x<width; x++) {
      int y = (int)func(x, coeffs);
      if (prevPt != null) {
        line(prevPt.x, prevPt.y, x, y);
      }
      prevPt = new Point(x, y);
    }
  }
  
  // Button states
  update(mouseX, mouseY);
  
  // Resolve button
  fill(buttonResolveColor);
  buttonResolveWidth = (int)(textWidth(BUTTON_RESOLVE_LABEL) + (2 * buttonTextPadding));
  rect(buttonResolvePosX, buttonResolvePosY, buttonResolveWidth, buttonResolveHeight);
  textSize(buttonFontSize);
  fill(WHITE);
  text(BUTTON_RESOLVE_LABEL, buttonResolvePosX + buttonTextPadding, buttonResolvePosY + buttonFontSize + buttonTextPadding);
  // Button Clear
  fill(buttonClearColor);
  buttonClearWidth = (int)(textWidth(BUTTON_CLEAR_LABEL) + (2 * buttonTextPadding));
  buttonClearPosX = buttonResolvePosX + buttonResolveWidth + buttonMargin;
  rect(buttonClearPosX, buttonClearPosY, buttonClearWidth, buttonClearHeight);
  textSize(buttonFontSize);
  fill(WHITE);
  text(BUTTON_CLEAR_LABEL, buttonClearPosX + buttonTextPadding, buttonClearPosY + buttonFontSize + buttonTextPadding);
}

void dispose() {
  println("Bye now.");
}

void update(int x, int y) {
  if ( overResolveButton(buttonResolvePosX, buttonResolvePosY, buttonResolveWidth, buttonResolveHeight) ) {
    buttonResolveOver = true;
  } else {
    buttonResolveOver = false;
  }

  if ( overClearButton(buttonClearPosX, buttonClearPosY, buttonClearWidth, buttonClearHeight) ) {
    buttonClearOver = true;
  } else {
    buttonClearOver = false;
  }
}

boolean overResolveButton(int x, int y, int width, int height)  {
  if (mouseX >= x && mouseX <= (x + width) && 
      mouseY >= y && mouseY <= (y + height)) {
    buttonResolveColor = BUTTON_HIGHLIGHT;
    return true;
  } else {
    buttonResolveColor = BUTTON_COLOR;
    return false;
  }
}

boolean overClearButton(int x, int y, int width, int height)  {
  if (mouseX >= x && mouseX <= (x + width) && 
      mouseY >= y && mouseY <= (y + height)) {
    buttonClearColor = BUTTON_HIGHLIGHT;
    return true;
  } else {
    buttonClearColor = BUTTON_COLOR;
    return false;
  }
}
void mousePressed() {
  if (buttonResolveOver) { // Resolution
 // solveSystem();
    smooth();
  } else if (buttonClearOver) { // Clear
    println("Clear!");
    points = new ArrayList<Point>();
    coeffs = null;
  } else { // More here... like dropping points on canvas
    points.add(new Point(mouseX, mouseY));
    println(String.format("Now %d point(s) in the buffer", points.size()));
  }
}

void mouseDragged() {
  points.add(new Point(mouseX, mouseY));
  println(String.format("Now %d point(s) in the buffer", points.size()));
}

static double func(double x, double[] coeff) {
  double d = 0;
  int len = coeff.length;
  for (int i=0; i<len; i++) {
    d += (coeff[i] * Math.pow(x, (len - 1 - i))); 
  }
  return d;
}
/**
 * For details on the least squares method:
 * See http://www.efunda.com/math/leastsquares/leastsquares.cfm
 * See http://www.lediouris.net/original/sailing/PolarCO2/index.html
 */
void smooth() {
  int dimension = REQUITED_SMOOTHING_DEGREE + 1;
  double[] sumXArray = new double[(REQUITED_SMOOTHING_DEGREE * 2) + 1]; // Will fill the matrix
  double[] sumY      = new double[REQUITED_SMOOTHING_DEGREE + 1];
  // Init
  for (int i=0; i<((REQUITED_SMOOTHING_DEGREE * 2) + 1); i++) {
    sumXArray[i] = 0.0;
  }
  for (int i=0; i<(REQUITED_SMOOTHING_DEGREE + 1); i++) {
    sumY[i] = 0.0;
  }
  for (Point pt : points) {
    for (int i=0; i<((REQUITED_SMOOTHING_DEGREE * 2) + 1); i++) {
      sumXArray[i] += Math.pow(pt.x, i);
    }
    for (int i=0; i<(REQUITED_SMOOTHING_DEGREE + 1); i++) {
      sumY[i] += (pt.y * Math.pow(pt.x, i));
    }
  }
  // Fill the matrix
  SquareMatrix squareMatrix = new SquareMatrix(dimension);
  for (int row=0; row<dimension; row++) {
    for (int col=0; col<dimension; col++) {
      int powerRnk = (REQUITED_SMOOTHING_DEGREE - row) + (REQUITED_SMOOTHING_DEGREE - col);
      println("[" + row + "," + col + ":" + (powerRnk) + "] = " + sumXArray[powerRnk]);
      squareMatrix.setElementAt(row, col, sumXArray[powerRnk]);
    }
  }
  // System coeffs
  double[] constants = new double[dimension];
  for (int i=0; i<dimension; i++) {
    constants[i] = sumY[REQUITED_SMOOTHING_DEGREE - i];
    println("[" + (REQUITED_SMOOTHING_DEGREE - i) + "] = " + constants[i]);
  }
  // Resolution
  println("Resolving:");
  SystemUtil.printSystem(squareMatrix, constants);
  println();

  double[] result = SystemUtil.solveSystem(squareMatrix, constants);
  String out = "[ ";
  for (int i=0; i<result.length; i++) {
    out += String.format("%s%f", (i > 0 ? ", " : ""), result[i]);
  }
  out += " ]";
  println(out);
  coeffs = result; // For the drawing
}
