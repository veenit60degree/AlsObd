package com.als.obd.tools;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Utils
{
    public static String LogFilePath = getAlsLogFilePath().toString();  //"sdcard/obd_log.txt";

    public static String createLogFile()
    {
        File file = getAlsLogFilePath(); //new File("sdcard/obd_log.txt");
        if (!file.exists())
        {
            try
            {
                file.createNewFile();
            } catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        return file.getPath();
    }

    public static void writeToLogFile(String value)
    {
        File LogFile = new File(LogFilePath);
        if (!LogFile.exists())
        { //Create if it isn't exist
            try
            {
                LogFile.createNewFile();
            } catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        try
        {
            FileOutputStream fos = new FileOutputStream(LogFile, true);
            value = value + "                       " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(System.currentTimeMillis())) + "\r\n";
            fos.write(value.getBytes());
            fos.close();
        } catch (FileNotFoundException e)
        {
            e.printStackTrace();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }




/*
    public static File getAlsFolderFilePath(){
        File apkStorageDir = new File(Environment.getExternalStorageDirectory(),"Logistic/AlsApp");

        // Create the storage directory if it does not exist
        if (!apkStorageDir.exists()) {
            if (!apkStorageDir.mkdirs()) {
                Log.d("IMAGE_DIRECTORY_NAME", "Oops! Failed create " + "Logistic" + " directory");
                return null;
            }
        }

        return apkStorageDir;
    }
*/

    public static File getAlsFolderPathWithFile(){
        File apkStorageDir = new File(Environment.getExternalStorageDirectory(),"Logistic/AlsApp");

        // Create the storage directory if it does not exist
        if (!apkStorageDir.exists()) {
            if (!apkStorageDir.mkdirs()) {
                Log.d("IMAGE_DIRECTORY_NAME", "Oops! Failed create " + "Logistic" + " directory");
                return null;
            }
        }
        File filePath = new File(apkStorageDir.toString() + "/obd_server_log.txt");
        return filePath;
    }


    public static File getAlsLogFilePath(){
        File apkStorageDir = new File(Environment.getExternalStorageDirectory(),"Logistic/AlsLog");

        // Create the storage directory if it does not exist
        if (!apkStorageDir.exists()) {
            if (!apkStorageDir.mkdirs()) {
                Log.d("IMAGE_DIRECTORY_NAME", "Oops! Failed create " + "Logistic" + " directory");
                return null;
            }
        }
        File filePath = new File(apkStorageDir.toString() + "/obd_server_log.txt");
        return filePath;
    }

}
