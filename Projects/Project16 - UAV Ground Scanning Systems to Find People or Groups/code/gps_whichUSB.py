#!/usr/bin/env python
import serial
import datetime
import os
gps_con = 0

while gps_con == 0:
   if os.path.exists('/dev/ttyUSB0') == True:
      ser = serial.Serial('/dev/ttyUSB0',4800,timeout = 10)
      gps_con = 1
      print("GPS on USB0")

   elif os.path.exists('/dev/ttyUSB1') == True:
      ser = serial.Serial('/dev/ttyUSB1',4800,timeout = 10)
      gps_con = 1
      print("GPS on USB1")

while True:
     
   gps = ser.readline()
   print(gps)
####the output is 0, but it should 1 in our case
