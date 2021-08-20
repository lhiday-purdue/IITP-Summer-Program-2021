# IITP Summer Program 2021

## PROJECT 11
### Enhancing the physical security system of the company's access control using RFID<br>
#### \- Dividing into two aspects: Human and equipment access control

-----

### ■&nbsp;&nbsp;TEAM MEMBERS
| NAME           | University           |
| -------------- | -------------------- |
| Cheongjun Kim  | Chung-Ang University |
| Jaehyun Shin   | Chung-Ang University |
| Myoungjin Oh   | Chung-Ang University |
| Sangyeop Park  | Chung-Ang University |
| Seunghyeon Lee | Chung-Ang University |
| Uichan Kim     | Chung-Ang University |
| Zhongyuan Hu   | Purdue University    |

-----

### ■&nbsp;&nbsp;Project Introduction
▣ The project is part of the IITP Summer Program, and **Korean universities & Purdue university** participated together.<br>
▣ The members consist of 6 **Chung-Ang University** students and 1 **Purdue University** student.<br>
▣ This project's goal is to improve physical security with **RFID systems** and ensure that assets can be managed safely and efficiently.<br>
<br>
_It has the following advantages:_
#### **1. The research strengthens the management of equipment and personnel in various companies**
- This can make the use of the equipment and people become clearer so that the management becomes more convenient. 
- It also reduces the possibility of theft and loss. 
- Even if theft and loss occur, liability can be found from the person who used the equipment.

#### **2. The technology of security level based on RFID will classify the safety level according to the characteristics of different equipment.**
- Using the classification of security level, it will become more convenient to manage equipment.
- In addition, it can have a more precise concept for future buying of equipment.

#### **3. Based on the function of collecting information through RFID, epidemiological investigation can be carried out.**
- Through the time information stored when people pass the system, tracing the movement becomes effective.

-----

### ■&nbsp;&nbsp;Project Differentiation
- In this project, RFID systems were used to design an access control system that considered both human and physical security.
- This access control system protected information through hash algorithms. Also, the salting technique was applied when generating hash values to prevent vulnerabilities in existing hash functions such as Brute Force and Rainbow table attack.
- In addition, security levels were applied to classify assets so that registered assets could be managed more efficiently.

-----

### ■&nbsp;&nbsp;Project Implementation
#### 1. Environment setting
- The system is implemented with Python, and utilizes Google Cloud Firestore.
- There should be a JSON file contatining the key to access to the Firebase.
- Libraries including firebase-admin 5.0.1.
- MF RFID readers and MF 13.56MHz RFID tags are used for the project.

#### 2. Process of the project
- The system works through this process when UID is entered.

![image](https://user-images.githubusercontent.com/51505940/129325758-d76aa13d-48b6-488e-ab13-6f62ad887321.png)

- The information is protected using SHA3-256 with salt. And Each UID has different salt value.

#### 3. User management
- When UID is entered, system checks if user has permission, and records entry-time, exit-time.
- If user loses his or her card, he or she can report the loss by entering the name and password.
- DB schema for user

|  UID  |  Entry_time  |  Exit_time  |  Name  |  Password  |
| ----- | ------------ | ----------- | ------ | ---------- |
| UID-1 | Entry_time-1 | Exit_time-1 | Name-1 | Password-1 |
| UID-2 | Entry_time-2 | Exit_time-2 | Name-2 | Password-2 |
|  ...  |      ...     |     ...     |  ...   |    ...     |
| UID-n | Entry_time-n | Exit_time-n | Name-n | Password-n |

#### 4. Material management
- When UID is entered, system checks if UID is already registered, and checks the due-date.
- Security level of the RFID tag is calculated with `UID value mod 4 + 1`.
- The security level division can reduce the amount of computation in the Database.
- Security level of material

|  Level  |  Type of material                         |
| ------- | ----------------------------------------- |
|    1    |  Radio communication available equipment  |
|    2    |  Recording available equipment            |
|    3    |  Storage device                           |
|    4    |  Confidential document                    |

- DB schema for material

|  UID  |  Name  |  Security_level  |  Due_date  |
| ----- | ------ | ---------------- | ---------- |
| UID-1 | Name-1 | Security_level-1 | Due_date-1 |
| UID-2 | Name-2 | Security_level-2 | Due_date-2 |
|  ...  |  ...   |       ...        |    ...     |
| UID-n | Name-n | Security_level-n | Due_date-n |

#### 5. Test
- Test for user

![image](https://user-images.githubusercontent.com/51505940/129316283-7c9222f3-7de4-4895-80e8-5767c179a6e0.png)

- Test for material

![image](https://user-images.githubusercontent.com/51505940/129316309-7bb18fcd-d813-4625-bc16-ddc48fdff889.png)
