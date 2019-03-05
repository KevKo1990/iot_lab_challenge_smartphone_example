package com.iot.lab.cansignalsapp;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.media.session.PlaybackStateCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * HomeFragment shows general information and if the app can connect to ALEN.
 */
public class HomeFragment extends Fragment {

    private static String PARAM1_CONNECTION_POSSIBLE = "connection_possible";

    private boolean mConnectionPossible;

    public HomeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param connectionPossible Boolean if ALEN box is reachable.
     * @return A new instance of fragment HomeFragment.
     */
    public static HomeFragment newInstance(boolean connectionPossible) {
        HomeFragment homeFragment = new HomeFragment();

        Bundle args = new Bundle();
        args.putBoolean(PARAM1_CONNECTION_POSSIBLE, connectionPossible);
        homeFragment.setArguments(args);

        return homeFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.home_fragment, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        boolean connectionPossible = getArguments().getBoolean(PARAM1_CONNECTION_POSSIBLE, false);
        setConnectionStatus(connectionPossible);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    public void setConnectionStatus(boolean connectionPossible) {
        View view = this.getView();
        if (view != null && mConnectionPossible != connectionPossible) {
            mConnectionPossible = connectionPossible;

            LinearLayout linearLayout = view.findViewById(R.id.connection_status_box);
            linearLayout.setBackgroundResource(connectionPossible ? R.drawable.border_green : R.drawable.border_red);
            TextView textConnectionStatus = view.findViewById(connectionPossible ? R.id.text_connection_status : R.id.text_connection_status);
            textConnectionStatus.setText(connectionPossible ? R.string.text_connected : R.string.text_not_connected);

            ImageView imageConnectionStatus = view.findViewById(R.id.image_connection_status);
            imageConnectionStatus.setImageResource(connectionPossible ? R.drawable.ic_check_green : R.drawable.ic_error_red);
        }
    }
}
