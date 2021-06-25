#include <iostream>
#include <wiringPi.h>

#define GPIO23     4
#define GPIO24     5

using namespace std;

static int trigger = GPIO23;
static int echo    = GPIO24;

static volatile long startTimeUsec;
static volatile long endTimeUsec;

void recordPulseLength (void) {
    startTimeUsec = micros();
    while ( digitalRead(echo) == HIGH );
    endTimeUsec = micros();
}


/**
 * Uses the HC SR04 ultrasonic sensor to measure distance. The HC SR04
 * provides 2cm to 400cm range measurement.
 *
 * WARNING: This is a standalone version.
 *          This is NOT the one used from Java
 *          See WiringPI_HC_SR04
 */
int main() {
    double speedOfSoundMetersPerSecond = 340.29;

    wiringPiSetup();

    pinMode(trigger, OUTPUT);
    pinMode(echo, INPUT);

    cout << "While distance > 5cm...";

    bool go = true;
    while (go) {
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
        double distanceMeters = ((travelTimeUsec/1000000.0)*speedOfSoundMetersPerSecond)/2;

        cout << "Distance is " << distanceMeters*100 << " cm." << endl;

        go = (distanceMeters*100 > 5);
    }
    cout << "Bye...";
    return 0;
}
