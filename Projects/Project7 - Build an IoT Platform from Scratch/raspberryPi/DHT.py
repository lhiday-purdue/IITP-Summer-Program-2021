import Adafruit_DHT as dht
from time import sleep
import paho.mqtt.client as mqtt
import sys

DHT=4

broker_ip="143.198.112.40"
#broker_ip = "https://www.ultra7.xyz:1880/admin"

#client ID
mqttc = mqtt.Client("dht_test")
mqttc.tls_set('/usr/share/ca-certificates/extra/ca.crt')
mqttc.tls_insecure_set(True)
mqttc.username_pw_set("root","7smartHome")
mqttc.connect(broker_ip, 8883, 60)
mqttc.loop()

while True:
    try:
        #read Temperature and Humidity  value
        hum, temp = dht.read_retry(dht.DHT22, DHT)
        payload_hum = str(hum)
        payload_temp = str(temp)

        if hum is not None and temp is not None : 
            #send MQTT message
            print('Temp = {0:0.1f}*C Humidity = {1:0.1f}%'.format(temp, hum))
            mqttc.publish("dht22_hum",payload_hum)
            mqttc.publish("dht22_temp",payload_temp)
        
        #wait 5 sec
        sleep(5)

    #when systemExit or keyboard interrupt Exit
    except( SystemExit, KeyboardInterrupt) :
        mqttc.disconnect()
        sys.exit()
