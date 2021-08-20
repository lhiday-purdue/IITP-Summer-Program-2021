from hash import hash as h
import datetime
from secret import secret as s
import datetime
import random

while True:
    print("1 : Material management")
    print("2 : User management")
    print("0: Exit")
    n = input("Input = ")
    if n == "0":
        break
    elif n == "1":
        while True:
            print("1 : Check the security level")
            print("2 : Registration")
            print("3 : Access control mode")
            print("4 : Return")
            print("0 : Exit")
            n = input("Input = ")
            if n == "0":
                break
            elif n == "1":
                _uid = s.get_secret()
                print("Security level = " + str(int(_uid) % 4 + 1))
            elif n == "2":
                _uid = s.get_secret()
                _level = int(_uid) % 4 + 1
                salt_list = f.get_salt_list()
                check = False

                # todo : add code to add into Material database

            elif n == "3":
                _uid = s.get_secret()
                _level = int(_uid) % 4 + 1
                salt = ""
                # salt_list = f.get_salt_list()
                check = False
                for _salt in salt_list:
                    h_uid = h.hash_function(_uid, _salt)

                if check:
                    print("Pass")
                else:
                    print("Warning - uid doesn't exist in DB.")
                    continue
                _uid = h.hash_function(_uid, salt)
                # _due = f.get_field("Material_" + str(_level), _uid, "due_date")
                # _due = _due.replace(tzinfo=None)
                if datetime.datetime.now() >= _due:
                    print("Warning - due date was over, please return it.")

            # option 4 not yet implemented
