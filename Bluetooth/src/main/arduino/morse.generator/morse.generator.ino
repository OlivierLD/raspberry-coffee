/*
  This program blinks morse code on pin 13 of the Arduino (the
  built-in LED)
*/
#define NULL 0
#define ledPin LED_BUILTIN // or 13...

String EOS = "\r\n";
String receivedSentence = "";

typedef struct {
     char letter;
     String code;
 } morseCode;

morseCode alphabet[26];

void initMorseAlphabet() {
  alphabet[0].letter = 'A'; alphabet[0].code = "._";    // Allo
  alphabet[1].letter = 'B'; alphabet[1].code = "_...";  // Bombardement
  alphabet[2].letter = 'C'; alphabet[2].code = "_._.";  // Compte Goutte
  alphabet[3].letter = 'D'; alphabet[3].code = "_..";   // Domaine
  alphabet[4].letter = 'E'; alphabet[4].code = ".";     // Eh!
  alphabet[5].letter = 'F'; alphabet[5].code = ".._.";  // Fanfaronne
  alphabet[6].letter = 'G'; alphabet[6].code = "__.";   // Gloutonne
  alphabet[7].letter = 'H'; alphabet[7].code = "....";  // Heureusement
  alphabet[8].letter = 'I'; alphabet[8].code = "..";    // Ile
  alphabet[9].letter = 'J'; alphabet[9].code = ".___";  // J'ai mon loto
  alphabet[10].letter = 'K'; alphabet[10].code = "_._"; // Kohinor
  alphabet[11].letter = 'L'; alphabet[11].code = "._..";// Linotype 
  alphabet[12].letter = 'M'; alphabet[12].code = "__";  // Moto
  alphabet[13].letter = 'N'; alphabet[13].code = "_.";  // Note
  alphabet[14].letter = 'O'; alphabet[14].code = "___"; // Oloron
  alphabet[15].letter = 'P'; alphabet[15].code = ".__.";// Pyrophore
  alphabet[16].letter = 'Q'; alphabet[16].code = "__._";// Quocorico
  alphabet[17].letter = 'R'; alphabet[17].code = "._";  // Raisonne
  alphabet[18].letter = 'S'; alphabet[18].code = "..."; // Salade
  alphabet[19].letter = 'T'; alphabet[19].code = "_";   // Top
  alphabet[20].letter = 'U'; alphabet[20].code = ".._"; // Unisson
  alphabet[21].letter = 'V'; alphabet[21].code = "..._";// Vegetation 
  alphabet[22].letter = 'W'; alphabet[22].code = ".__"; // Wagon long
  alphabet[23].letter = 'X'; alphabet[23].code = "_.._";// Xochimilco
  alphabet[24].letter = 'Y'; alphabet[24].code = "_.__";// Yotcheoufou
  alphabet[25].letter = 'Z'; alphabet[25].code = "__..";// Zoroastre
}

void setup() {
  pinMode(ledPin, OUTPUT);
  Serial.begin(9600);
  
  initMorseAlphabet();  
}

void renderCode(char letter) {
  Serial.print("Rendering ");
  Serial.print(letter);
  Serial.print(" ");
  // Find it
  morseCode* found = NULL;
  for (int i=0; i<26; i++) {
    if (alphabet[i].letter == letter) {
      found = &alphabet[i];
      break;
    }
  }
  if (found != NULL) {
    String code = found->code;
    for (int i=0; i<code.length(); i++) {
      digitalWrite(ledPin, HIGH);
      if (code.charAt(i) == '.') {
        Serial.print(". ");
        delay(300);
      } else {
        Serial.print("_ ");
        delay(1000);
      }
    }
    Serial.println();
    digitalWrite(ledPin, LOW);
    delay(300);
  } else {
    Serial.print(letter);
    Serial.println(" not found...");
  }
}

void loop() {
  
  int data = -1;
  while (Serial.available() > 0) { // Checks whether data is coming from the serial port
    data = Serial.read(); // Reads the data from the serial port
    // You can use Serial.print(data, HEX); in the Serial console, NOT if another process is expecting the response
    receivedSentence.concat((char)data);
  }
  // Received a String
  if (receivedSentence.length() > 0) {
    receivedSentence.toUpperCase();
    Serial.print("To translate:");
    Serial.println(receivedSentence);
    for (int i=0; i<receivedSentence.length(); i++) {
      renderCode(receivedSentence.charAt(i));
    }
  }
  receivedSentence = ""; // Reset

  delay(1000); // Wait for 1000 millisecond(s)
}
