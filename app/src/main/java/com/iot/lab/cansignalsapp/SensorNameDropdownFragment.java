package com.iot.lab.cansignalsapp;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.iot.lab.cansignalsapp.models.SensorName;
import com.iot.lab.cansignalsapp.models.HttpConnectionSingleton;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * SensorNameDropdownFragment allows to select specific sensor signals.
 * Sensor selection is propagated with {@link #onItemSelected(AdapterView, View, int, long)}.
 * Provides an interface to monitor changes {@link SensorNameDropdownFragment.OnSensorNameChangeListener}
 */
public class SensorNameDropdownFragment extends Fragment implements AdapterView.OnItemSelectedListener {

    private String TAG_SIGNAL_DROPDOWN = "signal_dropdown";

    private String[] mSensorNames;
    private int mSelectedPosition;
    private SharedPreferences mSharedPreferences;

    private OnSensorNameChangeListener mSensorNameChangeCallback;

    public SensorNameDropdownFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment.
     *
     * @return A new instance of fragment SensorNameDropdownFragment.
     */
    public static SensorNameDropdownFragment newInstance() {
        return new SensorNameDropdownFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.sensor_name_dropdown_fragment, container, false);

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        setOnSingleCallButtonListener(view);
        fillSensorValuesDropdown(view);

        return view;
    }

    private void setOnSingleCallButtonListener(View view) {
        Button singleCallButton = view.findViewById(R.id.button_single_call);
        singleCallButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSensorNameChangeCallback.OnClickSingleRequest();
            }
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnSensorNameChangeListener) {
            mSensorNameChangeCallback = (OnSensorNameChangeListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnIntervalChangeListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        HttpConnectionSingleton.getInstance(this.getContext()).cancelAllRequests(TAG_SIGNAL_DROPDOWN);
        mSensorNameChangeCallback = null;
    }

    private void fillSensorValuesDropdown(final View view) {
        if (mSharedPreferences.getBoolean("debug", false)) {
            mSensorNames = new String[]{"Example Sensor 1", "Example Sensor 2"};
            fillDropdown(view);
        } else {
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                    (Request.Method.GET, SensorName.getURL(mSharedPreferences.getString("ip", "")), null, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                mSensorNames = SensorName.parseJsonResponseObjectToNamesStringArray(response);
                            } catch (JSONException e) {
                                e.printStackTrace();
                                Toast.makeText(getContext(), R.string.text_parsing_error, Toast.LENGTH_SHORT).show();
                                mSensorNames = new String[]{getString(R.string.text_sensor_name_fail)};
                            }
                            fillDropdown(view);
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            error.printStackTrace();
                            Toast.makeText(getContext(), R.string.text_query_error, Toast.LENGTH_SHORT).show();
                            mSensorNames = new String[]{getString(R.string.text_sensor_name_fail)};
                            fillDropdown(view);
                        }
                    });
            HttpConnectionSingleton.getInstance(this.getContext()).addToRequestQueue(jsonObjectRequest, TAG_SIGNAL_DROPDOWN);
        }
    }

    private void fillDropdown(View view) {
        Spinner dropdown = view.findViewById(R.id.spinner_dropdown_sensor_names);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(view.getContext(),
                android.R.layout.simple_spinner_dropdown_item, mSensorNames);
        dropdown.setAdapter(adapter);
        dropdown.setOnItemSelectedListener(this);
        mSelectedPosition = 0;
        mSensorNameChangeCallback.OnSensorNameChange(mSensorNames[mSelectedPosition]);
    }

    public void setOnSettingChangeListener(Activity activity) {
        mSensorNameChangeCallback = (OnSensorNameChangeListener) activity;
    }

    public interface OnSensorNameChangeListener {
        void OnSensorNameChange(String sensorName);

        void OnClickSingleRequest();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (mSelectedPosition != position) {
            mSelectedPosition = position;
            mSensorNameChangeCallback.OnSensorNameChange(mSensorNames[mSelectedPosition]);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // required from interface
    }
}
