package com.vvt.digitalfifo.activities;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.vvt.digitalfifo.LoginActivity;
import com.vvt.digitalfifo.R;
import com.vvt.digitalfifo.apirequests.LogOut;
import com.vvt.digitalfifo.apiresponses.logout.LogOutResponse;
import com.vvt.digitalfifo.utils.OnResponseListener;
import com.vvt.digitalfifo.utils.Utilities;
import com.vvt.digitalfifo.utils.WebServices;

import static com.vvt.digitalfifo.utils.Utilities.saveLogInPreference;
import static com.vvt.digitalfifo.utils.Utilities.showSnackBar;

public class QuarantineEntryActivity extends AppCompatActivity implements View.OnClickListener, OnResponseListener {

    CardView mQuarantineEntryIn,mQuarantineEntryOut;
    LinearLayout mLogOut;
    ProgressBar mProgressBar;
    public boolean doubleBackToExitPressedOnce = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*to preventing from taking screen shots*/
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.activity_quarantine_entry);
        initializeViews();
    }

    private void initializeViews() {
        AssetManager assetManager = getAssets();
        Typeface historiaTypeface = Typeface.createFromAsset(assetManager, "fonts/Mergic.otf");
        TextView mTitle = findViewById(R.id.vT_aqe_title);
        mTitle.setTypeface(historiaTypeface);

        TextView mVersion=findViewById(R.id.vT_aqe_versionname);

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
        mProgressBar=findViewById(R.id.vP_aqe_progressbar);
        mProgressBar.setVisibility(View.GONE);

        mQuarantineEntryIn=findViewById(R.id.vC_aqe_kanbanin);
        mQuarantineEntryOut=findViewById(R.id.vC_aqe_kanbanout);

        mLogOut=findViewById(R.id.vL_aqe_logout);

        mQuarantineEntryIn.setOnClickListener(this);
        mQuarantineEntryOut.setOnClickListener(this);
        mLogOut.setOnClickListener(this);

    }

    private void callLogOutAPI(String lineName, String stationName) {
        // StationName stationName = new StationName(lineName);
        LogOut logOut=new LogOut(lineName,stationName);
        if (Utilities.isConnectedToInternet(getApplicationContext())) {

            toggleVisibility(true,mProgressBar);

            WebServices<LogOutResponse> webServices = new WebServices<LogOutResponse>(QuarantineEntryActivity.this);
            webServices.logOut(Utilities.getBaseURL(QuarantineEntryActivity.this), WebServices.ApiType.logOut, logOut);

        } else {
            Utilities.showToast(QuarantineEntryActivity.this,getResources().getString(R.string.err_msg_nointernet));
        }
    }

    @Override
    public void onClick(View view) {

        switch (view.getId())
        {
            case R.id.vC_aqe_kanbanin:
                if(mProgressBar!=null)
                {
                    if(!mProgressBar.isShown())
                    {
                        gotoNextActivity(MainActivity.class);
                    }
                    else
                    {
                        showSnackBar(this,"Wait untill current process to complete");
                    }
                }

                break;

            case R.id.vC_aqe_kanbanout:
                if(mProgressBar!=null)
                {
                    if(!mProgressBar.isShown())
                    {
                        gotoNextActivity(QuarantineOutActivity.class);
                    }
                    else
                    {
                        showSnackBar(this,"Wait untill current process to complete");
                    }
                }

                break;

            case R.id.vL_aqe_logout:
                if(mProgressBar!=null)
                {
                    if(!mProgressBar.isShown())
                    {
                        String[] logInDetails = Utilities.getIsLoggedIn(this);
                        try {

                            String lineName = logInDetails[1].toUpperCase().trim();
                            String stationName = logInDetails[2].toUpperCase().trim();

                            callLogOutAPI(lineName,stationName);
                        }catch (ArrayIndexOutOfBoundsException e)
                        {
                            e.printStackTrace();
                        }



                    }
                    else
                    {
                        showSnackBar(this,"Wait untill current process to complete");
                    }
                }
                break;
        }

    }

    private void gotoNextActivity(Class<?> target) {

        // finish();
        Intent intent=new Intent(this,target);
        startActivity(intent);
        //this.finish();
    }

    @Override
    public void onResponse(Object response, WebServices.ApiType URL, boolean isSucces, int code) {
        switch (URL)
        {
            case logOut:
                toggleVisibility(false,mProgressBar);
                if(isSucces)
                {
                    LogOutResponse logOutResponse= (LogOutResponse) response;
                    if(logOutResponse!=null)
                    {
                        if(logOutResponse.getStatus()!=null && logOutResponse.getMessage()!=null)
                        {
                            if(logOutResponse.getStatus().equalsIgnoreCase("true"))
                            {
                                //Success
                                saveLogInPreference(this,false);

                                //startActivity(new Intent(this,LoginActivity.class));
                                gotoNextActivity(LoginActivity.class);
                                this.finish();
                            }
                            else {
                                showSnackBar(this,logOutResponse.getMessage());
                            }
                        }
                        else {
                            showSnackBar(this,"Something went wrong please try again");
                        }

                    }else {
                        showSnackBar(this,"Server is busy");
                    }

                }
                else {
                    //API call failed
                    showSnackBar(this,"Server Timeout");
                }
                break;
        }

    }

    //For Views
    private void toggleVisibility(boolean status,View... views) {
        for (View v : views) {
            //if status is true make view visible, else visibility gone
            if(status)
            {
                v.setVisibility(View.VISIBLE);

            }
            else {
                v.setVisibility(View.GONE);
            }

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

}
