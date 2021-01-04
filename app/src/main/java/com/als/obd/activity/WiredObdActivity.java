package com.als.obd.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.als.obd.R;
import com.als.obd.services.WiredObdService;
import com.als.obd.tools.SharedPref;
import com.hjq.permissions.OnPermission;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;
import com.hjq.toast.ToastUtils;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class WiredObdActivity extends Activity {

    long MIN_TIME_BW_UPDATES = 2000;  // 2 Sec
    TextView odometerTv,engineHrTv, ignitionStatusTv,tripDistanceTv, vinNumberTv, rpmTv, vssTv, timeStampTv, highPrecesionOdoTv;
    MyTimerTask timerTask;
    private Timer mTimer;
    SharedPref sharedPref;
    LinearLayout odoDetailItemView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.wired_obd_activity);

        sharedPref = new SharedPref();

        odometerTv = (TextView)findViewById(R.id.odometerTv);
        engineHrTv = (TextView)findViewById(R.id.engineHrTv);
        ignitionStatusTv = (TextView)findViewById(R.id.ignitionStatusTv);
        tripDistanceTv = (TextView)findViewById(R.id.tripDistanceTv);
        vinNumberTv = (TextView)findViewById(R.id.vinNumberTv);
        rpmTv = (TextView)findViewById(R.id.rpmTv);
        vssTv = (TextView)findViewById(R.id.vssTv);
        timeStampTv = (TextView)findViewById(R.id.timeStampTv);
        highPrecesionOdoTv = (TextView)findViewById(R.id.highPrecesionOdoTv);

        odoDetailItemView=(LinearLayout)findViewById(R.id.odoDetailItemView);

        odoDetailItemView.setVisibility(View.VISIBLE);

        requestPermission();


    }


    @Override
    protected void onResume() {
        super.onResume();
        RestartTimer();
    }


    @Override
    public void onPause() {
        super.onPause();
        clearTimer();
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

                            /*========= Start Service =============*/
                            Intent serviceIntent = new Intent(getApplicationContext(), WiredObdService.class);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                startForegroundService(serviceIntent);
                            }
                            startService(serviceIntent);


                        }else {
                            ToastUtils.show(R.string.toast_request_some_failed);
                        }
                    }

                    @Override
                    public void noPermission(List<String> denied, boolean quick) {
                        if(quick) {
                            ToastUtils.show(R.string.toast_request_denied);
                            //If it is permanently rejected, jump to the application permission in system Settings page
                            XXPermissions.gotoPermissionSettings(WiredObdActivity.this);
                        }else {
                            ToastUtils.show(R.string.toast_request_failed);
                        }
                    }
                });
    }




    private class MyTimerTask extends TimerTask {
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        public void run() {
            Log.e("Log", "----TimerTask Running");

            try {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {


                        odometerTv.setText( sharedPref.getOdometer(getApplicationContext()) );
                        engineHrTv.setText(sharedPref.getEngineHours(getApplicationContext()) );
                        ignitionStatusTv.setText(sharedPref.getIgnitionStatus(getApplicationContext()) );
                        tripDistanceTv.setText(sharedPref.getTripDistance(getApplicationContext()) );
                        vinNumberTv.setText(sharedPref.getVINNumber(getApplicationContext()) );
                        rpmTv.setText(sharedPref.getRPM(getApplicationContext()) );
                        vssTv.setText("" + sharedPref.getVss(getApplicationContext()) );
                        timeStampTv.setText(sharedPref.getTimeStamp(getApplicationContext()) );


                    }
                });
            } catch (Exception e) {
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
