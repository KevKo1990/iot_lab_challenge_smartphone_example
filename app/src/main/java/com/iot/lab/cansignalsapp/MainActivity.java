package com.iot.lab.cansignalsapp;

import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.Toast;

import com.android.volley.ClientError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.iot.lab.cansignalsapp.models.HttpConnectionSingleton;
import com.iot.lab.cansignalsapp.util.util;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Main Activity of a single activity app
 * All fragments communicate via this activity (implemented interfaces).
 * All navigation is done within this activity {@link #onNavigationItemSelected(int)}.
 * Runs a service to check if connection to ALEN is possible {@link #startServerConnectionCheckThread()}.
 */
public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        SensorNameDropdownFragment.OnSensorNameChangeListener,
        IntervalSyncFragment.OnIntervalChangeListener {

    private String TAG_CONNECTION_CHECK = "check_connection";
    private String TAG_HOME = "home";
    private String TAG_REST = "rest";
    private String TAG_SENSOR_NAME = "sensor_name";
    private String TAG_INTERVAL = "interval";
    private String TAG_WEB_SOCKET = "web_socket";

    private String CONFIG_FILE = "config.json";

    private boolean mRunServerConnectionCheckThread = false;
    private boolean mConnectionPossible = false;
    private SharedPreferences mSharedPreferences;

    private int CONNECTION_CHECK_INTERVAL = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        readConfiguration();
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        createNavigationToolbarAndDrawer();
        setAllowedOrientation();
    }

    private void readConfiguration() {
        JSONObject config = util.readJSON(getApplicationContext(), CONFIG_FILE);
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit();
        try {
            editor.putBoolean("debug", config.getBoolean("debug"));
        } catch (JSONException e) {
            e.printStackTrace();
            editor.putBoolean("debug", false);
        }
        try {
            editor.putString("ip", config.getString("ip"));
        } catch (JSONException e) {
            e.printStackTrace();
            editor.putString("ip", "");
        }
        editor.apply();
    }

    private void createNavigationToolbarAndDrawer() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }


    private void setAllowedOrientation() {
        if (getResources().getBoolean(R.bool.portrait_only)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        startServerConnectionCheckThread();
        onNavigationItemSelected(R.id.nav_home);
    }

    @Override
    public void onResume() {
        super.onResume();
        startServerConnectionCheckThread();
    }

    @Override
    public void onPause() {
        super.onPause();
        stopServerConnectionCheckThread();
    }

    private void startServerConnectionCheckThread() {
        Thread queryThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (mRunServerConnectionCheckThread) {
                    RequestForConnectionStatusChange();
                    try {
                        Thread.sleep(CONNECTION_CHECK_INTERVAL);
                    } catch (InterruptedException e) {
                        // Process exception
                        e.printStackTrace();
                    }
                }
            }
        });
        mRunServerConnectionCheckThread = true;
        queryThread.start();
    }

    private void RequestForConnectionStatusChange() {
        if (mSharedPreferences.getBoolean("debug", false)) {
            checkForConnectionStatusChange(true);
        } else {
            StringRequest stringRequest = new StringRequest
                    (Request.Method.GET, HttpConnectionSingleton.getURL(mSharedPreferences.getString("ip", "")), new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            checkForConnectionStatusChange(true);
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            error.printStackTrace();
                            // TODO means 404 dirty fix (they should provide an empty page
                            if (error instanceof ClientError) {
                                checkForConnectionStatusChange(true);
                            } else {
                                checkForConnectionStatusChange(false);
                            }
                        }
                    });
            HttpConnectionSingleton.getInstance(getApplicationContext()).addToRequestQueue(stringRequest, TAG_CONNECTION_CHECK);
        }
    }

    private void checkForConnectionStatusChange(boolean connectionPossible) {
        if (mConnectionPossible != connectionPossible) {
            mConnectionPossible = connectionPossible;
            HomeFragment homeFragment = (HomeFragment) getSupportFragmentManager().findFragmentByTag(TAG_HOME);

            if (homeFragment != null) {
                homeFragment.setConnectionStatus(connectionPossible);
            } else {
                onNavigationItemSelected(R.id.nav_home);
            }
        }
    }

    private void stopServerConnectionCheckThread() {
        if (mRunServerConnectionCheckThread) {
            mRunServerConnectionCheckThread = false;
        }
        HttpConnectionSingleton.getInstance(getApplicationContext()).cancelAllRequests(TAG_CONNECTION_CHECK);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        if (fragment instanceof SensorNameDropdownFragment) {
            SensorNameDropdownFragment sensorNameDropdownFragment = (SensorNameDropdownFragment) fragment;
            sensorNameDropdownFragment.setOnSettingChangeListener(this);
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (!mConnectionPossible) {
            Toast.makeText(getApplicationContext(), R.string.text_connection_problem, Toast.LENGTH_LONG).show();
        }
        return onNavigationItemSelected(id);
    }

    private boolean onNavigationItemSelected(int id) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

        if (id == R.id.nav_home) {
            setHomeScreen(ft);
        }
        if (mConnectionPossible) {
            if (id == R.id.nav_rest) {
                setRESTScreen(ft);
            } else if (id == R.id.nav_websocket) {
                setWebsocketScreen(ft);
            }
        }

        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        ft.commit();

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        return true;
    }

    private void setHomeScreen(FragmentTransaction ft) {
        ft.replace(R.id.fragment_container1, HomeFragment.newInstance(mConnectionPossible), TAG_HOME);
        ft.replace(R.id.fragment_container2, new Fragment());
        ft.replace(R.id.fragment_container3, new Fragment());
        setTitle(R.string.app_name);
    }

    private void setRESTScreen(FragmentTransaction ft) {
        ft.replace(R.id.fragment_container1, SensorNameDropdownFragment.newInstance(), TAG_SENSOR_NAME);
        ft.replace(R.id.fragment_container2, IntervalSyncFragment.newInstance(300, 50, 10000), TAG_INTERVAL);
        ft.replace(R.id.fragment_container3, GraphFragment.newInstance(), TAG_REST);
        setTitle(R.string.REST_screen);
    }

    private void setWebsocketScreen(FragmentTransaction ft) {
        ft.replace(R.id.fragment_container1, IntervalSyncFragment.newInstance(5000, 1000, 10000), TAG_INTERVAL);
        ft.replace(R.id.fragment_container2, LogFragment.newInstance(), TAG_WEB_SOCKET);
        ft.replace(R.id.fragment_container3, new Fragment());
        setTitle(R.string.websocket_screen);
    }

    @Override
    public void OnIntervalChange(int interval) {
        GraphFragment graphFragment = (GraphFragment) getSupportFragmentManager().findFragmentByTag(TAG_REST);
        if (graphFragment != null) {
            graphFragment.updateInterval(interval);
        }

        LogFragment logFragment = (LogFragment) getSupportFragmentManager().findFragmentByTag(TAG_WEB_SOCKET);
        if (logFragment != null) {
            logFragment.updateInterval(interval);
        }
    }

    @Override
    public void OnSensorNameChange(String sensorName) {
        GraphFragment graphFragment = (GraphFragment) getSupportFragmentManager().findFragmentByTag(TAG_REST);
        if (graphFragment != null) {
            graphFragment.updateSensorName(sensorName);
        }
    }

    @Override
    public void OnClickSingleRequest() {
        GraphFragment graphFragment = (GraphFragment) getSupportFragmentManager().findFragmentByTag(TAG_REST);
        if (graphFragment != null) {
            graphFragment.requestSingleLastValue();
        }

        IntervalSyncFragment intervalSyncFragment = (IntervalSyncFragment) getSupportFragmentManager().findFragmentByTag(TAG_INTERVAL);
        if (intervalSyncFragment != null) {
            intervalSyncFragment.stopSync();
        }
    }
}
