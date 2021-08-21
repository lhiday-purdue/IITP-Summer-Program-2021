int trig=11;
int echo=12;
int led1=6;//green
int led2=7;
void setup() {
  Serial.begin(9600);
  pinMode(trig,OUTPUT);
  pinMode(echo,INPUT);
  pinMode(led1,OUTPUT);
  pinMode(led2,OUTPUT);
}

void loop() {
  int distance=0;
  digitalWrite(trig,HIGH);
  delayMicroseconds(1000);
  digitalWrite(trig,LOW);
  distance=pulseIn(echo,HIGH)/58.2;

//  int distance=pulseIn(echo, HIGH)*17/1000;

  Serial.print(distance);
  Serial.print("cm");
  Serial.println();

  if(distance<7)
  {
    digitalWrite(led1,LOW);
    digitalWrite(led2,HIGH);
  }
  else
  {
    digitalWrite(led1,HIGH);
    digitalWrite(led2,LOW);
  }
}
