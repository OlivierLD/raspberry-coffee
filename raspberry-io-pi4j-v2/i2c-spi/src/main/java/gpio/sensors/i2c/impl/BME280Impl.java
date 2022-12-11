package gpio.sensors.i2c.impl;

import com.pi4j.context.Context;
import com.pi4j.io.i2c.I2C;
import com.pi4j.io.i2c.I2CConfig;
import com.pi4j.io.i2c.I2CProvider;
import gpio.sensors.i2c.BME280;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static gpio.sensors.i2c.impl.Utils.*;

/**
 * Inspired by: https://github.com/s5uishida/bme280-driver/blob/master/src/io/github/s5uishida/iot/device/bme280/driver/BME280Driver.java
 */
public class BME280Impl implements BME280 {

    private static final Logger LOG = LoggerFactory.getLogger(BME280Impl.class);

    public static final int BME280_ADDRESS = 0x76;

    private final static String I2C_PROVIDER = System.getProperty("i2c-provider", "linuxfs-i2c");

    private static final int ID_REGISTER     = 0xD0;
    private static final int RESET_REGISTER  = 0xE0;
    private static final int STATUS_REGISTER = 0xF3;

    private static final int CALIBRATION_DATA_LENGTH_1	 = 24;
    private static final int CALIBRATION_DATA_LENGTH_2	 = 7;

    private static final int DIG_T1_REG = 0x88;
    private static final int DIG_H1_REG = 0xa1;
    private static final int DIG_H2_REG = 0xe1;

    private static final int CONTROL_HUMIDITY_REG				= 0xf2;
    private static final int CONTROL_MEASUREMENT_REG			= 0xf4;
    private static final byte CONTROL_HUMIDITY_OSRS_H_1			= 0x01;
    private static final byte CONTROL_MEASUREMENT_OSRS_T_1		= (byte)0x20;
    private static final byte CONTROL_MEASUREMENT_OSRS_P_1		= (byte)0x04;
    private static final byte CONTROL_MEASUREMENT_FORCED_MODE		= (byte)0x01;

    private static final int CONFIG_REG			= 0xf5;
    private static final byte CONFIG_T_SB_0_5		= (byte)0x00;
    private static final int PRESSURE_DATA_REG		= 0xf7;
    private static final int SENSOR_DATA_LENGTH	 = 8;

    private static final int MEASUREMENT_TIME_MILLIS = 10;

    private final int address;
    private final String deviceId;
    private final Context context;
    private final int i2cBus;
    private final I2C bme280;

    private int digT1;
    private int digT2;
    private int digT3;
    private int digP1;
    private int digP2;
    private int digP3;
    private int digP4;
    private int digP5;
    private int digP6;
    private int digP7;
    private int digP8;
    private int digP9;
    private int digH1;
    private int digH2;
    private int digH3;
    private int digH4;
    private int digH5;
    private int digH6;

    public BME280Impl(Context pi4j) {
        this(pi4j, BME280_ADDRESS, 1);
    }

    public BME280Impl(Context pi4j, int address, int i2cBus) {
        this.address = address;
        this.deviceId = "BME280";
        this.context = pi4j;
        this.i2cBus = i2cBus;
        I2CProvider i2CProvider = pi4j.provider(I2C_PROVIDER);
        I2CConfig i2cConfig = I2C.newConfigBuilder(pi4j).id(deviceId).bus(i2cBus).device(address).build();
        bme280 = i2CProvider.create(i2cConfig);
        LOG.info("BME280 Connected to i2c bus={} address={}. OK.", i2cBus, address);
        readCalibrationData();
    }

    private void readCalibrationData() {
        LOG.debug("readCalibrationData:");
        byte[] data = read(DIG_T1_REG, CALIBRATION_DATA_LENGTH_1);

        digT1 = unsigned16Bits(data, 0);
        digT2 = signed16Bits(data, 2);
        digT3 = signed16Bits(data, 4);

        digP1 = unsigned16Bits(data, 6);
        digP2 = signed16Bits(data, 8);
        digP3 = signed16Bits(data, 10);
        digP4 = signed16Bits(data, 12);
        digP5 = signed16Bits(data, 14);
        digP6 = signed16Bits(data, 16);
        digP7 = signed16Bits(data, 18);
        digP8 = signed16Bits(data, 20);
        digP9 = signed16Bits(data, 22);

        digH1 = read(DIG_H1_REG) & 0xff;
        data = read(DIG_H2_REG, CALIBRATION_DATA_LENGTH_2);

        digH2 = signed16Bits(data, 0);
        digH3 = data[2] & 0xff;
        digH4 = ((data[3] & 0xff) << 4) + (data[4] & 0x0f);
        digH5 = ((data[5] & 0xff) << 4) + ((data[4] & 0xff) >> 4);
        digH6 = data[6];
    }

    private byte[] read(int register, int length) {
        byte[] in = new byte[length];
        bme280.readRegister(register, in);
        return in;
    }

    private int read(int register) {
        return bme280.readRegister(register);
    }

    private void write(int register, byte out) {
        bme280.writeRegister(register, out);
    }

    @Override
    public Data getSensorValues() {
        write(CONTROL_HUMIDITY_REG, CONTROL_HUMIDITY_OSRS_H_1);
        write(CONTROL_MEASUREMENT_REG, (byte)(CONTROL_MEASUREMENT_OSRS_T_1 | CONTROL_MEASUREMENT_OSRS_P_1 | CONTROL_MEASUREMENT_FORCED_MODE));
        write(CONFIG_REG, CONFIG_T_SB_0_5);
        waitFor(MEASUREMENT_TIME_MILLIS);
        byte[] data = read(PRESSURE_DATA_REG, SENSOR_DATA_LENGTH);
        float[] floats = compensateDataBME280(data, digT1, digT2, digT3,
                digP1, digP2, digP3, digP4, digP5, digP6, digP7, digP8, digP9,
                digH1, digH2, digH3, digH4, digH5, digH6);
        return new Data(floats[0], floats[1], floats[2]);
    }

    @Override
    public int getAddress() {
        return address;
    }

    @Override
    public Context getContext() {
        return context;
    }

    @Override
    public int getI2CBus() {
        return i2cBus;
    }

    @Override
    public String getDeviceId() {
        return deviceId;
    }

    @Override
    public int getId() {
        return bme280.readRegister(ID_REGISTER);
    }

    @Override
    public void reset() {
        bme280.writeRegister(RESET_REGISTER, 0xB6);
    }

    @Override
    public int getStatus() {
        return bme280.readRegister(STATUS_REGISTER);
    }

    @Override
    public float getTemperature() {
        return getSensorValues().getTemperature();
    }

    @Override
    public float getPressure() {
        return getSensorValues().getPressure();
    }

    @Override
    public float getRelativeHumidity() {
        return getSensorValues().getRelativeHumidity();
    }

    @Override
    public void close() throws Exception {
        bme280.close();
    }

    public class Data {
        private final float temperature;
        private final float relativeHumidity;
        private final float pressure;

        public Data(float temperature, float relativeHumidity, float pressure) {
            this.temperature = temperature;
            this.relativeHumidity = relativeHumidity;
            this.pressure = pressure;
        }

        public float getTemperature() {
            return temperature;
        }

        public float getRelativeHumidity() {
            return relativeHumidity;
        }

        public float getPressure() {
            return pressure;
        }
    }

}
