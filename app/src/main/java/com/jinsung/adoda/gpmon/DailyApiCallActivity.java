package com.jinsung.adoda.gpmon;

import com.jinsung.adoda.gpmon.data.ApiInfo;
import com.jinsung.adoda.gpmon.data.DailyApiCalls;
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
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import cz.msebera.android.httpclient.Header;

public class DailyApiCallActivity extends TestBase implements OnChartValueSelectedListener {

    final String URL = "http://gpmon-adodajs.rhcloud.com/apis/";
    private AsyncHttpClient mClient;
    private GetApiCallsResponse mResponse;
    private Machine mTargetMachine;
    private HashMap<String, DailyApiCalls> mData;

    protected HorizontalBarChart mChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_daily_api_call);

        Intent intent = getIntent();
        mTargetMachine = (Machine)intent.getSerializableExtra("targetMachine");

        mClient = new AsyncHttpClient();
        mResponse = new GetApiCallsResponse();
        mData = new HashMap<String, DailyApiCalls>();

        String requestUrl = URL + mTargetMachine.getName() + ".json";
        Log.v("requestUrl", requestUrl);
        mClient.get(requestUrl, mResponse);

        ActionBar actionBar = getActionBar();
        actionBar.setTitle(mTargetMachine.getName());

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

        Intent intent = new Intent(getApplicationContext(), HourlyApiCalls.class);
        intent.putExtra("targetMachine", mTargetMachine);
        intent.putExtra("data", mData);
        intent.putExtra("apiName", apiName);

        startActivity(intent);
    }

    @Override
    public void onNothingSelected() {

    }

    public class GetApiCallsResponse extends AsyncHttpResponseHandler {

        ProgressDialog dialog;

        @Override
        public void onStart() {
            dialog = new ProgressDialog(DailyApiCallActivity.this);
            dialog.setMessage("잠시만 기다려주세요...");
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
            try {
                //통신 결과를 문자열로 변환한다.
                String response = new String(body, "UTF-8");

                //문자열을 JSONArray로 변환한다.
                JSONArray jsonArray = new JSONArray(response);

                DailyApiCalls dailyApiCalls;
                ArrayList apiInfolist;

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject obj = jsonArray.getJSONObject(i);
                    dailyApiCalls = new DailyApiCalls();

                    String date = obj.getString("date");
                    JSONArray results = obj.getJSONArray("results");
                    for (int j = 0; j < results.length(); j++) {
                        JSONObject result = results.getJSONObject(j);


                        int hour = result.getInt("time");
                        JSONArray apis = result.getJSONArray("apis");
                        apiInfolist = new ArrayList();

                        for (int k = 0; k < apis.length(); k++) {
                            JSONObject api = apis.getJSONObject(k);


                            String apiName = api.getString("name");
                            int requestCount = api.getInt("count");

                            apiInfolist.add(new ApiInfo(apiName, requestCount));
                        }

                        dailyApiCalls.addItem(hour, apiInfolist);
                    }

                    mData.put(date, dailyApiCalls);
                }

                String key = "2015-12-23";
                //String key = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
                TextView dateView = (TextView) findViewById(R.id.date);
                dateView.setText(key);

                ArrayList<BarEntry> yVals = new ArrayList<BarEntry>();
                ArrayList<String> xVals = new ArrayList<String>();

                DailyApiCalls temp = mData.get(key);
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

            } catch (JSONException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // 성공, 실패 여부에 상관 없이 통신이 종료되면 실행.
        @Override
        public void onFinish() {
            dialog.dismiss();
            dialog = null;
        }
    }
}
