package com.als.obd.activity;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.content.FileProvider;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.als.obd.BuildConfig;
import com.als.obd.R;
import com.als.obd.services.WiredObdService;
import com.als.obd.tools.ConfirmationDialog;
import com.als.obd.tools.Constants;
import com.als.obd.tools.DownloadAppService;
import com.als.obd.tools.NetUtils;
import com.als.obd.tools.SharedPref;
import com.als.obd.tools.VolleyRequest;
import com.android.volley.VolleyError;
import com.hjq.permissions.OnPermission;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;
import com.hjq.toast.ToastUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class ObdMainActivity extends Activity  {

    String ApiGetAppDetails = "http://eld.alsrealtime.com/api/ELDAPI/GetAndroidOBDAppDetail";
    long MIN_TIME_BW_UPDATES = 2000;  // 2 Sec
    Integer pageNumber = 1;
    MyTimerTask timerTask;
    private Timer mTimer;
    SharedPref sharedPref;

    TextView versionTv, odometerTv,engineHrTv, ignitionStatusTv,tripDistanceTv, vinNumberTv, rpmTv, vssTv, highPrecesionOdoTv, timeStampTv, welcomeTV, checkAppUpdateTV;
    LinearLayout odoDetailItemView;
    RelativeLayout checkAppUpdateBtn;
    ProgressBar downloadProgressBar;
    boolean isPermit = false, IsDownloading = false;
    ConfirmationDialog confirmationDialog;
    String VersionCode = "", VersionName = "", ExistingApkVersionCode = "", ExistingApkVersionName = "" ;
    private String ApkFilePath = "", existingApkFilePath = "";
    int ExistingVersionCodeInt  = 0,  VersionCodeInt = 0, AppInstallAttemp = 0;
    long progressPercentage = 0;
    Constants constants;
    VolleyRequest GetAppUpdateRequest;
    DownloadAppService downloadAppService = new DownloadAppService();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.obd_main_activity);

        sharedPref = new SharedPref();
        constants   = new Constants();
        GetAppUpdateRequest         = new VolleyRequest(getApplicationContext());

        versionTv = (TextView)findViewById(R.id.versionTv);
        odometerTv = (TextView)findViewById(R.id.odometerTv);
        engineHrTv = (TextView)findViewById(R.id.engineHrTv);
        ignitionStatusTv = (TextView)findViewById(R.id.ignitionStatusTv);
        tripDistanceTv = (TextView)findViewById(R.id.tripDistanceTv);
        vinNumberTv = (TextView)findViewById(R.id.vinNumberTv);
        rpmTv = (TextView)findViewById(R.id.rpmTv);
        vssTv = (TextView)findViewById(R.id.vssTv);
        timeStampTv = (TextView)findViewById(R.id.timeStampTv);
        highPrecesionOdoTv = (TextView)findViewById(R.id.highPrecesionOdoTv);

        welcomeTV = (TextView)findViewById(R.id.welcomeTV);
        checkAppUpdateTV = (TextView)findViewById(R.id.checkAppUpdateTV);

        downloadProgressBar = (ProgressBar)findViewById(R.id.downloadProgressBar);
        checkAppUpdateBtn = (RelativeLayout)findViewById(R.id.checkAppUpdateBtn);
        odoDetailItemView = (LinearLayout)findViewById(R.id.odoDetailItemView);
        odoDetailItemView.setVisibility(View.VISIBLE);


        String AppVersion = constants.GetAppVersion(this, "VersionName");
        versionTv.setText("Version " + AppVersion);


        checkAppUpdateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(downloadProgressBar.getVisibility() == View.GONE){

                    if(IsDownloading){

                        if (confirmationDialog != null && confirmationDialog.isShowing())
                            confirmationDialog.dismiss();
                        confirmationDialog = new ConfirmationDialog(getApplicationContext(), "settings", new ConfirmListener());
                        confirmationDialog.show();

                    }else {

                        getInstalledAppDetail();
                        File existingFile = new File(constants.getAlsApkPath() + "/" + constants.getExistingApkPath());
                        if (!existingFile.isFile()) {
                            checkAppUpdateTV.setText(getResources().getString(R.string.checkUpdate));
                        }

                        if (ExistingApkVersionCode.equals(VersionCode) && ExistingApkVersionName.equals(VersionName)) {
                            ToastUtils.show("Your application is up to date");
                        } else {
                            String updateTvText = checkAppUpdateTV.getText().toString();
                            if (updateTvText.equals(getResources().getString(R.string.installUpdate))) {
                                if (ApkFilePath.length() > 0) {
                                    InstallApp(ApkFilePath);
                                } else {
                                    checkAppUpdateTV.setText(getResources().getString(R.string.checkUpdate));
                                    ToastUtils.show("File not found");
                                }
                            } else {

                                if(ApkFilePath.length() == 0) {
                                    GetAppDetails();
                                }else{

                                    CheckAppStatus(); //downloadButtonClicked(ApkFilePath, VersionCode, VersionName, IsDownloading);

                                }
                            }
                        }
                    }


                }
            }
        });

        requestPermission();


    }



    @Override
    protected void onResume() {
        super.onResume();
        if(isPermit) {
            RestartTimer();
        }

        getInstalledAppDetail();
        existingApkFilePath = constants.getExistingApkPath();

        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver( progressReceiver, new IntentFilter("download_progress"));


    }


    @Override
    public void onPause() {
        super.onPause();
        if(isPermit) {
            clearTimer();
        }

        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(progressReceiver);

    }



    void CheckAppStatus(){
        if (ExistingVersionCodeInt < VersionCodeInt) {

            // Check app is already saved in sd card
            existingApkFilePath = constants.getExistingApkPath();
            if (existingApkFilePath.length() > 0) {
                String[] apkPathArray = existingApkFilePath.split("_");
                if (apkPathArray.length > 2) {
                    ExistingApkVersionCode = apkPathArray[1];
                    ExistingApkVersionName = apkPathArray[2];
                    ExistingApkVersionName = ExistingApkVersionName.replaceAll(".apk", "");

                    if (ExistingApkVersionCode.equals(VersionCode) && ExistingApkVersionName.equals(VersionName)) {
                        checkAppUpdateTV.setText("Install Updates");
                        ToastUtils.show("This application is already in (Logistic/ObdServer/) folder");
                        ApkFilePath = constants.getAlsApkPath() + "/" + existingApkFilePath;
                        InstallApp(ApkFilePath);
                    } else {
                        downloadButtonClicked(ApkFilePath, VersionCode, VersionName, IsDownloading);
                    }
                } else {
                    downloadButtonClicked(ApkFilePath, VersionCode, VersionName, IsDownloading);
                }
            } else {
                downloadButtonClicked(ApkFilePath, VersionCode, VersionName, IsDownloading);
            }

        }else{
            ToastUtils.show("Your application is up to date");
        }
    }

    void downloadButtonClicked(String url, String VersionCode, String VersionName, boolean downloadStatus) {

        IsDownloading = true;
        downloadProgressBar.setVisibility(View.VISIBLE);
        // checkAppUpdateBtn.setEnabled(false);
        checkAppUpdateTV.setText("Downloading");
        ApkFilePath = "";
        progressPercentage = 0;


        Intent serviceIntent = new Intent(getApplicationContext(), downloadAppService.getClass());
        serviceIntent.putExtra("url", url);
        serviceIntent.putExtra("VersionCode", VersionCode);
        serviceIntent.putExtra("VersionName", VersionName);
        serviceIntent.putExtra("isDownloading", downloadStatus);
        startService(serviceIntent);

    }





    /*================== Get app details ===================*/
  void GetAppDetails(){  // final String SearchDate

      if(NetUtils.isConnected(getApplicationContext())) {
          downloadProgressBar.setVisibility(View.VISIBLE);

          Map<String, String> params = new HashMap<String, String>();
          GetAppUpdateRequest.executeRequest(com.android.volley.Request.Method.GET, ApiGetAppDetails, params, 101,
                  10000, ResponseCallBack, ErrorCallBack);
      }else{
          ToastUtils.show("Not connected with internet");
      }

    }





    void InstallApp(String appPath){
        progressPercentage = 0;
        File toInstall = new File(appPath);

        if(toInstall.isFile()) {

            if(AppInstallAttemp < 2) { // It means apk file has some problem and need to delete it to download again.
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    Uri apkUri = FileProvider.getUriForFile(getApplicationContext(), BuildConfig.APPLICATION_ID + ".provider", toInstall);
                    Intent intent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
                    intent.setData(apkUri);
                    intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                    intent.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true);
                    intent.putExtra(Intent.EXTRA_RETURN_RESULT, true);
                    intent.putExtra(Intent.EXTRA_INSTALLER_PACKAGE_NAME, getApplicationInfo().packageName);
                    //  startActivityForResult(intent, REQUEST_INSTALL);


                    startActivity(intent);
                } else {
                    Uri apkUri = Uri.fromFile(toInstall);
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
                AppInstallAttemp++;
            }else{
                // Deleter apk file is exist
                String folder      = constants.getAlsApkPath().toString();
                constants.DeleteDirectory(folder);
                AppInstallAttemp = 0;

                checkAppUpdateBtn.performClick();

            }
        }

    }


    void getInstalledAppDetail(){
        ExistingApkVersionCode = constants.GetAppVersion(getApplicationContext(), "VersionCode");
        ExistingApkVersionName = constants.GetAppVersion(getApplicationContext(), "VersionName");

    }







    VolleyRequest.VolleyCallback ResponseCallBack = new VolleyRequest.VolleyCallback() {

        @Override
        public void getResponse(String response, int flag) {

            Log.d("response", "response: " + response);

            downloadProgressBar.setVisibility(View.GONE);

            try {



                JSONObject obj = new JSONObject(response);
                String status = obj.getString("Status");

                if (status.equalsIgnoreCase("true")) {
                    JSONObject dataObj = new JSONObject(obj.getString("Data"));
                    VersionCode = dataObj.getString("VersionCode");
                    VersionName = dataObj.getString("VersionName");
                    ApkFilePath = dataObj.getString("ApkFilePath");

                   // ApkFilePath = ApkFilePath.replace("Resources/", "");
                    try {
                        ExistingVersionCodeInt = Integer.valueOf(ExistingApkVersionCode);
                        VersionCodeInt = Integer.valueOf(VersionCode);
                    } catch (Exception e) {
                        ExistingVersionCodeInt = 0;
                        VersionCodeInt = 0;
                        e.printStackTrace();
                    }

                    if (ExistingApkVersionCode.equals(VersionCode) && ExistingApkVersionName.equals(VersionName)) {
                        ToastUtils.show("Your application is up to date");
                    } else {
                        CheckAppStatus();
                    }
                }else{
                    ToastUtils.show(obj.getString("Message"));
                }
            }catch (Exception e){
                e.printStackTrace();
            }

        }

    };

    VolleyRequest.VolleyErrorCall ErrorCallBack = new VolleyRequest.VolleyErrorCall(){
        @Override
        public void getError(VolleyError error, int flag) {
            switch (flag){

                default:
                   downloadProgressBar.setVisibility(View.GONE);
                    ToastUtils.show(error.toString());

                    Log.d("Driver", "error" + error.toString());
                    break;
            }
        }
    };





    /*================== Confirmation Listener ====================*/
    private class ConfirmListener implements ConfirmationDialog.ConfirmationListener {

        @Override
        public void OkBtnReady() {

            sharedPref.setAsyncCancelStatus(true, getApplicationContext());
            IsDownloading = false;

            downloadProgressBar.setVisibility(View.GONE);
            checkAppUpdateTV.setText(getResources().getString(R.string.checkUpdate));

            confirmationDialog.dismiss();
        }
    }




    public void requestPermission() {
        XXPermissions.with(this)
                //You can set up a rejected application to continue request permission until the user is authorized or permanently rejected
                .constantRequest()
                .permission(Manifest.permission.BLUETOOTH_ADMIN)
                .permission(Manifest.permission.ACCESS_NETWORK_STATE,Manifest.permission.INTERNET)
                .permission(Permission.Group.LOCATION,Permission.Group.STORAGE)
                .request(new OnPermission() {

                    @Override
                    public void hasPermission(List<String> granted, boolean isAll) {
                        if (isAll) {

                            isPermit = true;


                            /*========= Start Service =============*/
                            Intent serviceIntent = new Intent(getApplicationContext(), WiredObdService.class);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                startForegroundService(serviceIntent);
                            }
                            startService(serviceIntent);



                        }else {
                            ToastUtils.show(getString(R.string.toast_request_some_failed));
                        }
                    }

                    @Override
                    public void noPermission(List<String> denied, boolean quick) {
                        if(quick) {
                            ToastUtils.show(getString(R.string.toast_request_denied));
                            //If it is permanently rejected, jump to the application permission in system Settings page
                            XXPermissions.gotoPermissionSettings(ObdMainActivity.this);
                        }else {
                            ToastUtils.show(getString(R.string.toast_request_failed));
                        }
                    }
                });
    }





    private BroadcastReceiver progressReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            long percentage     = intent.getIntExtra("percentage", 0);
            ApkFilePath         = intent.getStringExtra("path");
            boolean isCompleted = intent.getBooleanExtra("isCompleted", false);

            if(percentage >= progressPercentage) {
                progressPercentage = percentage;
            }

            if(isCompleted){
                IsDownloading = false;
                downloadProgressBar.setVisibility(View.GONE);

                if(ApkFilePath.equals("Downloading failed.")){
                    ToastUtils.show(ApkFilePath);
                    ApkFilePath = "";
                    ExistingApkVersionCode = "";
                    ExistingApkVersionName = "";
                    checkAppUpdateTV.setText(getResources().getString(R.string.checkUpdate));
                }else{
                    ToastUtils.show("Downloading completed");
                    checkAppUpdateTV.setText(getResources().getString(R.string.installUpdate));
                }
            }

        }
    };





    private class MyTimerTask extends TimerTask {
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        public void run() {
            Log.e("Log", "----TimerTask Running");

            try {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        welcomeTV.setVisibility(View.GONE);

                        odometerTv.setText( sharedPref.getOdometer(getApplicationContext()) );
                        engineHrTv.setText(sharedPref.getEngineHours(getApplicationContext()) );
                        ignitionStatusTv.setText(sharedPref.getIgnitionStatus(getApplicationContext()) );
                        tripDistanceTv.setText(sharedPref.getTripDistance(getApplicationContext()) );
                        vinNumberTv.setText(sharedPref.getVINNumber(getApplicationContext()) );
                        rpmTv.setText(sharedPref.getRPM(getApplicationContext()) );
                        vssTv.setText("" + sharedPref.getVss(getApplicationContext()) );
                        timeStampTv.setText(sharedPref.getTimeStamp(getApplicationContext()) );

                        try {
                            String HighPrecisionOdometer = sharedPref.getHighPrecisionOdometer(getApplicationContext());
                            int intHighPrecisionOdometer = (int)(Integer.valueOf(HighPrecisionOdometer) * 0.001);
                            highPrecesionOdoTv.setText(String.valueOf(intHighPrecisionOdometer) );
                        }catch (Exception e){
                            e.printStackTrace();
                        }

                    }
                });
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        }
    }




    private void RestartTimer() {
        try {
            clearTimer();
            mTimer = new Timer();
            timerTask = new MyTimerTask();
            mTimer.schedule(timerTask, MIN_TIME_BW_UPDATES, MIN_TIME_BW_UPDATES);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    void clearTimer() {
        try {
            if (mTimer != null) {
                mTimer.cancel();
                timerTask.cancel();
                mTimer = null;
                timerTask = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }




}
