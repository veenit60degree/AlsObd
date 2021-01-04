package com.als.obd.tools;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

public class DownloadAppService extends Service {

    private String TAG = DownloadAppService.class.getSimpleName();
    private String VersionCode = "", VersionName = "";
    boolean isDownloading ;
    DownloadingTask downloadTaskAsync = new DownloadingTask();

    @Override
    public void onCreate() {
       // downloadTaskAsync = new DownloadingTask();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        String url = " ";

        if (intent != null) {
            url             = intent.getStringExtra("url");
            VersionCode     = intent.getStringExtra("VersionCode");
            VersionName     = intent.getStringExtra("VersionName");
            isDownloading   = intent.getBooleanExtra("isDownloading", false);
        }

        String[] params = new String[]{url};

        if(!isDownloading){
            SharedPref.setAsyncCancelStatus(false, getApplicationContext());
            downloadTaskAsync = new DownloadingTask();
            downloadTaskAsync.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);
        }

        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {

        if(downloadTaskAsync != null) {
            downloadTaskAsync.cancel(true);
            downloadTaskAsync = null;
        }
        SharedPref.setAsyncCancelStatus(true, getApplicationContext());

        Log.d("AsyncTask", "onDestroy");
       // Toast.makeText(this, "Download Completed.", Toast.LENGTH_SHORT).show();
    }



    class DownloadingTask extends AsyncTask<String, String, String> {
        private String fileName;
        private String folder;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            try{
                folder      = Constants.getAlsApkPath().toString();
                fileName    = "/OBD_"+ VersionCode + "_" + VersionName + ".apk";
                Constants.DeleteDirectory(folder);
            }catch (Exception e){
                e.printStackTrace();
            }

        }

        @Override
        protected String doInBackground(String... urls) {

            Log.d(TAG, "doInBackground: " + urls[0]);

            int count;
            try {
                URL url = new URL(urls[0]);
                URLConnection connection = url.openConnection();
                connection.connect();
                // getting file length
                int lengthOfFile = connection.getContentLength();


                // input stream to read file - with 8k buffer
                InputStream input = new BufferedInputStream(url.openStream(), 8192);

                //Extract file name from URL
              //  fileName = urls[0].substring(urls[0].lastIndexOf('/') + 1, urls[0].length());


                //External directory path to save file      (folder = Environment.getExternalStorageDirectory() + File.separator + "ALS/";)
                // Output stream to write file
                OutputStream output = new FileOutputStream(folder + fileName);

                byte data[] = new byte[1024];

                long total = 0;

                while ((count = input.read(data)) != -1) {
                    total += count;
                    // publishing the progress....
                    // After this onProgressUpdate will be called
                    publishProgress("" + (int) ((total * 100) / lengthOfFile));
                    Log.d(TAG, "Progress: " + (int) ((total * 100) / lengthOfFile));

                    // writing data to file
                    output.write(data, 0, count);

                    // If cancel request to stop download
                    if(SharedPref.getAsyncCancelStatus(getApplicationContext())) {
                        cancel(true);
                        DeleteFile(folder + fileName);
                        break;
                    }

                }

                // flushing output
                output.flush();

                // closing streams
                output.close();
                input.close();
                return folder + fileName;

            } catch (Exception e) {
                Log.e("Error: ", e.getMessage());
            }


            return "Downloading failed.";

        }


        @Override
        protected void onProgressUpdate(String... progress) {
            super.onProgressUpdate(progress);

            int Progress = Integer.parseInt(progress[0]);
            Intent intent = new Intent("download_progress");
            intent.putExtra("percentage", Progress);
            intent.putExtra("path", "");
            intent.putExtra("isCompleted", false);
            LocalBroadcastManager.getInstance(DownloadAppService.this).sendBroadcast(intent);

        }


        @Override
        protected void onPostExecute(String result) {
            Log.d(TAG, "onPostExecute");
            stopSelf();

            if (result.equals("Downloading failed.")) {
                DeleteFile(folder + fileName);
            }

            // Display File path after downloading
            Intent intent = new Intent("download_progress");
            intent.putExtra("percentage", 100);
            intent.putExtra("path", result);
            intent.putExtra("isCompleted", true);
            LocalBroadcastManager.getInstance(DownloadAppService.this).sendBroadcast(intent);



        }
    }


    void DeleteFile(String filePath){
        try {
                File file = new File(filePath);
                if (file.isFile()) {
                    file.delete();
                }
        }catch (Exception e){
            e.printStackTrace();
        }
    }


}


