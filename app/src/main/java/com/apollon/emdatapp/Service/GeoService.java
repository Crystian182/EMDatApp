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
import com.apollon.emdatapp.Model.Country;
import com.google.gson.Gson;
import com.google.gson.JsonParser;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

public class GeoService extends Service {

    private String address = "http://api.geonames.org/";
    private String username = "username=crystian182";
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

    public void getCountries(final Context context) {

        fetchData(context, "countryInfoJSON?", new VolleyCallback() {
                    @Override
                    public void onSuccessResponse(String result) {
                        Intent intent = new Intent("countryUpdates");
                        intent.putExtra("type", "countries");
                        intent.putExtra("countries", result);
                        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                    }
                });
    }

    public void getChildren1(final Context context, int idChildren) {

        fetchData(context, "childrenJSON?geonameId=" + idChildren + "&", new VolleyCallback() {
            @Override
            public void onSuccessResponse(String result) {
                Intent intent = new Intent("countryUpdates");
                intent.putExtra("type", "children1");
                intent.putExtra("children1", result);
                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                System.out.println(result);
            }
        });
    }

    public void getChildren2(final Context context, int idChildren) {

        fetchData(context, "childrenJSON?geonameId=" + idChildren + "&", new VolleyCallback() {
            @Override
            public void onSuccessResponse(String result) {
                Intent intent = new Intent("countryUpdates");
                intent.putExtra("type", "children2");
                intent.putExtra("children2", result);
                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                System.out.println(result);
            }
        });
    }

    public void getChildren3(final Context context, int idChildren) {

        fetchData(context, "childrenJSON?geonameId=" + idChildren + "&", new VolleyCallback() {
            @Override
            public void onSuccessResponse(String result) {
                Intent intent = new Intent("countryUpdates");
                intent.putExtra("type", "children3");
                intent.putExtra("children3", result);
                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                System.out.println(result);
            }
        });
    }

    public void fetchData(Context context, String middleUrl, final VolleyCallback callback) {
        try {

            RequestQueue requestQueue = Volley.newRequestQueue(context);
            String URL = address + middleUrl + username;

            StringRequest stringRequest = new StringRequest(Request.Method.GET, URL, new Response.Listener<String>() {
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

            };

            requestQueue.add(stringRequest);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
