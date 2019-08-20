package com.vvt.digitalfifo.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

public class Utilities {

    public static Snackbar snackbar;
    private static Toast mToast;
    private static String loginPreference="LOGINPREFERENCE";
    private static String ipAddressPreference="IPADDRESSPREFERENCE";

/*
* To showSnackBar message
* -----------------------------------------------------------------------------------------------------------------
* */
    public static void showSnackBar(Context context, String message) {
        Activity activity = (Activity) context;
        if (snackbar != null) {
            snackbar.dismiss();
        }
        snackbar = Snackbar.make(activity.findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG);
        View view = snackbar.getView();
        view.setBackgroundColor(Color.BLACK);
        TextView tv = (TextView) view.findViewById(android.support.design.R.id.snackbar_text);
        tv.setTextColor(Color.WHITE);
        tv.setTextSize(16);
        snackbar.show();
    }

    /*
    * --------------------------------------------------------------------------------------------------------------
    * */


/*
* To show Toast message
* -----------------------------------------------------------------------------------------------------------------
* */
    public static void showToast(Context context,String message) {
        if (mToast != null) {
            mToast.cancel();
        }
        mToast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
        mToast.show();
    }

    /*
    * ------------------------------------------------------------------------------------------------------------
    * */


/*For getting BaseURL
* -------------------------------------------------------------------------------------------------------------------
 * */
    public static String getBaseURL(Context context)
    {
        SharedPreferences sharedPreferences=context.getSharedPreferences(ipAddressPreference,Context.MODE_PRIVATE);
        String ipaddress=sharedPreferences.getString("IPADDRESS",null);
        if(ipaddress!=null)
        {
            return "http://"+ipaddress+":8080/DigitalFIFOandInventory/rest/";

        }

        return null;
    }
    /*
    * ------------------------------------------------------------------------------------------------------------------
    * */

/*For saving IP Address
* -------------------------------------------------------------------------------------------------------------------
* */
    public static void saveIPAddressPreference(Context context,String ipAddress)
    {
        SharedPreferences sharedPreferences=context.getSharedPreferences(ipAddressPreference,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor=sharedPreferences.edit();

       /* String ipaddress=sharedPreferences.getString("IPADDRESS",null);
        if(ipaddress!=null)*/
        if(sharedPreferences.contains("IPADDRESS"))
        {
            editor.clear();
            editor.apply();
        }
        editor.putString("IPADDRESS",ipAddress);
        editor.apply();
    }


    public static String getIPAddress(Context context)
    {
        SharedPreferences sharedPreferences=context.getSharedPreferences(ipAddressPreference,Context.MODE_PRIVATE);
        return sharedPreferences.getString("IPADDRESS",null);

    }
    /*
 * ------------------   -------------------------------------------------------------------------------------------------
     * */


    /*
    To saveLogInPreference
 * -------------------------------------------------------------------------------------------------------------------
     * */

    public static void saveLogInPreference(Context context,boolean isLoggedIn,String...strings)
    {
        SharedPreferences sharedPreferences=context.getSharedPreferences(loginPreference,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor=sharedPreferences.edit();

        if(isLoggedIn)
        {
            editor.putBoolean("ISLOGGEDIN",isLoggedIn);
            editor.putString("LINENAME",strings[0]);
            editor.putString("STATIONNAME",strings[1]);
            editor.putString("STATIONID",strings[2]);
            editor.apply();
        }
        else {
            editor.clear();
            editor.apply();
        }

    }

    public static String[] getIsLoggedIn(Context context)
    {
        SharedPreferences sharedPreferences=context.getSharedPreferences(loginPreference,Context.MODE_PRIVATE);
        boolean isloggedin= sharedPreferences.getBoolean("ISLOGGEDIN",false);

        if(isloggedin)
        {
            return new String[]{"true",
                    sharedPreferences.getString("LINENAME",null),
                    sharedPreferences.getString("STATIONNAME",null),
                    sharedPreferences.getString("STATIONID",null)
            };

        }
        else {
            return new String[]{"false",
                    sharedPreferences.getString("LINENAME",null),
                    sharedPreferences.getString("STATIONNAME",null),
                    sharedPreferences.getString("STATIONID",null)};
        }

    }

    /*
* -------------------------------------------------------------------------------------------------------------------
     * */


    /*
    To check whether the app is connected to Internet or not
 * -------------------------------------------------------------------------------------------------------------------
     * */

    public static boolean isConnectedToInternet(Context con){
        ConnectivityManager connectivity = (ConnectivityManager) con.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null){
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null)
                for (NetworkInfo anInfo : info)
                    if (anInfo.getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }

        }
        return false;
    }

    /*
    * ------------------------------------------------------------------------------------------------------------------
    * */



}
