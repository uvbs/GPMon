package com.jinsung.adoda.gpmon;

import android.app.ActionBar;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.Legend.LegendForm;
import com.github.mikephil.charting.components.Legend.LegendPosition;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.components.YAxis.AxisDependency;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.filter.Approximator;
import com.github.mikephil.charting.data.filter.Approximator.ApproximatorType;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.highlight.Highlight;

import com.jinsung.adoda.gpmon.data.DailyApiCalls;
import com.jinsung.adoda.gpmon.data.Machine;
import com.jinsung.adoda.gpmon.fortest.TestBase;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;

public class HourlyApiCallsActivity extends TestBase implements OnChartValueSelectedListener {

    private Machine mTargetMachine;
    private HashMap<String, DailyApiCalls> mData;
    private String mApiName;

    protected LineChart mChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_hourly_api_calls);

        Intent intent = getIntent();
        mData = (HashMap<String, DailyApiCalls>)intent.getSerializableExtra("data");
        mTargetMachine = (Machine)intent.getSerializableExtra("targetMachine");
        mApiName = (String)intent.getSerializableExtra("apiName");

        ActionBar actionBar = getActionBar();
        actionBar.setTitle(mTargetMachine.getName());

        TextView apiNameView = (TextView) findViewById(R.id.apiName);
        apiNameView.setText(mApiName);
        apiNameView.setTypeface(Typeface.DEFAULT);

        mChart = (LineChart)findViewById(R.id.chart1);
        mChart.setOnChartValueSelectedListener(this);

        // no description text
        mChart.setDescription("");
        mChart.setNoDataTextDescription("You need to provide data for the chart.");

        // enable touch gestures
        mChart.setTouchEnabled(true);

        mChart.setDragDecelerationFrictionCoef(0.9f);

        // enable scaling and dragging
        mChart.setDragEnabled(true);
        mChart.setScaleEnabled(true);
        mChart.setDrawGridBackground(false);
        mChart.setHighlightPerDragEnabled(true);

        // if disabled, scaling can be done on x- and y-axis separately
        mChart.setPinchZoom(true);

        // set an alternative background color
        mChart.setBackgroundColor(Color.LTGRAY);

        // add data
        setData();

        mChart.animateX(2500);

        // get the legend (only possible after setting data)
        Legend l = mChart.getLegend();

        XAxis xAxis = mChart.getXAxis();
        xAxis.setTypeface(Typeface.DEFAULT);
        xAxis.setTextSize(12f);
        xAxis.setTextColor(Color.WHITE);
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(false);
        xAxis.setSpaceBetweenLabels(1);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.setTypeface(Typeface.DEFAULT);
        leftAxis.setTextColor(ColorTemplate.getHoloBlue());
        leftAxis.setAxisMaxValue(mChart.getYChartMax());
        leftAxis.setDrawGridLines(true);

        YAxis rightAxis = mChart.getAxisRight();
        rightAxis.setEnabled(false);
    }

    @Override
    public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {

    }

    @Override
    public void onNothingSelected() {

    }

    void setData() {

        int count = 20; // for test data
        int range = 30; // for test data

        ArrayList<LineDataSet> dataSets = new ArrayList<LineDataSet>();
        ArrayList<String> xVals = new ArrayList<String>();
        for (int i = 0; i < 24; i++) {
            xVals.add((i) + "시");
        }

        ArrayList<Entry> yVals;

        LineDataSet set;

        DailyApiCalls temp;
        Set<String> keyList = mData.keySet();
        int index = 0;
        for (String key : keyList) {
            yVals = new ArrayList<Entry>();
            temp = mData.get(key);
            for (int i = 0; i < xVals.size(); i++) {
                yVals.add(new Entry(temp.getCount(mApiName, i), i));
            }

            set = new LineDataSet(yVals, key);
            set.setAxisDependency(AxisDependency.LEFT);
            //set.setColor(ColorTemplate.getHoloBlue());
            set.setColor(ColorTemplate.COLORFUL_COLORS[index]);
            set.setCircleColor(Color.WHITE);
            set.setLineWidth(2f);
            set.setCircleSize(3f);
            set.setFillAlpha(65);
            set.setFillColor(ColorTemplate.COLORFUL_COLORS[index]);
            set.setHighLightColor(Color.rgb(244, 117, 117));
            set.setDrawCircleHole(false);

            dataSets.add(set);
            index++;
        }

        // create a data object with the datasets
        LineData data = new LineData(xVals, dataSets);
        data.setValueTextColor(Color.WHITE);
        data.setValueTextSize(9f);

        // set data
        mChart.setData(data);
        index++;
    }
}
