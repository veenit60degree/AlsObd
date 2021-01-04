package com.als.obd.services;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.als.obd.tools.Constants;
import com.als.obd.tools.SharedPref;
import com.als.obd.tools.TcpClient;
import com.als.obd.tools.WiFiConfig;

import org.json.JSONObject;

import dal.tables.OBDDeviceData;
import obdDecoder.Decoder;

public class WifiObdService extends Service {

    SharedPref sharedPref;
    TcpClient tcpClient;
    OBDDeviceData data;
    Decoder decoder;
    WiFiConfig wifiConfig;

    int obdVehicleSpeed = -1;
    String defaultValue = "-1";


    @Override
    public void onCreate() {
        super.onCreate();

        data                    = new OBDDeviceData();
        decoder                 = new Decoder();
        wifiConfig              = new WiFiConfig();
        tcpClient               = new TcpClient(obdResponseHandler);
        sharedPref              = new SharedPref();

    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        // get Obd odometer Data
        boolean isAlsNetworkConnected = wifiConfig.IsAlsNetworkConnected(getApplicationContext());
        if (isAlsNetworkConnected ) {
            tcpClient.sendMessage("123456,gps");
            tcpClient.sendMessage("123456,can");
        }

        sendMessageToMainService("Connection not occured");

        //Make it stick to the notification panel so it is less prone to get cancelled by the Operating System.
        return START_STICKY;
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }



    TcpClient.OnMessageReceived obdResponseHandler = new TcpClient.OnMessageReceived() {
        @Override
        public void messageReceived(String message) {
             Log.d("response", "OBD Respone: " +message);
            obdVehicleSpeed = -1;
            String noObd = "obd not connected";

            if(!message.equals(noObd) && message.length() > 10){

                if(message.contains("CAN")) {
                    if (message.contains("CAN:UNCONNECTED")) {
                        sharedPref.SetCurrentObdOdometer(defaultValue, getApplicationContext());

                    } else {

                        try {
                            String preFix = "*TS01,861107039609723,050743230120,";
                            String postFix = "#";

                            if(message.length() > 5 ){
                                String first = message.substring(0, 5);
                                String last = message.substring(message.length()-1, message.length());
                                if(!first.equals("*TS01") && !last.equals("#")){
                                    message = preFix + message + postFix;
                                }
                            }

                            data = decoder.DecodeTextAndSave(message, new OBDDeviceData());
                            JSONObject canObj = new JSONObject(data.toString());

                            String HighResolutionDistance = wifiConfig.checkJsonParameter(canObj, "HighResolutionTotalVehicleDistanceInKM", "-1");
                            sharedPref.SetCurrentObdOdometer(HighResolutionDistance, getApplicationContext());

                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                }else{
                    if (message.contains("GPS")) {
                        try{
                            String[] responseArray = message.split("GPS");
                            if (responseArray.length > 1) {
                                String gpsData = responseArray[1];
                                String[] gpsArray = gpsData.split(";");
                                if (gpsArray.length > 3) {
                                    String latitude = gpsArray[1].substring(1, gpsArray[1].length());
                                    String longitude = gpsArray[2].substring(1, gpsArray[2].length());

                                    obdVehicleSpeed = Integer.valueOf(gpsArray[3]);;

                                    // save GPS values in shared pref
                                    sharedPref.SetCurrentGpsLocation(latitude, longitude, obdVehicleSpeed, getApplicationContext());

                                }
                            }else{
                                // save GPS values in shared pref
                                sharedPref.SetCurrentGpsLocation(defaultValue, defaultValue, obdVehicleSpeed, getApplicationContext());

                            }
                        }catch (Exception e){
                            e.printStackTrace();
                        }

                    }
                }
            }
        }
    };


    public void sendMessageToMainService( String msg) {
     /*   Intent intent = new Intent("wifi_obd");
        // You can also include some extra data.
        intent.putExtra("Status", msg);
        Bundle b = new Bundle();
      //  b.putParcelable("Location", l);
        intent.putExtra("data", b);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
*/
        Intent intent = new Intent();
        intent.putExtra("Status", msg);
        intent.setAction(Constants.packageName);
        sendBroadcast(intent);


    }



    @Override
    public void onDestroy() {
        super.onDestroy();
    }


}
