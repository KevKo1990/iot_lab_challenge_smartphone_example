package com.iot.lab.cansignalsapp.models;

import com.github.mikephil.charting.data.Entry;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Random;

/**
 * A standard class for incoming sensor names.
 * {@link #parseValueOnlyJsonResponseObject(String, JSONObject)} and #parseCompleteValueJsonResponseObject(String, JSONObject)}
 * provide examples parsing from the REST query.
 * {@link #transformToEntry(long)} parses sensor values to entry values for {@link com.iot.lab.cansignalsapp.GraphFragment}.
 */
public class SensorValue {
    private static String SIGNAL_VALUE_COMPLETE_QUERY = "/signal/%s";
    private static String SIGNAL_VALUE_ONLY_QUERY = SIGNAL_VALUE_COMPLETE_QUERY + "/value";

    private String name;
    private double value;
    private long timestamp;

    private SensorValue(String signalName, JSONObject json) throws JSONException {
        this.name = signalName;
        this.value = json.getJSONObject("measurement").getDouble("value");
        this.timestamp = json.getJSONObject("measurement").getLong("utc");
    }

    private SensorValue(String name, double value, long timestamp) {
        this.name = name;
        this.value = value;
        this.timestamp = timestamp;
    }

    public String getName() {
        return name;
    }

    public double getValue() {
        return value;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public static String getURLCompleteValue(String ip, String signalName) {
        return HttpConnectionSingleton.getURL(ip) + String.format(SIGNAL_VALUE_COMPLETE_QUERY, signalName);
    }

    public static String getURLOnlyValue(String ip, String signalName) {
        return HttpConnectionSingleton.getURL(ip) + String.format(SIGNAL_VALUE_ONLY_QUERY, signalName);
    }

    public static SensorValue parseCompleteValueJsonResponseObject(String signalName, JSONObject response) throws JSONException {
        JSONObject jsonObject = response.getJSONObject("signal");
        return new SensorValue(signalName, jsonObject);
    }

    public static SensorValue parseValueOnlyJsonResponseObject(String signalName, JSONObject response) throws JSONException {
        return new SensorValue(signalName, response);
    }

    public Entry transformToEntry(long minTime) {
        return new Entry(this.getTimestamp() - minTime, (float) this.getValue());
    }

    private static Random mRand = new Random();
    private static long number = 0;

    public static SensorValue generateRandomSensorValue(String sensorName) {
        long x = number++;
        double y = mRand.nextDouble();
        return new SensorValue(sensorName, y, x);
    }
}
