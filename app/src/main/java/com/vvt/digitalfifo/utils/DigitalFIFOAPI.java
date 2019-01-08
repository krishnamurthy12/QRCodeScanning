package com.vvt.digitalfifo.utils;

import com.vvt.digitalfifo.apirequests.KanbanScan;
import com.vvt.digitalfifo.apirequests.LogIn;
import com.vvt.digitalfifo.apirequests.LogOut;
import com.vvt.digitalfifo.apirequests.QuarantineOut;
import com.vvt.digitalfifo.apirequests.StationName;
import com.vvt.digitalfifo.apiresponses.kanbonscan.KanbonScanResponse;
import com.vvt.digitalfifo.apiresponses.lineresponse.LineNamesResponse;
import com.vvt.digitalfifo.apiresponses.login.LogInResponse;
import com.vvt.digitalfifo.apiresponses.logout.LogOutResponse;
import com.vvt.digitalfifo.apiresponses.stationresponse.StationNamesResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface DigitalFIFOAPI {

    @Headers("Content-Type: application/json")
    @GET("lineandstation/linenames/line")
    Call<LineNamesResponse> getLines();

    @Headers("Content-Type: application/json")
    @POST("lineandstation/stationnames")
    Call<StationNamesResponse> getstations(@Body StationName stationName);

    @Headers("Content-Type: application/json")
    @POST("Login/logindetails")
    Call<LogInResponse> logIn(@Body LogIn logIn);

    @Headers("Content-Type: application/json")
    @POST("fifo/flow")
    Call<KanbonScanResponse> validateKanbon(@Body KanbanScan kanbonScan);

    @Headers("Content-Type: application/json")
    @POST("optional/optionalscan/")
    Call<KanbonScanResponse> analySisAndLomsOut(@Body KanbanScan kanbonScan);

    @Headers("Content-Type: application/json")
    @POST("optional/quarantine/")
    Call<KanbonScanResponse> quarantineOut(@Body QuarantineOut quarantineOut);


    @Headers("Content-Type: application/json")
    @POST("Login/logout")
    Call<LogOutResponse> logOut(@Body LogOut logOut);

    /*@GET("loginprocess/{ntuserId}/{employeeId}")
    Call<String> logIn(@Path("ntuserId")String ntuserId, @Path("employeeId")String employeeID);

    @GET("loginprocess/{employeeId}")
    Call<String> logOut(@Path("employeeId")String employeeID);*/
}
