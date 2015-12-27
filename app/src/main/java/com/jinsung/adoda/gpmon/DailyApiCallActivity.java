package com.jinsung.adoda.gpmon;

import com.jinsung.adoda.gpmon.data.ApiInfo;
import com.jinsung.adoda.gpmon.data.DailyApiCalls;
import com.jinsung.adoda.gpmon.data.DataContainer;
import com.jinsung.adoda.gpmon.data.Machine;
import com.jinsung.adoda.gpmon.fortest.TestBase;
import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.Legend.LegendPosition;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.XAxis.XAxisPosition;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.filter.Approximator;
import com.github.mikephil.charting.data.filter.Approximator.ApproximatorType;

import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.highlight.Highlight;

import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.jinsung.adoda.gpmon.utils.DateUtil;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;

import cz.msebera.android.httpclient.Header;

public class DailyApiCallActivity extends TestBase implements OnChartValueSelectedListener {

    protected HorizontalBarChart mChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_daily_api_call);

        getActionBar().setTitle(
                DataContainer.getInstance().getSelectedMachine().getName()
        );

        CreateChart();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // 기존에는 onCreate에서 했었지만, onResume으로 옮긴다.
        // request total api list
        DataContainer.getInstance().requestAllApis(DailyApiCallActivity.this, new GetAllApisResponse());
    }

    @Override
    public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {
        if (e == null)
            return;

        RectF bounds = mChart.getBarBounds((BarEntry) e);
        PointF position = mChart.getPosition(e, mChart.getData().getDataSetByIndex(dataSetIndex)
                .getAxisDependency());

        Log.i("bounds", bounds.toString());
        Log.i("position", position.toString());

        BarEntry barEntry = (BarEntry)e;
        String apiName = mChart.getXValue(barEntry.getXIndex());
        Toast.makeText(DailyApiCallActivity.this, apiName, Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(getApplicationContext(), HourlyApiCallsActivity.class);
        intent.putExtra("data", DataContainer.getInstance().getApiCalls());
        intent.putExtra("apiName", apiName);

        startActivity(intent);
    }

    @Override
    public void onNothingSelected() { }

    private void CreateChart () {
        mChart = (HorizontalBarChart) findViewById(R.id.chart1);
        mChart.setOnChartValueSelectedListener(this);

        mChart.setDrawGridBackground(false);
        mChart.setDrawBarShadow(false);
        mChart.setDrawValueAboveBar(true);
        mChart.setDescription("Daily Number Of API Call");

        // if more than 60 entries are displayed in the chart, no values will be drawn
        mChart.setMaxVisibleValueCount(50);
        // scaling can now only be done on x- and y-axis separately
        mChart.setPinchZoom(false);

        XAxis xl = mChart.getXAxis();
        xl.setPosition(XAxisPosition.BOTTOM);
        xl.setTypeface(Typeface.DEFAULT);
        xl.setDrawAxisLine(true);
        xl.setDrawGridLines(true);
        xl.setGridLineWidth(0.3f);

        YAxis yl = mChart.getAxisLeft();
        yl.setTypeface(Typeface.DEFAULT);
        yl.setDrawAxisLine(true);
        yl.setDrawGridLines(true);
        yl.setGridLineWidth(0.3f);

        YAxis yr = mChart.getAxisRight();
        yr.setTypeface(Typeface.DEFAULT);
        yr.setDrawAxisLine(true);
        yr.setDrawGridLines(false);

        mChart.animateY(2500);

        Legend l = mChart.getLegend();
        l.setPosition(LegendPosition.BELOW_CHART_LEFT);
        l.setFormSize(8f);
        l.setXEntrySpace(4f);
    }

    public class GetAllApisResponse implements DataContainer.IResponseInterface {

        ProgressDialog dialog;

        @Override
        public void onStart() {
            dialog = new ProgressDialog(DailyApiCallActivity.this);
            dialog.setMessage(getString(R.string.dlgtext_waiting));
            dialog.setCancelable(false);
            dialog.show();
        }

        @Override
        public void onFailure(int stateCode, Header[] header, byte[] body, Throwable error) {
            String errMsg = "State Code :" + stateCode + "\n";
            errMsg += "Error Message :" + error.getMessage();
            Toast.makeText(DailyApiCallActivity.this, errMsg, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onSuccess(int stateCode, Header[] header, byte[] body) {
            DataContainer.getInstance().requestApiCalls(
                    DailyApiCallActivity.this,
                    new GetApiCallsResponse()
            );
        }

        @Override
        public void onFinish() {
            dialog.dismiss();
            dialog = null;
        }
    }

    public class GetApiCallsResponse implements  DataContainer.IResponseInterface {

        ProgressDialog dialog;

        @Override
        public void onStart() {
            dialog = new ProgressDialog(DailyApiCallActivity.this);
            dialog.setMessage(getString(R.string.dlgtext_waiting));
            dialog.setCancelable(false);
            dialog.show();
        }

        @Override
        public void onFailure(int stateCode, Header[] header, byte[] body, Throwable error) {
            String errMsg = "State Code :" + stateCode + "\n";
            errMsg += "Error Message :" + error.getMessage();
            Toast.makeText(DailyApiCallActivity.this, errMsg, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onSuccess(int stateCode, Header[] header, byte[] body) {

            //String key = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
            TextView dateView = (TextView) findViewById(R.id.date);
            dateView.setText(DataContainer.getInstance().getSelectedDate());

            ArrayList<BarEntry> yVals = new ArrayList<BarEntry>();
            ArrayList<String> xVals = new ArrayList<String>();

            DailyApiCalls temp = DataContainer.getInstance().getDailyApiCalls();
            int numOfApi = temp.getNumOfApi();
            for (int i = 0; i < numOfApi; i++) {
                String apiName = temp.getApiName(i);
                xVals.add(apiName);
                yVals.add(new BarEntry(temp.getTotalCount(apiName), i));
            }

            BarDataSet set1 = new BarDataSet(yVals, "Total Api Request Count");

            ArrayList<BarDataSet> dataSets = new ArrayList<BarDataSet>();
            dataSets.add(set1);

            BarData data = new BarData(xVals, dataSets);
            data.setValueTextSize(10f);
            data.setValueTypeface(Typeface.DEFAULT);

            mChart.setData(data);
        }

        @Override
        public void onFinish() {
            dialog.dismiss();
            dialog = null;
        }
    }



}
