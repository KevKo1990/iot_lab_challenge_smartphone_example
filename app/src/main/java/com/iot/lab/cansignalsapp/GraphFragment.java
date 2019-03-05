package com.iot.lab.cansignalsapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.iot.lab.cansignalsapp.models.SensorValue;
import com.iot.lab.cansignalsapp.models.HttpConnectionSingleton;
import com.iot.lab.cansignalsapp.util.TimeToSecondsFormatter;
import com.iot.lab.cansignalsapp.util.util;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * GraphFragment provides a chart for showing single sensor values.
 * Charts are created with {@link #createGraph(View)}.
 * Data can be requested per single value {@link #requestSingleLastValue()} or {@link #requestLastValue(String)}
 * Data can be requested continuously {@link #startContinuousLastValueQuery()}.
 */
public class GraphFragment extends Fragment {

    private String TAG_LAST_VALUE = "last_value";

    private String mSelectedSensor = "";
    private boolean mActiveRunnable;
    private int mQueryInterval;
    private LineDataSet mDataSet;
    private LineData mLineData;
    private long minXValue;
    private SharedPreferences mSharedPreferences;

    public GraphFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment.
     *
     * @return A new instance of fragment GraphFragment.
     */
    public static GraphFragment newInstance() {
        return new GraphFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.graph_fragment, container, false);

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());

        createGraph(view);

        return view;
    }

    private void createGraph(View view) {
        LineChart chart = view.findViewById(R.id.chart);
        Description description = new Description();
        description.setText("Value over time");
        chart.setDescription(description);
        XAxis xAxis = chart.getXAxis();
        if (!mSharedPreferences.getBoolean("debug", true)) {
            xAxis.setValueFormatter(new TimeToSecondsFormatter());
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        stopContinuousLastValueQuery();
        HttpConnectionSingleton.getInstance(getContext()).cancelAllRequests(TAG_LAST_VALUE);
    }

    private void startContinuousLastValueQuery() {
        if (!mActiveRunnable) {
            Thread queryThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (mActiveRunnable) {
                        requestLastValue(mSelectedSensor);
                        try {
                            Thread.sleep(mQueryInterval);
                        } catch (InterruptedException e) {
                            // Process exception
                        }
                    }
                }
            });
            mActiveRunnable = true;
            queryThread.start();
        }
    }

    private void requestLastValue(final String sensorName) {
        if (mSharedPreferences.getBoolean("debug", false)) {
            appendDataToGraph(SensorValue.generateRandomSensorValue(sensorName));
        } else {
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                    (Request.Method.GET, SensorValue.getURLOnlyValue(mSharedPreferences.getString("ip", ""), sensorName), null, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            SensorValue sensorValue;
                            try {
                                sensorValue = SensorValue.parseValueOnlyJsonResponseObject(sensorName, response);
                                appendDataToGraph(sensorValue);
                            } catch (JSONException e) {
                                e.printStackTrace();
                                Toast.makeText(getContext(), R.string.text_parsing_error, Toast.LENGTH_SHORT).show();
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            error.printStackTrace();
                            Toast.makeText(getContext(), R.string.text_query_error, Toast.LENGTH_SHORT).show();
                        }
                    });

            HttpConnectionSingleton.getInstance(this.getContext()).addToRequestQueue(jsonObjectRequest, TAG_LAST_VALUE);
        }
    }

    public void requestSingleLastValue() {
        stopContinuousLastValueQuery();
        requestLastValue(mSelectedSensor);
    }

    private void stopContinuousLastValueQuery() {
        if (mActiveRunnable) {
            mActiveRunnable = false;
        }
    }

    public void updateSensorName(String sensorName) {
        if (!mSelectedSensor.equals(sensorName)) {
            mSelectedSensor = sensorName;
        }
    }

    public void updateInterval(int interval) {
        if (interval == 0) {
            stopContinuousLastValueQuery();
        } else {
            mQueryInterval = interval;
            startContinuousLastValueQuery();
        }
    }

    private void appendDataToGraph(SensorValue sensorValue) {
        View view = this.getView();
        if (view != null) {
            LineChart chart = view.findViewById(R.id.chart);
            if (!selectedSensorEqualToGraphSensor()) {
                List<Entry> entries = new ArrayList<>();
                minXValue = sensorValue.getTimestamp();
                entries.add(sensorValue.transformToEntry(minXValue));
                mDataSet = new LineDataSet(entries, mSelectedSensor);

                int color = util.createColorFromString(mSelectedSensor);
                mDataSet.setColor(color);
                mDataSet.setCircleColor(color);

                mLineData = new LineData(mDataSet);
                chart.setData(mLineData);
            } else {
                if (!mDataSet.contains(sensorValue.transformToEntry(minXValue))) {
                    mDataSet.addEntry(sensorValue.transformToEntry(minXValue));
                    mLineData.notifyDataChanged();
                }
            }
            chart.notifyDataSetChanged();
            chart.invalidate();
        }
    }

    private boolean selectedSensorEqualToGraphSensor() {
        return mDataSet != null && mSelectedSensor.equals(mDataSet.getLabel());
    }
}
