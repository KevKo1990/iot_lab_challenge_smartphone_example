package com.iot.lab.cansignalsapp.util;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.iot.lab.cansignalsapp.GraphFragment;

import java.text.DecimalFormat;

/**
 * Used by {@link GraphFragment} to scale the axis.
 */
public class TimeToSecondsFormatter implements IAxisValueFormatter {
    private DecimalFormat mFormat;

    public TimeToSecondsFormatter() {
        // format values to 1 decimal digit
        mFormat = new DecimalFormat("0");
    }

    @Override
    public String getFormattedValue(float value, AxisBase axis) {
        // "value" represents the position of the label on the axis (x or y)
        return mFormat.format(value / Math.pow(10, 9));
    }
}
