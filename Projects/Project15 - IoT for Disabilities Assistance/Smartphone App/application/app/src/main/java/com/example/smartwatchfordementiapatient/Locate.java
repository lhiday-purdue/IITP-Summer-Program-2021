package com.example.smartwatchfordementiapatient;

import java.io.Serializable;

public class Locate implements Serializable {
    private double latitude;
    private double longitude;
    Locate(double latitude, double longitude){
        this.latitude = latitude;
        this.longitude = longitude;
    }
    public double getLatitude(){
        return this.latitude;
    }
    public double getLongitude(){
        return this.longitude;
    }
}