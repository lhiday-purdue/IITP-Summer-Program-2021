package com.example.smbeaconclient.firebase;

public class Workplace {
    private String token;
    private int floor;
    private boolean enter;
    private boolean insider;
    private String tell;

    public Workplace(String token, boolean insider) {
        this.token = token;
        this.insider = insider;
    }
    public Workplace(String token, boolean insider, String tell) {
        this.token = token;
        this.insider = insider;
        this.tell = tell;
    }
    public Workplace(String token) {
        this.token = token;
    }
    public Workplace() {    }

    public String getTell() {
        return tell;
    }

    public void setTell(String tell) {
        this.tell = tell;
    }

    public boolean isInsider() {
        return insider;
    }

    public void setInsider(boolean insider) {
        this.insider = insider;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public int getFloor() {
        return floor;
    }

    public void setFloor(int floor) {
        this.floor = floor;
    }

    public boolean isEnter() {
        return enter;
    }

    public void setEnter(boolean enter) {
        this.enter = enter;
    }
}
