import RPi.GPIO as GPIO 
import time 
import paho.mqtt.client as mqtt
import sys
 
led1 =23 #led GPIO 23
broker_ip="test.mosquitto.org"
 
GPIO.setmode(GPIO.BCM)  
GPIO.setup(23, GPIO.OUT) 
 
def on_connect(client, userdata, flags, rc): 
  print ("Connected with" + str(rc)) 
  client.subscribe("test/led1") 
 
def on_message(client, userdata, msg): 
    print(msg.topic) 
    print(str(msg.payload)) 
    if str(msg.payload) =="b'on'": 
        GPIO.output(led1, GPIO.HIGH) #on
    if str(msg.payload) =="b'off'": 
        GPIO.output(led1, GPIO.LOW) #off
 
 
mqttc = mqtt.Client()
mqttc2 = mqtt.Client() 

mqttc2.tls_set('/usr/share/ca-certificates/extra/ca.crt')
mqttc.tls_insecure_set(True)
mqttc.username_pw_set("root","7smartHome")

mqttc2.username_pw_set("root","7smartHome")

mqttc.on_connect = on_connect     
mqttc.on_message = on_message
mqttc2.on_connect = on_connect     
mqttc2.on_message = on_message

mqttc.connect(broker_ip, 8883, 60)
  
mqttc2.connect("143.198.112.40", 8883, 60)

mqttc.loop_forever()
mqttc2.loop_forever()