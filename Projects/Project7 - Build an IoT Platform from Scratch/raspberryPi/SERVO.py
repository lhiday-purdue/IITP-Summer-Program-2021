import RPi.GPIO as GPIO
from time import sleep
import paho.mqtt.client as mqtt
import sys

servo =17
GPIO.setmode(GPIO.BCM)
GPIO.setup(servo, GPIO.OUT)
pwm=GPIO.PWM(servo, 50)

pwm.start(0)


    
def SetAngle(angle):
    duty = angle/18+2
    GPIO.output(servo, True)
    pwm.ChangeDutyCycle(duty)
    sleep(2)
    GPIO.output(servo, False)
    pwm.ChangeDutyCycle(0)

def on_connect(client, userdata, flags, rc): 
  print ("Connected with" + str(rc)) 
  client.subscribe("test/door") 


def on_message(client, userdata, msg): 
    print(msg.topic) 
    print(str(msg.payload)) 
    if str(msg.payload) =="b'open'": 
        print("open")
        SetAngle(90)
        SetAngle(0)#on
    if str(msg.payload) =="b'close'": 
        print("close")
        SetAngle(0)
        SetAngle(90)#off
      



broker_ip="143.198.112.40"
mqttc = mqtt.Client("")
mqttc.tls_set('/usr/share/ca-certificates/extra/ca.crt')
mqttc.tls_insecure_set(True)
mqttc.username_pw_set("root","7smartHome")

mqttc.on_connect = on_connect  
mqttc.on_message = on_message   

mqttc.connect(broker_ip, 8883, 60)
mqttc.loop_forever()

pwm.stop()
GPIO.cleanup()

