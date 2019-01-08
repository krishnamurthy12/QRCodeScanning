package com.vvt.digitalfifo.apirequests;

public class LogIn {
    private String linename,stationname,password;

    public LogIn(String linename, String stationname, String password) {
        this.linename = linename;
        this.stationname = stationname;
        this.password = password;
    }
}
