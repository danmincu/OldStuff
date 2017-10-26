/*
  Software serial multple serial test
 
 Receives from the hardware serial, sends to software serial.
 Receives from software serial, sends to hardware serial.
 
 The circuit: 
 * RX is digital pin 10 (connect to TX of other device)
 * TX is digital pin 11 (connect to RX of other device)
 
 Note:
 Not all pins on the Mega and Mega 2560 support change interrupts, 
 so only the following can be used for RX: 
 10, 11, 12, 13, 50, 51, 52, 53, 62, 63, 64, 65, 66, 67, 68, 69
 
 Not all pins on the Leonardo support change interrupts, 
 so only the following can be used for RX: 
 8, 9, 10, 11, 14 (MISO), 15 (SCK), 16 (MOSI).
 
 created back in the mists of time
 modified 25 May 2012
 by Tom Igoe
 based on Mikal Hart's example
 
 This example code is in the public domain.
 
 */
#include <SoftwareSerial.h>

SoftwareSerial mySerial(10, 11); // RX, TX
const int buttonPin = 2; 
const int ledPin =  13;      // the number of the LED pin
void setup()  
{
  // Open serial communications and wait for port to open:
  Serial.begin(9600);
  while (!Serial) {
    ; // wait for serial port to connect. Needed for Leonardo only
  }


  Serial.println("Send a command!");

  // set the data rate for the SoftwareSerial port
  mySerial.begin(9600);
  //mySerial.println("Hello, world?");
  pinMode(buttonPin, INPUT);  
  pinMode(ledPin, OUTPUT);
}

long randNumber;
long randNumber1;
long randNumber2;



int analogPin = 0;     // potentiometer wiper (middle terminal) connected to analog pin 3

                       // outside leads to ground and +5V

int val = 0;   
int is_on = 0;
int samples = 0;
unsigned long time;
int masterswitch = 0;


int buttonState = 0;       

void loop() // run over and over
{
  if (mySerial.available())
    Serial.write(mySerial.read());
    
    
  val = analogRead(analogPin);
  //Serial.println(val);
  if ((masterswitch == 1) && (is_on == 0) && (val < 30))
  {
    Serial.println("start collecting samples");
    Serial.println(val);
    time = millis();
    is_on = 1;
  }
  
   buttonState = digitalRead(buttonPin);

  // check if the pushbutton is pressed.
  // if it is, the buttonState is HIGH:
  if (buttonState == HIGH) {     
    // turn LED on:    
    digitalWrite(ledPin, HIGH);  
    
   if (masterswitch != 1) 
{    masterswitch = 1;
    Serial.println("read allowed. after trigger it'l record for 1 minute");
}
    
  } 
  else 
  digitalWrite(ledPin, LOW);  
  
  if (Serial.available() && Serial.read() == 's')
  {
    masterswitch = 1;
    Serial.println("read allowed");
  }
  
  if (is_on != 0 && masterswitch != 0)
  {
    val = analogRead(analogPin);    // read the input pin
    //Serial.println(val);             // debug value
    
    byte hiByte = highByte(val);
    byte loByte = lowByte(val);    
    mySerial.write(loByte);    
    mySerial.write(hiByte);
    //Serial.println(val);
   /* Serial.print(hiByte);
    Serial.print(":");
    Serial.print(loByte);
    Serial.print("-");
    Serial.print(val);
    Serial.print(" ");*/
    samples++;
    if (samples > 4000)
    {
      is_on = 0;
      masterswitch = 0;
      samples = 0;
      time = millis() - time;      
      Serial.print("Sample read done in");
      Serial.print(time);
      Serial.println("ms");
      Serial.println("read turned off");
      
    }
  }



}

