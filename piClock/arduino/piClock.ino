#include <TM1637Display.h>
#include <Wire.h>
#include <radio.h>
#include <SI4703.h>


// Module connection pins (Digital Pins)
#define CLK 2
#define DIO 3
#define BTNA 6
#define LDR A1
#define BUZZER 9
#define SPEAKER_MOSFET 8
#define RESET_PIN_RADIO 5
#define SDIO  A4

SI4703   radio(RESET_PIN_RADIO, SDIO);    ///< Create an instance of a SI4703 chip radio.

TM1637Display display(CLK, DIO);

//serial variables
const byte numChars = 9;
char receivedChars[numChars];
boolean newData = false;

//buzzer variables
boolean buzzerOn = false;
boolean buzzerPulseOn = false;
unsigned long prevBuzzerMillis = 0;
unsigned long prevBuzzerStartMillis = 0;
uint8_t bzCnt = 0;

//LDR variables
boolean sendLdrData = false;

//TIME variables
int militaryTime = 0;
uint8_t level = 0;

//send back char buffer
char sendMsg[8];
char bufferChar[5];

//btn
uint8_t buttonState;             // the current reading from the input pin
uint8_t lastButtonState = HIGH;  // the previous reading from the input pin

// the following variables are unsigned longs because the time, measured in
// milliseconds, will quickly become a bigger number than can be stored in an int.
unsigned long lastDebounceTime = 0;  // the last time the output pin was toggled
unsigned long debounceDelay = 50;    // the debounce time; increase if the out

//radio
boolean scanFm = false;
int scanLastChannel = 0;
int fmStation = 10690;  //106.9

void setup() {
  pinMode(BTNA, INPUT);
  pinMode(LDR, INPUT);
  pinMode(BUZZER, OUTPUT);
  pinMode(SPEAKER_MOSFET, OUTPUT);

  digitalWrite(SPEAKER_MOSFET, HIGH);

  Serial.begin(9600);
  Serial.print(F("<ready>"));//send ready signal to calluer
  Serial.flush();
}

void loop() {
  recvWithStartEndMarkers();
  handleSerialData();

  //Buzzer function
  if (buzzerOn) {
    buzzerHandler();
  }

  readButton1(); //read button 1

  if (scanFm) {
    scanFmBand();
  }

}

//handle the buzzer when ON. Basically it will turn off the buzzer automatically if not done by the user.
void buzzerHandler() {
  //buzzer tone

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

  if (bzCnt > 50) {
    buzzerOn = false;
    noTone(BUZZER);
  }
}

//send ldr data to caller
void sendLdrDataFunc() {
  uint8_t value = map(analogRead(LDR), 0, 1023, 0, 255); //read ldr value

  clearMsg();

  itoa(value, bufferChar, 10); //int to char

  sendMsg[0] = '<';
  sendMsg[1] = 'l';

  uint8_t i = 0;
  uint8_t idx = 2;
  for (i = 0 ; i < strlen(bufferChar) ; i ++) {
    sendMsg[idx] = bufferChar[i];
    idx ++;
  }

  sendMsg[idx] = '>';

  Serial.print(sendMsg);
  Serial.flush();//that means do not wait and send info to caller
}
// print time and turn on display if off
void printTime() {

  char timeBuffer[5];
  timeBuffer [0] = receivedChars[1];
  timeBuffer [1] = receivedChars[2];
  timeBuffer [2] = receivedChars[3];
  timeBuffer [3] = receivedChars[4];
  timeBuffer [4] = '\0';

  militaryTime = atoi(timeBuffer);

  display.setBrightness(level, true);
  display.showNumberDecEx(militaryTime, (0x80 >> 1), false, 4, 0);

}
void turnTimeDisplayOff() {
  display.setBrightness(level, false);
  display.showNumberDec(0000);
}
/*Adjust brightness of display. can go up to 7*/
void timeDisplayBrightness() {

  level = receivedChars[1];
  display.setBrightness(level);
  display.showNumberDecEx(militaryTime, (0x80 >> 1), true);
}
void handleBuzzer() {

  if (buzzerOn) {
    bzCnt = 0;
    buzzerPulseOn = true;
  } else {
    noTone(BUZZER);
    buzzerOn = false;
  }

}
void handleSpeakerMosfet() {


  if (receivedChars[1] == '8') { //ON
    digitalWrite(SPEAKER_MOSFET, LOW);
  } else {
    //off
    digitalWrite(SPEAKER_MOSFET, HIGH);
  }
}

//function to handle recieving data.
void recvWithStartEndMarkers() {
  static boolean recvInProgress = false;
  static byte ndx = 0;
  char startMarker = '<';
  char endMarker = '>';
  char rc;

  while (Serial.available() > 0 && newData == false) {
    rc = Serial.read();

    if (recvInProgress == true) {
      if (rc != endMarker) {
        receivedChars[ndx] = rc;
        ndx++;
        if (ndx >= numChars) {
          ndx = numChars - 1;
        }
      }
      else {
        receivedChars[ndx] = '\0'; // terminate the string
        recvInProgress = false;
        ndx = 0;
        newData = true;
      }
    }

    else if (rc == startMarker) {
      recvInProgress = true;
    }
  }
}

void handleSerialData() {
  if (newData == true) {

    switch (receivedChars[0]) {
      case 'l':
        sendLdrDataFunc();
        break;
      case 'b':
        if (receivedChars[1] == '8') {
          buzzerOn = true;
        } else {
          buzzerOn = false;
        }
        handleBuzzer();
        break;
      case 't':
        printTime();
        break;
      case 'o':
        turnTimeDisplayOff();
        break;
      case 'm':
        handleSpeakerMosfet();
        break;
      case 'c':
        timeDisplayBrightness();
        break;
      case 's':
        if (!scanFm) {
          fmStation = radio.getMinFrequency();// Rock FM.
          scanLastChannel = radio.getMinFrequency();
          radio.setFrequency(fmStation);
          scanFm = true;
        }
        break;
      case 'r':
        if (receivedChars[1] == '8') {
                     setRadioChannel();

        } else {
          radio.setMute(true);
          radio.setVolume(0);

        }
        break;
      case 'u': //turn radio on or off
        if (receivedChars[1] == '8') { //on
          radio.init();
          radio.setBandFrequency(RADIO_BAND_FM, fmStation);
          radio.setMute(false);
          radio.setVolume(1);
        } else {
          digitalWrite(RESET_PIN_RADIO, LOW);

        }
        break;
      default:
        // statements
        break;
    }
    newData = false;
  }
}

void clearMsg() {
  for ( int i = 0; i < sizeof(sendMsg);  ++i )
    sendMsg[i] = (char)0;
}


void readButton1() {

  uint8_t reading = digitalRead(BTNA);

  // If the switch changed, due to noise or pressing:
  if (reading != lastButtonState) {
    // reset the debouncing timer
    lastDebounceTime = millis();
  }

  if ((millis() - lastDebounceTime) > debounceDelay) {
    // whatever the reading is at, it's been there for longer than the debounce
    // delay, so take it as the actual current state:

    // if the button state has changed:
    if (reading != buttonState) {
      buttonState = reading;

      // only toggle the LED if the new button state is HIGH
      if (buttonState == HIGH) {
        clearMsg();
        sendMsg[0] = '<';
        sendMsg[1] = 'a';
        sendMsg[2] = 49; //ascii for 1
        sendMsg[3] = '>';

        Serial.print(sendMsg);
        Serial.flush();
      }
    }
  }
  // save the reading. Next time through the loop, it'll be the lastButtonState:
  lastButtonState = reading;

}

void scanFmBand() {

  if (fmStation >= scanLastChannel) {
    scanLastChannel = fmStation;

    radio.seekUp(true);
    //  Serial.print("Freq: ");
    fmStation = radio.getFrequency();
    //  Serial.println(fmStation);
    clearMsg();

    itoa(fmStation, bufferChar, 10);

    sendMsg[0] = '<';
    sendMsg[1] = 's';

    uint8_t i = 0;
    uint8_t idx = 2;
    for (i = 0 ; i < strlen(bufferChar) ; i ++) {
      sendMsg[idx] = bufferChar[i];
      idx ++;
    }

    sendMsg[idx] = '>';

    Serial.print(sendMsg);
    Serial.flush();//that means do not wait and send info to caller


  } else {

    clearMsg();
    sendMsg[0] = '<';
    sendMsg[1] = 's';
    sendMsg[2] = 'e';
    sendMsg[3] = 'n';
    sendMsg[4] = 'd';
    sendMsg[5] = '>';
    delay(50);
    Serial.print(sendMsg);
    Serial.flush();
    scanFm = false;
  }


}
void setRadioChannel() {
  char channelBuffer[6];
  channelBuffer [0] = receivedChars[2];
  channelBuffer [1] = receivedChars[3];
  channelBuffer [2] = receivedChars[4];
  channelBuffer [3] = receivedChars[5];

  if (strlen(receivedChars) > 6 ) { // 8990 = 4, 10690 = 5 so r88990 = 6 and r810690 = 7
    channelBuffer [4] = receivedChars[6];
    channelBuffer [5] = '\0';
  } else {
    channelBuffer [4] = '\0';
  }
//Serial.println(channelBuffer);
//Serial.println(receivedChars);
  fmStation = atoi(channelBuffer);
//  Serial.println(fmStation);

  radio.setFrequency(fmStation);



}