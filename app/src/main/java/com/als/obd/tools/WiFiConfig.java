package com.als.obd.tools;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class WiFiConfig {


    public WiFiConfig() {
        super();
    }

    public static SharedPreferences preferences, prefs;
    String TAG = "WifiConnection";
    final int SWITCHED_OFF = 1;
    final public static int CONNECT = 101;
    final public static int DISCONNECT = 102;
    final public static String WIFI_STATE = "COMPLETED";
    public static String PREVIOUS_SISD    = "";
    final static String POSITION = "position";  //Home@2151
    final static String AlsWiFiSSID = "OBD_AP";   //Arethos_

    private String SupplicantState = "", networkSSID = "", networkPass = "";
    private WifiManager wifiManager;
    int pos = 0;
    ArrayList<String> SSIS_LIST = new ArrayList<String>();

    public void onReceive(Context c, final Intent intent, final int flag, final WiFiCallback callback) {
        Log.d(TAG, "onReceive() called with: intent = [" + intent + "]");

        wifiManager = (WifiManager) c.getSystemService(Context.WIFI_SERVICE);
        int wifiState = wifiManager.getWifiState();

        SupplicantState = wifiManager.getConnectionInfo().getSupplicantState().toString();

        Log.d("SupplicantState", "--SupplicantState: " + SupplicantState);
        String ssidName = wifiManager.getConnectionInfo().getSSID().toString();
        ssidName = ssidName.replaceAll("\"", "");

        if (flag == CONNECT) {

            if (!SupplicantState.equals(WIFI_STATE) ) {
                if (wifiState == SWITCHED_OFF) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            CheckWifiCredToConnect(intent, callback);
                        }
                    }, 2000);
                } else {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            CheckWifiCredToConnect(intent, callback);
                        }
                    }, 800);
                }
            } else {
                if(ssidName.contains(AlsWiFiSSID)) {
                    String getSupplicantState = wifiManager.getConnectionInfo().getSupplicantState().toString();
                    String SsidName = wifiManager.getConnectionInfo().getSSID().toString();
                    // Log.d(TAG, "getSupplicantState: " + getSupplicantState + "  --SsidName : " + SsidName);
                    callback.getResponse(getSupplicantState, SsidName);
                }else{
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            CheckWifiCredToConnect(intent, callback);
                        }
                    }, 2000);
                }
            }
        } else {
            ForgetWifiConfig(c);
        }


    }

    public String checkJsonParameter(JSONObject canObj, String key, String defaultValue){
        String val = defaultValue;
        try {
            if(canObj.has(key)){
                val = canObj.getString(key);
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        return val;
    }


    private void CheckWifiCredToConnect(Intent intent, WiFiCallback callback) {
        // Connects to a specific wifi network
        final String networkSSID = intent.getStringExtra("ssid");
        final String networkPassword = intent.getStringExtra("password");
        connectToWifi(networkSSID, networkPassword, callback);
    }


    /**
     * Connect to the specified wifi network.
     *
     * @param networkSSID     - The wifi network SSID
     * @param networkPassword - the wifi password
     */
    private void connectToWifi(final String networkSSID, final String networkPassword, final WiFiCallback callback) {

        SupplicantState = wifiManager.getConnectionInfo().getSupplicantState().toString();
        if (!SupplicantState.equals(WIFI_STATE)) {
            if (!wifiManager.isWifiEnabled()) {
                wifiManager.setWifiEnabled(true);
            }

            Log.d("SSID_PASS", "--networkSSID: " + networkSSID + "    networkPassword: " + networkPassword);

            WifiConfiguration conf = new WifiConfiguration();
            conf.SSID = String.format("\"%s\"", networkSSID);
            conf.preSharedKey = String.format("\"%s\"", networkPassword);
            conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK); // This is for a public network which dont have any authentication

            int netId = wifiManager.addNetwork(conf);
            wifiManager.disconnect();
            wifiManager.enableNetwork(netId, true);
            wifiManager.reconnect();

        }

        String getSupplicantState = wifiManager.getConnectionInfo().getSupplicantState().toString();
        String SsidName = wifiManager.getConnectionInfo().getSSID().toString();
        callback.getResponse(getSupplicantState, SsidName);

    }


    /* ------- Remove wifi network. --------- */
    public void ForgetWifiConfig(Context c) {
        WifiManager wifiMgr = (WifiManager) c.getSystemService(Context.WIFI_SERVICE);
        String ssidName = wifiMgr.getConnectionInfo().getSSID().toString();
        ssidName = ssidName.replaceAll("\"", "");

        if (ssidName.contains(AlsWiFiSSID) && wifiMgr.isWifiEnabled()) {
            int netId = wifiMgr.getConnectionInfo().getNetworkId();
            wifiMgr.removeNetwork(netId);
            wifiMgr.saveConfiguration();
        }
    }


    public boolean IsAlsNetworkConnected(Context c) {
        WifiManager wifiMgr = (WifiManager) c.getSystemService(Context.WIFI_SERVICE);
        String ssidName = wifiMgr.getConnectionInfo().getSSID().toString();
        ssidName = ssidName.replaceAll("\"", "");

        boolean IsConnected = IsWifiConnected(c);
        Log.d("IsWifiConnected", "--IsWifi list: " + ssidName);

        if(IsConnected){                            //wifiMgr.isWifiEnabled()
            if (ssidName.contains(AlsWiFiSSID)){
                return true;
            }else{
                return false;
            }
        }else{
            return false;
        }

    }



    private boolean IsWifiConnected(Context c) {
        ConnectivityManager connectionManager = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiCheck=connectionManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return wifiCheck.isConnected();
    }


    public interface WiFiCallback {
        public void getResponse(String response, String SSID_Name);
    }


    // Save Wifi List Position -------------------
    public static void setWifiListPosition( int value, Context context) {
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(POSITION, value);
        editor.commit();
    }

    // Get Wifi List Position -------------------
    public static int getWifiListPosition( Context context) {
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getInt(POSITION, 0);
    }


    public List<WiFiModel> GetSavedSSIDList(){
        List wifiList = new ArrayList<WiFiModel>();
        WiFiModel wifiModel;

        wifiModel = new WiFiModel("", "12345678");
        wifiList.add(wifiModel);

        wifiModel = new WiFiModel("", "23546778");
        wifiList.add(wifiModel);

        wifiModel = new WiFiModel("", "22442349");
        wifiList.add(wifiModel);

        wifiModel = new WiFiModel("", "17625412");
        wifiList.add(wifiModel);


        return wifiList;

    }



    public ArrayList<String> GetOBDDeviceSSIS(Context c){
        ArrayList<String> searchedSSIDArray = new ArrayList<String>();
        String searchedSSID = "";

        WifiManager wifiManager = (WifiManager) c.getSystemService(Context.WIFI_SERVICE);
        if (wifiManager.isWifiEnabled() == false) {
            wifiManager.setWifiEnabled(true);
            wifiManager.startScan();
        }else{
            wifiManager.startScan();
        }

        List<ScanResult> results = wifiManager.getScanResults();
        int size = results.size();

        try {
            size = size - 1;
            while (size >= 0) {
                searchedSSID = results.get(size).SSID ; //+ "  " + results.get(size).capabilities;
                if(searchedSSID.contains(AlsWiFiSSID)){
                    searchedSSIDArray.add(searchedSSID);
                }
                size--;
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return searchedSSIDArray;
    }




    public void CheckWiFiConnection(int position, int WiFiStatus, List<WiFiModel> wifiList, Context context, WiFiCallback ResponseCallBack) {


   /*     String SSID_Name = PREVIOUS_SISD;
        pos = position;
        wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if (wifiManager != null && !wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }

        if (pos == wifiList.size()) {
            pos = 0;
            SSID_Name = GetLastSavedSSID(context, true);
        }else{
            if(SSID_Name.length() == 0){
                SSID_Name = GetLastSavedSSID(context, false);
            }
        }

        if(IsAlsNetworkConnected(context)) {
            for (int i = pos; i < wifiList.size(); i++) {
                networkSSID = SSID_Name; //wifiList.get(i).getNetworkSSID();
                networkPass = wifiList.get(i).getNetworkPass();

                if(networkSSID.length() > 0) {
                    pos++;
                    setWifiListPosition(pos, context);
                    ConnectWiFi(context, WiFiStatus, networkSSID, networkPass, ResponseCallBack);
                }else{

                }
                break;
            }
        }
      */

    }

    String GetLastSavedSSID(Context context, boolean IsRefreshed){
        String SSID_Name = "";
        if(SSIS_LIST.size() == 0 || IsRefreshed) {
            SSIS_LIST = GetOBDDeviceSSIS(context);
        }

        for (int i = 0; i < SSIS_LIST.size(); i++) {
            if(PREVIOUS_SISD.length() > 0) {
                if (PREVIOUS_SISD.equals(SSIS_LIST.get(i))) {
                    if (i == SSIS_LIST.size() - 1) {
                        PREVIOUS_SISD = SSIS_LIST.get(0);
                    } else {
                        PREVIOUS_SISD = SSIS_LIST.get(i + 1);
                    }
                    SSID_Name = PREVIOUS_SISD;
                    break;
                }

            }else{
                PREVIOUS_SISD = SSIS_LIST.get(0);
                SSID_Name = PREVIOUS_SISD;
            }
        }

        return SSID_Name;
    }

    public void ConnectWiFi(Context context, int flag, String networkSSID, String networkPass, WiFiCallback ResponseCallBack){
        Intent broadcast = new Intent();
        broadcast.putExtra("ssid", networkSSID);
        broadcast.putExtra("password", networkPass);
        onReceive(context, broadcast, flag, ResponseCallBack);
    }


    public int GetConnectedSsidPosition(String SSIS, List<WiFiModel> wifiList){
        for (int i = 0; i < wifiList.size(); i++) {
            if(SSIS.equals(wifiList.get(i).getNetworkSSID())){
                return i;
            }
        }
        return 0;
    }



}
