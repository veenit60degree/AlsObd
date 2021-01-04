package com.als.obd.tools;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.hjq.toast.ToastUtils;


public class SharedPref {


    static String CURRENT_DATE                        = "currentDate";
    static String DRIVER_TYPE                	  	  = "driver_type";
    static String CURRENT_DRIVER_TYPE				  = "current_driver_type";

    public static String CurrentLat                   = "current_lat";
    public static String CurrentLon                   = "current_lon";
    public static String CurrentSpeed                 = "current_speed";


    public SharedPref() {
        super();
    }





    // Save VIN Number
    public static void setEngineHours(String VIN, Context context){

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("EngineHours", VIN);
        editor.commit();

    }


    // Get VIN Number -------------------
    public static String getEngineHours( Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString("EngineHours", "--");
    }



    // Set UTC Time Zone -------------------
    public static void setIgnitionStatus(String value, Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("ignition", value);
        editor.commit();
    }

    // Get UTC Time Zone -------------------
    public static String getIgnitionStatus(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString("ignition", "--");
    }




    // Set trip Distance -------------------
    public static void setTripDistance( String value, Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("TripDistance", value);
        editor.commit();
    }

    // Get trip Distance -------------------
    public static String getTripDistance(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString("TripDistance", "0");
    }




    // Save VIN Number
    public static void setVINNumber(String VIN, Context context){

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("VIN", VIN);
        editor.commit();

    }


    // Get VIN Number -------------------
    public static String getVINNumber( Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString("VIN", "--");
    }



    public static void setRPM( String value, Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("rpm", value);
        editor.commit();
    }

    // Get Trailor Number -------------------
    public static String getRPM(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString("rpm", "0");
    }





    public static void setVss( int value, Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("vss", value);
        editor.commit();
    }

    // Get Trailor Number -------------------
    public static int getVss(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getInt("vss", -1);
    }




    // Set Current Saved Time -------------------
    public static void setTimeStamp( String value, Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("TimeStamp", value);
        editor.commit();
    }
    // Get Current Saved Time -------------------
    public static String getTimeStamp(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString("TimeStamp", "--");
    }


    // Set Odometer value -------------------
    public static void setOdometer( String value, Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("Odometer", value);
        editor.commit();
    }

    // Get Odometer value -------------------
    public static String getOdometer(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString("Odometer", "0");
    }




    // Set High Precision Odometer value  -------------------
    public static void setHighPrecisionOdometer( long value, Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("highPrecisionOdometer", String.valueOf(value));
        editor.commit();
    }

    // Get High Precision Odometer value -------------------
    public static String getHighPrecisionOdometer(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString("highPrecisionOdometer", "0");
    }





    // Set Current Obd Odometer -------------------
    public static void SetCurrentObdOdometer( String value, Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("current_obd", value);
        editor.commit();
    }

    // Get Current Obd Odometer -------------------
    public static int GetCurrentObdOdometer(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getInt("current_obd", -1);
    }




    // Set Current Obd Odometer -------------------
    public static void SetCurrentGpsLocation( String valueLat, String valueLong, int speed,Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(CurrentLat, valueLat);
        editor.putString(CurrentLon, valueLong);
        editor.putInt(CurrentSpeed, speed);
        editor.commit();
    }


    // Get Current Obd Odometer -------------------
    public static int GetCurrentGpsValue(String value, Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getInt(value, -1);
    }



    // Set AsyncTask Status -------------------
    public static void setAsyncCancelStatus(boolean value, Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("AsyncTaskStatus", value);
        editor.commit();
    }


    // Get AsyncTask Status -------------------
    public static boolean getAsyncCancelStatus(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getBoolean("AsyncTaskStatus", false);
    }



/*
    // =========================== Set Offline Data Status ===========================
    public void SetOfflineData(boolean value, Context context) {
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("offlineData", value);
        editor.commit();
    }

    // =========================== Get Offline Data Status ===========================
    public boolean GetOfflineData(Context context) {
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getBoolean("offlineData", false);
    }

*/


}
