import cv2
import numpy as np

cap = cv2.VideoCapture(0)
cap.set(set.CAP_PROP_FRAME_WIDTH, 1280)
cap.set(set.CAP_PROP_FRAME_HEIGHT, 720)

while True:
    ret, frame = cap.read()
    cv2.imshow('frmae', frame)
    if cv2.waitkey(1) & 0xFF == ord('q'):
        break

cap.release()
cv2.destoryAllWindows()
