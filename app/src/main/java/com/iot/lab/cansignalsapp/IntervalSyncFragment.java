package com.iot.lab.cansignalsapp;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.iot.lab.cansignalsapp.util.util;

import java.util.Locale;

/**
 * IntervalSyncFragment provides controls to establish a continuous data stream.
 * Sync start with {@link #startSync()}.
 * Sync stop with {@link #stopSync()}.
 * Provides an interface to monitor changes {@link IntervalSyncFragment.OnIntervalChangeListener}
 */
public class IntervalSyncFragment extends Fragment {
    private static final String ARG_MIN_INTERVAL = "min_interval";
    private static final String ARG_MAX_INTERVAL = "max_interval";
    private static final String ARG_DEFAULT_INTERVAL = "default_interval";

    private int mMinInterval;
    private int mMaxInterval;

    private int mSelectedInterval;
    private boolean mSyncStarted = false;

    private OnIntervalChangeListener mIntervalChangeCallback;

    public IntervalSyncFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param defaultInterval Starting interval for text field.
     * @param minInterval     Minimum interval for requests.
     * @param maxInterval     Maximum interval for requests.
     * @return A new instance of fragment IntervalSyncFragment.
     */
    public static IntervalSyncFragment newInstance(int defaultInterval, int minInterval, int maxInterval) {
        IntervalSyncFragment fragment = new IntervalSyncFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_DEFAULT_INTERVAL, defaultInterval);
        args.putInt(ARG_MIN_INTERVAL, minInterval);
        args.putInt(ARG_MAX_INTERVAL, maxInterval);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mSelectedInterval = getArguments().getInt(ARG_DEFAULT_INTERVAL);
            mMinInterval = getArguments().getInt(ARG_MIN_INTERVAL);
            mMaxInterval = getArguments().getInt(ARG_MAX_INTERVAL);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.interval_sync_fragment, container, false);
        setOnSyncPlayButtonListener(view);
        setOnSyncStopButtonListener(view);
        setOnIntervalChangeListener(view);
        return view;
    }

    private void setOnSyncPlayButtonListener(View view) {
        ImageButton syncPlayButton = view.findViewById(R.id.button_sync_start);
        syncPlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startSync();
            }
        });
    }

    private void setOnSyncStopButtonListener(final View view) {
        ImageButton syncStopButton = view.findViewById(R.id.button_sync_stop);
        syncStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopSync();
            }
        });
        syncStopButton.setClickable(false);
    }

    private void setOnIntervalChangeListener(View view) {
        EditText editTextInterval = view.findViewById(R.id.text_sensor_interval);
        editTextInterval.setText(String.valueOf(mSelectedInterval));

        editTextInterval.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().equals("")) {
                    showHintsIntervalSize(true);
                    return;
                }
                int value = Integer.parseInt(s.toString());
                if (value >= mMinInterval && value <= mMaxInterval) {
                    mSelectedInterval = value;
                    showHintsIntervalSize(false);
                } else {
                    showHintsIntervalSize(true);
                }
            }
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnIntervalChangeListener) {
            mIntervalChangeCallback = (OnIntervalChangeListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnIntervalChangeListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mIntervalChangeCallback = null;
    }

    private void startSync() {
        Activity activity = this.getActivity();
        if (activity == null) {
            return;
        }

        if (!mSyncStarted) {
            mIntervalChangeCallback.OnIntervalChange(mSelectedInterval);

            // UI changes
            ImageButton syncPlayButton = activity.findViewById(R.id.button_sync_start);
            syncPlayButton.setImageResource(R.drawable.ic_pause);
            syncPlayButton.setBackgroundColor(getResources().getColor(R.color.colorPrimaryUnfocused));
            syncPlayButton.setClickable(false);
            Snackbar.make(syncPlayButton, getString(R.string.start_sync), Snackbar.LENGTH_SHORT).show();

            ImageButton syncStopButton = activity.findViewById(R.id.button_sync_stop);
            syncStopButton.setBackgroundColor(getResources().getColor(R.color.colorAccent));
            syncStopButton.setClickable(true);

            mSyncStarted = true;
            util.hideSoftKeyboard(syncPlayButton);
        }
    }

    public void stopSync() {
        Activity activity = this.getActivity();
        if (activity == null) {
            return;
        }

        if (mSyncStarted) {
            mIntervalChangeCallback.OnIntervalChange(0);

            ImageButton syncPlayButton = activity.findViewById(R.id.button_sync_start);
            Snackbar.make(syncPlayButton, getString(R.string.stop_sync), Snackbar.LENGTH_SHORT).show();
            if (mSyncStarted) {
                syncPlayButton.setImageResource(R.drawable.ic_play);
                syncPlayButton.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                syncPlayButton.setClickable(true);

                ImageButton syncStopButton = activity.findViewById(R.id.button_sync_stop);
                syncStopButton.setBackgroundColor(getResources().getColor(R.color.colorAccentUnfocused));
                syncStopButton.setClickable(false);
                mSyncStarted = false;
            }

            util.hideSoftKeyboard(syncPlayButton);
        }
    }

    private void showHintsIntervalSize(boolean show) {
        Activity activity = this.getActivity();
        if (activity == null) {
            return;
        }

        ImageButton syncPlayButton = activity.findViewById(R.id.button_sync_start);
        EditText editTextInterval = activity.findViewById(R.id.text_sensor_interval);
        syncPlayButton.setClickable(!show);
        if (show) {
            syncPlayButton.setBackgroundColor(getResources().getColor(R.color.colorPrimaryUnfocused));
            editTextInterval.setTextColor(Color.RED);
            String toastText =
                    String.format(Locale.US, "Only use interval values between %d and %d ms",
                            mMinInterval, mMaxInterval);
            Toast.makeText(activity, toastText, Toast.LENGTH_SHORT).show();
        } else {
            syncPlayButton.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            editTextInterval.setTextColor(Color.BLACK);
        }
    }

    public interface OnIntervalChangeListener {
        void OnIntervalChange(int interval);
    }
}
