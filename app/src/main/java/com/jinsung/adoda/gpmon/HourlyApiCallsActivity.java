package com.jinsung.adoda.gpmon;

import android.app.ActionBar;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.components.YAxis.AxisDependency;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.jinsung.adoda.gpmon.data.DailyApiCalls;
import com.jinsung.adoda.gpmon.data.DataContainer;
import com.jinsung.adoda.gpmon.data.Machine;
import com.jinsung.adoda.gpmon.fortest.TestBase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

public class HourlyApiCallsActivity extends TestBase implements OnChartValueSelectedListener {

    private Machine mTargetMachine;
    private HashMap<String, DailyApiCalls> mData;
    private String mApiName;

    protected LineChart mChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hourly_api_calls);

        Intent intent = getIntent();
        mApiName = (String)intent.getSerializableExtra("apiName");

        mData = DataContainer.getInstance().getApiCalls();

        ActionBar actionBar = getActionBar();
        actionBar.setTitle(
                (CharSequence) DataContainer.getInstance().getSelectedMachine().getName()
        );

        TextView apiNameView = (TextView) findViewById(R.id.apiName);
        apiNameView.setText(mApiName);
        apiNameView.setTypeface(Typeface.DEFAULT);

        createChart();
    }

    @Override
    public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {

    }

    @Override
    public void onNothingSelected() {

    }

    private void createChart() {
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

        setChartData();

        mChart.animateX(2500);

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

    private void setChartData() {

        ArrayList<LineDataSet> dataSets = new ArrayList<LineDataSet>();
        ArrayList<String> xVals = new ArrayList<String>();
        for (int i = 0; i < 24; i++) {
            xVals.add((i) + "시");
        }

        DailyApiCalls temp;
        Set<String> keySet = DataContainer.getInstance().getApiCalls().keySet();
        ArrayList<String> dates = new ArrayList<String>(keySet);
        Collections.sort(dates);

        int endIdx = dates.indexOf(DataContainer.getInstance().getSelectedDate());
        int beginIdx = endIdx - 2;
        if (beginIdx < 0)
            beginIdx = 0;

        for (int index = beginIdx; index <= endIdx; index++) {
            String key = dates.get(index);
            int colorIdx = index - beginIdx;

            ArrayList<Entry> yVals = new ArrayList<Entry>();
            temp = DataContainer.getInstance().getApiCalls().get(key);
            for (int i = 0; i < xVals.size(); i++) {
                yVals.add(new Entry(temp.getCount(mApiName, i), i));
            }

            LineDataSet set = new LineDataSet(yVals, key);
            set.setAxisDependency(AxisDependency.LEFT);
            //set.setColor(ColorTemplate.getHoloBlue());
            set.setColor(ColorTemplate.COLORFUL_COLORS[colorIdx]);
            set.setCircleColor(Color.WHITE);
            set.setLineWidth(2f);
            set.setCircleSize(3f);
            set.setFillAlpha(65);
            set.setFillColor(ColorTemplate.COLORFUL_COLORS[colorIdx]);
            set.setHighLightColor(Color.rgb(244, 117, 117));
            set.setDrawCircleHole(false);

            dataSets.add(set);
        }

        // create a data object with the datasets
        LineData data = new LineData(xVals, dataSets);
        data.setValueTextColor(Color.WHITE);
        data.setValueTextSize(9f);

        // set data
        mChart.setData(data);
        mChart.notifyDataSetChanged();
    }
}
