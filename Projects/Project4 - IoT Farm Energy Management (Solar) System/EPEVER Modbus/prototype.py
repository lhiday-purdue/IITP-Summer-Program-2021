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
client = pymongo.MongoClient("mongodb+srv://id:password@cluster0.kii4s.mongodb.net/test?retryWrites=true&w=majority", tlsCAFile=ca)
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
    

        
