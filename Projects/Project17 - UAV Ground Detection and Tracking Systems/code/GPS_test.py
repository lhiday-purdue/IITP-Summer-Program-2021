import serial
import pynmea2
import time
#import libary

port = '/dev/ttyAMA0'
ser = serial.Serial(port, baudrate=9600)
print("serial connect")
#Using RPi UART 0 PORT
#import port and bitrate 9600, not timeout)

while True:
    data = ser.readline().decode('utf-8')
    #Import GPS data from serial port GPS
    
    # GPGGA is Global Positioning system fix data
    if data[0:6] == '$GPGGA':
        #parseing gps data, and decode string
        msg = pynmea2.parse(data)
        #latitude
        latval = msg.latitude
        #longitude
        longval = msg.longitude
        print('latitude :', latval)
        print('longtidue :', longval)

    time.sleep(0.5)
    
"""
while True:
    data = ser.readline()
    temp = str(data)
    if temp[2:8] == '$GPGGA':
        print(temp)
    time.sleep(0.5)
"""
