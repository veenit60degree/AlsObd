package com.als.obd.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.Html;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.als.obd.R;
import com.als.obd.tools.TcpClient;
import com.ble.utils.ToastUtil;
import com.hjq.toast.ToastUtils;

import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import dal.tables.OBDDeviceData;
import obdDecoder.Decoder;

public class WifiObdActivity extends Activity implements View.OnClickListener {

    public String SERVER_IP = "192.168.225.1";//192.168.225.1
    public static final int SERVER_PORT = 5544;

    TcpClient tcpClient;
    OBDDeviceData data;
    Decoder decoder;
    TextView dataTxtView, obdRawDataTxtView;
    Button gpsBtn, canBtn, restartBtn;
    int clickBtnFlag = 0;
    int GpsFlag = 101;
    int CanFlag = 102;
    int RestartObdFlag = 103;

    String preFix = "*TS01,861107039609723,050743230120,";
    String postFix = "#";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_obd);

        decoder         = new Decoder();
        tcpClient       = new TcpClient(obdResponseHandler);
        dataTxtView     = (TextView)findViewById(R.id.dataTxtView);
        obdRawDataTxtView= (TextView)findViewById(R.id.obdRawDataTxtView);

        gpsBtn          = (Button)findViewById(R.id.gpsBtn);
        canBtn          = (Button)findViewById(R.id.canBtn);
        restartBtn      = (Button)findViewById(R.id.restartBtn);

        obdRawDataTxtView.setOnClickListener(this);
        gpsBtn.setOnClickListener(this);
        canBtn.setOnClickListener(this);
        restartBtn.setOnClickListener(this);

    }


    @Override
    public void onClick(View v) {

        switch (v.getId()){

            case R.id.obdRawDataTxtView:
                if(obdRawDataTxtView.getText().toString().length() > 0) {
                    ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("Data Copied", obdRawDataTxtView.getText().toString());
                    clipboard.setPrimaryClip(clip);
                    ToastUtils.show("Data Copied");
                }
                break;

            case R.id.gpsBtn:
                clickBtnFlag = GpsFlag;

                // get Obd GPS Data
               tcpClient.sendMessage("123456,gps");
                obdRawDataTxtView.setText("");


                break;


            case R.id.canBtn:
                clickBtnFlag = CanFlag;

                tcpClient.sendMessage("123456,can");
                obdRawDataTxtView.setText("");

              /*  String message1 = "*TS01,868323029746804,064019120319,GPS:3;N52.109740;W119.309128;0;354;1.35,STT:C242;0,MGR:462311294,ADC:0;14.43;1;55.19;2;4.04,GFS:0;0,CAN:1600FEE10303602246804D29282D25B8422120357B05190B00F004107D82431500F4820B00F004007D82481500F4820B00FEC1B4E0B310FFFFFFFF,EGT:24124002,EVT:1#";
                String message2 = "*TS01,861641040534124,225428240120,CAN:0B00F004307D83051C00F4830B00FEC1924D2E0EFFFFFFFF0B00FEC1924D2E0EFFFFFFFF0B00FEEE7C3E402DFFFF55FF0B00FEE5A7230500396F13000B00FEE8A00A0000FFFFCF4E0B00FEE9B6C10400005F0D000B00F003DD0008FFFFFF5CFF0B00FEEFB5FFFF36FFFFFFFA0B00FEF6FF035536FF0931FF0B00FEF7FFFFFFFF1A01FFFF0B00FEFCFFDCFFFFFFFFFFFF#";
                  String message3 = "*TS01,861107033601593,051913170919,GPS:3;N49.177049;W122.723174;0;0;1.19,STT:C242;0,MGR:709596933,ADC:0;13.98;1;55.64;2;4.10,CAN:0B00F004407D83C31200F4830B00FEC1CAC39404FFFFFFFF0B00FEE01DE92E001DE92E000B00FEEE6C3B962AFFFF3AFF0B00FEE56AA80100FFFFFFFF0B00FEE8402E0000FFFFA94F0B00FEE9BAED0300BAED03000B00F003D1000AFFFFFF5FFF0B00FEEFA7FFFF49FFFFFFFA0B00FEF6FF004D32FFFFFFFF0B00FEF7FFFFFFFF1901FFFF0B00FEFCFFA4FFFFFFFFFFFF,EGT:19350812,EVT:1#";
                  String message4 = "*TS01,861107034211905,043806261119,CAN:0B00F004607D87DA15000F880B00FEC129EA9303FFFFFFFF0B00FEE097E80700F7A224000B00FEEE4EFF5027FFFFFFFF0B00FEE5693A010097C504000B00FEE8FFFFFFFFFFFF9E4D0B00FEE9CAE70000AE2604000B00F003D10015FFFF4F5E800B00FEEFFFFFFF450F7DFFFA0B00FEF6FF033AFFFFFFFFFF0B00FEF7FFFFFFFF1501FFFF0B00FEFCFFFFFFFFFFFFFFFF#";
                parseObdDeviceData(message3, "OBD data not available");

*/

                break;

            case R.id.restartBtn:
                clickBtnFlag = RestartObdFlag;

                tcpClient.sendMessage("123456,rst");
                obdRawDataTxtView.setText("");

               /* String odbText = "*TS01,861107033601593,051913170919,GPS:3;N49.177049;W122.723174;0;0;1.19,STT:C242;0,MGR:709596933,ADC:0;13.98;1;55.64;2;4.10,CAN:0B00F004407D83C31200F4830B00FEC1CAC39404FFFFFFFF0B00FEE01DE92E001DE92E000B00FEEE6C3B962AFFFF3AFF0B00FEE56AA80100FFFFFFFF0B00FEE8402E0000FFFFA94F0B00FEE9BAED0300BAED03000B00F003D1000AFFFFFF5FFF0B00FEEFA7FFFF49FFFFFFFA0B00FEF6FF004D32FFFFFFFF0B00FEF7FFFFFFFF1901FFFF0B00FEFCFFA4FFFFFFFFFFFF,EGT:19350812,EVT:1#";
                data = decoder.DecodeTextAndSave(odbText, new OBDDeviceData());
                Log.d("response: ", "response: " + data);
*/
                break;



        }
    }




    private String getLocalIpAddress() {
        @SuppressLint("WifiManagerLeak") WifiManager wm = (WifiManager) getSystemService(WIFI_SERVICE);
        String ip = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
        return ip;
    }

    private String getLocalIpPort() {
        @SuppressLint("WifiManagerLeak") WifiManager wm = (WifiManager) getSystemService(WIFI_SERVICE);
        String ip = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
        return ip;
    }


    private class SocketServerThread extends Thread {

        String TAG = "Tag";
        @Override
        public void run() {

            ServerSocket serverSocket;
            Socket socket = null;
            DataInputStream dataInputStream = null;
            DataOutputStream dataOutputStream = null;

            try {
                Log.i(TAG, "Creating server socket");
                serverSocket = new ServerSocket(5544);

                while (true) {
                    socket = serverSocket.accept();
                    dataInputStream = new DataInputStream(
                            socket.getInputStream());
                    dataOutputStream = new DataOutputStream(
                            socket.getOutputStream());

                    String messageFromClient, messageToClient, request;

                    //If no message sent from client, this code will block the program
                    messageFromClient = dataInputStream.readUTF();

                    final JSONObject jsondata;

                }

            } catch (Exception e) {
                e.printStackTrace();
            }  finally {
                if (socket != null) {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                if (dataInputStream != null) {
                    try {
                        dataInputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                if (dataOutputStream != null) {
                    try {
                        dataOutputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

        }

    }



    TcpClient.OnMessageReceived obdResponseHandler = new TcpClient.OnMessageReceived() {
        @Override
        public void messageReceived(String message) {
            Log.d("response", "OBD Respone: " +message);
            String noCanData = "CAN data not available";
            String noObd = "obd not connected";
            dataTxtView.setText(noObd);
            obdRawDataTxtView.setText(message);

            if(!message.equals(noObd) && message.length() > 10){

                if(clickBtnFlag == GpsFlag) {
                    if (message.contains("GPS")) {
                        String[] responseArray = message.split("GPS");
                        if (responseArray.length > 1) {
                            String gpsData = responseArray[1];
                            String[] gpsArray = gpsData.split(";");
                            if (gpsArray.length > 3) {
                                String latitude = gpsArray[1].substring(1, gpsArray[1].length());
                                String longitude = gpsArray[2].substring(1, gpsArray[2].length());
                                String speed = gpsArray[3];

                                String obdGPS = "<b>Latitude:</b> " + latitude + "<br />" +
                                        "<b>Longitude:</b> " + longitude + "<br />" +
                                        "<b>Speed:</b> " + speed;
                                Log.d("obdGPS", "obdGPS: " + obdGPS);
                                dataTxtView.setText(Html.fromHtml(obdGPS));

                                //  SpeakText("OBD " + obdVehicleSpeed);
                                //  global.ShowNotificationWithSound(getApplicationContext(), "OBD", gpsData + "\n" +obdGPS, mNotificationManager);

                            }
                        }
                    }
                }else if(clickBtnFlag == CanFlag){
                    if(message.contains("CAN:UNCONNECTED")){
                        ToastUtils.show(noCanData);
                        dataTxtView.setText(noCanData);

                    }else {

                        parseObdDeviceData(message, noCanData);

                    }


                }
            }else{
                ToastUtils.show(noObd);
            }
            //   textView.setText(message);
        }
    };


    private void parseObdDeviceData(String message, String noCanData){
        try {
            if(message.length() > 5 ){
                String first = message.substring(0, 5);
                String last = message.substring(message.length()-1, message.length());
                if(!first.equals("*TS01") && !last.equals("#")){
                    message = preFix + message + postFix;
                }
            }

            data = decoder.DecodeTextAndSave(message, new OBDDeviceData());
            JSONObject canObj = new JSONObject(data.toString());

            String MileageInMeters = checkJsonParameter(canObj, "MileageInMeters", "0");
            String TripDistanceInKM = checkJsonParameter(canObj, "TripDistanceInKM", "0");
            String HighResolutionDistance = checkJsonParameter(canObj, "HighResolutionTotalVehicleDistanceInKM", "0");
            String DeviceId = checkJsonParameter(canObj, "DeviceId", "");

            String canData =
                    "<b>Mileage In Meters:</b> " + MileageInMeters + "<br />" +
                            "<b>TripDistance In KM:</b> " + TripDistanceInKM + "<br />" +
                            "<b>Total Vehicle Distance:</b> " + HighResolutionDistance + "<br />" +
                            "<b>DeviceId:</b> " + DeviceId  ;

            dataTxtView.setText(Html.fromHtml(canData));

        }catch (Exception e){
            dataTxtView.setText(noCanData);
            e.printStackTrace();
        }

    }


    private String checkJsonParameter(JSONObject canObj, String key, String defaultValue){
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

}
