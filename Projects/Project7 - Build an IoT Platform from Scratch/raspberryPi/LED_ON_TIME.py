import time
import paho.mqtt.client as mqtt
import RPi.GPIO as GPIO
from datetime import datetime

def on_message(client, userdata, message):
	received_msg = str(message.payload.decode("utf-8"))
	
	print("from topic", message.topic)
	print("message received", received_msg)
	if message.topic == topic1:
		control_led(led_light1, received_msg)
	if message.topic == topic2:
		control_led(led_light2, received_msg)

def on_connect(client, userdata, flags, rc):
	if rc==0:
		print("connected ok")
	else:
		print("connection ERROR", rc)

def control_led(led_num, msg):
	idx=led_num-11
	if msg == "TurnON":
		if led_flag[idx] == False:
			GPIO.output(led_num, True)
			led_flag[idx] = True
			led_start[idx]=datetime.now() # record led turn on time
			print("start : ", led_start[idx])
			
	if msg == "TurnOFF":
		if led_flag[idx] == True:
			GPIO.output(led_num, False)
			led_flag[idx] = False
			led_end[idx]=datetime.now() # record led turn off time
			print("end : ", led_end[idx])
			led_on_time=led_end[idx]-led_start[idx] # calculate how long led was turned on
			my_msg=led_on_time.seconds
			client.publish(led_topic[idx], my_msg)
			print("Transmission complete successfully", my_msg, "sec")

GPIO.setmode(GPIO.BOARD)

led_light1=11
led_light2=12
GPIO.setup(led_light1,GPIO.OUT)
GPIO.setup(led_light2,GPIO.OUT)

broker_address="143.198.112.40"
port=8883
ca_file_path="/home/pi/project7/ca.crt"

notice_topic="project7/notice" # for listen when node-red or sensors connected
topic1="myled1" # for subscribing
topic2="myled2" # for subscribing
led_topic=["project7/ledTime1","project7/ledTime2"] # for publishing

led_flag=[False, False]
led_start=[0,0]
led_end=[0,0]

print("creating new instance")
client=mqtt.Client("project7_led")
client.tls_set(ca_file_path)
client.tls_insecure_set(True)
client.on_message=on_message
client.on_connect=on_connect

print("connecting to broker")
client.connect(broker_address,port)

print("subscribing to Notice Topic")
client.subscribe(notice_topic)
print("subscribing to topic", topic1)
client.subscribe(topic1)
print("subscribing to topic", topic2)
client.subscribe(topic2)

try:
	client.loop_start()
	while True:
		pass
		
except KeyboardInterrupt:
	print("Keyboard Interrupt")
except:
	print("Something Wrong")
finally:
	print("Cleaned up, Terminating")
	time.sleep(3)
	client.loop_stop()
	client.disconnect()
	GPIO.cleanup()
