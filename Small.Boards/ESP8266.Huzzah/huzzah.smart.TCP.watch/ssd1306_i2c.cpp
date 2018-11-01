/**
  Adapted from the code by Daniel Eichhorn, at http://blog.squix.ch
*/

#include "ssd1306_i2c.h"
#include <Wire.h>
#include "font.h"

const int DISPLAY_ON            = 0xAF;
const int DISPLAY_OFF           = 0xAE;
const int NORMAL_DISPLAY        = 0xA6;
const int INVERSE_DISPLAY       = 0xA7;
const int SET_DISPLAY_CLOCK_DIV = 0xD5;
const int SUGGESTED_RATIO       = 0x80;
const int SET_MULTIPLEX         = 0xA8;
const int SET_DISPLAY_OFFSET    = 0xD3;
const int NO_OFFSET             = 0x00;
const int SET_START_LINE        = 0x40;
const int CHARGE_PUMP           = 0x8D;
const int MEMORY_MODE           = 0x20;
const int COM_SCAN_DEC_         = 0x12;
const int SET_CONTRAST          = 0x81;
const int SET_PRE_CHARGE        = 0xD9;
const int SET_COM_PINS          = 0xDA;
const int SET_VCOM_DETECT       = 0xDB;
const int DISPLAY_ALL_ON_RESUME = 0xA4;
const int SEG_REMAP             = 0xA0;
const int COM_SCAN_INC          = 0xC0;
const int COM_SCAN_DEC          = 0xC8;
const int DEACTIVATE_SCROLL     = 0x2E;

SSD1306::SSD1306(int i2cAddress, int sda, int sdc) {
  myI2cAddress = i2cAddress;
  mySda = sda;
  mySdc = sdc;
}

void SSD1306::init() {
  Wire.begin(mySda, mySdc);
  Wire.setClock(250000);
  sendInitCommands();
  resetDisplay();
}

void SSD1306::resetDisplay(void) {
  displayOff();
  clear();
  display();
  displayOn();
}

void SSD1306::reconnect() {
  Wire.begin();
}

void SSD1306::displayOn(void) {
  sendCommand(DISPLAY_ON);
}

void SSD1306::displayOff(void) {
  sendCommand(DISPLAY_OFF);
}

void SSD1306::setContrast(char contrast) {
  sendCommand(SET_CONTRAST);
  sendCommand(contrast);
}
void SSD1306::flipScreenVertically() {
  sendCommand(SEG_REMAP | 0x1);      // Rotate screen 180 deg
  sendCommand(COM_SCAN_DEC);         // Rotate screen 180 Deg
}
void SSD1306::clear(void) {
  memset(buffer, 0, (128 * 64 / 8));
}

void SSD1306::display(void) {
  for (uint16_t i = 0; i < (128 * 64 / 8); i++) {
    // send a bunch of data in one xmission
    Wire.beginTransmission(myI2cAddress);
    Wire.write(0x40);
    for (uint8_t x = 0; x < 16; x++) {
      Wire.write(buffer[i]);
      i++;
    }
    i--;
    yield();
    Wire.endTransmission();
  }
}

void SSD1306::setPixel(int x, int y) {
  if (x >= 0 && x < 128 && y >= 0 && y < 64) {
    switch (myColor) {
      case WHITE:
        buffer[x + (y / 8) * 128] |=  (1 << (y & 7));
        break;
      case BLACK:
        buffer[x + (y / 8) * 128] &= ~(1 << (y & 7));
        break;
      case INVERSE:
        buffer[x + (y / 8) * 128] ^=  (1 << (y & 7));
        break;
    }
  }
}

void SSD1306::setChar(int x, int y, unsigned char data) {
  for (int i = 0; i < 8; i++) {
    if (bitRead(data, i)) {
      setPixel(x, y + i);
    }
  }
}

void SSD1306::drawString(int x, int y, String text) {
  for (int j = 0; j < text.length(); j++) {
    for (int i = 0; i < 8; i++) {
      unsigned char charColumn = pgm_read_byte(myFont[text.charAt(j) - 0x20] + i);
      for (int pixel = 0; pixel < 8; pixel++) {
        if (bitRead(charColumn, pixel)) {
          if (myIsFontScaling2x2) {
            setPixel(x + 2 * (j * 8 + i), y + 2 * pixel);
            setPixel(x + 2 * (j * 8 + i) + 1, y + 2 * pixel + 1);
            setPixel(x + 2 * (j * 8 + i) + 1, y + 2 * pixel);
            setPixel(x + 2 * (j * 8 + i), y + 2 * pixel + 1);
          } else {
            setPixel(x + j * 8 + i, y + pixel);
          }
        }
      }
    }
  }
}

void SSD1306::setFontScale2x2(bool isFontScaling2x2) {
  myIsFontScaling2x2 = isFontScaling2x2;
}

void SSD1306::drawBitmap(int x, int y, int width, int height, const char *bitmap) {
  for (int i = 0; i < width * height / 8; i++ ) {
    unsigned char charColumn = 255 - pgm_read_byte(bitmap + i);
    for (int j = 0; j < 8; j++) {
      int targetX = i % width + x;
      int targetY = (i / (width)) * 8 + j + y;
      if (bitRead(charColumn, j)) {
        setPixel(targetX, targetY);
      }
    }
  }
}

void SSD1306::setColor(int color) {
  myColor = color;
}

void SSD1306::drawRect(int x, int y, int width, int height) {
  for (int i = x; i < x + width; i++) {
    setPixel(i, y);
    setPixel(i, y + height);
  }
  for (int i = y; i < y + height; i++) {
    setPixel(x, i);
    setPixel(x + width, i);
  }
}

void SSD1306::fillRect(int x, int y, int width, int height) {
  for (int i = x; i < x + width; i++) {
    for (int j = 0; j < y + height; j++) {
      setPixel(i, j);
    }
  }
}

void SSD1306::drawXbm(int x, int y, int width, int height, const char *xbm) {
  if (width % 8 != 0) {
    width =  ((width / 8) + 1) * 8;
  }
  for (int i = 0; i < width * height / 8; i++ ) {
    unsigned char charColumn = pgm_read_byte(xbm + i);
    for (int j = 0; j < 8; j++) {
      int targetX = (i * 8 + j) % width + x;
      int targetY = (8 * i / (width)) + y;
      if (bitRead(charColumn, j)) {
        setPixel(targetX, targetY);
      }
    }
  }
}

void SSD1306::sendCommand(unsigned char com) {
  // Wire.begin(mySda, mySdc);
  Wire.beginTransmission(myI2cAddress);      // begin transmitting
  Wire.write(0x80);                          // command mode
  Wire.write(com);
  Wire.endTransmission();                    // stop transmitting
}

void SSD1306::sendInitCommands(void) {
  sendCommand(DISPLAY_OFF);
  sendCommand(NORMAL_DISPLAY);
  // Adafruit Init sequence for 128x64 OLED module
  sendCommand(DISPLAY_OFF);
  sendCommand(SET_DISPLAY_CLOCK_DIV);
  sendCommand(SUGGESTED_RATIO);
  sendCommand(SET_MULTIPLEX);
  sendCommand(0x3F);
  sendCommand(SET_DISPLAY_OFFSET);
  sendCommand(NO_OFFSET);
  sendCommand(SET_START_LINE | NO_OFFSET);
  sendCommand(CHARGE_PUMP);
  sendCommand(0x14);
  sendCommand(MEMORY_MODE);
  sendCommand(NO_OFFSET);          // 0x0 act like ks0108

  //sendCommand(SEG_REMAP | 0x1);  // Rotate screen 180 deg
  sendCommand(SEG_REMAP);

  //sendCommand(COM_SCAN_DEC);     // Rotate screen 180 Deg
  sendCommand(COM_SCAN_INC);

  sendCommand(SET_COM_PINS);
  sendCommand(COM_SCAN_DEC_);
  sendCommand(SET_CONTRAST);
  sendCommand(0xCF);       //
  sendCommand(SET_PRE_CHARGE);
  sendCommand(0xF1);
  sendCommand(SET_VCOM_DETECT);
  sendCommand(SET_START_LINE);
  sendCommand(DISPLAY_ALL_ON_RESUME);
  sendCommand(NORMAL_DISPLAY);

  sendCommand(DEACTIVATE_SCROLL);       // stop scroll
  //----------------------------REVERSE comments----------------------------//
  //  sendCommand(0xa0);		// seg re-map 0->127(default)
  //  sendCommand(0xa1);		// seg re-map 127->0
  //  sendCommand(0xc8);
  //  delay(1000);
  //----------------------------REVERSE comments----------------------------//
  // sendCommand(INVERSE_DISPLAY);    // Set Inverse Display
  // sendCommand(DISPLAY_OFF);		 // display off
  sendCommand(0x20);       // Set Memory Addressing Mode
  sendCommand(0x00);       // Set Memory Addressing Mode ab Horizontal addressing mode
  // sendCommand(0x02);    // Set Memory Addressing Mode ab Page addressing mode(RESET)
}

void SSD1306::nextFrameTick() {
  myFrameTick++;
  if (myFrameTick == myFrameWaitTicks && myFrameState == 0 || myFrameTick == myFrameTransitionTicks && myFrameState == 1) {
    myFrameState = (myFrameState + 1) %  2;
    if (myFrameState == FRAME_STATE_FIX) {
      myCurrentFrame = (myCurrentFrame + 1) % myFrameCount;
    }
    myFrameTick = 0;
  }
  drawIndicators(myFrameCount, myCurrentFrame);

  switch (myFrameState) {
    case 0:
      (*myFrameCallbacks[myCurrentFrame])(0, 0);
      break;
    case 1:
      (*myFrameCallbacks[myCurrentFrame])(-128 * myFrameTick / myFrameTransitionTicks, 0);
      (*myFrameCallbacks[(myCurrentFrame + 1) % myFrameCount])(-128 * myFrameTick / myFrameTransitionTicks + 128, 0);
      break;
  }
}

void SSD1306::drawIndicators(int frameCount, int activeFrame) {
  for (int i = 0; i < frameCount; i++) {
    const char *xbm;
    if (activeFrame == i) {
      xbm = active_bits;
    } else {
      xbm = inactive_bits;
    }
    drawXbm(64 - (12 * frameCount / 2) + 12 * i, 56, 8, 8, xbm);
  }
}

void SSD1306::setFrameCallbacks(int frameCount, void (*frameCallbacks[])(int x, int y)) {
  myFrameCount = frameCount;
  myFrameCallbacks = frameCallbacks;
}

void SSD1306::setFrameWaitTicks(int frameWaitTicks) {
  myFrameWaitTicks = frameWaitTicks;
}

void SSD1306::setFrameTransitionTicks(int frameTransitionTicks) {
  myFrameTransitionTicks = frameTransitionTicks;
}

int SSD1306::getFrameState() {
  return myFrameState;
}

