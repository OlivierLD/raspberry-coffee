import utils.*;

/**
 * Add the required jar files using Sketch > Add File...
 */
void setup() {
  println("Starting...");
  println(String.format("LPad: %s", StringUtils.lpad("Small", 10, "X")));
  DumpUtil.displayDualDump("Hello Processing World!");
}