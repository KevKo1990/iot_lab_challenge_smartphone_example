package com.iot.lab.cansignalsapp.models;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * HttpConnectionSingleton establishes a single connection to the ALEN box for REST queries.
 * {@link #getInstance(Context)} returns the connection to the server.
 * New request should be added to {@link #addToRequestQueue(Request, String)}.
 */
public class HttpConnectionSingleton {

    private static HttpConnectionSingleton instance;
    private RequestQueue requestQueue;
    private static Context mContext;

    private HttpConnectionSingleton(Context context) {
        mContext = context;
        requestQueue = getRequestQueue();
    }

    public static synchronized HttpConnectionSingleton getInstance(Context context) {
        if (instance == null) {
            instance = new HttpConnectionSingleton(context);
        }
        return instance;
    }

    private RequestQueue getRequestQueue() {
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(mContext.getApplicationContext());
        }
        return requestQueue;
    }

    public static String getURL(String ip) {
        return "http://" + ip;
    }

    public <T> void addToRequestQueue(Request<T> req, String tag) {
        req.setTag(tag);
        getRequestQueue().add(req);
    }

    public void cancelAllRequests(final String tag)
    {
        getRequestQueue().cancelAll(tag);
    }
}
