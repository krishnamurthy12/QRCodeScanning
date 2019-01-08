package com.vvt.digitalfifo.apirequests;

public class QuarantineOut {

    String linename,stationname,partnumber,kanbannumber,quantity,reason;

    public QuarantineOut(String linename, String stationname, String partnumber, String kanbannumber, String quantity, String reason) {
        this.linename = linename;
        this.stationname = stationname;
        this.partnumber = partnumber;
        this.kanbannumber = kanbannumber;
        this.quantity = quantity;
        this.reason = reason;
    }

    public String getLinename() {
        return linename;
    }

    public void setLinename(String linename) {
        this.linename = linename;
    }

    public String getStationname() {
        return stationname;
    }

    public void setStationname(String stationname) {
        this.stationname = stationname;
    }

    public String getPartnumber() {
        return partnumber;
    }

    public void setPartnumber(String partnumber) {
        this.partnumber = partnumber;
    }

    public String getKanbannumber() {
        return kanbannumber;
    }

    public void setKanbannumber(String kanbannumber) {
        this.kanbannumber = kanbannumber;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}

