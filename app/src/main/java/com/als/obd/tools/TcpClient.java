package com.als.obd.tools;

import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;


public class TcpClient {

    public String SERVER_IP = "192.168.225.1";//192.168.225.1
    public static final int SERVER_PORT = 5544;
    private OnMessageReceived mMessageListener = null;
    String noObd = "obd not connected";

    /**
     *  Constructor of the class. OnMessagedReceived listens for the messages received from server
     */
    public TcpClient(final OnMessageReceived listener)
    {
        mMessageListener = listener;
    }


    public void sendMessage(String message){
        new CommandObd().execute(message);
    }


    public class CommandObd extends AsyncTask<String, String, String>{


        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        protected String doInBackground(String... strings) {

            String response = "";
            String command = strings[0];
            PrintWriter output = null;
            OutputStream out = null;
            SocketAddress address = new InetSocketAddress(SERVER_IP, SERVER_PORT);
            Socket socket = new Socket();
            StringBuilder textBuilder = new StringBuilder();

            try {
                socket.connect(address, 1700);
                socket.setSoTimeout(1500);
            } catch (IOException e) {
                Log.e("socket","socket Invalid connection");
              //  e.printStackTrace();
            }

            try {
                out = socket.getOutputStream();
                output = new PrintWriter(out, true);
                output.print(command);
                output.flush();

            } catch (IOException e) {
                response = noObd;
              //  Log.e("PrintWriter", "PrintWriter object(out) is null" );
            }


            try {
                InputStream inputStream = socket.getInputStream();

                try (Reader reader = new BufferedReader(new InputStreamReader
                        (inputStream, Charset.forName(StandardCharsets.UTF_8.name())))) {

                    int c = 0;
                    while ((c = reader.read()) != -1) {
                        textBuilder.append((char) c);
                    }
                }
                response = textBuilder.toString();
            //    Log.d("test", "trying to read from server: " );
            } catch (IOException e) {
                if(!response.equals(noObd))
                    response = textBuilder.toString();
            }

            try {
                if(output != null)
                    output.close();
            }catch (Exception e){
                e.printStackTrace();
            }
            try {
                if(socket != null)
                    socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

         //   Log.d("tag", "done server");

            return response;
        }


        @Override
        protected void onPostExecute(String response) {
            super.onPostExecute(response);
            mMessageListener.messageReceived(response);

        }
    }

    public interface OnMessageReceived {
        public void messageReceived(String message);
    }


}
