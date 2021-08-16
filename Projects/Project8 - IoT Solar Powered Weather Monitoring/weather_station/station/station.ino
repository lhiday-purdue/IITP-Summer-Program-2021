// Uncomment for BastWAN
#ifndef BASTWAN
#define BASTWAN
#endif

// Uncomment for ESP8266
// #ifndef ESP8266
// #define ESP8266
// #endif

#include <Wire.h> // I2C
#include <OneWire.h> // 1-Wire

// BME680 temperature, humidity, pressure, IAQ sensor
#include <Adafruit_Sensor.h>
#include "Adafruit_BME680.h"

// DS18B20 temperature sensor
#include <DallasTemperature.h>

#ifdef BASTWAN
#include <lorawan.h>
#include "ArduinoLowPower.h"
#endif

#ifdef ESP8266
#include <ESP8266WiFi.h>
#include <PubSubClient.h>
#include "base64.hpp"
#endif

#include "secrets.h"

#ifdef BASTWAN
// LoRaWAN OTAA credentials
// IMPORTANT: DO NOT EDIT HERE DIRECTLY. Edit these in `secrets.h` instead.
const char *devEui = SECRET_DEV_EUI;
const char *appEui = SECRET_APP_EUI;
const char *appKey = SECRET_APP_KEY;

// Pin configurations
const sRFM_pins RFM_pins = {
  .CS = SS,
  .RST = RFM_RST,
  .DIO0 = RFM_DIO0,
  .DIO1 = RFM_DIO1,
  .DIO2 = RFM_DIO2,
  .DIO5 = RFM_DIO5,
};
#define ONE_WIRE_BUS 10
#define UV_PIN A0
#define SOIL_MOIST_PIN A1
#define VIN_PIN A2
#define BAT_PIN A3
#endif

#ifdef ESP8266
// WiFi credentials
// IMPORTANT: DO NOT EDIT HERE DIRECTLY. Edit these in `secrets.h` instead.
const char *ssid = SECRET_WIFI_SSID;
const char *password = SECRET_WIFI_PW;

// MQTT settings
const char *mqtt_server = SECRET_MQTT_HOST;

// Pin configurations
// NOTE: ESP8266 only has one ADC.
#define ONE_WIRE_BUS 12
#define UV_PIN A0
#endif

OneWire oneWire(ONE_WIRE_BUS);

Adafruit_BME680 bme;

DallasTemperature dtSensors(&oneWire);
DeviceAddress soilTempSensorAddr = { 0x28, 0xF8, 0x9A, 0x07, 0xD6, 0x01, 0x3C, 0x50 };

#ifdef ESP8266
WiFiClient espClient;
PubSubClient mqttClient(espClient);
#endif

void setup() {
  Serial.begin(115200);
  delay(5000);

  #ifndef ESP8266
  pinMode(VIN_PIN, INPUT);
  pinMode(BAT_PIN, INPUT);
  #endif

  // BME680 setup
  if (!bme.begin()) {
    Serial.println("ERR: BME680 not present.");
  }
  bme.setTemperatureOversampling(BME680_OS_8X);
  bme.setHumidityOversampling(BME680_OS_2X);
  bme.setPressureOversampling(BME680_OS_4X);
  bme.setIIRFilterSize(BME680_FILTER_SIZE_3);
  bme.setGasHeater(320, 150); // 320*C for 150 ms

  // DS18B20 setup
  dtSensors.begin();
  if (dtSensors.getDeviceCount() <= 0) {
    Serial.println("ERR: DS18B20 not present.");
  }
  dtSensors.setResolution(soilTempSensorAddr, 9);

  // Soil moisture sensor setup
  #ifndef ESP8266
  pinMode(SOIL_MOIST_PIN, INPUT);
  #endif

  // ML8511 setup
  pinMode(UV_PIN, INPUT);

  pinMode(LED_BUILTIN, OUTPUT);

  // Network setup
  setupNetwork();
  #ifdef ESP8266
  mqttClient.setServer(mqtt_server, 1883);
  mqttClient.connect("ESP8266");
  #endif
}

void loop() {
  digitalWrite(LED_BUILTIN, HIGH);
  unsigned char data[16];
  encodeSensorValues(data);
  for (int i = 0; i < 16; i++) {
    Serial.print(data[i], HEX);
    Serial.print(" ");
  }
  Serial.print("\n\n");
  sendData(data);
  digitalWrite(LED_BUILTIN, LOW);

  #ifdef BASTWAN
  LowPower.sleep(5 * 60 * 1000); // 5 mins
  #else
  delay(60 * 1000); // 1 min, For testing purpose
  #endif
}

void encodeSensorValues(unsigned char *data) {
  // === Read battery voltage ===
  #ifdef ESP8266
  uint16_t batVoltage = random(320, 420);
  #else
  uint16_t batValue = averageAnalogRead(BAT_PIN);
  uint16_t batVoltage = map(batValue, 0, 712, 0, 450);
  #endif
  print("Battery", batVoltage / 100.0, "V");
  print("Battery raw", batValue, "");

  // === Read VIN voltage ===
  #ifdef ESP8266
  uint8_t vinVoltage = random(0, 50);
  #else
  uint16_t vinValue = averageAnalogRead(VIN_PIN);
  uint8_t vinVoltage = map(vinValue, 0, 778, 0, 49);
  #endif
  print("VIN", vinVoltage / 10.0, "V");
  print("VIN raw", vinValue, "");

  Serial.println();

  // === Read BME680 ===
  Serial.print("BME680: ");
  if (!bme.performReading()) { Serial.print("ERR! "); }

  float ambTemp = bme.temperature;
  float ambHumidity = bme.humidity;
  float pressure = bme.pressure;
  float gasResistance = bme.gas_resistance;

  print("Temp", ambTemp, "*C");
  print("Humidity", ambHumidity, "%");
  print("Pressure", pressure / 100.0, "hPa");
  print("Gas", gasResistance / 1000.0, "KOhms");
  Serial.println();

  // === Read DS18B20 ===
  Serial.print("DS18B20: ");
  dtSensors.requestTemperatures();

  float soilTemp = dtSensors.getTempC(soilTempSensorAddr);

  print("Temp", soilTemp, "*C");
  Serial.println();

  // === Read soil moisture sensor ===
  Serial.print("Soil moisture: ");

  #ifdef ESP8266
  uint16_t soilMoisture = random(0, 1024);
  #else
  uint16_t soilMoisture = averageAnalogRead(SOIL_MOIST_PIN);
  #endif

  print("Raw", soilMoisture, "");
  Serial.println();

  // === Read ML8511 ===
  Serial.print("ML8511: ");

  // Raw ADC value -> voltage -> UV intensity
  // See datasheet(http://www.jkelec.co.kr/img/sensors/se1/ML8511/ML8511_D.pdf) for characteristics.
  int uvValue = averageAnalogRead(UV_PIN);
  float uvVoltage = 3.3 / 1024 * uvValue;
  float uvIntensity = max(0, mapfloat(uvVoltage, 0.99, 2.8, 0.0, 15));

  print("UV intensity", uvIntensity, "mW/cm^2");
  Serial.println();

  // Encode sensor data
  uint8_t batVoltage8 = batVoltage - 320;
  int16_t ambTemp16 = ambTemp * 100.0;
  uint16_t ambHumidity16 = ambHumidity * 100.0;
  uint16_t pressure16 = pressure / 100.0 * 10.0;
  uint16_t gasResistance16 = gasResistance / 1000.0 * 100.0;
  int16_t soilTemp16 = soilTemp * 100.0;
  uint16_t uvIntensity16 = uvIntensity * 100.0;

  data[0]  = char(batVoltage8);
  data[1]  = char(vinVoltage);
  data[2]  = char((ambTemp16 >> 8));       data[3]  = char(ambTemp16);
  data[4]  = char((ambHumidity16 >> 8));   data[5]  = char(ambHumidity16);
  data[6]  = char((pressure16 >> 8));      data[7]  = char(pressure16);
  data[8]  = char((gasResistance16 >> 8)); data[9]  = char(gasResistance16);
  data[10] = char((soilTemp16 >> 8));      data[11] = char(soilTemp16);
  data[12] = char((soilMoisture >> 8));    data[13] = char(soilMoisture);
  data[14] = char((uvIntensity16 >> 8));   data[15] = char(uvIntensity16);
}

void setupNetwork() {
  #if defined(BASTWAN)
  if(!lora.init()){
    Serial.println("RFM95 not detected");
    delay(5000);
    return;
  }

  lora.setDeviceClass(CLASS_A);
  lora.setDataRate(SF8BW125);
  lora.setChannel(MULTI);
  
  lora.setDevEUI(devEui);
  lora.setAppEUI(appEui);
  lora.setAppKey(appKey);

  // Join procedure
  bool isJoined;
  do {
    Serial.println("Joining...");

    digitalWrite(LED_BUILTIN, HIGH);
    isJoined = lora.join();
    digitalWrite(LED_BUILTIN, LOW);
    
    //wait for 10s to try again
    delay(10000);
  }while(!isJoined);
  Serial.println("Joined to network");

  #elif defined(ESP8266)
  delay(10);
  Serial.println();
  Serial.print("Connecting to ");
  Serial.println(ssid);

  WiFi.begin(ssid, password);

  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }

  Serial.println("");
  Serial.println("WiFi connected");
  Serial.println("IP address: ");
  Serial.println(WiFi.localIP());
  #endif
}

void sendData(unsigned char *data) {
  #if defined(BASTWAN)
  lora.sendUplink((char*)data, 16, 0, 1);
  lora.update();

  #elif defined(ESP8266)
  // Base64 encoding
  unsigned char encodedString[100];
  unsigned int encodedSize = encode_base64(data, 16, encodedString);

  // MQTT publish
  while (!mqttClient.connected()) {
    Serial.print("Restoring MQTT connection... ");
    mqttClient.connect("ESP8266");
    if (mqttClient.connected()) Serial.println("Connected");
    else delay(500); // Retry after 500ms
  }
  mqttClient.publish("test/up", (char*)encodedString);
  #endif
}

int averageAnalogRead(int pinToRead)
{
  byte numberOfReadings = 8;
  unsigned int runningValue = 0; 

  for(int x = 0 ; x < numberOfReadings ; x++)
    runningValue += analogRead(pinToRead);
  runningValue /= numberOfReadings;

  return(runningValue);  
}

float mapfloat(float x, float in_min, float in_max, float out_min, float out_max)
{
  return (x - in_min) * (out_max - out_min) / (in_max - in_min) + out_min;
}

#ifdef ESP8266
float max(float x, float y) {
  if (x > y) return x;
  else return y;
}
#endif

void print(String name, float value, String unit) {
  String str = name + " = " + String(value) + unit + " ";
  Serial.print(str);
}
