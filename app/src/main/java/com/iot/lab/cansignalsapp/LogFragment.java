package com.iot.lab.cansignalsapp;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import android.widget.TextView;
import android.widget.Toast;

import com.iot.lab.cansignalsapp.models.CANWebsocketClient;
import com.iot.lab.cansignalsapp.util.util;

import java.net.URISyntaxException;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * LogFragment provides a console log for websocket messages.
 * Establishes a socket connection with {@link #connectToWebsocket()} and {@link CANDataClient}
 */
public class LogFragment extends Fragment {

    private static final String TAG = LogFragment.class.getName();

    private CANDataClient mClient;
    private SharedPreferences mSharedPreferences;
    private Thread mDebugThread;
    private boolean mDebugThreadRuns;
    private int mSignalInterval;
    private boolean mShowEmptyWarning = true;

    /**
     * Manages Websocket connection
     * Necessary to open websocket and use incoming messages in this fragment
     */
    public class CANDataClient extends CANWebsocketClient {

        public CANDataClient(String ip, int signalRate, Context context, SharedPreferences sharedPreferences) throws URISyntaxException {
            super(ip, signalRate, context, sharedPreferences);
        }

        @Override
        public void onMessage(String message) {
            HashMap<String, Double> newValues = parseNewValuesOfMessage(message);
            addTableRowOnMainThread(parseToString(newValues));
        }
    }

    public LogFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment.
     *
     * @return A new instance of fragment LogFragment.
     */
    public static LogFragment newInstance() {
        return new LogFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.log_fragment, container, false);
        addTableRow(view, getString(R.string.text_log_header1), getString(R.string.text_log_header2), true);

        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        closeWebsocketConnection();
    }

    private void connectToWebsocket() {
        if (mSharedPreferences.getBoolean("debug", false)) {
            createDebugResponses();
        } else {
            if (mClient == null || mClient.isClosed()) {
                mClient = null;
                try {
                    mClient = new CANDataClient(mSharedPreferences.getString("ip", ""), mSignalInterval, getContext(), mSharedPreferences);
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
                mClient.connect();
            }
        }
    }

    private void closeWebsocketConnection() {
        if (mSharedPreferences.getBoolean("debug", false)) {
            mDebugThreadRuns = false;
        } else {
            if (!mClient.isClosed()) {
                mClient.close();
            }
        }
    }

    public void updateInterval(int interval) {
        if (interval == 0 && mDebugThreadRuns) {
            closeWebsocketConnection();
        } else {
            mSignalInterval = interval;
            connectToWebsocket();
        }

        Log.d(TAG, String.format("Updated interval = %d", mSignalInterval));
    }

    private void addTableRowOnMainThread(final String text) {
        Activity activity = getActivity();
        if (activity != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    View view = getView();
                    if (view != null) {
                        addTableRow(view, new Date().toString(), text, false);
                        Toast.makeText(getContext(), R.string.text_received_web_socket, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void addTableRow(View view, String column1, String column2, boolean headerRow) {
        View tableRow = getLayoutInflater().inflate(R.layout.log_table_row, null, false);
        TextView time = tableRow.findViewById(R.id.column1);
        TextView values = tableRow.findViewById(R.id.column2);
        time.setText(column1);
        values.setText(column2);

        LinearLayout linearLayout;
        if (headerRow) {
            time.setTypeface(null, Typeface.BOLD);
            values.setTypeface(null, Typeface.BOLD);
            linearLayout = view.findViewById(R.id.log_table);
            linearLayout.addView(tableRow, 0);
        } else {
            linearLayout = view.findViewById(R.id.log_table_scroll);
            linearLayout.addView(tableRow);
            if (mShowEmptyWarning) {
                View warning = view.findViewById(R.id.log_empty_warning);
                linearLayout.removeView(warning);
                mShowEmptyWarning = false;
            }
        }
    }

    private String parseToString(HashMap<String, Double> newValues) {
        if (newValues.size() == 0) {
            return getString(R.string.text_no_values_changed);
        }
        List<String> values = new ArrayList<>();
        for (String key : newValues.keySet()) {
            values.add(key + ": " + newValues.get(key));
        }
        return android.text.TextUtils.join(", \n", values);
    }

    private void createDebugResponses() {
        mDebugThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (mDebugThreadRuns) {
                    addTableRowOnMainThread("{values}");
                    try {
                        Thread.sleep(mSignalInterval);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        mDebugThreadRuns = true;
        mDebugThread.start();
    }
}
