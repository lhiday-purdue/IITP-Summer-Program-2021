package com.example.smartwatchfordementiapatient;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
// It is for google address => make a list of address
public class AddressList extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address_list);
        final Geocoder geocoder = new Geocoder(this);
        Intent intent = getIntent();
        ArrayList<Locate> locate_data = (ArrayList<Locate>)intent.getSerializableExtra("datalist");
        Double latitude = locate_data.get(0).getLatitude(); // latitude
        Double longitude = locate_data.get(0).getLongitude(); // longitude


        final TextView tvSelect = findViewById(R.id.tv_select);
        ListView listView = findViewById(R.id.listView);
        List<Address> list = null;
        try {
            list = geocoder.getFromLocation(
                    latitude,
                    longitude,
                    30);
        }catch(IOException e){
            e.printStackTrace();
        }
        List<String> llist = new ArrayList<>();
        if(list!=null){
            if(list.size()==0){
                finish();
            }else{
                for(Address temp : list){
                    llist.add(temp.getAddressLine(0));
                }

            }
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,llist);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String data = (String)parent.getItemAtPosition(position);
                Intent i = new Intent();
                Bundle extra = new Bundle();
                extra.putString("data",data);
//                extra.putDouble("latitude",latitude);
//                extra.putDouble("longitude",longitude);
                i.putExtras(extra);
                setResult(RESULT_OK,i);
                finish();
            }
        });
    }
}