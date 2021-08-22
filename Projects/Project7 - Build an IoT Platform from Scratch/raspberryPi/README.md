# raspberryPi
## raspberry pi as IoT home



These codes are written to control devices via Raspberry Pi 4 B model and make them communicate with broker through the MQTT protocol.

Raspberry Pi OS 32-bit is used for the RPi.

paho-mqtt is installed to set the Rpi as a MQTT Client.

* Following devices are used.
  * a Servo motor
  * 2 LEDs
  * a Raspberry pi camera v2
  * a DHT22
  
* To enable the devices, following libraries are used.
  * RPi.GPIO
  * Adafruit_DHT
  * OpenCV
  * face_recognition
  * imutils
  * pickle
  * time
  * cv2
  * requests
