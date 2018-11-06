/**
 Adapted from the code by Daniel Eichhorn
 See more at http://blog.squix.ch
*/

#include <Arduino.h>

#define BLACK 0
#define WHITE 1
#define INVERSE 2

class SSD1306 {

private:
   int myI2cAddress;
   int mySda;
   int mySdc;
   uint8_t buffer[128 * 64 / 8];
   bool myIsFontScaling2x2 = false;
   int myFrameState = 0;
   int myFrameTick = 0;
   int myCurrentFrame = 0;
   int myFrameCount = 0;
   int myFrameWaitTicks = 80;
   int myFrameTransitionTicks = 25;
   int myColor = WHITE;
   void (**myFrameCallbacks)(int x, int y);


public:
   // Empty constructor
   SSD1306(int i2cAddress, int sda, int sdc);
   void init();
   void resetDisplay(void);
   void reconnect(void);
   void displayOn(void);
   void displayOff(void);
   void clear(void);
   void display(void);
   void setPixel(int x, int y);
   void setChar(int x, int y, unsigned char data);
   void drawString(int x, int y, String text);
   void setFontScale2x2(bool isFontScaling2x2);
   void drawBitmap(int x, int y, int width, int height, const char *bitmap);
   void drawXbm(int x, int y, int width, int height, const char *xbm);
   void sendCommand(unsigned char com);
   void sendInitCommands(void);
   void setColor(int color);
   void drawRect(int x, int y, int width, int height);
   void fillRect(int x, int y, int width, int height);

   void setContrast(char contrast);
   void flipScreenVertically();

   void setFrameCallbacks(int frameCount, void (*frameCallbacks[])(int x, int y));
   void nextFrameTick(void);
   void drawIndicators(int frameCount, int activeFrame);
   void setFrameWaitTicks(int frameWaitTicks);
   void setFrameTransitionTicks(int frameTransitionTicks);
   int getFrameState();

   const int FRAME_STATE_FIX = 0;
   const int FRAME_STATE_TRANSITION = 1;

};
