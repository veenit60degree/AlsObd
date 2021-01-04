package com.als.obd.tools;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import java.io.File;

public class Constants {



    public Constants() {
        super();
    }

    public static String packageName            = "com.als.obd";


    // OBD parameters
    public static String OBD_Odometer           = "obd_Odometer";
    public static String OBD_HighPrecisionOdometer   = "obd_highPrecisionOdometer";
    public static String OBD_EngineHours        = "obd_EngineHours";
    public static String OBD_IgnitionStatus     = "obd_IgnitionStatus";
    public static String OBD_TripDistance       = "obd_TripDistance";
    public static String OBD_VINNumber          = "obd_VINNumber";
    public static String OBD_TimeStamp          = "obd_TimeStamp";
    public static String OBD_RPM                = "obd_RPM";
    public static String OBD_Vss                = "obd_Vss";






    public boolean isServiceRunning(Context cxt, Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) cxt.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    /*========= Start Service =============*/
    public void startObdService(Context cxt, Class<?> serviceClass){
        Intent serviceIntent = new Intent(cxt, serviceClass);
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            cxt.startForegroundService(serviceIntent);
        }
        cxt.startService(serviceIntent);

    }


    /*========= Stop Service =============*/
    public void stopObdService(Context cxt, Class<?> serviceClass){
        Intent serviceIntent = new Intent(cxt, serviceClass);
        cxt.stopService(serviceIntent);

    }



    public static String GetAppVersion(Context context, String type){
        String AppVersion = "";
        PackageManager manager = context.getPackageManager();
        PackageInfo info = null;

        try {
            info = manager.getPackageInfo(context.getPackageName(), 0);
            if(type.equals("VersionName")) {
                AppVersion = info.versionName;
            }else{
                AppVersion = String.valueOf(info.versionCode);
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return AppVersion;
    }




    public static File getAlsApkPath(){
        File apkStorageDir = new File(Environment.getExternalStorageDirectory(),"Logistic/ObdServer");

        // Create the storage directory if it does not exist
        if (!apkStorageDir.exists()) {
            if (!apkStorageDir.mkdirs()) {
                Log.d("IMAGE_DIRECTORY_NAME", "Oops! Failed create " + "Logistic" + " directory");
                return null;
            }
        }

        return apkStorageDir;
    }


    public String getExistingApkPath(){
        File apkFile = getAlsApkPath();
        String path = "";
        try{
            if(apkFile != null) {
                for (File f : apkFile.listFiles()) {
                    if (f.isFile()) {
                        path = f.getName();
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return path;
    }


    public static void DeleteDirectory(String directory) {
        try {
            // External sdcard location
            File mediaStorageDir = new File(directory);
            // delete the storage directory if it exists
            if (mediaStorageDir.isDirectory()) {
                String[] children = mediaStorageDir.list();
                for (int i = 0; i < children.length; i++) {
                    new File(mediaStorageDir, children[i]).delete();
                }
                //mediaStorageDir.delete();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }




}
