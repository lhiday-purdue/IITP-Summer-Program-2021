import sys
import json
from flask import Flask, render_template, request

from db_manager import DatabaseManager

app = Flask(__name__)


@app.route('/', methods=['GET'])
def handle_request():
    return "Welcome to the \"Smart Watch for Dementia Patients\" Project Server.<br>" \
           "This server is for research purpose."


@app.route('/append-location', methods=['POST'])
def handle_gps_location_set():
    if request.is_json:
        params = request.get_json()
        user_id = params['id']
        my_db = DatabaseManager().instance()
        my_db.create_connection(DatabaseManager.DB_WATCH_DATA)
        my_db.get_cursor()
        my_db.insert_row(user_id, params['longitude'], params['latitude'],
                         database=DatabaseManager.DB_WATCH_DATA,
                         table_name="SmartWatch"
                         )
        my_db.close_connection(DatabaseManager.DB_WATCH_DATA)
        my_db.create_connection(DatabaseManager.DB_USER_DATA)
        my_db.get_cursor()
        selected_cols = my_db.select_first_element_matches(
            "patient_locate_longitude", "patient_locate_latitude", "is_patient_away",
            match_keyword=user_id,
            finding_column="id",
            table_name="parent_user"
        )
        my_db.close_connection(DatabaseManager.DB_USER_DATA)
        print(selected_cols, file=sys.stderr)
        return_dict = {
                'longitude': selected_cols['patient_locate_longitude'],
                'latitude': selected_cols['patient_locate_latitude'],
                'is_patient_away': selected_cols['is_patient_away']
                }
        result = json.dumps(return_dict)
        return result
    else:
        return 'Failed to get longitude & latitude parameters.'


@app.route('/query-location', methods=['POST'])
def query_patient_location():
    if request.is_json:
        params = request.get_json()
        user_id = params['id']
        my_db = DatabaseManager().instance()
        my_db.create_connection(DatabaseManager.DB_WATCH_DATA)
        my_db.get_cursor()
        location = my_db.select_last_element_matches(
            "longitude", "latitude",
            match_keyword=user_id,
            finding_column="parent_id",
            table_name="SmartWatch"
        )
        my_db.close_connection(DatabaseManager.DB_WATCH_DATA)
        print(location, file=sys.stderr)
        return_dict = {
                'longitude': location['longitude'],
                'latitude' : location['latitude']
                }
        result = json.dumps(return_dict)
        return result
    else:
        return "Failed to query patient's last location."


@app.route('/address', methods=['GET', 'POST'])
def address_request():
    return render_template("daum.html")

# for signup with userinfo DB
@app.route('/signup',methods=['POST'])
def signup():
    if(request.is_json):
        user_my_db = DatabaseManager.instance()
        user_my_db.create_connection(DatabaseManager.DB_USER_DATA)
        user_my_db.get_cursor()
        
        params = request.get_json()
        id = params['id']
        name = params['name']
        phone = params['phone']
        pw = params['pw']

        patient_id = params['patient']
        latitude = params['selected_latitude']
        longitude = params['selected_longitude']
        patient_range = params['range']
        field = ["id","pw","name","phone","patient_name","patient_locate_latitude","patient_locate_longitude","patient_range"]

        
        user_my_db.insert_with_specific_field(id,pw,name,phone,patient_id,latitude,longitude,patient_range,
                                            table_name="parent_user", field_name = field
                                            )
        user_my_db.close_connection(DatabaseManager.DB_USER_DATA)
        return 'success'
    return 'failed'
@app.route('/login',methods=['POST'])
def login():
    if(request.is_json):
        user_my_db = DatabaseManager.instance()
        user_my_db.create_connection(DatabaseManager.DB_USER_DATA)
        user_my_db.get_cursor()

        params = request.get_json()
        login_id = params['id']
        pw = params['pw']
        result = user_my_db.get_login_info(login_id=login_id,pw=pw,table_name="parent_user")
        user_my_db.close_connection(DatabaseManager.DB_USER_DATA)
#        return result
#        print(result)
        if result == "wrong":
            return result
        else: 
            return_result = {
                'id' : result['id'],
                'name' : result['name'],
                'phone' : result['phone'],
                'latitude' : result['patient_locate_latitude'],
                'longitude' : result['patient_locate_longitude'],
                'patient_range' : result['patient_range']
            }
            print('asdddddd', return_result)
            return json.dumps(return_result)
        

app.run(host="0.0.0.0", port=5000, debug=True)
