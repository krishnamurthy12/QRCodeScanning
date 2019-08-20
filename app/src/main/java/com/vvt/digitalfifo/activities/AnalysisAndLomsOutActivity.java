package com.vvt.digitalfifo.activities;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;
import com.vvt.digitalfifo.LoginActivity;
import com.vvt.digitalfifo.R;
import com.vvt.digitalfifo.apirequests.KanbanScan;
import com.vvt.digitalfifo.apirequests.LogOut;
import com.vvt.digitalfifo.apiresponses.kanbonscan.KanbonScanResponse;
import com.vvt.digitalfifo.apiresponses.logout.LogOutResponse;
import com.vvt.digitalfifo.barcode.BarcodeCaptureActivity;
import com.vvt.digitalfifo.utils.OnResponseListener;
import com.vvt.digitalfifo.utils.Utilities;
import com.vvt.digitalfifo.utils.WebServices;

import java.util.Arrays;

import static com.vvt.digitalfifo.utils.Utilities.saveLogInPreference;
import static com.vvt.digitalfifo.utils.Utilities.showSnackBar;

public class AnalysisAndLomsOutActivity extends AppCompatActivity implements View.OnClickListener, OnResponseListener {

    LinearLayout mLogOut, mSuccessLayout, mFailureLayout, mScanLayout;
        ProgressBar mProgressBar;
    TextView mLineName, mStationName, mScannedText, mSuccessMessage, mFailureMessage;
    Handler mHandler;

    //Button mSuccess;

    private int BARCODE_READER_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*to preventing from taking screen shots*/
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE);

        setContentView(R.layout.activity_analysis_and_loms_scan);
        initializeViews();
    }

    private void initializeViews() {

        mLineName = findViewById(R.id.vT_aaal_line_name);
        mStationName = findViewById(R.id.vT_aaal_station_name);

        mScannedText = findViewById(R.id.vT_aaal_result_textview);

        mLogOut = findViewById(R.id.vL_aaal_logout);
        mLogOut.setOnClickListener(this);

        mProgressBar = findViewById(R.id.vP_aaal_progressbar);
        mProgressBar.setVisibility(View.GONE);

        mSuccessLayout = findViewById(R.id.vL_aaal_success_layout);
        mFailureLayout = findViewById(R.id.vL_aaal_failure_layout);

        mSuccessMessage = findViewById(R.id.vT_aaal_success_msg);
        mFailureMessage = findViewById(R.id.vT_aaal_failure_msg);

        mScanLayout = findViewById(R.id.vL_aaal_scan_layout);
        mScanLayout.setOnClickListener(this);

      /*  mSuccess=findViewById(R.id.btn_success);
        mSuccess.setOnClickListener(this);*/

        mHandler = new Handler(getMainLooper());

        //To hide views
        hideSuccessAndfailureLayouts();

        String[] logInDetails = Utilities.getIsLoggedIn(this);

        try {

            String LINENAME = logInDetails[1].toUpperCase().trim();
            String STATIONNAME = logInDetails[2].toUpperCase().trim();

            if (!TextUtils.isEmpty(LINENAME) && !TextUtils.isEmpty(STATIONNAME)) {
                mLineName.setText(LINENAME);
                mStationName.setText(STATIONNAME);
            }

        } catch (ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.vL_aaal_logout:
                if (mProgressBar != null) {
                    if (!mProgressBar.isShown()) {
                        hideSuccessAndfailureLayouts();

                        String lineName = mLineName.getText().toString().trim();
                        String stationName = mStationName.getText().toString().trim();
                        callLogOutAPI(lineName, stationName);

                    } else {
                        showSnackBar(this, "Wait untill current process to complete");
                    }
                }
                break;

            case R.id.vL_aaal_scan_layout:
                if (mProgressBar != null) {
                    if (!mProgressBar.isShown()) {
                        hideSuccessAndfailureLayouts();
                        scanCode();
                    }
                }

                break;

        }

    }

    private void scanCode() {
        hideSuccessAndfailureLayouts();
        Intent intent = new Intent(this, BarcodeCaptureActivity.class);
        startActivityForResult(intent, BARCODE_READER_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == BARCODE_READER_REQUEST_CODE) {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                if (data != null) {

                    Barcode barcode = data.getParcelableExtra(BarcodeCaptureActivity.BarcodeObject);
                    String scannedtext = barcode.displayValue;
                    scannedtext = scannedtext.replaceAll("\\*", "");
                    // mScannedText.setText(scannedtext);
                    if (scannedtext != null) {

                        Log.d("capturedtext", scannedtext);
                        String partNumber = null, quantity = null, kanbanNumber = null;
                        if (scannedtext.startsWith("@") && scannedtext.endsWith("@")) {
                            //Right QR code
                            scannedtext = scannedtext.replaceAll("@", "");

                            mScannedText.setTextColor(getResources().getColor(R.color.colorPrimary));
                            mScannedText.setText("");

                            String[] scannedDetails = scannedtext.split("#");

                            String lineName = mLineName.getText().toString().trim();
                            String stationName = mStationName.getText().toString().trim();

                            Log.d("scanneddetails", Arrays.toString(scannedDetails));
                            try {
                                if (scannedDetails.length >= 2) {
                                    partNumber = scannedDetails[0].trim();
                                    kanbanNumber = scannedDetails[1].trim();
                                    quantity = scannedDetails[2].trim();

                                    mScannedText.setTextColor(getResources().getColor(R.color.design_default_color_primary_dark));
                                    mScannedText.setText("Part Number=> " + partNumber + "\nKanban Number=> " + kanbanNumber);

                                    callValidateKanbonAPI(lineName, stationName, partNumber, quantity, kanbanNumber);

                                } else {
                                    Log.d("scanneddetails", "Invalid parameters");
                                    // Toast.makeText(this, "scannedDetails is less than 3", Toast.LENGTH_SHORT).show();
                                }
                            } catch (ArrayIndexOutOfBoundsException e) {
                                e.printStackTrace();
                            }

                        } else {
                            //Wrong QR code
                            mScannedText.setTextColor(getResources().getColor(R.color.error_red));
                            mScannedText.setText("Invalid QR code");
                        }


                        //checkscannedText(scannedtext);

                    }

                } else {
                    mScannedText.setTextColor(getResources().getColor(R.color.error_red));
                    mScannedText.setText(R.string.no_barcode_captured);
                }
            } else {
                mScannedText.setTextColor(getResources().getColor(R.color.error_red));
                mScannedText.setText(R.string.barcode_error_format);
            }
        }

    }

    private void callValidateKanbonAPI(String lineName, String stationName, String partNumber, String quantity, String kanbonNumber) {
        // StationName stationName = new StationName(lineName);

        KanbanScan kanbonScan = new KanbanScan(lineName, stationName, partNumber, kanbonNumber, quantity);
        if (Utilities.isConnectedToInternet(getApplicationContext())) {

            toggleVisibility(true, mProgressBar);

            WebServices<KanbonScanResponse> webServices = new WebServices<KanbonScanResponse>(this);
            webServices.analysisAndLomsOut(Utilities.getBaseURL(this), WebServices.ApiType.analysisAndLomsOut, kanbonScan);

        } else {
            Utilities.showToast(this, getResources().getString(R.string.err_msg_nointernet));
        }
    }

    private void callLogOutAPI(String lineName, String stationName) {
        // StationName stationName = new StationName(lineName);
        LogOut logOut = new LogOut(lineName, stationName);
        if (Utilities.isConnectedToInternet(getApplicationContext())) {

            toggleVisibility(true, mProgressBar);

            WebServices<LogOutResponse> webServices = new WebServices<LogOutResponse>(this);
            webServices.logOut(Utilities.getBaseURL(this), WebServices.ApiType.logOut, logOut);

        } else {
            Utilities.showToast(this, getResources().getString(R.string.err_msg_nointernet));
        }
    }

    @Override
    public void onResponse(Object response, WebServices.ApiType URL, boolean isSucces, int code) {
        switch (URL) {
            case analysisAndLomsOut:
                toggleVisibility(false, mProgressBar);
                if (isSucces) {
                    KanbonScanResponse kanbonScanResponse = (KanbonScanResponse) response;
                    if (kanbonScanResponse != null) {
                        if (kanbonScanResponse.getMessage() != null && kanbonScanResponse.getStatus() != null) {
                            if (kanbonScanResponse.getStatus().equalsIgnoreCase("success")) {
                                //Success case
                                showSuccessLayout(kanbonScanResponse.getMessage());

                            } else {
                                //Failure case
                                showFailureLayout(kanbonScanResponse.getMessage());
                            }

                        } else {
                            showSnackBar(this, "Something went wrong please try again");
                        }

                    } else {
                        showSnackBar(this, "Server is busy");
                    }

                } else {
                    //API call failed
                    showSnackBar(this, "Server Timeout");
                }
                break;

            case logOut:
                toggleVisibility(false, mProgressBar);
                if (isSucces) {
                    LogOutResponse logOutResponse = (LogOutResponse) response;
                    if (logOutResponse != null) {
                        if (logOutResponse.getStatus() != null && logOutResponse.getMessage() != null) {
                            if (logOutResponse.getStatus().equalsIgnoreCase("true")) {
                                //Success
                                saveLogInPreference(this, false);

                                startActivity(new Intent(this, LoginActivity.class));
                                this.finish();
                            } else {
                                showSnackBar(this, logOutResponse.getMessage());
                            }
                        } else {
                            showSnackBar(this, "Something went wrong please try again");
                        }

                    } else {
                        showSnackBar(this, "Server is busy");
                    }

                } else {
                    //API call failed
                    showSnackBar(this, "Server Timeout");
                }
                break;
        }

    }

    private void showSuccessLayout(String successMessage) {
        toggleVisibility(false, mProgressBar);
        toggleVisibility(true, mSuccessLayout);
        mSuccessMessage.setText(successMessage.trim());

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                hideSuccessAndfailureLayouts();
                mHandler.removeCallbacksAndMessages(null);
            }
        }, 60000);

    }

    private void showFailureLayout(String failureMessage) {
        toggleVisibility(false, mProgressBar);
        toggleVisibility(true, mFailureLayout);
        mFailureMessage.setText(failureMessage.trim());

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                hideSuccessAndfailureLayouts();
                mHandler.removeCallbacksAndMessages(null);
            }
        }, 60000);

    }

    private void hideSuccessAndfailureLayouts() {
        toggleVisibility(false, mProgressBar);
        mHandler.removeCallbacksAndMessages(null);
        toggleVisibility(false, mSuccessLayout, mFailureLayout);
    }

    //For Views
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

    //For ViewGroup/s
    private void toggleVisibility(boolean status, ViewGroup... views) {
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
        super.onBackPressed();
        hideSuccessAndfailureLayouts();
        this.finish();
    }
}
