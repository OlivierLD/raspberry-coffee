void setup() {
  pinMode(0, OUTPUT);
  Serial.begin(115200);
  Serial.println("Setup OK");
}

void loop() {
  digitalWrite(0, HIGH);
  Serial.println("Akeu");
  delay(500);
  digitalWrite(0, LOW);
  Serial.println("Coucou");
  delay(500);
}
