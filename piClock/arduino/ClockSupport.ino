#include <Wire.h>
#include <Arduino.h>
#include <TM1637Display.h>

// Module connection pins (Digital Pins)
#define CLK 2
#define DIO 3

TM1637Display display(CLK, DIO);
#define BTNA 6
#define LDR A1
#define BUZZER 9
#define SPEAKER_MOSFET 8

char cmd;

boolean buzzerOn = false;
boolean buzzerPulseOn = true;
unsigned long prevBuzzerMillis = 0;
unsigned long prevBuzzerStartMillis = 0;
uint8_t bzCnt = 0;

byte level = 4;
int militaryTime = 0;

//l = read ldr; a = read button ; t = display time; o = time off; b = buzzer (01=ON, 02=OFF),
void setup() {

  pinMode(BTNA, INPUT);
  pinMode(LDR, INPUT);
  pinMode(BUZZER, OUTPUT);
  pinMode(SPEAKER_MOSFET, OUTPUT);

  digitalWrite(SPEAKER_MOSFET, HIGH);
//blink status
  pinMode(LED_BUILTIN, OUTPUT);
  for (byte a = 0; a < 5; a++){
    digitalWrite(LED_BUILTIN, HIGH);
    delay(400);
    digitalWrite(LED_BUILTIN, LOW);
    delay(400);
  }
   
  digitalWrite(LED_BUILTIN, HIGH);
  Wire.begin(8);                // join i2c bus with address #8
  Wire.onRequest(requestEvent); // register event
  Wire.onReceive(receiveEvent);
} 
void loop() {

  //buzzer tone
  if (buzzerOn) {
    if ((millis() - prevBuzzerStartMillis) >= 500 && buzzerPulseOn) {
      prevBuzzerMillis = millis();
      tone(BUZZER, 350);
      buzzerPulseOn = false;

    } else if ( (millis() - prevBuzzerMillis) >= 500 && !buzzerPulseOn) {

      prevBuzzerStartMillis = millis();
      noTone(BUZZER);
      bzCnt ++;
      buzzerPulseOn = true;      
    }


    if (bzCnt > 30) {
      buzzerOn = false;
      noTone(BUZZER);
    }
  }
} 
//
// function that executes whenever data is requested by master
// this function is registered as an event, see setup()
/**requests coming from the master to provide him information  (LDR)**/
void requestEvent() {

  byte value = 1;
  if (cmd == 'l') {
    value = map(analogRead(LDR), 0, 1023, 0, 255); //read ldr value
  } else if ( cmd == 'a') {
    //read button
    value = digitalRead(BTNA);
  }

  Wire.write(value);
}

// function that executes whenever data is received from master
// this function is registered as an event, see setup()
/**recievent event to perform something from the master.**/
void receiveEvent(int howMany) {

  char c = Wire.read();

  if (c == 't') { //time
    printTime();
  } else if (c == 'o') { //time off
    turnTimeDisplayOff();
  } else if (c == 'b') { //buzzer
    handleBuzzer();
  } else if (c == 'm') {
    handleSpeakerMosfet();
  } else if (c == 'c') {
    timeDisplayBrightness();
  } else {
    cmd = c; //commands for the onrequest
  }
}
void printTime() {
  byte  hours = 0; //or int
  byte minutes = 0;

  hours = Wire.read();
  minutes = Wire.read();

  militaryTime = (hours * 100) + minutes;

  display.setBrightness(level, true);
  display.showNumberDecEx(militaryTime, (0x80 >> 1), true);

}
void turnTimeDisplayOff() {
  display.setBrightness(7, false);
  display.showNumberDec(0000);
}
void handleBuzzer() {
  byte one = Wire.read();
  byte two = Wire.read();
  if (one == 0 && two == 1) { //on
    buzzerOn = true;
    bzCnt = 0;
    buzzerPulseOn = true;
  } else if (one == 0 && two == 2) {
    noTone(BUZZER);
    buzzerOn = false;
  }
}

void handleSpeakerMosfet() {
  byte onOff = Wire.read();

  if (onOff == 1) { //ON
    digitalWrite(SPEAKER_MOSFET, LOW);
  } else {
    //off
    digitalWrite(SPEAKER_MOSFET, HIGH);
  }
}

/*Adjust brightness of display. can go up to 7*/
void timeDisplayBrightness(){
  level = Wire.read();
  display.setBrightness(level, true);
  display.showNumberDecEx(militaryTime, (0x80 >> 1), true);
}
