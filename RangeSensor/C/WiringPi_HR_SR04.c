#include <jni.h>
#include <iostream>
#include <wiringPi.h>
#include "rangesensor_JNI_HC_SR04.h"

// For the pinout, see https://pi.gadgetoid.com/pinout

#define GPIO23     4
#define GPIO24     5

#define GPIO17     0
#define GPIO27     2
#define GPIO22     3

using namespace std;

//static int trigger = GPIO23;
//static int echo    = GPIO24;

static int trigger = GPIO22;
static int echo    = GPIO27;

static volatile long startTimeUsec;
static volatile long endTimeUsec;

// static double speedOfSoundMetersPerSecond = 340.29;
static double speedOfSoundMetersPerSecond = 343.00;

void init();
double readRange();

JNIEXPORT void JNICALL Java_rangesensor_JNI_1HC_1SR04_init__ (JNIEnv * env, jobject obj)
{
  init();  
}

JNIEXPORT void JNICALL Java_rangesensor_JNI_1HC_1SR04_init__II (JNIEnv * env, jobject obj, jint trigPin, jint echoPin)
{
  trigger = (int)trigPin;
  echo    = (int)echoPin;
  init();
}


JNIEXPORT jdouble JNICALL Java_rangesensor_JNI_1HC_1SR04_readRange (JNIEnv * env, jobject obj)
{
  return readRange();
}

void recordPulseLength (void) {
  startTimeUsec = micros();
  while ( digitalRead(echo) == HIGH );
  endTimeUsec = micros();
}

void init()
{
  wiringPiSetup();

  pinMode(trigger, OUTPUT);
  pinMode(echo,    INPUT);    
}

double readRange()
{
  // Initialize the sensor's trigger pin to low. If we don't pause
  // after setting it to low, sometimes the sensor doesn't work right.
  digitalWrite(trigger, LOW);
  delay(500); // .5 seconds

  // Triggering the sensor for 10 microseconds will cause it to send out
  // 8 ultrasonic (40Khz) bursts and listen for the echos.
  digitalWrite(trigger, HIGH);
  delayMicroseconds(10);
  digitalWrite(trigger, LOW);

  // The sensor will raise the echo pin high for the length of time that it took
  // the ultrasonic bursts to travel round trip.
  // Doesn't work; endTimeUsec and startTimeUsec are always the same.
  //wiringPiISR(echo, INT_EDGE_RISING, &recordPulseLength);
  while ( digitalRead(echo) == LOW);
  recordPulseLength();

  long travelTimeUsec = endTimeUsec - startTimeUsec;
  double distanceMeters = ((travelTimeUsec / 1000000.0) * speedOfSoundMetersPerSecond) / 2;
  return distanceMeters;
}

/**
 * Uses the HC SR04 ultrasonic sensor to measure distance. The HC SR04
 * provides 2cm to 400cm range measurement.
 */
int main()
{
  init();

  cout << "While distance > 5cm...";

  bool go = true;
  while (go)
  {
    double dm = readRange();
    cout << "Distance is " << dm * 100 << " cm." << endl;
    go = (dm * 100 > 5);
  }
  cout << "Bye...";
  return 0;
}
