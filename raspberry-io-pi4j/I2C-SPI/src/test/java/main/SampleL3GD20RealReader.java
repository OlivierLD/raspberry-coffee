package main;

import i2c.sensor.L3GD20;
import i2c.sensor.listener.SensorL3GD20Context;
import i2c.sensor.utils.L3GD20Dictionaries;

/*
 * Read real data,
 * and broadcast to a listener
 */
public class SampleL3GD20RealReader {
    private boolean go = true;
    private L3GD20 sensor;
    private double refX = 0, refY = 0, refZ = 0;

    public SampleL3GD20RealReader() throws Exception {
        sensor = new L3GD20();
        sensor.setPowerMode(L3GD20Dictionaries.NORMAL);
        sensor.setFullScaleValue(L3GD20Dictionaries._250_DPS);
        sensor.setAxisXEnabled(true);
        sensor.setAxisYEnabled(true);
        sensor.setAxisZEnabled(true);

        sensor.init();
        sensor.calibrate();
    }

    private final static int MIN_MOVE = 10;

    public void start() throws Exception {
        long wait = 20L;
        double x = 0, y = 0, z = 0;
        while (go) {
            double[] data = sensor.getCalOutValue();
            x = data[0];
            y = data[1];
            z = data[2];
            // Broadcast if needed
            if (Math.abs(x - refX) > MIN_MOVE || Math.abs(y - refY) > MIN_MOVE || Math.abs(z - refZ) > MIN_MOVE) {
//      System.out.println("X:" + refX + " -> " + x);
//      System.out.println("Y:" + refY + " -> " + y);
//      System.out.println("Z:" + refZ + " -> " + z);
                refX = x;
                refY = y;
                refZ = z;
                SensorL3GD20Context.getInstance().fireMotionDetected(x, y, z);
            }
//    System.out.printf("X:%.2f, Y:%.2f, Z:%.2f%n", x, y, z);
            try {
                Thread.sleep(wait);
            } catch (InterruptedException ex) {
            }
        }
    }

    public void stop() {
        this.go = false;
    }
}
