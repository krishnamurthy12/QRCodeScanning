package com.vvt.digitalfifo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatAutoCompleteTextView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.vvt.digitalfifo.activities.MainActivity;
import com.vvt.digitalfifo.activities.AnalysisEntryActivity;
import com.vvt.digitalfifo.apirequests.LogIn;
import com.vvt.digitalfifo.apirequests.StationName;
import com.vvt.digitalfifo.apiresponses.lineresponse.LineNamesResponse;
import com.vvt.digitalfifo.apiresponses.login.LogInResponse;
import com.vvt.digitalfifo.apiresponses.stationresponse.StationNamesResponse;
import com.vvt.digitalfifo.activities.LomsEntryActivity;
import com.vvt.digitalfifo.activities.QuarantineEntryActivity;
import com.vvt.digitalfifo.utils.OnResponseListener;
import com.vvt.digitalfifo.utils.Utilities;
import com.vvt.digitalfifo.utils.WebServices;

import static com.vvt.digitalfifo.utils.Utilities.saveLogInPreference;
import static com.vvt.digitalfifo.utils.Utilities.showSnackBar;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener, OnResponseListener {

    //Spinner mLineSpinner,mStationSpinner;
    LinearLayout mLogIn;
    EditText mPassword, mIPAddressEditText, mLoginPassword;
    TextView mVersion;
    ImageView mEditIP;
    AppCompatAutoCompleteTextView mLineName, mStationname;
    ProgressBar mProgressBar;

    AlertDialog.Builder builder;
    AlertDialog alertDialog;

    public boolean doubleBackToExitPressedOnce = false;


    @Override
    protected void onStart() {
        super.onStart();
        /*to preventing from taking screen shots*/
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE);

        String ipAddress = Utilities.getIPAddress(this);
        checkPowerOptimizationPermission();
        if (ipAddress == null) {
            Utilities.showToast(this, "IP address is empty");
        } else {
            String baseURL = Utilities.getBaseURL(this);
            Log.d("baseurl", baseURL);

            callGetLinesAPI();
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*to preventing from taking screen shots*/
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE);

        String[] logInDetails = Utilities.getIsLoggedIn(LoginActivity.this);
        try {

            if (logInDetails[0].equalsIgnoreCase("true")) {

                String LINENAME = logInDetails[1].toUpperCase().trim();
                String stationName = logInDetails[2].toUpperCase().trim();

                if (stationName.equalsIgnoreCase("analysis")) {
                    //
                    gotoNextActivity(AnalysisEntryActivity.class);
                }
                else if( stationName.equalsIgnoreCase("loms"))
                {
                    gotoNextActivity(LomsEntryActivity.class);
                }

                else if (stationName.equalsIgnoreCase("quarantine")) {
                    //
                    gotoNextActivity(QuarantineEntryActivity.class);
                } else {
                    gotoNextActivity(MainActivity.class);

                }

            } else {
                setContentView(R.layout.activity_login);
                initializeViews();
            }

        } catch (ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
        }


    }

    private void gotoNextActivity(Class<?> target) {

        // finish();
        Intent intent=new Intent(this,target);
        startActivity(intent);
        this.finish();
    }

    private void initializeViews() {

        AssetManager assetManager = getAssets();
        Typeface historiaTypeface = Typeface.createFromAsset(assetManager, "fonts/Mergic.otf");
        TextView mTitle = findViewById(R.id.vT_title);
        mTitle.setTypeface(historiaTypeface);

        mVersion = findViewById(R.id.vT_al_versionname);

        mLoginPassword = findViewById(R.id.vE_al_password);
        mEditIP = findViewById(R.id.vI_al_edit_ip);
        mEditIP.setOnClickListener(this);

        mProgressBar = findViewById(R.id.vP_al_progressbar);
        mProgressBar.setVisibility(View.GONE);

        //mLineSpinner=findViewById(R.id.vS_line_name);
        //mStationSpinner=findViewById(R.id.vS_ststion_name);

        mLineName = findViewById(R.id.vAT_al_line_name);
       /* ArrayAdapter<String> adapter = new ArrayAdapter<String>
                (this,android.R.layout.simple_dropdown_item_1line, lineNames);

        mLineName.setThreshold(1);

        mLineName.setAdapter(adapter);*/
        mLineName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                if (count >= 3) {
                    Log.d("linename", "Char sequence=>" + charSequence.toString() + "start =>" + start + " before=>" + before + " count" + count);
                    callStationsAPI(charSequence.toString().trim());
                }

            }

            @Override
            public void afterTextChanged(Editable editable) {
                //callStationsAPI(editable.toString().trim());


            }
        });

        mStationname = findViewById(R.id.vAT_al_station_name);

        mLogIn = findViewById(R.id.vL_al_login_layout);
        mLogIn.setOnClickListener(this);

        try {
            PackageInfo pinfo = null;
            String versionName;

            pinfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            // int versionNumber = pinfo.versionCode;
            versionName = pinfo.versionName;
            mVersion.setText("V:" + versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.vL_al_login_layout:
                //disableViews(mLogIn);
                String enteredline = mLineName.getText().toString().trim();
                String eneteredststion = mStationname.getText().toString().trim();
                String password = mLoginPassword.getText().toString().trim();

                if (mProgressBar != null) {
                    if (mProgressBar.getVisibility() == View.VISIBLE) {
                        showSnackBar(this, "wait untill the current process to finish");

                    } else {
                        // validateFields
                        if (!enteredline.isEmpty()) {
                            if (!eneteredststion.isEmpty()) {
                                if (!password.isEmpty() && password.length() > 5) {
                                    callUserLogInAPI(enteredline, eneteredststion, password);
                                } else {
                                    showSnackBar(this, "Enter a valid password");
                                    //mStationname.setError("Field Should not be empty");
                                }


                            } else {
                                showSnackBar(this, "Enter a valid station name");
                                //mStationname.setError("Field Should not be empty");
                            }

                        } else {
                            showSnackBar(this, "Enter a valid line name");
                            // mLineName.setError("Field Should not be empty");
                        }
                    }
                }
                break;
            case R.id.vI_al_edit_ip:
                showPasswordLayout();
                break;

            case R.id.vT_password_ok:
                hideKeyBoard();
                if (mPassword.getText().toString().equalsIgnoreCase("123456")) {
                    alertDialog.dismiss();
                    showIPAddressLayout();
                } else {
                    showSnackBar(this, "Password incorrect");
                }

                break;

            case R.id.vT_password_cancel:
                hideKeyBoard();
                alertDialog.dismiss();
                break;

            case R.id.vT_ipaddress_cancel:
                hideKeyBoard();
                alertDialog.dismiss();
                break;

            case R.id.vT_ipaddress_ok:
                hideKeyBoard();
                saveIpAddress();
                break;
        }

    }

    public void showPasswordLayout() {
        LinearLayout rootlayout = findViewById(R.id.vL_al_root_layout);
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View dialogView = inflater.inflate(R.layout.password_layout, rootlayout, false);
        mPassword = dialogView.findViewById(R.id.vE_password);
        TextView cancel = dialogView.findViewById(R.id.vT_password_cancel);
        TextView ok = dialogView.findViewById(R.id.vT_password_ok);

        builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        builder.setCancelable(false);

        alertDialog = builder.create();
        alertDialog.show();
        cancel.setOnClickListener(LoginActivity.this);
        ok.setOnClickListener(LoginActivity.this);

    }

    private void showIPAddressLayout() {
        String currentIP = Utilities.getIPAddress(LoginActivity.this);

        LinearLayout rootlayout = findViewById(R.id.vL_al_root_layout);
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View dialogView = inflater.inflate(R.layout.ipaddress_layout, rootlayout, false);
        mIPAddressEditText = dialogView.findViewById(R.id.vE_ipaddress);
        if (currentIP != null) {
            mIPAddressEditText.setText(currentIP);
        }
        TextView cancel = dialogView.findViewById(R.id.vT_ipaddress_cancel);
        TextView save = dialogView.findViewById(R.id.vT_ipaddress_ok);

        // builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        builder.setCancelable(false);

        alertDialog = builder.create();
        alertDialog.show();
        cancel.setOnClickListener(LoginActivity.this);
        save.setOnClickListener(LoginActivity.this);

    }

    private void saveIpAddress() {
        hideKeyBoard();
        String ip = mIPAddressEditText.getText().toString().trim();
        if (TextUtils.isEmpty(ip) || ip.length() < 12 || !ip.contains(".")) {
            showSnackBar(this, "enter a valid IP address");
            hideKeyBoard();
        } else {
            alertDialog.dismiss();
            hideKeyBoard();
            Utilities.saveIPAddressPreference(LoginActivity.this, ip);

            callGetLinesAPI();
        }

    }

    private void hideKeyBoard() {
        try {
            //InputMethodManager is used to hide the virtual keyboard from the user after finishing the user input
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            assert imm != null;
            if (imm.isAcceptingText()) {
                imm.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), 0);
            }
        } catch (NullPointerException e) {
            Log.e("Exception", e.getMessage() + ">>");
        }

    }

    private void callGetLinesAPI() {
        if (Utilities.isConnectedToInternet(getApplicationContext())) {

            toggleVisibility(true, mProgressBar);

            WebServices<LineNamesResponse> webServices = new WebServices<LineNamesResponse>(LoginActivity.this);
            webServices.getLineNames(Utilities.getBaseURL(LoginActivity.this), WebServices.ApiType.lineNames);

        } else {
            Utilities.showToast(LoginActivity.this, getResources().getString(R.string.err_msg_nointernet));
        }
    }

    private void callStationsAPI(String lineName) {
        StationName stationName = new StationName(lineName);
        if (Utilities.isConnectedToInternet(getApplicationContext())) {

            toggleVisibility(true, mProgressBar);

            WebServices<StationNamesResponse> webServices = new WebServices<StationNamesResponse>(LoginActivity.this);
            webServices.getStationNames(Utilities.getBaseURL(LoginActivity.this), WebServices.ApiType.stationNames, stationName);

        } else {
            Utilities.showToast(LoginActivity.this, getResources().getString(R.string.err_msg_nointernet));
        }
    }

    private void callUserLogInAPI(String lineName, String stationName, String password) {

        LogIn logIn = new LogIn(lineName, stationName, password);

        if (Utilities.isConnectedToInternet(getApplicationContext())) {

            toggleVisibility(true, mProgressBar);

            WebServices<LogInResponse> webServices = new WebServices<LogInResponse>(LoginActivity.this);
            webServices.userLogIn(Utilities.getBaseURL(LoginActivity.this), WebServices.ApiType.logIn, logIn);

        } else {
            Utilities.showToast(LoginActivity.this, getResources().getString(R.string.err_msg_nointernet));
        }
    }

    @Override
    public void onResponse(Object response, WebServices.ApiType URL, boolean isSucces, int code) {

        switch (URL) {
            case lineNames:
                toggleVisibility(false, mProgressBar);
                if (isSucces) {
                    LineNamesResponse lineNamesResponse = (LineNamesResponse) response;
                    if (lineNamesResponse != null) {
                        if (lineNamesResponse.getLinenames() != null) {
                            ArrayAdapter<String> adapter = new ArrayAdapter<String>
                                    (this, android.R.layout.simple_dropdown_item_1line, lineNamesResponse.getLinenames());

                            mLineName.setThreshold(1);

                            mLineName.setAdapter(adapter);

                        } else {
                            showSnackBar(LoginActivity.this, "Line names not found");
                        }
                    } else {
                        showSnackBar(LoginActivity.this, "Server is busy");
                    }

                } else {
                    //API call failed
                    showSnackBar(LoginActivity.this, "Server Timeout");
                }
                break;
            case stationNames:
                toggleVisibility(false, mProgressBar);
                if (isSucces) {
                    StationNamesResponse stationNamesResponse = (StationNamesResponse) response;
                    if (stationNamesResponse != null) {
                        if (stationNamesResponse.getStationNames() != null) {
                            ArrayAdapter<String> adapter = new ArrayAdapter<String>
                                    (this, android.R.layout.simple_dropdown_item_1line, stationNamesResponse.getStationNames());

                            mStationname.setThreshold(1);

                            mStationname.setAdapter(adapter);

                        } else {
                            showSnackBar(LoginActivity.this, "Station names not found for the entered line");
                        }
                    } else {
                        showSnackBar(LoginActivity.this, "Server is busy");
                    }

                } else {
                    //API call failed
                    showSnackBar(LoginActivity.this, "Server Timeout");
                }
                break;
            case logIn:
                toggleVisibility(false, mProgressBar);
                if (isSucces) {
                    LogInResponse logInResponse = (LogInResponse) response;
                    if (logInResponse != null) {
                        if (logInResponse.getStatus() != null && logInResponse.getMessage() != null) {
                            if (logInResponse.getStatus().contains("true")) {
                                //Success
                                String[] stationArray = logInResponse.getStatus().split("-");
                                String stationId = null;
                                try {
                                    stationId = stationArray[1];
                                } catch (ArrayIndexOutOfBoundsException e) {
                                    e.printStackTrace();
                                }

                                String stationName=mStationname.getText().toString().trim();
                                saveLogInPreference(LoginActivity.this, true,
                                        mLineName.getText().toString().trim(), stationName, stationId);



                                if (stationName.equalsIgnoreCase("analysis")) {
                                    //
                                    gotoNextActivity(AnalysisEntryActivity.class);
                                }
                                else if( stationName.equalsIgnoreCase("loms"))
                                {
                                    gotoNextActivity(LomsEntryActivity.class);
                                }

                                else if (stationName.equalsIgnoreCase("quarantine")) {
                                    //
                                    gotoNextActivity(QuarantineEntryActivity.class);
                                }
                                else {
                                    gotoNextActivity(MainActivity.class);

                                }

                                mLineName.setText("");
                                mStationname.setText("");
                                mLoginPassword.setText("");

                            } else {
                                showSnackBar(LoginActivity.this, logInResponse.getMessage());
                            }
                        } else {
                            showSnackBar(LoginActivity.this, "Something went wrong please try again");
                        }

                    } else {
                        showSnackBar(LoginActivity.this, "Server is busy");
                    }

                } else {
                    //API call failed
                    showSnackBar(LoginActivity.this, "Server Timeout");
                }
                break;
        }

    }

    private void toggleVisibility(boolean status, View... views) {
        for (View v : views) {
            //if status is true make view visible, else visibility gone
            if (status) {
                v.setVisibility(View.VISIBLE);

            } else {
                v.setVisibility(View.GONE);
            }
            /*if (v.getVisibility() == View.VISIBLE) {
                v.setVisibility(View.GONE);
            } else {
                v.setVisibility(View.VISIBLE);
            }*/
        }
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            this.finish();
            finishAffinity();
        }

        this.doubleBackToExitPressedOnce = true;
        // Snackbar.make(mCheckIn, R.string.back_press_to_exit, Snackbar.LENGTH_SHORT).show();
        //showToast(getResources().getString(R.string.back_press_to_exit));

       /* Snackbar snackbar = Snackbar.make(mLogIn,  R.string.back_press_to_exit,Snackbar.LENGTH_SHORT);
        View view = snackbar.getView();
        view.setBackgroundColor(Color.BLACK);
        TextView tv = (TextView)view.findViewById(android.support.design.R.id.snackbar_text);
        tv.setTextColor(Color.WHITE);
        snackbar.show();*/
        showSnackBar(this, getString(R.string.back_press_to_exit));
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2000);
    }

    @SuppressLint("BatteryLife")
    private void checkPowerOptimizationPermission() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Intent intent = new Intent();
            String packageName = getPackageName();
            PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData(Uri.parse("package:" + packageName));
                startActivity(intent);
            } else {
                //Permissions granted do whatever you want to do
            }
        }
    }

}
