
package com.vvt.digitalfifo.apiresponses.stationresponse;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class StationNamesResponse {

    @SerializedName("stationNames")
    @Expose
    private List<String> stationNames = null;

    public List<String> getStationNames() {
        return stationNames;
    }

    public void setStationNames(List<String> stationNames) {
        this.stationNames = stationNames;
    }

}
