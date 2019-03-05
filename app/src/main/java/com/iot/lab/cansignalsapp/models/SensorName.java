package com.iot.lab.cansignalsapp.models;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * SensorName provides a standard object for incoming sensor names.
 * {@link #getAllFromJSONObject(JSONObject)} provides an example parsing from the REST query.
 */
public class SensorName {
    private static String LIST_QUERY = "/list";

    private String name;

    private SensorName(String name) {
        this.name = name;
    }

    public static String getURL(String ip) {
        return HttpConnectionSingleton.getURL(ip) + LIST_QUERY;
    }

    public String getName() {
        return name;
    }

    private static List<SensorName> getAllFromJSONObject(JSONObject jsonObject) throws JSONException {
        List<SensorName> sensorNames = new ArrayList<>();
        JSONArray jsonArraySensorNames = jsonObject.getJSONObject("gateway").getJSONArray("signals");
        int i = 0;
        while (i < jsonArraySensorNames.length()) {
            sensorNames.add(new SensorName(jsonArraySensorNames.getString(i++)));
        }

        return sensorNames;
    }

    public static String[] parseJsonResponseObjectToNamesStringArray(JSONObject response) throws JSONException {
        List<SensorName> sensorNamesList = SensorName.getAllFromJSONObject(response);
        String[] sensorNamesArray = new String[sensorNamesList.size()];
        for (int i = 0; i < sensorNamesList.size(); i++) {
            sensorNamesArray[i] = sensorNamesList.get(i).getName();
        }

        return sensorNamesArray;
    }
}
