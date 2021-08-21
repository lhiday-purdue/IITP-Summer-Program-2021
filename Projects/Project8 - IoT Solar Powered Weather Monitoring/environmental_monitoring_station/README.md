# LoRaWAN / WiFi Weather Station

BastWAN or ESP8266 micro weather station.

## Hardware

- RAK3244 (a.k.a. BastWAN) or ESP8266*
- BME680 (Intergrated temperature, humidity, barometric prssure, IAQ sensor)
- DS18B20 (Temperature sensor)
- Generic soil moisture sensor
- ML8511 (UV sensor)

\* ESP8266 support is only for testing purpose.

## Required Libraries

- [Adafruit BME680 Library](https://github.com/adafruit/Adafruit_BME680)
- [Arduino Library for Maxim Temperature Integrated Circuits (a.k.a. DallasTemperature)](https://github.com/milesburton/Arduino-Temperature-Control-Library)
- For BastWAN
    - [Beelan-LoRaWAN with KR920 support](https://github.com/sh1217sh/Beelan-LoRaWAN/tree/KR920)
    - [ElectronicCats fork of Arduino Low Power](https://github.com/ElectronicCats/ArduinoLowPower)
- For ESP8266
    - [Arduino Client for MQTT (a.k.a. PubSubClient)](https://pubsubclient.knolleary.net)
    - [base64_arduino](https://github.com/Densaugeo/base64_arduino)

## Data Packet Encoding

The weather station sends 16 byte packet.

```
+------------------------+------------------------+-----------+-----------+-------------+-----------+------------+------------+-------------+-----------+------------+----------+------------+------------+-------------+-----------+
|            0           |            1           |     2     |     3     |      4      |     5     |      6     |      7     |      8      |     9     |     10     |    11    |     12     |     13     |      14     |     15    |
+------------------------+------------------------+-----------+-----------+-------------+-----------+------------+------------+-------------+-----------+------------+----------+------------+------------+-------------+-----------+
|     Battery Voltage    |       VIN Voltage      |  Ambient Temperature  |     Ambient Humidity    |   Barometric Pressure   |      Gas Resistance     |    Soil Temperature   |      Soil Moisture      |       UV Intensity      |
+------------------------+------------------------+-----------------------+-------------------------+-------------------------+-------------------------+-----------------------+-------------------------+-------------------------+
| 8-bit Unsigned Integer | 8-bit Unsigned Integer | 16-bit Signed Integer | 16-bit Unsigned Integer | 16-bit Unsigned Integer | 16-bit Unsigned Integer | 16-bit Signed Integer | 16-bit Unsigned Integer | 16-bit Unsigned Integer |
+------------------------+------------------------+-----------------------+-------------------------+-------------------------+-------------------------+-----------------------+-------------------------+-------------------------+
```
All 16-bit integers are in Big-Endian (MSB first) byte order.

After converting bytes into integers, use following table to obtain original values.

| Data | Encoded with | Decode with | Unit |
| --- | --- | --- | --- |
| Battery Voltage* | (value * 100) - 320 | (data + 320) / 100 | V |
| VIN Voltage* | value * 10 | data / 10 | V |
| Ambient Temperature | value * 100 | data / 100 | ℃ |
| Ambient Humidity | value * 100 | data / 100 | % |
| Barometric Pressure | value * 10 | data / 10 | ㍱ |
| Gas Resistance | value * 100 | data / 100 | kΩ |
| Soil Temperature | value * 100 | data / 100 | ℃ |
| Soil Moisture* | value | data | - (Raw 10-bit ACD value) |
| UV Intensity | value * 100 | data / 100 | ㎽∕㎠ |

\* If this sketch is running on ESP8266, battery voltage, VIN voltage, soil moisture values are randomly generated since ESP8266 only has one ADC(Analog-to-Digital Converter).
