package com.als.obd.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.als.obd.tools.Constants;
import com.als.obd.tools.SharedPref;
import com.hjq.toast.ToastUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainService extends Service {

    private Constants constants;
    private Messenger messenger; //receives remote invocations
    IncomingHandler incommingHandler;
    Handler handler;
    String TAG          = "MainService";
    String WiredOBD     = "wired_obd";
    String WifiOBD      = "wifi_obd";
    String BluetoothOBD = "bluetooth_obd";
    String StopServer   = "stop_obd";
    String command = "",  receivedHandleMsg = "";
    Message message;
    ServiceBroadcastReceiver mMessageReceiver;
    Bundle bundle;
    SharedPref sharedPref;


    @Override
    public void onCreate() {
        super.onCreate();

        constants   = new Constants();
        sharedPref  = new SharedPref();
        mMessageReceiver = new ServiceBroadcastReceiver();

        ServiceReceiverUpdate();

    }

    @Override
    public IBinder onBind(Intent intent)
    {
        System.out.println("*********** Remote Service onBind **********");

        if(this.messenger == null)
        {
            synchronized(MainService.class)
            {
                if(this.messenger == null)
                {
                    incommingHandler = new IncomingHandler();
                    this.messenger = new Messenger(incommingHandler);
                    handler = incommingHandler;
                }
            }
        }
        //Return the proper IBinder instance
        return this.messenger.getBinder();
    }

    private class IncomingHandler extends Handler
    {
        @Override
        public void handleMessage(Message msg)
        {

            System.out.println("***** Remote Service successfully invoked!!!!!!");
            bundle = msg.getData();
            command = bundle.getString("key");
            message = msg;
            Log.d("data", "coming: " + command);

            // ================================================================
            if(command.equals(WiredOBD)){

                if(constants.isServiceRunning(getBaseContext(), WiredObdService.class)){
                    Log.d(TAG, "WiredObdService already running");

                    if(sharedPref.getRPM(getApplicationContext()).equals("0")){
                        constants.startObdService(getBaseContext(), WiredObdService.class);

                        String replyStr = "Hello Client, your msg has been received. Sent via Wired OBD";
                        sendReply(this, replyStr, msg);
                    }else{
                        String replyStr = "Hello Client, your msg has been received. Sent via Wired OBD";
                        sendReply(this, replyStr, msg);
                    }

                }else {
                    constants.startObdService(getBaseContext(), WiredObdService.class);
                }

            }else if(command.equals(WifiOBD)){

                Log.d(TAG, "WiredObdService already running");
                constants.startObdService(getBaseContext(), WifiObdService.class);


                // sendReply(this,  receivedHandleMsg, msg);


               /* if(constants.isServiceRunning(getBaseContext(), WifiObdService.class)){
                    Log.d(TAG, "WiredObdService already running");
                    sendReply(this, msg);

                 //   LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver( mMessageReceiver, new IntentFilter("wifi_obd"));

                    constants.startObdService(getBaseContext(), WifiObdService.class);
                }else {
                    constants.startObdService(getBaseContext(), WifiObdService.class);
                }*/

            }else if(command.equals(StopServer)){
                if(constants.isServiceRunning(getBaseContext(), WiredObdService.class)){
                    constants.stopObdService(getBaseContext(), WiredObdService.class);
                }

                if(constants.isServiceRunning(getBaseContext(), WifiObdService.class)){
                    unregisterReceiver(mMessageReceiver );
                    constants.stopObdService(getBaseContext(), WifiObdService.class);
                }
            }else{
                stopSelf();
            }

        }
    }

    @Override
    public void onDestroy() {
        if(constants.isServiceRunning(getBaseContext(), WiredObdService.class)){
            constants.stopObdService(getBaseContext(), WiredObdService.class);
        }

        if(constants.isServiceRunning(getBaseContext(), WifiObdService.class)){
            constants.stopObdService(getBaseContext(), WifiObdService.class);
        }


        super.onDestroy();
    }



    private class ServiceBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            receivedHandleMsg = intent.getStringExtra("Status");
           // Bundle b = intent.getBundleExtra("Location");
          //  lastKnownLoc = (Location) b.getParcelable("Location");

               Log.d("wifi response", "message: " +receivedHandleMsg );
           // sendReply(incommingHandler, msg, message);
        }
    }

    private void ServiceReceiverUpdate(){
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(constants.packageName);
        getApplicationContext().registerReceiver(mMessageReceiver, intentFilter);
    }


    //Setup the reply message
    private void sendReply(Handler handler, String obddata, Message msgg){
     //   SimpleDateFormat dateFormatGmt = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a");
     //   String currentTime = dateFormatGmt.format(new Date());

        Message msg1 = handler.obtainMessage();
        Bundle bundle = passValuesInBundle();
        msg1.setData(bundle);

        try {
            //make the RPC invocation
            Messenger replyTo = msgg.replyTo;
            replyTo.send(msg1);

        } catch (Exception rme) {
            rme.printStackTrace();
            ToastUtils.show("Invocation Failed!!");
        }
    }


    private Bundle passValuesInBundle(){
        Bundle bundle = new Bundle();
        bundle.putString( constants.OBD_Odometer, sharedPref.getOdometer(getApplicationContext()) );
        bundle.putString( constants.OBD_EngineHours, sharedPref.getEngineHours(getApplicationContext()) );
        bundle.putString( constants.OBD_IgnitionStatus, sharedPref.getIgnitionStatus(getApplicationContext()));
        bundle.putString( constants.OBD_TripDistance, sharedPref.getTripDistance(getApplicationContext()));
        bundle.putString( constants.OBD_VINNumber, sharedPref.getVINNumber(getApplicationContext()));
        bundle.putString( constants.OBD_TimeStamp, sharedPref.getTimeStamp(getApplicationContext()));
        bundle.putString( constants.OBD_RPM, sharedPref.getRPM(getApplicationContext()));
        bundle.putInt( constants.OBD_Vss, sharedPref.getVss(getApplicationContext()));


        try{
            bundle.putString( constants.OBD_HighPrecisionOdometer, sharedPref.getHighPrecisionOdometer(getApplicationContext()) );
        }catch (Exception e){
            e.printStackTrace();
        }
        return bundle;

    }


}
