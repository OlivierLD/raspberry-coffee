// System resolution, with a GUI

// Sample, data
double[][] mat = new double[][] {
  { 12, 13, 14 },
  { 1.345, -654, 0.001 },
  { 23.09, 5.3, -12.34 }
};
double[] constants = new double[]{ 234, 98.87, 9.876 };

void setup() {
  
  size(640, 640);

  SquareMatrix sm = new SquareMatrix(3, true);
  
  sm.setmatrixElements(mat);

  println("Solving:");
  SystemUtil.printSystem(sm, constants);

  double[] result = SystemUtil.solveSystem(sm, constants);

  println(String.format("A = %f", result[0]));
  println(String.format("B = %f", result[1]));
  println(String.format("C = %f", result[2]));

}

void draw() {
  background(128);
}

void dispose() {
  println("Bye now.");
}
