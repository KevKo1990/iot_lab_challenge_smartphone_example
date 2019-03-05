package com.iot.lab.cansignalsapp.util;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;


/**
 * Various util functions
 */
public class util {
    private static final int WRITE_TO_SD_CARD_CODE = 1337;

    public static void hideSoftKeyboard(View v) {
        InputMethodManager inputMethodManager = (InputMethodManager) v.getContext().getSystemService(
                Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

    public static int createColorFromString(String name) {
        int hash = name.hashCode();
        int[] colors = new int[3];
        for (int i = name.length(); i >= 0; i--) {
            colors[i % 3] *= 10;
            colors[i % 3] += (int) (hash % Math.pow(10, i));
        }
        for (int i = 0; i < colors.length; i++) {
            colors[i] = colors[i] % 256;
        }

        return Color.rgb(colors[0], colors[1], colors[2]);
    }

    public static JSONObject readJSON(Context context, String filename) {
        JSONObject json;
        try {
            InputStream is = context.getAssets().open(filename);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String string = new String(buffer, "UTF-8");
            json = new JSONObject(string);
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

        return json;
    }
}
