package com.vvt.digitalfifo.apiresponses.lineresponse;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class LineNamesResponse {
    @SerializedName("linenames")
    @Expose
    private List<String> linenames = null;

    public List<String> getLinenames() {
        return linenames;
    }

    public void setLinenames(List<String> linenames) {
        this.linenames = linenames;
    }
}
