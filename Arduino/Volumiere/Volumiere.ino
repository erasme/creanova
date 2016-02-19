
String inputString = ""; // a string to hold incoming data

const int BlueA = 9;
const int GreenA = 10;
const int RedA = 11;

const int BlueB = 3;
const int GreenB = 5;
const int RedB = 6;

unsigned int Blue = 0;
unsigned int Green = 0;
unsigned int Red = 0;

void setup() {
  Serial.begin(9600);
  // reserve 16 bytes for the inputString:
  inputString.reserve(16);
  // setup pin mode
  pinMode(BlueA, OUTPUT);
  pinMode(GreenA, OUTPUT);
  pinMode(RedA, OUTPUT);
  pinMode(BlueB, OUTPUT);
  pinMode(GreenB, OUTPUT);
  pinMode(RedB, OUTPUT);
}

void loop() {

  analogWrite(BlueA, 255 - Blue);
  analogWrite(GreenA, 255 - Green);
  analogWrite(RedA, 255 - Red);
  
  analogWrite(BlueB, 255 - Blue);
  analogWrite(GreenB, 255 - Green);
  analogWrite(RedB, 255 - Red);
}

unsigned int hex2int(String a) {
    int val = 0;
    for(int i = 0; i < a.length(); i++) {
      val <<= 4;
      if(a[i] == '0')
        val += 0;
      else if(a[i] == '1')
        val += 1;
      else if(a[i] == '2')
        val += 2;
      else if(a[i] == '3')
        val += 3;
      else if(a[i] == '4')
        val += 4;
      else if(a[i] == '5')
        val += 5;
      else if(a[i] == '6')
        val += 6;
      else if(a[i] == '7')
        val += 7;
      else if(a[i] == '8')
        val += 8;
      else if(a[i] == '9')
        val += 9;
      else if(a[i] == 'a')
        val += 10;
      else if(a[i] == 'b')
        val += 11;
      else if(a[i] == 'c')
        val += 12;
      else if(a[i] == 'd')
        val += 13;
      else if(a[i] == 'e')
        val += 14;
      else if(a[i] == 'f')
        val += 15;
    }
    return val;
}

void serialEvent() {
  while (Serial.available()) {
    // get the new byte:
    char inChar = (char)Serial.read();
    // add it to the inputString:
    inputString += inChar;
    // if the incoming character is a newline, set a flag
    // so the main loop can do something about it:
    if (inChar == '\n') {
      // test if the string is a correct command else discard
      if((inputString.length() >= 7) && (inputString[0] == '#')) {
        String redString = inputString.substring(1, 3);
        Red = hex2int(redString);
        String greenString = inputString.substring(3, 5);
        Green = hex2int(greenString);
        String blueString = inputString.substring(5, 7);
        Blue = hex2int(blueString);
      }
      inputString = "";
    }
  }
}
