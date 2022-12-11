package gpio.sensors.i2c.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Utils {

    private static final Logger LOG = LoggerFactory.getLogger(Utils.class);

    private Utils() {
    }

    public static float[] compensateDataBME280(byte[] data, int digT1, int digT2, int digT3,
                                               int digP1, int digP2, int digP3, int digP4, int digP5, int digP6, int digP7, int digP8, int digP9,
                                               int digH1, int digH2, int digH3, int digH4, int digH5, int digH6) {
        LOG.debug("compensateData: data={}", data);
        LOG.debug("compensateData: digT1={}, digT2={}, digT3={}", digT1, digT2, digT3);
        LOG.debug("compensateData: digP1={}, digP2={}, digP3={}, digP4={}, digP5={}, digP6={}, digP7={}, digP8={}, digP9={}",
                digP1, digP2, digP3, digP4, digP5, digP6, digP7, digP8, digP9);
        LOG.debug("compensateData: digH1={}, digH2={}, digH3={}, digH4={}, digH5={}, digH6={}",
                digH1, digH2, digH3, digH4, digH5, digH6);

        int adc_P = (((int)(data[0] & 0xff) << 16) + ((int)(data[1] & 0xff) << 8) + ((int)(data[2] & 0xff))) >> 4;
        int adc_T = (((int)(data[3] & 0xff) << 16) + ((int)(data[4] & 0xff) << 8) + ((int)(data[5] & 0xff))) >> 4;
        int adc_H = ((int)(data[6] & 0xff) << 8) + ((int)(data[7] & 0xff));

        // Temperature
        int varT1 = ((((adc_T >> 3) - (digT1 << 1))) * (digT2)) >> 11;
        int varT2 = (((((adc_T >> 4) - digT1) * ((adc_T >> 4) - digT1)) >> 12) * digT3) >> 14;
        int t_fine = varT1 + varT2;
        float temperature = ((t_fine * 5 + 128) >> 8) / 100F;

        // Pressure
        long varP1 = (long)t_fine - 128000;
        long varP2 = varP1 * varP1 * (long)digP6;
        varP2 += ((varP1 * (long)digP5) << 17);
        varP2 += (((long)digP4) << 35);
        varP1 = ((varP1 * varP1 * (long)digP3) >> 8) + ((varP1 * (long)digP2) << 12);
        varP1 = (((((long)1) << 47) + varP1)) * ((long)digP1) >> 33;

        float pressure;
        if (varP1 == 0) {
            pressure = 0F;
        } else {
            long p = 1048576 - adc_P;
            p = (((p << 31) - varP2) * 3125) / varP1;
            varP1 = (((long)digP9) * (p >> 13) * (p >> 13)) >> 25;
            varP2 = (((long)digP8) * p) >> 19;
            pressure = (((p + varP1 + varP2) >> 8) + (((long)digP7) << 4)) / 256F;
        }

        // Humidity
        int v_x1_u32r= t_fine - 76800;
        v_x1_u32r = (((((adc_H << 14) - (digH4 << 20) - (digH5 * v_x1_u32r)) +
                16384) >> 15) * (((((((v_x1_u32r * digH6) >> 10) * (((v_x1_u32r * digH3) >> 11) + 32768)) >> 10) +
                2097152) * digH2 + 8192) >> 14));
        v_x1_u32r -= (((((v_x1_u32r >> 15) * (v_x1_u32r >> 15)) >> 7) * digH1) >> 4);
        v_x1_u32r = (v_x1_u32r < 0) ? 0 : v_x1_u32r;
        v_x1_u32r = (v_x1_u32r > 419430400) ? 419430400 : v_x1_u32r;
        float humidity = (v_x1_u32r >> 12) / 1024F;

        float[] ret = new float[3];
        ret[0] = temperature;
        ret[1] = humidity;
        ret[2] = pressure;

        LOG.debug("compensateData: Calibrated temperature = {} C", temperature);
        LOG.debug("compensateData: Calibrated humidity = {} %", humidity);
        LOG.debug("compensateData: Calibrated pressure = {} Pa", pressure);

        return ret;
    }

    public static float compensateTemperatureBMP180(int ut, int calAC6, int calAC5, int calMC, int calMD) {
        LOG.debug("compensateTemperature: ut={}, calAC6={}, calAC5={}, calMC={}, calMD={}", ut, calAC6, calAC5, calMC, calMD);
        int x1 = ((ut - calAC6) * calAC5) >> 15;
        int x2 = (calMC << 11) / (x1 + calMD);
        int b5 = x1 + x2;
        float temp = ((b5 + 8) >> 4) / 10.0f;
        LOG.debug("compensateTemperature: Calibrated temperature = {} C", temp);
        return temp;
    }

    public static int getRawValue(int msb, int lsb, int xlsb) {
        LOG.debug("getRawValue: msb={} lsb={} xlsb={}", msb, lsb, xlsb);
        return (((msb << 8) + lsb) << 4) + (xlsb >> 4);
    }

    public static int getRawValue(int msb, int lsb) {
        LOG.debug("getRawValue: msb={} lsb={}", msb, lsb);
        return (msb << 8) + lsb;
    }

    public static int getSigned(int unsigned) {
        if (unsigned > 127) {
            unsigned -= 256;
        }
        return unsigned;
    }

    public static void waitFor(long howMuch) {
        try {
            Thread.sleep(howMuch);
        } catch (InterruptedException e) {
            LOG.error("Error: ", e);
        }
    }

    public static int signed16Bits(byte[] data, int offset) {
        int byte0 = data[offset] & 0xff;
        int byte1 = (int)data[offset + 1];
        return (byte1 << 8) + byte0;
    }

    public static int unsigned16Bits(byte[] data, int offset) {
        int byte0 = data[offset] & 0xff;
        int byte1 = data[offset + 1] & 0xff;
        return (byte1 << 8) + byte0;
    }

}
