https://github.com/lhiday-purdue/Project4-IoT-Farm-Energy-Management-System
# Remote Solar Panel Managing Application
> Application that provides a way to manage the remote
Photovoltaic(PV) solar panels in real-time

<br/>
<br/>

## Link
[Application Link](http://18.116.64.150:1880/ui/)

<br/>
<br/>

## BackGround
Amongst renewable energy sources, solar power energy is gaining popularity thanks to low carbon dioxide emission and increasing efficiency. 

Photovoltaic(PV) solar panels are one of the ways to harness solar power into renewable energy with only a major initial cost but low maintenance. 

However, due to the tendency of solar energy systems being far away from the electricity users, energy health monitoring in real time and maintaining recovery from faults is the key goal. 

Our Application provide a way to interact and maintain an off-grid solar energy system from many miles away based on IOT Technolo
<br/>
<br/>


## Environment Setup


<br/>
<br/>

### Solar Panel Related Equipment
- Rasberry Pi
- Photovoltaic Solar Panel
- Epever Xtra3210N ( MPPT Solar Charge Controller )
- 12v 2a Lead-Acid Battery
- Cat. 5e Cable
- Pin Layout Of CAT.5e Cable
- Usb to R5485 module

![](./README_Images/RasberrypiEquipment.png)


<br/>
<br/>

### Rasberry pi

> Prepare latest version of Python3 and several modules installed mentioned below

```python
certifi        2021.5.30
dnspython      1.16.0
minimalmodbus  1.0.2
pip            21.2.4
pymodbus       2.5.2
pymongo        3.12.0
pyserial       3.5
six            1.16.0
```
<br/>
<br/>

> Running the Script

[1]. Your PC or raspberry pi should be connected to your solar charge controller via USB to RS485 cable prior to running the script.

[2]. If you are a windows user, the USB to RS485 module we used requires CH341 driver to be installed in order to recognition on Windows.

Make sure to install the driver before connecting the cable. 

_please refer to the [Installing the Driver][https://learn.sparkfun.com/tutorials/how-to-install-ch340-drivers/all]._


<br/>

[3]. The Database address written in the 'prototype.py' script won't work since invalid id and password are written.

If you are to use database address that starts with 'mongodb+srv://', Install pymongo[srv] 

```python
python3 -m pip install pymongo[srv]
```

[4]. After all modules and driver installations are complete, you are ready to run the script

[5]. Open 'prototype.py' and run the module. 

<br/>
<br/>

### Setting Up AWS Ubuntu EC2

> Buy AWS Ubuntu EC2 Instance

![](./README_Images/UbuntuBuy1.png)

<br/>
<br/>

> DownLoad your .pem key for Instance

![](./README_Images/UbuntuPemKey.png)

<br/>
<br/>

> Edit Inbound Rule 

You have to open the port 1880 for Ip from anywhere
![](./README_Images/UbuntuEditBoundRule.png)



<br/>
<br/>

### Running Node-Red on AWS EC2 with Ubuntu

> Enter your AWS Instance
```git
ssh -i "pem key" ubuntu@"aws public IP"
```
<br/>
<br/>

> Make Virtual Environment Folder
```python
# setting up locale to prevent erro 
export LC_ALL="en_US.UTF-8"
export LC_CTYPE="en_US.UTF-8"

sudo dpkg-reconfigure locales
# All locales --> en_US.UTF-8

sudo apt-get update

# make virtual folder 
python3 -m venv ./myenv
```

<br/>
<br/>

> Activate Virtual Environment
```git
. myenv/bin/activate
```

<br/>
<br/>

> Setup Node-Red

```git
curl -sL https://deb.nodesource.com/setup_12.x | sudo -E bash -
sudo apt-get install -y nodejs build-essential
sudo npm install -g --unsafe-perm node-red
```

<br/>
<br/>

> Get Node-Red to start automatically
whenever aws instance is restarted

```git
sudo npm install -g --unsafe-perm pm2
pm2 start `which node-red` -- -v
pm2 save
pm2 startup
```

<br/>
<br/>

> start Node-Red in your instance

```git
node-red
```

_For more examples and usage, please refer to the [Running on AWS EC2 with Ubuntu][https://nodered.org/docs/getting-started/aws]._

<br/>
<br/>




## Flow Diagram
![](./README_Images/flowchart.png)


<br/>
<br/>

## Usage Example

### Solar Panel Controller, Rasberry Pi, MongoDB

There are mainly 2 processes 

1) Extract Solar Panel Info from Controller to Rasberry Pi
2) Insert Extracted Info into MongoDB Database

```python

# This is the main script to read all neccessary registers from EPEVER XTRA and upload the datas to MongoDB
import pprint
import minimalmodbus
import serial
import time
import datetime
from epever_registers import *
import pymongo
import certifi

#  Connect to MongoDB/Cluster0/test/limitTen
ca = certifi.where()
client = pymongo.MongoClient("Your Mongodb Url", tlsCAFile=ca)
db = client.test
collection = db["limitTen"] 

# RS-485 Modbus Connection Check
while True:
    try:
        # (1)The ID of the controller is 1 by default and can be modified by PC software(Solar Station Monitor) or remote meter MT50.
        XTRA3210N = minimalmodbus.Instrument(port='COM1', slaveaddress=1, mode=minimalmodbus.MODE_RTU)

        # (2)The serial communication parameters: 115200bps baudrate, 8 data bits, 1 stop bit and no parity,no handshaking
        XTRA3210N.serial.baudrate = 115200
        XTRA3210N.serial.stopbits = 1
        XTRA3210N.serial.bytesize = 8
        XTRA3210N.serial.parity = serial.PARITY_NONE
        XTRA3210N.serial.timeout = 1

        XTRA3210N.clear_buffers_before_each_transaction = True
        break
    except serial.SerialException:
        print("Connection failure. Check cable connection")
        print("Retry connection in...", end=' ')
        for i in range(10, 0, -1):
            print(i, end=' ', flush=True)
            time.sleep(1)


# (3)The register address below is in hexadecimal format.
# (4)For the data with the length of 32 bits, such as power, using the L and H registers represent the low and high 16 bits value,respectively. 
# e.g.The charging input rated power is actually 3000W, multiples of 100 times, then the value of 0x3002 register is 0x93E0 and value of 0x3003 is 0x0004

print()

# print all rated data
print("***RATED DATA***")
for data in Rated_Data:
    while True:
        try:
            if data.address == CHARGING_MODE_:
                mode = XTRA3210N.read_register(data.address, 0, 4)
                if mode == 0:
                    print("\"{}\" : {}{}".format(data.name, "Connect|Disconnect", data.unit()[1]))
                elif mode == 1:
                    print("\"{}\" : {}{}".format(data.name, "PWM", data.unit()[1]))
                elif mode == 2:
                    print("\"{}\" : {}{}".format(data.name, "MPPT", data.unit()[1]))
                break
            else:
                if data.times == 100:
                    if data.size == 2:
                        high = XTRA3210N.read_register(data.address[1], 2, 4) * 65536
                        low = XTRA3210N.read_register(data.address[0], 2, 4)
                        print("\"{}\" : {}{}".format(data.name, high + low, data.unit()[1]))
                    else:
                        print("\"{}\" : {}{}".format(data.name, XTRA3210N.read_register(data.address, 2, 4), data.unit()[1]))
                if data.times == 1:
                    print("\"{}\" : {}{}".format(data.name, XTRA3210N.read_register(data.address, 0, 4), data.unit()[1]))
                break
        except minimalmodbus.NoResponseError:
            continue
        except KeyboardInterrupt:
                print("Monitoring halted by Keyboard Input")
                exit(0)

print()

# Count pre-saved documents in the database
numOfDocuments = collection.count_documents({})

# Add key and values to the dictionary and upload to MongoDB
while True: 

    # print the number of documents in the database collection
    print("\nNumber of Documents: {}\n".format(numOfDocuments))
    

    post = {}
    # print(str(datetime.datetime.now()))
    post["time"] = str(datetime.datetime.now())

    for data in Real_Time_Data:
        while True:
            try:
                if data.times == 100:
                    if data.size == 2:
                        high = XTRA3210N.read_register(data.address[1], 2, 4) * 65536
                        low = XTRA3210N.read_register(data.address[0], 2, 4)
                        # print("\"{}\" : \"{}\"{}".format(data.name, high + low, data.unit()[1]))
                        post[data.name] = high + low
                    else:
                        # print("\"{}\" : \"{}\"{}".format(data.name, XTRA3210N.read_register(data.address, 2, 4), data.unit()[1]))
                        post[data.name] = XTRA3210N.read_register(data.address, 2, 4)
                if data.times == 1:
                    # print("\"{}\" : \"{}\"{}".format(data.name, XTRA3210N.read_register(data.address, 0, 4), data.unit()[1]))
                    post[data.name] = XTRA3210N.read_register(data.address, 0, 4)

                break
            except minimalmodbus.NoResponseError:
                continue
            except KeyboardInterrupt:
                print("Monitoring halted by Keyboard Input")
                exit(0)
                

    for data in Stat_Param:
        while True:
            try:
                if data.size == 2:
                    high = XTRA3210N.read_register(data.address[1], 2, 4) * 65536
                    low = XTRA3210N.read_register(data.address[0], 2, 4)
                    # print("\"{}\" : \"{}\"{}".format(data.name, high + low, data.unit()[1]))
                    post[data.name] = high + low
                else:
                    # print("\"{}\" : \"{}\"{}".format(data.name, XTRA3210N.read_register(data.address, 2, 4), data.unit()[1]))
                    post[data.name] = XTRA3210N.read_register(data.address, 2, 4)

                break
            except minimalmodbus.NoResponseError:
                continue
            except KeyboardInterrupt:
                print("Monitoring halted by Keyboard Input")
                exit(0)


    # Battery Status 
    # 0: Normal
    # Charging Status
    # 1: Running/Not Charging, 7: Running/Float Charge, 11: Running/Boost Charge  
    # Dischargning Status  
    # 0: Standby, 1: Running
    for data in Real_Time_Status:
        while True:
            try:
                post[data.name] = XTRA3210N.read_register(data.address, 0, 4)
                break
            except minimalmodbus.NoResponseError:
                continue
            except KeyboardInterrupt:
                print("Monitoring halted by Keyboard Input")
                exit(0)

    # Upload data to the database and limit the number of documents
    collection.insert_one(post)
    numOfDocuments = collection.count_documents({})
    count = 0
    if numOfDocuments >= 10:
        for i in range(numOfDocuments-10):
            collection.delete_one({})
            count += 1
        print("{} document(s) deleted\n".format(count))

    try:
        pprint.pprint(post, sort_dicts=False)
        post.clear()
        time.sleep(20)
    except KeyboardInterrupt:
        print("Monitoring halted by Keyboard Input")
        exit(0)
    

        


```

<br/>
<br/>

### Node-Red

Node-RED displays data saved in MongoDB

The Node-RED pages consist of 1) Main tracer and 2) Gauge. 

Main tracer shows the real time data from the MPPT controller. 
Data mainly consist of Solar, Battery, DC Load, Controller, Energy Information. 
Real-time Voltage data of PV, Battery, DC Load are shown as Chart also so that use can compare them easily. 

Gauge shows outward humidity and temperature data. 
User can also see the real-time change of data with Chart below the page.
Displayed data is updated per 30 seconds, according to the timestamp. 
Most Recent data is shown every 30 seconds.


<br/>

> [1] Main Tracer

![](./README_Images/NodeRed_MainTracer.png)

<br/>
<br/>

> [2] Gauge

![](./README_Images/NodeRed_Gauge.png)

_For more examples and usage, please refer to the [Wiki][wiki]._

<br/>
<br/>


> [3] Sample Node-Red Code

![](./README_Images/NodeRedCode.png)


<br/>
<br/>

> Code
```js
[{"id":"f4c7e0778c61d5d4","type":"tab","label":"Solar Information","disabled":false,"info":""},{"id":"4f487ef734e059ae","type":"tab","label":"Battery Information","disabled":false,"info":""},{"id":"59daf32f25cbd1d2","type":"tab","label":"DC Load Information","disabled":false,"info":""},{"id":"6dd8aa40d8657a9e","type":"tab","label":"Controller Information","disabled":false,"info":""},{"id":"65fa3f57759f038b","type":"tab","label":"Energy Generated","disabled":false,"info":""},{"id":"e2e6a78e97bf63d1","type":"tab","label":"Energy Consumed","disabled":false,"info":""},{"id":"b1badf938559d63b","type":"tab","label":"Real Time Curve","disabled":false,"info":""},{"id":"4806778d1b1de567","type":"tab","label":"Temperature","disabled":false,"info":""},{"id":"f2d3dabc953c2103","type":"tab","label":"Humidity","disabled":false,"info":""},{"id":"0be0a18b974421a4","type":"ui_base","theme":{"name":"theme-light","lightTheme":{"default":"#0094CE","baseColor":"#0094CE","baseFont":"-apple-system,BlinkMacSystemFont,Segoe UI,Roboto,Oxygen-Sans,Ubuntu,Cantarell,Helvetica Neue,sans-serif","edited":true,"reset":false},"darkTheme":{"default":"#097479","baseColor":"#097479","baseFont":"-apple-system,BlinkMacSystemFont,Segoe UI,Roboto,Oxygen-Sans,Ubuntu,Cantarell,Helvetica Neue,sans-serif","edited":false},"customTheme":{"name":"Untitled Theme 1","default":"#4B7930","baseColor":"#4B7930","baseFont":"-apple-system,BlinkMacSystemFont,Segoe UI,Roboto,Oxygen-Sans,Ubuntu,Cantarell,Helvetica Neue,sans-serif"},"themeState":{"base-color":{"default":"#0094CE","value":"#0094CE","edited":false},"page-titlebar-backgroundColor":{"value":"#0094CE","edited":false},"page-backgroundColor":{"value":"#fafafa","edited":false},"page-sidebar-backgroundColor":{"value":"#ffffff","edited":false},"group-textColor":{"value":"#1bbfff","edited":false},"group-borderColor":{"value":"#ffffff","edited":false},"group-backgroundColor":{"value":"#ffffff","edited":false},"widget-textColor":{"value":"#111111","edited":false},"widget-backgroundColor":{"value":"#0094ce","edited":false},"widget-borderColor":{"value":"#ffffff","edited":false},"base-font":{"value":"-apple-system,BlinkMacSystemFont,Segoe UI,Roboto,Oxygen-Sans,Ubuntu,Cantarell,Helvetica Neue,sans-serif"}},"angularTheme":{"primary":"indigo","accents":"blue","warn":"red","background":"grey","palette":"light"}},"site":{"name":"Node-RED Dashboard","hideToolbar":"false","allowSwipe":"false","lockMenu":"false","allowTempTheme":"true","dateFormat":"DD/MM/YYYY","sizes":{"sx":48,"sy":48,"gx":10,"gy":10,"cx":6,"cy":6,"px":6,"py":6}}},{"id":"212bc13ef8625513","type":"ui_tab","name":"Gauge","icon":"dashboard","order":3,"disabled":false,"hidden":false},{"id":"e8d5b0b6cf6e034b","type":"ui_tab","name":"Main Tracer","icon":"dashboard","order":1,"disabled":false,"hidden":false},{"id":"afa06d464482da5a","type":"ui_group","name":"Humidity","tab":"212bc13ef8625513","order":3,"disp":true,"width":"7","collapse":false},{"id":"db53270c289713c0","type":"ui_group","name":"Temperature","tab":"212bc13ef8625513","order":4,"disp":true,"width":"7","collapse":false},{"id":"fc0c1b893dbe831c","type":"ui_group","name":"Information","tab":"212bc13ef8625513","order":1,"disp":true,"width":"4","collapse":false},{"id":"2ce0376077df9e91","type":"ui_group","name":"Battery Information","tab":"e8d5b0b6cf6e034b","order":3,"disp":true,"width":"4","collapse":false},{"id":"956cd2984aafd6f8","type":"ui_group","name":"DC Load Information","tab":"e8d5b0b6cf6e034b","order":5,"disp":true,"width":"4","collapse":false},{"id":"11f2ac9c84a47d2d","type":"ui_group","name":"Controller Information","tab":"e8d5b0b6cf6e034b","order":6,"disp":true,"width":"5","collapse":false},{"id":"7b24092d05298e03","type":"ui_group","name":"Energy Generated (k-Wh)","tab":"e8d5b0b6cf6e034b","order":7,"disp":true,"width":"6","collapse":false},{"id":"7d1564c81cfbb80d","type":"ui_group","name":"Energy Consumed(k-Wh)","tab":"e8d5b0b6cf6e034b","order":8,"disp":true,"width":"6","collapse":false},{"id":"f71beaa233afc11f","type":"ui_group","name":"Real Time Curve","tab":"e8d5b0b6cf6e034b","order":9,"disp":true,"width":"10","collapse":false},{"id":"6890b3adef823566","type":"mongodb3","uri":"mongodb+srv://lee:mineral9!!@cluster0.kii4s.mongodb.net/test?retryWrites=true&w=majority","name":"DB","options":"","parallelism":"-1"},{"id":"db3fde717fc0d1d8","type":"ui_spacer","z":"6dd8aa40d8657a9e","name":"spacer","group":"11f2ac9c84a47d2d","order":3,"width":1,"height":"2"},{"id":"c33b984bbf39704b","type":"ui_spacer","z":"6dd8aa40d8657a9e","name":"spacer","group":"","order":1,"width":1,"height":1},{"id":"d21194c95c74cfac","type":"ui_spacer","z":"4806778d1b1de567","name":"spacer","group":"","order":2,"width":1,"height":1},{"id":"29ffbe7b344b18a4","type":"mui_base","theme":{"name":"theme-light","lightTheme":{"default":"#0094CE","baseColor":"#0094CE","baseFont":"-apple-system,BlinkMacSystemFont,Segoe UI,Roboto,Oxygen-Sans,Ubuntu,Cantarell,Helvetica Neue,sans-serif","edited":true,"reset":false},"darkTheme":{"default":"#097479","baseColor":"#097479","baseFont":"-apple-system,BlinkMacSystemFont,Segoe UI,Roboto,Oxygen-Sans,Ubuntu,Cantarell,Helvetica Neue,sans-serif","edited":false},"customTheme":{"name":"Untitled Theme 1","default":"#4B7930","baseColor":"#4B7930","baseFont":"-apple-system,BlinkMacSystemFont,Segoe UI,Roboto,Oxygen-Sans,Ubuntu,Cantarell,Helvetica Neue,sans-serif"},"themeState":{"m-base-color":{"default":"#0094CE","value":"#0094CE","edited":false},"m-page-titlebar-backgroundColor":{"value":"#0094CE","edited":false},"m-page-backgroundColor":{"value":"#fafafa","edited":false},"m-page-sidebar-backgroundColor":{"value":"#ffffff","edited":false},"m-group-textColor":{"value":"#1bbfff","edited":false},"m-group-borderColor":{"value":"#ffffff","edited":false},"m-group-backgroundColor":{"value":"#ffffff","edited":false},"m-widget-textColor":{"value":"#111111","edited":false},"m-widget-backgroundColor":{"value":"#0094ce","edited":false},"m-widget-borderColor":{"value":"#ffffff","edited":false},"base-font":{"value":"-apple-system,BlinkMacSystemFont,Segoe UI,Roboto,Oxygen-Sans,Ubuntu,Cantarell,Helvetica Neue,sans-serif"}},"angularTheme":{"primary":"indigo","accents":"blue","warn":"red","background":"grey"}},"site":{"name":"Node-RED Dashboard","hideToolbar":"false","allowSwipe":"false","lockMenu":"false","allowTempTheme":"true","dateFormat":"DD/MM/YYYY","sizes":{"sx":48,"sy":48,"gx":6,"gy":6,"cx":6,"cy":6,"px":0,"py":0}}},{"id":"f690278b422b2b5b","type":"ui_group","name":"","tab":"e8d5b0b6cf6e034b","order":4,"disp":true,"width":"4","collapse":false},{"id":"3d51e080f3b6b360","type":"ui_spacer","z":"65fa3f57759f038b","name":"spacer","group":"","order":4,"width":"4","height":1},{"id":"880cc37e094fd321","type":"ui_group","name":"Solar Information","tab":"e8d5b0b6cf6e034b","order":1,"disp":true,"width":"4","collapse":false},{"id":"d3a9c3bb3e602858","type":"ui_gauge","z":"4806778d1b1de567","name":"","group":"db53270c289713c0","order":1,"width":0,"height":0,"gtype":"donut","title":"Temperature","label":"°C","format":"{{value}}","min":"-100","max":"200","colors":["#00b500","#e6e600","#ca3838"],"seg1":"","seg2":"","x":530,"y":80,"wires":[]},{"id":"2eb37acafdaab4c1","type":"ui_chart","z":"4806778d1b1de567","name":"","group":"db53270c289713c0","order":2,"width":0,"height":0,"label":"chart","chartType":"line","legend":"false","xformat":"HH:mm:ss","interpolate":"linear","nodata":"","dot":false,"ymin":"","ymax":"","removeOlder":1,"removeOlderPoints":"","removeOlderUnit":"3600","cutout":0,"useOneColor":false,"useUTC":false,"colors":["#1f77b4","#aec7e8","#ff7f0e","#2ca02c","#98df8a","#d62728","#ff9896","#9467bd","#c5b0d5"],"outputs":1,"useDifferentColor":false,"x":510,"y":120,"wires":[[]]},{"id":"9b03ed93b9f3ae31","type":"inject","z":"4806778d1b1de567","name":"timestamp","props":[{"p":"payload"},{"p":"topic","vt":"str"}],"repeat":"30","crontab":"","once":true,"onceDelay":0.1,"topic":"","payloadType":"date","x":110,"y":80,"wires":[["66d5339cad9379d9"]]},{"id":"66d5339cad9379d9","type":"function","z":"4806778d1b1de567","name":"FindAll","func":" var newMsg = {};\nnewMsg.operation  = 'findOne';\nnewMsg.sort = {timestamp : -1}\nreturn newMsg;","outputs":1,"noerr":0,"initialize":"","finalize":"","libs":[],"x":250,"y":80,"wires":[["e6485d71a289f192"]]},{"id":"f926a34648bcf179","type":"delay","z":"4806778d1b1de567","name":"","pauseType":"rate","timeout":"1","timeoutUnits":"seconds","rate":"1","nbRateUnits":"1","rateUnits":"second","randomFirst":"1","randomLast":"5","randomUnits":"seconds","drop":false,"allowrate":false,"x":250,"y":420,"wires":[["f830ff6a50055fb3","d3a9c3bb3e602858","2eb37acafdaab4c1"]]},{"id":"f830ff6a50055fb3","type":"debug","z":"4806778d1b1de567","name":"","active":true,"tosidebar":true,"console":false,"tostatus":false,"complete":"payload","targetType":"msg","statusVal":"","statusType":"auto","x":490,"y":180,"wires":[]},{"id":"372e7d452690a1e6","type":"ui_gauge","z":"f2d3dabc953c2103","name":"","group":"afa06d464482da5a","order":1,"width":0,"height":0,"gtype":"donut","title":"Humidity","label":"%","format":"{{value}}","min":"0","max":"100","colors":["#00b500","#e6e600","#ca3838"],"seg1":"","seg2":"","x":560,"y":40,"wires":[]},{"id":"b988f99b5ea9e8ee","type":"ui_chart","z":"f2d3dabc953c2103","name":"","group":"afa06d464482da5a","order":2,"width":0,"height":0,"label":"chart","chartType":"line","legend":"false","xformat":"HH:mm:ss","interpolate":"linear","nodata":"","dot":false,"ymin":"","ymax":"","removeOlder":1,"removeOlderPoints":"","removeOlderUnit":"3600","cutout":0,"useOneColor":false,"useUTC":false,"colors":["#1f77b4","#aec7e8","#ff7f0e","#2ca02c","#98df8a","#d62728","#ff9896","#9467bd","#c5b0d5"],"outputs":1,"useDifferentColor":false,"x":550,"y":120,"wires":[[]]},{"id":"4273f1ad76b36377","type":"inject","z":"f2d3dabc953c2103","name":"timestamp","props":[{"p":"payload"},{"p":"topic","vt":"str"}],"repeat":"30","crontab":"","once":true,"onceDelay":0.1,"topic":"","payloadType":"date","x":150,"y":60,"wires":[["6dd103b74cb46c39"]]},{"id":"6dd103b74cb46c39","type":"function","z":"f2d3dabc953c2103","name":"FindAll","func":" var newMsg = {};\nnewMsg.operation  = 'findOne';\nnewMsg.sort = {timestamp : -1}\nreturn newMsg;","outputs":1,"noerr":0,"initialize":"","finalize":"","libs":[],"x":290,"y":60,"wires":[["dfd54142f2dc0ff2"]]},{"id":"23899973d839b423","type":"delay","z":"f2d3dabc953c2103","name":"","pauseType":"rate","timeout":"1","timeoutUnits":"seconds","rate":"1","nbRateUnits":"1","rateUnits":"second","randomFirst":"1","randomLast":"5","randomUnits":"seconds","drop":false,"allowrate":false,"x":270,"y":360,"wires":[["b988f99b5ea9e8ee","02672f7be4086657","372e7d452690a1e6"]]},{"id":"02672f7be4086657","type":"debug","z":"f2d3dabc953c2103","name":"","active":true,"tosidebar":true,"console":false,"tostatus":false,"complete":"payload","targetType":"msg","statusVal":"","statusType":"auto","x":530,"y":160,"wires":[]},{"id":"f5461580a0c3f7b6","type":"ui_text","z":"f4c7e0778c61d5d4","group":"880cc37e094fd321","order":1,"width":0,"height":0,"name":"","label":"Solar Current (A)","format":"{{msg.payload}}","layout":"col-center","x":890,"y":100,"wires":[],"icon":"font-awesome/fa-dashboard"},{"id":"d834ef4bbffafb3b","type":"ui_text","z":"f4c7e0778c61d5d4","group":"880cc37e094fd321","order":2,"width":0,"height":0,"name":"","label":"Solar Volatage (V)","format":"{{msg.payload}}","layout":"col-center","x":890,"y":160,"wires":[]},{"id":"8b23541d2f1ee164","type":"inject","z":"f4c7e0778c61d5d4","name":"timestamp","props":[{"p":"payload"},{"p":"topic","vt":"str"}],"repeat":"30","crontab":"","once":true,"onceDelay":0.1,"topic":"","payloadType":"date","x":140,"y":100,"wires":[["6b32ac8b9adfeb32"]]},{"id":"8c49cef22c9af5a0","type":"ui_text","z":"4f487ef734e059ae","group":"2ce0376077df9e91","order":1,"width":0,"height":0,"name":"","label":"Battery Volatage (V)","format":"{{msg.payload}}","layout":"col-center","x":900,"y":60,"wires":[]},{"id":"468a308fd6698834","type":"inject","z":"4f487ef734e059ae","name":"timestamp","props":[{"p":"payload"},{"p":"topic","vt":"str"}],"repeat":"30","crontab":"","once":true,"onceDelay":0.1,"topic":"","payloadType":"date","x":110,"y":200,"wires":[["6ecc83bd89e7c19a"]]},{"id":"fa854f67617d36ad","type":"ui_text","z":"59daf32f25cbd1d2","group":"956cd2984aafd6f8","order":0,"width":0,"height":0,"name":"","label":"Load Current (A)","format":"{{msg.payload}}","layout":"col-center","x":870,"y":100,"wires":[],"icon":"font-awesome/fa-dashboard"},{"id":"ca218d955ebbb9fc","type":"ui_text","z":"59daf32f25cbd1d2","group":"956cd2984aafd6f8","order":0,"width":0,"height":0,"name":"","label":"Load Volatage (V)","format":"{{msg.payload}}","layout":"col-center","x":870,"y":160,"wires":[]},{"id":"9f3fb3944f7b053f","type":"ui_text","z":"59daf32f25cbd1d2","group":"956cd2984aafd6f8","order":0,"width":0,"height":0,"name":"","label":"Load Power (W)","format":"{{msg.payload}}","layout":"col-center","x":860,"y":220,"wires":[]},{"id":"7114b4b5105621f5","type":"inject","z":"59daf32f25cbd1d2","name":"timestamp","props":[{"p":"payload"},{"p":"topic","vt":"str"}],"repeat":"30","crontab":"","once":true,"onceDelay":0.1,"topic":"","payloadType":"date","x":110,"y":80,"wires":[["c5d308263d214be6"]]},{"id":"19a7d772a185642c","type":"inject","z":"6dd8aa40d8657a9e","name":"timestamp","props":[{"p":"payload"},{"p":"topic","vt":"str"}],"repeat":"1","crontab":"","once":true,"onceDelay":0.1,"topic":"","payloadType":"date","x":140,"y":80,"wires":[["4b348eb213c7e5c7"]]},{"id":"9d2ba8151114c5f5","type":"inject","z":"65fa3f57759f038b","name":"timestamp","props":[{"p":"payload"},{"p":"topic","vt":"str"}],"repeat":"30","crontab":"","once":true,"onceDelay":0.1,"topic":"","payloadType":"date","x":130,"y":100,"wires":[["dd224d12f7a3a948"]]},{"id":"9d1a97531e0fd37a","type":"ui_text","z":"65fa3f57759f038b","group":"7b24092d05298e03","order":1,"width":0,"height":0,"name":"","label":"Daily","format":"{{msg.payload}}","layout":"row-spread","x":890,"y":100,"wires":[]},{"id":"c5ee4cdc7155f0d6","type":"ui_text","z":"65fa3f57759f038b","group":"7b24092d05298e03","order":2,"width":0,"height":0,"name":"","label":"Monthly","format":"{{msg.payload}}","layout":"row-spread","x":900,"y":160,"wires":[]},{"id":"d5c6d06024209818","type":"ui_text","z":"65fa3f57759f038b","group":"7b24092d05298e03","order":3,"width":0,"height":0,"name":"","label":"Total","format":"{{msg.payload}}","layout":"row-spread","x":890,"y":280,"wires":[]},{"id":"9edb84eafa835346","type":"ui_text","z":"65fa3f57759f038b","group":"7b24092d05298e03","order":4,"width":0,"height":0,"name":"","label":"Annual","format":"{{msg.payload}}","layout":"row-spread","x":900,"y":220,"wires":[]},{"id":"424be9f7eecbac29","type":"inject","z":"e2e6a78e97bf63d1","name":"timestamp","props":[{"p":"payload"},{"p":"topic","vt":"str"}],"repeat":"30","crontab":"","once":true,"onceDelay":0.1,"topic":"","payloadType":"date","x":130,"y":120,"wires":[["2bfe663265c3ad87"]]},{"id":"5f70762087edb056","type":"ui_text","z":"e2e6a78e97bf63d1","group":"7d1564c81cfbb80d","order":1,"width":0,"height":0,"name":"","label":"Daily","format":"{{msg.payload}}","layout":"row-spread","x":930,"y":120,"wires":[]},{"id":"6a9a92a936aeccb7","type":"ui_text","z":"e2e6a78e97bf63d1","group":"7d1564c81cfbb80d","order":2,"width":0,"height":0,"name":"","label":"Monthly","format":"{{msg.payload}}","layout":"row-spread","x":940,"y":180,"wires":[]},{"id":"efe335bf108eca0b","type":"ui_text","z":"e2e6a78e97bf63d1","group":"7d1564c81cfbb80d","order":3,"width":0,"height":0,"name":"","label":"Total","format":"{{msg.payload}}","layout":"row-spread","x":930,"y":300,"wires":[]},{"id":"948c006a3d20bdc5","type":"ui_text","z":"e2e6a78e97bf63d1","group":"7d1564c81cfbb80d","order":4,"width":0,"height":0,"name":"","label":"Annual","format":"{{msg.payload}}","layout":"row-spread","x":940,"y":240,"wires":[]},{"id":"e6485d71a289f192","type":"mongodb3 in","z":"4806778d1b1de567","service":"_ext_","configNode":"6890b3adef823566","name":"Temperature","collection":"todos","operation":"","x":210,"y":180,"wires":[["61da0ea5a25c1c19","f830ff6a50055fb3"]]},{"id":"61da0ea5a25c1c19","type":"function","z":"4806778d1b1de567","name":"","func":"var newMsg = {};\nnewMsg.payload    = msg.payload.Temperature;\nreturn newMsg;\n","outputs":1,"noerr":0,"initialize":"","finalize":"","libs":[],"x":200,"y":300,"wires":[["f926a34648bcf179"]]},{"id":"2bfe663265c3ad87","type":"function","z":"e2e6a78e97bf63d1","name":"FindAll","func":" var newMsg = {};\nnewMsg.operation  = 'findOne';\nnewMsg.sort = {timestamp : -1}\nreturn newMsg;","outputs":1,"noerr":0,"initialize":"","finalize":"","libs":[],"x":310,"y":100,"wires":[["47dd4fcb2f43b3be"]]},{"id":"4e5bbbd67b707c1d","type":"delay","z":"e2e6a78e97bf63d1","name":"","pauseType":"rate","timeout":"1","timeoutUnits":"seconds","rate":"1","nbRateUnits":"1","rateUnits":"second","randomFirst":"1","randomLast":"5","randomUnits":"seconds","drop":false,"allowrate":false,"x":450,"y":220,"wires":[["62d8dd209f561f48","f6b6e6a517a98913","2e33f67f76a65a24","7cb10c9b75a56e87"]]},{"id":"47dd4fcb2f43b3be","type":"mongodb3 in","z":"e2e6a78e97bf63d1","service":"_ext_","configNode":"6890b3adef823566","name":"Temperature","collection":"todos","operation":"","x":470,"y":80,"wires":[["4e5bbbd67b707c1d"]]},{"id":"62d8dd209f561f48","type":"function","z":"e2e6a78e97bf63d1","name":"Consumed energy today","func":"var newMsg = {};\nnewMsg.payload    = msg.payload[\"Consumed energy today\"];\nreturn newMsg;\n","outputs":1,"noerr":0,"initialize":"","finalize":"","libs":[],"x":690,"y":120,"wires":[["5f70762087edb056"]]},{"id":"f6b6e6a517a98913","type":"function","z":"e2e6a78e97bf63d1","name":"Consumed energy this month","func":"var newMsg = {};\nnewMsg.payload    = msg.payload[\"Consumed energy this month\"];\nreturn newMsg;\n","outputs":1,"noerr":0,"initialize":"","finalize":"","libs":[],"x":700,"y":180,"wires":[["6a9a92a936aeccb7"]]},{"id":"7cb10c9b75a56e87","type":"function","z":"e2e6a78e97bf63d1","name":"Consumed energy Total","func":"var newMsg = {};\nnewMsg.payload    = msg.payload[\"Total consumed energy\"];\nreturn newMsg;\n","outputs":1,"noerr":0,"initialize":"","finalize":"","libs":[],"x":690,"y":300,"wires":[["efe335bf108eca0b"]]},{"id":"2e33f67f76a65a24","type":"function","z":"e2e6a78e97bf63d1","name":"Consumed energy this year","func":"var newMsg = {};\nnewMsg.payload    = msg.payload[\"Consumed energy this year\"];\nreturn newMsg;\n","outputs":1,"noerr":0,"initialize":"","finalize":"","libs":[],"x":700,"y":240,"wires":[["948c006a3d20bdc5"]]},{"id":"dd224d12f7a3a948","type":"function","z":"65fa3f57759f038b","name":"FindAll","func":" var newMsg = {};\nnewMsg.operation  = 'findOne';\nnewMsg.sort = {timestamp : -1}\nreturn newMsg;","outputs":1,"noerr":0,"initialize":"","finalize":"","libs":[],"x":270,"y":80,"wires":[["b1f1a4d87ed137f7"]]},{"id":"f8cde0fb01e4fe4c","type":"delay","z":"65fa3f57759f038b","name":"","pauseType":"rate","timeout":"1","timeoutUnits":"seconds","rate":"1","nbRateUnits":"1","rateUnits":"second","randomFirst":"1","randomLast":"5","randomUnits":"seconds","drop":false,"allowrate":false,"x":410,"y":200,"wires":[["f73c39f38e373d22","da2108fbf9e588e7","4ca26346e47d4c92","b368d0aa595fa5c7"]]},{"id":"b1f1a4d87ed137f7","type":"mongodb3 in","z":"65fa3f57759f038b","service":"_ext_","configNode":"6890b3adef823566","name":"DB","collection":"todos","operation":"","x":410,"y":60,"wires":[["f8cde0fb01e4fe4c"]]},{"id":"f73c39f38e373d22","type":"function","z":"65fa3f57759f038b","name":"Generated energy today","func":"var newMsg = {};\nnewMsg.payload    = msg.payload[\"Generated energy today\"];\nreturn newMsg;\n","outputs":1,"noerr":0,"initialize":"","finalize":"","libs":[],"x":650,"y":100,"wires":[["9d1a97531e0fd37a"]]},{"id":"da2108fbf9e588e7","type":"function","z":"65fa3f57759f038b","name":"Generated energy this month","func":"var newMsg = {};\nnewMsg.payload    = msg.payload[\"Generated energy this month\"];\nreturn newMsg;\n","outputs":1,"noerr":0,"initialize":"","finalize":"","libs":[],"x":660,"y":160,"wires":[["c5ee4cdc7155f0d6"]]},{"id":"b368d0aa595fa5c7","type":"function","z":"65fa3f57759f038b","name":"Generated energy Total","func":"var newMsg = {};\nnewMsg.payload    = msg.payload[\"Total generated energy\"];\nreturn newMsg;\n","outputs":1,"noerr":0,"initialize":"","finalize":"","libs":[],"x":650,"y":280,"wires":[["d5c6d06024209818"]]},{"id":"4ca26346e47d4c92","type":"function","z":"65fa3f57759f038b","name":"Generated energy this year","func":"var newMsg = {};\nnewMsg.payload    = msg.payload[\"Generated energy this year\"];\nreturn newMsg;\n","outputs":1,"noerr":0,"initialize":"","finalize":"","libs":[],"x":660,"y":220,"wires":[["9edb84eafa835346"]]},{"id":"4b348eb213c7e5c7","type":"function","z":"6dd8aa40d8657a9e","name":"FindAll","func":" var newMsg = {};\nnewMsg.operation  = 'findOne';\nnewMsg.sort = {timestamp : -1}\nreturn newMsg;","outputs":1,"noerr":0,"initialize":"","finalize":"","libs":[],"x":270,"y":180,"wires":[["815554161ef5a7d9"]]},{"id":"cb467c237e3ecba4","type":"delay","z":"6dd8aa40d8657a9e","name":"","pauseType":"rate","timeout":"1","timeoutUnits":"seconds","rate":"1","nbRateUnits":"1","rateUnits":"second","randomFirst":"1","randomLast":"5","randomUnits":"seconds","drop":false,"allowrate":false,"x":570,"y":180,"wires":[["9ffb22dae7455c01"]]},{"id":"815554161ef5a7d9","type":"mongodb3 in","z":"6dd8aa40d8657a9e","service":"_ext_","configNode":"6890b3adef823566","name":"DB","collection":"todos","operation":"","x":410,"y":180,"wires":[["cb467c237e3ecba4"]]},{"id":"c5d308263d214be6","type":"function","z":"59daf32f25cbd1d2","name":"FindAll","func":" var newMsg = {};\nnewMsg.operation  = 'findOne';\nnewMsg.sort = {timestamp : -1}\nreturn newMsg;","outputs":1,"noerr":0,"initialize":"","finalize":"","libs":[],"x":210,"y":180,"wires":[["64267ecb8eff1134"]]},{"id":"a30840805d09c525","type":"delay","z":"59daf32f25cbd1d2","name":"","pauseType":"rate","timeout":"1","timeoutUnits":"seconds","rate":"1","nbRateUnits":"1","rateUnits":"second","randomFirst":"1","randomLast":"5","randomUnits":"seconds","drop":false,"allowrate":false,"x":330,"y":240,"wires":[["8dbcb95d51f4d46a","1e8e3851adfd9595","a72784c38a64fcd0"]]},{"id":"64267ecb8eff1134","type":"mongodb3 in","z":"59daf32f25cbd1d2","service":"_ext_","configNode":"6890b3adef823566","name":"DB","collection":"todos","operation":"","x":350,"y":180,"wires":[["a30840805d09c525"]]},{"id":"8dbcb95d51f4d46a","type":"function","z":"59daf32f25cbd1d2","name":"Load Current","func":"var newMsg = {};\nnewMsg.payload    = msg.payload[\"Load current\"];\nreturn newMsg;\n","outputs":1,"noerr":0,"initialize":"","finalize":"","libs":[],"x":550,"y":100,"wires":[["fa854f67617d36ad"]]},{"id":"1e8e3851adfd9595","type":"function","z":"59daf32f25cbd1d2","name":"Load Voltage","func":"var newMsg = {};\nnewMsg.payload    = msg.payload[\"Load voltage\"];\nreturn newMsg;\n","outputs":1,"noerr":0,"initialize":"","finalize":"","libs":[],"x":550,"y":160,"wires":[["ca218d955ebbb9fc"]]},{"id":"a72784c38a64fcd0","type":"function","z":"59daf32f25cbd1d2","name":"Load Power","func":"var newMsg = {};\nnewMsg.payload    = msg.payload[\"Load power\"];\nreturn newMsg;\n","outputs":1,"noerr":0,"initialize":"","finalize":"","libs":[],"x":550,"y":220,"wires":[["9f3fb3944f7b053f"]]},{"id":"6ecc83bd89e7c19a","type":"function","z":"4f487ef734e059ae","name":"FindAll","func":" var newMsg = {};\nnewMsg.operation  = 'findOne';\nnewMsg.sort = {timestamp : -1}\nreturn newMsg;","outputs":1,"noerr":0,"initialize":"","finalize":"","libs":[],"x":210,"y":240,"wires":[["159ff071c38f3fab"]]},{"id":"d9a23eb528b7e2ac","type":"delay","z":"4f487ef734e059ae","name":"","pauseType":"rate","timeout":"1","timeoutUnits":"seconds","rate":"1","nbRateUnits":"1","rateUnits":"second","randomFirst":"1","randomLast":"5","randomUnits":"seconds","drop":false,"allowrate":false,"x":370,"y":300,"wires":[["9529604b5cd4de71","0ddf21d7c24e71a4","203558e6a75da690","a9de648b2b05ae18","91266b9616dd0e3c","070b174c30ba4645"]]},{"id":"159ff071c38f3fab","type":"mongodb3 in","z":"4f487ef734e059ae","service":"_ext_","configNode":"6890b3adef823566","name":"DB","collection":"todos","operation":"","x":330,"y":240,"wires":[["d9a23eb528b7e2ac"]]},{"id":"9529604b5cd4de71","type":"function","z":"4f487ef734e059ae","name":"Battery Voltage","func":"var newMsg = {};\nnewMsg.payload    = msg.payload[\"Battery voltage\"];\nreturn newMsg;\n","outputs":1,"noerr":0,"initialize":"","finalize":"","libs":[],"x":600,"y":60,"wires":[["8c49cef22c9af5a0"]]},{"id":"6b32ac8b9adfeb32","type":"function","z":"f4c7e0778c61d5d4","name":"FindAll","func":" var newMsg = {};\nnewMsg.operation  = 'findOne';\nnewMsg.sort = {timestamp : -1}\nreturn newMsg;","outputs":1,"noerr":0,"initialize":"","finalize":"","libs":[],"x":270,"y":100,"wires":[["43b7bdd49086c809"]]},{"id":"4307b5c7d421af93","type":"delay","z":"f4c7e0778c61d5d4","name":"","pauseType":"rate","timeout":"1","timeoutUnits":"seconds","rate":"1","nbRateUnits":"1","rateUnits":"second","randomFirst":"1","randomLast":"5","randomUnits":"seconds","drop":false,"allowrate":false,"x":550,"y":100,"wires":[["59327badcab5a603","54ef43ba7c72949b","6e1b171672079eed"]]},{"id":"43b7bdd49086c809","type":"mongodb3 in","z":"f4c7e0778c61d5d4","service":"_ext_","configNode":"6890b3adef823566","name":"DB","collection":"todos","operation":"","x":390,"y":100,"wires":[["4307b5c7d421af93"]]},{"id":"59327badcab5a603","type":"function","z":"f4c7e0778c61d5d4","name":"Solar Current","func":"var newMsg = {};\nnewMsg.payload    = msg.payload[\"PV array input current\"];\nreturn newMsg;\n","outputs":1,"noerr":0,"initialize":"","finalize":"","libs":[],"x":550,"y":180,"wires":[["f5461580a0c3f7b6"]]},{"id":"54ef43ba7c72949b","type":"function","z":"f4c7e0778c61d5d4","name":"Solar Voltage","func":"var newMsg = {};\nnewMsg.payload    = msg.payload[\"PV array input voltage\"];\nreturn newMsg;\n","outputs":1,"noerr":0,"initialize":"","finalize":"","libs":[],"x":550,"y":240,"wires":[["d834ef4bbffafb3b"]]},{"id":"6e1b171672079eed","type":"function","z":"f4c7e0778c61d5d4","name":"Solar Power","func":"var newMsg = {};\nnewMsg.payload    = msg.payload[\"PV array input power\"];\nreturn newMsg;\n","outputs":1,"noerr":0,"initialize":"","finalize":"","libs":[],"x":550,"y":300,"wires":[["472791565dc2b369"]]},{"id":"9ffb22dae7455c01","type":"function","z":"6dd8aa40d8657a9e","name":"Controller Temp","func":"var newMsg = {};\nnewMsg.payload    = msg.payload[\"Temperature inside equipment\"];\nreturn newMsg;\n","outputs":1,"noerr":0,"initialize":"","finalize":"","libs":[],"x":760,"y":180,"wires":[["ba7ddf7c81aeffdb"]]},{"id":"ba7ddf7c81aeffdb","type":"ui_text","z":"6dd8aa40d8657a9e","group":"11f2ac9c84a47d2d","order":2,"width":0,"height":0,"name":"","label":"Controller Temp.(°C)","format":"{{msg.payload}}","layout":"col-center","x":980,"y":180,"wires":[],"icon":"font-awesome/fa-dashboard"},{"id":"dfd54142f2dc0ff2","type":"mongodb3 in","z":"f2d3dabc953c2103","service":"_ext_","configNode":"6890b3adef823566","name":"Humidity","collection":"todos","operation":"","x":260,"y":160,"wires":[["49bec664777ef066"]]},{"id":"49bec664777ef066","type":"function","z":"f2d3dabc953c2103","name":"","func":"var newMsg = {};\nnewMsg.payload    = msg.payload.Humidity;\nreturn newMsg;\n","outputs":1,"noerr":0,"initialize":"","finalize":"","libs":[],"x":260,"y":280,"wires":[["23899973d839b423"]]},{"id":"a9ab545ac805cf69","type":"inject","z":"b1badf938559d63b","name":"timestamp","props":[{"p":"payload"},{"p":"topic","vt":"str"}],"repeat":"1","crontab":"","once":true,"onceDelay":0.1,"topic":"site1","payloadType":"date","x":130,"y":280,"wires":[["c493b058b550fb64"]]},{"id":"c493b058b550fb64","type":"function","z":"b1badf938559d63b","name":"FindAll","func":" var newMsg = {};\nnewMsg.operation  = 'findOne';\nnewMsg.sort = {timestamp : -1}\nreturn newMsg;","outputs":1,"noerr":0,"initialize":"","finalize":"","libs":[],"x":230,"y":380,"wires":[["e529c38ff7d5ba4c"]]},{"id":"5292ebb77a540843","type":"delay","z":"b1badf938559d63b","name":"","pauseType":"rate","timeout":"1","timeoutUnits":"seconds","rate":"1","nbRateUnits":"1","rateUnits":"second","randomFirst":"1","randomLast":"5","randomUnits":"seconds","drop":false,"allowrate":false,"x":350,"y":440,"wires":[["566d21ede09ba3fe","73177dccfd9bea32","3f4bb22e310f5db2"]]},{"id":"e529c38ff7d5ba4c","type":"mongodb3 in","z":"b1badf938559d63b","service":"_ext_","configNode":"6890b3adef823566","name":"DB","collection":"todos","operation":"","x":370,"y":380,"wires":[["5292ebb77a540843"]]},{"id":"566d21ede09ba3fe","type":"function","z":"b1badf938559d63b","name":"Load Voltage","func":"var newMsg = {};\nnewMsg.payload    = msg.payload[\"Load voltage\"];\nnewMsg.topic = 'Load Voltage';\nnewMsg.label = \"hello\"\nreturn newMsg;\n","outputs":1,"noerr":0,"initialize":"","finalize":"","libs":[],"x":570,"y":360,"wires":[["31c9cbb000f9f00d"]]},{"id":"73177dccfd9bea32","type":"function","z":"b1badf938559d63b","name":"Battery Voltage","func":"var newMsg = {};\nnewMsg.payload    = msg.payload[\"Battery voltage\"];\nnewMsg.topic = 'Battery Voltage';\nreturn newMsg;\n","outputs":1,"noerr":0,"initialize":"","finalize":"","libs":[],"x":580,"y":400,"wires":[["31c9cbb000f9f00d"]]},{"id":"3f4bb22e310f5db2","type":"function","z":"b1badf938559d63b","name":"Solar Voltage","func":"var newMsg = {};\nnewMsg.payload    = msg.payload[\"PV array input voltage\"];\nnewMsg.topic = 'Pv Voltage';\nreturn newMsg;\n","outputs":1,"noerr":0,"initialize":"","finalize":"","libs":[],"x":570,"y":440,"wires":[["31c9cbb000f9f00d"]]},{"id":"31c9cbb000f9f00d","type":"ui_chart","z":"b1badf938559d63b","name":"","group":"f71beaa233afc11f","order":3,"width":0,"height":0,"label":"chart","chartType":"line","legend":"false","xformat":"HH:mm:ss","interpolate":"linear","nodata":"","dot":false,"ymin":"","ymax":"","removeOlder":1,"removeOlderPoints":"","removeOlderUnit":"3600","cutout":0,"useOneColor":false,"useUTC":false,"colors":["#1f77b4","#aec7e8","#ff7f0e","#2ca02c","#98df8a","#d62728","#ff9896","#9467bd","#c5b0d5"],"outputs":1,"useDifferentColor":false,"x":830,"y":200,"wires":[[]]},{"id":"d033e3f297e9caec","type":"ui_text","z":"4f487ef734e059ae","group":"2ce0376077df9e91","order":2,"width":0,"height":0,"name":"","label":"Battery Current (A)","format":"{{msg.payload}}","layout":"col-center","x":910,"y":180,"wires":[],"icon":"font-awesome/fa-dashboard"},{"id":"df46e6b4c02ab63a","type":"ui_text","z":"4f487ef734e059ae","group":"f690278b422b2b5b","order":1,"width":0,"height":0,"name":"","label":"Max Volatage (V)","format":"{{msg.payload}}","layout":"col-center","x":910,"y":120,"wires":[]},{"id":"0ddf21d7c24e71a4","type":"function","z":"4f487ef734e059ae","name":"Max Voltage","func":"var newMsg = {};\nnewMsg.payload    = msg.payload[\"Maximum battery volt today \"];\nreturn newMsg;\n","outputs":1,"noerr":0,"initialize":"","finalize":"","libs":[],"x":590,"y":120,"wires":[["df46e6b4c02ab63a"]]},{"id":"203558e6a75da690","type":"function","z":"4f487ef734e059ae","name":"Battery Current","func":"var newMsg = {};\nnewMsg.payload    = msg.payload[\"Battery current\"];\nreturn newMsg;\n","outputs":1,"noerr":0,"initialize":"","finalize":"","libs":[],"x":600,"y":180,"wires":[["d033e3f297e9caec"]]},{"id":"0e38eb753522b148","type":"ui_text","z":"4f487ef734e059ae","group":"f690278b422b2b5b","order":2,"width":0,"height":0,"name":"","label":"Min Volatage (V)","format":"{{msg.payload}}","layout":"col-center","x":900,"y":340,"wires":[]},{"id":"28dcd8921a4add33","type":"ui_text","z":"4f487ef734e059ae","group":"f690278b422b2b5b","order":3,"width":0,"height":0,"name":"","label":"Battery SOC(%)","format":"{{msg.payload}}","layout":"col-center","x":900,"y":420,"wires":[]},{"id":"a9de648b2b05ae18","type":"function","z":"4f487ef734e059ae","name":"Min Voltage","func":"var newMsg = {};\nnewMsg.payload    = msg.payload[\"Minimum input volt (PV) today\"];\nreturn newMsg;\n","outputs":1,"noerr":0,"initialize":"","finalize":"","libs":[],"x":590,"y":340,"wires":[["0e38eb753522b148"]]},{"id":"91266b9616dd0e3c","type":"function","z":"4f487ef734e059ae","name":"Battery SOC","func":"var newMsg = {};\nnewMsg.payload    = msg.payload[\"Battery SOC\"];\nreturn newMsg;\n","outputs":1,"noerr":0,"initialize":"","finalize":"","libs":[],"x":590,"y":420,"wires":[["28dcd8921a4add33"]]},{"id":"ede3c069a3340e31","type":"ui_text","z":"4f487ef734e059ae","group":"2ce0376077df9e91","order":3,"width":0,"height":0,"name":"","label":"Battery Temp.(°C)","format":"{{msg.payload}}","layout":"col-center","x":910,"y":260,"wires":[],"icon":"font-awesome/fa-dashboard"},{"id":"070b174c30ba4645","type":"function","z":"4f487ef734e059ae","name":"Battery Temp","func":"var newMsg = {};\nnewMsg.payload    = msg.payload[\"Battery Temp.\"];\nreturn newMsg;\n","outputs":1,"noerr":0,"initialize":"","finalize":"","libs":[],"x":590,"y":260,"wires":[["ede3c069a3340e31"]]},{"id":"472791565dc2b369","type":"ui_text","z":"f4c7e0778c61d5d4","group":"880cc37e094fd321","order":3,"width":0,"height":0,"name":"","label":"Solar Power (W)","format":"{{msg.payload}}","layout":"col-center","x":880,"y":220,"wires":[]}]
```


<br/>
<br/>

## Built With

* [yhb1834](https://github.com/yhb1834)
  * write a Node-Red Code
  * make Flow-Diagram
* [ohbumjun](https://github.com/ohbumjun)
  * write a part of Node-Red Code
  * prepare, run AWS EC2 Ubuntu Server
* [JackWanggg](https://github.com/JackWanggg)
  * write a RasberryPi code
* [darksilicon13](https://github.com/darksilicon13)
  * write RasberrPi Code
  * worked upon Solar panel, Rasberry Pi
  * worked upon setting up MongoDB
  


