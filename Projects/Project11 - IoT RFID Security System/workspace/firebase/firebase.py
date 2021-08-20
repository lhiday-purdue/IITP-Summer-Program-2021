import firebase_admin
from firebase_admin import credentials
from firebase_admin import firestore


cred = credentials.Certificate('./auth.json')
firebase_admin.initialize_app(cred)
db = firestore.client()


def register_material(material_id, name, due_date, security_level):
    doc_ref = db.collection("Material_" + str(security_level)).document(material_id)
    doc_ref.set({'name': name, 'due_date': due_date, 'security_level': security_level})


def register_user(user_id, name, password, entry_time, exit_time):
    doc_ref = db.collection("User").document(user_id)
    doc_ref.set({'name': name, 'password': password, 'entry_time': entry_time, 'exit_time': exit_time})


def find_doc(coll_name, doc_name):
    doc_ref = db.collection(coll_name).document(doc_name)
    doc = doc_ref.get()
    if doc.exists:
        return True
    else:
        return False


def delete_doc(coll_name, doc_name):
    doc_ref = db.collection(coll_name).document(doc_name)
    doc = doc_ref.get()
    if doc.exists:
        doc_ref.delete()
    else:
        print("Error")


def get_field(coll_name, doc_name, field_name):
    doc_ref = db.collection(coll_name).document(doc_name)
    doc = doc_ref.get()
    if doc.exists:
        return doc.get(field_name)
    else:
        return False


def set_field(coll_name, doc_name, field_name, value):
    doc_ref = db.collection(coll_name).document(doc_name)
    doc = doc_ref.get()
    if doc.exists:
        doc_ref.update({field_name: value})
    else:
        print("Error")


def find_doc_with_np(name, password):
    docs = db.collection("User").stream()
    for doc in docs:
        if doc.exists:
            if doc.get("name") == name and doc.get("password") == password:
                return doc.id
    return False


def get_salt_list():
    salt_list = []
    docs = db.collection("Salt").stream()
    for doc in docs:
        if doc.exists:
            salt_list.append(doc.get("salt"))
    return salt_list
