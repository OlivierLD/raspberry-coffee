#include <Wire.h>
#include <SD.h>

const int WID         =  42; // I2C Address of the AlaMode, 0x2A
//const int DS3231_ADDR = 104;
const int N_DATA_BYTE =  32;
const int BAUD        = 9600;
const int CHIPSELECT  = 10;
uint8_t data[N_DATA_BYTE];
uint8_t address;

boolean sd_initialized = false;

void setup() {
  while (!Serial);
  
  Serial.begin(BAUD);
  Serial.print("----------------------------------\n");
  Serial.print("Initialized...\n");
  Serial.println("----------------------------------");
  
  Wire.begin(WID);

  Wire.onReceive(ALAMODE_onReceive);
  Wire.onRequest(ALAMODE_onRequest);
  for (int ii=0; ii < N_DATA_BYTE; ii++) {
    data[ii] = 255;
  }
//data[1] = test_SD();
}
//                            0        1         2         3         4         5  
//                            12345678901234567890123456789012345678901234567890
const String REF_STRING_01 = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
const String REF_STRING_02 = "12345678901234567890";
const String REF_STRING_03 = "This is a small message coming from the Arduino";
const String REF_STRING_04 = "Call me Darth Wador, if you dare.";

void loop() {
  // Make sure the Serial Monitor is NOT opened.
  int pos = 0;
  char buffer[64];
  while (Serial.available() > 0) {
    char in = Serial.read();
    buffer[pos++] = in;    
    delay(10); // Let it arrive...
  }
  buffer[pos] = '\0';
  if (strlen(buffer) > 0) {
    if (strcmp(buffer, "SD") == 0) {
      test_SD();
    } else if (strcmp(buffer, "R") == 0 || strcmp(buffer, "W") == 0) {
      // Already taken care of
    } else {
      if (false) {
        int nb = atoi(buffer);
        String customMess = "- Not Found -";
        if (nb % 4 == 0) {
          customMess = REF_STRING_01;
        } else if (nb % 4 == 1) {
          customMess = REF_STRING_02;
        } else if (nb % 4 == 2) {
          customMess = REF_STRING_03;
        } else if (nb % 4 == 3) {
          customMess = REF_STRING_04;
        }
        char buf[64];
        customMess.toCharArray(buf, customMess.length() + 1);    
        char mess[64];
        sprintf(mess, "Choice #%d:%s", nb, buf);
        Serial.println(mess);
      } else {
        Serial.println(buffer); // Echo
      }
    }
  }
  /*
  if (data[2]) {
    digitalWrite(13, HIGH);
  } else {
    digitalWrite(13, LOW);
  }
  */
}

void ALAMODE_onReceive(int n_byte) {
  address = Wire.read();
  int idx = address;
  while (Wire.available() && (address + idx < N_DATA_BYTE)) {
    data[address + idx] = Wire.read();
    idx++;
  }
  char buf[64];
  sprintf(buf, "I2C: ALAMODE_onReceive addr:%d, idx:%d", address, idx);
  Serial.println(buf);
}

void ALAMODE_onRequest() {
  int n_byte = 32;
  if (N_DATA_BYTE - address < 32) {
    n_byte = N_DATA_BYTE - address;
  }
  Wire.write(data + address, n_byte);
  char buf[64];
  sprintf(buf, "I2C: ALAMODE_onRequest addr:%d", address);
  Serial.println(buf);
}

uint8_t test_SD() {
  uint8_t status = 0; // ALL PASS
  File myFile;
  char *msg = "0123456789";

  pinMode(CHIPSELECT, OUTPUT);
  if (!sd_initialized) {
    if (!SD.begin(CHIPSELECT)) {
      Serial.println("initialization failed!");
      status |= 1 << 2;
    } else {
      sd_initialized = true;
      Serial.println("SD Card OK");
      SD.remove("test.txt");
      myFile = SD.open("test.txt", FILE_WRITE);
  
      // if the file opened okay, write to it:
      if (myFile) {
        delay(300);
        myFile.print(msg);
        myFile.close();
      } 
      else {
        // if the file didn't open for writing, print an error:
        status |= 1 << 1;
      }
  
      // re-open the file for reading:
      myFile = SD.open("test.txt");
      if (myFile) {
        int ii = 0;
        
        while (myFile.available()) {
  	      if(msg[ii++] != myFile.read()) {
  	        status |= 1;
  	      }
        }
        myFile.close();
      }
    } 
  } else {
    Serial.println("SD Card already initialized");
  }
  return status;
}
