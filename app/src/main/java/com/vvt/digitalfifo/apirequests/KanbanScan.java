package com.vvt.digitalfifo.apirequests;

public class KanbanScan {
    String linename,stationname,partnumber,kanbannumber,quantity;

    public KanbanScan(String linename, String stationname, String partnumber, String kanbannumber, String quantity) {
        this.linename = linename;
        this.stationname = stationname;
        this.partnumber = partnumber;
        this.kanbannumber = kanbannumber;
        this.quantity = quantity;
    }
}
