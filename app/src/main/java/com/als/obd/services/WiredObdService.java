package com.als.obd.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.IBinder;
import android.os.Looper;
import android.os.StrictMode;
import android.util.Log;
import android.widget.Toast;

import com.als.obd.DataBase.DBUtils;
import com.als.obd.DataBase.ELD_DATA;
import com.als.obd.DataBase.StaticMessage;
import com.als.obd.R;
import com.als.obd.UILApplication;
import com.als.obd.tools.SharedPref;
import com.android.eldbox_api.Data;
import com.android.eldbox_api.EldboxCallBack;
import com.android.eldbox_api.Response;
import com.ble.utils.ToastUtil;
import com.hjq.toast.ToastUtils;

import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.TimeInfo;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import static com.als.obd.tools.Utils.createLogFile;
import static com.als.obd.tools.Utils.writeToLogFile;

public class WiredObdService extends Service {


    String TAG = "Service";
    String TAG_Msg = "Service msg";
    String packageName = "com.obd.app";
    public static final String ACTION_CONNECT_TIMEOUT = ".ACTION_CONNECT_TIMEOUT";
    public static final String ACTION_CONNECT_ERROR = ".ACTION_CONNECT_ERROR";
    public static final String ACTION_GATT_CONNECTED = ".ACTION_GATT_CONNECTED";
    public static final String ACTION_GATT_DISCONNECTED = ".ACTION_GATT_DISCONNECTED";
    public static final String ACTION_DATA_AVAILABLE = ".ACTION_DATA_AVAILABLE";//Received data
        public static int sTimestamp_inSMB = 0;//save static data by id..

        //Intent
        public static final String sVersionIntent = "Version";
        public static final String sVersionIntentData = "version";
        public static final String sSleepDelayIntent = "SleepDelay";
        public static final String sSleepDelayIntentData = "sleepDelay";

        //Intent
        public static final String sHighPrecisionOdometerIntent     = "HighPrecisionOdometer";
        public static final String sHighPrecisionOdometerIntentData = "highPrecisionOdometer";

        public static final String sResponseIntent = "Response";
        public static final String sResponseIntentType = "responseType";
        public static final String sResponseIntentData = "responseData";
        //Intent
        public static final String sCmdDataIntent = "CmdData";
        public static final String sCmdDataIntentData = "cmdData";



        //other
    private static final String sZero       = "0";
    private static int          preOdometer = 0;
    private String              preVin      = "";
    public static boolean       isConnect   = false;
    long    lastClick            = 0;
    boolean hasSyncTime          = false;//Whether the time has been synchronized
    boolean WhetherSyncTime      = false;//whether the time should be synchronize
    boolean needOpenWifiSettings = false;//Whether to return from Settings

    SharedPref sharedPref;


    @Override
    public void onCreate() {
        super.onCreate();

        Log.i(TAG, "---------onCreate Service");


        StrictMode.ThreadPolicy old = StrictMode.getThreadPolicy();
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder(old)
                .permitDiskWrites()
                .build());
        StrictMode.setThreadPolicy(old);

        sharedPref = new SharedPref();



       openSerialPort();//for serial_ui


        if(!isConnect) {//断开时总里程置0  Odometer set zero when bluetooh disconnected
            preOdometer = 0;
            sharedPref.setOdometer(sZero, getApplicationContext());
        }

        WhetherSyncTime = true;
        SyncTimeFromTablet();

        refreshFile(createLogFile());

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_GATT_CONNECTED);
        intentFilter.addAction(ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(ACTION_DATA_AVAILABLE);
        registerReceiver(receiver, intentFilter);

    }




    private void SyncTimeFromTablet(){
        if(!WhetherSyncTime || hasSyncTime)
            return;

        new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                try {
                    ((UILApplication) getApplication()).serialAgent.requestTimeSync(System.currentTimeMillis());
                    hasSyncTime = true;
                 //   Toast.makeText(getApplicationContext(), getString(R.string.toast_time_sync_success), Toast.LENGTH_SHORT).show();
                }catch (IOException e) {
                    e.printStackTrace();
                  //  Toast.makeText(getApplicationContext(), getString(R.string.toast_time_sync_fail), Toast.LENGTH_SHORT).show();
                }
                Looper.loop();
            }
        }).start();

        WhetherSyncTime = true;

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.i(TAG, "---------onStartCommand Service");

        //SyncTimeWhenConnectNetwork();//hjh 20190921 add for sync time
        SyncTimeFromTablet();

        DBUtils.queryLastOneData(getApplication(), new DBUtils.OnGetLastOneListener() {
            @Override
            public void onGetLastOne(final List<ELD_DATA> list) {
                // TODO: 8/21/2019 从数据库读取总里程  Read odometer from the database
                if (list.size() > 0) {
                    //直接读取1939的总里程 Read the odometer of 1939 directly
                    if(list.get(0).Odometer != 0) {
                        preOdometer = list.get(0).Odometer;
                        sharedPref.setOdometer(preOdometer +"", getApplicationContext());
                    }
                }
            }
        });


        DBUtils.queryLastOneData_inSMB(getApplication(), new DBUtils.OnGetSMBLastOneListener() {
            @Override
            public void onGetLastOne(final List<StaticMessage> list) {
                // TODO: 8/21/2019 如果数据库读取的总里程为空，使用用户输入的总里程。
                // If the odometer read from the database is empty, use the odometer entered by the user.
                if (list.size() > 0) {

                    if(sharedPref.getOdometer(getApplicationContext()).equals("0"))
                        sharedPref.setOdometer(String.valueOf(list.get(0).Odometer_For_OBD), getApplicationContext());

                    sharedPref.setVINNumber(String.valueOf(list.get(0).VIN), getApplicationContext());

                }
            }
        });


        return START_STICKY;
    }



    /**
     * @method
     * @description NTP Synchronization thread
     * @author: HJH
     * @param * @Param null:
     * @return * @return: null
     */
    class NTPThread extends Thread{

        NTPThread() {
        }

        @Override
        public void run() {
            super.run();
            try {
                long waitToSyncTime = getNTPTime();
                ((UILApplication) getApplication()).serialAgent.requestTimeSync(waitToSyncTime);
                Log.w(TAG,"getNTPTime "+waitToSyncTime);
                switch ((int)waitToSyncTime) {
                    case 0:

                        if(getApplicationContext() != null) {
                            ToastUtils.show(R.string.toast_time_sync_fail);
                        }else{
                            Log.d(TAG_Msg, getString(R.string.toast_time_sync_fail));
                        }


                        break;
                    case -1:
                        if(getApplicationContext() != null) {
                            ToastUtils.show(R.string.toast_time_sync_fail);
                        }else{
                            Log.d(TAG_Msg, getString(R.string.toast_time_sync_fail));
                        }
                        break;
                    case -2:
                        if(getApplicationContext() != null) {
                            ToastUtils.show(R.string.toast_time_sync_retry);
                        }else{
                            Log.d(TAG_Msg, getString(R.string.toast_time_sync_retry));
                        }

                        break;
                    case -3:
                        if(getApplicationContext() != null) {
                            ToastUtils.show(R.string.toast_time_sync_fail);
                        }else{
                            Log.d(TAG_Msg, getString(R.string.toast_time_sync_fail));
                        }
                        break;
                    default:
                        if(getApplicationContext() != null) {
                          //  Toast.makeText(getApplicationContext(), getString(R.string.toast_time_sync_success), Toast.LENGTH_SHORT).show();
                            Log.i(TAG,getString(R.string.toast_time_sync_success));
                            hasSyncTime = true;//sync time success
                        }else{
                            Log.d(TAG_Msg, getString(R.string.toast_time_sync_success));
                        }


                        break;
                }
            }catch (IOException e) {
                if(getApplicationContext() != null) {
                   // Toast.makeText(getApplicationContext(), getString(R.string.toast_time_sync_fail), Toast.LENGTH_SHORT).show();
                }else{
                    Log.d(TAG_Msg, getString(R.string.toast_time_sync_fail));
                }
            }
            WhetherSyncTime = true;//thread has died,can create new thread for sync time
        }
        /**
         * @method getNTPTime
         * @description get NTP time
         * @date: 8/31/2019 5:18 PM
         * @author: HJH
         * @param
         * @return * @return: long UTC timeStamp
         */
        private long getNTPTime() {

            String[] hosts = new String[] { "ntp02.oal.ul.pt", "ntp04.oal.ul.pt",
                    "ntp.xs4all.nl", "time.foo.com", "time.nist.gov" };

            NTPUDPClient client = new NTPUDPClient();
            // We want to timeout if a response takes longer than 5 seconds
            client.setDefaultTimeout(2000);
            for (String host : hosts) {
                try {
                    InetAddress hostAddr = InetAddress.getByName(host);
                    TimeInfo timeInfo = client.getTime(hostAddr);
                    return timeInfo.getMessage().getTransmitTimeStamp().getTime();
                }catch (UnknownHostException e1)
                {
                    e1.printStackTrace();
                    client.close();
                    return -1;
                }catch (SocketTimeoutException e2)
                {
                    e2.printStackTrace();
                    client.close();
                    return -2;
                }
                catch (IOException e3) {
                    e3.printStackTrace();
                    client.close();
                    return -3;
                }
            }
            client.close();
            return 0;
        }
    }

    /**
     * @description Broadcast Receive
     * @author: HJH
     */
    BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            switch (Objects.requireNonNull(intent.getAction())) {
                case ACTION_DATA_AVAILABLE: {
                    writeToLogFile(getString(R.string._receive));
                    break;
                }
                case ACTION_GATT_CONNECTED: {
                    writeToLogFile(getString(R.string._connect));
                    break;
                }
                case ACTION_GATT_DISCONNECTED: {
                    writeToLogFile(getString(R.string._disconnect));
                    sharedPref.setIgnitionStatus(getString(R.string.ignition_off), getApplicationContext());
                    sharedPref.setEngineHours( sZero, getApplicationContext());
                    sharedPref.setTripDistance( sZero, getApplicationContext());
                    sharedPref.setRPM(sZero, getApplicationContext());

                }
                break;
            }
        }
    };



    /**
     * @description 回调函数 CallBack
     * @author: HJH
     */
    EldboxCallBack mEldboxCallBack = new EldboxCallBack() {
        /*********    Bluetooth CallBack   *********/
        @Override
        public void onConnected() {
            sendBroadcast(new Intent(ACTION_GATT_CONNECTED));
        }

        @Override
        public void onDisconnected() {
            sendBroadcast(new Intent(ACTION_GATT_DISCONNECTED));
        }

        /*********    ELD Data CallBack   *********/

        /**
         * @method onReceiveVin
         * @description API callback for receive Vin code
         * //The ELD communication protocol uses a Unix timestamp, which is explained as follows
         * //Unix timestamp is the number of seconds that have passed since January 1, 1970 (midnight UTC / GMT), regardless of leap seconds
         * //The vin data is not frequently changed, so it is stored into the Static Message Book on a daily basis.
         *The timestamp used by the ELD communication protocol is a Unix timestamp, explained as follow:
        The Unix timestamp is the number of seconds elapsed since January 1,1970 (the midnight of UTC/GMT),
        regardless of leap seconds.Vin data is not often required to change data, so it is stored in the Static
        Message Book by day
         * @author: HJH
         * @param * @Param timeStamp: 时间戳  timestamp
         * @Param vin: vin code
         * @return * @return: void
         */

        @Override
        public void onReceiveVin(long timeStamp, final String vin){
            //vin 码可以通过J1939和OBD读取到,非实时数据，无需频繁保留。
            //The vin code can be read by J1939 and OBD, non-real time data, without frequent reservation.
            if(vin.equals(preVin))
                return;
            preVin = vin;

            sharedPref.setVINNumber(vin, getApplicationContext());

            DBUtils.insert_VIN_inSMB(getApplication(), vin, sTimestamp_inSMB);
        }



        /**
         * @method onReceiveEngineHours
         * @description API回调 接收引擎启动时间  API callback for receive EngineHours
         * @author: HJH
         * @param * @Param timeStamp: 时间戳 Timestamp
         * @Param time: EngineHours
         * @return * @return: void
         */
        @Override
        public void onReceiveEngineHours(long timeStamp, long time) {
           // final String eS = String.valueOf(time);

            double enginehours = time/(double)20;
            final String eS = String.valueOf(enginehours);

            sharedPref.setEngineHours( eS, getApplicationContext());

            DBUtils.insertEngineHours(getApplication(), (int) time, timeStamp);
        }

        /**
         * @method onReceiveTripDistance
         * @description API回调 接收短里程  API callback for eceive this tripDistance
         * @author: HJH
         * @param * @Param timeStamp: 时间戳 Timestamp
         * @Param tripDistance: 短里程 trip Distance
         * @return * @return: void
         */
        @Override
        public void onReceiveTripDistance(long timeStamp, final int distance) {
            if (-1 == distance) return;

            sharedPref.setTripDistance( String.valueOf(distance), getApplicationContext());
            DBUtils.insertTripDistance(getApplication(), distance, timeStamp);
        }

        /**
         * @method onReceiveRpm
         * @description API回调 接收RPM发动机转速 API callback for receiving RPM engine speed
         * @author: HJH
         * @param * @Param timeStamp: 时间戳 timeStamp
         * @Param rpm: RPM发动机转速 RPM engine speed
         * @return * @return: void
         */
        @Override
        public void onReceiveRpm(long timeStamp, final int rpm) {
            if (-1 == rpm) return;

            sharedPref.setRPM( String.valueOf(rpm), getApplicationContext());
            sharedPref.setIgnitionStatus( rpm == 0?getString(R.string.ignition_off):getString(R.string.ignition_on), getApplicationContext());

            DBUtils.insertRPM(getApplication(), rpm, timeStamp);

        }

        /**
         * @method onReceiveVss
         * @description API回调 接收VSS车速 API callback for eceive VSS speed
         * @author: HJH
         * @param * @Param timeStamp: 时间戳 Timestamp
         * @Param Vss:车速 VSS  vehicle speed
         * @return * @return: void
         */
        @Override
        public void onReceiveVss(final long timeStamp, final int Vss) {
            sharedPref.setVss(Vss, getApplicationContext());
            if (-1 == Vss) return;

            sharedPref.setTimeStamp( new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date(((long) timeStamp) * 1000)) , getApplicationContext());

            DBUtils.insertVSS(getApplication(), Vss, timeStamp);

        }



        /**
         * @method onReceiveVersion
         * @description API回调 接收单片机版本号  API callback for receive the MCU firmware version
         * @author: HJH
         * @param  * @param Version :单片机版本  the MCU firmware version
         * @return * @return: void
         */
        @Override
        public void onReceiveVersion(String Version) {
           /* Intent intent = new Intent(sVersionIntent);
            intent.putExtra(sVersionIntentData, Version);
            sendBroadcast(intent);*/
        }

        /**
         * @method onReceiveSleepDelay
         * @description API回调 接收睡眠延时。如果转速为零，达到延时时间，盒子将会睡眠。软件可以设置时间（分钟计）。
         * API callback for receive sleep delay time. If the vehicle speed is zero and the delay time is reached,
         * the box will sleep.  We can set the delay time (in minutes).
         * @author: HJH
         * @param sleepDelay 睡眠延时 Sleep delay time
         * @return * @return: void
         */
        @Override
        public void onReceiveSleepDelay(int sleepDelay) {
           /* Intent intent = new Intent(sSleepDelayIntent);
            intent.putExtra(sSleepDelayIntentData, sleepDelay);
            sendBroadcast(intent);*/

            ((UILApplication) getApplication()).mAppData.setSleepDelay(sleepDelay);

        }

        /**
         * @method onReceiveOdometer
         * @description API回调 接收总里程 API callback for receive odometer
         * @author: HJH
         * @param timeStamp 时间戳 Timestamp
         * @param odometer 总里程 odometer
         * @return * @return: void
         */
        @Override
        public void onReceiveOdometer(long timeStamp, final int odometer) {
            DBUtils.insertOdometer(getApplication(), odometer, timeStamp);
            sharedPref.setOdometer(String.valueOf(odometer), getApplicationContext());

            if(odometer != 0) {
                preOdometer = odometer;
            }

        }



        @Override
        public void onReceiveDTC(long timeStamp, byte[] dtc) {
            ((UILApplication) getApplication()).mAppData.setDtcList(dtc);
        }




        /**
         * @method onReceiveHighPrecisionOdometer
         * @description API回调 接收高精度总里程（仅J1939） API callback for receive highPrecisionOdometer
         * @author: HJH
         * @param * @Param timeStamp: 时间戳 Timestamp
         * @Param highPrecisionOdometer: 高精度总里程
         * @return * @return: void
         */
        @Override
        public void onReceiveHighPrecisionOdometer(long timeStamp, final long highPrecisionOdometer) {

            try{
                sharedPref.setHighPrecisionOdometer( highPrecisionOdometer, getApplicationContext());
                DBUtils.insertHighPrecisionOdometer(getApplication(), highPrecisionOdometer, timeStamp);

            }catch (Exception e){}

           /* if(highPrecisionOdometer != 0) {
                Intent intent = new Intent(sHighPrecisionOdometerIntent);
                intent.putExtra(sHighPrecisionOdometerIntentData, highPrecisionOdometer);
                sendBroadcast(intent);
            }*/

        }


        /**
         * @method onReceiveResponse
         * @description API回调 接收盒子设备的应答 API callback for receive response from eldbox device
         * @author: HJH
         * @param * @Param responseType:应答类型 response type
         * @Param response:应答数据 response data
         * @return * @return: void
         */
        @Override
        public void onReceiveResponse(int responseType, byte[] response) {
            Intent intent = new Intent(sResponseIntent);
            intent.putExtra(sResponseIntentType,responseType);
            intent.putExtra(sResponseIntentData, response);
            sendBroadcast(intent);

            int reslut = response[0] & 0xff;
            switch (responseType) {
                case Response.TimeSyncResponse:
                    if(reslut == 0)
                    {
                        //设置同步时间失败 Failed to sync time
                    }else{
                        //设置同步时间成功 sync time successfully
                    }
                    break;
                case Response.DataSyncResponse:
                    if(reslut == 0)
                    {
                        //请求同步失败 Request synchronization failed
                        ((UILApplication)getApplication()).isWaitDataSync = false;
                        ToastUtils.show(R.string.toast_request_data_sync_fail);
                    }else if(reslut == 0xAA){
                        //数据同步结束 Data synchronization ends
                        ((UILApplication)getApplication()).isWaitDataSync = false;
                        ToastUtils.show(R.string.toast_data_sync_success);
                    }else {
                        //请求同步成功 Request synchronization successfully
                        ((UILApplication)getApplication()).isWaitDataSync = true;
                        ToastUtils.show(R.string.toast_request_data_sync_success);
                    }
                    break;
                case Response.SleepDelayResponse:
                    if(reslut == 0)
                    {
                        //设置休眠时间失败 Failed to set sleep time
                    }else{
                        //设置休眠时间成功 Set sleep time successfully
                    }
                    break;
                case Response.VerifyResponse:
                    if(reslut == 0)
                    {
                        //接收到的数据校验失败 Received data validation failed
                        ToastUtils.show(R.string.toast_send_to_eld_device_invalid);
                    }else{
                        //接收到的数据校验正确 The received data check is correct
                    }
                    break;
                case Response.WorkModeResponse:
                    if(reslut == 0){
                        //当前工作模式为ELD  Current work mode is Eld mode
                    }else {
                        //当前工作模式为CMD  Current work mode is cmd mode
                    }
                    break;
                default:
                    break;
            }
        }

        /**
         * @method onResponseNeedToSend
         * @description 将response发往设备，response是机器给设备的应答
         * @author: HJH
         * @param * @Param response:
         * @return * @return: void
         */
        @Override
        public void onResponseNeedToSend(byte[] response) {
            try {
                ((UILApplication) getApplication()).serialAgent.writeData(response);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }



        /**
         * @method onReceiveHistoryData
         * @description 同步选定时间段的数据（8*24小时） Synchronize data for the selected time period (8*24 hours)
         * @author: HJH
         * @param data ELD 数据  ELD data
         * @return * @return: void
         */
        @Override
        public void onReceiveHistoryData(Data data) {
            ELD_DATA eld_data = new ELD_DATA();
            eld_data.ENGINE_Hours = data.enginehours;
            eld_data.Trip_Distance = data.tripDistance;
            eld_data.RPM = data.rpm;
            eld_data.TIME_STAMP = data.timeStamp;
            eld_data.Odometer = data.odometer;
            eld_data.VSS = data.vss;
            eld_data.High_Precision_Odometer = data.highPrecisionOdometer;
            DBUtils.insert(getApplication(), eld_data);
            Log.i(TAG,getString(R.string._sync));

        }

        @Override
        public void onReceiveCmdData(byte[] data) {
            Intent intent = new Intent(sCmdDataIntent);
            intent.putExtra(sCmdDataIntentData, new String(data));
            sendBroadcast(intent);
        }



    };




    /**
     * @method refreshFile
     * @description 刷新文件列表，使文件能够在文件管理器等被看到。
     * Refresh the file list so that the file can be seen in the file manager, etc.
     * @author: HJH
     * @param filePath 文件路径 file path
     * @return * @return: void
     */
    private void refreshFile(String filePath) {
        Uri localUri = Uri.fromFile(new File(filePath));
        Intent localIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, localUri);
        sendBroadcast(localIntent);
    }


    private void openSerialPort(){
        if (((UILApplication) getApplication()).serialAgent == null) {
            ((UILApplication) getApplication()).createSerialAgent().setmEldboxCallBack(mEldboxCallBack);
            try {
                ((UILApplication) getApplication()).serialAgent.openSerialPort().startListening();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }



    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onDestroy() {
        Log.i(TAG, "---------onDestroy Service ");

    }




}
