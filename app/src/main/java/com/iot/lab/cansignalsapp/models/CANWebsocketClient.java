package com.iot.lab.cansignalsapp.models;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.iot.lab.cansignalsapp.R;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.HashMap;

/**
 * CANWebsocketClient provides a standard implementation for the used websocket.
 * An example implementation how signals are registered is provided {@link #requestAllSignals()}.
 * An example how to parse incoming messages (only new/changed values) is provided {@link #parseNewValuesOfMessage(String)}.
 */
public abstract class CANWebsocketClient extends WebSocketClient {
    private static String TAG_REQUEST_SIGNALS = "request_signals";

    private boolean WITH_TIMESTAMP = true;

    private String[] mSensorNames;
    private HashMap<String, Double> recentValues = new HashMap<>();
    private Context mContext;
    private int mSignalRate;
    private SharedPreferences mSharedPreferences;

    public CANWebsocketClient(String ip, int signalRate, Context context, SharedPreferences sharedPreferences) throws URISyntaxException {
        super(new URI("ws://" + ip + "/ws"));
        mSignalRate = signalRate;
        mContext = context;
        mSharedPreferences = sharedPreferences;
    }

    @Override
    public void onOpen(ServerHandshake handshakeData) {
        requestAllSignals();
    }

    private void requestAllSignals() {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, SensorName.getURL(mSharedPreferences.getString("ip", "")), null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            mSensorNames = SensorName.parseJsonResponseObjectToNamesStringArray(response);
                            sendRequestToServer();
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(mContext, R.string.text_parsing_error, Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        Toast.makeText(mContext, R.string.text_query_error, Toast.LENGTH_SHORT).show();
                    }
                });
        HttpConnectionSingleton.getInstance(mContext).addToRequestQueue(jsonObjectRequest, TAG_REQUEST_SIGNALS);
    }

    private void sendRequestToServer() {
        JSONObject container = new JSONObject();
        JSONArray signals = new JSONArray();
        try {
            // request all signals
            for (String name : mSensorNames) {
                JSONObject tmpSignal = new JSONObject();
                tmpSignal.put("Name", name);
                signals.put(tmpSignal);
            }
            container.put("signals", signals);
            container.put("samplerate", mSignalRate);
            container.put("withtimestamp", WITH_TIMESTAMP);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        send(container.toString());
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println("closed with exit code " + code + " additional info: " + reason);
        HttpConnectionSingleton.getInstance(mContext).cancelAllRequests(TAG_REQUEST_SIGNALS);
    }


    @Override
    public void onMessage(ByteBuffer message) {
        System.out.println("received ByteBuffer");
    }

    @Override
    public void onError(Exception ex) {
        System.err.println("an error occurred:" + ex);
    }

    /**
     * Takes the websocket message and parses it to return only new values
     * (value changed in comparison to last available value)
     *
     * @param message: String of websocket message
     * @return HashMap: Only new/changed values with sensor names as key and sensor value as values
     */
    protected HashMap<String, Double> parseNewValuesOfMessage(String message) {
        HashMap<String, Double> newValues = new HashMap<>();

        JSONObject samples = null;
        try {
            samples = new JSONObject(message).getJSONObject("samples");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        for (String sensorName : mSensorNames) {
            double value;
            try {
                value = samples.getJSONObject(sensorName).getDouble("value");

                if (!recentValues.containsKey(sensorName)) {
                    newValues.put(sensorName, value);
                }
                if (recentValues.containsKey(sensorName) && recentValues.get(sensorName) != value) {
                    newValues.put(sensorName, value);
                }

                recentValues.put(sensorName, value);
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }
        return newValues;
    }
}
