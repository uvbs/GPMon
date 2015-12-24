package com.jinsung.adoda.gpmon;

import android.app.ActionBar;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.jinsung.adoda.gpmon.data.DailyApiCalls;
import com.jinsung.adoda.gpmon.data.Machine;

import java.util.HashMap;

public class HourlyApiCalls extends ActionBarActivity {

    private Machine mTargetMachine;
    private HashMap<String, DailyApiCalls> mData;
    private String mApiName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
    }
}
