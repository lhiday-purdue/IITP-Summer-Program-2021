from firebase import firebase as f
from hash import hash as h
from secret import secret as s
import datetime
import random


while True:
    print("1 : Material management")
    print("2 : User management")
    print("0 : Exit")
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
                print("Security level = " + str((int(_uid) % 4 + 1)))
            elif n == "2":
                _uid = s.get_secret()
                _level = int(_uid) % 4 + 1
                salt_list = f.get_salt_list()
                check = False
                for _salt in salt_list:
                    h_uid = h.hash_function(_uid, _salt)
                    if f.find_doc("Material_" + str(_level), h_uid):
                        print("Error - id already exists in DB.")
                        check = True
                        break
                if check:
                    continue
                _name = input("Name = ")
                idx = random.randint(0, len(salt_list) - 1)
                salt = salt_list[idx]
                _uid = h.hash_function(_uid, salt)
                _name = h.hash_function(_name, salt)
                f.register_material(_uid, _name, (datetime.datetime.now() + datetime.timedelta(7, 0, 0, 0)).replace(tzinfo=None), _level)
                print("Success to register.")
            elif n == "3":
                _uid = s.get_secret()
                _level = int(_uid) % 4 + 1
                salt = ""
                salt_list = f.get_salt_list()
                check = False
                for _salt in salt_list:
                    h_uid = h.hash_function(_uid, _salt)
                    if f.find_doc("Material_" + str(_level), h_uid):
                        salt = _salt
                        check = True
                        break
                if check:
                    print("Pass")
                else:
                    print("Warning - uid doesn't exist in DB.")
                    continue
                _uid = h.hash_function(_uid, salt)
                _due = f.get_field("Material_" + str(_level), _uid, "due_date")
                _due = _due.replace(tzinfo=None)
                if datetime.datetime.now() >= _due:
                    print("Warning - due date was over, please return it.")
            elif n == "4":
                _uid = s.get_secret()
                _level = int(_uid) % 4 + 1
                salt = ""
                salt_list = f.get_salt_list()
                check = False
                for _salt in salt_list:
                    h_uid = h.hash_function(_uid, _salt)
                    if f.find_doc("Material_" + str(_level), h_uid):
                        salt = _salt
                        check = True
                        break
                if check:
                    _uid = h.hash_function(_uid, salt)
                    _due = f.get_field("Material_" + str(_level), _uid, "due_date")
                    f.delete_doc("Material_" + str(_level), _uid)
                    print("Success to return.")
                    print("Due date : " + str(_due))
                    print("Export time : " + str(datetime.datetime.now().replace(tzinfo=None)))
                else:
                    print("Error - uid doesn't exist in DB.")
            else:
                print("Unexpected input - " + n + ". Try again.")
    elif n == "2":
        while True:
            print("1 : Card registration")
            print("2 : Access control mode")
            print("3 : Report lost and stolen")
            print("4 : Card release")
            print("5 : Check the entry and exit time")
            print("0 : Exit")
            n = input("Input = ")
            if n == "0":
                break
            elif n == "1":
                _uid = s.get_secret()
                salt_list = f.get_salt_list()
                check = False
                for _salt in salt_list:
                    h_uid = h.hash_function(_uid, _salt)
                    if f.find_doc("User", h_uid):
                        print("Error - uid already exists in DB.")
                        check = True
                        break
                if check:
                    continue
                _name = input("Name = ")
                _password = s.get_secret("Password = ")
                idx = random.randint(0, len(salt_list) - 1)
                salt = salt_list[idx]
                _uid = h.hash_function(_uid, salt)
                _name = h.hash_function(_name, salt)
                _password = h.hash_function(_password, salt)
                f.register_user(_uid, _name, _password, None, None)
                print("Success to register.")
            elif n == "2":
                _uid = s.get_secret()
                salt = ""
                salt_list = f.get_salt_list()
                check = False
                for _salt in salt_list:
                    h_uid = h.hash_function(_uid, _salt)
                    if f.find_doc("User", h_uid):
                        salt = _salt
                        check = True
                        break
                if check:
                    print("Pass")
                else:
                    print("Warning - uid doesn't exist in DB.")
                    continue
                _uid = h.hash_function(_uid, salt)
                if f.get_field("User", _uid, "entry_time") is None:
                    f.set_field("User", _uid, "entry_time", datetime.datetime.now())
                    print("Check-in")
                elif f.get_field("User", _uid, "exit_time") is None:
                    f.set_field("User", _uid, "exit_time", datetime.datetime.now())
                    print("Check-out")
                else:
                    f.set_field("User", _uid, "entry_time", datetime.datetime.now())
                    f.set_field("User", _uid, "exit_time", None)
                    print("Check-in")
            elif n == "3":
                _name = input("Name = ")
                _password = s.get_secret("Password = ")
                h_uid = ""
                salt = ""
                salt_list = f.get_salt_list()
                check = False
                for _salt in salt_list:
                    h_name = h.hash_function(_name, _salt)
                    h_password = h.hash_function(_password, _salt)
                    doc = f.find_doc_with_np(h_name, h_password)
                    if doc:
                        salt = _salt
                        h_uid = doc
                        check = True
                if check:
                    f.delete_doc("User", h_uid)
                    print("Success to delete information.")
                else:
                    print("Error - corresponding information doesn't exist.")
            elif n == "4":
                _uid = s.get_secret()
                salt = ""
                salt_list = f.get_salt_list()
                check = False
                for _salt in salt_list:
                    h_uid = h.hash_function(_uid, _salt)
                    if f.find_doc("User", h_uid):
                        salt = _salt
                        check = True
                        break
                if check:
                    _uid = h.hash_function(_uid, salt)
                    f.delete_doc("User", _uid)
                    print("Success to release the card.")
                else:
                    print("Error - uid doesn't exist in DB.")
            elif n == "5":
                _uid = s.get_secret()
                salt = ""
                salt_list = f.get_salt_list()
                check = False
                for _salt in salt_list:
                    h_uid = h.hash_function(_uid, _salt)
                    if f.find_doc("User", h_uid):
                        salt = _salt
                        check = True
                        break
                if check:
                    _uid = h.hash_function(_uid, salt)
                    print("Entry_time : " + str(f.get_field("User", _uid, "entry_time")))
                    print("Exit_time : " + str(f.get_field("User", _uid, "exit_time")))
                else:
                    print("Error - uid doesn't exist in DB.")
            else:
                print("Unexpected input " + n + ". Try again.")
    else:
        print("Unexpected input " + n + ". Try again.")
