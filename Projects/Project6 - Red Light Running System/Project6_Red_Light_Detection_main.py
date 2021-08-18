import threading
from concurrent.futures import thread
from multiprocessing import Process

import time
import cv2
import numpy as np
from playsound import playsound
from PIL import Image

prevTime = 0
red_img = cv2.imread('traffic red.png')
driver_img = cv2.imread('driver.png')
cv2.namedWindow('test title')

ip_address = 'http://192.168.0.138:8080/video'
cap = cv2.VideoCapture('traffic light.mp4')

whT = 320
confThreshold = 0.5
nmsThreshold = 0.3

classesFile = 'coco.names'
classNames = []
with open(classesFile, 'rt') as f:
    classNames = f.read().rstrip('\n').split('\n')
print(classNames)
print(len(classNames))

modelConfiguration = 'yolov3-tiny.cfg'
modelWeights = 'yolov3-tiny.weights'

net = cv2.dnn.readNetFromDarknet(modelConfiguration, modelWeights)
net.setPreferableBackend(cv2.dnn.DNN_BACKEND_OPENCV)
net.setPreferableTarget(cv2.dnn.DNN_TARGET_CPU)

def alert():
    threading.Thread(target=playsound, args=('sound.wav',), daemon=True).start()


def check_color_pattern(frame):
    img_hsv=cv2.cvtColor(frame,cv2.COLOR_BGR2HSV)

    lower_red=(-10,100,100)
    upper_red=(10,255,255)

    img_mask=cv2.inRange(img_hsv,lower_red,upper_red)

    img_result=cv2.bitwise_and(frame,frame,mask=img_mask)

    cv2.imshow('frame',frame)
    cv2.imshow('img_mask',img_mask)
    cv2.imshow('img_result',img_result)
    #cv2.moveWindow('frame', 1100, 400)

    pixels = cv2.countNonZero(img_mask)
    if pixels > 0:
        print("red exist")
        return 'red'
    else:
        print("not found")
        return 'no'


def findObjects(outputs, img):
    hT, wT, cT = img.shape
    bbox = []
    classIds = []
    confs = []

    for output in outputs:
        for det in output:
            scores = det[5:]
            classId = np.argmax(scores)
            confidence = scores[classId]
            if confidence > confThreshold:
                w,h = int(det[2]*wT) , int(det[3]*hT)
                x,y = int((det[0]*wT)-w/2), int((det[1]*hT)-h/2)
                bbox.append([x,y,w,h])
                classIds.append(classId)
                confs.append(float(confidence))
    indices = cv2.dnn.NMSBoxes(bbox, confs, confThreshold, nmsThreshold)
    #print(indices)
    cv2.imshow('test title', driver_img)
    for i in indices:
        i = i[0]
        box = bbox[i]
        x,y,w,h = box[0], box[1], box[2], box[3]
        if (classIds[i]==9):
            cv2.rectangle(img, (x, y), (x+w, y+h), (255,0,255),2)
            cv2.putText(img, f'{classNames[classIds[i]].upper()} {int(confs[i]*100)}%',
                    (x,y-10), cv2.FONT_HERSHEY_SIMPLEX,0.6,(255,0,255),2)
            print(w,h)
            trafficLight_red_roi=img[y:y+int(h/3),x:x+w]
            trafficLight=img[y:y+h,x:x+w]
            cv2.imshow('img',trafficLight)
            if ((w>=80) &  (h>=150)):
                light=check_color_pattern(trafficLight_red_roi)
                if light == 'red':
                    alert()
                    cv2.imshow('test title', red_img)

while True:
    success, img = cap.read()

    curTime = time.time()
    sec = curTime - prevTime
    prevTime = curTime

    fps = 1/(sec)

    str = "FPS: %0.1f" % fps
    cv2.putText(img, str, (0,100), cv2.FONT_HERSHEY_SIMPLEX, 1, (0,255,0))
    blob = cv2.dnn.blobFromImage(img, 1/255, (whT, whT), [0,0,0], 1, crop=False)
    net.setInput(blob)

    layerNames = net.getLayerNames()
    #print(layerNames)

    outputNames = [layerNames[i[0]-1] for i in net.getUnconnectedOutLayers()]
    outputs = net.forward(outputNames)
    #print(outputs[0].shape)
    #print(outputs[1].shape)
    #print(outputs[2].shape)
    #print(outputs[0][0])

    findObjects(outputs, img)

    cv2.imshow('Image', img)
    cv2.waitKey(1)