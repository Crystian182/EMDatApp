package com.apollon.emdatapp.Service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.apollon.emdatapp.Model.User;
import com.google.gson.Gson;

import java.io.UnsupportedEncodingException;

public class UserService extends Service {

    private final String BACKEND_ADDRESS = "51.68.124.145:8000";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void registerUser(final Context context, User user) {

        postData(context, user, new VolleyCallback() {
            @Override
            public void onSuccessResponse(String result) {
                Intent intent = new Intent("registerUser");
                intent.putExtra("response", result);
                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
            }
        });
    }

    public void postData(Context context, User user, final VolleyCallback callback) {

        try {
            Gson gson = new Gson();
            RequestQueue requestQueue = Volley.newRequestQueue(context);
            String URL = "http://" + BACKEND_ADDRESS + "/persona/register";
            final String requestBody = gson.toJson(user);

            StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    callback.onSuccessResponse(response);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    //Toast.makeText(InfoAnalysisActivity.this, "Errore durante l'invio",
                    //Toast.LENGTH_LONG).show();
                }
            }) {
                @Override
                public String getBodyContentType() {
                    return "application/json; charset=utf-8";
                }

                @Override
                public byte[] getBody() throws AuthFailureError {
                    try {
                        return requestBody == null ? null : requestBody.getBytes("utf-8");
                    } catch (UnsupportedEncodingException uee) {
                        VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", requestBody, "utf-8");
                        return null;
                    }
                }

                @Override
                protected Response<String> parseNetworkResponse(NetworkResponse response) {
                    String responseString = "";
                    if (response != null) {
                        responseString = String.valueOf(response.statusCode);
                        // can get more details such as response.headers
                    }
                    return Response.success(responseString, HttpHeaderParser.parseCacheHeaders(response));
                }
            };

            requestQueue.add(stringRequest);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


}
