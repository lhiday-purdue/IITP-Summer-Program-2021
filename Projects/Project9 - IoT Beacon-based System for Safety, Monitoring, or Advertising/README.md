# Beacon-Based IoT System for Use in the Office: Monitor User’s Location and Alert an Emergency
### ■&nbsp;&nbsp;Project Introduction
▣ This project is part of the IITP Summer Program, and **Korean universities & Purdue university** participated together.<br>
▣ The members consist of 6 **Pusan National University** students and 1 **Purdue University** student.<br>
▣ This project's goal is to improve the problems of previous disaster prevention systems using IoT technology.<br>
<br>
_It has the following advantages:_
#### **1. Accurate information collection and secure information reliability through a separate network.**
- It has a wider sensing distance than Bluetooth.
- Detailed location information can be identified.


#### **2. A low unit price compared to previous communication technology.**
- It can be maintained for a long time with low power using Bluetooth Low Energy(BLE)
- It is advantageous for real-time large data processing.

### ■&nbsp;&nbsp;Project Differentiation
- Through constant interaction with Beacon, users' information is updated periodically.
- This information is used to control Personnel and to figure out people locate in disaster accidents in real time.
- It is expected to reduce bottlenecks and panics that have increased the scale of disasters.

### ■&nbsp;&nbsp; Components
#### Hardware
- Raspberry Pi 3B
- iBeacon
- Flame sensor
- MQ-2 gas sensor

<img src = "https://user-images.githubusercontent.com/80534651/130169277-9f46ea48-cf96-4196-932a-31eb9599b0d6.jpg" width="450px">

#### Software
- Firebase - Server, Database
- Python - Sensor Processing
- Java - Android Mobile Application

### ■&nbsp;&nbsp; Installation
```bash
sudo pip install firebase-admin
sudo pip install json
sudo pip install requests
sudo apt-get install python-rpi.gpio
sudo pip install spidev
```

### ■&nbsp;&nbsp; Team Members
- [Sumin Sohn](https://github.com/sonmansu) (Pusan National University)  
- [Taeyeon Kim](https://github.com/kimty103) (Pusan National University)  
- [Jinhyeok Jeon](https://github.com/jinhyeok0204) (Pusan National University)  
- Sean Gomez (Purdue University)  
- Seockwoo Lee (Pusan National University)  
- [Taehoon Ha](https://github.com/Hooni-27) (Pusan National University)  
- Byeongjin Kim (Pusan National University)  
