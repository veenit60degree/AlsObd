package com.als.obd.tools;

import android.content.Context;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.Map;


public class VolleyRequest {
    final String contentType = "application/x-www-form-urlencoded";  //"application/json; charset=utf-8";
    Context context;
    RequestQueue requestQueue;

    public VolleyRequest(Context context) {
        this.context = context;
    }

   /* public void addHeader(String key, String value) {
        header.put(key, value);
    }*/

    public void executeRequest(int method, final String JsonURL, final Map<String, String> params,
                               final int flag, int socketTimeout, final VolleyCallback callback, final VolleyErrorCall ErrorCallback) {

        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(context);
        }


        StringRequest postRequest = new StringRequest(method, JsonURL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                 Log.d("RES", " Response - " + flag + " : " + response);
                callback.getResponse(response, flag );
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("error", " error - " +flag + ": " + error);
                ErrorCallback.getError(error, flag );
            }
        }) {

           /* @Override
            public String getBodyContentType() {
                return contentType;
            }
*/
            @Override
            protected Map<String, String> getParams() {
              //  Log.e("params", " params: " + params);
                return params;
            }

    };

        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        postRequest.setRetryPolicy(policy);
        requestQueue.add(postRequest);

    }


    public interface VolleyCallback {
        public void getResponse(String response, int flag);
    }

    public interface VolleyErrorCall {
        public void getError(VolleyError error, int flag);
    }

}
