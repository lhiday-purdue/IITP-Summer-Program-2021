# -*- coding: utf-8 -*-
"""
Created on Wed Apr 21 20:55:16 2021
@author: Jeong-Daniel
"""

#import os
#import re
import time
import cv2
#import numpy as np
#from os.path import isfile, join
start = time.time()

import serial
import pynmea2

port = '/dev/ttyAMA0'
ser = serial.Serial(port, baudrate=9600)
print("serial connect")

import pymysql

conn = None
cur = None

sql = ""

conn = pymysql.connect(host='192.168.0.6', user='root', password='project17', db='project17', charset='utf8')
cur = conn.cursor()
sql = 'CREATE TABLE IF NOT EXISTS TreeTable (latitude FLOAT, longitude FLOAT, TreeNumber int)'
cur.execute(sql)
conn.commit()
conn.close()

tree_classifier = cv2.CascadeClassifier('cascade.xml')
cap = cv2.VideoCapture('/home/pi/Desktop/project/video/TEMP.MP4')
#out = cv2.VideoWriter('results.avi',cv2.VideoWriter_fourcc(*'XVID'), 20, (1280, 720))

while True:
    time.sleep(.05)
    ret, frame = cap.read()

    if ret is True:
        gray = cv2.cvtColor(frame, cv2.COLOR_BGR2GRAY)
        trees = tree_classifier.detectMultiScale(gray, 1.3, 5)
        for (x, y, w, h) in trees:
            image = cv2.rectangle(frame, (x, y), (x+w, y+h), (0,0,255), 2)
            cv2.namedWindow('Trees',cv2.WINDOW_NORMAL)
            cv2.putText(image, 'Tree', (x, y - 10), cv2.FONT_HERSHEY_SIMPLEX, 0.7, (0, 0, 255), 2)
            cv2.imshow('Trees', image)
            cv2.resizeWindow('Trees', 1280,720)
            #out.write(image)
            cv2.waitKey(1)
            
        if trees is True:
            data = ser.readline().decode('utf-8')
            while True:
                if data[0:6] == '$GPGGA':
                    msg = pynmea2.parse(data)
                    latval = msg.latitude
                    longval = msg.longitude
                    sql = "INSERT INTO TreeTable VALUES('" + latval  + "','" + longval + "','" + len(trees) + "')"
                    cur.execute(sql)
                    conn.commit()
                    conn.close()
                    break
    else:
        break

#out.release()
cap.release()
cv2.destroyAllWindows()
print("time :", time.time() - start)