/*
 *  This is a single sketch for tilt compensated compass using an LSM303DLHC.
 *  It does not use any libraries, other than wire, because I found a number of
 *  errors in libraries that are out there.The biggest problem is with libraries
 *  that rely on the application note for the lsm303.  That application note seems to
 *  include assumptions that are wrong, and I was not able to get it to produce
 *  reliable tilt compensated heading values.  If you look at how pitch, roll and heading
 *  are calculated below, you will see that the formula have some of the factors from
 *  the application note, but other factors are not included.
 *
 *  Somehow the matrix manipulations that are described in the application note
 *  introduce extra factors that are wrong.  For example, the pitch and roll calculation should
 *  not depend on whether you rotate pitch before roll, or vice versa. But if you
 *  go through the math using the application note, you will find that they give
 *  different formula
 *
 *  Same with Heading.  When I run this sketch on my Adafruit break-out board, I get very
 *  stable pitch, roll and heading, and the heading is very nicely tilt compensated, regardless
 *  of pitch or roll.
 */
int ACC_Data0, ACC_Data1, ACC_Data2, ACC_Data3, ACC_Data4, ACC_Data5;

int MAG_Data0, MAG_Data1, MAG_Data2, MAG_Data3, MAG_Data4, MAG_Data5;

int Ax, Ay, Az, Mx, My, Mz;

float Xm_print, Ym_print, Zm_print;
float Xm_off, Ym_off, Zm_off, Xm_cal, Ym_cal, Zm_cal, Norm_m;

float Xa_print, Ya_print, Za_print;
float Xa_off, Ya_off, Za_off, Xa_cal, Ya_cal, Za_cal, Norm_a;

const float alpha = 0.15;
float fXa = 0;
float fYa = 0;
float fZa = 0;
float fXm = 0;
float fYm = 0;
float fZm = 0;
float pitch, pitch_print, roll, roll_print, Heading;
float fXm_comp, fYm_comp;


#include <Wire.h>

void setup() {

  Wire.begin();
  Serial.begin(9600);

  Wire.beginTransmission(0x19); // Set accel
  Wire.write(0x20);             // CTRL_REG1_A register
  Wire.write(0x47);             // 50 Hz, normal power, all 3 axis enabled
  Wire.endTransmission();

  Wire.beginTransmission(0x19); // Set accel
  Wire.write(0x23);             // CTRL_REG4_A register
  Wire.write(0x08);             // continuous update, littleendian, 2g, high resolution, 4-wire spi
  Wire.endTransmission();

  Wire.beginTransmission(0x1E); // set Mag
  Wire.write(0x00);             // CRA_REG_M register
  Wire.write(0x14);             // 30 Hz min data output rate
  Wire.endTransmission();

  Wire.beginTransmission(0x1E); // Set Mag
  Wire.write(0x02);             // MR_REG_M
  Wire.write(0x00);             // continuous conversion mode
  Wire.endTransmission();


}

void loop() {
  Wire.beginTransmission(0x19);
  Wire.write(0x28);
  Wire.endTransmission();
  Wire.requestFrom(0x19, (byte)1);
  ACC_Data0 = Wire.read();

  Wire.beginTransmission(0x19);
  Wire.write(0x29);
  Wire.endTransmission();
  Wire.requestFrom(0x19, (byte)1);
  ACC_Data1 = Wire.read();

  Wire.beginTransmission(0x19);
  Wire.write(0x2A);
  Wire.endTransmission();
  Wire.requestFrom(0x19, (byte)1);
  ACC_Data2 = Wire.read();

  Wire.beginTransmission(0x19);
  Wire.write(0x2B);
  Wire.endTransmission();
  Wire.requestFrom(0x19, (byte)1);
  ACC_Data3 = Wire.read();

  Wire.beginTransmission(0x19);
  Wire.write(0x2C);
  Wire.endTransmission();
  Wire.requestFrom(0x19, (byte)1);
  ACC_Data4 = Wire.read();

  Wire.beginTransmission(0x19);
  Wire.write(0x2D);
  Wire.endTransmission();
  Wire.requestFrom(0x19, (byte)1);
  ACC_Data5 = Wire.read();

  Ax = (int16_t)(ACC_Data1 << 8) | ACC_Data0;
  Ay = (int16_t)(ACC_Data3 << 8) | ACC_Data2;
  Az = (int16_t)(ACC_Data5 << 8) | ACC_Data4;

  Xa_off = Ax/16.0 + 14.510699; // add/subtract bias calculated by Magneto 1.2. Search the web and you will
  Ya_off = Ay/16.0 - 17.648453; // find this Windows application.  It works very well to find calibrations
  Za_off = Az/16.0 -  6.134981;
  Xa_cal =  1.006480*Xa_off - 0.012172*Ya_off + 0.002273*Za_off; // apply scale factors calculated by Magneto1.2
  Ya_cal = -0.012172*Xa_off + 0.963586*Ya_off - 0.006436*Za_off;
  Za_cal =  0.002273*Xa_off - 0.006436*Ya_off + 0.965482*Za_off;
  Norm_a = sqrt(Xa_cal * Xa_cal + Ya_cal * Ya_cal + Za_cal * Za_cal); //original code did not appear to normalize, and this seems to help
  Xa_cal = Xa_cal / Norm_a;
  Ya_cal = Ya_cal / Norm_a;
  Za_cal = Za_cal / Norm_a;

  Ya_cal = -1.0 * Ya_cal;  // This sign inversion is needed because the chip has +Z up, while algorithms assume +Z down
  Za_cal = -1.0 * Za_cal;  // This sign inversion is needed for the same reason and to preserve right hand rotation system

// Low-Pass filter accelerometer
  fXa = Xa_cal * alpha + (fXa * (1.0 - alpha));
  fYa = Ya_cal * alpha + (fYa * (1.0 - alpha));
  fZa = Za_cal * alpha + (fZa * (1.0 - alpha));


  Wire.beginTransmission(0x1E);
  Wire.write(0x03);
  Wire.endTransmission();

  Wire.requestFrom(0x1E, (byte)6);
  MAG_Data0 = Wire.read();
  MAG_Data1 = Wire.read();
  MAG_Data2 = Wire.read();
  MAG_Data3 = Wire.read();
  MAG_Data4 = Wire.read();
  MAG_Data5 = Wire.read();

  Mx = (int16_t)(MAG_Data0 << 8) | MAG_Data1;
  Mz = (int16_t)(MAG_Data2 << 8) | MAG_Data3;
  My = (int16_t)(MAG_Data4 << 8) | MAG_Data5;

  Xm_off = Mx*(100000.0/1100.0) -   617.106577; // Gain X [LSB/Gauss] for selected sensor input field range (1.3 in these case)
  Ym_off = My*(100000.0/1100.0) -  3724.617984; // Gain Y [LSB/Gauss] for selected sensor input field range
  Zm_off = Mz*(100000.0/980.0 ) - 16432.772031;  // Gain Z [LSB/Gauss] for selected sensor input field range
  Xm_cal =  0.982945*Xm_off + 0.012083*Ym_off + 0.014055*Zm_off; // same calibration program used for mag as accel.
  Ym_cal =  0.012083*Xm_off + 0.964757*Ym_off - 0.001436*Zm_off;
  Zm_cal =  0.014055*Xm_off - 0.001436*Ym_off + 0.952889*Zm_off;
  Norm_m = sqrt(Xm_cal * Xm_cal + Ym_cal * Ym_cal + Zm_cal * Zm_cal); // original code did not appear to normalize  This seems to help
  Xm_cal = Xm_cal / Norm_m;
  Ym_cal = Ym_cal / Norm_m;
  Zm_cal = Zm_cal / Norm_m;

  Ym_cal = -1.0 * Ym_cal;  // This sign inversion is needed because the chip has +Z up, while algorithms assume +Z down
  Zm_cal = -1.0 * Zm_cal;  // This sign inversion is needed for the same reason and to preserve right hand rotation system

// Low-Pass filter magnetometer
  fXm = Xm_cal * alpha + (fXm * (1.0 - alpha));
  fYm = Ym_cal * alpha + (fYm * (1.0 - alpha));
  fZm = Zm_cal * alpha + (fZm * (1.0 - alpha));


// Pitch and roll
  pitch = asin(fXa);
  roll = -asin(fYa);
  pitch_print = pitch*180.0/M_PI;
  roll_print = roll*180.0/M_PI;

// Tilt compensated magnetic sensor measurements
  fXm_comp = fXm*cos(pitch) + fZm*sin(pitch);
  fYm_comp = fYm*cos(roll) - fZm*sin(roll);

  Heading = (atan2(-fYm_comp,fXm_comp)*180.0)/M_PI;

  if (Heading < 0)
  Heading += 360;

  Serial.print("Pitch (X): "); Serial.print(pitch_print); Serial.print("  ");
  Serial.print("Roll (Y): "); Serial.print(roll_print); Serial.print("  ");
  Serial.print("Heading: "); Serial.println(Heading);

  delay(250);

}
