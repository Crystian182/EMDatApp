package com.apollon.emdatapp.Service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

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
import com.google.gson.Gson;

import java.io.UnsupportedEncodingException;

public class GeoService extends Service {

    private String address = "http://api.geonames.org/";
    private String username = "?username=crystian182";
    /*
    http://api.geonames.org/countryInfoJSON?username=crystian182
    http://api.geonames.org/childrenJSON?geonameId=3175395&username=crystian182
    http://api.geonames.org/childrenJSON?geonameId=3174952&username=crystian182
     */

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void getCountries(Context context) {
        try {
            Gson gson = new Gson();
            RequestQueue requestQueue = Volley.newRequestQueue(context);
            String URL = address + "countryInfoJSON" + username;

            StringRequest stringRequest = new StringRequest(Request.Method.GET, URL, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    System.out.println(response);
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

            };

            requestQueue.add(stringRequest);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
